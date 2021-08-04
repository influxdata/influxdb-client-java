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

import com.influxdb.client.InfluxDBClientOptions;
import com.influxdb.client.reactive.InfluxDBClientReactive;
import com.influxdb.client.reactive.InfluxDBClientReactiveFactory;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for InfluxDB 2.
 *
 * @author Jakub Bednar (bednar@github) (06/05/2019 13:09)
 */
@Configuration
@ConditionalOnClass(name = "com.influxdb.client.reactive.InfluxDBClientReactive")
@EnableConfigurationProperties(InfluxDB2Properties.class)
public class InfluxDB2AutoConfigurationReactive extends AbstractInfluxDB2AutoConfiguration {

    public InfluxDB2AutoConfigurationReactive(final InfluxDB2Properties properties,
                                              final ObjectProvider<InfluxDB2OkHttpClientBuilderProvider>
                                                      builderProvider) {
        super(properties, builderProvider.getIfAvailable());
    }

    @Bean
    @ConditionalOnProperty("influx.url")
    @ConditionalOnMissingBean(InfluxDBClientReactive.class)
    public InfluxDBClientReactive influxDBClientReactive() {
        InfluxDBClientOptions.Builder influxBuilder = makeBuilder();

        return InfluxDBClientReactiveFactory.create(influxBuilder.build()).setLogLevel(properties.getLogLevel());
    }
}
