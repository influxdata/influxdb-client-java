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
import java.util.concurrent.TimeUnit;

import com.influxdb.client.WriteOptions;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.exceptions.InfluxException;
import com.influxdb.test.AbstractMockServerTest;

import io.reactivex.Flowable;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.TestScheduler;
import io.reactivex.subscribers.TestSubscriber;
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

    }

    @AfterEach
    void tearDown() {

        influxDBClient.close();

        testScheduler.shutdown();
        RxJavaPlugins.setComputationSchedulerHandler(null);
    }

    @Test
    void batches() {

        mockServer.enqueue(createResponse("{}"));
        mockServer.enqueue(createResponse("{}"));
        writeClient = influxDBClient.getWriteReactiveApi(WriteOptions.builder().batchSize(1).build());

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

        writeClient = influxDBClient.getWriteReactiveApi(WriteOptions.builder().batchSize(100_000)
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
    public void networkError() throws IOException {
        mockServer.shutdown();

        writeClient = influxDBClient.getWriteReactiveApi();

        Publisher<WriteReactiveApi.Success> success = writeClient.writeRecord("my-bucket", "my-org", WritePrecision.S, "mem,tag=a field=10");
        Flowable.fromPublisher(success)
                .test()
                .assertError(throwable -> {
                    Assertions.assertThat(throwable).isInstanceOf(InfluxException.class);
                    Assertions.assertThat(throwable).hasCauseInstanceOf(IOException.class);
                    return true;
                });
    }
}
