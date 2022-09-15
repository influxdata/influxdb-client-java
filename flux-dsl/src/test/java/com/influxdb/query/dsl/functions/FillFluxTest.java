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

import com.influxdb.query.dsl.Flux;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Jakub Bednar (bednar@github) (09/10/2018 13:27)
 */
class FillFluxTest {

    @Test
    void fillString() {

        Flux flux = Flux
                .from("telegraf")
                .fill("foo");

        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace("from(bucket:\"telegraf\")\n" +
                "\t|> fill(value:\"foo\")");
    }

    @Test
    void fillBoolean() {

        Flux flux = Flux
                .from("telegraf")
                .fill(true);

        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace("from(bucket:\"telegraf\")\n" +
                "\t|> fill(value:true)");
    }

    @Test
    void fillInt() {

        Flux flux = Flux
                .from("telegraf")
                .fill(42);

        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace("from(bucket:\"telegraf\")\n" +
                "\t|> fill(value:42)");
    }

    @Test
    void fillFloat() {

        Flux flux = Flux
                .from("telegraf")
                .fill(42.0);

        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace("from(bucket:\"telegraf\")\n" +
                "\t|> fill(value:42.0)");
    }

    @Test
    void fillTime() {

        Flux flux = Flux
                .from("telegraf")
                .fill(Instant.ofEpochMilli(0));

        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace("from(bucket:\"telegraf\")\n" +
                "\t|> fill(value:1970-01-01T00:00:00.000000000Z)");
    }

    @Test
    void fillPrevious() {

        Flux flux = Flux
                .from("telegraf")
                .fill()
                    .withColumn("other")
                    .withUsePrevious(true);

        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace("from(bucket:\"telegraf\")\n" +
                "\t|> fill(column:\"other\", usePrevious:true)");
    }
}
