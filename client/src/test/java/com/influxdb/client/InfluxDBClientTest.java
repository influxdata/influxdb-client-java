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

import com.influxdb.LogLevel;
import com.influxdb.client.domain.Authorization;
import com.influxdb.client.domain.Run;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.internal.AbstractInfluxDBClientTest;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (05/09/2018 14:00)
 */
@RunWith(JUnitPlatform.class)
class InfluxDBClientTest extends AbstractInfluxDBClientTest {

    @Test
    void createQueryClient() {
        Assertions.assertThat(influxDBClient.getQueryApi()).isNotNull();
    }

    @Test
    void createWriteClient() {

        Assertions.assertThat(influxDBClient.getWriteApi()).isNotNull();
        Assertions.assertThat(influxDBClient.getWriteApi(WriteOptions.DEFAULTS)).isNotNull();
    }

    @Test
    void createAuthorizationClient() {
        Assertions.assertThat(influxDBClient.getAuthorizationsApi()).isNotNull();
    }

    @Test
    void createBucketClient() {
        Assertions.assertThat(influxDBClient.getBucketsApi()).isNotNull();
    }

    @Test
    void createOrganizationClient() {
        Assertions.assertThat(influxDBClient.getOrganizationsApi()).isNotNull();
    }

    @Test
    void createSourceClient() {
        Assertions.assertThat(influxDBClient.getSourcesApi()).isNotNull();
    }

    @Test
    void createTaskClient() {
        Assertions.assertThat(influxDBClient.getTasksApi()).isNotNull();
    }

    @Test
    void createUserClient() {
        Assertions.assertThat(influxDBClient.getUsersApi()).isNotNull();
    }

    @Test
    void createWriteBlockingApi() {
        Assertions.assertThat(influxDBClient.getWriteApiBlocking()).isNotNull();
    }

    @Test
    public void createNotificationEndpointApi() {
        Assertions.assertThat(influxDBClient.getNotificationEndpointsApi()).isNotNull();
    }

    @Test
    public void createChecksApi() {
        Assertions.assertThat(influxDBClient.getChecksApi()).isNotNull();
    }

    @Test
    public void createNotificationRulesApi() {
        Assertions.assertThat(influxDBClient.getNotificationRulesApi()).isNotNull();
    }

    @Test
    void logLevel() {

        // default NONE
        Assertions.assertThat(this.influxDBClient.getLogLevel()).isEqualTo(LogLevel.NONE);

        // set HEADERS
        InfluxDBClient influxDBClient = this.influxDBClient.setLogLevel(LogLevel.HEADERS);
        Assertions.assertThat(influxDBClient).isEqualTo(this.influxDBClient);

        Assertions.assertThat(this.influxDBClient.getLogLevel()).isEqualTo(LogLevel.HEADERS);
    }

    @Test
    void gzip() {

        Assertions.assertThat(this.influxDBClient.isGzipEnabled()).isFalse();

        // Enable GZIP
        InfluxDBClient influxDBClient = this.influxDBClient.enableGzip();
        Assertions.assertThat(influxDBClient).isEqualTo(this.influxDBClient);
        Assertions.assertThat(this.influxDBClient.isGzipEnabled()).isTrue();

        // Disable GZIP
        influxDBClient = this.influxDBClient.disableGzip();
        Assertions.assertThat(influxDBClient).isEqualTo(this.influxDBClient);
        Assertions.assertThat(this.influxDBClient.isGzipEnabled()).isFalse();
    }

    @Test
    void close() throws Exception {

        influxDBClient.close();
        Assertions.assertThat(mockServer.getRequestCount()).isEqualTo(0);
    }

    @Test
    void closeWithSignout() throws Exception {

        mockServer.enqueue(new MockResponse());
        mockServer.enqueue(new MockResponse());

        InfluxDBClient influxDBClient = InfluxDBClientFactory
                .create(mockServer.url("/").toString(), "user", "password".toCharArray());

        Assertions.assertThat(mockServer.getRequestCount()).isEqualTo(1);
        influxDBClient.close();
        Assertions.assertThat(mockServer.getRequestCount()).isEqualTo(2);

        // sign in
        mockServer.takeRequest();
        // request to signout
        RecordedRequest signOut = mockServer.takeRequest();
        Assertions.assertThat(signOut.getPath()).endsWith("/api/v2/signout");
    }

    @Test
    void parseUnknownEnumAsNull() {

        mockServer.enqueue(new MockResponse().setBody("{\"status\":\"active\"}"));
        mockServer.enqueue(new MockResponse().setBody("{\"status\":\"unknown\"}"));

        Authorization authorization = influxDBClient.getAuthorizationsApi().findAuthorizationByID("id");
        Assertions.assertThat(authorization).isNotNull();
        Assertions.assertThat(authorization.getStatus()).isEqualTo(Authorization.StatusEnum.ACTIVE);

        authorization = influxDBClient.getAuthorizationsApi().findAuthorizationByID("id");
        Assertions.assertThat(authorization).isNotNull();
        Assertions.assertThat(authorization.getStatus()).isNull();
    }

