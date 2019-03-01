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
package org.influxdata.client;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

import org.influxdata.client.domain.Label;
import org.influxdata.client.domain.Organization;
import org.influxdata.client.domain.ResourceMember;
import org.influxdata.client.domain.TelegrafConfig;
import org.influxdata.client.domain.TelegrafPlugin;
import org.influxdata.client.domain.TelegrafPluginType;
import org.influxdata.client.domain.User;
import org.influxdata.exceptions.NotFoundException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (28/02/2019 10:31)
 */
@RunWith(JUnitPlatform.class)
class ITTelegrafsApi extends AbstractITClientTest {

    private TelegrafsApi telegrafsApi;
    private UsersApi usersApi;
    private Organization organization;

    @BeforeEach
    void setUp() {

        telegrafsApi = influxDBClient.getTelegrafsApi();
        usersApi = influxDBClient.getUsersApi();
        organization = findMyOrg();

        telegrafsApi.findTelegrafConfigs().forEach(telegrafConfig -> telegrafsApi.deleteTelegrafConfig(telegrafConfig));
    }

    @Test
    void createTelegrafConfig() {

        String name = generateName("TelegrafConfig");

        TelegrafPlugin output = newOutputPlugin();
        TelegrafPlugin cpu = newCpuPlugin();

        TelegrafConfig telegrafConfig = telegrafsApi
                .createTelegrafConfig(name, "test-config", organization, 1_000, output, cpu);

        Assertions.assertThat(telegrafConfig).isNotNull();
        Assertions.assertThat(telegrafConfig.getName()).isEqualTo(name);
        Assertions.assertThat(telegrafConfig.getDescription()).isEqualTo("test-config");
        Assertions.assertThat(telegrafConfig.getOrgID()).isEqualTo(organization.getId());
        Assertions.assertThat(telegrafConfig.getAgent()).isNotNull();
        Assertions.assertThat(telegrafConfig.getAgent().getCollectionInterval()).isEqualTo(1_000);
        Assertions.assertThat(telegrafConfig.getPlugins()).hasSize(2);
        Assertions.assertThat(telegrafConfig.getPlugins().get(0).getName()).isEqualTo("influxdb_v2");
        Assertions.assertThat(telegrafConfig.getPlugins().get(0).getComment()).isEqualTo("Output to Influx 2.0");
        Assertions.assertThat(telegrafConfig.getPlugins().get(0).getType()).isEqualTo(TelegrafPluginType.OUTPUT);
        Assertions.assertThat(telegrafConfig.getPlugins().get(1).getName()).isEqualTo("cpu");
        Assertions.assertThat(telegrafConfig.getPlugins().get(1).getType()).isEqualTo(TelegrafPluginType.INPUT);
    }

    @Test
    void updateTelegrafConfig() {

        TelegrafConfig telegrafConfig = telegrafsApi
                .createTelegrafConfig(generateName("tc"), "test-config", organization, 1_000, newCpuPlugin(), newOutputPlugin());

        telegrafConfig.setDescription("updated");
        telegrafConfig.getAgent().setCollectionInterval(500);
        telegrafConfig.getPlugins().remove(0);

        telegrafConfig = telegrafsApi.updateTelegrafConfig(telegrafConfig);

        Assertions.assertThat(telegrafConfig.getDescription()).isEqualTo("updated");
        Assertions.assertThat(telegrafConfig.getAgent().getCollectionInterval()).isEqualTo(500);
        Assertions.assertThat(telegrafConfig.getPlugins()).hasSize(1);
    }

    @Test
    void deleteTelegrafConfig() {

        List<TelegrafPlugin> plugins = Arrays.asList(newOutputPlugin(), newCpuPlugin());

        TelegrafConfig createdConfig = telegrafsApi
                .createTelegrafConfig(generateName("tc"), "test-config", organization, 1_000, plugins);
        Assertions.assertThat(createdConfig).isNotNull();

        TelegrafConfig foundTelegrafConfig = telegrafsApi.findTelegrafConfigByID(createdConfig.getId());
        Assertions.assertThat(foundTelegrafConfig).isNotNull();

        // delete source
        telegrafsApi.deleteTelegrafConfig(createdConfig);

        foundTelegrafConfig = telegrafsApi.findTelegrafConfigByID(createdConfig.getId());
        Assertions.assertThat(foundTelegrafConfig).isNull();
    }

