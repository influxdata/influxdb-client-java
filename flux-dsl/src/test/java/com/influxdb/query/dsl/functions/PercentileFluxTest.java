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

import com.influxdb.query.dsl.AbstractFluxTest;
import com.influxdb.query.dsl.Flux;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (10/10/2018 12:38)
 */
@RunWith(JUnitPlatform.class)
class PercentileFluxTest extends AbstractFluxTest {

    @Test
    void percentile() {

        Flux flux = Flux
                .from("telegraf")
                .percentile()
                .withColumns(new String[]{"value2"})
                .withPercentile(0.75F)
                .withMethod("exact_mean")
                .withCompression(2_000F);

        String expected = "from(bucket: v0) |> "
                + "percentile(columns: v1, percentile: v2, method: v3, compression: v4)";

        Flux.Query query = flux.toQuery();
        Assertions.assertThat(query.flux).isEqualToIgnoringWhitespace(expected);
        assertVariables(query,
                "v0", "\"telegraf\"",
                "v1", new String[]{"value2"}, "v2", 0.75F, "v3", "\"exact_mean\"", "v4", 2_000F);
    }

    @Test
    void percentilePercentile() {

        Flux flux = Flux
                .from("telegraf")
                .percentile(0.80F);

        Flux.Query query = flux.toQuery();
        Assertions.assertThat(query.flux).isEqualToIgnoringWhitespace("from(bucket: v0) |> percentile(percentile: v1)");
        assertVariables(query, "v0", "\"telegraf\"", "v1", 0.80F);
    }

    @Test
    void percentilePercentileMethod() {

        Flux flux = Flux
                .from("telegraf")
                .percentile(0.80F, PercentileFlux.MethodType.EXACT_SELECTOR);

        Flux.Query query = flux.toQuery();
        Assertions.assertThat(query.flux)
                .isEqualToIgnoringWhitespace("from(bucket: v0) |> percentile(percentile: v1, method: v2)");
        assertVariables(query, "v0", "\"telegraf\"", "v1", 0.80F, "v2", "\"exact_selector\"");
    }

    @Test
    void percentilePercentileMethodCompression() {

        Flux flux = Flux
                .from("telegraf")
                .percentile(0.80F, PercentileFlux.MethodType.EXACT_SELECTOR, 3_000F);

        Flux.Query query = flux.toQuery();
        Assertions.assertThat(query.flux)
                .isEqualToIgnoringWhitespace("from(bucket: v0) |> percentile(percentile: v1, method: v2, compression: v3)");
        assertVariables(query,
                "v0", "\"telegraf\"",
                "v1", 0.80F,
                "v2", "\"exact_selector\"",
                "v3", 3_000F);
    }

    @Test
    void percentileColumnsPercentileMethodCompression() {

        Flux flux = Flux
                .from("telegraf")
                .percentile(new String[]{"_value", "_value2"}, 0.80F, PercentileFlux.MethodType.EXACT_SELECTOR, 3_000F);

        String expected = "from(bucket: v0) |> "
                + "percentile(columns: v1, percentile: v2, method: v3, compression: v4)";
        Flux.Query query = flux.toQuery();
        Assertions.assertThat(query.flux).isEqualToIgnoringWhitespace(expected);
        assertVariables(query,
                "v0", "\"telegraf\"",
                "v1", new String[]{"_value", "_value2"},
                "v2", 0.80F,
                "v3", "\"exact_selector\"",
                "v4", 3_000F);
    }

    @Test
    void percentileColumnsListPercentileMethodCompression() {

        String[] columns = {"_value", "_value2"};

        Flux flux = Flux
                .from("telegraf")
                .percentile(Arrays.asList(columns), 0.80F, PercentileFlux.MethodType.ESTIMATE_TDIGEST, 3_000F);

        String expected = "from(bucket: v0) |> "
                + "percentile(columns: v1, percentile: v2, method: v3, compression: v4)";

        Flux.Query query = flux.toQuery();
        Assertions.assertThat(query.flux).isEqualToIgnoringWhitespace(expected);
        assertVariables(query,
                "v0", "\"telegraf\"",
                "v1", Arrays.asList(columns),
                "v2", 0.80F,
                "v3", "\"estimate_tdigest\"",
                "v4", 3_000F);
    }
}
