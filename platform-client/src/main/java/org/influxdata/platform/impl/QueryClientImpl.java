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
import java.util.logging.Level;
import java.util.logging.Logger;
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

    private static final Logger LOG = Logger.getLogger(QueryClientImpl.class.getName());

    private final PlatformService platformService;

    QueryClientImpl(@Nonnull final PlatformService platformService) {

        Arguments.checkNotNull(platformService, "PlatformService");

        this.platformService = platformService;
    }

    @Nonnull
    @Override
    public List<FluxTable> query(@Nonnull final String query, @Nonnull final String orgID) {

        Arguments.checkNonEmpty(query, "query");
        Arguments.checkNonEmpty(orgID, "orgID");

        FluxCsvParser.FluxResponseConsumerTable consumer = fluxCsvParser.new FluxResponseConsumerTable();

        query(query, DEFAULT_DIALECT.toString(), orgID, consumer, ERROR_CONSUMER, EMPTY_ACTION, false);

        return consumer.getTables();
    }

    @Nonnull
    @Override
    public <M> List<M> query(@Nonnull final String query,
                             @Nonnull final String orgID,
                             @Nonnull final Class<M> measurementType) {

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

        query(query, DEFAULT_DIALECT.toString(), orgID, consumer, ERROR_CONSUMER, EMPTY_ACTION, false);

        return measurements;
    }

    @Override
    public void query(@Nonnull final String query,
                      @Nonnull final String orgID,
                      @Nonnull final BiConsumer<Cancellable, FluxRecord> onNext) {

        Arguments.checkNonEmpty(query, "query");
        Arguments.checkNotNull(onNext, "onNext");

        query(query, orgID, onNext, ERROR_CONSUMER);
    }

    @Override
    public <M> void query(@Nonnull final String query,
                          @Nonnull final String orgID,
                          @Nonnull final Class<M> measurementType,
                          @Nonnull final BiConsumer<Cancellable, M> onNext) {

        Arguments.checkNonEmpty(query, "query");
        Arguments.checkNonEmpty(orgID, "orgID");
        Arguments.checkNotNull(onNext, "onNext");
        Arguments.checkNotNull(measurementType, "measurementType");

        query(query, orgID, measurementType, onNext, ERROR_CONSUMER);
    }

    @Override
    public void query(@Nonnull final String query,
                      @Nonnull final String orgID,
                      @Nonnull final BiConsumer<Cancellable, FluxRecord> onNext,
                      @Nonnull final Consumer<? super Throwable> onError) {

        Arguments.checkNonEmpty(query, "query");
        Arguments.checkNonEmpty(orgID, "orgID");
        Arguments.checkNotNull(onNext, "onNext");
        Arguments.checkNotNull(onError, "onError");

        query(query, orgID, onNext, onError, EMPTY_ACTION);
    }

    @Override
    public <M> void query(@Nonnull final String query,
                          @Nonnull final String orgID,
                          @Nonnull final Class<M> measurementType,
                          @Nonnull final BiConsumer<Cancellable, M> onNext,
                          @Nonnull final Consumer<? super Throwable> onError) {

        Arguments.checkNonEmpty(query, "query");
        Arguments.checkNonEmpty(orgID, "orgID");
        Arguments.checkNotNull(onNext, "onNext");
        Arguments.checkNotNull(onError, "onError");
        Arguments.checkNotNull(measurementType, "measurementType");

        query(query, orgID, measurementType, onNext, onError, EMPTY_ACTION);
    }

    @Override
    public void query(@Nonnull final String query,
                      @Nonnull final String orgID,
                      @Nonnull final BiConsumer<Cancellable, FluxRecord> onNext,
                      @Nonnull final Consumer<? super Throwable> onError,
                      @Nonnull final Runnable onComplete) {

        Arguments.checkNonEmpty(query, "query");
        Arguments.checkNonEmpty(orgID, "orgID");
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

        query(query, DEFAULT_DIALECT.toString(), orgID, consumer, onError, onComplete, true);
    }

    @Override
    public <M> void query(@Nonnull final String query,
                          @Nonnull final String orgID,
                          @Nonnull final Class<M> measurementType,
                          @Nonnull final BiConsumer<Cancellable, M> onNext,
                          @Nonnull final Consumer<? super Throwable> onError,
                          @Nonnull final Runnable onComplete) {

        Arguments.checkNonEmpty(query, "query");
        Arguments.checkNonEmpty(orgID, "orgID");
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

        query(query, DEFAULT_DIALECT.toString(), orgID, consumer, onError, onComplete, true);
    }

    @Nonnull
    @Override
    public String queryRaw(@Nonnull final String query, @Nonnull final String orgID) {

        Arguments.checkNonEmpty(query, "query");
        Arguments.checkNonEmpty(orgID, "orgID");

        return queryRaw(query, null, orgID);
    }

    @Nonnull
    @Override
    public String queryRaw(@Nonnull final String query,
                           @Nullable final String dialect,
                           @Nonnull final String orgID) {

        Arguments.checkNonEmpty(query, "query");
        Arguments.checkNonEmpty(orgID, "orgID");

        List<String> rows = new ArrayList<>();

        BiConsumer<Cancellable, String> consumer = (cancellable, row) -> rows.add(row);

        queryRaw(query, dialect, orgID, consumer, ERROR_CONSUMER, EMPTY_ACTION, false);

        return String.join("\n", rows);
    }

    @Override
    public void queryRaw(@Nonnull final String query,
                         @Nonnull final String orgID,
                         @Nonnull final BiConsumer<Cancellable, String> onResponse) {

        Arguments.checkNonEmpty(query, "query");
        Arguments.checkNonEmpty(orgID, "orgID");
        Arguments.checkNotNull(onResponse, "onResponse");

        queryRaw(query, null, orgID, onResponse);
    }

    @Override
    public void queryRaw(@Nonnull final String query,
                         @Nullable final String dialect,
                         @Nonnull final String orgID,
                         @Nonnull final BiConsumer<Cancellable, String> onResponse) {

        Arguments.checkNonEmpty(query, "query");
        Arguments.checkNonEmpty(orgID, "orgID");
        Arguments.checkNotNull(onResponse, "onResponse");

        queryRaw(query, dialect, orgID, onResponse, ERROR_CONSUMER);
    }

    @Override
    public void queryRaw(@Nonnull final String query,
                         @Nonnull final String orgID,
                         @Nonnull final BiConsumer<Cancellable, String> onResponse,
                         @Nonnull final Consumer<? super Throwable> onError) {

        Arguments.checkNonEmpty(query, "query");
        Arguments.checkNotNull(onResponse, "onNext");
        Arguments.checkNotNull(onError, "onError");

        queryRaw(query, orgID, onResponse, onError, EMPTY_ACTION);
    }

    @Override
    public void queryRaw(@Nonnull final String query,
                         @Nullable final String dialect,
                         @Nonnull final String orgID,
                         @Nonnull final BiConsumer<Cancellable, String> onResponse,
                         @Nonnull final Consumer<? super Throwable> onError) {

        Arguments.checkNonEmpty(query, "query");
        Arguments.checkNonEmpty(orgID, "orgID");
        Arguments.checkNotNull(onResponse, "onNext");
        Arguments.checkNotNull(onError, "onError");

        queryRaw(query, dialect, orgID, onResponse, onError, EMPTY_ACTION);
    }

    @Override
    public void queryRaw(@Nonnull final String query,
                         @Nonnull final String orgID,
                         @Nonnull final BiConsumer<Cancellable, String> onResponse,
                         @Nonnull final Consumer<? super Throwable> onError,
                         @Nonnull final Runnable onComplete) {

        Arguments.checkNonEmpty(query, "query");
        Arguments.checkNonEmpty(orgID, "orgID");
        Arguments.checkNotNull(onResponse, "onNext");
        Arguments.checkNotNull(onError, "onError");
        Arguments.checkNotNull(onComplete, "onComplete");

        queryRaw(query, null, orgID, onResponse, onError, onComplete);
    }

    @Override
    public void queryRaw(@Nonnull final String query,
                         @Nullable final String dialect,
                         @Nonnull final String orgID,
                         @Nonnull final BiConsumer<Cancellable, String> onResponse,
                         @Nonnull final Consumer<? super Throwable> onError,
                         @Nonnull final Runnable onComplete) {

        Arguments.checkNonEmpty(query, "query");
        Arguments.checkNonEmpty(orgID, "orgID");
        Arguments.checkNotNull(onResponse, "onNext");
        Arguments.checkNotNull(onError, "onError");
        Arguments.checkNotNull(onComplete, "onComplete");

        queryRaw(query, dialect, orgID, onResponse, onError, onComplete, true);
    }

    private void query(@Nonnull final String query,
                       @Nonnull final String dialect,
                       @Nonnull final String orgID,
                       @Nonnull final FluxCsvParser.FluxResponseConsumer responseConsumer,
                       @Nonnull final Consumer<? super Throwable> onError,
                       @Nonnull final Runnable onComplete,
                       @Nonnull final Boolean asynchronously) {

        Call<ResponseBody> queryCall = platformService.query(orgID, createBody(dialect, query));

        LOG.log(Level.FINEST, "Prepare query \"{0}\" with dialect \"{1}\" on organization \"{2}\".",
                new Object[]{query, dialect, orgID});

        query(queryCall, responseConsumer, onError, onComplete, asynchronously);
    }

    private void queryRaw(@Nonnull final String query,
                          @Nullable final String dialect,
                          @Nonnull final String orgID,
                          @Nonnull final BiConsumer<Cancellable, String> onResponse,
                          @Nonnull final Consumer<? super Throwable> onError,
                          @Nonnull final Runnable onComplete,
                          @Nonnull final Boolean asynchronously) {

        Call<ResponseBody> queryCall = platformService.query(orgID, createBody(dialect, query));

        LOG.log(Level.FINEST, "Prepare raw query \"{0}\" with dialect \"{1}\" on organization \"{2}\".",
                new String[]{query, dialect, orgID});

        queryRaw(queryCall, onResponse, onError, onComplete, asynchronously);
    }
}