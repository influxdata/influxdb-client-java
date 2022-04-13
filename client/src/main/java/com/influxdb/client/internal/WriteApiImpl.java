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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.influxdb.client.InfluxDBClientOptions;
import com.influxdb.client.WriteApi;
import com.influxdb.client.WriteOptions;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.service.WriteService;
import com.influxdb.client.write.Point;
import com.influxdb.client.write.WriteParameters;
import com.influxdb.client.write.events.AbstractWriteEvent;
import com.influxdb.client.write.events.EventListener;
import com.influxdb.client.write.events.ListenerRegistration;
import com.influxdb.utils.Arguments;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.Disposable;


/**
 * @author Jakub Bednar (bednar@github) (15/10/2018 09:42)
 */
final class WriteApiImpl extends AbstractWriteClient implements WriteApi {

    WriteApiImpl(@Nonnull final WriteOptions writeOptions,
                 @Nonnull final WriteService service,
                 @Nonnull final InfluxDBClientOptions options, final Collection<AutoCloseable> autoCloseables) {

        super(writeOptions, options, writeOptions.getWriteScheduler(), service, autoCloseables);
    }

    @Override
    public void writeRecord(@Nonnull final WritePrecision precision, @Nullable final String record) {

        Arguments.checkNotNull(options.getBucket(), "InfluxDBClientOptions.getBucket");
        Arguments.checkNotNull(options.getOrg(), "InfluxDBClientOptions.getOrg");

        writeRecord(options.getBucket(), options.getOrg(), precision, record);
    }

    @Override
    public void writeRecord(@Nonnull final String bucket,
                            @Nonnull final String org,
                            @Nonnull final WritePrecision precision,
                            @Nullable final String record) {

        Arguments.checkNonEmpty(bucket, "bucket");
        Arguments.checkNonEmpty(org, "org");
        Arguments.checkNotNull(precision, "WritePrecision is required");

        writeRecord(record, new WriteParameters(bucket, org, precision));
    }

    @Override
    public void writeRecord(@Nullable final String record, @Nonnull final WriteParameters parameters) {
        if (record == null) {
            return;
        }

        Arguments.checkNotNull(parameters, "WriteParameters");
        parameters.check(options);

        write(parameters, Flowable.just(new BatchWriteDataRecord(record)));
    }

    @Override
    public void writeRecords(@Nonnull final WritePrecision precision, @Nonnull final List<String> records) {

        Arguments.checkNotNull(options.getBucket(), "InfluxDBClientOptions.getBucket");
        Arguments.checkNotNull(options.getOrg(), "InfluxDBClientOptions.getOrg");

        writeRecords(options.getBucket(), options.getOrg(), precision, records);
    }

    @Override
    public void writeRecords(@Nonnull final String bucket,
                             @Nonnull final String org,
                             @Nonnull final WritePrecision precision,
                             @Nonnull final List<String> records) {

        Arguments.checkNonEmpty(bucket, "bucket");
        Arguments.checkNonEmpty(org, "org");
        Arguments.checkNotNull(precision, "WritePrecision is required");
        Arguments.checkNotNull(records, "records");

        writeRecords(records, new WriteParameters(bucket, org, precision));
    }

    @Override
    public void writeRecords(@Nonnull final List<String> records,
                             @Nonnull final WriteParameters parameters) {

        Arguments.checkNotNull(records, "records");
        Arguments.checkNotNull(parameters, "WriteParameters");
        parameters.check(options);

        Flowable<BatchWriteData> stream = Flowable.fromIterable(records).map(BatchWriteDataRecord::new);

        write(parameters, stream);
    }

    @Override
    public void writePoint(@Nullable final Point point) {

        Arguments.checkNotNull(options.getBucket(), "InfluxDBClientOptions.getBucket");
        Arguments.checkNotNull(options.getOrg(), "InfluxDBClientOptions.getOrg");

        writePoint(options.getBucket(), options.getOrg(), point);
    }

    @Override
    public void writePoint(@Nonnull final String bucket,
                           @Nonnull final String org,
                           @Nullable final Point point) {

        writePoint(point, new WriteParameters(bucket, org, WriteParameters.DEFAULT_WRITE_PRECISION));
    }

    @Override
    public void writePoint(@Nullable final Point point, @Nonnull final WriteParameters parameters) {

        if (point == null) {
            return;
        }

        Arguments.checkNotNull(parameters, "WriteParameters");
        parameters.check(options);

        writePoints(Collections.singletonList(point), parameters);
    }

