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

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import akka.stream.testkit.scaladsl.TestSink
import org.json.JSONObject
import org.scalatest.Matchers

import scala.concurrent.Await
import scala.concurrent.duration.Duration


/**
 * @author Jakub Bednar (bednar@github) (07/11/2018 13:47)
 */
class ITFluxClientScalaQueryRaw extends AbstractITFluxClientScala with Matchers {

  implicit val system: ActorSystem = ActorSystem("it-tests")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  test("Map to String") {

    val flux = ("from(bucket:\"flux_database_scala\")" + "\n"
      + "\t|> range(start: 1970-01-01T00:00:00.000000000Z)\n"
      + "\t|> filter(fn: (r) => (r[\"_measurement\"] == \"mem\" AND r[\"_field\"] == \"free\"))\n"
      + "\t|> sum()")

    val source = fluxClient.queryRaw(flux).runWith(TestSink.probe[String])

    var line = source.requestNext()
    line should be("#datatype,string,long,dateTime:RFC3339,dateTime:RFC3339,string,string,string,string,long")

    line = source.requestNext()
    line should be("#group,false,false,true,true,true,true,true,true,false")

    line = source.requestNext()
    line should be("#default,_result,,,,,,,,")

    line = source.requestNext()
    line should be(",result,table,_start,_stop,_field,_measurement,host,region,_value")

    line = source.requestNext()
    line should endWith(",free,mem,A,west,21")

    line = source.requestNext()
    line should endWith(",free,mem,B,west,42")

    line = source.requestNext()
    line shouldBe empty

    source.expectComplete()
  }

  test("Custom dialect") {

    val flux = ("from(bucket:\"flux_database_scala\")" + "\n"
      + "\t|> range(start: 1970-01-01T00:00:00.000000000Z)\n"
      + "\t|> filter(fn: (r) => (r[\"_measurement\"] == \"mem\" AND r[\"_field\"] == \"free\"))\n"
      + "\t|> sum()")

    val dialect = new JSONObject()
      .put("header", false)
      .toString()

    val source = fluxClient.queryRaw(flux, dialect).runWith(TestSink.probe[String])

    var line = source.requestNext()
    line should be("#datatype,string,long,dateTime:RFC3339,dateTime:RFC3339,string,string,string,string,long")

    line = source.requestNext()
    line should be("#group,false,false,true,true,true,true,true,true,false")

    line = source.requestNext()
    line should be("#default,_result,,,,,,,,")

    line = source.requestNext()
    line should endWith(",free,mem,A,west,21")

    line = source.requestNext()
    line should endWith(",free,mem,B,west,42")

    line = source.requestNext()
    line shouldBe empty

    source.expectComplete()
  }

  test("Stop processing") {

    influxDBUtils.prepareChunkRecords()

    val flux = ("from(bucket:\"flux_database_scala\")" + "\n"
      + "\t|> filter(fn: (r) => r[\"_measurement\"] == \"chunked\")\n"
      + "\t|> range(start: 1970-01-01T00:00:00.000000000Z)\n"
      + "\t|> window(every: 10m)")


    val eventualStrings = fluxClient.queryRaw(flux).take(10).runWith(Sink.seq)
    val result = Await.result(eventualStrings, Duration.Inf)

    result should have size 10
    result.mkString("\n") should be("#datatype,string,long,dateTime:RFC3339,dateTime:RFC3339,dateTime:RFC3339,long,string,string,string,string\n" +
      "#group,false,false,true,true,false,false,true,true,true,true\n" +
      "#default,_result,,,,,,,,,\n" +
      ",result,table,_start,_stop,_time,_value,_field,_measurement,host,region\n" +
      ",,0,1970-01-01T00:00:00Z,1970-01-01T00:10:00Z,1970-01-01T00:00:00.000000001Z,1,free,chunked,A,west\n" +
      ",,0,1970-01-01T00:00:00Z,1970-01-01T00:10:00Z,1970-01-01T00:00:00.000000002Z,2,free,chunked,A,west\n" +
      ",,0,1970-01-01T00:00:00Z,1970-01-01T00:10:00Z,1970-01-01T00:00:00.000000003Z,3,free,chunked,A,west\n" +
      ",,0,1970-01-01T00:00:00Z,1970-01-01T00:10:00Z,1970-01-01T00:00:00.000000004Z,4,free,chunked,A,west\n" +
      ",,0,1970-01-01T00:00:00Z,1970-01-01T00:10:00Z,1970-01-01T00:00:00.000000005Z,5,free,chunked,A,west\n" +
      ",,0,1970-01-01T00:00:00Z,1970-01-01T00:10:00Z,1970-01-01T00:00:00.000000006Z,6,free,chunked,A,west")
  }
}
