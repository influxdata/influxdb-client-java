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

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import com.influxdb.query.dsl.Flux;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (17/07/2018 12:57)
 */
@RunWith(JUnitPlatform.class)
class DifferenceFluxTest {

    @Test
    void differenceNonNegative() {

        Flux flux = Flux
                .from("telegraf")
                .groupBy("_measurement")
                .difference(false);

        String expected = "from(bucket:\"telegraf\") |> group(columns: [\"_measurement\"]) |> difference(nonNegative: false)";
        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void differenceStringCollection() {

        List<String> columns = new ArrayList<>();
        columns.add("_time");
        columns.add("_value");

        Flux flux = Flux
                .from("telegraf")
                .groupBy("_measurement")
                .difference(columns);

        String expected = "from(bucket:\"telegraf\") |> group(columns: [\"_measurement\"]) |> difference(columns: [\"_time\", \"_value\"])";
        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void differenceArray() {

        Flux flux = Flux
                .from("telegraf")
                .range(-5L, ChronoUnit.MINUTES)
                .difference(new String[]{"_value", "_time"});

        String expected = "from(bucket:\"telegraf\") |> range(start: -5m) |> difference(columns: [\"_value\", \"_time\"])";
        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void differenceStringCollectionNonNegative() {

        List<String> columns = new ArrayList<>();
        columns.add("_time");
        columns.add("_value");

        Flux flux = Flux
                .from("telegraf")
                .groupBy("_measurement")
                .difference(columns, true);

        String expected = "from(bucket:\"telegraf\") |> group(columns: [\"_measurement\"]) |> difference(columns: [\"_time\", \"_value\"], nonNegative: true)";
        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void differenceArrayNonNegative() {

        Flux flux = Flux
                .from("telegraf")
                .groupBy("_measurement")
                .difference(new String[]{"_val", "_time"}, false);

        String expected = "from(bucket:\"telegraf\") |> group(columns: [\"_measurement\"]) |> difference(columns: [\"_val\", \"_time\"], nonNegative: false)";
        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void differenceByProperty() {

        Flux flux = Flux
                .from("telegraf")
                .groupBy("_measurement")
                .difference()
                .withColumns(new String[]{"_value", "_oldValue"});


        String expected = "from(bucket:\"telegraf\") |> group(columns: [\"_measurement\"]) |> difference(columns: [\"_value\", \"_oldValue\"])";
        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void onlyDifference() {

        Flux flux = Flux
                .from("telegraf")
                .groupBy("_measurement")
                .difference();

        String expected = "from(bucket:\"telegraf\") |> group(columns: [\"_measurement\"]) |> difference()";
        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace(expected);
    }
}