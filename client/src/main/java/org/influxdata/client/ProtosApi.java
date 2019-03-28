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

import org.influxdata.client.domain.Dashboard;
import org.influxdata.client.domain.Organization;
import org.influxdata.client.domain.Proto;

/**
 * The client of the InfluxDB 2.0 that implement Protos HTTP API endpoint.
 *
 * @author Jakub Bednar (28/03/2019 10:00)
 */
public interface ProtosApi {

    /**
     * List of available protos (templates of tasks/dashboards/etc).
     *
     * @return List of protos
     */
    @Nonnull
    List<Proto> getProtos();

    /**
     * Create instance of a proto dashboard.
     *
     * @param proto        proto to create
     * @param organization organization that the dashboard will be created as
     * @return List of dashboards that was created
     */
    @Nonnull
    List<Dashboard> createProtoDashboard(@Nonnull final Proto proto, @Nonnull final Organization organization);

    /**
     * Create instance of a proto dashboard.
     *
     * @param protoID ID of proto
     * @param orgID   organization id that the dashboard will be created as
     * @return List of dashboards that was created
     */
    @Nonnull
    List<Dashboard> createProtoDashboard(@Nonnull final String protoID, @Nonnull final String orgID);
}
