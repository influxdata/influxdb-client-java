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
import java.util.Map;
import javax.annotation.Nonnull;

import com.influxdb.client.domain.Label;
import com.influxdb.client.domain.LabelResponse;
import com.influxdb.client.domain.OperationLog;
import com.influxdb.client.domain.OperationLogs;
import com.influxdb.client.domain.Organization;
import com.influxdb.client.domain.ResourceMember;
import com.influxdb.client.domain.ResourceOwner;
import com.influxdb.client.domain.SecretKeys;
import com.influxdb.client.domain.SecretKeysResponse;
import com.influxdb.client.domain.User;

/**
 * The client of the InfluxDB 2.0 that implement Organization HTTP API endpoint.
 *
 * @author Jakub Bednar (bednar@github) (11/09/2018 14:58)
 */
public interface OrganizationsApi {

    /**
     * Creates a new organization and sets {@link Organization#getId()} with the new identifier.
     *
     * @param organization the organization to create
     * @return Organization created
     */
    @Nonnull
    Organization createOrganization(@Nonnull final Organization organization);

    /**
     * Creates a new organization and sets {@link Organization#getId()} with the new identifier.
     *
     * @param name name of the organization
     * @return Organization created
     */
    @Nonnull
    Organization createOrganization(@Nonnull final String name);

    /**
     * Update an organization.
     *
     * @param organization organization update to apply
     * @return organization updated
     */
    @Nonnull
    Organization updateOrganization(@Nonnull final Organization organization);

    /**
     * Delete an organization.
     *
     * @param organization organization to delete
     */
    void deleteOrganization(@Nonnull final Organization organization);

    /**
     * Delete an organization.
     *
     * @param orgID ID of organization to delete
     */
    void deleteOrganization(@Nonnull final String orgID);

    /**
     * Clone an organization.
     *
     * @param clonedName name of cloned organization
     * @param orgID      ID of organization to clone
     * @return cloned organization
     */
    @Nonnull
    Organization cloneOrganization(@Nonnull final String clonedName, @Nonnull final String orgID);

    /**
     * Clone an organization.
     *
     * @param clonedName   name of cloned organization
     * @param organization organization to clone
     * @return cloned organization
     */
    @Nonnull
    Organization cloneOrganization(@Nonnull final String clonedName, @Nonnull final Organization organization);

    /**
     * Retrieve an organization.
     *
     * @param orgID ID of organization to get
     * @return organization details
     */
    @Nonnull
    Organization findOrganizationByID(@Nonnull final String orgID);

    /**
     * List all organizations.
     *
     * @return List all organizations
     */
    @Nonnull
    List<Organization> findOrganizations();

    /**
     * List of secret keys the are stored for Organization.
     * <p>
     * For example
     * <pre>
     *     github_api_key,
     *     some_other_key,
     *     a_secret_key
     * </pre>
     *
     * @param organization the organization for get secrets
     * @return the secret keys
     */
    SecretKeysResponse getSecrets(@Nonnull final Organization organization);

    /**
     * List of secret keys the are stored for Organization.
     * <p>
     * For example
     * <pre>
     *     github_api_key,
     *     some_other_key,
     *     a_secret_key
     * </pre>
     *
     * @param orgID the organization for get secrets
     * @return the secret keys
     */
    SecretKeysResponse getSecrets(@Nonnull final String orgID);

    /**
     * Patches all provided secrets and updates any previous values.
     *
     * @param secrets      secrets to update/add
     * @param organization the organization for put secrets
     */
    void putSecrets(@Nonnull final Map<String, String> secrets, @Nonnull final Organization organization);

    /**
     * Patches all provided secrets and updates any previous values.
     *
     * @param secrets secrets to update/add
     * @param orgID   the organization for put secrets
     */
    void putSecrets(@Nonnull final Map<String, String> secrets, @Nonnull final String orgID);

    /**
     * Delete provided secrets.
     *
     * @param secrets      secrets to delete
     * @param organization the organization for delete secrets
     */
    void deleteSecrets(@Nonnull final List<String> secrets, @Nonnull final Organization organization);

    /**
     * Delete provided secrets.
     *
     * @param secrets secrets to delete
     * @param orgID   the organization for delete secrets
     */
    void deleteSecrets(@Nonnull final List<String> secrets, @Nonnull final String orgID);

    /**
     * Delete provided secrets.
     *
     * @param secretKeys secret key to deleted (required)
     * @param orgID ID of the organization (required)
     */
    void deleteSecrets(@Nonnull final SecretKeys secretKeys, @Nonnull final String orgID);

    /**
     * List all members of an organization.
     *
     * @param organization of the members
     * @return return the List all members of an organization
     */
    @Nonnull
    List<ResourceMember> getMembers(@Nonnull final Organization organization);

