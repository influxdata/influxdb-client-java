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
package example

import java.time.temporal.ChronoUnit

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import org.influxdata.client.flux.domain.FluxRecord
import org.influxdata.flux.dsl.Flux
import org.influxdata.flux.dsl.functions.restriction.Restrictions
import org.influxdata.scala.client.FluxClientScalaFactory

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
 * @author Jakub Bednar (bednar@github) (08/11/2018 10:29)
 */
object FluxClientScalaExampleDSL {

  implicit val system: ActorSystem = ActorSystem("it-tests")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  def main(args: Array[String]) {

    val fluxClient = FluxClientScalaFactory
      .create("http://localhost:8086?readTimeout=5000&connectTimeout=5000")

    val mem = Flux.from("telegraf")
      .filter(Restrictions.and(Restrictions.measurement().equal("mem"), Restrictions.field().equal("used_percent")))
      .range(-30L, ChronoUnit.MINUTES)

    //Result is returned as a stream
    val results = fluxClient.query(mem.toString())

    //Example of additional result stream processing on client side
    val sink = results
      //filter on client side using `filter` built-in operator
      .filter(it => it.getValue.asInstanceOf[Double] > 55)
      //take first 20 records
      .take(20)
      //print results
      .runWith(Sink.foreach[FluxRecord](it => println(s"Measurement: ${it.getMeasurement}, value: ${it.getValue}")))

    // wait to finish
    Await.result(sink, Duration.Inf)

    fluxClient.close()
    system.terminate()
  }
}
