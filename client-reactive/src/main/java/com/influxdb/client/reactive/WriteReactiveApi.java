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
package com.influxdb.client.reactive;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import com.influxdb.client.InfluxDBClientOptions;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

/**
 * Write time-series data into InfluxDB 2.0.
 * <p>
 * The data are formatted in <a href="https://bit.ly/line-protocol">Line Protocol</a>.
 *
 * @author Jakub Bednar (bednar@github) (22/11/2018 06:49)
 */
@ThreadSafe
public interface WriteReactiveApi {

    /**
     * The class represents a successful written operation.
     */
    class Success {
    }

    /**
     * Write Line Protocol record into specified bucket.
     *
     * <p>
     * The {@link InfluxDBClientOptions#getBucket()} will be use as destination bucket
     * and {@link InfluxDBClientOptions#getOrg()} will be used as destination organization.
     * </p>
     *
     * @param precision specifies the precision for the unix timestamps within the body line-protocol
     * @param record    specifies the record in InfluxDB Line Protocol.
     * @return Publisher representing a successful written operation or signal
     * the {@link com.influxdb.exceptions.InfluxException} via {@link Subscriber#onError}
     */
    Publisher<Success> writeRecord(@Nonnull final WritePrecision precision,
                                   @Nullable final String record);

    /**
     * Write Line Protocol record into specified bucket.
     *
     * @param bucket    specifies the destination bucket for writes
     * @param org       specifies the destination organization for writes
     * @param precision specifies the precision for the unix timestamps within the body line-protocol
     * @param record    specifies the record in InfluxDB Line Protocol.
     * @return Publisher representing a successful written operation or signal
     * the {@link com.influxdb.exceptions.InfluxException} via {@link Subscriber#onError}
     */
    Publisher<Success> writeRecord(@Nonnull final String bucket,
                                   @Nonnull final String org,
                                   @Nonnull final WritePrecision precision,
                                   @Nullable final String record);


    /**
     * Write Line Protocol records into specified bucket.
     *
     * <p>
     * The {@link InfluxDBClientOptions#getBucket()} will be use as destination bucket
     * and {@link InfluxDBClientOptions#getOrg()} will be used as destination organization.
     * </p>
     *
     * @param precision specifies the precision for the unix timestamps within the body line-protocol
     * @param records   specifies the records in InfluxDB Line Protocol
     * @return Publisher representing a successful written operation or signal
     * the {@link com.influxdb.exceptions.InfluxException} via {@link Subscriber#onError}
     */
    Publisher<Success> writeRecords(@Nonnull final WritePrecision precision,
                                    @Nonnull final Publisher<String> records);


    /**
     * Write Line Protocol records into specified bucket.
     *
     * @param bucket    specifies the destination bucket for writes
     * @param org       specifies the destination organization for writes
     * @param precision specifies the precision for the unix timestamps within the body line-protocol
     * @param records   specifies the records in InfluxDB Line Protocol
     * @return Publisher representing a successful written operation or signal
     * the {@link com.influxdb.exceptions.InfluxException} via {@link Subscriber#onError}
     */
    Publisher<Success> writeRecords(@Nonnull final String bucket,
                                    @Nonnull final String org,
                                    @Nonnull final WritePrecision precision,
                                    @Nonnull final Publisher<String> records);

    /**
     * Write Data point into specified bucket.
     *
     * <p>
     * The {@link InfluxDBClientOptions#getBucket()} will be use as destination bucket
     * and {@link InfluxDBClientOptions#getOrg()} will be used as destination organization.
     * </p>
     *
     * @param precision specifies the precision for the unix timestamps within the body line-protocol
     * @param point     specifies the Data point to write into bucket
     * @return Publisher representing a successful written operation or signal
     * the {@link com.influxdb.exceptions.InfluxException} via {@link Subscriber#onError}
     */
    Publisher<Success> writePoint(@Nonnull final WritePrecision precision,
                                  @Nonnull final Point point);

    /**
     * Write Data point into specified bucket.
     *
     * @param bucket    specifies the destination bucket for writes
     * @param org       specifies the destination organization for writes
     * @param precision specifies the precision for the unix timestamps within the body line-protocol
     * @param point     specifies the Data point to write into bucket
     * @return Publisher representing a successful written operation or signal
     * the {@link com.influxdb.exceptions.InfluxException} via {@link Subscriber#onError}
     */
    Publisher<Success> writePoint(@Nonnull final String bucket,
                                  @Nonnull final String org,
                                  @Nonnull final WritePrecision precision,
                                  @Nonnull final Point point);

