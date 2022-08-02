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

import java.math.BigDecimal;
import java.time.Instant;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.InfluxQLQueryApi;
import com.influxdb.client.domain.InfluxQLQuery;
import com.influxdb.query.InfluxQLQueryResult;

public class InfluxQLExample {

    private static char[] token = "my-token".toCharArray();
    private static String org = "my-org";

    private static String database = "my-org";

    public static void main(final String[] args) {

        try (InfluxDBClient influxDBClient = InfluxDBClientFactory.create("http://localhost:8086", token, org)) {

            //
            // Query data
            //
            String influxQL = "SELECT FIRST(\"free\") FROM \"influxql\"";

            InfluxQLQueryApi queryApi = influxDBClient.getInfluxQLQueryApi();

            // send request
            InfluxQLQueryResult result = queryApi.query(new InfluxQLQuery(influxQL, database).setPrecision(InfluxQLQuery.InfluxQLPrecision.SECONDS),
                    (columnName, rawValue, resultIndex, seriesName) -> {
                        // convert columns
                        switch (columnName) {
                            case "time":
                                return Instant.ofEpochSecond(Long.parseLong(rawValue));
                            case "first":
                                return new BigDecimal(rawValue);
                            default:
                                throw new IllegalArgumentException("unexpected column " + columnName);
                        }
                    });

            for (InfluxQLQueryResult.Result resultResult : result.getResults()) {
                for (InfluxQLQueryResult.Series series : resultResult.getSeries()) {
                    for (InfluxQLQueryResult.Series.Record record : series.getValues()) {
                        System.out.println(record.getValueByKey("time") + ": " + record.getValueByKey("first"));
                    }
                }
            }

        }
    }
}
