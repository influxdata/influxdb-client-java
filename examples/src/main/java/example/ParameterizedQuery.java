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
import java.time.Period;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxTable;

public class ParameterizedQuery {

    public static void main(String[] args) {

        String url = "https://us-west-2-1.aws.cloud2.influxdata.com";
        String token = "my-token";
        String org = "my-org";
        String bucket = "my-bucket";
        try (InfluxDBClient client = InfluxDBClientFactory.create(url, token.toCharArray(), org, bucket)) {

            QueryApi queryApi = client.getQueryApi();

            Instant yesterday = Instant.now().minus(Period.ofDays(1));

            Point p = Point.measurement("temperature")
                .addTag("location", "north")
                .addField("value", 60.0)
                .time(yesterday, WritePrecision.NS);

            WriteApiBlocking writeApi = client.getWriteApiBlocking();
            writeApi.writePoint(p);

            //
            // Query range start parameter using Instant
            //
            Map<String, Object> params = new HashMap<>();
            params.put("bucketParam", bucket);
            params.put("startParam", yesterday.toString());

            String parametrizedQuery = "from(bucket: params.bucketParam) |> range(start: time(v: params.startParam))";

            List<FluxTable> query = queryApi.query(parametrizedQuery, org, params);
            query.forEach(fluxTable -> fluxTable.getRecords()
                .forEach(r -> System.out.println(r.getTime() + ": " + r.getValueByKey("_value"))));

            //
            // Query range start parameter using duration
            //
            params.put("startParam", "-1d10s");
            parametrizedQuery = "from(bucket: params.bucketParam) |> range(start: duration(v: params.startParam))";
            query = queryApi.query(parametrizedQuery, org, params);
            query.forEach(fluxTable -> fluxTable.getRecords()
                .forEach(r -> System.out.println(r.getTime() + ": " + r.getValueByKey("_value"))));

        }
    }
}
