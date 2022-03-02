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

import java.io.InterruptedIOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.influxdb.client.domain.Bucket;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.events.BackpressureEvent;
import com.influxdb.query.FluxTable;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * Client benchmark test
 */
@RunWith(JUnitPlatform.class)
class ITBenchmarkTest extends AbstractITClientTest {

    private BucketsApi bucketsApi;
    private Bucket bucket;

    @BeforeEach
    void setUp() {

        bucketsApi = influxDBClient.getBucketsApi();
        OrganizationsApi organizationsApi = influxDBClient.getOrganizationsApi();

        organizationsApi.findOrganizations()
            .stream()
            .filter(org -> org.getName().endsWith("-IT"))
            .forEach(organizationsApi::deleteOrganization);

        bucketsApi.findBuckets()
            .stream()
            .filter(bucket -> bucket.getName().endsWith("-IT"))
            .forEach(bucket -> {
                System.out.println("delete bucket: " + bucket.getName());
                bucketsApi.deleteBucket(bucket);
            });

        bucket = bucketsApi.createBucket("benchmark-IT", findMyOrg());

    }


    @ParameterizedTest
    @CsvSource(value = {
        "500, 10, 100, 1_000, 5_000, 10_000_000",
        "800, 10, 100, 1_000, 5_000, 10_000_000",
        "1000, 10, 100, 1_000, 5_000, 10_000_000",
    })
    public void clientBenchmark(int numberOfThreads,
                                int secondsCount,
                                int lineProtocolsCount,
                                int flushInterval,
                                int batchSize,
                                int maxBufferSize) {
        System.out.println("Server Version: " + influxDBClient.version());
        System.out.println("java.runtime.version: " + System.getProperty("java.runtime.version"));
        TestBenchmarkOptions opts = new TestBenchmarkOptions("sensor_" + System.currentTimeMillis(),
            numberOfThreads, secondsCount, lineProtocolsCount, batchSize, maxBufferSize, flushInterval);
        TestBenchmark benchmarkWriter = new TestBenchmark(opts);
        benchmarkWriter.start().verify();

    }


    private class TestBenchmark {

        private final int expectedCount;
        private final InfluxDBClient client;
        private final WriteApi writeApi;
        TestBenchmarkOptions opts;

        private volatile boolean execute = true;

        TestBenchmark(TestBenchmarkOptions options) {
            this.opts = options;
            expectedCount = opts.threadsCount * opts.secondsCount * opts.lineProtocolsCount;

            System.out.println("batchSize:          " + opts.batchSize);
            System.out.println("bufferLimit:        " + opts.bufferLimit);
            System.out.println("flushInterval:      " + opts.flushInterval);
            System.out.println("measurement:        " + opts.measurementName);
            System.out.println("threadsCount:       " + opts.threadsCount);
            System.out.println("secondsCount:       " + opts.secondsCount);
            System.out.println("lineProtocolsCount: " + opts.lineProtocolsCount);
            System.out.println();

            client = InfluxDBClientFactory.create(getInfluxDb2Url(), "my-token".toCharArray());

            WriteOptions writeOptions = WriteOptions.builder()
                .batchSize(opts.batchSize)
                .bufferLimit(opts.bufferLimit)
                .flushInterval(opts.flushInterval).build();

            writeApi = client.makeWriteApi(writeOptions);
            WriteEventListener<BackpressureEvent> backpressureEventWriteEventListener = new WriteEventListener<>();
            writeApi.listenEvents(BackpressureEvent.class, backpressureEventWriteEventListener);

        }

        TestBenchmark start() {

            System.out.println("expected size: " + expectedCount);
            System.out.println();

            ExecutorService executor = Executors.newFixedThreadPool(opts.threadsCount);

            for (int i = 1; i < opts.threadsCount + 1; i++) {
                Runnable worker = new TestBechmarkWorker(i, opts.measurementName, opts.secondsCount, opts.lineProtocolsCount);
                executor.execute(worker);
            }
            executor.shutdown();

            long start = System.currentTimeMillis();

            // Wait until all threads are finish
            while (!executor.isTerminated()) {
                try {
                    Thread.sleep(100);

                } catch (InterruptedException e) {
                    //ignored
                }

                // Stop benchmark after elapsed time
                if (System.currentTimeMillis() - start > opts.secondsCount * 1_000L && execute) {
                    System.out.println("\n\nThe time: " + opts.secondsCount + " seconds elapsed! Stopping all writers");
                    execute = false;
                    executor.shutdownNow();
                }
            }

            System.out.println();
            System.out.println();

            finished();

            return this;
        }

