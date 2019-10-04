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

import com.influxdb.client.flux.FluxClient;
import com.influxdb.client.flux.FluxClientFactory;
import com.influxdb.client.flux.FluxConnectionOptions;

@SuppressWarnings("CheckStyle")
public class FluxClientFactoryExample {

    public static void main(final String[] args) {

        FluxConnectionOptions options = FluxConnectionOptions.builder()
            .url("http://localhost:8086/")
            .build();

        FluxClient fluxClient = FluxClientFactory.create(options);

        String fluxQuery = "from(bucket: \"telegraf\")\n"
            + " |> range(start: -1d)"
            + " |> filter(fn: (r) => (r[\"_measurement\"] == \"cpu\" and r[\"_field\"] == \"usage_system\"))"
            + " |> sample(n: 5, pos: 1)";

        fluxClient.query(
            fluxQuery, (cancellable, record) -> {
                // process the flux query result record
                System.out.println(
                    record.getTime() + ": " + record.getValue());

            }, error -> {
                // error handling while processing result
                error.printStackTrace();

            }, () -> {
                // on complete
                System.out.println("Query completed");
            });

        fluxClient.close();

    }
}
