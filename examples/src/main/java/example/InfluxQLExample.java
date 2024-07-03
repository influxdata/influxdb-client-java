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

import com.influxdb.LogLevel;
import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.InfluxQLQueryApi;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.InfluxQLQuery;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.query.InfluxQLQueryResult;

public class InfluxQLExample {

    private static char[] token = "my-token".toCharArray();
    private static String org = "my-org";

    private static String database = "my-bucket";

    public static void main(final String[] args) {

        try (InfluxDBClient influxDBClient = InfluxDBClientFactory.create("http://localhost:8086", token, org, database)) {
            //influxDBClient.setLogLevel(LogLevel.BODY); // uncomment to inspect communication messages

            write(influxDBClient);

            //
            // Query data
            //
            String influxQL = "SELECT FIRST(\"free\") FROM \"influxql\"";

            InfluxQLQueryApi queryApi = influxDBClient.getInfluxQLQueryApi();

            // send request - uses default Accept: application/json and returns RFC3339 timestamp
            InfluxQLQueryResult result = queryApi.query(
              new InfluxQLQuery(influxQL, database),
                    (columnName, rawValue, resultIndex, seriesName) -> {  // custom valueExtractor
                        // convert columns
                      return switch (columnName) {
                        case "time" -> Instant.parse(rawValue);
                        case "first" -> Long.parseLong(rawValue);
                        default -> throw new IllegalArgumentException("unexpected column " + columnName);
                      };
                    });

            System.out.println("Default query with valueExtractor");
            dumpResult(result);

            // send request - use Accept: application/csv returns epoch timestamp
            result = queryApi.queryCSV(
              new InfluxQLQuery(influxQL,database),
              (columnName, rawValue, resultIndex, seriesName) -> { // custom valueExtractor
                  // convert columns
                return switch (columnName) {
                  case "time" -> {
                    long l = Long.parseLong(rawValue);
                    yield Instant.ofEpochSecond(l / 1_000_000_000L,
                      l % 1_000_000_000L);
                  }
                  case "first" -> Long.parseLong(rawValue);
                  default -> throw new IllegalArgumentException("unexpected column " + columnName);
                };
              });

            System.out.println("QueryCSV with valueExtractor.");
            dumpResult(result);

            // send request - set `Accept` header in InfluxQLQuery object, use raw results.
            //  N.B. timestamp returned is Epoch nanos in String format.
            result = queryApi.query(
              new InfluxQLQuery(influxQL,database)
                .setAcceptHeader(InfluxQLQuery.AcceptHeader.CSV)
            );

            System.out.println("Default query method with AcceptHeader.CSV in InfluxQLQuery object.  Raw results");
            dumpResult(result);

            // send request - use default `Accept` header (application/json),
            // but specify epoch precision, use raw results
            result = queryApi.query(
              new InfluxQLQuery(influxQL, database)
                .setPrecision(InfluxQLQuery.InfluxQLPrecision.MILLISECONDS)
            );

            System.out.println("Default query method with Epoch precision in InfluxQLQuery object. Raw results.");
            dumpResult(result);

        }
    }

    public static void write(InfluxDBClient influxDBClient){
        WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();

        InfluxQLTestData testData = new InfluxQLTestData(Instant.now().minusSeconds(1));

        writeApi.writeMeasurement(WritePrecision.NS, testData);

    }

    public static void dumpResult(InfluxQLQueryResult result){
        for (InfluxQLQueryResult.Result resultResult : result.getResults()) {
            for (InfluxQLQueryResult.Series series : resultResult.getSeries()) {
                for (InfluxQLQueryResult.Series.Record record : series.getValues()) {
                    System.out.println(record.getValueByKey("time") + ": " + record.getValueByKey("first"));
                }
            }
        }
    }

    @Measurement(name = "influxql")
    public static class InfluxQLTestData{
        @Column(timestamp = true)
        Instant time;

        @Column
        Long free;

        @Column(tag = true)
        String machine;

        public InfluxQLTestData(Instant instant) {
            free = (long) (Math.random() * 100);
            machine = "test";
            time = instant;
        }
    }
}
