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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.client.write.events.AbstractWriteEvent;
import com.influxdb.client.write.events.BackpressureEvent;
import com.influxdb.client.write.events.WriteErrorEvent;
import com.influxdb.client.write.events.WriteSuccessEvent;

import io.reactivex.rxjava3.core.BackpressureOverflowStrategy;
import io.reactivex.rxjava3.exceptions.MissingBackpressureException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (24/01/2020 09:40)
 */
@RunWith(JUnitPlatform.class)
class ITBackpressure extends AbstractITWrite {

    private static final Logger LOG = Logger.getLogger(WriteApiTest.class.getName());

    private static final int WRITER_COUNT = 4;
    private static final int BATCH_SIZE = 50_000;
    private static final int SECONDS_COUNT = 15;

    @Test
    public void backpressureAndBufferConsistency() throws InterruptedException {

        List<AbstractWriteEvent> events = stressfulWrite(WriteOptions.builder()
                .batchSize(BATCH_SIZE)
                .flushInterval(1_000_000)
                .build());

        //
        // Test backpressure presents
        //
        List<BackpressureEvent> backpressure = events.stream()
                .filter(it -> it instanceof BackpressureEvent)
                .map(it -> (BackpressureEvent) it)
                .collect(Collectors.toList());

        Assertions.assertThat(backpressure).isNotEmpty();

        //
        // Test consistent Bath size
        //
        List<WriteSuccessEvent> success = events.stream()
                .filter(it -> it instanceof WriteSuccessEvent)
                .map(it -> (WriteSuccessEvent) it)
                .collect(Collectors.toList());

        Assertions.assertThat(success).isNotEmpty();
        Assertions.assertThat(success)
                .allMatch(
                        event -> {
                            int length = event.getLineProtocol().split("\n").length;
                            boolean expected_batch_size = length == BATCH_SIZE;

                            if (!expected_batch_size) {

                                LOG.severe("Unexpected length: " + length);
                            }

                            return expected_batch_size;
                        },
                        "The batch size should be: " + BATCH_SIZE);

        //
        // Test without errors
        //
        List<WriteErrorEvent> error = events.stream()
                .filter(it -> it instanceof WriteErrorEvent)
                .map(it -> (WriteErrorEvent) it)
                .filter(this::isNotInterruptedException)
                .collect(Collectors.toList());

        Assertions.assertThat(error).isEmpty();
    }

    @Test
    void publishRuntimeErrorAsWriteErrorEvent() throws InterruptedException {
        
        List<AbstractWriteEvent> events = stressfulWrite(WriteOptions.builder()
                .batchSize(BATCH_SIZE)
                .backpressureStrategy(BackpressureOverflowStrategy.ERROR)
                .flushInterval(1_000_000)
                .build());

        // propagated BackPressure error
        List<WriteErrorEvent> errors = events.stream()
                .filter(it -> it instanceof WriteErrorEvent)
                .map(it -> (WriteErrorEvent) it)
                .filter(this::isNotInterruptedException)
                .collect(Collectors.toList());

        Assertions.assertThat(errors).hasSize(1);
        Assertions.assertThat(errors.get(0).getThrowable().getCause())
                .isInstanceOf(MissingBackpressureException.class)
                .hasMessage(null);
    }

    @Nonnull
    private List<AbstractWriteEvent> stressfulWrite(@Nonnull final WriteOptions options) throws InterruptedException {

        AtomicBoolean stopped = new AtomicBoolean(false);
        List<AbstractWriteEvent> events = new CopyOnWriteArrayList<>();

        WriteApi api = influxDBClient
                .makeWriteApi(options);

        api.listenEvents(AbstractWriteEvent.class, events::add)                        ;

        ExecutorService executorService = Executors.newFixedThreadPool(WRITER_COUNT);

        for (int i = 1; i <= WRITER_COUNT; i++) {
            executorService.submit(new Writer(i, stopped, api));
        }

        long start = System.currentTimeMillis();

        while (System.currentTimeMillis() - start <= SECONDS_COUNT *1_000) {
            Thread.sleep(100);
        }

        //
        // Shutdown writers
        //
        stopped.set(true);
        executorService.shutdownNow();
        Thread.sleep(1000);
        return events;
    }

    private boolean isNotInterruptedException(final WriteErrorEvent it) {
        return it.getThrowable().getCause() == null || !(it.getThrowable().getCause() instanceof java.io.InterruptedIOException);
    }

    private static class Writer implements Runnable {

        private final int id;
        private final AtomicBoolean stopped;
        private final WriteApi api;

        public Writer(final int id, final AtomicBoolean stopped, final WriteApi api) {
            this.id = id;
            this.stopped = stopped;
            this.api = api;
        }

        @Override
        public void run() {
            long time = 1;
            while (!stopped.get()) {
                Point point = Point.measurement("push")
                        .addTag("id", String.valueOf(id))
                        .addField("value", 1)
                        .time(time, WritePrecision.NS);
                api.writePoint(point);
                if (id == 1 && time % 250_000 == 0) {
                    LOG.info("Generated point: " + point.toLineProtocol());
                }
                time += 1;
            }
        }
    }
}
