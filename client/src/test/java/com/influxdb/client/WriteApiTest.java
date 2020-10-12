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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.internal.AbstractInfluxDBClientTest;
import com.influxdb.client.write.Point;
import com.influxdb.client.write.events.WriteErrorEvent;
import com.influxdb.client.write.events.WriteRetriableErrorEvent;
import com.influxdb.client.write.events.WriteSuccessEvent;
import com.influxdb.exceptions.BadRequestException;
import com.influxdb.exceptions.ForbiddenException;
import com.influxdb.exceptions.InfluxException;
import com.influxdb.exceptions.RequestEntityTooLargeException;
import com.influxdb.exceptions.UnauthorizedException;

import io.reactivex.schedulers.TestScheduler;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (21/09/2018 11:36)
 */
@RunWith(JUnitPlatform.class)
class WriteApiTest extends AbstractInfluxDBClientTest {

    private WriteApi writeApi;
    private TestScheduler scheduler;

    @BeforeEach
    protected void setUp() {
        super.setUp();

        scheduler = new TestScheduler();
    }

    @AfterEach
    void tearDown() {
        if (writeApi != null) {
            writeApi.close();
        }
    }

    @Test
    void writePoint() throws InterruptedException {

        mockServer.enqueue(createResponse("{}"));

        writeApi = influxDBClient.getWriteApi();

        writeApi.writePoint("b1", "org1", Point.measurement("h2o").addTag("location", "europe").addField("level", 2));

        RecordedRequest request = takeRequest();

        // value
        Assertions.assertThat(request.getBody().readUtf8()).isEqualTo("h2o,location=europe level=2i");

        // organization
        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("org1");
        // bucket
        Assertions.assertThat(request.getRequestUrl().queryParameter("bucket")).isEqualTo("b1");
        // precision
        Assertions.assertThat(request.getRequestUrl().queryParameter("precision")).isEqualTo("ns");
    }

    @Test
    void writePointNull() {

        mockServer.enqueue(createResponse("{}"));

        writeApi = influxDBClient.getWriteApi();

        writeApi.writePoint("b1", "org1", null);

        Assertions.assertThat(mockServer.getRequestCount()).isEqualTo(0);
    }

    @Test
    void writePointDifferentPrecision() throws InterruptedException {

        mockServer.enqueue(createResponse("{}"));
        mockServer.enqueue(createResponse("{}"));

        writeApi = influxDBClient.getWriteApi(WriteOptions.builder().batchSize(1).build());

        Point point1 = Point.measurement("h2o").addTag("location", "europe").addField("level", 1).time(1L, WritePrecision.MS);
        Point point2 = Point.measurement("h2o").addTag("location", "europe").addField("level", 2).time(2L, WritePrecision.S);

        writeApi.writePoints("b1", "org1", Arrays.asList(point1, point2));

        RecordedRequest request = takeRequest();

        // request 1
        Assertions.assertThat(request.getBody().readUtf8()).isEqualTo("h2o,location=europe level=1i 1");
        Assertions.assertThat(request.getRequestUrl().queryParameter("precision")).isEqualTo("ms");

        request = takeRequest();

        Assertions.assertThat(request.getBody().readUtf8()).isEqualTo("h2o,location=europe level=2i 2");
        Assertions.assertThat(request.getRequestUrl().queryParameter("precision")).isEqualTo("s");

        Assertions.assertThat(mockServer.getRequestCount()).isEqualTo(2);
    }

    @Test
    void writeMeasurement() throws InterruptedException {

        mockServer.enqueue(new MockResponse());

        writeApi = influxDBClient.getWriteApi();

        H2OFeetMeasurement measurement = new H2OFeetMeasurement(
                "coyote_creek", 2.927, "below 3 feet", 1440046800L);

        // response
        writeApi.writeMeasurement("b1", "org1", WritePrecision.NS, measurement);

        RecordedRequest request = takeRequest();

        // value
        Assertions.assertThat(request.getBody().readUtf8()).isEqualTo("h2o,location=coyote_creek level\\ description=\"below 3 feet\",water_level=2.927 1440046800000000");

        // organization
        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("org1");
        // bucket
        Assertions.assertThat(request.getRequestUrl().queryParameter("bucket")).isEqualTo("b1");
        // precision
        Assertions.assertThat(request.getRequestUrl().queryParameter("precision")).isEqualTo("ns");
    }

