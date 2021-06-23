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

import com.influxdb.Arguments;
import com.influxdb.query.dsl.Flux;

/**
 * Shift add a fixed duration to time columns.
 * <a href="http://bit.ly/flux-spec#timeshift">See SPEC</a>.
 *
 * <h3>Options</h3>
 * <ul>
 * <li><b>duration</b> - The amount to add to each time value [duration]</li>
 * <li><b>columns</b> - The list of all columns that should be shifted.
 * Defaults `["_start", "_stop", "_time"]` [array of strings].</li>
 * </ul>
 *
 * <h3>Example</h3>
 * <pre>
 * Flux flux = Flux
 *     .from("telegraf")
 *     .timeShift(10L, ChronoUnit.HOURS);
 *
 * Flux flux = Flux
 *     .from("telegraf")
 *     .timeShift(10L, ChronoUnit.HOURS, new String[]{"_time", "custom"});
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (29/06/2018 10:27)
 */
public final class TimeShiftFlux extends AbstractParametrizedFlux {

    public TimeShiftFlux(@Nonnull final Flux source) {
        super(source);
    }

    @Nonnull
    @Override
    protected String operatorName() {
        return "timeShift";
    }

    /**
     * @param amount The amount to add to each time value
     * @param unit   a {@code ChronoUnit} determining how to interpret the {@code amount} parameter
     * @return this
     */
    @Nonnull
    public TimeShiftFlux withDuration(@Nonnull final Long amount, @Nonnull final ChronoUnit unit) {
        Arguments.checkNotNull(amount, "Amount is required");
        Arguments.checkNotNull(unit, "ChronoUnit is required");

        this.withPropertyValue("duration", amount, unit);

        return this;
    }

    /**
     * @param amount The amount to add to each time value
     * @return this
     */
    @Nonnull
    public TimeShiftFlux withDuration(@Nonnull final String amount) {
        Arguments.checkDuration(amount, "Amount");

        this.withPropertyValue("duration", amount);

        return this;
    }

    /**
     * @param columns The list of all columns that should be shifted.
     * @return this
     */
    @Nonnull
    public TimeShiftFlux withColumns(@Nonnull final String[] columns) {

        Arguments.checkNotNull(columns, "Columns are required");

        this.withPropertyValue("columns", columns);

        return this;
    }

    /**
     * @param columns The list of all columns that should be shifted.
     * @return this
     */
    @Nonnull
    public TimeShiftFlux withColumns(@Nonnull final Collection<String> columns) {

        Arguments.checkNotNull(columns, "Columns are required");

        this.withPropertyValue("columns", columns);

        return this;
    }
}
