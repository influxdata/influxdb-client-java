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
package com.influxdb.query.dsl;

import java.time.temporal.ChronoUnit;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Jakub Bednar (bednar@github) (22/06/2018 10:27)
 */
class FluxTest {

    @Test
    void flux() {

        String flux = Flux
                .from("telegraf")
                .count()
                .toString();

        Assertions.assertThat(flux).isEqualToIgnoringWhitespace("from(bucket:\"telegraf\") |> count()");
    }

    @Test
    void propertyValueEscapedNull() {

        Flux flux = Flux
                .from("telegraf")
                .count()
                    .withPropertyValueEscaped("unused", null);

        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace("from(bucket:\"telegraf\") |> count()");
    }

    @Test
    void propertyValueNull() {

        Flux flux = Flux
                .from("telegraf")
                .count()
                    .withPropertyValue("unused", null);

        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace("from(bucket:\"telegraf\") |> count()");
    }

    @Test
    void propertyValueAmountNull() {

        Flux flux = Flux
                .from("telegraf")
                .count()
                    .withPropertyValue("unused", null, ChronoUnit.HOURS);

        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace("from(bucket:\"telegraf\") |> count()");
    }

    @Test
    void propertyValueUnitNull() {

        Flux flux = Flux
                .from("telegraf")
                .count()
                    .withPropertyValue("unused", 10L, null);

        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace("from(bucket:\"telegraf\") |> count()");
    }

    @Test
    void withLocationNamed() {

        Flux flux = Flux
                .from("telegraf")
                .withLocationNamed("America/Los_Angeles")
                .count();

        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace("import \"timezone\" option location = timezone.location(name: \"America/Los_Angeles\") from(bucket:\"telegraf\") |> count()");
    }

    @Test
    void withLocationFixed() {

        Flux flux = Flux
                .from("telegraf")
                .withLocationFixed("-8h")
                .count();

        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace("import \"timezone\" option location = timezone.fixed(offset: -8h) from(bucket:\"telegraf\") |> count()");
    }
}