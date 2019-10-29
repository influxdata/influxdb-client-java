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

import java.time.OffsetDateTime;
import javax.annotation.Nonnull;

import com.influxdb.client.domain.DeletePredicateRequest;

/**
 * API to Delete time-series data from InfluxDB 2.0.
 *
 * @author Pavlina Rolincova (rolincova@github) (25/10/2019).
 */
public interface DeleteApi {

    /**
     * Delete Time series data from InfluxDB.
     *
     * @param start time
     * @param stop time
     * @param predicate sql where like delete statement.
     * @param bucket The bucket from which data will be deleted.
     * @param org The organization name of the above bucket.
     **/
    void delete(@Nonnull final OffsetDateTime start,
                @Nonnull final OffsetDateTime stop,
                @Nonnull final String predicate,
                @Nonnull final String bucket,
                @Nonnull final String org);

    /**
     * Delete Time series data from InfluxDB.
     *
     * @param predicate delete request.
     * @param bucket The bucket from which data will be deleted.
     * @param org The organization name of the above bucket.
     **/
    void delete(@Nonnull final DeletePredicateRequest predicate,
                @Nonnull final String bucket,
                @Nonnull final String org);
}
