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

import java.util.List;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxTable;

/**
 * @author Jakub Bednar (16/04/2020 11:00)
 */
public class InfluxDB18Example {

    public static void main(final String[] args) {

        String database = "telegraf";
        String retentionPolicy = "autogen";

        InfluxDBClient client = InfluxDBClientFactory.createV1("http://localhost:8086",
                "username",
                "password".toCharArray(),
                database,
                retentionPolicy);

        System.out.println("*** Write Points ***");

        try (WriteApi writeApi = client.getWriteApi()) {

            Point point = Point.measurement("mem")
                    .addTag("host", "host1")
                    .addField("used_percent", 29.43234543);

            System.out.println(point.toLineProtocol());

            writeApi.writePoint(point);
        }

        System.out.println("*** Query Points ***");
        String query = String.format("from(bucket: \"%s/%s\") |> range(start: -1h)", database, retentionPolicy);

        List<FluxTable> tables = client.getQueryApi().query(query);
        tables.get(0).getRecords()
                .forEach(record -> System.out.println(String.format("%s %s: %s %s",
                        record.getTime(), record.getMeasurement(), record.getField(), record.getValue())));

        client.close();
    }
}
