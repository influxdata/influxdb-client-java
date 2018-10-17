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
package org.influxdata.platform.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.influxdata.flux.domain.FluxRecord;
import org.influxdata.flux.domain.FluxTable;
import org.influxdata.flux.impl.FluxCsvParser;
import org.influxdata.platform.Arguments;
import org.influxdata.platform.QueryClient;
import org.influxdata.platform.rest.AbstractQueryClient;
import org.influxdata.platform.rest.Cancellable;

import okhttp3.ResponseBody;
import retrofit2.Call;

/**
 * @author Jakub Bednar (bednar@github) (17/10/2018 10:50)
 */
final class QueryClientImpl extends AbstractQueryClient implements QueryClient {

    private final FluxCsvParser fluxCsvParser;
    private final PlatformService platformService;

    QueryClientImpl(@Nonnull final PlatformService platformService) {

        Arguments.checkNotNull(platformService, "PlatformService");

        this.platformService = platformService;
        this.fluxCsvParser = new FluxCsvParser();
    }

    @Nonnull
    @Override
    public List<FluxTable> query(@Nonnull final String query) {

        Arguments.checkNonEmpty(query, "query");

        FluxCsvParser.FluxResponseConsumerTable consumer = fluxCsvParser.new FluxResponseConsumerTable();

        query(query, DEFAULT_DIALECT.toString(), consumer, ERROR_CONSUMER, EMPTY_ACTION, false);

        return consumer.getTables();
    }

    @Nonnull
    @Override
    public <M> List<M> query(@Nonnull final String query, @Nonnull final Class<M> measurementType) {

        Arguments.checkNonEmpty(query, "query");

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
    public void queryRaw(@Nonnull final String query, @Nonnull final BiConsumer<Cancellable, String> onResponse) {
        Arguments.checkNonEmpty(query, "query");
        Arguments.checkNotNull(onResponse, "onResponse");

        queryRaw(query, null, onResponse);
    }

    @Override
    public void queryRaw(@Nonnull final String query,
                         @Nullable final String dialect,
                         @Nonnull final BiConsumer<Cancellable, String> onResponse) {

        Arguments.checkNonEmpty(query, "query");
        Arguments.checkNotNull(onResponse, "onResponse");

        queryRaw(query, dialect, onResponse, ERROR_CONSUMER);
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

    private void query(@Nonnull final String query,
                       @Nonnull final String dialect,
                       @Nonnull final FluxCsvParser.FluxResponseConsumer responseConsumer,
                       @Nonnull final Consumer<? super Throwable> onError,
                       @Nonnull final Runnable onComplete,
                       @Nonnull final Boolean asynchronously) {

        Call<ResponseBody> queryCall = platformService.query(createBody(dialect, query));

        query(queryCall, responseConsumer, onError, onComplete, asynchronously);
    }

    private void queryRaw(@Nonnull final String query,
                          @Nullable final String dialect,
                          @Nonnull final BiConsumer<Cancellable, String> onResponse,
                          @Nonnull final Consumer<? super Throwable> onError,
                          @Nonnull final Runnable onComplete,
                          @Nonnull final Boolean asynchronously) {

        Call<ResponseBody> queryCall = platformService.query(createBody(dialect, query));

        queryRaw(queryCall, onResponse, onError, onComplete, asynchronously);
    }
}