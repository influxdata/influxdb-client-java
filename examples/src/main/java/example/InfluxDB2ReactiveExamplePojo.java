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

import java.time.Instant;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import com.influxdb.client.reactive.InfluxDBClientReactive;
import com.influxdb.client.reactive.InfluxDBClientReactiveFactory;
import com.influxdb.client.reactive.QueryReactiveApi;

public class InfluxDB2ReactiveExamplePojo {

    private static char[] token = "my-token".toCharArray();

    public static void main(final String[] args) {

        InfluxDBClientReactive influxDBClient = InfluxDBClientReactiveFactory.create("http://localhost:9999", token);
        //
        // Query data
        //
        String flux = "from(bucket:\"my-bucket\") |> range(start: 0) |> filter(fn: (r) => r._measurement == \"temperature\")";

        QueryReactiveApi queryApi = influxDBClient.getQueryReactiveApi();

        queryApi
                .query(flux, "my-org", Temperature.class)
                //
                // Take first 10 records
                //
                .take(10)
                .subscribe(temperature -> {
                    //
                    // The callback to consume a FluxRecord mapped to POJO.
                    //
                    System.out.println(temperature.location + ": " + temperature.value + " at " + temperature.time);
                });

        influxDBClient.close();
    }

    @Measurement(name = "temperature")
    public static class Temperature {

        @Column(tag = true)
        String location;

        @Column
        Double value;

        @Column(timestamp = true)
        Instant time;
    }
}
