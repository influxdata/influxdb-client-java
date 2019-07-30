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

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.influxdb.client.domain.Cell;
import com.influxdb.client.domain.CellUpdate;
import com.influxdb.client.domain.CreateCell;
import com.influxdb.client.domain.CreateDashboardRequest;
import com.influxdb.client.domain.Dashboard;
import com.influxdb.client.domain.Label;
import com.influxdb.client.domain.LabelResponse;
import com.influxdb.client.domain.OperationLog;
import com.influxdb.client.domain.OperationLogs;
import com.influxdb.client.domain.Organization;
import com.influxdb.client.domain.ResourceMember;
import com.influxdb.client.domain.ResourceOwner;
import com.influxdb.client.domain.User;
import com.influxdb.client.domain.View;

/**
 * The client of the InfluxDB 2.0 that implement Dashboards HTTP API endpoint.
 *
 * @author Jakub Bednar (bednar@github) (01/04/2019 10:47)
 */
public interface DashboardsApi {

    /**
     * Create a dashboard.
     *
     * @param name        user-facing name of the dashboard
     * @param description user-facing description of the dashboard
     * @param orgID       id of the organization that owns the dashboard
     * @return created Dashboard
     */
    Dashboard createDashboard(@Nonnull final String name,
                              @Nullable final String description,
                              @Nonnull final String orgID);

    /**
     * Create a dashboard.
     *
     * @param createDashboardRequest dashboard to create
     * @return created Dashboard
     */
    @Nonnull
    Dashboard createDashboard(@Nonnull final CreateDashboardRequest createDashboardRequest);

    /**
     * Update a single dashboard.
     *
     * @param dashboard patching of a dashboard
     * @return Updated dashboard
     */
    @Nonnull
    Dashboard updateDashboard(@Nonnull final Dashboard dashboard);

    /**
     * Delete a dashboard.
     *
     * @param dashboard dashboard to delete
     */
    void deleteDashboard(@Nonnull final Dashboard dashboard);

    /**
     * Delete a dashboard.
     *
     * @param dashboardID ID of dashboard to delete
     */
    void deleteDashboard(@Nonnull final String dashboardID);

    /**
     * Get a single Dashboard.
     *
     * @param dashboardID ID of dashboard to get
     * @return a single dashboard
     */
    @Nonnull
    Dashboard findDashboardByID(@Nonnull final String dashboardID);

    /**
     * Get all dashboards.
     *
     * @return a list of dashboard
     */
    @Nonnull
    List<Dashboard> findDashboards();

    /**
     * Get dashboards.
     *
     * @param organization filter dashboards to a specific organization
     * @return a list of dashboard
     */
    @Nonnull
    List<Dashboard> findDashboardsByOrganization(@Nonnull final Organization organization);

    /**
     * Get dashboards.
     *
     * @param orgName filter dashboards to a specific organization name
     * @return a list of dashboard
     */
    @Nonnull
    List<Dashboard> findDashboardsByOrgName(@Nullable final String orgName);

    /**
     * Retrieve operation logs for a dashboard.
     *
     * @param dashboard the dashboard
     * @return operation logs for the dashboard
     */
    @Nonnull
    List<OperationLog> findDashboardLogs(@Nonnull final Dashboard dashboard);

    /**
     * Retrieve operation logs for a dashboard.
     *
     * @param dashboard   the dashboard
     * @param findOptions the find options
     * @return operation logs for the dashboard
     */
    @Nonnull
    OperationLogs findDashboardLogs(@Nonnull final Dashboard dashboard, @Nonnull final FindOptions findOptions);

    /**
     * Retrieve operation logs for a dashboard.
     *
     * @param dashboardID ID of the dashboard
     * @return operation logs for the dashboard
     */
    @Nonnull
    List<OperationLog> findDashboardLogs(@Nonnull final String dashboardID);

    /**
     * Retrieve operation logs for a dashboard.
     *
     * @param dashboardID ID of the dashboard
     * @param findOptions the find options
     * @return operation logs for the dashboard
     */
    @Nonnull
    OperationLogs findDashboardLogs(@Nonnull final String dashboardID, @Nonnull final FindOptions findOptions);

    /**
     * List all dashboard members.
     *
     * @param dashboard the dashboard
     * @return a list of users who have member privileges for a dashboard
     */
    @Nonnull
    List<ResourceMember> getMembers(@Nonnull final Dashboard dashboard);

