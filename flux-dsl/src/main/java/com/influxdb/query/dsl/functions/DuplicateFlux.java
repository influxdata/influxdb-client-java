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

import com.influxdb.Arguments;
import com.influxdb.query.dsl.Flux;

/**
 * Duplicate will duplicate a specified column in a table.
 * <a href="http://bit.ly/flux-spec#duplicate">See SPEC</a>.
 *
 * <h3>Options</h3>
 * <ul>
 * <li>
 * <b>column</b> - The column to duplicate. [string]
 * </li>
 * <li>
 * <b>as</b> - The name that should be assigned to the duplicate column. [string]
 * </li>
 * </ul>
 *
 * <h3>Example</h3>
 * <pre>
 * Flux flux = Flux
 *     .from("telegraf")
 *     .duplicate("host", "server");
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (09/10/2018 13:13)
 */
public final class DuplicateFlux extends AbstractParametrizedFlux {

    public DuplicateFlux(@Nonnull final Flux source) {
        super(source);
    }

    @Nonnull
    @Override
    protected String operatorName() {
        return "duplicate";
    }

    /**
     * @param column the column to duplicate
     * @return this
     */
    @Nonnull
    public DuplicateFlux withColumn(@Nonnull final String column) {

        Arguments.checkNonEmpty(column, "column");

        this.withPropertyValueEscaped("column", column);

        return this;
    }

    /**
     * @param as the name that should be assigned to the duplicate column
     * @return this
     */
    @Nonnull
    public DuplicateFlux withAs(@Nonnull final String as) {

        Arguments.checkNonEmpty(as, "as");

        this.withPropertyValueEscaped("as", as);

        return this;
    }
}