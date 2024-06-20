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

import com.influxdb.client.domain.Bucket;
import com.influxdb.client.domain.DBRPCreate;
import com.influxdb.client.domain.InfluxQLQuery;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.service.DbrPsService;
import com.influxdb.client.write.Point;
import com.influxdb.query.InfluxQLQueryResult;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.ListAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
	void testQueryData() {
		InfluxQLQueryResult result = influxQLQueryApi.query(new InfluxQLQuery("SELECT FIRST(\"free\") FROM \"influxql\"", DATABASE_NAME));
		assertSingleSeriesRecords(result)
				.hasSize(1)
				.first()
				.satisfies(record -> {
//					Assertions.assertThat(record.getValueByKey("time")).isEqualTo("1655900000000000000");
					Assertions.assertThat(record.getValueByKey("time")).isEqualTo("2022-06-22T12:13:20Z");
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
					// Assertions.assertThat(record.getValueByKey("time")).isEqualTo("1655900000000000000");
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
				Assertions.assertThat(record.getValueByKey("host")).isNull();
				Assertions.assertThat(record.getValueByKey("time")).isEqualTo("2022-06-22T12:13:20Z");
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
}
