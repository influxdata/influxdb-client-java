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
package com.influxdb.client.reactive;

import com.influxdb.LogLevel;
import com.influxdb.client.domain.HealthCheck;

import io.reactivex.Single;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (20/11/2018 08:06)
 */
@RunWith(JUnitPlatform.class)
class ITInfluxDBReactiveClient extends AbstractITInfluxDBClientTest {

    @Test
    void queryClient() {

        Assertions.assertThat(influxDBClient.getQueryReactiveApi()).isNotNull();
    }

    @Test
    void writeClient() {

        Assertions.assertThat(influxDBClient.getWriteReactiveApi()).isNotNull();
    }

    @Test
    void health() {

        Single<HealthCheck> check = influxDBClient.health();

        check
                .test()
                .assertNoErrors()
                .assertValue(it -> {

                    Assertions.assertThat(it).isNotNull();
                    Assertions.assertThat(it.getStatus()).isEqualTo(HealthCheck.StatusEnum.PASS);
                    Assertions.assertThat(it.getMessage()).isEqualTo("ready for queries and writes");

                    return true;
                });
    }

    @Test
    void healthNotRunningInstance() throws Exception {

        InfluxDBClientReactive clientNotRunning = InfluxDBClientReactiveFactory.create("http://localhost:8099");
        Single<HealthCheck> health = clientNotRunning.health();

        health
                .test()
                .assertNoErrors()
                .assertValue(it -> {

                    Assertions.assertThat(it).isNotNull();
                    Assertions.assertThat(it.getStatus()).isEqualTo(HealthCheck.StatusEnum.FAIL);
                    Assertions.assertThat(it.getMessage()).startsWith("Failed to connect to");

                    return true;
                });

        clientNotRunning.close();
    }

    @Test
    void logLevel() {

        // default NONE
        Assertions.assertThat(this.influxDBClient.getLogLevel()).isEqualTo(LogLevel.NONE);

        // set HEADERS
        InfluxDBClientReactive influxDBClient = this.influxDBClient.setLogLevel(LogLevel.HEADERS);
        Assertions.assertThat(influxDBClient).isEqualTo(this.influxDBClient);

        Assertions.assertThat(this.influxDBClient.getLogLevel()).isEqualTo(LogLevel.HEADERS);
    }

    @Test
    void gzip() {

        Assertions.assertThat(influxDBClient.isGzipEnabled()).isFalse();

        // Enable GZIP
        InfluxDBClientReactive influxDBClient = this.influxDBClient.enableGzip();
        Assertions.assertThat(influxDBClient).isEqualTo(this.influxDBClient);
        Assertions.assertThat(this.influxDBClient.isGzipEnabled()).isTrue();

        // Disable GZIP
        influxDBClient = this.influxDBClient.disableGzip();
        Assertions.assertThat(influxDBClient).isEqualTo(this.influxDBClient);
        Assertions.assertThat(this.influxDBClient.isGzipEnabled()).isFalse();
    }
}