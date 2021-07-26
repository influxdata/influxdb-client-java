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

import java.util.Map;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.spring.influx.InfluxDB2AutoConfiguration;

import org.springframework.boot.actuate.autoconfigure.health.CompositeHealthContributorConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.HealthContributor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for {@link InfluxDB2HealthIndicator}.
 *
 * @author Jakub Bednar
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(InfluxDBClient.class)
@ConditionalOnBean(InfluxDBClient.class)
@ConditionalOnEnabledHealthIndicator("influx")
@AutoConfigureAfter(InfluxDB2AutoConfiguration.class)
public class InfluxDB2HealthIndicatorAutoConfiguration
        extends CompositeHealthContributorConfiguration<InfluxDB2HealthIndicator, InfluxDBClient> {

    @Bean
    @ConditionalOnMissingBean(name = { "influxDB2HealthIndicator", "influxDB2HealthContributor" })
    public HealthContributor influxDbHealthContributor(final Map<String, InfluxDBClient> influxDBClients) {
        return createContributor(influxDBClients);
    }

}