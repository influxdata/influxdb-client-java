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

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.influxdb.Arguments;
import com.influxdb.client.TelegrafsApi;
import com.influxdb.client.domain.AddResourceMemberRequestBody;
import com.influxdb.client.domain.Label;
import com.influxdb.client.domain.LabelMapping;
import com.influxdb.client.domain.LabelResponse;
import com.influxdb.client.domain.LabelsResponse;
import com.influxdb.client.domain.Organization;
import com.influxdb.client.domain.ResourceMember;
import com.influxdb.client.domain.ResourceMembers;
import com.influxdb.client.domain.ResourceOwner;
import com.influxdb.client.domain.ResourceOwners;
import com.influxdb.client.domain.Telegraf;
import com.influxdb.client.domain.TelegrafRequest;
import com.influxdb.client.domain.TelegrafRequestAgent;
import com.influxdb.client.domain.TelegrafRequestPlugin;
import com.influxdb.client.domain.Telegrafs;
import com.influxdb.client.domain.User;
import com.influxdb.client.service.TelegrafsService;
import com.influxdb.internal.AbstractRestClient;

import retrofit2.Call;

/**
 * @author Jakub Bednar (bednar@github) (28/02/2019 10:25)
 */
final class TelegrafsApiImpl extends AbstractRestClient implements TelegrafsApi {

    private static final Logger LOG = Logger.getLogger(TelegrafsApiImpl.class.getName());

    private final TelegrafsService service;

    TelegrafsApiImpl(@Nonnull final TelegrafsService service) {

        Arguments.checkNotNull(service, "service");

        this.service = service;
    }

    @Nonnull
    @Override
    public Telegraf createTelegraf(@Nonnull final TelegrafRequest telegrafRequest) {

        Arguments.checkNotNull(telegrafRequest, "telegrafRequest");

        Call<Telegraf> call = service.postTelegrafs(telegrafRequest, null);

        return execute(call);
    }

    @Nonnull
    @Override
    public Telegraf createTelegraf(@Nonnull final String name,
                                   @Nullable final String description,
                                   @Nonnull final String orgID,
                                   @Nonnull final Integer collectionInterval,
                                   @Nonnull final TelegrafRequestPlugin... plugins) {

        Arguments.checkNonEmpty(name, "TelegrafConfig.name");
        Arguments.checkNonEmpty(orgID, "TelegrafConfig.orgID");
        Arguments.checkPositiveNumber(collectionInterval, "TelegrafConfig.collectionInterval");

        return createTelegraf(name, description, orgID, collectionInterval, Arrays.asList(plugins));
    }

    @Nonnull
    @Override
    public Telegraf createTelegraf(@Nonnull final String name,
                                   @Nullable final String description,
                                   @Nonnull final Organization org,
                                   @Nonnull final Integer collectionInterval,
                                   @Nonnull final TelegrafRequestPlugin... plugins) {

        Arguments.checkNonEmpty(name, "TelegrafConfig.name");
        Arguments.checkNotNull(org, "TelegrafConfig.org");
        Arguments.checkPositiveNumber(collectionInterval, "TelegrafConfig.collectionInterval");

        return createTelegraf(name, description, org.getId(), collectionInterval, plugins);
    }

    @Nonnull
    @Override
    public Telegraf createTelegraf(@Nonnull final String name,
                                   @Nullable final String description,
                                   @Nonnull final Organization org,
                                   @Nonnull final Integer collectionInterval,
                                   @Nonnull final List<TelegrafRequestPlugin> plugins) {

        Arguments.checkNonEmpty(name, "TelegrafConfig.name");
        Arguments.checkNotNull(org, "TelegrafConfig.org");
        Arguments.checkPositiveNumber(collectionInterval, "TelegrafConfig.collectionInterval");

        return createTelegraf(name, description, org.getId(), collectionInterval, plugins);
    }