    @Test
    void writeMeasurementInheritance() throws InterruptedException {

        mockServer.enqueue(new MockResponse());

        writeApi = influxDBClient.getWriteApi();

        Metric measurement = new Visitor();
        //noinspection CastCanBeRemovedNarrowingVariableType
        ((Visitor) measurement).count = 99;
        measurement.source = "metric-source";

        // response
        writeApi.writeMeasurement("b1", "org1", WritePrecision.S, measurement);

        RecordedRequest request = takeRequest();

        // value
        Assertions.assertThat(request.getBody().readUtf8()).isEqualTo("visitor,source=metric-source count=99i");

        // organization
        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("org1");
        // bucket
        Assertions.assertThat(request.getRequestUrl().queryParameter("bucket")).isEqualTo("b1");
        // precision
        Assertions.assertThat(request.getRequestUrl().queryParameter("precision")).isEqualTo("s");
    }

    @Test
    void writeMeasurementNull() {

        mockServer.enqueue(createResponse("{}"));

        writeApi = influxDBClient.getWriteApi();

        writeApi.writeMeasurement("b1", "org1", WritePrecision.S, null);

        Assertions.assertThat(mockServer.getRequestCount()).isEqualTo(0);
    }

    @Test
    void writeMeasurementWhichIsNotMappableToPoint() {

        writeApi = influxDBClient.getWriteApi();
        WriteEventListener<WriteErrorEvent> listener = new WriteEventListener<>();
        writeApi.listenEvents(WriteErrorEvent.class, listener);

        writeApi.writeMeasurement("b1", "org1", WritePrecision.S, 15);

        Assertions.assertThat(mockServer.getRequestCount()).isEqualTo(0);

       listener.awaitCount(1);

        Assertions.assertThat(listener.values).hasSize(1);
        Assertions.assertThat(listener.getValue()).isNotNull();
        Assertions.assertThat(listener.getValue().getThrowable())
                .isInstanceOf(InfluxException.class)
                .hasMessage("Measurement type 'class java.lang.Integer' does not have a @Measurement annotation.");
    }

    @Test
    void writeMeasurements() throws InterruptedException {

        writeApi = influxDBClient.getWriteApi(WriteOptions.builder().batchSize(1).build());

        mockServer.enqueue(new MockResponse());
        mockServer.enqueue(new MockResponse());

        H2OFeetMeasurement measurement1 = new H2OFeetMeasurement(
                "coyote_creek", 2.927, "below 3 feet", 1440046800L);

        H2OFeetMeasurement measurement2 = new H2OFeetMeasurement(
                "coyote_creek", 1.927, "below 2 feet", 1440049800L);

        writeApi.writeMeasurements("b1", "org1", WritePrecision.NS, Arrays.asList(measurement1, measurement2));


        RecordedRequest request = takeRequest();

        // request 1
        Assertions.assertThat(request.getBody().readUtf8()).isEqualTo("h2o,location=coyote_creek level\\ description=\"below 3 feet\",water_level=2.927 1440046800000000");
        Assertions.assertThat(request.getRequestUrl().queryParameter("precision")).isEqualTo("ns");

        request = takeRequest();

        Assertions.assertThat(request.getBody().readUtf8()).isEqualTo("h2o,location=coyote_creek level\\ description=\"below 2 feet\",water_level=1.927 1440049800000000");
        Assertions.assertThat(request.getRequestUrl().queryParameter("precision")).isEqualTo("ns");
        
        Assertions.assertThat(mockServer.getRequestCount()).isEqualTo(2);
    }

    @Test
    void requestParameters() throws InterruptedException {

        mockServer.enqueue(createResponse("{}"));

        writeApi = influxDBClient.getWriteApi();

        writeApi.writeRecord("b1", "org1", WritePrecision.NS,
                "h2o_feet,location=coyote_creek level\\ description=\"feet 1\",water_level=1.0 1");

        RecordedRequest request = takeRequest();

        // organization
        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("org1");
        // bucket
        Assertions.assertThat(request.getRequestUrl().queryParameter("bucket")).isEqualTo("b1");
        // precision
        Assertions.assertThat(request.getRequestUrl().queryParameter("precision")).isEqualTo("ns");
    }