        void verify() {
            double count = countInDB();

            System.out.println("Results:");
            System.out.println("-> measurement:        " + opts.measurementName);
            System.out.println("-> expected:        " + expectedCount);
            System.out.println("-> total:           " + count);
            System.out.println("-> rate [%]:        " + (count / expectedCount) * 100);
            System.out.println("-> rate [msg/sec]:  " + (count / opts.secondsCount));

            Assertions.assertEquals(expectedCount, count);
        }

        void writeRecord(final String records) {
            writeApi.writeRecord(bucket.getName(), "my-org", WritePrecision.NS, records);
        }

        void finished() {
            client.close();
        }

        class TestBechmarkWorker implements Runnable {

            private final Integer id;
            private final String measurementName;
            private final int secondCount;
            private final int lineProtocolsCount;


            TestBechmarkWorker(Integer id, String measurementName, final int secondCount, final int lineProtocolsCount) {
                this.id = id;
                this.measurementName = measurementName;
                this.secondCount = secondCount;
                this.lineProtocolsCount = lineProtocolsCount;
            }

            @Override
            public void run() {

                try {
                    doLoad();
                } catch (Exception e) {
                    if (e instanceof InterruptedException || e instanceof InterruptedIOException) {
                        return;
                    }
                    System.out.println("e.getMessage() = " + e.getMessage());
                }
            }

            private void doLoad() throws Exception {
                for (int i = 0; i < secondCount && execute; i++) {

                    if (!execute) {
                        break;
                    }

                    if (id == 1) {
                        System.out.printf("writing iterations: %s/%s %n", i + 1, secondCount);
                    }

                    // Generate data
                    int start = i * lineProtocolsCount;
                    int end = start + lineProtocolsCount;
                    List<String> records = IntStream
                        .range(start, end)
                        .mapToObj(j -> String.format("%s,id=%s temperature=%d %d", measurementName, id, System.currentTimeMillis(), j))
                        .collect(Collectors.toList());

                    // Write records one by one
                    for (String record : records) {
                        if (execute) {
                            writeRecord(record);
                        }
                    }

                    if (!execute) {
                        break;
                    }

                    Thread.sleep(1000);
                }
            }
        }


        public Double countInDB() {

            System.out.println("Querying InfluxDB 2.0...");

            String query = "from(bucket:\"" + bucket.getName() + "\")\n"
                + "  |> range(start: 0, stop: now())\n"
                + "  |> filter(fn: (r) => r._measurement == \"" + opts.measurementName + "\")\n"
                + "  |> pivot(rowKey:[\"_time\"], columnKey: [\"_field\"], valueColumn: \"_value\")\n"
                + "  |> drop(columns: [\"id\", \"host\"])\n"
                + "  |> count(column: \"temperature\")";

            InfluxDBClientOptions options = InfluxDBClientOptions.builder()
                .url(getInfluxDb2Url())
                .org("my-org")
                .authenticateToken("my-token".toCharArray())
                .okHttpClient(new OkHttpClient.Builder().readTimeout(30, TimeUnit.SECONDS))
                .build();

            try (InfluxDBClient client = InfluxDBClientFactory.create(options)) {

                List<FluxTable> results = client.getQueryApi().query(query);
                Long count = (Long) results.get(0).getRecords().get(0).getValueByKey("temperature");
                assert count != null;
                return count.doubleValue();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class TestBenchmarkOptions {
        final String measurementName;
        private final int threadsCount;
        private final int secondsCount;
        private final int lineProtocolsCount;
        private final int batchSize;
        private final int flushInterval;
        private final int bufferLimit;

        public TestBenchmarkOptions(final String measurementName,
                                    final int threadsCount,
                                    final int secondsCount,
                                    final int lineProtocolsCount,
                                    final int batchSize,
                                    final int bufferLimit,
                                    final int flushInterval) {
            this.measurementName = measurementName;
            this.threadsCount = threadsCount;
            this.secondsCount = secondsCount;
            this.lineProtocolsCount = lineProtocolsCount;
            this.batchSize = batchSize;
            this.bufferLimit = bufferLimit;
            this.flushInterval = flushInterval;
        }
    }

}