package org.influxdata.client;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.influxdata.LogLevel;
import org.influxdata.client.domain.Dashboard;
import org.influxdata.client.domain.Organization;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (01/04/2019 10:58)
 */
@RunWith(JUnitPlatform.class)
class ITDashboardsApiTest extends AbstractITClientTest {

    private DashboardsApi dashboardsApi;
    private Organization organization;

    @BeforeEach
    void setUp() {

        dashboardsApi = influxDBClient.getDashboardsApi();
        organization = findMyOrg();

        influxDBClient.setLogLevel(LogLevel.BODY);
    }

    @Test
    void createDashboard() {

        Dashboard dashboard = dashboardsApi.createDashboard(generateName("dashboard"), "coolest dashboard", organization.getId());

        Assertions.assertThat(dashboard).isNotNull();
        Assertions.assertThat(dashboard.getId()).isNotEmpty();
        Assertions.assertThat(dashboard.getOrgID()).isEqualTo(organization.getId());
        Assertions.assertThat(dashboard.getCells()).hasSize(0);
        Assertions.assertThat(dashboard.getMeta()).isNotNull();
        Assertions.assertThat(dashboard.getMeta().getCreatedAt()).isBefore(OffsetDateTime.now(ZoneOffset.UTC));
        Assertions.assertThat(dashboard.getMeta().getUpdatedAt()).isBefore(OffsetDateTime.now(ZoneOffset.UTC));
        Assertions.assertThat(dashboard.getLabels()).hasSize(0);
        Assertions.assertThat(dashboard.getLinks()).isNotNull();
        Assertions.assertThat(dashboard.getLinks().getSelf()).isEqualTo("/api/v2/dashboards/" + dashboard.getId());
        Assertions.assertThat(dashboard.getLinks().getMembers()).isEqualTo("/api/v2/dashboards/" + dashboard.getId() + "/members");
        Assertions.assertThat(dashboard.getLinks().getOwners()).isEqualTo("/api/v2/dashboards/" + dashboard.getId() + "/owners");
        Assertions.assertThat(dashboard.getLinks().getCells()).isEqualTo("/api/v2/dashboards/" + dashboard.getId() + "/cells");
        Assertions.assertThat(dashboard.getLinks().getLogs()).isEqualTo("/api/v2/dashboards/" + dashboard.getId() + "/logs");

        //TODO https://github.com/influxdata/influxdb/issues/13036
        // Assertions.assertThat(dashboard.getLinks().getLabels()).isEqualTo("/api/v2/dashboards/" + dashboard.getId() + "/labels");
        // Assertions.assertThat(dashboard.getLinks().getOrg()).isEqualTo("/api/v2/orgs/" + organization.getId());
    }
}