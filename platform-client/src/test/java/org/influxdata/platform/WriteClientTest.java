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
package org.influxdata.platform;

import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

import org.influxdata.platform.error.InfluxException;
import org.influxdata.platform.impl.AbstractPlatformClientTest;
import org.influxdata.platform.option.WriteOptions;
import org.influxdata.platform.write.Point;
import org.influxdata.platform.write.event.BackpressureEvent;
import org.influxdata.platform.write.event.WriteErrorEvent;
import org.influxdata.platform.write.event.WriteSuccessEvent;

import io.reactivex.Flowable;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.schedulers.TestScheduler;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (21/09/2018 11:36)
 */
@RunWith(JUnitPlatform.class)
class WriteClientTest extends AbstractPlatformClientTest {

    private WriteClient writeClient;
    private TestScheduler scheduler;

    @BeforeEach
    protected void setUp() {
        super.setUp();

        scheduler = new TestScheduler();
    }

    @AfterEach
    void tearDown() {
        if (writeClient != null) {
            writeClient.close();
        }
    }

    @Test
    void writePoint() throws InterruptedException {

        mockServer.enqueue(createResponse("{}"));

        writeClient = createWriteClient();

        writeClient.writePoint("b1", "org1", Point.measurement("h2o").addTag("location", "europe").addField("level", 2));

        RecordedRequest request = mockServer.takeRequest(10L, TimeUnit.SECONDS);

        // value
        Assertions.assertThat(request.getBody().readUtf8()).isEqualTo("h2o,location=europe level=2i");

        // organization
        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("org1");
        // bucket
        Assertions.assertThat(request.getRequestUrl().queryParameter("bucket")).isEqualTo("b1");
        // precision
        Assertions.assertThat(request.getRequestUrl().queryParameter("precision")).isEqualTo("n");
    }

    @Test
    void writePointNull() {

        mockServer.enqueue(createResponse("{}"));

        writeClient = createWriteClient();

        writeClient.writePoint("b1", "org1", null);

        Assertions.assertThat(mockServer.getRequestCount()).isEqualTo(0);
    }

    @Test
    void writePointDifferentPrecision() throws InterruptedException {

        mockServer.enqueue(createResponse("{}"));
        mockServer.enqueue(createResponse("{}"));

        writeClient = createWriteClient(WriteOptions.builder().batchSize(1).build());

        Point point1 = Point.measurement("h2o").addTag("location", "europe").addField("level", 1).time(1L, ChronoUnit.MILLIS);
        Point point2 = Point.measurement("h2o").addTag("location", "europe").addField("level", 2).time(2L, ChronoUnit.SECONDS);

        writeClient.writePoints("b1", "org1", Arrays.asList(point1, point2));

        RecordedRequest request = mockServer.takeRequest(10L, TimeUnit.SECONDS);

        // request 1
        Assertions.assertThat(request.getBody().readUtf8()).isEqualTo("h2o,location=europe level=1i 1");
        Assertions.assertThat(request.getRequestUrl().queryParameter("precision")).isEqualTo("ms");

        request = mockServer.takeRequest(10L, TimeUnit.SECONDS);

        Assertions.assertThat(request.getBody().readUtf8()).isEqualTo("h2o,location=europe level=2i 2");
        Assertions.assertThat(request.getRequestUrl().queryParameter("precision")).isEqualTo("s");

        Assertions.assertThat(mockServer.getRequestCount()).isEqualTo(2);
    }

    @Test
    void writeMeasurement() throws InterruptedException {

        mockServer.enqueue(new MockResponse());

        writeClient = createWriteClient();

        H2OFeetMeasurement measurement = new H2OFeetMeasurement(
                "coyote_creek", 2.927, "below 3 feet", 1440046800L);

        // response
        writeClient.writeMeasurement("b1", "org1", ChronoUnit.NANOS, measurement);

        RecordedRequest request = mockServer.takeRequest(10L, TimeUnit.SECONDS);

        // value
        Assertions.assertThat(request.getBody().readUtf8()).isEqualTo("h2o,location=coyote_creek level\\ description=\"below 3 feet\",water_level=2.927 1440046800000000");

        // organization
        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("org1");
        // bucket
        Assertions.assertThat(request.getRequestUrl().queryParameter("bucket")).isEqualTo("b1");
        // precision
        Assertions.assertThat(request.getRequestUrl().queryParameter("precision")).isEqualTo("n");
    }

    @Test
    void writeMeasurementNull() {

        mockServer.enqueue(createResponse("{}"));

        writeClient = createWriteClient();

        writeClient.writeMeasurement("b1", "org1", ChronoUnit.SECONDS, null);

        Assertions.assertThat(mockServer.getRequestCount()).isEqualTo(0);
    }

