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

import com.influxdb.client.InfluxDBClientOptions;
import com.influxdb.test.AbstractMockServerTest;

import okhttp3.Cookie;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (12/10/2018 12:50)
 */
@RunWith(JUnitPlatform.class)
class AuthenticateInterceptorTest extends AbstractMockServerTest {

    private String influxDB_URL;

    @BeforeEach
    void before() {

        influxDB_URL = startMockServer();
    }

    @Test
    void authorizationNone() throws IOException, InterruptedException {

        InfluxDBClientOptions options = InfluxDBClientOptions.builder()
                .url(influxDB_URL)
                .build();

        mockServer.enqueue(new MockResponse());

        Request request = new Request.Builder()
                .url(options.getUrl())
                .get()
                .build();

        buildOkHttpClient(options).newCall(request).execute();

        Assertions.assertThat(mockServer.getRequestCount()).isEqualTo(1);

        RecordedRequest recordedRequest = mockServer.takeRequest();
        Assertions.assertThat(recordedRequest.getHeader("Authorization")).isNull();
    }

    @Test
    void authorizationSession() throws IOException, InterruptedException {

        InfluxDBClientOptions options = InfluxDBClientOptions.builder()
                .url(influxDB_URL)
                .authenticate("user", "secret".toCharArray())
                .build();

        Assertions.assertThat(options.getAuthScheme()).isEqualTo(InfluxDBClientOptions.AuthScheme.SESSION);

        Cookie session = new Cookie.Builder()
                .name("session")
                .value("yCgXaEBF8mYSmJUweRcW0g_5jElMs7mv6_-G1bNcau4Z0ZLQYtj0BkHZYRnBVA6uXHtyuhflcOzyNDNRxnaC0A==")
                .hostOnlyDomain("127.0.0.1")
                .path("/api/v2")
                .build();

        MockResponse sigInResponse = new MockResponse()
                .addHeader(String.format("Set-Cookie: %s ", session.toString()));

        mockServer.enqueue(sigInResponse);
        mockServer.enqueue(new MockResponse());

        Request request = new Request.Builder()
                .url(influxDB_URL + "/api/v2/tasks")
                .get()
                .build();

        buildOkHttpClient(options).newCall(request).execute();

        // Sign in request
        RecordedRequest requestToSignIn = mockServer.takeRequest();
        Assertions.assertThat(requestToSignIn.getPath()).endsWith("/api/v2/signin");
        Assertions.assertThat(requestToSignIn.getHeader("Authorization"))
                .isEqualTo(Credentials.basic("user", "secret"));

        RecordedRequest requestToTasks = mockServer.takeRequest();
        Assertions.assertThat(requestToTasks.getPath()).endsWith("/api/v2/tasks");
        Assertions.assertThat(requestToTasks.getHeader("Cookie"))
                .isEqualTo("session=yCgXaEBF8mYSmJUweRcW0g_5jElMs7mv6_-G1bNcau4Z0ZLQYtj0BkHZYRnBVA6uXHtyuhflcOzyNDNRxnaC0A==");
    }

