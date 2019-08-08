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
package com.influxdb.client.scala

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.influxdb.client.domain.{Dialect, Query}
import com.influxdb.query.FluxRecord
import javax.annotation.Nonnull

/**
 * The client that allows perform Flux queries against the InfluxDB /api/v2/query endpoint.
 *
 * @author Jakub Bednar (bednar@github) (02/11/2018 09:48)
 */
trait QueryScalaApi {

  /**
   * Executes the Flux query against the InfluxDB and asynchronously stream [[FluxRecord]]s to [[Stream]].
   *
   * @param query the flux query to execute
   * @param org specifies the source organization
   * @return the stream of [[FluxRecord]]s
   */
  @Nonnull def query(@Nonnull query: String, @Nonnull org: String): Source[FluxRecord, NotUsed]

  /**
   * Executes the Flux query against the InfluxDB and asynchronously stream [[FluxRecord]]s to [[Stream]].
   *
   * @param query the flux query to execute
   * @param org specifies the source organization
   * @return the stream of [[FluxRecord]]s
   */
  @Nonnull def query(@Nonnull query: Query, @Nonnull org: String): Source[FluxRecord, NotUsed]

  /**
   * Executes the Flux query against the InfluxDB and asynchronously stream measurements to [[Stream]].
   *
   * @param query           the flux query to execute
   * @param org           specifies the source organization
   * @param measurementType the measurement (POJO)
   * @tparam M the type of the measurement (POJO)
   * @return the stream of measurements
   */
  @Nonnull def query[M](@Nonnull query: String, @Nonnull org: String, @Nonnull measurementType: Class[M]): Source[M, NotUsed]

  /**
   * Executes the Flux query against the InfluxDB and asynchronously stream measurements to [[Stream]].
   *
   * @param query           the flux query to execute
   * @param org           specifies the source organization
   * @param measurementType the measurement (POJO)
   * @tparam M the type of the measurement (POJO)
   * @return the stream of measurements
   */
  @Nonnull def query[M](@Nonnull query: Query, @Nonnull org: String, @Nonnull measurementType: Class[M]): Source[M, NotUsed]

  /**
   * Executes the Flux query against the InfluxDB and asynchronously stream response to [[Stream]].
   *
   * @param query the flux query to execute
   * @param org specifies the source organization
   * @return the response stream
   */
  @Nonnull def queryRaw(@Nonnull query: String, @Nonnull org: String): Source[String, NotUsed]

  /**
   * Executes the Flux query against the InfluxDB and asynchronously stream response to [[Stream]].
   *
   * @param query   the flux query to execute
   * @param dialect Dialect is an object defining the options to use when encoding the response.
   *                [[http://bit.ly/flux-dialect See dialect SPEC]].
   * @param org   specifies the source organization
   * @return the response stream
   */
  @Nonnull def queryRaw(@Nonnull query: String, @Nonnull dialect: Dialect, @Nonnull org: String): Source[String, NotUsed]

  /**
   * Executes the Flux query against the InfluxDB and asynchronously stream response to [[Stream]].
   *
   * @param query   the flux query to execute
   * @param org   specifies the source organization
   * @return the response stream
   */
  @Nonnull def queryRaw(@Nonnull query: Query, @Nonnull org: String): Source[String, NotUsed]
}
