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
package com.influxdb.client.internal;

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
import com.influxdb.client.InfluxDBClientOptions;
import com.influxdb.client.QueryApi;
import com.influxdb.client.domain.Dialect;
import com.influxdb.client.domain.Query;
import com.influxdb.client.service.QueryService;
import com.influxdb.internal.AbstractQueryApi;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.influxdb.query.internal.FluxCsvParser;

import okhttp3.ResponseBody;
import retrofit2.Call;

/**
 * @author Jakub Bednar (bednar@github) (17/10/2018 10:50)
 */
final class QueryApiImpl extends AbstractQueryApi implements QueryApi {

    private static final Logger LOG = Logger.getLogger(QueryApiImpl.class.getName());

    private final QueryService service;
    private final InfluxDBClientOptions options;

    QueryApiImpl(@Nonnull final QueryService service, @Nonnull final InfluxDBClientOptions options) {

        Arguments.checkNotNull(service, "service");
        Arguments.checkNotNull(options, "options");

        this.service = service;
        this.options = options;
    }

    @Nonnull
    @Override
    public List<FluxTable> query(@Nonnull final String query) {

        Arguments.checkNotNull(options.getOrg(), "InfluxDBClientOptions.getOrg");

        return query(query, options.getOrg());
    }

    @Nonnull
    @Override
    public List<FluxTable> query(@Nonnull final String query, @Nonnull final String org) {

        Arguments.checkNonEmpty(query, "query");
        Arguments.checkNonEmpty(org, "org");

        return query(new Query().query(query).dialect(AbstractInfluxDBClient.DEFAULT_DIALECT), org);
    }

    @Nonnull
    @Override
    public List<FluxTable> query(@Nonnull final Query query) {

        Arguments.checkNotNull(options.getOrg(), "InfluxDBClientOptions.getOrg");

        return query(query, options.getOrg());
    }

    @Nonnull
    @Override
    public List<FluxTable> query(@Nonnull final Query query,
                                 @Nonnull final String org) {

        Arguments.checkNotNull(query, "query");
        Arguments.checkNonEmpty(org, "org");

        FluxCsvParser.FluxResponseConsumerTable consumer = fluxCsvParser.new FluxResponseConsumerTable();

        query(query, org, consumer, ERROR_CONSUMER, EMPTY_ACTION, false);

        return consumer.getTables();
    }

    @Nonnull
    @Override
    public <M> List<M> query(@Nonnull final String query, @Nonnull final Class<M> measurementType) {

        Arguments.checkNotNull(options.getOrg(), "InfluxDBClientOptions.getOrg");

        return query(query, options.getOrg(), measurementType);
    }

    @Nonnull
    @Override
    public <M> List<M> query(@Nonnull final String query,
                             @Nonnull final String org,
                             @Nonnull final Class<M> measurementType) {

        Arguments.checkNonEmpty(query, "query");

        Query dialect = new Query().query(query).dialect(AbstractInfluxDBClient.DEFAULT_DIALECT);

        return query(dialect, org, measurementType);
    }

    @Nonnull
    @Override
    public <M> List<M> query(@Nonnull final Query query, @Nonnull final Class<M> measurementType) {

        Arguments.checkNotNull(options.getOrg(), "InfluxDBClientOptions.getOrg");

        return query(query, options.getOrg(), measurementType);
    }

