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
package com.influxdb.client;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.influxdb.client.domain.Authorization;
import com.influxdb.client.domain.Bucket;
import com.influxdb.client.domain.Organization;
import com.influxdb.client.domain.Permission;
import com.influxdb.client.domain.PermissionResource;
import com.influxdb.client.domain.User;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.client.write.events.WriteErrorEvent;
import com.influxdb.client.write.events.WriteSuccessEvent;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;

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
class ITWriteQueryApi extends AbstractITClientTest {

    private static final Logger LOG = Logger.getLogger(ITWriteQueryApi.class.getName());

    private WriteApi writeApi;
    private QueryApi queryApi;

    private Bucket bucket;
    private Organization organization;
    private String token;

    @BeforeEach
    void setUp() throws Exception {

        organization = findMyOrg();

        bucket = influxDBClient.getBucketsApi()
                .createBucket(generateName("h2o"), retentionRule(), organization);

        PermissionResource resource = new PermissionResource();
        resource.setId(bucket.getId());
        resource.setOrgID(organization.getId());
        resource.setType(PermissionResource.TypeEnum.BUCKETS);

        //
        // Add Permissions to read and write to the Bucket
        //
        Permission readBucket = new Permission();
        readBucket.setResource(resource);
        readBucket.setAction(Permission.ActionEnum.READ);

        Permission writeBucket = new Permission();
        writeBucket.setResource(resource);
        writeBucket.setAction(Permission.ActionEnum.WRITE);

        Authorization authorization = influxDBClient.getAuthorizationsApi()
                .createAuthorization(organization, Arrays.asList(readBucket, writeBucket));

        token = authorization.getToken();

        influxDBClient.close();
        influxDBClient = InfluxDBClientFactory.create(influxDB_URL, token.toCharArray());
        queryApi = influxDBClient.getQueryApi();
    }

    @AfterEach
    void tearDown() throws Exception {

        if (writeApi != null) {
            writeApi.close();
        }
        
        influxDBClient.close();
    }

    @Test
    void writeRecord() {

        String bucketName = bucket.getName();

        writeApi = influxDBClient.getWriteApi();

        String record = "h2o_feet,location=coyote_creek level\\ water_level=1.0 1";

        WriteEventListener<WriteSuccessEvent> listener = new WriteEventListener<>();
        writeApi.listenEvents(WriteSuccessEvent.class, listener);

        LOG.log(Level.FINEST, "Write Event Listener count down: {0}. Before write.", countDownLatch);

        writeApi.writeRecord(bucketName, organization.getId(), WritePrecision.NS, record);

        waitToCallback(listener.countDownLatch, 10);

        LOG.log(Level.FINEST, "Write Event Listener count down: {0}. After write.", countDownLatch);
        LOG.log(Level.FINEST, "Listener values: {0} errors: {1}", new Object[]{listener.values, listener.errors});

        Assertions.assertThat(listener.getValue()).isNotNull();
        Assertions.assertThat(listener.getValue().getBucket()).isEqualTo(bucket.getName());
        Assertions.assertThat(listener.getValue().getOrganization()).isEqualTo(organization.getId());
        Assertions.assertThat(listener.getValue().getLineProtocol()).isEqualTo(record);

        List<FluxTable> query = queryApi.query("from(bucket:\"" + bucketName + "\") |> range(start: 1970-01-01T00:00:00.000000001Z) |> last()", organization.getId());

        Assertions.assertThat(query).hasSize(1);
        Assertions.assertThat(query.get(0).getRecords()).hasSize(1);
        Assertions.assertThat(query.get(0).getRecords().get(0).getMeasurement()).isEqualTo("h2o_feet");
        Assertions.assertThat(query.get(0).getRecords().get(0).getValue()).isEqualTo(1.0D);
        Assertions.assertThat(query.get(0).getRecords().get(0).getField()).isEqualTo("level water_level");
        Assertions.assertThat(query.get(0).getRecords().get(0).getTime()).isEqualTo(Instant.ofEpochSecond(0,1));
    }