    /**
     * List all dashboard members.
     *
     * @param dashboardID ID of the dashboard
     * @return a list of users who have member privileges for a dashboard
     */
    @Nonnull
    List<ResourceMember> getMembers(@Nonnull final String dashboardID);

    /**
     * Add dashboard member.
     *
     * @param member    user to add as member
     * @param dashboard the dashboard
     * @return added to dashboard members
     */
    @Nonnull
    ResourceMember addMember(@Nonnull final User member, @Nonnull final Dashboard dashboard);

    /**
     * Add dashboard member.
     *
     * @param memberID    user to add as member
     * @param dashboardID ID of the dashboard
     * @return added to dashboard members
     */
    @Nonnull
    ResourceMember addMember(@Nonnull final String memberID, @Nonnull final String dashboardID);

    /**
     * Removes a member from an dashboard.
     *
     * @param member    member to remove
     * @param dashboard the dashboard
     */
    void deleteMember(@Nonnull final User member, @Nonnull final Dashboard dashboard);

    /**
     * Removes a member from an dashboard.
     *
     * @param memberID    ID of member to remove
     * @param dashboardID ID of the dashboard
     */
    void deleteMember(@Nonnull final String memberID, @Nonnull final String dashboardID);

    /**
     * List all dashboard owners.
     *
     * @param dashboard the dashboard
     * @return a list of users who have owner privileges for a dashboard
     */
    @Nonnull
    List<ResourceOwner> getOwners(@Nonnull final Dashboard dashboard);

    /**
     * List all dashboard owners.
     *
     * @param dashboardID ID of the dashboard
     * @return a list of users who have owner privileges for a dashboard
     */
    @Nonnull
    List<ResourceOwner> getOwners(@Nonnull final String dashboardID);

    /**
     * Add dashboard owner.
     *
     * @param owner     user to add as owner
     * @param dashboard the dashboard
     * @return added to dashboard owners
     */
    @Nonnull
    ResourceOwner addOwner(@Nonnull final User owner, @Nonnull final Dashboard dashboard);

    /**
     * Add dashboard owner.
     *
     * @param ownerID     user to add as owner
     * @param dashboardID ID of the dashboard
     * @return added to dashboard owners
     */
    @Nonnull
    ResourceOwner addOwner(@Nonnull final String ownerID, @Nonnull final String dashboardID);

    /**
     * Removes an owner from a dashboard.
     *
     * @param owner     owner to remove
     * @param dashboard the dashboard
     */
    void deleteOwner(@Nonnull final User owner, @Nonnull final Dashboard dashboard);

    /**
     * Removes an owner from a dashboard.
     *
     * @param ownerID     ID of owner to remove
     * @param dashboardID ID of the dashboard
     */
    void deleteOwner(@Nonnull final String ownerID, @Nonnull final String dashboardID);

    /**
     * List all labels for a dashboard.
     *
     * @param dashboard the dashboard
     * @return a list of all labels for a dashboard
     */
    @Nonnull
    List<Label> getLabels(@Nonnull final Dashboard dashboard);

    /**
     * List all labels for a dashboard.
     *
     * @param dashboardID ID of the dashboard
     * @return a list of all labels for a dashboard
     */
    @Nonnull
    List<Label> getLabels(@Nonnull final String dashboardID);

    /**
     * Add a label to a dashboard.
     *
     * @param label     label to add
     * @param dashboard the dashboard
     * @return the label added to the dashboard
     */
    @Nonnull
    LabelResponse addLabel(@Nonnull final Label label, @Nonnull final Dashboard dashboard);

    /**
     * Add a label to a dashboard.
     *
     * @param labelID     label ID to add
     * @param dashboardID ID of the dashboard
     * @return the label added to the dashboard
     */
    @Nonnull
    LabelResponse addLabel(@Nonnull final String labelID, @Nonnull final String dashboardID);

    /**
     * Delete a label from a dashboard.
     *
     * @param label     the label to delete
     * @param dashboard the dashboard
     */
    void deleteLabel(@Nonnull final Label label, @Nonnull final Dashboard dashboard);

    /**
     * Delete a label from a dashboard.
     *
     * @param labelID     the label id to delete
     * @param dashboardID ID of the dashboard
     */
    void deleteLabel(@Nonnull final String labelID, @Nonnull final String dashboardID);

