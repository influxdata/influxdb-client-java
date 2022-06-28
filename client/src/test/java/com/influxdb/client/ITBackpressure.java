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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.influxdb.LogLevel;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.client.write.events.AbstractWriteEvent;
import com.influxdb.client.write.events.BackpressureEvent;
import com.influxdb.client.write.events.WriteErrorEvent;
import com.influxdb.client.write.events.WriteSuccessEvent;
import com.influxdb.test.MockServerExtension;

import io.reactivex.rxjava3.core.BackpressureOverflowStrategy;
import io.reactivex.rxjava3.exceptions.MissingBackpressureException;
import okhttp3.mockwebserver.MockResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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

    private WriterConfig writerConfig;

    protected MockServerExtension mockServerExtension = new MockServerExtension();

    @BeforeEach
    protected void before() {
        writerConfig = new WriterConfig();
    }

    @AfterEach
    protected void after() throws IOException {
        mockServerExtension.shutdown();
    }

    @Test
    public void backpressureAndBufferConsistency() throws InterruptedException {

        WriteOptions build = WriteOptions.builder()
                .batchSize(BATCH_SIZE)
                .flushInterval(1_000_000)
                .bufferLimit(BATCH_SIZE * 10)
                .build();

        stressfulWriteValidate(build, null);
    }

    @Test
    public void backpressureWithNotRunningInstance() throws InterruptedException {

        initWithMockServer();

        mockServerExtension.server.enqueue(new MockResponse());

        WriteOptions build = WriteOptions.builder()
                .batchSize(BATCH_SIZE)
                .bufferLimit(BATCH_SIZE * 10)
                .flushInterval(1_000_000)
                .build();

        stressfulWriteValidate(build, backpressureEvents -> Assertions.assertThat(backpressureEvents)
                .anyMatch(backpressureEvent -> backpressureEvent.getReason()
                        .equals(BackpressureEvent.BackpressureReason.TOO_MUCH_BATCHES)));
    }

    @Test
    public void backpressureAndBufferConsistencyWithJitter() throws InterruptedException {

        WriteOptions build = WriteOptions.builder()
                .batchSize(BATCH_SIZE)
                .bufferLimit(BATCH_SIZE * 10)
                .jitterInterval(1_000)
                .flushInterval(1_000_000)
                .build();

        stressfulWriteValidate(build, null);
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

    @Test
    void writeToDifferentBuckets() throws InterruptedException {

        writerConfig.secondsCount = -1;
        writerConfig.differentBucketsCount = 500;
        initWithMockServer();


        // Generate mocked HTTP responses
        //
        // (500 * 4 * 4) / 1_000 = 8 batches
        IntStream
                .range(0, (writerConfig.differentBucketsCount * WRITER_COUNT * 4) / 1_000)
                .forEach(nbr -> mockServerExtension.server.enqueue(new MockResponse()));

        List<AbstractWriteEvent> events = stressfulWrite(WriteOptions.builder()
                .flushInterval(1_000_000)
                .batchSize(1_000)
                .build());

        Assertions.assertThat(events).hasSize(8);
        Assertions.assertThat(mockServerExtension.server.getRequestCount()).isEqualTo(8);

        Map<String, Long> batches = new HashMap<>();
        for (AbstractWriteEvent event : events) {
            Assertions.assertThat(event).isExactlyInstanceOf(WriteSuccessEvent.class);
            WriteSuccessEvent successEvent = (WriteSuccessEvent) event;

            String[] protocols = successEvent.getLineProtocol().split("\n");

            // correct content of batch
            Assertions
                    .assertThat(protocols)
                    .allMatch(protocol -> protocol.startsWith(successEvent.getBucket()), "All starts with: " + successEvent.getBucket());

            Assertions.assertThat(protocols.length).isEqualTo(1_000);

            Long count = batches.getOrDefault(successEvent.getBucket(), 0L);
            count += protocols.length;
            batches.put(successEvent.getBucket(), count);
        }

        Assertions.assertThat(batches).hasSize(4);
        Assertions.assertThat(batches.get("my-bucket_1")).isEqualTo(2000);
        Assertions.assertThat(batches.get("my-bucket_2")).isEqualTo(2000);
        Assertions.assertThat(batches.get("my-bucket_3")).isEqualTo(2000);
        Assertions.assertThat(batches.get("my-bucket_4")).isEqualTo(2000);
    }

    private void stressfulWriteValidate(@Nonnull final WriteOptions writeOptions,
                                        @Nullable final Consumer<List<BackpressureEvent>> backpressureAssert) throws InterruptedException {

        List<AbstractWriteEvent> events = stressfulWrite(writeOptions);

        //
        // Test backpressure presents
        //
        List<BackpressureEvent> backpressure = events.stream()
                .filter(it -> it instanceof BackpressureEvent)
                .map(it -> (BackpressureEvent) it)
                .collect(Collectors.toList());

        if (backpressureAssert == null) {
            Assertions.assertThat(backpressure).isNotEmpty();
        } else {
            backpressureAssert.accept(backpressure);
        }

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

    @Nonnull
    private List<AbstractWriteEvent> stressfulWrite(@Nonnull final WriteOptions options) throws InterruptedException {

        AtomicBoolean stopped = new AtomicBoolean(false);
        List<AbstractWriteEvent> events = new CopyOnWriteArrayList<>();

        WriteApi api = influxDBClient.makeWriteApi(options);

        api.listenEvents(AbstractWriteEvent.class, events::add);

        ExecutorService executorService = Executors.newFixedThreadPool(WRITER_COUNT);

        for (int i = 1; i <= WRITER_COUNT; i++) {
            executorService.submit(new Writer(i, stopped, writerConfig, api));
        }

        long start = System.currentTimeMillis();

        if (writerConfig.secondsCount != -1) {
            while (System.currentTimeMillis() - start <= writerConfig.secondsCount * 1_000) {
                Thread.sleep(100);
            }
        } else {
            executorService.awaitTermination(5, TimeUnit.SECONDS);
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

    private void initWithMockServer() {
        influxDBClient.close();
        mockServerExtension.start();
        influxDBClient = InfluxDBClientFactory.create(
                mockServerExtension.baseURL,
                "my-token".toCharArray(),
                "my-org",
                "my-bucket");
        influxDBClient.setLogLevel(LogLevel.BASIC);
    }

    private static class Writer implements Runnable {

        private final int id;
        private final AtomicBoolean stopped;
        private final WriterConfig config;
        private final WriteApi api;

        public Writer(final int id, final AtomicBoolean stopped, final WriterConfig config, final WriteApi api) {
            this.id = id;
            this.stopped = stopped;
            this.config = config;
            this.api = api;
        }

        @Override
        public void run() {
            long time = 1;
            while (!stopped.get() && (config.differentBucketsCount == -1 || time <= config.differentBucketsCount)) {

                if (config.differentBucketsCount == -1) {
                    Point point = Point.measurement("push")
                            .addTag("id", String.valueOf(id))
                            .addField("value", 1)
                            .time(time, WritePrecision.NS);
                    api.writePoint(point);
                    if (id == 1 && time % 250_000 == 0) {
                        LOG.info("Generated point: " + point.toLineProtocol());
                    }
                } else {
                    for (int i = 1; i <= 4; i++) {
                        String bucketName = "my-bucket_" + i;
                        Point point = Point.measurement(bucketName)
                                .addTag("id", String.valueOf(id))
                                .addField("value", 1)
                                .time(time, WritePrecision.NS);
                        api.writePoint(bucketName, "my-org", point);
                    }
                }
                time += 1;
            }
        }
    }

    private static class WriterConfig {
        private int differentBucketsCount = -1;
        private int secondsCount = 15;
    }
}
