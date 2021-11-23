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
import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.reactive.InfluxDBClientReactive;
import com.influxdb.client.reactive.InfluxDBClientReactiveFactory;
import com.influxdb.client.reactive.WriteReactiveApi;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import org.reactivestreams.Publisher;

public class InfluxDB2ReactiveExampleWriteEveryTenSeconds {

    private static char[] token = "my-token".toCharArray();
    private static String org = "my-org";
    private static String bucket = "my-bucket";

    public static void main(final String[] args) throws InterruptedException {

        InfluxDBClientReactive influxDBClient = InfluxDBClientReactiveFactory.create("http://localhost:9999", token, org, bucket);

        //
        // Write data
        //
        WriteReactiveApi writeApi = influxDBClient.getWriteReactiveApi();

        Flowable<Temperature> measurements = Flowable.interval(10, TimeUnit.SECONDS)
                .map(time -> {

                    Temperature temperature = new Temperature();
                    temperature.location = getLocation();
                    temperature.value = getValue();
                    temperature.time = Instant.now();
                    return temperature;
                });

        //
        // ReactiveStreams publisher
        //
        Publisher<WriteReactiveApi.Success> publisher = writeApi.writeMeasurements(WritePrecision.NS, measurements);

        //
        // Subscribe to Publisher
        //
        Disposable subscriber = Flowable.fromPublisher(publisher)
                .subscribe(success -> System.out.println("Successfully written temperature"));

        Thread.sleep(35_000);

        subscriber.dispose();

        influxDBClient.close();
    }

    private static Double getValue() {
        Random r = new Random();

        return -20 + 70 * r.nextDouble();
    }

    private static String getLocation() {
        return "Prague";
    }

    @Measurement(name = "temperature")
    private static class Temperature {

        @Column(tag = true)
        String location;

        @Column
        Double value;

        @Column(timestamp = true)
        Instant time;
    }
}
