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

import com.influxdb.annotations.Column
import com.influxdb.annotations.Measurement
import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.kotlin.InfluxDBClientKotlinFactory
import com.influxdb.client.write.Point
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.runBlocking
import java.time.Instant

fun main() = runBlocking {

    val org = "my-org"
    val bucket = "my-bucket"

    //
    // Initialize client
    //
    val client = InfluxDBClientKotlinFactory
        .create("http://localhost:8086", "my-token".toCharArray(), org, bucket)

    client.use {

        val writeApi = client.getWriteKotlinApi()

        //
        // Write by Data Point
        //
        val point = Point.measurement("temperature")
            .addTag("location", "west")
            .addField("value", 55.0)
            .time(Instant.now().toEpochMilli(), WritePrecision.MS)

        writeApi.writePoint(point)

        //
        // Write by LineProtocol
        //
        writeApi.writeRecord("temperature,location=north value=60.0", WritePrecision.NS)

        //
        // Write by DataClass
        //
        val temperature = Temperature("south", 62.0, Instant.now())

        writeApi.writeMeasurement(temperature, WritePrecision.NS)

        //
        // Query results
        //
        val fluxQuery =
            """from(bucket: "$bucket") |> range(start: 0) |> filter(fn: (r) => (r["_measurement"] == "temperature"))"""

        client
            .getQueryKotlinApi()
            .query(fluxQuery)
            .consumeAsFlow()
            .collect { println("Measurement: ${it.measurement}, value: ${it.value}") }
    }
}

@Measurement(name = "temperature")
data class Temperature(
    @Column(tag = true) val location: String,
    @Column val value: Double,
    @Column(timestamp = true) val time: Instant
)
