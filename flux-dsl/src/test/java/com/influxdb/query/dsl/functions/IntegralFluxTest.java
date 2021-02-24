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

import com.influxdb.query.dsl.AbstractFluxTest;
import com.influxdb.query.dsl.Flux;
import com.influxdb.query.dsl.functions.properties.TimeInterval;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (03/07/2018 12:48)
 */
@RunWith(JUnitPlatform.class)
class IntegralFluxTest extends AbstractFluxTest {

    @Test
    void integral() {

        Flux flux = Flux
                .from("telegraf")
                .integral(1L, ChronoUnit.MINUTES);

        Flux.Query query = flux.toQuery();
        Assertions.assertThat(query.flux).isEqualToIgnoringWhitespace("from(bucket:v0) |> integral(unit: v1)");
        assertVariables(query,
                "v0", "\"telegraf\"",
                "v1", new TimeInterval(1L, ChronoUnit.MINUTES));
    }

    @Test
    void integralByParameter() {

        Flux flux = Flux
                .from("telegraf")
                .integral()
                .withUnit(5L, ChronoUnit.MINUTES);

        Flux.Query query = flux.toQuery();
        Assertions.assertThat(query.flux).isEqualToIgnoringWhitespace("from(bucket:v0) |> integral(unit: v1)");
        assertVariables(query,
                "v0", "\"telegraf\"",
                "v1", new TimeInterval(5L, ChronoUnit.MINUTES));
    }

    @Test
    void integralByString() {

        Flux flux = Flux
                .from("telegraf")
                .integral()
                .withUnit("10m6h");

        Flux.Query query = flux.toQuery();
        Assertions.assertThat(query.flux).isEqualToIgnoringWhitespace("from(bucket:v0) |> integral(unit: v1)");
        assertVariables(query,
                "v0", "\"telegraf\"",
                "v1", "10m6h");
    }
}