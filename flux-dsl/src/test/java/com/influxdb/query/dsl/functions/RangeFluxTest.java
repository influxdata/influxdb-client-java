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

import com.influxdb.query.dsl.Flux;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (26/06/2018 07:23)
 */
@RunWith(JUnitPlatform.class)
class RangeFluxTest {

    @Test
    void startInstant() {

        Flux flux = Flux
                .from("telegraf")
                .range(Instant.ofEpochSecond(1_500_000));

        Assertions.assertThat(flux.toString())
                .isEqualToIgnoringWhitespace("from(bucket:\"telegraf\") |> range(start: 1970-01-18T08:40:00.000000000Z)");
    }

    @Test
    void startStopInstant() {

        Flux flux = Flux
                .from("telegraf")
                .range(Instant.ofEpochSecond(1_500_000), Instant.ofEpochSecond(2_000_000));

        String expected = "from(bucket:\"telegraf\") |> "
                + "range(start: 1970-01-18T08:40:00.000000000Z, stop: 1970-01-24T03:33:20.000000000Z)";

        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void startUnit() {

        Flux flux = Flux
                .from("telegraf")
                .range(15L, ChronoUnit.SECONDS);

        Assertions.assertThat(flux.toString())
                .isEqualToIgnoringWhitespace("from(bucket:\"telegraf\") |> range(start: 15s)");
    }

    @Test
    void startUnitNegative() {

        Flux flux = Flux
                .from("telegraf")
                .range(-33L, ChronoUnit.HOURS);

        Assertions.assertThat(flux.toString())
                .isEqualToIgnoringWhitespace("from(bucket:\"telegraf\") |> range(start: -33h)");
    }

    @Test
    void startStopUnit() {

        Flux flux = Flux
                .from("telegraf")
                .range(15L, 44L, ChronoUnit.NANOS);

        Assertions.assertThat(flux.toString())
                .isEqualToIgnoringWhitespace("from(bucket:\"telegraf\") |> range(start: 15ns, stop: 44ns)");
    }

    @Test
    void startStopString() {

        Flux flux = Flux
                .from("telegraf")
                .range()
                    .withStart("-1h")
                    .withStop("10h");

        Assertions.assertThat(flux.toString())
                .isEqualToIgnoringWhitespace("from(bucket:\"telegraf\") |> range(start: -1h, stop: 10h)");
    }

    @Test
    void startParameter() {

        Flux flux = Flux
                .from("telegraf")
                .range()
                .withPropertyNamed("start", "startParameter");

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("startParameter", Instant.ofEpochSecond(1_600_000));

        Assertions.assertThat(flux.toString(parameters))
                .isEqualToIgnoringWhitespace("from(bucket:\"telegraf\") |> range(start: 1970-01-19T12:26:40.000000000Z)");
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

        String expected = "from(bucket:\"telegraf\") |> "
                + "range(start: 1970-01-19T12:26:40.000000000Z, stop: 1970-01-21T20:00:00.000000000Z)";

        Assertions.assertThat(flux.toString(parameters)).isEqualToIgnoringWhitespace(expected);
    }
}