    @Nonnull
    @Override
    public Telegraf createTelegraf(@Nonnull final String name,
                                   @Nullable final String description,
                                   @Nonnull final String orgID,
                                   @Nonnull final Integer collectionInterval,
                                   @Nonnull final List<TelegrafRequestPlugin> plugins) {

        Arguments.checkNonEmpty(name, "TelegrafConfig.name");
        Arguments.checkNonEmpty(orgID, "TelegrafConfig.orgID");
        Arguments.checkPositiveNumber(collectionInterval, "TelegrafConfig.collectionInterval");

        TelegrafRequestAgent telegrafAgent = new TelegrafRequestAgent();
        telegrafAgent.setCollectionInterval(collectionInterval);

        TelegrafRequest telegrafConfig = new TelegrafRequest();
        telegrafConfig.setName(name);
        telegrafConfig.setDescription(description);
        telegrafConfig.setOrgID(orgID);
        telegrafConfig.setAgent(telegrafAgent);
        telegrafConfig.plugins(plugins);

        return createTelegraf(telegrafConfig);
    }

    @Nonnull
    @Override
    public Telegraf updateTelegraf(@Nonnull final Telegraf telegraf) {

        Arguments.checkNotNull(telegraf, "TelegrafConfig");

        TelegrafRequest telegrafRequest = toTelegrafRequest(telegraf);

        return updateTelegraf(telegraf.getId(), telegrafRequest);
    }

    @Nonnull
    @Override
    public Telegraf updateTelegraf(@Nonnull final String telegrafID,
                                   @Nonnull final TelegrafRequest telegrafRequest) {

        Arguments.checkNotNull(telegrafRequest, "TelegrafRequest");


        Call<Telegraf> telegrafConfigCall = service.putTelegrafsID(telegrafID, telegrafRequest, null);

        return execute(telegrafConfigCall);
    }

    @Override
    public void deleteTelegraf(@Nonnull final Telegraf telegraf) {

        Arguments.checkNotNull(telegraf, "TelegrafConfig");

        deleteTelegraf(telegraf.getId());
    }

    @Override
    public void deleteTelegraf(@Nonnull final String telegrafID) {

        Arguments.checkNonEmpty(telegrafID, "telegrafConfigID");

        Call<Void> call = service.deleteTelegrafsID(telegrafID, null);
        execute(call);
    }

    @Nonnull
    @Override
    public Telegraf cloneTelegraf(@Nonnull final String clonedName,
                                  @Nonnull final String telegrafConfigID) {

        Arguments.checkNonEmpty(clonedName, "clonedName");
        Arguments.checkNonEmpty(telegrafConfigID, "telegrafConfigID");

        Telegraf telegrafConfig = findTelegrafByID(telegrafConfigID);

        return cloneTelegraf(clonedName, telegrafConfig);
    }

    @Nonnull
    @Override
    public Telegraf cloneTelegraf(@Nonnull final String clonedName,
                                  @Nonnull final Telegraf telegraf) {

        Arguments.checkNonEmpty(clonedName, "clonedName");
        Arguments.checkNotNull(telegraf, "TelegrafConfig");


        TelegrafRequest telegrafRequest = toTelegrafRequest(telegraf);

        Telegraf created = createTelegraf(telegrafRequest);
        created.setName(clonedName);

        getLabels(telegraf).forEach(label -> addLabel(label, created));

        return created;
    }

    @Nonnull
    @Override
    public Telegraf findTelegrafByID(@Nonnull final String telegrafID) {

        Arguments.checkNonEmpty(telegrafID, "TelegrafConfig ID");

        Call<Telegraf> telegrafConfig = service.getTelegrafsIDTelegraf(telegrafID, null, "application/json");

        return execute(telegrafConfig);
    }

    @Nonnull
    @Override
    public List<Telegraf> findTelegrafs() {
        return findTelegrafsByOrgId(null);
    }

    @Nonnull
    @Override
    public List<Telegraf> findTelegrafsByOrg(@Nonnull final Organization organization) {

        Arguments.checkNotNull(organization, "organization");

        return findTelegrafsByOrgId(organization.getId());
    }

    @Nonnull
    @Override
    public List<Telegraf> findTelegrafsByOrgId(@Nullable final String orgID) {

        Call<Telegrafs> configsCall = service.getTelegrafs(orgID, null);

        Telegrafs telegrafConfigs = execute(configsCall);
        LOG.log(Level.FINEST, "findTelegrafs found: {0}", telegrafConfigs);

        return telegrafConfigs.getConfigurations();
    }

