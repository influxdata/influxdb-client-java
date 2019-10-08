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

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import com.influxdb.client.domain.HTTPNotificationEndpoint;
import com.influxdb.client.domain.Label;
import com.influxdb.client.domain.NotificationEndpoint;
import com.influxdb.client.domain.NotificationEndpointBase;
import com.influxdb.client.domain.NotificationEndpointType;
import com.influxdb.client.domain.NotificationEndpointUpdate;
import com.influxdb.client.domain.NotificationEndpoints;
import com.influxdb.client.domain.PagerDutyNotificationEndpoint;
import com.influxdb.client.domain.SlackNotificationEndpoint;
import com.influxdb.exceptions.BadRequestException;
import com.influxdb.exceptions.NotFoundException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (10/09/2019 08:50)
 */
@RunWith(JUnitPlatform.class)
class ITNotificationEndpointsApi extends AbstractITClientTest {

    private NotificationEndpointsApi notificationEndpointsApi;
    private String orgID;

    @BeforeEach
    void setUp() {
        notificationEndpointsApi = influxDBClient.getNotificationEndpointsApi();
        orgID = findMyOrg().getId();

        notificationEndpointsApi.findNotificationEndpoints(orgID, new FindOptions())
                .getNotificationEndpoints()
                .stream()
                .filter(notificationEndpoint -> notificationEndpoint.getName().endsWith("-IT"))
                .forEach(notificationEndpoint -> notificationEndpointsApi.deleteNotificationEndpoint(notificationEndpoint));
    }

    @Test
    public void createSlackEndpoint() {

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        SlackNotificationEndpoint endpoint = notificationEndpointsApi
                .createSlackEndpoint(generateName("slack"), "https://hooks.slack.com/services/x/y/z", null, orgID);

        Assertions.assertThat(endpoint).isNotNull();
        Assertions.assertThat(endpoint.getUrl()).isEqualTo("https://hooks.slack.com/services/x/y/z");
        Assertions.assertThat(endpoint.getToken()).isBlank();
        Assertions.assertThat(endpoint.getId()).isNotBlank();
        Assertions.assertThat(endpoint.getOrgID()).isEqualTo(orgID);
        Assertions.assertThat(endpoint.getCreatedAt()).isAfter(now);
        Assertions.assertThat(endpoint.getUpdatedAt()).isAfter(now);
        Assertions.assertThat(endpoint.getName()).startsWith("slack");
        Assertions.assertThat(endpoint.getStatus()).isEqualTo(NotificationEndpointBase.StatusEnum.ACTIVE);
        Assertions.assertThat(endpoint.getLabels()).isEmpty();
        Assertions.assertThat(endpoint.getType()).isEqualTo(NotificationEndpointType.SLACK);
        Assertions.assertThat(endpoint.getLinks()).isNotNull();
        Assertions.assertThat(endpoint.getLinks().getSelf())
                .isEqualTo(String.format("/api/v2/notificationEndpoints/%s", endpoint.getId()));
        Assertions.assertThat(endpoint.getLinks().getLabels())
                .isEqualTo(String.format("/api/v2/notificationEndpoints/%s/labels", endpoint.getId()));
        Assertions.assertThat(endpoint.getLinks().getMembers())
                .isEqualTo(String.format("/api/v2/notificationEndpoints/%s/members", endpoint.getId()));
        Assertions.assertThat(endpoint.getLinks().getOwners())
                .isEqualTo(String.format("/api/v2/notificationEndpoints/%s/owners", endpoint.getId()));
    }

    @Test
    public void createSlackEndpointSecret() {

        SlackNotificationEndpoint endpoint = notificationEndpointsApi
                .createSlackEndpoint(generateName("slack"), "https://hooks.slack.com/services/x/y/z", "slack-secret", orgID);

        Assertions.assertThat(endpoint).isNotNull();
        Assertions.assertThat(endpoint.getToken()).isEqualTo(String.format("secret: %s-token", endpoint.getId()));
    }

