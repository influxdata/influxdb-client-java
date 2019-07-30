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

import javax.annotation.Nonnull;

import com.influxdb.query.dsl.Flux;

/**
 * Difference between min and max values.
 * <a href="http://bit.ly/flux-spec#spread">See SPEC</a>.
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
 *     .spread();
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (25/06/2018 10:10)
 */
public final class SpreadFlux extends AbstractParametrizedFlux {

    public SpreadFlux(@Nonnull final Flux source) {
        super(source);
    }

    @Nonnull
    @Override
    protected String operatorName() {
        return "spread";
    }

    /**
     * @param useStartTime Use the start time as the timestamp of the resulting aggregate
     * @return this
     */
    @Nonnull
    public SpreadFlux withUseStartTime(final boolean useStartTime) {

        this.withPropertyValue("useStartTime", useStartTime);

        return this;
    }
}
