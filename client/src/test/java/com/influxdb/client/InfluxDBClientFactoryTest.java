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

import java.util.Map;

import com.influxdb.LogLevel;
import com.influxdb.client.internal.AbstractInfluxDBClient;
import com.influxdb.client.write.PointSettings;
import com.influxdb.exceptions.InfluxException;
import com.influxdb.test.AbstractTest;

import okhttp3.OkHttpClient;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import retrofit2.Retrofit;

/**
 * @author Jakub Bednar (bednar@github) (05/09/2018 10:59)
 */
@RunWith(JUnitPlatform.class)
class InfluxDBClientFactoryTest extends AbstractTest {

    @Test
    void createInstance() {

        InfluxDBClient client = InfluxDBClientFactory.create("http://localhost:9999");

        Assertions.assertThat(client).isNotNull();
    }

    @Test
    void createInstanceUsername() {

        InfluxDBClient client = InfluxDBClientFactory.create("http://localhost:9999", "user", "secret".toCharArray());

        Assertions.assertThat(client).isNotNull();
    }

    @Test
    void createInstanceToken() {

        InfluxDBClient client = InfluxDBClientFactory.create("http://localhost:9999", "xyz".toCharArray());

        Assertions.assertThat(client).isNotNull();
    }

    @Test
    void loadFromConnectionString() throws NoSuchFieldException, IllegalAccessException {

        InfluxDBClient influxDBClient = InfluxDBClientFactory.create("http://localhost:9999?" +
                "readTimeout=1000&writeTimeout=3000&connectTimeout=2000&logLevel=HEADERS&token=my-token&bucket=my-bucket&org=my-org");

        InfluxDBClientOptions options = getDeclaredField(influxDBClient, "options", AbstractInfluxDBClient.class);

        Assertions.assertThat(options.getUrl()).isEqualTo("http://localhost:9999/");
        Assertions.assertThat(options.getOrg()).isEqualTo("my-org");
        Assertions.assertThat(options.getBucket()).isEqualTo("my-bucket");
        Assertions.assertThat(options.getToken()).isEqualTo("my-token".toCharArray());
        Assertions.assertThat(options.getLogLevel()).isEqualTo(LogLevel.HEADERS);
        Assertions.assertThat(influxDBClient.getLogLevel()).isEqualTo(LogLevel.HEADERS);

        Retrofit retrofit = getDeclaredField(influxDBClient, "retrofit", AbstractInfluxDBClient.class);
        OkHttpClient okHttpClient = (OkHttpClient) retrofit.callFactory();

        Assertions.assertThat(okHttpClient.readTimeoutMillis()).isEqualTo(1_000);
        Assertions.assertThat(okHttpClient.writeTimeoutMillis()).isEqualTo(3_000);
        Assertions.assertThat(okHttpClient.connectTimeoutMillis()).isEqualTo(2_000);
    }

    @Test
    void loadFromConnectionStringUnits() throws NoSuchFieldException, IllegalAccessException {

        InfluxDBClient influxDBClient = InfluxDBClientFactory.create("http://localhost:9999?" +
                "readTimeout=1ms&writeTimeout=3s&connectTimeout=2m&logLevel=HEADERS&token=my-token&bucket=my-bucket&org=my-org");

        InfluxDBClientOptions options = getDeclaredField(influxDBClient, "options", AbstractInfluxDBClient.class);

        Assertions.assertThat(options.getUrl()).isEqualTo("http://localhost:9999/");
        Assertions.assertThat(options.getOrg()).isEqualTo("my-org");
        Assertions.assertThat(options.getBucket()).isEqualTo("my-bucket");
        Assertions.assertThat(options.getToken()).isEqualTo("my-token".toCharArray());
        Assertions.assertThat(options.getLogLevel()).isEqualTo(LogLevel.HEADERS);
        Assertions.assertThat(influxDBClient.getLogLevel()).isEqualTo(LogLevel.HEADERS);

        Retrofit retrofit = getDeclaredField(influxDBClient, "retrofit", AbstractInfluxDBClient.class);
        OkHttpClient okHttpClient = (OkHttpClient) retrofit.callFactory();

        Assertions.assertThat(okHttpClient.readTimeoutMillis()).isEqualTo(1);
        Assertions.assertThat(okHttpClient.writeTimeoutMillis()).isEqualTo(3_000);
        Assertions.assertThat(okHttpClient.connectTimeoutMillis()).isEqualTo(120_000);
    }

    @Test
    void loadFromConnectionNotValidDuration() {

        Assertions.assertThatThrownBy(() -> InfluxDBClientFactory.create("http://localhost:9999?" +
                "readTimeout=x&writeTimeout=3s&connectTimeout=2m&logLevel=HEADERS&token=my-token&bucket=my-bucket&org=my-org"))
                .hasMessage("'x' is not a valid duration")
                .isInstanceOf(InfluxException.class);
    }

    @Test
    void loadFromConnectionUnknownUnit() {

        Assertions.assertThatThrownBy(() -> InfluxDBClientFactory.create("http://localhost:9999?" +
                "readTimeout=1y&writeTimeout=3s&connectTimeout=2m&logLevel=HEADERS&token=my-token&bucket=my-bucket&org=my-org"))
                .hasMessage("unknown unit for '1y'")
                .isInstanceOf(InfluxException.class);
    }

    @Test
    void loadFromProperties() throws NoSuchFieldException, IllegalAccessException {

        InfluxDBClient influxDBClient = InfluxDBClientFactory.create();

        InfluxDBClientOptions options = getDeclaredField(influxDBClient, "options", AbstractInfluxDBClient.class);

        Assertions.assertThat(options.getUrl()).isEqualTo("http://localhost:9999");
        Assertions.assertThat(options.getOrg()).isEqualTo("my-org");
        Assertions.assertThat(options.getBucket()).isEqualTo("my-bucket");
        Assertions.assertThat(options.getToken()).isEqualTo("my-token".toCharArray());
        Assertions.assertThat(options.getLogLevel()).isEqualTo(LogLevel.BODY);
        Assertions.assertThat(influxDBClient.getLogLevel()).isEqualTo(LogLevel.BODY);

        Retrofit retrofit = getDeclaredField(influxDBClient, "retrofit", AbstractInfluxDBClient.class);
        OkHttpClient okHttpClient = (OkHttpClient) retrofit.callFactory();

        Assertions.assertThat(okHttpClient.readTimeoutMillis()).isEqualTo(5_000);
        Assertions.assertThat(okHttpClient.writeTimeoutMillis()).isEqualTo(10_000);
        Assertions.assertThat(okHttpClient.connectTimeoutMillis()).isEqualTo(5_000);

        Map<String, String> defaultTags = getDeclaredField(options.getPointSettings(), "defaultTags", PointSettings.class);

        Assertions.assertThat(defaultTags).hasSize(4)
                .hasEntrySatisfying("id", value -> Assertions.assertThat(value).isEqualTo("132-987-655"))
                .hasEntrySatisfying("customer", value -> Assertions.assertThat(value).isEqualTo("California Miner"))
                .hasEntrySatisfying("version", value -> Assertions.assertThat(value).isEqualTo("${version}"))
                .hasEntrySatisfying("hostname", value -> Assertions.assertThat(value).isEqualTo("${env.hostname}"));
    }
}