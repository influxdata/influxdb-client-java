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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.influxdata.Arguments;
import org.influxdata.client.TelegrafsApi;
import org.influxdata.client.domain.AddResourceMemberRequestBody;
import org.influxdata.client.domain.Label;
import org.influxdata.client.domain.Organization;
import org.influxdata.client.domain.ResourceMember;
import org.influxdata.client.domain.ResourceMembers;
import org.influxdata.client.domain.ResourceOwner;
import org.influxdata.client.domain.ResourceOwners;
import org.influxdata.client.domain.Telegraf;
import org.influxdata.client.domain.TelegrafRequest;
import org.influxdata.client.domain.TelegrafRequestAgent;
import org.influxdata.client.domain.TelegrafRequestPlugin;
import org.influxdata.client.domain.Telegrafs;
import org.influxdata.client.domain.User;
import org.influxdata.exceptions.InfluxException;
import org.influxdata.exceptions.NotFoundException;

import com.google.gson.Gson;
import okhttp3.ResponseBody;
import retrofit2.Call;

/**
 * @author Jakub Bednar (bednar@github) (28/02/2019 10:25)
 */
final class TelegrafsApiImpl extends AbstractInfluxDBRestClient implements TelegrafsApi {

    private static final Logger LOG = Logger.getLogger(TelegrafsApiImpl.class.getName());

    TelegrafsApiImpl(@Nonnull final InfluxDBService influxDBService, @Nonnull final Gson gson) {

        super(influxDBService, gson);
    }

    @Nonnull
    @Override
    public Telegraf createTelegrafConfig(@Nonnull final TelegrafRequest telegrafRequest) {

        Arguments.checkNotNull(telegrafRequest, "telegrafRequest");

        String json = gson.toJson(telegrafRequest);

        Call<Telegraf> call = influxDBService.createTelegrafConfig(createBody(json));

        return execute(call);
    }

    @Nonnull
    @Override
    public Telegraf createTelegrafConfig(@Nonnull final String name,
                                         @Nullable final String description,
                                         @Nonnull final String orgID,
                                         @Nonnull final Integer collectionInterval,
                                         @Nonnull final TelegrafRequestPlugin... plugins) {

        Arguments.checkNonEmpty(name, "TelegrafConfig.name");
        Arguments.checkNonEmpty(orgID, "TelegrafConfig.orgID");
        Arguments.checkPositiveNumber(collectionInterval, "TelegrafConfig.collectionInterval");

        return createTelegrafConfig(name, description, orgID, collectionInterval, Arrays.asList(plugins));
    }

    @Nonnull
    @Override
    public Telegraf createTelegrafConfig(@Nonnull final String name,
                                         @Nullable final String description,
                                         @Nonnull final Organization org,
                                         @Nonnull final Integer collectionInterval,
                                         @Nonnull final TelegrafRequestPlugin... plugins) {

        Arguments.checkNonEmpty(name, "TelegrafConfig.name");
        Arguments.checkNotNull(org, "TelegrafConfig.org");
        Arguments.checkPositiveNumber(collectionInterval, "TelegrafConfig.collectionInterval");

        return createTelegrafConfig(name, description, org.getId(), collectionInterval, plugins);
    }

    @Nonnull
    @Override
    public Telegraf createTelegrafConfig(@Nonnull final String name,
                                         @Nullable final String description,
                                         @Nonnull final Organization org,
                                         @Nonnull final Integer collectionInterval,
                                         @Nonnull final List<TelegrafRequestPlugin> plugins) {

        Arguments.checkNonEmpty(name, "TelegrafConfig.name");
        Arguments.checkNotNull(org, "TelegrafConfig.org");
        Arguments.checkPositiveNumber(collectionInterval, "TelegrafConfig.collectionInterval");

        return createTelegrafConfig(name, description, org.getId(), collectionInterval, plugins);
    }

    @Nonnull
    @Override
    public Telegraf createTelegrafConfig(@Nonnull final String name,
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
        telegrafConfig.setOrganizationID(orgID);
        telegrafConfig.setAgent(telegrafAgent);
        telegrafConfig.plugins(plugins);

        return createTelegrafConfig(telegrafConfig);
    }