    @Test
    void writeMeasurementWhichIsNotMappableToPoint() {

        writeClient = createWriteClient();
        TestObserver<WriteErrorEvent> listener = writeClient.listenEvents(WriteErrorEvent.class).test();

        writeClient.writeMeasurement("b1", "org1", ChronoUnit.SECONDS, 15);

        Assertions.assertThat(mockServer.getRequestCount()).isEqualTo(0);

        listener
                .assertValue(event -> {

                    Assertions.assertThat(event).isNotNull();
                    Assertions.assertThat(event.getThrowable()).isNotNull();
                    Assertions.assertThat(event.getThrowable())
                            .isInstanceOf(InfluxException.class)
                            .hasMessage("Measurement type 'class java.lang.Integer' does not have a @Measurement annotation.");

                    return true;
                })
                .assertSubscribed()
                .assertNotComplete();
    }

    @Test
    void writeMeasurements() throws InterruptedException {

        writeClient = createWriteClient(WriteOptions.builder().batchSize(1).build());

        mockServer.enqueue(new MockResponse());
        mockServer.enqueue(new MockResponse());

        H2OFeetMeasurement measurement1 = new H2OFeetMeasurement(
                "coyote_creek", 2.927, "below 3 feet", 1440046800L);

        H2OFeetMeasurement measurement2 = new H2OFeetMeasurement(
                "coyote_creek", 1.927, "below 2 feet", 1440049800L);

        writeClient.writeMeasurements("b1", "org1", ChronoUnit.NANOS, Arrays.asList(measurement1, measurement2));


        RecordedRequest request = mockServer.takeRequest(10L, TimeUnit.SECONDS);

        // request 1
        Assertions.assertThat(request.getBody().readUtf8()).isEqualTo("h2o,location=coyote_creek level\\ description=\"below 3 feet\",water_level=2.927 1440046800000000");
        Assertions.assertThat(request.getRequestUrl().queryParameter("precision")).isEqualTo("n");

        request = mockServer.takeRequest(10L, TimeUnit.SECONDS);

        Assertions.assertThat(request.getBody().readUtf8()).isEqualTo("h2o,location=coyote_creek level\\ description=\"below 2 feet\",water_level=1.927 1440049800000000");
        Assertions.assertThat(request.getRequestUrl().queryParameter("precision")).isEqualTo("n");
        
        Assertions.assertThat(mockServer.getRequestCount()).isEqualTo(2);
    }

    @Test
    void requestParameters() throws InterruptedException {

        mockServer.enqueue(createResponse("{}"));

        writeClient = createWriteClient();

        writeClient.writeRecord("b1", "org1", ChronoUnit.NANOS,
                "h2o_feet,location=coyote_creek level\\ description=\"feet 1\",water_level=1.0 1");

        RecordedRequest request = mockServer.takeRequest(10L, TimeUnit.SECONDS);

        // organization
        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("org1");
        // bucket
        Assertions.assertThat(request.getRequestUrl().queryParameter("bucket")).isEqualTo("b1");
        // precision
        Assertions.assertThat(request.getRequestUrl().queryParameter("precision")).isEqualTo("n");
    }

    @Test
    void emptyRequest() {

        writeClient = createWriteClient();
        TestObserver<WriteErrorEvent> listener = writeClient.listenEvents(WriteErrorEvent.class).test();

        writeClient.writeRecords("b1", "org1", ChronoUnit.NANOS, Lists.emptyList());
        writeClient.writeRecord("b1", "org1", ChronoUnit.NANOS, null);
        writeClient.writeRecord("b1", "org1", ChronoUnit.NANOS, "");

        Assertions.assertThat(mockServer.getRequestCount()).isEqualTo(0);

        listener.assertNoErrors();
    }

