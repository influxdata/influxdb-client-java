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
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.influxdata.Arguments;
import org.influxdata.client.LabelsApi;
import org.influxdata.client.domain.Label;
import org.influxdata.client.domain.LabelResponse;
import org.influxdata.client.domain.Labels;
import org.influxdata.exceptions.NotFoundException;
import org.influxdata.internal.AbstractRestClient;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import retrofit2.Call;

/**
 * @author Jakub Bednar (bednar@github) (28/01/2019 10:48)
 */
class LabelsApiImpl extends AbstractRestClient implements LabelsApi {

    private static final Logger LOG = Logger.getLogger(LabelsApiImpl.class.getName());

    private final InfluxDBService influxDBService;
    private final JsonAdapter<Label> adapter;

    LabelsApiImpl(@Nonnull final InfluxDBService influxDBService, @Nonnull final Moshi moshi) {

        Arguments.checkNotNull(influxDBService, "InfluxDBService");
        Arguments.checkNotNull(moshi, "Moshi to create adapter");

        this.influxDBService = influxDBService;
        this.adapter = moshi.adapter(Label.class);
    }

    @Nonnull
    @Override
    public Label createLabel(@Nonnull final Label label) {

        Arguments.checkNotNull(label, "label");

        Call<LabelResponse> call = influxDBService.createLabel(createBody(adapter.toJson(label)));
        LabelResponse labelResponse = execute(call);

        LOG.log(Level.FINEST, "createLabel response: {0}", labelResponse);

        return labelResponse.getLabel();
    }

    @Nonnull
    @Override
    public Label createLabel(@Nonnull final String name, @Nonnull final Map<String, String> properties) {

        Arguments.checkNonEmpty(name, "name");
        Arguments.checkNotNull(properties, "properties");

        Label label = new Label();
        label.setName(name);
        label.setProperties(properties);

        return createLabel(label);
    }

    @Nonnull
    @Override
    public Label updateLabel(@Nonnull final Label label) {

        Arguments.checkNotNull(label, "label");

        String json = adapter.toJson(label);

        Call<LabelResponse> call = influxDBService.updateLabel(label.getId(), createBody(json));
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

        Call<Void> call = influxDBService.deleteLabel(labelID);
        execute(call);
    }

    @Nullable
    @Override
    public Label findLabelByID(@Nonnull final String labelID) {

        Arguments.checkNonEmpty(labelID, "labelID");

        Call<LabelResponse> call = influxDBService.findLabelByID(labelID);
        LabelResponse labelResponse = execute(call, NotFoundException.class);

        if (labelResponse == null) {
            return null;
        }

        LOG.log(Level.FINEST, "findLabelByID response: {0}", labelResponse);

        return labelResponse.getLabel();
    }

    @Nonnull
    @Override
    public List<Label> findLabels() {

        Call<Labels> sourcesCall = influxDBService.findLabels();

        Labels labels = execute(sourcesCall);
        LOG.log(Level.FINEST, "findLabels found: {0}", labels);

        return labels.getLabels();
    }
}