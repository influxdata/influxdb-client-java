package org.influxdata.client;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.influxdata.client.domain.CreateDashboardRequest;
import org.influxdata.client.domain.Dashboard;

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
}