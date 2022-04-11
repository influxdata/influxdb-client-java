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

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.influxdb.client.InfluxDBClientOptions;
import com.influxdb.client.domain.WriteConsistency;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.internal.AbstractWriteClient.BatchWriteDataMeasurement;
import com.influxdb.client.service.WriteService;
import com.influxdb.client.write.WriteParameters;
import com.influxdb.internal.AbstractRestClient;
import com.influxdb.utils.Arguments;

import retrofit2.Call;

/**
 * @author Jakub Bednar (20/04/2021 9:48)
 */
public abstract class AbstractWriteBlockingClient extends AbstractRestClient {

    private static final Logger LOG = Logger.getLogger(AbstractWriteBlockingClient.class.getName());

    private final WriteService service;
    private final MeasurementMapper measurementMapper = new MeasurementMapper();

    protected final InfluxDBClientOptions options;

    public AbstractWriteBlockingClient(@Nonnull final WriteService service,
                                       @Nonnull final InfluxDBClientOptions options) {

        Arguments.checkNotNull(service, "service");
        Arguments.checkNotNull(options, "options");

        this.options = options;
        this.service = service;
    }

    protected void write(@Nonnull final WriteParameters parameters,
                         @Nonnull final Stream<AbstractWriteClient.BatchWriteData> stream) {

        String lineProtocol = stream.map(AbstractWriteClient.BatchWriteData::toLineProtocol)
                .filter(it -> it != null && !it.isEmpty())
                .collect(Collectors.joining("\n"));

        if (lineProtocol.isEmpty()) {

            LOG.warning("The writes: " + stream + " doesn't contains any Line Protocol, skipping");
            return;
        }

        String organization = parameters.orgSafe(options);
        String bucket = parameters.bucketSafe(options);
        WritePrecision precision = parameters.precisionSafe(options);
        WriteConsistency consistency = parameters.consistencySafe(options);

        LOG.log(Level.FINEST,
                "Writing time-series data into InfluxDB (org={0}, bucket={1}, precision={2})...",
                new Object[]{organization, bucket, precision});

        Call<Void> voidCall = service.postWrite(organization, bucket, lineProtocol, null,
                "identity", "text/plain; charset=utf-8", null,
                "application/json", null, precision, consistency);

        execute(voidCall);

        LOG.log(Level.FINEST, "Written data into InfluxDB: {0}", lineProtocol);
    }

    @Nonnull
    protected <M> BatchWriteDataMeasurement toMeasurementBatch(@Nullable final M measurement,
                                                               @Nonnull final WritePrecision precision) {

        Arguments.checkNotNull(precision, "WritePrecision");

        return new BatchWriteDataMeasurement(measurement, precision, options, measurementMapper);
    }
}
