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
package com.influxdb.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

import com.influxdb.client.domain.ConstantVariableProperties;
import com.influxdb.client.domain.Label;
import com.influxdb.client.domain.MapVariableProperties;
import com.influxdb.client.domain.Organization;
import com.influxdb.client.domain.QueryVariableProperties;
import com.influxdb.client.domain.QueryVariablePropertiesValues;
import com.influxdb.client.domain.Variable;
import com.influxdb.exceptions.NotFoundException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (27/03/2019 09:46)
 */
@RunWith(JUnitPlatform.class)
class ITVariablesApi extends AbstractITClientTest {

    private VariablesApi variablesApi;
    private Organization organization;

    @BeforeEach
    void setUp() {

        variablesApi = influxDBClient.getVariablesApi();
        organization = findMyOrg();

        variablesApi.findVariables(organization)
                .forEach(variable -> variablesApi.deleteVariable(variable));
    }

    @Test
    void create() {

        Variable variable = newConstantVariable();

        Variable created = variablesApi.createVariable(variable);

        Assertions.assertThat(created).isNotNull();
        Assertions.assertThat(created.getId()).isNotBlank();
        Assertions.assertThat(created.getOrgID()).isEqualTo(organization.getId());
        Assertions.assertThat(created.getName()).startsWith("my-variable");
        Assertions.assertThat(created.getDescription()).isEqualTo("my-top-variable");
        Assertions.assertThat(created.getSelected()).hasSize(1);
        Assertions.assertThat(created.getSelected()).contains("constant2");
        Assertions.assertThat(created.getArguments()).isNotNull();
        Assertions.assertThat((ConstantVariableProperties) created.getArguments()).isNotNull();
        Assertions.assertThat(((ConstantVariableProperties) created.getArguments()).getType()).isEqualTo(ConstantVariableProperties.TypeEnum.CONSTANT);
        Assertions.assertThat(((ConstantVariableProperties) created.getArguments()).getValues()).hasSize(3);
        Assertions.assertThat(((ConstantVariableProperties) created.getArguments()).getValues()).containsExactlyInAnyOrder("constant1", "constant2", "constant3");
        Assertions.assertThat(created.getLinks()).isNotNull();
        Assertions.assertThat(created.getLinks().getOrg()).isEqualTo("/api/v2/orgs/" + organization.getId());
        Assertions.assertThat(created.getLinks().getSelf()).isEqualTo("/api/v2/variables/" + created.getId());
    }

    @Test
    void createMapVariableProperties() {

        Variable variable = new Variable();
        variable.name(generateName("my-variable"));
        variable.orgID(organization.getId());
        variable.addSelectedItem("map3");

        MapVariableProperties type = new MapVariableProperties()
                .putValuesItem("map1", "1")
                .putValuesItem("map2", "2")
                .putValuesItem("map3", "3")
                .putValuesItem("map4", "4");
        variable.arguments(type);

        Variable created = variablesApi.createVariable(variable);
        Assertions.assertThat(created).isNotNull();
        Assertions.assertThat(created.getId()).isNotBlank();
        Assertions.assertThat(created.getOrgID()).isEqualTo(organization.getId());
        Assertions.assertThat(created.getName()).startsWith("my-variable");
        Assertions.assertThat(created.getSelected()).hasSize(1);
        Assertions.assertThat(created.getSelected()).contains("map3");
        Assertions.assertThat(created.getArguments()).isNotNull();
        Assertions.assertThat((MapVariableProperties) created.getArguments()).isNotNull();
        Assertions.assertThat(((MapVariableProperties) created.getArguments()).getType()).isEqualTo(MapVariableProperties.TypeEnum.MAP);
        Assertions.assertThat(((MapVariableProperties) created.getArguments()).getValues()).hasSize(4);
        Assertions.assertThat(((MapVariableProperties) created.getArguments()).getValues())
                .hasEntrySatisfying("map1", value -> Assertions.assertThat(value).isEqualTo("1"))
                .hasEntrySatisfying("map2", value -> Assertions.assertThat(value).isEqualTo("2"))
                .hasEntrySatisfying("map3", value -> Assertions.assertThat(value).isEqualTo("3"))
                .hasEntrySatisfying("map4", value -> Assertions.assertThat(value).isEqualTo("4"));

        Assertions.assertThat(created.getLinks()).isNotNull();
        Assertions.assertThat(created.getLinks().getOrg()).isEqualTo("/api/v2/orgs/" + organization.getId());
        Assertions.assertThat(created.getLinks().getSelf()).isEqualTo("/api/v2/variables/" + created.getId());
    }