    @Test
    void emptyRequest() {

        writeApi = influxDBClient.getWriteApi();

        WriteEventListener<WriteErrorEvent> listener = new WriteEventListener<>();
        writeApi.listenEvents(WriteErrorEvent.class, listener);

        writeApi.writeRecords("b1", "org1", WritePrecision.NS, Lists.emptyList());
        writeApi.writeRecord("b1", "org1", WritePrecision.NS, null);
        writeApi.writeRecord("b1", "org1", WritePrecision.NS, "");

        Assertions.assertThat(mockServer.getRequestCount()).isEqualTo(0);

        Assertions.assertThat(listener.values).hasSize(0);
    }

    @Test
    void precision() throws InterruptedException {

        mockServer.enqueue(createResponse("{}"));
        mockServer.enqueue(createResponse("{}"));
        mockServer.enqueue(createResponse("{}"));
        mockServer.enqueue(createResponse("{}"));

        writeApi = influxDBClient.getWriteApi(WriteOptions.builder().batchSize(1).build());

        String record1 = "h2o_feet,location=coyote_creek level\\ description=\"feet 1\",water_level=1.0 1";
        writeApi.writeRecord("b1", "org1", WritePrecision.NS, record1);

        String record2 = "h2o_feet,location=coyote_creek level\\ description=\"feet 2\",water_level=2.0 2";
        writeApi.writeRecord("b1", "org1", WritePrecision.US, record2);

        String record3 = "h2o_feet,location=coyote_creek level\\ description=\"feet 3\",water_level=3.0 3";
        writeApi.writeRecord("b1", "org1", WritePrecision.MS, record3);

        String record4 = "h2o_feet,location=coyote_creek level\\ description=\"feet 4\",water_level=4.0 4";
        writeApi.writeRecord("b1", "org1", WritePrecision.S, record4);

        RecordedRequest request1 = takeRequest();
        Assertions.assertThat(request1.getRequestUrl().queryParameter("precision")).isEqualTo("ns");

        RecordedRequest request2 = takeRequest();
        Assertions.assertThat(request2.getRequestUrl().queryParameter("precision")).isEqualTo("us");

        RecordedRequest request3 = takeRequest();
        Assertions.assertThat(request3.getRequestUrl().queryParameter("precision")).isEqualTo("ms");

        RecordedRequest request4 = takeRequest();
        Assertions.assertThat(request4.getRequestUrl().queryParameter("precision")).isEqualTo("s");
    }

    @Test
    void batching() {

        enqueuedResponse();
        enqueuedResponse();

        writeApi = influxDBClient.getWriteApi(WriteOptions.builder().flushInterval(10_000).batchSize(2).build());

        String record1 = "h2o_feet,location=coyote_creek level\\ description=\"feet 1\",water_level=1.0 1";
        String record2 = "h2o_feet,location=coyote_creek level\\ description=\"feet 2\",water_level=2.0 2";
        String record3 = "h2o_feet,location=coyote_creek level\\ description=\"feet 3\",water_level=3.0 3";
        String record4 = "h2o_feet,location=coyote_creek level\\ description=\"feet 4\",water_level=4.0 4";

        writeApi.writeRecords("b1", "org1", WritePrecision.NS, Arrays.asList(record1, record2, record3, record4));

        String body1 = getRequestBody(mockServer);
        Assertions.assertThat(body1).isEqualTo(record1 + "\n" + record2);

        String body2 = getRequestBody(mockServer);
        Assertions.assertThat(body2).isEqualTo(record3 + "\n" + record4);
    }

    @Test
    void batchingOne() {

        mockServer.enqueue(createResponse("{}"));
        mockServer.enqueue(createResponse("{}"));
        mockServer.enqueue(createResponse("{}"));
        mockServer.enqueue(createResponse("{}"));

        writeApi = influxDBClient.getWriteApi(WriteOptions.builder().batchSize(1).flushInterval(10_00000).build());

        String record1 = "h2o_feet,location=coyote_creek level\\ description=\"feet 1\",water_level=1.0 1";
        String record2 = "h2o_feet,location=coyote_creek level\\ description=\"feet 2\",water_level=2.0 2";
        String record3 = "h2o_feet,location=coyote_creek level\\ description=\"feet 3\",water_level=3.0 3";
        String record4 = "h2o_feet,location=coyote_creek level\\ description=\"feet 4\",water_level=4.0 4";

        writeApi.writeRecord("b1", "org1", WritePrecision.NS, record1);
        writeApi.writeRecord("b1", "org1", WritePrecision.NS, record2);
        writeApi.writeRecord("b1", "org1", WritePrecision.NS, record3);
        writeApi.writeRecord("b1", "org1", WritePrecision.NS, record4);

        String body1 = getRequestBody(mockServer);
        Assertions.assertThat(body1).isEqualTo(record1);

        String body2 = getRequestBody(mockServer);
        Assertions.assertThat(body2).isEqualTo(record2);

        String body3 = getRequestBody(mockServer);
        Assertions.assertThat(body3).isEqualTo(record3);

        String body4 = getRequestBody(mockServer);
        Assertions.assertThat(body4).isEqualTo(record4);
    }

