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
package com.influxdb.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import javax.annotation.Nonnull;

import com.influxdb.Cancellable;
import com.influxdb.exceptions.InfluxException;
import com.influxdb.query.internal.FluxCsvParser;
import com.influxdb.test.AbstractMockServerTest;

import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.mockwebserver.MockResponse;
import okio.Buffer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Streaming;

/**
 * @author Jakub Bednar (bednar@github) (17/10/2018 09:44)
 */
@RunWith(JUnitPlatform.class)
class QueryAbstractApiTest extends AbstractMockServerTest {

    private AbstractQueryApi queryClient;
    private QueryAPI queryAPI;

    @BeforeEach
    void setUp() {
        queryClient = new AbstractQueryApi() {
        };

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(startMockServer())
                .client(new OkHttpClient())
                .build();

        queryAPI = retrofit.create(QueryAPI.class);
    }

    @Test
    void createBody() throws IOException {

        RequestBody body = queryClient.createBody(null, "from(bucket:\"telegraf\")");

        Buffer buffer = new Buffer();
        body.writeTo(buffer);

        Assertions.assertThat(buffer.readUtf8()).isEqualTo("{\"query\":\"from(bucket:\\\"telegraf\\\")\"}");
    }

    @Test
    void createBodyWithDialect() throws IOException {

        RequestBody body = queryClient.createBody("{header: false}", "from(bucket:\"telegraf\")");

        Buffer buffer = new Buffer();
        body.writeTo(buffer);

        Assertions.assertThat(buffer.readUtf8()).isEqualTo("{\"dialect\":{\"header\":false},\"query\":\"from(bucket:\\\"telegraf\\\")\"}");
    }

    @Test
    void createBodyEmptyDialect() throws IOException {

        RequestBody body = queryClient.createBody("", "from(bucket:\"telegraf\")");

        Buffer buffer = new Buffer();
        body.writeTo(buffer);

        Assertions.assertThat(buffer.readUtf8()).isEqualTo("{\"query\":\"from(bucket:\\\"telegraf\\\")\"}");
    }

    @Test
    void createBodyDefaultDialect() throws IOException {

        RequestBody body = queryClient.createBody(AbstractQueryApi.DEFAULT_DIALECT.toString(), "from(bucket:\"telegraf\")");

        Buffer buffer = new Buffer();
        body.writeTo(buffer);

        String expected = "{\"dialect\":{\"quoteChar\":\"\\\"\",\"commentPrefix\":\"#\",\"delimiter\":\",\",\"header\":true,\"annotations\":[\"datatype\",\"group\",\"default\"]},\"query\":\"from(bucket:\\\"telegraf\\\")\"}";
        Assertions.assertThat(buffer.readUtf8()).isEqualTo(expected);
    }

    @Test
    void querySynchronous() {

        mockServer.enqueue(createResponse());

        FluxCsvParser.FluxResponseConsumerTable consumer = queryClient.fluxCsvParser.new FluxResponseConsumerTable();

        queryClient.query(createCall(), consumer, AbstractQueryApi.ERROR_CONSUMER, () -> {

        }, false);

        Assertions.assertThat(consumer.getTables()).hasSize(1);
        Assertions.assertThat(consumer.getTables().get(0).getRecords()).hasSize(2);
    }

    @Test
    void queryAsynchronous() {

        mockServer.enqueue(createResponse());

        FluxCsvParser.FluxResponseConsumerTable consumer = queryClient.fluxCsvParser.new FluxResponseConsumerTable();

        queryClient.query(createCall(), consumer, AbstractQueryApi.ERROR_CONSUMER, () -> countDownLatch.countDown(), true);

        waitToCallback();

        Assertions.assertThat(consumer.getTables()).hasSize(1);
        Assertions.assertThat(consumer.getTables().get(0).getRecords()).hasSize(2);
    }

    @Test
    void queryOnComplete() {

        mockServer.enqueue(createResponse());

        FluxCsvParser.FluxResponseConsumerTable consumer = queryClient.fluxCsvParser.new FluxResponseConsumerTable();

        queryClient.query(createCall(), consumer, AbstractQueryApi.ERROR_CONSUMER, () -> countDownLatch.countDown(), false);

        waitToCallback();
    }

