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
import java.util.List;

import com.influxdb.query.dsl.Flux;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (10/10/2018 06:59)
 */
@RunWith(JUnitPlatform.class)
class PivotFluxTest {

    @Test
    void pivot() {

        Flux flux = Flux.from("telegraf")
                .pivot()
                    .withRowKey(new String[]{"_time"})
                .withColumnKey(new String[]{"_field"})
                .withValueColumn("_value");

        String expected = "from(bucket:\"telegraf\") |> pivot(rowKey: [\"_time\"], columnKey: [\"_field\"], valueColumn: \"_value\")";

        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void pivotArray() {

        Flux flux = Flux.from("telegraf")
                .pivot(new String[]{"_time"}, new String[]{"_measurement", "_field"}, "_value");

        String expected = "from(bucket:\"telegraf\") |> pivot(rowKey: [\"_time\"], columnKey: [\"_measurement\", \"_field\"], valueColumn: \"_value\")";

        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void pivotCollection() {

        List<String> rowKey = new ArrayList<>();
        rowKey.add("_stop");
        List<String> columnKey = new ArrayList<>();
        columnKey.add("host");

        Flux flux = Flux.from("telegraf")
                .pivot(rowKey, columnKey, "_value");

        String expected = "from(bucket:\"telegraf\") |> pivot(rowKey: [\"_stop\"], columnKey: [\"host\"], valueColumn: \"_value\")";

        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace(expected);
    }
}