    @Test
    void createQueryVariableProperties() {

        Variable variable = new Variable();
        variable.name(generateName("my-variable"));
        variable.orgID(organization.getId());

        QueryVariableProperties type = new QueryVariableProperties()
                .values(new QueryVariablePropertiesValues()
                        .language("flux")
                        .query("from(bucket:\"telegraf\") |> last()"));
        variable.arguments(type);

        Variable created = variablesApi.createVariable(variable);
        Assertions.assertThat(created).isNotNull();
        Assertions.assertThat(created.getId()).isNotBlank();
        Assertions.assertThat(created.getOrgID()).isEqualTo(organization.getId());
        Assertions.assertThat(created.getName()).startsWith("my-variable");
        Assertions.assertThat(created.getSelected()).hasSize(0);
        Assertions.assertThat(created.getArguments()).isNotNull();
        Assertions.assertThat((QueryVariableProperties) created.getArguments()).isNotNull();
        Assertions.assertThat(((QueryVariableProperties) created.getArguments()).getType()).isEqualTo(QueryVariableProperties.TypeEnum.QUERY);
        Assertions.assertThat(((QueryVariableProperties) created.getArguments()).getValues()).isNotNull();
        Assertions.assertThat(((QueryVariableProperties) created.getArguments()).getValues().getLanguage()).isEqualTo("flux");
        Assertions.assertThat(((QueryVariableProperties) created.getArguments()).getValues().getQuery()).isEqualTo("from(bucket:\"telegraf\") |> last()");
        Assertions.assertThat(created.getLinks()).isNotNull();
        Assertions.assertThat(created.getLinks().getOrg()).isEqualTo("/api/v2/orgs/" + organization.getId());
        Assertions.assertThat(created.getLinks().getSelf()).isEqualTo("/api/v2/variables/" + created.getId());
    }

    @Test
    void findVariableByID() {

        Variable variable = newConstantVariable();

        variable = variablesApi.createVariable(variable);
        variable = variablesApi.findVariableByID(variable.getId());

        Assertions.assertThat(variable).isNotNull();
        Assertions.assertThat(variable.getId()).isNotBlank();
        Assertions.assertThat(variable.getOrgID()).isEqualTo(organization.getId());
        Assertions.assertThat(variable.getName()).startsWith("my-variable");
        Assertions.assertThat(variable.getSelected()).hasSize(1);
        Assertions.assertThat(variable.getSelected()).contains("constant2");
        Assertions.assertThat(variable.getArguments()).isNotNull();
        Assertions.assertThat((ConstantVariableProperties) variable.getArguments()).isNotNull();
        Assertions.assertThat(((ConstantVariableProperties) variable.getArguments()).getType()).isEqualTo(ConstantVariableProperties.TypeEnum.CONSTANT);
        Assertions.assertThat(((ConstantVariableProperties) variable.getArguments()).getValues()).hasSize(3);
        Assertions.assertThat(((ConstantVariableProperties) variable.getArguments()).getValues()).containsExactlyInAnyOrder("constant1", "constant2", "constant3");
        Assertions.assertThat(variable.getLinks()).isNotNull();
        Assertions.assertThat(variable.getLinks().getOrg()).isEqualTo("/api/v2/orgs/" + organization.getId());
        Assertions.assertThat(variable.getLinks().getSelf()).isEqualTo("/api/v2/variables/" + variable.getId());
    }