    @Test
    void precision() throws InterruptedException {

        mockServer.enqueue(createResponse("{}"));
        mockServer.enqueue(createResponse("{}"));
        mockServer.enqueue(createResponse("{}"));
        mockServer.enqueue(createResponse("{}"));

        writeClient = createWriteClient(WriteOptions.builder().batchSize(1).build());

        String record1 = "h2o_feet,location=coyote_creek level\\ description=\"feet 1\",water_level=1.0 1";
        writeClient.writeRecord("b1", "org1", ChronoUnit.NANOS, record1);

        String record2 = "h2o_feet,location=coyote_creek level\\ description=\"feet 2\",water_level=2.0 2";
        writeClient.writeRecord("b1", "org1", ChronoUnit.MICROS, record2);

        String record3 = "h2o_feet,location=coyote_creek level\\ description=\"feet 3\",water_level=3.0 3";
        writeClient.writeRecord("b1", "org1", ChronoUnit.MILLIS, record3);

        String record4 = "h2o_feet,location=coyote_creek level\\ description=\"feet 4\",water_level=4.0 4";
        writeClient.writeRecord("b1", "org1", ChronoUnit.SECONDS, record4);

        RecordedRequest request1 = mockServer.takeRequest(10L, TimeUnit.SECONDS);
        Assertions.assertThat(request1.getRequestUrl().queryParameter("precision")).isEqualTo("n");

        RecordedRequest request2 = mockServer.takeRequest(10L, TimeUnit.SECONDS);
        Assertions.assertThat(request2.getRequestUrl().queryParameter("precision")).isEqualTo("u");

        RecordedRequest request3 = mockServer.takeRequest(10L, TimeUnit.SECONDS);
        Assertions.assertThat(request3.getRequestUrl().queryParameter("precision")).isEqualTo("ms");

        RecordedRequest request4 = mockServer.takeRequest(10L, TimeUnit.SECONDS);
        Assertions.assertThat(request4.getRequestUrl().queryParameter("precision")).isEqualTo("s");

        Assertions.assertThatThrownBy(() ->
                writeClient.writeRecord("b1", "org1", ChronoUnit.MINUTES, record1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Precision must be one of: [Nanos, Micros, Millis, Seconds]");

        Assertions.assertThatThrownBy(() ->
                writeClient.writeRecord("b1", "org1", ChronoUnit.HOURS, record1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Precision must be one of: [Nanos, Micros, Millis, Seconds]");

        Assertions.assertThatThrownBy(() ->
                writeClient.writeRecord("b1", "org1", ChronoUnit.DAYS, record1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Precision must be one of: [Nanos, Micros, Millis, Seconds]");
    }

    @Test
    void batching() {

        mockServer.enqueue(createResponse("{}"));
        mockServer.enqueue(createResponse("{}"));

        writeClient = createWriteClient(WriteOptions.builder().flushInterval(10_000).batchSize(2).build());

        String record1 = "h2o_feet,location=coyote_creek level\\ description=\"feet 1\",water_level=1.0 1";
        String record2 = "h2o_feet,location=coyote_creek level\\ description=\"feet 2\",water_level=2.0 2";
        String record3 = "h2o_feet,location=coyote_creek level\\ description=\"feet 3\",water_level=3.0 3";
        String record4 = "h2o_feet,location=coyote_creek level\\ description=\"feet 4\",water_level=4.0 4";

        writeClient.writeRecords("b1", "org1", ChronoUnit.NANOS, Arrays.asList(record1, record2, record3, record4));

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

        writeClient = createWriteClient(WriteOptions.builder().batchSize(1).flushInterval(10_00000).build());

        String record1 = "h2o_feet,location=coyote_creek level\\ description=\"feet 1\",water_level=1.0 1";
        String record2 = "h2o_feet,location=coyote_creek level\\ description=\"feet 2\",water_level=2.0 2";
        String record3 = "h2o_feet,location=coyote_creek level\\ description=\"feet 3\",water_level=3.0 3";
        String record4 = "h2o_feet,location=coyote_creek level\\ description=\"feet 4\",water_level=4.0 4";

        writeClient.writeRecord("b1", "org1", ChronoUnit.NANOS, record1);
        writeClient.writeRecord("b1", "org1", ChronoUnit.NANOS, record2);
        writeClient.writeRecord("b1", "org1", ChronoUnit.NANOS, record3);
        writeClient.writeRecord("b1", "org1", ChronoUnit.NANOS, record4);

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

        writeClient = createWriteClient(WriteOptions.builder().batchSize(1).build());

        String record1 = "h2o_feet,location=coyote_creek level\\ description=\"feet 1\",water_level=1.0 1";
        String record2 = "h2o_feet,location=coyote_creek level\\ description=\"feet 2\",water_level=2.0 2";
        String record3 = "h2o_feet,location=coyote_creek level\\ description=\"feet 3\",water_level=3.0 3";
        String record4 = "h2o_feet,location=coyote_creek level\\ description=\"feet 4\",water_level=4.0 4";

        List<String> records = Lists.list(record1, record2, record3, record4);
        writeClient.writeRecords("b1", "org1", ChronoUnit.NANOS, records);

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
    void flushByDuration() {

        mockServer.enqueue(createResponse("{}"));

        WriteOptions writeOptions = WriteOptions.builder()
                .batchSize(10)
                .flushInterval(1_000_000)
                .writeScheduler(scheduler)
                .build();

        writeClient = createWriteClient(writeOptions);

        writeClient.writeRecord("b1", "org1", ChronoUnit.NANOS,
                "h2o_feet,location=coyote_creek level\\ description=\"feet 1\",water_level=1.0 1");

        Assertions.assertThat(mockServer.getRequestCount()).isEqualTo(0);

        scheduler.advanceTimeBy(1_000, TimeUnit.SECONDS);

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

        writeClient = createWriteClient(writeOptions);

        writeClient.writeRecord("b1", "org1", ChronoUnit.NANOS,
                "h2o_feet,location=coyote_creek level\\ description=\"feet 1\",water_level=1.0 1");

        // move time to feature by 10 seconds - flush interval elapsed
        scheduler.advanceTimeBy(10, TimeUnit.SECONDS);

        // without call remote api
        Assertions.assertThat(mockServer.getRequestCount()).isEqualTo(0);

        // move time to feature by 5 seconds - jitter interval elapsed
        scheduler.advanceTimeBy(6, TimeUnit.SECONDS);

        // was call remote API
        Assertions.assertThat(mockServer.getRequestCount()).isEqualTo(1);
    }

    @Test
    void flushBeforeClose() {

        mockServer.enqueue(createResponse("{}"));

        writeClient = createWriteClient();

        writeClient.writeRecord("b1", "org1", ChronoUnit.NANOS,
                "h2o_feet,location=coyote_creek level\\ description=\"feet 1\",water_level=1.0 1");

        Assertions.assertThat(mockServer.getRequestCount()).isEqualTo(0);

        writeClient.close();

        // wait for request
        getRequestBody(mockServer);

        Assertions.assertThat(mockServer.getRequestCount()).isEqualTo(1);
    }

    @Test
    void eventWriteSuccessEvent() {

        mockServer.enqueue(createResponse("{}"));

        writeClient = createWriteClient(WriteOptions.builder().batchSize(1).build());
        TestObserver<WriteSuccessEvent> listener = writeClient.listenEvents(WriteSuccessEvent.class).test();

        writeClient.writeRecord("b1", "org1", ChronoUnit.NANOS,
                "h2o_feet,location=coyote_creek level\\ description=\"feet 1\",water_level=1.0 1");

        // wait for request
        getRequestBody(mockServer);

        listener
                .assertValue(event -> {

                    Assertions.assertThat(event).isNotNull();
                    Assertions.assertThat(event.getBucket()).isEqualTo("b1");
                    Assertions.assertThat(event.getOrganization()).isEqualTo("org1");
                    Assertions.assertThat(event.getPrecision()).isEqualTo(ChronoUnit.NANOS);
                    Assertions.assertThat(event.getLineProtocol()).isEqualTo("h2o_feet,location=coyote_creek level\\ description=\"feet 1\",water_level=1.0 1");

                    return true;
                })
                .assertSubscribed()
                .assertNotComplete();
    }

    @Test
    void eventUnhandledErrorEvent() {

        mockServer.enqueue(createErrorResponse("Failed to find bucket"));

        writeClient = createWriteClient(WriteOptions.builder().batchSize(1).build());
        TestObserver<WriteErrorEvent> listener = writeClient.listenEvents(WriteErrorEvent.class).test();

        writeClient.writeRecord("b1", "org1", ChronoUnit.NANOS,
                "h2o_feet,location=coyote_creek level\\ description=\"feet 1\",water_level=1.0 1");

        // wait for request
        getRequestBody(mockServer);

        listener
                .assertValue(event -> {

                    Assertions.assertThat(event).isNotNull();
                    Assertions.assertThat(event.getThrowable()).isNotNull();
                    Assertions.assertThat(event.getThrowable())
                            .isInstanceOf(InfluxException.class)
                            .hasMessage("Failed to find bucket");

                    return true;
                })
                .assertSubscribed()
                .assertNotComplete();
    }

    @Test
    //TODO
    @Disabled
    void eventBackpressureEvent() {

        mockServer.enqueue(new MockResponse().setBodyDelay(1, TimeUnit.SECONDS));

        writeClient = platformClient.createWriteClient(WriteOptions.builder().writeScheduler(new TestScheduler()).bufferLimit(100).build());

        TestObserver<BackpressureEvent> listener = writeClient
                .listenEvents(BackpressureEvent.class)
                .test();

        Flowable
                .range(0, 5000)
                .subscribeOn(Schedulers.newThread())
                .subscribe(index -> {
                    String record = String.format("h2o_feet,location=coyote_creek level\\ description=\"feet 1\",water_level=1.0 %s", index);
                    writeClient.writeRecord("b_" + index, "org1", ChronoUnit.NANOS, record);
                });
        
        listener
                .awaitCount(1)
                .assertValueAt(0, event -> {
                    Assertions.assertThat(event).isNotNull();
                    return true;
                });
    }

    @Nonnull
    private WriteClient createWriteClient() {
        return createWriteClient(WriteOptions.DEFAULTS);
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

}