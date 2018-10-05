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
package org.influxdata.flux;

import java.time.Instant;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.influxdata.flux.domain.FluxRecord;
import org.influxdata.flux.error.FluxQueryException;
import org.influxdata.platform.error.InfluxException;

import io.reactivex.Flowable;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.assertj.core.api.Assertions;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (26/06/2018 13:54)
 */
@RunWith(JUnitPlatform.class)
class FluxClientReactiveQueryTest extends AbstractFluxClientReactiveTest {

    @Test
    void query() throws InterruptedException {

        mockServer.enqueue(createResponse());

        String query = "from(bucket:\"telegraf\") |> " +
                "filter(fn: (r) => r[\"_measurement\"] == \"cpu\" AND r[\"_field\"] == \"usage_user\") |> sum()";

        Flowable<FluxRecord> results = fluxClient.query(query);
        results
                .test()
                .assertValueCount(6);

        Assertions.assertThat(queryFromRequest()).isEqualToIgnoringWhitespace(query);
    }

    @Test
    void queryPublisher() throws InterruptedException {

        mockServer.enqueue(createResponse());

        Flowable<FluxRecord> results = fluxClient.query(Flowable.just("from(bucket:\"flux_database\") |> limit(n: 5)"));
        results
                .test()
                .assertValueCount(6);

        Assertions.assertThat(queryFromRequest())
                .isEqualToIgnoringWhitespace("from(bucket:\"flux_database\") |> limit(n: 5)");
    }


    @Test
    void queryErrorSuccessResponse() {

        String error =
                "#datatype,string,string\n"
                        + "#group,true,true\n"
                        + "#default,,\n"
                        + ",error,reference\n"
                        + ",failed to create physical plan: invalid time bounds from procedure from: bounds contain zero time,897";

        mockServer.enqueue(createResponse(error));

        Flowable<FluxRecord> results = fluxClient.query("from(bucket:\"telegraf\")");
        results
                .test()
                .assertError(FluxQueryException.class)
                .assertErrorMessage("failed to create physical plan: invalid time bounds from procedure from: bounds contain zero time")
                .assertError(throwable -> ((InfluxException) throwable).reference() == 897);
    }

    @Test
    void parsingToFluxRecordsMultiTable() {

        mockServer.enqueue(createMultiTableResponse());

        Flowable<FluxRecord> results = fluxClient.query("from(bucket:\"telegraf\")");
        results
                .take(4)
                .test()
                .assertValueCount(4)
                .assertValueAt(0, fluxRecord -> {

                    {
                        Assertions.assertThat(fluxRecord.getValue()).isEqualTo(50d);
                        Assertions.assertThat(fluxRecord.getField()).isEqualTo("cpu_usage");

                        Assertions.assertThat(fluxRecord.getStart()).isEqualTo(Instant.parse("1677-09-21T00:12:43.145224192Z"));
                        Assertions.assertThat(fluxRecord.getStop()).isEqualTo(Instant.parse("2018-06-27T06:22:38.347437344Z"));
                        Assertions.assertThat(fluxRecord.getTime()).isEqualTo(Instant.parse("2018-06-27T05:56:40.001Z"));

                        Assertions.assertThat(fluxRecord.getMeasurement()).isEqualTo("server_performance");
                        Assertions.assertThat(fluxRecord.getValues())
                                .hasEntrySatisfying("location", value -> Assertions.assertThat(value).isEqualTo("Area 1° 10' \"20"))
                                .hasEntrySatisfying("production_usage", value -> Assertions.assertThat(value).isEqualTo("false"));
                    }

                    return true;
                }).assertValueAt(2, fluxRecord -> {

                    {
                        Assertions.assertThat(fluxRecord.getValue()).isEqualTo("Server no. 1");
                        Assertions.assertThat(fluxRecord.getField()).isEqualTo("server description");

                        Assertions.assertThat(fluxRecord.getStart()).isEqualTo(Instant.parse("1677-09-21T00:12:43.145224192Z"));
                        Assertions.assertThat(fluxRecord.getStop()).isEqualTo(Instant.parse("2018-06-27T06:22:38.347437344Z"));
                        Assertions.assertThat(fluxRecord.getTime()).isEqualTo(Instant.parse("2018-06-27T05:56:40.001Z"));

                        Assertions.assertThat(fluxRecord.getMeasurement()).isEqualTo("server_performance");
                        Assertions.assertThat(fluxRecord.getValues())
                                .hasEntrySatisfying("location", value -> Assertions.assertThat(value).isEqualTo("Area 1° 10' \"20"))
                                .hasEntrySatisfying("production_usage", value -> Assertions.assertThat(value).isEqualTo("false"));
                    }

                    return true;
                });
    }