    @Test
    void deleteTelegrafConfigNotFound() {

        Assertions.assertThatThrownBy(() -> telegrafsApi.deleteTelegrafConfig("020f755c3d082000"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("telegraf configuration not found");
    }

    @Test
    void findTelegrafConfigByID() {

        TelegrafConfig telegrafConfig = telegrafsApi
                .createTelegrafConfig(generateName("tc"), "test-config", organization, 1_000, newCpuPlugin(), newOutputPlugin());

        TelegrafConfig telegrafConfigByID = telegrafsApi.findTelegrafConfigByID(telegrafConfig.getId());

        Assertions.assertThat(telegrafConfigByID).isNotNull();
        Assertions.assertThat(telegrafConfigByID.getId()).isEqualTo(telegrafConfig.getId());
        Assertions.assertThat(telegrafConfigByID.getName()).isEqualTo(telegrafConfig.getName());
        Assertions.assertThat(telegrafConfigByID.getOrgID()).isEqualTo(telegrafConfig.getOrgID());
        Assertions.assertThat(telegrafConfigByID.getDescription()).isEqualTo(telegrafConfig.getDescription());
        Assertions.assertThat(telegrafConfigByID.getAgent().getCollectionInterval()).isEqualTo(1_000);
        Assertions.assertThat(telegrafConfigByID.getPlugins()).hasSize(2);
    }

    @Test
    void findTelegrafConfigByIDNull() {

        TelegrafConfig telegrafConfig = telegrafsApi.findTelegrafConfigByID("020f755c3d082000");

        Assertions.assertThat(telegrafConfig).isNull();
    }

    @Test
    void findTelegrafConfigByOrg() {

        String orgName = generateName("Constant Pro");

        Organization organization = influxDBClient.getOrganizationsApi().createOrganization(orgName);
        List<TelegrafConfig> telegrafConfigs = telegrafsApi.findTelegrafConfigsByOrg(organization);

        Assertions.assertThat(telegrafConfigs).hasSize(0);

        telegrafsApi
                .createTelegrafConfig(generateName("tc"), "test-config", organization, 1_000, newCpuPlugin(), newOutputPlugin());

        telegrafConfigs = telegrafsApi.findTelegrafConfigsByOrg(organization);

        Assertions.assertThat(telegrafConfigs).hasSize(1);

        telegrafsApi.deleteTelegrafConfig(telegrafConfigs.get(0));
    }

    @Test
    void findTelegrafConfigs() {

        int size = telegrafsApi.findTelegrafConfigs().size();

        telegrafsApi
                .createTelegrafConfig(generateName("tc"), "test-config", organization, 1_000, newCpuPlugin(), newOutputPlugin());

        List<TelegrafConfig> telegrafConfigs = telegrafsApi.findTelegrafConfigs();
        Assertions.assertThat(telegrafConfigs).hasSize(size + 1);
    }

    @Test
    void getTOML() {

        TelegrafConfig telegrafConfig = telegrafsApi
                .createTelegrafConfig(generateName("tc"), "test-config", organization, 1_000, newCpuPlugin(), newOutputPlugin());

        String toml = telegrafsApi.getTOML(telegrafConfig);

        Assertions.assertThat(toml).contains("[[inputs.cpu]]");
        Assertions.assertThat(toml).contains("[[outputs.influxdb_v2]]");
        Assertions.assertThat(toml).contains("organization = \"my-org\"");
        Assertions.assertThat(toml).contains("bucket = \"my-bucket\"");
    }

    @Test
    void getTOMLNotFound() {

        Assertions.assertThatThrownBy(() -> telegrafsApi.getTOML("020f755c3d082000"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("telegraf configuration not found");
    }

    @Test
    void member() {

        TelegrafConfig telegrafConfig = telegrafsApi
                .createTelegrafConfig(generateName("tc"), "test-config", organization, 1_000, newCpuPlugin(), newOutputPlugin());

        List<ResourceMember> members = telegrafsApi.getMembers(telegrafConfig);
        Assertions.assertThat(members).hasSize(0);

        User user = usersApi.createUser(generateName("Luke Health"));

        ResourceMember resourceMember = telegrafsApi.addMember(user, telegrafConfig);
        Assertions.assertThat(resourceMember).isNotNull();
        Assertions.assertThat(resourceMember.getUserID()).isEqualTo(user.getId());
        Assertions.assertThat(resourceMember.getUserName()).isEqualTo(user.getName());
        Assertions.assertThat(resourceMember.getRole()).isEqualTo(ResourceMember.UserType.MEMBER);

        members = telegrafsApi.getMembers(telegrafConfig);
        Assertions.assertThat(members).hasSize(1);
        Assertions.assertThat(members.get(0).getRole()).isEqualTo(ResourceMember.UserType.MEMBER);
        Assertions.assertThat(members.get(0).getUserID()).isEqualTo(user.getId());
        Assertions.assertThat(members.get(0).getUserName()).isEqualTo(user.getName());

        telegrafsApi.deleteMember(user, telegrafConfig);

        members = telegrafsApi.getMembers(telegrafConfig);
        Assertions.assertThat(members).hasSize(0);
    }

    @Test
    void owner() {

        TelegrafConfig telegrafConfig = telegrafsApi
                .createTelegrafConfig(generateName("tc"), "test-config", organization, 1_000, newCpuPlugin(), newOutputPlugin());

        List<ResourceMember> owners = telegrafsApi.getOwners(telegrafConfig);
        Assertions.assertThat(owners).hasSize(1);
        Assertions.assertThat(owners.get(0).getUserName()).isEqualTo("my-user");

        User user = usersApi.createUser(generateName("Luke Health"));

        ResourceMember resourceMember = telegrafsApi.addOwner(user, telegrafConfig);
        Assertions.assertThat(resourceMember).isNotNull();
        Assertions.assertThat(resourceMember.getUserID()).isEqualTo(user.getId());
        Assertions.assertThat(resourceMember.getUserName()).isEqualTo(user.getName());
        Assertions.assertThat(resourceMember.getRole()).isEqualTo(ResourceMember.UserType.OWNER);

        owners = telegrafsApi.getOwners(telegrafConfig);
        Assertions.assertThat(owners).hasSize(2);
        Assertions.assertThat(owners.get(1).getRole()).isEqualTo(ResourceMember.UserType.OWNER);
        Assertions.assertThat(owners.get(1).getUserID()).isEqualTo(user.getId());
        Assertions.assertThat(owners.get(1).getUserName()).isEqualTo(user.getName());

        telegrafsApi.deleteOwner(user, telegrafConfig);

        owners = telegrafsApi.getOwners(telegrafConfig);
        Assertions.assertThat(owners).hasSize(1);
    }

    @Test
    void labels() {

        LabelsApi labelsApi = influxDBClient.getLabelsApi();

        TelegrafConfig telegrafConfig = telegrafsApi
                .createTelegrafConfig(generateName("tc"), "test-config", organization, 1_000, newCpuPlugin(), newOutputPlugin());

        Map<String, String> properties = new HashMap<>();
        properties.put("color", "green");
        properties.put("location", "west");

        Label label = labelsApi.createLabel(generateName("Cool Resource"), properties);

        List<Label> labels = telegrafsApi.getLabels(telegrafConfig);
        Assertions.assertThat(labels).hasSize(0);

        Label addedLabel = telegrafsApi.addLabel(label, telegrafConfig);
        Assertions.assertThat(addedLabel).isNotNull();
        Assertions.assertThat(addedLabel.getId()).isEqualTo(label.getId());
        Assertions.assertThat(addedLabel.getName()).isEqualTo(label.getName());
        Assertions.assertThat(addedLabel.getProperties()).isEqualTo(label.getProperties());

        labels = telegrafsApi.getLabels(telegrafConfig);
        Assertions.assertThat(labels).hasSize(1);
        Assertions.assertThat(labels.get(0).getId()).isEqualTo(label.getId());
        Assertions.assertThat(labels.get(0).getName()).isEqualTo(label.getName());

        telegrafsApi.deleteLabel(label, telegrafConfig);

        labels = telegrafsApi.getLabels(telegrafConfig);
        Assertions.assertThat(labels).hasSize(0);
    }

    @Test
    void labelAddNotExists() {

        TelegrafConfig telegrafConfig = telegrafsApi
                .createTelegrafConfig(generateName("tc"), "test-config", organization, 1_000, newCpuPlugin(), newOutputPlugin());

        Assertions.assertThatThrownBy(() -> telegrafsApi.addLabel("020f755c3c082000", telegrafConfig.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void labelDeleteNotExists() {

        TelegrafConfig telegrafConfig = telegrafsApi
                .createTelegrafConfig(generateName("tc"), "test-config", organization, 1_000, newCpuPlugin(), newOutputPlugin());

        telegrafsApi.deleteLabel("020f755c3c082000", telegrafConfig.getId());
    }

    @Nonnull
    private TelegrafPlugin newCpuPlugin() {

        TelegrafPlugin cpu = new TelegrafPlugin();
        cpu.setName("cpu");
        cpu.setType(TelegrafPluginType.INPUT);

        return cpu;
    }

    @Nonnull
    private TelegrafPlugin newOutputPlugin() {

        TelegrafPlugin output = new TelegrafPlugin();
        output.setName("influxdb_v2");
        output.setType(TelegrafPluginType.OUTPUT);
        output.setComment("Output to Influx 2.0");
        output.getConfig().put("organization", "my-org");
        output.getConfig().put("bucket", "my-bucket");
        output.getConfig().put("urls", new String[]{"http://127.0.0.1:9999"});
        output.getConfig().put("token", "$INFLUX_TOKEN");

        return output;
    }

}