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
import java.util.ArrayList;
import java.util.List;

import com.influxdb.query.dsl.Expressions;
import com.influxdb.query.dsl.Flux;
import com.influxdb.query.dsl.functions.restriction.Restrictions;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Jakub Bednar (bednar@github) (19/07/2018 12:59)
 */
class JoinFluxTest {

    @Test
    void join() {

        Flux cpu = Flux.from("telegraf")
                .filter(Restrictions.and(Restrictions.measurement().equal("cpu"), Restrictions.field().equal("usage_user")))
                .range(-30L, ChronoUnit.MINUTES);

        Flux mem = Flux.from("telegraf")
                .filter(Restrictions.and(Restrictions.measurement().equal("mem"), Restrictions.field().equal("used_percent")))
                .range(-30L, ChronoUnit.MINUTES);

        Flux flux = Flux
                .join("cpu", cpu, "mem", mem, "host", "left");

        String expected = "cpu = from(bucket:\"telegraf\") |> filter(fn: (r) => (r[\"_measurement\"] == \"cpu\" and r[\"_field\"] == \"usage_user\")) |> range(start: -30m) "
                + "mem = from(bucket:\"telegraf\") |> filter(fn: (r) => (r[\"_measurement\"] == \"mem\" and r[\"_field\"] == \"used_percent\")) |> range(start: -30m) "
                + "join(tables: {cpu:cpu, mem:mem}, on: [\"host\"], method: \"left\")";

        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void joinByParameters() {

        Flux cpu = Flux.from("telegraf")
                .filter(Restrictions.and(Restrictions.measurement().equal("cpu"), Restrictions.field().equal("usage_user")))
                .range(-30L, ChronoUnit.MINUTES);

        Flux mem = Flux.from("telegraf")
                .filter(Restrictions.and(Restrictions.measurement().equal("mem"), Restrictions.field().equal("used_percent")))
                .range(-30L, ChronoUnit.MINUTES);

        List<String> tags = new ArrayList<>();
        tags.add("host");
        tags.add("production");

        Flux flux = Flux.join()
                .withTable("cpu", cpu)
                .withTable("mem", mem)
                .withOn(tags)
                .withMethod("cross");

        String expected = "cpu = from(bucket:\"telegraf\") |> filter(fn: (r) => (r[\"_measurement\"] == \"cpu\" and r[\"_field\"] == \"usage_user\")) |> range(start: -30m) "
                + "mem = from(bucket:\"telegraf\") |> filter(fn: (r) => (r[\"_measurement\"] == \"mem\" and r[\"_field\"] == \"used_percent\")) |> range(start: -30m) "
                + "join(tables: {cpu:cpu, mem:mem}, on: [\"host\", \"production\"], method: \"cross\")";

        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void methodByEnum() {

        Flux cpu = Flux.from("telegraf")
                .filter(Restrictions.and(Restrictions.measurement().equal("cpu"), Restrictions.field().equal("usage_user")))
                .range(-30L, ChronoUnit.MINUTES);

        Flux mem = Flux.from("telegraf")
                .filter(Restrictions.and(Restrictions.measurement().equal("mem"), Restrictions.field().equal("used_percent")))
                .range(-30L, ChronoUnit.MINUTES);

        List<String> tags = new ArrayList<>();
        tags.add("host");
        tags.add("production");

        Flux flux = Flux.join()
                .withTable("cpu", cpu)
                .withTable("mem", mem)
                .withOn(tags)
                .withMethod(JoinFlux.MethodType.RIGHT);

        String expected = "cpu = from(bucket:\"telegraf\") |> filter(fn: (r) => (r[\"_measurement\"] == \"cpu\" and r[\"_field\"] == \"usage_user\")) |> range(start: -30m) "
                + "mem = from(bucket:\"telegraf\") |> filter(fn: (r) => (r[\"_measurement\"] == \"mem\" and r[\"_field\"] == \"used_percent\")) |> range(start: -30m) "
                + "join(tables: {cpu:cpu, mem:mem}, on: [\"host\", \"production\"], method: \"right\")";

        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void join3TablesDirectly() {

        Flux b1 = Flux.from("bucket1")
                .range(-30L, ChronoUnit.MINUTES);
        Flux b2 = Flux.from("bucket2")
                .range(-30L, ChronoUnit.MINUTES);
        Flux b3 = Flux.from("bucket3")
                .range(-30L, ChronoUnit.MINUTES);

        Flux join1 = Flux
                .join("b1", b1, "b2", b2, "_time", "left");

        Flux join2 = Flux
                .join("j1", join1, "b3", b3, "_time", "left");

        String expected = "b1 = from(bucket:\"bucket1\")\n" +
                "\t|> range(start:-30m)\n" +
                "b2 = from(bucket:\"bucket2\")\n" +
                "\t|> range(start:-30m)\n" +
                "j1 = join(tables:{b1:b1, b2:b2}, on:[\"_time\"], method:\"left\")\n" +
                "b3 = from(bucket:\"bucket3\")\n" +
                "\t|> range(start:-30m)\n" +
                "join(tables:{j1:j1, b3:b3}, on:[\"_time\"], method:\"left\")";

        Assertions.assertThat(join2.toString()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void join3TablesViaVariables() {

        Flux b1 = Flux.from("bucket1")
                .range(-30L, ChronoUnit.MINUTES)
                .asVariable("table1");
        Flux b2 = Flux.from("bucket2")
                .range(-30L, ChronoUnit.MINUTES)
                .asVariable("table2");
        Flux b3 = Flux.from("bucket3")
                .range(-30L, ChronoUnit.MINUTES)
                .asVariable("table3");

        Flux join1 = Flux
                .join("b1", b1, "b2", b2, "_time", "left")
                .asVariable("join1");

        Flux join2 = Flux
                .join("j1", join1, "b3", b3, "_time", "left");

        Expressions expressions = new Expressions(
                b1,
                b2,
                b3,
                join1,
                join2
        );
        String expected = "table1 = from(bucket:\"bucket1\")\n" +
                "\t|> range(start:-30m)\n" +
                "table2 = from(bucket:\"bucket2\")\n" +
                "\t|> range(start:-30m)\n" +
                "table3 = from(bucket:\"bucket3\")\n" +
                "\t|> range(start:-30m)\n" +
                "join1 = join(tables:{b1:table1, b2:table2}, on:[\"_time\"], method:\"left\")\n" +
                "join(tables:{j1:join1, b3:table3}, on:[\"_time\"], method:\"left\")";

        Assertions.assertThat(expressions.toString()).isEqualToIgnoringWhitespace(expected);
    }
}