    @Test
    void parseDateTime() {
        mockServer.enqueue(new MockResponse().setBody("{\"id\":\"runID\",\"taskID\":\"taskID\",\"startedAt\":\"2019-03-11T11:57:30.830995162Z\"}"));

        Run run = influxDBClient.getTasksApi().getRun("taskID", "runID");

        Assertions.assertThat(run).isNotNull();
        Assertions.assertThat(run.getStartedAt()).isNotNull();
    }

    @Test
    public void autoClosable() {
        try (InfluxDBClient client = InfluxDBClientFactory.create(mockServer.url("/").url().toString())) {
            Assertions.assertThat(client).isNotNull();
        }
    }

    @Test
    public void trailingSlashInUrl() throws InterruptedException {
        mockServer.enqueue(new MockResponse());
        mockServer.enqueue(new MockResponse());

        String path = mockServer.url("/").toString();
        InfluxDBClient influxDBClient = InfluxDBClientFactory
                .create(path, "my-token".toCharArray());

        influxDBClient.getWriteApiBlocking().writeRecord("my-bucket", "my-org", WritePrecision.NS, "record,tag=a value=1");

        RecordedRequest request = mockServer.takeRequest();
        Assertions.assertThat(request.getRequestUrl().toString()).isEqualTo(path + "api/v2/write?org=my-org&bucket=my-bucket&precision=ns");
        influxDBClient.close();

        influxDBClient = InfluxDBClientFactory
                .create(path.substring(0, path.length() - 1), "my-token".toCharArray());

        influxDBClient.getWriteApiBlocking().writeRecord("my-bucket", "my-org", WritePrecision.NS, "record,tag=a value=1");

        request = mockServer.takeRequest();
        Assertions.assertThat(request.getRequestUrl().toString()).isEqualTo(path + "api/v2/write?org=my-org&bucket=my-bucket&precision=ns");
        influxDBClient.close();
    }

    @Test
    void customPath() throws InterruptedException {

        // http://localhost:8086
        String serverURL = String.format("http://%s:%d", mockServer.url("").host(), mockServer.url("").port());

        String[][] connectionStrings = {
                // http://localhost:8086/influxDB/ -> http://localhost:8086/influxDB/api/v2/query
                {serverURL + "/influxDB/", serverURL + "/influxDB/api/v2/query"},
                // http://localhost:8086/influxDB -> http://localhost:8086/influxDB/api/v2/query
                {serverURL + "/influxDB", serverURL + "/influxDB/api/v2/query"},
                // http://localhost:8086/ -> http://localhost:8086/api/v2/query
                {serverURL + "/", serverURL + "/api/v2/query"},
                // http://localhost:8086 -> http://localhost:8086/api/v2/query
                {serverURL, serverURL + "/api/v2/query"},
                // http://localhost:8086?readTimeout=1000&writeTimeout=3000&connectTimeout=2000&logLevel=HEADERS" -> http://localhost:8086/api/v2/query
                {serverURL + "?readTimeout=1000&writeTimeout=3000&connectTimeout=2000&logLevel=HEADERS", serverURL + "/api/v2/query"},
                // http://localhost:8086/influx?readTimeout=1000&writeTimeout=3000&connectTimeout=2000&logLevel=HEADERS" -> http://localhost:8086/influx/api/v2/query
                {serverURL + "/influx?readTimeout=1000&writeTimeout=3000&connectTimeout=2000&logLevel=HEADERS", serverURL + "/influx/api/v2/query"}
        };

        for (String[] connectionString : connectionStrings) {

            mockServer.enqueue(new MockResponse());
            mockServer.enqueue(new MockResponse());

            // client via url
            InfluxDBClient clientURL = InfluxDBClientFactory.create(connectionString[0], "my-token".toCharArray());
            clientURL.getQueryApi().query("from(bucket:\"test\") |> range(start:-5m)", "my-org");
            queryAndTest(connectionString[1]);

            // client via connectionString
            InfluxDBClient clientConnectionString = InfluxDBClientFactory.create(connectionString[0]);
            clientConnectionString.getQueryApi().query("from(bucket:\"test\") |> range(start:-5m)", "my-org");
            queryAndTest(connectionString[1]);
            
            clientURL.close();
            clientConnectionString.close();
        }
    }

    private void queryAndTest(final String expected) throws InterruptedException {
        RecordedRequest request = takeRequest();
        Assertions.assertThat(request).isNotNull();
        Assertions.assertThat(request.getRequestUrl()).isNotNull();
        Assertions.assertThat(request.getRequestUrl().toString()).isEqualTo(expected + "?org=my-org");
    }
}