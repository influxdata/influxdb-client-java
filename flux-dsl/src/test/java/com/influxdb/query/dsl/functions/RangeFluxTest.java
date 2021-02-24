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
 * @author Jakub Bednar (bednar@github) (26/06/2018 07:23)
 */
@RunWith(JUnitPlatform.class)
class RangeFluxTest extends AbstractFluxTest {

    @Test
    void startInstant() {

        Flux flux = Flux
                .from("telegraf")
                .range(Instant.ofEpochSecond(1_500_000));

        Flux.Query query = flux.toQuery();
        Assertions.assertThat(query.flux)
                .isEqualToIgnoringWhitespace("from(bucket: v0) |> range(start: v1)");
        assertVariables(query,
                "v0", "\"telegraf\"",
                "v1", Instant.ofEpochSecond(1_500_000));
    }

    @Test
    void startStopInstant() {

        Flux flux = Flux
                .from("telegraf")
                .range(Instant.ofEpochSecond(1_500_000), Instant.ofEpochSecond(2_000_000));

        String expected = "from(bucket: v0) |> "
                + "range(start: v1, stop: v2)";

        Flux.Query query = flux.toQuery();
        Assertions.assertThat(query.flux).isEqualToIgnoringWhitespace(expected);
        assertVariables(query,
                "v0", "\"telegraf\"",
                "v1", Instant.ofEpochSecond(1_500_000),
                "v2", Instant.ofEpochSecond(2_000_000));
    }

    @Test
    void startUnit() {

        Flux flux = Flux
                .from("telegraf")
                .range(15L, ChronoUnit.SECONDS);

        Flux.Query query = flux.toQuery();
        Assertions.assertThat(query.flux)
                .isEqualToIgnoringWhitespace("from(bucket: v0) |> range(start: v1)");
        assertVariables(query,
                "v0", "\"telegraf\"",
                "v1", new TimeInterval(15L, ChronoUnit.SECONDS));
    }

    @Test
    void startUnitNegative() {

        Flux flux = Flux
                .from("telegraf")
                .range(-33L, ChronoUnit.HOURS);

        Flux.Query query = flux.toQuery();
        Assertions.assertThat(query.flux)
                .isEqualToIgnoringWhitespace("from(bucket: v0) |> range(start: v1)");
        assertVariables(query,
                "v0", "\"telegraf\"",
                "v1", new TimeInterval(-33L, ChronoUnit.HOURS));
    }

    @Test
    void startStopUnit() {

        Flux flux = Flux
                .from("telegraf")
                .range(15L, 44L, ChronoUnit.NANOS);

        Flux.Query query = flux.toQuery();
        Assertions.assertThat(query.flux)
                .isEqualToIgnoringWhitespace("from(bucket: v0) |> range(start: v1, stop: v2)");
        assertVariables(query,
                "v0", "\"telegraf\"",
                "v1", new TimeInterval(15L, ChronoUnit.NANOS),
                "v2", new TimeInterval(44L, ChronoUnit.NANOS));
    }

    @Test
    void startStopString() {

        Flux flux = Flux
                .from("telegraf")
                .range()
                .withStart("-1h")
                .withStop("10h");

        Flux.Query query = flux.toQuery();
        Assertions.assertThat(query.flux)
                .isEqualToIgnoringWhitespace("from(bucket: v0) |> range(start: v1, stop: v2)");

        assertVariables(query, "v0", "\"telegraf\"", "v1", "-1h", "v2", "10h");
    }

    @Test
    void startParameter() {

        Flux flux = Flux
                .from("telegraf")
                .range()
                .withPropertyNamed("start", "startParameter");

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("startParameter", Instant.ofEpochSecond(1_600_000));

        Flux.Query query = flux.toQuery(parameters);
        Assertions.assertThat(query.flux)
                .isEqualToIgnoringWhitespace("from(bucket: v1) |> range(start: startParameter)");
        assertVariables(query, "v1", "\"telegraf\"", "startParameter", Instant.ofEpochSecond(1_600_000));
    }

    @Test
    void startStopParameter() {

        Flux flux = Flux
                .from("telegraf")
                .range()
                .withPropertyNamed("start", "startParameter")
                .withPropertyNamed("stop", "stopParameter");

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("startParameter", Instant.ofEpochSecond(1_600_000));
        parameters.put("stopParameter", Instant.ofEpochSecond(1_800_000));

        String expected = "from(bucket: v2) |> "
                + "range(start: startParameter, stop: stopParameter)";

        Flux.Query query = flux.toQuery(parameters);
        Assertions.assertThat(query.flux).isEqualToIgnoringWhitespace(expected);
        assertVariables(query, "v2", "\"telegraf\"",
                "startParameter", Instant.ofEpochSecond(1_600_000),
                "stopParameter", Instant.ofEpochSecond(1_800_000));
    }
}