    @Nonnull
    @Override
    public String getTOML(@Nonnull final Telegraf telegraf) {

        Arguments.checkNotNull(telegraf, "TelegrafConfig");

        return getTOML(telegraf.getId());
    }

    @Nonnull
    @Override
    public String getTOML(@Nonnull final String telegrafID) {

        Arguments.checkNonEmpty(telegrafID, "TelegrafConfig ID");

        Call<String> telegrafConfig = service
                .getTelegrafsID(telegrafID, null, "application/toml");

        return execute(telegrafConfig);
    }

    @Nonnull
    @Override
    public List<ResourceMember> getMembers(@Nonnull final Telegraf telegraf) {

        Arguments.checkNotNull(telegraf, "TelegrafConfig");

        return getMembers(telegraf.getId());
    }

    @Nonnull
    @Override
    public List<ResourceMember> getMembers(@Nonnull final String telegrafID) {

        Arguments.checkNonEmpty(telegrafID, "TelegrafConfig.ID");

        Call<ResourceMembers> call = service.getTelegrafsIDMembers(telegrafID, null);
        ResourceMembers resourceMembers = execute(call);
        LOG.log(Level.FINEST, "findTelegrafConfigMembers found: {0}", resourceMembers);

        return resourceMembers.getUsers();
    }

    @Nonnull
    @Override
    public ResourceMember addMember(@Nonnull final User member, @Nonnull final Telegraf telegraf) {

        Arguments.checkNotNull(telegraf, "telegrafConfig");
        Arguments.checkNotNull(member, "member");

        return addMember(member.getId(), telegraf.getId());
    }

    @Nonnull
    @Override
    public ResourceMember addMember(@Nonnull final String memberID, @Nonnull final String telegrafID) {

        Arguments.checkNonEmpty(memberID, "Member ID");
        Arguments.checkNonEmpty(telegrafID, "TelegrafConfig.ID");

        AddResourceMemberRequestBody user = new AddResourceMemberRequestBody();
        user.setId(memberID);

        Call<ResourceMember> call = service.postTelegrafsIDMembers(telegrafID, user, null);

        return execute(call);
    }

    @Override
    public void deleteMember(@Nonnull final User member, @Nonnull final Telegraf telegraf) {

        Arguments.checkNotNull(telegraf, "telegrafConfig");
        Arguments.checkNotNull(member, "member");

        deleteMember(member.getId(), telegraf.getId());
    }

    @Override
    public void deleteMember(@Nonnull final String memberID, @Nonnull final String telegrafID) {

        Arguments.checkNonEmpty(memberID, "Member ID");
        Arguments.checkNonEmpty(telegrafID, "TelegrafConfig.ID");

        Call<Void> call = service.deleteTelegrafsIDMembersID(memberID, telegrafID, null);
        execute(call);
    }

    @Nonnull
    @Override
    public List<ResourceOwner> getOwners(@Nonnull final Telegraf telegraf) {

        Arguments.checkNotNull(telegraf, "telegrafConfig");

        return getOwners(telegraf.getId());
    }

    @Nonnull
    @Override
    public List<ResourceOwner> getOwners(@Nonnull final String telegrafID) {

        Arguments.checkNonEmpty(telegrafID, "TelegrafConfig.ID");

        Call<ResourceOwners> call = service.getTelegrafsIDOwners(telegrafID, null);
        ResourceOwners resourceMembers = execute(call);
        LOG.log(Level.FINEST, "findTelegrafConfigOwners found: {0}", resourceMembers);

        return resourceMembers.getUsers();
    }

    @Nonnull
    @Override
    public ResourceOwner addOwner(@Nonnull final User owner, @Nonnull final Telegraf telegraf) {

        Arguments.checkNotNull(telegraf, "telegrafConfig");
        Arguments.checkNotNull(owner, "owner");

        return addOwner(owner.getId(), telegraf.getId());
    }

    //TODO add to all API the methods with ResourceOwner, ResourceMember parameter

