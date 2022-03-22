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
package com.influxdb.client.benchmark;

import java.io.InterruptedIOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import com.influxdb.LogLevel;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.InfluxDBClientOptions;
import com.influxdb.client.WriteApi;
import com.influxdb.client.WriteOptions;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxTable;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Assertions;

public class ClientBenchmark {

    private final int expectedCount;
    private final InfluxDBClient client;
    private final WriteApi writeApi;
    BenchmarkOptions opts;

    public ClientBenchmark(BenchmarkOptions options) {
        this.opts = options;

        client = InfluxDBClientFactory.create(opts.influxUrl, opts.token.toCharArray());

        expectedCount = opts.threadsCount * opts.secondsCount * opts.lineProtocolsCount;
        console("Running benchmark:     " + opts.influxUrl);
        console("threadsCount:          " + opts.threadsCount);
        console("Server Version:        " + client.version());
        console("java.runtime.version:  " + System.getProperty("java.runtime.version"));
        console("bucket:                " + opts.bucket);
        console("batchSize:             " + opts.batchSize);
        console("bufferLimit:           " + opts.bufferLimit);
        console("flushInterval:         " + opts.flushInterval);
        console("measurement:           " + opts.measurementName);
        console("secondsCount:          " + opts.secondsCount);
        console("lineProtocolsCount:    " + opts.lineProtocolsCount);
        console("expected count:        " + expectedCount);
        client.setLogLevel(LogLevel.NONE);

        WriteOptions writeOptions = WriteOptions.builder()
            .batchSize(opts.batchSize)
            .bufferLimit(opts.bufferLimit)
            .flushInterval(opts.flushInterval).build();

        writeApi = client.makeWriteApi(writeOptions);
    }

    public ClientBenchmark start() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(opts.threadsCount);
        CountDownLatch doneSignal = new CountDownLatch(opts.threadsCount);

        for (int i = 1; i < opts.threadsCount + 1; i++) {
            Runnable worker = new BenchmarkWorker(doneSignal,
                i, opts.measurementName, opts.secondsCount, opts.lineProtocolsCount);
            executor.execute(worker);
        }

        executor.shutdown();

        long start = System.currentTimeMillis();

        doneSignal.await();
        console("-> all workers finished:   " + Duration.ofMillis(System.currentTimeMillis() - start));
        writeApi.flush();
        executor.shutdownNow();
        console("-> client.flush():         " + Duration.ofMillis(System.currentTimeMillis() - start));
        client.close();
        console("-> client.close():         " + Duration.ofMillis(System.currentTimeMillis() - start));
        return this;
    }

    public double verify() {
        long count = countInDB();
        console("Verify:");
        console("-> expected:        " + expectedCount);
        console("-> total:           " + count);
        console("-> rate [%]:        " + ((double) count / expectedCount) * 100);
        Assertions.assertEquals(expectedCount, count);
        return count;
    }

    class BenchmarkWorker implements Runnable {

        private final CountDownLatch doneSignal;
        private final Integer id;
        private final String measurementName;
        private final int secondCount;
        private final int lineProtocolsCount;


        BenchmarkWorker(final CountDownLatch doneSignal,
                        final Integer id,
                        final String measurementName,
                        final int secondCount,
                        final int lineProtocolsCount) {
            this.doneSignal = doneSignal;
            this.id = id;
            this.measurementName = measurementName;
            this.secondCount = secondCount;
            this.lineProtocolsCount = lineProtocolsCount;
        }

        @Override
        public void run() {
            try {
                doWork();
                doneSignal.countDown();
            } catch (Exception e) {
                if (e instanceof InterruptedException || e instanceof InterruptedIOException) {
                    return;
                }
                console("e.getMessage() = " + e.getMessage());
            }
        }

        private void doWork() throws Exception {
            for (int i = 0; i < secondCount; i++) {

                if (id == 1) {
                    System.out.printf("writing iterations: %s/%s %n", i + 1, secondCount);
                }

                // Generate data
                int start = i * lineProtocolsCount;
                int end = start + lineProtocolsCount;
                IntStream
                    .range(start, end)
                    //create line protocol
                    .mapToObj(j -> new Point(measurementName)
                        .addTag("id", id.toString())
                        .addField("temperature", System.currentTimeMillis())
                        .time(j, WritePrecision.NS))
                    .forEach(point -> writeApi.writePoint(opts.bucket, opts.org, point));
                //write using LP string
//                    .mapToObj(j -> String.format("%s,id=%s temperature=%d %d", measurementName, id,
//                        System.currentTimeMillis(), j))
//                    .forEach(record -> writeApi.writeRecord(opts.bucket, opts.org, WritePrecision.NS, record));
                Thread.sleep(1000);
            }
        }
    }


    public long countInDB() {

        String query = "from(bucket:\"" + opts.bucket + "\")\n"
            + "  |> range(start: 0, stop: now())\n"
            + "  |> filter(fn: (r) => r._measurement == \"" + opts.measurementName + "\")\n"
            + "  |> pivot(rowKey:[\"_time\"], columnKey: [\"_field\"], valueColumn: \"_value\")\n"
            + "  |> drop(columns: [\"id\", \"host\"])\n"
            + "  |> count(column: \"temperature\")";

        InfluxDBClientOptions options = InfluxDBClientOptions.builder()
            .url(opts.influxUrl)
            .org(opts.org)
            .authenticateToken(opts.token.toCharArray())
            .okHttpClient(new OkHttpClient.Builder().readTimeout(120, TimeUnit.SECONDS))
            .build();

        try (InfluxDBClient client = InfluxDBClientFactory.create(options)) {
            List<FluxTable> results = client.getQueryApi().query(query);
            return (Long) results.get(0).getRecords().get(0).getValueByKey("temperature");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void console(Object msg) {
        System.out.println(msg);
    }
}
