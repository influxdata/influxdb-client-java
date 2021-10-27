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
import javax.annotation.Nonnull;

import com.influxdb.client.VariablesApi;
import com.influxdb.client.domain.Label;
import com.influxdb.client.domain.LabelMapping;
import com.influxdb.client.domain.LabelResponse;
import com.influxdb.client.domain.LabelsResponse;
import com.influxdb.client.domain.Organization;
import com.influxdb.client.domain.Variable;
import com.influxdb.client.domain.Variables;
import com.influxdb.client.service.VariablesService;
import com.influxdb.internal.AbstractRestClient;
import com.influxdb.utils.Arguments;

import retrofit2.Call;

/**
 * @author Jakub Bednar (27/03/2019 09:37)
 */
final class VariablesApiImpl extends AbstractRestClient implements VariablesApi {

    private final VariablesService service;

    VariablesApiImpl(@Nonnull final VariablesService service) {

        Arguments.checkNotNull(service, "service");

        this.service = service;
    }

    @Nonnull
    @Override
    public Variable createVariable(@Nonnull final Variable variable) {

        Arguments.checkNotNull(variable, "variable");

        Call<Variable> call = service.postVariables(variable, null);

        return execute(call);
    }

    @Nonnull
    @Override
    public Variable updateVariable(@Nonnull final Variable variable) {

        Arguments.checkNotNull(variable, "variable");

        Call<Variable> call = service.patchVariablesID(variable.getId(), variable, null);

        return execute(call);
    }

    @Override
    public void deleteVariable(@Nonnull final Variable variable) {

        Arguments.checkNotNull(variable, "variable");

        deleteVariable(variable.getId());
    }

    @Override
    public void deleteVariable(@Nonnull final String variableID) {

        Arguments.checkNonEmpty(variableID, "variableID");

        Call<Void> call = service.deleteVariablesID(variableID, null);

        execute(call);
    }

    @Nonnull
    @Override
    public Variable cloneVariable(@Nonnull final String clonedName, @Nonnull final String variableID) {

        Arguments.checkNonEmpty(clonedName, "clonedName");
        Arguments.checkNonEmpty(variableID, "variableID");

        return cloneVariable(clonedName, findVariableByID(variableID));
    }

    @Nonnull
    @Override
    public Variable cloneVariable(@Nonnull final String clonedName, @Nonnull final Variable variable) {

        Arguments.checkNonEmpty(clonedName, "clonedName");
        Arguments.checkNotNull(variable, "variable");

        Variable cloned = new Variable();
        cloned.name(clonedName)
                .orgID(variable.getOrgID())
                .selected(variable.getSelected())
                .arguments(variable.getArguments());

        return createVariable(cloned);
    }

    @Nonnull
    @Override
    public Variable findVariableByID(@Nonnull final String variableID) {

        Arguments.checkNonEmpty(variableID, "variableID");

        Call<Variable> call = service.getVariablesID(variableID, null);

        return execute(call);
    }

    @Nonnull
    @Override
    public List<Variable> findVariables(@Nonnull final Organization organization) {

        Arguments.checkNotNull(organization, "organization");

        return findVariables(organization.getId());
    }

    @Nonnull
    @Override
    public List<Variable> findVariables(@Nonnull final String orgID) {

        Arguments.checkNonEmpty(orgID, "orgID");

        Call<Variables> call = service.getVariables(null, null, orgID);

        return execute(call).getVariables();
    }

    @Nonnull
    @Override
    public List<Label> getLabels(@Nonnull final Variable variable) {

        Arguments.checkNotNull(variable, "variable");

        return getLabels(variable.getId());
    }

    @Nonnull
    @Override
    public List<Label> getLabels(@Nonnull final String variableID) {

        Arguments.checkNonEmpty("variableID", variableID);

        Call<LabelsResponse> call = service.getVariablesIDLabels(variableID, null);

        return execute(call).getLabels();
    }

    @Nonnull
    @Override
    public Label addLabel(@Nonnull final Label label,
                                   @Nonnull final Variable variable) {

        Arguments.checkNotNull(label, "label");
        Arguments.checkNotNull(variable, "variable");

        return addLabel(label.getId(), variable.getId());
    }

    @Nonnull
    @Override
    public Label addLabel(@Nonnull final String labelID, @Nonnull final String variableID) {

        Arguments.checkNonEmpty("variableID", variableID);
        Arguments.checkNonEmpty("labelID", labelID);

        Call<LabelResponse> call = service
                .postVariablesIDLabels(variableID, new LabelMapping().labelID(labelID), null);

        return execute(call).getLabel();
    }

    @Override
    public void deleteLabel(@Nonnull final Label label, @Nonnull final Variable variable) {

        Arguments.checkNotNull(label, "label");
        Arguments.checkNotNull(variable, "variable");

        deleteLabel(label.getId(), variable.getId());
    }

    @Override
    public void deleteLabel(@Nonnull final String labelID, @Nonnull final String variableID) {

        Arguments.checkNonEmpty("variableID", variableID);
        Arguments.checkNonEmpty("labelID", labelID);

        Call<Void> call = service.deleteVariablesIDLabelsID(variableID, labelID, null);
        execute(call);
    }
}
