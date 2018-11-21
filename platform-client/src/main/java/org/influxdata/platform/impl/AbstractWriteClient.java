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

import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.influxdata.platform.Arguments;
import org.influxdata.platform.error.InfluxException;
import org.influxdata.platform.option.WriteOptions;
import org.influxdata.platform.rest.AbstractRestClient;
import org.influxdata.platform.write.Point;
import org.influxdata.platform.write.event.AbstractWriteEvent;
import org.influxdata.platform.write.event.BackpressureEvent;
import org.influxdata.platform.write.event.WriteErrorEvent;
import org.influxdata.platform.write.event.WriteSuccessEvent;

import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.functions.Function;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.subjects.PublishSubject;
import okhttp3.RequestBody;
import org.reactivestreams.Publisher;

/**
 * @author Jakub Bednar (bednar@github) (21/11/2018 09:26)
 */
abstract class AbstractWriteClient extends AbstractRestClient {

    private static final Logger LOG = Logger.getLogger(AbstractWriteClient.class.getName());

    private final WriteOptions writeOptions;

    private final PublishProcessor<AbstractWriteClient.BatchWriteItem> processor;
    private final PublishSubject<AbstractWriteEvent> eventPublisher;

    private final MeasurementMapper measurementMapper = new MeasurementMapper();

    AbstractWriteClient(@Nonnull final WriteOptions writeOptions,
                        @Nonnull final Scheduler processorScheduler,
                        @Nonnull final Scheduler batchScheduler,
                        @Nonnull final Scheduler jitterScheduler,
                        @Nonnull final Scheduler retryScheduler) {

        this.writeOptions = writeOptions;

        this.eventPublisher = PublishSubject.create();
        this.processor = PublishProcessor.create();
        this.processor
                //
                // Backpressure
                //
                .onBackpressureBuffer(
                        writeOptions.getBufferLimit(),
                        () -> publish(new BackpressureEvent()),
                        writeOptions.getBackpressureStrategy())
                .observeOn(processorScheduler)
                //
                // Batching
                //
                .window(writeOptions.getFlushInterval(),
                        TimeUnit.MILLISECONDS,
                        batchScheduler,
                        writeOptions.getBatchSize(),
                        true)
                //
                // Group by key - same bucket, same org
                //
                .concatMap(it -> it.groupBy(batchWrite -> batchWrite.batchWriteOptions))
                //
                // Create Write Point = bucket, org, ... + data
                //
                .concatMapSingle(grouped -> grouped

                        //
                        // Create Line Protocol
                        //
                        .reduce("", (lineProtocol, batchWrite) -> {

                            String data = null;
                            try {
                                data = batchWrite.data.toLineProtocol();
                            } catch (Exception e) {
                                publish(new WriteErrorEvent(e));
                            }

                            if (data == null || data.isEmpty()) {
                                return lineProtocol;
                            }

                            if (lineProtocol.isEmpty()) {
                                return data;
                            }
                            return String.join("\n", lineProtocol, data);
                        })
                        //
                        // "Group" with bucket, org, ...
                        //
                        .map(records -> new BatchWriteItem(grouped.getKey(), new BatchWriteDataRecord(records))))
                //
                // Jitter interval
                //
                .compose(jitter(jitterScheduler))
                //
                // To WritePoints "request creator"
                //
                .concatMapCompletable(new ToWritePointsCompletable(retryScheduler))
                //
                // Publish Error event
                //
                .subscribe(
                        () -> LOG.log(Level.FINEST, "Finished batch write."),
                        throwable -> publish(new WriteErrorEvent(new InfluxException(throwable))));
    }

    @Nonnull
    <T extends AbstractWriteEvent> Observable<T> listenEvents(@Nonnull final Class<T> eventType) {

        Objects.requireNonNull(eventType, "EventType is required");

        return eventPublisher.ofType(eventType);
    }

    void close() {

        LOG.log(Level.INFO, "Flushing any cached BatchWrites before shutdown.");

        processor.onComplete();
        eventPublisher.onComplete();
    }

    void write(@Nonnull final String bucket,
               @Nonnull final String organization,
               @Nonnull final ChronoUnit precision,
               @Nonnull final Publisher<AbstractWriteClient.BatchWriteData> stream) {

        Arguments.checkNonEmpty(bucket, "bucket");
        Arguments.checkNonEmpty(organization, "organization");
        Arguments.checkPrecision(precision);
        Arguments.checkNotNull(stream, "data to write");

        BatchWriteOptions batchWriteOptions = new BatchWriteOptions(bucket, organization, precision);

        Flowable.fromPublisher(stream)
                .map(it -> new BatchWriteItem(batchWriteOptions, it))
                .subscribe(processor::onNext, throwable -> publish(new WriteErrorEvent(throwable)));
    }

    abstract Completable writeCall(final RequestBody requestBody,
                                   final String organization,
                                   final String bucket,
                                   final String precision);

    @Nonnull
    private String toPrecisionParameter(@Nonnull final ChronoUnit precision) {

        switch (precision) {
            case NANOS:
                return "n";
            case MICROS:
                return "u";
            case MILLIS:
                return "ms";
            case SECONDS:
                return "s";
            default:
                throw new IllegalArgumentException("Not supported precision: " + precision);
        }
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

    interface BatchWriteData {

        @Nullable
        String toLineProtocol();
    }

    final class BatchWriteDataRecord implements BatchWriteData {

        private final String record;

        BatchWriteDataRecord(@Nullable final String record) {
            this.record = record;
        }

        @Nullable
        @Override
        public String toLineProtocol() {
            return record;
        }
    }

    final class BatchWriteDataPoint implements BatchWriteData {

        private final Point point;

        BatchWriteDataPoint(@Nullable final Point point) {
            this.point = point;
        }

        @Nullable
        @Override
        public String toLineProtocol() {

            if (point == null) {
                return null;
            }

            return point.toString();
        }
    }

    final class BatchWriteDataMeasurement implements BatchWriteData {

        private final Object measurement;
        private final ChronoUnit precision;

        BatchWriteDataMeasurement(@Nullable final Object measurement,
                                  @Nonnull final ChronoUnit precision) {
            this.measurement = measurement;
            this.precision = precision;
        }

        @Nullable
        @Override
        public String toLineProtocol() {

            if (measurement == null) {
                return null;
            }

            return measurementMapper.toPoint(measurement, precision).toString();
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
        private ChronoUnit precision;

        private BatchWriteOptions(@Nonnull final String bucket,
                                  @Nonnull final String organization,
                                  @Nonnull final ChronoUnit precision) {

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

    private final class ToWritePointsCompletable implements Function<BatchWriteItem, CompletableSource> {

        // TODO implement retry, delete retry scheduler?
        private final Scheduler retryScheduler;

        private ToWritePointsCompletable(@Nonnull final Scheduler retryScheduler) {
            this.retryScheduler = retryScheduler;
        }

        @Override
        public CompletableSource apply(final BatchWriteItem batchWrite) {

            String content = batchWrite.data.toLineProtocol();

            if (content == null || content.isEmpty()) {
                return Completable.complete();
            }

            //
            // InfluxDB Line Protocol => to Request Body
            //
            RequestBody requestBody = createBody(content);

            //
            // Parameters
            String organization = batchWrite.batchWriteOptions.organization;
            String bucket = batchWrite.batchWriteOptions.bucket;
            String precision = AbstractWriteClient.this.toPrecisionParameter(batchWrite.batchWriteOptions.precision);

            return writeCall(requestBody, organization, bucket, precision)
                    .doOnComplete(() -> publish(toSuccessEvent(batchWrite, content)));
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
}