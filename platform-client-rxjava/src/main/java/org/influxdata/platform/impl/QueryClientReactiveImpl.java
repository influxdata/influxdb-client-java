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

import java.util.function.BiConsumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.influxdata.flux.domain.FluxRecord;
import org.influxdata.flux.domain.FluxTable;
import org.influxdata.flux.impl.FluxCsvParser;
import org.influxdata.platform.Arguments;
import org.influxdata.platform.QueryClientReactive;
import org.influxdata.platform.rest.AbstractQueryClient;
import org.influxdata.platform.rest.Cancellable;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import org.reactivestreams.Publisher;

/**
 * @author Jakub Bednar (bednar@github) (21/11/2018 07:21)
 */
final class QueryClientReactiveImpl extends AbstractQueryClient implements QueryClientReactive {

    private final PlatformReactiveService platformService;

    QueryClientReactiveImpl(@Nonnull final PlatformReactiveService platformService) {

        Arguments.checkNotNull(platformService, "PlatformReactiveService");

        this.platformService = platformService;
    }

    @Nonnull
    @Override
    public Flowable<FluxRecord> query(@Nonnull final String query, @Nonnull final String organization) {

        Arguments.checkNonEmpty(query, "Flux query");
        Arguments.checkNonEmpty(organization, "organization");

        return query(Flowable.just(query), organization);
    }

    @Override
    public <M> Flowable<M> query(@Nonnull final String query,
                                 @Nonnull final String organization,
                                 @Nonnull final Class<M> measurementType) {

        Arguments.checkNonEmpty(query, "Flux query");
        Arguments.checkNotNull(measurementType, "Measurement type");
        Arguments.checkNonEmpty(organization, "organization");

        return query(Flowable.just(query), organization, measurementType);
    }

    @Nonnull
    @Override
    public Flowable<FluxRecord> query(@Nonnull final Publisher<String> queryStream,
                                      @Nonnull final String organization) {

        Arguments.checkNotNull(queryStream, "queryStream");
        Arguments.checkNonEmpty(organization, "organization");

        return Flowable
                .fromPublisher(queryStream)
                .map(it -> platformService.query(organization, createBody(DEFAULT_DIALECT.toString(), it)))
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

                        query(queryCall, consumer, subscriber::onError, subscriber::onComplete, false);
                    });

                    return observable.toFlowable(BackpressureStrategy.BUFFER);
                });
    }

    @Nonnull
    @Override
    public <M> Flowable<M> query(@Nonnull final Publisher<String> queryStream,
                                 @Nonnull final String organization,
                                 @Nonnull final Class<M> measurementType) {

        Arguments.checkNotNull(queryStream, "queryStream");
        Arguments.checkNotNull(measurementType, "Measurement type");
        Arguments.checkNonEmpty(organization, "organization");

        return query(queryStream, organization).map(fluxRecord -> resultMapper.toPOJO(fluxRecord, measurementType));
    }

    @Nonnull
    @Override
    public Flowable<String> queryRaw(@Nonnull final String query, @Nonnull final String organization) {

        Arguments.checkNonEmpty(query, "Flux query");
        Arguments.checkNonEmpty(organization, "organization");

        return queryRaw(Flowable.just(query), organization);
    }

    @Nonnull
    @Override
    public Flowable<String> queryRaw(@Nonnull final Publisher<String> queryStream,
                                     @Nonnull final String organization) {

        Arguments.checkNotNull(queryStream, "queryStream");
        Arguments.checkNonEmpty(organization, "organization");

        return queryRaw(queryStream, DEFAULT_DIALECT.toString(), organization);
    }

    @Nonnull
    @Override
    public Flowable<String> queryRaw(@Nonnull final String query,
                                     @Nullable final String dialect,
                                     @Nonnull final String organization) {

        Arguments.checkNonEmpty(query, "Flux query");
        Arguments.checkNonEmpty(organization, "organization");

        return queryRaw(Flowable.just(query), dialect, organization);
    }

    @Nonnull
    @Override
    public Flowable<String> queryRaw(@Nonnull final Publisher<String> queryStream,
                                     @Nullable final String dialect,
                                     @Nonnull final String organization) {

        Arguments.checkNotNull(queryStream, "queryStream");
        Arguments.checkNonEmpty(organization, "organization");

        return Flowable
                .fromPublisher(queryStream)
                .map(it -> platformService.query(organization, createBody(dialect, it)))
                .flatMap(queryCall -> {

                    Observable<String> observable = Observable.create(subscriber -> {


                        BiConsumer<Cancellable, String> consumer = (cancellable, line) -> {
                            if (subscriber.isDisposed()) {
                                cancellable.cancel();
                            } else {
                                subscriber.onNext(line);
                            }
                        };

                        queryRaw(queryCall, consumer, subscriber::onError, subscriber::onComplete, false);
                    });

                    return observable.toFlowable(BackpressureStrategy.BUFFER);
                });
    }
}