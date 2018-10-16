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
package org.influxdata.platform;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.influxdata.platform.write.Point;
import org.influxdata.platform.write.event.AbstractWriteEvent;

import io.reactivex.Observable;

/**
 * Write time-series data into InfluxData Platform 2.0.
 * <p>
 * The data are formatted in <a href="https://bit.ly/2QL99fu">Line Protocol</a>.
 *
 * @author Jakub Bednar (bednar@github) (20/09/2018 10:58)
 */
public interface WriteClient {

    /**
     * Write Line Protocol record into specified bucket.
     *
     * @param bucket       specifies the destination bucket ID for writes
     * @param organization specifies the destination organization ID for writes
     * @param precision    specifies the precision for the unix timestamps within the body line-protocol.
     *                     Available values : {@link ChronoUnit#NANOS}, {@link ChronoUnit#MICROS},
     *                     {@link ChronoUnit#MILLIS}, {@link ChronoUnit#SECONDS}.
     *                     Default value : {@link TimeUnit#NANOSECONDS}.
     * @param record       specifies the record in InfluxDB Line Protocol.
     *                     The {@code record} is considered as one batch unit.
     */
    void writeRecord(@Nonnull final String bucket,
                     @Nonnull final String organization,
                     @Nonnull final ChronoUnit precision,
                     @Nullable final String record);


    /**
     * Write Line Protocol records into specified bucket.
     *
     * @param bucket       specifies the destination bucket ID for writes
     * @param organization specifies the destination organization ID for writes
     * @param precision    specifies the precision for the unix timestamps within the body line-protocol.
     *                     Available values : {@link ChronoUnit#NANOS}, {@link ChronoUnit#MICROS},
     *                     {@link ChronoUnit#MILLIS}, {@link ChronoUnit#SECONDS}.
     *                     Default value : {@link TimeUnit#NANOSECONDS}.
     * @param records      specifies the records in InfluxDB Line Protocol
     */
    void writeRecords(@Nonnull final String bucket,
                      @Nonnull final String organization,
                      @Nonnull final ChronoUnit precision,
                      @Nonnull final List<String> records);

    /**
     * Write Data point into specified bucket.
     *
     * @param bucket       specifies the destination bucket ID for writes
     * @param organization specifies the destination organization ID for writes
     * @param point        specifies the Data point to write into bucket
     */
    void writePoint(@Nonnull final String bucket,
                    @Nonnull final String organization,
                    @Nullable final Point point);


    /**
     * Write Data points into specified bucket.
     *
     * @param bucket       specifies the destination bucket ID for writes
     * @param organization specifies the destination organization ID for writes
     * @param points       specifies the Data points to write into bucket
     */
    void writePoints(@Nonnull final String bucket,
                     @Nonnull final String organization,
                     @Nonnull final List<Point> points);


    /**
     * Write Measurement into specified bucket.
     *
     * @param bucket       specifies the destination bucket ID for writes
     * @param organization specifies the destination organization ID for writes
     * @param precision    specifies the precision for the unix timestamps within the body line-protocol.
     *                     Available values : {@link ChronoUnit#NANOS}, {@link ChronoUnit#MICROS},
     *                     {@link ChronoUnit#MILLIS}, {@link ChronoUnit#SECONDS}.
     *                     Default value : {@link TimeUnit#NANOSECONDS}.
     * @param measurement  specifies the Measurement to write into bucket
     */
    void writeMeasurement(@Nonnull final String bucket,
                          @Nonnull final String organization,
                          @Nonnull final ChronoUnit precision,
                          @Nullable final Object measurement);


    /**
     * Write Measurements into specified bucket.
     *
     * @param bucket       specifies the destination bucket ID for writes
     * @param organization specifies the destination organization ID for writes
     * @param precision    specifies the precision for the unix timestamps within the body line-protocol.
     *                     Available values : {@link ChronoUnit#NANOS}, {@link ChronoUnit#MICROS},
     *                     {@link ChronoUnit#MILLIS}, {@link ChronoUnit#SECONDS}.
     *                     Default value : {@link TimeUnit#NANOSECONDS}.
     * @param measurements specifies the Measurements to write into bucket
     */
    void writeMeasurements(@Nonnull final String bucket,
                           @Nonnull final String organization,
                           @Nonnull final ChronoUnit precision,
                           @Nonnull final List<Object> measurements);

    /**
     * Listen the events produced by {@link WriteClient}.
     * <p>
     * The {@link WriteClient} produces: {@link org.influxdata.platform.write.event.WriteSuccessEvent},
     * {@link org.influxdata.platform.write.event.BackpressureEvent} and
     * {@link org.influxdata.platform.write.event.WriteErrorEvent}.
     *
     * @param eventType type of event to listen
     * @param <T>       type of event to listen
     * @return lister for {@code eventType} events
     */
    @Nonnull
    <T extends AbstractWriteEvent> Observable<T> listenEvents(@Nonnull final Class<T> eventType);

    /**
     * Close threads for asynchronous batch writing.
     *
     * @return the {@link WriteClient} instance to be able to use it in a fluent manner.
     */
    @Nonnull
    WriteClient close();

    //TODO IT test to precision (only Nanoseconds works?)
}