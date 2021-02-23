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

import com.influxdb.query.dsl.AbstractFluxTest;
import com.influxdb.query.dsl.Flux;
import com.influxdb.query.dsl.functions.properties.TimeInterval;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (13/05/2020 15:07)
 */
@RunWith(JUnitPlatform.class)
class AggregateWindowTest extends AbstractFluxTest {

    @Test
    void aggregateWindow() {

        Flux flux = Flux
                .from("telegraf")
                .aggregateWindow(10L, ChronoUnit.SECONDS, "mean");

        Flux.Query query = flux.toQuery();
        Assertions
                .assertThat(query.flux)
                .isEqualToIgnoringWhitespace("from(bucket:v0) |> aggregateWindow(every: v1, fn: v2)");
        assertVariables(query,
                "v0", "\"telegraf\"",
                "v1", new TimeInterval(10L, ChronoUnit.SECONDS),
                "v2", "mean");
    }

    @Test
    void aggregateWindowAllParameters() {

        Flux flux = Flux
                .from("telegraf")
                .aggregateWindow()
                .withEvery("10s")
                .withAggregateFunction("sum")
                .withColumn("_value")
                .withTimeSrc("_stop")
                .withTimeDst("_time")
                .withCreateEmpty(true);

        Flux.Query query = flux.toQuery();
        Assertions
                .assertThat(query.flux)
                .isEqualToIgnoringWhitespace("from(bucket:v0) |> aggregateWindow(every: v1, fn: v2, column: v3, timeSrc: v4, timeDst: v5, createEmpty:v6)");
        assertVariables(query,
                "v0", "\"telegraf\"",
                "v1", "10s",
                "v2", "sum",
                "v3", "\"_value\"",
                "v4", "\"_stop\"",
                "v5", "\"_time\"",
                "v6", true);
    }

    @Test
    void aggregateWindowFunction() {
        Flux flux = Flux
                .from("telegraf")
                .aggregateWindow()
                .withEvery(5L, ChronoUnit.MINUTES)
                .withFunction("tables |> quantile(q: 0.99, column:column)");

        Flux.Query query = flux.toQuery();
        Assertions
                .assertThat(query.flux)
                .isEqualToIgnoringWhitespace("from(bucket:v0) |> aggregateWindow(every: v1, fn: (column, tables=<-) => v2)");
        
        assertVariables(query,
                "v0", "\"telegraf\"",
                "v1", new TimeInterval(5L, ChronoUnit.MINUTES),
                "v2", "tables |> quantile(q: 0.99, column:column)");
    }
}
