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

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.scaladsl.{Keep, Source}
import com.influxdb.annotations.{Column, Measurement}
import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.write.{Point, WriteParameters}
import com.influxdb.exceptions.InternalServerErrorException
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import java.time.Instant
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.language.postfixOps

class WriteScalaApiTest extends AnyFunSuite with Matchers with BeforeAndAfter with ScalaFutures {

  implicit val system: ActorSystem = ActorSystem("unit-tests")

  var utils: InfluxDBUtils = _
  var client: InfluxDBClientScala = _

  before {
    utils = new InfluxDBUtils {}
    client = InfluxDBClientScalaFactory.create(utils.serverStart, "my-token".toCharArray, "my-org", "my-bucket")
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
    val request = utils.serverTakeRequest()
    // check request
    request.getBody.readUtf8() should be("m2m,tag=a value=1i")
    request.getRequestUrl.queryParameter("bucket") should be("my-bucket")
    request.getRequestUrl.queryParameter("org") should be("my-org")
    request.getRequestUrl.queryParameter("precision") should be("ns")
  }

  test("write record as stream") {

    utils.serverMockResponse()

    val source = Source(List("m2m,tag=a value=1i 1", "m2m,tag=a value=2i 2"))
    val sink = client.getWriteScalaApi.writeRecord()
    val materialized = source.toMat(sink)(Keep.right)

    Await.ready(materialized.run(), Duration.Inf)

    utils.getRequestCount should be(2)
    val request = utils.serverTakeRequest()
    // check request
    request.getBody.readUtf8() should be("m2m,tag=a value=1i 1")
    request.getRequestUrl.queryParameter("bucket") should be("my-bucket")
    request.getRequestUrl.queryParameter("org") should be("my-org")
    request.getRequestUrl.queryParameter("precision") should be("ns")
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

  test("write records custom params") {

    utils.serverMockResponse()

    val source = Source.single("m2m,tag=a value=1i 1").map(it => Seq(it))
    val sink = client.getWriteScalaApi.writeRecords(new WriteParameters("my-bucket-2", null, null, null))
    val materialized = source.toMat(sink)(Keep.right)

    Await.ready(materialized.run(), Duration.Inf)

    utils.getRequestCount should be(1)
    val request = utils.serverTakeRequest()
    request.getBody.readUtf8() should be("m2m,tag=a value=1i 1")
    request.getRequestUrl.queryParameter("bucket") should be("my-bucket-2")
  }

  test("write records error propagation") {

    utils.serverMockErrorResponse("line protocol poorly formed and no points were written")

    val source = Source.single(Seq("m2m,tag=a value=1i 1", "m2m,tag=a value=2i 2"))
    val sink = client.getWriteScalaApi.writeRecords()
    val materialized = source.toMat(sink)(Keep.right)

    whenReady(materialized.run().failed) { exc => {
      exc.getMessage should be("HTTP status code: 500; Message: line protocol poorly formed and no points were written")
      exc.getClass should be(classOf[InternalServerErrorException])
    }
    }
  }

  test("write point") {

    utils.serverMockResponse()

    val point = Point
      .measurement("h2o")
      .addTag("location", "europe")
      .addField("level", 1)
      .time(1L, WritePrecision.NS)

    val source = Source.single(point)
    val sink = client.getWriteScalaApi.writePoint()
    val materialized = source.toMat(sink)(Keep.right)

    Await.ready(materialized.run(), Duration.Inf)

    utils.getRequestCount should be(1)
    val request = utils.serverTakeRequest()
    // check request
    request.getBody.readUtf8() should be("h2o,location=europe level=1i 1")
    request.getRequestUrl.queryParameter("bucket") should be("my-bucket")
    request.getRequestUrl.queryParameter("org") should be("my-org")
    request.getRequestUrl.queryParameter("precision") should be("ns")
  }

  test("write point as stream") {

    utils.serverMockResponse()

    val point = Point
      .measurement("h2o")
      .addTag("location", "europe")
      .addField("level", 1)
      .time(1L, WritePrecision.NS)

    val point2 = Point
      .measurement("h2o")
      .addTag("location", "europe")
      .addField("level", 2)
      .time(2L, WritePrecision.NS)

    val source = Source(List(point, point2))
    val sink = client.getWriteScalaApi.writePoint()
    val materialized = source.toMat(sink)(Keep.right)

    Await.ready(materialized.run(), Duration.Inf)

    utils.getRequestCount should be(2)
    val request = utils.serverTakeRequest()
    // check request
    request.getBody.readUtf8() should be("h2o,location=europe level=1i 1")
    request.getRequestUrl.queryParameter("bucket") should be("my-bucket")
    request.getRequestUrl.queryParameter("org") should be("my-org")
    request.getRequestUrl.queryParameter("precision") should be("ns")
  }

  test("write points") {

    utils.serverMockResponse()

    val point1 = Point
      .measurement("h2o")
      .addTag("location", "europe")
      .addField("level", 1)
      .time(1L, WritePrecision.NS)

    val point2 = Point
      .measurement("h2o")
      .addTag("location", "europe")
      .addField("level", 2)
      .time(2L, WritePrecision.NS)

    val source = Source.single(Seq(point1, point2))
    val sink = client.getWriteScalaApi.writePoints()
    val materialized = source.toMat(sink)(Keep.right)

    Await.ready(materialized.run(), Duration.Inf)

    utils.getRequestCount should be(1)
    utils.serverTakeRequest().getBody.readUtf8() should be("h2o,location=europe level=1i 1\nh2o,location=europe level=2i 2")
  }

  test("write points different precision") {

    utils.serverMockResponse()
    utils.serverMockResponse()

    val point1 = Point
      .measurement("h2o")
      .addTag("location", "europe")
      .addField("level", 1)
      .time(1L, WritePrecision.NS)

    val point2 = Point
      .measurement("h2o")
      .addTag("location", "europe")
      .addField("level", 2)
      .time(2L, WritePrecision.S)

    val source = Source.single(Seq(point1, point2))
    val sink = client.getWriteScalaApi.writePoints()
    val materialized = source.toMat(sink)(Keep.right)

    Await.ready(materialized.run(), Duration.Inf)

    utils.getRequestCount should be(2)
    utils.serverTakeRequest().getBody.readUtf8() should be("h2o,location=europe level=1i 1")
    utils.serverTakeRequest().getBody.readUtf8() should be("h2o,location=europe level=2i 2")
  }

  test("write measurement") {

    utils.serverMockResponse()

    val measurement = new H2O()
    measurement.location = "coyote_creek"
    measurement.level = 2.927
    measurement.description = "below 3 feet"
    measurement.time = Instant.ofEpochMilli(1440046800L)

    val source = Source.single(measurement)
    val sink = client.getWriteScalaApi.writeMeasurement()
    val materialized = source.toMat(sink)(Keep.right)

    Await.ready(materialized.run(), Duration.Inf)

    utils.getRequestCount should be(1)
    val request = utils.serverTakeRequest()
    // check request
    request.getBody.readUtf8() should be("h2o,location=coyote_creek level\\ description=\"below 3 feet\",water_level=2.927 1440046800000000")
    request.getRequestUrl.queryParameter("bucket") should be("my-bucket")
    request.getRequestUrl.queryParameter("org") should be("my-org")
    request.getRequestUrl.queryParameter("precision") should be("ns")
  }

  test("write measurements") {

    utils.serverMockResponse()

    val measurement1 = new H2O()
    measurement1.location = "coyote_creek"
    measurement1.level = 2.927
    measurement1.description = "below 3 feet"
    measurement1.time = Instant.ofEpochMilli(1440046800L)

    val measurement2 = new H2O()
    measurement2.location = "europe"
    measurement2.level = 10
    measurement2.description = "below 3 feet"
    measurement2.time = Instant.ofEpochMilli(1440046800L)

    val source = Source.single(Seq(measurement1, measurement2))
    val sink = client.getWriteScalaApi.writeMeasurements()
    val materialized = source.toMat(sink)(Keep.right)

    Await.ready(materialized.run(), Duration.Inf)

    utils.getRequestCount should be(1)
    utils.serverTakeRequest().getBody.readUtf8() should be("h2o,location=coyote_creek level\\ description=\"below 3 feet\",water_level=2.927 1440046800000000\nh2o,location=europe level\\ description=\"below 3 feet\",water_level=10.0 1440046800000000")
  }

  @Measurement(name = "h2o")
  class H2O() {
    @Column(name = "location", tag = true)
    var location: String = _
    @Column(name = "water_level")
    var level: Double = _
    @Column(name = "level description")
    var description: String = _
    @Column(name = "time", timestamp = true)
    var time: Instant = _
  }
}
