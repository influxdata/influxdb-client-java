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
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WriteConsistency;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.WriteParameters;

/**
 * How to use `consistency` parameter for InfluxDB Enterprise.
 * 
 * @author Jakub Bednar (12/04/2022 11:00)
 */
public class InfluxDBEnterpriseExample {

    private static final String URL = "http://ec2-13-57-181-120.us-west-1.compute.amazonaws.com:8086";
    /**
     * Credentials
     */
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    /**
     * Database name
     */
    private static final String DB = "benchmark_db";
    /**
     * Retention policy name
     */
    private static final String RP = "autogen";

    public static void main(final String[] args) {

        try (InfluxDBClient client = InfluxDBClientFactory.createV1(URL, USERNAME, PASSWORD.toCharArray(), DB, RP)) {

            System.out.println("--- Write Record ---");

            // Initialize Blocking API
            WriteApiBlocking writeApi = client.getWriteApiBlocking();

            // Configure precision and consistency
            WriteParameters parameters = new WriteParameters(WritePrecision.NS, WriteConsistency.ALL);

            // Write record
            writeApi.writeRecord("cpu_load_short,host=server02 value=0.67", parameters);

            System.out.println("--- Written data with consistency set to: ALL ---");
        }
    }
}
