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
package org.influxdata.platform;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.influxdata.platform.domain.Organization;

/**
 * The client of the InfluxData Platform that implement Organization HTTP API endpoint.
 *
 * @author Jakub Bednar (bednar@github) (11/09/2018 14:58)
 */
public interface OrganizationClient {

    /**
     * Creates a new organization and sets {@link Organization#id} with the new identifier.
     *
     * @param organization the organization to create
     * @return Organization created
     */
    @Nonnull
    Organization createOrganization(@Nonnull final Organization organization);

    /**
     * Creates a new organization and sets {@link Organization#id} with the new identifier.
     *
     * @param name name of the organization
     * @return Organization created
     */
    @Nonnull
    Organization createOrganization(@Nonnull final String name);

    /**
     * Update a organization.
     *
     * @param organization organization update to apply
     * @return organization updated
     */
    @Nonnull
    Organization updateOrganization(@Nonnull final Organization organization);

    /**
     * Delete a organization.
     *
     * @param organization organization to delete
     */
    void deleteOrganization(@Nonnull final Organization organization);

    /**
     * Delete a organization.
     *
     * @param organizationID ID of organization to delete
     */
    void deleteOrganization(@Nonnull final String organizationID);

    /**
     * Retrieve a organization.
     *
     * @param organizationID ID of organization to get
     * @return organization details
     */
    @Nullable
    Organization findOrganizationByID(@Nonnull final String organizationID);

    /**
     * List all organizations.
     *
     * @return List all organizations
     */
    @Nonnull
    List<Organization> findOrganizations();
}