    @Test
    public void createSlackEndpointWithDescription() {

        SlackNotificationEndpoint endpoint = new SlackNotificationEndpoint();
        endpoint.setType(NotificationEndpointType.SLACK);
        endpoint.setUrl("https://hooks.slack.com/services/x/y/z");
        endpoint.orgID(orgID);
        endpoint.setName(generateName("slack"));
        endpoint.setDescription("my production slack channel");
        endpoint.setStatus(NotificationEndpointBase.StatusEnum.ACTIVE);

        endpoint = (SlackNotificationEndpoint) notificationEndpointsApi.createEndpoint(endpoint);

        Assertions.assertThat(endpoint.getDescription()).isEqualTo("my production slack channel");
    }

    @Test
    public void createPagerDutyEndpoint() {

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        PagerDutyNotificationEndpoint endpoint = notificationEndpointsApi
                .createPagerDutyEndpoint(generateName("pager-duty"), "https://events.pagerduty.com/v2/enqueue", "secret-key", orgID);

        Assertions.assertThat(endpoint).isNotNull();
        Assertions.assertThat(endpoint.getClientURL()).isEqualTo("https://events.pagerduty.com/v2/enqueue");
        Assertions.assertThat(endpoint.getRoutingKey()).isEqualTo(String.format("secret: %s-routing-key", endpoint.getId()));
        Assertions.assertThat(endpoint.getId()).isNotBlank();
        Assertions.assertThat(endpoint.getOrgID()).isEqualTo(orgID);
        Assertions.assertThat(endpoint.getCreatedAt()).isAfter(now);
        Assertions.assertThat(endpoint.getUpdatedAt()).isAfter(now);
        Assertions.assertThat(endpoint.getName()).startsWith("pager-duty");
        Assertions.assertThat(endpoint.getStatus()).isEqualTo(NotificationEndpointBase.StatusEnum.ACTIVE);
        Assertions.assertThat(endpoint.getLabels()).isEmpty();
        Assertions.assertThat(endpoint.getType()).isEqualTo(NotificationEndpointType.PAGERDUTY);
        Assertions.assertThat(endpoint.getLinks()).isNotNull();
        Assertions.assertThat(endpoint.getLinks().getSelf())
                .isEqualTo(String.format("/api/v2/notificationEndpoints/%s", endpoint.getId()));
        Assertions.assertThat(endpoint.getLinks().getLabels())
                .isEqualTo(String.format("/api/v2/notificationEndpoints/%s/labels", endpoint.getId()));
        Assertions.assertThat(endpoint.getLinks().getMembers())
                .isEqualTo(String.format("/api/v2/notificationEndpoints/%s/members", endpoint.getId()));
        Assertions.assertThat(endpoint.getLinks().getOwners())
                .isEqualTo(String.format("/api/v2/notificationEndpoints/%s/owners", endpoint.getId()));
    }

    @Test
    public void createSlackTokenOrUrlShouldBeDefined() {

        notificationEndpointsApi
                .createSlackEndpoint(generateName("slack"), null, "token", orgID);

        Assertions.assertThatThrownBy(() -> notificationEndpointsApi
                .createSlackEndpoint(generateName("slack"), null, null, orgID))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("slack endpoint URL and token are empty");
    }

    @Test
    public void createHTTPEndpoint() {

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        HTTPNotificationEndpoint endpoint = notificationEndpointsApi
                .createHTTPEndpoint(generateName("http"), "http://localhost:1234/mock",
                        HTTPNotificationEndpoint.MethodEnum.POST, orgID);

        Assertions.assertThat(endpoint).isNotNull();
        Assertions.assertThat(endpoint.getUrl()).isEqualTo("http://localhost:1234/mock");
        Assertions.assertThat(endpoint.getAuthMethod()).isEqualTo(HTTPNotificationEndpoint.AuthMethodEnum.NONE);
        Assertions.assertThat(endpoint.getMethod()).isEqualTo(HTTPNotificationEndpoint.MethodEnum.POST);
        Assertions.assertThat(endpoint.getContentTemplate()).isBlank();
        Assertions.assertThat(endpoint.getUsername()).isBlank();
        Assertions.assertThat(endpoint.getPassword()).isBlank();
        Assertions.assertThat(endpoint.getToken()).isBlank();
        Assertions.assertThat(endpoint.getHeaders()).isEmpty();
        Assertions.assertThat(endpoint.getId()).isNotBlank();
        Assertions.assertThat(endpoint.getOrgID()).isEqualTo(orgID);
        Assertions.assertThat(endpoint.getCreatedAt()).isAfter(now);
        Assertions.assertThat(endpoint.getUpdatedAt()).isAfter(now);
        Assertions.assertThat(endpoint.getName()).startsWith("http");
        Assertions.assertThat(endpoint.getStatus()).isEqualTo(NotificationEndpointBase.StatusEnum.ACTIVE);
        Assertions.assertThat(endpoint.getLabels()).isEmpty();
        Assertions.assertThat(endpoint.getType()).isEqualTo(NotificationEndpointType.HTTP);
        Assertions.assertThat(endpoint.getLinks()).isNotNull();
        Assertions.assertThat(endpoint.getLinks().getSelf())
                .isEqualTo(String.format("/api/v2/notificationEndpoints/%s", endpoint.getId()));
        Assertions.assertThat(endpoint.getLinks().getLabels())
                .isEqualTo(String.format("/api/v2/notificationEndpoints/%s/labels", endpoint.getId()));
        Assertions.assertThat(endpoint.getLinks().getMembers())
                .isEqualTo(String.format("/api/v2/notificationEndpoints/%s/members", endpoint.getId()));
        Assertions.assertThat(endpoint.getLinks().getOwners())
                .isEqualTo(String.format("/api/v2/notificationEndpoints/%s/owners", endpoint.getId()));
    }

