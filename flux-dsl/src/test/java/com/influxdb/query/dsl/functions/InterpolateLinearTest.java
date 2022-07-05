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

import com.influxdb.query.dsl.Flux;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

@RunWith(JUnitPlatform.class)
class InterpolateLinearTest {

    @Test
    void interpolateLinear() {

        Flux flux = Flux
                .from("telegraf")
                .interpolateLinear(1L, ChronoUnit.MINUTES);

        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace("import \"interpolate\"\n" +
                "from(bucket:\"telegraf\") |> interpolate.linear(every:1m)");
    }

    @Test
    void interpolateLinearByParameter() {

        Flux flux = Flux
                .from("telegraf")
                .interpolateLinear()
                .withEvery(5L, ChronoUnit.MINUTES);

        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace("import \"interpolate\"\n" +
                "from(bucket:\"telegraf\") |> interpolate.linear(every:5m)");
    }

    @Test
    void interpolateLinearByString() {

        Flux flux = Flux
                .from("telegraf")
                .interpolateLinear()
                .withEvery("10m6h");

        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace("import \"interpolate\"\n" +
                "from(bucket:\"telegraf\") |> interpolate.linear(every:10m6h)");
    }
}
