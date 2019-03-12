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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.influxdata.Arguments;
import org.influxdata.client.SourcesApi;
import org.influxdata.client.domain.Bucket;
import org.influxdata.client.domain.Check;
import org.influxdata.client.domain.Source;
import org.influxdata.client.domain.Sources;
import org.influxdata.exceptions.NotFoundException;

import com.google.gson.Gson;
import retrofit2.Call;

/**
 * @author Jakub Bednar (bednar@github) (18/09/2018 09:40)
 */
final class SourcesApiImpl extends AbstractInfluxDBRestClient implements SourcesApi {

    private static final Logger LOG = Logger.getLogger(SourcesApiImpl.class.getName());

    private final InfluxDBClientImpl influxDBClient;

    SourcesApiImpl(@Nonnull final InfluxDBService influxDBService,
                   @Nonnull final InfluxDBClientImpl influxDBClient,
                   @Nonnull final Gson gson) {

        super(influxDBService, gson);

        Arguments.checkNotNull(influxDBClient, "InfluxDBClient");

        this.influxDBClient = influxDBClient;
    }

    @Nonnull
    @Override
    public Source createSource(@Nonnull final Source source) {

        Arguments.checkNotNull(source, "Source is required");

        String json = gson.toJson(source);

        Call<Source> call = influxDBService.createSource(createBody(json));
        return execute(call);
    }

    @Nonnull
    @Override
    public Source updateSource(@Nonnull final Source source) {

        Arguments.checkNotNull(source, "Source is required");

        String json = gson.toJson(source);

        Call<Source> call = influxDBService.updateSource(source.getId(), createBody(json));
        return execute(call);
    }

    @Override
    public void deleteSource(@Nonnull final Source source) {

        Arguments.checkNotNull(source, "Source is required");

        deleteSource(source.getId());
    }

    @Override
    public void deleteSource(@Nonnull final String sourceID) {

        Arguments.checkNonEmpty(sourceID, "sourceID");

        Call<Void> call = influxDBService.deleteSource(sourceID);
        execute(call);
    }

    @Nonnull
    @Override
    public Source cloneSource(@Nonnull final String clonedName, @Nonnull final String sourceID) {

        Arguments.checkNonEmpty(clonedName, "clonedName");
        Arguments.checkNonEmpty(sourceID, "sourceID");

        Source source = findSourceByID(sourceID);
        if (source == null) {
            throw new IllegalStateException("NotFound Source with ID: " + sourceID);
        }

        return cloneSource(clonedName, source);
    }

    @Nonnull
    @Override
    public Source cloneSource(@Nonnull final String clonedName, @Nonnull final Source source) {

        Arguments.checkNonEmpty(clonedName, "clonedName");
        Arguments.checkNotNull(source, "source");

        Source cloned = new Source();
        cloned.setName(clonedName);
        cloned.setOrgID(source.getOrgID());
        cloned.setDefault(source.isDefault());
        cloned.setType(source.getType());
        cloned.setUrl(source.getUrl());
        cloned.setInsecureSkipVerify(source.isInsecureSkipVerify());
        cloned.setTelegraf(source.getTelegraf());
        cloned.setToken(source.getToken());
        cloned.setUsername(source.getUsername());
        cloned.setPassword(source.getPassword());
        cloned.setSharedSecret(source.getSharedSecret());
        cloned.setMetaUrl(source.getMetaUrl());
        cloned.setDefaultRP(source.getDefaultRP());

        return createSource(cloned);
    }

    @Nullable
    @Override
    public Source findSourceByID(@Nonnull final String sourceID) {

        Arguments.checkNonEmpty(sourceID, "sourceID");

        Call<Source> call = influxDBService.findSource(sourceID);

        return execute(call, NotFoundException.class);
    }

    @Nonnull
    @Override
    public List<Source> findSources() {
        Call<Sources> sourcesCall = influxDBService.findSources();

        Sources sources = execute(sourcesCall);
        LOG.log(Level.FINEST, "findSources found: {0}", sources);

        return sources.getSources();
    }

    @Nullable
    @Override
    public List<Bucket> findBucketsBySource(@Nonnull final Source source) {

        Arguments.checkNotNull(source, "Source is required");

        return findBucketsBySourceID(source.getId());
    }

    @Nullable
    @Override
    public List<Bucket> findBucketsBySourceID(@Nonnull final String sourceID) {

        Arguments.checkNonEmpty(sourceID, "sourceID");

        Call<List<Bucket>> call = influxDBService.findSourceBuckets(sourceID);

        return execute(call, NotFoundException.class);
    }

    @Nonnull
    @Override
    public Check health(@Nonnull final Source source) {

        Arguments.checkNotNull(source, "Source is required");

        return health(source.getId());
    }

    @Nonnull
    @Override
    public Check health(@Nonnull final String sourceID) {

        Arguments.checkNonEmpty(sourceID, "sourceID");

        return influxDBClient.health(influxDBService.findSourceHealth(sourceID));
    }
}