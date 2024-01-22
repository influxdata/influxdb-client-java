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

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.influxdb.Cancellable;
import com.influxdb.client.InfluxDBClientOptions;
import com.influxdb.client.domain.Dialect;
import com.influxdb.client.domain.Query;
import com.influxdb.client.internal.AbstractInfluxDBClient;
import com.influxdb.client.reactive.QueryReactiveApi;
import com.influxdb.client.service.QueryService;
import com.influxdb.internal.AbstractQueryApi;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.influxdb.query.internal.FluxCsvParser;
import com.influxdb.utils.Arguments;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import org.reactivestreams.Publisher;

/**
 * @author Jakub Bednar (bednar@github) (21/11/2018 07:21)
 */
final class QueryReactiveApiImpl extends AbstractQueryApi implements QueryReactiveApi {

    private final QueryService service;
    private final InfluxDBClientOptions options;

    QueryReactiveApiImpl(@Nonnull final QueryService service, @Nonnull final InfluxDBClientOptions options) {

        Arguments.checkNotNull(service, "InfluxDBReactiveService");
        Arguments.checkNotNull(options, "options");

        this.service = service;
        this.options = options;
    }

    @Nonnull
    @Override
    public Publisher<FluxRecord> query(@Nonnull final String query) {

        Arguments.checkNotNull(options.getOrg(), "InfluxDBClientOptions.getOrg");

        return query(query, options.getOrg());
    }

    @Nonnull
    @Override
    public Publisher<FluxRecord> query(@Nonnull final String query, @Nonnull final String org) {

        Arguments.checkNonEmpty(query, "Flux query");
        Arguments.checkNonEmpty(org, "org");

        return query(Flowable.just(query), org);
    }

    @Nonnull
    @Override
    public Publisher<FluxRecord> query(@Nonnull final Query query, @Nonnull final String org) {

        Arguments.checkNotNull(query, "Flux query");
        Arguments.checkNonEmpty(org, "org");
        return queryQuery(Flowable.just(query), org);
    }

    @Nonnull

    @Override
    public Publisher<FluxRecord> query(@Nonnull final Query query) {

        Arguments.checkNotNull(options.getOrg(), "InfluxDBClientOptions.getOrg");
        Arguments.checkNotNull(query, "Flux query");

        return queryQuery(Flowable.just(query), options.getOrg());
    }

    @Override
    public <M> Publisher<M> query(@Nonnull final String query, @Nonnull final Class<M> measurementType) {

        Arguments.checkNotNull(options.getOrg(), "InfluxDBClientOptions.getOrg");

        return query(query, options.getOrg(), measurementType);
    }

    @Override
    public <M> Publisher<M> query(@Nonnull final String query,
                                  @Nonnull final String org,
                                  @Nonnull final Class<M> measurementType) {

        Arguments.checkNonEmpty(query, "Flux query");
        Arguments.checkNotNull(measurementType, "Measurement type");
        Arguments.checkNonEmpty(org, "org");

        return query(Flowable.just(query), org, measurementType);
    }

    @Override
    public <M> Publisher<M> query(@Nonnull final Query query, @Nonnull final String org,
                                  @Nonnull final Class<M> measurementType) {
        Arguments.checkNotNull(query, "Flux query");
        Arguments.checkNotNull(measurementType, "Measurement type");
        Arguments.checkNonEmpty(org, "org");

        return Flowable
            .fromPublisher(queryQuery(Flowable.just(query), org))
            .map(fluxRecord -> resultMapper.toPOJO(fluxRecord, measurementType));
    }


    @Nonnull
    @Override
    public Publisher<FluxRecord> query(@Nonnull final Publisher<String> queryStream) {

        Arguments.checkNotNull(options.getOrg(), "InfluxDBClientOptions.getOrg");

        return query(queryStream, options.getOrg());
    }

    @Nonnull
    @Override
    public Publisher<FluxRecord> query(@Nonnull final Publisher<String> queryStream,
                                       @Nonnull final String org) {
        return queryQuery(Flowable.fromPublisher(queryStream)
            .map(query -> new Query().query(query).dialect(AbstractInfluxDBClient.DEFAULT_DIALECT)), org);
    }

