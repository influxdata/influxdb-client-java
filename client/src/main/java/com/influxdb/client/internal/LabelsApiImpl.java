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

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.influxdb.Arguments;
import com.influxdb.client.LabelsApi;
import com.influxdb.client.domain.Label;
import com.influxdb.client.domain.LabelCreateRequest;
import com.influxdb.client.domain.LabelResponse;
import com.influxdb.client.domain.LabelUpdate;
import com.influxdb.client.domain.LabelsResponse;
import com.influxdb.client.domain.Organization;
import com.influxdb.client.service.LabelsService;
import com.influxdb.internal.AbstractRestClient;

import retrofit2.Call;

/**
 * @author Jakub Bednar (bednar@github) (28/01/2019 10:48)
 */
final class LabelsApiImpl extends AbstractRestClient implements LabelsApi {

    private static final Logger LOG = Logger.getLogger(LabelsApiImpl.class.getName());

    private final LabelsService service;

    LabelsApiImpl(@Nonnull final LabelsService service) {

        Arguments.checkNotNull(service, "service");

        this.service = service;
    }

    @Nonnull
    @Override
    public Label createLabel(@Nonnull final String name,
                             @Nonnull final Map<String, String> properties,
                             @Nonnull final String orgID) {

        Arguments.checkNonEmpty(orgID, "orgID");
        Arguments.checkNonEmpty(name, "name");
        Arguments.checkNotNull(properties, "properties");

        LabelCreateRequest label = new LabelCreateRequest();
        label.setName(name);
        label.setProperties(properties);
        label.setOrgID(orgID);

        return createLabel(label);
    }

    @Nonnull
    @Override
    public Label createLabel(@Nonnull final LabelCreateRequest request) {

        Arguments.checkNotNull(request, "request");

        Call<LabelResponse> call = service.postLabels(request);
        LabelResponse labelResponse = execute(call);

        LOG.log(Level.FINEST, "createLabel response: {0}", labelResponse);

        return labelResponse.getLabel();
    }

    @Nonnull
    @Override
    public Label updateLabel(@Nonnull final Label label) {

        Arguments.checkNotNull(label, "label");

        LabelUpdate labelUpdate = new LabelUpdate();
        labelUpdate.properties(label.getProperties());

        return updateLabel(label.getId(), labelUpdate);
    }

    @Nonnull
    @Override
    public Label updateLabel(@Nonnull final String labelID, @Nonnull final LabelUpdate labelUpdate) {

        Arguments.checkNonEmpty(labelID, "labelID");
        Arguments.checkNotNull(labelUpdate, "labelUpdate");

        Call<LabelResponse> call = service.patchLabelsID(labelID, labelUpdate, null);
        LabelResponse labelResponse = execute(call);

        LOG.log(Level.FINEST, "updateLabel response: {0}", labelResponse);

        return labelResponse.getLabel();
    }

    @Override
    public void deleteLabel(@Nonnull final Label label) {

        Arguments.checkNotNull(label, "label");

        deleteLabel(label.getId());
    }

    @Override
    public void deleteLabel(@Nonnull final String labelID) {

        Arguments.checkNonEmpty(labelID, "labelID");

        Call<Void> call = service.deleteLabelsID(labelID, null);
        execute(call);
    }

    @Nonnull
    @Override
    public Label cloneLabel(@Nonnull final String clonedName, @Nonnull final String labelID) {

        Arguments.checkNonEmpty(clonedName, "clonedName");
        Arguments.checkNonEmpty(labelID, "labelID");

        Label label = findLabelByID(labelID);
        if (label == null) {
            throw new IllegalStateException("NotFound Label with ID: " + labelID);
        }

        return cloneLabel(clonedName, label);
    }

    @Nonnull
    @Override
    public Label cloneLabel(@Nonnull final String clonedName, @Nonnull final Label label) {

        Arguments.checkNonEmpty(clonedName, "clonedName");
        Arguments.checkNotNull(label, "label");

        LabelCreateRequest cloned = new LabelCreateRequest();
        cloned.setName(clonedName);
        cloned.setOrgID(label.getOrgID());

        if (label.getProperties() != null) {
            label.getProperties().forEach(cloned::putPropertiesItem);
        }
        cloned.getProperties().putAll(label.getProperties());

        return createLabel(cloned);
    }

    @Nonnull
    @Override
    public Label findLabelByID(@Nonnull final String labelID) {

        Arguments.checkNonEmpty(labelID, "labelID");

        Call<LabelResponse> call = service.getLabelsID(labelID, null);

        return execute(call).getLabel();
    }

    @Nonnull
    @Override
    public List<Label> findLabels() {

        return findLabelsByOrgId(null);
    }

    @Nonnull
    @Override
    public List<Label> findLabelsByOrg(@Nonnull final Organization organization) {

        Arguments.checkNotNull(organization, "organization");

        return findLabelsByOrgId(organization.getId());
    }

    @Nonnull
    @Override
    public List<Label> findLabelsByOrgId(@Nullable final String orgID) {

        Call<LabelsResponse> sourcesCall = service.getLabels(orgID, null);

        LabelsResponse labels = execute(sourcesCall);
        LOG.log(Level.FINEST, "findLabels found: {0}", labels);

        return labels.getLabels();
    }
}