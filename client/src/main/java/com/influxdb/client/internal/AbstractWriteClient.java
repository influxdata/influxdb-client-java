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

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.influxdb.client.InfluxDBClientOptions;
import com.influxdb.client.WriteApi;
import com.influxdb.client.WriteOptions;
import com.influxdb.client.domain.WriteConsistency;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.service.WriteService;
import com.influxdb.client.write.Point;
import com.influxdb.client.write.WriteParameters;
import com.influxdb.client.write.events.AbstractWriteEvent;
import com.influxdb.client.write.events.BackpressureEvent;
import com.influxdb.client.write.events.WriteErrorEvent;
import com.influxdb.client.write.events.WriteRetriableErrorEvent;
import com.influxdb.client.write.events.WriteSuccessEvent;
import com.influxdb.exceptions.InfluxException;
import com.influxdb.internal.AbstractRestClient;
import com.influxdb.utils.Arguments;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableTransformer;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Notification;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.subjects.PublishSubject;
import org.reactivestreams.Publisher;
import retrofit2.HttpException;
import retrofit2.Response;

/**
 * @author Jakub Bednar (bednar@github) (21/11/2018 09:26)
 */
public abstract class AbstractWriteClient extends AbstractRestClient implements AutoCloseable {

    private static final Logger LOG = Logger.getLogger(AbstractWriteClient.class.getName());
    private static final String CLOSED_EXCEPTION = "WriteApi is closed. "
            + "Data should be written before calling InfluxDBClient.close or WriteApi.close.";
    private static final int DEFAULT_WAIT = 30_000;
    private static final int DEFAULT_SLEEP = 25;

    private final WriteOptions writeOptions;
    protected final InfluxDBClientOptions options;

    private final PublishProcessor<BatchWriteItem> processor;
    private final PublishProcessor<Flowable<BatchWriteItem>> flushPublisher;
    private final PublishSubject<AbstractWriteEvent> eventPublisher;

    protected final MeasurementMapper measurementMapper = new MeasurementMapper();
    private final WriteService service;
    private final Collection<AutoCloseable> autoCloseables;
    private final PublishProcessor<Object> tempBoundary;

    private AtomicBoolean finished = new AtomicBoolean(false);

    public AbstractWriteClient(@Nonnull final WriteOptions writeOptions,
                               @Nonnull final InfluxDBClientOptions options,
                               @Nonnull final Scheduler processorScheduler,
                               @Nonnull final WriteService service,
                               @Nonnull final Collection<AutoCloseable> autoCloseables) {

        Arguments.checkNotNull(options, "options");

        this.writeOptions = writeOptions;
        this.options = options;
        this.service = service;
        this.autoCloseables = autoCloseables;

        this.flushPublisher = PublishProcessor.create();
        this.eventPublisher = PublishSubject.create();
        this.tempBoundary = PublishProcessor.create();
        this.processor = PublishProcessor.create();

        PublishProcessor<Flowable<BatchWriteItem>> tempBoundary = PublishProcessor.create();

        processor
                //
                // Enable Backpressure
                //
                .onBackpressureBuffer(
                        writeOptions.getBufferLimit(),
                        () -> publish(new BackpressureEvent()),
                        writeOptions.getBackpressureStrategy())
                //
                // Batching
                //
                .publish(connectedSource -> {

                    return connectedSource
                            .window(tempBoundary)
                            .mergeWith(Flowable.defer(() -> {
                                connectedSource
                                        // Buffering
                                        .window(writeOptions.getFlushInterval(),
                                                TimeUnit.MILLISECONDS,
                                                processorScheduler,
                                                writeOptions.getBatchSize(),
                                                true)
                                        // Flushing
                                        .mergeWith(flushPublisher)
                                        // Generate window
                                        .subscribe(tempBoundary);
                                return Flowable.empty();
                            }));
                })
                //
                // Group by key - same bucket, same org
                //
                .concatMap(it -> it.groupBy(batchWrite -> batchWrite.writeParameters))
                //
                // Create Write Point = bucket, org, ... + data
                //
                .concatMapSingle(grouped -> grouped
                        .map(it -> {
                            try {
                                String lineProtocol = it.data.toLineProtocol();
                                if (lineProtocol == null) {
                                    return "";
                                }
                                return lineProtocol;
                            } catch (Exception e) {
                                publish(new WriteErrorEvent(e));
                                return "";
                            }
                        })
                        .filter(it -> it != null && !it.isEmpty())
                        .collect(StringBuilder::new, (sb, x) -> {
                            if (sb.length() > 0) {
                                sb.append("\n");
                            }
                            sb.append(x);
                        })
                        .map(StringBuilder::toString)
                        .map(it -> new BatchWriteItem(grouped.getKey(), new BatchWriteDataRecord(it))))
                //
                // Jitter interval
                //
                .compose(jitter(processorScheduler, writeOptions))
                //
                // To WritePoints "request creator"
                //
                .concatMapMaybe(new ToWritePointsMaybe(processorScheduler))
                .doFinally(() -> finished.set(true))
                .subscribe(responseNotification -> {

                    if (responseNotification.isOnError()) {
                        publish(new WriteErrorEvent(toInfluxException(responseNotification.getError())));
                    }
                }, throwable -> publish(new WriteErrorEvent(toInfluxException(throwable))));

        autoCloseables.add(this);
    }

