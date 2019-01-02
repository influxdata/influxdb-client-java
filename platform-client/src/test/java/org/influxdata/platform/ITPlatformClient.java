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

import java.time.Instant;

import org.influxdata.platform.domain.Health;
import org.influxdata.platform.domain.Ready;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (20/11/2018 07:37)
 */
@RunWith(JUnitPlatform.class)
class ITPlatformClient extends AbstractITClientTest {

    @BeforeEach
    void setUp() throws Exception  {
        super.setUp();
    }

    @Test
    void health() {

        Health health = platformClient.health();

        Assertions.assertThat(health).isNotNull();
        Assertions.assertThat(health.isHealthy()).isTrue();
        Assertions.assertThat(health.getMessage()).isEqualTo("ready for queries and writes");
    }

    @Test
    void healthNotRunningInstance() throws Exception {

        PlatformClient clientNotRunning = PlatformClientFactory.create("http://localhost:8099");
        Health health = clientNotRunning.health();

        Assertions.assertThat(health).isNotNull();
        Assertions.assertThat(health.isHealthy()).isFalse();
        Assertions.assertThat(health.getMessage()).startsWith("Failed to connect to");

        clientNotRunning.close();
    }

    @Test
    void ready() {

        Ready ready = platformClient.ready();

        Assertions.assertThat(ready).isNotNull();
        Assertions.assertThat(ready.getStatus()).isEqualTo("ready");
        Assertions.assertThat(ready.getStarted()).isNotNull();
        Assertions.assertThat(ready.getStarted()).isBefore(Instant.now());
        Assertions.assertThat(ready.getUp()).isNotBlank();
    }

    @Test
    void readyNotRunningInstance() throws Exception {

        PlatformClient clientNotRunning = PlatformClientFactory.create("http://localhost:8099");

        Ready ready = clientNotRunning.ready();
        Assertions.assertThat(ready).isNull();

        clientNotRunning.close();
    }
}