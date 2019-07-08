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
package com.influxdb.client;

import okhttp3.OkHttpClient;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (05/09/2018 10:38)
 */
@RunWith(JUnitPlatform.class)
class InfluxDBClientOptionsTest {

    @Test
    void defaultValue() {

        InfluxDBClientOptions options = InfluxDBClientOptions.builder().url("http://localhost:9999")
                .authenticateToken("xyz".toCharArray())
                .build();

        Assertions.assertThat(options.getUrl()).isEqualTo("http://localhost:9999");
        Assertions.assertThat(options.getAuthScheme()).isEqualTo(InfluxDBClientOptions.AuthScheme.TOKEN);
        Assertions.assertThat(options.getOkHttpClient()).isNotNull();
    }

    @Test
    void okHttpBuilder() {

        OkHttpClient.Builder okHttpClient = new OkHttpClient.Builder();
        InfluxDBClientOptions options = InfluxDBClientOptions.builder().url("http://localhost:9999")
                .authenticateToken("xyz".toCharArray())
                .okHttpClient(okHttpClient).build();

        Assertions.assertThat(options.getOkHttpClient()).isEqualTo(okHttpClient);
    }

    @Test
    void urlRequired() {

        InfluxDBClientOptions.Builder builder = InfluxDBClientOptions.builder()
                .authenticateToken("xyz".toCharArray());

        Assertions.assertThatThrownBy(builder::build)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("The url to connect to InfluxDB has to be defined.");
    }

    @Test
    void authorizationNone() {

        InfluxDBClientOptions options = InfluxDBClientOptions.builder()
                .url("http://localhost:9999")
                .build();

        Assertions.assertThat(options.getAuthScheme()).isNull();
    }

    @Test
    void authorizationSession() {

        InfluxDBClientOptions options = InfluxDBClientOptions.builder()
                .url("http://localhost:9999")
                .authenticate("user", "secret".toCharArray())
                .build();

        Assertions.assertThat(options.getAuthScheme()).isEqualTo(InfluxDBClientOptions.AuthScheme.SESSION);
    }
}