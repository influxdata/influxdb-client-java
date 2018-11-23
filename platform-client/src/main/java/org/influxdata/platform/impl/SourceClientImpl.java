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
package org.influxdata.platform.impl;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.influxdata.platform.Arguments;
import org.influxdata.platform.SourceClient;
import org.influxdata.platform.domain.Bucket;
import org.influxdata.platform.domain.Health;
import org.influxdata.platform.domain.Source;
import org.influxdata.platform.domain.Sources;
import org.influxdata.platform.error.InfluxException;
import org.influxdata.platform.rest.AbstractRestClient;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import okhttp3.ResponseBody;
import retrofit2.Call;

/**
 * @author Jakub Bednar (bednar@github) (18/09/2018 09:40)
 */
final class SourceClientImpl extends AbstractRestClient implements SourceClient {

    private static final Logger LOG = Logger.getLogger(SourceClientImpl.class.getName());

    private final PlatformService platformService;
    private final JsonAdapter<Source> adapter;

    SourceClientImpl(@Nonnull final PlatformService platformService, @Nonnull final Moshi moshi) {

        Arguments.checkNotNull(platformService, "PlatformService");
        Arguments.checkNotNull(moshi, "Moshi to create adapter");

        this.platformService = platformService;
        this.adapter = moshi.adapter(Source.class);
    }

    @Nonnull
    @Override
    public Source createSource(@Nonnull final Source source) {

        Arguments.checkNotNull(source, "Source is required");

        String json = adapter.toJson(source);

        Call<Source> call = platformService.createSource(createBody(json));
        return execute(call);
    }

    @Nonnull
    @Override
    public Source updateSource(@Nonnull final Source source) {

        Arguments.checkNotNull(source, "Source is required");

        String json = adapter.toJson(source);

        Call<Source> call = platformService.updateSource(source.getId(), createBody(json));
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

        Call<Void> call = platformService.deleteSource(sourceID);
        execute(call);

    }

    @Nullable
    @Override
    public Source findSourceByID(@Nonnull final String sourceID) {

        Arguments.checkNonEmpty(sourceID, "sourceID");

        Call<Source> call = platformService.findSource(sourceID);

        return execute(call, "source not found");
    }

    @Nonnull
    @Override
    public List<Source> findSources() {
        Call<Sources> sourcesCall = platformService.findSources();

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

        Call<List<Bucket>> call = platformService.findSourceBuckets(sourceID);

        return execute(call, "source not found");
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

        Health health = new Health();

        Call<ResponseBody> call = platformService.findSourceHealth(sourceID);
        try {
            execute(call);
            health.setStatus(Health.HEALTHY_STATUS);

            //TODO wait for implementation of handleGetSourceHealth failure,
            // after fix refactor to platformClient.health implementation
        } catch (InfluxException e) {
            health.setStatus("error");
            health.setMessage(e.getMessage());
        }

        return health;
    }
}