    @Test
    void listAsMoreBatchUnits() {

        mockServer.enqueue(createResponse("{}"));
        mockServer.enqueue(createResponse("{}"));
        mockServer.enqueue(createResponse("{}"));
        mockServer.enqueue(createResponse("{}"));

        writeApi = influxDBClient.getWriteApi(WriteOptions.builder().batchSize(1).build());

        String record1 = "h2o_feet,location=coyote_creek level\\ description=\"feet 1\",water_level=1.0 1";
        String record2 = "h2o_feet,location=coyote_creek level\\ description=\"feet 2\",water_level=2.0 2";
        String record3 = "h2o_feet,location=coyote_creek level\\ description=\"feet 3\",water_level=3.0 3";
        String record4 = "h2o_feet,location=coyote_creek level\\ description=\"feet 4\",water_level=4.0 4";

        List<String> records = Lists.list(record1, record2, record3, record4);
        writeApi.writeRecords("b1", "org1", WritePrecision.NS, records);

        String body1 = getRequestBody(mockServer);
        Assertions.assertThat(body1).isEqualTo(record1);

        String body2 = getRequestBody(mockServer);
        Assertions.assertThat(body2).isEqualTo(record2);

        String body3 = getRequestBody(mockServer);
        Assertions.assertThat(body3).isEqualTo(record3);

        String body4 = getRequestBody(mockServer);
        Assertions.assertThat(body4).isEqualTo(record4);
    }

    @Test
    void flushByCount() {

        mockServer.enqueue(createResponse("{}"));
        mockServer.enqueue(createResponse("{}"));
        mockServer.enqueue(createResponse("{}"));
        mockServer.enqueue(createResponse("{}"));
        mockServer.enqueue(createResponse("{}"));
        mockServer.enqueue(createResponse("{}"));
        mockServer.enqueue(createResponse("{}"));
        mockServer.enqueue(createResponse("{}"));
        mockServer.enqueue(createResponse("{}"));
        mockServer.enqueue(createResponse("{}"));

        WriteOptions writeOptions = WriteOptions.builder()
                .bufferLimit(100_000)
                .batchSize(10_000)
                .flushInterval(100_000_000)
                .build();

        writeApi = influxDBClient.getWriteApi(writeOptions);

        WriteEventListener<WriteSuccessEvent> listener = new WriteEventListener<>();
        writeApi.listenEvents(WriteSuccessEvent.class, listener);

        for (int i = 0; i < 100_000; i++) {

            writeApi.writeRecord("my-bucket", "my-org", WritePrecision.S,
                    String.format("sensor_1569839027289,id=1120 temperature=1569839028399 %s", i));
        }

        listener.awaitCount(10);

        Assertions.assertThat(mockServer.getRequestCount()).isEqualTo(10);
    }

    @Test
    void flushByDuration() {

        mockServer.enqueue(createResponse("{}"));

        WriteOptions writeOptions = WriteOptions.builder()
                .batchSize(10)
                .flushInterval(1_000_000)
                .writeScheduler(scheduler)
                .build();

        writeApi = influxDBClient.getWriteApi(writeOptions);
        
        WriteEventListener<WriteSuccessEvent> listener = new WriteEventListener<>();
        writeApi.listenEvents(WriteSuccessEvent.class, listener);


        writeApi.writeRecord("b1", "org1", WritePrecision.NS,
                "h2o_feet,location=coyote_creek level\\ description=\"feet 1\",water_level=1.0 1");

        Assertions.assertThat(mockServer.getRequestCount()).isEqualTo(0);

        scheduler.advanceTimeBy(1_100, TimeUnit.SECONDS);

        listener.awaitCount(1);

        Assertions.assertThat(mockServer.getRequestCount()).isEqualTo(1);
    }

