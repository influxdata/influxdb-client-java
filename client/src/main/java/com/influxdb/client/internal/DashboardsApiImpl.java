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
package com.influxdb.client.internal;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.influxdb.Arguments;
import com.influxdb.client.DashboardsApi;
import com.influxdb.client.FindOptions;
import com.influxdb.client.domain.AddResourceMemberRequestBody;
import com.influxdb.client.domain.Cell;
import com.influxdb.client.domain.CellUpdate;
import com.influxdb.client.domain.CreateCell;
import com.influxdb.client.domain.CreateDashboardRequest;
import com.influxdb.client.domain.Dashboard;
import com.influxdb.client.domain.Dashboards;
import com.influxdb.client.domain.Label;
import com.influxdb.client.domain.LabelMapping;
import com.influxdb.client.domain.LabelResponse;
import com.influxdb.client.domain.LabelsResponse;
import com.influxdb.client.domain.OperationLog;
import com.influxdb.client.domain.OperationLogs;
import com.influxdb.client.domain.Organization;
import com.influxdb.client.domain.ResourceMember;
import com.influxdb.client.domain.ResourceMembers;
import com.influxdb.client.domain.ResourceOwner;
import com.influxdb.client.domain.ResourceOwners;
import com.influxdb.client.domain.User;
import com.influxdb.client.domain.View;
import com.influxdb.client.service.DashboardsService;
import com.influxdb.internal.AbstractRestClient;

import retrofit2.Call;

/**
 * @author Jakub Bednar (bednar@github) (01/04/2019 10:51)
 */
final class DashboardsApiImpl extends AbstractRestClient implements DashboardsApi {

    private final DashboardsService service;

    DashboardsApiImpl(@Nonnull final DashboardsService service) {

        Arguments.checkNotNull(service, "service");

        this.service = service;
    }

    @Override
    public Dashboard createDashboard(@Nonnull final String name,
                                     @Nullable final String description,
                                     @Nonnull final String orgID) {

        Arguments.checkNonEmpty(name, "name");
        Arguments.checkNonEmpty(description, "description");
        Arguments.checkNonEmpty(orgID, "orgID");

        return createDashboard(new CreateDashboardRequest().name(name).description(description).orgID(orgID));
    }

    @Nonnull
    @Override
    public Dashboard createDashboard(@Nonnull final CreateDashboardRequest createDashboardRequest) {

        Arguments.checkNotNull(createDashboardRequest, "createDashboardRequest");

        Call<Dashboard> call = service.postDashboards(createDashboardRequest, null);

        return execute(call);
    }

    @Nonnull
    @Override
    public Dashboard updateDashboard(@Nonnull final Dashboard dashboard) {

        Arguments.checkNotNull(dashboard, "Dashboard");

        Call<Dashboard> call = service.patchDashboardsID(dashboard.getId(), dashboard, null);

        return execute(call);
    }

    @Override
    public void deleteDashboard(@Nonnull final Dashboard dashboard) {

        Arguments.checkNotNull(dashboard, "dashboard");

        deleteDashboard(dashboard.getId());
    }

    @Override
    public void deleteDashboard(@Nonnull final String dashboardID) {

        Arguments.checkNotNull(dashboardID, "dashboardID");

        Call<Void> call = service.deleteDashboardsID(dashboardID, null);

        execute(call);
    }

    @Nonnull
    @Override
    public Dashboard findDashboardByID(@Nonnull final String dashboardID) {

        Arguments.checkNotNull(dashboardID, "dashboardID");

        Call<Dashboard> call = service.getDashboardsID(dashboardID, null);

        return execute(call);
    }

    @Nonnull
    @Override
    public List<Dashboard> findDashboards() {
        return findDashboardsByOrgName(null);
    }

    @Nonnull
    @Override
    public List<Dashboard> findDashboardsByOrganization(@Nonnull final Organization organization) {

        return findDashboardsByOrgName(organization.getName());
    }

    @Nonnull
    @Override
    public List<Dashboard> findDashboardsByOrgName(@Nullable final String orgName) {

        Call<Dashboards> call = service
                .getDashboards(null, null, null, null, null, orgName);

        return execute(call).getDashboards();
    }