    @Nonnull
    protected <T extends AbstractWriteEvent> Observable<T> addEventListener(@Nonnull final Class<T> eventType) {

        Objects.requireNonNull(eventType, "EventType is required");

        return eventPublisher.ofType(eventType);
    }

    public void flush() {
        flushPublisher.offer(Flowable.empty());
    }

    public void close() {

        LOG.log(Level.FINE, "Flushing any cached BatchWrites before shutdown.");

        autoCloseables.remove(this);

        processor.onComplete();

        flushPublisher.onComplete();
        tempBoundary.onComplete();
        eventPublisher.onComplete();

        waitToCondition(() -> finished.get(), DEFAULT_WAIT);
    }

    public void writePoints(@Nonnull final WriteParameters writeParameters,
                            @Nonnull final Flowable<BatchWriteDataPoint> stream) {

        if (processor.hasComplete()) {
            throw new InfluxException(CLOSED_EXCEPTION);
        }

        stream.subscribe(
                dataPoint -> {
                    WritePrecision precision = dataPoint.point.getPrecision();
                    write(writeParameters.copy(precision, options), Flowable.just(dataPoint));
                },
                throwable -> publish(new WriteErrorEvent(throwable)));
    }

    public void write(@Nonnull final WriteParameters writeParameters,
                      @Nonnull final Publisher<AbstractWriteClient.BatchWriteData> stream) {

        Arguments.checkNotNull(writeParameters, "writeParameters");
        Arguments.checkNotNull(stream, "data to write");

        if (processor.hasComplete()) {
            throw new InfluxException(CLOSED_EXCEPTION);
        }

        Flowable.fromPublisher(stream)
                .map(it -> new BatchWriteItem(writeParameters, it))
                .subscribe(processor::onNext, throwable -> publish(new WriteErrorEvent(throwable)));
    }

    private <T extends AbstractWriteEvent> void publish(@Nonnull final T event) {

        Arguments.checkNotNull(event, "event");

        event.logEvent();
        eventPublisher.onNext(event);
    }

    public interface BatchWriteData {

        @Nullable
        String toLineProtocol();
    }

    public static final class BatchWriteDataRecord implements BatchWriteData {

        private final String record;

        public BatchWriteDataRecord(@Nullable final String record) {
            this.record = record;
        }

        @Nullable
        @Override
        public String toLineProtocol() {
            return record;
        }
    }

    public static final class BatchWriteDataPoint implements BatchWriteData {

        private static final Logger LOG = Logger.getLogger(BatchWriteDataPoint.class.getName());

        private final Point point;
        private final WritePrecision precision;
        private final InfluxDBClientOptions options;

