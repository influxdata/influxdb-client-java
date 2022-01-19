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
package com.influxdb.client.internal;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.client.write.events.WriteErrorEvent;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * This test ensures publish is called with WriteErrorEvent as described in
 * https://github.com/influxdata/influxdb-client-java/issues/291
 */
@RunWith(JUnitPlatform.class)
public class PublishRuntimeErrorAsWriteErrorEventTest extends AbstractInfluxDBClientTest {

    @Test
    void publishRuntimeErrorAsWriteErrorEvent() {
        WriteApi writeApi = influxDBClient.makeWriteApi();

        CompletableFuture<Throwable> errorEventTriggered = new CompletableFuture<>();
        writeApi.listenEvents(WriteErrorEvent.class, event -> {
            Throwable exception = event.getThrowable();
            errorEventTriggered.complete(exception);
        });
        CompletableFuture<Object> supplyAsync = CompletableFuture.supplyAsync(() -> {
            for (int i = 0; i < 100000; i++) {
                writeApi.writePoint("my-bucket", "my-org", new Point("foo" + i).time(Instant.now(), WritePrecision.MS).addField("value", i));
            }
            return null;
        });
        try {
            supplyAsync.get(1, TimeUnit.MINUTES);
            assertNotNull(errorEventTriggered.get(1, TimeUnit.MINUTES));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
            fail(e);
        }
    }
}
