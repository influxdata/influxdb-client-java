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
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.influxdata.Arguments;
import org.influxdata.client.OrganizationsApi;
import org.influxdata.client.domain.FindOptions;
import org.influxdata.client.domain.Label;
import org.influxdata.client.domain.OperationLogEntries;
import org.influxdata.client.domain.OperationLogEntry;
import org.influxdata.client.domain.Organization;
import org.influxdata.client.domain.Organizations;
import org.influxdata.client.domain.ResourceMember;
import org.influxdata.client.domain.ResourceMembers;
import org.influxdata.client.domain.ResourceType;
import org.influxdata.client.domain.Secrets;
import org.influxdata.client.domain.User;
import org.influxdata.exceptions.NotFoundException;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import retrofit2.Call;

/**
 * @author Jakub Bednar (bednar@github) (12/09/2018 08:57)
 */
final class OrganizationsApiImpl extends AbstractInfluxDBRestClient implements OrganizationsApi {

    private static final Logger LOG = Logger.getLogger(OrganizationsApiImpl.class.getName());

    private final JsonAdapter<Organization> adapter;
    private final JsonAdapter<User> userAdapter;
    private final JsonAdapter<Map> mapAdapter;
    private final JsonAdapter<List> listAdapter;

    OrganizationsApiImpl(@Nonnull final InfluxDBService influxDBService, @Nonnull final Moshi moshi) {

        super(influxDBService, moshi);

        this.adapter = moshi.adapter(Organization.class);
        this.userAdapter = moshi.adapter(User.class);
        this.mapAdapter = moshi.adapter(Map.class);
        this.listAdapter = moshi.adapter(List.class);
    }

    @Nullable
    @Override
    public Organization findOrganizationByID(@Nonnull final String orgID) {

        Arguments.checkNonEmpty(orgID, "Organization ID");

        Call<Organization> organization = influxDBService.findOrganizationByID(orgID);

        return execute(organization, NotFoundException.class);
    }

    @Nonnull
    @Override
    public List<Organization> findOrganizations() {

        Call<Organizations> organizationsCall = influxDBService.findOrganizations();

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

        String json = adapter.toJson(organization);

        Call<Organization> call = influxDBService.createOrganization(createBody(json));

        return execute(call);
    }

    @Nonnull
    @Override
    public Organization updateOrganization(@Nonnull final Organization organization) {

        Arguments.checkNotNull(organization, "Organization");

        String json = adapter.toJson(organization);

        Call<Organization> orgCall = influxDBService.updateOrganization(organization.getId(), createBody(json));

        return execute(orgCall);
    }

    @Override
    public void deleteOrganization(@Nonnull final Organization organization) {

        Objects.requireNonNull(organization, "Organization is required");

        deleteOrganization(organization.getId());
    }

    @Override
    public void deleteOrganization(@Nonnull final String orgID) {

        Arguments.checkNonEmpty(orgID, "Organization ID");

        Call<Void> call = influxDBService.deleteOrganization(orgID);
        execute(call);
    }

    @Override
    public List<String> getSecrets(@Nonnull final Organization organization) {

        Arguments.checkNotNull(organization, "Organization");

        return getSecrets(organization.getId());
    }

    @Override
    public List<String> getSecrets(@Nonnull final String orgID) {

        Arguments.checkNonEmpty(orgID, "Organization ID");

        Call<Secrets> call = influxDBService.getSecrets(orgID);

        Secrets secrets = execute(call);
        LOG.log(Level.FINEST, "getSecrets found: {0}", secrets);

        return secrets.getSecrets();
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

        Call<Void> call = influxDBService.putSecrets(orgID, createBody(mapAdapter.toJson(secrets)));
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

        Call<Void> call = influxDBService.deleteSecrets(orgID, createBody(listAdapter.toJson(secrets)));
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

        Call<ResourceMembers> call = influxDBService.findOrganizationMembers(orgID);
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

        User user = new User();
        user.setId(memberID);

        String json = userAdapter.toJson(user);
        Call<ResourceMember> call = influxDBService.addOrganizationMember(orgID, createBody(json));

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

        Call<Void> call = influxDBService.deleteOrganizationMember(orgID, memberID);
        execute(call);
    }

    @Nonnull
    @Override
    public List<ResourceMember> getOwners(@Nonnull final Organization organization) {

        Arguments.checkNotNull(organization, "Organization");

        return getOwners(organization.getId());
    }

