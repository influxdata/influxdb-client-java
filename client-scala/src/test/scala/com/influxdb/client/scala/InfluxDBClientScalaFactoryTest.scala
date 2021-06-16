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

import com.influxdb.LogLevel
import com.influxdb.client.InfluxDBClientOptions
import com.influxdb.client.internal.AbstractInfluxDBClient
import okhttp3.OkHttpClient
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import retrofit2.Retrofit

/**
 * @author Jakub Bednar (bednar@github) (05/11/2018 09:22)
 */
class InfluxDBClientScalaFactoryTest extends AnyFunSuite with Matchers {

  test("connect") {

    val client = InfluxDBClientScalaFactory.create("http://localhost:8093")

    client should not be null
  }

  test("loadFromProperties") {

    val utils = new InfluxDBUtils {}

    val client = InfluxDBClientScalaFactory.create()
    val options = utils.getDeclaredField(client, "options", classOf[AbstractInfluxDBClient]).asInstanceOf[InfluxDBClientOptions]

    options.getUrl should be("http://localhost:9999/")
    options.getOrg should be("my-org")
    options.getBucket should be("my-bucket")
    options.getToken should be("my-token".toCharArray)
    options.getLogLevel should be(LogLevel.BODY)
    client.getLogLevel should be(LogLevel.BODY)

    val retrofit = utils.getDeclaredField(client, "retrofit", classOf[AbstractInfluxDBClient]).asInstanceOf[Retrofit]
    val okHttpClient = retrofit.callFactory.asInstanceOf[OkHttpClient]

    okHttpClient.readTimeoutMillis should be(5000)
    okHttpClient.writeTimeoutMillis should be(10000)
    okHttpClient.connectTimeoutMillis should be(18000)
  }
}
