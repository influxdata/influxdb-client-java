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

import com.influxdb.query.dsl.AbstractFluxTest;
import com.influxdb.query.dsl.Flux;
import com.influxdb.query.dsl.functions.properties.TimeInterval;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (03/07/2018 15:05)
 */
@RunWith(JUnitPlatform.class)
class DerivativeFluxTest extends AbstractFluxTest {

    @Test
    void derivative() {

        Flux flux = Flux
                .from("telegraf")
                .derivative(1L, ChronoUnit.MINUTES);

        Flux.Query query = flux.toQuery();
        Assertions.assertThat(query.flux).isEqualToIgnoringWhitespace("from(bucket:v0) |> derivative(unit: v1)");
        assertVariables(query,
                "v0", "\"telegraf\"",
                "v1", new TimeInterval(1L, ChronoUnit.MINUTES));
    }

    @Test
    void derivativeByParameters() {

        Flux flux = Flux
                .from("telegraf")
                .derivative()
                .withUnit(10L, ChronoUnit.DAYS)
                .withNonNegative(true)
                .withColumns(new String[]{"time1", "time2"})
                .withTimeColumn("_timeMy");

        Flux.Query query = flux.toQuery();
        Assertions.assertThat(query.flux).isEqualToIgnoringWhitespace("from(bucket:v0) |> derivative(unit: v1, nonNegative: v2, columns: v3, timeColumn: v4)");
        assertVariables(query,
                "v0", "\"telegraf\"",
                "v1", new TimeInterval(10L, ChronoUnit.DAYS),
                "v2", true,
                "v3", new String[]{"time1", "time2"},
                "v4", "\"_timeMy\"");
    }

    @Test
    void derivativeByColumnsCollection() {

        List<String> columns = new ArrayList<>();
        columns.add("time");
        columns.add("century");

        Flux flux = Flux
                .from("telegraf")
                .derivative()
                .withUnit(15L, ChronoUnit.DAYS)
                .withColumns(columns);

        Flux.Query query = flux.toQuery();
        Assertions.assertThat(query.flux).isEqualToIgnoringWhitespace("from(bucket:v0) |> derivative(unit: v1, columns: v2)");
        assertVariables(query,
                "v0", "\"telegraf\"",
                "v1", new TimeInterval(15L, ChronoUnit.DAYS),
                "v2", columns);
    }

    @Test
    void derivativeByString() {

        Flux flux = Flux
                .from("telegraf")
                .derivative()
                    .withUnit("15s");

        Flux.Query query = flux.toQuery();
        Assertions.assertThat(query.flux).isEqualToIgnoringWhitespace("from(bucket:v0) |> derivative(unit: v1)");
        assertVariables(query,
                "v0", "\"telegraf\"",
                "v1", "15s");
    }
}