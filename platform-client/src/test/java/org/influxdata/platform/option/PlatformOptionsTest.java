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
package org.influxdata.platform.option;

import java.io.IOException;

import org.influxdata.platform.AbstractMockServerTest;

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
 * @author Jakub Bednar (bednar@github) (05/09/2018 10:38)
 */
@RunWith(JUnitPlatform.class)
class PlatformOptionsTest extends AbstractMockServerTest {

    private String platformURL;

    @BeforeEach
    void before() {

        platformURL = startMockServer();
    }

    @Test
    void defaultValue() {

        PlatformOptions options = PlatformOptions.builder().url("http://localhost:9999")
                .authenticateToken("xyz".toCharArray())
                .build();

        Assertions.assertThat(options.getUrl()).isEqualTo("http://localhost:9999");
        Assertions.assertThat(options.getAuthScheme()).isEqualTo(PlatformOptions.AuthScheme.TOKEN);
        Assertions.assertThat(options.getOkHttpClient()).isNotNull();
    }

    @Test
    void okHttpBuilderAuthenticate() throws IOException, InterruptedException {

        OkHttpClient.Builder okHttpClient = new OkHttpClient.Builder();
        PlatformOptions options = PlatformOptions.builder()
                .url(platformURL)
                .authenticateToken("xyz".toCharArray())
                .okHttpClient(okHttpClient, true)
                .build();

        Assertions.assertThat(options.getOkHttpClient()).isEqualTo(okHttpClient);

        mockServer.enqueue(new MockResponse());

        Request request = new Request.Builder()
                .url(platformURL)
                .get()
                .build();

        options.getOkHttpClient().build().newCall(request).execute();

        RecordedRequest recordedRequest = mockServer.takeRequest();
        Assertions.assertThat(recordedRequest.getHeader("Authorization")).isEqualTo("Token xyz");
    }

    @Test
    void okHttpBuilderNotAuthenticate() throws IOException, InterruptedException {

        OkHttpClient.Builder okHttpClient = new OkHttpClient.Builder();
        PlatformOptions options = PlatformOptions.builder()
                .url(platformURL)
                .okHttpClient(okHttpClient, false)
                .build();

        Assertions.assertThat(options.getOkHttpClient()).isEqualTo(okHttpClient);

        mockServer.enqueue(new MockResponse());

        Request request = new Request.Builder()
                .url(platformURL)
                .get()
                .build();

        options.getOkHttpClient().build().newCall(request).execute();

        RecordedRequest recordedRequest = mockServer.takeRequest();
        Assertions.assertThat(recordedRequest.getHeader("Authorization")).isNull();
    }

    @Test
    void urlRequired() {

        PlatformOptions.Builder builder = PlatformOptions.builder()
                .authenticateToken("xyz".toCharArray());

        Assertions.assertThatThrownBy(builder::build)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("The url to connect to Platform has to be defined.");
    }

    @Test
    void authorizationNone() throws IOException, InterruptedException {

        PlatformOptions options = PlatformOptions.builder()
                .url(platformURL)
                .build();

        mockServer.enqueue(new MockResponse());

        Request request = new Request.Builder()
                .url(options.getUrl())
                .get()
                .build();

        options.getOkHttpClient().build().newCall(request).execute();

        Assertions.assertThat(mockServer.getRequestCount()).isEqualTo(1);

        RecordedRequest recordedRequest = mockServer.takeRequest();
        Assertions.assertThat(recordedRequest.getHeader("Authorization")).isNull();
    }

    @Test
    void authorizationSession() throws IOException, InterruptedException {

        PlatformOptions options = PlatformOptions.builder()
                .url(platformURL)
                .authenticate("user", "secret".toCharArray())
                .build();

        Assertions.assertThat(options.getAuthScheme()).isEqualTo(PlatformOptions.AuthScheme.SESSION);

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
                .url(platformURL + "api/v2/tasks")
                .get()
                .build();

        options.getOkHttpClient().build().newCall(request).execute();

        // Sign in request
        RecordedRequest requestToSignIn = mockServer.takeRequest();
        Assertions.assertThat(requestToSignIn.getPath()).isEqualTo("/api/v2/signin");
        Assertions.assertThat(requestToSignIn.getHeader("Authorization"))
                .isEqualTo(Credentials.basic("user", "secret"));

        RecordedRequest requestToTasks = mockServer.takeRequest();
        Assertions.assertThat(requestToTasks.getPath()).isEqualTo("/api/v2/tasks");
        Assertions.assertThat(requestToTasks.getHeader("Cookie"))
                .isEqualTo("session=yCgXaEBF8mYSmJUweRcW0g_5jElMs7mv6_-G1bNcau4Z0ZLQYtj0BkHZYRnBVA6uXHtyuhflcOzyNDNRxnaC0A==");
    }

    @Test
    void authorizationSessionWithoutCookie() throws IOException, InterruptedException {

        PlatformOptions options = PlatformOptions.builder()
                .url(platformURL)
                .authenticate("user", "secret".toCharArray())
                .build();

        Assertions.assertThat(options.getAuthScheme()).isEqualTo(PlatformOptions.AuthScheme.SESSION);

        MockResponse sigInResponse = new MockResponse();

        mockServer.enqueue(sigInResponse);
        mockServer.enqueue(new MockResponse());

        Request request = new Request.Builder()
                .url(platformURL + "api/v2/tasks")
                .get()
                .build();

        options.getOkHttpClient().build().newCall(request).execute();

        // Sign in request
        RecordedRequest requestToSignIn = mockServer.takeRequest();
        Assertions.assertThat(requestToSignIn.getPath()).isEqualTo("/api/v2/signin");
        Assertions.assertThat(requestToSignIn.getHeader("Authorization"))
                .isEqualTo(Credentials.basic("user", "secret"));

        RecordedRequest requestToTasks = mockServer.takeRequest();
        Assertions.assertThat(requestToTasks.getPath()).isEqualTo("/api/v2/tasks");
        Assertions.assertThat(requestToTasks.getHeader("Cookie")).isNull();
    }

    @Test
    void authorizationToken() throws IOException, InterruptedException {

        PlatformOptions options = PlatformOptions.builder()
                .url(platformURL)
                .authenticateToken("xyz".toCharArray())
                .build();

        Assertions.assertThat(options.getAuthScheme()).isEqualTo(PlatformOptions.AuthScheme.TOKEN);

        mockServer.enqueue(new MockResponse());

        Request request = new Request.Builder()
                .url(options.getUrl())
                .get()
                .build();

        options.getOkHttpClient().build().newCall(request).execute();

        RecordedRequest recordedRequest = mockServer.takeRequest();
        Assertions.assertThat(recordedRequest.getHeader("Authorization")).isEqualTo("Token xyz");
    }
}