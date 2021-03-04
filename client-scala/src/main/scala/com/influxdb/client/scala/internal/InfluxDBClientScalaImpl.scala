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
package com.influxdb.client.scala.internal

import com.influxdb.LogLevel
import com.influxdb.client.InfluxDBClientOptions
import com.influxdb.client.domain.HealthCheck
import com.influxdb.client.internal.AbstractInfluxDBClient
import com.influxdb.client.scala.{InfluxDBClientScala, QueryScalaApi}
import com.influxdb.client.service.QueryService

import javax.annotation.Nonnull

/**
 * @author Jakub Bednar (bednar@github) (08/02/2019 09:26)
 */
class InfluxDBClientScalaImpl(@Nonnull options: InfluxDBClientOptions) extends AbstractInfluxDBClient(options, "scala") with InfluxDBClientScala {
  /**
   * Get the Query client.
   *
   * @return the new client instance for the Query API
   */
  override def getQueryScalaApi(): QueryScalaApi = new QueryScalaApiImpl(retrofit.create(classOf[QueryService]), options)

  /**
   * Get the health of an instance.
   *
   * @return health of an instance
   */
  override def health: HealthCheck = health(healthService.getHealth(null))

  /**
   * Gets the [[LogLevel]] that is used for logging requests and responses.
   *
   * @return the [[LogLevel]] that is used for logging requests and responses
   */
  override def getLogLevel: LogLevel = getLogLevel(loggingInterceptor)

  /**
   * Sets the log level for the request and response information.
   *
   * @param logLevel the log level to set.
   * @return the [[InfluxDBClientScala]] instance to be able to use it in a fluent manner.
   */
  override def setLogLevel(logLevel: LogLevel): InfluxDBClientScala = {
    setLogLevel(loggingInterceptor, logLevel)
    this
  }

  /**
   * Enable Gzip compress for http requests.
   *
   * Currently only the "Write" and "Query" endpoints supports the Gzip compression.
   *
   * @return the [[InfluxDBClientScala]] instance to be able to use it in a fluent manner.
   */
  override def enableGzip(): InfluxDBClientScala = {
    gzipInterceptor.enableGzip()
    this
  }

  /**
   * Disable Gzip compress for http request body.
   *
   * @return the [[InfluxDBClientScala]] instance to be able to use it in a fluent manner.
   */
  override def disableGzip(): InfluxDBClientScala = {
    gzipInterceptor.disableGzip()
    this
  }

  /**
   * Returns whether Gzip compress for http request body is enabled.
   *
   * @return true if gzip is enabled.
   */
  override def isGzipEnabled: Boolean = gzipInterceptor.isEnabledGzip
}