    @Nonnull
    @Override
    public List<OperationLog> findDashboardLogs(@Nonnull final Dashboard dashboard) {

        Arguments.checkNotNull(dashboard, "dashboard");

        return findDashboardLogs(dashboard.getId());
    }

    @Nonnull
    @Override
    public OperationLogs findDashboardLogs(@Nonnull final Dashboard dashboard,
                                           @Nonnull final FindOptions findOptions) {

        Arguments.checkNotNull(dashboard, "dashboard");
        Arguments.checkNotNull(findOptions, "findOptions");

        return findDashboardLogs(dashboard.getId(), findOptions);
    }

    @Nonnull
    @Override
    public List<OperationLog> findDashboardLogs(@Nonnull final String dashboardID) {

        Arguments.checkNotNull(dashboardID, "dashboardID");

        return findDashboardLogs(dashboardID, new FindOptions()).getLogs();
    }

    @Nonnull
    @Override
    public OperationLogs findDashboardLogs(@Nonnull final String dashboardID, @Nonnull final FindOptions findOptions) {

        Arguments.checkNotNull(dashboardID, "dashboardID");
        Arguments.checkNotNull(findOptions, "findOptions");

        Call<OperationLogs> call = service
                .getDashboardsIDLogs(dashboardID, null, findOptions.getOffset(), findOptions.getLimit());

        return execute(call);
    }

    @Nonnull
    @Override
    public List<ResourceMember> getMembers(@Nonnull final Dashboard dashboard) {

        Arguments.checkNotNull(dashboard, "dashboard");

        return getMembers(dashboard.getId());
    }

    @Nonnull
    @Override
    public List<ResourceMember> getMembers(@Nonnull final String dashboardID) {

        Arguments.checkNotNull(dashboardID, "dashboardID");

        Call<ResourceMembers> call = service.getDashboardsIDMembers(dashboardID, null);

        return execute(call).getUsers();
    }

    @Nonnull
    @Override
    public ResourceMember addMember(@Nonnull final User member, @Nonnull final Dashboard dashboard) {

        Arguments.checkNotNull(member, "member");
        Arguments.checkNotNull(dashboard, "dashboard");

        return addMember(member.getId(), dashboard.getId());
    }

    @Nonnull
    @Override
    public ResourceMember addMember(@Nonnull final String memberID, @Nonnull final String dashboardID) {

        Arguments.checkNotNull(memberID, "memberID");
        Arguments.checkNotNull(dashboardID, "dashboardID");

        AddResourceMemberRequestBody user = new AddResourceMemberRequestBody().id(memberID);

        Call<ResourceMember> call = service.postDashboardsIDMembers(dashboardID, user, null);

        return execute(call);
    }

    @Override
    public void deleteMember(@Nonnull final User member, @Nonnull final Dashboard dashboard) {

        Arguments.checkNotNull(member, "member");
        Arguments.checkNotNull(dashboard, "dashboard");

        deleteMember(member.getId(), dashboard.getId());
    }

    @Override
    public void deleteMember(@Nonnull final String memberID, @Nonnull final String dashboardID) {

        Arguments.checkNotNull(memberID, "memberID");
        Arguments.checkNotNull(dashboardID, "dashboardID");

        Call<Void> call = service.deleteDashboardsIDMembersID(memberID, dashboardID, null);

        execute(call);
    }

    @Nonnull
    @Override
    public List<ResourceOwner> getOwners(@Nonnull final Dashboard dashboard) {

        Arguments.checkNotNull(dashboard, "dashboard");

        return getOwners(dashboard.getId());
    }

    @Nonnull
    @Override
    public List<ResourceOwner> getOwners(@Nonnull final String dashboardID) {

        Arguments.checkNotNull(dashboardID, "dashboardID");

        Call<ResourceOwners> call = service.getDashboardsIDOwners(dashboardID, null);

        return execute(call).getUsers();
    }

    @Nonnull
    @Override
    public ResourceOwner addOwner(@Nonnull final User owner, @Nonnull final Dashboard dashboard) {

        Arguments.checkNotNull(owner, "owner");
        Arguments.checkNotNull(dashboard, "dashboard");

        return addOwner(owner.getId(), dashboard.getId());
    }

