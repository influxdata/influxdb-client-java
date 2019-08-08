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
import java.util.concurrent.TimeUnit;

import com.influxdb.client.domain.Dialect;
import com.influxdb.client.domain.Query;
import com.influxdb.client.internal.AbstractInfluxDBClientTest;

import okhttp3.mockwebserver.RecordedRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (07/05/2019 09:17)
 */
@RunWith(JUnitPlatform.class)
class QueryApiTest extends AbstractInfluxDBClientTest {

    @Test
    void parametersFromOptions() throws InterruptedException, IOException {

        after();

        InfluxDBClientOptions options = InfluxDBClientOptions.builder()
                .url(startMockServer())
                .org("123456")
                .build();

        influxDBClient = InfluxDBClientFactory.create(options);

        QueryApi queryApi = influxDBClient.getQueryApi();

        // String
        mockServer.enqueue(createResponse(""));

        queryApi.query("from(bucket: \"telegraf\")");

        RecordedRequest request = mockServer.takeRequest(10L, TimeUnit.SECONDS);

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456");

        // Query
        mockServer.enqueue(createResponse(""));

        queryApi.query(new Query().query("from(bucket: \"telegraf\")"));

        request = mockServer.takeRequest(10L, TimeUnit.SECONDS);

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456");

        // String Measurement
        mockServer.enqueue(createResponse(""));

        queryApi.query("from(bucket: \"telegraf\")", H2OFeetMeasurement.class);

        request = mockServer.takeRequest(10L, TimeUnit.SECONDS);

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456");

        // Query Measurement
        mockServer.enqueue(createResponse(""));

        queryApi.query(new Query().query("from(bucket: \"telegraf\")"), H2OFeetMeasurement.class);

        request = mockServer.takeRequest(10L, TimeUnit.SECONDS);

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456");

        // String OnNext
        mockServer.enqueue(createResponse(""));

        queryApi.query("from(bucket: \"telegraf\")", (cancellable, fluxRecord) -> {

        });

        request = mockServer.takeRequest(10L, TimeUnit.SECONDS);

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456");

        // Query OnNext
        mockServer.enqueue(createResponse(""));

        queryApi.query(new Query().query("from(bucket: \"telegraf\")"), (cancellable, fluxRecord) -> {

        });

        request = mockServer.takeRequest(10L, TimeUnit.SECONDS);

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456");

        // String OnNext Measurement
        mockServer.enqueue(createResponse(""));

        queryApi.query("from(bucket: \"telegraf\")", H2OFeetMeasurement.class, (cancellable, fluxRecord) -> {

        });

        request = mockServer.takeRequest(10L, TimeUnit.SECONDS);

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456");

        // Query OnNext Measurement
        mockServer.enqueue(createResponse(""));

        queryApi.query(new Query().query("from(bucket: \"telegraf\")"), H2OFeetMeasurement.class, (cancellable, fluxRecord) -> {

        });

        request = mockServer.takeRequest(10L, TimeUnit.SECONDS);

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456");

        // String OnNext, OnError
        mockServer.enqueue(createResponse(""));

        queryApi.query("from(bucket: \"telegraf\")", (cancellable, fluxRecord) -> {

        }, throwable -> {

        });

        request = mockServer.takeRequest(10L, TimeUnit.SECONDS);

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456");


        // Query OnNext, OnError
        mockServer.enqueue(createResponse(""));

        queryApi.query(new Query().query("from(bucket: \"telegraf\")"), (cancellable, fluxRecord) -> {

        }, throwable -> {

        });

        request = mockServer.takeRequest(10L, TimeUnit.SECONDS);

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456");

        // String OnNext, OnError Measurement
        mockServer.enqueue(createResponse(""));

        queryApi.query("from(bucket: \"telegraf\")", H2OFeetMeasurement.class, (cancellable, fluxRecord) -> {

        }, throwable -> {

        });

        request = mockServer.takeRequest(10L, TimeUnit.SECONDS);

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456");

        // Query OnNext, OnError Measurement
        mockServer.enqueue(createResponse(""));

        queryApi.query(new Query().query("from(bucket: \"telegraf\")"), H2OFeetMeasurement.class, (cancellable, fluxRecord) -> {

        }, throwable -> {

        });

        request = mockServer.takeRequest(10L, TimeUnit.SECONDS);

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456");

        // String OnNext, OnError, OnComplete
        mockServer.enqueue(createResponse(""));

        queryApi.query("from(bucket: \"telegraf\")", (cancellable, fluxRecord) -> {

        }, throwable -> {

        }, () -> {

        });

        request = mockServer.takeRequest(10L, TimeUnit.SECONDS);

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456");


        // Query OnNext, OnError, OnComplete
        mockServer.enqueue(createResponse(""));

        queryApi.query(new Query().query("from(bucket: \"telegraf\")"), (cancellable, fluxRecord) -> {

        }, throwable -> {

        },() -> {

        });

        request = mockServer.takeRequest(10L, TimeUnit.SECONDS);

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456");

        // String OnNext, OnError Measurement, OnComplete
        mockServer.enqueue(createResponse(""));

        queryApi.query("from(bucket: \"telegraf\")", H2OFeetMeasurement.class, (cancellable, fluxRecord) -> {

        }, throwable -> {

        },() -> {

        });

        request = mockServer.takeRequest(10L, TimeUnit.SECONDS);

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456");

        // Query OnNext, OnError Measurement, OnComplete
        mockServer.enqueue(createResponse(""));

        queryApi.query(new Query().query("from(bucket: \"telegraf\")"), H2OFeetMeasurement.class, (cancellable, fluxRecord) -> {

        }, throwable -> {

        }, () -> {

        });

        request = mockServer.takeRequest(10L, TimeUnit.SECONDS);

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456"); // Query OnNext, OnError Measurement, OnComplete

        // Raw string
        mockServer.enqueue(createResponse(""));

        queryApi.queryRaw("from(bucket: \"telegraf\")");

        request = mockServer.takeRequest(10L, TimeUnit.SECONDS);

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456");

        // Raw string + dialect
        mockServer.enqueue(createResponse(""));

        queryApi.queryRaw("from(bucket: \"telegraf\")", new Dialect());

        request = mockServer.takeRequest(10L, TimeUnit.SECONDS);

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456");

        // Raw Query
        mockServer.enqueue(createResponse(""));

        queryApi.queryRaw(new Query().query("from(bucket: \"telegraf\")"));

        request = mockServer.takeRequest(10L, TimeUnit.SECONDS);

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456");

        // Raw String OnResponse
        mockServer.enqueue(createResponse(""));

        queryApi.queryRaw("from(bucket: \"telegraf\")", (cancellable, s) -> {

        });

        request = mockServer.takeRequest(10L, TimeUnit.SECONDS);

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456");

        // Raw String + Dialect OnResponse
        mockServer.enqueue(createResponse(""));

        queryApi.queryRaw("from(bucket: \"telegraf\")", new Dialect(), (cancellable, s) -> {

        });

        request = mockServer.takeRequest(10L, TimeUnit.SECONDS);

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456");

        // Raw Query OnResponse
        mockServer.enqueue(createResponse(""));

        queryApi.queryRaw(new Query().query("from(bucket: \"telegraf\")"), (cancellable, s) -> {

        });

        request = mockServer.takeRequest(10L, TimeUnit.SECONDS);

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456");

        // Raw String OnResponse, OnError
        mockServer.enqueue(createResponse(""));

        queryApi.queryRaw("from(bucket: \"telegraf\")", (cancellable, s) -> {

        }, throwable -> {

        });

        request = mockServer.takeRequest(10L, TimeUnit.SECONDS);

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456");

        // Raw String + Dialect OnResponse, OnError
        mockServer.enqueue(createResponse(""));

        queryApi.queryRaw("from(bucket: \"telegraf\")", new Dialect(), (cancellable, s) -> {

        }, throwable -> {

        });

        request = mockServer.takeRequest(10L, TimeUnit.SECONDS);

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456");

        // Raw Query OnResponse, OnError
        mockServer.enqueue(createResponse(""));

        queryApi.queryRaw(new Query().query("from(bucket: \"telegraf\")"), (cancellable, s) -> {

        }, throwable -> {

        });

        request = mockServer.takeRequest(10L, TimeUnit.SECONDS);

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456");

        // Raw String OnResponse, OnError, OnComplete
        mockServer.enqueue(createResponse(""));

        queryApi.queryRaw("from(bucket: \"telegraf\")", (cancellable, s) -> {

        }, throwable -> {

        }, () -> {

        });

        request = mockServer.takeRequest(10L, TimeUnit.SECONDS);

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456");

        // Raw String + Dialect OnResponse, OnError, OnComplete
        mockServer.enqueue(createResponse(""));

        queryApi.queryRaw("from(bucket: \"telegraf\")", new Dialect(), (cancellable, s) -> {

        }, throwable -> {

        }, () -> {

        });

        request = mockServer.takeRequest(10L, TimeUnit.SECONDS);

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456");

        // Raw Query OnResponse, OnError, OnComplete
        mockServer.enqueue(createResponse(""));

        queryApi.queryRaw(new Query().query("from(bucket: \"telegraf\")"), (cancellable, s) -> {

        }, throwable -> {

        }, () -> {

        });

        request = mockServer.takeRequest(10L, TimeUnit.SECONDS);

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456");


    }
}