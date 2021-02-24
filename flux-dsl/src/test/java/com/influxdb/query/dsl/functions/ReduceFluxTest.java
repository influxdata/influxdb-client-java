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

import com.influxdb.query.dsl.AbstractFluxTest;
import com.influxdb.query.dsl.Flux;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (24/02/2020 13:26)
 */
@RunWith(JUnitPlatform.class)
class ReduceFluxTest extends AbstractFluxTest {

    @Test
    void reduce() {

        Flux flux = Flux
                .from("telegraf")
                .reduce("{ sum: r._value + accumulator.sum }", "{sum: 0.0}");

        String expected = "from(bucket: v0) "
                + "|> reduce(fn: (r, accumulator) => v1, identity: v2)";

        Flux.Query query = flux.toQuery();
        Assertions.assertThat(query.flux).isEqualToIgnoringWhitespace(expected);
        assertVariables(query,
                "v0", "\"telegraf\"",
                "v1", "({ sum: r._value + accumulator.sum })",
                "v2", "{sum: 0.0}");
    }

    @Test
    public void reduceByParameter() {

        Flux flux = Flux
                .from("telegraf")
                .reduce()
                .withFunction("{sum: r._value + accumulator.sum,\ncount: accumulator.count + 1.0}")
                .withIdentity("{sum: 0.0, count: 0.0}");

        String expected = "from(bucket: v0) "
                + "|> reduce(fn: (r, accumulator) => v1, identity: v2)";

        Flux.Query query = flux.toQuery();
        Assertions.assertThat(query.flux).isEqualToIgnoringWhitespace(expected);
        assertVariables(query,
                "v0", "\"telegraf\"",
                "v1", "({sum: r._value + accumulator.sum,\ncount: accumulator.count + 1.0})",
                "v2", "{sum: 0.0, count: 0.0}");
    }
}