    @Test
    void writeAndQueryByOrgName() {

        String bucketName = bucket.getName();

        writeApi = influxDBClient.getWriteApi();

        String record = "h2o_feet,location=coyote_creek level\\ water_level=1.0 1";

        WriteEventListener<WriteSuccessEvent> listener = new WriteEventListener<>();
        writeApi.listenEvents(WriteSuccessEvent.class, listener);

        LOG.log(Level.FINEST, "Write Event Listener count down: {0}. Before write.", countDownLatch);

        writeApi.writeRecord(bucketName, organization.getName(), WritePrecision.NS, record);

        waitToCallback(listener.countDownLatch, 10);

        LOG.log(Level.FINEST, "Write Event Listener count down: {0}. After write.", countDownLatch);
        LOG.log(Level.FINEST, "Listener values: {0} errors: {1}", new Object[]{listener.values, listener.errors});

        Assertions.assertThat(listener.getValue()).isNotNull();
        Assertions.assertThat(listener.getValue().getBucket()).isEqualTo(bucket.getName());
        Assertions.assertThat(listener.getValue().getOrganization()).isEqualTo(organization.getName());
        Assertions.assertThat(listener.getValue().getLineProtocol()).isEqualTo(record);

        List<FluxTable> query = queryApi.query("from(bucket:\"" + bucketName + "\") |> range(start: 1970-01-01T00:00:00.000000001Z) |> last()", organization.getName());

        Assertions.assertThat(query).hasSize(1);
        Assertions.assertThat(query.get(0).getRecords()).hasSize(1);
        Assertions.assertThat(query.get(0).getRecords().get(0).getMeasurement()).isEqualTo("h2o_feet");
        Assertions.assertThat(query.get(0).getRecords().get(0).getValue()).isEqualTo(1.0D);
        Assertions.assertThat(query.get(0).getRecords().get(0).getField()).isEqualTo("level water_level");
        Assertions.assertThat(query.get(0).getRecords().get(0).getTime()).isEqualTo(Instant.ofEpochSecond(0,1));
    }

    /**
     * Test WriteEventLister.onEvent invocation on write error.
     */
    @Test
    void writeErrorListenerTest () {
        String bucketName = "non_existing_bucket";

        writeApi = influxDBClient.getWriteApi();

        String record = "h2o_feet,location=coyote_creek level\\ water_level=1.0 1";

        WriteEventListener<WriteErrorEvent> listener = new WriteEventListener<>();

        writeApi.listenEvents(WriteErrorEvent.class, listener);

        LOG.log(Level.INFO, "Write Event Listener count down: {0}. Before write.", countDownLatch);

        writeApi.writeRecord(bucketName, organization.getId(), WritePrecision.NS, record);

        //check that error event is fired.
        waitToCallback(listener.countDownLatch, 5);
    }

    @Test
    void writePrecisionMicros() {

        String bucketName = bucket.getName();

        writeApi = influxDBClient.getWriteApi();

        String record = "h2o_feet,location=coyote_creek level\\ water_level=1.0 1";

        WriteEventListener<WriteSuccessEvent> listener = new WriteEventListener<>();
        writeApi.listenEvents(WriteSuccessEvent.class, listener);

        writeApi.writeRecord(bucketName, organization.getId(), WritePrecision.US, record);

        waitToCallback(listener.countDownLatch, 10);

        List<FluxTable> query = queryApi.query("from(bucket:\"" + bucketName + "\") |> range(start: 1970-01-01T00:00:00.000000001Z) |> last()", organization.getId());

        Assertions.assertThat(query).hasSize(1);
        Assertions.assertThat(query.get(0).getRecords().get(0).getTime()).isEqualTo(Instant.ofEpochSecond(0, 1000));
    }

    @Test
    void writePrecisionMillis() {

        String bucketName = bucket.getName();

        writeApi = influxDBClient.getWriteApi();

        String record = "h2o_feet,location=coyote_creek level\\ water_level=1.0 1";

        WriteEventListener<WriteSuccessEvent> listener = new WriteEventListener<>();
        writeApi.listenEvents(WriteSuccessEvent.class, listener);

        writeApi.writeRecord(bucketName, organization.getId(), WritePrecision.MS, record);

        waitToCallback(listener.countDownLatch, 10);

        List<FluxTable> query = queryApi.query("from(bucket:\"" + bucketName + "\") |> range(start: 1970-01-01T00:00:00.000000001Z) |> last()", organization.getId());

        Assertions.assertThat(query).hasSize(1);
        Assertions.assertThat(query.get(0).getRecords().get(0).getTime()).isEqualTo(Instant.ofEpochMilli(1));
    }

