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
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.influxdata.flux.domain.FluxRecord;
import org.influxdata.flux.domain.FluxTable;
import org.influxdata.platform.PlatformClient;
import org.influxdata.platform.PlatformClientFactory;
import org.influxdata.platform.WriteClient;
import org.influxdata.platform.annotations.Column;
import org.influxdata.platform.annotations.Measurement;
import org.influxdata.platform.domain.Authorization;
import org.influxdata.platform.domain.Bucket;
import org.influxdata.platform.domain.Organization;
import org.influxdata.platform.domain.Permission;
import org.influxdata.platform.domain.PermissionResourceType;
import org.influxdata.platform.option.WriteOptions;
import org.influxdata.platform.write.Point;
import org.influxdata.platform.write.event.WriteSuccessEvent;

import io.reactivex.BackpressureOverflowStrategy;

/*
  InfluxPlatform OSS2.0 onboarding tasks (create default user, organization and bucket) :

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

        PlatformClient platform = PlatformClientFactory.create("http://localhost:9999",
            "my-user", "my-password".toCharArray());

        Organization medicalGMBH = platform.createOrganizationClient()
                .createOrganization("Medical Corp" + System.currentTimeMillis());

        //
        // Create New Bucket with retention 1h
        //
        Bucket temperatureBucket = platform.createBucketClient()
            .createBucket("temperature-sensors", medicalGMBH.getName());

        //
        // Add Permissions to read and write to the Bucket
        //
        Permission readBucket = new Permission();
        readBucket.setId(temperatureBucket.getId());
        readBucket.setResource(PermissionResourceType.BUCKET);
        readBucket.setAction(Permission.READ_ACTION);

        Permission writeBucket = new Permission();
        writeBucket.setId(temperatureBucket.getId());
        writeBucket.setResource(PermissionResourceType.BUCKET);
        writeBucket.setAction(Permission.WRITE_ACTION);

        Authorization authorization = platform.createAuthorizationClient()
            .createAuthorization(medicalGMBH, Arrays.asList(readBucket, writeBucket));

        String token = authorization.getToken();
        System.out.println("The token to write to temperature-sensors bucket " + token);

        PlatformClient client = PlatformClientFactory.create("http://localhost:9999", token.toCharArray());

        CountDownLatch countDownLatch = new CountDownLatch(1);
        try (WriteClient writeClient = client.createWriteClient(WriteOptions.builder()
            .batchSize(5000)
            .flushInterval(1000)
            .backpressureStrategy(BackpressureOverflowStrategy.DROP_OLDEST)
            .bufferLimit(10000)
            .jitterInterval(1000)
            .retryInterval(5000)
            .build())) {

            writeClient.listenEvents(WriteSuccessEvent.class, (value) -> countDownLatch.countDown());

            //
            // Write by POJO
            //
            Temperature temperature = new Temperature();
            temperature.location = "south";
            temperature.value = 62D;
            temperature.time = Instant.now();
            writeClient.writeMeasurement("temperature-sensors", medicalGMBH.getName(), ChronoUnit.NANOS, temperature);

            //
            // Write by Point
            //
            Point point = Point.measurement("temperature")
                    .addTag("location", "west")
                    .addField("value", 55D)
                    .time(Instant.now().toEpochMilli(), ChronoUnit.NANOS);
            writeClient.writePoint("temperature-sensors", medicalGMBH.getName(), point);

            //
            // Write by LineProtocol
            //
            String record = "temperature,location=north value=60.0";
            writeClient.writeRecord("temperature-sensors", medicalGMBH.getName(), ChronoUnit.NANOS, record);

            countDownLatch.await(2, TimeUnit.SECONDS);
        }

        List<FluxTable> tables = client.createQueryClient().query("from(bucket:\"temperature-sensors\") |> range(start: 0)", medicalGMBH.getId());

        for (FluxTable fluxTable : tables) {
            List<FluxRecord> records = fluxTable.getRecords();
            for (FluxRecord fluxRecord : records) {
                System.out.println(fluxRecord.getTime() + ": " + fluxRecord.getValueByKey("_value"));
            }
        }

        client.close();
        platform.close();
    }
}
