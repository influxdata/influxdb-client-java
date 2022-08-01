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

import java.util.Collection;
import javax.annotation.Nonnull;

import com.influxdb.query.dsl.Flux;
import com.influxdb.utils.Arguments;

/**
 * Quantile is both an aggregate operation and a selector operation depending on selected options.
 * In the aggregate methods, it outputs the value that represents the specified quantile of the non null record
 * as a float.
 * <a href="http://bit.ly/flux-spec#quantile-aggregate">See SPEC</a>.
 *
 * <h3>Options</h3>
 * <ul>
 *     <li><b>column</b> - column to aggregate. Defaults to <i>_value</i>. [string]</li>
 *     <li><b>quantile</b> - value between 0 and 1 indicating the desired quantile. [float]</li>
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
 *     .quantile(0.80F);
 *
 * Flux flux = Flux
 *     .from("telegraf")
 *     .quantile()
 *         .withQuantile(0.75F)
 *         .withMethod(MethodType.EXACT_MEAN)
 *         .withCompression(2_000F);
 * </pre>
 *
 * @author Jakub Bednar (10/10/2018 11:34)
 */
public final class QuantileFlux extends AbstractParametrizedFlux {

    public QuantileFlux(@Nonnull final Flux source) {
        super(source);
    }

    @Nonnull
    @Override
    protected String operatorName() {
        return "quantile";
    }

    /**
     * Methods for computation.
     */
    public enum MethodType {

        /**
         * An aggregate result that uses a tdigest data structure to compute an accurate quantile estimate
         * on large data sources.
         */
        ESTIMATE_TDIGEST,

        /**
         * An aggregate result that takes the average of the two points closest to the quantile value.
         */
        EXACT_MEAN,

        EXACT_SELECTOR
    }

    /**
     * @param column The column to aggregate. Defaults to "_value".
     * @return this
     */
    @Nonnull
    public QuantileFlux withColumn(@Nonnull final String column) {

        Arguments.checkNonEmpty(column, "column");

        this.withPropertyValueEscaped("column", column);

        return this;
    }

    /**
     * @param quantile value between 0 and 1 indicating the desired quantile
     * @return this
     */
    @Nonnull
    public QuantileFlux withQuantile(@Nonnull final Float quantile) {

        Arguments.checkNotNull(quantile, "quantile");

        this.withPropertyValue("q", quantile);

        return this;
    }

    /**
     * @param compression indicates how many centroids to use when compressing the dataset.
     * @return this
     */
    @Nonnull
    public QuantileFlux withCompression(@Nonnull final Float compression) {

        Arguments.checkNotNull(compression, "compression");

        this.withPropertyValue("compression", compression);

        return this;
    }

    /**
     * @param method method to aggregate
     * @return this
     */
    @Nonnull
    public QuantileFlux withMethod(@Nonnull final String method) {

        Arguments.checkNotNull(method, "method");

        this.withPropertyValueEscaped("method", method);

        return this;
    }

    /**
     * @param method method to aggregate
     * @return this
     */
    @Nonnull
    public QuantileFlux withMethod(@Nonnull final MethodType method) {

        Arguments.checkNotNull(method, "method");

        this.withPropertyValueEscaped("method", method.toString().toLowerCase());

        return this;
    }
}
