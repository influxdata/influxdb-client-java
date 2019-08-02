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

import com.influxdb.Arguments
import com.influxdb.Cancellable
import com.influxdb.client.domain.Dialect
import com.influxdb.client.domain.Query
import com.influxdb.client.internal.AbstractInfluxDBClient
import com.influxdb.client.kotlin.QueryKotlinApi
import com.influxdb.client.service.QueryService
import com.influxdb.internal.AbstractQueryApi
import com.influxdb.query.FluxRecord
import com.influxdb.query.FluxTable
import com.influxdb.query.internal.FluxCsvParser.FluxResponseConsumer
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import java.util.function.BiConsumer
import java.util.function.Consumer

/**
 * @author Jakub Bednar (bednar@github) (30/10/2018 08:51)
 */
internal class QueryKotlinApiImpl(private val service: QueryService) : AbstractQueryApi(), QueryKotlinApi {

    override fun query(query: String, org: String): Channel<FluxRecord> {

        Arguments.checkNonEmpty(query, "query")
        Arguments.checkNonEmpty(org, "org")

        return query(Query().dialect(AbstractInfluxDBClient.DEFAULT_DIALECT).query(query), org)
    }

    override fun query(query: Query, org: String): Channel<FluxRecord> {

        Arguments.checkNotNull(query, "query")
        Arguments.checkNonEmpty(org, "org")

        val consumer = BiConsumer { channel: Channel<FluxRecord>, record: FluxRecord ->
            runBlocking {
                channel.send(record)
            }
        }

        return query(query, org, consumer)
    }

    override fun <M> query(query: String, org: String, measurementType: Class<M>): Channel<M> {

        Arguments.checkNonEmpty(query, "query")
        Arguments.checkNonEmpty(org, "org")
        Arguments.checkNotNull(measurementType, "measurementType")

        val consumer = BiConsumer { channel: Channel<M>, record: FluxRecord ->
            runBlocking {
                channel.send(resultMapper.toPOJO(record, measurementType))
            }
        }

        return query(Query().dialect(AbstractInfluxDBClient.DEFAULT_DIALECT).query(query), org, consumer)
    }

    override fun <M> query(query: Query, org: String, measurementType: Class<M>): Channel<M> {

        Arguments.checkNotNull(query, "query")
        Arguments.checkNonEmpty(org, "org")
        Arguments.checkNotNull(measurementType, "measurementType")

        val consumer = BiConsumer { channel: Channel<M>, record: FluxRecord ->
            runBlocking {
                channel.send(resultMapper.toPOJO(record, measurementType))
            }
        }

        return query(query, org, consumer)
    }

    override fun queryRaw(query: String, org: String): Channel<String> {

        Arguments.checkNonEmpty(query, "query")
        Arguments.checkNonEmpty(org, "org")

        return queryRaw(query, AbstractInfluxDBClient.DEFAULT_DIALECT, org)
    }

    override fun queryRaw(query: String, dialect: Dialect, org: String): Channel<String> {

        Arguments.checkNonEmpty(query, "query")
        Arguments.checkNonEmpty(org, "org")

        return queryRaw(Query().dialect(dialect).query(query), org)
    }

    override fun queryRaw(query: Query, org: String): Channel<String> {

        Arguments.checkNotNull(query, "query")
        Arguments.checkNonEmpty(org, "org")

        val channel = Channel<String>()

        val queryCall = service.postQueryResponseBody(null,  "application/json",
                null, null, org, query)

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

    private fun <T> query(query: Query, org: String, consumer: BiConsumer<Channel<T>, FluxRecord>): Channel<T> {

        Arguments.checkNotNull(query, "query")
        Arguments.checkNonEmpty(org, "org")

        val channel = Channel<T>()

        val queryCall = service.postQueryResponseBody(null, "application/json",
                null, null, org, query)

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