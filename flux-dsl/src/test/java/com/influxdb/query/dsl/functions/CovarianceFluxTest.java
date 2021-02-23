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
package com.influxdb.query.dsl.functions;

import java.util.ArrayList;
import java.util.Collection;

import com.influxdb.query.dsl.AbstractFluxTest;
import com.influxdb.query.dsl.Flux;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (17/07/2018 13:51)
 */
@RunWith(JUnitPlatform.class)
class CovarianceFluxTest extends AbstractFluxTest {

    @Test
    void covarianceByColumnsArray() {

        Flux flux = Flux
                .from("telegraf")
                .covariance(new String[]{"_value", "_oldValue"});

        String expected = "from(bucket:v0) |> covariance(columns: v1)";

        Flux.Query query = flux.toQuery();
        Assertions.assertThat(query.flux).isEqualToIgnoringWhitespace(expected);
        assertVariables(query,
                "v0", "\"telegraf\"",
                "v1", new String[]{"_value", "_oldValue"});
    }

    @Test
    void covarianceByColumnsCollection() {

        Collection<String> columns = new ArrayList<>();
        columns.add("_time");
        columns.add("_value");

        Flux flux = Flux
                .from("telegraf")
                .covariance(columns);

        String expected = "from(bucket:v0) |> covariance(columns: v1)";

        Flux.Query query = flux.toQuery();
        Assertions.assertThat(query.flux).isEqualToIgnoringWhitespace(expected);
        assertVariables(query,
                "v0", "\"telegraf\"",
                "v1", columns);
    }

    @Test
    void covarianceByColumnsArrayValueDst() {

        Flux flux = Flux
                .from("telegraf")
                .covariance(new String[]{"_value", "_oldValue"}, "_covValue");

        String expected = "from(bucket:v0) |> covariance(columns: v1, valueDst: v2)";

        Flux.Query query = flux.toQuery();
        Assertions.assertThat(query.flux).isEqualToIgnoringWhitespace(expected);
        assertVariables(query,
                "v0", "\"telegraf\"",
                "v1", new String[]{"_value", "_oldValue"},
                "v2", "\"_covValue\"");
    }

    @Test
    void covarianceByColumnsCollectionValueDst() {

        Collection<String> columns = new ArrayList<>();
        columns.add("_time");
        columns.add("_value");

        Flux flux = Flux
                .from("telegraf")
                .covariance(columns, "_covValue");

        String expected = "from(bucket:v0) |> covariance(columns: v1, valueDst: v2)";

        Flux.Query query = flux.toQuery();
        Assertions.assertThat(query.flux).isEqualToIgnoringWhitespace(expected);
        assertVariables(query,
                "v0", "\"telegraf\"",
                "v1", columns,
                "v2", "\"_covValue\"");
    }

    @Test
    void covarianceByColumnsArrayPearsonrValueDst() {

        Flux flux = Flux
                .from("telegraf")
                .covariance(new String[]{"_value", "_oldValue"}, true, "_covValue");

        String expected = "from(bucket:v0) |> covariance(columns: v1, pearsonr: v2, valueDst: v3)";

        Flux.Query query = flux.toQuery();
        Assertions.assertThat(query.flux).isEqualToIgnoringWhitespace(expected);
        assertVariables(query,
                "v0", "\"telegraf\"",
                "v1", new String[]{"_value", "_oldValue"},
                "v2", true,
                "v3", "\"_covValue\"");
    }

    @Test
    void covarianceByColumnsCollectionPearsonr() {

        Collection<String> columns = new ArrayList<>();
        columns.add("_time");
        columns.add("_value");

        Flux flux = Flux
                .from("telegraf")
                .covariance(columns, false);

        String expected = "from(bucket:v0) |> covariance(columns: v1, pearsonr: v2)";

        Flux.Query query = flux.toQuery();
        Assertions.assertThat(query.flux).isEqualToIgnoringWhitespace(expected);
        assertVariables(query,
                "v0", "\"telegraf\"",
                "v1", columns,
                "v2", false);
    }

    @Test
    void covarianceByColumnsArrayPearsonr() {

        Flux flux = Flux
                .from("telegraf")
                .covariance(new String[]{"_value", "_oldValue"}, true);

        String expected = "from(bucket:v0) |> covariance(columns: v1, pearsonr: v2)";

        Flux.Query query = flux.toQuery();
        Assertions.assertThat(query.flux).isEqualToIgnoringWhitespace(expected);
        assertVariables(query,
                "v0", "\"telegraf\"",
                "v1", new String[]{"_value", "_oldValue"},
                "v2", true);
    }

    @Test
    void covarianceByColumnsCollectionPearsonrValueDst() {

        Collection<String> columns = new ArrayList<>();
        columns.add("_time");
        columns.add("_value");

        Flux flux = Flux
                .from("telegraf")
                .covariance(columns, false, "_covValue");

        String expected = "from(bucket:v0) |> covariance(columns: v1, pearsonr: v2, valueDst: v3)";

        Flux.Query query = flux.toQuery();
        Assertions.assertThat(query.flux).isEqualToIgnoringWhitespace(expected);
        assertVariables(query,
                "v0", "\"telegraf\"",
                "v1", columns,
                "v2", false,
                "v3", "\"_covValue\"");
    }

    @Test
    void covarianceByProperties() {

        Flux flux = Flux
                .from("telegraf")
                .covariance()
                .withColumns(new String[]{"columnA", "columnB"})
                .withPearsonr(true)
                .withValueDst("_newColumn");

        String expected = "from(bucket:v0) |> covariance(columns: v1, pearsonr: v2, valueDst: v3)";

        Flux.Query query = flux.toQuery();
        Assertions.assertThat(query.flux).isEqualToIgnoringWhitespace(expected);
        assertVariables(query,
                "v0", "\"telegraf\"",
                "v1", new String[]{"columnA", "columnB"},
                "v2", true,
                "v3", "\"_newColumn\"");
    }

    @Test
    void expectedExactlyTwoColumnsArray() {

        CovarianceFlux flux = Flux.from("telegraf").covariance();

        Assertions.assertThatThrownBy(() -> flux.withColumns(new String[]{}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Exactly two columns must be provided.");

        Assertions.assertThatThrownBy(() -> flux.withColumns(new String[]{"val1"}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Exactly two columns must be provided.");

        Assertions.assertThatThrownBy(() -> flux.withColumns(new String[]{"val1", "val2", "val3"}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Exactly two columns must be provided.");
    }

    @Test
    void expectedExactlyTwoColumnsCollection() {

        CovarianceFlux flux = Flux.from("telegraf").covariance();

        Collection<String> columns = new ArrayList<>();

        Assertions.assertThatThrownBy(() -> flux.withColumns(columns))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Exactly two columns must be provided.");

        columns.add("val1");
        Assertions.assertThatThrownBy(() -> flux.withColumns(columns))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Exactly two columns must be provided.");

        columns.add("val2");
        columns.add("val3");
        Assertions.assertThatThrownBy(() -> flux.withColumns(columns))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Exactly two columns must be provided.");
    }
}