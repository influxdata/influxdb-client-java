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

import org.influxdata.platform.Arguments;
import org.influxdata.platform.domain.Label;
import org.influxdata.platform.domain.LabelMapping;
import org.influxdata.platform.domain.LabelResponse;
import org.influxdata.platform.domain.Labels;
import org.influxdata.platform.domain.ResourceType;
import org.influxdata.platform.rest.AbstractRestClient;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import retrofit2.Call;

/**
 * @author Jakub Bednar (bednar@github) (28/01/2019 10:06)
 */
abstract class AbstractLabelRestClient extends AbstractRestClient {

    private static final Logger LOG = Logger.getLogger(AbstractLabelRestClient.class.getName());

    protected final PlatformService platformService;
    private final JsonAdapter<LabelMapping> labelMappingAdapter;

    AbstractLabelRestClient(@Nonnull final PlatformService platformService,
                            @Nonnull final Moshi moshi) {

        Arguments.checkNotNull(platformService, "PlatformService");
        Arguments.checkNotNull(moshi, "Moshi to create adapter");

        this.platformService = platformService;
        this.labelMappingAdapter = moshi.adapter(LabelMapping.class);
    }

    @Nonnull
    List<Label> getLabels(@Nonnull final String resourceID, @Nonnull final String resourcePath) {

        Arguments.checkNonEmpty(resourceID, "resourceID");
        Arguments.checkNonEmpty(resourcePath, "resourcePath");

        Call<Labels> call = platformService.findResourceLabels(resourceID, resourcePath);
        Labels labels = execute(call);

        LOG.log(Level.FINEST, "findResourceLabels response: {0}", labels);

        return labels.getLabels();
    }

    @Nonnull
    Label addLabel(@Nonnull final String labelID,
                   @Nonnull final String resourceID,
                   @Nonnull final String resourcePath,
                   @Nonnull final ResourceType resourceType) {

        Arguments.checkNonEmpty(labelID, "labelID");
        Arguments.checkNonEmpty(resourceID, "resourceID");
        Arguments.checkNonEmpty(resourcePath, "resourcePath");

        LabelMapping labelMapping = new LabelMapping();
        labelMapping.setLabelID(labelID);
        labelMapping.setResourceType(resourceType);

        String json = labelMappingAdapter.toJson(labelMapping);
        Call<LabelResponse> call = platformService.addResourceLabelOwner(resourceID, resourcePath, createBody(json));

        LabelResponse labelResponse = execute(call);

        LOG.log(Level.FINEST, "addResourceLabelOwner response: {0}", labelResponse);

        return labelResponse.getLabel();
    }

    void deleteLabel(@Nonnull final String labelID, @Nonnull final String resourceID,
                     @Nonnull final String resourcePath) {

        Arguments.checkNonEmpty(labelID, "labelID");
        Arguments.checkNonEmpty(resourceID, "resourceID");
        Arguments.checkNonEmpty(resourcePath, "resourcePath");

        Call<Void> call = platformService.deleteResourceLabelOwner(resourceID, resourcePath, labelID);

        execute(call);
    }
}