    @Test
    void writePrecisionSeconds() {

        String bucketName = bucket.getName();

        writeApi = influxDBClient.getWriteApi();

        String record = "h2o_feet,location=coyote_creek level\\ water_level=1.0 1";

        WriteEventListener<WriteSuccessEvent> listener = new WriteEventListener<>();
        writeApi.listenEvents(WriteSuccessEvent.class, listener);

        writeApi.writeRecord(bucketName, organization.getId(), WritePrecision.S, record);

        waitToCallback(listener.countDownLatch, 10);

        List<FluxTable> query = queryApi.query("from(bucket:\"" + bucketName + "\") |> range(start: 1970-01-01T00:00:00.000000001Z) |> last()", organization.getId());

        Assertions.assertThat(query).hasSize(1);
        Assertions.assertThat(query.get(0).getRecords().get(0).getTime()).isEqualTo(Instant.ofEpochSecond(1));
    }

    @Test
    void writePoints() {

        String bucketName = bucket.getName();

        writeApi = influxDBClient.getWriteApi();
        WriteEventListener<WriteSuccessEvent> listener = new WriteEventListener<>();
        writeApi.listenEvents(WriteSuccessEvent.class, listener);

        Instant time = Instant.now();

        Point point1 = Point.measurement("h2o_feet").addTag("location", "west").addField("water_level", 1).time(time, WritePrecision.MS);
        Point point2 = Point.measurement("h2o_feet").addTag("location", "west").addField("water_level", 2).time(time.plusMillis(10), WritePrecision.MS);

        writeApi.writePoints(bucketName, organization.getId(), Arrays.asList(point1, point2, point2));

        waitToCallback(listener.countDownLatch, 10);

        List<FluxRecord> fluxRecords = new ArrayList<>();

        CountDownLatch queryCountDown = new CountDownLatch(2);
        queryApi.query("from(bucket:\"" + bucketName + "\") |> range(start: 1970-01-01T00:00:00.000000001Z)", organization.getId(), (cancellable, fluxRecord) -> {
            fluxRecords.add(fluxRecord);
            queryCountDown.countDown();

        });

        waitToCallback(queryCountDown, 10);

        Assertions.assertThat(fluxRecords).hasSize(2);
        Assertions.assertThat(fluxRecords.get(0).getValue()).isEqualTo(1L);
        Assertions.assertThat(fluxRecords.get(1).getValue()).isEqualTo(2L);
    }

    @Test
    void writePointsWithoutFields() {

        String bucketName = bucket.getName();

        writeApi = influxDBClient.getWriteApi();
        WriteEventListener<WriteSuccessEvent> listener = new WriteEventListener<>();
        writeApi.listenEvents(WriteSuccessEvent.class, listener);

        Instant time = Instant.now();

        Point point1 = Point.measurement("h2o_feet").addTag("location", "west").time(time, WritePrecision.MS);
        Point point2 = Point.measurement("h2o_feet").addTag("location", "west").addField("water_level", 2).time(time.plusMillis(10), WritePrecision.MS);

        writeApi.writePoints(bucketName, organization.getId(), Arrays.asList(point1, point2, point2));

        waitToCallback(listener.countDownLatch, 10);

        List<FluxRecord> fluxRecords = new ArrayList<>();

        CountDownLatch queryCountDown = new CountDownLatch(1);
        queryApi.query("from(bucket:\"" + bucketName + "\") |> range(start: 1970-01-01T00:00:00.000000001Z)", organization.getId(), (cancellable, fluxRecord) -> {
            fluxRecords.add(fluxRecord);
            queryCountDown.countDown();

        });

        waitToCallback(queryCountDown, 10);

        Assertions.assertThat(fluxRecords).hasSize(1);
        Assertions.assertThat(fluxRecords.get(0).getValue()).isEqualTo(2L);
    }

    @Test
    void writeMeasurement() {

        String bucketName = bucket.getName();

        writeApi = influxDBClient.getWriteApi();
        WriteEventListener<WriteSuccessEvent> listener = new WriteEventListener<>();
        writeApi.listenEvents(WriteSuccessEvent.class, listener);

        long millis = Instant.now().toEpochMilli();
        H2OFeetMeasurement measurement = new H2OFeetMeasurement(
                "coyote_creek", 2.927, null, millis);

        writeApi.writeMeasurement(bucketName, organization.getId(), WritePrecision.NS, measurement);

        waitToCallback(listener.countDownLatch, 10);

        List<H2OFeetMeasurement> measurements = queryApi.query("from(bucket:\"" + bucketName + "\") |> range(start: 1970-01-01T00:00:00.000000001Z) |> last() |> rename(columns:{_value: \"water_level\"})", organization.getId(), H2OFeetMeasurement.class);

        Assertions.assertThat(measurements).hasSize(1);
        Assertions.assertThat(measurements.get(0).location).isEqualTo("coyote_creek");
        Assertions.assertThat(measurements.get(0).description).isNull();
        Assertions.assertThat(measurements.get(0).level).isEqualTo(2.927);
        Assertions.assertThat(measurements.get(0).time).isEqualTo(measurement.time);
    }

