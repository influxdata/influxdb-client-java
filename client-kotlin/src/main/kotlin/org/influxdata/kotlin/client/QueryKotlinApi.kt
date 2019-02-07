/**
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

package org.influxdata.kotlin.client

import kotlinx.coroutines.channels.Channel
import org.influxdata.client.flux.domain.FluxRecord

/**
 * The client that allows perform Flux queries against the InfluxDB /api/v2/query endpoint.
 *
 * @author Jakub Bednar (bednar@github) (29/10/2018 10:45)
 */
interface QueryKotlinApi {

    /**
     * Executes the Flux query against the InfluxDB and asynchronously stream [FluxRecord]s to [Channel].
     *
     * @param query the flux query to execute
     * @param orgID           specifies the source organization
     * @return the stream of [FluxRecord]s
     */
    fun query(query: String, orgID: String): Channel<FluxRecord>

    /**
     * Executes the Flux query against the InfluxDB and asynchronously stream measurements to [Channel].
     *
     * @param query the flux query to execute
     * @param orgID           specifies the source organization
     * @param <M> the type of the measurement (POJO)
     * @return the stream of measurements
     */
    fun <M> query(query: String, orgID: String, measurementType: Class<M>): Channel<M>

    /**
     * Executes the Flux query against the InfluxDB and asynchronously stream response to [Channel].
     *
     * @param query the flux query to execute
     * @param orgID           specifies the source organization
     * @return the response stream
     */
    fun queryRaw(query: String, orgID: String): Channel<String>

    /**
     * Executes the Flux query against the InfluxDB and asynchronously stream response to [Channel].
     *
     * @param query the flux query to execute
     * @param orgID           specifies the source organization
     * @param dialect    Dialect is an object defining the options to use when encoding the response.
     *                  [See dialect SPEC](http://bit.ly/flux-dialect).
     * @return the response stream
     */
    fun queryRaw(query: String, dialect: String, orgID: String): Channel<String>

}