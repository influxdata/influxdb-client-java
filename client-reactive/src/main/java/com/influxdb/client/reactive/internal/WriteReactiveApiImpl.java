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

import java.text.MessageFormat;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.influxdb.client.InfluxDBClientOptions;
import com.influxdb.client.domain.WriteConsistency;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.internal.AbstractWriteClient;
import com.influxdb.client.internal.AbstractWriteClient.BatchWriteData;
import com.influxdb.client.internal.AbstractWriteClient.BatchWriteDataMeasurement;
import com.influxdb.client.internal.AbstractWriteClient.BatchWriteDataPoint;
import com.influxdb.client.internal.AbstractWriteClient.BatchWriteDataRecord;
import com.influxdb.client.internal.MeasurementMapper;
import com.influxdb.client.reactive.WriteOptionsReactive;
import com.influxdb.client.reactive.WriteReactiveApi;
import com.influxdb.client.service.WriteService;
import com.influxdb.client.write.Point;
import com.influxdb.client.write.WriteParameters;
import com.influxdb.internal.AbstractRestClient;
import com.influxdb.utils.Arguments;

import io.reactivex.rxjava3.core.Flowable;
import org.reactivestreams.Publisher;
import retrofit2.HttpException;

/**
 * @author Jakub Bednar (bednar@github) (22/11/2018 06:50)
 */
public class WriteReactiveApiImpl extends AbstractRestClient implements WriteReactiveApi {

    private static final Logger LOG = Logger.getLogger(WriteReactiveApi.class.getName());

    private final WriteOptionsReactive writeOptions;
    private final InfluxDBClientOptions options;
    private final WriteService service;
    private final MeasurementMapper measurementMapper = new MeasurementMapper();

    WriteReactiveApiImpl(@Nonnull final WriteOptionsReactive writeOptions,
                         @Nonnull final WriteService service,
                         @Nonnull final InfluxDBClientOptions options) {

        this.writeOptions = writeOptions;
        this.options = options;
        this.service = service;
    }

    @Override
    public Publisher<Success> writeRecord(@Nonnull final WritePrecision precision, @Nullable final String record) {

        Arguments.checkNotNull(options.getBucket(), "InfluxDBClientOptions.getBucket");
        Arguments.checkNotNull(options.getOrg(), "InfluxDBClientOptions.getOrg");

        return writeRecord(options.getBucket(), options.getOrg(), precision, record);
    }

    @Override
    public Publisher<Success> writeRecord(@Nonnull final String bucket,
                                          @Nonnull final String org,
                                          @Nonnull final WritePrecision precision,
                                          @Nullable final String record) {

        Arguments.checkNonEmpty(bucket, "bucket");
        Arguments.checkNonEmpty(org, "organization");
        Arguments.checkNotNull(precision, "precision");

        if (record == null) {
            LOG.log(Level.FINE, "The record is null for bucket: ''{0}'', org: ''{1}'' and precision: ''{2}''.",
                    new Object[]{bucket, org, precision});

            return Flowable.just(new Success());
        }

        return writeRecords(bucket, org, precision, Flowable.just(record));
    }

    @Override
    public Publisher<Success> writeRecords(@Nonnull final WritePrecision precision,
                                           @Nonnull final Publisher<String> records) {

        Arguments.checkNotNull(options.getBucket(), "InfluxDBClientOptions.getBucket");
        Arguments.checkNotNull(options.getOrg(), "InfluxDBClientOptions.getOrg");

        return writeRecords(options.getBucket(), options.getOrg(), precision, records);
    }

    @Override
    public Publisher<Success> writeRecords(@Nonnull final String bucket,
                                           @Nonnull final String org,
                                           @Nonnull final WritePrecision precision,
                                           @Nonnull final Publisher<String> records) {

        Arguments.checkNonEmpty(bucket, "bucket");
        Arguments.checkNonEmpty(org, "organization");
        Arguments.checkNotNull(precision, "precision");
        Arguments.checkNotNull(records, "records");

        Flowable<BatchWriteData> stream = Flowable.fromPublisher(records).map(BatchWriteDataRecord::new);

        return write(new WriteParameters(bucket, org, precision), stream);
    }

