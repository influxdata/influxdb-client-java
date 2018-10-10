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
package org.influxdata.flux.functions;

import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.influxdata.flux.Flux;

/**
 * @author Jakub Bednar (bednar@github) (27/06/2018 14:03)
 */
public abstract class AbstractParametrizedFlux extends AbstractFluxWithUpstream {

    protected AbstractParametrizedFlux() {
        super();
    }

    protected AbstractParametrizedFlux(@Nonnull final Flux source) {
        super(source);
    }

    @Override
    public void appendActual(@Nonnull final Map<String, Object> parameters, @Nonnull final StringBuilder builder) {

        super.appendActual(parameters, builder);

        StringBuilder operator = new StringBuilder();
        //
        // see JoinFlux
        beforeAppendOperatorName(operator, parameters);
        //

        //
        // function(
        //
        operator.append(operatorName()).append("(");
        //
        //
        // parameters: false
        boolean wasAppended = false;

        for (String name : functionsParameters.keys()) {

            String propertyValue = functionsParameters.get(name, parameters);

            wasAppended = appendParameterTo(name, propertyValue, operator, wasAppended);
        }
        //
        // )
        //
        operator.append(")");

        appendDelimiter(builder);
        builder.append(operator);
    }

    /**
     * @return name of function
     */
    @Nonnull
    protected abstract String operatorName();

    /**
     * For value property it is ": ", but for function it is "=&gt;".
     *
     * @param operatorName function name
     * @return property value delimiter
     * @see AbstractParametrizedFlux#propertyDelimiter(String)
     */
    @Nonnull
    protected String propertyDelimiter(@Nonnull final String operatorName) {
        return ": ";
    }

    /**
     * Possibility to customize function.
     *
     * @param operator   current Flux function
     * @param parameters parameters
     * @see JoinFlux
     */
    protected void beforeAppendOperatorName(@Nonnull final StringBuilder operator,
                                            @Nonnull final Map<String, Object> parameters) {
    }

    /**
     * @return {@link Boolean#TRUE} if was appended parameter
     */
    private boolean appendParameterTo(@Nonnull final String operatorName,
                                      @Nullable final String propertyValue,
                                      @Nonnull final StringBuilder operator,
                                      final boolean wasAppendProperty) {

        if (propertyValue == null) {
            return wasAppendProperty;
        }

        // delimit previously appended parameter
        if (wasAppendProperty) {
            operator.append(", ");
        }

        // n: 5
        operator
                .append(operatorName)
                .append(propertyDelimiter(operatorName))
                .append(propertyValue);

        return true;
    }
}
