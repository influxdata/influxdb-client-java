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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import com.influxdb.client.WriteOptions;
import com.influxdb.client.domain.Authorization;
import com.influxdb.client.domain.Bucket;
import com.influxdb.client.domain.Organization;
import com.influxdb.client.domain.Permission;
import com.influxdb.client.domain.PermissionResource;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.client.write.events.WriteSuccessEvent;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;

import io.reactivex.BackpressureOverflowStrategy;

/*
  InfluxDB 2.0 onboarding tasks (create default user, organization and bucket) :

  curl -i -X POST http://localhost:9999/api/v2/setup -H 'accept: application/json' \
      -d '{
              "username": "my-user",
              "password": "my-password",
              "org": "my-org",
              "bucket": "my-bucket"
          }'
 */

@SuppressWarnings("CheckStyle")
public class PlatformExample {

    @Measurement(name = "temperature")
    private static class Temperature {

        @Column(tag = true)
        String location;

        @Column
        Double value;

        @Column(timestamp = true)
        Instant time;
    }

    public static void main(final String[] args) throws Exception {

        InfluxDBClient influxDBClient = InfluxDBClientFactory.create("http://localhost:9999", findToken().toCharArray());

        Organization medicalGMBH = influxDBClient.getOrganizationsApi()
                .createOrganization("Medical Corp" + System.currentTimeMillis());

        //
        // Create New Bucket with retention 1h
        //
        Bucket temperatureBucket = influxDBClient.getBucketsApi().createBucket("temperature-sensors", medicalGMBH);

        //
        // Add Permissions to read and write to the Bucket
        //
        PermissionResource resource = new PermissionResource();
        resource.setId(temperatureBucket.getId());
        resource.setOrgID(medicalGMBH.getId());
        resource.setType(PermissionResource.TypeEnum.BUCKETS);

        Permission readBucket = new Permission();
        readBucket.setResource(resource);
        readBucket.setAction(Permission.ActionEnum.READ);

        Permission writeBucket = new Permission();
        writeBucket.setResource(resource);
        writeBucket.setAction(Permission.ActionEnum.WRITE);

        Authorization authorization = influxDBClient.getAuthorizationsApi()
                .createAuthorization(medicalGMBH, Arrays.asList(readBucket, writeBucket));

        String token = authorization.getToken();
        System.out.println("The token to write to temperature-sensors bucket " + token);

        InfluxDBClient client = InfluxDBClientFactory.create("http://localhost:9999", token.toCharArray());

        CountDownLatch countDownLatch = new CountDownLatch(1);

        //
        // Write data
        //
        try (WriteApi writeApi = client.getWriteApi(WriteOptions.builder()
                .batchSize(5000)
                .flushInterval(1000)
                .backpressureStrategy(BackpressureOverflowStrategy.DROP_OLDEST)
                .bufferLimit(10000)
                .jitterInterval(1000)
                .retryInterval(5000)
                .build())) {

            writeApi.listenEvents(WriteSuccessEvent.class, (value) -> countDownLatch.countDown());

            //
            // Write by POJO
            //
            Temperature temperature = new Temperature();
            temperature.location = "south";
            temperature.value = 62D;
            temperature.time = Instant.now();
            writeApi.writeMeasurement("temperature-sensors", medicalGMBH.getId(), WritePrecision.NS, temperature);

            //
            // Write by Point
            //
            Point point = Point.measurement("temperature")
                    .addTag("location", "west")
                    .addField("value", 55D)
                    .time(Instant.now().toEpochMilli(), WritePrecision.NS);
            writeApi.writePoint("temperature-sensors", medicalGMBH.getId(), point);

            //
            // Write by LineProtocol
            //
            String record = "temperature,location=north value=60.0";
            writeApi.writeRecord("temperature-sensors", medicalGMBH.getId(), WritePrecision.NS, record);

            countDownLatch.await(2, TimeUnit.SECONDS);
        }

        //
        // Read data
        //
        List<FluxTable> tables = client.getQueryApi().query("from(bucket:\"temperature-sensors\") |> range(start: 0)", medicalGMBH.getId());

        for (FluxTable fluxTable : tables) {
            List<FluxRecord> records = fluxTable.getRecords();
            for (FluxRecord fluxRecord : records) {
                System.out.println(fluxRecord.getTime() + ": " + fluxRecord.getValueByKey("_value"));
            }
        }

        client.close();
        influxDBClient.close();
    }

    private static String findToken() throws Exception {

        InfluxDBClient influxDBClient = InfluxDBClientFactory.create("http://localhost:9999",
                "my-user", "my-password".toCharArray());

        String token = influxDBClient.getAuthorizationsApi()
                .findAuthorizations()
                .stream()
                .filter(authorization -> authorization.getPermissions().stream()
                        .map(Permission::getResource)
                        .anyMatch(resource ->
                                resource.getType().equals(PermissionResource.TypeEnum.ORGS) &&
                                        resource.getId() == null &&
                                        resource.getOrgID() == null))
                .findFirst()
                .orElseThrow(IllegalStateException::new).getToken();

        influxDBClient.close();

        return token;
    }
}