    @Test
    public void createHTTPEndpointBasic() {

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        HTTPNotificationEndpoint endpoint = notificationEndpointsApi
                .createHTTPEndpointBasicAuth(generateName("http"), "http://localhost:1234/mock",
                        HTTPNotificationEndpoint.MethodEnum.PUT, "my-user", "my-password", orgID);

        Assertions.assertThat(endpoint).isNotNull();
        Assertions.assertThat(endpoint.getUrl()).isEqualTo("http://localhost:1234/mock");
        Assertions.assertThat(endpoint.getAuthMethod()).isEqualTo(HTTPNotificationEndpoint.AuthMethodEnum.BASIC);
        Assertions.assertThat(endpoint.getMethod()).isEqualTo(HTTPNotificationEndpoint.MethodEnum.PUT);
        Assertions.assertThat(endpoint.getContentTemplate()).isBlank();
        Assertions.assertThat(endpoint.getUsername()).isEqualTo(String.format("secret: %s-username", endpoint.getId()));
        Assertions.assertThat(endpoint.getPassword()).isEqualTo(String.format("secret: %s-password", endpoint.getId()));
        Assertions.assertThat(endpoint.getToken()).isBlank();
        Assertions.assertThat(endpoint.getHeaders()).isEmpty();
        Assertions.assertThat(endpoint.getId()).isNotBlank();
        Assertions.assertThat(endpoint.getOrgID()).isEqualTo(orgID);
        Assertions.assertThat(endpoint.getCreatedAt()).isAfter(now);
        Assertions.assertThat(endpoint.getUpdatedAt()).isAfter(now);
        Assertions.assertThat(endpoint.getName()).startsWith("http");
        Assertions.assertThat(endpoint.getStatus()).isEqualTo(NotificationEndpointBase.StatusEnum.ACTIVE);
        Assertions.assertThat(endpoint.getLabels()).isEmpty();
        Assertions.assertThat(endpoint.getType()).isEqualTo(NotificationEndpointType.HTTP);
        Assertions.assertThat(endpoint.getLinks()).isNotNull();
        Assertions.assertThat(endpoint.getLinks().getSelf())
                .isEqualTo(String.format("/api/v2/notificationEndpoints/%s", endpoint.getId()));
        Assertions.assertThat(endpoint.getLinks().getLabels())
                .isEqualTo(String.format("/api/v2/notificationEndpoints/%s/labels", endpoint.getId()));
        Assertions.assertThat(endpoint.getLinks().getMembers())
                .isEqualTo(String.format("/api/v2/notificationEndpoints/%s/members", endpoint.getId()));
        Assertions.assertThat(endpoint.getLinks().getOwners())
                .isEqualTo(String.format("/api/v2/notificationEndpoints/%s/owners", endpoint.getId()));
    }

