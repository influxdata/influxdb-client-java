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

import java.io.IOException;
import java.io.UncheckedIOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.influxdata.flux.FluxClientReactive;
import org.influxdata.flux.domain.FluxRecord;
import org.influxdata.flux.domain.FluxTable;
import org.influxdata.flux.option.FluxConnectionOptions;
import org.influxdata.platform.Arguments;
import org.influxdata.platform.error.InfluxException;
import org.influxdata.platform.rest.Cancellable;
import org.influxdata.platform.rest.LogLevel;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.Single;
import io.reactivex.functions.BiConsumer;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import org.reactivestreams.Publisher;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

/**
 * @author Jakub Bednar (bednar@github) (05/10/2018 09:56)
 */
public class FluxClientReactiveImpl extends AbstractFluxClient<FluxServiceReactive> implements FluxClientReactive {

    protected final FluxResultMapper mapper = new FluxResultMapper();


    public FluxClientReactiveImpl(@Nonnull final FluxConnectionOptions options) {
        super(options, FluxServiceReactive.class);
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
    public <M> Flowable<M> query(final @Nonnull String query, final @Nonnull Class<M> recordType) {

        return query(query)
                .map(fluxResults -> mapper.toPOJO(fluxResults, recordType));
    }

    @Nonnull
    @Override
    public Flowable<FluxRecord> query(@Nonnull final Publisher<String> queryStream) {

        Arguments.checkNotNull(queryStream, "Flux query stream");

        BiConsumer<BufferedSource, ObservableEmitter<FluxRecord>> consumer = (source, observable) ->
                fluxCsvParser.parseFluxResponse(
                        source,
                        new ObservableCancellable(observable),
                        new FluxCsvParser.FluxResponseConsumer() {

                            @Override
                            public void accept(final int index,
                                               @Nonnull final Cancellable cancellable,
                                               @Nonnull final FluxTable table) {

                            }

                            @Override
                            public void accept(final int index,
                                               @Nonnull final Cancellable cancellable,
                                               @Nonnull final FluxRecord record) {

                                observable.onNext(record);
                            }
                        });

        return query(queryStream, DEFAULT_DIALECT.toString(), consumer);

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

        BiConsumer<BufferedSource, ObservableEmitter<String>> consumer = (source, observable) ->
                parseFluxResponseToLines(observable::onNext, new ObservableCancellable(observable), source);

        return query(queryStream, dialect, consumer);
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
    private <T> Flowable<T> query(@Nonnull final Publisher<String> queryStream,
                                  @Nullable final String dialect,
                                  @Nonnull final BiConsumer<BufferedSource, ObservableEmitter<T>> consumer) {

        return Flowable
                .fromPublisher(queryStream)
                .concatMap(query -> fluxService
                        .query(createBody(dialect, query))
                        .flatMap(responseBody -> chunkReader(responseBody, consumer))
                        .toFlowable(BackpressureStrategy.BUFFER))
                .onErrorResumeNext(throwable -> {
                    return Flowable.error(toInfluxException(throwable));
                });
    }

    @Nonnull
    private <T> Observable<T> chunkReader(@Nonnull final ResponseBody body,
                                          @Nonnull final BiConsumer<BufferedSource, ObservableEmitter<T>> consumer) {

        Arguments.checkNotNull(body, "ResponseBody");
        Arguments.checkNotNull(consumer, "BufferedSource consumer");

        return Observable.create(subscriber -> {

            boolean isCompleted = false;
            try {
                BufferedSource source = body.source();

                //
                // Subscriber is not disposed && source has data => parse
                //
                while (!subscriber.isDisposed() && !source.exhausted()) {

                    consumer.accept(source, subscriber);
                }

            } catch (IOException e) {

                //
                // Socket close by remote server or end of data
                //
                if (isCloseException(e)) {
                    isCompleted = true;
                    subscriber.onComplete();
                } else {
                    throw new UncheckedIOException(e);
                }
            }

            //if response end we get here
            if (!isCompleted) {
                subscriber.onComplete();
            }

            body.close();
        });
    }

    @Nonnull
    private InfluxException toInfluxException(@Nonnull final Throwable throwable) {

        if (throwable instanceof InfluxException) {
            return (InfluxException) throwable;
        }

        return new InfluxException(throwable);
    }

    private final class ObservableCancellable implements Cancellable {

        private final ObservableEmitter<?> observable;

        private ObservableCancellable(@Nonnull final ObservableEmitter<?> observable) {
            this.observable = observable;
        }

        @Override
        public void cancel() {

        }

        @Override
        public boolean isCancelled() {
            return observable.isDisposed();
        }
    }
}