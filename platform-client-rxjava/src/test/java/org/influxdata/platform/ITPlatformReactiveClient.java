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

import org.influxdata.platform.domain.Health;
import org.influxdata.platform.rest.LogLevel;

import io.reactivex.Single;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (20/11/2018 08:06)
 */
@RunWith(JUnitPlatform.class)
class ITPlatformReactiveClient extends AbstractITPlatformClientTest {

    @Test
    void queryClient() {

        Assertions.assertThat(platformClient.createQueryClient()).isNotNull();
    }

    @Test
    void health() {

        Single<Health> health = platformClient.health();

        health
                .test()
                .assertNoErrors()
                .assertValue(it -> {

                    Assertions.assertThat(it).isNotNull();
                    Assertions.assertThat(it.isHealthy()).isTrue();
                    Assertions.assertThat(it.getMessage()).isEqualTo("howdy y'all");

                    return true;
                });
    }

    @Test
    void healthNotRunningInstance() throws Exception {

        PlatformClientReactive clientNotRunning = PlatformClientReactiveFactory.create("http://localhost:8099");
        Single<Health> health = clientNotRunning.health();

        health.test().assertError(throwable -> {

            Assertions.assertThat(throwable).hasMessageStartingWith("Failed to connect to");

            return true;
        });

        clientNotRunning.close();
    }

    @Test
    void logLevel() {

        // default NONE
        Assertions.assertThat(this.platformClient.getLogLevel()).isEqualTo(LogLevel.NONE);

        // set HEADERS
        PlatformClientReactive platformClient = this.platformClient.setLogLevel(LogLevel.HEADERS);
        Assertions.assertThat(platformClient).isEqualTo(this.platformClient);

        Assertions.assertThat(this.platformClient.getLogLevel()).isEqualTo(LogLevel.HEADERS);
    }

    @Test
    void gzip() {

        Assertions.assertThat(platformClient.isGzipEnabled()).isFalse();

        // Enable GZIP
        PlatformClientReactive platformClient = this.platformClient.enableGzip();
        Assertions.assertThat(platformClient).isEqualTo(this.platformClient);
        Assertions.assertThat(this.platformClient.isGzipEnabled()).isTrue();

        // Disable GZIP
        platformClient = this.platformClient.disableGzip();
        Assertions.assertThat(platformClient).isEqualTo(this.platformClient);
        Assertions.assertThat(this.platformClient.isGzipEnabled()).isFalse();
    }
}