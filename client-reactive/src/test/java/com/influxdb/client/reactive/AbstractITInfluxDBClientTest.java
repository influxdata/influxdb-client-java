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
package com.influxdb.client.reactive;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.domain.Organization;
import com.influxdb.test.AbstractTest;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * @author Jakub Bednar (bednar@github) (11/09/2018 10:29)
 */
abstract class AbstractITInfluxDBClientTest extends AbstractTest {

    private static final Logger LOG = Logger.getLogger(AbstractITInfluxDBClientTest.class.getName());

    InfluxDBClientReactive influxDBClient;
    String influxDB_URL;
    Organization organization;

    @BeforeEach
    void setUp() throws Exception {

        influxDB_URL = getInfluxDb2Url();
        LOG.log(Level.FINEST, "InfluxDB URL: {0}", influxDB_URL);

        InfluxDBClient influxDBClient = InfluxDBClientFactory.create(influxDB_URL, "my-user", "my-password".toCharArray());

        organization = influxDBClient.getOrganizationsApi()
                .findOrganizations().stream()
                .filter(organization -> organization.getName().equals("my-org"))
                .findFirst()
                .orElseThrow(IllegalStateException::new);

        influxDBClient.close();

        try {
            this.influxDBClient = InfluxDBClientReactiveFactory.create(influxDB_URL, "my-user", "my-password".toCharArray());
        } catch (Exception e) {
            Assertions.fail("Can't authorize via password", e);
        }

    }

    @AfterEach
    void logout() throws Exception {
        influxDBClient.close();
    }

}