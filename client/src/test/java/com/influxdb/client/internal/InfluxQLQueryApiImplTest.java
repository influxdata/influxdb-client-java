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
package com.influxdb.client.internal;

import java.io.IOException;
import java.io.StringReader;
import java.time.Instant;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.influxdb.Cancellable;
import com.influxdb.query.InfluxQLQueryResult;
import org.assertj.core.api.Assertions;
import org.junit.Ignore;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;

class InfluxQLQueryApiImplTest {

	private static final Cancellable NO_CANCELLING = new Cancellable() {
		@Override
		public void cancel() {
		}

		@Override
		public boolean isCancelled() {
			return false;
		}
	};

	private static Map<String, String> mapOf(@Nonnull final String... valuePairs) {
		Map<String, String> map = new HashMap<>();
		if (valuePairs.length % 2 != 0) {
			throw new IllegalArgumentException("value pairs must be even");
		}
		for (int i = 0; i < valuePairs.length; i += 2) {
			map.put(valuePairs[i], valuePairs[i + 1]);
		}
		return map;
	}

	private static void assertParsedTags(
		@Nonnull final String rawTags,
		@Nonnull final Map<String, String> expectedTags
	) throws IOException {

		StringReader reader = new StringReader(
			"name,tags,time,value\n" +
				"m,\"" + rawTags + "\",1,1\n"
		);
		InfluxQLQueryResult result = InfluxQLQueryApiImpl.readInfluxQLCSVResult(reader, NO_CANCELLING, null);

		Assertions.assertThat(result.getResults()).hasSize(1);
		Assertions.assertThat(result.getResults().get(0).getSeries()).hasSize(1);
		Assertions.assertThat(result.getResults().get(0).getSeries().get(0).getTags()).isEqualTo(expectedTags);
	}

	@Test
	void readInfluxQLResultWithMalformedAndBoundaryTagCases() throws IOException {
		assertParsedTags("", mapOf());
		assertParsedTags("host=", mapOf("host", ""));
		assertParsedTags("host=a,host=b", mapOf("host", "b"));
		assertParsedTags("host=a,broken", mapOf("host", "a"));
		assertParsedTags("=a,host=b", mapOf("host", "b"));
		assertParsedTags("host=a,", mapOf("host", "a"));
		assertParsedTags(",host=a", mapOf(",host", "a"));
		assertParsedTags("a=1,,b=2", mapOf("a", "1", ",b", "2"));
		assertParsedTags("a=foo\\", mapOf("a", "foo"));
		assertParsedTags("k\\==v\\=1", mapOf("k=", "v=1"));
		assertParsedTags("k\\,x=v\\,y,b=2", mapOf("k,x", "v,y", "b", "2"));
		assertParsedTags("k\\=x", mapOf());
	}

