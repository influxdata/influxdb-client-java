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

import com.influxdb.query.dsl.Expressions;
import com.influxdb.query.dsl.Flux;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (29/06/2018 10:03)
 */
@RunWith(JUnitPlatform.class)
class UnionFluxTest {

    @Test
    void union() {

        Flux flux = Flux.union(
                Flux.from("telegraf1"),
                Flux.from("telegraf2")
        );
        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace("union(tables:[from(bucket:\"telegraf1\"),from(bucket:\"telegraf2\")])");
    }

    @Test
    void unionVariables() {

        Flux v1 = Flux.from("telegraf1").asVariable("v1");
        Flux v2 = Flux.from("telegraf1").asVariable("v2");
        Flux flux = Flux.union(v1, v2);
        Assertions.assertThat(new Expressions(v1, v2, flux).toString()).isEqualToIgnoringWhitespace(
                "v1 = from(bucket:\"telegraf1\")\n" +
                "v2 = from(bucket:\"telegraf1\")\n" +
                "union(tables:[v1,v2])");
    }
}