    @Nonnull
    @Override
    public Telegraf updateTelegrafConfig(@Nonnull final Telegraf telegrafConfig) {

        Arguments.checkNotNull(telegrafConfig, "TelegrafConfig");

        Call<Telegraf> telegrafConfigCall = influxDBService
                .updateTelegrafConfig(telegrafConfig.getId(), createBody(gson.toJson(telegrafConfig)));

        return execute(telegrafConfigCall);
    }

    @Nonnull
    @Override
    public Telegraf updateTelegrafConfig(@Nonnull final String telegrafID,
                                         @Nonnull final TelegrafRequest telegrafRequest) {

        Arguments.checkNotNull(telegrafRequest, "TelegrafRequest");

        String json = gson.toJson(telegrafRequest);

        Call<Telegraf> telegrafConfigCall = influxDBService
                .updateTelegrafConfig(telegrafID, createBody(json));

        return execute(telegrafConfigCall);
    }

    @Override
    public void deleteTelegrafConfig(@Nonnull final Telegraf telegrafConfig) {

        Arguments.checkNotNull(telegrafConfig, "TelegrafConfig");

        deleteTelegrafConfig(telegrafConfig.getId());
    }

    @Override
    public void deleteTelegrafConfig(@Nonnull final String telegrafConfigID) {

        Arguments.checkNonEmpty(telegrafConfigID, "telegrafConfigID");

        Call<Void> call = influxDBService.deleteTelegrafConfig(telegrafConfigID);
        execute(call);
    }

    @Nonnull
    @Override
    public Telegraf cloneTelegrafConfig(@Nonnull final String clonedName,
                                        @Nonnull final String telegrafConfigID) {

        Arguments.checkNonEmpty(clonedName, "clonedName");
        Arguments.checkNonEmpty(telegrafConfigID, "telegrafConfigID");

        Telegraf telegrafConfig = findTelegrafConfigByID(telegrafConfigID);
        if (telegrafConfig == null) {
            throw new IllegalStateException("NotFound Telegraf with ID: " + telegrafConfigID);
        }

        return cloneTelegrafConfig(clonedName, telegrafConfig);
    }

    @Nonnull
    @Override
    public Telegraf cloneTelegrafConfig(@Nonnull final String clonedName,
                                        @Nonnull final Telegraf telegrafConfig) {

        Arguments.checkNonEmpty(clonedName, "clonedName");
        Arguments.checkNotNull(telegrafConfig, "TelegrafConfig");

        Telegraf cloned = new Telegraf();
        cloned.setName(clonedName);
        cloned.setOrganizationID(telegrafConfig.getOrganizationID());
        cloned.setDescription(telegrafConfig.getDescription());
        cloned.setAgent(new TelegrafRequestAgent());
        cloned.getAgent().setCollectionInterval(telegrafConfig.getAgent().getCollectionInterval());
        cloned.getPlugins().addAll(telegrafConfig.getPlugins());

        Call<Telegraf> call = influxDBService.createTelegrafConfig(createBody(gson.toJson(cloned)));
        Telegraf created = execute(call);

        getLabels(telegrafConfig).forEach(label -> addLabel(label, created));

        return created;
    }

    @Nullable
    @Override
    public Telegraf findTelegrafConfigByID(@Nonnull final String telegrafConfigID) {

        Arguments.checkNonEmpty(telegrafConfigID, "TelegrafConfig ID");

        Call<Telegraf> telegrafConfig = influxDBService.findTelegrafConfigByID(telegrafConfigID);

        return execute(telegrafConfig, NotFoundException.class);
    }

    @Nonnull
    @Override
    public List<Telegraf> findTelegrafConfigs() {
        return findTelegrafConfigsByOrgId(null);
    }

    @Nonnull
    @Override
    public List<Telegraf> findTelegrafConfigsByOrg(@Nonnull final Organization organization) {

        Arguments.checkNotNull(organization, "organization");

        return findTelegrafConfigsByOrgId(organization.getId());
    }

