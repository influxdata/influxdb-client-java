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

import java.util.Objects;
import javax.annotation.Nonnull;

import com.influxdb.Arguments;
import com.influxdb.client.InfluxDBClientOptions;
import com.influxdb.client.WriteOptions;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.internal.AbstractWriteClient;
import com.influxdb.client.reactive.WriteReactiveApi;
import com.influxdb.client.service.WriteService;
import com.influxdb.client.write.Point;
import com.influxdb.client.write.events.AbstractWriteEvent;

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
                         @Nonnull final InfluxDBClientOptions options) {

        super(writeOptions, options, writeOptions.getWriteScheduler(), service);
    }

    @Override
    public void writeRecord(@Nonnull final String bucket,
                            @Nonnull final String orgID,
                            @Nonnull final WritePrecision precision,
                            @Nonnull final Maybe<String> record) {

        Arguments.checkNonEmpty(bucket, "bucket");
        Arguments.checkNonEmpty(orgID, "organization");
        Arguments.checkNotNull(precision, "precision");
        Arguments.checkNotNull(record, "record");

        writeRecords(bucket, orgID, precision, record.toFlowable());
    }

    @Override
    public void writeRecords(@Nonnull final String bucket,
                             @Nonnull final String orgID,
                             @Nonnull final WritePrecision precision,
                             @Nonnull final Flowable<String> records) {

        Arguments.checkNonEmpty(bucket, "bucket");
        Arguments.checkNonEmpty(orgID, "organization");
        Arguments.checkNotNull(precision, "precision");
        Arguments.checkNotNull(records, "records");

        Flowable<BatchWriteData> stream = records.map(BatchWriteDataRecord::new);

        write(bucket, orgID, precision, stream);
    }

    @Override
    public void writeRecords(@Nonnull final String bucket,
                             @Nonnull final String orgID,
                             @Nonnull final WritePrecision precision,
                             @Nonnull final Publisher<String> records) {

        Arguments.checkNonEmpty(bucket, "bucket");
        Arguments.checkNonEmpty(orgID, "organization");
        Arguments.checkNotNull(precision, "precision");
        Arguments.checkNotNull(records, "records");

        writeRecords(bucket, orgID, precision, Flowable.fromPublisher(records));
    }

    @Override
    public void writePoint(@Nonnull final String bucket,
                           @Nonnull final String orgID,
                           @Nonnull final Maybe<Point> point) {

        Arguments.checkNonEmpty(bucket, "bucket");
        Arguments.checkNonEmpty(orgID, "organization");
        Arguments.checkNotNull(point, "point");

        writePoints(bucket, orgID, point.toFlowable());
    }

    @Override
    public void writePoints(@Nonnull final String bucket,
                            @Nonnull final String orgID,
                            @Nonnull final Flowable<Point> points) {

        Arguments.checkNonEmpty(bucket, "bucket");
        Arguments.checkNonEmpty(orgID, "organization");
        Arguments.checkNotNull(points, "points");

        Flowable<BatchWriteDataPoint> stream = points.filter(Objects::nonNull)
                .map(point -> new BatchWriteDataPoint(point, options));

        write(bucket, orgID, stream);
    }

    @Override
    public void writePoints(@Nonnull final String bucket,
                            @Nonnull final String orgID,
                            @Nonnull final Publisher<Point> points) {

        Arguments.checkNonEmpty(bucket, "bucket");
        Arguments.checkNonEmpty(orgID, "organization");
        Arguments.checkNotNull(points, "points");

        writePoints(bucket, orgID, Flowable.fromPublisher(points));
    }

    @Override
    public <M> void writeMeasurement(@Nonnull final String bucket,
                                     @Nonnull final String orgID,
                                     @Nonnull final WritePrecision precision,
                                     @Nonnull final Maybe<M> measurement) {

        Arguments.checkNonEmpty(bucket, "bucket");
        Arguments.checkNonEmpty(orgID, "organization");
        Arguments.checkNotNull(precision, "precision");
        Arguments.checkNotNull(measurement, "measurement");

        writeMeasurements(bucket, orgID, precision, measurement.toFlowable());
    }

    @Override
    public <M> void writeMeasurements(@Nonnull final String bucket,
                                      @Nonnull final String orgID,
                                      @Nonnull final WritePrecision precision,
                                      @Nonnull final Flowable<M> measurements) {

        Arguments.checkNonEmpty(bucket, "bucket");
        Arguments.checkNonEmpty(orgID, "organization");
        Arguments.checkNotNull(precision, "precision");
        Arguments.checkNotNull(measurements, "measurements");

        Flowable<BatchWriteData> stream = measurements
                .map(it -> new BatchWriteDataMeasurement(it, precision, options, measurementMapper));

        write(bucket, orgID, precision, stream);
    }

    @Override
    public <M> void writeMeasurements(@Nonnull final String bucket,
                                      @Nonnull final String orgID,
                                      @Nonnull final WritePrecision precision,
                                      @Nonnull final Publisher<M> measurements) {

        Arguments.checkNonEmpty(bucket, "bucket");
        Arguments.checkNonEmpty(orgID, "organization");
        Arguments.checkNotNull(precision, "precision");
        Arguments.checkNotNull(measurements, "measurements");

        writeMeasurements(bucket, orgID, precision, Flowable.fromPublisher(measurements));
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