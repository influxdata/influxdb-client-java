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
package org.influxdata.flux.dsl.functions;

import java.util.Collection;
import javax.annotation.Nonnull;

import org.influxdata.Arguments;
import org.influxdata.flux.dsl.Flux;

/**
 * Cumulative sum computes a running sum for non null records in the table.
 * The output table schema will be the same as the input table.
 * <a href="http://bit.ly/flux-spec#cumulative-sum">See SPEC</a>.
 *
 * <h3>Options</h3>
 * <ul>
 * <li><b>columns</b> - a list of columns on which to operate [array of strings]</li>
 * </ul>
 *
 * <h3>Example</h3>
 * <pre>
 * Flux flux = Flux
 *     .from("telegraf")
 *     .cumulativeSum(new String[]{"_value"});
 * </pre>
 *
 * @author Jakub Bednar (10/10/2018 07:32)
 */
public final class CumulativeSumFlux extends AbstractParametrizedFlux {

    public CumulativeSumFlux(@Nonnull final Flux source) {
        super(source);
    }

    @Nonnull
    @Override
    protected String operatorName() {
        return "cumulativeSum";
    }

    /**
     * @param columns the columns on which to operate
     * @return this
     */
    @Nonnull
    public CumulativeSumFlux withColumns(@Nonnull final String[] columns) {

        Arguments.checkNotNull(columns, "columns");

        this.withPropertyValue("columns", columns);

        return this;
    }

    /**
     * @param columns the columns on which to operate
     * @return this
     */
    @Nonnull
    public CumulativeSumFlux withColumns(@Nonnull final Collection<String> columns) {

        Arguments.checkNotNull(columns, "columns");

        this.withPropertyValue("columns", columns);

        return this;
    }

}
