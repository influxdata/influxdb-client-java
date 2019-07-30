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
package com.influxdb.test;

import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;

/**
 * @author Jakub Bednar (bednar@github) (05/10/2018 08:36)
 */
public abstract class AbstractMockServerTest extends AbstractTest {

    private static final int INTERNAL_SERVER_ERROR = 500;
    protected MockWebServer mockServer;

    /**
     * Start Mock server.
     *
     * @return the mock server URL
     */
    @Nonnull
    protected String startMockServer() {

        mockServer = new MockWebServer();
        try {
            mockServer.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return mockServer.url("/").url().toString();
    }

    @AfterEach
    protected void after() throws IOException {
        if (mockServer != null) {
            mockServer.shutdown();
        }
    }

    @Nonnull
    protected MockResponse createResponse(final String data) {
        return createResponse(data, "text/csv", true);
    }

    @Nonnull
    protected MockResponse createResponse(final String data, final String contentType, final boolean chunked) {

        MockResponse response = new MockResponse()
                .setHeader("Content-Type", contentType + "; charset=utf-8")
                .setHeader("Date", "Tue, 26 Jun 2018 13:15:01 GMT");

        if (chunked) {
            response.setChunkedBody(data, data.length());
        } else {
            response.setBody(data);
        }

        return response;
    }

    @Nonnull
    protected MockResponse createErrorResponse(@Nullable final String influxError) {
        return createErrorResponse(influxError, false);
    }

    @Nonnull
    protected MockResponse createErrorResponse(@Nullable final String influxError, final boolean chunked) {
        return createErrorResponse(influxError, chunked, INTERNAL_SERVER_ERROR);
    }

    @Nonnull
    protected MockResponse createErrorResponse(@Nullable final String influxError,
                                               final boolean chunked,
                                               final int responseCode) {

        String body = String.format("{\"error\":\"%s\"}", influxError);

        MockResponse mockResponse = new MockResponse()
                .setResponseCode(responseCode)
                .addHeader("X-Influx-Error", influxError);

        if (chunked) {
            return mockResponse.setChunkedBody(body, body.length());
        }

        return mockResponse.setBody(body);
    }
}