    /**
     * List all members of an organization.
     *
     * @param orgID ID of organization to get members
     * @return return the List all members of an organization
     */
    @Nonnull
    List<ResourceMember> getMembers(@Nonnull final String orgID);

    /**
     * Add organization member.
     *
     * @param member       the member of an organization
     * @param organization the organization for the member
     * @return created mapping
     */
    @Nonnull
    ResourceMember addMember(@Nonnull final User member, @Nonnull final Organization organization);

    /**
     * Add organization member.
     *
     * @param memberID the ID of a member
     * @param orgID    the ID of an organization
     * @return created mapping
     */
    @Nonnull
    ResourceMember addMember(@Nonnull final String memberID, @Nonnull final String orgID);

    /**
     * Removes a member from an organization.
     *
     * @param member       the member of an organization
     * @param organization the organization of a member
     */
    void deleteMember(@Nonnull final User member, @Nonnull final Organization organization);

    /**
     * Removes a member from an organization.
     *
     * @param orgID    the ID of an organization
     * @param memberID the ID of a member
     */
    void deleteMember(@Nonnull final String memberID, @Nonnull final String orgID);

    /**
     * List all owners of an organization.
     *
     * @param organization of the owners
     * @return return the List all owners of an organization
     */
    @Nonnull
    List<ResourceOwner> getOwners(@Nonnull final Organization organization);

    /**
     * List all owners of an organization.
     *
     * @param orgID ID of organization to get owners
     * @return return the List all owners of an organization
     */
    @Nonnull
    List<ResourceOwner> getOwners(@Nonnull final String orgID);

    /**
     * Add organization owner.
     *
     * @param owner        the owner of an organization
     * @param organization the organization of a owner
     * @return created mapping
     */
    @Nonnull
    ResourceOwner addOwner(@Nonnull final User owner, @Nonnull final Organization organization);

    /**
     * Add organization owner.
     *
     * @param orgID   the ID of an organization
     * @param ownerID the ID of a owner
     * @return created mapping
     */
    @Nonnull
    ResourceOwner addOwner(@Nonnull final String ownerID, @Nonnull final String orgID);

    /**
     * Removes a owner from an organization.
     *
     * @param owner        the owner of an organization
     * @param organization the organization of a owner
     */
    void deleteOwner(@Nonnull final User owner, @Nonnull final Organization organization);

    /**
     * Removes a owner from an organization.
     *
     * @param orgID   the ID of an organization
     * @param ownerID the ID of a owner
     */
    void deleteOwner(@Nonnull final String ownerID, @Nonnull final String orgID);

    /**
     * List all labels of an organization.
     *
     * @param organization the organization with labels
     * @return return List all labels of an organization.
     */
    @Nonnull
    List<Label> getLabels(@Nonnull final Organization organization);

    /**
     * List all labels of an organization.
     *
     * @param orgID ID of organization to get labels
     * @return return List all labels of an organization
     */
    @Nonnull
    List<Label> getLabels(@Nonnull final String orgID);

    /**
     * Add the organization label.
     *
     * @param label        the label of an organization
     * @param organization the organization
     * @return added label
     */
    @Nonnull
    LabelResponse addLabel(@Nonnull final Label label, @Nonnull final Organization organization);

    /**
     * Add the organization label.
     *
     * @param orgID   the ID of an organization
     * @param labelID the ID of a label
     * @return added label
     */
    @Nonnull
    LabelResponse addLabel(@Nonnull final String labelID, @Nonnull final String orgID);

    /**
     * Removes a label from an organization.
     *
     * @param label        the label
     * @param organization the organization
     */
    void deleteLabel(@Nonnull final Label label, @Nonnull final Organization organization);

    /**
     * Removes a label from an organization.
     *
     * @param orgID   the ID of an organization
     * @param labelID the ID of a label
     */
    void deleteLabel(@Nonnull final String labelID, @Nonnull final String orgID);

    /**
     * Retrieve an organization's logs.
     *
     * @param organization for retrieve logs
     * @return logs
     */
    @Nonnull
    List<OperationLog> findOrganizationLogs(@Nonnull final Organization organization);

    /**
     * Retrieve an organization's logs.
     *
     * @param organization for retrieve logs
     * @return logs
     */
    @Nonnull
    OperationLogs findOrganizationLogs(@Nonnull final Organization organization,
                                       @Nonnull final FindOptions findOptions);

    /**
     * Retrieve an organization's logs.
     *
     * @param orgID id of an organization
     * @return logs
     */
    @Nonnull
    List<OperationLog> findOrganizationLogs(@Nonnull final String orgID);

    /**
     * Retrieve an organization's logs.
     *
     * @param orgID id of an organization
     * @return logs
     */
    @Nonnull
    OperationLogs findOrganizationLogs(@Nonnull final String orgID, @Nonnull final FindOptions findOptions);
}