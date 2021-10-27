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

import java.util.Collection;
import java.util.Objects;
import javax.annotation.Nonnull;

import com.influxdb.client.InfluxDBClientOptions;
import com.influxdb.client.WriteOptions;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.internal.AbstractWriteClient;
import com.influxdb.client.reactive.WriteReactiveApi;
import com.influxdb.client.service.WriteService;
import com.influxdb.client.write.Point;
import com.influxdb.client.write.events.AbstractWriteEvent;
import com.influxdb.utils.Arguments;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import org.reactivestreams.Publisher;

/**
 * @author Jakub Bednar (bednar@github) (22/11/2018 06:50)
 */
public class WriteReactiveApiImpl extends AbstractWriteClient implements WriteReactiveApi {

    WriteReactiveApiImpl(@Nonnull final WriteOptions writeOptions,
                         @Nonnull final WriteService service,
                         @Nonnull final InfluxDBClientOptions options, final Collection<AutoCloseable> autoCloseables) {

        super(writeOptions, options, writeOptions.getWriteScheduler(), service, autoCloseables);
    }

    @Override
    public void writeRecord(@Nonnull final WritePrecision precision, @Nonnull final Maybe<String> record) {

        Arguments.checkNotNull(options.getBucket(), "InfluxDBClientOptions.getBucket");
        Arguments.checkNotNull(options.getOrg(), "InfluxDBClientOptions.getOrg");

        writeRecord(options.getBucket(), options.getOrg(), precision, record);
    }

    @Override
    public void writeRecord(@Nonnull final String bucket,
                            @Nonnull final String org,
                            @Nonnull final WritePrecision precision,
                            @Nonnull final Maybe<String> record) {

        Arguments.checkNonEmpty(bucket, "bucket");
        Arguments.checkNonEmpty(org, "organization");
        Arguments.checkNotNull(precision, "precision");
        Arguments.checkNotNull(record, "record");

        writeRecords(bucket, org, precision, record.toFlowable());
    }

    @Override
    public void writeRecords(@Nonnull final WritePrecision precision, @Nonnull final Flowable<String> records) {

        Arguments.checkNotNull(options.getBucket(), "InfluxDBClientOptions.getBucket");
        Arguments.checkNotNull(options.getOrg(), "InfluxDBClientOptions.getOrg");

        writeRecords(options.getBucket(), options.getOrg(), precision, records);
    }

    @Override
    public void writeRecords(@Nonnull final String bucket,
                             @Nonnull final String org,
                             @Nonnull final WritePrecision precision,
                             @Nonnull final Flowable<String> records) {

        Arguments.checkNonEmpty(bucket, "bucket");
        Arguments.checkNonEmpty(org, "organization");
        Arguments.checkNotNull(precision, "precision");
        Arguments.checkNotNull(records, "records");

        Flowable<BatchWriteData> stream = records.map(BatchWriteDataRecord::new);

        write(bucket, org, precision, stream);
    }

    @Override
    public void writeRecords(@Nonnull final WritePrecision precision, @Nonnull final Publisher<String> records) {

        Arguments.checkNotNull(options.getBucket(), "InfluxDBClientOptions.getBucket");
        Arguments.checkNotNull(options.getOrg(), "InfluxDBClientOptions.getOrg");

        writeRecords(options.getBucket(), options.getOrg(), precision, records);
    }

    @Override
    public void writeRecords(@Nonnull final String bucket,
                             @Nonnull final String org,
                             @Nonnull final WritePrecision precision,
                             @Nonnull final Publisher<String> records) {

        Arguments.checkNonEmpty(bucket, "bucket");
        Arguments.checkNonEmpty(org, "organization");
        Arguments.checkNotNull(precision, "precision");
        Arguments.checkNotNull(records, "records");

        writeRecords(bucket, org, precision, Flowable.fromPublisher(records));
    }

    @Override
    public void writePoint(@Nonnull final Maybe<Point> point) {

        Arguments.checkNotNull(options.getBucket(), "InfluxDBClientOptions.getBucket");
        Arguments.checkNotNull(options.getOrg(), "InfluxDBClientOptions.getOrg");

        writePoint(options.getBucket(), options.getOrg(), point);
    }

    @Override
    public void writePoint(@Nonnull final String bucket,
                           @Nonnull final String org,
                           @Nonnull final Maybe<Point> point) {

        Arguments.checkNonEmpty(bucket, "bucket");
        Arguments.checkNonEmpty(org, "organization");
        Arguments.checkNotNull(point, "point");

        writePoints(bucket, org, point.toFlowable());
    }

