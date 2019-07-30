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

import java.util.HashMap;

import com.influxdb.query.dsl.Flux;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (25/06/2018 11:55)
 */
@RunWith(JUnitPlatform.class)
class LimitFluxTest {

    @Test
    void limit() {

        Flux flux = Flux
                .from("telegraf")
                .limit(5);

        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace("from(bucket:\"telegraf\") |> limit(n: 5)");
    }

    @Test
    void limitPositive() {
        Assertions.assertThatThrownBy(() -> Flux.from("telegraf").limit(-5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Expecting a positive number for Number of results");
    }

    @Test
    void limitByParameter() {

        Flux flux = Flux
                .from("telegraf")
                .limit()
                .withPropertyNamed("n", "limit");

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("limit", 15);

        Assertions.assertThat(flux.toString(parameters))
                .isEqualToIgnoringWhitespace("from(bucket:\"telegraf\") |> limit(n: 15)");
    }

    @Test
    void limitByParameterMissing() {

        Assertions.assertThatThrownBy(() -> Flux.from("telegraf").limit().withPropertyNamed("limit").toString())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("The parameter 'limit' is not defined.");
    }
}