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