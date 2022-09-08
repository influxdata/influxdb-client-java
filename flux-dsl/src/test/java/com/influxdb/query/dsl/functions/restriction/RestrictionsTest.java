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
package com.influxdb.query.dsl.functions.restriction;

import java.util.HashMap;
import java.util.Map;

import com.influxdb.query.dsl.Flux;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Jakub Bednar (bednar@github) (09/10/2018 10:33)
 */
class RestrictionsTest {

    @Test
    void notEqual() {

        Restrictions restrictions = Restrictions.start().notEqual(10);

        Assertions.assertThat(restrictions.toString()).isEqualTo("r[\"_start\"] != 10");
    }

    @Test
    void less() {

        Restrictions restrictions = Restrictions.stop().less(10);

        Assertions.assertThat(restrictions.toString()).isEqualTo("r[\"_stop\"] < 10");
    }

    @Test
    void greaterOrEqual() {

        Restrictions restrictions = Restrictions.value().greaterOrEqual(10);

        Assertions.assertThat(restrictions.toString()).isEqualTo("r[\"_value\"] >= 10");
    }

    @Test
    void exists() {

        Restrictions restrictions = Restrictions.value().exists();

        Assertions.assertThat(restrictions.toString()).isEqualTo("exists r[\"_value\"]");
    }

    @Test
    void contains() {

        Restrictions restrictions = Restrictions.value().contains(new String[]{"value1", "value2"});

        Assertions.assertThat(restrictions.toString()).isEqualTo("contains(value: r[\"_value\"], set:[\"value1\", \"value2\"])");
    }

    @Test
    void not() {

        Restrictions restrictions = Restrictions.not(Restrictions.value().exists());
        Assertions.assertThat(restrictions.toString()).isEqualTo("not exists r[\"_value\"]");
    }

    @Test
    void emptyLogical() {

        Restrictions restrictions = Restrictions.and(
                Restrictions.tag("tag").equal("production"),
                Restrictions.or(),
                Restrictions.measurement().equal("data")
        );
        Assertions.assertThat(restrictions.toString()).isEqualTo("(r[\"tag\"] == \"production\" and r[\"_measurement\"] == \"data\")");

        restrictions = Restrictions.and(Restrictions.or());
        Assertions.assertThat(restrictions.toString()).isEqualTo("");

        restrictions = Restrictions.or(Restrictions.or());
        Assertions.assertThat(restrictions.toString()).isEqualTo("");
    }

    @Test
    void escaping() {

        Restrictions restrictions = Restrictions.tag("my-tag").equal("escaped\"tag");
        Assertions.assertThat(restrictions.toString()).isEqualTo("r[\"my-tag\"] == \"escaped\\\"tag\"");

        restrictions = Restrictions.column("my\"column").exists();
        Assertions.assertThat(restrictions.toString()).isEqualTo("exists r[\"my\\\"column\"]");

        restrictions = Restrictions.tag("_value").contains(new String[]{"val\"ue1", "value2"});

        Assertions.assertThat(restrictions.toString()).isEqualTo("contains(value: r[\"_value\"], set:[\"val\\\"ue1\", \"value2\"])");

        Flux flux = Flux
                .from("telegraf")
                .drop(new String[]{"host", "ta\"g"});

        Assertions.assertThat(flux.toString())
                .isEqualToIgnoringWhitespace("from(bucket:\"telegraf\") |> drop(columns:[\"host\", \"ta\\\"g\"])");

        Map<String, String> map = new HashMap<>();
        map.put("host", "ser\"ver");
        map.put("_value", "va\"l");

        flux = Flux
                .from("tel\"egraf")
                .rename(map);

        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace("from(bucket:\"tel\\\"egraf\") |> rename(columns: {host: \"ser\\\"ver\", _value: \"va\\\"l\"})");

    }

}
