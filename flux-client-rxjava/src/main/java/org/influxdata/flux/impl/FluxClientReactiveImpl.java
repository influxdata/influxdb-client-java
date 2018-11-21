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
package org.influxdata.flux.impl;

import java.util.function.BiConsumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.influxdata.flux.FluxClientReactive;
import org.influxdata.flux.domain.FluxRecord;
import org.influxdata.flux.domain.FluxTable;
import org.influxdata.flux.impl.FluxCsvParser.FluxResponseConsumer;
import org.influxdata.flux.option.FluxConnectionOptions;
import org.influxdata.platform.Arguments;
import org.influxdata.platform.error.InfluxException;
import org.influxdata.platform.rest.Cancellable;
import org.influxdata.platform.rest.LogLevel;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.reactivestreams.Publisher;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

/**
 * @author Jakub Bednar (bednar@github) (05/10/2018 09:56)
 */
public class FluxClientReactiveImpl extends AbstractFluxClient<FluxServiceReactive> implements FluxClientReactive {

    public FluxClientReactiveImpl(@Nonnull final FluxConnectionOptions options) {
        super(options.getOkHttpClient(), options.getUrl(), options.getParameters(), FluxServiceReactive.class);
    }

    @Override
    protected void configure(@Nonnull final Retrofit.Builder serviceBuilder) {
        serviceBuilder.addCallAdapterFactory(RxJava2CallAdapterFactory.create());
    }

    @Nonnull
    @Override
    public Flowable<FluxRecord> query(@Nonnull final String query) {

        Arguments.checkNonEmpty(query, "Flux query");

        return query(Flowable.just(query));
    }

    @Override
    public <M> Flowable<M> query(final @Nonnull String query, final @Nonnull Class<M> measurementType) {

        Arguments.checkNonEmpty(query, "Flux query");
        Arguments.checkNotNull(measurementType, "The Record type");

        return query(query).map(fluxRecord -> resultMapper.toPOJO(fluxRecord, measurementType));
    }

    @Nonnull
    @Override
    public Flowable<FluxRecord> query(@Nonnull final Publisher<String> queryStream) {

        Arguments.checkNotNull(queryStream, "Flux query stream");

        return Flowable
                .fromPublisher(queryStream)
                .map(it -> fluxService.query(createBody(DEFAULT_DIALECT.toString(), it)))
                .flatMap(queryCall -> {

                    Observable<FluxRecord> observable = Observable.create(subscriber -> {

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
                                 @Nonnull final Class<M> measurementType) {

        Arguments.checkNotNull(queryStream, "Flux query stream");
        Arguments.checkNotNull(measurementType, "Record type");

        return query(queryStream).map(fluxRecord -> resultMapper.toPOJO(fluxRecord, measurementType));

    }

    @Nonnull
    @Override
    public Flowable<String> queryRaw(@Nonnull final String query) {

        Arguments.checkNonEmpty(query, "Flux query");

        return queryRaw(query, null);
    }

    @Nonnull
    @Override
    public Flowable<String> queryRaw(@Nonnull final Publisher<String> queryStream) {

        Arguments.checkNotNull(queryStream, "Flux query stream");

        return queryRaw(queryStream, null);
    }

    @Nonnull
    @Override
    public Flowable<String> queryRaw(@Nonnull final String query, @Nullable final String dialect) {

        Arguments.checkNonEmpty(query, "Flux query");

        return queryRaw(Flowable.just(query), dialect);
    }

    @Nonnull
    @Override
    public Flowable<String> queryRaw(@Nonnull final Publisher<String> queryStream, @Nullable final String dialect) {

        Arguments.checkNotNull(queryStream, "Flux query stream");

        return Flowable
                .fromPublisher(queryStream)
                .map(it -> fluxService.query(createBody(DEFAULT_DIALECT.toString(), it)))
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

    @Nonnull
    @Override
    public Single<Boolean> ping() {
        return fluxService
                .ping()
                .map(Response::isSuccessful)
                .onErrorReturn(throwable -> false);
    }

    @Nonnull
    @Override
    public Single<String> version() {

        return fluxService
                .ping()
                .map(this::getVersion)
                .onErrorResumeNext(throwable -> Single.error(toInfluxException(throwable)));
    }

    @Nonnull
    @Override
    public LogLevel getLogLevel() {
        return getLogLevel(this.loggingInterceptor);
    }

    @Nonnull
    @Override
    public FluxClientReactive setLogLevel(@Nonnull final LogLevel logLevel) {

        Arguments.checkNotNull(logLevel, "LogLevel");

        setLogLevel(this.loggingInterceptor, logLevel);

        return this;
    }

    @Nonnull
    private InfluxException toInfluxException(@Nonnull final Throwable throwable) {

        if (throwable instanceof InfluxException) {
            return (InfluxException) throwable;
        }

        return new InfluxException(throwable);
    }
}