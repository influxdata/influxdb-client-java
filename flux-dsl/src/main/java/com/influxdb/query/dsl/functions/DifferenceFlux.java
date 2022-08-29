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
 * Difference computes the difference between subsequent non null records.
 * <a href="http://bit.ly/flux-spec#difference">See SPEC</a>.
 *
 * <p>
 * <b>Options</b>
 * <ul>
 * <li>
 * <b>nonNegative</b> - Indicates if the derivative is allowed to be negative.
 * If a value is encountered which is less than the previous value
 * then it is assumed the previous value should have been a zero [boolean].
 * </li>
 * <li>
 * <b>columns</b> - The list of columns on which to compute the difference.
 * Defaults <i>["_value"]</i> [array of strings].
 * </li>
 * </ul>
 *
 * <p>
 * <b>Example</b>
 * <pre>
 * Flux flux = Flux
 *     .from("telegraf")
 *     .groupBy("_measurement")
 *     .difference();
 *
 * Flux flux = Flux
 *     .from("telegraf")
 *     .range(-5L, ChronoUnit.MINUTES)
 *     .difference(new String[]{"_value", "_time"}, false)
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (17/07/2018 12:28)
 */
public final class DifferenceFlux extends AbstractParametrizedFlux {

    public DifferenceFlux(@Nonnull final Flux source) {
        super(source);
    }

    @Nonnull
    @Override
    protected String operatorName() {
        return "difference";
    }

    /**
     * @param nonNegative indicates if the derivative is allowed to be negative
     * @return this
     */
    @Nonnull
    public DifferenceFlux withNonNegative(final boolean nonNegative) {

        this.withPropertyValue("nonNegative", nonNegative);

        return this;
    }

    /**
     * @param columns list of columns on which to compute the difference
     * @return this
     */
    @Nonnull
    public DifferenceFlux withColumns(@Nonnull final String[] columns) {

        Arguments.checkNotNull(columns, "Columns are required");

        this.withPropertyValue("columns", columns);

        return this;
    }

    /**
     * @param columns list of columns on which to compute the difference
     * @return this
     */
    @Nonnull
    public DifferenceFlux withColumns(@Nonnull final Collection<String> columns) {

        Arguments.checkNotNull(columns, "Columns are required");

        this.withPropertyValue("columns", columns);

        return this;
    }
}
