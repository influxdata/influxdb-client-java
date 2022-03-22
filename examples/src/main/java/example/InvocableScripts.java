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
import java.util.Collections;
import java.util.List;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.InvocableScriptsApi;
import com.influxdb.client.domain.Script;
import com.influxdb.client.domain.ScriptCreateRequest;
import com.influxdb.client.domain.ScriptLanguage;
import com.influxdb.client.domain.ScriptUpdateRequest;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;

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

            InvocableScriptsApi scriptsApi = client.getInvocableScriptsApi();

            //
            // Create Invocable Script
            //
            System.out.println("------- Create -------\n");
            ScriptCreateRequest createRequest = new ScriptCreateRequest()
                    .name("my_script_" + System.currentTimeMillis())
                    .description("my first try")
                    .language(ScriptLanguage.FLUX)
                    .script("from(bucket: params.bucket_name) |> range(start: -30d) |> limit(n:2)");
            Script createdScript = scriptsApi.createScript(createRequest);
            System.out.println(createdScript);

            //
            // Update Invocable Script
            //
            System.out.println("------- Update -------\n");
            ScriptUpdateRequest updateRequest = new ScriptUpdateRequest().description("my updated description");
            createdScript = scriptsApi.updateScript(createdScript.getId(), updateRequest);
            System.out.println(createdScript);

            //
            // Invoke a script
            //
            
            // List<FluxTable>
            System.out.println("\n------- Invoke to List<FluxTable> -------\n") ;
            List<FluxTable> tables = scriptsApi.invokeScript(createdScript.getId(), Collections.singletonMap("bucket_name", bucket));
            for (FluxTable table : tables) {
                for (FluxRecord record : table.getRecords()) {
                    System.out.printf("%s: %s %s°C%n", record.getValueByKey("_time"), record.getValueByKey("location"), record.getValue());
                }
            }

            //
            // List scripts
            //
            System.out.println("\n------- List -------\n");
            List<Script> scripts = scriptsApi.findScripts();
            for (Script script : scripts) {
                System.out.printf(" ---\n ID: %s\n Name: %s\n Description: %s%n", script.getId(), script.getName(), script.getDescription());
            }
            System.out.println("---");

            //
            // Delete previously created Script
            //
            System.out.println("------- Delete -------\n");
            scriptsApi.deleteScript(createdScript.getId());
            System.out.printf(" Successfully deleted script: '%s'%n", createdScript.getName());
        }
    }
}