    @Test
    void jitterInterval() {

        mockServer.enqueue(createResponse("{}"));

        // after 5 batchSize or 10 seconds + 5 seconds jitter interval
        WriteOptions writeOptions = WriteOptions.builder()
                .batchSize(5)
                .flushInterval(10_000)
                .jitterInterval(5_000)
                .writeScheduler(scheduler)
                .build();

        writeApi = influxDBClient.getWriteApi(writeOptions);
        WriteEventListener<WriteSuccessEvent> listener = new WriteEventListener<>();
        writeApi.listenEvents(WriteSuccessEvent.class, listener);

        writeApi.writeRecord("b1", "org1", WritePrecision.NS,
                "h2o_feet,location=coyote_creek level\\ description=\"feet 1\",water_level=1.0 1");

        // move time to feature by 10 seconds - flush interval elapsed
        scheduler.advanceTimeBy(10, TimeUnit.SECONDS);

        // without call remote api
        Assertions.assertThat(mockServer.getRequestCount()).isEqualTo(0);

        // move time to feature by 5 seconds - jitter interval elapsed
        scheduler.advanceTimeBy(6, TimeUnit.SECONDS);

        listener.awaitCount(1);

        // was call remote API
        Assertions.assertThat(mockServer.getRequestCount()).isEqualTo(1);
    }

    @Test
    void flushBeforeClose() {

        mockServer.enqueue(createResponse("{}"));

        writeApi = influxDBClient.getWriteApi();

        writeApi.writeRecord("b1", "org1", WritePrecision.NS,
                "h2o_feet,location=coyote_creek level\\ description=\"feet 1\",water_level=1.0 1");

        Assertions.assertThat(mockServer.getRequestCount()).isEqualTo(0);

        writeApi.close();

        // wait for request
        getRequestBody(mockServer);

        Assertions.assertThat(mockServer.getRequestCount()).isEqualTo(1);
    }

    @Test
    void eventWriteSuccessEvent() {

        mockServer.enqueue(createResponse("{}"));

        writeApi = influxDBClient.getWriteApi(WriteOptions.builder().batchSize(1).build());

        WriteEventListener<WriteSuccessEvent> listener = new WriteEventListener<>();
        writeApi.listenEvents(WriteSuccessEvent.class, listener);

        writeApi.writeRecord("b1", "org1", WritePrecision.NS,
                "h2o_feet,location=coyote_creek level\\ description=\"feet 1\",water_level=1.0 1");

        // wait for request
        getRequestBody(mockServer);

        listener.awaitCount(1);

        Assertions.assertThat(listener.getValue()).isNotNull();
        Assertions.assertThat(listener.errors).isEmpty();
        Assertions.assertThat(listener.values).hasSize(1);

        Assertions.assertThat(listener.getValue().getBucket()).isEqualTo("b1");
        Assertions.assertThat(listener.getValue().getOrganization()).isEqualTo("org1");
        Assertions.assertThat(listener.getValue().getPrecision()).isEqualTo(WritePrecision.NS);
        Assertions.assertThat(listener.getValue().getLineProtocol()).isEqualTo("h2o_feet,location=coyote_creek level\\ description=\"feet 1\",water_level=1.0 1");
    }

    @Test
    void eventWriteSuccessEventDispose() {

        mockServer.enqueue(createResponse("{}"));

        writeApi = influxDBClient.getWriteApi(WriteOptions.builder().batchSize(1).build());

        WriteEventListener<WriteSuccessEvent> listener = new WriteEventListener<>();
        writeApi.listenEvents(WriteSuccessEvent.class, listener).dispose();

        writeApi.writeRecord("b1", "org1", WritePrecision.NS,
                "h2o_feet,location=coyote_creek level\\ description=\"feet 1\",water_level=1.0 1");

        // wait for request
        getRequestBody(mockServer);

        Assertions.assertThat(listener.errors).isEmpty();
        Assertions.assertThat(listener.values).isEmpty();
    }

