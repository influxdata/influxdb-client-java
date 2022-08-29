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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import com.influxdb.client.domain.InfluxQLQuery;
import com.influxdb.query.InfluxQLQueryResult;

/**
 * The <code>InfluxQL</code> can be used with <code>/query compatibility</code> endpoint which uses the
 * <strong>{@link InfluxQLQuery#getDatabase() database}</strong> and
 * <strong>{@link InfluxQLQuery#getRetentionPolicy() retention policy}</strong> specified in the query request to
 * map the request to an InfluxDB bucket.
 * <br>
 * For more information, see:
 * <ul>
 *     <li>
 *         <a href="https://docs.influxdata.com/influxdb/latest/reference/api/influxdb-1x/query/">
 *             /query 1.x compatibility API
 *         </a>
 *     </li>
 *     <li>
 *         <a href="https://docs.influxdata.com/influxdb/latest/reference/api/influxdb-1x/dbrp/">
 *             Database and retention policy mapping
 *         </a>
 *     </li>
 * </ul>
 **/
@ThreadSafe
public interface InfluxQLQueryApi {

    /**
     * Executes an InfluxQL query against the legacy endpoint.
     *
     * @param influxQlQuery the query
     * @return the result
     */
    @Nonnull
    InfluxQLQueryResult query(@Nonnull final InfluxQLQuery influxQlQuery);

    /**
     * Executes an InfluxQL query against the legacy endpoint.
     * The value extractor is called for each resulting column to convert the string value returned by query into a
     * custom type.
     * <p>
     * <b>Example:</b>
     * <pre>
     * InfluxQLQueryResult result = influxQLQueryApi.query(
     *             new InfluxQLQuery("SELECT FIRST(\"free\") FROM \"influxql\"", DATABASE_NAME)
     *                     .setPrecision(InfluxQLQuery.InfluxQLPrecision.SECONDS),
     *             (columnName, rawValue, resultIndex, seriesName) -&gt; {
     *                 switch (columnName) {
     *                     case "time":
     *                         return Instant.ofEpochSecond(Long.parseLong(rawValue));
     *                     case "first":
     *                         return new BigDecimal(rawValue);
     *                     default:
     *                         throw new IllegalArgumentException("unexpected column " + columnName);
     *                 }
     *             }
     *     );
     * </pre>
     *
     * @param influxQlQuery  the query
     * @param valueExtractor a callback, to convert column values
     * @return the result
     */
    @Nonnull
    InfluxQLQueryResult query(
            @Nonnull InfluxQLQuery influxQlQuery,
            @Nullable InfluxQLQueryResult.Series.ValueExtractor valueExtractor
    );
}
