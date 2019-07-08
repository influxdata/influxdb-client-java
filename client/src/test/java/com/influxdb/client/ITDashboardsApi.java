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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import com.influxdb.client.domain.Cell;
import com.influxdb.client.domain.CellUpdate;
import com.influxdb.client.domain.CreateCell;
import com.influxdb.client.domain.Dashboard;
import com.influxdb.client.domain.Label;
import com.influxdb.client.domain.MarkdownViewProperties;
import com.influxdb.client.domain.OperationLog;
import com.influxdb.client.domain.OperationLogs;
import com.influxdb.client.domain.Organization;
import com.influxdb.client.domain.ResourceMember;
import com.influxdb.client.domain.ResourceOwner;
import com.influxdb.client.domain.User;
import com.influxdb.client.domain.View;
import com.influxdb.exceptions.NotFoundException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (01/04/2019 10:58)
 */
@RunWith(JUnitPlatform.class)
class ITDashboardsApi extends AbstractITClientTest {

    private DashboardsApi dashboardsApi;
    private Organization organization;
    private UsersApi usersApi;

    @BeforeEach
    void setUp() {

        dashboardsApi = influxDBClient.getDashboardsApi();
        usersApi = influxDBClient.getUsersApi();
        organization = findMyOrg();

        dashboardsApi.findDashboards().forEach(dashboard -> dashboardsApi.deleteDashboard(dashboard));
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
        Assertions.assertThat(dashboard.getLinks().getLabels()).isEqualTo("/api/v2/dashboards/" + dashboard.getId() + "/labels");
        Assertions.assertThat(dashboard.getLinks().getOrg()).isEqualTo("/api/v2/orgs/" + organization.getId());
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

    @Test
    void addCell() {

        Dashboard dashboard = dashboardsApi
                .createDashboard(generateName("dashboard"), "coolest dashboard", organization.getId());
        Assertions.assertThat(dashboard.getCells()).hasSize(0);

        CreateCell createCell = new CreateCell()
                .h(10)
                .w(15)
                .x(20)
                .y(25)
                .name("my-view");

        Cell cell = dashboardsApi.addCell(createCell, dashboard);

        Assertions.assertThat(cell.getId()).isNotNull();
        Assertions.assertThat(cell.getX()).isEqualTo(20);
        Assertions.assertThat(cell.getY()).isEqualTo(25);
        Assertions.assertThat(cell.getW()).isEqualTo(15);
        Assertions.assertThat(cell.getH()).isEqualTo(10);
        Assertions.assertThat(cell.getLinks()).isNotNull();
        Assertions.assertThat(cell.getLinks().getSelf()).isEqualTo("/api/v2/dashboards/" + dashboard.getId() + "/cells/" + cell.getId());
        Assertions.assertThat(cell.getLinks().getView()).isEqualTo("/api/v2/dashboards/" + dashboard.getId() + "/cells/" + cell.getId() + "/view");
        Assertions.assertThat(cell.getViewID()).isNull();

        List<Cell> cells = dashboardsApi.findDashboardByID(dashboard.getId()).getCells();
        Assertions.assertThat(cells).hasSize(1);
    }

    @Test
    void updateCell() {

        Dashboard dashboard = dashboardsApi
                .createDashboard(generateName("dashboard"), "coolest dashboard", organization.getId());
        Assertions.assertThat(dashboard.getCells()).hasSize(0);

        CreateCell createCell = new CreateCell()
                .h(10)
                .w(15)
                .x(20)
                .y(25)
                .name("my-cell");

        Cell cell = dashboardsApi.addCell(createCell, dashboard);

        Cell changedCell = dashboardsApi.updateCell(new CellUpdate().x(1).y(2).h(3).w(4), cell.getId(), dashboard.getId());

        Assertions.assertThat(changedCell.getX()).isEqualTo(1);
        Assertions.assertThat(changedCell.getY()).isEqualTo(2);
        Assertions.assertThat(changedCell.getH()).isEqualTo(3);
        Assertions.assertThat(changedCell.getW()).isEqualTo(4);
    }

    @Test
    void addCellDashboardNotFound() {

        CreateCell createCell = new CreateCell()
                .h(10)
                .w(15)
                .x(20)
                .y(25)
                .name("my-cell");

        Assertions.assertThatThrownBy(() -> dashboardsApi.addCell(createCell, "020f755c3c082000"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("dashboard not found");
    }

    @Test
    void addCellCopyView() {

        Dashboard dashboard = dashboardsApi
                .createDashboard(generateName("dashboard"), "coolest dashboard", organization.getId());

        Cell cell = dashboardsApi.addCell(new CreateCell()
                .h(10)
                .w(15)
                .x(20)
                .y(25)
                .name("my-cell"), dashboard);

        View view = new View()
                .name("view-name")
                .properties(new MarkdownViewProperties().note("my-note"));

        view = dashboardsApi.addCellView(view, cell, dashboard);

        CreateCell newCell = new CreateCell()
                .h(10)
                .w(15)
                .x(20)
                .y(25)
                .name("my-cell-2")
                .usingView(view.getId());

        Cell cellWithView = dashboardsApi.addCell(newCell, dashboard);

        //TODO https://github.com/influxdata/influxdb/issues/13084
//        View cellView = dashboardsApi.getCellView(cellWithView, dashboard);
//        Assertions.assertThat(cellView).isNotNull();
//        Assertions.assertThat(cellView.getId()).isEqualTo(view.getId());
//        Assertions.assertThat(((MarkdownViewProperties) cellView.getProperties()).getType()).isEqualTo(MarkdownViewProperties.TypeEnum.MARKDOWN);
//        Assertions.assertThat(((MarkdownViewProperties) cellView.getProperties()).getNote()).isEqualTo("my-note");
    }

    @Test
    void addCellDirectToView() {

        Dashboard dashboard = dashboardsApi
                .createDashboard(generateName("dashboard"), "coolest dashboard", organization.getId());

        Cell cell = dashboardsApi.addCell(new CreateCell()
                .h(10)
                .w(15)
                .x(20)
                .y(25)
                .name("my-cell"), dashboard);

        //TODO https://github.com/influxdata/influxdb/issues/13084
        // View emptyProperties = dashboardsApi.getCellView(cell, dashboard);
        // Assertions.assertThat(((EmptyViewProperties)emptyProperties.getProperties()).getType()).isEqualTo(EmptyViewProperties.TypeEnum.EMPTY);

        View view = new View()
                .name("view-name")
                .properties(new MarkdownViewProperties().note("my-note"));

        view = dashboardsApi.addCellView(view, cell, dashboard);

        CreateCell newCell = new CreateCell()
                .h(10)
                .w(15)
                .x(20)
                .y(25)
                .name("my-cell-2");

        Cell cellWithView = dashboardsApi.addCell(newCell, dashboard);

        //TODO https://github.com/influxdata/influxdb/issues/13084
        // View cellView = dashboardsApi.getCellView(cellWithView, dashboard);
        // Assertions.assertThat(cellView).isNotNull();
        // Assertions.assertThat(((MarkdownViewProperties)cellView.getProperties()).getType()).isEqualTo(MarkdownViewProperties.TypeEnum.MARKDOWN);
        // Assertions.assertThat(((MarkdownViewProperties)cellView.getProperties()).getNote()).isEqualTo("my-note");
    }

    @Test
    void deleteCell() {

        Dashboard dashboard = dashboardsApi
                .createDashboard(generateName("dashboard"), "coolest dashboard", organization.getId());

        CreateCell createCell = new CreateCell()
                .h(10)
                .w(15)
                .x(20)
                .y(25)
                .name("my-cell");

        Cell cell = dashboardsApi.addCell(createCell, dashboard);

        dashboard = dashboardsApi.findDashboardByID(dashboard.getId());
        Assertions.assertThat(dashboard.getCells()).hasSize(1);

        dashboardsApi.deleteCell(cell, dashboard);

        dashboard = dashboardsApi.findDashboardByID(dashboard.getId());
        Assertions.assertThat(dashboard.getCells()).hasSize(0);
    }

    @Test
    void deleteCellNotFound() {

        Dashboard dashboard = dashboardsApi
                .createDashboard(generateName("dashboard"), "coolest dashboard", organization.getId());

        Assertions.assertThatThrownBy(() -> dashboardsApi.deleteCell("020f755c3c082000", dashboard.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("cell not found");
    }

    @Test
    void deleteCellDashboardNotFound() {

        Dashboard dashboard = dashboardsApi
                .createDashboard(generateName("dashboard"), "coolest dashboard", organization.getId());

        CreateCell createCell = new CreateCell()
                .h(10)
                .w(15)
                .x(20)
                .y(25)
                .name("my-cell");

        Cell cell = dashboardsApi.addCell(createCell, dashboard);

        Assertions.assertThatThrownBy(() -> dashboardsApi.deleteCell(cell.getId(), "020f755c3c082000"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("dashboard not found");
    }

    @Test
    void replaceCells() {

        Dashboard dashboard = dashboardsApi
                .createDashboard(generateName("dashboard"), "coolest dashboard", organization.getId());

        CreateCell createCell = new CreateCell()
                .h(1)
                .w(2)
                .x(3)
                .y(4)
                .name("my-cell-1");

        Cell cell1 = dashboardsApi.addCell(createCell, dashboard);
        Cell cell2 = dashboardsApi.addCell(createCell.h(5)
                .w(6)
                .x(7)
                .y(8).name("my-cell-2"), dashboard);

        Dashboard dashboardWithCells = dashboardsApi.replaceCells(Arrays.asList(cell1, cell2), dashboard);

        Assertions.assertThat(dashboardWithCells.getCells()).hasSize(2);
        Assertions.assertThat(dashboardWithCells.getCells().get(0).getH()).isEqualTo(1);
        Assertions.assertThat(dashboardWithCells.getCells().get(0).getW()).isEqualTo(2);
        Assertions.assertThat(dashboardWithCells.getCells().get(0).getX()).isEqualTo(3);
        Assertions.assertThat(dashboardWithCells.getCells().get(0).getY()).isEqualTo(4);
        Assertions.assertThat(dashboardWithCells.getCells().get(1).getH()).isEqualTo(5);
        Assertions.assertThat(dashboardWithCells.getCells().get(1).getW()).isEqualTo(6);
        Assertions.assertThat(dashboardWithCells.getCells().get(1).getX()).isEqualTo(7);
        Assertions.assertThat(dashboardWithCells.getCells().get(1).getY()).isEqualTo(8);
    }

    @Test
    void cellView() {

        Dashboard dashboard = dashboardsApi
                .createDashboard(generateName("dashboard"), "coolest dashboard", organization.getId());

        CreateCell createCell = new CreateCell()
                .h(10)
                .w(15)
                .x(20)
                .y(25)
                .name("my-cell");

        Cell cell = dashboardsApi.addCell(createCell, dashboard);
        Assertions.assertThat(cell.getViewID()).isNull();

        View view = new View()
                .name("view-name")
                .properties(new MarkdownViewProperties().note("my-note"));

        View created = dashboardsApi.addCellView(view, cell, dashboard);
        //TODO https://github.com/influxdata/influxdb/issues/13395
        // Assertions.assertThat(created.getId()).isNotNull();
        Assertions.assertThat(created.getName()).isEqualTo("view-name");
        Assertions.assertThat(created.getProperties()).isNotNull();
        Assertions.assertThat(((MarkdownViewProperties) created.getProperties()).getNote()).isEqualTo("my-note");
        Assertions.assertThat(((MarkdownViewProperties) created.getProperties()).getType()).isEqualTo(MarkdownViewProperties.TypeEnum.MARKDOWN);
        Assertions.assertThat(((MarkdownViewProperties) created.getProperties()).getShape()).isEqualTo(MarkdownViewProperties.ShapeEnum.CHRONOGRAF_V2);

        cell = dashboardsApi.findDashboardByID(dashboard.getId()).getCells().get(0);

        //TODO https://github.com/influxdata/influxdb/issues/13080
        // Assertions.assertThat(cell.getViewID()).isNotNull();

        View updated = dashboardsApi.updateCellView(view.name("updated-name"), cell, dashboard);
        //TODO https://github.com/influxdata/influxdb/issues/13395
        // Assertions.assertThat(updated.getId()).isNotNull();
        Assertions.assertThat(updated.getName()).isEqualTo("updated-name");

        View viewByGet = dashboardsApi.getCellView(cell, dashboard);

        Assertions.assertThat(viewByGet.getId()).isEqualTo(created.getId());
    }

    @Test
    void addCellViewNotFound() {

        Dashboard dashboard = dashboardsApi
                .createDashboard(generateName("dashboard"), "coolest dashboard", organization.getId());

        CreateCell createCell = new CreateCell()
                .h(10)
                .w(15)
                .x(20)
                .y(25)
                .name("my-cell");

        Cell cell = dashboardsApi.addCell(createCell, dashboard);

        View view = new View()
                .name("view-name")
                .properties(new MarkdownViewProperties().note("my-note"));

        Assertions.assertThatThrownBy(() -> dashboardsApi.addCellView(view, cell.getId(), "020f755c3c082000"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("dashboard not found");

        //TODO https://github.com/influxdata/influxdb/issues/13083
        // Assertions.assertThatThrownBy(() -> dashboardsApi.addCellView(view, "020f755c3c082000", dashboard.getId()))
        //        .isInstanceOf(NotFoundException.class)
        //        .hasMessage("cell not found");
    }

    @Test
    void getCellViewNotFound() {

        Dashboard dashboard = dashboardsApi
                .createDashboard(generateName("dashboard"), "coolest dashboard", organization.getId());

        CreateCell createCell = new CreateCell()
                .h(10)
                .w(15)
                .x(20)
                .y(25)
                .name("my-cell");

        Cell cell = dashboardsApi.addCell(createCell, dashboard);

        Assertions.assertThatThrownBy(() -> dashboardsApi.getCellView(cell.getId(), "020f755c3c082000"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("dashboard not found");

        //TODO https://github.com/influxdata/influxdb/issues/13083
        // Assertions.assertThatThrownBy(() -> dashboardsApi.getCellView("ffffffffffffffff", dashboard.getId()))
        //        .isInstanceOf(NotFoundException.class)
        //        .hasMessage("cell not found");
    }
}