        public BatchWriteDataPoint(@Nonnull final Point point,
                                   @Nonnull final InfluxDBClientOptions options) {

            this(point, point.getPrecision(), options);
        }

        public BatchWriteDataPoint(@Nonnull final Point point,
                                   @Nonnull final WritePrecision precision,
                                   @Nonnull final InfluxDBClientOptions options) {

            this.point = point;
            this.precision = precision;
            this.options = options;
        }

        @Nullable
        @Override
        public String toLineProtocol() {

            if (!point.hasFields()) {

                LOG.warning("The point: " + point + "doesn't contains any fields, skipping");

                return null;
            }

            return point.toLineProtocol(options.getPointSettings(), precision);
        }
    }

    public static final class BatchWriteDataMeasurement implements BatchWriteData {

        private final Object measurement;
        private final WritePrecision precision;
        private final InfluxDBClientOptions options;
        private final MeasurementMapper measurementMapper;

        public BatchWriteDataMeasurement(@Nullable final Object measurement,
                                         @Nonnull final WritePrecision precision,
                                         @Nonnull final InfluxDBClientOptions options,
                                         @Nonnull final MeasurementMapper measurementMapper) {
            this.measurement = measurement;
            this.precision = precision;
            this.options = options;
            this.measurementMapper = measurementMapper;
        }

        @Nullable
        @Override
        public String toLineProtocol() {

            if (measurement == null) {
                return null;
            }

            Point point = measurementMapper.toPoint(measurement, precision);
            if (!point.hasFields()) {

                LOG.warning("The measurement: " + measurement + "doesn't contains any fields, skipping");

                return null;
            }

            return point.toLineProtocol(options.getPointSettings());
        }
    }

    /**
     * The Batch Write Item.
     */
    final class BatchWriteItem {

        private WriteParameters writeParameters;
        private BatchWriteData data;

        private BatchWriteItem(@Nonnull final WriteParameters writeParameters,
                               @Nonnull final BatchWriteData data) {

            Arguments.checkNotNull(writeParameters, "writeParameters");
            Arguments.checkNotNull(data, "data");

            this.writeParameters = writeParameters;
            this.data = data;
        }
    }

    @SuppressWarnings("rawtypes")
    private final class ToWritePointsMaybe implements Function<BatchWriteItem, Maybe<Notification<Response>>> {

        private final Scheduler retryScheduler;

        private ToWritePointsMaybe(@Nonnull final Scheduler retryScheduler) {
            this.retryScheduler = retryScheduler;
        }

        @Override
        public Maybe<Notification<Response>> apply(final BatchWriteItem batchWrite) {

            String content = batchWrite.data.toLineProtocol();

            if (content == null || content.isEmpty()) {
                return Maybe.empty();
            }

            // Parameters
            String organization = batchWrite.writeParameters.orgSafe(options);
            String bucket = batchWrite.writeParameters.bucketSafe(options);
            WritePrecision precision = batchWrite.writeParameters.precisionSafe(options);
            WriteConsistency consistency = batchWrite.writeParameters.consistencySafe(options);

            Maybe<Response<Void>> requestSource = service
                    .postWriteRx(organization, bucket, content, null,
                            "identity", "text/plain; charset=utf-8", null,
                            "application/json", null, precision, consistency)
                    .toMaybe();

            return requestSource
                    //
                    // Response is not Successful => throw exception
                    //
                    .map((Function<Response<Void>, Response>) response -> {

                        if (!response.isSuccessful()) {
                            throw new HttpException(response);
                        }

                        return response;
                    })
                    //
                    // Is exception retriable?
                    //
                    .retryWhen(retry(retryScheduler, writeOptions, (throwable, retryInterval) ->
                            publish(new WriteRetriableErrorEvent(toInfluxException(throwable), retryInterval))))
                    //
                    // maxRetryTime timeout
                    //
                    .timeout(writeOptions.getMaxRetryTime(), TimeUnit.MILLISECONDS, retryScheduler,
                        Maybe.error(new TimeoutException("Max retry time exceeded.")))
                    //
                    // Map response to Notification => possibility to consume error as event
                    //
                    .map((Function<Response, Notification<Response>>) response -> {

                        if (response.isSuccessful()) {
                            return Notification.createOnNext(response);
                        }

                        return Notification.createOnError(new HttpException(response));
                    })
                    .doOnSuccess(responseNotification -> {
                        if (!responseNotification.isOnError()) {
                            publish(toSuccessEvent(batchWrite, content));
                        }
                    })
                    .onErrorResumeNext(throwable -> {
                        return Maybe.just(Notification.createOnError(throwable));
                    });
        }

