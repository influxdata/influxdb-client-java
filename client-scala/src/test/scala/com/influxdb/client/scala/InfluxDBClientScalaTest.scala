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

import akka.actor.ActorSystem
import akka.stream.testkit.scaladsl.TestSink
import com.influxdb.query.FluxRecord
import org.scalatest.BeforeAndAfter
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

/**
 * @author Jakub Bednar (09/06/2020 07:19)
 */
class InfluxDBClientScalaTest extends AnyFunSuite with Matchers with BeforeAndAfter {

  implicit val system: ActorSystem = ActorSystem("unit-tests")

  var utils: InfluxDBUtils = _

  before {
    utils = new InfluxDBUtils {}
  }

  after {
    utils.serverStop()
  }

  test("userAgent") {

    val url = utils.serverStart
    utils.serverMockResponse()

    val client = InfluxDBClientScalaFactory.create(url)

    val queryScalaApi = client.getQueryScalaApi()
    val flux = "from(bucket:\"my-bucket\")\n\t" +
      "|> range(start: 1970-01-01T00:00:00.000000001Z)\n\t" +
      "|> filter(fn: (r) => (r[\"_measurement\"] == \"mem\" and r[\"_field\"] == \"free\" and r[\"host\"] == \"A\"))" +
      "|> sum()"

    val source = queryScalaApi.query(flux, "my-org").runWith(TestSink.probe[FluxRecord])
    source.expectSubscriptionAndComplete()

    val request = utils.serverTakeRequest()
    request.getHeader("User-Agent") should startWith("influxdb-client-scala/")
  }
}
