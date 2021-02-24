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
import java.util.HashMap;
import java.util.List;

import com.influxdb.query.dsl.AbstractFluxTest;
import com.influxdb.query.dsl.Flux;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (25/06/2018 15:16)
 */
@RunWith(JUnitPlatform.class)
class GroupFluxTest extends AbstractFluxTest {

    @Test
    void group() {

        Flux flux = Flux
                .from("telegraf")
                .group();

        Flux.Query query = flux.toQuery();
        Assertions.assertThat(query.flux)
                .isEqualToIgnoringWhitespace("from(bucket: v0) |> group()");
        assertVariables(query, "v0", "\"telegraf\"");
    }

    @Test
    void groupByString() {

        Flux flux = Flux
                .from("telegraf")
                .groupBy("location");

        Flux.Query query = flux.toQuery();
        Assertions.assertThat(query.flux)
                .isEqualToIgnoringWhitespace("from(bucket:v0) |> group(columns: v1)");
        assertVariables(query,
                "v0", "\"telegraf\"",
                "v1", new String[]{"location"});
    }

    @Test
    void groupByCollection() {

        List<String> groupBy = new ArrayList<>();
        groupBy.add("region");
        groupBy.add("host");

        Flux flux = Flux
                .from("telegraf")
                .groupBy(groupBy);

        Flux.Query query = flux.toQuery();
        Assertions.assertThat(query.flux)
                .isEqualToIgnoringWhitespace("from(bucket:v0) |> group(columns: v1)");
        assertVariables(query, "v0", "\"telegraf\"", "v1", groupBy);
    }

    @Test
    void groupByParameter() {

        Flux flux = Flux
                .from("telegraf")
                .group()
                .withPropertyNamed("columns", "groupByParameter");

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("groupByParameter", new String[]{"region", "zip"});

        Flux.Query query = flux.toQuery(parameters);
        Assertions.assertThat(query.flux)
                .isEqualToIgnoringWhitespace("from(bucket:v1) |> group(columns: groupByParameter)");
        assertVariables(query,
                "v1", "\"telegraf\"",
                "groupByParameter", new String[]{"region", "zip"});
    }

    @Test
    void groupByArray() {

        Flux flux = Flux
                .from("telegraf")
                .groupBy(new String[]{"region", "value"});

        Flux.Query query = flux.toQuery();
        Assertions.assertThat(query.flux)
                .isEqualToIgnoringWhitespace("from(bucket:v0) |> group(columns: v1)");
        assertVariables(query,
                "v0", "\"telegraf\"",
                "v1", new String[]{"region", "value"});
    }


    @Test
    void groupExceptCollection() {

        List<String> groupBy = new ArrayList<>();
        groupBy.add("region");
        groupBy.add("host");

        Flux flux = Flux
                .from("telegraf")
                .groupExcept(groupBy);

        Flux.Query query = flux.toQuery();
        Assertions.assertThat(query.flux)
                .isEqualToIgnoringWhitespace("from(bucket:v0) |> group(columns: v1, mode: v2)");
        assertVariables(query,
                "v0", "\"telegraf\"",
                "v1", groupBy,
                "v2", "\"except\"");
    }

    @Test
    void groupModeParameter() {

        Flux flux = Flux
                .from("telegraf")
                .group()
                .withPropertyNamed("columns", "columns")
                .withPropertyValueEscaped("mode", "except");

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("columns", new String[]{"region", "zip"});

        Flux.Query query = flux.toQuery(parameters);
        Assertions.assertThat(query.flux)
                .isEqualToIgnoringWhitespace("from(bucket:v1) |> group(columns: columns, mode: v2)");

        assertVariables(query,
                "v1", "\"telegraf\"",
                "columns", new String[]{"region", "zip"},
                "v2", "\"except\"");
    }

    @Test
    void groupExcept() {

        Flux flux = Flux
                .from("telegraf")
                .groupExcept("region");

        Flux.Query query = flux.toQuery();
        Assertions.assertThat(query.flux)
                .isEqualToIgnoringWhitespace("from(bucket:v0) |> group(columns: v1, mode: v2)");

        assertVariables(query,
                "v0", "\"telegraf\"",
                "v1", new String[]{"region"},
                "v2", "\"except\"");
    }

    @Test
    void groupExceptArray() {

        Flux flux = Flux
                .from("telegraf")
                .groupExcept(new String[]{"region", "value"});

        Flux.Query query = flux.toQuery();
        Assertions.assertThat(query.flux)
                .isEqualToIgnoringWhitespace("from(bucket:v0) |> group(columns: v1, mode: v2)");

        assertVariables(query,
                "v0", "\"telegraf\"",
                "v1", new String[]{"region", "value"},
                "v2", "\"except\"");
    }

}