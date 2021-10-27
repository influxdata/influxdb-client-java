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
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

import com.influxdb.client.OrganizationsApi;
import com.influxdb.client.domain.AddResourceMemberRequestBody;
import com.influxdb.client.domain.Organization;
import com.influxdb.client.domain.Organizations;
import com.influxdb.client.domain.PatchOrganizationRequest;
import com.influxdb.client.domain.PostOrganizationRequest;
import com.influxdb.client.domain.ResourceMember;
import com.influxdb.client.domain.ResourceMembers;
import com.influxdb.client.domain.ResourceOwner;
import com.influxdb.client.domain.ResourceOwners;
import com.influxdb.client.domain.SecretKeys;
import com.influxdb.client.domain.SecretKeysResponse;
import com.influxdb.client.domain.User;
import com.influxdb.client.service.OrganizationsService;
import com.influxdb.client.service.SecretsService;
import com.influxdb.internal.AbstractRestClient;
import com.influxdb.utils.Arguments;

import retrofit2.Call;

/**
 * @author Jakub Bednar (bednar@github) (12/09/2018 08:57)
 */
final class OrganizationsApiImpl extends AbstractRestClient implements OrganizationsApi {

    private static final Logger LOG = Logger.getLogger(OrganizationsApiImpl.class.getName());

    private final OrganizationsService service;
    private final SecretsService secretsService;

    OrganizationsApiImpl(@Nonnull final OrganizationsService service, @Nonnull final SecretsService secretsService) {

        Arguments.checkNotNull(service, "service");
        Arguments.checkNotNull(secretsService, "secretsService");

        this.service = service;
        this.secretsService = secretsService;
    }

    @Nonnull
    @Override
    public Organization findOrganizationByID(@Nonnull final String orgID) {

        Arguments.checkNonEmpty(orgID, "Organization ID");

        Call<Organization> organization = service.getOrgsID(orgID, null);

        return execute(organization);
    }

    @Nonnull
    @Override
    public List<Organization> findOrganizations() {

        Call<Organizations> organizationsCall = service.getOrgs(null, null, null, null, null, null, null);

        Organizations organizations = execute(organizationsCall);
        LOG.log(Level.FINEST, "findOrganizations found: {0}", organizations);

        return organizations.getOrgs();
    }

    @Nonnull
    @Override
    public Organization createOrganization(@Nonnull final String name) {

        Arguments.checkNonEmpty(name, "Organization name");

        Organization organization = new Organization();
        organization.setName(name);

        return createOrganization(organization);
    }

    @Nonnull
    @Override
    public Organization createOrganization(@Nonnull final Organization organization) {

        Arguments.checkNotNull(organization, "Organization");

        PostOrganizationRequest request = new PostOrganizationRequest()
                .name(organization.getName())
                .description(organization.getDescription());

        Call<Organization> call = service.postOrgs(request, null);

        return execute(call);
    }

    @Nonnull
    @Override
    public Organization updateOrganization(@Nonnull final Organization organization) {

        Arguments.checkNotNull(organization, "Organization");

        PatchOrganizationRequest patch = new PatchOrganizationRequest()
                .name(organization.getName())
                .description(organization.getName());

        Call<Organization> orgCall = service
                .patchOrgsID(organization.getId(), patch, null);

        return execute(orgCall);
    }

    @Override
    public void deleteOrganization(@Nonnull final Organization organization) {

        Arguments.checkNotNull(organization, "Organization is required");

        deleteOrganization(organization.getId());
    }

    @Override
    public void deleteOrganization(@Nonnull final String orgID) {

        Arguments.checkNonEmpty(orgID, "Organization ID");

        Call<Void> call = service.deleteOrgsID(orgID, null);
        execute(call);
    }

    @Nonnull
    @Override
    public Organization cloneOrganization(@Nonnull final String clonedName, @Nonnull final String orgID) {

        Arguments.checkNonEmpty(clonedName, "clonedName");
        Arguments.checkNonEmpty(orgID, "orgID");

        Organization organization = findOrganizationByID(orgID);

        return cloneOrganization(clonedName, organization);
    }

    @Nonnull
    @Override
    public Organization cloneOrganization(@Nonnull final String clonedName, @Nonnull final Organization organization) {

        Arguments.checkNonEmpty(clonedName, "clonedName");
        Arguments.checkNotNull(organization, "Organization");

        Organization cloned = new Organization();
        cloned.setName(clonedName);
        cloned.setDescription(organization.getDescription());

        Organization created = createOrganization(cloned);
        return created;
    }

    @Override
    public SecretKeysResponse getSecrets(@Nonnull final Organization organization) {

        Arguments.checkNotNull(organization, "Organization");

        return getSecrets(organization.getId());
    }

    @Override
    public SecretKeysResponse getSecrets(@Nonnull final String orgID) {

        Arguments.checkNonEmpty(orgID, "Organization ID");

        Call<SecretKeysResponse> call = secretsService.getOrgsIDSecrets(orgID, null);

        return execute(call);
    }

    @Override
    public void putSecrets(@Nonnull final Map<String, String> secrets, @Nonnull final Organization organization) {

        Arguments.checkNotNull(organization, "Organization");

        putSecrets(secrets, organization.getId());
    }

    @Override
    public void putSecrets(@Nonnull final Map<String, String> secrets, @Nonnull final String orgID) {

        Arguments.checkNonEmpty(orgID, "Organization ID");
        Arguments.checkNotNull(secrets, "secrets");

        Call<Void> call = secretsService.patchOrgsIDSecrets(orgID, secrets, null);
        execute(call);
    }

