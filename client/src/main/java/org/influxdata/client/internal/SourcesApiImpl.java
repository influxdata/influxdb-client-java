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
import org.influxdata.client.domain.Health;
import org.influxdata.client.domain.Source;
import org.influxdata.client.domain.Sources;
import org.influxdata.exceptions.NotFoundException;
import org.influxdata.internal.AbstractRestClient;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import retrofit2.Call;

/**
 * @author Jakub Bednar (bednar@github) (18/09/2018 09:40)
 */
final class SourcesApiImpl extends AbstractRestClient implements SourcesApi {

    private static final Logger LOG = Logger.getLogger(SourcesApiImpl.class.getName());

    private final InfluxDBService influxDBService;
    private final JsonAdapter<Source> adapter;
    private final InfluxDBClientImpl influxDBClient;

    SourcesApiImpl(@Nonnull final InfluxDBService influxDBService,
                   @Nonnull final Moshi moshi,
                   @Nonnull final InfluxDBClientImpl influxDBClient) {

        Arguments.checkNotNull(influxDBService, "InfluxDBService");
        Arguments.checkNotNull(moshi, "Moshi to create adapter");
        Arguments.checkNotNull(influxDBClient, "InfluxDBClient");

        this.influxDBService = influxDBService;
        this.adapter = moshi.adapter(Source.class);
        this.influxDBClient = influxDBClient;
    }

    @Nonnull
    @Override
    public Source createSource(@Nonnull final Source source) {

        Arguments.checkNotNull(source, "Source is required");

        String json = adapter.toJson(source);

        Call<Source> call = influxDBService.createSource(createBody(json));
        return execute(call);
    }

    @Nonnull
    @Override
    public Source updateSource(@Nonnull final Source source) {

        Arguments.checkNotNull(source, "Source is required");

        String json = adapter.toJson(source);

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
    public Health health(@Nonnull final Source source) {

        Arguments.checkNotNull(source, "Source is required");

        return health(source.getId());
    }

    @Nonnull
    @Override
    public Health health(@Nonnull final String sourceID) {

        Arguments.checkNonEmpty(sourceID, "sourceID");

        return influxDBClient.health(influxDBService.findSourceHealth(sourceID));
    }
}