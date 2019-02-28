package org.influxdata.client;

import org.influxdata.LogLevel;
import org.influxdata.client.domain.Organization;
import org.influxdata.client.domain.TelegrafConfig;
import org.influxdata.client.domain.TelegrafPlugin;
import org.influxdata.client.domain.TelegrafPluginType;

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

        influxDBClient.setLogLevel(LogLevel.BODY);
    }

    @Test
    void createTelegrafsConfig() {

        String name = generateName("TelegrafConfig");

        TelegrafPlugin output = new TelegrafPlugin();
        output.setName("influxdb_v2");
        output.setType(TelegrafPluginType.OUTPUT);
        output.setComment("Output to Influx 2.0");
        output.getConfig().put("organization", "my-org");
        output.getConfig().put("bucket", "my-bucket");
        output.getConfig().put("urls", new String[]{"http://127.0.0.1:9999"});
        output.getConfig().put("token", "$INFLUX_TOKEN");

        TelegrafPlugin cpu = new TelegrafPlugin();
        cpu.setName("cpu");
        cpu.setType(TelegrafPluginType.INPUT);

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
}