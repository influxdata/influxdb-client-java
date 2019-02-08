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
package org.influxdata.client;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.influxdata.client.write.Point;
import org.influxdata.client.write.events.AbstractWriteEvent;
import org.influxdata.client.write.events.BackpressureEvent;
import org.influxdata.client.write.events.EventListener;
import org.influxdata.client.write.events.ListenerRegistration;
import org.influxdata.client.write.events.WriteErrorEvent;
import org.influxdata.client.write.events.WriteSuccessEvent;

/**
 * Write time-series data intoInfluxDB 2.02.0.
 * <p>
 * The data are formatted in <a href="https://bit.ly/2QL99fu">Line Protocol</a>.
 *
 * //TODO support orgID and orgName (+ query)
 *
 * @author Jakub Bednar (bednar@github) (20/09/2018 10:58)
 */
public interface WriteApi extends AutoCloseable {

    /**
     * Write Line Protocol record into specified bucket.
     *
     * @param bucket    specifies the destination bucket for writes
     * @param orgID     specifies the destination organization for writes
     * @param precision specifies the precision for the unix timestamps within the body line-protocol.
     *                  Available values : {@link ChronoUnit#NANOS}, {@link ChronoUnit#MICROS},
     *                  {@link ChronoUnit#MILLIS}, {@link ChronoUnit#SECONDS}.
     *                  Default value : {@link TimeUnit#NANOSECONDS}.
     * @param record    specifies the record in InfluxDB Line Protocol.
     *                  The {@code record} is considered as one batch unit.
     */
    void writeRecord(@Nonnull final String bucket,
                     @Nonnull final String orgID,
                     @Nonnull final ChronoUnit precision,
                     @Nullable final String record);


    /**
     * Write Line Protocol records into specified bucket.
     *
     * @param bucket    specifies the destination bucket for writes
     * @param orgID     specifies the destination organization for writes
     * @param precision specifies the precision for the unix timestamps within the body line-protocol.
     *                  Available values : {@link ChronoUnit#NANOS}, {@link ChronoUnit#MICROS},
     *                  {@link ChronoUnit#MILLIS}, {@link ChronoUnit#SECONDS}.
     *                  Default value : {@link TimeUnit#NANOSECONDS}.
     * @param records   specifies the records in InfluxDB Line Protocol
     */
    void writeRecords(@Nonnull final String bucket,
                      @Nonnull final String orgID,
                      @Nonnull final ChronoUnit precision,
                      @Nonnull final List<String> records);

    /**
     * Write Data point into specified bucket.
     *
     * @param bucket specifies the destination bucket for writes
     * @param orgID  specifies the destination organization for writes
     * @param point  specifies the Data point to write into bucket
     */
    void writePoint(@Nonnull final String bucket,
                    @Nonnull final String orgID,
                    @Nullable final Point point);


    /**
     * Write Data points into specified bucket.
     *
     * @param bucket specifies the destination bucket ID for writes
     * @param orgID  specifies the destination organization ID for writes
     * @param points specifies the Data points to write into bucket
     */
    void writePoints(@Nonnull final String bucket,
                     @Nonnull final String orgID,
                     @Nonnull final List<Point> points);


    /**
     * Write Measurement into specified bucket.
     *
     * @param bucket      specifies the destination bucket for writes
     * @param orgID       specifies the destination organization for writes
     * @param precision   specifies the precision for the unix timestamps within the body line-protocol.
     *                    Available values : {@link ChronoUnit#NANOS}, {@link ChronoUnit#MICROS},
     *                    {@link ChronoUnit#MILLIS}, {@link ChronoUnit#SECONDS}.
     *                    Default value : {@link TimeUnit#NANOSECONDS}.
     * @param <M>         measurement type
     * @param measurement specifies the Measurement to write into bucket
     */
    <M> void writeMeasurement(@Nonnull final String bucket,
                              @Nonnull final String orgID,
                              @Nonnull final ChronoUnit precision,
                              @Nullable final M measurement);


    /**
     * Write Measurements into specified bucket.
     *
     * @param bucket       specifies the destination bucket for writes
     * @param orgID        specifies the destination organization for writes
     * @param precision    specifies the precision for the unix timestamps within the body line-protocol.
     *                     Available values : {@link ChronoUnit#NANOS}, {@link ChronoUnit#MICROS},
     *                     {@link ChronoUnit#MILLIS}, {@link ChronoUnit#SECONDS}.
     *                     Default value : {@link TimeUnit#NANOSECONDS}.
     * @param <M>          measurement type
     * @param measurements specifies the Measurements to write into bucket
     */
    <M> void writeMeasurements(@Nonnull final String bucket,
                               @Nonnull final String orgID,
                               @Nonnull final ChronoUnit precision,
                               @Nonnull final List<M> measurements);

    /**
     * Listen the events produced by {@link WriteApi}.
     * <p>
     * The {@link WriteApi} produces: {@link WriteSuccessEvent},
     * {@link BackpressureEvent} and
     * {@link WriteErrorEvent}.
     *
     * @param eventType type of event to listen
     * @param <T>       type of event to listen
     * @return lister for {@code eventType} events
     */
    @Nonnull
    <T extends AbstractWriteEvent> ListenerRegistration listenEvents(@Nonnull final Class<T> eventType,
                                                                     @Nonnull final EventListener<T> listener);

    /**
     * Forces the client to flush all pending writes from the buffer toInfluxDB 2.0via HTTP.
     */
    void flush();

    /**
     * Close threads for asynchronous batch writing.
     */
    void close();
}