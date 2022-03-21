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

import java.util.Arrays;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.InvocableScriptsApi;
import com.influxdb.client.OrganizationsQuery;
import com.influxdb.client.domain.Organization;
import com.influxdb.client.write.Point;

public class InvocableScripts {

    //
    // Define credentials
    //
    private static String url = "https://us-west-2-1.aws.cloud2.influxdata.com";
    private static char[] token = "my-token".toCharArray();
    private static String org = "my-org";
    private static String bucket = "my-bucket";

    public static void main(final String[] args) {

        try (InfluxDBClient client = InfluxDBClientFactory.create(url, token, org, bucket)) {
            //
            // Prepare data
            //
            Point point1 = Point.measurement("my_measurement").addTag("location", "Prague").addField("temperature", 25.3);
            Point point2 = Point.measurement("my_measurement").addTag("location", "New York").addField("temperature", 24.3);
            client.getWriteApiBlocking().writePoints(bucket, org, Arrays.asList(point1, point2));

            //
            // Find Organization ID by Organization API.
            //
            OrganizationsQuery orgFilter = new OrganizationsQuery();
            orgFilter.setOrg("my-org");
            Organization organization = client.getOrganizationsApi().findOrganizations(orgFilter).get(0);

            InvocableScriptsApi scriptsApi = client.getInvocableScriptsApi();
        }
    }
}
