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

import java.util.Arrays;

import com.influxdb.client.domain.Authorization;
import com.influxdb.client.domain.Bucket;
import com.influxdb.client.domain.Organization;
import com.influxdb.client.domain.Permission;
import com.influxdb.client.domain.PermissionResource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * @author Jakub Bednar (24/01/2020 10:05)
 */
abstract class AbstractITWrite extends AbstractITClientTest {

    protected Bucket bucket;
    protected Organization organization;
    protected String token;

    @BeforeEach
    void setUp() {

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
    }

    @AfterEach
    void tearDown() {

        influxDBClient.close();
    }
}