    @Test
    void writeMeasurementWithoutFields() {

        String bucketName = bucket.getName();

        writeApi = influxDBClient.getWriteApi();
        WriteEventListener<WriteSuccessEvent> listener = new WriteEventListener<>();
        writeApi.listenEvents(WriteSuccessEvent.class, listener);

        long millis = Instant.now().toEpochMilli();
        H2OFeetMeasurement measurement1 = new H2OFeetMeasurement(
                "coyote_creek", 2.927, null, millis);
        H2OFeetMeasurement measurement2 = new H2OFeetMeasurement(
                "coyote_creek", null, null, millis);

        writeApi.writeMeasurements(bucketName, organization.getId(), WritePrecision.NS, Arrays.asList(measurement1, measurement2));

        waitToCallback(listener.countDownLatch, 10);

        List<H2OFeetMeasurement> measurements = queryApi.query("from(bucket:\"" + bucketName + "\") |> range(start: 1970-01-01T00:00:00.000000001Z) |> last() |> rename(columns:{_value: \"water_level\"})", organization.getId(), H2OFeetMeasurement.class);

        Assertions.assertThat(measurements).hasSize(1);
        Assertions.assertThat(measurements.get(0).location).isEqualTo("coyote_creek");
        Assertions.assertThat(measurements.get(0).description).isNull();
        Assertions.assertThat(measurements.get(0).level).isEqualTo(2.927);
        Assertions.assertThat(measurements.get(0).time).isEqualTo(measurement1.time);
    }

    @Test
    void queryDataFromNewOrganization() throws Exception {

        // Login as operator
        influxDBClient = InfluxDBClientFactory.create(influxDB_URL, "my-token".toCharArray());

        String orgName = generateName("new-org");
        Organization organization =
                influxDBClient.getOrganizationsApi().createOrganization(orgName);

        Bucket bucket = influxDBClient.getBucketsApi()
                .createBucket(generateName("h2o"), retentionRule(), organization);

        PermissionResource bucketResource = new PermissionResource();
        bucketResource.setId(bucket.getId());
        bucketResource.setOrgID(organization.getId());
        bucketResource.setType(PermissionResource.TypeEnum.BUCKETS);

        Permission readBucket = new Permission();
        readBucket.setResource(bucketResource);
        readBucket.setAction(Permission.ActionEnum.READ);

        Permission writeBucket = new Permission();
        writeBucket.setResource(bucketResource);
        writeBucket.setAction(Permission.ActionEnum.WRITE);

        PermissionResource orgResource = new PermissionResource();
        orgResource.setId(organization.getId());
        orgResource.setOrgID(organization.getId());
        orgResource.setType(PermissionResource.TypeEnum.ORGS);

        Permission readOrganization = new Permission();
        readOrganization.setResource(orgResource);
        readOrganization.setAction(Permission.ActionEnum.READ);

        Permission writeOrganization = new Permission();
        writeOrganization.setResource(orgResource);
        writeOrganization.setAction(Permission.ActionEnum.WRITE);

        User loggedUser = influxDBClient.getUsersApi().createUser(generateName("Tom Lik"));
        Assertions.assertThat(loggedUser).isNotNull();

        Authorization authorization = influxDBClient.getAuthorizationsApi()
                .createAuthorization(organization, Arrays.asList(readOrganization, writeOrganization, readBucket, writeBucket));

        token = authorization.getToken();

        influxDBClient.close();

        // Login as new user
        influxDBClient = InfluxDBClientFactory.create(influxDB_URL, token.toCharArray());
        queryApi = influxDBClient.getQueryApi();

        Point point = Point.measurement("h2o_feet")
                .addTag("location", "atlantic")
                .addField("water_level", 1)
                .time(Instant.now(), WritePrecision.MS);

        writeApi = influxDBClient.getWriteApi();
        WriteEventListener<WriteSuccessEvent> listener = new WriteEventListener<>();
        writeApi.listenEvents(WriteSuccessEvent.class, listener);

        writeApi.writePoint(bucket.getName(), organization.getId(), point);

        waitToCallback(listener.countDownLatch, 10);

        String query = queryApi.queryRaw("from(bucket:\"" + bucket.getName() + "\") |> range(start: 1970-01-01T00:00:00.000000001Z) |> last()", organization.getId());
        Assertions.assertThat(query).endsWith("1,water_level,h2o_feet,atlantic\n");
    }

