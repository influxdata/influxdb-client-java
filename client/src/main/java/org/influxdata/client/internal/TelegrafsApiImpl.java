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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.influxdata.Arguments;
import org.influxdata.client.TelegrafsApi;
import org.influxdata.client.domain.Organization;
import org.influxdata.client.domain.TelegrafAgent;
import org.influxdata.client.domain.TelegrafConfig;
import org.influxdata.client.domain.TelegrafConfigs;
import org.influxdata.client.domain.TelegrafPlugin;
import org.influxdata.exceptions.NotFoundException;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import retrofit2.Call;

/**
 * @author Jakub Bednar (bednar@github) (28/02/2019 10:25)
 */
final class TelegrafsApiImpl extends AbstractInfluxDBRestClient implements TelegrafsApi {

    private static final Logger LOG = Logger.getLogger(TelegrafsApiImpl.class.getName());

    private final JsonAdapter<TelegrafConfig> adapter;

    TelegrafsApiImpl(@Nonnull final InfluxDBService influxDBService, @Nonnull final Moshi moshi) {

        super(influxDBService, moshi);

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

    @Nullable
    @Override
    public TelegrafConfig findTelegrafConfigByID(@Nonnull final String telegrafConfigID) {

        Arguments.checkNonEmpty(telegrafConfigID, "TelegrafConfig ID");

        Call<TelegrafConfig> telegrafConfig = influxDBService.findTelegrafConfigByID(telegrafConfigID, "application/json");

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
    public List<TelegrafConfig> findTelegrafConfigsByOrgId(@Nullable final String orgId) {

        Call<TelegrafConfigs> configsCall = influxDBService.findTelegrafConfigs(orgId);

        TelegrafConfigs telegrafConfigs = execute(configsCall);
        LOG.log(Level.FINEST, "findTelegrafConfigs found: {0}", telegrafConfigs);
        
        return telegrafConfigs.getConfigs();
    }
}