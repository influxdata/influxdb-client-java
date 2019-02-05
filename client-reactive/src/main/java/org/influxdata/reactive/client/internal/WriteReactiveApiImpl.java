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
package org.influxdata.reactive.client.internal;

import java.time.temporal.ChronoUnit;
import java.util.Objects;
import javax.annotation.Nonnull;

import org.influxdata.client.Arguments;
import org.influxdata.java.client.WriteOptions;
import org.influxdata.java.client.internal.AbstractWriteClient;
import org.influxdata.java.client.writes.Point;
import org.influxdata.java.client.writes.events.AbstractWriteEvent;
import org.influxdata.reactive.client.WriteReactiveApi;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.RequestBody;
import org.reactivestreams.Publisher;

/**
 * @author Jakub Bednar (bednar@github) (22/11/2018 06:50)
 */
public class WriteReactiveApiImpl extends AbstractWriteClient implements WriteReactiveApi {

    private final InfluxDBReactiveService influxDBService;

    WriteReactiveApiImpl(@Nonnull final WriteOptions writeOptions,
                         @Nonnull final InfluxDBReactiveService influxDBService) {

        super(writeOptions,
                Schedulers.newThread(),
                Schedulers.trampoline());

        this.influxDBService = influxDBService;
    }

    @Override
    public void writeRecord(@Nonnull final String bucket,
                            @Nonnull final String orgID,
                            @Nonnull final ChronoUnit precision,
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
                             @Nonnull final ChronoUnit precision,
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
                             @Nonnull final ChronoUnit precision,
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

        Flowable<BatchWriteDataPoint> stream = points.filter(Objects::nonNull).map(BatchWriteDataPoint::new);

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
                                     @Nonnull final ChronoUnit precision,
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
                                      @Nonnull final ChronoUnit precision,
                                      @Nonnull final Flowable<M> measurements) {

        Arguments.checkNonEmpty(bucket, "bucket");
        Arguments.checkNonEmpty(orgID, "organization");
        Arguments.checkNotNull(precision, "precision");
        Arguments.checkNotNull(measurements, "measurements");

        Flowable<BatchWriteData> stream = measurements.map(it -> new BatchWriteDataMeasurement(it, precision));

        write(bucket, orgID, precision, stream);
    }

    @Override
    public <M> void writeMeasurements(@Nonnull final String bucket,
                                      @Nonnull final String orgID,
                                      @Nonnull final ChronoUnit precision,
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

    @Override
    public Completable writeCall(final RequestBody requestBody,
                                 final String organization,
                                 final String bucket,
                                 final String precision) {
        return influxDBService.writePoints(organization, bucket, precision, requestBody);
    }
}