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
package com.influxdb.client.flux.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.influxdb.Arguments;
import com.influxdb.Cancellable;
import com.influxdb.LogLevel;
import com.influxdb.client.flux.FluxClient;
import com.influxdb.client.flux.FluxConnectionOptions;
import com.influxdb.exceptions.InfluxException;
import com.influxdb.internal.AbstractQueryApi;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.influxdb.query.internal.FluxCsvParser.FluxResponseConsumer;
import com.influxdb.query.internal.FluxCsvParser.FluxResponseConsumerTable;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;

/**
 * @author Jakub Bednar (bednar@github) (03/10/2018 14:20)
 */
public class FluxApiImpl extends AbstractQueryApi implements FluxClient {

    private static final Logger LOG = Logger.getLogger(FluxApiImpl.class.getName());

    private final FluxService fluxService;

    private final HttpLoggingInterceptor loggingInterceptor;
    private final OkHttpClient okHttpClient;

    public FluxApiImpl(@Nonnull final FluxConnectionOptions options) {

        Arguments.checkNotNull(options, "options");

        this.loggingInterceptor = new HttpLoggingInterceptor();

        String logLevelParam = options.getParameters().get("logLevel");

        if (logLevelParam == null) {
            this.loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE);
        } else {
            this.loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.valueOf(logLevelParam));
        }

        this.okHttpClient = options.getOkHttpClient()
                .addInterceptor(this.loggingInterceptor)
                .build();

        Retrofit.Builder serviceBuilder = new Retrofit.Builder()
                .baseUrl(options.getUrl())
                .client(this.okHttpClient);

        this.fluxService = serviceBuilder
                .build()
                .create(FluxService.class);
    }

    @Nonnull
    @Override
    public List<FluxTable> query(@Nonnull final String query) {

        Arguments.checkNonEmpty(query, "query");

        FluxResponseConsumerTable consumer = fluxCsvParser.new FluxResponseConsumerTable();

        query(query, DEFAULT_DIALECT.toString(), consumer, ERROR_CONSUMER, EMPTY_ACTION, false);

        return consumer.getTables();
    }

    @Nonnull
    @Override
    public <M> List<M> query(@Nonnull final String query, @Nonnull final Class<M> measurementType) {

        Arguments.checkNonEmpty(query, "query");
        Arguments.checkNotNull(measurementType, "measurementType");

        List<M> measurements = new ArrayList<>();

        FluxResponseConsumer consumer = new FluxResponseConsumer() {

            @Override
            public void accept(final int index,
                               @Nonnull final Cancellable cancellable,
                               @Nonnull final FluxTable table) {

            }

            @Override
            public void accept(final int index,
                               @Nonnull final Cancellable cancellable,
                               @Nonnull final FluxRecord record) {

                measurements.add(resultMapper.toPOJO(record, measurementType));
            }
        };

        query(query, DEFAULT_DIALECT.toString(), consumer, ERROR_CONSUMER, EMPTY_ACTION, false);

        return measurements;
    }

    @Override
    public void query(@Nonnull final String query, @Nonnull final BiConsumer<Cancellable, FluxRecord> onNext) {

        Arguments.checkNonEmpty(query, "query");
        Arguments.checkNotNull(onNext, "onNext");

        query(query, onNext, ERROR_CONSUMER);
    }

    @Override
    public <M> void query(@Nonnull final String query,
                          @Nonnull final Class<M> measurementType,
                          @Nonnull final BiConsumer<Cancellable, M> onNext) {

        Arguments.checkNonEmpty(query, "query");
        Arguments.checkNotNull(onNext, "onNext");
        Arguments.checkNotNull(measurementType, "measurementType");

        query(query, measurementType, onNext, ERROR_CONSUMER);
    }

    @Override
    public void query(@Nonnull final String query,
                      @Nonnull final BiConsumer<Cancellable, FluxRecord> onNext,
                      @Nonnull final Consumer<? super Throwable> onError) {

        Arguments.checkNonEmpty(query, "query");
        Arguments.checkNotNull(onNext, "onNext");
        Arguments.checkNotNull(onError, "onError");

        query(query, onNext, onError, EMPTY_ACTION);
    }

    @Override
    public <M> void query(@Nonnull final String query,
                          @Nonnull final Class<M> measurementType,
                          @Nonnull final BiConsumer<Cancellable, M> onNext,
                          @Nonnull final Consumer<? super Throwable> onError) {

        Arguments.checkNonEmpty(query, "query");
        Arguments.checkNotNull(onNext, "onNext");
        Arguments.checkNotNull(onError, "onError");
        Arguments.checkNotNull(measurementType, "measurementType");

        query(query, measurementType, onNext, onError, EMPTY_ACTION);
    }

    @Override
    public void query(@Nonnull final String query,
                      @Nonnull final BiConsumer<Cancellable, FluxRecord> onNext,
                      @Nonnull final Consumer<? super Throwable> onError,
                      @Nonnull final Runnable onComplete) {

        Arguments.checkNonEmpty(query, "query");
        Arguments.checkNotNull(onNext, "onNext");
        Arguments.checkNotNull(onError, "onError");
        Arguments.checkNotNull(onComplete, "onComplete");

        FluxResponseConsumer consumer = new FluxResponseConsumer() {

            @Override
            public void accept(final int index,
                               @Nonnull final Cancellable cancellable,
                               @Nonnull final FluxTable table) {
            }

            @Override
            public void accept(final int index,
                               @Nonnull final Cancellable cancellable,
                               @Nonnull final FluxRecord record) {
                onNext.accept(cancellable, record);
            }
        };

        query(query, DEFAULT_DIALECT.toString(), consumer, onError, onComplete, true);
    }


    @Override
    public <M> void query(@Nonnull final String query,
                          @Nonnull final Class<M> measurementType,
                          @Nonnull final BiConsumer<Cancellable, M> onNext,
                          @Nonnull final Consumer<? super Throwable> onError,
                          @Nonnull final Runnable onComplete) {

        Arguments.checkNonEmpty(query, "query");
        Arguments.checkNotNull(onNext, "onNext");
        Arguments.checkNotNull(onError, "onError");
        Arguments.checkNotNull(onComplete, "onComplete");
        Arguments.checkNotNull(measurementType, "measurementType");


        FluxResponseConsumer consumer = new FluxResponseConsumer() {

            @Override
            public void accept(final int index,
                               @Nonnull final Cancellable cancellable,
                               @Nonnull final FluxTable table) {

            }

            @Override
            public void accept(final int index,
                               @Nonnull final Cancellable cancellable,
                               @Nonnull final FluxRecord record) {

                onNext.accept(cancellable, resultMapper.toPOJO(record, measurementType));

            }
        };

        query(query, DEFAULT_DIALECT.toString(), consumer, onError, onComplete, true);

    }

    @Nonnull
    @Override
    public String queryRaw(@Nonnull final String query) {

        Arguments.checkNonEmpty(query, "query");

        return queryRaw(query, (String) null);
    }

    @Nonnull
    @Override
    public String queryRaw(@Nonnull final String query, @Nullable final String dialect) {

        Arguments.checkNonEmpty(query, "query");

        List<String> rows = new ArrayList<>();

        BiConsumer<Cancellable, String> consumer = (cancellable, row) -> rows.add(row);

        queryRaw(query, dialect, consumer, ERROR_CONSUMER, EMPTY_ACTION, false);

        return String.join("\n", rows);
    }

    @Override
    public void queryRaw(@Nonnull final String query,
                         @Nonnull final BiConsumer<Cancellable, String> onResponse) {

        Arguments.checkNonEmpty(query, "query");
        Arguments.checkNotNull(onResponse, "onNext");

        queryRaw(query, null, onResponse);
    }

    @Override
    public void queryRaw(@Nonnull final String query,
                         @Nullable final String dialect,
                         @Nonnull final BiConsumer<Cancellable, String> onNext) {

        Arguments.checkNonEmpty(query, "query");
        Arguments.checkNotNull(onNext, "onNext");

        queryRaw(query, dialect, onNext, ERROR_CONSUMER);
    }

    @Override
    public void queryRaw(@Nonnull final String query,
                         @Nonnull final BiConsumer<Cancellable, String> onResponse,
                         @Nonnull final Consumer<? super Throwable> onError) {

        Arguments.checkNonEmpty(query, "query");
        Arguments.checkNotNull(onResponse, "onNext");
        Arguments.checkNotNull(onError, "onError");

        queryRaw(query, onResponse, onError, EMPTY_ACTION);
    }

    @Override
    public void queryRaw(@Nonnull final String query,
                         @Nullable final String dialect,
                         @Nonnull final BiConsumer<Cancellable, String> onResponse,
                         @Nonnull final Consumer<? super Throwable> onError) {

        Arguments.checkNonEmpty(query, "query");
        Arguments.checkNotNull(onResponse, "onNext");
        Arguments.checkNotNull(onError, "onError");

        queryRaw(query, dialect, onResponse, onError, EMPTY_ACTION);
    }

    @Override
    public void queryRaw(@Nonnull final String query,
                         @Nonnull final BiConsumer<Cancellable, String> onResponse,
                         @Nonnull final Consumer<? super Throwable> onError,
                         @Nonnull final Runnable onComplete) {

        Arguments.checkNonEmpty(query, "query");
        Arguments.checkNotNull(onResponse, "onNext");
        Arguments.checkNotNull(onError, "onError");
        Arguments.checkNotNull(onComplete, "onComplete");

        queryRaw(query, null, onResponse, onError, onComplete);
    }

    @Override
    public void queryRaw(@Nonnull final String query,
                         @Nullable final String dialect,
                         @Nonnull final BiConsumer<Cancellable, String> onResponse,
                         @Nonnull final Consumer<? super Throwable> onError,
                         @Nonnull final Runnable onComplete) {

        Arguments.checkNonEmpty(query, "query");
        Arguments.checkNotNull(onResponse, "onNext");
        Arguments.checkNotNull(onError, "onError");
        Arguments.checkNotNull(onComplete, "onComplete");

        queryRaw(query, dialect, onResponse, onError, onComplete, true);
    }

    @Nonnull
    @Override
    public Boolean ping() {

        Call<ResponseBody> ping = fluxService.ping();

        try {
            return ping.execute().isSuccessful();
        } catch (IOException e) {

            LOG.log(Level.WARNING, "Ping request wasn't successful", e);
            return false;
        }
    }

    @Override
    @Nonnull
    public String version() {

        Call<ResponseBody> ping = fluxService.ping();

        try {
            String version = ping.execute().headers().get("X-Influxdb-Version");
            if (version != null) {
                return version;
            }

            return "unknown";
        } catch (IOException e) {
            throw new InfluxException(e);
        }
    }

    @Nonnull
    @Override
    public LogLevel getLogLevel() {
        return getLogLevel(this.loggingInterceptor);
    }

    @Nonnull
    @Override
    public FluxClient setLogLevel(@Nonnull final LogLevel logLevel) {

        Arguments.checkNotNull(logLevel, "LogLevel");

        setLogLevel(this.loggingInterceptor, logLevel);

        return this;
    }

    /**
     * Closes the client, initiates shutdown, no new running calls are accepted during shutdown.
     */
    public void close() {
        okHttpClient.connectionPool().evictAll();
        okHttpClient.dispatcher().executorService().shutdown();
    }

    private void query(@Nonnull final String query,
                       @Nonnull final String dialect,
                       @Nonnull final FluxResponseConsumer responseConsumer,
                       @Nonnull final Consumer<? super Throwable> onError,
                       @Nonnull final Runnable onComplete,
                       @Nonnull final Boolean asynchronously) {

        Call<ResponseBody> queryCall = fluxService.query(createBody(dialect, query));

        query(queryCall, responseConsumer, onError, onComplete, asynchronously);
    }

    private void queryRaw(@Nonnull final String query,
                          @Nullable final String dialect,
                          @Nonnull final BiConsumer<Cancellable, String> onResponse,
                          @Nonnull final Consumer<? super Throwable> onError,
                          @Nonnull final Runnable onComplete,
                          @Nonnull final Boolean asynchronously) {

        Call<ResponseBody> queryCall = fluxService.query(createBody(dialect, query));

        queryRaw(queryCall, onResponse, onError, onComplete, asynchronously);
    }
}