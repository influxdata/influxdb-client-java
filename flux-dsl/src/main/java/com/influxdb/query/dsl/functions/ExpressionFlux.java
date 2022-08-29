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

import java.util.Map;
import javax.annotation.Nonnull;

import com.influxdb.query.dsl.Flux;
import com.influxdb.utils.Arguments;

/**
 * The custom Flux expression.
 *
 * <p>
 * <b>Example</b>
 * <pre>
 *     Flux.from("telegraf")
 *          .expression("map(fn: (r) =&gt; r._value * r._value)")
 *          .expression("sum()")
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (27/06/2018 11:21)
 */
public final class ExpressionFlux extends AbstractFluxWithUpstream {

    private final String expression;

    public ExpressionFlux(@Nonnull final Flux source, @Nonnull final String expression) {
        super(source);

        Arguments.checkNonEmpty(expression, "Expression");

        this.expression = expression;
    }

    @Override
    public void appendActual(@Nonnull final Map<String, Object> parameters, @Nonnull final StringBuilder builder) {

        super.appendActual(parameters, builder);

        appendDelimiter(builder);
        builder.append(expression);
    }
}
