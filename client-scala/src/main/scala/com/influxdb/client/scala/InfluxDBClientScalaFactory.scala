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

import com.influxdb.Arguments
import com.influxdb.client.InfluxDBClientOptions
import com.influxdb.client.scala.internal.InfluxDBClientScalaImpl

import javax.annotation.Nonnull

/**
 * The Factory that creates an instance of a Flux client.
 * 
 * @author Jakub Bednar (bednar@github) (05/11/2018 10:10)
 */
object InfluxDBClientScalaFactory {


  /**
   * Create an instance of the InfluxDB 2.0 client that is configured via `influx2.properties`.
   * The `influx2.properties` has to be located on classpath.
   *
   * @return client
   */
  @Nonnull def create(): InfluxDBClientScala = {

    val options = InfluxDBClientOptions.builder()
      .loadProperties()
      .build()

    create(options)
  }

  /**
   * Create an instance of the InfluxDB 2.0 client. The url could be a connection string with various configurations.
   *
   * e.g.: "http://localhost:8086?readTimeout=5000&amp;connectTimeout=5000&amp;logLevel=BASIC
   *
   * @param connectionString connection string with various configurations.
   * @return client
   */
  @Nonnull def create(connectionString: String): InfluxDBClientScala = {

    val options = InfluxDBClientOptions.builder()
      .url(connectionString)
      .build()

    create(options)
  }

  /**
   * Create an instance of the InfluxDB 2.0 reactive client.
   *
   * The ''username/password'' auth is based on
   * [[http://bit.ly/http-basic-auth HTTP "Basic" authentication]]. The authorization expires when the
   * [[http://bit.ly/session-lengthh time-to-live (TTL)]] (default 60 minutes) is reached
   * and client produces [[com.influxdb.exceptions.UnauthorizedException]].
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
   * Create an instance of the InfluxDB 2.0 reactive client.
   *
   * @param url   the url to connect to the InfluxDB
   * @param token the token to use for the authorization
   * @return client
   * @see [[InfluxDBClientOptions.Builder]]
   */
  @Nonnull def create(url: String, token: Array[Char]): InfluxDBClientScala = {

    create(url, token, null)
  }

  /**
   * Create an instance of the InfluxDB 2.0 reactive client.
   *
   * @param url   the url to connect to the InfluxDB
   * @param token the token to use for the authorization
   * @param org      the name of an organization
   * @return client
   * @see [[InfluxDBClientOptions.Builder]]
   */
  @Nonnull def create(url: String, token: Array[Char], org: String): InfluxDBClientScala = {

    create(url, token, org, null)
  }

  /**
   * Create an instance of the InfluxDB 2.0 reactive client.
   *
   * @param url   the url to connect to the InfluxDB
   * @param token the token to use for the authorization
   * @param org      the name of an organization
   * @param bucket   the name of a bucket
   * @return client
   * @see [[InfluxDBClientOptions.Builder]]
   */
  @Nonnull def create(url: String, token: Array[Char], org: String, bucket: String): InfluxDBClientScala = {

    val options = InfluxDBClientOptions.builder()
      .url(url)
      .authenticateToken(token)
      .org(org)
      .bucket(bucket)
      .build()

    create(options)
  }

  /**
   * Create an instance of the InfluxDB 2.0 reactive client.
   *
   * @param options the connection configuration
   * @return client
   */
  @Nonnull def create(@Nonnull options: InfluxDBClientOptions): InfluxDBClientScala = {

    Arguments.checkNotNull(options, "InfluxDBClientOptions")

    new InfluxDBClientScalaImpl(options)
  }
}
