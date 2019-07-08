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
import java.util.regex.Pattern;

import com.influxdb.query.dsl.Flux;
import com.influxdb.query.dsl.functions.properties.TimeInterval;
import com.influxdb.query.dsl.functions.restriction.Restrictions;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (28/06/2018 11:46)
 */
@RunWith(JUnitPlatform.class)
class FilterFluxTest {

    @Test
    void filter() {

        Restrictions restriction = Restrictions.and(
                Restrictions.tag("t1").equal("val1"),
                Restrictions.tag("t2").equal("val2")
        );

        Flux flux = Flux
                .from("telegraf")
                .filter(restriction)
                .range(-4L, -2L, ChronoUnit.HOURS)
                .count();

        String expected = "from(bucket:\"telegraf\") |> filter(fn: (r) => (r[\"t1\"]==\"val1\" and r[\"t2\"]==\"val2\")) |> "
                + "range(start:-4h, stop:-2h) |> count()";

        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void filterExample() {

        Restrictions restriction = Restrictions.and(
                Restrictions.measurement().equal("mem"),
                Restrictions.field().equal("usage_system"),
                Restrictions.tag("service").equal("app-server")
        );

        Flux flux = Flux
                .from("telegraf")
                .filter(restriction)
                .range(-4L, ChronoUnit.HOURS)
                .count();

        String expected = "from(bucket:\"telegraf\") |> filter(fn: (r) => (r[\"_measurement\"] == \"mem\" and r[\"_field\"] == \"usage_system\" and r[\"service\"] == \"app-server\")) |> "
                + "range(start:-4h) |> count()";

        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void filterInnerOr() {

        Restrictions and = Restrictions.and(
                Restrictions.tag("t1").equal("val1"),
                Restrictions.tag("t2").equal("val2")
        );

        Restrictions restrictions = Restrictions.or(and, Restrictions.tag("t3").equal("val3"));

        Flux flux = Flux
                .from("telegraf")
                .filter(restrictions)
                .range(4L, 2L, ChronoUnit.HOURS)
                .count();

        String expected = "from(bucket:\"telegraf\") |> filter(fn: (r) => ((r[\"t1\"]==\"val1\" and r[\"t2\"]==\"val2\") or r[\"t3\"]==\"val3\")) |> "
                + "range(start:4h, stop:2h) |> count()";

        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void filterDoubleAndRegexpValue() {

        Restrictions restriction = Restrictions.and(
                Restrictions.tag("instance_type").equal(Pattern.compile("/prod/")),
                Restrictions.field().greater(10.5D),
                Restrictions.time().lessOrEqual(new TimeInterval(-15L, ChronoUnit.HOURS))
        );

        Flux flux = Flux
                .from("telegraf")
                .filter(restriction)
                .range(-4L, 2L, ChronoUnit.HOURS)
                .count();

        String expected = "from(bucket:\"telegraf\") |> filter(fn: (r) => (r[\"instance_type\"]==/prod/ and r[\"_field\"] > 10.5 and r[\"_time\"] <= -15h)) |> "
                + "range(start:-4h, stop:2h) |> count()";

        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace(expected);
    }

}