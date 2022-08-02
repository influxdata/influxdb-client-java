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
import java.util.List;

import com.influxdb.Cancellable;
import com.influxdb.query.InfluxQLQueryResult;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

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
				"databases,,measurement-2");

		InfluxQLQueryResult result = InfluxQLQueryApiImpl.readInfluxQLResult(reader, NO_CANCELLING, extractValues);

		List<InfluxQLQueryResult.Result> results = result.getResults();
		Assertions.assertThat(results).hasSize(3);
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
	}
}
