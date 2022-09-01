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
 * Inserts rows at regular intervals using linear interpolation to determine values for inserted rows.
 *
 * <p>
 * <b>Example</b>
 * <pre>
 *         Flux flux = Flux
 *                 .from("telegraf")
 *                 .interpolateLinear()
 *                 .withEvery(5L, ChronoUnit.MINUTES);
 * </pre>
 */
public class InterpolateLinearFlux extends AbstractParametrizedFlux {

    public InterpolateLinearFlux(@Nonnull final Flux source) {
        super(source);
        addImport("interpolate");
    }

    @Nonnull
    @Override
    protected String operatorName() {
        return "interpolate.linear";
    }

    /**
     *
     * @param duration Time duration to use when computing the interpolation
     * @param unit The unit of the <code>duration</code>
     * @return this
     */
    public InterpolateLinearFlux withEvery(final long duration, final ChronoUnit unit) {
        this.withPropertyValue("every", duration, unit);
        return this;
    }

    /**
     * @param every Time duration to use when computing the interpolation
     * @return this
     */
    @Nonnull
    public InterpolateLinearFlux withEvery(@Nonnull final String every) {

        Arguments.checkDuration(every, "every");

        this.withPropertyValue("every", every);

        return this;
    }
}
