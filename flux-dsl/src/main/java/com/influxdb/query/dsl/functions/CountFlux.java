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
 * Counts the number of results. <a href="http://bit.ly/flux-spec#count">See SPEC</a>.
 *
 * <h3>Options</h3>
 * <ul>
 * <li><b>column</b> - The column to aggregate [string]. Default to <i>_value</i>.</li>
 * </ul>
 *
 * <h3>Example</h3>
 * <pre>
 * Flux flux = Flux
 *      .from("telegraf")
 *      .count();
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (22/06/2018 11:06)
 */
public final class CountFlux extends AbstractParametrizedFlux {

    public CountFlux(@Nonnull final Flux source) {
        super(source);
    }

    @Nonnull
    @Override
    protected String operatorName() {
        return "count";
    }

    /**
     * @param column The column to aggregate.
     * @return this
     */
    @Nonnull
    public CountFlux withColumn(@Nonnull final String column) {

        Arguments.checkNonEmpty(column, "Column");

        this.withPropertyValueEscaped("column", column);

        return this;
    }
}
