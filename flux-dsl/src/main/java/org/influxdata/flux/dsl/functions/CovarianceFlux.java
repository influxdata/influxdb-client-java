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

import org.influxdata.client.Arguments;
import org.influxdata.flux.dsl.Flux;

/**
 * Covariance is an aggregate operation. Covariance computes the covariance between two columns.
 * <a href="http://bit.ly/flux-spec#covariance">See SPEC</a>.
 *
 * <h3>Options</h3>
 * <ul>
 * <li>
 * <b>columns</b> - List of columns on which to compute the covariance.
 * Exactly two columns must be provided [array of strings].
 * </li>
 * <li>
 * <b>pearsonr</b> - Indicates whether the result should be normalized to be the
 * <a href="https://en.wikipedia.org/wiki/Pearson_correlation_coefficient">Pearson R coefficient</a> [boolean].
 * </li>
 * <li><b>valueDst</b> - The column into which the result will be placed. Defaults to <i>_value`</i> [string].</li>
 * </ul>
 *
 * <h3>Example</h3>
 * <pre>
 * Flux flux = Flux
 *     .from("telegraf")
 *     .covariance(new String[]{"_value", "_valueSquare"});
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (17/07/2018 13:13)
 */
public final class CovarianceFlux extends AbstractParametrizedFlux {

    public CovarianceFlux(@Nonnull final Flux source) {
        super(source);
    }

    @Nonnull
    @Override
    protected String operatorName() {
        return "covariance";
    }

    /**
     * @param columns list of columns on which to compute the covariance. Exactly two columns must be provided.
     * @return this
     */
    @Nonnull
    public CovarianceFlux withColumns(@Nonnull final String[] columns) {

        Arguments.checkNotNull(columns, "Columns are required");

        if (columns.length != 2) {
            throw new IllegalArgumentException("Exactly two columns must be provided.");
        }

        this.withPropertyValue("columns", columns);

        return this;
    }

    /**
     * @param columns list of columns on which to compute the covariance. Exactly two columns must be provided.
     * @return this
     */
    @Nonnull
    public CovarianceFlux withColumns(@Nonnull final Collection<String> columns) {

        Arguments.checkNotNull(columns, "Columns are required");

        if (columns.size() != 2) {
            throw new IllegalArgumentException("Exactly two columns must be provided.");
        }

        this.withPropertyValue("columns", columns);

        return this;
    }

    /**
     * @param pearsonr Indicates whether the result should be normalized to be the Pearson R coefficient
     * @return this
     */
    @Nonnull
    public CovarianceFlux withPearsonr(final boolean pearsonr) {

        this.withPropertyValue("pearsonr", pearsonr);

        return this;
    }

    /**
     * @param valueDst column into which the result will be placed.
     * @return this
     */
    @Nonnull
    public CovarianceFlux withValueDst(@Nonnull final String valueDst) {

        Arguments.checkNonEmpty(valueDst, "Value destination");

        this.withPropertyValueEscaped("valueDst", valueDst);

        return this;
    }
}
