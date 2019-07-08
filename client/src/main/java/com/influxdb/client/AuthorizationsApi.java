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

import com.influxdb.client.domain.Authorization;
import com.influxdb.client.domain.Organization;
import com.influxdb.client.domain.Permission;
import com.influxdb.client.domain.User;

/**
 * The client of the InfluxDB 2.0 that implement Authorization HTTP API endpoint.
 *
 * @author Jakub Bednar (bednar@github) (17/09/2018 11:09)
 */
public interface AuthorizationsApi {

    /**
     * Create an new authorization.
     *
     * @param authorization authorization to create.
     * @return created authorization
     */
    @Nonnull
    Authorization createAuthorization(@Nonnull final Authorization authorization);

    /**
     * Create an authorization with defined {@code permissions}.
     *
     * @param organization owner of authorization
     * @param permissions  the permissions for the authorization
     * @return created authorization
     */
    @Nonnull
    Authorization createAuthorization(@Nonnull final Organization organization,
                                      @Nonnull final List<Permission> permissions);

    /**
     * Create an authorization with defined {@code permissions}.
     *
     * @param orgID       owner id of authorization
     * @param permissions the permissions for the authorization
     * @return created authorization
     */
    @Nonnull
    Authorization createAuthorization(@Nonnull final String orgID,
                                      @Nonnull final List<Permission> permissions);

    /**
     * Updates the status of the authorization. Useful for setting an authorization to inactive or active.
     *
     * @param authorization the authorization with updated status
     * @return updated authorization
     */
    @Nonnull
    Authorization updateAuthorization(@Nonnull final Authorization authorization);

    /**
     * Delete an authorization.
     *
     * @param authorization authorization to delete
     */
    void deleteAuthorization(@Nonnull final Authorization authorization);

    /**
     * Delete an authorization.
     *
     * @param authorizationID ID of authorization to delete
     */
    void deleteAuthorization(@Nonnull final String authorizationID);

    /**
     * Clone an authorization.
     *
     * @param authorizationID ID of authorization to clone
     * @return cloned authorization
     */
    @Nonnull
    Authorization cloneAuthorization(@Nonnull final String authorizationID);

    /**
     * Clone an authorization.
     *
     * @param authorization authorization to clone
     * @return cloned authorization
     */
    @Nonnull
    Authorization cloneAuthorization(@Nonnull final Authorization authorization);

    /**
     * List all authorizations.
     *
     * @return list all authorizations
     */
    @Nonnull
    List<Authorization> findAuthorizations();

    /**
     * Retrieve an authorization.
     *
     * @param authorizationID ID of authorization to get
     * @return authorization details
     */
    @Nonnull
    Authorization findAuthorizationByID(@Nonnull final String authorizationID);

    /**
     * List all authorizations for specified {@code user}.
     *
     * @param user filter authorizations belonging to a user
     * @return A list of authorizations
     */
    @Nonnull
    List<Authorization> findAuthorizationsByUser(@Nonnull final User user);

    /**
     * List all authorizations for specified {@code userID}.
     *
     * @param userID filter authorizations belonging to a user ID
     * @return A list of authorizations
     */
    @Nonnull
    List<Authorization> findAuthorizationsByUserID(@Nullable final String userID);

    /**
     * List all authorizations for specified {@code userName}.
     *
     * @param userName filter authorizations belonging to a user name
     * @return A list of authorizations
     */
    @Nonnull
    List<Authorization> findAuthorizationsByUserName(@Nullable final String userName);

    /**
     * List all authorizations for specified {@code organization}.
     *
     * @param organization filter authorizations belonging to a org
     * @return A list of authorizations
     */
    @Nonnull
    List<Authorization> findAuthorizationsByOrg(@Nonnull final Organization organization);

    /**
     * List all authorizations for specified {@code orgID}.
     *
     * @param orgID filter authorizations belonging to a org id
     * @return A list of authorizations
     */
    @Nonnull
    List<Authorization> findAuthorizationsByOrgID(@Nullable final String orgID);
}