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

import java.util.List;

import org.influxdata.client.domain.Cell;
import org.influxdata.client.domain.Dashboard;
import org.influxdata.client.domain.MarkdownViewProperties;
import org.influxdata.client.domain.Proto;
import org.influxdata.client.domain.ProtoDashboard;
import org.influxdata.client.domain.View;
import org.influxdata.exceptions.NotFoundException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (28/03/2019 10:08)
 */
@RunWith(JUnitPlatform.class)
class ITProtosApiTest extends AbstractITClientTest {

    private ProtosApi protosApi;

    @BeforeEach
    void setUp() {

        protosApi = influxDBClient.getProtosApi();
    }

    @Test
    void protos() {

        List<Proto> protos = protosApi.getProtos();

        Assertions.assertThat(protos).isNotEmpty();

        Proto proto = protos.get(0);
        Assertions.assertThat(proto.getName()).isEqualTo("system");
        Assertions.assertThat(proto.getId()).isNotBlank();
        Assertions.assertThat(proto.getDashboards()).hasSize(1);

        ProtoDashboard protoDashboard = proto.getDashboards().get(0);
        Dashboard dashboard = protoDashboard.getDashboard();
        Assertions.assertThat(dashboard).isNotNull();
        Assertions.assertThat(dashboard.getName()).isEqualTo("System");
        Assertions.assertThat(dashboard.getDescription()).isEqualTo("A collection of useful visualizations for monitoring your System");
        Assertions.assertThat(dashboard.getCells()).hasSize(13);

        Cell cell = dashboard.getCells().get(0);
        Assertions.assertThat(cell.getId()).isNotBlank();
        Assertions.assertThat(cell.getX()).isNotNull();
        Assertions.assertThat(cell.getY()).isNotNull();
        Assertions.assertThat(cell.getH()).isNotNull();
        Assertions.assertThat(cell.getW()).isNotNull();

        Assertions.assertThat(protoDashboard.getViews()).hasSize(13);

        View view = protoDashboard.getViews().entrySet().iterator().next().getValue();
        Assertions.assertThat(view.getId()).isNotBlank();
        Assertions.assertThat(view.getName()).isEqualTo("Name this Cell");
        Assertions.assertThat((MarkdownViewProperties) view.getProperties()).isNotNull();
        Assertions.assertThat(((MarkdownViewProperties) view.getProperties()).getNote()).startsWith("This dashboard gives you an overview of System metrics with metrics from `system`");
    }

    @Test
    void createProtoDashboard() {

        List<Dashboard> dashboards = protosApi.createProtoDashboard(protosApi.getProtos().get(0), findMyOrg());

        Assertions.assertThat(dashboards).hasSize(1);
        Assertions.assertThat(dashboards.get(0)).isNotNull();
        Assertions.assertThat(dashboards.get(0).getName()).isEqualTo("System");
        Assertions.assertThat(dashboards.get(0).getDescription()).isEqualTo("A collection of useful visualizations for monitoring your System");
        Assertions.assertThat(dashboards.get(0).getCells()).hasSize(13);
    }

    @Test
    void createProtoDashboardNotFoundProto() {

        Assertions.assertThatThrownBy(() -> protosApi
                .createProtoDashboard("020f755c3c082000", findMyOrg().getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("proto not found");
    }
}