    @Test
    void eventUnhandledErrorEvent() {

        mockServer.enqueue(createErrorResponse("no token was sent and they are required", true, 403));

        writeApi = influxDBClient.getWriteApi(WriteOptions.builder().batchSize(1).build());
        WriteEventListener<WriteErrorEvent> listener = new WriteEventListener<>();
        writeApi.listenEvents(WriteErrorEvent.class, listener);

        writeApi.writeRecord("b1", "org1", WritePrecision.NS,
                "h2o_feet,location=coyote_creek level\\ description=\"feet 1\",water_level=1.0 1");

        // wait for request
        getRequestBody(mockServer);

        listener.awaitCount(1);

        Assertions.assertThat(listener.getValue()).isNotNull();
        Assertions.assertThat(listener.getValue().getThrowable()).isNotNull();
        Assertions.assertThat(listener.getValue().getThrowable())
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("no token was sent and they are required");
    }

    @Test
    void retry() {

        mockServer.enqueue(createErrorResponse("token is temporarily over quota", true, 429));
        mockServer.enqueue(createResponse("{}"));

        writeApi = influxDBClient.getWriteApi();

        writeApi.writePoint("b1", "org1", Point.measurement("h2o").addTag("location", "europe").addField("level", 2));

        String body1 = getRequestBody(mockServer);
        Assertions.assertThat(body1).isEqualTo("h2o,location=europe level=2i");

        String body2 = getRequestBody(mockServer);
        Assertions.assertThat(body2).isEqualTo("h2o,location=europe level=2i");

        Assertions.assertThat(mockServer.getRequestCount())
                .isEqualTo(2);
    }

    @Test
    void retryWithRetryAfter() throws InterruptedException {

        MockResponse errorResponse = createErrorResponse("token is temporarily over quota", true, 429);
        errorResponse.addHeader("Retry-After", 5);
        mockServer.enqueue(errorResponse);
        mockServer.enqueue(createResponse("{}"));

        writeApi = influxDBClient.getWriteApi(WriteOptions.builder().batchSize(1).build());

        WriteEventListener<WriteRetriableErrorEvent> retriableListener = new WriteEventListener<>();
        writeApi.listenEvents(WriteRetriableErrorEvent.class, retriableListener);

        WriteEventListener<WriteSuccessEvent> successListener = new WriteEventListener<>();
        writeApi.listenEvents(WriteSuccessEvent.class, successListener);

        writeApi.writePoint("b1", "org1", Point.measurement("h2o").addTag("location", "europe").addField("level", 2));

        retriableListener.awaitCount(1);
        Assertions.assertThat(retriableListener.getValue().getThrowable())
                .isInstanceOf(InfluxException.class)
                .hasMessage("token is temporarily over quota");
        
        Assertions.assertThat(retriableListener.getValue().getRetryInterval()).isEqualTo(5000);

        Thread.sleep(2_000);

        Assertions.assertThat(successListener.values).hasSize(0);
        successListener.awaitCount(1);

        String body1 = getRequestBody(mockServer);
        Assertions.assertThat(body1).isEqualTo("h2o,location=europe level=2i");

        String body2 = getRequestBody(mockServer);
        Assertions.assertThat(body2).isEqualTo("h2o,location=europe level=2i");

        Assertions.assertThat(mockServer.getRequestCount()).isEqualTo(2);
    }

    @Test
    void retryNotApplied() {

        mockServer.enqueue(createErrorResponse("line protocol poorly formed and no points were written", true, 400));
        mockServer.enqueue(createErrorResponse("token does not have sufficient permissions to write to this organization and bucket or the organization and bucket do not exist", true, 401));
        mockServer.enqueue(createErrorResponse("no token was sent and they are required", true, 403));
        mockServer.enqueue(createErrorResponse("write has been rejected because the payload is too large. Error message returns max size supported. All data in body was rejected and not written", true, 413));

        writeApi = influxDBClient.getWriteApi();

        WriteEventListener<WriteErrorEvent> listener = new WriteEventListener<>();
        writeApi.listenEvents(WriteErrorEvent.class, listener);

        //
        // 400
        //
        writeApi.writeRecord("b1", "org1", WritePrecision.NS,
                "h2o_feet,location=coyote_creek level\\ description=\"feet 1\",water_level=1.0 1");

        WriteErrorEvent error = listener.awaitCount(1).popValue();
        Assertions.assertThat(error.getThrowable())
                .isInstanceOf(BadRequestException.class)
                .hasMessage("line protocol poorly formed and no points were written");

        //
        // 401
        //
        writeApi.writeRecord("b1", "org1", WritePrecision.NS,
                "h2o_feet,location=coyote_creek level\\ description=\"feet 1\",water_level=1.0 1");
        
        error = listener.awaitCount(1).popValue();
        Assertions.assertThat(error.getThrowable())
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("token does not have sufficient permissions to write to this organization and bucket or the organization and bucket do not exist");

        //
        // 403
        //
        writeApi.writeRecord("b1", "org1", WritePrecision.NS,
                "h2o_feet,location=coyote_creek level\\ description=\"feet 1\",water_level=1.0 1");

        error = listener.awaitCount(1).popValue();
        Assertions.assertThat(error.getThrowable())
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("no token was sent and they are required");

        //
        // 413
        //
        writeApi.writeRecord("b1", "org1", WritePrecision.NS,
                "h2o_feet,location=coyote_creek level\\ description=\"feet 1\",water_level=1.0 1");

        error = listener.awaitCount(1).popValue();
        Assertions.assertThat(error.getThrowable())
                .isInstanceOf(RequestEntityTooLargeException.class)
                .hasMessage("write has been rejected because the payload is too large. Error message returns max size supported. All data in body was rejected and not written");

        Assertions.assertThat(mockServer.getRequestCount())
                .isEqualTo(4);
    }

