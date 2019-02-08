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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import javax.annotation.Nonnull;

import org.influxdata.Arguments;
import org.influxdata.flux.dsl.Flux;

/**
 * Filters the results by time boundaries.
 * <a href="http://bit.ly/flux-spec#range">See SPEC</a>.
 *
 * <h3>Options</h3>
 * <ul>
 * <li><b>start</b> - Specifies the oldest time to be included in the results [duration or timestamp]</li>
 * <li>
 * <b>stop</b> - Specifies the exclusive newest time to be included in the results.
 * Defaults to "now". [duration or timestamp]
 * </li>
 * </ul>
 *
 * <h3>Example</h3>
 * <pre>
 * Flux flux = Flux
 *     .from("telegraf")
 *     .range(-12L, -1L, ChronoUnit.HOURS)
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (26/06/2018 07:04)
 */
public final class RangeFlux extends AbstractParametrizedFlux {

    public RangeFlux(@Nonnull final Flux flux) {
        super(flux);
    }

    @Nonnull
    @Override
    protected String operatorName() {
        return "range";
    }

    /**
     * @param start Specifies the oldest time to be included in the results
     * @return this
     */
    @Nonnull
    public RangeFlux withStart(@Nonnull final Instant start) {

        Arguments.checkNotNull(start, "Start is required");

        this.withPropertyValue("start", start);

        return this;
    }

    /**
     * @param start Specifies the oldest time to be included in the results
     * @param unit  a {@code ChronoUnit} determining how to interpret the {@code start} parameter
     * @return this
     */
    @Nonnull
    public RangeFlux withStart(@Nonnull final Long start, @Nonnull final ChronoUnit unit) {

        Arguments.checkNotNull(start, "Start is required");
        Arguments.checkNotNull(unit, "ChronoUnit is required");

        this.withPropertyValue("start", start, unit);

        return this;
    }

    /**
     * @param start Specifies the oldest time to be included in the results
     * @return this
     */
    @Nonnull
    public RangeFlux withStart(@Nonnull final String start) {

        Arguments.checkDuration(start, "Start");

        this.withPropertyValue("start", start);

        return this;
    }

    /**
     * @param stop Specifies the exclusive newest time to be included in the results
     * @return this
     */
    @Nonnull
    public RangeFlux withStop(@Nonnull final Instant stop) {

        Arguments.checkNotNull(stop, "Stop is required");

        this.withPropertyValue("stop", stop);

        return this;
    }

    /**
     * @param stop Specifies the exclusive newest time to be included in the results
     * @param unit a {@code ChronoUnit} determining how to interpret the {@code start} parameter
     * @return this
     */
    @Nonnull
    public RangeFlux withStop(@Nonnull final Long stop, @Nonnull final ChronoUnit unit) {

        Arguments.checkNotNull(stop, "Stop is required");
        Arguments.checkNotNull(unit, "ChronoUnit is required");

        this.withPropertyValue("stop", stop, unit);

        return this;
    }

    /**
     * @param stop Specifies the exclusive newest time to be included in the results
     * @return this
     */
    @Nonnull
    public RangeFlux withStop(@Nonnull final String stop) {

        Arguments.checkDuration(stop, "Stop");

        this.withPropertyValue("stop", stop);

        return this;
    }
}
