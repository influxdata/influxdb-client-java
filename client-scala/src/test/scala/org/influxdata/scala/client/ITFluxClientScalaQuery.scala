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
package org.influxdata.scala.client

import java.net.ConnectException
import java.time.Instant

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.testkit.scaladsl.TestSink
import org.influxdata.client.annotations.Column
import org.influxdata.client.exceptions.InfluxException
import org.influxdata.client.flux.domain.FluxRecord
import org.scalatest.Matchers

/**
 * @author Jakub Bednar (bednar@github) (07/11/2018 10:00)
 */
class ITFluxClientScalaQuery extends AbstractITFluxClientScala with Matchers {

  implicit val system: ActorSystem = ActorSystem("it-tests")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  test("Simple query mapped to FluxRecords") {

    val flux = ("from(bucket:\"flux_database_scala\")" + "\n"
      + "\t|> range(start: 1970-01-01T00:00:00.000000000Z)\n"
      + "\t|> filter(fn: (r) => (r[\"_measurement\"] == \"mem\" AND r[\"_field\"] == \"free\"))\n"
      + "\t|> sum()")

    val source = fluxClient.query(flux).runWith(TestSink.probe[FluxRecord])

    val record1 = source.requestNext()

    record1.getMeasurement should be("mem")
    record1.getValue should be(21)

    val record2 = source.requestNext()

    record2.getMeasurement should be("mem")
    record2.getValue should be(42)

    source.expectComplete()
  }

  test("Simple query FluxRecords order") {

    val flux = ("from(bucket:\"flux_database_scala\")" + "\n"
      + "\t|> range(start: 1970-01-01T00:00:00.000000000Z)")

    val source = fluxClient.query(flux).map(it => it.getValue).runWith(TestSink.probe[Object])

    source.requestNext() should be(55L)
    source.requestNext() should be(65L)
    source.requestNext() should be(35L)
    source.requestNext() should be(38L)
    source.requestNext() should be(45L)
    source.requestNext() should be(49L)
    source.requestNext() should be(10L)
    source.requestNext() should be(11L)
    source.requestNext() should be(20L)
    source.requestNext() should be(22L)

    source.expectComplete()
  }

  test("Mapping to POJO") {

    val flux = ("from(bucket:\"flux_database_scala\")" + "\n"
      + "\t|> range(start: 1970-01-01T00:00:00.000000000Z)\n"
      + "\t|> filter(fn: (r) => (r[\"_measurement\"] == \"mem\" AND r[\"_field\"] == \"free\"))")

    val source = fluxClient.query(flux, classOf[Mem]).runWith(TestSink.probe[Mem])

    var mem = source.requestNext()
    mem.host should be("A")
    mem.region should be("west")
    mem.free should be(10L)
    mem.time should be(Instant.ofEpochSecond(10))

    mem = source.requestNext()
    mem.host should be("A")
    mem.region should be("west")
    mem.free should be(11L)
    mem.time should be(Instant.ofEpochSecond(20))

    mem = source.requestNext()
    mem.host should be("B")
    mem.region should be("west")
    mem.free should be(20L)
    mem.time should be(Instant.ofEpochSecond(10))

    mem = source.requestNext()
    mem.host should be("B")
    mem.region should be("west")
    mem.free should be(22L)
    mem.time should be(Instant.ofEpochSecond(20))

    source.expectComplete()
  }

  test("Stop processing") {

    influxDBUtils.prepareChunkRecords()

    val flux = ("from(bucket:\"flux_database_scala\")" + "\n"
      + "\t|> filter(fn: (r) => r[\"_measurement\"] == \"chunked\")\n"
      + "\t|> range(start: 1970-01-01T00:00:00.000000000Z)\n"
      + "\t|> window(every: 10m)")


    val source = fluxClient.query(flux).take(10).runWith(TestSink.probe[FluxRecord])

    var record = source.requestNext()
    record.getValue should be(1L)

    record = source.requestNext()
    record.getValue should be(2L)

    record = source.requestNext()
    record.getValue should be(3L)

    record = source.requestNext()
    record.getValue should be(4L)

    record = source.requestNext()
    record.getValue should be(5L)

    record = source.requestNext()
    record.getValue should be(6L)

    record = source.requestNext()
    record.getValue should be(7L)

    record = source.requestNext()
    record.getValue should be(8L)

    record = source.requestNext()
    record.getValue should be(9L)

    record = source.requestNext()
    record.getValue should be(10L)

    source.expectComplete()
  }

  test("Error propagation") {

    val flux = ("fromx(bucket:\"flux_database_scala\")" + "\n"
      + "\t|> range(start: 1970-01-01T00:00:00.000000000Z)\n"
      + "\t|> filter(fn: (r) => (r[\"_measurement\"] == \"mem\" AND r[\"_field\"] == \"free\"))\n"
      + "\t|> sum()")

    val source = fluxClient.query(flux).runWith(TestSink.probe[FluxRecord])

    val throwable = source.expectSubscriptionAndError()

    throwable shouldBe a[InfluxException]
  }

  test("Not running server") {

    val clientNotRunning = FluxClientScalaFactory.create("http://localhost:8099")

    val flux = ("from(bucket:\"flux_database_scala\")" + "\n"
      + "\t|> range(start: 1970-01-01T00:00:00.000000000Z)")

    val source = clientNotRunning.query(flux).runWith(TestSink.probe[FluxRecord])

    val throwable = source.expectSubscriptionAndError()

    throwable shouldBe a[ConnectException]

    clientNotRunning.close()
  }
}

class Mem {

  var host: String = _
  var region: String = _

  @Column(name = "_value")
  var free: java.lang.Long = _
  @Column(name = "_time")
  var time: Instant = _
}
