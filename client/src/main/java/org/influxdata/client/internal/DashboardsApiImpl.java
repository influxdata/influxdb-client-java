package org.influxdata.client.internal;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.influxdata.Arguments;
import org.influxdata.client.DashboardsApi;
import org.influxdata.client.domain.CreateDashboardRequest;
import org.influxdata.client.domain.Dashboard;
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
}