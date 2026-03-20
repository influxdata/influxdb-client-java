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
			"location=Cheb\\\\,\\\\ CZ", // simpleTag with value comma and space
			"location=Cheb_CZ,branch=Munchen_DE", // multiple tags with underscore
			"location=Cheb\\\\,\\\\ CZ,branch=Munchen\\\\,\\\\ DE", // multiple tags with comma and space
			"model\\\\,\\\\ uin=C3PO", // tag with comma space in key
			"model\\\\,\\\\ uin=Droid\\\\, C3PO", // tag with comma space in key and value
			"model\\\\,\\\\ uin=Droid\\\\,\\\\ C3PO,location=Cheb\\\\,\\\\ CZ,branch=Munchen\\\\,\\\\ DE", // comma space in key and val
			"silly\\\\,\\\\=long\\\\,tag=a\\\\,b\\\\\\\\\\,\\\\ c\\\\,\\\\ d", // multi commas in k and v plus escaped reserved chars
			"region=us\\\\,\\\\ east-1,host\\\\,\\\\ name=ser\\\\,\\\\ ver1" // legacy broken tags
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
				mapOf("location", "Cheb\\,\\ CZ")),
			// 4. multiple tags with underscore
			new AbstractMap.SimpleImmutableEntry<>(testTags.get(3),
				mapOf(
					"location", "Cheb_CZ",
					"branch", "Munchen_DE"
				)),
			// 5. multiple tags with comma and space
			new AbstractMap.SimpleImmutableEntry<>(testTags.get(4),
				mapOf(
					"location", "Cheb\\,\\ CZ",
					"branch", "Munchen\\,\\ DE"
				)),
			// 6. tag with comma and space in key
			new AbstractMap.SimpleImmutableEntry<>(testTags.get(5),
				mapOf("model\\,\\ uin", "C3PO")),
			// 7. tag with comma and space in key and value
			new AbstractMap.SimpleImmutableEntry<>(testTags.get(6),
				mapOf("model\\,\\ uin", "Droid\\, C3PO")),
			// 8. comma space in key and val with multiple tags
			new AbstractMap.SimpleImmutableEntry<>(testTags.get(7),
				mapOf(
					"model\\,\\ uin", "Droid\\,\\ C3PO",
					"location", "Cheb\\,\\ CZ",
					"branch", "Munchen\\,\\ DE"
				)),
			// 9. multiple commas in key and value
		    new AbstractMap.SimpleImmutableEntry<>(testTags.get(8),
				mapOf(
					"silly\\,\\=long\\,tag", "a\\,b\\\\\\,\\ c\\,\\ d"
				)),
			// legacy broken tags
			new AbstractMap.SimpleImmutableEntry<>(testTags.get(9),
				mapOf(
					"region", "us\\,\\ east-1",
					"host\\,\\ name", "ser\\,\\ ver1"
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

		InfluxQLQueryResult result = InfluxQLQueryApiImpl.readInfluxQLResult(reader, NO_CANCELLING, extractValue);
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

	/*
	Sample response 1 - note escaped commas
name,tags,time,fVal,iVal,id,location,"location\,boo",model,"model\,uin",sVal
zaphrod_b,,1773307528202967039,26.54671,-6922649068284626682,bar,Harfa,,R2D2,,FOO
zaphrod_b,,1773322199131651270,26.54671,-6922649068284626682,bar,,Harfa,R2D2,,FOO
zaphrod_b,,1773322228235655514,26.54671,-6922649068284626682,bar,,"Harfa\,\ Praha",R2D2,,FOO
zaphrod_b,,1773322254827374192,26.54671,-6922649068284626682,bar,,"Harfa\,\ Praha",,R2D2,FOO
	 */

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
				"cpu,\"region=us-east-1,host=server2\",1483225200,67.91,1.3\n");

		InfluxQLQueryResult result = InfluxQLQueryApiImpl.readInfluxQLResult(reader, NO_CANCELLING, extractValues);

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
}