	@Test
	void readInfluxQLResultWithTagCommas() throws IOException {
		InfluxQLQueryResult.Series.ValueExtractor extractValue = (columnName, rawValue, resultIndex, seriesName) -> {
			if (resultIndex == 0 && seriesName.equals("data1")){
				switch (columnName){
					case "time": return Instant.ofEpochSecond(Long.parseLong(rawValue));
					case "first":
						return Double.valueOf(rawValue);
				}
			}
			return rawValue;
		};

		// Note that escapes in tags returned from server are themselves escaped
		List<String> testTags = Arrays.asList(
			"location=Cheb_CZ", //simpleTag
			"region=us-east-1,host=server1", // standardTags * 2
			"location=Cheb\\,\\ CZ", // simpleTag with value comma and space
			"location=Cheb_CZ,branch=Munchen_DE", // multiple tags with underscore
			"location=Cheb\\,\\ CZ,branch=Munchen\\,\\ DE", // multiple tags with comma and space
			"model\\,\\ uin=C3PO", // tag with comma space in key
			"model\\,\\ uin=Droid\\,\\ C3PO", // tag with comma space in key and value
			"model\\,\\ uin=Droid\\,\\ C3PO,location=Cheb\\,\\ CZ,branch=Munchen\\,\\ DE", // comma space in key and val
			"silly\\,\\=long\\,tag=a\\,b\\\\\\,\\ c\\,\\ d", // multi commas in k and v plus escaped reserved chars
			"region=us\\,\\ east-1,host\\,\\ name=ser\\,\\ ver1" // legacy broken tags
		);

		Map<String,Map<String,String>> expectedTagsMap = Stream.of(
			// 1. simpleTag
			new AbstractMap.SimpleImmutableEntry<>(testTags.get(0),
				mapOf("location", "Cheb_CZ")),
			// 2. standardTags * 2
			new AbstractMap.SimpleImmutableEntry<>(testTags.get(1),
				mapOf(
					"region", "us-east-1",
					"host", "server1"
				)),
			// 3. simpleTag with value comma and space
			new AbstractMap.SimpleImmutableEntry<>(testTags.get(2),
				mapOf("location", "Cheb, CZ")),
			// 4. multiple tags with underscore
			new AbstractMap.SimpleImmutableEntry<>(testTags.get(3),
				mapOf(
					"location", "Cheb_CZ",
					"branch", "Munchen_DE"
				)),
			// 5. multiple tags with comma and space
			new AbstractMap.SimpleImmutableEntry<>(testTags.get(4),
				mapOf(
					"location", "Cheb, CZ",
					"branch", "Munchen, DE"
				)),
			// 6. tag with comma and space in key
			new AbstractMap.SimpleImmutableEntry<>(testTags.get(5),
				mapOf("model, uin", "C3PO")),
			// 7. tag with comma and space in key and value
			new AbstractMap.SimpleImmutableEntry<>(testTags.get(6),
				mapOf("model, uin", "Droid, C3PO")),
			// 8. comma space in key and val with multiple tags
			new AbstractMap.SimpleImmutableEntry<>(testTags.get(7),
				mapOf(
					"model, uin", "Droid, C3PO",
					"location", "Cheb, CZ",
					"branch", "Munchen, DE"
				)),
			// 9. multiple commas in key and value
			new AbstractMap.SimpleImmutableEntry<>(testTags.get(8),
				mapOf(
					"silly,=long,tag", "a,b\\, c, d"
				)),
			// legacy broken tags
			new AbstractMap.SimpleImmutableEntry<>(testTags.get(9),
				mapOf(
					"region", "us, east-1",
					"host, name", "ser, ver1"
				))
		).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		StringReader reader = new StringReader("name,tags,time,first\n"
			+ "data1,\"" + testTags.get(0) + "\",1483225200,42\n"
			+ "data1,\"" + testTags.get(1) + "\",1483225200,42\n"
			+ "data1,\"" + testTags.get(2) + "\",1483225200,42\n"
			+ "data1,\"" + testTags.get(3) + "\",1483225200,42\n"
			+ "data1,\"" + testTags.get(4) + "\",1483225200,42\n"
			+ "data1,\"" + testTags.get(5) + "\",1483225200,42\n"
			+ "data1,\"" + testTags.get(6) + "\",1483225200,42\n"
			+ "data1,\"" + testTags.get(7) + "\",1483225200,42\n"
			+ "data1,\"" + testTags.get(8) + "\",1483225200,42\n"
			+ "\n"
			+ "name,tags,time,usage_user,usage_system\n"
			+ "cpu,\"" + testTags.get(9) + "\",1483225200,13.57,1.4\n"
		);

		InfluxQLQueryResult result = InfluxQLQueryApiImpl.readInfluxQLCSVResult(reader, NO_CANCELLING, extractValue);
		List<InfluxQLQueryResult.Result> results = result.getResults();
		int index = 0;
		for(InfluxQLQueryResult.Result r : results) {
			for(InfluxQLQueryResult.Series s : r.getSeries()){
				Assertions.assertThat(s.getTags()).isEqualTo(expectedTagsMap.get(testTags.get(index++)));
				if(index < 10) {
					Assertions.assertThat(s.getColumns()).containsOnlyKeys("time", "first");
					InfluxQLQueryResult.Series.Record valRec = s.getValues().get(0);
					Assertions.assertThat(valRec.getValueByKey("first")).isEqualTo(Double.valueOf("42.0"));
					Assertions.assertThat(valRec.getValueByKey("time")).isEqualTo(Instant.ofEpochSecond(1483225200L));
				} else if (index == 10) {
					Assertions.assertThat(s.getColumns()).containsOnlyKeys("time", "usage_user", "usage_system");
					InfluxQLQueryResult.Series.Record valRec = s.getValues().get(0);
					// No value extractor created for "cpu" series
					Assertions.assertThat(valRec.getValueByKey("time")).isEqualTo("1483225200");
					Assertions.assertThat(valRec.getValueByKey("usage_user")).isEqualTo("13.57");
					Assertions.assertThat(valRec.getValueByKey("usage_system")).isEqualTo("1.4");
				}
			}
		}
		Assertions.assertThat(index).isEqualTo(testTags.size());
	}

