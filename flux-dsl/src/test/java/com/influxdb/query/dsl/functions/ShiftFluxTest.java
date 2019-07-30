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
 * @author Jakub Bednar (bednar@github) (29/06/2018 10:46)
 */
@RunWith(JUnitPlatform.class)
class ShiftFluxTest {

    @Test
    void shift() {

        Flux flux = Flux
                .from("telegraf")
                .shift(10L, ChronoUnit.HOURS);

        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace("from(bucket:\"telegraf\") |> shift(shift: 10h)");
    }

    @Test
    void shiftColumnsArray() {

        Flux flux = Flux
                .from("telegraf")
                .shift(10L, ChronoUnit.HOURS, new String[]{"time", "custom"});

        Assertions.assertThat(flux.toString())
                .isEqualToIgnoringWhitespace("from(bucket:\"telegraf\") |> shift(shift: 10h, columns: [\"time\", \"custom\"])");
    }

    @Test
    void shiftColumnsCollection() {

        List<String> columns = new ArrayList<>();
        columns.add("time");
        columns.add("_start");

        Flux flux = Flux
                .from("telegraf")
                .shift(10L, ChronoUnit.HOURS, columns);

        Assertions.assertThat(flux.toString())
                .isEqualToIgnoringWhitespace("from(bucket:\"telegraf\") |> shift(shift: 10h, columns: [\"time\", \"_start\"])");
    }

    @Test
    void shiftByWith() {

        Flux flux = Flux
                .from("telegraf")
                .shift()
                .withShift(20L, ChronoUnit.DAYS);

        Assertions.assertThat(flux.toString())
                .isEqualToIgnoringWhitespace("from(bucket:\"telegraf\") |> shift(shift: 20d)");
    }

    @Test
    void shiftByString() {

        Flux flux = Flux
                .from("telegraf")
                .shift()
                    .withShift("2y");

        Assertions.assertThat(flux.toString())
                .isEqualToIgnoringWhitespace("from(bucket:\"telegraf\") |> shift(shift: 2y)");
    }
}