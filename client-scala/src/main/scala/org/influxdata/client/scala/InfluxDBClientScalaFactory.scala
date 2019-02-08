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
package org.influxdata.client.scala

import akka.stream.OverflowStrategy
import javax.annotation.Nonnull
import org.influxdata.Arguments
import org.influxdata.client.InfluxDBClientOptions
import org.influxdata.client.scala.internal.InfluxDBClientScalaImpl

/**
 * The Factory that creates a instance of a Flux client.
 * 
 * @author Jakub Bednar (bednar@github) (05/11/2018 10:10)
 */
object InfluxDBClientScalaFactory {

  /**
   * Create a instance of the InfluxDB 2.0 reactive client.
   *
   * @param url the url to connect to the InfluxDB
   * @return client
   * @see [[InfluxDBClientOptions.Builder]]
   */
  @Nonnull def create(url: String): InfluxDBClientScala = {

    val options = InfluxDBClientOptions.builder()
      .url(url)
      .build()

    create(options)
  }

  /**
   * Create a instance of the InfluxDB 2.0 reactive client.
   *
   * @param url      the url to connect to the InfluxDB
   * @param username the username to use in the basic auth
   * @param password the password to use in the basic auth
   * @return client
   * @see [[InfluxDBClientOptions.Builder]]
   */
  @Nonnull def create(url: String, username: String, password: Array[Char]): InfluxDBClientScala = {

    val options = InfluxDBClientOptions.builder()
      .url(url)
      .authenticate(username, password)
      .build()

    create(options)
  }

  /**
   * Create a instance of the InfluxDB 2.0 reactive client.
   *
   * @param url   the url to connect to the InfluxDB
   * @param token the token to use for the authorization
   * @return client
   * @see [[InfluxDBClientOptions.Builder]]
   */
  @Nonnull def create(url: String, token: Array[Char]): InfluxDBClientScala = {

    val options = InfluxDBClientOptions.builder()
      .url(url)
      .authenticateToken(token)
      .build()

    create(options)
  }

  /**
   * Create a instance of the InfluxDB 2.0 reactive client.
   *
   * @param options          the connection configuration
   * @param bufferSize       size of a buffer for incoming responses. Default 10000.
   * @param overflowStrategy Strategy that is used when incoming response cannot fit inside the buffer.
   *                         Default [[akka.stream.OverflowStrategies.Backpressure]].
   * @see [[akka.stream.scaladsl.Source#queue(int, akka.stream.OverflowStrategy)]]
   * @return client
   */
  @Nonnull def create(@Nonnull options: InfluxDBClientOptions,
                      @Nonnull bufferSize: Int = 10000,
                      @Nonnull overflowStrategy: OverflowStrategy = OverflowStrategy.backpressure): InfluxDBClientScala = {

    Arguments.checkNotNull(options, "InfluxDBClientOptions")

    new InfluxDBClientScalaImpl(options, bufferSize, overflowStrategy)
  }
}
