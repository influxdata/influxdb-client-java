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
package com.influxdb.client;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.exceptions.InfluxException;


/**
 * The synchronous blocking API to Write time-series data into InfluxDB 2.0.
 * <p>
 * The data are formatted in <a href="https://bit.ly/2QL99fu">Line Protocol</a>.
 * <p>
 *
 * @author Jakub Bednar (bednar@github) (20/09/2018 10:58)
 */
public interface WriteApiBlocking {

    /**
     * Write Line Protocol record into specified bucket.
     *
     * <p>
     * The {@link InfluxDBClientOptions#getBucket()} will be use as destination bucket
     * and {@link InfluxDBClientOptions#getOrg()} will be used as destination organization.
     * </p>
     *
     * <p>
     * NOTE: This method directly write data info InfluxDB 2.0 without batching, jittering and backpressure.
     * The method blocks the executing thread until their operation finished.
     * There is also non-blocking alternative {@link WriteApi#writeRecord(WritePrecision, String)}.
     * </p>
     *
     * @param precision specifies the precision for the unix timestamps within the body line-protocol (optional)
     * @param record    specifies the record in InfluxDB Line Protocol.
     *                  The {@code record} is considered as one batch unit.
     * @throws InfluxException if a problem occurred during write time-series data into InfluxDB
     */
    void writeRecord(@Nonnull final WritePrecision precision,
                     @Nullable final String record) throws InfluxException;

    /**
     * Write Line Protocol record into specified bucket.
     *
     * <p>
     * NOTE: This method directly write data info InfluxDB 2.0 without batching, jittering and backpressure.
     * The method blocks the executing thread until their operation finished.
     * There is also non-blocking alternative {@link WriteApi#writeRecords(String, String, WritePrecision, List)}.
     * </p>
     *
     * @param bucket    specifies the destination bucket for writes
     * @param org       specifies the destination organization for writes
     * @param precision specifies the precision for the unix timestamps within the body line-protocol (optional)
     * @param record    specifies the record in InfluxDB Line Protocol.
     *                  The {@code record} is considered as one batch unit.
     * @throws InfluxException if a problem occurred during write time-series data into InfluxDB
     */
    void writeRecord(@Nonnull final String bucket,
                     @Nonnull final String org,
                     @Nonnull final WritePrecision precision,
                     @Nullable final String record) throws InfluxException;

    /**
     * Write Line Protocol records into specified bucket.
     *
     * <p>
     * The {@link InfluxDBClientOptions#getBucket()} will be use as destination bucket
     * and {@link InfluxDBClientOptions#getOrg()} will be used as destination organization.
     * </p>
     *
     * <p>
     * NOTE: This method directly write data info InfluxDB 2.0 without batching, jittering and backpressure.
     * The method blocks the executing thread until their operation finished.
     * There is also non-blocking alternative {@link WriteApi#writeRecords(WritePrecision, List)}.
     * </p>
     *
     * @param precision specifies the precision for the unix timestamps within the body line-protocol (optional)
     * @param records   specifies the records in InfluxDB Line Protocol
     * @throws InfluxException if a problem occurred during write time-series data into InfluxDB
     */
    void writeRecords(@Nonnull final WritePrecision precision,
                      @Nonnull final List<String> records) throws InfluxException;

    /**
     * Write Line Protocol records into specified bucket.
     *
     * <p>
     * NOTE: This method directly write data info InfluxDB 2.0 without batching, jittering and backpressure.
     * The method blocks the executing thread until their operation finished.
     * There is also non-blocking alternative {@link WriteApi#writeRecords(String, String, WritePrecision, List)}.
     * </p>
     *
     * @param bucket    specifies the destination bucket for writes
     * @param org       specifies the destination organization for writes
     * @param precision specifies the precision for the unix timestamps within the body line-protocol (optional)
     * @param records   specifies the records in InfluxDB Line Protocol
     * @throws InfluxException if a problem occurred during write time-series data into InfluxDB
     */
    void writeRecords(@Nonnull final String bucket,
                      @Nonnull final String org,
                      @Nonnull final WritePrecision precision,
                      @Nonnull final List<String> records) throws InfluxException;

    /**
     * Write Data point into specified bucket.
     *
     * <p>
     * The {@link InfluxDBClientOptions#getBucket()} will be use as destination bucket
     * and {@link InfluxDBClientOptions#getOrg()} will be used as destination organization.
     * </p>
     *
     * <p>
     * NOTE: This method directly write data info InfluxDB 2.0 without batching, jittering and backpressure.
     * The method blocks the executing thread until their operation finished.
     * There is also non-blocking alternative {@link WriteApi#writePoint(Point)}.
     * </p>
     *
     * @param point specifies the Data point to write into bucket
     * @throws InfluxException if a problem occurred during write time-series data into InfluxDB
     */
    void writePoint(@Nullable final Point point) throws InfluxException;

    /**
     * Write Data point into specified bucket.
     *
     * <p>
     * NOTE: This method directly write data info InfluxDB 2.0 without batching, jittering and backpressure.
     * The method blocks the executing thread until their operation finished.
     * There is also non-blocking alternative {@link WriteApi#writePoint(String, String, Point)}.
     * </p>
     *
     * @param bucket specifies the destination bucket for writes
     * @param org    specifies the destination organization for writes
     * @param point  specifies the Data point to write into bucket
     * @throws InfluxException if a problem occurred during write time-series data into InfluxDB
     */
    void writePoint(@Nonnull final String bucket,
                    @Nonnull final String org,
                    @Nullable final Point point) throws InfluxException;

