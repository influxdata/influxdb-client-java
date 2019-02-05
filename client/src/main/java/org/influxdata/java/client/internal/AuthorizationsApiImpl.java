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
package org.influxdata.java.client.internal;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.influxdata.client.Arguments;
import org.influxdata.client.exceptions.NotFoundException;
import org.influxdata.client.internal.AbstractRestClient;
import org.influxdata.java.client.AuthorizationsApi;
import org.influxdata.java.client.domain.Authorization;
import org.influxdata.java.client.domain.Authorizations;
import org.influxdata.java.client.domain.Organization;
import org.influxdata.java.client.domain.Permission;
import org.influxdata.java.client.domain.Status;
import org.influxdata.java.client.domain.User;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import retrofit2.Call;

/**
 * @author Jakub Bednar (bednar@github) (17/09/2018 12:00)
 */
final class AuthorizationsApiImpl extends AbstractRestClient implements AuthorizationsApi {

    private static final Logger LOG = Logger.getLogger(AuthorizationsApiImpl.class.getName());

    private final InfluxDBService influxDBService;
    private final JsonAdapter<Authorization> adapter;

    AuthorizationsApiImpl(@Nonnull final InfluxDBService influxDBService, @Nonnull final Moshi moshi) {

        Arguments.checkNotNull(influxDBService, "InfluxDBService");
        Arguments.checkNotNull(moshi, "Moshi to create adapter");

        this.influxDBService = influxDBService;
        this.adapter = moshi.adapter(Authorization.class);
    }

    @Nonnull
    @Override
    public Authorization createAuthorization(@Nonnull final Organization organization,
                                             @Nonnull final List<Permission> permissions) {

        Arguments.checkNotNull(organization, "Organization is required");
        Arguments.checkNotNull(permissions, "Permissions are required");

        return createAuthorization(organization.getId(), permissions);
    }

    @Nonnull
    @Override
    public Authorization createAuthorization(@Nonnull final String orgID,
                                             @Nonnull final List<Permission> permissions) {

        Arguments.checkNonEmpty(orgID, "Organization ID");
        Arguments.checkNotNull(permissions, "Permissions are required");

        Authorization authorization = new Authorization();
        authorization.setOrgID(orgID);
        authorization.setPermissions(permissions);
        authorization.setStatus(Status.ACTIVE);

        return createAuthorization(authorization);
    }

    @Nonnull
    @Override
    public Authorization createAuthorization(@Nonnull final Authorization authorization) {

        Arguments.checkNotNull(authorization, "Authorization is required");

        String json = adapter.toJson(authorization);

        Call<Authorization> call = influxDBService.createAuthorization(createBody(json));

        return execute(call);
    }

    @Nonnull
    @Override
    public List<Authorization> findAuthorizations() {
        return findAuthorizationsByUserID(null);
    }

    @Nullable
    @Override
    public Authorization findAuthorizationByID(@Nonnull final String authorizationID) {

        Arguments.checkNonEmpty(authorizationID, "authorizationID");

        Call<Authorization> call = influxDBService.findAuthorization(authorizationID);

        return execute(call, NotFoundException.class);
    }

    @Nonnull
    @Override
    public List<Authorization> findAuthorizationsByUser(@Nonnull final User user) {

        Arguments.checkNotNull(user, "User is required");

        return findAuthorizations(user.getId(), null);
    }

    @Nonnull
    @Override
    public List<Authorization> findAuthorizationsByUserID(@Nullable final String userID) {
        return findAuthorizations(userID, null);
    }

    @Nonnull
    @Override
    public List<Authorization> findAuthorizationsByUserName(@Nullable final String userName) {
        return findAuthorizations(null, userName);
    }

    @Nonnull
    @Override
    public Authorization updateAuthorization(@Nonnull final Authorization authorization) {

        Arguments.checkNotNull(authorization, "Authorization is required");

        String json = adapter.toJson(authorization);

        Call<Authorization> authorizationCall = influxDBService
                .updateAuthorization(authorization.getId(), createBody(json));

        return execute(authorizationCall);
    }

    @Override
    public void deleteAuthorization(@Nonnull final Authorization authorization) {

        Arguments.checkNotNull(authorization, "Authorization is required");

        deleteAuthorization(authorization.getId());
    }

    @Override
    public void deleteAuthorization(@Nonnull final String authorizationID) {

        Arguments.checkNonEmpty(authorizationID, "authorizationID");

        Call<Void> call = influxDBService.deleteAuthorization(authorizationID);
        execute(call);
    }

    @Nonnull
    private List<Authorization> findAuthorizations(@Nullable final String userID, @Nullable final String userName) {

        Call<Authorizations> authorizationsCall = influxDBService.findAuthorizations(userID, userName);

        Authorizations authorizations = execute(authorizationsCall);
        LOG.log(Level.FINEST, "findAuthorizations found: {0}", authorizations);

        return authorizations.getAuthorizations();
    }
}