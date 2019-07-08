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
package com.influxdb.spring.metrics;

import io.micrometer.core.instrument.Clock;
import io.micrometer.influx2.Influx2Config;
import io.micrometer.influx2.Influx2MeterRegistry;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Tests for {@link Influx2MetricsExportAutoConfiguration}.
 *
 * @author Jakub Bednar (bednar@github) (07/05/2019 12:49)
 */
@RunWith(JUnitPlatform.class)
class Influx2MetricsExportAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(
                    AutoConfigurations.of(Influx2MetricsExportAutoConfiguration.class));

    @Test
    public void backsOffWithoutAClock() {
        this.contextRunner.run((context) -> Assertions.assertThat(context)
                .doesNotHaveBean(Influx2MeterRegistry.class));
    }

    @Test
    public void autoConfiguresItsConfigAndMeterRegistry() {
        this.contextRunner.withUserConfiguration(BaseConfiguration.class)
                .run((context) -> Assertions.assertThat(context)
                        .hasSingleBean(Influx2MeterRegistry.class)
                        .hasSingleBean(Influx2Config.class));
    }

    @Test
    public void autoConfigurationCanBeDisabled() {
        this.contextRunner.withUserConfiguration(BaseConfiguration.class)
                .withPropertyValues("management.metrics.export.influx2.enabled=false")
                .run((context) -> Assertions.assertThat(context)
                        .doesNotHaveBean(Influx2MeterRegistry.class)
                        .doesNotHaveBean(Influx2Config.class));
    }

    @Test
    public void allowsCustomConfigToBeUsed() {
        this.contextRunner.withUserConfiguration(CustomConfigConfiguration.class)
                .run((context) -> Assertions.assertThat(context)
                        .hasSingleBean(Influx2MeterRegistry.class)
                        .hasSingleBean(Influx2Config.class).hasBean("customConfig"));
    }

    @Test
    public void allowsCustomRegistryToBeUsed() {
        this.contextRunner.withUserConfiguration(CustomRegistryConfiguration.class)
                .run((context) -> Assertions.assertThat(context)
                        .hasSingleBean(Influx2MeterRegistry.class)
                        .hasBean("customRegistry").hasSingleBean(Influx2Config.class));
    }

    @Test
    public void stopsMeterRegistryWhenContextIsClosed() {
        this.contextRunner.withUserConfiguration(BaseConfiguration.class)
                .run((context) -> {
                    Influx2MeterRegistry registry = context                              .getBean(Influx2MeterRegistry.class);
                    Assertions.assertThat(registry.isClosed()).isFalse();
                    context.close();
                    Assertions.assertThat(registry.isClosed()).isTrue();
                });
    }

    @Configuration
    static class BaseConfiguration {

        @Bean
        public Clock clock() {
            return Clock.SYSTEM;
        }

    }

    @Configuration
    @Import(BaseConfiguration.class)
    static class CustomConfigConfiguration {

        @Bean
        public Influx2Config customConfig() {
            return new Influx2Config() {

                @Override
                public String get(String k) {
                    return null;
                }

            };
        }
    }

    @Configuration
    @Import(BaseConfiguration.class)
    static class CustomRegistryConfiguration {

        @Bean
        public Influx2MeterRegistry customRegistry(Influx2Config config, Clock clock) {
            return new Influx2MeterRegistry(config, clock);
        }

    }

}