    @Nonnull
    @Override
    public ResourceOwner addOwner(@Nonnull final String ownerID, @Nonnull final String telegrafID) {

        Arguments.checkNonEmpty(ownerID, "Owner ID");
        Arguments.checkNonEmpty(telegrafID, "TelegrafConfig.ID");

        AddResourceMemberRequestBody user = new AddResourceMemberRequestBody();
        user.setId(ownerID);

        Call<ResourceOwner> call = service.postTelegrafsIDOwners(telegrafID, user, null);

        return execute(call);
    }

    @Override
    public void deleteOwner(@Nonnull final User owner, @Nonnull final Telegraf telegraf) {

        Arguments.checkNotNull(telegraf, "telegrafConfig");
        Arguments.checkNotNull(owner, "owner");

        deleteOwner(owner.getId(), telegraf.getId());
    }

    @Override
    public void deleteOwner(@Nonnull final String ownerID, @Nonnull final String telegrafID) {

        Arguments.checkNonEmpty(ownerID, "Owner ID");
        Arguments.checkNonEmpty(telegrafID, "TelegrafConfig.ID");

        Call<Void> call = service.deleteTelegrafsIDOwnersID(ownerID, telegrafID, null);
        execute(call);
    }

    @Nonnull
    @Override
    public List<Label> getLabels(@Nonnull final Telegraf telegraf) {

        Arguments.checkNotNull(telegraf, "telegrafConfig");

        return getLabels(telegraf.getId());
    }

    @Nonnull
    @Override
    public List<Label> getLabels(@Nonnull final String telegrafID) {

        Arguments.checkNonEmpty(telegrafID, "TelegrafConfig.ID");

        Call<LabelsResponse> call = service.getTelegrafsIDLabels(telegrafID, null);

        return execute(call).getLabels();
    }

    @Nonnull
    @Override
    public LabelResponse addLabel(@Nonnull final Label label, @Nonnull final Telegraf telegraf) {

        Arguments.checkNotNull(label, "label");
        Arguments.checkNotNull(telegraf, "telegrafConfig");

        return addLabel(label.getId(), telegraf.getId());
    }

    @Nonnull
    @Override
    public LabelResponse addLabel(@Nonnull final String labelID, @Nonnull final String telegrafID) {

        Arguments.checkNonEmpty(labelID, "labelID");
        Arguments.checkNonEmpty(telegrafID, "telegrafConfigID");

        LabelMapping labelMapping = new LabelMapping();
        labelMapping.setLabelID(labelID);

        Call<LabelResponse> call = service.postTelegrafsIDLabels(telegrafID, labelMapping, null);

        return execute(call);
    }

    @Override
    public void deleteLabel(@Nonnull final Label label, @Nonnull final Telegraf telegraf) {

        Arguments.checkNotNull(label, "label");
        Arguments.checkNotNull(telegraf, "telegrafConfig");

        deleteLabel(label.getId(), telegraf.getId());
    }

    @Override
    public void deleteLabel(@Nonnull final String labelID, @Nonnull final String telegrafID) {

        Arguments.checkNonEmpty(labelID, "labelID");
        Arguments.checkNonEmpty(telegrafID, "telegrafConfigID");

        Call<Void> call = service.deleteTelegrafsIDLabelsID(telegrafID, labelID, null);
        execute(call);
    }

    @Nonnull
    private TelegrafRequest toTelegrafRequest(@Nonnull final Telegraf telegraf) {

        Arguments.checkNotNull(telegraf, "telegraf");

        TelegrafRequest telegrafRequest = new TelegrafRequest();
        telegrafRequest.setName(telegraf.getName());
        telegrafRequest.setDescription(telegraf.getDescription());
        telegrafRequest.setAgent(telegraf.getAgent());
        telegrafRequest.setOrgID(telegraf.getOrgID());
        if (telegraf.getPlugins() != null) {
            telegraf.getPlugins().forEach(telegrafPlugin -> {
                TelegrafRequestPlugin<Object, Object> requestPlugin = new TelegrafRequestPlugin<>();
                requestPlugin.setType(telegrafPlugin.getType());
                requestPlugin.setName(telegrafPlugin.getName());
                requestPlugin.setConfig(telegrafPlugin.getConfig());

                telegrafRequest.addPluginsItem(requestPlugin);
            });
        }

        return telegrafRequest;
    }
}