    @Override
    public Publisher<Success> writePoint(@Nonnull final WritePrecision precision, @Nonnull final Point point) {

        Arguments.checkNotNull(precision, "precision");
        Arguments.checkNotNull(options.getBucket(), "InfluxDBClientOptions.getBucket");
        Arguments.checkNotNull(options.getOrg(), "InfluxDBClientOptions.getOrg");

        return writePoint(options.getBucket(), options.getOrg(), precision, point);
    }

    @Override
    public Publisher<Success> writePoint(@Nonnull final String bucket,
                                         @Nonnull final String org,
                                         @Nonnull final WritePrecision precision,
                                         @Nonnull final Point point) {

        Arguments.checkNonEmpty(bucket, "bucket");
        Arguments.checkNonEmpty(org, "organization");
        Arguments.checkNotNull(precision, "precision");
        Arguments.checkNotNull(point, "point");

        return writePoints(bucket, org, precision, Flowable.just(point));
    }

    @Override
    public Publisher<Success> writePoints(@Nonnull final WritePrecision precision,
                                          @Nonnull final Publisher<Point> points) {

        Arguments.checkNotNull(options.getBucket(), "InfluxDBClientOptions.getBucket");
        Arguments.checkNotNull(options.getOrg(), "InfluxDBClientOptions.getOrg");

        return writePoints(options.getBucket(), options.getOrg(), precision, points);
    }

    @Override
    public Publisher<Success> writePoints(@Nonnull final String bucket,
                                          @Nonnull final String org,
                                          @Nonnull final WritePrecision precision,
                                          @Nonnull final Publisher<Point> points) {

        Arguments.checkNonEmpty(bucket, "bucket");
        Arguments.checkNonEmpty(org, "organization");
        Arguments.checkNotNull(points, "points");

        Flowable<BatchWriteData> stream = Flowable
                .fromPublisher(points)
                .filter(Objects::nonNull)
                .map(point -> new BatchWriteDataPoint(point, precision, options));

        return write(new WriteParameters(bucket, org, precision), stream);
    }

    @Override
    public <M> Publisher<Success> writeMeasurement(@Nonnull final WritePrecision precision,
                                                   @Nonnull final M measurement) {

        Arguments.checkNotNull(options.getBucket(), "InfluxDBClientOptions.getBucket");
        Arguments.checkNotNull(options.getOrg(), "InfluxDBClientOptions.getOrg");

        return writeMeasurement(options.getBucket(), options.getOrg(), precision, measurement);
    }

    @Override
    public <M> Publisher<Success> writeMeasurement(@Nonnull final String bucket,
                                                   @Nonnull final String org,
                                                   @Nonnull final WritePrecision precision,
                                                   @Nonnull final M measurement) {

        Arguments.checkNonEmpty(bucket, "bucket");
        Arguments.checkNonEmpty(org, "organization");
        Arguments.checkNotNull(precision, "precision");
        Arguments.checkNotNull(measurement, "measurement");

        return writeMeasurements(bucket, org, precision, Flowable.just(measurement));
    }

    @Override
    public <M> Publisher<Success> writeMeasurements(@Nonnull final WritePrecision precision,
                                                    @Nonnull final Publisher<M> measurements) {

        Arguments.checkNotNull(options.getBucket(), "InfluxDBClientOptions.getBucket");
        Arguments.checkNotNull(options.getOrg(), "InfluxDBClientOptions.getOrg");

        return writeMeasurements(options.getBucket(), options.getOrg(), precision, measurements);
    }

