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

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.Nonnull;

import com.influxdb.query.dsl.utils.ImportUtils;

/**
 * Hold the variable name and expression of an assignment.
 * <p>
 * <b>Example</b>
 * <pre>
 * VariableAssignment a = Flux.from("test1").asVariable("a");
 * VariableAssignment b = Flux.from("test2").asVariable("b");
 *
 * String flux = new Expressions(
 *    a,
 *    b,
 *    a.first().yield("firstA"),
 *    b.first().yield("firstB"),
 *    a.last().yield("lastA"),
 *    b.last().yield("lastB")
 * ).toString();
 * </pre>
 *
 * @see Expressions
 * @see Flux#asVariable(String)
 * @see com.influxdb.query.dsl.functions.AbstractFunctionFlux
 */
public class VariableAssignment extends Flux implements IsVariableAssignment {

    @Nonnull
    protected final String name;

    @Nonnull
    protected final Expression expression;

    /**
     * @param name       the name of teh variable
     * @param expression the expression to assign to the variable
     */
    public VariableAssignment(@Nonnull final String name, @Nonnull final Expression expression) {
        this.name = name;
        this.expression = expression;
    }

    @Nonnull
    public String getVariableName() {
        return name;
    }

    @Override
    public void appendActual(@Nonnull final Map<String, Object> parameters, @Nonnull final StringBuilder builder) {
        builder.append(name);
    }

    @Override
    public String toString(@Nonnull final Map<String, Object> parameters, final boolean prependImports) {
        StringBuilder builder = new StringBuilder();

        if (prependImports) {
            builder.append(ImportUtils.getImportsString(this));
        }
        builder.append(name).append(" = ").append(expression.toString(parameters, false));

        return builder.toString();
    }


    @Override
    public Set<String> getImports() {
        Set<String> result = new TreeSet<>(expression.getImports());
        if (imports != null) {
            result.addAll(imports);
        }
        return result;
    }

}
