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

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.influxdata.client.domain.CreateDashboardRequest;
import org.influxdata.client.domain.Dashboard;
import org.influxdata.client.domain.Organization;
import org.influxdata.client.domain.ResourceMember;
import org.influxdata.client.domain.ResourceOwner;
import org.influxdata.client.domain.User;

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

}