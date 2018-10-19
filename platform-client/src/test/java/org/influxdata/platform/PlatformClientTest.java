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
package org.influxdata.platform;

import javax.annotation.Nonnull;

import org.influxdata.platform.impl.AbstractPlatformClientTest;
import org.influxdata.platform.impl.TodoException;
import org.influxdata.platform.option.WriteOptions;
import org.influxdata.platform.rest.LogLevel;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (05/09/2018 14:00)
 */
@RunWith(JUnitPlatform.class)
class PlatformClientTest extends AbstractPlatformClientTest {

    @Test
    void createQueryClient() {
        Assertions.assertThat(platformClient.createQueryClient()).isNotNull();
    }

    @Test
    void createWriteClient() {

        Assertions.assertThat(platformClient.createWriteClient()).isNotNull();
        Assertions.assertThat(platformClient.createWriteClient(WriteOptions.DEFAULTS)).isNotNull();
    }

    @Test
    void createAuthorizationClient() {
        Assertions.assertThat(platformClient.createAuthorizationClient()).isNotNull();
    }

    @Test
    void createBucketClient() {
        Assertions.assertThat(platformClient.createBucketClient()).isNotNull();
    }

    @Test
    void createOrganizationClient() {
        Assertions.assertThat(platformClient.createOrganizationClient()).isNotNull();
    }

    @Test
    void createSourceClient() {
        Assertions.assertThat(platformClient.createSourceClient()).isNotNull();
    }

    @Test
    void createTaskClient() {
        todo(() -> platformClient.createTaskClient());
    }

    @Test
    void createUserClient() {
        Assertions.assertThat(platformClient.createUserClient()).isNotNull();
    }

    @Test
    void logLevel() {

        // default NONE
        Assertions.assertThat(this.platformClient.getLogLevel()).isEqualTo(LogLevel.NONE);

        // set HEADERS
        PlatformClient platformClient = this.platformClient.setLogLevel(LogLevel.HEADERS);
        Assertions.assertThat(platformClient).isEqualTo(this.platformClient);

        Assertions.assertThat(this.platformClient.getLogLevel()).isEqualTo(LogLevel.HEADERS);
    }

    @Test
    void gzip() {

        Assertions.assertThat(platformClient.isGzipEnabled()).isFalse();

        // Enable GZIP
        PlatformClient platformClient = this.platformClient.enableGzip();
        Assertions.assertThat(platformClient).isEqualTo(this.platformClient);
        Assertions.assertThat(this.platformClient.isGzipEnabled()).isTrue();

        // Disable GZIP
        platformClient = this.platformClient.disableGzip();
        Assertions.assertThat(platformClient).isEqualTo(this.platformClient);
        Assertions.assertThat(this.platformClient.isGzipEnabled()).isFalse();
    }

    @Test
    void close() throws Exception {

        platformClient.close();
        Assertions.assertThat(mockServer.getRequestCount()).isEqualTo(0);
    }

    @Test
    void closeWithSignout() throws Exception {

        mockServer.enqueue(new MockResponse());
        mockServer.enqueue(new MockResponse());

        PlatformClient platformClient = PlatformClientFactory
                .create(mockServer.url("/").toString(), "user", "password".toCharArray());

        Assertions.assertThat(mockServer.getRequestCount()).isEqualTo(1);
        platformClient.close();
        Assertions.assertThat(mockServer.getRequestCount()).isEqualTo(2);

        // sign in
        mockServer.takeRequest();
        // request to signout
        RecordedRequest signOut = mockServer.takeRequest();
        Assertions.assertThat(signOut.getPath()).endsWith("/api/v2/signout");
    }

    private void todo(@Nonnull final ThrowableAssert.ThrowingCallable callable) {
        Assertions.assertThatThrownBy(callable)
                .isInstanceOf(TodoException.class)
                .hasMessage("TODO");
    }
}