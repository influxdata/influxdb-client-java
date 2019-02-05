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
package org.influxdata.reactive.client.internal;

import javax.annotation.Nonnull;

import org.influxdata.client.Arguments;
import org.influxdata.client.LogLevel;
import org.influxdata.java.client.InfluxDBClientOptions;
import org.influxdata.java.client.WriteOptions;
import org.influxdata.java.client.domain.Health;
import org.influxdata.java.client.internal.AbstractInfluxDBClient;
import org.influxdata.reactive.client.InfluxDBClientReactive;
import org.influxdata.reactive.client.QueryReactiveApi;
import org.influxdata.reactive.client.WriteReactiveApi;

import io.reactivex.Single;

/**
 * @author Jakub Bednar (bednar@github) (20/11/2018 07:12)
 */
public class InfluxDBClientReactiveImpl extends AbstractInfluxDBClient<InfluxDBReactiveService>
        implements InfluxDBClientReactive {

    public InfluxDBClientReactiveImpl(@Nonnull final InfluxDBClientOptions options) {
        super(options, InfluxDBReactiveService.class);
    }

    @Nonnull
    @Override
    public QueryReactiveApi getQueryReactiveApi() {
        return new QueryReactiveApiImpl(influxDBService);
    }


    @Nonnull
    @Override
    public WriteReactiveApi getWriteReactiveApi() {
        return getWriteReactiveApi(WriteOptions.DEFAULTS);
    }

    @Nonnull
    @Override
    public WriteReactiveApi getWriteReactiveApi(@Nonnull final WriteOptions writeOptions) {

        Arguments.checkNotNull(writeOptions, "WriteOptions");

        return new WriteReactiveApiImpl(writeOptions, influxDBService);
    }

    @Nonnull
    @Override
    public Single<Health> health() {

        return Single.fromCallable(() -> health(influxDBService.health()));
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