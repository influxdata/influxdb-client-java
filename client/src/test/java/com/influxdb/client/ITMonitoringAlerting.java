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
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.TimeUnit;

import com.influxdb.client.domain.CheckStatusLevel;
import com.influxdb.client.domain.LesserThreshold;
import com.influxdb.client.domain.Organization;
import com.influxdb.client.domain.RuleStatusLevel;
import com.influxdb.client.domain.SlackNotificationEndpoint;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;

import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.assertj.core.api.Assertions;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (25/09/2019 09:41)
 */
@RunWith(JUnitPlatform.class)
class ITMonitoringAlerting extends AbstractITClientTest {

    private MockWebServer mockServer;

    @BeforeEach
    void startSlackServer() {
        mockServer = new MockWebServer();
        try {
            mockServer.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterEach
    void stopSlackServer() throws IOException {
        mockServer.shutdown();
    }

    @Test
    //TODO fix CI
    @Disabled
    public void createMonitoringAndAlerting() throws InterruptedException {

        Organization org = findMyOrg();

        ChecksApi checksApi = influxDBClient.getChecksApi();
        NotificationEndpointsApi notificationEndpointsApi = influxDBClient.getNotificationEndpointsApi();
        NotificationRulesApi notificationRulesApi = influxDBClient.getNotificationRulesApi();

        //
        // Create Threshold Check
        //
        // Set status to 'Critical' if the 'current' value for 'stock' measurement is lesser than '35'
        //
        String query = "from(bucket: \"my-bucket\") "
                + "|> range(start: v.timeRangeStart, stop: v.timeRangeStop)  "
                + "|> filter(fn: (r) => r._measurement == \"stock\")  "
                + "|> filter(fn: (r) => r.company == \"zyz\")  "
                + "|> aggregateWindow(every: 5s, fn: mean)  "
                + "|> yield(name: \"mean\")";

        LesserThreshold threshold = new LesserThreshold();
        threshold.setLevel(CheckStatusLevel.CRIT);
        threshold.setValue(35F);

        String message = "The Stock price for XYZ is on: ${ r._level } level!";

        checksApi.createThresholdCheck(generateName("XYZ Stock value"), query, "current", "5s", message, threshold, org.getId());

        //
        // Create Slack Notification endpoint
        //
        String url = "http://" + getHostNetwork() + ":" + mockServer.getPort();
        SlackNotificationEndpoint endpoint = notificationEndpointsApi.createSlackEndpoint(generateName("Slack Endpoint"), url, org.getId());

        //
        // Create Notification Rule
        //
        // Send message if the status is 'Critical'
        //
        notificationRulesApi.createSlackRule(generateName("Critical status to Slack"), "10s", "${ r._message }", RuleStatusLevel.CRIT, endpoint, org.getId());

        //
        // Write data
        //
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        Point measurement = Point
                .measurement("stock").addTag("company", "zyz")
                .addField("current", 33.65)
                .time(now.toInstant(), WritePrecision.NS);

        influxDBClient.getWriteApiBlocking().writePoint("my-bucket", "my-org", measurement);

        RecordedRequest request = mockServer.takeRequest(30, TimeUnit.SECONDS);
        Assertions.assertThat(request).isNotNull();

        JSONObject json = new JSONObject(request.getBody().readUtf8());
        Assertions.assertThat(json.has("attachments")).isTrue();

        JSONArray attachments = json.getJSONArray("attachments");
        Assertions.assertThat(attachments).hasSize(1);

        JSONObject notification = attachments.getJSONObject(0);
        Assertions.assertThat(notification).isNotNull();
        Assertions.assertThat(notification.getString("text")).isEqualTo("The Stock price for XYZ is on: crit level!");
    }

    private String getHostNetwork() {
        if (System.getProperty("os.name").toLowerCase().contains("nix")) {
            return "docker.for.lin.host.internal";
        }
        return "host.docker.internal";
    }
}
