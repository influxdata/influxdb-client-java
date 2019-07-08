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
package com.influxdb.client.kotlin.internal

import com.influxdb.LogLevel
import com.influxdb.client.InfluxDBClientOptions
import com.influxdb.client.domain.Check
import com.influxdb.client.internal.AbstractInfluxDBClient
import com.influxdb.client.kotlin.InfluxDBClientKotlin
import com.influxdb.client.kotlin.QueryKotlinApi
import com.influxdb.client.service.QueryService

/**
 * @author Jakub Bednar (bednar@github) (07/02/2019 13:21)
 */
internal class InfluxDBClientKotlinImpl(options: InfluxDBClientOptions) : AbstractInfluxDBClient(options), InfluxDBClientKotlin {

    override fun getQueryKotlinApi(): QueryKotlinApi {
        return QueryKotlinApiImpl(retrofit.create<QueryService>(QueryService::class.java))
    }

    override fun health(): Check {
        return health(healthService.getHealth(null))
    }

    override fun getLogLevel(): LogLevel {
        return getLogLevel(loggingInterceptor)
    }

    override fun setLogLevel(logLevel: LogLevel): InfluxDBClientKotlin {
        setLogLevel(loggingInterceptor, logLevel)

        return this
    }

    override fun enableGzip(): InfluxDBClientKotlin {
        this.gzipInterceptor.enableGzip()

        return this
    }

    override fun disableGzip(): InfluxDBClientKotlin {
        this.gzipInterceptor.disableGzip()

        return this
    }

    override fun isGzipEnabled(): Boolean {
        return gzipInterceptor.isEnabledGzip
    }
}