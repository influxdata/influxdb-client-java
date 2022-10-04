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

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;

import java.util.List;

public class RecordRowExample {

    public static void main(final String[] args) throws Exception {

        final char[] token = "my-token".toCharArray();
        final String org = "my-org";
        final String bucket = "my-bucket";

        InfluxDBClient influxDBClient = InfluxDBClientFactory.create("http://localhost:9999", token, org, bucket);

        //
        // Prepare Data
        //
        WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();
        for (int i = 1; i <= 5; i++)
            writeApi.writeRecord(WritePrecision.NS, String.format("point,table=my-table result=%d", i));

        //
        // Query data
        //
        String fluxQuery = String.format("from(bucket: \"%s\")\n", bucket)
                + " |> range(start: -1m)"
                + " |> filter(fn: (r) => (r[\"_measurement\"] == \"point\"))"
                + " |> pivot(rowKey:[\"_time\"], columnKey: [\"_field\"], valueColumn: \"_value\")";

        QueryApi queryApi = influxDBClient.getQueryApi();

        //
        // Query data
        //
        List<FluxTable> tables = queryApi.query(fluxQuery);
        System.out.println("--------------------------------- FluxRecord.getValues() --------------------------------");
        for (FluxTable fluxTable : tables) {
            List<FluxRecord> records = fluxTable.getRecords();
            for (FluxRecord fluxRecord : records) {
                System.out.println(fluxRecord.getValues());
            }
        }

        System.out.println("---------------------------------- FluxRecord.getRow() ----------------------------------");
        for (FluxTable fluxTable : tables) {
            List<FluxRecord> records = fluxTable.getRecords();
            for (FluxRecord fluxRecord : records) {
                System.out.println(fluxRecord.getRow());
            }
        }

        influxDBClient.close();
    }
}