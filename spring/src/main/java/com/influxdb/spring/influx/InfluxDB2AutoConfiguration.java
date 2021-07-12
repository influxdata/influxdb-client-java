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

import java.util.Collections;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.InfluxDBClientOptions;

import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for InfluxDB 2.
 *
 * @author Jakub Bednar (bednar@github) (06/05/2019 13:09)
 */
@Configuration
@ConditionalOnClass(InfluxDBClient.class)
@EnableConfigurationProperties(InfluxDB2Properties.class)
public class InfluxDB2AutoConfiguration {

    private final InfluxDB2Properties properties;

    private final InfluxDB2OkHttpClientBuilderProvider builderProvider;

    public InfluxDB2AutoConfiguration(final InfluxDB2Properties properties,
                                      final ObjectProvider<InfluxDB2OkHttpClientBuilderProvider> builderProvider) {
        this.properties = properties;
        this.builderProvider = builderProvider.getIfAvailable();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty("spring.influx2.url")
    public InfluxDBClient influxDBClient() {

        OkHttpClient.Builder okHttpBuilder;
        if (builderProvider == null) {
            okHttpBuilder = new OkHttpClient.Builder()
                    .protocols(Collections.singletonList(Protocol.HTTP_1_1))
                    .readTimeout(properties.getReadTimeout())
                    .writeTimeout(properties.getWriteTimeout())
                    .connectTimeout(properties.getConnectTimeout());
        } else {
            okHttpBuilder = builderProvider.get();
        }

        InfluxDBClientOptions.Builder influxBuilder = InfluxDBClientOptions.builder()
                .url(properties.getUrl())
                .bucket(properties.getBucket())
                .org(properties.getOrg())
                .okHttpClient(okHttpBuilder);

        if (StringUtils.hasLength(properties.getToken())) {
            influxBuilder.authenticateToken(properties.getToken().toCharArray());
        } else if (StringUtils.hasLength(properties.getUsername()) && StringUtils.hasLength(properties.getPassword())) {
            influxBuilder.authenticate(properties.getUsername(), properties.getPassword().toCharArray());
        }

        return InfluxDBClientFactory.create(influxBuilder.build()).setLogLevel(properties.getLogLevel());
    }

}
