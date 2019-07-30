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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.influxdb.exceptions.InfluxException;

import okhttp3.mockwebserver.MockResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (03/08/2018 12:14)
 */
@RunWith(JUnitPlatform.class)
class FluxClientQueryRawTest extends AbstractFluxClientTest {

    @Test
    void queryRaw() {

        mockServer.enqueue(createResponse());

        String result = fluxClient.queryRaw("from(bucket:\"telegraf\")");

        assertSuccessResult(result);
    }

    @Test
    void queryRawError() {

        mockServer.enqueue(createErrorResponse());

        Assertions.assertThatThrownBy(() -> fluxClient.queryRaw("from(bucket:\"telegraf\")"))
                .hasMessage("Flux query is not valid")
                .isInstanceOf(InfluxException.class);
    }

    @Test
    void queryRawCallback() {

        countDownLatch = new CountDownLatch(8);

        mockServer.enqueue(createResponse());

        List<String> results = new ArrayList<>();
        fluxClient.queryRaw("from(bucket:\"telegraf\")", (cancellable, result) -> {
            results.add(result);
            countDownLatch.countDown();
        });

        waitToCallback();

        Assertions.assertThat(results).hasSize(8);
        assertSuccessResult(String.join("\n", results));
    }

    @Test
    void queryRawCallbackOnComplete() {

        countDownLatch = new CountDownLatch(1);
        mockServer.enqueue(createResponse());

        List<String> results = new ArrayList<>();
        fluxClient.queryRaw("from(bucket:\"telegraf\")", null,
                (cancellable, result) -> results.add(result),
                throwable -> Assertions.fail("Unreachable"),
                () -> countDownLatch.countDown());

        waitToCallback();
        assertSuccessResult(String.join("\n", results));
    }

    @Test
    void queryRawCallbackOnError() throws IOException {

        mockServer.shutdown();

        fluxClient.queryRaw("from(bucket:\"telegraf\")",
                (cancellable, result) -> Assertions.fail("Unreachable"),
                throwable -> countDownLatch.countDown());

        waitToCallback();
    }

    private void assertSuccessResult(@Nullable final String result) {

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result).isEqualTo(SUCCESS_DATA);
    }

    @Nonnull
    private MockResponse createErrorResponse() {
        return createErrorResponse("Flux query is not valid");
    }

}