    @Nonnull
    @Override
    public ResourceOwner addOwner(@Nonnull final String ownerID, @Nonnull final String dashboardID) {

        Arguments.checkNotNull(ownerID, "ownerID");
        Arguments.checkNotNull(dashboardID, "dashboardID");

        AddResourceMemberRequestBody user = new AddResourceMemberRequestBody().id(ownerID);

        Call<ResourceOwner> call = service.postDashboardsIDOwners(dashboardID, user, null);

        return execute(call);
    }

    @Override
    public void deleteOwner(@Nonnull final User owner, @Nonnull final Dashboard dashboard) {

        Arguments.checkNotNull(owner, "owner");
        Arguments.checkNotNull(dashboard, "dashboard");

        deleteOwner(owner.getId(), dashboard.getId());
    }

    @Override
    public void deleteOwner(@Nonnull final String ownerID, @Nonnull final String dashboardID) {

        Arguments.checkNotNull(ownerID, "ownerID");
        Arguments.checkNotNull(dashboardID, "dashboardID");

        Call<Void> call = service.deleteDashboardsIDOwnersID(ownerID, dashboardID, null);

        execute(call);
    }

    @Nonnull
    @Override
    public List<Label> getLabels(@Nonnull final Dashboard dashboard) {

        Arguments.checkNotNull(dashboard, "dashboard");

        return getLabels(dashboard.getId());
    }

    @Nonnull
    @Override
    public List<Label> getLabels(@Nonnull final String dashboardID) {

        Arguments.checkNotNull(dashboardID, "dashboardID");

        Call<LabelsResponse> call = service.getDashboardsIDLabels(dashboardID, null);

        return execute(call).getLabels();
    }

    @Nonnull
    @Override
    public LabelResponse addLabel(@Nonnull final Label label, @Nonnull final Dashboard dashboard) {

        Arguments.checkNotNull(label, "label");
        Arguments.checkNotNull(dashboard, "dashboard");

        return addLabel(label.getId(), dashboard.getId());
    }

    @Nonnull
    @Override
    public LabelResponse addLabel(@Nonnull final String labelID, @Nonnull final String dashboardID) {

        Arguments.checkNonEmpty(labelID, "labelID");
        Arguments.checkNonEmpty(dashboardID, "dashboardID");

        LabelMapping labelMapping = new LabelMapping().labelID(labelID);

        Call<LabelResponse> call = service.postDashboardsIDLabels(dashboardID, labelMapping, null);

        return execute(call);
    }

    @Override
    public void deleteLabel(@Nonnull final Label label, @Nonnull final Dashboard dashboard) {

        Arguments.checkNotNull(label, "label");
        Arguments.checkNotNull(dashboard, "dashboard");

        deleteLabel(label.getId(), dashboard.getId());
    }

    @Override
    public void deleteLabel(@Nonnull final String labelID, @Nonnull final String dashboardID) {

        Arguments.checkNonEmpty(labelID, "labelID");
        Arguments.checkNonEmpty(dashboardID, "dashboardID");

        Call<Void> call = service.deleteDashboardsIDLabelsID(dashboardID, labelID, null);

        execute(call);
    }

    @Nonnull
    @Override
    public Cell addCell(@Nonnull final CreateCell createCell, @Nonnull final Dashboard dashboard) {

        Arguments.checkNotNull(createCell, "createCell");
        Arguments.checkNotNull(dashboard, "dashboard");

        return addCell(createCell, dashboard.getId());
    }

    @Nonnull
    @Override
    public Cell addCell(@Nonnull final CreateCell createCell, @Nonnull final String dashboardID) {

        Arguments.checkNotNull(createCell, "createCell");
        Arguments.checkNonEmpty(dashboardID, "dashboardID");

        Call<Cell> call = service.postDashboardsIDCells(dashboardID, createCell, null);

        return execute(call);
    }