    @Test
    void flush() throws InterruptedException {

        String bucketName = bucket.getName();

        writeApi = influxDBClient.getWriteApi(WriteOptions.builder().batchSize(100_000)
                .flushInterval(100_000).build());

        String record = "h2o_feet,location=coyote_creek level\\ water_level=1.0 1";

        writeApi.writeRecord(bucketName, organization.getId(), WritePrecision.NS, record);

        List<FluxTable> query = queryApi.query("from(bucket:\"" + bucketName + "\") |> range(start: 1970-01-01T00:00:00.000000001Z) |> last()", organization.getId());
        Assertions.assertThat(query).hasSize(0);

        writeApi.flush();
        Thread.sleep(10);

        query = queryApi.query("from(bucket:\"" + bucketName + "\") |> range(start: 1970-01-01T00:00:00.000000001Z) |> last()", organization.getId());

        Assertions.assertThat(query).hasSize(1);
        Assertions.assertThat(query.get(0).getRecords()).hasSize(1);
    }

    @Test
    void flushByTime() throws InterruptedException {

        String bucketName = bucket.getName();

        writeApi = influxDBClient.getWriteApi(WriteOptions.builder().batchSize(100_000)
                .flushInterval(500).build());

        String record1 = "h2o_feet,location=coyote_creek level\\ water_level=1.0 1";
        String record2 = "h2o_feet,location=coyote_creek level\\ water_level=2.0 2";
        String record3 = "h2o_feet,location=coyote_creek level\\ water_level=3.0 3";
        String record4 = "h2o_feet,location=coyote_creek level\\ water_level=4.0 4";
        String record5 = "h2o_feet,location=coyote_creek level\\ water_level=5.0 5";

        writeApi.writeRecords(bucketName, organization.getId(), WritePrecision.NS, Arrays.asList(record1, record2, record3, record4, record5));

        List<FluxTable> query = queryApi.query("from(bucket:\"" + bucketName + "\") |> range(start: 1970-01-01T00:00:00.000000001Z)", organization.getId());
        Assertions.assertThat(query).hasSize(0);

        Thread.sleep(500);

        query = queryApi.query("from(bucket:\"" + bucketName + "\") |> range(start: 1970-01-01T00:00:00.000000001Z)", organization.getId());

        Assertions.assertThat(query).hasSize(1);
        Assertions.assertThat(query.get(0).getRecords()).hasSize(5);
    }

    @Test
    void flushByCount() throws InterruptedException {

        String bucketName = bucket.getName();

        writeApi = influxDBClient.getWriteApi(WriteOptions.builder().batchSize(6)
                .flushInterval(100_000).build());

        String record1 = "h2o_feet,location=coyote_creek level\\ water_level=1.0 1";
        String record2 = "h2o_feet,location=coyote_creek level\\ water_level=2.0 2";
        String record3 = "h2o_feet,location=coyote_creek level\\ water_level=3.0 3";
        String record4 = "h2o_feet,location=coyote_creek level\\ water_level=4.0 4";
        String record5 = "h2o_feet,location=coyote_creek level\\ water_level=5.0 5";
        String record6 = "h2o_feet,location=coyote_creek level\\ water_level=6.0 6";

        writeApi.writeRecord(bucketName, organization.getId(), WritePrecision.NS, record1);
        writeApi.writeRecord(bucketName, organization.getId(), WritePrecision.NS, record2);
        writeApi.writeRecord(bucketName, organization.getId(), WritePrecision.NS, record3);
        writeApi.writeRecord(bucketName, organization.getId(), WritePrecision.NS, record4);
        writeApi.writeRecord(bucketName, organization.getId(), WritePrecision.NS, record5);

        Thread.sleep(100);
        List<FluxTable> query = queryApi.query("from(bucket:\"" + bucketName + "\") |> range(start: 1970-01-01T00:00:00.000000001Z)", organization.getId());
        Assertions.assertThat(query).hasSize(0);

        writeApi.writeRecord(bucketName, organization.getId(), WritePrecision.NS, record6);
        Thread.sleep(100);

        query = queryApi.query("from(bucket:\"" + bucketName + "\") |> range(start: 1970-01-01T00:00:00.000000001Z)", organization.getId());

        Assertions.assertThat(query).hasSize(1);
        Assertions.assertThat(query.get(0).getRecords()).hasSize(6);
    }

