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
package com.influxdb.client.reactive;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import com.influxdb.client.domain.WriteConsistency;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.internal.RetryAttempt;
import com.influxdb.client.write.Point;
import com.influxdb.client.write.WriteParameters;
import com.influxdb.exceptions.InfluxException;
import com.influxdb.test.AbstractMockServerTest;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.TestScheduler;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.reactivestreams.Publisher;

/**
 * @author Jakub Bednar (02/08/2021 12:28)
 */
@RunWith(JUnitPlatform.class)
class WriteReactiveApiTest extends AbstractMockServerTest {

    private TestScheduler testScheduler;
    private WriteReactiveApi writeClient;
    private InfluxDBClientReactive influxDBClient;

    @BeforeEach
    void setUp() {

        influxDBClient = InfluxDBClientReactiveFactory.create(startMockServer());

        testScheduler = new TestScheduler();
        RxJavaPlugins.setComputationSchedulerHandler(scheduler -> testScheduler);
        RetryAttempt.setRetryRandomSupplier(() -> 0D);
    }

    @AfterEach
    void tearDown() {

        influxDBClient.close();

        testScheduler.shutdown();
        RxJavaPlugins.setComputationSchedulerHandler(null);
        RetryAttempt.setJitterRandomSupplier(null);
    }

    @Test
    void batches() {

        mockServer.enqueue(createResponse("{}"));
        mockServer.enqueue(createResponse("{}"));
        writeClient = influxDBClient.getWriteReactiveApi(WriteOptionsReactive.builder().batchSize(1).build());

        Publisher<String> records = Flowable.just(
                "h2o_feet,location=coyote_creek level=1.0 1",
                "h2o_feet,location=coyote_creek level=2.0 2");

        Publisher<WriteReactiveApi.Success> success = writeClient.writeRecords("my-bucket", "my-org", WritePrecision.S, records);
        Flowable.fromPublisher(success)
                .test()
                .assertValueCount(2);

        Assertions.assertThat(getRequestBody(mockServer)).isEqualTo("h2o_feet,location=coyote_creek level=1.0 1");
        Assertions.assertThat(getRequestBody(mockServer)).isEqualTo("h2o_feet,location=coyote_creek level=2.0 2");

        Assertions.assertThat(mockServer.getRequestCount()).isEqualTo(2);
    }

    @Test
    void flushByTime() {

        mockServer.enqueue(createResponse("{}"));
        mockServer.enqueue(createResponse("{}"));
        mockServer.enqueue(createResponse("{}"));
        mockServer.enqueue(createResponse("{}"));
        mockServer.enqueue(createResponse("{}"));

        writeClient = influxDBClient.getWriteReactiveApi(WriteOptionsReactive.builder().batchSize(100_000)
                .flushInterval(100).build());

        Flowable<String> records = Flowable.interval(500, TimeUnit.MILLISECONDS)
                .take(5)
                .map(it -> String.format("mem,tag=a field=%d %d", it, it));

        Publisher<WriteReactiveApi.Success> success = writeClient
                .writeRecords("my-bucket", "my-org", WritePrecision.S, records);

        TestSubscriber<WriteReactiveApi.Success> test = Flowable.fromPublisher(success)
                .test();

        testScheduler.advanceTimeBy(500, TimeUnit.MILLISECONDS);
        test.awaitCount(1);

        testScheduler.advanceTimeBy(500, TimeUnit.MILLISECONDS);
        test.awaitCount(2).assertNotComplete();

        testScheduler.advanceTimeBy(500, TimeUnit.MILLISECONDS);
        test.awaitCount(3).assertNotComplete();

        testScheduler.advanceTimeBy(500, TimeUnit.MILLISECONDS);
        test.awaitCount(4).assertNotComplete();

        testScheduler.advanceTimeBy(500, TimeUnit.MILLISECONDS);
        test.awaitCount(5).assertComplete();

        Assertions.assertThat(mockServer.getRequestCount()).isEqualTo(5);
    }

    @Test
    void jitter() {
        mockServer.enqueue(createResponse("{}"));
        mockServer.enqueue(createResponse("{}"));
        RetryAttempt.setJitterRandomSupplier(() -> 1D);

        // Without jitter
        {
            writeClient = influxDBClient.getWriteReactiveApi(WriteOptionsReactive.builder().batchSize(1).build());

            Publisher<WriteReactiveApi.Success> success = writeClient
                    .writeRecord("my-bucket", "my-org", WritePrecision.S, "mem,tag=a field=1 1");

            TestSubscriber<WriteReactiveApi.Success> test = Flowable.fromPublisher(success)
                    .test();

            testScheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS);
            test.assertValueCount(1).assertComplete();
        }

