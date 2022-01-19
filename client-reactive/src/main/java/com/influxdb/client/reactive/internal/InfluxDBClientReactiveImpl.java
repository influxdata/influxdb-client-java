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
package com.influxdb.client.reactive.internal;

import java.util.Collections;
import javax.annotation.Nonnull;

import com.influxdb.LogLevel;
import com.influxdb.client.InfluxDBClientOptions;
import com.influxdb.client.domain.HealthCheck;
import com.influxdb.client.internal.AbstractInfluxDBClient;
import com.influxdb.client.reactive.InfluxDBClientReactive;
import com.influxdb.client.reactive.QueryReactiveApi;
import com.influxdb.client.reactive.WriteOptionsReactive;
import com.influxdb.client.reactive.WriteReactiveApi;
import com.influxdb.client.service.QueryService;
import com.influxdb.client.service.WriteService;
import com.influxdb.utils.Arguments;

import io.reactivex.rxjava3.core.Single;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;

/**
 * @author Jakub Bednar (bednar@github) (20/11/2018 07:12)
 */
public class InfluxDBClientReactiveImpl extends AbstractInfluxDBClient
        implements InfluxDBClientReactive {

    public InfluxDBClientReactiveImpl(@Nonnull final InfluxDBClientOptions options) {
        super(options, "java", Collections.singletonList(RxJava3CallAdapterFactory.create()));
    }

    @Nonnull
    @Override
    public QueryReactiveApi getQueryReactiveApi() {
        return new QueryReactiveApiImpl(retrofit.create(QueryService.class), options);
    }

    @Nonnull
    @Override
    public WriteReactiveApi getWriteReactiveApi() {
        return getWriteReactiveApi(WriteOptionsReactive.DEFAULTS);
    }

    @Nonnull
    @Override
    public WriteReactiveApi getWriteReactiveApi(@Nonnull final WriteOptionsReactive writeOptions) {

        Arguments.checkNotNull(writeOptions, "WriteOptions");

        return new WriteReactiveApiImpl(writeOptions, retrofit.create(WriteService.class), options);
    }

    @Nonnull
    @Override
    public Single<HealthCheck> health() {

        return Single.fromCallable(() -> health(healthService.getHealth(null)));
    }

    @Nonnull
    @Override
    public LogLevel getLogLevel() {
        return getLogLevel(this.loggingInterceptor);
    }

    @Nonnull
    @Override
    public InfluxDBClientReactive setLogLevel(@Nonnull final LogLevel logLevel) {

        setLogLevel(this.loggingInterceptor, logLevel);

        return this;
    }

    @Nonnull
    @Override
    public InfluxDBClientReactive enableGzip() {

        this.gzipInterceptor.enableGzip();

        return this;
    }

    @Nonnull
    @Override
    public InfluxDBClientReactive disableGzip() {

        this.gzipInterceptor.disableGzip();

        return this;
    }

    @Override
    public boolean isGzipEnabled() {

        return this.gzipInterceptor.isEnabledGzip();
    }
}