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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.influxdb.client.domain.Dialect;
import com.influxdb.client.domain.Query;
import com.influxdb.client.internal.AbstractInfluxDBClientTest;

import com.google.gson.Gson;
import okhttp3.mockwebserver.RecordedRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.influxdb.client.internal.AbstractInfluxDBClient.DEFAULT_DIALECT;

/**
 * @author Jakub Bednar (bednar@github) (07/05/2019 09:17)
 */
class QueryApiTest extends AbstractInfluxDBClientTest {

    @Test
    void parametersFromOptions() throws InterruptedException, IOException {

        after();

        String url = startMockServer();
        InfluxDBClientOptions options = InfluxDBClientOptions.builder()
                .url(url)
                .org("123456")
                .build();

        influxDBClient = InfluxDBClientFactory.create(options);

        QueryApi queryApi = influxDBClient.getQueryApi();

        // String
        enqueuedResponse();

        queryApi.query("from(bucket: \"telegraf\")");

        RecordedRequest request = takeRequest();

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456");

        influxDBClient = InfluxDBClientFactory.create(url, "my-token".toCharArray(), "123456");

        queryApi = influxDBClient.getQueryApi();

        // String & params
        enqueuedResponse();

        Map<String, Object> params = new HashMap<>();
        params.put("bucketParam","telegraf");

        queryApi.query("from(bucket: \"telegraf\")", "123456", params);

        request = takeRequest();

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456");
        Gson gson = JSON.createGson().create();
        Map p = (Map) gson.fromJson(request.getBody().readUtf8(), Map.class).get("params");

        Assertions.assertThat(p.get("bucketParam")).isEqualTo("telegraf");

        influxDBClient = InfluxDBClientFactory.create(url, "my-token".toCharArray(), "123456");

        queryApi = influxDBClient.getQueryApi();

        // String
        enqueuedResponse();

        queryApi.query("from(bucket: \"telegraf\")");

        request = takeRequest();

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456");

        // Query
        enqueuedResponse();

        queryApi.query(new Query().query("from(bucket: \"telegraf\")"));

        request = takeRequest();

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456");

        // check default dialect presence
        Map dialect = (Map) gson.fromJson(request.getBody().readUtf8(), Map.class).get("dialect");
        Assertions.assertThat(dialect.get("header")).isEqualTo(DEFAULT_DIALECT.getHeader());
        Assertions.assertThat(dialect.get("delimiter")).isEqualTo(DEFAULT_DIALECT.getDelimiter());
        Assertions.assertThat(dialect.get("annotations")).isEqualTo(Arrays.asList("datatype", "group", "default"));
        Assertions.assertThat(dialect.get("dateTimeFormat")).isEqualTo(DEFAULT_DIALECT.getDateTimeFormat().toString());
        Assertions.assertThat(dialect.get("commentPrefix")).isEqualTo(DEFAULT_DIALECT.getCommentPrefix());

        // String Measurement
        enqueuedResponse();

        queryApi.query("from(bucket: \"telegraf\")", H2OFeetMeasurement.class);

        request = takeRequest();

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456");

        // Query parameters
        enqueuedResponse();

        queryApi.query("from(bucket: params.bucketParam)", "123456", H2OFeetMeasurement.class, params);
        request = takeRequest();

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456");
        p = (Map) gson.fromJson(request.getBody().readUtf8(), Map.class).get("params");
        Assertions.assertThat(p.get("bucketParam")).isEqualTo("telegraf");
        Assertions.assertThat(p).isEqualTo(params);

        // Query Measurement
        enqueuedResponse();

        queryApi.query(new Query().query("from(bucket: \"telegraf\")"), H2OFeetMeasurement.class);

        request = takeRequest();

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456");

        // String OnNext
        enqueuedResponse();

        queryApi.query("from(bucket: \"telegraf\")", (cancellable, fluxRecord) -> {

        });

        request = takeRequest();

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456");

        // Query OnNext
        enqueuedResponse();

        queryApi.query(new Query().query("from(bucket: \"telegraf\")"), (cancellable, fluxRecord) -> {

        });

        request = takeRequest();

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456");

        // String OnNext Measurement
        enqueuedResponse();

        queryApi.query("from(bucket: \"telegraf\")", H2OFeetMeasurement.class, (cancellable, fluxRecord) -> {

        });

        request = takeRequest();

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456");

        // Query OnNext Measurement
        enqueuedResponse();

        queryApi.query(new Query().query("from(bucket: \"telegraf\")"), H2OFeetMeasurement.class, (cancellable, fluxRecord) -> {

        });

        request = takeRequest();

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456");

        // String OnNext, OnError
        enqueuedResponse();

        queryApi.query("from(bucket: \"telegraf\")", (cancellable, fluxRecord) -> {

        }, throwable -> {

        });

        request = takeRequest();

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456");


        // Query OnNext, OnError
        enqueuedResponse();

        queryApi.query(new Query().query("from(bucket: \"telegraf\")"), (cancellable, fluxRecord) -> {

        }, throwable -> {

        });

        request = takeRequest();

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456");

        // String OnNext, OnError Measurement
        enqueuedResponse();

        queryApi.query("from(bucket: \"telegraf\")", H2OFeetMeasurement.class, (cancellable, fluxRecord) -> {

        }, throwable -> {

        });

        request = takeRequest();

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456");

        // Query OnNext, OnError Measurement
        enqueuedResponse();

        queryApi.query(new Query().query("from(bucket: \"telegraf\")"), H2OFeetMeasurement.class, (cancellable, fluxRecord) -> {

        }, throwable -> {

        });

        request = takeRequest();

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456");

        // String OnNext, OnError, OnComplete
        enqueuedResponse();

        queryApi.query("from(bucket: \"telegraf\")", (cancellable, fluxRecord) -> {

        }, throwable -> {

        }, () -> {

        });

        request = takeRequest();

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456");


        // Query OnNext, OnError, OnComplete
        enqueuedResponse();

        queryApi.query(new Query().query("from(bucket: \"telegraf\")"), (cancellable, fluxRecord) -> {

        }, throwable -> {

        },() -> {

        });

        request = takeRequest();

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456");

        // String OnNext, OnError Measurement, OnComplete
        enqueuedResponse();

        queryApi.query("from(bucket: \"telegraf\")", H2OFeetMeasurement.class, (cancellable, fluxRecord) -> {

        }, throwable -> {

        },() -> {

        });

        request = takeRequest();

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456");

        // Query OnNext, OnError Measurement, OnComplete
        enqueuedResponse();

        queryApi.query(new Query().query("from(bucket: \"telegraf\")"), H2OFeetMeasurement.class, (cancellable, fluxRecord) -> {

        }, throwable -> {

        }, () -> {

        });

        request = takeRequest();

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456"); // Query OnNext, OnError Measurement, OnComplete

        // Raw string
        enqueuedResponse();

        queryApi.queryRaw("from(bucket: \"telegraf\")");

        request = takeRequest();

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456");

        // Raw string + dialect
        enqueuedResponse();

        queryApi.queryRaw("from(bucket: \"telegraf\")", new Dialect());

        request = takeRequest();

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456");

        // Raw Query
        enqueuedResponse();

        queryApi.queryRaw(new Query().query("from(bucket: \"telegraf\")"));

        request = takeRequest();

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456");

        // Raw String OnResponse
        enqueuedResponse();

        queryApi.queryRaw("from(bucket: \"telegraf\")", (cancellable, s) -> {

        });

        request = takeRequest();

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456");

        // Raw String + Dialect OnResponse
        enqueuedResponse();

        queryApi.queryRaw("from(bucket: \"telegraf\")", new Dialect(), (cancellable, s) -> {

        });

        request = takeRequest();

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456");

        // Raw Query OnResponse
        enqueuedResponse();

        queryApi.queryRaw(new Query().query("from(bucket: \"telegraf\")"), (cancellable, s) -> {

        });

        request = takeRequest();

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456");

        // Raw String OnResponse, OnError
        enqueuedResponse();

        queryApi.queryRaw("from(bucket: \"telegraf\")", (cancellable, s) -> {

        }, throwable -> {

        });

        request = takeRequest();

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456");

        // Raw String + Dialect OnResponse, OnError
        enqueuedResponse();

        queryApi.queryRaw("from(bucket: \"telegraf\")", new Dialect(), (cancellable, s) -> {

        }, throwable -> {

        });

        request = takeRequest();

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456");

        // Raw Query OnResponse, OnError
        enqueuedResponse();

        queryApi.queryRaw(new Query().query("from(bucket: \"telegraf\")"), (cancellable, s) -> {

        }, throwable -> {

        });

        request = takeRequest();

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456");

        // Raw String OnResponse, OnError, OnComplete
        enqueuedResponse();

        queryApi.queryRaw("from(bucket: \"telegraf\")", (cancellable, s) -> {

        }, throwable -> {

        }, () -> {

        });

        request = takeRequest();

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456");

        // Raw String + Dialect OnResponse, OnError, OnComplete
        enqueuedResponse();

        queryApi.queryRaw("from(bucket: \"telegraf\")", new Dialect(), (cancellable, s) -> {

        }, throwable -> {

        }, () -> {

        });

        request = takeRequest();

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456");

        // Raw Query OnResponse, OnError, OnComplete
        enqueuedResponse();

        queryApi.queryRaw(new Query().query("from(bucket: \"telegraf\")"), (cancellable, s) -> {

        }, throwable -> {

        }, () -> {

        });

        request = takeRequest();

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456");
    }
}