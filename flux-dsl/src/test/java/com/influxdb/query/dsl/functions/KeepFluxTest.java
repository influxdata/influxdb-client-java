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

import com.influxdb.query.dsl.Flux;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (02/08/2018 11:43)
 */
@RunWith(JUnitPlatform.class)
class KeepFluxTest {

    @Test
    void keepByArray() {

        Flux flux = Flux
                .from("telegraf")
                .keep(new String[]{"host", "_measurement"});

        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace("from(bucket:\"telegraf\") |> keep(columns: [\"host\", \"_measurement\"])");
    }

    @Test
    void keepByCollectionArray() {

        Collection<String> columns = new ArrayList<>();
        columns.add("host");
        columns.add("_value");

        Flux flux = Flux
                .from("telegraf")
                .keep(columns);

        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace("from(bucket:\"telegraf\") |> keep(columns: [\"host\", \"_value\"])");
    }

    @Test
    void keepByFunction() {

        Flux flux = Flux
                .from("telegraf")
                .keep("column =~ /usage*/");

        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace("from(bucket:\"telegraf\") |> keep(fn: (column) => column =~ /usage*/)");
    }

    @Test
    void keepByParameters() {

        Flux flux = Flux
                .from("telegraf")
                .keep()
                .withFunction("column =~ /inodes*/");

        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace("from(bucket:\"telegraf\") |> keep(fn: (column) => column =~ /inodes*/)");
    }
}