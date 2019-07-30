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

import java.util.Collection;
import javax.annotation.Nonnull;

import com.influxdb.Arguments;
import com.influxdb.query.dsl.Flux;

/**
 * Drop will exclude specified columns from a table.
 * <a href="http://bit.ly/flux-spec#drop">See SPEC</a>.
 *
 * <h3>Options</h3>
 * <ul>
 * <li>
 * <b>columns</b> - The list of columns which should be excluded from the resulting table.
 * Cannot be used with <i>fn</i>. [array of strings]
 * </li>
 * <li>
 * <b>fn</b> - The function which takes a column name as a parameter and returns a boolean indicating whether
 * or not the column should be excluded from the resulting table. Cannot be used with <i>columns</i>. [function(column)]
 * </li>
 * </ul>
 *
 * <h3>Example</h3>
 * <pre>
 * Flux flux = Flux
 *     .from("telegraf")
 *     .drop(new String[]{"host", "_measurement"});
 *
 * Flux flux = Flux
 *     .from("telegraf")
 *     .drop()
 *          .withFunction("column =~ /*free/");
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (02/08/2018 09:47)
 */
public final class DropFlux extends AbstractParametrizedFlux {

    public DropFlux(@Nonnull final Flux source) {
        super(source);
    }

    @Nonnull
    @Override
    protected String operatorName() {
        return "drop";
    }

    /**
     * @param columns The list of columns which should be excluded from the resulting table.
     * @return this
     */
    @Nonnull
    public DropFlux withColumns(@Nonnull final String[] columns) {

        Arguments.checkNotNull(columns, "Columns are required");

        this.withPropertyValue("columns", columns);

        return this;
    }

    /**
     * @param columns The list of columns which should be excluded from the resulting table.
     * @return this
     */
    @Nonnull
    public DropFlux withColumns(@Nonnull final Collection<String> columns) {

        Arguments.checkNotNull(columns, "Columns are required");

        this.withPropertyValue("columns", columns);

        return this;
    }

    /**
     * @param function The function which takes a column name as a parameter and returns a boolean indicating whether
     *                 or not the column should be excluded from the resulting table.
     * @return this
     */
    @Nonnull
    public DropFlux withFunction(@Nonnull final String function) {

        Arguments.checkNonEmpty(function, "Function");

        this.withFunction("fn: (column)", function);

        return this;
    }
}