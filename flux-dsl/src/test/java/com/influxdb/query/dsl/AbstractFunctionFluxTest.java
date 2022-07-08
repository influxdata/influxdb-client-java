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

import java.time.temporal.ChronoUnit;
import javax.annotation.Nonnull;

import com.influxdb.query.dsl.functions.AbstractFunctionCallFlux;
import com.influxdb.query.dsl.functions.AbstractFunctionFlux;
import com.influxdb.query.dsl.functions.FreestyleExpression;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

@RunWith(JUnitPlatform.class)
public class AbstractFunctionFluxTest {

    @Test
    void testCustomFunction() {

        MultByXFunction multByX = new MultByXFunction();

        Expressions flux = new Expressions(
                multByX,
                Flux.from("telegraph")
                        .withPipedFunction(multByX)
                        .withX(42.)
                        .count()
        );

        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace("multByX = (tables=<-, x) => ()\n" +
                "\t|> map(fn: (r) => (r) => ({r with _value: r._value * x}))\n" +
                "from(bucket:\"telegraph\")\n" +
                "\t|> multByX(x:42.0)\n" +
                "\t|> count()\n");
    }

    @Test
    void testFreestyleFunction() {
        MyCustomFreestyleFunction fun = new MyCustomFreestyleFunction();

        Expressions flux = new Expressions(
                fun,
                fun.invoke().withN(0)
        );

        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace(
                "import \"freestyle/import\"\n" +
                        "freestyle = (?n, ignore1 = 42, ignore2 = true, ignore3 = \"foo\", ignore4 = 1s) => n * n\n" +
                        "freestyle(n:0)\n");
    }

    public static class MultByXFunction extends AbstractFunctionFlux<MultByXFunction.MultByXFunctionCall> {

        public MultByXFunction() {
            super("multByX",
                    new FreestyleExpression("tables")
                            .map("(r) => ({r with _value: r._value * x})"),
                    MultByXFunctionCall::new,
                    new Parameter("tables").withPipeForward(true),
                    new Parameter("x"));
        }

        public static class MultByXFunctionCall extends AbstractFunctionCallFlux {

            public MultByXFunctionCall(@Nonnull String name) {
                super(name);
            }

            @Nonnull
            public MultByXFunctionCall withX(final Number x) {
                this.withPropertyValue("x", x);
                return this;
            }
        }
    }

    public static class MyCustomFreestyleFunction extends AbstractFunctionFlux<MyCustomFreestyleFunction.MyCustomFreestyleFunctionCall> {

        public MyCustomFreestyleFunction() {
            super("freestyle",
                    new FreestyleExpression("n * n").addImport("freestyle/import"),
                    MyCustomFreestyleFunctionCall::new,
                    new Parameter("n").withOptional(true),
                    new Parameter("ignore1").withDefaultValue(42),
                    new Parameter("ignore2").withDefaultValue(true),
                    new Parameter("ignore3").withDefaultValue("foo"),
                    new Parameter("ignore4").withDefaultValue(1, ChronoUnit.SECONDS)
            );
        }

        public static class MyCustomFreestyleFunctionCall extends AbstractFunctionCallFlux {

            public MyCustomFreestyleFunctionCall(@Nonnull final String name) {
                super(name);
            }

            @Nonnull
            public MyCustomFreestyleFunctionCall withN(final Number b) {
                this.withPropertyValue("n", b);
                return this;
            }
        }
    }
}