	@Test
	void readInfluxQLResult() throws IOException {
		InfluxQLQueryResult.Series.ValueExtractor extractValues = (columnName, rawValue, resultIndex, seriesName) -> {
			if (resultIndex == 0 && seriesName.equals("data2")){
				switch (columnName){
					case "time": return Instant.ofEpochSecond(Long.parseLong(rawValue));
					case "first": return Double.valueOf(rawValue);
				}
			}
			return rawValue;
		};

		StringReader reader = new StringReader("name,tags,time,first\n" +
				"data1,,1483225200,1\n" +
				"data2,,1483225200,2\n" +
				"\n" +
				"name,tags,time,first,text\n" +
				"data,,1500000000,42,foo\n" +
				"\n" +
				"name,tags,name\n" +
				"databases,,measurement-1\n" +
				"databases,,measurement-2\n" +
				"\n" +
				"name,tags,time,usage_user,usage_system\n" +
				"cpu,\"region=us-east-1,host=server1\",1483225200,13.57,1.4\n" +
				"cpu,\"region=us-east-1,host=server1\",1483225201,14.06,1.7\n" +
				"cpu,\"region=us-east-1,host=server2\",1483225200,67.91,1.3\n"
		);

		InfluxQLQueryResult result = InfluxQLQueryApiImpl.readInfluxQLCSVResult(reader, NO_CANCELLING, extractValues);

		List<InfluxQLQueryResult.Result> results = result.getResults();
		Assertions.assertThat(results).hasSize(4);
		Assertions.assertThat(results.get(0))
				.extracting(InfluxQLQueryResult.Result::getSeries)
				.satisfies(series -> {
					Assertions.assertThat(series).hasSize(2);
					Assertions.assertThat(series.get(0))
							.satisfies(series1 -> {
								Assertions.assertThat(series1.getName()).isEqualTo("data1");
								Assertions.assertThat(series1.getColumns()).containsOnlyKeys("time", "first");
								Assertions.assertThat(series1.getValues()).hasSize(1);
								InfluxQLQueryResult.Series.Record record = series1.getValues().get(0);
								Assertions.assertThat(record.getValueByKey("time")).isEqualTo("1483225200");
								Assertions.assertThat(record.getValueByKey("first")).isEqualTo("1");
							});
					Assertions.assertThat(series.get(1))
							.satisfies(series2 -> {
								Assertions.assertThat(series2.getName()).isEqualTo("data2");
								Assertions.assertThat(series2.getColumns()).containsOnlyKeys("time", "first");
								Assertions.assertThat(series2.getValues()).hasSize(1);
								InfluxQLQueryResult.Series.Record record = series2.getValues().get(0);
								Assertions.assertThat(record.getValueByKey("time")).isEqualTo(Instant.ofEpochSecond(1483225200L));
								Assertions.assertThat(record.getValueByKey("first")).isEqualTo(2.);
							});
				});

		Assertions.assertThat(results.get(1))
				.extracting(InfluxQLQueryResult.Result::getSeries)
				.satisfies(series -> {
					Assertions.assertThat(series).hasSize(1);
					Assertions.assertThat(series.get(0))
							.satisfies(series1 -> {
								Assertions.assertThat(series1.getName()).isEqualTo("data");
								Assertions.assertThat(series1.getColumns()).containsOnlyKeys("time", "first", "text");
								Assertions.assertThat(series1.getValues()).hasSize(1);
								InfluxQLQueryResult.Series.Record record = series1.getValues().get(0);
								Assertions.assertThat(record.getValueByKey("time")).isEqualTo("1500000000");
								Assertions.assertThat(record.getValueByKey("first")).isEqualTo("42");
								Assertions.assertThat(record.getValueByKey("text")).isEqualTo("foo");
							});
				});

		Assertions.assertThat(results.get(2))
				.extracting(InfluxQLQueryResult.Result::getSeries)
				.satisfies(series -> {
					Assertions.assertThat(series).hasSize(1);
					Assertions.assertThat(series.get(0))
							.satisfies(series1 -> {
								Assertions.assertThat(series1.getName()).isEqualTo("databases");
								Assertions.assertThat(series1.getColumns()).containsOnlyKeys("name");
								Assertions.assertThat(series1.getValues()).hasSize(2);

								Assertions.assertThat( series1.getValues().get(0).getValueByKey("name"))
										.isEqualTo("measurement-1");
								Assertions.assertThat( series1.getValues().get(1).getValueByKey("name"))
										.isEqualTo("measurement-2");
							});
				});

		Assertions.assertThat(results.get(3))
				.extracting(InfluxQLQueryResult.Result::getSeries)
				.satisfies(series -> {
					Assertions.assertThat(series).hasSize(2);
					Assertions.assertThat(series.get(0))
							.satisfies(series1 -> {
								Assertions.assertThat(series1.getName()).isEqualTo("cpu");
								Assertions.assertThat(series1.getTags()).containsOnlyKeys("region", "host");
								Assertions.assertThat(series1.getTags().get("region")).isEqualTo("us-east-1");
								Assertions.assertThat(series1.getTags().get("host")).isEqualTo("server1");
								Assertions.assertThat(series1.getColumns()).containsOnlyKeys("time","usage_user","usage_system");
								Assertions.assertThat(series1.getValues()).hasSize(2);

								Assertions.assertThat( series1.getValues().get(0).getValueByKey("usage_user"))
										.isEqualTo("13.57");
								Assertions.assertThat( series1.getValues().get(0).getValueByKey("usage_system"))
										.isEqualTo("1.4");
								Assertions.assertThat( series1.getValues().get(1).getValueByKey("usage_user"))
										.isEqualTo("14.06");
								Assertions.assertThat( series1.getValues().get(1).getValueByKey("usage_system"))
										.isEqualTo("1.7");
							});
					Assertions.assertThat(series.get(1))
							.satisfies(series2 -> {
								Assertions.assertThat(series2.getName()).isEqualTo("cpu");
								Assertions.assertThat(series2.getTags()).containsOnlyKeys("region", "host");
								Assertions.assertThat(series2.getTags().get("region")).isEqualTo("us-east-1");
								Assertions.assertThat(series2.getTags().get("host")).isEqualTo("server2");
								Assertions.assertThat(series2.getColumns()).containsOnlyKeys("time","usage_user","usage_system");
								Assertions.assertThat(series2.getValues()).hasSize(1);

								Assertions.assertThat( series2.getValues().get(0).getValueByKey("usage_user"))
										.isEqualTo("67.91");
								Assertions.assertThat( series2.getValues().get(0).getValueByKey("usage_system"))
										.isEqualTo("1.3");
							});
				});
	}

