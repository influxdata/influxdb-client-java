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
package org.influxdata.client;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.influxdata.LogLevel;
import org.influxdata.client.domain.Dashboard;
import org.influxdata.client.domain.Label;
import org.influxdata.client.domain.OperationLog;
import org.influxdata.client.domain.OperationLogs;
import org.influxdata.client.domain.Organization;
import org.influxdata.client.domain.ResourceMember;
import org.influxdata.client.domain.ResourceOwner;
import org.influxdata.client.domain.User;
import org.influxdata.exceptions.NotFoundException;

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
    private UsersApi usersApi;

    @BeforeEach
    void setUp() {

        dashboardsApi = influxDBClient.getDashboardsApi();
        usersApi = influxDBClient.getUsersApi();
        organization = findMyOrg();

        dashboardsApi.findDashboards().forEach(dashboard -> dashboardsApi.deleteDashboard(dashboard));
        influxDBClient.setLogLevel(LogLevel.BODY);
    }

    @Test
    void createDashboard() {

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        Dashboard dashboard = dashboardsApi.createDashboard(generateName("dashboard"), "coolest dashboard", organization.getId());

        Assertions.assertThat(dashboard).isNotNull();
        Assertions.assertThat(dashboard.getId()).isNotEmpty();
        Assertions.assertThat(dashboard.getOrgID()).isEqualTo(organization.getId());
        Assertions.assertThat(dashboard.getCells()).hasSize(0);
        Assertions.assertThat(dashboard.getMeta()).isNotNull();
        Assertions.assertThat(dashboard.getMeta().getCreatedAt()).isAfter(now);
        Assertions.assertThat(dashboard.getMeta().getUpdatedAt()).isAfter(now);
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

    @Test
    void updateDashboard() {

        Dashboard created = dashboardsApi.createDashboard(generateName("dashboard"), "coolest dashboard", organization.getId());

        created
                .name(generateName("updated-"))
                .description("changed description");

        Dashboard updated = dashboardsApi.updateDashboard(created);

        Assertions.assertThat(updated.getName()).startsWith("updated");
        Assertions.assertThat(updated.getDescription()).isEqualTo("changed description");
        Assertions.assertThat(created.getMeta().getUpdatedAt()).isBefore(updated.getMeta().getUpdatedAt());
    }

    @Test
    void deleteDashboard() {

        Dashboard created = dashboardsApi.createDashboard(generateName("dashboard"), "coolest dashboard", organization.getId());

        Assertions.assertThat(created).isNotNull();

        Dashboard foundDashboard = dashboardsApi.findDashboardByID(created.getId());
        Assertions.assertThat(foundDashboard).isNotNull();

        // delete Dashboard
        dashboardsApi.deleteDashboard(created);

        Assertions.assertThatThrownBy(() -> dashboardsApi.findDashboardByID(created.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void findDashboardByID() {

        Dashboard dashboard = dashboardsApi.createDashboard(generateName("dashboard"), "coolest dashboard", organization.getId());

        Dashboard dashboardByID = dashboardsApi.findDashboardByID(dashboard.getId());

        Assertions.assertThat(dashboardByID).isNotNull();
        Assertions.assertThat(dashboardByID.getId()).isEqualTo(dashboard.getId());
        Assertions.assertThat(dashboardByID.getName()).isEqualTo(dashboard.getName());
    }

    @Test
    void findDashboardByIDNotFound() {

        Assertions.assertThatThrownBy(() -> dashboardsApi.findDashboardByID("020f755c3c082000"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("dashboard not found");
    }

    @Test
    void findDashboards() {

        int count = dashboardsApi.findDashboards().size();

        dashboardsApi.createDashboard(generateName("dashboard"), "coolest dashboard", organization.getId());

        List<Dashboard> dashboards = dashboardsApi.findDashboards();

        Assertions.assertThat(dashboards).hasSize(count + 1);
    }

    @Test
    void findDashboardByOrganization() {

        Organization organization = influxDBClient.getOrganizationsApi().createOrganization(generateName("org for dashboard"));

        List<Dashboard> dashboards = dashboardsApi.findDashboardsByOrganization(organization);

        Assertions.assertThat(dashboards).hasSize(0);

        dashboardsApi.createDashboard(generateName("dashboard"), "coolest dashboard", organization.getId());

        dashboards = dashboardsApi.findDashboardsByOrganization(organization);

        Assertions.assertThat(dashboards).hasSize(1);
    }

    @Test
    void member() {

        Dashboard dashboard = dashboardsApi.createDashboard(generateName("dashboard"), "coolest dashboard", organization.getId());

        List<ResourceMember> members = dashboardsApi.getMembers(dashboard);
        Assertions.assertThat(members).hasSize(0);

        User user = usersApi.createUser(generateName("Luke Health"));

        ResourceMember resourceMember = dashboardsApi.addMember(user, dashboard);
        Assertions.assertThat(resourceMember).isNotNull();
        Assertions.assertThat(resourceMember.getId()).isEqualTo(user.getId());
        Assertions.assertThat(resourceMember.getName()).isEqualTo(user.getName());
        Assertions.assertThat(resourceMember.getRole()).isEqualTo(ResourceMember.RoleEnum.MEMBER);

        members = dashboardsApi.getMembers(dashboard);
        Assertions.assertThat(members).hasSize(1);
        Assertions.assertThat(members.get(0).getRole()).isEqualTo(ResourceMember.RoleEnum.MEMBER);
        Assertions.assertThat(members.get(0).getId()).isEqualTo(user.getId());
        Assertions.assertThat(members.get(0).getName()).isEqualTo(user.getName());

        dashboardsApi.deleteMember(user, dashboard);

        members = dashboardsApi.getMembers(dashboard);
        Assertions.assertThat(members).hasSize(0);
    }

    @Test
    void owner() {

        Dashboard dashboard = dashboardsApi.createDashboard(generateName("dashboard"), "coolest dashboard", organization.getId());

        List<ResourceOwner> owners = dashboardsApi.getOwners(dashboard);
        Assertions.assertThat(owners).hasSize(1);
        Assertions.assertThat(owners.get(0).getName()).isEqualTo("my-user");

        User user = usersApi.createUser(generateName("Luke Health"));

        ResourceOwner resourceMember = dashboardsApi.addOwner(user, dashboard);
        Assertions.assertThat(resourceMember).isNotNull();
        Assertions.assertThat(resourceMember.getId()).isEqualTo(user.getId());
        Assertions.assertThat(resourceMember.getName()).isEqualTo(user.getName());
        Assertions.assertThat(resourceMember.getRole()).isEqualTo(ResourceOwner.RoleEnum.OWNER);

        owners = dashboardsApi.getOwners(dashboard);
        Assertions.assertThat(owners).hasSize(2);
        Assertions.assertThat(owners.get(1).getRole()).isEqualTo(ResourceOwner.RoleEnum.OWNER);
        Assertions.assertThat(owners.get(1).getId()).isEqualTo(user.getId());
        Assertions.assertThat(owners.get(1).getName()).isEqualTo(user.getName());

        dashboardsApi.deleteOwner(user, dashboard);

        owners = dashboardsApi.getOwners(dashboard);
        Assertions.assertThat(owners).hasSize(1);
    }

    @Test
    void labels() {

        LabelsApi labelsApi = influxDBClient.getLabelsApi();

        Dashboard dashboard = dashboardsApi.createDashboard(generateName("dashboard"), "coolest dashboard", organization.getId());

        Map<String, String> properties = new HashMap<>();
        properties.put("color", "green");
        properties.put("location", "west");

        Label label = labelsApi.createLabel(generateName("Cool Resource"), properties, organization.getId());

        List<Label> labels = dashboardsApi.getLabels(dashboard);
        Assertions.assertThat(labels).hasSize(0);

        Label addedLabel = dashboardsApi.addLabel(label, dashboard).getLabel();
        Assertions.assertThat(addedLabel).isNotNull();
        Assertions.assertThat(addedLabel.getId()).isEqualTo(label.getId());
        Assertions.assertThat(addedLabel.getName()).isEqualTo(label.getName());
        Assertions.assertThat(addedLabel.getProperties()).isEqualTo(label.getProperties());

        labels = dashboardsApi.getLabels(dashboard);
        Assertions.assertThat(labels).hasSize(1);
        Assertions.assertThat(labels.get(0).getId()).isEqualTo(label.getId());
        Assertions.assertThat(labels.get(0).getName()).isEqualTo(label.getName());

        dashboardsApi.deleteLabel(label, dashboard);

        labels = dashboardsApi.getLabels(dashboard);
        Assertions.assertThat(labels).hasSize(0);
    }

    @Test
    void labelAddNotExists() {

        Dashboard dashboard = dashboardsApi.createDashboard(generateName("dashboard"), "coolest dashboard", organization.getId());

        Assertions.assertThatThrownBy(() -> dashboardsApi.addLabel("020f755c3c082000", dashboard.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void labelDeleteNotExists() {

        Dashboard dashboard = dashboardsApi.createDashboard(generateName("dashboard"), "coolest dashboard", organization.getId());

        dashboardsApi.deleteLabel("020f755c3c082000", dashboard.getId());
    }

    @Test
    void findLogs() {

        Dashboard dashboard = dashboardsApi.createDashboard(generateName("dashboard"), "coolest dashboard", organization.getId());

        List<OperationLog> userLogs = dashboardsApi.findDashboardLogs(dashboard);
        Assertions.assertThat(userLogs).isNotEmpty();
        Assertions.assertThat(userLogs.get(0).getDescription()).isEqualTo("Dashboard Created");
    }

    @Test
    void findLogsNotFound() {
        List<OperationLog> userLogs = dashboardsApi.findDashboardLogs("020f755c3c082000");
        Assertions.assertThat(userLogs).isEmpty();
    }

    @Test
    void findLogsPaging() {

        Dashboard dashboard = dashboardsApi.createDashboard(generateName("dashboard"), "coolest dashboard", organization.getId());

        IntStream
                .range(0, 19)
                .forEach(value -> {

                    dashboard.setName(value + "_" + dashboard.getName());
                    dashboardsApi.updateDashboard(dashboard);
                });

        List<OperationLog> logs = dashboardsApi.findDashboardLogs(dashboard);

        Assertions.assertThat(logs).hasSize(20);
        Assertions.assertThat(logs.get(0).getDescription()).isEqualTo("Dashboard Created");
        Assertions.assertThat(logs.get(19).getDescription()).isEqualTo("Dashboard Updated");

        FindOptions findOptions = new FindOptions();
        findOptions.setLimit(5);
        findOptions.setOffset(0);

        OperationLogs entries = dashboardsApi.findDashboardLogs(dashboard, findOptions);

        Assertions.assertThat(entries.getLogs()).hasSize(5);
        Assertions.assertThat(entries.getLogs().get(0).getDescription()).isEqualTo("Dashboard Created");
        Assertions.assertThat(entries.getLogs().get(1).getDescription()).isEqualTo("Dashboard Updated");
        Assertions.assertThat(entries.getLogs().get(2).getDescription()).isEqualTo("Dashboard Updated");
        Assertions.assertThat(entries.getLogs().get(3).getDescription()).isEqualTo("Dashboard Updated");
        Assertions.assertThat(entries.getLogs().get(4).getDescription()).isEqualTo("Dashboard Updated");

        findOptions.setOffset(findOptions.getOffset() + 5);
        Assertions.assertThat(entries.getLinks().getNext()).isNull();

        entries = dashboardsApi.findDashboardLogs(dashboard, findOptions);
        Assertions.assertThat(entries.getLogs()).hasSize(5);
        Assertions.assertThat(entries.getLogs().get(0).getDescription()).isEqualTo("Dashboard Updated");
        Assertions.assertThat(entries.getLogs().get(1).getDescription()).isEqualTo("Dashboard Updated");
        Assertions.assertThat(entries.getLogs().get(2).getDescription()).isEqualTo("Dashboard Updated");
        Assertions.assertThat(entries.getLogs().get(3).getDescription()).isEqualTo("Dashboard Updated");
        Assertions.assertThat(entries.getLogs().get(4).getDescription()).isEqualTo("Dashboard Updated");

        findOptions.setOffset(findOptions.getOffset() + 5);
        Assertions.assertThat(entries.getLinks().getNext()).isNull();

        entries = dashboardsApi.findDashboardLogs(dashboard, findOptions);
        Assertions.assertThat(entries.getLogs().get(0).getDescription()).isEqualTo("Dashboard Updated");
        Assertions.assertThat(entries.getLogs().get(1).getDescription()).isEqualTo("Dashboard Updated");
        Assertions.assertThat(entries.getLogs().get(2).getDescription()).isEqualTo("Dashboard Updated");
        Assertions.assertThat(entries.getLogs().get(3).getDescription()).isEqualTo("Dashboard Updated");
        Assertions.assertThat(entries.getLogs().get(4).getDescription()).isEqualTo("Dashboard Updated");

        findOptions.setOffset(findOptions.getOffset() + 5);
        Assertions.assertThat(entries.getLinks().getNext()).isNull();

        entries = dashboardsApi.findDashboardLogs(dashboard, findOptions);
        Assertions.assertThat(entries.getLogs()).hasSize(5);
        Assertions.assertThat(entries.getLogs().get(0).getDescription()).isEqualTo("Dashboard Updated");
        Assertions.assertThat(entries.getLogs().get(1).getDescription()).isEqualTo("Dashboard Updated");
        Assertions.assertThat(entries.getLogs().get(2).getDescription()).isEqualTo("Dashboard Updated");
        Assertions.assertThat(entries.getLogs().get(3).getDescription()).isEqualTo("Dashboard Updated");
        Assertions.assertThat(entries.getLogs().get(4).getDescription()).isEqualTo("Dashboard Updated");

        findOptions.setOffset(findOptions.getOffset() + 5);
        Assertions.assertThat(entries.getLinks().getNext()).isNull();

        entries = dashboardsApi.findDashboardLogs(dashboard, findOptions);
        Assertions.assertThat(entries.getLogs()).hasSize(0);
        Assertions.assertThat(entries.getLinks().getNext()).isNull();

        // order
        findOptions = new FindOptions();
        findOptions.setDescending(false);

        entries = dashboardsApi.findDashboardLogs(dashboard, findOptions);

        Assertions.assertThat(entries.getLogs()).hasSize(20);
        Assertions.assertThat(entries.getLogs().get(19).getDescription()).isEqualTo("Dashboard Updated");
        Assertions.assertThat(entries.getLogs().get(0).getDescription()).isEqualTo("Dashboard Created");
    }
}