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

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

import com.influxdb.client.DeleteApi;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.exceptions.InfluxException;

/**
 * @author Pavlina Rolincova (rolincova@github) (29/10/2019).
 */
public class DeleteData {

    private static char[] token = "my-token".toCharArray();

    public static void main(final String[] args) {

        InfluxDBClient influxDBClient = InfluxDBClientFactory.create("http://localhost:8086", token);

        DeleteApi deleteApi = influxDBClient.getDeleteApi();

        try {

            OffsetDateTime start = OffsetDateTime.now().minus(1, ChronoUnit.HOURS);
            OffsetDateTime stop = OffsetDateTime.now();

            deleteApi.delete(start, stop, "", "my-bucket", "my-org");

        } catch (InfluxException ie) {
            System.out.println("InfluxException: " + ie);
        }

        influxDBClient.close();
    }
}
