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
import java.util.Arrays;
import java.util.List;

import com.influxdb.client.domain.Authorization;
import com.influxdb.client.domain.Bucket;
import com.influxdb.client.domain.Organization;
import com.influxdb.client.domain.Permission;
import com.influxdb.client.domain.PermissionResource;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.exceptions.BadRequestException;
import com.influxdb.exceptions.InfluxException;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (16/07/2019 07:13)
 */
@RunWith(JUnitPlatform.class)
class ITWriteApiBlocking extends AbstractITClientTest {

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

        InfluxDBClientOptions options = InfluxDBClientOptions.builder()
                .url(influxDB_URL)
                .authenticateToken(token.toCharArray())
                .org(organization.getId())
                .bucket(bucket.getName())
                .build();
        influxDBClient = InfluxDBClientFactory.create(options);
    }

    @Test
    void write() {

        WriteApiBlocking api = influxDBClient.getWriteApiBlocking();

        // By LineProtocol
        api.writeRecord(WritePrecision.NS, "h2o,location=coyote_creek water_level=1.0 1");
        api.writeRecord(bucket.getName(), organization.getName(), WritePrecision.NS, "h2o,location=coyote_creek water_level=2.0 2");
        api.writeRecord(bucket.getName(), organization.getName(), WritePrecision.NS, null);
        api.writeRecords(WritePrecision.NS, Arrays.asList("h2o,location=coyote_creek water_level=3.0 3", "h2o,location=coyote_creek water_level=4.0 4"));
        api.writeRecords(bucket.getName(), organization.getName(), WritePrecision.NS, Arrays.asList("h2o,location=coyote_creek water_level=5.0 5", "h2o,location=coyote_creek water_level=6.0 6"));

        // By DataPoint
        api.writePoint(Point.measurement("h2o").addTag("location", "coyote_creek").addField("water_level", 7.0D).time(7L, WritePrecision.NS));
        api.writePoint(bucket.getName(), organization.getName(), Point.measurement("h2o").addTag("location", "coyote_creek").addField("water_level", 8.0D).time(8L, WritePrecision.NS));
        api.writePoint(bucket.getName(), organization.getName(), null);
        api.writePoints(Arrays.asList(Point.measurement("h2o").addTag("location", "coyote_creek").addField("water_level", 9.0D).time(9L, WritePrecision.NS), Point.measurement("h2o").addTag("location", "coyote_creek").addField("water_level", 10.0D).time(10L, WritePrecision.NS)));
        api.writePoints(bucket.getName(), organization.getName(), Arrays.asList(Point.measurement("h2o").addTag("location", "coyote_creek").addField("water_level", 11.0D).time(11L, WritePrecision.NS), Point.measurement("h2o").addTag("location", "coyote_creek").addField("water_level", 12.0D).time(12L, WritePrecision.NS)));

        // By Measurement
        api.writeMeasurement(WritePrecision.NS, new H2OFeetMeasurement("coyote_creek", 13.0D, null, Instant.ofEpochSecond(0, 13)));
        api.writeMeasurement(bucket.getName(), organization.getName(), WritePrecision.NS, new H2OFeetMeasurement("coyote_creek", 14.0D, null, Instant.ofEpochSecond(0, 14)));
        api.writeMeasurement(bucket.getName(), organization.getName(), WritePrecision.NS, null);
        api.writeMeasurements(WritePrecision.NS, Arrays.asList(new H2OFeetMeasurement("coyote_creek", 15.0D, null, Instant.ofEpochSecond(0, 15)), new H2OFeetMeasurement("coyote_creek", 16.0D, null, Instant.ofEpochSecond(0, 16))));
        api.writeMeasurements(bucket.getName(), organization.getName(), WritePrecision.NS, Arrays.asList(new H2OFeetMeasurement("coyote_creek", 17.0D, null, Instant.ofEpochSecond(0, 17)), new H2OFeetMeasurement("coyote_creek", 18.0D, null, Instant.ofEpochSecond(0, 18))));

        List<FluxTable> query = influxDBClient.getQueryApi().query("from(bucket:\"" + bucket.getName() + "\") |> range(start: 1970-01-01T00:00:00.000000001Z)", organization.getId());

        Assertions.assertThat(query).hasSize(1);
        Assertions.assertThat(query.get(0).getRecords()).hasSize(18);

        for (int i = 1; i <= 17; i++) {
            FluxRecord record = query.get(0).getRecords().get(i - 1);
            Assertions.assertThat(record.getMeasurement()).isEqualTo("h2o");
            Assertions.assertThat(record.getValue()).isEqualTo((double) i);
            Assertions.assertThat(record.getField()).isEqualTo("water_level");
            Assertions.assertThat(record.getTime()).isEqualTo(Instant.ofEpochSecond(0, i));
        }
    }

    @Test
    void writePointsWithoutFields() {

        WriteApiBlocking api = influxDBClient.getWriteApiBlocking();

        api.writePoints(Arrays.asList(
                Point.measurement("h2o").addTag("location", "coyote_creek").time(9L, WritePrecision.NS),
                Point.measurement("h2o").addTag("location", "coyote_creek").addField("water_level", 10.0D).time(10L, WritePrecision.NS)));

        List<FluxTable> query = influxDBClient.getQueryApi().query("from(bucket:\"" + bucket.getName() + "\") |> range(start: 1970-01-01T00:00:00.000000001Z)", organization.getId());

        Assertions.assertThat(query).hasSize(1);
        Assertions.assertThat(query.get(0).getRecords()).hasSize(1);

        FluxRecord record = query.get(0).getRecords().get(0);
        Assertions.assertThat(record.getMeasurement()).isEqualTo("h2o");
        Assertions.assertThat(record.getValue()).isEqualTo(10D);
        Assertions.assertThat(record.getField()).isEqualTo("water_level");
        Assertions.assertThat(record.getTime()).isEqualTo(Instant.ofEpochSecond(0, 10));
    }

    @Test
    void writeMeasurementWithoutFields() {

        WriteApiBlocking api = influxDBClient.getWriteApiBlocking();

        api.writeMeasurements(WritePrecision.NS, Arrays.asList(
                new H2OFeetMeasurement("coyote_creek", null, null, Instant.ofEpochSecond(0, 15)),
                new H2OFeetMeasurement("coyote_creek", 16.0D, null, Instant.ofEpochSecond(0, 16))));

        List<FluxTable> query = influxDBClient.getQueryApi().query("from(bucket:\"" + bucket.getName() + "\") |> range(start: 1970-01-01T00:00:00.000000001Z)", organization.getId());

        Assertions.assertThat(query).hasSize(1);
        Assertions.assertThat(query.get(0).getRecords()).hasSize(1);

        FluxRecord record = query.get(0).getRecords().get(0);
        Assertions.assertThat(record.getMeasurement()).isEqualTo("h2o");
        Assertions.assertThat(record.getValue()).isEqualTo(16D);
        Assertions.assertThat(record.getField()).isEqualTo("water_level");
        Assertions.assertThat(record.getTime()).isEqualTo(Instant.ofEpochSecond(0, 16));
    }

    @Test
    void propagateServerException() {

        WriteApiBlocking api = influxDBClient.getWriteApiBlocking();

        Assertions.assertThatThrownBy(() -> api.writeRecord(WritePrecision.NS, "h2o,location=coyote_creek"))
                .hasMessageStartingWith("unable to parse points: unable to parse 'h2o,location=coyote_creek': missing fields")
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void propagateHttpException() throws Exception {

        logout();

        influxDBClient = InfluxDBClientFactory.create("http://localhost:9998", "my-token".toCharArray());
        WriteApiBlocking api = influxDBClient.getWriteApiBlocking();

        Assertions.assertThatThrownBy(() -> api.writeRecord(bucket.getName(), organization.getName(), WritePrecision.NS, "h2o,location=coyote_creek water_level=1.0 1"))
                .hasMessageStartingWith("Failed to connect to")
                .hasMessageEndingWith("9998")
                .isInstanceOf(InfluxException.class);
    }

    @Test
    void defaultTags() throws Exception {

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

        Instant time = Instant.now();

        Point point = Point
                .measurement("h2o")
                .addTag("location", "west")
                .addField("water_level", 1)
                .time(time, WritePrecision.MS);

        influxDBClient.getWriteApiBlocking().writePoint(bucket.getName(), organization.getId(), point);

        List<FluxTable> query = influxDBClient.getQueryApi().query("from(bucket:\"" + bucket.getName() + "\") |> range(start: 1970-01-01T00:00:00.000000001Z) |> pivot(rowKey:[\"_time\"], columnKey: [\"_field\"], valueColumn: \"_value\")", organization.getId());

        Assertions.assertThat(query).hasSize(1);
        Assertions.assertThat(query.get(0).getRecords()).hasSize(1);
        Assertions.assertThat(query.get(0).getRecords().get(0).getMeasurement()).isEqualTo("h2o");
        Assertions.assertThat(query.get(0).getRecords().get(0).getValueByKey("water_level")).isEqualTo(1L);
        Assertions.assertThat(query.get(0).getRecords().get(0).getValueByKey("location")).isEqualTo("west");
        Assertions.assertThat(query.get(0).getRecords().get(0).getValueByKey("id")).isEqualTo("132-987-655");
        Assertions.assertThat(query.get(0).getRecords().get(0).getValueByKey("customer")).isEqualTo("California Miner");
        Assertions.assertThat(query.get(0).getRecords().get(0).getValueByKey("sensor-version")).isEqualTo("1.23a");
        Assertions.assertThat(query.get(0).getRecords().get(0).getValueByKey("env-var")).isEqualTo(System.getenv(envKey));
    }
}