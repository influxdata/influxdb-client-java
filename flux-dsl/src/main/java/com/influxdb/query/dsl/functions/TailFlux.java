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
import com.influxdb.utils.Arguments;

/**
 * Tail caps the number of records in output tables to a fixed size <i>n</i>.
 * <a href="http://bit.ly/flux-spec#tail">See SPEC</a>.
 *
 * <p>
 * <b>Options</b>
 * <ul>
 * <li><b>n</b> - The maximum number of records to output [int].</li>
 * <li><b>offset</b> - The number of records to skip per table [int]. Default to <i>0</i>.</li>
 * </ul>
 *
 * <p>
 * <b>Example</b>
 * <pre>
 * Flux flux = Flux
 *     .from("telegraf")
 *     .tail(5);
 * </pre>
 *
 * @author Jakub Bednar (14/12/2020 10:20)
 */
public final class TailFlux extends AbstractParametrizedFlux {
    public TailFlux(@Nonnull final Flux flux) {
        super(flux);
    }

    @Nonnull
    @Override
    protected String operatorName() {
        return "tail";
    }

    /**
     * @param numberOfResults The number of results
     * @return this
     */
    @Nonnull
    public TailFlux withN(final int numberOfResults) {

        Arguments.checkPositiveNumber(numberOfResults, "Number of results");

        this.withPropertyValue("n", numberOfResults);

        return this;
    }

    /**
     * @param offset The number of records to skip per table.
     * @return this
     */
    @Nonnull
    public TailFlux withOffset(final int offset) {

        Arguments.checkNotNegativeNumber(offset, "The number of records to skip");

        this.withPropertyValue("offset", offset);

        return this;
    }
}