    @Override
    public <M> Publisher<Success> writeMeasurements(@Nonnull final String bucket,
                                                    @Nonnull final String org,
                                                    @Nonnull final WritePrecision precision,
                                                    @Nonnull final Publisher<M> measurements) {

        Arguments.checkNonEmpty(bucket, "bucket");
        Arguments.checkNonEmpty(org, "organization");
        Arguments.checkNotNull(precision, "precision");
        Arguments.checkNotNull(measurements, "measurements");

        Flowable<BatchWriteData> stream = Flowable.fromPublisher(measurements)
                .map(it -> new BatchWriteDataMeasurement(it, precision, options, measurementMapper));

        return write(new WriteParameters(bucket, org, precision), stream);
    }

    @Nonnull
    @SuppressWarnings("MagicNumber")
    private Publisher<Success> write(@Nonnull final WriteParameters parameters,
                                     @Nonnull final Flowable<BatchWriteData> stream) {

        Arguments.checkNotNull(parameters, "parameters");
        Arguments.checkNotNull(stream, "stream");

        Flowable<String> batches = stream
                //
                // Create batches
                //
                .compose(source -> {
                    //
                    // disabled batches
                    //
                    if (writeOptions.getBatchSize() == 0) {
                        return Flowable.just(Flowable.fromPublisher(stream));
                    }

                    //
                    // batching
                    //
                    return source.window(writeOptions.getFlushInterval(),
                            TimeUnit.MILLISECONDS,
                            writeOptions.getComputationScheduler(),
                            writeOptions.getBatchSize(),
                            true);
                })
                //
                // Collect batch to LineProtocol concatenated by '\n'
                //
                .concatMapSingle(batch -> batch
                        .map(item -> {
                            String lineProtocol = item.toLineProtocol();
                            if (lineProtocol == null) {
                                return "";
                            }
                            return lineProtocol;
                        })
                        .filter(it -> !it.isEmpty())
                        .collect(StringBuilder::new, (sb, x) -> {
                            if (sb.length() > 0) {
                                sb.append("\n");
                            }
                            sb.append(x);
                        })
                        .map(StringBuilder::toString))
                // Filter empty batches
                .filter(it -> !it.isEmpty());

        return batches
                //
                // Jitter
                //
                .compose(AbstractWriteClient.jitter(writeOptions.getComputationScheduler(), writeOptions))
                //
                // HTTP post
                //
                .flatMapSingle(it -> {
                            String organization = parameters.orgSafe(options);
                            String bucket = parameters.bucketSafe(options);
                            WritePrecision precision = parameters.precisionSafe(options);
                            WriteConsistency consistency = parameters.consistencySafe(options);

                            return service.postWriteRx(organization, bucket, it, null,
                                    "identity", "text/plain; charset=utf-8", null,
                                    "application/json", null, precision, consistency);
                        }
                )
                //
                // Map to Success
                //
                .flatMap(response -> {
                    if (!response.isSuccessful()) {
                        return Flowable.error(new HttpException(response));
                    }

                    return Flowable.just(new Success());
                })
                //
                // Retry
                //
                .retryWhen(AbstractWriteClient.retry(
                        writeOptions.getComputationScheduler(),
                        writeOptions,
                        (throwable, retryInterval) -> {
                            String msg = MessageFormat.format(
                                    "The retriable error occurred during writing of data. Retry in: {0}s.",
                                    (double) retryInterval / 1000);
                            LOG.log(Level.WARNING, msg, throwable);
                        }))
                //
                // maxRetryTime timeout
                //
                .timeout(writeOptions.getMaxRetryTime(),
                        TimeUnit.MILLISECONDS,
                        writeOptions.getComputationScheduler(),
                        Flowable.error(new TimeoutException("Max retry time exceeded.")))
                //
                // Map to Influx Error
                //
                .onErrorResumeNext(throwable -> {
                    return Flowable.error(toInfluxException(throwable));
                });
    }
}