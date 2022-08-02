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

import java.util.Arrays;

import com.influxdb.query.dsl.Flux;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (10/10/2018 12:38)
 */
@RunWith(JUnitPlatform.class)
class QuantileFluxTest {

    @Test
    void quantile() {

        Flux flux = Flux
                .from("telegraf")
                .quantile()
                    .withColumn("value2")
                    .withQuantile(0.75F)
                    .withMethod("exact_mean")
                    .withCompression(2_000F);

        String expected = "from(bucket:\"telegraf\") |> "
                + "quantile(column:\"value2\", q:0.75, method:\"exact_mean\", compression:2000.0)";
        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void quantileQuantile() {

        Flux flux = Flux
                .from("telegraf")
                .quantile(0.80F);

        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace("from(bucket:\"telegraf\") |> quantile(q:0.8)");
    }

    @Test
    void quantileQuantileMethod() {

        Flux flux = Flux
                .from("telegraf")
                .quantile(0.80F, QuantileFlux.MethodType.EXACT_SELECTOR);

        Assertions.assertThat(flux.toString())
                .isEqualToIgnoringWhitespace("from(bucket:\"telegraf\") |> quantile(q:0.8, method:\"exact_selector\")");
    }

    @Test
    void quantileQuantileMethodCompression() {

        Flux flux = Flux
                .from("telegraf")
                .quantile(0.80F, QuantileFlux.MethodType.EXACT_SELECTOR, 3_000F);

        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace("from(bucket:\"telegraf\") |> quantile(q:0.8, method:\"exact_selector\", compression:3000.0)");
    }
}
