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
package example;

import org.influxdata.flux.FluxClientReactive;
import org.influxdata.flux.FluxClientReactiveFactory;
import org.influxdata.flux.domain.FluxRecord;
import org.influxdata.flux.option.FluxConnectionOptions;

import io.reactivex.Flowable;

@SuppressWarnings("CheckStyle")
public class FluxClientReactiveFactoryExample {
    public static void main(String[] args) {

        FluxConnectionOptions options = FluxConnectionOptions.builder()
            .url("http://localhost:8086/")
            .build();

        FluxClientReactive fluxClient = FluxClientReactiveFactory.create(options);

        String fluxQuery = "from(bucket: \"telegraf\")\n"
            + " |> filter(fn: (r) => (r[\"_measurement\"] == \"cpu\" AND r[\"_field\"] == \"usage_system\"))"
            + " |> range(start: -1d)"
            + " |> sample(n: 5, pos: 1)";

        //Result is returned as a RxJava stream

        Flowable<FluxRecord> recordFlowable = fluxClient.query(fluxQuery);

        //Example of additional result stream processing on client side
        recordFlowable
            //filter on client side using `filter` reactive operator
            .filter(fluxRecord -> ("localhost".equals(fluxRecord.getValueByKey("host"))))
            //take first 20 records
            .take(20)
            //print results
            .subscribe(fluxRecord -> System.out.println(fluxRecord.getValue()));
    }
}
