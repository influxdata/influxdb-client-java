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

import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.internal.AbstractInfluxDBClientTest;
import com.influxdb.client.write.Point;
import com.influxdb.client.write.events.BackpressureEvent;
import okhttp3.mockwebserver.MockResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * @author Fred Park (fredjoonpark@github)
 */
class ITCaptureBackpressureDataTest extends AbstractInfluxDBClientTest {

    private WriteApi writeApi;

    @AfterEach
    void tearDown() {
        if (writeApi != null) {
            writeApi.close();
        }
    }

    @Test
    void backpressureEventContainsDroppedLineProtocol() throws InterruptedException {
        List<BackpressureEvent> backpressureEvents = new CopyOnWriteArrayList<>();

        WriteOptions writeOptions = WriteOptions.builder()
                .batchSize(2) // Small batches
                .bufferLimit(4)
                .flushInterval(100_000) // Very long flush - batches only created when full
                .maxRetries(1)
                .retryInterval(100)
                .maxRetryDelay(100)
                .concatMapPrefetch(1)
                .captureBackpressureData(true)
                .build();

        writeApi = influxDBClient.makeWriteApi(writeOptions);

        // Listen for backpressure events
        writeApi.listenEvents(BackpressureEvent.class, event -> {
            System.out.println(">>> Backpressure event received: " + event.getReason()
                    + " (buffered items: " + event.getDroppedLineProtocol().size() + ")");
            backpressureEvents.add(event);
        });

        // Initial request + prefetched batch = 4 points
        // 2 batches loaded into buffer = 4 points
        // Write 2 more points (10 points total) to trigger flush and backpressure
        for (int i = 0; i < 10; i++) {
            Point point = Point.measurement("temperature")
                    .addTag("location", "room1")
                    .addField("value", 20.0 + i)
                    .time(System.nanoTime() + i, WritePrecision.NS);
            writeApi.writePoint("my-bucket", "my-org", point);
        }

        Assertions.assertThat(backpressureEvents)
                .as("Should have backpressure events")
                .isNotEmpty();

        boolean hasBufferedData = backpressureEvents.stream()
                .anyMatch(event -> !event.getDroppedLineProtocol().isEmpty());
        Assertions.assertThat(hasBufferedData)
                .as("Backpressure event should contain buffered line protocol")
                .isTrue();

        long eventsWithData = backpressureEvents.stream()
                .filter(event -> !event.getDroppedLineProtocol().isEmpty())
                .count();

        List<BackpressureEvent> batchBackpressure = backpressureEvents.stream()
                .filter(e -> !e.getDroppedLineProtocol().isEmpty())
                .collect(Collectors.toList());
        Assertions.assertThat(batchBackpressure)
                .as("Should have TOO_MUCH_BATCHES backpressure with buffered data")
                .isNotEmpty();

        // Verify the buffered line protocol is valid
        for (BackpressureEvent event : batchBackpressure) {
            System.out.println("  Reason: " + event.getReason());
            System.out.println("  Buffered points count: " + event.getDroppedLineProtocol().size());

            Assertions.assertThat(event.getDroppedLineProtocol().size())
                    .as("Should have buffered points")
                    .isGreaterThan(0);

            // Verify each line protocol string is valid
            for (String lineProtocol : event.getDroppedLineProtocol()) {
                Assertions.assertThat(lineProtocol)
                        .as("Line protocol should not be empty")
                        .isNotEmpty();

                Assertions.assertThat(lineProtocol)
                        .as("Line protocol should contain measurement name")
                        .contains("temperature");
            }
        }
    }
}