    @Test
    public void createHTTPEndpointBearer() {

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        HTTPNotificationEndpoint endpoint = notificationEndpointsApi
                .createHTTPEndpointBearer(generateName("http"), "http://localhost:1234/mock",
                        HTTPNotificationEndpoint.MethodEnum.GET, "my-token", orgID);

        Assertions.assertThat(endpoint).isNotNull();
        Assertions.assertThat(endpoint.getUrl()).isEqualTo("http://localhost:1234/mock");
        Assertions.assertThat(endpoint.getAuthMethod()).isEqualTo(HTTPNotificationEndpoint.AuthMethodEnum.BEARER);
        Assertions.assertThat(endpoint.getMethod()).isEqualTo(HTTPNotificationEndpoint.MethodEnum.GET);
        Assertions.assertThat(endpoint.getContentTemplate()).isBlank();
        Assertions.assertThat(endpoint.getUsername()).isBlank();
        Assertions.assertThat(endpoint.getPassword()).isBlank();
        Assertions.assertThat(endpoint.getToken()).isEqualTo(String.format("secret: %s-token", endpoint.getId()));
        Assertions.assertThat(endpoint.getHeaders()).isEmpty();
        Assertions.assertThat(endpoint.getId()).isNotBlank();
        Assertions.assertThat(endpoint.getOrgID()).isEqualTo(orgID);
        Assertions.assertThat(endpoint.getCreatedAt()).isAfter(now);
        Assertions.assertThat(endpoint.getUpdatedAt()).isAfter(now);
        Assertions.assertThat(endpoint.getName()).startsWith("http");
        Assertions.assertThat(endpoint.getStatus()).isEqualTo(NotificationEndpointBase.StatusEnum.ACTIVE);
        Assertions.assertThat(endpoint.getLabels()).isEmpty();
        Assertions.assertThat(endpoint.getType()).isEqualTo(NotificationEndpointType.HTTP);
        Assertions.assertThat(endpoint.getLinks()).isNotNull();
        Assertions.assertThat(endpoint.getLinks().getSelf())
                .isEqualTo(String.format("/api/v2/notificationEndpoints/%s", endpoint.getId()));
        Assertions.assertThat(endpoint.getLinks().getLabels())
                .isEqualTo(String.format("/api/v2/notificationEndpoints/%s/labels", endpoint.getId()));
        Assertions.assertThat(endpoint.getLinks().getMembers())
                .isEqualTo(String.format("/api/v2/notificationEndpoints/%s/members", endpoint.getId()));
        Assertions.assertThat(endpoint.getLinks().getOwners())
                .isEqualTo(String.format("/api/v2/notificationEndpoints/%s/owners", endpoint.getId()));
    }

    @Test
    public void createHTTPEndpointHeadersTemplate() {

        HashMap<String, String> headers = new HashMap<>();
        headers.put("custom-header", "123");
        headers.put("client", "InfluxDB");

        HTTPNotificationEndpoint endpoint = new HTTPNotificationEndpoint();
        endpoint.setType(NotificationEndpointType.HTTP);
        endpoint.setMethod(HTTPNotificationEndpoint.MethodEnum.POST);
        endpoint.setUrl("http://localhost:1234/mock");
        endpoint.orgID(orgID);
        endpoint.setName(generateName("http"));
        endpoint.setAuthMethod(HTTPNotificationEndpoint.AuthMethodEnum.NONE);
        endpoint.setStatus(NotificationEndpointBase.StatusEnum.ACTIVE);
        endpoint.setHeaders(headers);
        endpoint.setContentTemplate("content - template");

        endpoint = (HTTPNotificationEndpoint) notificationEndpointsApi.createEndpoint(endpoint);

        Assertions.assertThat(endpoint).isNotNull();
        Assertions.assertThat(endpoint.getHeaders()).hasSize(2);
        Assertions.assertThat(endpoint.getHeaders())
                .hasEntrySatisfying("custom-header", value -> Assertions.assertThat(value).isEqualTo("123"));
        Assertions.assertThat(endpoint.getHeaders())
                .hasEntrySatisfying("client", value -> Assertions.assertThat(value).isEqualTo("InfluxDB"));
        Assertions.assertThat(endpoint.getContentTemplate()).isEqualTo("content - template");
    }

