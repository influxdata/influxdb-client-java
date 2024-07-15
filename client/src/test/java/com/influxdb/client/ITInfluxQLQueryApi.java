/*
 * The MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.influxdb.client;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.influxdb.client.domain.Bucket;
import com.influxdb.client.domain.DBRP;
import com.influxdb.client.domain.DBRPCreate;
import com.influxdb.client.domain.InfluxQLQuery;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.service.DbrPsService;
import com.influxdb.client.write.Point;
import com.influxdb.query.InfluxQLQueryResult;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ListAssert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import retrofit2.Response;

import static org.assertj.core.api.InstanceOfAssertFactories.BIG_DECIMAL;
import static org.assertj.core.api.InstanceOfAssertFactories.INSTANT;
import static org.assertj.core.api.InstanceOfAssertFactories.list;

class ITInfluxQLQueryApi extends AbstractITClientTest {

	private static final String DATABASE_NAME = "my-database";
	private InfluxQLQueryApi influxQLQueryApi;


	@BeforeEach
	void setUp() throws IOException {
		influxQLQueryApi = influxDBClient.getInfluxQLQueryApi();
		DbrPsService dbrPsService = influxDBClient.getService(DbrPsService.class);

		Bucket bucket = influxDBClient.getBucketsApi().findBucketByName("my-bucket");
		Assertions.assertThat(bucket).isNotNull();

		dbrPsService.postDBRP(new DBRPCreate()
								.bucketID(bucket.getId())
								.orgID(bucket.getOrgID())
								.database(DATABASE_NAME)
								._default(true)
								.retentionPolicy("autogen")
						, null)
				.execute();
		influxDBClient.getWriteApiBlocking()
				.writePoint(bucket.getId(), bucket.getOrgID(), new Point("influxql")
						.time(1655900000, WritePrecision.S)
						.addField("free", 10)
						.addTag("host", "A")
						.addTag("region", "west"));
	}

	@Test
	void testShowDatabases() {
		InfluxQLQueryResult result = influxQLQueryApi.query(new InfluxQLQuery("SHOW DATABASES", DATABASE_NAME));
		assertSingleSeriesRecords(result)
				.map(record -> record.getValueByKey("name"))
				// internal buckets are also available by DBRP mapping
				.contains(DATABASE_NAME);
	}

	@Test
	void testShowDatabasesCSV() {
		InfluxQLQueryResult result = influxQLQueryApi.query(
			new InfluxQLQuery("SHOW DATABASES", DATABASE_NAME, InfluxQLQuery.AcceptHeader.CSV));
		assertSingleSeriesRecords(result)
			.map(record -> record.getValueByKey("name"))
			// internal buckets are also available by DBRP mapping
			.contains(DATABASE_NAME);
	}

	@Test
	void testQueryData() {
		InfluxQLQueryResult result = influxQLQueryApi.query(new InfluxQLQuery("SELECT FIRST(\"free\") FROM \"influxql\"", DATABASE_NAME));
		assertSingleSeriesRecords(result)
				.hasSize(1)
				.first()
				.satisfies(record -> {
					Assertions.assertThat(record.getValueByKey("time")).isEqualTo("1655900000000000000");
//					Assertions.assertThat(record.getValueByKey("time")).isEqualTo("2022-06-22T12:13:20Z");
					Assertions.assertThat(record.getValueByKey("first")).isEqualTo("10");
				});
	}

	@Test
	void testQueryDataWithConversion() {
		InfluxQLQueryResult result = influxQLQueryApi.query(
				new InfluxQLQuery("SELECT FIRST(\"free\") FROM \"influxql\"", DATABASE_NAME)
						.setPrecision(InfluxQLQuery.InfluxQLPrecision.SECONDS),
				(columnName, rawValue, resultIndex, seriesName) -> {
					switch (columnName) {
						case "time":
							return Instant.ofEpochSecond(Long.parseLong(rawValue));
						case "first":
							return new BigDecimal(rawValue);
						default:
							throw new IllegalArgumentException("unexpected column " + columnName);
					}
				}
		);
		assertSingleSeriesRecords(result)
				.hasSize(1)
				.first()
				.satisfies(record -> {
					Assertions.assertThat(record.getValueByKey("time")).asInstanceOf(INSTANT).isEqualTo("2022-06-22T12:13:20Z");
					Assertions.assertThat(record.getValueByKey("first")).asInstanceOf(BIG_DECIMAL).isEqualTo("10");
				});
	}

	@Test
	void testSelectAll() {
		InfluxQLQueryResult result = influxQLQueryApi.query(new InfluxQLQuery("SELECT * FROM \"influxql\"", DATABASE_NAME));
		assertSingleSeriesRecords(result)
				.hasSize(1)
				.first()
				.satisfies(record -> {
					Assertions.assertThat(record.getValueByKey("time")).isEqualTo("1655900000000000000");
					// Assertions.assertThat(record.getValueByKey("time")).isEqualTo("2022-06-22T12:13:20Z");
					Assertions.assertThat(record.getValueByKey("free")).isEqualTo("10");
					Assertions.assertThat(record.getValueByKey("host")).isEqualTo("A");
					Assertions.assertThat(record.getValueByKey("region")).isEqualTo("west");
				});
	}

	@Test
	void testSelectAllJSON() {
		InfluxQLQueryResult result = influxQLQueryApi.query(
			new InfluxQLQuery("SELECT * FROM \"influxql\"", DATABASE_NAME, InfluxQLQuery.AcceptHeader.JSON)
		);
		assertSingleSeriesRecords(result)
			.hasSize(1)
			.first()
			.satisfies(record -> {
				//Assertions.assertThat(record.getValueByKey("time")).isEqualTo("1655900000000000000");
				Assertions.assertThat(record.getValueByKey("time")).isEqualTo("2022-06-22T12:13:20Z");
				Assertions.assertThat(record.getValueByKey("free")).isEqualTo("10");
				Assertions.assertThat(record.getValueByKey("host")).isEqualTo("A");
				Assertions.assertThat(record.getValueByKey("region")).isEqualTo("west");
			});
	}

	@Test
	public void testSelectGroupBy(){
		InfluxQLQueryResult result = influxQLQueryApi.query(
			new InfluxQLQuery("SELECT * FROM \"influxql\" GROUP By \"region\",\"host\"", DATABASE_NAME)
		);

		assertSingleSeriesRecords(result)
			.hasSize(1)
			.first()
			.satisfies(record -> {
				Assertions.assertThat(record.getValueByKey("region")).isNull();
				Assertions.assertThat(record.getValueByKey("time")).isEqualTo("1655900000000000000");
				Assertions.assertThat(record.getValueByKey("host")).isNull();
				// Assertions.assertThat(record.getValueByKey("time")).isEqualTo("2022-06-22T12:13:20Z");
				Assertions.assertThat(record.getValueByKey("free")).isEqualTo("10");
			});

		Assertions.assertThat(result)
			.extracting(InfluxQLQueryResult::getResults, list(InfluxQLQueryResult.Result.class))
			.hasSize(1)
			.first()
				.extracting(InfluxQLQueryResult.Result::getSeries, list(InfluxQLQueryResult.Series.class))
			  .hasSize(1)
				.first()
				  .extracting(InfluxQLQueryResult.Series::getTags)
					.satisfies(tagz -> {
					  Assertions.assertThat(tagz).isNotNull();
						Assertions.assertThat(tagz.get("host")).isEqualTo("A");
						Assertions.assertThat(tagz.get("region")).isEqualTo("west");
					});
	}

	@Test
	void testInfluxDB18() {
		// create database
		String db = "testing_database";
		influxDBQuery("CREATE DATABASE " + db, db);

		// connect to InfluxDB 1.8
		influxDBClient.close();
		influxDBClient = InfluxDBClientFactory.createV1(getInfluxDbUrl(), "username", "password".toCharArray(),
				db, "autogen");
		influxQLQueryApi = influxDBClient.getInfluxQLQueryApi();

		// test query to InfluxDB 1.8
		InfluxQLQueryResult result = influxQLQueryApi.query(new InfluxQLQuery("SHOW DATABASES", db));
		assertSingleSeriesRecords(result)
				.map(record -> record.getValueByKey("name"))
				.contains(db);


		// drop database
		influxDBQuery("DROP DATABASE " + db, db);
	}

	private ListAssert<InfluxQLQueryResult.Series.Record> assertSingleSeriesRecords(InfluxQLQueryResult result) {
		return Assertions.assertThat(result)
				.extracting(InfluxQLQueryResult::getResults, list(InfluxQLQueryResult.Result.class))
				.hasSize(1)
				.first()
				.extracting(InfluxQLQueryResult.Result::getSeries, list(InfluxQLQueryResult.Series.class))
				.hasSize(1)
				.first()
				.extracting(InfluxQLQueryResult.Series::getValues, list(InfluxQLQueryResult.Series.Record.class));
	}

  @Nested
  class ServiceHeaderTest {

		protected MockWebServer mockServer = new MockWebServer();

		@BeforeEach
		void setUp() throws IOException {
			mockServer.start();
		}

		@AfterEach
		void tearDown() throws IOException {
			mockServer.shutdown();
		}

		@Test
		public void serviceHeaderCSV() throws InterruptedException {
			mockServer.enqueue(new MockResponse().setResponseCode(200).setBody("a,b,c,d,e,f"));
			InfluxDBClient client = InfluxDBClientFactory.create(
				mockServer.url("/").toString(),
				"my_token".toCharArray(),
				"my_org",
				"my_bucket"
			);

			InfluxQLQueryApi influxQuery = client.getInfluxQLQueryApi();
			InfluxQLQueryResult result = influxQuery.query(new InfluxQLQuery("SELECT * FROM cpu", "test_db", InfluxQLQuery.AcceptHeader.CSV));
			Assertions.assertThat(result.getResults()).hasSize(1);

			RecordedRequest request = mockServer.takeRequest();
			Assertions.assertThat(request.getHeader("Authorization")).isEqualTo("Token my_token");
			Assertions.assertThat(request.getHeader("Accept")).isEqualTo("application/csv");
		}


		@Test
		public void serviceHeaderJSON() throws InterruptedException {
			mockServer.enqueue(new MockResponse().setResponseCode(200).setBody("{results:[]}"));
			InfluxDBClient client = InfluxDBClientFactory.create(
				mockServer.url("/").toString(),
				"my_token".toCharArray(),
				"my_org",
				"my_bucket"
			);

			InfluxQLQueryApi influxQuery = client.getInfluxQLQueryApi();
			InfluxQLQueryResult result = influxQuery.query(new InfluxQLQuery("SELECT * FROM cpu", "test_db",
				InfluxQLQuery.AcceptHeader.JSON));
			Assertions.assertThat(result.getResults()).hasSize(0);

			RecordedRequest request = mockServer.takeRequest();
			Assertions.assertThat(request.getHeader("Authorization")).isEqualTo("Token my_token");
			Assertions.assertThat(request.getHeader("Accept")).isEqualTo("application/json");
		}

		@Test
		public void serviceHeaderDefault() throws InterruptedException {
			mockServer.enqueue(new MockResponse().setResponseCode(200).setBody("{results:[]}"));
			InfluxDBClient client = InfluxDBClientFactory.create(
				mockServer.url("/").toString(),
				"my_token".toCharArray(),
				"my_org",
				"my_bucket"
			);

			InfluxQLQueryApi influxQuery = client.getInfluxQLQueryApi();
			InfluxQLQueryResult result = influxQuery.query(new InfluxQLQuery("SELECT * FROM cpu", "test_db"));
			RecordedRequest request = mockServer.takeRequest();
			Assertions.assertThat(request.getHeader("Authorization")).isEqualTo("Token my_token");
			Assertions.assertThat(request.getHeader("Accept")).isEqualTo("application/csv");
		}

		@Test
		public void serviceHeaderMethodQueryCSV() throws InterruptedException {
			mockServer.enqueue(new MockResponse().setResponseCode(200).setBody("a,b,c,d,e,f"));
			InfluxDBClient client = InfluxDBClientFactory.create(
				mockServer.url("/").toString(),
				"my_token".toCharArray(),
				"my_org",
				"my_bucket"
			);

			InfluxQLQueryApi influxQuery = client.getInfluxQLQueryApi();
			InfluxQLQueryResult result = influxQuery.queryCSV(
				new InfluxQLQuery("SELECT * FROM cpu", "test_db"));
			Assertions.assertThat(result.getResults()).hasSize(1);
			RecordedRequest request = mockServer.takeRequest();
			Assertions.assertThat(request.getHeader("Authorization")).isEqualTo("Token my_token");
			Assertions.assertThat(request.getHeader("Accept")).isEqualTo("application/csv");
		}

		@Test
		public void serverHeaderMethodQueryCSVExtractor(){
			mockServer.enqueue(new MockResponse().setResponseCode(200).setBody("a,tags,c,d,e\n\"mem\",\"foo=bar\",2,3,4"));
			InfluxDBClient client = InfluxDBClientFactory.create(
				mockServer.url("/").toString(),
				"my_token".toCharArray(),
				"my_org",
				"my_bucket"
			);
			InfluxQLQueryApi influxQuery = client.getInfluxQLQueryApi();
			InfluxQLQueryResult result = influxQuery.queryCSV(
				new InfluxQLQuery("SELECT * FROM cpu", "test_db"),
				(columnName, rawValue, resultIndex, seriesName) -> {
					switch(columnName) {
						case "c":
							return Long.valueOf(rawValue);
						case "d":
							return Double.valueOf(rawValue);
					}
					return rawValue;
				});
			InfluxQLQueryResult.Series series = result.getResults().get(0).getSeries().get(0);
			Assertions.assertThat(series.getName()).isEqualTo("mem");
			Assertions.assertThat(series.getTags().get("foo")).isEqualTo("bar");
			Assertions.assertThat(series.getColumns().get("c")).isEqualTo(0);
			Assertions.assertThat(series.getColumns().get("d")).isEqualTo(1);
			Assertions.assertThat(series.getColumns().get("e")).isEqualTo(2);
			Assertions.assertThat(series.getValues().get(0).getValueByKey("c")).isEqualTo(2L);
			Assertions.assertThat(series.getValues().get(0).getValueByKey("d")).isEqualTo(3.0);
			Assertions.assertThat(series.getValues().get(0).getValueByKey("e")).isEqualTo("4");
		}

		@Test
		public void serviceHeaderMethodQueryJSON() throws InterruptedException {
			mockServer.enqueue(new MockResponse().setResponseCode(200).setBody("{results:[]}"));
			InfluxDBClient client = InfluxDBClientFactory.create(
				mockServer.url("/").toString(),
				"my_token".toCharArray(),
				"my_org",
				"my_bucket"
			);

			InfluxQLQueryApi influxQuery = client.getInfluxQLQueryApi();
			InfluxQLQueryResult result = influxQuery.queryJSON(new InfluxQLQuery("SELECT * FROM cpu", "test_db"));
			Assertions.assertThat(result.getResults()).hasSize(0);
			RecordedRequest request = mockServer.takeRequest();
			Assertions.assertThat(request.getHeader("Authorization")).isEqualTo("Token my_token");
			Assertions.assertThat(request.getHeader("Accept")).isEqualTo("application/json");
		}

		@Test
		public void serviceHeaderMethodQueryJSONExtractor() throws InterruptedException {
			mockServer.enqueue(new MockResponse().setResponseCode(200).setBody("{\"results\":[{\"statement_id\":0," +
				"\"series\":[{\"name\":\"mem\",\"tags\": { \"foo\":\"bar\"},\"columns\": [\"c\",\"d\",\"e\"]," +
				"\"values\":[[2,3,4]]}]}]}"));
			InfluxDBClient client = InfluxDBClientFactory.create(
				mockServer.url("/").toString(),
				"my_token".toCharArray(),
				"my_org",
				"my_bucket"
			);
			InfluxQLQueryApi influxQuery = client.getInfluxQLQueryApi();
			InfluxQLQueryResult result = influxQuery.queryJSON
				(new InfluxQLQuery("SELECT * FROM cpu", "test_db"),
					(columnName, rawValue, resultIndex, seriesName) -> {
						switch(columnName) {
							case "c":
								return Long.valueOf(rawValue);
							case "d":
								return Double.valueOf(rawValue);
						}
						return rawValue;
					});
			InfluxQLQueryResult.Series series = result.getResults().get(0).getSeries().get(0);
			Assertions.assertThat(series.getName()).isEqualTo("mem");
			Assertions.assertThat(series.getTags().get("foo")).isEqualTo("bar");
			Assertions.assertThat(series.getColumns().get("c")).isEqualTo(0);
			Assertions.assertThat(series.getColumns().get("d")).isEqualTo(1);
			Assertions.assertThat(series.getColumns().get("e")).isEqualTo(2);
			Assertions.assertThat(series.getValues().get(0).getValueByKey("c")).isEqualTo(2L);
			Assertions.assertThat(series.getValues().get(0).getValueByKey("d")).isEqualTo(3.0);
			Assertions.assertThat(series.getValues().get(0).getValueByKey("e")).isEqualTo("4");
		}

		@Test
		public void serviceHeaderMethodQueryCSVPrecedent() throws InterruptedException {
			mockServer.enqueue(new MockResponse().setResponseCode(200).setBody("a,b,c,d,e,f"));
			InfluxDBClient client = InfluxDBClientFactory.create(
				mockServer.url("/").toString(),
				"my_token".toCharArray(),
				"my_org",
				"my_bucket"
			);
			InfluxQLQueryApi influxQuery = client.getInfluxQLQueryApi();
			InfluxQLQueryResult result = influxQuery.queryCSV(
				new InfluxQLQuery("SELECT * FROM cpu", "test_db", InfluxQLQuery.AcceptHeader.JSON));
			Assertions.assertThat(result.getResults()).hasSize(1);
			RecordedRequest request = mockServer.takeRequest();
			Assertions.assertThat(request.getHeader("Authorization")).isEqualTo("Token my_token");
			Assertions.assertThat(request.getHeader("Accept")).isEqualTo("application/csv");
		}

		@Test
		public void serviceHeaderMethodQueryJSONPrecedent() throws InterruptedException {
			mockServer.enqueue(new MockResponse().setResponseCode(200).setBody("{results:[]}"));
			InfluxDBClient client = InfluxDBClientFactory.create(
				mockServer.url("/").toString(),
				"my_token".toCharArray(),
				"my_org",
				"my_bucket"
			);
			InfluxQLQueryApi influxQuery = client.getInfluxQLQueryApi();
			InfluxQLQueryResult result = influxQuery.queryJSON(
				new InfluxQLQuery("SELECT * FROM cpu", "test_db", InfluxQLQuery.AcceptHeader.CSV));
			Assertions.assertThat(result.getResults()).hasSize(0);
			RecordedRequest request = mockServer.takeRequest();
			Assertions.assertThat(request.getHeader("Authorization")).isEqualTo("Token my_token");
			Assertions.assertThat(request.getHeader("Accept")).isEqualTo("application/json");
		}
	}

	@Test
	public void testQueryJsonPrecision(){
		Bucket bucket = influxDBClient.getBucketsApi().findBucketByName("my-bucket");
		int idx = 0;
		Map<String,Instant> precisionValues = new HashMap<>();
	  for(WritePrecision precision : WritePrecision.values()){
			 Instant time = Instant.now().minusSeconds(10 * (1 + idx++));
			 long nanoTimestamp = (time.getEpochSecond() * 1_000_000_000L) + time.getNano();

			 long timestamp = 0;
			 switch(precision){
				 case S:
					 timestamp = nanoTimestamp/1_000_000_000L;
					 precisionValues.put(precision.getValue(), Instant.ofEpochSecond(timestamp));
           break;
				 case MS:
					 timestamp = nanoTimestamp/1_000_000L;
					 precisionValues.put(precision.getValue(), Instant.ofEpochMilli(timestamp));
					 break;
				 case US:
					 timestamp = nanoTimestamp/1_000L;
					 precisionValues.put(precision.getValue(),
						 Instant.ofEpochSecond(timestamp/1_000_000L, (timestamp%1_000_000L) * 1000));
					 break;
				 case NS:
					 timestamp = nanoTimestamp;
					 precisionValues.put(precision.getValue(),
						 Instant.ofEpochSecond(timestamp/1_000_000_000L, timestamp%1_000_000_000L));
					 break;
			 }
		   influxDBClient.getWriteApiBlocking()
			  .writePoint(bucket.getId(), bucket.getOrgID(), new Point("precise")
				  .time(timestamp, precision)
				  .addField("cpu_usage", 10.42)
				  .addTag("domain", precision.toString()));
		}
    assert bucket != null;
    InfluxQLQueryResult result = influxDBClient.getInfluxQLQueryApi()
			.queryJSON(new InfluxQLQuery(
				"SELECT * FROM precise WHERE time > now() - 1m",
				bucket.getName()));

		for(InfluxQLQueryResult.Result r: result.getResults()){
			InfluxQLQueryResult.Series s = r.getSeries().get(0);
			for(InfluxQLQueryResult.Series.Record record: s.getValues()){
				String domain = Objects.requireNonNull(record.getValueByKey("domain")).toString();
				Assertions.assertThat(precisionValues.get(domain))
					.isEqualTo(Instant.parse(
						Objects.requireNonNull(record.getValueByKey("time")
						).toString()));
			}
		}
	}

	@Test
	public void testEmptyResultsResponse() {

		try(InfluxDBClient localClient = InfluxDBClientFactory.create(influxDB_URL, "my-token".toCharArray())) {
			InfluxQLQueryResult result = localClient.getInfluxQLQueryApi().query(
				new InfluxQLQuery("SHOW FIELD KEYS", "inexistant", InfluxQLQuery.AcceptHeader.CSV));

			Assertions.assertThat(result.getResults()).hasSize(0);
		}
	}
}
