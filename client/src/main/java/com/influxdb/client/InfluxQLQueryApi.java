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
 * The <code>InfluxQL</code> API can be used with the <code>/query compatibility</code> endpoint which uses the
 * <strong>{@link InfluxQLQuery#getDatabase() database}</strong> and
 * <strong>{@link InfluxQLQuery#getRetentionPolicy() retention policy}</strong> specified in the query request to
 * map the request to an InfluxDB bucket.
 *
 * <p>Note that as of release 7.2 queries using the legacy <code>InfluxQL</code> compatible endpoint can specify
 * the <code>Accept</code> header MIME type.  Two MIME types are supported. </p>
 * <ul>
 *   <li><code>application/csv</code> - client default and legacy value.</li>
 *   <li><code>application/json</code></li>
 * </ul>
 *
 * <p>The selected <code>Accept</code> header mime type impacts the timestamp format returned from the server.</p>
 * <ul>
 *   <li><code>application/csv</code> returns timestamps in the POSIX epoch format.</li>
 *   <li><code>application/json</code> returns timestamps as RFC3339 strings.
 *     <ul>
 *       <li>Caveat.  If <code>InfluxQLQuery.setPrecision()</code> is called before the query is sent, then
 *       the timestamp will be returned as a POSIX epoch reflecting the desired precision, even when using the
 *       <code>application/json</code> MIME type.</li>
 *     </ul>
 *  </li>
 * </ul>
 *
 * <p>To explicitly choose one or the other MIME type new convenience methods are povided: <code>queryCSV</code>
 * and <code>queryJSON</code>. Note that the <code>Accept</code> header MIME type can now also be specified
 * when instantiating the {@link com.influxdb.client.domain.InfluxQLQuery} class.</p>
 *
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
 *     <li>
 *       <a href="https://docs.influxdata.com/influxdb/v2/api/v1-compatibility/#operation/PostQueryV1">
 *         OpenApi generated definitions
 *       </a>
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

    /**
     * Convenience method to specify use of the mime type <code>application/csv</code>
     * in the <code>Accept</code> header.  Result timestamps will be in the Epoch format.
     *
     * @param influxQLQuery the query
     * @return the result
     */
    @Nonnull
    InfluxQLQueryResult queryCSV(@Nonnull final InfluxQLQuery influxQLQuery);

    /**
     * Convenience method to specify use of the mime type <code>application/csv</code>
     * in the <code>Accept</code> header.  Result timestamps will be in the Epoch format.
     *
     * @param influxQLQuery the query
     * @param valueExtractor a callback, to convert column values
     * @return the result
     */
    InfluxQLQueryResult queryCSV(@Nonnull final InfluxQLQuery influxQLQuery,
                                 @Nullable InfluxQLQueryResult.Series.ValueExtractor valueExtractor);

    /**
     * Convenience method to specify use of the mime type <code>application/json</code>
     * in the <code>Accept</code> header.  Result timestamps will be in the RFC3339 format.
     *
     * @param influxQLQuery the query
     * @return the result
     */
    @Nonnull
    InfluxQLQueryResult queryJSON(@Nonnull final InfluxQLQuery influxQLQuery);

    /**
     * Convenience method to specify use of the mime type <code>application/json</code>
     * in the <code>Accept</code> header.  Result timestamps will be in the RFC3339 format.
     *
     * @param influxQLQuery the query
     * @param valueExtractor a callback, to convert column values
     * @return the result
     */
    @Nonnull
    InfluxQLQueryResult queryJSON(@Nonnull final InfluxQLQuery influxQLQuery,
                                 @Nullable InfluxQLQueryResult.Series.ValueExtractor valueExtractor);


}
