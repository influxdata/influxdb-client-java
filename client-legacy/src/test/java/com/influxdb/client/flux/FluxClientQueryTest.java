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
package com.influxdb.client.flux;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Predicate;
import javax.annotation.Nonnull;

import com.influxdb.annotations.Column;
import com.influxdb.exceptions.InfluxException;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (31/07/2018 07:05)
 */
@RunWith(JUnitPlatform.class)
class FluxClientQueryTest extends AbstractFluxClientTest {

    @Test
    void query() {

        mockServer.enqueue(createResponse());

        List<FluxTable> result = fluxClient.query("from(bucket:\"telegraf\")");

        assertSuccessResult(result);
    }

    @Test
    void queryToPOJO() {

        mockServer.enqueue(createResponse());

        List<Free> result = fluxClient.query("from(bucket:\"telegraf\")", Free.class);

        Assertions.assertThat(result).hasSize(4);

        // 1
        Assertions.assertThat(result.get(0).host).isEqualTo("A");
        Assertions.assertThat(result.get(0).region).isEqualTo("west");
        Assertions.assertThat(result.get(0).mem).isEqualTo(10L);

        // 2
        Assertions.assertThat(result.get(1).host).isEqualTo("B");
        Assertions.assertThat(result.get(1).region).isEqualTo("west");
        Assertions.assertThat(result.get(1).mem).isEqualTo(20L);

        // 3
        Assertions.assertThat(result.get(2).host).isEqualTo("A");
        Assertions.assertThat(result.get(2).region).isEqualTo("west");
        Assertions.assertThat(result.get(2).mem).isEqualTo(11L);

        // 4
        Assertions.assertThat(result.get(3).host).isEqualTo("B");
        Assertions.assertThat(result.get(3).region).isEqualTo("west");
        Assertions.assertThat(result.get(3).mem).isEqualTo(22L);
    }

    @Test
    void queryError() {

        mockServer.enqueue(createErrorResponse("Flux query is not valid"));

        Assertions.assertThatThrownBy(() -> fluxClient.query("from(bucket:\"telegraf\")"))
                .isInstanceOf(InfluxException.class)
                .hasMessage("Flux query is not valid");
    }

    @Test
    void queryErrorSuccessResponse() {

        String error =
                "#datatype,string,string\n"
                        + "#group,true,true\n"
                        + "#default,,\n"
                        + ",error,reference\n"
                        + ",failed to create physical plan: invalid time bounds from procedure from: bounds contain zero time,897";

        mockServer.enqueue(createResponse(error));

        Assertions.assertThatThrownBy(() -> fluxClient.query("from(bucket:\"telegraf\")"))
                .isInstanceOf(InfluxException.class)
                .hasMessage("failed to create physical plan: invalid time bounds from procedure from: bounds contain zero time")
                .matches((Predicate<Throwable>) throwable -> ((InfluxException) throwable).reference() == 897);
    }

    @Test
    void queryErrorSuccessResponseWithoutReference() {

        String error =
                "#datatype,string,string\n"
                        + "#group,true,true\n"
                        + "#default,,\n"
                        + ",error,reference\n"
                        + ",failed to create physical plan: invalid time bounds from procedure from: bounds contain zero time,";

        mockServer.enqueue(createResponse(error));

        Assertions.assertThatThrownBy(() -> fluxClient.query("from(bucket:\"telegraf\")"))
                .isInstanceOf(InfluxException.class)
                .hasMessage("failed to create physical plan: invalid time bounds from procedure from: bounds contain zero time");
    }

    @Test
    void queryCallback() {

        countDownLatch = new CountDownLatch(4);

        mockServer.enqueue(createResponse());

        List<FluxRecord> records = new ArrayList<>();
        fluxClient.query("from(bucket:\"telegraf\")", (cancellable, result) -> {
            records.add(result);

            countDownLatch.countDown();
        });

        waitToCallback();

        assertRecords(records);
    }

    @Test
    void queryCallbackOnComplete() {

        countDownLatch = new CountDownLatch(5);

        mockServer.enqueue(createResponse());

        List<FluxRecord> records = new ArrayList<>();
        fluxClient.query("from(bucket:\"telegraf\")", (cancellable, result) -> {
            records.add(result);

            countDownLatch.countDown();
        }, throwable -> Assertions.fail("Unreachable"), () -> countDownLatch.countDown());

        waitToCallback();

        assertRecords(records);
    }

    @Test
    void queryCallbackError() {

        mockServer.enqueue(createErrorResponse("Flux query is not valid", true));

        fluxClient.query("from(bucket:\"telegraf\")", (cancellable, result) -> {

            Assertions.fail("Unreachable");
        }, throwable -> countDownLatch.countDown());

        waitToCallback();
    }

    private void assertSuccessResult(@Nonnull final List<FluxTable> tables) {

        Assertions.assertThat(tables).isNotNull();
        Assertions.assertThat(tables).hasSize(1);
        List<FluxRecord> records = tables.get(0).getRecords();
        assertRecords(records);
    }

    private void assertRecords(@Nonnull final List<FluxRecord> records) {
        Assertions.assertThat(records).hasSize(4);
    }

    public static class Free {

        @Column(name = "host")
        private String host;

        @Column(name = "region")
        private String region;

        @Column(name = "_value")
        private Long mem;
    }
}