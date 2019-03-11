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
import java.util.Collection;
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
import org.influxdata.client.domain.TelegrafAgent;
import org.influxdata.client.domain.TelegrafConfig;
import org.influxdata.client.domain.TelegrafConfigs;
import org.influxdata.client.domain.TelegrafPlugin;
import org.influxdata.client.domain.User;
import org.influxdata.exceptions.InfluxException;
import org.influxdata.exceptions.NotFoundException;

import com.google.gson.Gson;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import okhttp3.ResponseBody;
import retrofit2.Call;

/**
 * @author Jakub Bednar (bednar@github) (28/02/2019 10:25)
 */
final class TelegrafsApiImpl extends AbstractInfluxDBRestClient implements TelegrafsApi {

    private static final Logger LOG = Logger.getLogger(TelegrafsApiImpl.class.getName());

    private final JsonAdapter<TelegrafConfig> adapter;

    TelegrafsApiImpl(@Nonnull final InfluxDBService influxDBService, @Nonnull final Moshi moshi,
                     @Nonnull final Gson gson) {

        super(influxDBService, gson);

        this.adapter = moshi.adapter(TelegrafConfig.class);
    }

    @Nonnull
    @Override
    public TelegrafConfig createTelegrafConfig(@Nonnull final TelegrafConfig telegrafConfig) {

        Arguments.checkNotNull(telegrafConfig, "telegrafConfig");

        String json = adapter.toJson(telegrafConfig);

        Call<TelegrafConfig> call = influxDBService.createTelegrafConfig(createBody(json));

        return execute(call);
    }

    @Nonnull
    @Override
    public TelegrafConfig createTelegrafConfig(@Nonnull final String name,
                                               @Nullable final String description,
                                               @Nonnull final String orgID,
                                               @Nonnull final Integer collectionInterval,
                                               @Nonnull final TelegrafPlugin... plugins) {

        Arguments.checkNonEmpty(name, "TelegrafConfig.name");
        Arguments.checkNonEmpty(orgID, "TelegrafConfig.orgID");
        Arguments.checkPositiveNumber(collectionInterval, "TelegrafConfig.collectionInterval");

        return createTelegrafConfig(name, description, orgID, collectionInterval, Arrays.asList(plugins));
    }

    @Nonnull
    @Override
    public TelegrafConfig createTelegrafConfig(@Nonnull final String name,
                                               @Nullable final String description,
                                               @Nonnull final Organization org,
                                               @Nonnull final Integer collectionInterval,
                                               @Nonnull final TelegrafPlugin... plugins) {

        Arguments.checkNonEmpty(name, "TelegrafConfig.name");
        Arguments.checkNotNull(org, "TelegrafConfig.org");
        Arguments.checkPositiveNumber(collectionInterval, "TelegrafConfig.collectionInterval");

        return createTelegrafConfig(name, description, org.getId(), collectionInterval, plugins);
    }

    @Nonnull
    @Override
    public TelegrafConfig createTelegrafConfig(@Nonnull final String name,
                                               @Nullable final String description,
                                               @Nonnull final Organization org,
                                               @Nonnull final Integer collectionInterval,
                                               @Nonnull final Collection<TelegrafPlugin> plugins) {

        Arguments.checkNonEmpty(name, "TelegrafConfig.name");
        Arguments.checkNotNull(org, "TelegrafConfig.org");
        Arguments.checkPositiveNumber(collectionInterval, "TelegrafConfig.collectionInterval");

        return createTelegrafConfig(name, description, org.getId(), collectionInterval, plugins);
    }

    @Nonnull
    @Override
    public TelegrafConfig createTelegrafConfig(@Nonnull final String name,
                                               @Nullable final String description,
                                               @Nonnull final String orgID,
                                               @Nonnull final Integer collectionInterval,
                                               @Nonnull final Collection<TelegrafPlugin> plugins) {

        Arguments.checkNonEmpty(name, "TelegrafConfig.name");
        Arguments.checkNonEmpty(orgID, "TelegrafConfig.orgID");
        Arguments.checkPositiveNumber(collectionInterval, "TelegrafConfig.collectionInterval");

        TelegrafAgent telegrafAgent = new TelegrafAgent();
        telegrafAgent.setCollectionInterval(collectionInterval);

        TelegrafConfig telegrafConfig = new TelegrafConfig();
        telegrafConfig.setName(name);
        telegrafConfig.setDescription(description);
        telegrafConfig.setOrgID(orgID);
        telegrafConfig.setAgent(telegrafAgent);
        telegrafConfig.getPlugins().addAll(plugins);

        return createTelegrafConfig(telegrafConfig);
    }

