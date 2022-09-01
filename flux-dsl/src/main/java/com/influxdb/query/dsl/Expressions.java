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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import com.influxdb.query.dsl.utils.ImportUtils;
import com.influxdb.utils.Arguments;

/**
 * A container holding a list of {@link Expression}s.
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
 */
public class Expressions implements HasImports {
    private final List<Expression> expressions = new ArrayList<>();

    /**
     * @param expressions the expressions to be used
     */
    public Expressions(@Nonnull final Expression... expressions) {
        Arguments.checkNotNull(expressions, "expressions");

        this.expressions.addAll(Arrays.stream(expressions).collect(Collectors.toList()));
    }

    /**
     * @param expressions the expressions to be used
     */
    public Expressions(@Nonnull final Collection<? extends Expression> expressions) {
        Arguments.checkNotNull(expressions, "expressions");

        this.expressions.addAll(expressions);
    }

    /**
     * Adds another expression to this container.
     *
     * @param expressions the expressions to be added
     * @return this
     */
    public Expressions addExpressions(@Nonnull final Expression... expressions) {
        Arguments.checkNotNull(expressions, "expression");

        this.expressions.addAll(Arrays.asList(expressions));
        return this;
    }

    /**
     * @param parameters     parameters to resolve
     * @param prependImports true, if the imports should be prepended
     * @return the string representation of the expressions
     */
    public String toString(@Nonnull final Map<String, Object> parameters, final boolean prependImports) {
        StringBuilder builder = new StringBuilder();

        if (prependImports) {
            builder.append(ImportUtils.getImportsString(this));
        }

        for (Expression expression : expressions) {
            builder.append(expression.toString(parameters, false)).append("\n");
        }

        return builder.toString();
    }

    @Override
    public String toString() {
        return toString(Collections.emptyMap(), true);
    }

    @Override
    public Set<String> getImports() {
        return expressions
                .stream()
                .map(HasImports::getImports)
                .flatMap(Collection::stream)
                .collect(Collectors.toCollection(TreeSet::new));
    }
}
