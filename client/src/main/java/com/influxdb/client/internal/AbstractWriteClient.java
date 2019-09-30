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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.influxdb.Arguments;
import com.influxdb.client.InfluxDBClientOptions;
import com.influxdb.client.WriteOptions;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.service.WriteService;
import com.influxdb.client.write.Point;
import com.influxdb.client.write.events.AbstractWriteEvent;
import com.influxdb.client.write.events.WriteErrorEvent;
import com.influxdb.client.write.events.WriteRetriableErrorEvent;
import com.influxdb.client.write.events.WriteSuccessEvent;
import com.influxdb.exceptions.InfluxException;
import com.influxdb.internal.AbstractRestClient;

import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import io.reactivex.Maybe;
import io.reactivex.Notification;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.functions.Function;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.subjects.PublishSubject;
import org.reactivestreams.Publisher;
import retrofit2.Call;
import retrofit2.HttpException;
import retrofit2.Response;

/**
 * @author Jakub Bednar (bednar@github) (21/11/2018 09:26)
 */
public abstract class AbstractWriteClient extends AbstractRestClient {

    private static final Logger LOG = Logger.getLogger(AbstractWriteClient.class.getName());
    private static final List<Integer> ABLE_TO_RETRY_ERRORS = Arrays.asList(429, 503);

    private final WriteOptions writeOptions;
    protected final InfluxDBClientOptions options;

    private final PublishProcessor<AbstractWriteClient.BatchWriteItem> processor;
    private final PublishProcessor<Flowable<BatchWriteItem>> flushPublisher;
    private final PublishSubject<AbstractWriteEvent> eventPublisher;

    protected final MeasurementMapper measurementMapper = new MeasurementMapper();
    private final WriteService service;

    public AbstractWriteClient(@Nonnull final WriteOptions writeOptions,
                               @Nonnull final InfluxDBClientOptions options,
                               @Nonnull final Scheduler processorScheduler,
                               @Nonnull final WriteService service) {

        Arguments.checkNotNull(options, "options");

        this.writeOptions = writeOptions;
        this.options = options;
        this.service = service;

        this.flushPublisher = PublishProcessor.create();
        this.eventPublisher = PublishSubject.create();
        this.processor = PublishProcessor.create();


        Flowable<Flowable<BatchWriteItem>> boundary = processor
                .window(writeOptions.getFlushInterval(),
                        TimeUnit.MILLISECONDS,
                        processorScheduler,
                        writeOptions.getBatchSize(),
                        true)

                .mergeWith(flushPublisher);

        PublishProcessor<Flowable<BatchWriteItem>> tempBoundary = PublishProcessor.create();

        processor
//                .onBackpressureBuffer(
//                        writeOptions.getBufferLimit(),
//                        () -> publish(new BackpressureEvent()),
//                        writeOptions.getBackpressureStrategy())
//                .observeOn(processorScheduler)
                //
                // Batching
                //
                .window(tempBoundary)
                //
                // Group by key - same bucket, same org
                //
                .concatMap(it -> it.groupBy(batchWrite -> batchWrite.batchWriteOptions))
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
                .compose(jitter(processorScheduler))
                //
                // To WritePoints "request creator"
                //
                .concatMapMaybe(new ToWritePointsMaybe(processorScheduler))
                .subscribe(responseNotification -> {

                    if (responseNotification.isOnError()) {
                        publish(new WriteErrorEvent(toInfluxException(responseNotification.getError())));
                    }
                }, throwable -> new WriteErrorEvent(toInfluxException(throwable)));

        boundary.subscribe(tempBoundary);
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

        LOG.log(Level.INFO, "Flushing any cached BatchWrites before shutdown.");

        processor.onComplete();
        eventPublisher.onComplete();
        flushPublisher.onComplete();
    }

    public void write(@Nonnull final String bucket,
                      @Nonnull final String organization,
                      @Nonnull final Flowable<BatchWriteDataPoint> stream) {

        stream.subscribe(
                dataPoint -> write(bucket, organization, dataPoint.point.getPrecision(), Flowable.just(dataPoint)),
                throwable -> publish(new WriteErrorEvent(throwable)));
    }

    public void write(@Nonnull final String bucket,
                      @Nonnull final String organization,
                      @Nonnull final WritePrecision precision,
                      @Nonnull final Publisher<AbstractWriteClient.BatchWriteData> stream) {

        Arguments.checkNonEmpty(bucket, "bucket");
        Arguments.checkNonEmpty(organization, "organization");
        Arguments.checkNotNull(stream, "data to write");

        BatchWriteOptions batchWriteOptions = new BatchWriteOptions(bucket, organization, precision);

        Flowable.fromPublisher(stream)
                .map(it -> new BatchWriteItem(batchWriteOptions, it))
                .subscribe(processor::onNext, throwable -> publish(new WriteErrorEvent(throwable)));
    }