    /**
     * Write Data points into specified bucket.
     *
     * <p>
     * The {@link InfluxDBClientOptions#getBucket()} will be use as destination bucket
     * and {@link InfluxDBClientOptions#getOrg()} will be used as destination organization.
     * </p>
     *
     * <p>
     * NOTE: This method directly write data info InfluxDB 2.0 without batching, jittering and backpressure.
     * The method blocks the executing thread until their operation finished.
     * There is also non-blocking alternative {@link WriteApi#writePoints(List)}.
     * </p>
     *
     * @param points specifies the Data points to write into bucket
     * @throws InfluxException if a problem occurred during write time-series data into InfluxDB
     */
    void writePoints(@Nonnull final List<Point> points) throws InfluxException;


    /**
     * Write Data points into specified bucket.
     *
     * <p>
     * NOTE: This method directly write data info InfluxDB 2.0 without batching, jittering and backpressure.
     * The method blocks the executing thread until their operation finished.
     * There is also non-blocking alternative {@link WriteApi#writePoints(String, String, List)}.
     * </p>
     *
     * @param bucket specifies the destination bucket ID for writes
     * @param org    specifies the destination organization ID for writes
     * @param points specifies the Data points to write into bucket
     * @throws InfluxException if a problem occurred during write time-series data into InfluxDB
     */
    void writePoints(@Nonnull final String bucket,
                     @Nonnull final String org,
                     @Nonnull final List<Point> points) throws InfluxException;

    /**
     * Write Measurement into specified bucket.
     *
     * <p>
     * The {@link InfluxDBClientOptions#getBucket()} will be use as destination bucket
     * and {@link InfluxDBClientOptions#getOrg()} will be used as destination organization.
     * </p>
     *
     * <p>
     * NOTE: This method directly write data info InfluxDB 2.0 without batching, jittering and backpressure.
     * The method blocks the executing thread until their operation finished.
     * There is also non-blocking alternative {@link WriteApi#writeMeasurement(WritePrecision, Object)}.
     * </p>
     *
     * @param precision   specifies the precision for the unix timestamps within the body line-protocol (optional)
     * @param <M>         measurement type
     * @param measurement specifies the Measurement to write into bucket
     * @throws InfluxException if a problem occurred during write time-series data into InfluxDB
     */
    <M> void writeMeasurement(@Nonnull final WritePrecision precision,
                              @Nullable final M measurement) throws InfluxException;

    /**
     * Write Measurement into specified bucket.
     *
     * <p>
     * NOTE: This method directly write data info InfluxDB 2.0 without batching, jittering and backpressure.
     * The method blocks the executing thread until their operation finished.
     * There is also non-blocking alternative {@link WriteApi#writeMeasurement(String, String, WritePrecision, Object)}.
     * </p>
     *
     * @param bucket      specifies the destination bucket for writes
     * @param org         specifies the destination organization for writes
     * @param precision   specifies the precision for the unix timestamps within the body line-protocol (optional)
     * @param <M>         measurement type
     * @param measurement specifies the Measurement to write into bucket
     * @throws InfluxException if a problem occurred during write time-series data into InfluxDB
     */
    <M> void writeMeasurement(@Nonnull final String bucket,
                              @Nonnull final String org,
                              @Nonnull final WritePrecision precision,
                              @Nullable final M measurement) throws InfluxException;

    /**
     * Write Measurements into specified bucket.
     *
     * <p>
     * The {@link InfluxDBClientOptions#getBucket()} will be use as destination bucket
     * and {@link InfluxDBClientOptions#getOrg()} will be used as destination organization.
     * </p>
     *
     * <p>
     * NOTE: This method directly write data info InfluxDB 2.0 without batching, jittering and backpressure.
     * The method blocks the executing thread until their operation finished.
     * There is also non-blocking alternative {@link WriteApi#writeMeasurements(WritePrecision, List)}.
     * </p>
     *
     * @param precision    specifies the precision for the unix timestamps within the body line-protocol (optional)
     * @param <M>          measurement type
     * @param measurements specifies the Measurements to write into bucket
     * @throws InfluxException if a problem occurred during write time-series data into InfluxDB
     */
    <M> void writeMeasurements(@Nonnull final WritePrecision precision,
                               @Nonnull final List<M> measurements) throws InfluxException;

    /**
     * Write Measurements into specified bucket.
     *
     * <p>
     * NOTE: This method directly write data info InfluxDB 2.0 without batching, jittering and backpressure.
     * The method blocks the executing thread until their operation finished.
     * There is also non-blocking alternative {@link WriteApi#writeMeasurements(String, String, WritePrecision, List)}.
     * </p>
     *
     * @param bucket       specifies the destination bucket for writes
     * @param org          specifies the destination organization for writes
     * @param precision    specifies the precision for the unix timestamps within the body line-protocol (optional)
     * @param <M>          measurement type
     * @param measurements specifies the Measurements to write into bucket
     * @throws InfluxException if a problem occurred during write time-series data into InfluxDB
     */
    <M> void writeMeasurements(@Nonnull final String bucket,
                               @Nonnull final String org,
                               @Nonnull final WritePrecision precision,
                               @Nonnull final List<M> measurements) throws InfluxException;
}