    @Test
    void parsingToFluxRecordsMultiRecordValues() {

        String data = "#datatype,string,long,dateTime:RFC3339,dateTime:RFC3339,string,string,string,string,long,long,string\n"
                + "#group,false,false,true,true,true,true,true,true,false,false,false\n"
                + "#default,_result,,,,,,,,,,\n"
                + ",result,table,_start,_stop,_field,_measurement,host,region,_value2,value1,value_str\n"
                + ",,0,1677-09-21T00:12:43.145224192Z,2018-07-16T11:21:02.547596934Z,free,mem,A,west,121,11,test";

        mockServer.enqueue(createResponse(data));

        Flowable<FluxRecord> results = fluxClient.query("from(bucket:\"telegraf\")");
        results
                .take(1)
                .test()
                .assertValueCount(1)
                .assertValue(fluxRecord -> {

                    Assertions.assertThat(fluxRecord.getValues())
                            .hasEntrySatisfying("host", value -> Assertions.assertThat(value).isEqualTo("A"))
                            .hasEntrySatisfying("region", value -> Assertions.assertThat(value).isEqualTo("west"));
                    Assertions.assertThat(fluxRecord.getValues()).hasSize(11);
                    Assertions.assertThat(fluxRecord.getValue()).isNull();
                    Assertions.assertThat(fluxRecord.getValues())
                            .hasEntrySatisfying("value1", value -> Assertions.assertThat(value).isEqualTo(11L))
                            .hasEntrySatisfying("_value2", value -> Assertions.assertThat(value).isEqualTo(121L))
                            .hasEntrySatisfying("value_str", value -> Assertions.assertThat(value).isEqualTo("test"));

                    return true;
                });
    }

    @Nonnull
    private MockResponse createMultiTableResponse() {

        String data =
                "#datatype,string,long,dateTime:RFC3339,dateTime:RFC3339,dateTime:RFC3339,double,string,string,string,string\n"
                        + "#group,false,false,true,true,false,false,true,true,true,true\n"
                        + "#default,_result,,,,,,,,,\n"
                        + ",result,table,_start,_stop,_time,_value,_field,_measurement,location,production_usage\n"
                        + ",,0,1677-09-21T00:12:43.145224192Z,2018-06-27T06:22:38.347437344Z,2018-06-27T05:56:40.001Z,50,cpu_usage,server_performance,\"Area 1° 10' \"\"20\",false\n"
                        + "\n"
                        + "#datatype,string,long,dateTime:RFC3339,dateTime:RFC3339,dateTime:RFC3339,long,string,string,string,string\n"
                        + "#group,false,false,true,true,false,false,true,true,true,true\n"
                        + "#default,_result,,,,,,,,,\n"
                        + ",result,table,_start,_stop,_time,_value,_field,_measurement,location,production_usage\n"
                        + ",,1,1677-09-21T00:12:43.145224192Z,2018-06-27T06:22:38.347437344Z,2018-06-27T05:56:40.001Z,1,rackNumber,server_performance,\"Area 1° 10' \"\"20\",false\n"
                        + "\n"
                        + "#datatype,string,long,dateTime:RFC3339,dateTime:RFC3339,dateTime:RFC3339,string,string,string,string,string\n"
                        + "#group,false,false,true,true,false,false,true,true,true,true\n"
                        + "#default,_result,,,,,,,,,\n"
                        + ",result,table,_start,_stop,_time,_value,_field,_measurement,location,production_usage\n"
                        + ",,2,1677-09-21T00:12:43.145224192Z,2018-06-27T06:22:38.347437344Z,2018-06-27T05:56:40.001Z,Server no. 1,server description,server_performance,\"Area 1° 10' \"\"20\",false\n"
                        + "\n"
                        + "#datatype,string,long,dateTime:RFC3339,dateTime:RFC3339,dateTime:RFC3339,long,string,string,string,string\n"
                        + "#group,false,false,true,true,false,false,true,true,true,true\n"
                        + "#default,_result,,,,,,,,,\n"
                        + ",result,table,_start,_stop,_time,_value,_field,_measurement,location,production_usage\n"
                        + ",,3,1677-09-21T00:12:43.145224192Z,2018-06-27T06:22:38.347437344Z,2018-06-27T05:56:40.001Z,10000,upTime,server_performance,\"Area 1° 10' \"\"20\",false";

        return createResponse(data);
    }

    @Nullable
    private String queryFromRequest() throws InterruptedException {

        RecordedRequest recordedRequest = mockServer.takeRequest();
        String body = recordedRequest.getBody().readUtf8();

        return new JSONObject(body).getString("query");
    }
}