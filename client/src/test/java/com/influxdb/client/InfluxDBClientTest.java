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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Objects;
import javax.annotation.Nonnull;

import com.influxdb.LogLevel;
import com.influxdb.client.domain.Authorization;
import com.influxdb.client.domain.Run;
import com.influxdb.client.domain.WriteConsistency;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.internal.AbstractInfluxDBClientTest;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
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

        Assertions.assertThat(influxDBClient.makeWriteApi()).isNotNull();
        Assertions.assertThat(influxDBClient.makeWriteApi(WriteOptions.DEFAULTS)).isNotNull();
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

        // verify Cloud URL
        {
            InfluxDBClientOptions options = InfluxDBClientOptions.builder()
                    .connectionString("https://us-west-2-1.aws.cloud2.influxdata.com/")
                    .build();
            Assertions.assertThat(options.getUrl()).isEqualTo("https://us-west-2-1.aws.cloud2.influxdata.com:443/");

            options = InfluxDBClientOptions.builder()
                    .url("https://us-west-2-1.aws.cloud2.influxdata.com/")
                    .authenticateToken("my-token".toCharArray())
                    .org("my-org")
                    .build();
            Assertions.assertThat(options.getUrl()).isEqualTo("https://us-west-2-1.aws.cloud2.influxdata.com:443/");
        }
    }

    @Test
    public void proxy() throws InterruptedException, IOException {

        mockServer.enqueue(new MockResponse());

        OkHttpClient httpClient = new OkHttpClient.Builder().build();

        MockWebServer proxy = new MockWebServer();
        proxy.setDispatcher(new Dispatcher() {

            @Nonnull
            @Override
            public MockResponse dispatch(@Nonnull final RecordedRequest recordedRequest) {

                HttpUrl httpUrl = HttpUrl.parse(mockServer.url("/") + recordedRequest.getPath());

                Request.Builder requestBuilder = new Request.Builder()
                        .url(Objects.requireNonNull(httpUrl).newBuilder().build())
                        .headers(recordedRequest.getHeaders());

                try {
                    Response response = httpClient.newCall(requestBuilder.build()).execute();

                    return new MockResponse()
                            .setHeaders(response.headers())
                            .setBody(Objects.requireNonNull(response.body()).string())
                            .setResponseCode(response.code());

                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            }
        });

        String token = "my-token";
        OkHttpClient.Builder okHttpClient = new OkHttpClient.Builder()
                .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", proxy.getPort())));

        InfluxDBClientOptions options = InfluxDBClientOptions.builder()
                .url(mockServer.url("/").toString())
                .authenticateToken(token.toCharArray())
                .okHttpClient(okHttpClient)
                .build();

        InfluxDBClient client = InfluxDBClientFactory.create(options);

        client.ping();

        RecordedRequest request = mockServer.takeRequest();
        Assertions.assertThat(request.getHeader("Authorization")).isEqualTo("Token my-token");

        // dispose
        client.close();
        proxy.shutdown();
    }

    @Test
    public void permanentRedirect() throws InterruptedException, IOException {

        mockServer.enqueue(new MockResponse());

        MockWebServer proxy = new MockWebServer();
        proxy.setDispatcher(new Dispatcher() {
            @Nonnull
            @Override
            public MockResponse dispatch(@Nonnull final RecordedRequest recordedRequest) {
                return new MockResponse()
                        .setResponseCode(301)
                        .setHeader("Location", mockServer.url("/").url().toString());
            }
        });

        String token = "my-token";
        OkHttpClient.Builder okHttpClient = new OkHttpClient.Builder()
                .addNetworkInterceptor(new Interceptor() {
                    @Nonnull
                    @Override
                    public Response intercept(@Nonnull final Chain chain) throws IOException {
                        Request authorization = chain.request().newBuilder()
                                .header("Authorization", "Token " + token)
                                .build();
                        return chain.proceed(authorization);
                    }
                });
        InfluxDBClientOptions options = InfluxDBClientOptions.builder()
                .url(proxy.url("/").toString())
                .authenticateToken(token.toCharArray())
                .okHttpClient(okHttpClient)
                .build();

        InfluxDBClient client = InfluxDBClientFactory.create(options);

        client.ping();

        RecordedRequest request = mockServer.takeRequest();
        Assertions.assertThat(request.getHeader("Authorization")).isEqualTo("Token my-token");

        // dispose
        client.close();
        proxy.shutdown();
    }

    @Test
    public void connectionStringPrecision() {
        InfluxDBClientOptions options = InfluxDBClientOptions.builder()
                .connectionString("https://us-west-2-1.aws.cloud2.influxdata.com?precision=US")
                .build();
        
        Assertions.assertThat(options.getPrecision()).isEqualTo(WritePrecision.US);
    }

    @Test
    public void propertiesPrecision() {
        InfluxDBClientOptions options = InfluxDBClientOptions.builder().loadProperties().build();
        
        Assertions.assertThat(options.getPrecision()).isEqualTo(WritePrecision.US);
    }

    @Test
    public void connectionStringConsistency() {
        InfluxDBClientOptions options = InfluxDBClientOptions.builder()
                .connectionString("https://us-west-2-1.aws.cloud2.influxdata.com?consistency=QUORUM")
                .build();

        Assertions.assertThat(options.getConsistency()).isEqualTo(WriteConsistency.QUORUM);
    }

    @Test
    public void propertiesConsistency() {
        InfluxDBClientOptions options = InfluxDBClientOptions.builder().loadProperties().build();

        Assertions.assertThat(options.getConsistency()).isEqualTo(WriteConsistency.QUORUM);
    }

    private void queryAndTest(final String expected) throws InterruptedException {
        RecordedRequest request = takeRequest();
        Assertions.assertThat(request).isNotNull();
        Assertions.assertThat(request.getRequestUrl()).isNotNull();
        Assertions.assertThat(request.getRequestUrl().toString()).isEqualTo(expected + "?org=my-org");
    }
}