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
package com.influxdb.client.kotlin

import com.influxdb.LogLevel
import com.influxdb.client.domain.Check

/**
 * The reference Kotlin client that allows query and write for the InfluxDB 2.0 by Kotlin Channel coroutines.
 * 
 * @author Jakub Bednar (bednar@github) (07/02/2019 13:11)
 */
interface InfluxDBClientKotlin {

    /**
     * Get the Query client.
     *
     * @return the new client instance for the Query API
     */
    fun getQueryKotlinApi() : QueryKotlinApi

    /**
     * Get the health of an instance.
     *
     * @return health of an instance
     */
    fun health(): Check

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
     * @return the [InfluxDBClientKotlin] instance to be able to use it in a fluent manner.
     */
    fun setLogLevel(logLevel: LogLevel): InfluxDBClientKotlin

    /**
     * Enable Gzip compress for http request body.
     *
     * Currently only the "Write" endpoint supports the Gzip compression.
     *
     * @return the [InfluxDBClientKotlin] instance to be able to use it in a fluent manner.
     */
    fun enableGzip(): InfluxDBClientKotlin

    /**
     * Disable Gzip compress for http request body.
     *
     * @return the [InfluxDBClientKotlin] instance to be able to use it in a fluent manner.
     */
    fun disableGzip(): InfluxDBClientKotlin

    /**
     * Returns whether Gzip compress for http request body is enabled.
     *
     * @return true if gzip is enabled.
     */
    fun isGzipEnabled(): Boolean

    /**
     * Shutdown and close the client.
     */
    fun close()
}