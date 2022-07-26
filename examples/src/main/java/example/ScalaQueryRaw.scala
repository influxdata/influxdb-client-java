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
package example

import akka.actor.ActorSystem
import akka.stream.scaladsl.Sink
import com.influxdb.client.scala.InfluxDBClientScalaFactory

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object ScalaQueryRaw {

  implicit val system: ActorSystem = ActorSystem("examples")

  def main(args: Array[String]): Unit = {
    val influxDBClient = InfluxDBClientScalaFactory
      .create("http://localhost:8086", "my-token".toCharArray, "my-org")

    val fluxQuery = ("from(bucket: \"my-bucket\")\n"
      + " |> range(start: -5m)"
      + " |> filter(fn: (r) => (r[\"_measurement\"] == \"cpu\" and r[\"_field\"] == \"usage_system\"))"
      + " |> sample(n: 5, pos: 1)")

    //Result is returned as a stream
    val sink = influxDBClient.getQueryScalaApi().queryRaw(fluxQuery)
      //print results
      .runWith(Sink.foreach[String](it => println(s"Line: $it")))

    // wait to finish
    Await.result(sink, Duration.Inf)

    influxDBClient.close()
    system.terminate()
  }
}
