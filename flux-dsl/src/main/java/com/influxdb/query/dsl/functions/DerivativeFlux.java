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
import java.util.Collection;
import javax.annotation.Nonnull;

import com.influxdb.query.dsl.Flux;
import com.influxdb.utils.Arguments;

/**
 * Computes the time based difference between subsequent non null records.
 * <a href="http://bit.ly/flux-spec#derivative">See SPEC</a>.
 *
 * <h3>Options</h3>
 * <ul>
 * <li><b>unit</b> - The time duration to use for the result [duration]</li>
 * <li><b>nonNegative</b> - Indicates if the derivative is allowed to be negative [boolean].</li>
 * <li><b>columns</b> - List of columns on which to compute the derivative [array of strings].</li>
 * <li><b>timeColumn</b> - The source column for the time values. Defaults to `_time` [string].</li>
 * </ul>
 *
 * <h3>Example</h3>
 * <pre>
 * Flux flux = Flux
 *     .from("telegraf")
 *     .derivative(1L, ChronoUnit.MINUTES);
 *
 * Flux flux = Flux
 *     .from("telegraf")
 *     .derivative()
 *         .withUnit(10L, ChronoUnit.DAYS)
 *         .withNonNegative(true)
 *         .withColumns(new String[]{"columnCompare_1", "columnCompare_2"})
 *         .withTimeColumn("_timeColumn");
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (03/07/2018 14:28)
 */
public final class DerivativeFlux extends AbstractParametrizedFlux {

    public DerivativeFlux(@Nonnull final Flux source) {
        super(source);
    }

    @Nonnull
    @Override
    protected String operatorName() {
        return "derivative";
    }

    /**
     * @param duration the time duration to use for the result
     * @param unit     a {@code ChronoUnit} determining how to interpret the {@code duration} parameter
     * @return this
     */
    @Nonnull
    public DerivativeFlux withUnit(@Nonnull final Long duration, @Nonnull final ChronoUnit unit) {

        Arguments.checkNotNull(duration, "Duration is required");
        Arguments.checkNotNull(unit, "ChronoUnit is required");

        this.withPropertyValue("unit", duration, unit);

        return this;
    }

    /**
     * @param unit the time duration to use for the result
     * @return this
     */
    @Nonnull
    public DerivativeFlux withUnit(@Nonnull final String unit) {

        Arguments.checkDuration(unit, "Unit");

        this.withPropertyValue("unit", unit);

        return this;
    }

    /**
     * @param useStartTime Indicates if the derivative is allowed to be negative
     * @return this
     */
    @Nonnull
    public DerivativeFlux withNonNegative(final boolean useStartTime) {

        this.withPropertyValue("nonNegative", useStartTime);

        return this;
    }

    /**
     * @param columns List of columns on which to compute the derivative.
     * @return this
     */
    @Nonnull
    public DerivativeFlux withColumns(@Nonnull final String[] columns) {

        Arguments.checkNotNull(columns, "Columns are required");

        this.withPropertyValue("columns", columns);

        return this;
    }

    /**
     * @param columns List of columns on which to compute the derivative.
     * @return this
     */
    @Nonnull
    public DerivativeFlux withColumns(@Nonnull final Collection<String> columns) {

        Arguments.checkNotNull(columns, "Columns are required");

        this.withPropertyValue("columns", columns);

        return this;
    }

    /**
     * @param timeColumn The source column for the time values
     * @return this
     */
    @Nonnull
    public DerivativeFlux withTimeColumn(@Nonnull final String timeColumn) {

        Arguments.checkNonEmpty(timeColumn, "Time column");

        this.withPropertyValueEscaped("timeColumn", timeColumn);

        return this;
    }
}
