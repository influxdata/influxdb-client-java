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

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Keep, Source}
import org.scalatest.BeforeAndAfter
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class WriteScalaApiTest extends AnyFunSuite with Matchers with BeforeAndAfter {

  implicit val system: ActorSystem = ActorSystem("unit-tests")

  var utils: InfluxDBUtils = _
  var client: InfluxDBClientScala = _

  before {
    utils = new InfluxDBUtils {}
    client = InfluxDBClientScalaFactory.create(utils.serverStart)
  }

  after {
    utils.serverStop()
  }

  test("write record") {

    utils.serverMockResponse()

    val source = Source.single("m2m,tag=a value=1i")
    val sink = client.getWriteScalaApi.writeRecord()
    val materialized = source.toMat(sink)(Keep.right)

    Await.ready(materialized.run(), Duration.Inf)

    utils.getRequestCount should be(1)
    utils.serverTakeRequest().getBody.readUtf8() should be("m2m,tag=a value=1i")
  }

  test("write records") {

    utils.serverMockResponse()

    val source = Source.single(Seq("m2m,tag=a value=1i 1", "m2m,tag=a value=2i 2"))
    val sink = client.getWriteScalaApi.writeRecords()
    val materialized = source.toMat(sink)(Keep.right)

    Await.ready(materialized.run(), Duration.Inf)

    utils.getRequestCount should be(1)
    utils.serverTakeRequest().getBody.readUtf8() should be("m2m,tag=a value=1i 1\nm2m,tag=a value=2i 2")
  }
}