    /**
     * Create a dashboard cell.
     *
     * @param createCell cell that will be added
     * @param dashboard  dashboard to update
     * @return Cell successfully added
     */
    @Nonnull
    Cell addCell(@Nonnull final CreateCell createCell, @Nonnull final Dashboard dashboard);

    /**
     * Create a dashboard cell.
     *
     * @param createCell  cell that will be added
     * @param dashboardID ID of dashboard to update
     * @return Cell successfully added
     */
    @Nonnull
    Cell addCell(@Nonnull final CreateCell createCell, @Nonnull final String dashboardID);

    /**
     * Update the non positional information related to a cell
     * (because updates to a single cells positional data could cause grid conflicts).
     *
     * @param cellUpdate  updates the non positional information related to a cell
     * @param cellID      ID of cell to update
     * @param dashboardID ID of dashboard to update
     * @return Updated dashboard cell
     */
    @Nonnull
    Cell updateCell(@Nonnull final CellUpdate cellUpdate,
                    @Nonnull final String cellID,
                    @Nonnull final String dashboardID);

    /**
     * Delete a dashboard cell.
     *
     * @param cell      cell to delete
     * @param dashboard dashboard to delete
     */
    void deleteCell(@Nonnull final Cell cell, @Nonnull final Dashboard dashboard);

    /**
     * Delete a dashboard cell.
     *
     * @param cellID      ID of cell to delete
     * @param dashboardID ID of dashboard to delete
     */
    void deleteCell(@Nonnull final String cellID, @Nonnull final String dashboardID);

    /**
     * Replace a dashboards cells.
     *
     * @param cells     batch replaces all of a dashboards cells (this is used primarily to update
     *                  the positional information of all of the cells)
     * @param dashboard dashboard to update
     * @return Replaced dashboard cells
     */
    @Nonnull
    Dashboard replaceCells(@Nonnull final List<Cell> cells, @Nonnull final Dashboard dashboard);

    /**
     * Replace a dashboards cells.
     *
     * @param cells       batch replaces all of a dashboards cells (this is used primarily to update
     *                    the positional information of all of the cells)
     * @param dashboardID ID of dashboard to update
     * @return Replaced dashboard cells
     */
    @Nonnull
    Dashboard replaceCells(@Nonnull final List<Cell> cells, @Nonnull final String dashboardID);

    /**
     * Add the view to a cell.
     *
     * @param view      the view for a cell
     * @param cell      cell to update
     * @param dashboard dashboard to update
     * @return Added cell view
     */
    @Nonnull
    View addCellView(@Nonnull final View view, @Nonnull final Cell cell, @Nonnull final Dashboard dashboard);

    /**
     * Add the view to a cell.
     *
     * @param view        the view for a cell
     * @param cellID      ID of cell to update
     * @param dashboardID ID of dashboard to update
     * @return Added cell view
     */
    @Nonnull
    View addCellView(@Nonnull final View view, @Nonnull final String cellID, @Nonnull final String dashboardID);

    /**
     * Update the view for a cell.
     *
     * @param view      updates the view for a cell
     * @param cell      cell to update
     * @param dashboard dashboard to update
     * @return Updated cell view
     */
    @Nonnull
    View updateCellView(@Nonnull final View view, @Nonnull final Cell cell, @Nonnull final Dashboard dashboard);

    /**
     * Update the view for a cell.
     *
     * @param view        updates the view for a cell
     * @param cellID      ID of cell to update
     * @param dashboardID ID of dashboard to update
     * @return Updated cell view
     */
    @Nonnull
    View updateCellView(@Nonnull final View view, @Nonnull final String cellID, @Nonnull final String dashboardID);

    /**
     * Retrieve the view for a cell in a dashboard.
     *
     * @param cell      cell
     * @param dashboard dashboard
     * @return A dashboard cells view
     */
    @Nonnull
    View getCellView(@Nonnull final Cell cell, @Nonnull final Dashboard dashboard);

    /**
     * Retrieve the view for a cell in a dashboard.
     *
     * @param cellID      ID of cell
     * @param dashboardID ID of dashboard
     * @return A dashboard cells view
     */
    @Nonnull
    View getCellView(@Nonnull final String cellID, @Nonnull final String dashboardID);
}