	@Test
	public void readInfluxQLShowSeriesRequest() throws IOException {

		StringReader reader = new StringReader("name,tags,key\n" + //emulate SHOW SERIES response
			",,temperature\n" +
			",,\"pressure\"\n" +
			",,humid\n" +
			",,\"temperature,locale=nw002,device=rpi5_88e1\""
		);

		InfluxQLQueryResult result = InfluxQLQueryApiImpl.readInfluxQLCSVResult(reader, NO_CANCELLING,
			(columnName, rawValue, resultIndex, seriesName) -> { return rawValue;});

		Assertions.assertThat(result.getResults().get(0))
			.extracting(InfluxQLQueryResult.Result::getSeries)
			.satisfies(series -> {
				Assertions.assertThat(series).hasSize(1);
				Assertions.assertThat(series.get(0))
					.satisfies(series1 -> {
						Assertions.assertThat(series1.getName()).isEmpty();
						Assertions.assertThat(series1.getTags()).isEmpty();
						Assertions.assertThat(series1.getValues()).hasSize(4);
						Assertions.assertThat(series1.getValues())
							.satisfies(records -> {
								Assertions.assertThat(records.size()).isEqualTo(4);
								Assertions.assertThat(records.get(0).getValueByKey("key"))
									.isEqualTo("temperature");
								Assertions.assertThat(records.get(1).getValueByKey("key"))
									.isEqualTo("pressure");
								Assertions.assertThat(records.get(2).getValueByKey("key"))
									.isEqualTo("humid");
								Assertions.assertThat(records.get(3).getValueByKey("key"))
									.isEqualTo("temperature,locale=nw002,device=rpi5_88e1");
							});
					});
			});
	}

