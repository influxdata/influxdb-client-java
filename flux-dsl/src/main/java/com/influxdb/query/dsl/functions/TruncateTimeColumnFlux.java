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
import javax.annotation.Nonnull;

import com.influxdb.query.dsl.Flux;
import com.influxdb.utils.Arguments;

/**
 * Truncates all input time values in the _time to a specified unit..
 *
 * <p>
 * <b>Options</b>
 * <ul>
 * <li><b>unit</b> - Unit of time to truncate to. [unit].</li>
 * </ul>
 *
 * <p>
 * <b>Example</b>
 * <pre>
 * Flux flux = Flux
 *     .from("telegraf")
 *     .truncateTimeColumn("s");
 * </pre>
 *
 */
public final class TruncateTimeColumnFlux extends AbstractParametrizedFlux {

    public TruncateTimeColumnFlux(@Nonnull final Flux source) {
        super(source);
    }

    @Nonnull
    @Override
    protected String operatorName() {
        return "truncateTimeColumn";
    }

    /**
     * @param unit Unit of time to truncate to. Has to be defined.
     * @return this
     */
    @Nonnull
    public TruncateTimeColumnFlux withUnit(@Nonnull final ChronoUnit unit) {
        Arguments.checkNotNull(unit, "unit");

        withPropertyValue("unit", 1L, unit);

        return this;
    }
}
