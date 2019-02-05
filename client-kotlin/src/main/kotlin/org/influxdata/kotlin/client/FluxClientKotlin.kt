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
import org.influxdata.client.LogLevel
import org.influxdata.client.flux.domain.FluxRecord

/**
 * The client that allows perform Flux queries against the InfluxDB /api/v2/query endpoint.
 *
 * @author Jakub Bednar (bednar@github) (29/10/2018 10:45)
 */
interface FluxClientKotlin {

    /**
     * Executes the Flux query against the InfluxDB and asynchronously stream [FluxRecord]s to [Channel].
     *
     * @param query the flux query to execute
     * @return the stream of [FluxRecord]s
     */
    fun query(query: String): Channel<FluxRecord>

    /**
     * Executes the Flux query against the InfluxDB and asynchronously stream measurements to [Channel].
     *
     * @param query the flux query to execute
     * @param <M> the type of the measurement (POJO)
     * @return the stream of measurements
     */
    fun <M> query(query: String, measurementType: Class<M>): Channel<M>

    /**
     * Executes the Flux query against the InfluxDB and asynchronously stream response to [Channel].
     *
     * @param query the flux query to execute
     * @return the response stream
     */
    fun queryRaw(query: String): Channel<String>

    /**
     * Executes the Flux query against the InfluxDB and asynchronously stream response to [Channel].
     *
     * @param query the flux query to execute
     * @param dialect    Dialect is an object defining the options to use when encoding the response.
     *                  [See dialect SPEC](http://bit.ly/flux-dialect).
     * @return the response stream
     */
    fun queryRaw(query: String, dialect: String): Channel<String>

    /**
     * Check the status of InfluxDB Server.
     *
     * @return [Boolean#TRUE] if server is healthy otherwise return [Boolean#FALSE]
     */
    fun ping(): Boolean

    /**
     * Returns the version of the connected InfluxDB Server.
     *
     * @return the version String, otherwise unknown.
     */
    fun version(): String

    /**
     * Gets the [LogLevel] that is used for logging requests and responses.
     *
     * @return the [LogLevel] that is used for logging requests and responses
     */
    fun getLogLevel(): LogLevel

    /**
     * Sets the log level for the request and response information.
     *
     * @param logLevel the log level to set.
     * @return the FluxClientKotlin instance to be able to use it in a fluent manner.
     */
    fun setLogLevel(logLevel: LogLevel): FluxClientKotlin

    /**
     * Shutdown and close the client.
     */
    fun close()
}