    @Nonnull
    private FlowableTransformer<BatchWriteItem, BatchWriteItem> jitter(@Nonnull final Scheduler scheduler) {

        Arguments.checkNotNull(scheduler, "Jitter scheduler is required");

        return source -> {

            //
            // source without jitter
            //
            if (writeOptions.getJitterInterval() <= 0) {
                return source;
            }

            //
            // Add jitter => dynamic delay
            //
            return source.delay((Function<BatchWriteItem, Flowable<Long>>) pointFlowable -> {

                int delay = jitterDelay();

                LOG.log(Level.FINEST, "Generated Jitter dynamic delay: {0}", delay);

                return Flowable.timer(delay, TimeUnit.MILLISECONDS, scheduler);
            });
        };
    }

    private int jitterDelay() {

        return (int) (Math.random() * writeOptions.getJitterInterval());
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
        private final InfluxDBClientOptions options;

        public BatchWriteDataPoint(@Nonnull final Point point,
                                   @Nonnull final InfluxDBClientOptions options) {

            this.point = point;
            this.options = options;
        }

        @Nullable
        @Override
        public String toLineProtocol() {

            if (!point.hasFields()) {

                LOG.warning("The point: " + point + "doesn't contains any fields, skipping");

                return null;
            }

            return point.toLineProtocol(options.getPointSettings());
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

        private BatchWriteOptions batchWriteOptions;
        private BatchWriteData data;

        private BatchWriteItem(@Nonnull final BatchWriteOptions batchWriteOptions,
                               @Nonnull final BatchWriteData data) {

            Arguments.checkNotNull(batchWriteOptions, "data");
            Arguments.checkNotNull(data, "write options");

            this.batchWriteOptions = batchWriteOptions;
            this.data = data;
        }
    }

    /**
     * The options to apply to a @{@link BatchWriteItem}.
     */
    private final class BatchWriteOptions {

        private String bucket;
        private String organization;
        private WritePrecision precision;

        private BatchWriteOptions(@Nonnull final String bucket,
                                  @Nonnull final String organization,
                                  @Nonnull final WritePrecision precision) {

            Arguments.checkNonEmpty(bucket, "bucket");
            Arguments.checkNonEmpty(organization, "organization");
            Arguments.checkNotNull(precision, "TimeUnit.precision is required");

            this.bucket = bucket;
            this.organization = organization;
            this.precision = precision;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof BatchWriteOptions)) {
                return false;
            }
            BatchWriteOptions that = (BatchWriteOptions) o;
            return Objects.equals(bucket, that.bucket)
                    && Objects.equals(organization, that.organization)
                    && precision == that.precision;
        }

        @Override
        public int hashCode() {
            return Objects.hash(bucket, organization, precision);
        }
    }

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
            String organization = batchWrite.batchWriteOptions.organization;
            String bucket = batchWrite.batchWriteOptions.bucket;
            WritePrecision precision = batchWrite.batchWriteOptions.precision;

            Maybe<Response<Void>> requestSource = Maybe
                    .fromCallable(() -> service
                            .postWrite(organization, bucket, content, null,
                                    "identity", "text/plain; charset=utf-8", null,
                                    "application/json", null, precision))
                    .map(Call::execute);

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
                    .retryWhen(AbstractWriteClient.this.retryHandler(retryScheduler, writeOptions))
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
                    batchWrite.batchWriteOptions.organization,
                    batchWrite.batchWriteOptions.bucket,
                    batchWrite.batchWriteOptions.precision,
                    lineProtocol);
        }
    }

    private Function<Flowable<Throwable>, Publisher<?>> retryHandler(@Nonnull final Scheduler retryScheduler,
                                                                     @Nonnull final WriteOptions writeOptions) {

        Objects.requireNonNull(writeOptions, "WriteOptions are required");
        Objects.requireNonNull(retryScheduler, "RetryScheduler is required");

        return errors -> errors.flatMap(throwable -> {

            if (throwable instanceof HttpException) {

                HttpException ie = (HttpException) throwable;

                //
                // The type of error is not able to retry
                //
                if (!ABLE_TO_RETRY_ERRORS.contains(ie.code())) {

                    return Flowable.error(throwable);
                }

                //
                // Retry request
                //
                long retryInterval;

                String retryAfter = ((HttpException) throwable).response().headers().get("Retry-After");
                if (retryAfter != null) {

                    retryInterval = TimeUnit.MILLISECONDS.convert(Integer.parseInt(retryAfter), TimeUnit.SECONDS);
                } else {

                    retryInterval = writeOptions.getRetryInterval();

                    String msg = "The InfluxDB does not specify \"Retry-After\". Use the default retryInterval: {0}";
                    LOG.log(Level.FINEST, msg, retryInterval);
                }

                retryInterval = retryInterval + jitterDelay();

                publish(new WriteRetriableErrorEvent(throwable, retryInterval));

                return Flowable.just("notify").delay(retryInterval, TimeUnit.MILLISECONDS, retryScheduler);
            }

            //
            // This type of throwable is not able to retry
            //
            return Flowable.error(throwable);
        });
    }

    @Nonnull
    private InfluxException toInfluxException(@Nonnull final Throwable throwable) {

        if (throwable instanceof InfluxException) {
            return (InfluxException) throwable;
        }

        if (throwable instanceof HttpException) {
            return responseToError(((HttpException) throwable).response());
        }

        return new InfluxException(throwable);
    }
}