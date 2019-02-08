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
 * Percentile is both an aggregate operation and a selector operation depending on selected options.
 * In the aggregate methods, it outputs the value that represents the specified percentile of the non null record
 * as a float.
 * <a href="http://bit.ly/flux-spec#percentile-aggregate">See SPEC</a>.
 *
 * <h3>Options</h3>
 * <ul>
 *     <li><b>columns</b> - specifies a list of columns to aggregate. Defaults to <i>_value</i>. [array of strings]</li>
 *     <li><b>percentile</b> - value between 0 and 1 indicating the desired percentile. [float]</li>
 *     <li><b>method</b> - method to aggregate</li>
 *     <li><b>compression</b> - Compression indicates how many centroids to use when compressing the dataset.
 *     A larger number produces a more accurate result at the cost of increased memory requirements.
 *     Defaults to <i>1000</i>.
 *     [float]</li>
 * </ul>
 *
 * <h3>Example</h3>
 * <pre>
 * Flux flux = Flux
 *     .from("telegraf")
 *     .percentile(0.80F);
 *
 * Flux flux = Flux
 *     .from("telegraf")
 *     .percentile()
 *         .withColumns(new String[]{"value2"})
 *         .withPercentile(0.75F)
 *         .withMethod(MethodType.EXACT_MEAN)
 *         .withCompression(2_000F);
 * </pre>
 *
 * @author Jakub Bednar (10/10/2018 11:34)
 */
public final class PercentileFlux extends AbstractParametrizedFlux {

    public PercentileFlux(@Nonnull final Flux source) {
        super(source);
    }

    @Nonnull
    @Override
    protected String operatorName() {
        return "percentile";
    }

    /**
     * Methods for computation.
     */
    public enum MethodType {

        /**
         * An aggregate result that uses a tdigest data structure to compute an accurate percentile estimate
         * on large data sources.
         */
        ESTIMATE_TDIGEST,

        /**
         * An aggregate result that takes the average of the two points closest to the percentile value.
         */
        EXACT_MEAN,

        EXACT_SELECTOR
    }

    /**
     * @param columns specifies a list of columns to aggregate
     * @return this
     */
    @Nonnull
    public PercentileFlux withColumns(@Nonnull final String[] columns) {

        Arguments.checkNotNull(columns, "columns");

        this.withPropertyValue("columns", columns);

        return this;
    }

    /**
     * @param columns specifies a list of columns to aggregate
     * @return this
     */
    @Nonnull
    public PercentileFlux withColumns(@Nonnull final Collection<String> columns) {

        Arguments.checkNotNull(columns, "columns");

        this.withPropertyValue("columns", columns);

        return this;
    }

    /**
     * @param percentile value between 0 and 1 indicating the desired percentile
     * @return this
     */
    @Nonnull
    public PercentileFlux withPercentile(@Nonnull final Float percentile) {

        Arguments.checkNotNull(percentile, "percentile");

        this.withPropertyValue("percentile", percentile);

        return this;
    }

    /**
     * @param compression indicates how many centroids to use when compressing the dataset.
     * @return this
     */
    @Nonnull
    public PercentileFlux withCompression(@Nonnull final Float compression) {

        Arguments.checkNotNull(compression, "compression");

        this.withPropertyValue("compression", compression);

        return this;
    }

    /**
     * @param method method to aggregate
     * @return this
     */
    @Nonnull
    public PercentileFlux withMethod(@Nonnull final String method) {

        Arguments.checkNotNull(method, "method");

        this.withPropertyValueEscaped("method", method);

        return this;
    }

    /**
     * @param method method to aggregate
     * @return this
     */
    @Nonnull
    public PercentileFlux withMethod(@Nonnull final MethodType method) {

        Arguments.checkNotNull(method, "method");

        this.withPropertyValueEscaped("method", method.toString().toLowerCase());

        return this;
    }
}