        // With jitter
        {
            writeClient = influxDBClient.getWriteReactiveApi(WriteOptionsReactive.builder().batchSize(1).jitterInterval(2_000).build());

            Publisher<WriteReactiveApi.Success> success = writeClient
                    .writeRecord("my-bucket", "my-org", WritePrecision.S, "mem,tag=a field=1 1");

            TestSubscriber<WriteReactiveApi.Success> test = Flowable.fromPublisher(success)
                    .test();

            testScheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS);
            test.assertValueCount(0).assertNotComplete();

            testScheduler.advanceTimeBy(2_000, TimeUnit.MILLISECONDS);
            test.assertValueCount(1).assertComplete();
        }
    }

    @Test
    void retry() {
        mockServer.enqueue(createErrorResponse("token is temporarily over quota", true, 429));
        mockServer.enqueue(createResponse("{}"));

        writeClient = influxDBClient.getWriteReactiveApi(WriteOptionsReactive.builder().batchSize(1).build());

        Point point = Point.measurement("h2o").addTag("location", "europe").addField("level", 2);
        Publisher<WriteReactiveApi.Success> success = writeClient.writePoint("b1", "org1", WritePrecision.NS, point);

        TestSubscriber<WriteReactiveApi.Success> test = Flowable.fromPublisher(success)
                .test();

        test.assertValueCount(0).assertNotComplete();

        // retry interval
        testScheduler.advanceTimeBy(5_000, TimeUnit.MILLISECONDS);
        test.assertValueCount(1).assertComplete();

        Assertions.assertThat(mockServer.getRequestCount()).isEqualTo(2);

        String body1 = getRequestBody(mockServer);
        Assertions.assertThat(body1).isEqualTo("h2o,location=europe level=2i");

        String body2 = getRequestBody(mockServer);
        Assertions.assertThat(body2).isEqualTo("h2o,location=europe level=2i");
    }

    @Test
    public void maxRetryTime() {
        MockResponse errorResponse = createErrorResponse("token is temporarily over quota", true, 429, 1000);
        errorResponse.addHeader("Retry-After", 1);
        mockServer.enqueue(errorResponse);

        writeClient = influxDBClient.getWriteReactiveApi(WriteOptionsReactive.builder().batchSize(1).maxRetryTime(500).build());

        Point point = Point.measurement("h2o").addTag("location", "europe").addField("level", 2);
        Publisher<WriteReactiveApi.Success> success = writeClient.writePoint("b1", "org1", WritePrecision.NS, point);

        TestSubscriber<WriteReactiveApi.Success> test = Flowable.fromPublisher(success)
                .test();

        test.assertValueCount(0).assertNotComplete();

        // max retry delay
        testScheduler.advanceTimeBy(500, TimeUnit.MILLISECONDS);
        test.awaitCount(1)
                .assertValueCount(0)
                .assertError(throwable -> {
                    Assertions.assertThat(throwable).isInstanceOf(InfluxException.class);
                    Assertions.assertThat(throwable).hasMessage("Max retry time exceeded.");
                    Assertions.assertThat(throwable).hasCauseInstanceOf(TimeoutException.class);
                    return true;
                })
                .assertNotComplete();
    }

    @Test
    void networkError() throws IOException {
        mockServer.shutdown();

        writeClient = influxDBClient.getWriteReactiveApi(WriteOptionsReactive.builder().batchSize(1).maxRetries(1).build());

        Publisher<WriteReactiveApi.Success> success = writeClient.writeRecord("my-bucket", "my-org", WritePrecision.S, "mem,tag=a field=10");
        TestSubscriber<WriteReactiveApi.Success> test = Flowable.fromPublisher(success)
                .test();

        testScheduler.advanceTimeBy(5_000, TimeUnit.MILLISECONDS);

        test
                .awaitCount(1)
                .assertValueCount(0)
                .assertError(throwable -> {
                    Assertions.assertThat(throwable).isInstanceOf(InfluxException.class);
                    Assertions.assertThat(throwable).hasCauseInstanceOf(IOException.class);
                    return true;
                })
                .assertNotComplete();
    }

    @Test
    void customBatching() {

        mockServer.enqueue(createResponse("{}"));
        mockServer.enqueue(createResponse("{}"));
        mockServer.enqueue(createResponse("{}"));

        writeClient = influxDBClient.getWriteReactiveApi(WriteOptionsReactive.builder().batchSize(0).build());

        Flowable<WriteReactiveApi.Success> successFlowable = Flowable.range(0, 100)
                .take(10)
                .map(it -> String.format("mem,tag=a field=%d %d", it, it))
                .buffer(4)
                .flatMap(records -> writeClient.writeRecords("my-bucket", "my-org", WritePrecision.S, Flowable.fromIterable(records)));

        Flowable.fromPublisher(successFlowable)
                .test()
                .awaitCount(3)
                .assertValueCount(3)
                .assertNoErrors()
                .assertComplete();

        Assertions.assertThat(mockServer.getRequestCount()).isEqualTo(3);

        Assertions.assertThat(getRequestBody(mockServer)).isEqualTo("mem,tag=a field=0 0\n"
                + "mem,tag=a field=1 1\n"
                + "mem,tag=a field=2 2\n"
                + "mem,tag=a field=3 3");
        Assertions.assertThat(getRequestBody(mockServer)).isEqualTo("mem,tag=a field=4 4\n"
                + "mem,tag=a field=5 5\n"
                + "mem,tag=a field=6 6\n"
                + "mem,tag=a field=7 7");
        Assertions.assertThat(getRequestBody(mockServer)).isEqualTo("mem,tag=a field=8 8\n"
                + "mem,tag=a field=9 9");
    }

    @Test
    void disableRetry() {
        mockServer.enqueue(createErrorResponse("token is temporarily over quota", true, 429));
        mockServer.enqueue(createResponse("{}"));

        writeClient = influxDBClient.getWriteReactiveApi(WriteOptionsReactive.builder().maxRetries(0).batchSize(1).build());

        Publisher<WriteReactiveApi.Success> success = writeClient
                .writeRecord("b1", "org1", WritePrecision.NS, "mem,tag=a field=0 0");

        TestSubscriber<WriteReactiveApi.Success> test = Flowable.fromPublisher(success)
                .test();

        test
                .awaitCount(1)
                .assertValueCount(0)
                .assertError(throwable -> {
                    Assertions.assertThat(throwable).isInstanceOf(InfluxException.class);
                    Assertions.assertThat(throwable).hasMessage("token is temporarily over quota");
                    return true;
                })
                .assertNotComplete();

        Assertions.assertThat(mockServer.getRequestCount()).isEqualTo(1);

        String body1 = getRequestBody(mockServer);
        Assertions.assertThat(body1).isEqualTo("mem,tag=a field=0 0");
    }

    @Test
    void pointsPrecision() {

        mockServer.enqueue(createResponse("{}"));

        writeClient = influxDBClient.getWriteReactiveApi();

        Instant time = Instant.ofEpochSecond(2000);

        Point point1 = Point.measurement("h2o_feet").addTag("location", "west").addField("water_level", 1).time(time, WritePrecision.MS);
        Point point2 = Point.measurement("h2o_feet").addTag("location", "west").addField("water_level", 2).time(time.plusSeconds(10), WritePrecision.S);


        Publisher<WriteReactiveApi.Success> success = writeClient.writePoints("b1", "org1", WritePrecision.S, Flowable.just(point1, point2));
        Flowable.fromPublisher(success)
                .test()
                .awaitCount(1)
                .assertValueCount(1)
                .assertNoErrors()
                .assertComplete();

        Assertions.assertThat(mockServer.getRequestCount()).isEqualTo(1);

        String body = getRequestBody(mockServer);
        Assertions.assertThat(body).isEqualTo("h2o_feet,location=west water_level=1i 2000\n"
                + "h2o_feet,location=west water_level=2i 2010");
    }

    @Test
    void writeParameters() {

        writeClient = influxDBClient.getWriteReactiveApi();

        Runnable assertParameters = () -> {
            RecordedRequest request;
            try {
                request = takeRequest();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            Assertions.assertThat(request.getRequestUrl()).isNotNull();
            Assertions.assertThat("s").isEqualTo(request.getRequestUrl().queryParameter("precision"));
            Assertions.assertThat("g").isEqualTo(request.getRequestUrl().queryParameter("bucket"));
            Assertions.assertThat("h").isEqualTo(request.getRequestUrl().queryParameter("org"));
            Assertions.assertThat("all").isEqualTo(request.getRequestUrl().queryParameter("consistency"));
        };

        Consumer<Publisher<WriteReactiveApi.Success>> assertSuccess = success -> Flowable.fromPublisher(success)
                .test()
                .awaitCount(1)
                .assertValueCount(1)
                .assertNoErrors()
                .assertComplete();

        WriteParameters parameters = new WriteParameters("g", "h", WritePrecision.S, WriteConsistency.ALL);

        // records
        mockServer.enqueue(createResponse("{}"));
        Publisher<WriteReactiveApi.Success> success = writeClient.writeRecords(Flowable.just("h2o,location=europe level=1i 1"), parameters);
        assertSuccess.accept(success);
        assertParameters.run();

        // points
        Point point = Point.measurement("h2o").addTag("location", "europe").addField("level", 1).time(1L, WritePrecision.S);
        mockServer.enqueue(createResponse("{}"));
        success = writeClient.writePoints(Flowable.just(point), parameters);
        assertSuccess.accept(success);
        assertParameters.run();

        // measurements
        ITWriteQueryReactiveApi.H2O measurement = new ITWriteQueryReactiveApi.H2O();
        measurement.location = "coyote_creek";
        measurement.level = 2.927;
        measurement.time = Instant.ofEpochSecond(10);
        mockServer.enqueue(createResponse("{}"));
        success = writeClient.writeMeasurements(Flowable.just(measurement), parameters);
        assertSuccess.accept(success);
        assertParameters.run();
    }
}