    @Nonnull
    @Override
    public Dashboard replaceCells(@Nonnull final List<Cell> cells, @Nonnull final Dashboard dashboard) {

        Arguments.checkNotNull(cells, "cells");
        Arguments.checkNotNull(dashboard, "dashboard");

        return replaceCells(cells, dashboard.getId());
    }

    @Nonnull
    @Override
    public Dashboard replaceCells(@Nonnull final List<Cell> cells, @Nonnull final String dashboardID) {

        Arguments.checkNotNull(cells, "cells");
        Arguments.checkNonEmpty(dashboardID, "dashboardID");

        Call<Dashboard> call = service.putDashboardsIDCells(dashboardID, cells, null);

        return execute(call);
    }

    @Nonnull
    @Override
    public Cell updateCell(@Nonnull final CellUpdate cellUpdate,
                           @Nonnull final String cellID,
                           @Nonnull final String dashboardID) {

        Arguments.checkNotNull(cellUpdate, "cellUpdate");
        Arguments.checkNonEmpty(cellID, "cellID");
        Arguments.checkNonEmpty(dashboardID, "dashboardID");

        Call<Cell> call = service.patchDashboardsIDCellsID(dashboardID, cellID, cellUpdate, null);

        return execute(call);
    }

    @Override
    public void deleteCell(@Nonnull final Cell cell, @Nonnull final Dashboard dashboard) {

        Arguments.checkNotNull(cell, "cell");
        Arguments.checkNotNull(dashboard, "dashboard");

        deleteCell(cell.getId(), dashboard.getId());
    }

    @Override
    public void deleteCell(@Nonnull final String cellID, @Nonnull final String dashboardID) {

        Arguments.checkNonEmpty(cellID, "cellID");
        Arguments.checkNonEmpty(dashboardID, "dashboardID");

        Call<Void> call = service.deleteDashboardsIDCellsID(dashboardID, cellID, null);

        execute(call);
    }

    @Nonnull
    @Override
    public View addCellView(@Nonnull final View view, @Nonnull final Cell cell, @Nonnull final Dashboard dashboard) {

        Arguments.checkNotNull(view, "view");
        Arguments.checkNotNull(cell, "cell");
        Arguments.checkNotNull(dashboard, "dashboard");

        return addCellView(view, cell.getId(), dashboard.getId());
    }

    @Nonnull
    @Override
    public View addCellView(@Nonnull final View view, @Nonnull final String cellID, final @Nonnull String dashboardID) {

        Arguments.checkNotNull(view, "view");
        Arguments.checkNonEmpty(cellID, "cellID");
        Arguments.checkNonEmpty(dashboardID, "dashboardID");

        return updateCellView(view, cellID, dashboardID);
    }

    @Nonnull
    @Override
    public View updateCellView(@Nonnull final View view, @Nonnull final Cell cell, @Nonnull final Dashboard dashboard) {

        Arguments.checkNotNull(view, "view");
        Arguments.checkNotNull(cell, "cell");
        Arguments.checkNotNull(dashboard, "dashboard");

        return updateCellView(view, cell.getId(), dashboard.getId());
    }

    @Nonnull
    @Override
    public View updateCellView(@Nonnull final View view,
                               @Nonnull final String cellID,
                               @Nonnull final String dashboardID) {

        Arguments.checkNotNull(view, "view");
        Arguments.checkNonEmpty(cellID, "cellID");
        Arguments.checkNonEmpty(dashboardID, "dashboardID");

        Call<View> call = service.patchDashboardsIDCellsIDView(dashboardID, cellID, view, null);

        return execute(call);
    }

    @Nonnull
    @Override
    public View getCellView(@Nonnull final Cell cell, @Nonnull final Dashboard dashboard) {

        Arguments.checkNotNull(cell, "cell");
        Arguments.checkNotNull(dashboard, "dashboard");

        return getCellView(cell.getId(), dashboard.getId());
    }

    @Nonnull
    @Override
    public View getCellView(@Nonnull final String cellID, @Nonnull final String dashboardID) {

        Arguments.checkNonEmpty(cellID, "cellID");
        Arguments.checkNonEmpty(dashboardID, "dashboardID");

        Call<View> call = service.getDashboardsIDCellsIDView(dashboardID, cellID, null);

        return execute(call);
    }
}