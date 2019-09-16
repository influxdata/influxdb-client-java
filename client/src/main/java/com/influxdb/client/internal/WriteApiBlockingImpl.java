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

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.influxdb.Arguments;
import com.influxdb.client.InfluxDBClientOptions;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.internal.AbstractWriteClient.BatchWriteData;
import com.influxdb.client.internal.AbstractWriteClient.BatchWriteDataMeasurement;
import com.influxdb.client.internal.AbstractWriteClient.BatchWriteDataPoint;
import com.influxdb.client.internal.AbstractWriteClient.BatchWriteDataRecord;
import com.influxdb.client.service.WriteService;
import com.influxdb.client.write.Point;
import com.influxdb.internal.AbstractRestClient;

import retrofit2.Call;

/**
 * @author Jakub Bednar (bednar@github) (16/07/2019 06:48)
 */
final class WriteApiBlockingImpl extends AbstractRestClient implements WriteApiBlocking {

    private static final Logger LOG = Logger.getLogger(WriteApiBlockingImpl.class.getName());

    private final WriteService service;
    private final InfluxDBClientOptions options;

    private final MeasurementMapper measurementMapper = new MeasurementMapper();

    WriteApiBlockingImpl(@Nonnull final WriteService service,
                         @Nonnull final InfluxDBClientOptions options) {

        this.service = service;
        this.options = options;
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

        if (record == null) {
            return;
        }

        write(bucket, org, precision, new BatchWriteDataRecord(record));
    }

    @Override
    public void writeRecords(@Nonnull final WritePrecision precision,
                             @Nonnull final List<String> records) {

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

        write(bucket, org, precision, records.stream().map(BatchWriteDataRecord::new));
    }

    @Override
    public void writePoint(@Nullable final Point point) {
        Arguments.checkNotNull(options.getBucket(), "InfluxDBClientOptions.getBucket");
        Arguments.checkNotNull(options.getOrg(), "InfluxDBClientOptions.getOrg");

        writePoint(options.getBucket(), options.getOrg(), point);
    }

    @Override
    public void writePoint(@Nonnull final String bucket, @Nonnull final String org, @Nullable final Point point) {
        if (point == null) {
            return;
        }

        writePoints(bucket, org, Collections.singletonList(point));
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

        points
                .stream()
                .filter(Objects::nonNull)
                .forEach(point -> write(bucket, org, point.getPrecision(), new BatchWriteDataPoint(point, options)));
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

        if (measurement == null) {
            return;
        }

        writeMeasurements(bucket, org, precision, Collections.singletonList(measurement));
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

        write(bucket, org, precision, measurements.stream()
                .map(it -> new BatchWriteDataMeasurement(it, precision, options, measurementMapper)));
    }

    private void write(@Nonnull final String bucket,
                       @Nonnull final String organization,
                       @Nonnull final WritePrecision precision,
                       @Nonnull final BatchWriteData data) {

        write(bucket, organization, precision, Stream.of(data));
    }

    private void write(@Nonnull final String bucket,
                       @Nonnull final String organization,
                       @Nonnull final WritePrecision precision,
                       @Nonnull final Stream<BatchWriteData> stream) {

        String lineProtocol = stream.map(BatchWriteData::toLineProtocol)
                .filter(it -> it != null && !it.isEmpty())
                .collect(Collectors.joining("\n"));

        if (lineProtocol.isEmpty()) {

            LOG.warning("The writes: " + stream + " doesn't contains any Line Protocol, skipping");
            return;
        }

        LOG.log(Level.FINEST,
                "Writing time-series data into InfluxDB (org={0}, bucket={1}, precision={2})...",
                new Object[]{organization, bucket, precision});

        Call<Void> voidCall = service.postWrite(organization, bucket, lineProtocol, null,
                "identity", "text/plain; charset=utf-8", null,
                "application/json", null, precision);

        execute(voidCall);

        LOG.log(Level.FINEST, "Written data into InfluxDB: {0}", lineProtocol);
    }
}