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
import org.influxdata.platform.ScraperClient;
import org.influxdata.platform.domain.ScraperTarget;
import org.influxdata.platform.domain.ScraperTargetResponse;
import org.influxdata.platform.domain.ScraperTargetResponses;
import org.influxdata.platform.domain.ScraperType;
import org.influxdata.platform.error.rest.NotFoundException;
import org.influxdata.platform.rest.AbstractRestClient;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import retrofit2.Call;

/**
 * @author Jakub Bednar (bednar@github) (22/01/2019 08:17)
 */
final class ScraperClientImpl extends AbstractRestClient implements ScraperClient {

    private static final Logger LOG = Logger.getLogger(ScraperClientImpl.class.getName());

    private final PlatformService platformService;
    private final JsonAdapter<ScraperTarget> adapter;

    ScraperClientImpl(@Nonnull final PlatformService platformService, @Nonnull final Moshi moshi) {

        Arguments.checkNotNull(platformService, "PlatformService");
        Arguments.checkNotNull(moshi, "Moshi to create adapter");

        this.platformService = platformService;
        this.adapter = moshi.adapter(ScraperTarget.class);
    }

    @Nonnull
    @Override
    public ScraperTargetResponse createScraperTarget(@Nonnull final ScraperTarget scraperTarget) {

        Arguments.checkNotNull(scraperTarget, "scraperTarget");

        String json = adapter.toJson(scraperTarget);
        Call<ScraperTargetResponse> call = platformService.createScraperTarget(createBody(json));

        return execute(call);
    }

    @Nonnull
    @Override
    public ScraperTargetResponse createScraperTarget(@Nonnull final String name,
                                                     @Nonnull final String url,
                                                     @Nonnull final String bucketID,
                                                     @Nonnull final String orgID) {

        Arguments.checkNonEmpty(name, "name");
        Arguments.checkNonEmpty(url, "url");
        Arguments.checkNonEmpty(bucketID, "bucketID");
        Arguments.checkNonEmpty(orgID, " orgID");

        ScraperTarget scraperTarget = new ScraperTarget();
        scraperTarget.setName(name);
        scraperTarget.setUrl(url);
        scraperTarget.setBucketID(bucketID);
        scraperTarget.setOrgID(orgID);
        scraperTarget.setType(ScraperType.PROMETHEUS);

        return createScraperTarget(scraperTarget);
    }

    @Nonnull
    @Override
    public ScraperTargetResponse updateScraperTarget(@Nonnull final ScraperTarget scraperTarget) {

        Arguments.checkNotNull(scraperTarget, "scraperTarget");

        String json = adapter.toJson(scraperTarget);

        Call<ScraperTargetResponse> call = platformService.updateScraperTarget(scraperTarget.getId(), createBody(json));

        return execute(call);
    }

    @Override
    public void deleteScraperTarget(@Nonnull final ScraperTarget scraperTarget) {

        Arguments.checkNotNull(scraperTarget, "scraperTarget");

        deleteScraperTarget(scraperTarget.getId());
    }

    @Override
    public void deleteScraperTarget(@Nonnull final String scraperTargetID) {

        Arguments.checkNonEmpty(scraperTargetID, "scraperTargetID");

        Call<Void> call = platformService.deleteScraperTarget(scraperTargetID);
        execute(call);
    }

    @Nullable
    @Override
    public ScraperTargetResponse findScraperTargetByID(@Nonnull final String scraperTargetID) {

        Arguments.checkNonEmpty(scraperTargetID, "scraperTargetID");

        Call<ScraperTargetResponse> call = platformService.findScraperTargetByID(scraperTargetID);

        return execute(call, NotFoundException.class);
    }

    @Nonnull
    @Override
    public List<ScraperTargetResponse> findScraperTargets() {

        Call<ScraperTargetResponses> call = platformService.findScraperTargets();

        ScraperTargetResponses responses = execute(call);
        LOG.log(Level.FINEST, "findScraperTargets found: {0}", responses);

        return responses.getTargetResponses();
    }
}