    @Test
    void authorizationSessionWithoutCookie() throws IOException, InterruptedException {

        InfluxDBClientOptions options = InfluxDBClientOptions.builder()
                .url(influxDB_URL)
                .authenticate("user", "secret".toCharArray())
                .build();

        Assertions.assertThat(options.getAuthScheme()).isEqualTo(InfluxDBClientOptions.AuthScheme.SESSION);

        MockResponse sigInResponse = new MockResponse();

        mockServer.enqueue(sigInResponse);
        mockServer.enqueue(sigInResponse);
        mockServer.enqueue(new MockResponse());

        Request request = new Request.Builder()
                .url(influxDB_URL + "api/v2/tasks")
                .get()
                .build();

        buildOkHttpClient(options).newCall(request).execute();

        // Sign in request in init
        RecordedRequest requestToSignIn = mockServer.takeRequest();
        Assertions.assertThat(requestToSignIn.getPath()).endsWith("/api/v2/signin");
        Assertions.assertThat(requestToSignIn.getHeader("Authorization"))
                .isEqualTo(Credentials.basic("user", "secret"));

        // Sign in request in /tasks
        requestToSignIn = mockServer.takeRequest();
        Assertions.assertThat(requestToSignIn.getPath()).endsWith("/api/v2/signin");
        Assertions.assertThat(requestToSignIn.getHeader("Authorization"))
                .isEqualTo(Credentials.basic("user", "secret"));

        RecordedRequest requestToTasks = mockServer.takeRequest();
        Assertions.assertThat(requestToTasks.getPath()).endsWith("/api/v2/tasks");
        Assertions.assertThat(requestToTasks.getHeader("Cookie")).isNull();
    }

    @Test
    void authorizationSessionInfluxDBNotRunning() throws IOException {

        InfluxDBClientOptions options = InfluxDBClientOptions.builder()
                .url(influxDB_URL)
                .authenticate("user", "secret".toCharArray())
                .build();

        mockServer.shutdown();

        // without error
        buildOkHttpClient(options);
    }

    @Test
    void authorizationSessionSignout() throws IOException, InterruptedException {

        mockServer.enqueue(new MockResponse());
        mockServer.enqueue(new MockResponse());

        InfluxDBClientOptions options = InfluxDBClientOptions.builder()
                .url(influxDB_URL)
                .authenticate("user", "secret".toCharArray())
                .build();

        AuthenticateInterceptor interceptor = new AuthenticateInterceptor(options);
        OkHttpClient okHttpClient = options.getOkHttpClient().addInterceptor(interceptor).build();
        interceptor.initToken(okHttpClient);

        interceptor.signout();
        interceptor.signout();

        Assertions.assertThat(mockServer.getRequestCount()).isEqualTo(2);
        mockServer.takeRequest();

        RecordedRequest signoutRequest = mockServer.takeRequest();
        Assertions.assertThat(signoutRequest.getPath()).endsWith("/api/v2/signout");
    }

    @Test
    void authorizationToken() throws IOException, InterruptedException {

        InfluxDBClientOptions options = InfluxDBClientOptions.builder()
                .url(influxDB_URL)
                .authenticateToken("xyz".toCharArray())
                .build();

        Assertions.assertThat(options.getAuthScheme()).isEqualTo(InfluxDBClientOptions.AuthScheme.TOKEN);

        mockServer.enqueue(new MockResponse());

        Request request = new Request.Builder()
                .url(options.getUrl())
                .get()
                .build();

        buildOkHttpClient(options).newCall(request).execute();

        RecordedRequest recordedRequest = mockServer.takeRequest();
        Assertions.assertThat(recordedRequest.getHeader("Authorization")).isEqualTo("Token xyz");
    }

    @Test
    void authorizationTokenSignout() throws IOException {

        InfluxDBClientOptions options = InfluxDBClientOptions.builder()
                .url(influxDB_URL)
                .authenticateToken("xyz".toCharArray())
                .build();

        AuthenticateInterceptor interceptor = new AuthenticateInterceptor(options);
        OkHttpClient okHttpClient = options.getOkHttpClient().addInterceptor(interceptor).build();
        interceptor.initToken(okHttpClient);

        interceptor.signout();
        interceptor.signout();

        Assertions.assertThat(mockServer.getRequestCount()).isEqualTo(0);
    }


    @Nonnull
    private OkHttpClient buildOkHttpClient(@Nonnull final InfluxDBClientOptions options) {
        AuthenticateInterceptor interceptor = new AuthenticateInterceptor(options);
        OkHttpClient okHttpClient = options.getOkHttpClient().addInterceptor(interceptor).build();
        interceptor.initToken(okHttpClient);
        return okHttpClient;
    }
}