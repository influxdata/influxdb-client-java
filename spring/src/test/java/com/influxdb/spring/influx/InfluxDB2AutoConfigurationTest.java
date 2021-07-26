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
package com.influxdb.spring.influx;

import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.reactive.InfluxDBClientReactive;

import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.util.ReflectionTestUtils;
import retrofit2.Retrofit;

/**
 * Tests for {@link InfluxDB2AutoConfiguration}.
 *
 * @author Jakub Bednar (bednar@github) (07/05/2019 12:59)
 */
@RunWith(JUnitPlatform.class)
class InfluxDB2AutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withClassLoader(new FilteredClassLoader(InfluxDBClientReactive.class))
            .withConfiguration(AutoConfigurations.of(InfluxDB2AutoConfiguration.class));

    @Test
    public void influxDBClientRequiresUrl() {
        this.contextRunner.run((context) -> Assertions.assertThat(context.getBeansOfType(InfluxDBClient.class))
                .isEmpty());
    }

    @Test
    public void influxDBClientCanBeCustomized() {
        this.contextRunner
                .withPropertyValues("influx.url=http://localhost:8086/",
                        "influx.password:password", "influx.username:username")
                .run(((context) -> Assertions.assertThat(context.getBeansOfType(InfluxDBClient.class))
                        .hasSize(1)));
    }

    @Test
    public void influxDBClientCanBeCreatedWithoutCredentials() {
        this.contextRunner.withPropertyValues("influx.url=http://localhost:8086/")
                .run((context) -> {
                    Assertions.assertThat(context.getBeansOfType(InfluxDBClient.class)).hasSize(1);
                    int readTimeout = getReadTimeoutProperty(context);
                    Assertions.assertThat(readTimeout).isEqualTo(10_000);
                });
    }

    @Test
    public void influxDBClientWithOkHttpClientBuilderProvider() {
        this.contextRunner
                .withUserConfiguration(CustomOkHttpClientBuilderProviderConfig.class)
                .withPropertyValues("influx.url=http://localhost:8086/", "influx.token:token")
                .run((context) -> {
                    Assertions.assertThat(context.getBeansOfType(InfluxDBClient.class)).hasSize(1);
                    int readTimeout = getReadTimeoutProperty(context);
                    Assertions.assertThat(readTimeout).isEqualTo(40_000);
                });
    }

    @Test
    public void influxDBClientWithReadTimeout() {
        this.contextRunner.withPropertyValues("influx.url=http://localhost:8086/", "influx.readTimeout=13s")
                .run((context) -> {
                    Assertions.assertThat(context.getBeansOfType(InfluxDBClient.class)).hasSize(1);
                    int readTimeout = getReadTimeoutProperty(context);
                    Assertions.assertThat(readTimeout).isEqualTo(13_000);
                });
    }

    @Test
    public void influxDBClientReactive() {
        ApplicationContextRunner contextRunner = new ApplicationContextRunner()
                .withPropertyValues("influx.url=http://localhost:8086/")
                .withConfiguration(AutoConfigurations.of(InfluxDB2AutoConfiguration.class));

        contextRunner
                .run(((context) -> Assertions.assertThat(context.getBeansOfType(InfluxDBClientReactive.class))
                .hasSize(1)));

        contextRunner
                .run(((context) -> Assertions.assertThat(context.getBeansOfType(InfluxDBClient.class))
                .hasSize(0)));
    }

    @Test
    public void protocolVersion() {
        this.contextRunner.withPropertyValues("influx.url=http://localhost:8086/", "spring.influx2.token:token")
                .run((context) -> {
                    List<Protocol> protocols = getOkHttpClient(context).protocols();
                    Assertions.assertThat(protocols).hasSize(1);
                    Assertions.assertThat(protocols).contains(Protocol.HTTP_1_1);
                });
    }

    private int getReadTimeoutProperty(AssertableApplicationContext context) {
        OkHttpClient callFactory = getOkHttpClient(context);
        return callFactory.readTimeoutMillis();
    }

    @Nonnull
    private OkHttpClient getOkHttpClient(final AssertableApplicationContext context) {
        InfluxDBClient influxDB = context.getBean(InfluxDBClient.class);
        Retrofit retrofit = (Retrofit) ReflectionTestUtils.getField(influxDB, "retrofit");
        OkHttpClient callFactory = (OkHttpClient) retrofit.callFactory();
        return callFactory;
    }

    @Configuration
    static class CustomOkHttpClientBuilderProviderConfig {

        @Bean
        public InfluxDB2OkHttpClientBuilderProvider influxDbOkHttpClientBuilderProvider() {
            return () -> new OkHttpClient.Builder().readTimeout(40, TimeUnit.SECONDS);
        }

    }
}