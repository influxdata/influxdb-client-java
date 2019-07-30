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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

import com.influxdb.LogLevel;
import com.influxdb.client.domain.Label;
import com.influxdb.client.domain.Organization;
import com.influxdb.client.domain.ResourceMember;
import com.influxdb.client.domain.ResourceOwner;
import com.influxdb.client.domain.Telegraf;
import com.influxdb.client.domain.TelegrafPluginInputCpu;
import com.influxdb.client.domain.TelegrafPluginInputKernel;
import com.influxdb.client.domain.TelegrafPluginOutputInfluxDBV2;
import com.influxdb.client.domain.TelegrafPluginOutputInfluxDBV2Config;
import com.influxdb.client.domain.TelegrafRequestPlugin;
import com.influxdb.client.domain.User;
import com.influxdb.exceptions.NotFoundException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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

        telegrafsApi.findTelegrafs().forEach(telegrafConfig -> telegrafsApi.deleteTelegraf(telegrafConfig));
    }

    @Test
    @Disabled
    //TODO https://github.com/influxdata/influxdb/issues/14062
    void createTelegrafConfig() {

        influxDBClient.setLogLevel(LogLevel.BODY);

        String name = generateName("TelegrafConfig");

        TelegrafRequestPlugin output = newOutputPlugin();
        TelegrafRequestPlugin cpu = newCpuPlugin();

        Telegraf telegrafConfig = telegrafsApi
                .createTelegraf(name, "test-config", organization, 1_000, output, cpu);

        Assertions.assertThat(telegrafConfig).isNotNull();
        Assertions.assertThat(telegrafConfig.getName()).isEqualTo(name);
        Assertions.assertThat(telegrafConfig.getDescription()).isEqualTo("test-config");
        Assertions.assertThat(telegrafConfig.getOrgID()).isEqualTo(organization.getId());
        Assertions.assertThat(telegrafConfig.getAgent()).isNotNull();
        Assertions.assertThat(telegrafConfig.getAgent().getCollectionInterval()).isEqualTo(1_000);
        Assertions.assertThat(telegrafConfig.getPlugins()).hasSize(2);

        TelegrafPluginOutputInfluxDBV2 outputResult = (TelegrafPluginOutputInfluxDBV2) telegrafConfig.getPlugins().get(0);
        Assertions.assertThat(outputResult.getName()).isEqualTo(TelegrafPluginOutputInfluxDBV2.NameEnum.INFLUXDB_V2);
        Assertions.assertThat(outputResult.getComment()).isEqualTo("Output to Influx 2.0");
        Assertions.assertThat(outputResult.getType()).isEqualTo(TelegrafRequestPlugin.TypeEnum.OUTPUT);
        
        TelegrafPluginInputCpu cpuResult = (TelegrafPluginInputCpu) telegrafConfig.getPlugins().get(1);
        Assertions.assertThat(cpuResult.getName()).isEqualTo(TelegrafPluginInputCpu.NameEnum.CPU);
        Assertions.assertThat(cpuResult.getType()).isEqualTo(TelegrafRequestPlugin.TypeEnum.INPUT);

        Assertions.assertThat(telegrafConfig.getLinks().getSelf()).isEqualTo("/api/v2/telegrafs/" + telegrafConfig.getId());
        Assertions.assertThat(telegrafConfig.getLinks().getLabels()).isEqualTo("/api/v2/telegrafs/" + telegrafConfig.getId() + "/labels");
        Assertions.assertThat(telegrafConfig.getLinks().getMembers()).isEqualTo("/api/v2/telegrafs/" + telegrafConfig.getId() + "/members");
        Assertions.assertThat(telegrafConfig.getLinks().getOwners()).isEqualTo("/api/v2/telegrafs/" + telegrafConfig.getId() + "/owners");
    }

    @Test
    @Disabled
    //TODO https://github.com/influxdata/influxdb/issues/12672
    void createTelegrafWithCustomPlugin() {

        String name = generateName("TelegrafConfig");

        TelegrafRequestPlugin output = newOutputPlugin();

        TelegrafRequestPlugin<String, Map<String, String>> custom = new TelegrafRequestPlugin<>();
        custom.setName("ping-plugin");
        custom.setType(TelegrafRequestPlugin.TypeEnum.INPUT);

        HashMap<String, String> config = new HashMap<>();
        config.put("bin", "/sbin/ping");
        config.put("count", "10");
        config.put("host", "8.8.8.8");
        custom.setConfig(config);

        Telegraf telegrafConfig = telegrafsApi
                .createTelegraf(name, "test-config", organization, 1_000, output, custom);

        Assertions.assertThat(telegrafConfig.getPlugins()).hasSize(2);
        TelegrafRequestPlugin telegrafPlugin = telegrafConfig.getPlugins().get(1);
        Assertions.assertThat(telegrafPlugin.getName()).isEqualTo("ping-plugin");
        Assertions.assertThat(telegrafPlugin.getType()).isEqualTo(TelegrafRequestPlugin.TypeEnum.INPUT);

        Map<String, String> configResponse = (Map<String, String>) telegrafPlugin.getConfig();
        Assertions.assertThat(configResponse)
                .hasEntrySatisfying("bin", value -> Assertions.assertThat(value).isEqualTo("/sbin/ping"))
                .hasEntrySatisfying("count", value -> Assertions.assertThat(value).isEqualTo("10"))
                .hasEntrySatisfying("host", value -> Assertions.assertThat(value).isEqualTo("8.8.8.8"));
    }

    @Test
    void pluginWithoutConfiguration() {

        String name = generateName("TelegrafConfig");

        TelegrafRequestPlugin output = newOutputPlugin();
        TelegrafRequestPlugin kernel = newKernelPlugin();

        Telegraf telegrafConfig = telegrafsApi
                .createTelegraf(name, "test-config", organization, 1_000, output, kernel);

        Assertions.assertThat(telegrafConfig.getPlugins()).hasSize(2);
    }

    @Test
    void updateTelegrafConfig() {

        Telegraf telegrafConfig = telegrafsApi
                .createTelegraf(generateName("tc"), "test-config", organization, 1_000, newCpuPlugin(), newOutputPlugin());

        telegrafConfig.setDescription("updated");
        telegrafConfig.getAgent().setCollectionInterval(500);
        telegrafConfig.getPlugins().remove(0);

        telegrafConfig = telegrafsApi.updateTelegraf(telegrafConfig);

        Assertions.assertThat(telegrafConfig.getDescription()).isEqualTo("updated");
        Assertions.assertThat(telegrafConfig.getAgent().getCollectionInterval()).isEqualTo(500);
        Assertions.assertThat(telegrafConfig.getPlugins()).hasSize(1);
    }

    @Test
    void deleteTelegrafConfig() {

        List<TelegrafRequestPlugin> plugins = Arrays.asList(newOutputPlugin(), newCpuPlugin());

        Telegraf createdConfig = telegrafsApi
                .createTelegraf(generateName("tc"), "test-config", organization, 1_000, plugins);
        Assertions.assertThat(createdConfig).isNotNull();

        Telegraf foundTelegrafConfig = telegrafsApi.findTelegrafByID(createdConfig.getId());
        Assertions.assertThat(foundTelegrafConfig).isNotNull();

        // delete source
        telegrafsApi.deleteTelegraf(createdConfig);

        Assertions.assertThatThrownBy(() -> telegrafsApi.findTelegrafByID(createdConfig.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("telegraf configuration not found");
    }

    @Test
    void deleteTelegrafConfigNotFound() {

        Assertions.assertThatThrownBy(() -> telegrafsApi.deleteTelegraf("020f755c3d082000"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("telegraf configuration not found");
    }

    @Test
    void findTelegrafConfigByID() {

        Telegraf telegrafConfig = telegrafsApi
                .createTelegraf(generateName("tc"), "test-config", organization, 1_000, newCpuPlugin(), newOutputPlugin());

        Telegraf telegrafConfigByID = telegrafsApi.findTelegrafByID(telegrafConfig.getId());

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

        Assertions.assertThatThrownBy(() -> telegrafsApi.findTelegrafByID("020f755c3d082000"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("telegraf configuration not found");
    }

    @Test
    void findTelegrafConfigByOrg() {

        String orgName = generateName("Constant Pro");

        Organization organization = influxDBClient.getOrganizationsApi().createOrganization(orgName);
        List<Telegraf> telegrafConfigs = telegrafsApi.findTelegrafsByOrg(organization);

        Assertions.assertThat(telegrafConfigs).hasSize(0);

        telegrafsApi
                .createTelegraf(generateName("tc"), "test-config", organization, 1_000, newCpuPlugin(), newOutputPlugin());

        telegrafConfigs = telegrafsApi.findTelegrafsByOrg(organization);

        Assertions.assertThat(telegrafConfigs).hasSize(1);

        telegrafsApi.deleteTelegraf(telegrafConfigs.get(0));
    }

    @Test
    void findTelegrafConfigs() {

        int size = telegrafsApi.findTelegrafs().size();

        telegrafsApi
                .createTelegraf(generateName("tc"), "test-config", organization, 1_000, newCpuPlugin(), newOutputPlugin());

        List<Telegraf> telegrafConfigs = telegrafsApi.findTelegrafs();
        Assertions.assertThat(telegrafConfigs).hasSize(size + 1);
    }

    @Test
    void getTOML() {

        Telegraf telegrafConfig = telegrafsApi
                .createTelegraf(generateName("tc"), "test-config", organization, 1_000, newCpuPlugin(), newOutputPlugin());

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

        Telegraf telegrafConfig = telegrafsApi
                .createTelegraf(generateName("tc"), "test-config", organization, 1_000, newCpuPlugin(), newOutputPlugin());

        List<ResourceMember> members = telegrafsApi.getMembers(telegrafConfig);
        Assertions.assertThat(members).hasSize(0);

        User user = usersApi.createUser(generateName("Luke Health"));

        ResourceMember resourceMember = telegrafsApi.addMember(user, telegrafConfig);
        Assertions.assertThat(resourceMember).isNotNull();
        Assertions.assertThat(resourceMember.getId()).isEqualTo(user.getId());
        Assertions.assertThat(resourceMember.getName()).isEqualTo(user.getName());
        Assertions.assertThat(resourceMember.getRole()).isEqualTo(ResourceMember.RoleEnum.MEMBER);

        members = telegrafsApi.getMembers(telegrafConfig);
        Assertions.assertThat(members).hasSize(1);
        Assertions.assertThat(members.get(0).getRole()).isEqualTo(ResourceMember.RoleEnum.MEMBER);
        Assertions.assertThat(members.get(0).getId()).isEqualTo(user.getId());
        Assertions.assertThat(members.get(0).getName()).isEqualTo(user.getName());

        telegrafsApi.deleteMember(user, telegrafConfig);

        members = telegrafsApi.getMembers(telegrafConfig);
        Assertions.assertThat(members).hasSize(0);
    }

    @Test
    void owner() {

        Telegraf telegrafConfig = telegrafsApi
                .createTelegraf(generateName("tc"), "test-config", organization, 1_000, newCpuPlugin(), newOutputPlugin());

        List<ResourceOwner> owners = telegrafsApi.getOwners(telegrafConfig);
        Assertions.assertThat(owners).hasSize(1);
        Assertions.assertThat(owners.get(0).getName()).isEqualTo("my-user");

        User user = usersApi.createUser(generateName("Luke Health"));

        ResourceOwner resourceMember = telegrafsApi.addOwner(user, telegrafConfig);
        Assertions.assertThat(resourceMember).isNotNull();
        Assertions.assertThat(resourceMember.getId()).isEqualTo(user.getId());
        Assertions.assertThat(resourceMember.getName()).isEqualTo(user.getName());
        Assertions.assertThat(resourceMember.getRole()).isEqualTo(ResourceOwner.RoleEnum.OWNER);

        owners = telegrafsApi.getOwners(telegrafConfig);
        Assertions.assertThat(owners).hasSize(2);
        Assertions.assertThat(owners.get(1).getRole()).isEqualTo(ResourceOwner.RoleEnum.OWNER);
        Assertions.assertThat(owners.get(1).getId()).isEqualTo(user.getId());
        Assertions.assertThat(owners.get(1).getName()).isEqualTo(user.getName());

        telegrafsApi.deleteOwner(user, telegrafConfig);

        owners = telegrafsApi.getOwners(telegrafConfig);
        Assertions.assertThat(owners).hasSize(1);
    }

    @Test
    void labels() {

        LabelsApi labelsApi = influxDBClient.getLabelsApi();

        Telegraf telegrafConfig = telegrafsApi
                .createTelegraf(generateName("tc"), "test-config", organization, 1_000, newCpuPlugin(), newOutputPlugin());

        Map<String, String> properties = new HashMap<>();
        properties.put("color", "green");
        properties.put("location", "west");

        Label label = labelsApi.createLabel(generateName("Cool Resource"), properties, organization.getId());

        List<Label> labels = telegrafsApi.getLabels(telegrafConfig);
        Assertions.assertThat(labels).hasSize(0);

        Label addedLabel = telegrafsApi.addLabel(label, telegrafConfig).getLabel();
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

        Telegraf telegrafConfig = telegrafsApi
                .createTelegraf(generateName("tc"), "test-config", organization, 1_000, newCpuPlugin(), newOutputPlugin());

        Assertions.assertThatThrownBy(() -> telegrafsApi.addLabel("020f755c3c082000", telegrafConfig.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void labelDeleteNotExists() {

        Telegraf telegrafConfig = telegrafsApi
                .createTelegraf(generateName("tc"), "test-config", organization, 1_000, newCpuPlugin(), newOutputPlugin());

        telegrafsApi.deleteLabel("020f755c3c082000", telegrafConfig.getId());
    }

    @Test
    @Disabled
    //TODO https://github.com/influxdata/influxdb/issues/14062
    void cloneTelegrafConfig() {

        Telegraf source = telegrafsApi
                .createTelegraf(generateName("tc"), "test-config", organization, 1_000, newCpuPlugin(), newOutputPlugin());

        String name = generateName("cloned");

        Map<String, String> properties = new HashMap<>();
        properties.put("color", "green");
        properties.put("location", "west");

        Label label = influxDBClient.getLabelsApi().createLabel(generateName("Cool Resource"), properties, organization.getId());

        telegrafsApi.addLabel(label, source);

        Telegraf cloned = telegrafsApi.cloneTelegraf(name, source.getId());

        Assertions.assertThat(cloned.getName()).isEqualTo(name);
        Assertions.assertThat(cloned.getOrgID()).isEqualTo(organization.getId());
        Assertions.assertThat(cloned.getDescription()).isEqualTo(source.getDescription());
        Assertions.assertThat(cloned.getAgent().getCollectionInterval()).isEqualTo(source.getAgent().getCollectionInterval());
        Assertions.assertThat(cloned.getPlugins()).hasSize(2);
        Assertions.assertThat(cloned.getPlugins().get(0).getName()).isEqualTo(TelegrafPluginInputCpu.NameEnum.CPU);
        Assertions.assertThat(cloned.getPlugins().get(1).getName()).isEqualTo(TelegrafPluginOutputInfluxDBV2.NameEnum.INFLUXDB_V2);

        List<Label> labels = telegrafsApi.getLabels(cloned);
        Assertions.assertThat(labels).hasSize(1);
        Assertions.assertThat(labels.get(0).getId()).isEqualTo(label.getId());
    }

    @Test
    void cloneTelegrafConfigNotFound() {
        Assertions.assertThatThrownBy(() -> telegrafsApi.cloneTelegraf(generateName("cloned"), "020f755c3c082000"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("telegraf configuration not found");
    }

    @Nonnull
    private TelegrafRequestPlugin newCpuPlugin() {

        TelegrafPluginInputCpu cpu = new TelegrafPluginInputCpu();

        return cpu;
    }

    @Nonnull
    private TelegrafRequestPlugin newKernelPlugin() {

        TelegrafPluginInputKernel kernel = new TelegrafPluginInputKernel();

        return kernel;
    }

    @Nonnull
    private TelegrafRequestPlugin newOutputPlugin() {

        TelegrafPluginOutputInfluxDBV2 output = new TelegrafPluginOutputInfluxDBV2();
        output.setComment("Output to Influx 2.0");
        output.setConfig(new TelegrafPluginOutputInfluxDBV2Config());
        output.getConfig().setOrganization("my-org");
        output.getConfig().setBucket("my-bucket");
        output.getConfig().setUrls(Collections.singletonList("http://127.0.0.1:9999"));
        output.getConfig().setToken("$INFLUX_TOKEN");

        return output;
    }

}