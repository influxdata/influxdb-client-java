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
package com.influxdb.spring.health;

import com.influxdb.client.InfluxDBClient;

import org.junit.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.springframework.boot.actuate.autoconfigure.health.HealthIndicatorAutoConfiguration;
import org.springframework.boot.actuate.health.ApplicationHealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link InfluxDB2HealthIndicatorAutoConfiguration}.
 *
 * @author Jakub Bednar (bednar@github) (07/05/2019 14:59)
 */
@RunWith(JUnitPlatform.class)
class InfluxDB2HealthIndicatorAutoConfigurationTest {

    private ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(InfluxDBClientConfiguration.class).withConfiguration(
                    AutoConfigurations.of(InfluxDB2HealthIndicatorAutoConfiguration.class,
                            HealthIndicatorAutoConfiguration.class));

    @Test
    public void runShouldCreateIndicator() {
        this.contextRunner.run((context) -> assertThat(context)
                .hasSingleBean(InfluxDB2HealthIndicator.class)
                .doesNotHaveBean(ApplicationHealthIndicator.class));
    }

    @Test
    public void runWhenDisabledShouldNotCreateIndicator() {
        this.contextRunner.withPropertyValues("management.health.influxdb2.enabled:false")
                .run((context) -> assertThat(context)
                        .doesNotHaveBean(InfluxDB2HealthIndicator.class)
                        .hasSingleBean(ApplicationHealthIndicator.class));
    }

    @Configuration
    static class InfluxDBClientConfiguration {

        @Bean
        public InfluxDBClient influxDBClient() {
            return mock(InfluxDBClient.class);
        }

    }
}