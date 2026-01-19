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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.influxdb.client.domain.WriteConsistency;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.internal.AbstractInfluxDBClientTest;
import com.influxdb.client.write.WriteParameters;
import com.influxdb.client.write.events.WriteSuccessEvent;

import io.reactivex.rxjava3.schedulers.TestScheduler;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the getPreBatchBufferSize functionality in WriteApi.
 * 
 * This test verifies that the pre-batch buffer size tracking works correctly,
 * allowing users to monitor how many data points are waiting to be batched
 * before being sent to InfluxDB.
 * 
 * @author Fred Park
 */
class PreBatchBufferSizeTest extends AbstractInfluxDBClientTest {

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
    void testReturnsZeroWhenNoDataWritten() {
        writeApi = influxDBClient.makeWriteApi(WriteOptions.builder()
                .batchSize(100)
                .flushInterval(10_000)
                .build());

        // Test all method overloads return 0 for non-existent destinations
        Assertions.assertThat(writeApi.getPreBatchBufferSize("bucket", "org", WritePrecision.NS))
                .isEqualTo(0);
        Assertions.assertThat(writeApi.getPreBatchBufferSize("bucket", "org", WritePrecision.NS, WriteConsistency.ALL))
                .isEqualTo(0);
        Assertions.assertThat(writeApi.getPreBatchBufferSize(new WriteParameters("bucket", "org", WritePrecision.NS)))
                .isEqualTo(0);
        Assertions.assertThat(writeApi.getPreBatchBufferSizes())
                .isEmpty();
    }

    @Test
    void testBufferSizeAccumulatesWithWriteRecord() throws InterruptedException {
        mockServer.enqueue(createResponse("{}"));

        writeApi = influxDBClient.makeWriteApi(WriteOptions.builder()
                .batchSize(100)
                .flushInterval(10_000)
                .enableBufferTracking(true)
                .writeScheduler(scheduler)
                .build());

        writeApi.writeRecord("test-bucket", "test-org", WritePrecision.NS,
                "measurement,tag=value field=1i 1");
        writeApi.writeRecord("test-bucket", "test-org", WritePrecision.NS,
                "measurement,tag=value field=2i 2");
        writeApi.writeRecord("test-bucket", "test-org", WritePrecision.NS,
                "measurement,tag=value field=3i 3");

        Thread.sleep(100);

        Assertions.assertThat(writeApi.getPreBatchBufferSize("test-bucket", "test-org", WritePrecision.NS))
                .isEqualTo(3);
    }

    @Test
    void testBufferSizeAccumulatesWithWriteRecords() throws InterruptedException {
        mockServer.enqueue(createResponse("{}"));

        writeApi = influxDBClient.makeWriteApi(WriteOptions.builder()
                .batchSize(100)
                .flushInterval(10_000)
                .enableBufferTracking(true)
                .writeScheduler(scheduler)
                .build());

        List<String> records = Arrays.asList(
                "measurement,tag=value field=1i 1",
                "measurement,tag=value field=2i 2",
                "measurement,tag=value field=3i 3",
                "measurement,tag=value field=4i 4",
                "measurement,tag=value field=5i 5"
        );
        writeApi.writeRecords("test-bucket", "test-org", WritePrecision.NS, records);

        Thread.sleep(100);

        Assertions.assertThat(writeApi.getPreBatchBufferSize("test-bucket", "test-org", WritePrecision.NS))
                .isEqualTo(5);
    }

    @Test
    void testBufferResetsAfterFlush() throws InterruptedException {
        mockServer.enqueue(createResponse("{}"));

        writeApi = influxDBClient.makeWriteApi(WriteOptions.builder()
                .batchSize(100)
                .flushInterval(10_000)
                .enableBufferTracking(true)
                .writeScheduler(scheduler)
                .build());

        WriteEventListener<WriteSuccessEvent> listener = new WriteEventListener<>();
        writeApi.listenEvents(WriteSuccessEvent.class, listener);

        writeApi.writeRecord("test-bucket", "test-org", WritePrecision.NS,
                "measurement,tag=value field=1i 1");

        writeApi.flush();
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
        listener.awaitCount(1);

        Assertions.assertThat(writeApi.getPreBatchBufferSize("test-bucket", "test-org", WritePrecision.NS))
                .isEqualTo(0);
    }

    @Test
    void testBufferResetsWhenBatchSizeReached() throws InterruptedException {
        mockServer.enqueue(createResponse("{}"));

        writeApi = influxDBClient.makeWriteApi(WriteOptions.builder()
                .batchSize(3)
                .flushInterval(10_000)
                .enableBufferTracking(true)
                .build());

        Assertions.assertThat(writeApi.getPreBatchBufferSize("test-bucket", "test-org", WritePrecision.NS))
                .isEqualTo(0);

        WriteEventListener<WriteSuccessEvent> listener = new WriteEventListener<>();
        writeApi.listenEvents(WriteSuccessEvent.class, listener);

        writeApi.writeRecord("test-bucket", "test-org", WritePrecision.NS, "m,t=v f=1i 1");
        writeApi.writeRecord("test-bucket", "test-org", WritePrecision.NS, "m,t=v f=2i 2");
        Assertions.assertThat(writeApi.getPreBatchBufferSize("test-bucket", "test-org", WritePrecision.NS))
                .isEqualTo(2);

        writeApi.writeRecord("test-bucket", "test-org", WritePrecision.NS, "m,t=v f=3i 3");
        listener.awaitCount(1);
        Assertions.assertThat(writeApi.getPreBatchBufferSize("test-bucket", "test-org", WritePrecision.NS))
                .isEqualTo(0);
    }

