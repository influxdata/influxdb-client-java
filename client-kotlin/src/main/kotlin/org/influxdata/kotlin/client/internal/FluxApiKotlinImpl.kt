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

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import org.influxdata.client.Arguments
import org.influxdata.client.Cancellable
import org.influxdata.client.LogLevel
import org.influxdata.client.exceptions.InfluxException
import org.influxdata.client.flux.domain.FluxRecord
import org.influxdata.client.flux.domain.FluxTable
import org.influxdata.client.flux.internal.FluxCsvParser.FluxResponseConsumer
import org.influxdata.client.internal.AbstractQueryApi
import org.influxdata.flux.client.FluxConnectionOptions
import org.influxdata.flux.client.internal.AbstractFluxApi
import org.influxdata.flux.client.internal.FluxApiImpl
import org.influxdata.flux.client.internal.FluxService
import org.influxdata.kotlin.client.FluxClientKotlin
import java.io.IOException
import java.util.function.BiConsumer
import java.util.function.Consumer
import java.util.logging.Level
import java.util.logging.Logger

/**
 * @author Jakub Bednar (bednar@github) (30/10/2018 08:51)
 */
internal class FluxApiKotlinImpl(options: FluxConnectionOptions) : AbstractFluxApi<FluxService>(options.okHttpClient, options.url, options.parameters, FluxService::class.java), FluxClientKotlin {

    @Suppress("PrivatePropertyName")
    private val LOG = Logger.getLogger(FluxApiImpl::class.java.name)

    override fun query(query: String): Channel<FluxRecord> {

        Arguments.checkNonEmpty(query, "query")

        val consumer = BiConsumer { channel: Channel<FluxRecord>, record: FluxRecord ->
            runBlocking {
                channel.send(record)
            }
        }

        return query(query, AbstractQueryApi.DEFAULT_DIALECT.toString(), consumer)
    }

    override fun <M> query(query: String, measurementType: Class<M>): Channel<M> {

        Arguments.checkNonEmpty(query, "query")
        Arguments.checkNotNull(measurementType, "measurementType")

        val consumer = BiConsumer { channel: Channel<M>, record: FluxRecord ->
            runBlocking {
                channel.send(resultMapper.toPOJO(record, measurementType))
            }
        }

        return query(query, AbstractQueryApi.DEFAULT_DIALECT.toString(), consumer)
    }

    override fun queryRaw(query: String): Channel<String> {

        Arguments.checkNonEmpty(query, "query")

        return queryRaw(query, AbstractQueryApi.DEFAULT_DIALECT.toString())
    }

    override fun queryRaw(query: String, dialect: String): Channel<String> {

        Arguments.checkNonEmpty(query, "query")
        Arguments.checkNonEmpty(dialect, "dialect")

        val channel = Channel<String>()

        val queryCall = fluxService.query(createBody(dialect, query))

        val consumer = BiConsumer { cancellable: Cancellable, line: String ->

            if (channel.isClosedForSend) {
                cancellable.cancel()
            } else {
                runBlocking {
                    channel.send(line)
                }
            }
        }

        queryRaw(queryCall, consumer, Consumer { channel.close(it) }, Runnable { channel.close() }, true)

        return channel
    }

    override fun ping(): Boolean {

        return try {
            fluxService.ping().execute().isSuccessful
        } catch (e: IOException) {

            LOG.log(Level.WARNING, "Ping request wasn't successful", e)
            false
        }
    }

    override fun version(): String {

        try {
            val execute = fluxService.ping().execute()
            return getVersion(execute)
        } catch (e: IOException) {
            throw InfluxException(e)
        }
    }

    override fun getLogLevel(): LogLevel {
        return getLogLevel(this.loggingInterceptor)
    }

    override fun setLogLevel(logLevel: LogLevel): FluxClientKotlin {

        Arguments.checkNotNull(logLevel, "LogLevel")

        setLogLevel(this.loggingInterceptor, logLevel)

        return this
    }

    private fun <T> query(query: String, dialect: String, consumer: BiConsumer<Channel<T>, FluxRecord>): Channel<T> {

        Arguments.checkNonEmpty(query, "query")
        Arguments.checkNonEmpty(dialect, "dialect")

        val channel = Channel<T>()

        val queryCall = fluxService.query(createBody(dialect, query))

        val responseConsumer = object : FluxResponseConsumer {

            override fun accept(index: Int, cancellable: Cancellable, table: FluxTable) {

            }

            override fun accept(index: Int, cancellable: Cancellable, record: FluxRecord) {

                if (channel.isClosedForSend) {
                    cancellable.cancel()
                } else {
                    consumer.accept(channel, record)
                }
            }
        }

        query(queryCall, responseConsumer, { channel.close(it) }, { channel.close() }, true)
        return channel
    }
}