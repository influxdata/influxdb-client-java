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
 * @author Jakub Bednar (bednar@github) (29/06/2018 10:46)
 */
@RunWith(JUnitPlatform.class)
class ShiftFluxTest extends AbstractFluxTest {

    @Test
    void shift() {

        Flux flux = Flux
                .from("telegraf")
                .shift(10L, ChronoUnit.HOURS);

        Flux.Query query = flux.toQuery();
        Assertions.assertThat(query.flux).isEqualToIgnoringWhitespace("from(bucket: v0) |> shift(shift: v1)");

        assertVariables(query,
                "v0", "\"telegraf\"",
                "v1", new TimeInterval(10L, ChronoUnit.HOURS));
    }

    @Test
    void shiftColumnsArray() {

        Flux flux = Flux
                .from("telegraf")
                .shift(10L, ChronoUnit.HOURS, new String[]{"time", "custom"});

        Flux.Query query = flux.toQuery();
        Assertions.assertThat(query.flux)
                .isEqualToIgnoringWhitespace("from(bucket: v0) |> shift(shift: v1, columns: v2)");

        assertVariables(query,
                "v0", "\"telegraf\"",
                "v1", new TimeInterval(10L, ChronoUnit.HOURS),
                "v2", new String[]{"time", "custom"});
    }

    @Test
    void shiftColumnsCollection() {

        List<String> columns = new ArrayList<>();
        columns.add("time");
        columns.add("_start");

        Flux flux = Flux
                .from("telegraf")
                .shift(10L, ChronoUnit.HOURS, columns);

        Flux.Query query = flux.toQuery();
        Assertions.assertThat(query.flux)
                .isEqualToIgnoringWhitespace("from(bucket: v0) |> shift(shift: v1, columns: v2)");

        assertVariables(query,
                "v0", "\"telegraf\"",
                "v1", new TimeInterval(10L, ChronoUnit.HOURS),
                "v2", columns);
    }

    @Test
    void shiftByWith() {

        Flux flux = Flux
                .from("telegraf")
                .shift()
                .withShift(20L, ChronoUnit.DAYS);

        Flux.Query query = flux.toQuery();
        Assertions.assertThat(query.flux)
                .isEqualToIgnoringWhitespace("from(bucket: v0) |> shift(shift: v1)");

        assertVariables(query,
                "v0", "\"telegraf\"",
                "v1", new TimeInterval(20L, ChronoUnit.DAYS));
    }

    @Test
    void shiftByString() {

        Flux flux = Flux
                .from("telegraf")
                .shift()
                .withShift("2y");

        Flux.Query query = flux.toQuery();
        Assertions.assertThat(query.flux)
                .isEqualToIgnoringWhitespace("from(bucket: v0) |> shift(shift: v1)");

        assertVariables(query, "v0", "\"telegraf\"", "v1", "2y");
    }
}