    @Override
    public void writePoints(@Nonnull final Flowable<Point> points) {

        Arguments.checkNotNull(options.getBucket(), "InfluxDBClientOptions.getBucket");
        Arguments.checkNotNull(options.getOrg(), "InfluxDBClientOptions.getOrg");

        writePoints(options.getBucket(), options.getOrg(), points);
    }

    @Override
    public void writePoints(@Nonnull final String bucket,
                            @Nonnull final String org,
                            @Nonnull final Flowable<Point> points) {

        Arguments.checkNonEmpty(bucket, "bucket");
        Arguments.checkNonEmpty(org, "organization");
        Arguments.checkNotNull(points, "points");

        Flowable<BatchWriteDataPoint> stream = points.filter(Objects::nonNull)
                .map(point -> new BatchWriteDataPoint(point, options));

        write(bucket, org, stream);
    }

    @Override
    public void writePoints(@Nonnull final Publisher<Point> points) {

        Arguments.checkNotNull(options.getBucket(), "InfluxDBClientOptions.getBucket");
        Arguments.checkNotNull(options.getOrg(), "InfluxDBClientOptions.getOrg");

        writePoints(options.getBucket(), options.getOrg(), points);
    }

    @Override
    public void writePoints(@Nonnull final String bucket,
                            @Nonnull final String org,
                            @Nonnull final Publisher<Point> points) {

        Arguments.checkNonEmpty(bucket, "bucket");
        Arguments.checkNonEmpty(org, "organization");
        Arguments.checkNotNull(points, "points");

        writePoints(bucket, org, Flowable.fromPublisher(points));
    }

    @Override
    public <M> void writeMeasurement(@Nonnull final WritePrecision precision, @Nonnull final Maybe<M> measurement) {

        Arguments.checkNotNull(options.getBucket(), "InfluxDBClientOptions.getBucket");
        Arguments.checkNotNull(options.getOrg(), "InfluxDBClientOptions.getOrg");

        writeMeasurement(options.getBucket(), options.getOrg(), precision, measurement);
    }

    @Override
    public <M> void writeMeasurement(@Nonnull final String bucket,
                                     @Nonnull final String org,
                                     @Nonnull final WritePrecision precision,
                                     @Nonnull final Maybe<M> measurement) {

        Arguments.checkNonEmpty(bucket, "bucket");
        Arguments.checkNonEmpty(org, "organization");
        Arguments.checkNotNull(precision, "precision");
        Arguments.checkNotNull(measurement, "measurement");

        writeMeasurements(bucket, org, precision, measurement.toFlowable());
    }

    @Override
    public <M> void writeMeasurements(@Nonnull final WritePrecision precision,
                                      @Nonnull final Flowable<M> measurements) {

        Arguments.checkNotNull(options.getBucket(), "InfluxDBClientOptions.getBucket");
        Arguments.checkNotNull(options.getOrg(), "InfluxDBClientOptions.getOrg");

        writeMeasurements(options.getBucket(), options.getOrg(), precision, measurements);
    }

    @Override
    public <M> void writeMeasurements(@Nonnull final String bucket,
                                      @Nonnull final String org,
                                      @Nonnull final WritePrecision precision,
                                      @Nonnull final Flowable<M> measurements) {

        Arguments.checkNonEmpty(bucket, "bucket");
        Arguments.checkNonEmpty(org, "organization");
        Arguments.checkNotNull(precision, "precision");
        Arguments.checkNotNull(measurements, "measurements");

        Flowable<BatchWriteData> stream = measurements
                .map(it -> new BatchWriteDataMeasurement(it, precision, options, measurementMapper));

        write(bucket, org, precision, stream);
    }

    @Override
    public <M> void writeMeasurements(@Nonnull final WritePrecision precision,
                                      @Nonnull final Publisher<M> measurements) {

        Arguments.checkNotNull(options.getBucket(), "InfluxDBClientOptions.getBucket");
        Arguments.checkNotNull(options.getOrg(), "InfluxDBClientOptions.getOrg");

        writeMeasurements(options.getBucket(), options.getOrg(), precision, measurements);
    }

    @Override
    public <M> void writeMeasurements(@Nonnull final String bucket,
                                      @Nonnull final String org,
                                      @Nonnull final WritePrecision precision,
                                      @Nonnull final Publisher<M> measurements) {

        Arguments.checkNonEmpty(bucket, "bucket");
        Arguments.checkNonEmpty(org, "organization");
        Arguments.checkNotNull(precision, "precision");
        Arguments.checkNotNull(measurements, "measurements");

        writeMeasurements(bucket, org, precision, Flowable.fromPublisher(measurements));
    }

    @Nonnull
    @Override
    public <T extends AbstractWriteEvent> Observable<T> listenEvents(@Nonnull final Class<T> eventType) {
        return super.addEventListener(eventType);
    }

    @Override
    public void close() {
        super.close();
    }
}