    @Test
    public void updateEndpoint() {

        HTTPNotificationEndpoint endpoint = notificationEndpointsApi
                .createHTTPEndpoint(generateName("http"), "http://localhost:1234/mock",
                        HTTPNotificationEndpoint.MethodEnum.POST, orgID);

        endpoint.setName(generateName("updated name"));
        endpoint.setDescription("updated description");
        endpoint.setStatus(NotificationEndpointBase.StatusEnum.INACTIVE);

        endpoint = (HTTPNotificationEndpoint) notificationEndpointsApi.updateEndpoint(endpoint);

        Assertions.assertThat(endpoint.getName()).startsWith("updated name");
        Assertions.assertThat(endpoint.getDescription()).isEqualTo("updated description");
        Assertions.assertThat(endpoint.getStatus()).isEqualTo(HTTPNotificationEndpoint.StatusEnum.INACTIVE);
    }

    @Test
    public void updateEndpointNotExists() {

        NotificationEndpointUpdate update = new NotificationEndpointUpdate()
                .name("not exists name")
                .description("not exists update")
                .status(NotificationEndpointUpdate.StatusEnum.ACTIVE);

        Assertions.assertThatThrownBy(() -> notificationEndpointsApi.updateEndpoint("020f755c3c082000", update))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("notification endpoint not found");
    }