    @Nonnull
    @Override
    public Publisher<FluxRecord> queryQuery(@Nonnull final Publisher<Query> queryStream,
                                            @Nonnull final String org) {

        Arguments.checkNotNull(queryStream, "queryStream");
        Arguments.checkNonEmpty(org, "org");

        return Flowable
                .fromPublisher(queryStream)
                .map(it -> service.postQueryResponseBody(null, null,
                        null, org, null, it.dialect(AbstractInfluxDBClient.DEFAULT_DIALECT)))
                .flatMap(queryCall -> {

                    Observable<FluxRecord> observable = Observable.create(subscriber -> {

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

                                if (subscriber.isDisposed()) {
                                    cancellable.cancel();
                                } else {
                                    subscriber.onNext(record);
                                }
                            }
                        };

                        query(queryCall, consumer, onError(subscriber), subscriber::onComplete, false);
                    });

                    return observable.toFlowable(BackpressureStrategy.BUFFER);
                });
    }

    @Nonnull
    @Override
    public <M> Publisher<M> query(@Nonnull final Publisher<String> queryStream,
                                  @Nonnull final Class<M> measurementType) {

        Arguments.checkNotNull(options.getOrg(), "InfluxDBClientOptions.getOrg");

        return query(queryStream, options.getOrg(), measurementType);
    }

    @Nonnull
    @Override
    public <M> Publisher<M> query(@Nonnull final Publisher<String> queryStream,
                                  @Nonnull final String org,
                                  @Nonnull final Class<M> measurementType) {

        Arguments.checkNotNull(queryStream, "queryStream");
        Arguments.checkNotNull(measurementType, "Measurement type");
        Arguments.checkNonEmpty(org, "org");

        return Flowable
                .fromPublisher(query(queryStream, org))
                .map(fluxRecord -> resultMapper.toPOJO(fluxRecord, measurementType));
    }

    @Nonnull
    @Override
    public Publisher<String> queryRaw(@Nonnull final String query) {

        Arguments.checkNotNull(options.getOrg(), "InfluxDBClientOptions.getOrg");

        return queryRaw(query, options.getOrg());
    }

    @Nonnull
    @Override
    public Publisher<String> queryRaw(@Nonnull final Query query) {

        Arguments.checkNotNull(options.getOrg(), "InfluxDBClientOptions.getOrg");

        return queryRawQuery(Flowable.just(query), null, options.getOrg());
    }

    @Nonnull
    @Override
    public Publisher<String> queryRaw(@Nonnull final String query, @Nonnull final String org) {

        Arguments.checkNonEmpty(query, "Flux query");
        Arguments.checkNonEmpty(org, "org");

        return queryRaw(Flowable.just(query), org);
    }

    @Nonnull
    @Override
    public Publisher<String> queryRaw(@Nonnull final Publisher<String> queryStream) {

        Arguments.checkNotNull(options.getOrg(), "InfluxDBClientOptions.getOrg");

        return queryRaw(queryStream, options.getOrg());
    }

    @Nonnull
    @Override
    public Publisher<String> queryRaw(@Nonnull final Publisher<String> queryStream,
                                      @Nonnull final String org) {

        Arguments.checkNotNull(queryStream, "queryStream");
        Arguments.checkNonEmpty(org, "org");

        return queryRaw(queryStream, AbstractInfluxDBClient.DEFAULT_DIALECT, org);
    }

    @Nonnull
    @Override
    public Publisher<String> queryRaw(@Nonnull final String query, @Nullable final Dialect dialect) {

        Arguments.checkNotNull(options.getOrg(), "InfluxDBClientOptions.getOrg");

        return queryRaw(query, dialect, options.getOrg());
    }

    @Nonnull
    @Override
    public Publisher<String> queryRaw(@Nonnull final String query,
                                      @Nullable final Dialect dialect,
                                      @Nonnull final String org) {

        Arguments.checkNonEmpty(query, "Flux query");
        Arguments.checkNonEmpty(org, "org");

        return queryRaw(Flowable.just(query), dialect, org);
    }

    @Nonnull
    @Override
    public Publisher<String> queryRaw(@Nonnull final Publisher<String> queryStream, @Nullable final Dialect dialect) {

        Arguments.checkNotNull(options.getOrg(), "InfluxDBClientOptions.getOrg");

        return queryRaw(queryStream, dialect, options.getOrg());
    }

    @Nonnull
    @Override
    public Publisher<String> queryRaw(@Nonnull final Query query,
                                      @Nullable final Dialect dialect,
                                      @Nonnull final String org) {
        return queryRawQuery(Flowable.just(query), dialect, org);
    }

    @Nonnull
    @Override
    public Publisher<String> queryRawQuery(@Nonnull final Publisher<Query> queryStream,
                                           @Nullable final Dialect dialect,
                                           @Nonnull final String org) {

        Arguments.checkNotNull(queryStream, "queryStream");
        Arguments.checkNonEmpty(org, "org");

        return Flowable
            .fromPublisher(queryStream)
            .map(it -> service.postQueryResponseBody(null, null,
                null, org, null, it.dialect(dialect)))
            .flatMap(queryCall -> {

                Observable<String> observable = Observable.create(subscriber -> {


                    BiConsumer<Cancellable, String> consumer = (cancellable, line) -> {
                        if (subscriber.isDisposed()) {
                            cancellable.cancel();
                        } else {
                            subscriber.onNext(line);
                        }
                    };

                    queryRaw(queryCall, consumer, onError(subscriber), subscriber::onComplete, false);
                });

                return observable.toFlowable(BackpressureStrategy.BUFFER);
            });
    }

    @Nonnull
    @Override
    public Publisher<String> queryRaw(@Nonnull final Publisher<String> queryStream,
                                      @Nullable final Dialect dialect,
                                      @Nonnull final String org) {

        Arguments.checkNotNull(queryStream, "queryStream");
        Arguments.checkNonEmpty(org, "org");

        return queryRawQuery(Flowable.fromPublisher(queryStream)
            .map(q -> new Query().query(q).dialect(dialect)), dialect, org);
    }

    @Nonnull
    private Consumer<Throwable> onError(final ObservableEmitter<?> subscriber) {
        return throwable -> {
            if (!subscriber.isDisposed()) {
                subscriber.onError(throwable);
            } else {
                LOG.log(Level.FINEST, "The exception could not be delivered to the consumer "
                        + "because it has already canceled/disposed.", throwable);
            }
        };
    }
}
