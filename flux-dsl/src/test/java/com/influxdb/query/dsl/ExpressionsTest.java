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
package com.influxdb.query.dsl;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class ExpressionsTest {

    @Test
    void testMultipleExpressions() {
        VariableAssignment a = Flux.from("test1").asVariable("a");
        VariableAssignment b = Flux.from("test2").asVariable("b");

        String flux = new Expressions(
                a,
                b,
                a.first().yield("firstA"),
                b.first().yield("firstB"),
                a.last().yield("lastA"),
                b.last().yield("lastB"))
                .toString();

        Assertions.assertThat(flux).isEqualToIgnoringWhitespace("a = from(bucket:\"test1\")\n" +
                "b = from(bucket:\"test2\")\n" +
                "a\n" +
                "\t|> first()\n" +
                "\t|> yield(name:\"firstA\")\n" +
                "b\n" +
                "\t|> first()\n" +
                "\t|> yield(name:\"firstB\")\n" +
                "a\n" +
                "\t|> last()\n" +
                "\t|> yield(name:\"lastA\")\n" +
                "b\n" +
                "\t|> last()\n" +
                "\t|> yield(name:\"lastB\")");
    }

}