    @Test
    void deleteVariable() {

        Variable createdVariable = variablesApi.createVariable(newConstantVariable());
        Assertions.assertThat(createdVariable).isNotNull();

        Variable foundVariable = variablesApi.findVariableByID(createdVariable.getId());
        Assertions.assertThat(foundVariable).isNotNull();

        // delete variable
        variablesApi.deleteVariable(createdVariable);

        Assertions.assertThatThrownBy(() -> variablesApi.findVariableByID(createdVariable.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void findVariableByIDNotFound() {

        Assertions.assertThatThrownBy(() -> variablesApi.findVariableByID("020f755c3c082000"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("variable not found");
    }

    @Test
    void findVariables() {

        Organization org = influxDBClient.getOrganizationsApi().createOrganization(generateName("org"));

        List<Variable> variables = variablesApi.findVariables(org);
        Assertions.assertThat(variables).isEmpty();

        Variable created = variablesApi.createVariable(newConstantVariable().orgID(org.getId()));

        variables = variablesApi.findVariables(org);
        Assertions.assertThat(variables).hasSize(1);
        Assertions.assertThat(variables.get(0).getId()).isEqualTo(created.getId());

        variablesApi.deleteVariable(created);

        variables = variablesApi.findVariables(org);
        Assertions.assertThat(variables).isEmpty();
    }

    @Test
    void findVariablesNotFound() {

        List<Variable> variables = variablesApi.findVariables("020f755c3c082000");

        Assertions.assertThat(variables).isEmpty();
    }

    @Test
    void updateVariable() {

        Variable variable = variablesApi.createVariable(newConstantVariable());

        QueryVariableProperties type = new QueryVariableProperties()
                .values(new QueryVariablePropertiesValues()
                        .language("flux")
                        .query("from(bucket:\"telegraf\") |> last()"));

        variable.name(generateName("updated"))
                .selected(new ArrayList<>())
                .addSelectedItem("updated-selected")
                .arguments(type);

        Variable updated = variablesApi.updateVariable(variable);

        Assertions.assertThat(updated).isNotNull();
        Assertions.assertThat(updated.getId()).isNotBlank();
        Assertions.assertThat(updated.getOrgID()).isEqualTo(organization.getId());
        Assertions.assertThat(updated.getName()).startsWith("updated");
        Assertions.assertThat(updated.getSelected()).hasSize(1);
        Assertions.assertThat(updated.getSelected()).contains("updated-selected");
        Assertions.assertThat(updated.getArguments()).isNotNull();
        Assertions.assertThat((QueryVariableProperties) updated.getArguments()).isNotNull();
        Assertions.assertThat(((QueryVariableProperties) updated.getArguments()).getType()).isEqualTo(QueryVariableProperties.TypeEnum.QUERY);
        Assertions.assertThat(((QueryVariableProperties) updated.getArguments()).getValues()).isNotNull();
        Assertions.assertThat(((QueryVariableProperties) updated.getArguments()).getValues().getLanguage()).isEqualTo("flux");
        Assertions.assertThat(((QueryVariableProperties) updated.getArguments()).getValues().getQuery()).isEqualTo("from(bucket:\"telegraf\") |> last()");
        Assertions.assertThat(updated.getLinks()).isNotNull();
        Assertions.assertThat(updated.getLinks().getOrg()).isEqualTo("/api/v2/orgs/" + organization.getId());
        Assertions.assertThat(updated.getLinks().getSelf()).isEqualTo("/api/v2/variables/" + updated.getId());
    }

    @Test
    void cloneVariable() {

        Variable variable = variablesApi.createVariable(newConstantVariable());

        Variable cloned = variablesApi.cloneVariable(generateName("cloned"), variable.getId());

        Assertions.assertThat(cloned).isNotNull();
        Assertions.assertThat(cloned.getId()).isNotBlank();
        Assertions.assertThat(cloned.getOrgID()).isEqualTo(organization.getId());
        Assertions.assertThat(cloned.getName()).startsWith("cloned");
        Assertions.assertThat(cloned.getSelected()).hasSize(1);
        Assertions.assertThat(cloned.getSelected()).contains("constant2");
        Assertions.assertThat(cloned.getArguments()).isNotNull();
        Assertions.assertThat((ConstantVariableProperties) cloned.getArguments()).isNotNull();
        Assertions.assertThat(((ConstantVariableProperties) cloned.getArguments()).getType()).isEqualTo(ConstantVariableProperties.TypeEnum.CONSTANT);
        Assertions.assertThat(((ConstantVariableProperties) cloned.getArguments()).getValues()).hasSize(3);
        Assertions.assertThat(((ConstantVariableProperties) cloned.getArguments()).getValues()).containsExactlyInAnyOrder("constant1", "constant2", "constant3");
        Assertions.assertThat(cloned.getLinks()).isNotNull();
        Assertions.assertThat(cloned.getLinks().getOrg()).isEqualTo("/api/v2/orgs/" + organization.getId());
        Assertions.assertThat(cloned.getLinks().getSelf()).isEqualTo("/api/v2/variables/" + cloned.getId());
    }

    @Test
    void labels() {

        LabelsApi labelsApi = influxDBClient.getLabelsApi();

        Variable variable = variablesApi.createVariable(newConstantVariable());

        Map<String, String> properties = new HashMap<>();
        properties.put("color", "green");
        properties.put("location", "west");

        Label label = labelsApi.createLabel(generateName("Cool Resource"), properties, organization.getId());

        List<Label> labels = variablesApi.getLabels(variable);
        Assertions.assertThat(labels).hasSize(0);

        Label created = variablesApi.addLabel(label, variable);
        Assertions.assertThat(created).isNotNull();
        Assertions.assertThat(created.getId()).isEqualTo(label.getId());
        Assertions.assertThat(created.getName()).isEqualTo(label.getName());
        Assertions.assertThat(created.getProperties()).isEqualTo(label.getProperties());

        labels = variablesApi.getLabels(variable);
        Assertions.assertThat(labels).hasSize(1);
        Assertions.assertThat(labels.get(0).getId()).isEqualTo(label.getId());
        Assertions.assertThat(labels.get(0).getName()).isEqualTo(label.getName());

        variablesApi.deleteLabel(label, variable);

        labels = variablesApi.getLabels(variable);
        Assertions.assertThat(labels).hasSize(0);
    }

    @Test
    void labelAddNotExists() {

        Variable variable = variablesApi.createVariable(newConstantVariable());

        Assertions.assertThatThrownBy(() -> variablesApi.addLabel("020f755c3c082000", variable.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void labelDeleteNotExists() {

        Variable variable = variablesApi.createVariable(newConstantVariable());

        variablesApi.deleteLabel("020f755c3c082000", variable.getId());
    }

    @Nonnull
    private Variable newConstantVariable() {

        Variable variable = new Variable();
        variable.name(generateName("my-variable"));
        variable.orgID(organization.getId());
        variable.addSelectedItem("constant2");
        variable.setDescription("my-top-variable");

        ConstantVariableProperties type = new ConstantVariableProperties()
                .addValuesItem("constant1")
                .addValuesItem("constant2")
                .addValuesItem("constant3");

        variable.arguments(type);

        return variable;
    }
}
