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

import java.time.temporal.ChronoUnit;
import java.util.Objects;
import javax.annotation.Nonnull;

import org.influxdata.flux.Flux;
import org.influxdata.platform.Arguments;

/**
 * <a href="https://github.com/influxdata/platform/blob/master/query/docs/SPEC.md#integral">integral</a> -
 * For each aggregate column, it outputs the area under the curve of non null records.
 * The curve is defined as function where the domain is the record times and the range is the record values.
 *
 * <h3>Options</h3>
 * <ul>
 * <li><b>unit</b> - Time duration to use when computing the integral [duration]</li>
 * </ul>
 *
 * <h3>Example</h3>
 * <pre>
 * Flux flux = Flux
 *     .from("telegraf")
 *     .integral(1L, ChronoUnit.MINUTES);
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (03/07/2018 12:33)
 * @since 1.0.0
 */
public final class IntegralFlux extends AbstractParametrizedFlux {

    public IntegralFlux(@Nonnull final Flux source) {
        super(source);
    }

    @Nonnull
    @Override
    protected String operatorName() {
        return "integral";
    }

    /**
     * @param duration Time duration to use when computing the integral
     * @param unit     a {@code ChronoUnit} determining how to interpret the {@code duration} parameter
     * @return this
     */
    @Nonnull
    public IntegralFlux withUnit(@Nonnull final Long duration, @Nonnull final ChronoUnit unit) {

        Objects.requireNonNull(duration, "Duration is required");
        Objects.requireNonNull(unit, "ChronoUnit is required");

        this.withPropertyValue("unit", duration, unit);

        return this;
    }

    /**
     * @param unit Time duration to use when computing the integral
     * @return this
     */
    @Nonnull
    public IntegralFlux withUnit(@Nonnull final String unit) {

        Arguments.checkDuration(unit, "Unit");

        this.withPropertyValue("unit", unit);

        return this;
    }
}