    @Test
    void retryOnNetworkError() throws IOException, InterruptedException {

        mockServer.close();

        WriteOptions options = WriteOptions.builder()
                .batchSize(1)
                .maxRetryDelay(2_000)
                .maxRetries(3)
                .build();
        writeApi = influxDBClient.getWriteApi(options);

        WriteEventListener<WriteRetriableErrorEvent> retriableListener = new WriteEventListener<>();
        writeApi.listenEvents(WriteRetriableErrorEvent.class, retriableListener);

        writeApi.writePoint("b1", "org1", Point.measurement("h2o").addTag("location", "europe").addField("level", 2));

        Thread.sleep(7_000);

        retriableListener.awaitCount(3);
    }

    @Test
    public void retryContainsMessage() {

        MockLogHandler handler = new MockLogHandler();

        final Logger logger = Logger.getLogger(WriteRetriableErrorEvent.class.getName());
        logger.addHandler(handler);

        MockResponse errorResponse = new MockResponse()
                .setResponseCode(429)
                .addHeader("Retry-After", 5)
                .setBody("{\"code\":\"too many requests\",\"message\":\"org 04014de4ed590000 has exceeded limited_write plan limit\"}");
        mockServer.enqueue(errorResponse);
        mockServer.enqueue(createResponse("{}"));

        writeApi = influxDBClient.getWriteApi(WriteOptions.builder().batchSize(1).build());

        WriteEventListener<WriteRetriableErrorEvent> listener = new WriteEventListener<>();
        writeApi.listenEvents(WriteRetriableErrorEvent.class, listener);

        writeApi.writePoint("b1", "org1", Point.measurement("h2o").addTag("location", "europe").addField("level", 2));

        listener.awaitCount(1);

        List<LogRecord> records = handler.getRecords(Level.WARNING);

        Assertions.assertThat(records).hasSize(1);
        Assertions.assertThat(records.get(0).getMessage())
                .isEqualTo("The retriable error occurred during writing of data. Reason: '{0}'. Retry in: {1}ms.");
        Assertions.assertThat(records.get(0).getParameters()).hasSize(2);
        Assertions.assertThat(records.get(0).getParameters()[0]).isEqualTo("org 04014de4ed590000 has exceeded limited_write plan limit");
        Assertions.assertThat(records.get(0).getParameters()[1]).isEqualTo(5000L);
    }

