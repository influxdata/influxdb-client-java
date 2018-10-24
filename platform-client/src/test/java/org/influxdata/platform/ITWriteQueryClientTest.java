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
package org.influxdata.platform;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.influxdata.flux.domain.FluxRecord;
import org.influxdata.flux.domain.FluxTable;
import org.influxdata.platform.domain.Authorization;
import org.influxdata.platform.domain.Bucket;
import org.influxdata.platform.domain.Organization;
import org.influxdata.platform.domain.Permission;
import org.influxdata.platform.domain.User;
import org.influxdata.platform.option.WriteOptions;
import org.influxdata.platform.write.Point;
import org.influxdata.platform.write.event.WriteSuccessEvent;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (25/09/2018 13:39)
 */
@RunWith(JUnitPlatform.class)
class ITWriteQueryClientTest extends AbstractITClientTest {

    private WriteClient writeClient;
    private PlatformClient platformClient;
    private QueryClient queryClient;

    private Bucket bucket;

    @BeforeEach
    void setUp() {

        super.setUp();

        bucket = platformService.createBucketClient()
                .createBucket(generateName("h2o"), retentionRule(), "my-org");

        //
        // Add Permissions to read and write to the Bucket
        //
        String bucketResource = Permission.bucketResource(bucket.getId());

        Permission readBucket = new Permission();
        readBucket.setResource(bucketResource);
        readBucket.setAction(Permission.READ_ACTION);

        Permission writeBucket = new Permission();
        writeBucket.setResource(bucketResource);
        writeBucket.setAction(Permission.WRITE_ACTION);

        User loggedUser = platformService.createUserClient().me();
        Assertions.assertThat(loggedUser).isNotNull();

        Authorization authorization = platformService.createAuthorizationClient()
                .createAuthorization(loggedUser, Arrays.asList(readBucket, writeBucket));

        String token = authorization.getToken();

        platformClient = PlatformClientFactory.create(platformURL, token.toCharArray());
        queryClient = platformClient.createQueryClient();
    }

    @AfterEach
    void tearDown() throws Exception {

        if (writeClient != null) {
            writeClient.close();
        }
        platformClient.close();
    }

    @Test
    void writeRecord() {

        String bucketName = bucket.getName();

        writeClient = platformClient.createWriteClient(WriteOptions.DISABLED_BATCHING);

        String record = "h2o_feet,location=coyote_creek level\\ water_level=1.0 1";

        writeClient
                .listenEvents(WriteSuccessEvent.class)
                .subscribe(event -> {

                    Assertions.assertThat(event).isNotNull();
                    Assertions.assertThat(event.getBucket()).isEqualTo(bucket.getName());
                    Assertions.assertThat(event.getOrganization()).isEqualTo("my-org");
                    Assertions.assertThat(event.getLineProtocol()).isEqualTo(record);

                    countDownLatch.countDown();
                });

        writeClient.writeRecord(bucketName, "my-org", ChronoUnit.NANOS, record);

        waitToCallback();

        Assertions.assertThat(countDownLatch.getCount()).isEqualTo(0);

        List<FluxTable> query = queryClient.query("from(bucket:\"" + bucketName + "\") |> last()", "my-org");

        Assertions.assertThat(query).hasSize(1);
        Assertions.assertThat(query.get(0).getRecords()).hasSize(1);
        Assertions.assertThat(query.get(0).getRecords().get(0).getMeasurement()).isEqualTo("h2o_feet");
        Assertions.assertThat(query.get(0).getRecords().get(0).getValue()).isEqualTo(1.0D);
        Assertions.assertThat(query.get(0).getRecords().get(0).getField()).isEqualTo("level water_level");
    }