        @Nonnull
        private WriteSuccessEvent toSuccessEvent(@Nonnull final BatchWriteItem batchWrite, final String lineProtocol) {

            return new WriteSuccessEvent(
                    batchWrite.writeParameters.orgSafe(options),
                    batchWrite.writeParameters.bucketSafe(options),
                    batchWrite.writeParameters.precisionSafe(options),
                    lineProtocol);
        }
    }

    /**
     * Add Jitter delay to upstream.
     *
     * @param scheduler    to use for timer operator
     * @param retryOptions with configured jitter interval
     * @param <T>          upstream type
     * @return Flowable with jitter delay
     */
    @Nonnull
    public static <T> FlowableTransformer<T, T> jitter(@Nonnull final Scheduler scheduler,
                                                       @Nonnull final WriteApi.RetryOptions retryOptions) {

        Arguments.checkNotNull(retryOptions, "JitterOptions is required");
        Arguments.checkNotNull(scheduler, "Jitter scheduler is required");

        return source -> {

            //
            // source without jitter
            //
            if (retryOptions.getJitterInterval() <= 0) {
                return source;
            }

            //
            // Add jitter => dynamic delay
            //
            return source.delay((Function<T, Flowable<Long>>) pointFlowable -> {

                int delay = RetryAttempt.jitterDelay(retryOptions.getJitterInterval());

                LOG.log(Level.FINEST, "Generated Jitter dynamic delay: {0}", delay);

                return Flowable.timer(delay, TimeUnit.MILLISECONDS, scheduler);
            });
        };
    }

    /**
     * Add Retry handler to upstream.
     *
     * @param retryScheduler for retry delay
     * @param retryOptions   with configured retry strategy
     * @param notify         to notify about retryable error
     * @return Flowable with retry handler
     */
    @Nonnull
    public static Function<Flowable<Throwable>, Publisher<?>> retry(@Nonnull final Scheduler retryScheduler,
                                                                    @Nonnull final WriteApi.RetryOptions retryOptions,
                                                                    @Nonnull final BiConsumer<Throwable, Long> notify) {

        Objects.requireNonNull(retryOptions, "RetryOptions are required");
        Objects.requireNonNull(retryScheduler, "RetryScheduler is required");

        return errors -> errors
                .zipWith(Flowable.range(1, retryOptions.getMaxRetries() + 1),
                        (throwable, count) -> new RetryAttempt(throwable, count, retryOptions))
                .flatMap(attempt -> {

                    Throwable throwable = attempt.getThrowable();
                    if (attempt.isRetry()) {

                        long retryInterval = attempt.getRetryInterval();

                        notify.accept(throwable, retryInterval);

                        return Flowable.just("notify").delay(retryInterval, TimeUnit.MILLISECONDS, retryScheduler);
                    }

                    //
                    // This type of throwable is not able to retry
                    //
                    return Flowable.error(throwable);
                });
    }

    static void waitToCondition(final Supplier<Boolean> condition, final int millis) {
        long start = System.currentTimeMillis();
        while (!condition.get()) {
            try {
                Thread.sleep(DEFAULT_SLEEP);
            } catch (InterruptedException e) {
                LOG.log(Level.SEVERE, "Interrupted during wait to dispose.", e);
            }
            if ((System.currentTimeMillis() - start) > millis) {
                LOG.severe("The WriteApi can't be gracefully dispose! - " + millis + "ms elapsed.");
                break;
            }
        }
    }
}
