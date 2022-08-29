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
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.influxdb.query.dsl.Expression;
import com.influxdb.query.dsl.Expressions;
import com.influxdb.query.dsl.Flux;
import com.influxdb.query.dsl.VariableAssignment;
import com.influxdb.query.dsl.functions.properties.TimeInterval;
import com.influxdb.query.dsl.utils.ImportUtils;
import com.influxdb.utils.Arguments;

import static com.influxdb.query.dsl.functions.properties.FunctionsParameters.escapeDoubleQuotes;

/**
 * The base class for function definitions.
 * The function definition describes the implementation of a function.
 * A concrete call to this function can be created via the {@link #invoke()} method.
 *
 * <p>
 * <b>Example Definition</b>
 * <pre>
 * public static class MyCustomFunction extends AbstractFunctionFlux&lt;MyCustomFunction.MyCustomFunctionCall&gt; {
 *
 *     public MyCustomFunction() {
 *         super("from2",
 *                 new FromFlux()
 *                         .withPropertyValue("bucket", "n"),
 *                 MyCustomFunctionCall::new,
 *                 new Parameter("n"));
 *         addImport("foo");
 *     }
 *
 *     public static class MyCustomFunctionCall extends AbstractFunctionCallFlux {
 *
 *         public MyCustomFunctionCall(@Nonnull String name) {
 *             super(name);
 *         }
 *
 *         public MyCustomFunctionCall withN(final String n) {
 *             this.withPropertyValueEscaped("n", n);
 *             return this;
 *         }
 *     }
 * }
 * </pre>
 * <p>
 * <b>Example Usage</b>
 * <pre>
 * MyCustomFunction fun = new MyCustomFunction();
 *
 * Expressions flux = new Expressions(
 *    fun,
 *    fun.invoke().withN("bar").count(),
 *    fun.invoke().withN("bar2"),
 * );
 * </pre>
 *
 * @param <CALL> the type of the {@link AbstractFunctionCallFlux}
 * @see Expressions
 */
public class AbstractFunctionFlux<CALL extends AbstractFunctionCallFlux> extends VariableAssignment {

    @Nonnull
    private final Function<String, CALL> invocationFactory;
    @Nonnull
    private final Parameter[] parameter;

    /**
     * @param name               the name of the function (the variable th function is assigned to)
     * @param functionDefinition the function body
     * @param invocationFactory  a factory to create an invocation, should be the
     *                           {@link AbstractFunctionCallFlux#AbstractFunctionCallFlux(String) constructor reference}
     *                           to the concrete <code>CALL</code> implementation
     * @param parameter          the parameters of this function
     */
    protected AbstractFunctionFlux(
            @Nonnull final String name,
            @Nonnull final Expression functionDefinition,
            @Nonnull final Function<String, CALL> invocationFactory,
            @Nonnull final Parameter... parameter) {
        super(name, functionDefinition);
        this.invocationFactory = invocationFactory;
        this.parameter = parameter;
    }

    @Override
    public String toString(@Nonnull final Map<String, Object> parameters, final boolean prependImports) {
        StringBuilder builder = new StringBuilder();

        if (prependImports) {
            builder.append(ImportUtils.getImportsString(this));
        }
        builder.append(name).append(" = ")
                .append(Arrays.stream(parameter)
                        .map(Parameter::toString)
                        .collect(Collectors.joining(", ", "(", ") => ")))
                .append(expression.toString(parameters, false));
        return builder.toString();
    }

    /**
     * @return A flux invoking this method. The invocation arguments can be adjusted via this flux.
     */
    @Nonnull
    public CALL invoke() {
        return invocationFactory.apply(getVariableName());
    }

    @Nonnull
    public CALL invokePiped(@Nonnull final Flux flux) {
        Arguments.checkNotNull(flux, "Source is required");

        CALL invoke = invoke();
        invoke.source = flux;
        return invoke;
    }

    @Override
    public Set<String> getImports() {
        return super.getImports();
    }


    public static class Parameter {
        @Nonnull
        private final String name;
        private String defaultValue;

        private boolean pipeForward;

        private boolean optional;

        public Parameter(@Nonnull final String name) {

            Arguments.checkNotNull(name, "name");

            this.name = name;
        }

        @Nonnull
        public String getName() {
            return name;
        }

        @Nullable
        public String getDefaultValue() {
            return defaultValue;
        }

        @Nonnull
        public Parameter withDefaultValue(@Nonnull final Number defaultValue) {
            Arguments.checkNotNull(defaultValue, "defaultValue");

            this.defaultValue = String.valueOf(defaultValue);
            return this;
        }

        @Nonnull
        public Parameter withDefaultValue(@Nonnull final String defaultValue) {
            Arguments.checkNotNull(defaultValue, "defaultValue");

            this.defaultValue = "\"" + escapeDoubleQuotes(defaultValue) + "\"";

            return this;
        }

        @Nonnull
        public Parameter withDefaultValue(final boolean defaultValue) {
            this.defaultValue = String.valueOf(defaultValue);
            return this;
        }

        @Nonnull
        public Parameter withDefaultValue(final long amount, @Nonnull final ChronoUnit unit) {
            Arguments.checkNotNull(unit, "unit");

            this.defaultValue = new TimeInterval(amount, unit).toString();
            return this;
        }

        /**
         * Indicates the parameter that, by default, represents the piped-forward value.
         *
         * @param pipeForward true to use <code>&lt;-</code> for this parameter
         * @return this
         * @see Flux#withPipedFunction(AbstractFunctionFlux)
         */
        @Nonnull
        public Parameter withPipeForward(final boolean pipeForward) {
            this.pipeForward = pipeForward;
            return this;
        }

        /**
         * @param optional true, if this parameter is optional
         * @return this
         */
        @Nonnull
        public Parameter withOptional(final boolean optional) {
            this.optional = optional;
            return this;
        }

        @Override
        public String toString() {
            StringBuilder result = new StringBuilder();
            if (optional) {
                result.append("?");
            }
            result.append(name);
            if (pipeForward) {
                result.append("=<-");
            } else if (defaultValue != null) {
                result.append(" = ").append(defaultValue);
            }
            return result.toString();
        }
    }
}
