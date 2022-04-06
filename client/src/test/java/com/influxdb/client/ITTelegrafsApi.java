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

import com.influxdb.client.domain.Label;
import com.influxdb.client.domain.Organization;
import com.influxdb.client.domain.ResourceMember;
import com.influxdb.client.domain.ResourceOwner;
import com.influxdb.client.domain.Telegraf;
import com.influxdb.client.domain.TelegrafPlugin;
import com.influxdb.client.domain.TelegrafPluginRequest;
import com.influxdb.client.domain.User;
import com.influxdb.exceptions.NotFoundException;

import com.moandjiezana.toml.Toml;
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
    void createTelegrafConfig() {

        String name = generateName("TelegrafConfig");

        TelegrafPlugin output = newOutputPlugin();
        TelegrafPlugin cpu = newCpuPlugin();

        Telegraf telegrafConfig = telegrafsApi
                .createTelegraf(name, "test-config", organization, Arrays.asList(cpu, output));

        Assertions.assertThat(telegrafConfig).isNotNull();
        Assertions.assertThat(telegrafConfig.getName()).isEqualTo(name);
        Assertions.assertThat(telegrafConfig.getDescription()).isEqualTo("test-config");
        Assertions.assertThat(telegrafConfig.getOrgID()).isEqualTo(organization.getId());

        Assertions.assertThat(telegrafConfig.getMetadata()).isNotNull();
        Assertions.assertThat(telegrafConfig.getMetadata().getBuckets()).isNotNull();
        Assertions.assertThat(telegrafConfig.getMetadata().getBuckets()).hasSize(1);

        Toml toml = new Toml().read(telegrafConfig.getConfig());

        Toml agent = toml.getTable("agent");
        Assertions.assertThat(agent).isNotNull();
        Assertions.assertThat(agent.getString("interval")).isEqualTo("10s");
        Assertions.assertThat(agent.getBoolean("round_interval")).isTrue();
        Assertions.assertThat(agent.getLong("metric_batch_size")).isEqualTo(1000);
        Assertions.assertThat(agent.getLong("metric_buffer_limit")).isEqualTo(10000);
        Assertions.assertThat(agent.getString("collection_jitter")).isEqualTo("0s");
        Assertions.assertThat(agent.getString("flush_jitter")).isEqualTo("0s");
        Assertions.assertThat(agent.getString("precision")).isEqualTo("");
        Assertions.assertThat(agent.getBoolean("omit_hostname")).isFalse();

        List<HashMap<String, Object>> outputs = toml.getList("outputs.influxdb_v2");
        Assertions.assertThat(outputs).hasSize(1);
        Assertions.assertThat(outputs.get(0)).hasEntrySatisfying("bucket", value -> Assertions.assertThat(value).isEqualTo("my-bucket"));
        Assertions.assertThat(outputs.get(0)).hasEntrySatisfying("organization", value -> Assertions.assertThat(value).isEqualTo("my-org"));
        Assertions.assertThat(outputs.get(0)).hasEntrySatisfying("token", value -> Assertions.assertThat(value).isEqualTo("$INFLUX_TOKEN"));
        Assertions.assertThat(outputs.get(0)).hasEntrySatisfying("urls", value -> Assertions.assertThat(value).isEqualTo(Arrays.asList("http://127.0.0.1:9999")));

        List<HashMap<String, Object>> inputs = toml.getList("inputs.cpu");
        Assertions.assertThat(inputs).hasSize(1);
        Assertions.assertThat(inputs.get(0)).hasEntrySatisfying("totalcpu", value -> Assertions.assertThat(value).isEqualTo(true));
        Assertions.assertThat(inputs.get(0)).hasEntrySatisfying("collect_cpu_time", value -> Assertions.assertThat(value).isEqualTo(false));
        Assertions.assertThat(inputs.get(0)).hasEntrySatisfying("report_active", value -> Assertions.assertThat(value).isEqualTo(false));
        Assertions.assertThat(inputs.get(0)).hasEntrySatisfying("percpu", value -> Assertions.assertThat(value).isEqualTo(true));
    }

    @Test
    void pluginWithoutConfiguration() {

        Telegraf telegrafConfig = telegrafsApi
                .createTelegraf(generateName("tc"), "test-config", organization, Arrays.asList(newOutputPlugin(), newKernelPlugin()));

        Toml toml = new Toml().read(telegrafConfig.getConfig());
        Assertions.assertThat(toml.contains("inputs.kernel")).isTrue();
    }

    @Test
    void updateTelegrafConfig() {

        Telegraf telegrafConfig = telegrafsApi
                .createTelegraf(generateName("tc"), "test-config", organization, Arrays.asList(newOutputPlugin(), newKernelPlugin()));

        Assertions.assertThat(telegrafConfig.getConfig()).isNotEqualTo("my-updated-config");
        telegrafConfig.setDescription("updated");
        telegrafConfig.setConfig("my-updated-config");

        TelegrafPluginRequest updated = new TelegrafPluginRequest()
                .name(telegrafConfig.getName())
                .description("updated")
                .metadata(telegrafConfig.getMetadata())
                .orgID(telegrafConfig.getOrgID());

        telegrafConfig = telegrafsApi.updateTelegraf(telegrafConfig.getId(), updated);

        Assertions.assertThat(telegrafConfig.getDescription()).isEqualTo("updated");
    }

    @Test
    void deleteTelegrafConfig() {

        Telegraf createdConfig = telegrafsApi
                .createTelegraf(generateName("tc"), "test-config", organization, Arrays.asList(newOutputPlugin(), newCpuPlugin()));
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
                .createTelegraf(generateName("tc"), "test-config", organization, Arrays.asList(newOutputPlugin(), newCpuPlugin()));

        Telegraf telegrafConfigByID = telegrafsApi.findTelegrafByID(telegrafConfig.getId());

        Assertions.assertThat(telegrafConfigByID).isNotNull();
        Assertions.assertThat(telegrafConfigByID.getId()).isEqualTo(telegrafConfig.getId());
        Assertions.assertThat(telegrafConfigByID.getName()).isEqualTo(telegrafConfig.getName());
        Assertions.assertThat(telegrafConfigByID.getOrgID()).isEqualTo(telegrafConfig.getOrgID());
        Assertions.assertThat(telegrafConfigByID.getDescription()).isEqualTo(telegrafConfig.getDescription());
        Assertions.assertThat(telegrafConfigByID.getConfig()).isEqualTo(telegrafConfig.getConfig());
        Assertions.assertThat(telegrafConfigByID.getMetadata().getBuckets()).isEqualTo(telegrafConfig.getMetadata().getBuckets());
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
                .createTelegraf(generateName("tc"), "test-config", organization, Arrays.asList(newOutputPlugin(), newCpuPlugin()));

        telegrafConfigs = telegrafsApi.findTelegrafsByOrg(organization);

        Assertions.assertThat(telegrafConfigs).hasSize(1);

        telegrafsApi.deleteTelegraf(telegrafConfigs.get(0));
    }

    @Test
    void findTelegrafConfigs() {

        int size = telegrafsApi.findTelegrafs().size();

        telegrafsApi
                .createTelegraf(generateName("tc"), "test-config", organization, Arrays.asList(newOutputPlugin(), newCpuPlugin()));

        List<Telegraf> telegrafConfigs = telegrafsApi.findTelegrafs();
        Assertions.assertThat(telegrafConfigs).hasSize(size + 1);
    }

    @Test
    void getTOML() {

        Telegraf telegrafConfig = telegrafsApi
                .createTelegraf(generateName("tc"), "test-config", organization, Arrays.asList(newOutputPlugin(), newCpuPlugin()));

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
                .createTelegraf(generateName("tc"), "test-config", organization, Arrays.asList(newOutputPlugin(), newCpuPlugin()));

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
    @Disabled
    //TODO https://github.com/influxdata/influxdb/issues/20005
    void owner() {

        Telegraf telegrafConfig = telegrafsApi
                .createTelegraf(generateName("tc"), "test-config", organization, Arrays.asList(newOutputPlugin(), newCpuPlugin()));

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
                .createTelegraf(generateName("tc"), "test-config", organization, Arrays.asList(newOutputPlugin(), newCpuPlugin()));

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
                .createTelegraf(generateName("tc"), "test-config", organization, Arrays.asList(newOutputPlugin(), newCpuPlugin()));

        Assertions.assertThatThrownBy(() -> telegrafsApi.addLabel("020f755c3c082000", telegrafConfig.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @Disabled("TODO https://github.com/influxdata/influxdb/issues/18409")
    void labelDeleteNotExists() {

        Telegraf telegrafConfig = telegrafsApi
                .createTelegraf(generateName("tc"), "test-config", organization, Arrays.asList(newOutputPlugin(), newCpuPlugin()));

        Assertions.assertThatThrownBy(() -> telegrafsApi.deleteLabel("020f755c3c082000", telegrafConfig.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("label not found");
    }

    @Test
    void cloneTelegrafConfig() {

        Telegraf source = telegrafsApi
                .createTelegraf(generateName("tc"), "test-config", organization, Arrays.asList(newOutputPlugin(), newCpuPlugin()));
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
    private TelegrafPlugin newCpuPlugin() {
        return new TelegrafPlugin()
                .type(TelegrafPlugin.TypeEnum.INPUT)
                .name("cpu")
                .putConfigItem("percpu", true)
                .putConfigItem("totalcpu", true)
                .putConfigItem("collect_cpu_time", false)
                .putConfigItem("report_active", false);
    }

    @Nonnull
    private TelegrafPlugin newKernelPlugin() {
        return new TelegrafPlugin().type(TelegrafPlugin.TypeEnum.INPUT).name("kernel");
    }

    @Nonnull
    private TelegrafPlugin newOutputPlugin() {

        return new TelegrafPlugin()
                .type(TelegrafPlugin.TypeEnum.OUTPUT)
                .name("influxdb_v2")
                .description("my instance")
                .putConfigItem("organization", "my-org")
                .putConfigItem("bucket", "my-bucket")
                .putConfigItem("token", "$INFLUX_TOKEN")
                .putConfigItem("urls", Collections.singletonList("http://127.0.0.1:9999"));
    }
}