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

import com.influxdb.query.dsl.Expression;
import com.influxdb.query.dsl.HasImports;
import com.influxdb.query.dsl.utils.ImportUtils;
import com.influxdb.utils.Arguments;

/**
 * An expression to encapsulate an arbitrary expression.
 */
public final class FreestyleExpression extends AbstractParametrizedFlux implements HasImports, Expression {

    private final String expression;

    /**
     * @param expression the string representation of th expression to encapsulate.
     */
    public FreestyleExpression(@Nonnull final String expression) {

        Arguments.checkNonEmpty(expression, "Expression");

        this.expression = expression;
    }

    @Override
    public String toString(@Nonnull final Map<String, Object> parameters, final boolean prependImports) {
        StringBuilder builder = new StringBuilder();

        if (prependImports) {
            builder.append(ImportUtils.getImportsString(this));
        }
        builder.append(expression);

        return builder.toString();
    }

    @Nonnull
    @Override
    protected String operatorName() {
        return "";
    }
}