    @Nonnull
    @Override
    public List<Telegraf> findTelegrafConfigsByOrgId(@Nullable final String orgID) {

        Call<Telegrafs> configsCall = influxDBService.findTelegrafConfigs(orgID);

        Telegrafs telegrafConfigs = execute(configsCall);
        LOG.log(Level.FINEST, "findTelegrafConfigs found: {0}", telegrafConfigs);

        return telegrafConfigs.getConfigurations();
    }

    @Nonnull
    @Override
    public String getTOML(@Nonnull final Telegraf telegrafConfig) {

        Arguments.checkNotNull(telegrafConfig, "TelegrafConfig");

        return getTOML(telegrafConfig.getId());
    }

    @Nonnull
    @Override
    public String getTOML(@Nonnull final String telegrafConfigID) {

        Arguments.checkNonEmpty(telegrafConfigID, "TelegrafConfig ID");

        Call<ResponseBody> telegrafConfig = influxDBService.findTelegrafConfigByIDInTOML(telegrafConfigID);

        try {
            return execute(telegrafConfig).string();
        } catch (IOException e) {
            throw new InfluxException(e);
        }
    }

    @Nonnull
    @Override
    public List<ResourceMember> getMembers(@Nonnull final Telegraf telegrafConfig) {

        Arguments.checkNotNull(telegrafConfig, "TelegrafConfig");

        return getMembers(telegrafConfig.getId());
    }

    @Nonnull
    @Override
    public List<ResourceMember> getMembers(@Nonnull final String telegrafConfigID) {

        Arguments.checkNonEmpty(telegrafConfigID, "TelegrafConfig.ID");

        Call<ResourceMembers> call = influxDBService.findTelegrafConfigMembers(telegrafConfigID);
        ResourceMembers resourceMembers = execute(call);
        LOG.log(Level.FINEST, "findTelegrafConfigMembers found: {0}", resourceMembers);

        return resourceMembers.getUsers();
    }

    @Nonnull
    @Override
    public ResourceMember addMember(@Nonnull final User member, @Nonnull final Telegraf telegrafConfig) {

        Arguments.checkNotNull(telegrafConfig, "telegrafConfig");
        Arguments.checkNotNull(member, "member");

        return addMember(member.getId(), telegrafConfig.getId());
    }

    @Nonnull
    @Override
    public ResourceMember addMember(@Nonnull final String memberID, @Nonnull final String telegrafConfigID) {

        Arguments.checkNonEmpty(memberID, "Member ID");
        Arguments.checkNonEmpty(telegrafConfigID, "TelegrafConfig.ID");

        AddResourceMemberRequestBody user = new AddResourceMemberRequestBody();
        user.setId(memberID);

        String json = gson.toJson(user);
        Call<ResourceMember> call = influxDBService.addTelegrafConfigMember(telegrafConfigID, createBody(json));

        return execute(call);
    }

    @Override
    public void deleteMember(@Nonnull final User member, @Nonnull final Telegraf telegrafConfig) {

        Arguments.checkNotNull(telegrafConfig, "telegrafConfig");
        Arguments.checkNotNull(member, "member");

        deleteMember(member.getId(), telegrafConfig.getId());
    }

    @Override
    public void deleteMember(@Nonnull final String memberID, @Nonnull final String telegrafConfigID) {

        Arguments.checkNonEmpty(memberID, "Member ID");
        Arguments.checkNonEmpty(telegrafConfigID, "TelegrafConfig.ID");

        Call<Void> call = influxDBService.deleteTelegrafConfigMember(telegrafConfigID, memberID);
        execute(call);
    }

    @Nonnull
    @Override
    public List<ResourceOwner> getOwners(@Nonnull final Telegraf telegrafConfig) {

        Arguments.checkNotNull(telegrafConfig, "telegrafConfig");

        return getOwners(telegrafConfig.getId());
    }

