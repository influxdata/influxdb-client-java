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
import java.util.List;
import javax.annotation.Nonnull;

import org.influxdata.LogLevel;
import org.influxdata.client.domain.Organization;
import org.influxdata.client.domain.TelegrafConfig;
import org.influxdata.client.domain.TelegrafPlugin;
import org.influxdata.client.domain.TelegrafPluginType;
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
    private Organization organization;

    @BeforeEach
    void setUp() {

        telegrafsApi = influxDBClient.getTelegrafsApi();
        organization = findMyOrg();

        telegrafsApi.findTelegrafConfigs().forEach(telegrafConfig -> telegrafsApi.deleteTelegrafConfig(telegrafConfig));

        influxDBClient.setLogLevel(LogLevel.BODY);
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