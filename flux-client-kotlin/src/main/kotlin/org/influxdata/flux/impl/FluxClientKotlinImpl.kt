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
package org.influxdata.flux.impl

import kotlinx.coroutines.channels.Channel
import org.influxdata.flux.FluxClientKotlin
import org.influxdata.flux.domain.FluxRecord
import org.influxdata.flux.option.FluxConnectionOptions
import org.influxdata.platform.Arguments
import org.influxdata.platform.rest.LogLevel

/**
 * @author Jakub Bednar (bednar@github) (30/10/2018 08:51)
 */
internal class FluxClientKotlinImpl(options: FluxConnectionOptions) : AbstractFluxClient<FluxService>(options.okHttpClient, options.url, options.parameters, FluxService::class.java), FluxClientKotlin {

    override fun query(query: String): Channel<FluxRecord> {
        TODO("not implemented")
    }

    override fun <M> query(query: String, measurementType: Class<M>): Channel<M> {
        TODO("not implemented")
    }

    override fun queryRaw(query: String): Channel<String> {
        TODO("not implemented")
    }

    override fun queryRaw(query: String, dialect: String): Channel<String> {
        TODO("not implemented")
    }

    override fun ping(): Boolean {
        TODO("not implemented")
    }

    override fun version(): String {
        TODO("not implemented")
    }

    override fun getLogLevel(): LogLevel {
        return getLogLevel(this.loggingInterceptor)
    }

    override fun setLogLevel(logLevel: LogLevel): FluxClientKotlin {
        
        Arguments.checkNotNull(logLevel, "LogLevel")

        setLogLevel(this.loggingInterceptor, logLevel)

        return this
    }
}