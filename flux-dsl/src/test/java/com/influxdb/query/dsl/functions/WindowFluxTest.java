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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;

import com.influxdb.query.dsl.AbstractFluxTest;
import com.influxdb.query.dsl.Flux;
import com.influxdb.query.dsl.functions.properties.TimeInterval;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (27/06/2018 12:42)
 */
@RunWith(JUnitPlatform.class)
class WindowFluxTest extends AbstractFluxTest {

    @Test
    void windowEveryChronoUnit() {

        Flux flux = Flux
                .from("telegraf")
                .window(15L, ChronoUnit.MINUTES);

        Flux.Query query = flux.toQuery();
        Assertions.assertThat(query.flux)
                .isEqualToIgnoringWhitespace("from(bucket: v0) |> window(every: v1)");
        assertVariables(query,
                "v0", "\"telegraf\"",
                "v1", new TimeInterval(15L, ChronoUnit.MINUTES));
    }

    @Test
    void windowEveryPeriodChronoUnit() {

        Flux flux = Flux
                .from("telegraf")
                .window(15L, ChronoUnit.MINUTES, 20L, ChronoUnit.SECONDS);

        Flux.Query query = flux.toQuery();
        Assertions.assertThat(query.flux)
                .isEqualToIgnoringWhitespace("from(bucket: v0) |> window(every: v1, period: v2)");
        assertVariables(query,
                "v0", "\"telegraf\"",
                "v1", new TimeInterval(15L, ChronoUnit.MINUTES),
                "v2", new TimeInterval(20L, ChronoUnit.SECONDS));
    }

    @Test
    void windowEveryPeriodOffsetChronoUnit() {

        Flux flux = Flux
                .from("telegraf")
                .window(15L, ChronoUnit.HALF_DAYS, 20L, ChronoUnit.SECONDS, -50L, ChronoUnit.DAYS);

        Flux.Query query = flux.toQuery();
        Assertions.assertThat(query.flux)
                .isEqualToIgnoringWhitespace("from(bucket: v0) |> window(every: v1, period: v2, offset: v3)");
        assertVariables(query,
                "v0", "\"telegraf\"",
                "v1", new TimeInterval(15L, ChronoUnit.HALF_DAYS),
                "v2", new TimeInterval(20L, ChronoUnit.SECONDS),
                "v3", new TimeInterval(-50L, ChronoUnit.DAYS));
    }

    @Test
    void windowEveryPeriodOffsetInstant() {

        Flux flux = Flux
                .from("telegraf")
                .window(15L, ChronoUnit.MINUTES, 20L, ChronoUnit.SECONDS, Instant.ofEpochSecond(1_750_000));

        String expected = "from(bucket: v0) |> window(every: v1, period: v2, offset: v3)";

        Flux.Query query = flux.toQuery();
        Assertions.assertThat(query.flux).isEqualToIgnoringWhitespace(expected);
        assertVariables(query,
                "v0", "\"telegraf\"",
                "v1", new TimeInterval(15L, ChronoUnit.MINUTES),
                "v2", new TimeInterval(20L, ChronoUnit.SECONDS),
                "v3", Instant.ofEpochSecond(1_750_000));
    }

    @Test
    void windowEveryPeriodOffsetString() {

        Flux flux = Flux
                .from("telegraf")
                .window()
                .withEvery("10s").withPeriod("30m").withOffset("-1d");

        String expected = "from(bucket: v0) |> window(every: v1, period: v2, offset: v3)";

        Flux.Query query = flux.toQuery();
        Assertions.assertThat(query.flux).isEqualToIgnoringWhitespace(expected);
        assertVariables(query,
                "v0", "\"telegraf\"",
                "v1", "10s",
                "v2", "30m",
                "v3", "-1d");
    }

    @Test
    void windowEveryPeriodOffsetChronoUnitColumns() {

        Flux flux = Flux
                .from("telegraf")
                .window(15L, ChronoUnit.MINUTES,
                        20L, ChronoUnit.SECONDS,
                        -50L, ChronoUnit.DAYS,
                        "time", "superStart", "totalEnd");

        String expected = "from(bucket: v0) |> "
                + "window(every: v1, period: v2, offset: v3, timeColumn: v4, "
                + "startColumn: v5, stopColumn: v6)";

        Flux.Query query = flux.toQuery();
        Assertions.assertThat(query.flux).isEqualToIgnoringWhitespace(expected);
        assertVariables(query,
                "v0", "\"telegraf\"",
                "v1", new TimeInterval(15L, ChronoUnit.MINUTES),
                "v2", new TimeInterval(20L, ChronoUnit.SECONDS),
                "v3", new TimeInterval(-50L, ChronoUnit.DAYS),
                "v4", "\"time\"",
                "v5", "\"superStart\"",
                "v6", "\"totalEnd\"");
    }

    @Test
    void windowEveryPeriodStartInstantColumns() {

        Flux flux = Flux
                .from("telegraf")
                .window(15L, ChronoUnit.MINUTES,
                        20L, ChronoUnit.SECONDS,
                        Instant.ofEpochSecond(1_750_000),
                        "time", "superStart", "totalEnd");

        String expected = "from(bucket: v0) |> "
                + "window(every: v1, period: v2, offset: v3, timeColumn: v4, "
                + "startColumn: v5, stopColumn: v6)";

        Flux.Query query = flux.toQuery();
        Assertions.assertThat(query.flux).isEqualToIgnoringWhitespace(expected);
        assertVariables(query,
                "v0", "\"telegraf\"",
                "v1", new TimeInterval(15L, ChronoUnit.MINUTES),
                "v2", new TimeInterval(20L, ChronoUnit.SECONDS),
                "v3", Instant.ofEpochSecond(1_750_000),
                "v4", "\"time\"",
                "v5", "\"superStart\"",
                "v6", "\"totalEnd\"");
    }

    @Test
    void namedParameters() {

        Flux flux = Flux
                .from("telegraf")
                .window()
                .withPropertyNamed("every")
                .withPropertyNamed("period")
                .withPropertyNamed("offset")
                .withPropertyNamed("round");

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("every", new TimeInterval(15L, ChronoUnit.MINUTES));
        parameters.put("period", new TimeInterval(20L, ChronoUnit.SECONDS));
        parameters.put("offset", new TimeInterval(-50L, ChronoUnit.DAYS));
        parameters.put("round", new TimeInterval(1L, ChronoUnit.HOURS));

        String expected = "from(bucket: v4) |> "
                + "window(every: every, period: period, offset: offset, round: round)";

        Flux.Query query = flux.toQuery(parameters);
        Assertions.assertThat(query.flux).isEqualToIgnoringWhitespace(expected);
        assertVariables(query,
                "v4", "\"telegraf\"",
                "every", new TimeInterval(15L, ChronoUnit.MINUTES),
                "period", new TimeInterval(20L, ChronoUnit.SECONDS),
                "offset", new TimeInterval(-50L, ChronoUnit.DAYS),
                "round", new TimeInterval(1L, ChronoUnit.HOURS));
    }

    @Test
    void onlyInterval() {

        Flux flux = Flux.from("telegraf")
                .window()
                .withPropertyValue("intervals", "intervals(every:1mo, period:-1d)");

        Flux.Query query = flux.toQuery();
        Assertions.assertThat(query.flux)
                .isEqualToIgnoringWhitespace("from(bucket: v0) |> window(intervals: v1)");
        assertVariables(query,
                "v0", "\"telegraf\"",
                "v1", "intervals(every:1mo, period:-1d)");
    }
}
