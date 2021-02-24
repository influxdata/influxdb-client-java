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
import com.influxdb.query.dsl.functions.restriction.Restrictions;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (29/06/2018 07:37)
 */
@RunWith(JUnitPlatform.class)
class SampleFluxTest extends AbstractFluxTest {

    @Test
    void sampleN() {

        Flux flux = Flux.from("telegraf")
                .sample(10);

        String expected = "from(bucket: v0) |> sample(n: v1)";

        Flux.Query query = flux.toQuery();
        Assertions.assertThat(query.flux).isEqualToIgnoringWhitespace(expected);

        assertVariables(query, "v0", "\"telegraf\"", "v1", 10);
    }

    @Test
    void sampleNPosition() {

        Flux flux = Flux.from("telegraf")
                .sample(5, 1);

        String expected = "from(bucket: v0) |> sample(n: v1, pos: v2)";

        Flux.Query query = flux.toQuery();
        Assertions.assertThat(query.flux).isEqualToIgnoringWhitespace(expected);

        assertVariables(query, "v0", "\"telegraf\"", "v1", 5, "v2", 1);
    }

    @Test
    void sampleWith() {

        Flux flux = Flux
                .from("telegraf")
                .sample()
                .withN(5);

        Flux.Query query = flux.toQuery();
        Assertions.assertThat(query.flux).isEqualToIgnoringWhitespace("from(bucket: v0) |> sample(n: v1)");
        assertVariables(query,
                "v0", "\"telegraf\"",
                "v1", 5);
    }

    @Test
    void samplePositionGreaterThanN() {

        Flux flux = Flux.from("telegraf")
                .filter(Restrictions.and(Restrictions.measurement().equal("cpu"), Restrictions.field().equal("usage_system")))
                .range(-1L, ChronoUnit.DAYS);


        Assertions.assertThatThrownBy(() -> flux.sample(5, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("pos must be less than n");
    }

    @Test
    void samplePositionSameThanN() {

        Flux flux = Flux.from("telegraf")
                .filter(Restrictions.and(Restrictions.measurement().equal("cpu"), Restrictions.field().equal("usage_system")))
                .range(-1L, ChronoUnit.DAYS);

        Assertions.assertThatThrownBy(() -> flux.sample(10, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("pos must be less than n");
    }
}