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
package org.influxdata.query.dsl.functions;

import javax.annotation.Nonnull;

import org.influxdata.query.dsl.Flux;

/**
 * Skew of the results.
 * <a href="http://bit.ly/flux-spec#skew">See SPEC</a>.
 *
 * <h3>Options</h3>
 * <ul>
 * <li><b>useStartTime</b> - Use the start time as the timestamp of the resulting aggregate [boolean]
 * </ul>
 *
 * <h3>Example</h3>
 * <pre>
 * Flux flux = Flux
 *     .from("telegraf")
 *     .range(-30L, -15L, ChronoUnit.MINUTES)
 *     .skew();
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (25/06/2018 10:06)
 */
public final class SkewFlux extends AbstractParametrizedFlux {

    public SkewFlux(@Nonnull final Flux source) {
        super(source);
    }

    @Nonnull
    @Override
    protected String operatorName() {
        return "skew";
    }

    /**
     * @param useStartTime Use the start time as the timestamp of the resulting aggregate
     * @return this
     */
    @Nonnull
    public SkewFlux withUseStartTime(final boolean useStartTime) {

        this.withPropertyValue("useStartTime", useStartTime);

        return this;
    }
}
