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
package org.influxdata.client.scala.internal

import java.util.function.BiConsumer

import akka.NotUsed
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.Source
import javax.annotation.Nonnull
import org.influxdata.client.internal.InfluxDBService
import org.influxdata.client.scala.QueryScalaApi
import org.influxdata.internal.AbstractQueryApi
import org.influxdata.query.internal.FluxCsvParser
import org.influxdata.query.{FluxRecord, FluxTable}
import org.influxdata.{Arguments, Cancellable}

import scala.compat.java8.FunctionConverters.asJavaConsumer

/**
 * @author Jakub Bednar (bednar@github) (06/11/2018 08:19)
 */
class QueryScalaApiImpl(@Nonnull influxDBService: InfluxDBService,
                        @Nonnull val bufferSize: Int,
                        @Nonnull val overflowStrategy: OverflowStrategy)

  extends AbstractQueryApi()
    with QueryScalaApi {

  Arguments.checkNotNull(overflowStrategy, "overflowStrategy")
  Arguments.checkNotNull(bufferSize, "bufferSize")

  /**
   * Executes the Flux query against the InfluxDB and asynchronously stream [[FluxRecord]]s to [[Stream]].
   *
   * @param query the flux query to execute
   * @return the stream of [[FluxRecord]]s
   */
  override def query(@Nonnull query: String, @Nonnull orgID: String): Source[FluxRecord, NotUsed] = {

    Arguments.checkNonEmpty(query, "query")
    Arguments.checkNonEmpty(orgID, "orgID")

    Source
      .single(query)
      .map(it => influxDBService.query(orgID, createBody(AbstractQueryApi.DEFAULT_DIALECT.toString, it)))
      .flatMapConcat(queryCall => {
        Source.queue[FluxRecord](bufferSize, overflowStrategy)
          .mapMaterializedValue(queue => {

            val eventualDone = queue.watchCompletion()
            val consumer = new FluxCsvParser.FluxResponseConsumer() {

              override
              def accept(index: Int, @Nonnull cancellable: Cancellable, @Nonnull table: FluxTable): Unit = {
              }

              override
              def accept(index: Int, @Nonnull cancellable: Cancellable, @Nonnull record: FluxRecord): Unit = {
                if (eventualDone.isCompleted) {
                  cancellable.cancel()
                } else {
                  queue.offer(record)
                }
              }
            }

            val onError = asJavaConsumer[Throwable](t =>  queue.fail(t))

            this.query(queryCall, consumer, onError, () => queue.complete, true)
          })
    })
  }

  /**
   * Executes the Flux query against the InfluxDB and asynchronously stream measurements to [[Stream]].
   *
   * @param query           the flux query to execute
   * @param measurementType the measurement (POJO)
   * @tparam M the type of the measurement (POJO)
   * @return the stream of measurements
   */
  override def query[M](@Nonnull query: String, @Nonnull orgID: String, @Nonnull measurementType: Class[M]): Source[M, NotUsed] = {

    Arguments.checkNonEmpty(query, "query")
    Arguments.checkNonEmpty(orgID, "orgID")
    Arguments.checkNotNull(measurementType, "measurementType")

    this.query(query, orgID).map(t => resultMapper.toPOJO(t, measurementType))
  }

  /**
   * Executes the Flux query against the InfluxDB and asynchronously stream response to [[Stream]].
   *
   * @param query the flux query to execute
   * @return the response stream
   */
  override def queryRaw(@Nonnull query: String, @Nonnull orgID: String): Source[String, NotUsed] = {

    Arguments.checkNonEmpty(query, "query")
    Arguments.checkNonEmpty(orgID, "orgID")

    queryRaw(query, AbstractQueryApi.DEFAULT_DIALECT.toString, orgID)
  }

  /**
   * Executes the Flux query against the InfluxDB and asynchronously stream response to [[Stream]].
   *
   * @param query   the flux query to execute
   * @param dialect Dialect is an object defining the options to use when encoding the response.
   *                [[http://bit.ly/flux-dialect See dialect SPEC]].
   * @return the response stream
   */
  override def queryRaw(query: String, dialect: String, orgID: String): Source[String, NotUsed] = {

    Arguments.checkNonEmpty(query, "query")
    Arguments.checkNonEmpty(orgID, "orgID")

    Source
      .single(query)
      .map(it => influxDBService.query(orgID, createBody(dialect, it)))
      .flatMapConcat(queryCall => {
        Source.queue[String](bufferSize, overflowStrategy)
          .mapMaterializedValue(queue => {

            val eventualDone = queue.watchCompletion()

            val onResponse = new BiConsumer[Cancellable, String] {
              override def accept(cancellable: Cancellable, line: String): Unit = {
                if (eventualDone.isCompleted) {
                  cancellable.cancel()
                } else {
                  queue.offer(line)
                }
              }
            }

            val onError = asJavaConsumer[Throwable](t =>  queue.fail(t))

            this.queryRaw(queryCall, onResponse, onError, () => queue.complete, true)
          })
      })
  }
}