    @Test
    void jitter() throws InterruptedException {

        String bucketName = bucket.getName();

        writeApi = influxDBClient.getWriteApi(WriteOptions.builder().batchSize(1)
                .jitterInterval(4_000)
                .build());

        String record = "h2o_feet,location=coyote_creek level\\ water_level=1.0 1";

        writeApi.writeRecord(bucketName, organization.getId(), WritePrecision.NS, record);

        List<FluxTable> query = queryApi.query("from(bucket:\"" + bucketName + "\") |> range(start: 1970-01-01T00:00:00.000000001Z) |> last()", organization.getId());
        Assertions.assertThat(query).hasSize(0);

        Thread.sleep(5000);

        query = queryApi.query("from(bucket:\"" + bucketName + "\") |> range(start: 1970-01-01T00:00:00.000000001Z) |> last()", organization.getId());

        Assertions.assertThat(query).hasSize(1);
        Assertions.assertThat(query.get(0).getRecords()).hasSize(1);
    }

    @Test
    void partialWrite() {

        String bucketName = bucket.getName();

        writeApi = influxDBClient.getWriteApi();

        String record1 = "h2o_feet,location=coyote_creek level\\ water_level=1.0 1";
        String record2 = "h2o_feet,location=coyote_hill level\\ water_level=2.0 2x";

        writeApi.writeRecords(bucket.getName(), organization.getId(), WritePrecision.NS, Arrays.asList(record1, record2));

        writeApi.close();

        List<FluxTable> tables = queryApi.query("from(bucket:\"" + bucketName + "\") |> range(start: 1970-01-01T00:00:00.000000001Z) |> last()", organization.getId());

        Assertions.assertThat(tables).hasSize(0);
    }

    @Test
    void recovery() {

        String bucketName = bucket.getName();

        writeApi = influxDBClient.getWriteApi();
        WriteEventListener<WriteErrorEvent> errorListener = new WriteEventListener<>();
        writeApi.listenEvents(WriteErrorEvent.class, errorListener);

        writeApi.writeRecord(bucketName, organization.getId(), WritePrecision.NS, "h2o_feet,location=coyote_creek level\\ water_level=1.0 1x");

        waitToCallback(errorListener.countDownLatch, 10);

        List<FluxTable> query = queryApi.query("from(bucket:\"" + bucketName + "\") |> range(start: 1970-01-01T00:00:00.000000001Z) |> last()", organization.getId());
        Assertions.assertThat(query).hasSize(0);

        WriteEventListener<WriteSuccessEvent> successListener = new WriteEventListener<>();
        writeApi.listenEvents(WriteSuccessEvent.class, successListener);

        writeApi.writeRecord(bucketName, organization.getId(), WritePrecision.NS, "h2o_feet,location=coyote_creek level\\ water_level=1.0 1");

        waitToCallback(successListener.countDownLatch, 10);

        query = queryApi.query("from(bucket:\"" + bucketName + "\") |> range(start: 1970-01-01T00:00:00.000000001Z) |> last()", organization.getId());
        Assertions.assertThat(query).hasSize(1);
        Assertions.assertThat(query.get(0).getRecords()).hasSize(1);
        Assertions.assertThat(query.get(0).getRecords().get(0).getValueByKey("location")).isEqualTo("coyote_creek");
        Assertions.assertThat(query.get(0).getRecords().get(0).getValueByKey("_field")).isEqualTo("level water_level");
        Assertions.assertThat(query.get(0).getRecords().get(0).getValue()).isEqualTo(1.0D);
    }

