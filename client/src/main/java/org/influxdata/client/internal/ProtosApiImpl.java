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

import org.influxdata.Arguments;
import org.influxdata.client.ProtosApi;
import org.influxdata.client.domain.CreateProtoResourcesRequest;
import org.influxdata.client.domain.Dashboard;
import org.influxdata.client.domain.Dashboards;
import org.influxdata.client.domain.Organization;
import org.influxdata.client.domain.Proto;
import org.influxdata.client.domain.Protos;
import org.influxdata.client.service.ProtosService;
import org.influxdata.internal.AbstractRestClient;

import retrofit2.Call;

/**
 * @author Jakub Bednar (28/03/2019 10:01)
 */
final class ProtosApiImpl extends AbstractRestClient implements ProtosApi {

    private final ProtosService service;

    ProtosApiImpl(@Nonnull final ProtosService service) {

        Arguments.checkNotNull(service, "service");

        this.service = service;
    }

    @Nonnull
    @Override
    public List<Proto> getProtos() {

        Call<Protos> call = service.protosGet(null);

        return execute(call).getProtos();
    }

    @Nonnull
    @Override
    public List<Dashboard> createProtoDashboard(@Nonnull final Proto proto, @Nonnull final Organization organization) {

        Arguments.checkNotNull(proto, "proto");
        Arguments.checkNotNull(organization, "organization");

        return createProtoDashboard(proto.getId(), organization.getId());
    }

    @Nonnull
    @Override
    public List<Dashboard> createProtoDashboard(@Nonnull final String protoID,
                                                @Nonnull final String orgID) {

        Arguments.checkNonEmpty(protoID, "protoID");
        Arguments.checkNonEmpty(orgID, "organizationID");

        CreateProtoResourcesRequest request = new CreateProtoResourcesRequest().orgID(orgID);

        Call<Dashboards> call = service.protosProtoIDDashboardsPost(protoID, request, null);

        return execute(call).getDashboards();
    }
}
