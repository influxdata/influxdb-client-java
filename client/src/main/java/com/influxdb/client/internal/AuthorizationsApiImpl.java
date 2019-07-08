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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.influxdb.Arguments;
import com.influxdb.client.AuthorizationsApi;
import com.influxdb.client.domain.Authorization;
import com.influxdb.client.domain.Authorizations;
import com.influxdb.client.domain.Organization;
import com.influxdb.client.domain.Permission;
import com.influxdb.client.domain.User;
import com.influxdb.client.service.AuthorizationsService;
import com.influxdb.internal.AbstractRestClient;

import retrofit2.Call;

/**
 * @author Jakub Bednar (bednar@github) (17/09/2018 12:00)
 */
final class AuthorizationsApiImpl extends AbstractRestClient implements AuthorizationsApi {

    private static final Logger LOG = Logger.getLogger(AuthorizationsApiImpl.class.getName());

    private final AuthorizationsService service;

    AuthorizationsApiImpl(@Nonnull final AuthorizationsService service) {

        Arguments.checkNotNull(service, "service");

        this.service = service;
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
        authorization.setStatus(Authorization.StatusEnum.ACTIVE);

        return createAuthorization(authorization);
    }

    @Nonnull
    @Override
    public Authorization createAuthorization(@Nonnull final Authorization authorization) {

        Arguments.checkNotNull(authorization, "Authorization is required");

        Call<Authorization> call = service.postAuthorizations(authorization, null);

        return execute(call);
    }

    @Nonnull
    @Override
    public List<Authorization> findAuthorizations() {
        return findAuthorizationsByUserID(null);
    }

    @Nonnull
    @Override
    public Authorization findAuthorizationByID(@Nonnull final String authorizationID) {

        Arguments.checkNonEmpty(authorizationID, "authorizationID");

        Call<Authorization> call = service.getAuthorizationsID(authorizationID, null);

        return execute(call);
    }

    @Nonnull
    @Override
    public List<Authorization> findAuthorizationsByUser(@Nonnull final User user) {

        Arguments.checkNotNull(user, "User is required");

        return findAuthorizations(user.getId(), null, null);
    }

    @Nonnull
    @Override
    public List<Authorization> findAuthorizationsByUserID(@Nullable final String userID) {
        return findAuthorizations(userID, null, null);
    }

    @Nonnull
    @Override
    public List<Authorization> findAuthorizationsByUserName(@Nullable final String userName) {
        return findAuthorizations(null, userName, null);
    }

    @Nonnull
    @Override
    public List<Authorization> findAuthorizationsByOrg(@Nonnull final Organization organization) {

        Arguments.checkNotNull(organization, "organization");

        return findAuthorizationsByOrgID(organization.getId());
    }

    @Nonnull
    @Override
    public List<Authorization> findAuthorizationsByOrgID(@Nullable final String orgID) {
        return findAuthorizations(null, null, orgID);
    }

    @Nonnull
    @Override
    public Authorization updateAuthorization(@Nonnull final Authorization authorization) {

        Arguments.checkNotNull(authorization, "Authorization is required");

        Call<Authorization> authorizationCall = service
                .patchAuthorizationsID(authorization.getId(), authorization, null);

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

        Call<Void> call = service.deleteAuthorizationsID(authorizationID, null);
        execute(call);
    }

    @Nonnull
    @Override
    public Authorization cloneAuthorization(@Nonnull final String authorizationID) {

        Arguments.checkNonEmpty(authorizationID, "authorizationID");

        Authorization authorization = findAuthorizationByID(authorizationID);

        return cloneAuthorization(authorization);
    }

    @Nonnull
    @Override
    public Authorization cloneAuthorization(@Nonnull final Authorization authorization) {

        Arguments.checkNotNull(authorization, "authorization");

        Authorization cloned = new Authorization();
        cloned.setOrgID(authorization.getOrgID());
        cloned.setStatus(Authorization.StatusEnum.ACTIVE);
        cloned.setDescription(authorization.getDescription());
        cloned.setPermissions(authorization.getPermissions());

        return createAuthorization(cloned);
    }

    @Nonnull
    private List<Authorization> findAuthorizations(@Nullable final String userID,
                                                   @Nullable final String userName,
                                                   @Nullable final String orgID) {

        Call<Authorizations> authorizationsCall = service.getAuthorizations(null, userID, userName, orgID, null);

        Authorizations authorizations = execute(authorizationsCall);
        LOG.log(Level.FINEST, "findAuthorizations found: {0}", authorizations);

        return authorizations.getAuthorizations();
    }
}