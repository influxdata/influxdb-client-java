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
package org.influxdata.flux

import javax.annotation.Nonnull
import org.influxdata.flux.domain.FluxRecord
import org.influxdata.platform.rest.LogLevel

/**
 * The client that allows perform Flux queries against the InfluxDB /api/v2/query endpoint.
 *
 * @author Jakub Bednar (bednar@github) (02/11/2018 09:48)
 */
trait FluxClientScala {

  /**
   * Executes the Flux query against the InfluxDB and asynchronously stream [[FluxRecord]]s to [[Stream]].
   *
   * @param query the flux query to execute
   * @return the stream of [[FluxRecord]]s
   */
  @Nonnull def query(@Nonnull query: String): Stream[FluxRecord]

  /**
   * Executes the Flux query against the InfluxDB and asynchronously stream measurements to [[Stream]].
   *
   * @param query           the flux query to execute
   * @param measurementType the measurement (POJO)
   * @tparam M the type of the measurement (POJO)
   * @return the stream of measurements
   */
  @Nonnull def query[M](@Nonnull query: String, @Nonnull measurementType: Class[M]): Stream[M]

  /**
   * Executes the Flux query against the InfluxDB and asynchronously stream response to [[Stream]].
   *
   * @param query the flux query to execute
   * @return the response stream
   */
  @Nonnull def queryRaw(@Nonnull query: String): Stream[String]

  /**
   * Executes the Flux query against the InfluxDB and asynchronously stream response to [[Stream]].
   *
   * @param query   the flux query to execute
   * @param dialect Dialect is an object defining the options to use when encoding the response.
   *                [[http://bit.ly/flux-dialect See dialect SPEC]].
   * @return the response stream
   */
  @Nonnull def queryRaw(@Nonnull query: String, @Nonnull dialect: String): Stream[String]

  /**
   * Check the status of InfluxDB Server.
   *
   * @return `true` if server is healthy otherwise return `false`
   */
  @Nonnull def ping: Boolean

  /**
   * Returns the version of the connected InfluxDB Server.
   *
   * @return the version String, otherwise unknown.
   */
  @Nonnull def version: String

  /**
   * Gets the [[LogLevel]] that is used for logging requests and responses.
   *
   * @return the [[LogLevel]] that is used for logging requests and responses
   */
  @Nonnull def getLogLevel: LogLevel

  /**
   * Sets the log level for the request and response information.
   *
   * @param logLevel the log level to set.
   * @return the FluxClient instance to be able to use it in a fluent manner.
   */
  @Nonnull def setLogLevel(@Nonnull logLevel: LogLevel): FluxClient

  /**
   * Shutdown and close the client.
   */
  def close(): Unit
}
