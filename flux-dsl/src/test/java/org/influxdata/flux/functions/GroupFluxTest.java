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
package org.influxdata.flux.functions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.influxdata.flux.Flux;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (25/06/2018 15:16)
 */
@RunWith(JUnitPlatform.class)
class GroupFluxTest {

    @Test
    void groupByString() {

        Flux flux = Flux
                .from("telegraf")
                .groupBy("location");

        Assertions.assertThat(flux.toString())
                .isEqualToIgnoringWhitespace("from(bucket:\"telegraf\") |> group(by: [\"location\"])");
    }

    @Test
    void groupByCollection() {

        List<String> groupBy = new ArrayList<>();
        groupBy.add("region");
        groupBy.add("host");

        Flux flux = Flux
                .from("telegraf")
                .groupBy(groupBy);

        Assertions.assertThat(flux.toString())
                .isEqualToIgnoringWhitespace("from(bucket:\"telegraf\") |> group(by: [\"region\", \"host\"])");
    }

    @Test
    void groupByKeepCollection() {

        List<String> groupBy = new ArrayList<>();
        groupBy.add("region");
        groupBy.add("host");

        List<String> keepBy = new ArrayList<>();
        keepBy.add("server");
        keepBy.add("state");

        Flux flux = Flux
                .from("telegraf")
                .groupBy(groupBy, keepBy);

        String expected = "from(bucket:\"telegraf\") |> group(by: [\"region\", \"host\"], keep: [\"server\", \"state\"])";
        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void groupByParameter() {

        Flux flux = Flux
                .from("telegraf")
                .group()
                .withPropertyNamed("by", "groupByParameter");

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("groupByParameter", new String[]{"region", "zip"});

        Assertions.assertThat(flux.toString(parameters))
                .isEqualToIgnoringWhitespace("from(bucket:\"telegraf\") |> group(by: [\"region\", \"zip\"])");
    }

    @Test
    void groupByArray() {

        Flux flux = Flux
                .from("telegraf")
                .groupBy(new String[]{"region", "value"});

        Assertions.assertThat(flux.toString())
                .isEqualToIgnoringWhitespace("from(bucket:\"telegraf\") |> group(by: [\"region\", \"value\"])");
    }


    @Test
    void groupByKeepArray() {

        Flux flux = Flux
                .from("telegraf")
                .groupBy(new String[]{"region", "value"}, new String[]{"server", "rack"});

        String expected = "from(bucket:\"telegraf\") |> group(by: [\"region\", \"value\"], keep: [\"server\", \"rack\"])";
        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void groupByKeepParameter() {

        Flux flux = Flux
                .from("telegraf")
                .group()
                .withPropertyNamed("by", "groupByParameter")
                .withPropertyNamed("keep", "keepByParameter");

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("groupByParameter", new String[]{"region", "zip"});
        parameters.put("keepByParameter", new String[]{"server", "price"});

        String expected = "from(bucket:\"telegraf\") |> group(by: [\"region\", \"zip\"], keep: [\"server\", \"price\"])";

        Assertions.assertThat(flux.toString(parameters)).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void groupExceptCollection() {

        List<String> groupBy = new ArrayList<>();
        groupBy.add("region");
        groupBy.add("host");

        Flux flux = Flux
                .from("telegraf")
                .groupExcept(groupBy);

        Assertions.assertThat(flux.toString())
                .isEqualToIgnoringWhitespace("from(bucket:\"telegraf\") |> group(except: [\"region\", \"host\"])");
    }

    @Test
    void groupExceptKeepCollection() {

        List<String> groupBy = new ArrayList<>();
        groupBy.add("region");
        groupBy.add("host");

        List<String> keepBy = new ArrayList<>();
        keepBy.add("server");
        keepBy.add("state");

        Flux flux = Flux
                .from("telegraf")
                .groupExcept(groupBy, keepBy);

        String expected = "from(bucket:\"telegraf\") |> group(except: [\"region\", \"host\"], keep: [\"server\", \"state\"])";
        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void groupExceptParameter() {

        Flux flux = Flux
                .from("telegraf")
                .group()
                .withPropertyNamed("except", "groupExceptParameter");

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("groupExceptParameter", new String[]{"region", "zip"});

        Assertions.assertThat(flux.toString(parameters))
                .isEqualToIgnoringWhitespace("from(bucket:\"telegraf\") |> group(except: [\"region\", \"zip\"])");
    }

    @Test
    void groupExceptArray() {

        Flux flux = Flux
                .from("telegraf")
                .groupExcept(new String[]{"region", "value"});

        Assertions.assertThat(flux.toString())
                .isEqualToIgnoringWhitespace("from(bucket:\"telegraf\") |> group(except: [\"region\", \"value\"])");
    }


    @Test
    void groupExceptKeepArray() {

        Flux flux = Flux
                .from("telegraf")
                .groupExcept(new String[]{"region", "value"}, new String[]{"server", "rack"});

        String expected = "from(bucket:\"telegraf\") |> group(except: [\"region\", \"value\"], keep: [\"server\", \"rack\"])";
        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void groupExceptKeepParameter() {

        Flux flux = Flux
                .from("telegraf")
                .group()
                .withPropertyNamed("except", "groupExceptParameter")
                .withPropertyNamed("keep", "keepExceptParameter");

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("groupExceptParameter", new String[]{"region", "zip"});
        parameters.put("keepExceptParameter", new String[]{"server", "price"});

        String expected = "from(bucket:\"telegraf\") |> group(except: [\"region\", \"zip\"], keep: [\"server\", \"price\"])";

        Assertions.assertThat(flux.toString(parameters)).isEqualToIgnoringWhitespace(expected);
    }
}