    @Override
    public void writePoints(@Nonnull final List<Point> points) {

        Arguments.checkNotNull(options.getBucket(), "InfluxDBClientOptions.getBucket");
        Arguments.checkNotNull(options.getOrg(), "InfluxDBClientOptions.getOrg");

        writePoints(options.getBucket(), options.getOrg(), points);
    }

    @Override
    public void writePoints(@Nonnull final String bucket,
                            @Nonnull final String org,
                            @Nonnull final List<Point> points) {

        Arguments.checkNonEmpty(bucket, "bucket");
        Arguments.checkNonEmpty(org, "org");
        Arguments.checkNotNull(points, "points");

        writePoints(points, new WriteParameters(bucket, org, WriteParameters.DEFAULT_WRITE_PRECISION));
    }

    @Override
    public void writePoints(@Nonnull final List<Point> points, @Nonnull final WriteParameters parameters) {

        Arguments.checkNotNull(points, "points");
        Arguments.checkNotNull(parameters, "WriteParameters");
        parameters.check(options);

        Flowable<BatchWriteDataPoint> stream = Flowable.fromIterable(points).filter(Objects::nonNull)
                .map(point -> new BatchWriteDataPoint(point, options));

        writePoints(parameters, stream);
    }

    @Override
    public <M> void writeMeasurement(@Nonnull final WritePrecision precision, @Nullable final M measurement) {

        Arguments.checkNotNull(options.getBucket(), "InfluxDBClientOptions.getBucket");
        Arguments.checkNotNull(options.getOrg(), "InfluxDBClientOptions.getOrg");

        writeMeasurement(options.getBucket(), options.getOrg(), precision, measurement);
    }

    @Override
    public <M> void writeMeasurement(@Nonnull final String bucket,
                                     @Nonnull final String org,
                                     @Nonnull final WritePrecision precision,
                                     @Nullable final M measurement) {

        writeMeasurement(measurement, new WriteParameters(bucket, org, precision));
    }

    @Override
    public <M> void writeMeasurement(@Nullable final M measurement, @Nonnull final WriteParameters parameters) {
        if (measurement == null) {
            return;
        }

        Arguments.checkNotNull(parameters, "WriteParameters");
        parameters.check(options);

        writeMeasurements(Collections.singletonList(measurement), parameters);
    }

    @Override
    public <M> void writeMeasurements(@Nonnull final WritePrecision precision, @Nonnull final List<M> measurements) {

        Arguments.checkNotNull(options.getBucket(), "InfluxDBClientOptions.getBucket");
        Arguments.checkNotNull(options.getOrg(), "InfluxDBClientOptions.getOrg");

        writeMeasurements(options.getBucket(), options.getOrg(), precision, measurements);
    }

    @Override
    public <M> void writeMeasurements(@Nonnull final String bucket,
                                      @Nonnull final String org,
                                      @Nonnull final WritePrecision precision,
                                      @Nonnull final List<M> measurements) {

        Arguments.checkNonEmpty(bucket, "bucket");
        Arguments.checkNonEmpty(org, "org");
        Arguments.checkNotNull(precision, "WritePrecision is required");
        Arguments.checkNotNull(measurements, "records");

        writeMeasurements(measurements, new WriteParameters(bucket, org, precision));
    }

    @Override
    public <M> void writeMeasurements(@Nonnull final List<M> measurements, @Nonnull final WriteParameters parameters) {
        Arguments.checkNotNull(measurements, "points");
        Arguments.checkNotNull(parameters, "WriteParameters");
        parameters.check(options);

        Flowable<BatchWriteData> stream = Flowable
                .fromIterable(measurements)
                .map(it -> new BatchWriteDataMeasurement(it, parameters.precisionSafe(options), options,
                        measurementMapper));

        write(parameters, stream);
    }

    @Nonnull
    public <T extends AbstractWriteEvent> ListenerRegistration listenEvents(@Nonnull final Class<T> eventType,
                                                                            @Nonnull final EventListener<T> listener) {

        Arguments.checkNotNull(eventType, "Type of listener");
        Arguments.checkNotNull(listener, "Listener");

        Disposable subscribe = super.addEventListener(eventType).subscribe(listener::onEvent);

        return subscribe::dispose;
    }

    @Override
    public void close() {
        super.close();
    }
}