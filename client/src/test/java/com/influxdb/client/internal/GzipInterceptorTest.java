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

import java.io.IOException;
import javax.annotation.Nonnull;

import com.influxdb.test.AbstractMockServerTest;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (15/10/2018 11:27)
 */
@RunWith(JUnitPlatform.class)
class GzipInterceptorTest extends AbstractMockServerTest {

    private String url;
    private GzipInterceptor interceptor;

    @BeforeEach
    void setUp() {

        interceptor = new GzipInterceptor();
        url = startMockServer();
    }

    @Test
    void defaultDisabled() {

        Assertions.assertThat(interceptor.isEnabledGzip()).isFalse();
    }

    @Test
    void gzipDisabled() throws IOException, InterruptedException {

        interceptor.disableGzip();

        RecordedRequest recordedRequest = newCall("/api/v2/users");
        Assertions.assertThat(recordedRequest.getHeader("Content-Encoding")).isNull();
        Assertions.assertThat(recordedRequest.getHeader("Accept-Encoding")).doesNotContain("gzip");

        recordedRequest = newCall("/api/v2/write");
        Assertions.assertThat(recordedRequest.getHeader("Content-Encoding")).isNull();
        Assertions.assertThat(recordedRequest.getHeader("Accept-Encoding")).doesNotContain("gzip");

        recordedRequest = newCall("/api/v2/query");
        Assertions.assertThat(recordedRequest.getHeader("Content-Encoding")).isNull();
        Assertions.assertThat(recordedRequest.getHeader("Accept-Encoding")).doesNotContain("gzip");
    }

    @Test
    void gzipEnabled() throws IOException, InterruptedException {

        interceptor.enableGzip();

        RecordedRequest recordedRequest = newCall("/api/v2/users");
        Assertions.assertThat(recordedRequest.getHeader("Content-Encoding")).isNull();
        Assertions.assertThat(recordedRequest.getHeader("Accept-Encoding")).doesNotContain("gzip");

        recordedRequest = newCall("/api/v2/write");
        Assertions.assertThat(recordedRequest.getHeader("Content-Encoding")).isEqualTo("gzip");
        Assertions.assertThat(recordedRequest.getHeader("Accept-Encoding")).doesNotContain("gzip");

        recordedRequest = newCall("/api/v2/query");
        Assertions.assertThat(recordedRequest.getHeader("Content-Encoding")).isNull();
        Assertions.assertThat(recordedRequest.getHeader("Accept-Encoding")).isEqualTo("gzip");
    }

    @Nonnull
    private RecordedRequest newCall(@Nonnull final String path) throws IOException, InterruptedException {

        mockServer.enqueue(new MockResponse());

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();

        Request request = new Request.Builder()
                .url(url + path)
                .addHeader("accept", "application/json")
                .post(RequestBody.create(MediaType.parse("application/json"), "{name: \"Tom Type\"}"))
                .build();

        okHttpClient.newCall(request).execute();

        return mockServer.takeRequest();
    }
}