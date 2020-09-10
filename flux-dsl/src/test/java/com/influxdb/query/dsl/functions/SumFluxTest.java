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

import java.util.HashMap;

import com.influxdb.query.dsl.Flux;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (25/06/2018 10:22)
 */
@RunWith(JUnitPlatform.class)
class SumFluxTest {

    @Test
    void sum() {

        Flux flux = Flux
                .from("telegraf")
                .sum();

        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace("from(bucket:\"telegraf\") |> sum()");
    }

    @Test
    void column() {

        Flux flux = Flux
                .from("telegraf")
                .sum("dif_val");

        Assertions.assertThat(flux.toString())
                .isEqualToIgnoringWhitespace("from(bucket:\"telegraf\") |> sum(column: \"dif_val\")");
    }

    @Test
    void columnByParameter() {

        Flux flux = Flux
                .from("telegraf")
                .sum()
                .withPropertyNamed("column", "parameter");

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("parameter", "\"column_b\"");

        Assertions.assertThat(flux.toString(parameters))
                .isEqualToIgnoringWhitespace("from(bucket:\"telegraf\") |> sum(column:\"column_b\")");
    }
}