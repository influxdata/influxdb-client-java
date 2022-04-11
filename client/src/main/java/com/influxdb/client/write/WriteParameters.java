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
package com.influxdb.client.write;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import com.influxdb.client.InfluxDBClientOptions;
import com.influxdb.client.domain.WriteConsistency;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.utils.Arguments;

/**
 * Write API parameters.
 * <p>
 * Supports to specify:
 * <ul>
 *     <li><code>bucket</code> - Specifies the destination bucket for writes.</li>
 *     <li><code>org</code> - Specifies the destination organization for writes.</li>
 *     <li><code>precision</code> - Precision for unix timestamps in the line protocol of the request payload.</li>
 *     <li>
 *         <code>consistency</code> - The write consistency for the point.
 *         InfluxDB assumes that the write consistency is {@link  WriteConsistency#ONE} if you do not
 *         specify consistency.
 *         See the <a href="https://bit.ly/enterprise-consistency">InfluxDB Enterprise documentation</a> for
 *         detailed descriptions of each consistency option.
 *         <b>Available with InfluxDB Enterprise clusters only!</b>
 *     </li>
 * </ul>
 */
@ThreadSafe
@SuppressWarnings("ConstantConditions")
public final class WriteParameters {

    public static final WritePrecision DEFAULT_WRITE_PRECISION = WritePrecision.NS;

    private final String bucket;
    private final String org;
    private final WritePrecision precision;
    private final WriteConsistency consistency;

    /**
     * Construct WriteAPI parameters.
     *
     * @param bucket      The destination bucket for writes.
     *                    If it is not specified then use {@link InfluxDBClientOptions#getBucket()}.
     * @param org         The destination organization for writes.
     *                    If it is not specified then use {@link InfluxDBClientOptions#getOrg()}.
     * @param precision   Precision for unix timestamps in the line protocol of the request payload.
     *                    If it is not specified then use {@link WritePrecision#NS}.
     * @param consistency The write consistency for the point. For more info see {@link WriteParameters}.
     */
    public WriteParameters(@Nullable final String bucket,
                           @Nullable final String org,
                           @Nullable final WritePrecision precision,
                           @Nullable final WriteConsistency consistency) {
        this.bucket = bucket;
        this.org = org;
        this.precision = precision;
        this.consistency = consistency;
    }

    /**
     * The backward internal constructor, please use
     * {@link #WriteParameters(String, String, WritePrecision, WriteConsistency)}.
     *
     * @param bucket    The destination bucket for writes.
     * @param org       The destination organization for writes.
     * @param precision Precision for unix timestamps in the line protocol of the request payload.
     */
    public WriteParameters(@Nonnull final String bucket,
                           @Nonnull final String org,
                           @Nonnull final WritePrecision precision) {

        Arguments.checkNonEmpty(bucket, "bucket");
        Arguments.checkNonEmpty(org, "org");
        Arguments.checkNotNull(precision, "WritePrecision");

        this.bucket = bucket;
        this.org = org;
        this.precision = precision;
        this.consistency = null;
    }

    /**
     * @param options with default value
     * @return The destination organization for writes.
     */
    @Nonnull
    public String orgSafe(@Nonnull final InfluxDBClientOptions options) {
        Arguments.checkNotNull(options, "options");
        return isNotDefined(org) ? options.getOrg() : org;
    }

    /**
     * @param options with default value
     * @return The destination bucket for writes.
     */
    @Nonnull
    public String bucketSafe(@Nonnull final InfluxDBClientOptions options) {
        Arguments.checkNotNull(options, "options");
        return isNotDefined(bucket) ? options.getBucket() : bucket;
    }

    /**
     * @param options with default value
     * @return Precision for unix timestamps in the line protocol of the request payload.
     */
    @Nonnull
    public WritePrecision precisionSafe(@Nonnull final InfluxDBClientOptions options) {
        Arguments.checkNotNull(options, "options");
        return precision == null ? options.getPrecision() : precision;
    }

    /**
     * @param options with default value
     * @return The write consistency for the point.
     */
    @Nullable
    public WriteConsistency consistencySafe(@Nonnull final InfluxDBClientOptions options) {
        Arguments.checkNotNull(options, "options");
        return consistency == null ? options.getConsistency() : consistency;
    }

    /**
     * Enforces that the destination {@code bucket} and destination {@code organization} are defined.
     *
     * @param options with default values
     * @throws IllegalArgumentException if the bucket or organization is not defined
     */
    public void check(@Nonnull final InfluxDBClientOptions options) {
        Arguments.checkNotNull(options, "options");

        if (isNotDefined(bucket) && isNotDefined(options.getBucket())) {
            throw new IllegalArgumentException("Expecting a non-empty string for destination bucket. "
                    + "Please specify the bucket as a method parameter or use default configuration "
                    + "at 'InfluxDBClientOptions.Bucket'.");
        }

        if (isNotDefined(org) && isNotDefined(options.getOrg())) {
            throw new IllegalArgumentException("Expecting a non-empty string for destination organization. "
                    + "Please specify the organization as a method parameter or use default configuration "
                    + "at 'InfluxDBClientOptions.Organization'.");
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof WriteParameters)) {
            return false;
        }

        WriteParameters that = (WriteParameters) o;

        if (!bucket.equals(that.bucket)) {
            return false;
        }
        if (!org.equals(that.org)) {
            return false;
        }
        if (precision != that.precision) {
            return false;
        }
        return consistency == that.consistency;
    }

    @Override
    @SuppressWarnings("MagicNumber")
    public int hashCode() {
        int result = bucket.hashCode();
        result = 31 * result + org.hashCode();
        result = 31 * result + precision.hashCode();
        result = 31 * result + (consistency != null ? consistency.hashCode() : 0);
        return result;
    }

    private boolean isNotDefined(final String option) {
        return option == null || option.isEmpty();
    }
}