	StringReader sampleReader = new StringReader("{\n" +
		"   \"results\":\n" +
		"[\n" +
		"  {\n" +
		"   \"statement_id\": 0,\n" +
		"   \"series\": \n" +
		"       [ \n" +
		"         {\n" +
		"          \"name\": \"data1\",\n" +
		"          \"columns\": [\"time\",\"first\"],\n" +
		"          \"values\": [\n" +
		"                 [1483225200, 1]\n" +
		"             ]\n" +
		"         },\n" +
		"         {\n" +
		"          \"name\": \"data2\",\n" +
		"          \"columns\": [\"time\",\"first\"],\n" +
		"          \"values\": [\n" +
		"               [1483225200, 2]\n" +
		"             ]\n" +
		"         }\n" +
		"       ]\n" +
		"   },\n" +
		"   {\n" +
		"    \"statement_id\": 1,\n" +
		"    \"series\":\n" +
		"       [      \n" +
		"         {\n" +
		"          \"name\": \"data\",\n" +
		"          \"columns\": [\"time\",\"first\",\"text\"],\n" +
		"          \"values\": [\n" +
		"                [1500000000, 42, \"foo\"]\n" +
		"             ]\n" +
		"         }\n" +
		"       ]\n" +
		"    },\n" +
		"    {\n" +
		"     \"statement_id\": 2,\n" +
		"     \"series\":\n" +
		"       [     \n" +
		"         {\n" +
		"          \"name\": \"databases\",\n" +
		"          \"columns\" : [\"name\"],\n" +
		"          \"values\" : [\n" +
		"                [\"measurement-1\"],\n" +
		"                [\"measurement-2\"]\n" +
		"             ]\n" +
		"         }\n" +
		"       ]\n" +
		"     },\n" +
		"     {\n" +
		"      \"statement_id\": 3,\n" +
		"      \"series\": \n" +
		"        [    \n" +
		"         {\n" +
		"          \"name\": \"cpu\",\n" +
		"          \"tags\": {\"region\": \"us-east-1\", \"host\": \"server1\" },\n" +
		"          \"columns\": [\"time\", \"usage_user\", \"usage_system\"],\n" +
		"          \"values\" : [\n" +
		"                [1483225200,13.57,1.4],\n" +
		"                [1483225201,14.06,1.7]\n" +
		"             ]  \n" +
		"         },\n" +
		"         {\n" +
		"          \"name\": \"cpu\",\n" +
		"          \"tags\": {\"region\": \"us-east-1\", \"host\": \"server2\" },\n" +
		"          \"columns\": [\"time\", \"usage_user\", \"usage_system\"],\n" +
		"          \"values\" : [\n" +
		"                [1483225200,67.91,1.3]\n" +
		"             ]  \n" +
		"         }\n" +
		"       ]\n" +
		"     },\n" +
		"     {\n" +
		"        \"statement_id\": 4,\n" +
		"        \"series\":\n" +
		"          [    \n" +
		"         {\n" +
		"          \"name\": \"login\",\n" +
		"          \"tags\": {\"region\": \"eu-west-3\", \"host\": \"portal-17\"},\n" +
		"          \"columns\": [\"time\", \"user_id\", \"success\", \"stay\"],\n" +
		"          \"values\" : [\n" +
		"             [ \"2024-06-18T11:29:48.454Z\", 958772110, true, 1.27],\n" +
		"             [ \"2024-06-18T11:29:47.124Z\", 452223904, false, 0.0],\n" +
		"             [ \"2024-06-18T11:29:45.007Z\", 147178901, true, 15.5],\n" +
		"             [ \"2024-06-18T11:29:41.881Z\", 71119178, true, 78.4]\n" +
		"           ]\n" +
		"         }\n" +
		"      ] \n" +
		"   }   \n" +
		"]\n" +
		"}");

