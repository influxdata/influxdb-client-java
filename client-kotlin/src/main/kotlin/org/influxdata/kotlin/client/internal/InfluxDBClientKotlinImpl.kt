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
package org.influxdata.kotlin.client.internal

import org.influxdata.client.LogLevel
import org.influxdata.java.client.InfluxDBClientOptions
import org.influxdata.java.client.domain.Health
import org.influxdata.java.client.internal.AbstractInfluxDBClient
import org.influxdata.java.client.internal.InfluxDBService
import org.influxdata.kotlin.client.InfluxDBClientKotlin
import org.influxdata.kotlin.client.QueryKotlinApi

/**
 * @author Jakub Bednar (bednar@github) (07/02/2019 13:21)
 */
internal class InfluxDBClientKotlinImpl(options: InfluxDBClientOptions) : AbstractInfluxDBClient<InfluxDBService>(options, InfluxDBService::class.java), InfluxDBClientKotlin {

    override fun getQueryKotlinApi(): QueryKotlinApi {
        return QueryKotlinApiImpl(influxDBService)
    }

    override fun health(): Health {
        return health(influxDBService.health())
    }

    override fun getLogLevel(): LogLevel {
        return getLogLevel(this.loggingInterceptor)
    }

    override fun setLogLevel(logLevel: LogLevel): InfluxDBClientKotlin {
        setLogLevel(this.loggingInterceptor, logLevel)

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
        return this.gzipInterceptor.isEnabledGzip
    }
}