    @Test
    void defaultTagsPoint() throws Exception {

        influxDBClient.close();

        System.setProperty("mine-sensor.version", "1.23a");
        String envKey = (String) System.getenv().keySet().toArray()[5];

        InfluxDBClientOptions options = InfluxDBClientOptions.builder().url(influxDB_URL)
                .authenticateToken(token.toCharArray())
                .addDefaultTag("id", "132-987-655")
                .addDefaultTag("customer", "California Miner")
                .addDefaultTag("env-var", "${env." + envKey + "}")
                .addDefaultTag("sensor-version", "${mine-sensor.version}")
                .build();

        influxDBClient = InfluxDBClientFactory.create(options);

        writeApi = influxDBClient.getWriteApi();
        WriteEventListener<WriteSuccessEvent> listener = new WriteEventListener<>();
        writeApi.listenEvents(WriteSuccessEvent.class, listener);

        Instant time = Instant.now();

        Point point = Point.measurement("h2o_feet").addTag("location", "west").addField("water_level", 1).time(time, WritePrecision.MS);

        writeApi.writePoint(bucket.getName(), organization.getId(), point);
        waitToCallback(listener.countDownLatch, 10);

        queryApi = influxDBClient.getQueryApi();
        List<FluxTable> query = queryApi.query("from(bucket:\"" + bucket.getName() + "\") |> range(start: 1970-01-01T00:00:00.000000001Z) |> pivot(rowKey:[\"_time\"], columnKey: [\"_field\"], valueColumn: \"_value\")", organization.getId());

        Assertions.assertThat(query).hasSize(1);
        Assertions.assertThat(query.get(0).getRecords()).hasSize(1);
        Assertions.assertThat(query.get(0).getRecords().get(0).getMeasurement()).isEqualTo("h2o_feet");
        Assertions.assertThat(query.get(0).getRecords().get(0).getValueByKey("water_level")).isEqualTo(1L);
        Assertions.assertThat(query.get(0).getRecords().get(0).getValueByKey("location")).isEqualTo("west");
        Assertions.assertThat(query.get(0).getRecords().get(0).getValueByKey("id")).isEqualTo("132-987-655");
        Assertions.assertThat(query.get(0).getRecords().get(0).getValueByKey("customer")).isEqualTo("California Miner");
        Assertions.assertThat(query.get(0).getRecords().get(0).getValueByKey("sensor-version")).isEqualTo("1.23a");
        Assertions.assertThat(query.get(0).getRecords().get(0).getValueByKey("env-var")).isEqualTo(System.getenv(envKey));
    }

    @Test
    void defaultTagsMeasurement() throws Exception {

        influxDBClient.close();

        System.setProperty("mine-sensor.version", "1.23a");
        String envKey = (String) System.getenv().keySet().toArray()[5];

        InfluxDBClientOptions options = InfluxDBClientOptions.builder()
                .url(influxDB_URL)
                .authenticateToken(token.toCharArray())
                .addDefaultTag("id", "132-987-655")
                .addDefaultTag("customer", "California Miner")
                .addDefaultTag("env-var", "${env." + envKey + "}")
                .addDefaultTag("sensor-version", "${mine-sensor.version}")
                .build();

        influxDBClient = InfluxDBClientFactory.create(options);

        writeApi = influxDBClient.getWriteApi();
        WriteEventListener<WriteSuccessEvent> listener = new WriteEventListener<>();
        writeApi.listenEvents(WriteSuccessEvent.class, listener);

        long millis = Instant.now().toEpochMilli();
        H2OFeetMeasurement measurement = new H2OFeetMeasurement(
                "coyote_creek", 2.927, null, millis);

        writeApi.writeMeasurement(bucket.getName(), organization.getId(), WritePrecision.NS, measurement);
        waitToCallback(listener.countDownLatch, 10);

        queryApi = influxDBClient.getQueryApi();
        List<FluxTable> query = queryApi.query("from(bucket:\"" + bucket.getName() + "\") |> range(start: 1970-01-01T00:00:00.000000001Z) |> pivot(rowKey:[\"_time\"], columnKey: [\"_field\"], valueColumn: \"_value\")", organization.getId());

        Assertions.assertThat(query).hasSize(1);
        Assertions.assertThat(query.get(0).getRecords()).hasSize(1);
        Assertions.assertThat(query.get(0).getRecords().get(0).getMeasurement()).isEqualTo("h2o");
        Assertions.assertThat(query.get(0).getRecords().get(0).getValueByKey("water_level")).isEqualTo(2.927);
        Assertions.assertThat(query.get(0).getRecords().get(0).getValueByKey("location")).isEqualTo("coyote_creek");
        Assertions.assertThat(query.get(0).getRecords().get(0).getValueByKey("id")).isEqualTo("132-987-655");
        Assertions.assertThat(query.get(0).getRecords().get(0).getValueByKey("customer")).isEqualTo("California Miner");
        Assertions.assertThat(query.get(0).getRecords().get(0).getValueByKey("sensor-version")).isEqualTo("1.23a");
        Assertions.assertThat(query.get(0).getRecords().get(0).getValueByKey("env-var")).isEqualTo(System.getenv(envKey));
    }

