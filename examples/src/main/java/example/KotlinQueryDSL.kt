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

import com.influxdb.client.kotlin.InfluxDBClientKotlinFactory
import com.influxdb.query.dsl.Flux
import com.influxdb.query.dsl.functions.restriction.Restrictions
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.runBlocking
import java.time.temporal.ChronoUnit

fun main() = runBlocking {

    val influxDBClient = InfluxDBClientKotlinFactory
        .create("http://localhost:8086", "my-token".toCharArray(), "my-org")

    val mem = Flux.from("my-bucket")
        .range(-30L, ChronoUnit.MINUTES)
        .filter(Restrictions.and(Restrictions.measurement().equal("mem"), Restrictions.field().equal("used_percent")))

    //Result is returned as a stream
    val results = influxDBClient.getQueryKotlinApi().query(mem.toString())

    //Example of additional result stream processing on client side
    results
        .consumeAsFlow()
        //filter on client side using `filter` built-in operator
        .filter { (it.value as Double) > 55 }
        // take first 20 records
        .take(20)
        //print results
        .collect { println("Measurement: ${it.measurement}, value: ${it.value}") }

    influxDBClient.close()
}
