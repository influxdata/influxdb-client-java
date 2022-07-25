/*
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
import com.influxdb.client.domain.HealthCheck
import org.scalatest.matchers.should.Matchers

/**
 * @author Jakub Bednar (bednar@github) (06/11/2018 09:52)
 */
class ITInfluxDBClientScala extends AbstractITQueryScalaApi with Matchers {

  before {
    setUp()
  }

  test("query client") {

    influxDBClient.getQueryScalaApi() should not be null
  }

  test("health") {

    val health = influxDBClient.health

    health should not be null
    health.getStatus should be(HealthCheck.StatusEnum.PASS)
    health.getMessage should be("ready for queries and writes")
  }

  test("health not running") {

    val client = InfluxDBClientScalaFactory.create("http://localhost:8099")

    val health = client.health

    health should not be null
    health.getStatus should be(HealthCheck.StatusEnum.FAIL)
    health.getMessage should startWith("Failed to connect to")

    client.close()
  }

  test("ping") {

    val ping = influxDBClient.ping

    ping should be(true)
  }

  test("ping not running") {
    val clientNotRunning = InfluxDBClientScalaFactory.create("http://localhost:8099")

    val ping = clientNotRunning.ping
    ping should be(false)

    clientNotRunning.close()
  }

  test("version") {
    val version = influxDBClient.version

    version should not be empty
  }

  test("version not running") {
    val clientNotRunning = InfluxDBClientScalaFactory.create("http://localhost:8099")

    assertThrows[com.influxdb.exceptions.InfluxException] { // Result type: Assertion
      clientNotRunning.version
    }

    clientNotRunning.close()
  }

  test("log level") {

    influxDBClient.getLogLevel should be(LogLevel.NONE)

    influxDBClient.setLogLevel(LogLevel.HEADERS)

    influxDBClient.getLogLevel should be(LogLevel.HEADERS)
  }

  test("gzip") {

    influxDBClient.isGzipEnabled should be(false)

    influxDBClient.enableGzip()
    influxDBClient.isGzipEnabled should be(true)

    influxDBClient.disableGzip()
    influxDBClient.isGzipEnabled should be(false)
  }
}
