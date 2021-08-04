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
import javax.annotation.Nonnull;

import com.influxdb.client.InfluxDBClientOptions;

import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import org.springframework.util.StringUtils;

/**
 * @author Jakub Bednar (04/08/2021 11:41)
 */
abstract class AbstractInfluxDB2AutoConfiguration {
    protected final InfluxDB2Properties properties;
    protected final InfluxDB2OkHttpClientBuilderProvider builderProvider;

    protected AbstractInfluxDB2AutoConfiguration(final InfluxDB2Properties properties,
                                                 final InfluxDB2OkHttpClientBuilderProvider builderProvider) {
        this.properties = properties;
        this.builderProvider = builderProvider;
    }

    @Nonnull
    protected InfluxDBClientOptions.Builder makeBuilder() {
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
        return influxBuilder;
    }
}