    @Test
    void writePoints() {

        String bucketName = bucket.getName();

        writeClient = platformClient.createWriteClient();
        writeClient
                .listenEvents(WriteSuccessEvent.class)
                .subscribe(event -> countDownLatch.countDown());

        Instant time = Instant.now();

        Point point1 = Point.name("h2o_feet").addTag("location", "west").addField("water_level", 1).time(time, ChronoUnit.MICROS);
        Point point2 = Point.name("h2o_feet").addTag("location", "west").addField("water_level", 2).time(time.plusSeconds(10), ChronoUnit.MICROS);

        writeClient.writePoints(bucketName, "my-org", Arrays.asList(point1, point2, point2));

        waitToCallback();

        List<FluxRecord> fluxRecords = new ArrayList<>();

        CountDownLatch queryCountDown = new CountDownLatch(2);
        queryClient.query("from(bucket:\"" + bucketName + "\") |> range(start: 0)", "my-org", (cancellable, fluxRecord) -> {
            fluxRecords.add(fluxRecord);
            queryCountDown.countDown();

        });

        waitToCallback(queryCountDown, 10);

        Assertions.assertThat(fluxRecords).hasSize(2);
        Assertions.assertThat(fluxRecords.get(0).getValue()).isEqualTo(1L);
        Assertions.assertThat(fluxRecords.get(1).getValue()).isEqualTo(2L);
    }

    @Test
    void writeMeasurement() {

        String bucketName = bucket.getName();

        writeClient = platformClient.createWriteClient();
        writeClient
                .listenEvents(WriteSuccessEvent.class)
                .subscribe(event -> countDownLatch.countDown());

        long millis = Instant.now().toEpochMilli();
        H2OFeetMeasurement measurement = new H2OFeetMeasurement(
                "coyote_creek", 2.927, null, millis);

        writeClient.writeMeasurement(bucketName, "my-org", ChronoUnit.NANOS, measurement);

        waitToCallback();

        List<H2OFeetMeasurement> measurements = queryClient.query("from(bucket:\"" + bucketName + "\") |> last() |> rename(columns:{_value: \"water_level\"})", "my-org", H2OFeetMeasurement.class);

        Assertions.assertThat(measurements).hasSize(1);
        Assertions.assertThat(measurements.get(0).location).isEqualTo("coyote_creek");
        Assertions.assertThat(measurements.get(0).description).isNull();
        Assertions.assertThat(measurements.get(0).level).isEqualTo(2.927);
        Assertions.assertThat(measurements.get(0).time).isEqualTo(measurement.time);
    }

    @Test
    void queryDataFromNewOrganization() throws Exception {

        platformClient.close();

        String orgName = generateName("new-org");
        Organization organization =
                platformService.createOrganizationClient().createOrganization(orgName);

        Bucket bucket = platformService.createBucketClient()
                .createBucket(generateName("h2o"), retentionRule(), orgName);

        String bucketResource = Permission.bucketResource(bucket.getId());

        Permission readBucket = new Permission();
        readBucket.setResource(bucketResource);
        readBucket.setAction(Permission.READ_ACTION);

        Permission writeBucket = new Permission();
        writeBucket.setResource(bucketResource);
        writeBucket.setAction(Permission.WRITE_ACTION);

        User loggedUser = platformService.createUserClient().me();
        Assertions.assertThat(loggedUser).isNotNull();

        Authorization authorization = platformService.createAuthorizationClient()
                .createAuthorization(loggedUser, Arrays.asList(readBucket, writeBucket));

        String token = authorization.getToken();

        platformClient = PlatformClientFactory.create(platformURL, token.toCharArray());
        queryClient = platformClient.createQueryClient();

        Point point = Point.name("h2o_feet")
                .addTag("location", "atlantic")
                .addField("water_level", 1)
                .time(Instant.now(), ChronoUnit.MICROS);

        writeClient = platformClient.createWriteClient();
        writeClient
                .listenEvents(WriteSuccessEvent.class)
                .subscribe(event -> countDownLatch.countDown());

        writeClient.writePoint(bucket.getName(), organization.getName(), point);

        waitToCallback();

        String query = queryClient.queryRaw("from(bucket:\"" + bucket.getName() + "\") |> last()", organization.getName());
        Assertions.assertThat(query).endsWith("1,water_level,h2o_feet,atlantic\n");
    }
}