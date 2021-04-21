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

import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.kotlin.InfluxDBClientKotlinFactory
import com.influxdb.client.write.Point
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.ZoneOffset


fun main() = runBlocking {

    val org = "my-org"
    val bucket = "my-bucket"

    //
    // Initialize client
    //
    val client = InfluxDBClientKotlinFactory
        .create("http://localhost:9999", "my-token".toCharArray(), org, bucket)

    client.use {

        val writeApi = client.getWriteKotlinApi()

        //
        // Initialize CSV reader
        //
        val reader = InputStreamReader(this.javaClass.getResourceAsStream("/vix-daily.csv"))
        val csvParser = CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())

        //
        // Write data
        //
        csvParser.use {

            //
            // Lazy read of CSV and create Batches
            //
            csvParser
                .iterator()
                // use Kotlin Flow
                .asFlow()
                // Map CSVRecord to DataPoint
                .map {
                    parseCSVRecord(it)
                }
                // Batch by 500 rows
                .chunks(500)
                // Write Batches
                .collect {
                    println("Writing... ${it.count()}")
                    writeApi.writePoints(it)
                }
        }

        //
        // Query max value of inserted data
        //
        val query =
            """from(bucket: "$bucket") 
                |> range(start: 0, stop: now()) 
                |> filter(fn: (r) => r._measurement == "financial-analysis") 
                |> max()"""

        client
            .getQueryKotlinApi()
            .query(query)
            .consumeAsFlow()
            .collect { println("max ${it.field} = ${it.value}") }
    }
}

/**
 * Parse CSV Record into Data Point.
 */
private fun parseCSVRecord(it: CSVRecord): Point {

    // parse date
    val date = LocalDate
        .parse(it.get("Date"))
        .atStartOfDay()
        .toInstant(ZoneOffset.UTC)

    // create Data Point
    return Point("financial-analysis")
        .addTag("type", "vix-daily")
        .addField("open", it.get("VIX Open").toFloat())
        .addField("high", it.get("VIX High").toFloat())
        .addField("low", it.get("VIX Low").toFloat())
        .addField("close", it.get("VIX Close").toFloat())
        .time(date, WritePrecision.NS)
}

private suspend fun <T> Flow<T>.chunks(size: Int): Flow<List<T>> = flow {
    // chunk storage
    val chunk = ArrayList<T>(size)
    collect {
        chunk += it
        // over size => emit
        if (chunk.size >= size) {
            emit(chunk)
            chunk.clear()
        }
    }
    // emit remaining
    if (chunk.size > 0) {
        emit(chunk)
    }
}

