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

    override fun query(query: String, orgID: String): Channel<FluxRecord> {

        Arguments.checkNonEmpty(query, "query")
        Arguments.checkNonEmpty(orgID, "orgID")

        return query(Query().dialect(AbstractInfluxDBClient.DEFAULT_DIALECT).query(query), orgID)
    }

    override fun query(query: Query, orgID: String): Channel<FluxRecord> {

        Arguments.checkNotNull(query, "query")
        Arguments.checkNonEmpty(orgID, "orgID")

        val consumer = BiConsumer { channel: Channel<FluxRecord>, record: FluxRecord ->
            runBlocking {
                channel.send(record)
            }
        }

        return query(query, orgID, consumer)
    }

    override fun <M> query(query: String, orgID: String, measurementType: Class<M>): Channel<M> {

        Arguments.checkNonEmpty(query, "query")
        Arguments.checkNonEmpty(orgID, "orgID")
        Arguments.checkNotNull(measurementType, "measurementType")

        val consumer = BiConsumer { channel: Channel<M>, record: FluxRecord ->
            runBlocking {
                channel.send(resultMapper.toPOJO(record, measurementType))
            }
        }

        return query(Query().dialect(AbstractInfluxDBClient.DEFAULT_DIALECT).query(query), orgID, consumer)
    }

    override fun <M> query(query: Query, orgID: String, measurementType: Class<M>): Channel<M> {

        Arguments.checkNotNull(query, "query")
        Arguments.checkNonEmpty(orgID, "orgID")
        Arguments.checkNotNull(measurementType, "measurementType")

        val consumer = BiConsumer { channel: Channel<M>, record: FluxRecord ->
            runBlocking {
                channel.send(resultMapper.toPOJO(record, measurementType))
            }
        }

        return query(query, orgID, consumer)
    }

    override fun queryRaw(query: String, orgID: String): Channel<String> {

        Arguments.checkNonEmpty(query, "query")
        Arguments.checkNonEmpty(orgID, "orgID")

        return queryRaw(query, AbstractInfluxDBClient.DEFAULT_DIALECT, orgID)
    }

    override fun queryRaw(query: String, dialect: Dialect, orgID: String): Channel<String> {

        Arguments.checkNonEmpty(query, "query")
        Arguments.checkNonEmpty(orgID, "orgID")

        return queryRaw(Query().dialect(dialect).query(query), orgID)
    }

    override fun queryRaw(query: Query, orgID: String): Channel<String> {

        Arguments.checkNotNull(query, "query")
        Arguments.checkNonEmpty(orgID, "orgID")

        val channel = Channel<String>()

        val queryCall = service.postQueryResponseBody(null,  "application/json",
                null, orgID, query)

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

    private fun <T> query(query: Query, orgID: String, consumer: BiConsumer<Channel<T>, FluxRecord>): Channel<T> {

        Arguments.checkNotNull(query, "query")
        Arguments.checkNonEmpty(orgID, "orgID")

        val channel = Channel<T>()

        val queryCall = service.postQueryResponseBody(null, "application/json",
                null, orgID, query)

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