    @Nonnull
    @Override
    public List<ResourceMember> getOwners(@Nonnull final String orgID) {

        Arguments.checkNonEmpty(orgID, "Organization ID");

        Call<ResourceMembers> call = influxDBService.findOrganizationOwners(orgID);
        ResourceMembers resourceMembers = execute(call);
        LOG.log(Level.FINEST, "findOrganizationOwners found: {0}", resourceMembers);

        return resourceMembers.getUsers();
    }

    @Nonnull
    @Override
    public ResourceMember addOwner(@Nonnull final User owner, @Nonnull final Organization organization) {

        Arguments.checkNotNull(organization, "Organization");
        Arguments.checkNotNull(owner, "owner");

        return addOwner(owner.getId(), organization.getId());
    }

    @Nonnull
    @Override
    public ResourceMember addOwner(@Nonnull final String ownerID, @Nonnull final String orgID) {

        Arguments.checkNonEmpty(ownerID, "Owner ID");
        Arguments.checkNonEmpty(orgID, "Organization ID");

        User user = new User();
        user.setId(ownerID);

        String json = userAdapter.toJson(user);
        Call<ResourceMember> call = influxDBService.addOrganizationOwner(orgID, createBody(json));

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

        Call<Void> call = influxDBService.deleteOrganizationOwner(orgID, ownerID);
        execute(call);
    }

    @Nonnull
    @Override
    public List<Label> getLabels(@Nonnull final Organization organization) {

        Arguments.checkNotNull(organization, "organization");

        return getLabels(organization.getId());
    }

    @Nonnull
    @Override
    public List<Label> getLabels(@Nonnull final String orgID) {

        Arguments.checkNonEmpty(orgID, "orgID");

        return getLabels(orgID, "orgs");
    }

    @Nonnull
    @Override
    public Label addLabel(@Nonnull final Label label, @Nonnull final Organization organization) {

        Arguments.checkNotNull(label, "label");
        Arguments.checkNotNull(organization, "organization");

        return addLabel(label.getId(), organization.getId());
    }

    @Nonnull
    @Override
    public Label addLabel(@Nonnull final String labelID, @Nonnull final String orgID) {

        Arguments.checkNonEmpty(labelID, "labelID");
        Arguments.checkNonEmpty(orgID, "orgID");

        return addLabel(labelID, orgID, "orgs", ResourceType.ORGS);
    }

    @Override
    public void deleteLabel(@Nonnull final Label label, @Nonnull final Organization organization) {

        Arguments.checkNotNull(label, "label");
        Arguments.checkNotNull(organization, "organization");

        deleteLabel(label.getId(), organization.getId());
    }

    @Override
    public void deleteLabel(@Nonnull final String labelID, @Nonnull final String orgID) {

        Arguments.checkNonEmpty(labelID, "labelID");
        Arguments.checkNonEmpty(orgID, "orgID");

        deleteLabel(labelID, orgID, "orgs");
    }

    @Nonnull
    @Override
    public List<OperationLogEntry> findOrganizationLogs(@Nonnull final Organization organization) {

        Arguments.checkNotNull(organization, "organization");

        return findOrganizationLogs(organization.getId());
    }

    @Nonnull
    @Override
    public OperationLogEntries findOrganizationLogs(@Nonnull final Organization organization,
                                                    @Nonnull final FindOptions findOptions) {

        Arguments.checkNotNull(organization, "organization");
        Arguments.checkNotNull(findOptions, "findOptions");

        return findOrganizationLogs(organization.getId(), findOptions);
    }

    @Nonnull
    @Override
    public List<OperationLogEntry> findOrganizationLogs(@Nonnull final String orgID) {

        Arguments.checkNonEmpty(orgID, "orgID");

        return findOrganizationLogs(orgID, new FindOptions()).getLogs();
    }

    @Nonnull
    @Override
    public OperationLogEntries findOrganizationLogs(@Nonnull final String orgID,
                                                    @Nonnull final FindOptions findOptions) {

        Arguments.checkNonEmpty(orgID, "orgID");
        Arguments.checkNotNull(findOptions, "findOptions");

        Call<OperationLogEntries> call = influxDBService.findOrganizationLogs(orgID, createQueryMap(findOptions));

        return getOperationLogEntries(call);
    }
}