	// All values as Strings - universal default
	@Test
	public void readInfluxQLJSONResult(){
		InfluxQLQueryResult result = InfluxQLQueryApiImpl.readInfluxQLJsonResult(sampleReader, NO_CANCELLING, null);
		List<InfluxQLQueryResult.Result> results = result.getResults();
		Assertions.assertThat(results).hasSize(5);
		Assertions.assertThat(results.get(0))
			.extracting(InfluxQLQueryResult.Result::getSeries)
			.satisfies(series -> {
				Assertions.assertThat(series).hasSize(2);
				Assertions.assertThat(series.get(0))
					.satisfies(series1 -> {
						Assertions.assertThat(series1.getName()).isEqualTo("data1");
						Assertions.assertThat(series1.getColumns()).containsOnlyKeys("time", "first");
						Assertions.assertThat(series1.getValues()).hasSize(1);
						InfluxQLQueryResult.Series.Record record = series1.getValues().get(0);
						Assertions.assertThat(record.getValueByKey("time")).isEqualTo("1483225200");
						Assertions.assertThat(record.getValueByKey("first")).isEqualTo("1");
					});
				Assertions.assertThat(series.get(1))
					.satisfies(series2 -> {
						Assertions.assertThat(series2.getName()).isEqualTo("data2");
						Assertions.assertThat(series2.getColumns()).containsOnlyKeys("time", "first");
						Assertions.assertThat(series2.getValues()).hasSize(1);
						InfluxQLQueryResult.Series.Record record = series2.getValues().get(0);
						Assertions.assertThat(record.getValueByKey("time")).isEqualTo("1483225200");
						Assertions.assertThat(record.getValueByKey("first")).isEqualTo("2");
					});
			});
		Assertions.assertThat(results.get(1))
			.extracting(InfluxQLQueryResult.Result::getSeries)
			.satisfies(series -> {
				Assertions.assertThat(series).hasSize(1);
				Assertions.assertThat(series.get(0))
					.satisfies(series1 -> {
						Assertions.assertThat(series1.getName()).isEqualTo("data");
						Assertions.assertThat(series1.getColumns()).containsOnlyKeys("time", "first", "text");
						Assertions.assertThat(series1.getValues()).hasSize(1);
						InfluxQLQueryResult.Series.Record record = series1.getValues().get(0);
						Assertions.assertThat(record.getValueByKey("time")).isEqualTo("1500000000");
						Assertions.assertThat(record.getValueByKey("first")).isEqualTo("42");
						Assertions.assertThat(record.getValueByKey("text")).isEqualTo("foo");
					});
			});
		Assertions.assertThat(results.get(2))
			.extracting(InfluxQLQueryResult.Result::getSeries)
			.satisfies(series -> {
				Assertions.assertThat(series).hasSize(1);
				Assertions.assertThat(series.get(0))
					.satisfies(series1 -> {
						Assertions.assertThat(series1.getName()).isEqualTo("databases");
						Assertions.assertThat(series1.getColumns()).containsOnlyKeys("name");
						Assertions.assertThat(series1.getValues()).hasSize(2);

						Assertions.assertThat( series1.getValues().get(0).getValueByKey("name"))
							.isEqualTo("measurement-1");
						Assertions.assertThat( series1.getValues().get(1).getValueByKey("name"))
							.isEqualTo("measurement-2");
					});
			});
		Assertions.assertThat(results.get(3))
			.extracting(InfluxQLQueryResult.Result::getSeries)
			.satisfies(series -> {
				Assertions.assertThat(series).hasSize(2);
				Assertions.assertThat(series.get(0))
					.satisfies(series1 -> {
						Assertions.assertThat(series1.getName()).isEqualTo("cpu");
						Assertions.assertThat(series1.getTags()).containsOnlyKeys("region", "host");
						Assertions.assertThat(series1.getTags().get("region")).isEqualTo("us-east-1");
						Assertions.assertThat(series1.getTags().get("host")).isEqualTo("server1");
						Assertions.assertThat(series1.getColumns()).containsOnlyKeys("time","usage_user","usage_system");
						Assertions.assertThat(series1.getValues()).hasSize(2);

						Assertions.assertThat( series1.getValues().get(0).getValueByKey("usage_user"))
							.isEqualTo("13.57");
						Assertions.assertThat( series1.getValues().get(0).getValueByKey("usage_system"))
							.isEqualTo("1.4");
						Assertions.assertThat( series1.getValues().get(1).getValueByKey("usage_user"))
							.isEqualTo("14.06");
						Assertions.assertThat( series1.getValues().get(1).getValueByKey("usage_system"))
							.isEqualTo("1.7");
					});
				Assertions.assertThat(series.get(1))
					.satisfies(series2 -> {
						Assertions.assertThat(series2.getName()).isEqualTo("cpu");
						Assertions.assertThat(series2.getTags()).containsOnlyKeys("region", "host");
						Assertions.assertThat(series2.getTags().get("region")).isEqualTo("us-east-1");
						Assertions.assertThat(series2.getTags().get("host")).isEqualTo("server2");
						Assertions.assertThat(series2.getColumns()).containsOnlyKeys("time","usage_user","usage_system");
						Assertions.assertThat(series2.getValues()).hasSize(1);

						Assertions.assertThat( series2.getValues().get(0).getValueByKey("usage_user"))
							.isEqualTo("67.91");
						Assertions.assertThat( series2.getValues().get(0).getValueByKey("usage_system"))
							.isEqualTo("1.3");
					});
			});
		Assertions.assertThat(results.get(4))
			.satisfies(r -> {
				Assertions.assertThat(r.getIndex()).isEqualTo(4);
			})
			.extracting(InfluxQLQueryResult.Result::getSeries)
			.satisfies(series -> {
				Assertions.assertThat(series).hasSize(1);
				Assertions.assertThat(series.get(0))
					.satisfies(series1 -> {
						Assertions.assertThat(series1.getName()).isEqualTo("login");
						Assertions.assertThat(series1.getTags()).containsOnlyKeys("region","host");
						Assertions.assertThat(series1.getTags().get("region")).isEqualTo("eu-west-3");
						Assertions.assertThat(series1.getTags().get("host")).isEqualTo("portal-17");
						Assertions.assertThat(series1.getColumns()).containsOnlyKeys("time","user_id","success","stay");
						Assertions.assertThat(series1.getValues()).hasSize(4);
						Assertions.assertThat(series1.getValues().get(0).getValueByKey("time")).isEqualTo("2024-06-18T11:29:48.454Z");
						Assertions.assertThat(series1.getValues().get(0).getValueByKey("user_id")).isEqualTo("958772110");
						Assertions.assertThat(series1.getValues().get(0).getValueByKey("success")).isEqualTo("true");
						Assertions.assertThat(series1.getValues().get(0).getValueByKey("stay")).isEqualTo("1.27");
						Assertions.assertThat(series1.getValues().get(1).getValueByKey("time")).isEqualTo("2024-06-18T11:29:47.124Z");
						Assertions.assertThat(series1.getValues().get(1).getValueByKey("user_id")).isEqualTo("452223904");
						Assertions.assertThat(series1.getValues().get(1).getValueByKey("success")).isEqualTo("false");
						Assertions.assertThat(series1.getValues().get(1).getValueByKey("stay")).isEqualTo("0.0");
						Assertions.assertThat(series1.getValues().get(3).getValueByKey("time")).isEqualTo("2024-06-18T11:29:41.881Z");
						Assertions.assertThat(series1.getValues().get(3).getValueByKey("user_id")).isEqualTo("71119178");
						Assertions.assertThat(series1.getValues().get(3).getValueByKey("success")).isEqualTo("true");
						Assertions.assertThat(series1.getValues().get(3).getValueByKey("stay")).isEqualTo("78.4");
					});
			});
	}