    @Override
    public void deleteSecrets(@Nonnull final List<String> secrets, @Nonnull final Organization organization) {

        Arguments.checkNotNull(organization, "Organization");
        Arguments.checkNotNull(secrets, "secrets");

        deleteSecrets(secrets, organization.getId());
    }

    @Override
    public void deleteSecrets(@Nonnull final List<String> secrets, @Nonnull final String orgID) {

        Arguments.checkNonEmpty(orgID, "Organization ID");
        Arguments.checkNotNull(secrets, "secrets");

        SecretKeys secretKeys = new SecretKeys();
        secrets.forEach(secretKeys::addSecretsItem);

        deleteSecrets(secretKeys, orgID);
    }

    @Override
    public void deleteSecrets(@Nonnull final SecretKeys secretKeys, @Nonnull final String orgID) {

        Arguments.checkNonEmpty(orgID, "Organization ID");
        Arguments.checkNotNull(secretKeys, "secretKeys");

        Call<Void> call = secretsService.postOrgsIDSecrets(orgID, secretKeys, null);
        execute(call);
    }

    @Nonnull
    @Override
    public List<ResourceMember> getMembers(@Nonnull final Organization organization) {

        Arguments.checkNotNull(organization, "Organization");

        return getMembers(organization.getId());
    }

    @Nonnull
    @Override
    public List<ResourceMember> getMembers(@Nonnull final String orgID) {

        Arguments.checkNonEmpty(orgID, "Organization ID");

        Call<ResourceMembers> call = service.getOrgsIDMembers(orgID, null);
        ResourceMembers resourceMembers = execute(call);
        LOG.log(Level.FINEST, "findOrganizationMembers found: {0}", resourceMembers);

        return resourceMembers.getUsers();
    }

    @Nonnull
    @Override
    public ResourceMember addMember(@Nonnull final User member, @Nonnull final Organization organization) {

        Arguments.checkNotNull(organization, "Organization");
        Arguments.checkNotNull(member, "member");

        return addMember(member.getId(), organization.getId());
    }

    @Nonnull
    @Override
    public ResourceMember addMember(@Nonnull final String memberID, @Nonnull final String orgID) {

        Arguments.checkNonEmpty(memberID, "Member ID");
        Arguments.checkNonEmpty(orgID, "Organization ID");

        AddResourceMemberRequestBody user = new AddResourceMemberRequestBody();
        user.setId(memberID);

        Call<ResourceMember> call = service.postOrgsIDMembers(orgID, user, null);

        return execute(call);
    }

    @Override
    public void deleteMember(@Nonnull final User member, @Nonnull final Organization organization) {

        Arguments.checkNotNull(organization, "Organization");
        Arguments.checkNotNull(member, "member");

        deleteMember(member.getId(), organization.getId());
    }

    @Override
    public void deleteMember(@Nonnull final String memberID, @Nonnull final String orgID) {

        Arguments.checkNonEmpty(memberID, "Member ID");
        Arguments.checkNonEmpty(orgID, "Organization ID");

        Call<Void> call = service.deleteOrgsIDMembersID(memberID, orgID, null);
        execute(call);
    }

    @Nonnull
    @Override
    public List<ResourceOwner> getOwners(@Nonnull final Organization organization) {

        Arguments.checkNotNull(organization, "Organization");

        return getOwners(organization.getId());
    }

    @Nonnull
    @Override
    public List<ResourceOwner> getOwners(@Nonnull final String orgID) {

        Arguments.checkNonEmpty(orgID, "Organization ID");

        Call<ResourceOwners> call = service.getOrgsIDOwners(orgID, null);
        ResourceOwners resourceMembers = execute(call);
        LOG.log(Level.FINEST, "findOrganizationOwners found: {0}", resourceMembers);

        return resourceMembers.getUsers();
    }

    @Nonnull
    @Override
    public ResourceOwner addOwner(@Nonnull final User owner, @Nonnull final Organization organization) {

        Arguments.checkNotNull(organization, "Organization");
        Arguments.checkNotNull(owner, "owner");

        return addOwner(owner.getId(), organization.getId());
    }

    @Nonnull
    @Override
    public ResourceOwner addOwner(@Nonnull final String ownerID, @Nonnull final String orgID) {

        Arguments.checkNonEmpty(ownerID, "Owner ID");
        Arguments.checkNonEmpty(orgID, "Organization ID");

        AddResourceMemberRequestBody user = new AddResourceMemberRequestBody();
        user.setId(ownerID);

        Call<ResourceOwner> call = service.postOrgsIDOwners(orgID, user, null);

        return execute(call);
    }

    @Override
    public void deleteOwner(@Nonnull final User owner, @Nonnull final Organization organization) {
        Arguments.checkNotNull(organization, "Organization");
        Arguments.checkNotNull(owner, "owner");

        deleteOwner(owner.getId(), organization.getId());
    }

    @Override
    public void deleteOwner(@Nonnull final String ownerID, @Nonnull final String orgID) {

        Arguments.checkNonEmpty(ownerID, "Owner ID");
        Arguments.checkNonEmpty(orgID, "Organization ID");

        Call<Void> call = service.deleteOrgsIDOwnersID(ownerID, orgID, null);
        execute(call);
    }
}