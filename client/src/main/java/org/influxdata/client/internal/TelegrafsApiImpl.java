package org.influxdata.client.internal;

import java.util.Arrays;
import java.util.Collection;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.influxdata.Arguments;
import org.influxdata.client.TelegrafsApi;
import org.influxdata.client.domain.Organization;
import org.influxdata.client.domain.TelegrafAgent;
import org.influxdata.client.domain.TelegrafConfig;
import org.influxdata.client.domain.TelegrafPlugin;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import retrofit2.Call;

/**
 * @author Jakub Bednar (bednar@github) (28/02/2019 10:25)
 */
final class TelegrafsApiImpl extends AbstractInfluxDBRestClient implements TelegrafsApi {

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
}