	// Custom
	@Test
	public void readInfluxQLJSONResultCustomExtractValue(){
		InfluxQLQueryResult.Series.ValueExtractor extractValues = (columnName, rawValue, resultIndex, seriesName) -> {
			if (resultIndex == 0 && seriesName.equals("data2")){
				switch (columnName){
					case "time":
						return Instant.ofEpochSecond(Long.parseLong(rawValue));
					case "first":
						return Double.valueOf(rawValue);
				}
			}
			if(seriesName.equals("login")){
        if (columnName.equals("success")) {
          return Boolean.parseBoolean(rawValue);
        }
			}
			return rawValue;
		};

		InfluxQLQueryResult result = InfluxQLQueryApiImpl.readInfluxQLJsonResult(sampleReader,
			NO_CANCELLING,
			extractValues
			);
		List<InfluxQLQueryResult.Result> results = result.getResults();
		Assertions.assertThat(results).hasSize(5);
		Assertions.assertThat(results.get(0))
			.extracting(InfluxQLQueryResult.Result::getSeries)
			.satisfies(series -> {
				Assertions.assertThat(series).hasSize(2);
				Assertions.assertThat(series.get(0))
					.satisfies(series1 -> {
						Assertions.assertThat(series1.getName()).isEqualTo("data1");
						Assertions.assertThat(series1.getColumns()).containsOnlyKeys("time", "first");
						Assertions.assertThat(series1.getValues()).hasSize(1);
						InfluxQLQueryResult.Series.Record record = series1.getValues().get(0);
						Assertions.assertThat(record.getValueByKey("time")).isEqualTo("1483225200");
						Assertions.assertThat(record.getValueByKey("first")).isEqualTo("1");
					});
				Assertions.assertThat(series.get(1))
					.satisfies(series2 -> {
						Assertions.assertThat(series2.getName()).isEqualTo("data2");
						Assertions.assertThat(series2.getColumns()).containsOnlyKeys("time", "first");
						Assertions.assertThat(series2.getValues()).hasSize(1);
						InfluxQLQueryResult.Series.Record record = series2.getValues().get(0);
						Assertions.assertThat(record.getValueByKey("time")).isEqualTo(Instant.ofEpochSecond(1483225200L));
						Assertions.assertThat(record.getValueByKey("first")).isEqualTo(2.0);
					});
			});
	}