    @Nonnull
    @Override
    public <M> List<M> query(@Nonnull final Query query,
                             @Nonnull final String org,
                             @Nonnull final Class<M> measurementType) {

        Arguments.checkNotNull(query, "query");

        List<M> measurements = new ArrayList<>();

        FluxCsvParser.FluxResponseConsumer consumer = new FluxCsvParser.FluxResponseConsumer() {

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

        query(query, org, consumer, ERROR_CONSUMER, EMPTY_ACTION, false);

        return measurements;
    }

    @Override
    public void query(@Nonnull final String query, @Nonnull final BiConsumer<Cancellable, FluxRecord> onNext) {

        Arguments.checkNotNull(options.getOrg(), "InfluxDBClientOptions.getOrg");

        query(query, options.getOrg(), onNext);
    }

    @Override
    public void query(@Nonnull final String query,
                      @Nonnull final String org,
                      @Nonnull final BiConsumer<Cancellable, FluxRecord> onNext) {

        Arguments.checkNonEmpty(query, "query");
        Arguments.checkNotNull(onNext, "onNext");

        query(query, org, onNext, ERROR_CONSUMER);
    }

    @Override
    public void query(@Nonnull final Query query, @Nonnull final BiConsumer<Cancellable, FluxRecord> onNext) {

        Arguments.checkNotNull(options.getOrg(), "InfluxDBClientOptions.getOrg");

        query(query, options.getOrg(), onNext);
    }

    @Override
    public void query(@Nonnull final Query query,
                      @Nonnull final String org,
                      @Nonnull final BiConsumer<Cancellable, FluxRecord> onNext) {

        Arguments.checkNotNull(query, "query");
        Arguments.checkNotNull(onNext, "onNext");

        query(query, org, onNext, ERROR_CONSUMER);
    }

    @Override
    public <M> void query(@Nonnull final String query,
                          @Nonnull final Class<M> measurementType,
                          @Nonnull final BiConsumer<Cancellable, M> onNext) {

        Arguments.checkNotNull(options.getOrg(), "InfluxDBClientOptions.getOrg");

        query(query, options.getOrg(), measurementType, onNext);
    }

    @Override
    public <M> void query(@Nonnull final String query,
                          @Nonnull final String org,
                          @Nonnull final Class<M> measurementType,
                          @Nonnull final BiConsumer<Cancellable, M> onNext) {

        Arguments.checkNonEmpty(query, "query");
        Arguments.checkNonEmpty(org, "org");
        Arguments.checkNotNull(onNext, "onNext");
        Arguments.checkNotNull(measurementType, "measurementType");

        query(query, org, measurementType, onNext, ERROR_CONSUMER);
    }

    @Override
    public <M> void query(@Nonnull final Query query,
                          @Nonnull final Class<M> measurementType,
                          @Nonnull final BiConsumer<Cancellable, M> onNext) {

        Arguments.checkNotNull(options.getOrg(), "InfluxDBClientOptions.getOrg");

        query(query, options.getOrg(), measurementType, onNext);
    }

    @Override
    public <M> void query(@Nonnull final Query query,
                          @Nonnull final String org,
                          @Nonnull final Class<M> measurementType,
                          @Nonnull final BiConsumer<Cancellable, M> onNext) {

        Arguments.checkNotNull(query, "query");
        Arguments.checkNonEmpty(org, "org");
        Arguments.checkNotNull(onNext, "onNext");
        Arguments.checkNotNull(measurementType, "measurementType");

        query(query, org, measurementType, onNext, ERROR_CONSUMER);
    }

    @Override
    public void query(@Nonnull final String query,
                      @Nonnull final BiConsumer<Cancellable, FluxRecord> onNext,
                      @Nonnull final Consumer<? super Throwable> onError) {

        Arguments.checkNotNull(options.getOrg(), "InfluxDBClientOptions.getOrg");

        query(query, options.getOrg(), onNext, onError);
    }

    @Override
    public void query(@Nonnull final String query,
                      @Nonnull final String org,
                      @Nonnull final BiConsumer<Cancellable, FluxRecord> onNext,
                      @Nonnull final Consumer<? super Throwable> onError) {

        Arguments.checkNonEmpty(query, "query");
        Arguments.checkNonEmpty(org, "org");
        Arguments.checkNotNull(onNext, "onNext");
        Arguments.checkNotNull(onError, "onError");

        query(query, org, onNext, onError, EMPTY_ACTION);
    }

    @Override
    public void query(@Nonnull final Query query,
                      @Nonnull final BiConsumer<Cancellable, FluxRecord> onNext,
                      @Nonnull final Consumer<? super Throwable> onError) {

        Arguments.checkNotNull(options.getOrg(), "InfluxDBClientOptions.getOrg");

        query(query, options.getOrg(), onNext, onError);
    }

    @Override
    public void query(@Nonnull final Query query,
                      @Nonnull final String org,
                      @Nonnull final BiConsumer<Cancellable, FluxRecord> onNext,
                      @Nonnull final Consumer<? super Throwable> onError) {

        Arguments.checkNotNull(query, "query");
        Arguments.checkNonEmpty(org, "org");
        Arguments.checkNotNull(onNext, "onNext");
        Arguments.checkNotNull(onError, "onError");

        query(query, org, onNext, onError, EMPTY_ACTION);
    }

    @Override
    public <M> void query(@Nonnull final String query,
                          @Nonnull final Class<M> measurementType,
                          @Nonnull final BiConsumer<Cancellable, M> onNext,
                          @Nonnull final Consumer<? super Throwable> onError) {

        Arguments.checkNotNull(options.getOrg(), "InfluxDBClientOptions.getOrg");

        query(query, options.getOrg(), measurementType, onNext, onError);
    }

    @Override
    public <M> void query(@Nonnull final String query,
                          @Nonnull final String org,
                          @Nonnull final Class<M> measurementType,
                          @Nonnull final BiConsumer<Cancellable, M> onNext,
                          @Nonnull final Consumer<? super Throwable> onError) {

        Arguments.checkNonEmpty(query, "query");
        Arguments.checkNonEmpty(org, "org");
        Arguments.checkNotNull(onNext, "onNext");
        Arguments.checkNotNull(onError, "onError");
        Arguments.checkNotNull(measurementType, "measurementType");

        query(query, org, measurementType, onNext, onError, EMPTY_ACTION);
    }

    @Override
    public <M> void query(@Nonnull final Query query,
                          @Nonnull final Class<M> measurementType,
                          @Nonnull final BiConsumer<Cancellable, M> onNext,
                          @Nonnull final Consumer<? super Throwable> onError) {

        Arguments.checkNotNull(options.getOrg(), "InfluxDBClientOptions.getOrg");

        query(query, options.getOrg(), measurementType, onNext, onError);
    }

    @Override
    public <M> void query(@Nonnull final Query query,
                          @Nonnull final String org,
                          @Nonnull final Class<M> measurementType,
                          @Nonnull final BiConsumer<Cancellable, M> onNext,
                          @Nonnull final Consumer<? super Throwable> onError) {

        Arguments.checkNotNull(query, "query");
        Arguments.checkNonEmpty(org, "org");
        Arguments.checkNotNull(onNext, "onNext");
        Arguments.checkNotNull(onError, "onError");
        Arguments.checkNotNull(measurementType, "measurementType");

        query(query, org, measurementType, onNext, onError, EMPTY_ACTION);
    }

    @Override
    public void query(@Nonnull final String query,
                      @Nonnull final BiConsumer<Cancellable, FluxRecord> onNext,
                      @Nonnull final Consumer<? super Throwable> onError,
                      @Nonnull final Runnable onComplete) {

        Arguments.checkNotNull(options.getOrg(), "InfluxDBClientOptions.getOrg");

        query(query, options.getOrg(), onNext, onError, onComplete);
    }

    @Override
    public void query(@Nonnull final String query,
                      @Nonnull final String org,
                      @Nonnull final BiConsumer<Cancellable, FluxRecord> onNext,
                      @Nonnull final Consumer<? super Throwable> onError,
                      @Nonnull final Runnable onComplete) {

        Arguments.checkNonEmpty(query, "query");
        Arguments.checkNonEmpty(org, "org");
        Arguments.checkNotNull(onNext, "onNext");
        Arguments.checkNotNull(onError, "onError");
        Arguments.checkNotNull(onComplete, "onComplete");

        Query queryObj = new Query().query(query).dialect(AbstractInfluxDBClient.DEFAULT_DIALECT);

        query(queryObj, org, onNext, onError, onComplete);
    }

    @Override
    public void query(@Nonnull final Query query,
                      @Nonnull final BiConsumer<Cancellable, FluxRecord> onNext,
                      @Nonnull final Consumer<? super Throwable> onError,
                      @Nonnull final Runnable onComplete) {

        Arguments.checkNotNull(options.getOrg(), "InfluxDBClientOptions.getOrg");

        query(query, options.getOrg(), onNext, onError, onComplete);
    }

    @Override
    public void query(@Nonnull final Query query,
                      @Nonnull final String org,
                      @Nonnull final BiConsumer<Cancellable, FluxRecord> onNext,
                      @Nonnull final Consumer<? super Throwable> onError,
                      @Nonnull final Runnable onComplete) {

        Arguments.checkNotNull(query, "query");
        Arguments.checkNonEmpty(org, "org");
        Arguments.checkNotNull(onNext, "onNext");
        Arguments.checkNotNull(onError, "onError");
        Arguments.checkNotNull(onComplete, "onComplete");

        FluxCsvParser.FluxResponseConsumer consumer = new FluxCsvParser.FluxResponseConsumer() {

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

        query(query, org, consumer, onError, onComplete, true);
    }

    @Override
    public <M> void query(@Nonnull final String query,
                          @Nonnull final Class<M> measurementType,
                          @Nonnull final BiConsumer<Cancellable, M> onNext,
                          @Nonnull final Consumer<? super Throwable> onError,
                          @Nonnull final Runnable onComplete) {

        Arguments.checkNotNull(options.getOrg(), "InfluxDBClientOptions.getOrg");

        query(query, options.getOrg(), measurementType, onNext, onError, onComplete);
    }

    @Override
    public <M> void query(@Nonnull final String query,
                          @Nonnull final String org,
                          @Nonnull final Class<M> measurementType,
                          @Nonnull final BiConsumer<Cancellable, M> onNext,
                          @Nonnull final Consumer<? super Throwable> onError,
                          @Nonnull final Runnable onComplete) {

        Arguments.checkNonEmpty(query, "query");
        Arguments.checkNonEmpty(org, "org");
        Arguments.checkNotNull(onNext, "onNext");
        Arguments.checkNotNull(onError, "onError");
        Arguments.checkNotNull(onComplete, "onComplete");
        Arguments.checkNotNull(measurementType, "measurementType");

        Query queryObj = new Query().query(query).dialect(AbstractInfluxDBClient.DEFAULT_DIALECT);

        query(queryObj, org, measurementType,  onNext, onError, onComplete);
    }

    @Override
    public <M> void query(@Nonnull final Query query,
                          @Nonnull final Class<M> measurementType,
                          @Nonnull final BiConsumer<Cancellable, M> onNext,
                          @Nonnull final Consumer<? super Throwable> onError,
                          @Nonnull final Runnable onComplete) {

        Arguments.checkNotNull(options.getOrg(), "InfluxDBClientOptions.getOrg");

        query(query, options.getOrg(), measurementType, onNext, onError, onComplete);
    }

    @Override
    public <M> void query(@Nonnull final Query query,
                          @Nonnull final String org,
                          @Nonnull final Class<M> measurementType,
                          @Nonnull final BiConsumer<Cancellable, M> onNext,
                          @Nonnull final Consumer<? super Throwable> onError,
                          @Nonnull final Runnable onComplete) {

        Arguments.checkNotNull(query, "query");
        Arguments.checkNonEmpty(org, "org");
        Arguments.checkNotNull(onNext, "onNext");
        Arguments.checkNotNull(onError, "onError");
        Arguments.checkNotNull(onComplete, "onComplete");
        Arguments.checkNotNull(measurementType, "measurementType");

        FluxCsvParser.FluxResponseConsumer consumer = new FluxCsvParser.FluxResponseConsumer() {

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

        query(query, org, consumer, onError, onComplete, true);
    }

    @Nonnull
    @Override
    public String queryRaw(@Nonnull final String query) {

        Arguments.checkNotNull(options.getOrg(), "InfluxDBClientOptions.getOrg");

        return queryRaw(query, options.getOrg());
    }

    @Nonnull
    @Override
    public String queryRaw(@Nonnull final String query, @Nonnull final String org) {

        Arguments.checkNonEmpty(query, "query");
        Arguments.checkNonEmpty(org, "org");

        return queryRaw(new Query().query(query), org);
    }

    @Nonnull
    @Override
    public String queryRaw(@Nonnull final String query, @Nullable final Dialect dialect) {

        Arguments.checkNotNull(options.getOrg(), "InfluxDBClientOptions.getOrg");

        return queryRaw(query, dialect, options.getOrg());
    }

    @Nonnull
    @Override
    public String queryRaw(@Nonnull final String query,
                           @Nullable final Dialect dialect,
                           @Nonnull final String org) {

        Arguments.checkNonEmpty(query, "query");
        Arguments.checkNonEmpty(org, "org");

        return queryRaw(new Query().query(query).dialect(dialect), org);
    }

    @Nonnull
    @Override
    public String queryRaw(@Nonnull final Query query) {

        Arguments.checkNotNull(options.getOrg(), "InfluxDBClientOptions.getOrg");

        return queryRaw(query, options.getOrg());
    }

    @Nonnull
    @Override
    public String queryRaw(@Nonnull final Query query, @Nonnull final String org) {

        Arguments.checkNotNull(query, "query");
        Arguments.checkNonEmpty(org, "org");

        List<String> rows = new ArrayList<>();

        BiConsumer<Cancellable, String> consumer = (cancellable, row) -> rows.add(row);

        queryRaw(query, org, consumer, ERROR_CONSUMER, EMPTY_ACTION, false);

        return String.join("\n", rows);
    }

    @Override
    public void queryRaw(@Nonnull final String query, @Nonnull final BiConsumer<Cancellable, String> onResponse) {

        Arguments.checkNotNull(options.getOrg(), "InfluxDBClientOptions.getOrg");

        queryRaw(query, options.getOrg(), onResponse);
    }

    @Override
    public void queryRaw(@Nonnull final String query,
                         @Nonnull final String org,
                         @Nonnull final BiConsumer<Cancellable, String> onResponse) {

        Arguments.checkNonEmpty(query, "query");
        Arguments.checkNonEmpty(org, "org");
        Arguments.checkNotNull(onResponse, "onResponse");

        queryRaw(query, null, org, onResponse);
    }

    @Override
    public void queryRaw(@Nonnull final Query query, @Nonnull final BiConsumer<Cancellable, String> onResponse) {

        Arguments.checkNotNull(options.getOrg(), "InfluxDBClientOptions.getOrg");

        queryRaw(query, options.getOrg(), onResponse);
    }

    @Override
    public void queryRaw(@Nonnull final Query query,
                         @Nonnull final String org,
                         @Nonnull final BiConsumer<Cancellable, String> onResponse) {

        Arguments.checkNotNull(query, "query");
        Arguments.checkNonEmpty(org, "org");
        Arguments.checkNotNull(onResponse, "onResponse");

        queryRaw(query, org, onResponse, ERROR_CONSUMER);
    }

    @Override
    public void queryRaw(@Nonnull final String query,
                         @Nullable final Dialect dialect,
                         @Nonnull final BiConsumer<Cancellable, String> onResponse) {

        Arguments.checkNotNull(options.getOrg(), "InfluxDBClientOptions.getOrg");

        queryRaw(query, dialect, options.getOrg(), onResponse);
    }

    @Override
    public void queryRaw(@Nonnull final String query,
                         @Nullable final Dialect dialect,
                         @Nonnull final String org,
                         @Nonnull final BiConsumer<Cancellable, String> onResponse) {

        Arguments.checkNonEmpty(query, "query");
        Arguments.checkNonEmpty(org, "org");
        Arguments.checkNotNull(onResponse, "onResponse");

        queryRaw(query, dialect, org, onResponse, ERROR_CONSUMER);
    }

    @Override
    public void queryRaw(@Nonnull final String query,
                         @Nonnull final BiConsumer<Cancellable, String> onResponse,
                         @Nonnull final Consumer<? super Throwable> onError) {

        Arguments.checkNotNull(options.getOrg(), "InfluxDBClientOptions.getOrg");

        queryRaw(query, options.getOrg(), onResponse, onError);
    }

    @Override
    public void queryRaw(@Nonnull final String query,
                         @Nonnull final String org,
                         @Nonnull final BiConsumer<Cancellable, String> onResponse,
                         @Nonnull final Consumer<? super Throwable> onError) {

        Arguments.checkNonEmpty(query, "query");
        Arguments.checkNotNull(onResponse, "onNext");
        Arguments.checkNotNull(onError, "onError");

        queryRaw(query, org, onResponse, onError, EMPTY_ACTION);
    }

    @Override
    public void queryRaw(@Nonnull final Query query,
                         @Nonnull final BiConsumer<Cancellable, String> onResponse,
                         @Nonnull final Consumer<? super Throwable> onError) {

        Arguments.checkNotNull(options.getOrg(), "InfluxDBClientOptions.getOrg");

        queryRaw(query, options.getOrg(), onResponse, onError);
    }

    @Override
    public void queryRaw(@Nonnull final Query query,
                         @Nonnull final String org,
                         @Nonnull final BiConsumer<Cancellable, String> onResponse,
                         @Nonnull final Consumer<? super Throwable> onError) {

        queryRaw(query, org, onResponse, onError, EMPTY_ACTION);
    }

    @Override
    public void queryRaw(@Nonnull final String query,
                         @Nullable final Dialect dialect,
                         @Nonnull final BiConsumer<Cancellable, String> onResponse,
                         @Nonnull final Consumer<? super Throwable> onError) {

        Arguments.checkNotNull(options.getOrg(), "InfluxDBClientOptions.getOrg");

        queryRaw(query, options.getOrg(), onResponse, onError);
    }

    @Override
    public void queryRaw(@Nonnull final String query,
                         @Nullable final Dialect dialect,
                         @Nonnull final String org,
                         @Nonnull final BiConsumer<Cancellable, String> onResponse,
                         @Nonnull final Consumer<? super Throwable> onError) {

        Arguments.checkNonEmpty(query, "query");
        Arguments.checkNonEmpty(org, "org");
        Arguments.checkNotNull(onResponse, "onNext");
        Arguments.checkNotNull(onError, "onError");

        queryRaw(query, dialect, org, onResponse, onError, EMPTY_ACTION);
    }

    @Override
    public void queryRaw(@Nonnull final String query,
                         @Nonnull final BiConsumer<Cancellable, String> onResponse,
                         @Nonnull final Consumer<? super Throwable> onError,
                         @Nonnull final Runnable onComplete) {

        Arguments.checkNotNull(options.getOrg(), "InfluxDBClientOptions.getOrg");

        queryRaw(query, options.getOrg(), onResponse, onError, onComplete);
    }

    @Override
    public void queryRaw(@Nonnull final String query,
                         @Nonnull final String org,
                         @Nonnull final BiConsumer<Cancellable, String> onResponse,
                         @Nonnull final Consumer<? super Throwable> onError,
                         @Nonnull final Runnable onComplete) {

        Arguments.checkNonEmpty(query, "query");
        Arguments.checkNonEmpty(org, "org");
        Arguments.checkNotNull(onResponse, "onNext");
        Arguments.checkNotNull(onError, "onError");
        Arguments.checkNotNull(onComplete, "onComplete");

        queryRaw(query, null, org, onResponse, onError, onComplete);
    }

    @Override
    public void queryRaw(@Nonnull final String query,
                         @Nullable final Dialect dialect,
                         @Nonnull final BiConsumer<Cancellable, String> onResponse,
                         @Nonnull final Consumer<? super Throwable> onError,
                         @Nonnull final Runnable onComplete) {

        Arguments.checkNotNull(options.getOrg(), "InfluxDBClientOptions.getOrg");

        queryRaw(query, dialect, options.getOrg(), onResponse, onError, onComplete);
    }

    @Override
    public void queryRaw(@Nonnull final String query,
                         @Nullable final Dialect dialect,
                         @Nonnull final String org,
                         @Nonnull final BiConsumer<Cancellable, String> onResponse,
                         @Nonnull final Consumer<? super Throwable> onError,
                         @Nonnull final Runnable onComplete) {

        Arguments.checkNonEmpty(query, "query");
        Arguments.checkNonEmpty(org, "org");
        Arguments.checkNotNull(onResponse, "onNext");
        Arguments.checkNotNull(onError, "onError");
        Arguments.checkNotNull(onComplete, "onComplete");

        queryRaw(new Query().query(query).dialect(dialect), org, onResponse, onError, onComplete, true);
    }

    @Override
    public void queryRaw(@Nonnull final Query query,
                         @Nonnull final BiConsumer<Cancellable, String> onResponse,
                         @Nonnull final Consumer<? super Throwable> onError,
                         @Nonnull final Runnable onComplete) {

        Arguments.checkNotNull(options.getOrg(), "InfluxDBClientOptions.getOrg");

        queryRaw(query, options.getOrg(), onResponse, onError, onComplete);
    }

    @Override
    public void queryRaw(@Nonnull final Query query,
                         @Nonnull final String org,
                         @Nonnull final BiConsumer<Cancellable, String> onResponse,
                         @Nonnull final Consumer<? super Throwable> onError,
                         @Nonnull final Runnable onComplete) {

        queryRaw(query, org, onResponse, onError, onComplete, true);
    }

    private void query(@Nonnull final Query query,
                       @Nonnull final String org,
                       @Nonnull final FluxCsvParser.FluxResponseConsumer responseConsumer,
                       @Nonnull final Consumer<? super Throwable> onError,
                       @Nonnull final Runnable onComplete,
                       @Nonnull final Boolean asynchronously) {

        Call<ResponseBody> queryCall = service
                .postQueryResponseBody(null, "application/json",
                        null, org, null, query);


        LOG.log(Level.FINEST, "Prepare query \"{0}\" with dialect \"{1}\" on organization \"{2}\".",
                new Object[]{query, query.getDialect(), org});

        query(queryCall, responseConsumer, onError, onComplete, asynchronously);
    }

    private void queryRaw(@Nonnull final Query query,
                          @Nonnull final String org,
                          @Nonnull final BiConsumer<Cancellable, String> onResponse,
                          @Nonnull final Consumer<? super Throwable> onError,
                          @Nonnull final Runnable onComplete,
                          @Nonnull final Boolean asynchronously) {

        Call<ResponseBody> queryCall = service
                .postQueryResponseBody(null, "application/json", null, org, null, query);

        LOG.log(Level.FINEST, "Prepare raw query \"{0}\" with dialect \"{1}\" on organization \"{2}\".",
                new Object[]{query, query.getDialect(), org});

        queryRaw(queryCall, onResponse, onError, onComplete, asynchronously);
    }
}