    @Nonnull
    @Override
    public List<ResourceOwner> getOwners(@Nonnull final String telegrafConfigID) {

        Arguments.checkNonEmpty(telegrafConfigID, "TelegrafConfig.ID");

        Call<ResourceOwners> call = influxDBService.findTelegrafConfigOwners(telegrafConfigID);
        ResourceOwners resourceMembers = execute(call);
        LOG.log(Level.FINEST, "findTelegrafConfigOwners found: {0}", resourceMembers);

        return resourceMembers.getUsers();
    }

    @Nonnull
    @Override
    public ResourceOwner addOwner(@Nonnull final User owner, @Nonnull final Telegraf telegrafConfig) {

        Arguments.checkNotNull(telegrafConfig, "telegrafConfig");
        Arguments.checkNotNull(owner, "owner");

        return addOwner(owner.getId(), telegrafConfig.getId());
    }

    //TODO add to all API the methods with ResourceOwner, ResourceMember parameter

    @Nonnull
    @Override
    public ResourceOwner addOwner(@Nonnull final String ownerID, @Nonnull final String telegrafConfigID) {

        Arguments.checkNonEmpty(ownerID, "Owner ID");
        Arguments.checkNonEmpty(telegrafConfigID, "TelegrafConfig.ID");

        AddResourceMemberRequestBody user = new AddResourceMemberRequestBody();
        user.setId(ownerID);

        String json = gson.toJson(user);
        Call<ResourceOwner> call = influxDBService.addTelegrafConfigOwner(telegrafConfigID, createBody(json));

        return execute(call);
    }

    @Override
    public void deleteOwner(@Nonnull final User owner, @Nonnull final Telegraf telegrafConfig) {

        Arguments.checkNotNull(telegrafConfig, "telegrafConfig");
        Arguments.checkNotNull(owner, "owner");

        deleteOwner(owner.getId(), telegrafConfig.getId());
    }

    @Override
    public void deleteOwner(@Nonnull final String ownerID, @Nonnull final String telegrafConfigID) {

        Arguments.checkNonEmpty(ownerID, "Owner ID");
        Arguments.checkNonEmpty(telegrafConfigID, "TelegrafConfig.ID");

        Call<Void> call = influxDBService.deleteTelegrafConfigOwner(telegrafConfigID, ownerID);
        execute(call);
    }

    @Nonnull
    @Override
    public List<Label> getLabels(@Nonnull final Telegraf telegrafConfig) {

        Arguments.checkNotNull(telegrafConfig, "telegrafConfig");

        return getLabels(telegrafConfig.getId());
    }

    @Nonnull
    @Override
    public List<Label> getLabels(@Nonnull final String telegrafConfigID) {

        Arguments.checkNonEmpty(telegrafConfigID, "TelegrafConfig.ID");

        return getLabels(telegrafConfigID, "telegrafs");
    }

    @Nonnull
    @Override
    public Label addLabel(@Nonnull final Label label, @Nonnull final Telegraf telegrafConfig) {

        Arguments.checkNotNull(label, "label");
        Arguments.checkNotNull(telegrafConfig, "telegrafConfig");

        return addLabel(label.getId(), telegrafConfig.getId());
    }

    @Nonnull
    @Override
    public Label addLabel(@Nonnull final String labelID, @Nonnull final String telegrafConfigID) {

        Arguments.checkNonEmpty(labelID, "labelID");
        Arguments.checkNonEmpty(telegrafConfigID, "telegrafConfigID");

        return addLabel(labelID, telegrafConfigID, "telegrafs");
    }

    @Override
    public void deleteLabel(@Nonnull final Label label, @Nonnull final Telegraf telegrafConfig) {

        Arguments.checkNotNull(label, "label");
        Arguments.checkNotNull(telegrafConfig, "telegrafConfig");

        deleteLabel(label.getId(), telegrafConfig.getId());
    }

    @Override
    public void deleteLabel(@Nonnull final String labelID, @Nonnull final String telegrafConfigID) {

        Arguments.checkNonEmpty(labelID, "labelID");
        Arguments.checkNonEmpty(telegrafConfigID, "telegrafConfigID");

        deleteLabel(labelID, telegrafConfigID, "telegrafs");
    }
}