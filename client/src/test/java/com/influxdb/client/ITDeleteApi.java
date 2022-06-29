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
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

import com.influxdb.client.domain.Authorization;
import com.influxdb.client.domain.Bucket;
import com.influxdb.client.domain.Organization;
import com.influxdb.client.domain.Permission;
import com.influxdb.client.domain.PermissionResource;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxTable;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Pavlina Rolincova (rolincova@github) (30/10/2019).
 */
@RunWith(JUnitPlatform.class)
class ITDeleteApi extends AbstractITClientTest {

    private DeleteApi deleteApi;

    private Organization organization;
    private Bucket bucket;

    private String token;

    @BeforeEach
    void setUp() throws Exception {

        organization = findMyOrg();

        bucket = influxDBClient.getBucketsApi()
                .createBucket(generateName("h2o"), null, organization);

        PermissionResource resource = new PermissionResource();
        resource.setId(bucket.getId());
        resource.setOrgID(organization.getId());
        resource.setType(PermissionResource.TYPE_BUCKETS);

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

        deleteApi = influxDBClient.getDeleteApi();
    }

    @Test
    void delete() {

        WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();

        // By DataPoint
        writeApi.writePoint(Point.measurement("h2o").addTag("location", "coyote_creek").addField("water_level", 7.0D).time(1L, WritePrecision.NS));
        writeApi.writePoint(bucket.getName(), organization.getName(), Point.measurement("h2o").addTag("location", "coyote_creek").addField("water_level", 8.0D).time(2L, WritePrecision.NS));
        writeApi.writePoint(bucket.getName(), organization.getName(), null);
        writeApi.writePoints(Arrays.asList(Point.measurement("h2o").addTag("location", "coyote_creek").addField("water_level", 9.0D).time(3L, WritePrecision.NS), Point.measurement("h2o").addTag("location", "coyote_creek").addField("water_level", 10.0D).time(4L, WritePrecision.NS)));
        writeApi.writePoints(bucket.getName(), organization.getName(), Arrays.asList(Point.measurement("h2o").addTag("location", "coyote_creek").addField("water_level", 11.0D).time(5L, WritePrecision.NS), Point.measurement("h2o").addTag("location", "coyote_creek").addField("water_level", 12.0D).time(6L, WritePrecision.NS)));

        writeApi.writePoint(Point.measurement("h2o").addTag("location", "coyote_creek").addField("water_level", 8.0D).time(7L, WritePrecision.NS));
        writeApi.writePoint(bucket.getName(), organization.getName(), Point.measurement("h2o").addTag("location", "coyote_creek").addField("water_level", 9.0D).time(8L, WritePrecision.NS));
        writeApi.writePoints(Arrays.asList(Point.measurement("h2o").addTag("location", "coyote_creek").addField("water_level", 9.0D).time(9L, WritePrecision.NS), Point.measurement("h2o").addTag("location", "coyote_creek").addField("water_level", 11.0D).time(10L, WritePrecision.NS)));
        writeApi.writePoints(bucket.getName(), organization.getName(), Arrays.asList(Point.measurement("h2o").addTag("location", "coyote_creek").addField("water_level", 11.0D).time(11L, WritePrecision.NS), Point.measurement("h2o").addTag("location", "coyote_creek").addField("water_level", 13.0D).time(12L, WritePrecision.NS)));

        List<FluxTable> query = influxDBClient.getQueryApi().query("from(bucket:\"" + bucket.getName() + "\") |> range(start: 1970-01-01T00:00:00.000000001Z)", organization.getId());

        Assertions.assertThat(query).hasSize(1);
        Assertions.assertThat(query.get(0).getRecords()).hasSize(12);

        OffsetDateTime start = OffsetDateTime.ofInstant(Instant.ofEpochSecond(0, 1), ZoneId.of("UTC"));
        OffsetDateTime stop = OffsetDateTime.ofInstant(Instant.ofEpochSecond(0, 12), ZoneId.of("UTC"));

        deleteApi.delete(start, stop, "", bucket, organization);

        List<FluxTable> query2 = influxDBClient.getQueryApi().query("from(bucket:\"" + bucket.getName() + "\") |> range(start: 1970-01-01T00:00:00.000000001Z)", organization.getId());

        Assertions.assertThat(query2).hasSize(0);
    }
}
