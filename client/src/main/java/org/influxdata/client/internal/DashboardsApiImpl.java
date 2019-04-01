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
package org.influxdata.client.internal;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.influxdata.Arguments;
import org.influxdata.client.DashboardsApi;
import org.influxdata.client.domain.CreateDashboardRequest;
import org.influxdata.client.domain.Dashboard;
import org.influxdata.client.domain.Dashboards;
import org.influxdata.client.domain.Organization;
import org.influxdata.client.service.DashboardsService;
import org.influxdata.internal.AbstractRestClient;

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

        Call<Dashboard> call = service.dashboardsPost(createDashboardRequest, null);

        return execute(call);
    }

    @Nonnull
    @Override
    public Dashboard updateDashboard(@Nonnull final Dashboard dashboard) {

        Arguments.checkNotNull(dashboard, "Dashboard");

        Call<Dashboard> call = service.dashboardsDashboardIDPatch(dashboard.getId(), dashboard, null);

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

        Call<Void> call = service.dashboardsDashboardIDDelete(dashboardID, null);

        execute(call);
    }

    @Nonnull
    @Override
    public Dashboard findDashboardByID(@Nonnull final String dashboardID) {

        Arguments.checkNotNull(dashboardID, "dashboardID");

        Call<Dashboard> call = service.dashboardsDashboardIDGet(dashboardID, null);

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
                .dashboardsGet(null, null, null, null, null, orgName);

        return execute(call).getDashboards();
    }
}