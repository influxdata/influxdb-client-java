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

import javax.annotation.Nonnull;

import org.influxdata.flux.Flux;

/**
 * <a href="https://github.com/influxdata/platform/tree/master/query#sample">sample</a> - Sample values from a table.
 *
 * <h3>Options</h3>
 * <ul>
 * <li><b>n</b> - Sample every Nth element [int]</li>
 * <li>
 * <b>pos</b> - Position offset from start of results to begin sampling. <i>pos</i> must be less than <i>n</i>.
 * If <i>pos</i> less than 0, a random offset is used. Default is -1 (random offset) [int].
 * </li>
 * </ul>
 *
 * <h3>Example</h3>
 * <pre>
 * Flux flux = Flux.from("telegraf")
 *     .filter(and(measurement().equal("cpu"), field().equal("usage_system")))
 *     .range(-1L, ChronoUnit.DAYS)
 *     .sample(10);
 *
 * Flux flux = Flux.from("telegraf")
 *     .filter(and(measurement().equal("cpu"), field().equal("usage_system")))
 *     .range(-1L, ChronoUnit.DAYS)
 *     .sample(5, 1);
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (29/06/2018 07:25)
 * @since 1.0.0
 */
public final class SampleFlux extends AbstractParametrizedFlux {

    public SampleFlux(@Nonnull final Flux source) {
        super(source);
    }

    @Nonnull
    @Override
    protected String operatorName() {
        return "sample";
    }

    /**
     * @param n Sample every Nth element.
     * @return this
     */
    @Nonnull
    public SampleFlux withN(final int n) {
        withPropertyValue("n", n);

        return this;
    }

    /**
     * @param pos Position offset from start of results to begin sampling. Must be less than @{code n}.
     * @return this
     */
    @Nonnull
    public SampleFlux withPos(final int pos) {
        withPropertyValue("pos", pos);

        return this;
    }
}
