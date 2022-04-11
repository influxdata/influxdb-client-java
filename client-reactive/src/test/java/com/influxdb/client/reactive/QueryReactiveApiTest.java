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

import java.io.InterruptedIOException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.influxdb.test.AbstractMockServerTest;

import io.reactivex.rxjava3.core.Flowable;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.suite.api.Suite;

/**
 * @author Jakub Bednar (03/16/2022 08:28)
 */
@Suite
class QueryReactiveApiTest extends AbstractMockServerTest {

    private InfluxDBClientReactive influxDBClient;

    @BeforeEach
    void setUp() {

        influxDBClient = InfluxDBClientReactiveFactory.create(startMockServer());
    }

    @AfterEach
    void tearDown() {

        influxDBClient.close();
    }

    @Test
    public void doNotPropagateErrorOnCanceledConsumer() throws InterruptedException {

        mockServer.enqueue(createErrorResponse("Request Timeout", true, 408)
                .setBodyDelay(3, TimeUnit.SECONDS));

        QueryReactiveApi queryApi = influxDBClient.getQueryReactiveApi();

        final Throwable[] throwable = new Throwable[1];
        Future<?> future = Executors
                .newSingleThreadScheduledExecutor()
                .scheduleWithFixedDelay(() -> Flowable
                                .fromPublisher(queryApi.queryRaw("from()", "my-org"))
                                .subscribe(
                                        s -> {

                                        },
                                        t -> throwable[0] = t),
                        0, 1, TimeUnit.SECONDS);

        Thread.sleep(2_000);
        future.cancel(true);
        Thread.sleep(2_000);

        Assertions.assertThat(throwable[0]).isNotNull();
        Assertions.assertThat(throwable[0]).isInstanceOf(InterruptedIOException.class);
    }
}
