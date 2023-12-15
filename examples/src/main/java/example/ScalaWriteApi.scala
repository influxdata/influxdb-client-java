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

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.scaladsl.{Keep, Source}
import com.influxdb.annotations.{Column, Measurement}
import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.scala.InfluxDBClientScalaFactory
import com.influxdb.client.write.Point

import java.time.Instant
import scala.concurrent.Await
import scala.concurrent.duration.Duration

object ScalaWriteApi {

  implicit val system: ActorSystem = ActorSystem("examples")

  def main(args: Array[String]): Unit = {

    val client = InfluxDBClientScalaFactory.create(
      "http://localhost:8086", "my-token".toCharArray, "my-org", "my-bucket")

    //
    // Use InfluxDB Line Protocol to write data
    //
    val record = "mem,host=host1 used_percent=23.43234543"

    val source = Source.single(record)
    val sink = client.getWriteScalaApi.writeRecord()
    val materialized = source.toMat(sink)(Keep.right)
    Await.result(materialized.run(), Duration.Inf)

    //
    // Use a Data Point to write data
    //
    val point = Point
      .measurement("mem")
      .addTag("host", "host1")
      .addField("used_percent", 23.43234543)
      .time(Instant.now(), WritePrecision.NS)

    val sourcePoint = Source.single(point)
    val sinkPoint = client.getWriteScalaApi.writePoint()
    val materializedPoint = sourcePoint.toMat(sinkPoint)(Keep.right)
    Await.result(materializedPoint.run(), Duration.Inf)

    //
    // Use POJO and corresponding class to write data
    //
    val mem = new Mem()
    mem.host = "host1"
    mem.used_percent = 23.43234543
    mem.time = Instant.now

    val sourcePOJO = Source.single(mem)
    val sinkPOJO = client.getWriteScalaApi.writeMeasurement()
    val materializedPOJO = sourcePOJO.toMat(sinkPOJO)(Keep.right)
    Await.result(materializedPOJO.run(), Duration.Inf)

    client.close()
    system.terminate()
  }

  @Measurement(name = "mem")
  class Mem() {
    @Column(tag = true)
    var host: String = _
    @Column
    var used_percent: Double = _
    @Column(timestamp = true)
    var time: Instant = _
  }
}