	@Test
	public void deserializeNullSeriesJSON(){
		String nullSeriesResponse = "{\"results\":[{\"statement_id\":0}]}";
		InfluxQLQueryResult result = InfluxQLQueryApiImpl.readInfluxQLJsonResult(new StringReader(nullSeriesResponse), NO_CANCELLING, null);
		List<InfluxQLQueryResult.Result> results = result.getResults();
		Assertions.assertThat(results).hasSize(1);
		Assertions.assertThat(results.get(0).getIndex()).isEqualTo(0);
		Assertions.assertThat(results.get(0).getSeries()).hasSize(0);
	}

	@Test
	public void deserializeNullSeriesCSV() throws IOException {
		String nullSeriesResponse = "name,tags,time,val1,val2";
		InfluxQLQueryResult result = InfluxQLQueryApiImpl.readInfluxQLCSVResult(new StringReader(nullSeriesResponse), NO_CANCELLING, null);
		List<InfluxQLQueryResult.Result> results = result.getResults();
		Assertions.assertThat(results).hasSize(1);
		Assertions.assertThat(results.get(0).getIndex()).isEqualTo(0);
		Assertions.assertThat(results.get(0).getSeries()).hasSize(0);
	}

	@Test
	public void deserializeZeroResultJSON() throws IOException {
		String zeroResultResponse = "{\"results\":[]}";
		InfluxQLQueryResult result = InfluxQLQueryApiImpl.readInfluxQLJsonResult(new StringReader(zeroResultResponse), NO_CANCELLING, null);
		List<InfluxQLQueryResult.Result> results = result.getResults();
		Assertions.assertThat(results).hasSize(0);
	}

	@Test
	public void deserializeZeroResultsCSV() throws IOException {
		String nullResponse = "";
		InfluxQLQueryResult result = InfluxQLQueryApiImpl.readInfluxQLCSVResult(new StringReader(nullResponse), NO_CANCELLING, null);
		List<InfluxQLQueryResult.Result> results = result.getResults();
		Assertions.assertThat(results).hasSize(0);
	}

	@Test
	public void deserializeEmptyResultJSON(){
		String emptyResultResponse = "{}";
		InfluxQLQueryResult result = InfluxQLQueryApiImpl.readInfluxQLJsonResult(new StringReader(emptyResultResponse), NO_CANCELLING, null);
		List<InfluxQLQueryResult.Result> results = result.getResults();
		Assertions.assertThat(results).hasSize(0);
	}
}