    @Test
    void queryError() {

        mockServer.enqueue(createErrorResponse("Flux query is not valid"));

        FluxCsvParser.FluxResponseConsumerTable consumer = queryClient.fluxCsvParser.new FluxResponseConsumerTable();

        Assertions.assertThatThrownBy(() -> queryClient.query(createCall(), consumer, AbstractQueryApi.ERROR_CONSUMER, () -> {
        }, false))
                .isInstanceOf(InfluxException.class)
                .hasMessage("Flux query is not valid");
    }

    @Test
    void queryErrorCustom() {

        mockServer.enqueue(createErrorResponse("Flux query is not valid"));

        FluxCsvParser.FluxResponseConsumerTable consumer = queryClient.fluxCsvParser.new FluxResponseConsumerTable();

        queryClient.query(createCall(), consumer, throwable -> countDownLatch.countDown(), () -> {
        }, false);

        waitToCallback();
    }

    @Test
    void queryRawSynchronous() {

        mockServer.enqueue(createResponse());

        RawConsumer consumer = new RawConsumer();

        queryClient.queryRaw(createCall(), consumer, AbstractQueryApi.ERROR_CONSUMER, () -> {

        }, false);

        Assertions.assertThat(consumer.lines).hasSize(6);
    }

    @Test
    void queryRawAsynchronous() {

        mockServer.enqueue(createResponse());

        RawConsumer consumer = new RawConsumer();

        queryClient.queryRaw(createCall(), consumer, AbstractQueryApi.ERROR_CONSUMER, () -> countDownLatch.countDown(), true);

        waitToCallback();

        Assertions.assertThat(consumer.lines).hasSize(6);
    }

    @Test
    void queryRawOnComplete() {

        mockServer.enqueue(createResponse());

        RawConsumer consumer = new RawConsumer();

        queryClient.queryRaw(createCall(), consumer, AbstractQueryApi.ERROR_CONSUMER, () -> countDownLatch.countDown(), false);

        waitToCallback();
    }

    @Test
    void queryRawError() {

        mockServer.enqueue(createErrorResponse("Flux query is not valid"));

        RawConsumer consumer = new RawConsumer();

        Assertions.assertThatThrownBy(() -> queryClient.queryRaw(createCall(), consumer, AbstractQueryApi.ERROR_CONSUMER, () -> {
        }, false))
                .isInstanceOf(InfluxException.class)
                .hasMessage("Flux query is not valid");
    }

    @Test
    void queryRawErrorCustom() {

        mockServer.enqueue(createErrorResponse("Flux query is not valid"));

        RawConsumer consumer = new RawConsumer();

        queryClient.queryRaw(createCall(), consumer, throwable -> countDownLatch.countDown(), () -> {
        }, false);

        waitToCallback();
    }

    @Test
    void queryCancel() {

        mockServer.enqueue(createResponse());

        List<String> lines = new ArrayList<>();

        queryClient.queryRaw(createCall(), (cancellable, line) -> {

            lines.add(line);
            cancellable.cancel();
            countDownLatch.countDown();
        }, AbstractQueryApi.ERROR_CONSUMER, () -> {
        }, false);

        waitToCallback();

        Assertions.assertThat(lines).hasSize(1);
    }

    @Nonnull
    private Call<ResponseBody> createCall() {

        RequestBody body = queryClient.createBody(null, "from(bucket:\"telegraf\")");

        return queryAPI.query(body);
    }

    @Nonnull
    private MockResponse createResponse() {
        String data =
                "#datatype,string,long,dateTime:RFC3339,dateTime:RFC3339,dateTime:RFC3339,long,string,string,string,string\n"
                        + "#group,false,false,false,false,false,false,false,false,false,true\n"
                        + "#default,_result,,,,,,,,,\n"
                        + ",result,table,_start,_stop,_time,_value,_field,_measurement,host,region\n"
                        + ",,0,1970-01-01T00:00:10Z,1970-01-01T00:00:20Z,1970-01-01T00:00:10Z,10,free,mem,A,west\n"
                        + ",,0,1970-01-01T00:00:10Z,1970-01-01T00:00:20Z,1970-01-01T00:00:10Z,20,free,mem,B,west\n";
        return createResponse(data);
    }

    private interface QueryAPI {

        @Streaming
        @POST("/query")
        @Nonnull
        @Headers("Content-Type: application/json")
        Call<ResponseBody> query(@Nonnull @Body final RequestBody query);
    }

    private class RawConsumer implements BiConsumer<Cancellable, String> {

        private List<String> lines = new ArrayList<>();

        @Override
        public void accept(final Cancellable cancellable, final String line) {
            lines.add(line);
        }
    }
}