    /**
     * Write Data points into specified bucket.
     *
     * <p>
     * The {@link InfluxDBClientOptions#getBucket()} will be use as destination bucket
     * and {@link InfluxDBClientOptions#getOrg()} will be used as destination organization.
     * </p>
     *
     * @param precision specifies the precision for the unix timestamps within the body line-protocol
     * @param points    specifies the Data points to write into bucket
     * @return Publisher representing a successful written operation or signal
     * the {@link com.influxdb.exceptions.InfluxException} via {@link Subscriber#onError}
     */
    Publisher<Success> writePoints(@Nonnull final WritePrecision precision,
                                   @Nonnull final Publisher<Point> points);

    /**
     * Write Data points into specified bucket.
     *
     * @param bucket    specifies the destination bucket ID for writes
     * @param org       specifies the destination organization ID for writes
     * @param precision specifies the precision for the unix timestamps within the body line-protocol
     * @param points    specifies the Data points to write into bucket
     * @return Publisher representing a successful written operation or signal
     * the {@link com.influxdb.exceptions.InfluxException} via {@link Subscriber#onError}
     */
    Publisher<Success> writePoints(@Nonnull final String bucket,
                                   @Nonnull final String org,
                                   @Nonnull final WritePrecision precision,
                                   @Nonnull final Publisher<Point> points);

    /**
     * Write Measurement into specified bucket.
     *
     * <p>
     * The {@link InfluxDBClientOptions#getBucket()} will be use as destination bucket
     * and {@link InfluxDBClientOptions#getOrg()} will be used as destination organization.
     * </p>
     *
     * @param precision   specifies the precision for the unix timestamps within the body line-protocol
     * @param measurement specifies the Measurement to write into bucket
     * @param <M>         type of measurement
     * @return Publisher representing a successful written operation or signal
     * the {@link com.influxdb.exceptions.InfluxException} via {@link Subscriber#onError}
     */
    <M> Publisher<Success> writeMeasurement(@Nonnull final WritePrecision precision,
                                            @Nonnull final M measurement);

    /**
     * Write Measurement into specified bucket.
     *
     * @param bucket      specifies the destination bucket for writes
     * @param org         specifies the destination organization for writes
     * @param precision   specifies the precision for the unix timestamps within the body line-protocol
     * @param measurement specifies the Measurement to write into bucket
     * @param <M>         type of measurement
     * @return Publisher representing a successful written operation or signal
     * the {@link com.influxdb.exceptions.InfluxException} via {@link Subscriber#onError}
     */
    <M> Publisher<Success> writeMeasurement(@Nonnull final String bucket,
                                            @Nonnull final String org,
                                            @Nonnull final WritePrecision precision,
                                            @Nonnull final M measurement);

    /**
     * Write Measurements into specified bucket.
     *
     * <p>
     * The {@link InfluxDBClientOptions#getBucket()} will be use as destination bucket
     * and {@link InfluxDBClientOptions#getOrg()} will be used as destination organization.
     * </p>
     *
     * @param precision    specifies the precision for the unix timestamps within the body line-protocol
     * @param measurements specifies the Measurements to write into bucket
     * @param <M>          type of measurement
     * @return Publisher representing a successful written operation or signal
     * the {@link com.influxdb.exceptions.InfluxException} via {@link Subscriber#onError}
     */
    <M> Publisher<Success> writeMeasurements(@Nonnull final WritePrecision precision,
                                             @Nonnull final Publisher<M> measurements);

    /**
     * Write Measurements into specified bucket.
     *
     * @param bucket       specifies the destination bucket for writes
     * @param org          specifies the destination organization for writes
     * @param precision    specifies the precision for the unix timestamps within the body line-protocol
     * @param measurements specifies the Measurements to write into bucket
     * @param <M>          type of measurement
     * @return Publisher representing a successful written operation or signal
     * the {@link com.influxdb.exceptions.InfluxException} via {@link Subscriber#onError}
     */
    <M> Publisher<Success> writeMeasurements(@Nonnull final String bucket,
                                             @Nonnull final String org,
                                             @Nonnull final WritePrecision precision,
                                             @Nonnull final Publisher<M> measurements);
}