    @Nonnull
    @Override
    public TelegrafConfig updateTelegrafConfig(@Nonnull final TelegrafConfig telegrafConfig) {

        Arguments.checkNotNull(telegrafConfig, "TelegrafConfig");

        String json = adapter.toJson(telegrafConfig);

        Call<TelegrafConfig> telegrafConfigCall = influxDBService
                .updateTelegrafConfig(telegrafConfig.getId(), createBody(json));

        return execute(telegrafConfigCall);
    }

    @Override
    public void deleteTelegrafConfig(@Nonnull final TelegrafConfig telegrafConfig) {

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
    public TelegrafConfig cloneTelegrafConfig(@Nonnull final String clonedName,
                                              @Nonnull final String telegrafConfigID) {

        Arguments.checkNonEmpty(clonedName, "clonedName");
        Arguments.checkNonEmpty(telegrafConfigID, "telegrafConfigID");

        TelegrafConfig telegrafConfig = findTelegrafConfigByID(telegrafConfigID);
        if (telegrafConfig == null) {
            throw new IllegalStateException("NotFound TelegrafConfig with ID: " + telegrafConfigID);
        }

        return cloneTelegrafConfig(clonedName, telegrafConfig);
    }

    @Nonnull
    @Override
    public TelegrafConfig cloneTelegrafConfig(@Nonnull final String clonedName,
                                              @Nonnull final TelegrafConfig telegrafConfig) {

        Arguments.checkNonEmpty(clonedName, "clonedName");
        Arguments.checkNotNull(telegrafConfig, "TelegrafConfig");

        TelegrafConfig cloned = new TelegrafConfig();
        cloned.setName(clonedName);
        cloned.setOrgID(telegrafConfig.getOrgID());
        cloned.setDescription(telegrafConfig.getDescription());
        cloned.setAgent(new TelegrafAgent());
        cloned.getAgent().setCollectionInterval(telegrafConfig.getAgent().getCollectionInterval());
        cloned.getPlugins().addAll(telegrafConfig.getPlugins());

        TelegrafConfig created = createTelegrafConfig(cloned);

        getLabels(telegrafConfig).forEach(label -> addLabel(label, created));

        return created;
    }

    @Nullable
    @Override
    public TelegrafConfig findTelegrafConfigByID(@Nonnull final String telegrafConfigID) {

        Arguments.checkNonEmpty(telegrafConfigID, "TelegrafConfig ID");

        Call<TelegrafConfig> telegrafConfig = influxDBService.findTelegrafConfigByID(telegrafConfigID);

        return execute(telegrafConfig, NotFoundException.class);
    }

    @Nonnull
    @Override
    public List<TelegrafConfig> findTelegrafConfigs() {
        return findTelegrafConfigsByOrgId(null);
    }

    @Nonnull
    @Override
    public List<TelegrafConfig> findTelegrafConfigsByOrg(@Nonnull final Organization organization) {

        Arguments.checkNotNull(organization, "organization");

        return findTelegrafConfigsByOrgId(organization.getId());
    }

    @Nonnull
    @Override
    public List<TelegrafConfig> findTelegrafConfigsByOrgId(@Nullable final String orgID) {

        Call<TelegrafConfigs> configsCall = influxDBService.findTelegrafConfigs(orgID);

        TelegrafConfigs telegrafConfigs = execute(configsCall);
        LOG.log(Level.FINEST, "findTelegrafConfigs found: {0}", telegrafConfigs);

        return telegrafConfigs.getConfigs();
    }

    @Nonnull
    @Override
    public String getTOML(@Nonnull final TelegrafConfig telegrafConfig) {

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
    public List<ResourceMember> getMembers(@Nonnull final TelegrafConfig telegrafConfig) {

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
    public ResourceMember addMember(@Nonnull final User member, @Nonnull final TelegrafConfig telegrafConfig) {

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
    public void deleteMember(@Nonnull final User member, @Nonnull final TelegrafConfig telegrafConfig) {

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
    public List<ResourceOwner> getOwners(@Nonnull final TelegrafConfig telegrafConfig) {

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
    public ResourceOwner addOwner(@Nonnull final User owner, @Nonnull final TelegrafConfig telegrafConfig) {

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
    public void deleteOwner(@Nonnull final User owner, @Nonnull final TelegrafConfig telegrafConfig) {

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
    public List<Label> getLabels(@Nonnull final TelegrafConfig telegrafConfig) {

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
    public Label addLabel(@Nonnull final Label label, @Nonnull final TelegrafConfig telegrafConfig) {

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
    public void deleteLabel(@Nonnull final Label label, @Nonnull final TelegrafConfig telegrafConfig) {

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