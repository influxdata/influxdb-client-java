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

import com.influxdb.query.dsl.Flux;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (25/06/2018 13:54)
 */
@RunWith(JUnitPlatform.class)
class SortFluxTest {

    @Test
    void sortByColumnsCollection() {

        List<String> sortBy = new ArrayList<>();
        sortBy.add("region");
        sortBy.add("host");

        Flux flux = Flux
                .from("telegraf")
                .sort(sortBy);

        Assertions.assertThat(flux.toString())
                .isEqualToIgnoringWhitespace("from(bucket:\"telegraf\") |> sort(columns: [\"region\", \"host\"])");
    }

    @Test
    void sortByColumnsCollectionEmpty() {

        Flux flux = Flux
                .from("telegraf")
                .sort(new ArrayList<>(), false);

        Assertions.assertThat(flux.toString())
                .isEqualToIgnoringWhitespace("from(bucket:\"telegraf\") |> sort(desc: false)");
    }

    @Test
    void sortByColumnsArray() {

        Flux flux = Flux
                .from("telegraf")
                .sort(new String[]{"region", "value"});

        Assertions.assertThat(flux.toString())
                .isEqualToIgnoringWhitespace("from(bucket:\"telegraf\") |> sort(columns: [\"region\", \"value\"])");
    }

    @Test
    void descendingSort() {
        Flux flux = Flux
                .from("telegraf")
                .sort(true);

        Assertions.assertThat(flux.toString())
                .isEqualToIgnoringWhitespace("from(bucket:\"telegraf\") |> sort(desc: true)");
    }

    @Test
    void descendingSortFalse() {

        Flux flux = Flux
                .from("telegraf")
                .sort(false);

        Assertions.assertThat(flux.toString())
                .isEqualToIgnoringWhitespace("from(bucket:\"telegraf\") |> sort(desc: false)");
    }

    @Test
    void sortByColumnsDescending() {

        Flux flux = Flux
                .from("telegraf")
                .sort(new String[]{"region", "value"}, true);

        String expected = "from(bucket:\"telegraf\") |> sort(columns: [\"region\", \"value\"], desc: true)";

        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void sortByColumnsAscending() {

        List<String> sortBy = new ArrayList<>();
        sortBy.add("region");
        sortBy.add("host");

        Flux flux = Flux
                .from("telegraf")
                .sort(sortBy, false);

        String expected = "from(bucket:\"telegraf\") |> sort(columns: [\"region\", \"host\"], desc: false)";

        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void sortByParameters() {

        Flux flux = Flux
                .from("telegraf")
                .sort()
                .withPropertyNamed("columns", "columnsParameter")
                .withPropertyNamed("desc", "descParameter");

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("columnsParameter", new String[]{"region", "tag"});
        parameters.put("descParameter", false);

        String expected = "from(bucket:\"telegraf\") |> sort(columns: [\"region\", \"tag\"], desc: false)";

        Assertions
                .assertThat(flux.toString(parameters))
                .isEqualToIgnoringWhitespace(expected);
    }
}