    @Test
    void defaultTagsFromConfiguration() throws Exception {

        influxDBClient.close();

        System.setProperty("version", "1.23a");

        InfluxDBClientOptions options = InfluxDBClientOptions.builder()
                .loadProperties()
                .url(influxDB_URL)
                .authenticateToken(token.toCharArray())
                .build();

        influxDBClient = InfluxDBClientFactory.create(options);

        writeApi = influxDBClient.getWriteApi();
        WriteEventListener<WriteSuccessEvent> listener = new WriteEventListener<>();
        writeApi.listenEvents(WriteSuccessEvent.class, listener);

        long millis = Instant.now().toEpochMilli();
        H2OFeetMeasurement measurement = new H2OFeetMeasurement(
                "coyote_creek", 2.927, null, millis);

        writeApi.writeMeasurement(bucket.getName(), organization.getId(), WritePrecision.NS, measurement);
        waitToCallback(listener.countDownLatch, 10);

        queryApi = influxDBClient.getQueryApi();
        List<FluxTable> query = queryApi.query("from(bucket:\"" + bucket.getName() + "\") |> range(start: 1970-01-01T00:00:00.000000001Z) |> pivot(rowKey:[\"_time\"], columnKey: [\"_field\"], valueColumn: \"_value\")", organization.getId());

        Assertions.assertThat(query).hasSize(1);
        Assertions.assertThat(query.get(0).getRecords()).hasSize(1);
        Assertions.assertThat(query.get(0).getRecords().get(0).getMeasurement()).isEqualTo("h2o");
        Assertions.assertThat(query.get(0).getRecords().get(0).getValueByKey("water_level")).isEqualTo(2.927);
        Assertions.assertThat(query.get(0).getRecords().get(0).getValueByKey("location")).isEqualTo("coyote_creek");
        Assertions.assertThat(query.get(0).getRecords().get(0).getValueByKey("id")).isEqualTo("132-987-655");
        Assertions.assertThat(query.get(0).getRecords().get(0).getValueByKey("customer")).isEqualTo("California Miner");
        Assertions.assertThat(query.get(0).getRecords().get(0).getValueByKey("version")).isEqualTo("1.23a");
    }

    @Test
    void queryWriteWithGZIP() {

        influxDBClient.enableGzip();

        String bucketName = bucket.getName();

        writeApi = influxDBClient.getWriteApi();

        String record = "h2o_feet,location=coyote_creek level\\ water_level=1.0 1";

        WriteEventListener<WriteSuccessEvent> listener = new WriteEventListener<>();
        writeApi.listenEvents(WriteSuccessEvent.class, listener);

        LOG.log(Level.FINEST, "Write Event Listener count down: {0}. Before write.", countDownLatch);

        writeApi.writeRecord(bucketName, organization.getId(), WritePrecision.NS, record);

        waitToCallback(listener.countDownLatch, 10);

        LOG.log(Level.FINEST, "Write Event Listener count down: {0}. After write.", countDownLatch);
        LOG.log(Level.FINEST, "Listener values: {0} errors: {1}", new Object[]{listener.values, listener.errors});

        Assertions.assertThat(listener.getValue()).isNotNull();
        Assertions.assertThat(listener.getValue().getBucket()).isEqualTo(bucket.getName());
        Assertions.assertThat(listener.getValue().getOrganization()).isEqualTo(organization.getId());
        Assertions.assertThat(listener.getValue().getLineProtocol()).isEqualTo(record);

        List<FluxTable> query = queryApi.query("from(bucket:\"" + bucketName + "\") |> range(start: 1970-01-01T00:00:00.000000001Z) |> last()", organization.getId());

        Assertions.assertThat(query).hasSize(1);
        Assertions.assertThat(query.get(0).getRecords()).hasSize(1);
        Assertions.assertThat(query.get(0).getRecords().get(0).getMeasurement()).isEqualTo("h2o_feet");
        Assertions.assertThat(query.get(0).getRecords().get(0).getValue()).isEqualTo(1.0D);
        Assertions.assertThat(query.get(0).getRecords().get(0).getField()).isEqualTo("level water_level");
        Assertions.assertThat(query.get(0).getRecords().get(0).getTime()).isEqualTo(Instant.ofEpochSecond(0,1));
    }
}