    @Test
    void testDifferentDestinationsTrackedSeparately() throws InterruptedException {
        mockServer.enqueue(createResponse("{}"));
        mockServer.enqueue(createResponse("{}"));
        mockServer.enqueue(createResponse("{}"));

        writeApi = influxDBClient.makeWriteApi(WriteOptions.builder()
                .batchSize(100)
                .flushInterval(10_000)
                .enableBufferTracking(true)
                .writeScheduler(scheduler)
                .build());

        // Different bucket/org
        writeApi.writeRecord("bucket1", "org1", WritePrecision.NS, "m,t=v f=1i 1");
        // Different precision
        writeApi.writeRecord("bucket1", "org1", WritePrecision.MS, "m,t=v f=2i 2");
        // Different bucket
        writeApi.writeRecord("bucket2", "org1", WritePrecision.NS, "m,t=v f=3i 3");

        Thread.sleep(100);

        Assertions.assertThat(writeApi.getPreBatchBufferSize("bucket1", "org1", WritePrecision.NS)).isEqualTo(1);
        Assertions.assertThat(writeApi.getPreBatchBufferSize("bucket1", "org1", WritePrecision.MS)).isEqualTo(1);
        Assertions.assertThat(writeApi.getPreBatchBufferSize("bucket2", "org1", WritePrecision.NS)).isEqualTo(1);
        
        Map<WriteParameters, Integer> sizes = writeApi.getPreBatchBufferSizes();
        Assertions.assertThat(sizes).hasSize(3);
    }

    @Test
    void testConsistencyDistinguishesDifferentBuffers() {
        writeApi = influxDBClient.makeWriteApi(WriteOptions.builder()
                .batchSize(100)
                .flushInterval(10_000)
                .build());

        WriteParameters paramsOne = new WriteParameters("bucket", "org", WritePrecision.NS, WriteConsistency.ONE);
        WriteParameters paramsAll = new WriteParameters("bucket", "org", WritePrecision.NS, WriteConsistency.ALL);

        // Different consistency values should be different keys
        Assertions.assertThat(paramsOne).isNotEqualTo(paramsAll);

        // Both should return 0 independently
        Assertions.assertThat(writeApi.getPreBatchBufferSize(paramsOne)).isEqualTo(0);
        Assertions.assertThat(writeApi.getPreBatchBufferSize(paramsAll)).isEqualTo(0);
        
        // Null consistency should work
        Assertions.assertThat(writeApi.getPreBatchBufferSize("bucket", "org", WritePrecision.NS, null))
                .isEqualTo(0);
    }

    @Test
    void testInputValidation() {
        writeApi = influxDBClient.makeWriteApi(WriteOptions.builder()
                .batchSize(100)
                .flushInterval(10_000)
                .build());

        // Empty bucket - 3 param overload
        Assertions.assertThatThrownBy(() -> 
                writeApi.getPreBatchBufferSize("", "org", WritePrecision.NS))
                .isInstanceOf(IllegalArgumentException.class);

        // Empty org - 3 param overload
        Assertions.assertThatThrownBy(() -> 
                writeApi.getPreBatchBufferSize("bucket", "", WritePrecision.NS))
                .isInstanceOf(IllegalArgumentException.class);

        // Null precision
        Assertions.assertThatThrownBy(() -> 
                writeApi.getPreBatchBufferSize("bucket", "org", null))
                .isInstanceOf(NullPointerException.class);

        // Null WriteParameters
        Assertions.assertThatThrownBy(() -> 
                writeApi.getPreBatchBufferSize(null))
                .isInstanceOf(NullPointerException.class);

        // Empty bucket - 4 param overload
        Assertions.assertThatThrownBy(() -> 
                writeApi.getPreBatchBufferSize("", "org", WritePrecision.NS, WriteConsistency.ALL))
                .isInstanceOf(IllegalArgumentException.class);

        // Empty org - 4 param overload
        Assertions.assertThatThrownBy(() -> 
                writeApi.getPreBatchBufferSize("bucket", "", WritePrecision.NS, WriteConsistency.ALL))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testBufferTrackingDisabled() throws InterruptedException {
        mockServer.enqueue(createResponse("{}"));

        // Buffer tracking is disabled by default
        writeApi = influxDBClient.makeWriteApi(WriteOptions.builder()
                .batchSize(100)
                .flushInterval(10_000)
                .writeScheduler(scheduler)
                .build());

        // Write some records
        writeApi.writeRecord("test-bucket", "test-org", WritePrecision.NS,
                "measurement,tag=value field=1i 1");
        writeApi.writeRecord("test-bucket", "test-org", WritePrecision.NS,
                "measurement,tag=value field=2i 2");

        Thread.sleep(100);

        // Buffer size should always be 0 when tracking is disabled
        Assertions.assertThat(writeApi.getPreBatchBufferSize("test-bucket", "test-org", WritePrecision.NS))
                .isEqualTo(0);
        Assertions.assertThat(writeApi.getPreBatchBufferSizes())
                .isEmpty();
    }
}
