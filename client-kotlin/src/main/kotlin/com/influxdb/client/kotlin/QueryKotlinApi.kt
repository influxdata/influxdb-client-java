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
package com.influxdb.client.kotlin

import com.influxdb.client.domain.Dialect
import com.influxdb.client.domain.Query
import com.influxdb.query.FluxRecord
import kotlinx.coroutines.channels.Channel

/**
 * The client that allows perform Flux queries against the InfluxDB /api/v2/query endpoint.
 *
 * For parametrized queries use [Query] object, see [com.influxdb.client.QueryApi] in Java module
 * for more details.
 *
 * @author Jakub Bednar (bednar@github) (29/10/2018 10:45)
 */
interface QueryKotlinApi {

    /**
     * Executes the Flux query against the InfluxDB and asynchronously stream
     * [com.influxdb.query.FluxRecord]s to [kotlinx.coroutines.channels.Channel].
     *
     * The [com.influxdb.client.InfluxDBClientOptions.getOrg] will be used as source organization.
     *
     * @param query the flux query to execute
     * @return the stream of [com.influxdb.query.FluxRecord]s
     */
    fun query(query: String): Channel<FluxRecord>

    /**
     * Executes the Flux query against the InfluxDB and asynchronously stream
     * [com.influxdb.query.FluxRecord]s to [kotlinx.coroutines.channels.Channel].
     *
     * @param query the flux query to execute
     * @param org specifies the source organization
     * @return the stream of [com.influxdb.query.FluxRecord]s
     */
    fun query(query: String, org: String): Channel<FluxRecord>

    /**
     * Executes the Flux query against the InfluxDB and asynchronously stream
     * [com.influxdb.query.FluxRecord]s to [kotlinx.coroutines.channels.Channel].
     *
     * The [com.influxdb.client.InfluxDBClientOptions.getOrg] will be used as source organization.
     *
     * @param query the flux query to execute
     * @return the stream of [com.influxdb.query.FluxRecord]s
     */
    fun query(query: Query): Channel<FluxRecord>

    /**
     * Executes the Flux query against the InfluxDB and asynchronously stream
     * [com.influxdb.query.FluxRecord]s to [kotlinx.coroutines.channels.Channel].
     *
     * @param query the flux query to execute
     * @param org specifies the source organization
     * @return the stream of [com.influxdb.query.FluxRecord]s
     */
    fun query(query: Query, org: String): Channel<FluxRecord>

    /**
     * Executes the Flux query against the InfluxDB and asynchronously stream measurements to
     * [kotlinx.coroutines.channels.Channel].
     *
     * The [com.influxdb.client.InfluxDBClientOptions.getOrg] will be used as source organization.
     *
     * @param query the flux query to execute
     * @param <M> the type of the measurement (POJO)
     * @return the stream of measurements
     */
    fun <M> query(query: String, measurementType: Class<M>): Channel<M>

    /**
     * Executes the Flux query against the InfluxDB and asynchronously stream measurements to
     * [kotlinx.coroutines.channels.Channel].
     *
     * @param query the flux query to execute
     * @param org specifies the source organization
     * @param <M> the type of the measurement (POJO)
     * @return the stream of measurements
     */
    fun <M> query(query: String, org: String, measurementType: Class<M>): Channel<M>

    /**
     * Executes the Flux query against the InfluxDB and asynchronously stream measurements to
     * [kotlinx.coroutines.channels.Channel].
     *
     * The [com.influxdb.client.InfluxDBClientOptions.getOrg] will be used as source organization.
     *
     * @param query the flux query to execute
     * @param <M> the type of the measurement (POJO)
     * @return the stream of measurements
     */
    fun <M> query(query: Query, measurementType: Class<M>): Channel<M>

    /**
     * Executes the Flux query against the InfluxDB and asynchronously stream measurements to
     * [kotlinx.coroutines.channels.Channel].
     *
     * @param query the flux query to execute
     * @param org specifies the source organization
     * @param <M> the type of the measurement (POJO)
     * @return the stream of measurements
     */
    fun <M> query(query: Query, org: String, measurementType: Class<M>): Channel<M>

    /**
     * Executes the Flux query against the InfluxDB and asynchronously stream response to
     * [kotlinx.coroutines.channels.Channel].
     *
     * The [com.influxdb.client.InfluxDBClientOptions.getOrg] will be used as source organization.
     *
     * @param query the flux query to execute
     * @return the response stream
     */
    fun queryRaw(query: String): Channel<String>

    /**
     * Executes the Flux query against the InfluxDB and asynchronously stream response to
     * [kotlinx.coroutines.channels.Channel].
     *
     * @param query the flux query to execute
     * @param org specifies the source organization
     * @return the response stream
     */
    fun queryRaw(query: String, org: String): Channel<String>

    /**
     * Executes the Flux query against the InfluxDB and asynchronously stream response to
     * [kotlinx.coroutines.channels.Channel].
     *
     * The [com.influxdb.client.InfluxDBClientOptions.getOrg] will be used as source organization.
     *
     * @param query the flux query to execute
     * @param dialect    Dialect is an object defining the options to use when encoding the response.
     *                  [See dialect SPEC](http://bit.ly/flux-dialect).
     * @return the response stream
     */
    fun queryRaw(query: String, dialect: Dialect): Channel<String>

    /**
     * Executes the Flux query against the InfluxDB and asynchronously stream response to
     * [kotlinx.coroutines.channels.Channel].
     *
     * @param query the flux query to execute
     * @param org  specifies the source organization
     * @param dialect    Dialect is an object defining the options to use when encoding the response.
     *                  [See dialect SPEC](http://bit.ly/flux-dialect).
     * @return the response stream
     */
    fun queryRaw(query: String, dialect: Dialect, org: String): Channel<String>

    /**
     * Executes the Flux query against the InfluxDB and asynchronously stream response to
     * [kotlinx.coroutines.channels.Channel].
     *
     * The [com.influxdb.client.InfluxDBClientOptions.getOrg] will be used as source organization.
     *
     * @param query the flux query to execute
     * @return the response stream
     */
    fun queryRaw(query: Query): Channel<String>

    /**
     * Executes the Flux query against the InfluxDB and asynchronously stream response to
     * [kotlinx.coroutines.channels.Channel].
     *
     * @param query the flux query to execute
     * @param org specifies the source organization
     * @return the response stream
     */
    fun queryRaw(query: Query, org: String): Channel<String>
}