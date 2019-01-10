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
import org.influxdata.platform.domain.PermissionResourceType;
import org.influxdata.platform.domain.User;
import org.influxdata.platform.option.WriteOptions;
import org.influxdata.platform.rest.LogLevel;
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
    private QueryClient queryClient;

    private Bucket bucket;
    private Organization organization;

    @BeforeEach
    void setUp() throws Exception {

        super.setUp();

        bucket = platformClient.createBucketClient()
                .createBucket(generateName("h2o"), retentionRule(), "my-org");

        //
        // Add Permissions to read and write to the Bucket
        //
        Permission readBucket = new Permission();
        readBucket.setResource(PermissionResourceType.BUCKET);
        readBucket.setAction(Permission.READ_ACTION);
        readBucket.setId(bucket.getId());

        Permission writeBucket = new Permission();
        writeBucket.setResource(PermissionResourceType.BUCKET);
        writeBucket.setAction(Permission.WRITE_ACTION);
        writeBucket.setId(bucket.getId());

        User loggedUser = platformClient.createUserClient().me();
        Assertions.assertThat(loggedUser).isNotNull();

        organization = findMyOrg();

        Authorization authorization = platformClient.createAuthorizationClient()
                .createAuthorization(organization, Arrays.asList(readBucket, writeBucket));

        String token = authorization.getToken();

        platformClient.close();
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

        writeClient = platformClient.createWriteClient();

        String record = "h2o_feet,location=coyote_creek level\\ water_level=1.0 1";

        WriteClientTest.EventListener<WriteSuccessEvent> listener = new WriteClientTest.EventListener<>();
        writeClient.listenEvents(WriteSuccessEvent.class, listener);

        platformClient.setLogLevel(LogLevel.BODY);

        writeClient.writeRecord(bucketName, "my-org", ChronoUnit.NANOS, record);

        listener.waitToCallback();
        
        Assertions.assertThat(listener.getValue()).isNotNull();
        Assertions.assertThat(listener.getValue().getBucket()).isEqualTo(bucket.getName());
        Assertions.assertThat(listener.getValue().getOrganization()).isEqualTo("my-org");
        Assertions.assertThat(listener.getValue().getLineProtocol()).isEqualTo(record);

        List<FluxTable> query = queryClient.query("from(bucket:\"" + bucketName + "\") |> range(start: 0) |> last()", organization.getId());

        Assertions.assertThat(query).hasSize(1);
        Assertions.assertThat(query.get(0).getRecords()).hasSize(1);
        Assertions.assertThat(query.get(0).getRecords().get(0).getMeasurement()).isEqualTo("h2o_feet");
        Assertions.assertThat(query.get(0).getRecords().get(0).getValue()).isEqualTo(1.0D);
        Assertions.assertThat(query.get(0).getRecords().get(0).getField()).isEqualTo("level water_level");
        Assertions.assertThat(query.get(0).getRecords().get(0).getTime()).isEqualTo(Instant.ofEpochSecond(0,1));
    }

    @Test
    void writePrecisionMicros() {

        String bucketName = bucket.getName();

        writeClient = platformClient.createWriteClient();

        String record = "h2o_feet,location=coyote_creek level\\ water_level=1.0 1";

        WriteClientTest.EventListener<WriteSuccessEvent> listener = new WriteClientTest.EventListener<>();
        writeClient.listenEvents(WriteSuccessEvent.class, listener);

        writeClient.writeRecord(bucketName, "my-org", ChronoUnit.MICROS, record);

        listener.waitToCallback();

        List<FluxTable> query = queryClient.query("from(bucket:\"" + bucketName + "\") |> range(start: 0) |> last()", organization.getId());

        Assertions.assertThat(query).hasSize(1);
        Assertions.assertThat(query.get(0).getRecords().get(0).getTime()).isEqualTo(Instant.ofEpochSecond(0, 1000));
    }

    @Test
    void writePrecisionMillis() {

        String bucketName = bucket.getName();

        writeClient = platformClient.createWriteClient();

        String record = "h2o_feet,location=coyote_creek level\\ water_level=1.0 1";

        WriteClientTest.EventListener<WriteSuccessEvent> listener = new WriteClientTest.EventListener<>();
        writeClient.listenEvents(WriteSuccessEvent.class, listener);

        writeClient.writeRecord(bucketName, "my-org", ChronoUnit.MILLIS, record);

        listener.waitToCallback();

        List<FluxTable> query = queryClient.query("from(bucket:\"" + bucketName + "\") |> range(start: 0) |> last()", organization.getId());

        Assertions.assertThat(query).hasSize(1);
        Assertions.assertThat(query.get(0).getRecords().get(0).getTime()).isEqualTo(Instant.ofEpochMilli(1));
    }

    @Test
    void writePrecisionSeconds() {

        String bucketName = bucket.getName();

        writeClient = platformClient.createWriteClient();

        String record = "h2o_feet,location=coyote_creek level\\ water_level=1.0 1";

        WriteClientTest.EventListener<WriteSuccessEvent> listener = new WriteClientTest.EventListener<>();
        writeClient.listenEvents(WriteSuccessEvent.class, listener);

        writeClient.writeRecord(bucketName, "my-org", ChronoUnit.SECONDS, record);

        listener.waitToCallback();

        List<FluxTable> query = queryClient.query("from(bucket:\"" + bucketName + "\") |> range(start: 0) |> last()", organization.getId());

        Assertions.assertThat(query).hasSize(1);
        Assertions.assertThat(query.get(0).getRecords().get(0).getTime()).isEqualTo(Instant.ofEpochSecond(1));
    }

    @Test
    void writePoints() {

        String bucketName = bucket.getName();

        writeClient = platformClient.createWriteClient();
        WriteClientTest.EventListener<WriteSuccessEvent> listener = new WriteClientTest.EventListener<>();
        writeClient.listenEvents(WriteSuccessEvent.class, listener);

        Instant time = Instant.now();

        Point point1 = Point.measurement("h2o_feet").addTag("location", "west").addField("water_level", 1).time(time, ChronoUnit.MILLIS);
        Point point2 = Point.measurement("h2o_feet").addTag("location", "west").addField("water_level", 2).time(time.plusMillis(10), ChronoUnit.MILLIS);

        writeClient.writePoints(bucketName, "my-org", Arrays.asList(point1, point2, point2));

        listener.waitToCallback();

        List<FluxRecord> fluxRecords = new ArrayList<>();

        CountDownLatch queryCountDown = new CountDownLatch(2);
        queryClient.query("from(bucket:\"" + bucketName + "\") |> range(start: 0)", organization.getId(), (cancellable, fluxRecord) -> {
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
        WriteClientTest.EventListener<WriteSuccessEvent> listener = new WriteClientTest.EventListener<>();
        writeClient.listenEvents(WriteSuccessEvent.class, listener);

        long millis = Instant.now().toEpochMilli();
        H2OFeetMeasurement measurement = new H2OFeetMeasurement(
                "coyote_creek", 2.927, null, millis);

        writeClient.writeMeasurement(bucketName, "my-org", ChronoUnit.NANOS, measurement);

        listener.waitToCallback();

        List<H2OFeetMeasurement> measurements = queryClient.query("from(bucket:\"" + bucketName + "\") |> range(start: 0) |> last() |> rename(columns:{_value: \"water_level\"})", organization.getId(), H2OFeetMeasurement.class);

        Assertions.assertThat(measurements).hasSize(1);
        Assertions.assertThat(measurements.get(0).location).isEqualTo("coyote_creek");
        Assertions.assertThat(measurements.get(0).description).isNull();
        Assertions.assertThat(measurements.get(0).level).isEqualTo(2.927);
        Assertions.assertThat(measurements.get(0).time).isEqualTo(measurement.time);
    }

    @Test
    void queryDataFromNewOrganization() throws Exception {

        String orgName = generateName("new-org");
        Organization organization =
                platformClient.createOrganizationClient().createOrganization(orgName);

        Bucket bucket = platformClient.createBucketClient()
                .createBucket(generateName("h2o"), retentionRule(), organization);

        Permission readBucket = new Permission();
        readBucket.setResource(PermissionResourceType.BUCKET);
        readBucket.setAction(Permission.READ_ACTION);
        readBucket.setId(bucket.getId());

        Permission writeBucket = new Permission();
        writeBucket.setResource(PermissionResourceType.BUCKET);
        writeBucket.setAction(Permission.WRITE_ACTION);
        writeBucket.setId(bucket.getId());

        Permission readOrganization = new Permission();
        readOrganization.setResource(PermissionResourceType.ORG);
        readOrganization.setAction(Permission.READ_ACTION);
        readOrganization.setId(organization.getId());

        Permission writeOrganization = new Permission();
        writeOrganization.setResource(PermissionResourceType.ORG);
        writeOrganization.setAction(Permission.WRITE_ACTION);
        writeOrganization.setId(organization.getId());

        User loggedUser = platformClient.createUserClient().createUser(generateName("Tom Lik"));
        Assertions.assertThat(loggedUser).isNotNull();

        Authorization authorization = platformClient.createAuthorizationClient()
                .createAuthorization(organization, Arrays.asList(readOrganization, writeOrganization, readBucket, writeBucket));

        String token = authorization.getToken();

        platformClient.close();
        platformClient = PlatformClientFactory.create(platformURL, token.toCharArray());
        queryClient = platformClient.createQueryClient();

        Point point = Point.measurement("h2o_feet")
                .addTag("location", "atlantic")
                .addField("water_level", 1)
                .time(Instant.now(), ChronoUnit.MICROS);

        writeClient = platformClient.createWriteClient();
        WriteClientTest.EventListener<WriteSuccessEvent> listener = new WriteClientTest.EventListener<>();
        writeClient.listenEvents(WriteSuccessEvent.class, listener);

        writeClient.writePoint(bucket.getName(), organization.getId(), point);

        listener.waitToCallback();

        String query = queryClient.queryRaw("from(bucket:\"" + bucket.getName() + "\") |> range(start: 0) |> last()", organization.getId());
        Assertions.assertThat(query).endsWith("1,water_level,h2o_feet,atlantic\n");
    }

    @Test
    void flush() throws InterruptedException {

        String bucketName = bucket.getName();

        writeClient = platformClient.createWriteClient(WriteOptions.builder().batchSize(100_000)
                .flushInterval(100_000).build());

        String record = "h2o_feet,location=coyote_creek level\\ water_level=1.0 1";

        writeClient.writeRecord(bucketName, "my-org", ChronoUnit.NANOS, record);

        List<FluxTable> query = queryClient.query("from(bucket:\"" + bucketName + "\") |> range(start: 0) |> last()", organization.getId());
        Assertions.assertThat(query).hasSize(0);

        writeClient.flush();
        Thread.sleep(10);

        query = queryClient.query("from(bucket:\"" + bucketName + "\") |> range(start: 0) |> last()", organization.getId());

        Assertions.assertThat(query).hasSize(1);
        Assertions.assertThat(query.get(0).getRecords()).hasSize(1);
    }

    @Test
    void flushByTime() throws InterruptedException {

        String bucketName = bucket.getName();

        writeClient = platformClient.createWriteClient(WriteOptions.builder().batchSize(100_000)
                .flushInterval(500).build());

        String record1 = "h2o_feet,location=coyote_creek level\\ water_level=1.0 1";
        String record2 = "h2o_feet,location=coyote_creek level\\ water_level=2.0 2";
        String record3 = "h2o_feet,location=coyote_creek level\\ water_level=3.0 3";
        String record4 = "h2o_feet,location=coyote_creek level\\ water_level=4.0 4";
        String record5 = "h2o_feet,location=coyote_creek level\\ water_level=5.0 5";

        writeClient.writeRecords(bucketName, "my-org", ChronoUnit.NANOS, Arrays.asList(record1, record2, record3, record4, record5));

        List<FluxTable> query = queryClient.query("from(bucket:\"" + bucketName + "\") |> range(start: 0)", organization.getId());
        Assertions.assertThat(query).hasSize(0);

        Thread.sleep(500);

        query = queryClient.query("from(bucket:\"" + bucketName + "\") |> range(start: 0)", organization.getId());

        Assertions.assertThat(query).hasSize(1);
        Assertions.assertThat(query.get(0).getRecords()).hasSize(5);
    }

    @Test
    void flushByCount() throws InterruptedException {

        String bucketName = bucket.getName();

        writeClient = platformClient.createWriteClient(WriteOptions.builder().batchSize(6)
                .flushInterval(100_000).build());

        String record1 = "h2o_feet,location=coyote_creek level\\ water_level=1.0 1";
        String record2 = "h2o_feet,location=coyote_creek level\\ water_level=2.0 2";
        String record3 = "h2o_feet,location=coyote_creek level\\ water_level=3.0 3";
        String record4 = "h2o_feet,location=coyote_creek level\\ water_level=4.0 4";
        String record5 = "h2o_feet,location=coyote_creek level\\ water_level=5.0 5";
        String record6 = "h2o_feet,location=coyote_creek level\\ water_level=6.0 6";

        writeClient.writeRecord(bucketName, "my-org", ChronoUnit.NANOS, record1);
        writeClient.writeRecord(bucketName, "my-org", ChronoUnit.NANOS, record2);
        writeClient.writeRecord(bucketName, "my-org", ChronoUnit.NANOS, record3);
        writeClient.writeRecord(bucketName, "my-org", ChronoUnit.NANOS, record4);
        writeClient.writeRecord(bucketName, "my-org", ChronoUnit.NANOS, record5);

        Thread.sleep(100);
        List<FluxTable> query = queryClient.query("from(bucket:\"" + bucketName + "\") |> range(start: 0)", organization.getId());
        Assertions.assertThat(query).hasSize(0);

        writeClient.writeRecord(bucketName, "my-org", ChronoUnit.NANOS, record6);
        Thread.sleep(100);

        query = queryClient.query("from(bucket:\"" + bucketName + "\") |> range(start: 0)", organization.getId());

        Assertions.assertThat(query).hasSize(1);
        Assertions.assertThat(query.get(0).getRecords()).hasSize(6);
    }

    @Test
    void jitter() throws InterruptedException {

        String bucketName = bucket.getName();

        writeClient = platformClient.createWriteClient(WriteOptions.builder().batchSize(1)
                .jitterInterval(4_000)
                .build());

        String record = "h2o_feet,location=coyote_creek level\\ water_level=1.0 1";

        writeClient.writeRecord(bucketName, "my-org", ChronoUnit.NANOS, record);

        List<FluxTable> query = queryClient.query("from(bucket:\"" + bucketName + "\") |> range(start: 0) |> last()", organization.getId());
        Assertions.assertThat(query).hasSize(0);

        Thread.sleep(5000);

        query = queryClient.query("from(bucket:\"" + bucketName + "\") |> range(start: 0) |> last()", organization.getId());

        Assertions.assertThat(query).hasSize(1);
        Assertions.assertThat(query.get(0).getRecords()).hasSize(1);
    }
}