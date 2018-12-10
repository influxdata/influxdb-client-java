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
import javax.annotation.Nonnull;

import org.influxdata.platform.Arguments;
import org.influxdata.platform.WriteClientReactive;
import org.influxdata.platform.option.WriteOptions;
import org.influxdata.platform.write.Point;
import org.influxdata.platform.write.event.AbstractWriteEvent;

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
public class WriteClientReactiveImpl extends AbstractWriteClient implements WriteClientReactive {

    private final PlatformReactiveService platformService;

    WriteClientReactiveImpl(@Nonnull final WriteOptions writeOptions,
                            @Nonnull final PlatformReactiveService platformService) {

        super(writeOptions,
                Schedulers.newThread(),
                Schedulers.trampoline());

        this.platformService = platformService;
    }

    @Override
    public void writeRecord(@Nonnull final String bucket,
                            @Nonnull final String organization,
                            @Nonnull final ChronoUnit precision,
                            @Nonnull final Maybe<String> record) {

        Arguments.checkNonEmpty(bucket, "bucket");
        Arguments.checkNonEmpty(organization, "organization");
        Arguments.checkNotNull(precision, "precision");
        Arguments.checkNotNull(record, "record");

        writeRecords(bucket, organization, precision, record.toFlowable());
    }

    @Override
    public void writeRecords(@Nonnull final String bucket,
                             @Nonnull final String organization,
                             @Nonnull final ChronoUnit precision,
                             @Nonnull final Flowable<String> records) {

        Arguments.checkNonEmpty(bucket, "bucket");
        Arguments.checkNonEmpty(organization, "organization");
        Arguments.checkNotNull(precision, "precision");
        Arguments.checkNotNull(records, "records");

        Flowable<BatchWriteData> stream = records.map(BatchWriteDataRecord::new);

        write(bucket, organization, precision, stream);
    }

    @Override
    public void writeRecords(@Nonnull final String bucket,
                             @Nonnull final String organization,
                             @Nonnull final ChronoUnit precision,
                             @Nonnull final Publisher<String> records) {

        Arguments.checkNonEmpty(bucket, "bucket");
        Arguments.checkNonEmpty(organization, "organization");
        Arguments.checkNotNull(precision, "precision");
        Arguments.checkNotNull(records, "records");

        writeRecords(bucket, organization, precision, Flowable.fromPublisher(records));
    }

    @Override
    public void writePoint(@Nonnull final String bucket,
                           @Nonnull final String organization,
                           @Nonnull final Maybe<Point> point) {

        Arguments.checkNonEmpty(bucket, "bucket");
        Arguments.checkNonEmpty(organization, "organization");
        Arguments.checkNotNull(point, "point");

        writePoints(bucket, organization, point.toFlowable());
    }

    @Override
    public void writePoints(@Nonnull final String bucket,
                            @Nonnull final String organization,
                            @Nonnull final Flowable<Point> points) {

        Arguments.checkNonEmpty(bucket, "bucket");
        Arguments.checkNonEmpty(organization, "organization");
        Arguments.checkNotNull(points, "points");

        Flowable<BatchWriteDataPoint> stream = points.filter(Objects::nonNull).map(BatchWriteDataPoint::new);

        write(bucket, organization, stream);
    }

    @Override
    public void writePoints(@Nonnull final String bucket,
                            @Nonnull final String organization,
                            @Nonnull final Publisher<Point> points) {

        Arguments.checkNonEmpty(bucket, "bucket");
        Arguments.checkNonEmpty(organization, "organization");
        Arguments.checkNotNull(points, "points");

        writePoints(bucket, organization, Flowable.fromPublisher(points));
    }

    @Override
    public <M> void writeMeasurement(@Nonnull final String bucket,
                                     @Nonnull final String organization,
                                     @Nonnull final ChronoUnit precision,
                                     @Nonnull final Maybe<M> measurement) {

        Arguments.checkNonEmpty(bucket, "bucket");
        Arguments.checkNonEmpty(organization, "organization");
        Arguments.checkNotNull(precision, "precision");
        Arguments.checkNotNull(measurement, "measurement");

        writeMeasurements(bucket, organization, precision, measurement.toFlowable());
    }

    @Override
    public <M> void writeMeasurements(@Nonnull final String bucket,
                                      @Nonnull final String organization,
                                      @Nonnull final ChronoUnit precision,
                                      @Nonnull final Flowable<M> measurements) {

        Arguments.checkNonEmpty(bucket, "bucket");
        Arguments.checkNonEmpty(organization, "organization");
        Arguments.checkNotNull(precision, "precision");
        Arguments.checkNotNull(measurements, "measurements");

        Flowable<BatchWriteData> stream = measurements.map(it -> new BatchWriteDataMeasurement(it, precision));

        write(bucket, organization, precision, stream);
    }

    @Override
    public <M> void writeMeasurements(@Nonnull final String bucket,
                                      @Nonnull final String organization,
                                      @Nonnull final ChronoUnit precision,
                                      @Nonnull final Publisher<M> measurements) {

        Arguments.checkNonEmpty(bucket, "bucket");
        Arguments.checkNonEmpty(organization, "organization");
        Arguments.checkNotNull(precision, "precision");
        Arguments.checkNotNull(measurements, "measurements");

        writeMeasurements(bucket, organization, precision, Flowable.fromPublisher(measurements));
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
    Completable writeCall(final RequestBody requestBody,
                          final String organization,
                          final String bucket,
                          final String precision) {
        return platformService.writePoints(organization, bucket, precision, requestBody);
    }
}