    @Test
    public void deleteEndpoint() {
        PagerDutyNotificationEndpoint created = notificationEndpointsApi
                .createPagerDutyEndpoint(generateName("pager-duty"), "https://events.pagerduty.com/v2/enqueue", "secret-key", orgID);

        PagerDutyNotificationEndpoint found = (PagerDutyNotificationEndpoint) notificationEndpointsApi
                .findNotificationEndpointByID(created.getId());

        notificationEndpointsApi.deleteNotificationEndpoint(found);

        Assertions.assertThatThrownBy(() -> notificationEndpointsApi.findNotificationEndpointByID(found.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("notification endpoint not found");
    }

    @Test
    public void deleteEndpointNotFound() {

        Assertions.assertThatThrownBy(() -> notificationEndpointsApi.deleteNotificationEndpoint("020f755c3c082000"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("notification endpoint not found");
    }

    @Test
    public void findNotificationEndpointByID() {

        PagerDutyNotificationEndpoint endpoint = notificationEndpointsApi
                .createPagerDutyEndpoint(generateName("pager-duty"), "https://events.pagerduty.com/v2/enqueue", "secret-key", orgID);

        PagerDutyNotificationEndpoint found = (PagerDutyNotificationEndpoint) notificationEndpointsApi
                .findNotificationEndpointByID(endpoint.getId());

        Assertions.assertThat(endpoint.getId()).isEqualTo(found.getId());
    }

    @Test
    public void findNotificationEndpointByIDNotFound() {
        Assertions.assertThatThrownBy(() -> notificationEndpointsApi.findNotificationEndpointByID("020f755c3c082000"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("notification endpoint not found");
    }

    @Test
    public void clonePagerDuty() {

        PagerDutyNotificationEndpoint endpoint = notificationEndpointsApi
                .createPagerDutyEndpoint(generateName("pager-duty"), "https://events.pagerduty.com/v2/enqueue", "secret-key", orgID);

        PagerDutyNotificationEndpoint cloned = notificationEndpointsApi
                .clonePagerDutyEndpoint(generateName("cloned-pager-duty"), "routing-key", endpoint);

        Assertions.assertThat(endpoint.getId()).isNotEqualTo(cloned.getId());
        Assertions.assertThat(cloned.getName()).startsWith("cloned-pager-duty");
        Assertions.assertThat(cloned.getClientURL()).isEqualTo("https://events.pagerduty.com/v2/enqueue");
        Assertions.assertThat(cloned.getRoutingKey()).isEqualTo(String.format("secret: %s-routing-key", cloned.getId()));
    }

    @Test
    public void cloneSlack() {

        SlackNotificationEndpoint endpoint = new SlackNotificationEndpoint();
        endpoint.setType(NotificationEndpointType.SLACK);
        endpoint.setUrl("https://hooks.slack.com/services/x/y/z");
        endpoint.orgID(orgID);
        endpoint.setToken("my-slack-token");
        endpoint.setName(generateName("slack"));
        endpoint.setDescription("my production slack channel");
        endpoint.setStatus(NotificationEndpointBase.StatusEnum.ACTIVE);

        endpoint = (SlackNotificationEndpoint) notificationEndpointsApi.createEndpoint(endpoint);

        SlackNotificationEndpoint cloned = notificationEndpointsApi
                .cloneSlackEndpoint(generateName("cloned-slack"), "my-slack-token", endpoint);

        Assertions.assertThat(endpoint.getId()).isNotEqualTo(cloned.getId());
        Assertions.assertThat(cloned.getName()).startsWith("cloned-slack");
        Assertions.assertThat(cloned.getUrl()).isEqualTo("https://hooks.slack.com/services/x/y/z");
        Assertions.assertThat(cloned.getToken()).isEqualTo(String.format("secret: %s-token", cloned.getId()));
        Assertions.assertThat(cloned.getDescription()).isEqualTo("my production slack channel");
        Assertions.assertThat(cloned.getStatus()).isEqualTo(NotificationEndpointBase.StatusEnum.ACTIVE);
        Assertions.assertThat(cloned.getType()).isEqualTo(NotificationEndpointType.SLACK);

    }

    @Test
    public void cloneHttpWithoutAuth() {

        HTTPNotificationEndpoint endpoint = notificationEndpointsApi
                .createHTTPEndpointBearer(generateName("http"), "http://localhost:1234/mock",
                        HTTPNotificationEndpoint.MethodEnum.GET, "my-token", orgID);

        HTTPNotificationEndpoint cloned = notificationEndpointsApi
                .cloneHTTPEndpoint(generateName("cloned-http"), endpoint);

        Assertions.assertThat(endpoint.getId()).isNotEqualTo(cloned.getId());
        Assertions.assertThat(cloned.getName()).startsWith("cloned-http");
        Assertions.assertThat(cloned.getMethod()).isEqualTo(HTTPNotificationEndpoint.MethodEnum.GET);
        Assertions.assertThat(cloned.getAuthMethod()).isEqualTo(HTTPNotificationEndpoint.AuthMethodEnum.NONE);
        Assertions.assertThat(cloned.getToken()).isBlank();
    }

    @Test
    public void cloneHttpBearerToken() {

        HashMap<String, String> headers = new HashMap<>();
        headers.put("custom-header", "123");
        headers.put("client", "InfluxDB");

        HTTPNotificationEndpoint endpoint = new HTTPNotificationEndpoint();
        endpoint.setType(NotificationEndpointType.HTTP);
        endpoint.setMethod(HTTPNotificationEndpoint.MethodEnum.POST);
        endpoint.setUrl("http://localhost:1234/mock");
        endpoint.orgID(orgID);
        endpoint.setName(generateName("http"));
        endpoint.setAuthMethod(HTTPNotificationEndpoint.AuthMethodEnum.BEARER);
        endpoint.setToken("bearer-token");
        endpoint.setStatus(NotificationEndpointBase.StatusEnum.ACTIVE);
        endpoint.setHeaders(headers);
        endpoint.setContentTemplate("content - template");

        endpoint = (HTTPNotificationEndpoint) notificationEndpointsApi.createEndpoint(endpoint);

        Assertions.assertThat(endpoint).isNotNull();
        Assertions.assertThat(endpoint.getHeaders()).hasSize(2);
        Assertions.assertThat(endpoint.getHeaders())
                .hasEntrySatisfying("custom-header", value -> Assertions.assertThat(value).isEqualTo("123"));
        Assertions.assertThat(endpoint.getHeaders())
                .hasEntrySatisfying("client", value -> Assertions.assertThat(value).isEqualTo("InfluxDB"));
        Assertions.assertThat(endpoint.getContentTemplate()).isEqualTo("content - template");

        HTTPNotificationEndpoint cloned = notificationEndpointsApi
                .cloneHTTPEndpointBearer(generateName("cloned-http"), "bearer-token", endpoint);

        Assertions.assertThat(endpoint.getId()).isNotEqualTo(cloned.getId());
        Assertions.assertThat(cloned.getName()).startsWith("cloned-http");
        Assertions.assertThat(cloned.getMethod()).isEqualTo(HTTPNotificationEndpoint.MethodEnum.POST);
        Assertions.assertThat(cloned.getAuthMethod()).isEqualTo(HTTPNotificationEndpoint.AuthMethodEnum.BEARER);
        Assertions.assertThat(cloned.getToken()).isEqualTo(String.format("secret: %s-token", cloned.getId()));
        Assertions.assertThat(cloned.getHeaders()).hasSize(2);
        Assertions.assertThat(cloned.getHeaders())
                .hasEntrySatisfying("custom-header", value -> Assertions.assertThat(value).isEqualTo("123"));
        Assertions.assertThat(cloned.getHeaders())
                .hasEntrySatisfying("client", value -> Assertions.assertThat(value).isEqualTo("InfluxDB"));
        Assertions.assertThat(cloned.getContentTemplate()).isEqualTo("content - template");
    }

    @Test
    public void cloneHttpBearerBasicAuth() {

        HTTPNotificationEndpoint endpoint = notificationEndpointsApi
                .createHTTPEndpointBasicAuth(generateName("http"), "http://localhost:1234/mock",
                        HTTPNotificationEndpoint.MethodEnum.PUT, "my-user", "my-password", orgID);

        Assertions.assertThat(endpoint).isNotNull();
        Assertions.assertThat(endpoint.getHeaders()).isEmpty();

        HTTPNotificationEndpoint cloned = notificationEndpointsApi
                .cloneHTTPEndpointBasicAuth(generateName("cloned-http"), "basic-username", "basic-password", endpoint);

        Assertions.assertThat(endpoint.getId()).isNotEqualTo(cloned.getId());
        Assertions.assertThat(cloned.getName()).startsWith("cloned-http");
        Assertions.assertThat(cloned.getMethod()).isEqualTo(HTTPNotificationEndpoint.MethodEnum.PUT);
        Assertions.assertThat(cloned.getAuthMethod()).isEqualTo(HTTPNotificationEndpoint.AuthMethodEnum.BASIC);
        Assertions.assertThat(cloned.getUsername()).isEqualTo(String.format("secret: %s-username", cloned.getId()));
        Assertions.assertThat(cloned.getPassword()).isEqualTo(String.format("secret: %s-password", cloned.getId()));
        Assertions.assertThat(cloned.getHeaders()).isEmpty();
    }

    @Test
    public void cloneNotFound() {

        Assertions.assertThatThrownBy(() -> notificationEndpointsApi
                .cloneSlackEndpoint("not-found-cloned", "token", "020f755c3c082000"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("notification endpoint not found");

        Assertions.assertThatThrownBy(() -> notificationEndpointsApi
                .clonePagerDutyEndpoint("not-found-cloned", "token", "020f755c3c082000"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("notification endpoint not found");

        Assertions.assertThatThrownBy(() -> notificationEndpointsApi
                .cloneHTTPEndpoint("not-found-cloned", "020f755c3c082000"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("notification endpoint not found");

        Assertions.assertThatThrownBy(() -> notificationEndpointsApi
                .cloneHTTPEndpointBearer("not-found-cloned", "token", "020f755c3c082000"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("notification endpoint not found");

        Assertions.assertThatThrownBy(() -> notificationEndpointsApi
                .cloneHTTPEndpointBasicAuth("not-found-cloned", "username", "password", "020f755c3c082000"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("notification endpoint not found");
    }

    @Test
    void labels() {

        LabelsApi labelsApi = influxDBClient.getLabelsApi();

        NotificationEndpoint endpoint = notificationEndpointsApi
                .createHTTPEndpointBearer(generateName("http"), "http://localhost:1234/mock",
                        HTTPNotificationEndpoint.MethodEnum.GET, "my-token", orgID);

        Map<String, String> properties = new HashMap<>();
        properties.put("color", "green");
        properties.put("location", "west");

        Label label = labelsApi.createLabel(generateName("Cool Resource"), properties, orgID);

        List<Label> labels = notificationEndpointsApi.getLabels(endpoint);
        Assertions.assertThat(labels).hasSize(0);

        Label addedLabel = notificationEndpointsApi.addLabel(label, endpoint).getLabel();
        Assertions.assertThat(addedLabel).isNotNull();
        Assertions.assertThat(addedLabel.getId()).isEqualTo(label.getId());
        Assertions.assertThat(addedLabel.getName()).isEqualTo(label.getName());
        Assertions.assertThat(addedLabel.getProperties()).isEqualTo(label.getProperties());

        labels = notificationEndpointsApi.getLabels(endpoint);
        Assertions.assertThat(labels).hasSize(1);
        Assertions.assertThat(labels.get(0).getId()).isEqualTo(label.getId());
        Assertions.assertThat(labels.get(0).getName()).isEqualTo(label.getName());

        notificationEndpointsApi.deleteLabel(label, endpoint);

        labels = notificationEndpointsApi.getLabels(endpoint);
        Assertions.assertThat(labels).hasSize(0);
    }

    @Test
    void labelAddNotExists() {

        NotificationEndpoint endpoint = notificationEndpointsApi
                .createHTTPEndpointBearer(generateName("http"), "http://localhost:1234/mock",
                        HTTPNotificationEndpoint.MethodEnum.GET, "my-token", orgID);

        Assertions.assertThatThrownBy(() -> notificationEndpointsApi.addLabel("020f755c3c082000", endpoint.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void labelDeleteNotExists() {

        NotificationEndpoint endpoint = notificationEndpointsApi
                .createHTTPEndpointBearer(generateName("http"), "http://localhost:1234/mock",
                        HTTPNotificationEndpoint.MethodEnum.GET, "my-token", orgID);

        notificationEndpointsApi.deleteLabel("020f755c3c082000", endpoint.getId());
    }

    @Test
    void findNotifications() {

        int size = notificationEndpointsApi.findNotificationEndpoints(orgID).size();

        notificationEndpointsApi
                .createHTTPEndpointBearer(generateName("http"), "http://localhost:1234/mock",
                        HTTPNotificationEndpoint.MethodEnum.GET, "my-token", orgID);

        List<NotificationEndpoint> endpoints = notificationEndpointsApi.findNotificationEndpoints(orgID);
        Assertions.assertThat(endpoints).hasSize(size + 1);
    }

    @Test
    void findNotificationsPaging() {

        IntStream
                .range(0, 20 - notificationEndpointsApi.findNotificationEndpoints(orgID).size())
                .forEach(value -> notificationEndpointsApi
                        .createHTTPEndpointBearer(generateName("http"), "http://localhost:1234/mock",
                                HTTPNotificationEndpoint.MethodEnum.GET, "my-token", orgID));

        FindOptions findOptions = new FindOptions();
        findOptions.setLimit(5);

        NotificationEndpoints notificationEndpoints = notificationEndpointsApi.findNotificationEndpoints(orgID, findOptions);
        Assertions.assertThat(notificationEndpoints.getNotificationEndpoints()).hasSize(5);
        Assertions.assertThat(notificationEndpoints.getLinks().getNext()).isEqualTo("/api/v2/notificationEndpoints?descending=false&limit=5&offset=5&orgID=" + orgID);

        notificationEndpoints = notificationEndpointsApi.findNotificationEndpoints(orgID, FindOptions.create(notificationEndpoints.getLinks().getNext()));
        Assertions.assertThat(notificationEndpoints.getNotificationEndpoints()).hasSize(5);
        Assertions.assertThat(notificationEndpoints.getLinks().getNext()).isEqualTo("/api/v2/notificationEndpoints?descending=false&limit=5&offset=10&orgID=" + orgID);

        notificationEndpoints = notificationEndpointsApi.findNotificationEndpoints(orgID, FindOptions.create(notificationEndpoints.getLinks().getNext()));
        Assertions.assertThat(notificationEndpoints.getNotificationEndpoints()).hasSize(5);
        Assertions.assertThat(notificationEndpoints.getLinks().getNext()).isEqualTo("/api/v2/notificationEndpoints?descending=false&limit=5&offset=15&orgID=" + orgID);

        notificationEndpoints = notificationEndpointsApi.findNotificationEndpoints(orgID, FindOptions.create(notificationEndpoints.getLinks().getNext()));
        Assertions.assertThat(notificationEndpoints.getNotificationEndpoints()).hasSize(5);
        Assertions.assertThat(notificationEndpoints.getLinks().getNext()).isEqualTo("/api/v2/notificationEndpoints?descending=false&limit=5&offset=20&orgID=" + orgID);

        notificationEndpoints = notificationEndpointsApi.findNotificationEndpoints(orgID, FindOptions.create(notificationEndpoints.getLinks().getNext()));
        Assertions.assertThat(notificationEndpoints.getNotificationEndpoints()).hasSize(0);
        Assertions.assertThat(notificationEndpoints.getLinks().getNext()).isNull();
    }
}