    @Test
    void parametersFromOptions() throws InterruptedException, IOException {

        tearDown();
        after();

        InfluxDBClientOptions options = InfluxDBClientOptions.builder()
                .url(startMockServer())
                .bucket("my-top-bucket")
                .org("123456")
                .build();

        influxDBClient = InfluxDBClientFactory.create(options);

        writeApi = influxDBClient.getWriteApi(WriteOptions.builder().batchSize(1).build());

        // Points
        mockServer.enqueue(createResponse("{}"));

        writeApi.writePoints(Collections.singletonList(Point.measurement("h2o")
                .addTag("location", "europe")
                .addField("level", 1)
                .time(1L, WritePrecision.MS)));

        RecordedRequest request = takeRequest();

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456");
        Assertions.assertThat(request.getRequestUrl().queryParameter("bucket")).isEqualTo("my-top-bucket");

        // Point
        mockServer.enqueue(createResponse("{}"));

        writeApi.writePoint(Point.measurement("h2o")
                .addTag("location", "europe")
                .addField("level", 1)
                .time(1L, WritePrecision.MS));

        request = takeRequest();

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456");
        Assertions.assertThat(request.getRequestUrl().queryParameter("bucket")).isEqualTo("my-top-bucket");

        // Record
        mockServer.enqueue(createResponse("{}"));

        writeApi.writeRecord(WritePrecision.NS, "h2o_feet,location=coyote_creek level\\ description=\"feet 1\",water_level=1.0 1");

        request = takeRequest();

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456");
        Assertions.assertThat(request.getRequestUrl().queryParameter("bucket")).isEqualTo("my-top-bucket");

        // Records
        mockServer.enqueue(createResponse("{}"));

        writeApi.writeRecords(WritePrecision.NS, Collections.singletonList("h2o_feet,location=coyote_creek level\\ description=\"feet 1\",water_level=1.0 1"));

        request = takeRequest();

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456");
        Assertions.assertThat(request.getRequestUrl().queryParameter("bucket")).isEqualTo("my-top-bucket");

        // Measurement
        mockServer.enqueue(createResponse("{}"));

        writeApi.writeMeasurement(WritePrecision.NS, new H2OFeetMeasurement(
                "coyote_creek", 2.927, "below 3 feet", 1440046800L));

        request = takeRequest();

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456");
        Assertions.assertThat(request.getRequestUrl().queryParameter("bucket")).isEqualTo("my-top-bucket");

        // Measurements
        mockServer.enqueue(createResponse("{}"));

        writeApi.writeMeasurements(WritePrecision.NS, Collections.singletonList(new H2OFeetMeasurement(
                "coyote_creek", 2.927, "below 3 feet", 1440046800L)));

        request = takeRequest();

        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("123456");
        Assertions.assertThat(request.getRequestUrl().queryParameter("bucket")).isEqualTo("my-top-bucket");
    }

    @Test
    public void callingClose() {
        
        writeApi = influxDBClient.getWriteApi();
        writeApi.close();
        writeApi.close();

        influxDBClient.close();
        influxDBClient.close();
        writeApi.close();
    }

    @Test
    public void writeToClosedClient() {
        
        WriteApi writeApiSelfClose = influxDBClient.getWriteApi();
        writeApiSelfClose.close();

        Assertions.assertThatThrownBy(() -> writeApiSelfClose.writeRecord("b1", "org1", WritePrecision.NS,"h2o_feet water_level=1.0 1"))
                .isInstanceOf(InfluxException.class)
                .hasMessage("WriteApi is closed. Data should be written before calling InfluxDBClient.close or WriteApi.close.");

        WriteApi writeApiClientClose = influxDBClient.getWriteApi();
        influxDBClient.close();

        Assertions.assertThatThrownBy(() -> writeApiClientClose.writePoint("b1", "org1", Point.measurement("h2o").addField("level", 1)))
                .isInstanceOf(InfluxException.class)
                .hasMessage("WriteApi is closed. Data should be written before calling InfluxDBClient.close or WriteApi.close.");
    }

    @Test
    void userAgent() throws InterruptedException {

        mockServer.enqueue(createResponse("{}"));

        writeApi = influxDBClient.getWriteApi();

        writeApi.writeRecord("b1", "org1", WritePrecision.NS, "h2o,location=coyote_creek level\\ description=\"below 3 feet\",water_level=2.927 1440046800000000");

        RecordedRequest recordedRequest = takeRequest();

        String userAgent = recordedRequest.getHeader("User-Agent");

        Assertions.assertThat(userAgent).startsWith("influxdb-client-java/1.");
    }

    @Nonnull
    private String getRequestBody(@Nonnull final MockWebServer server) {

        Assertions.assertThat(server).isNotNull();

        RecordedRequest recordedRequest = null;
        try {
            recordedRequest = server.takeRequest(10L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Assertions.fail("Unexpected exception", e);
        }
        Assertions.assertThat(recordedRequest).isNotNull();

        return recordedRequest.getBody().readUtf8();
    }

    public abstract class Metric {
        @Column(name = "source", tag = true)
        private String source;
    }


    @Measurement(name = "visitor")
    public class Visitor extends Metric {
        @Column(name = "count")
        private long count;
    }

}