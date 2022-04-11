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

import com.influxdb.client.InfluxDBClientOptions;
import com.influxdb.client.domain.WriteConsistency;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.utils.Arguments;

/**
 * Write API optional parameters.
 * <p>
 * Supports to specify:
 * <ul>
 *     <li><code>bucket</code> - Specifies the destination bucket for writes.</li>
 *     <li><code>organization</code> - Specifies the destination organization for writes.</li>
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
public final class WriteParameters {

    private final String bucket;
    private final String organization;
    private final WritePrecision precision;
    private final WriteConsistency consistency;

    /**
     * @param bucket       Specifies the destination bucket for writes.
     * @param organization Specifies the destination organization for writes.
     * @param precision    Precision for unix timestamps in the line protocol of the request payload.
     */
    public WriteParameters(@Nonnull final String bucket,
                           @Nonnull final String organization,
                           @Nonnull final WritePrecision precision) {

        Arguments.checkNonEmpty(bucket, "bucket");
        Arguments.checkNonEmpty(organization, "organization");
        Arguments.checkNotNull(precision, "WritePrecision");

        this.bucket = bucket;
        this.organization = organization;
        this.precision = precision;
        this.consistency = null;
    }

    /**
     * @param options with default value
     * @return The destination organization for writes.
     */
    @Nonnull
    public String organizationSafe(@Nonnull final InfluxDBClientOptions options) {
        return organization;
    }

    /**
     * @param options with default value
     * @return The destination bucket for writes.
     */
    @Nonnull
    public String bucketSafe(@Nonnull final InfluxDBClientOptions options) {
        return bucket;
    }

    /**
     * @param options with default value
     * @return Precision for unix timestamps in the line protocol of the request payload.
     */
    @Nonnull
    public WritePrecision precisionSafe(@Nonnull final InfluxDBClientOptions options) {
        return precision;
    }

    /**
     * @param options with default value
     * @return The write consistency for the point.
     */
    @Nullable
    public WriteConsistency consistencySafe(@Nonnull final InfluxDBClientOptions options) {
        return consistency;
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
        if (!organization.equals(that.organization)) {
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
        result = 31 * result + organization.hashCode();
        result = 31 * result + precision.hashCode();
        result = 31 * result + (consistency != null ? consistency.hashCode() : 0);
        return result;
    }
}
