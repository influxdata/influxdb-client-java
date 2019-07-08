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

import java.util.Map;
import javax.annotation.Nonnull;

import com.influxdb.Arguments;
import com.influxdb.query.dsl.Flux;

/**
 * Rename will rename specified columns in a table. If a column is renamed and is part of the group key,
 * the column name in the group key will be updated.
 * <a href="http://bit.ly/flux-spec#rename">See SPEC</a>.
 *
 * <h3>Options</h3>
 * <ul>
 * <li>
 * <b>columns</b> - The map of columns to rename and their corresponding new names. Cannot be used with <i>fn</i>.
 * [map of columns]
 * </li>
 * <li>
 * <b>fn</b> - The function which takes a single string parameter (the old column name) and
 * returns a string representing the new column name. Cannot be used with <i>columns</i>. [function(column)]
 * </li>
 * </ul>
 *
 * <h3>Example</h3>
 * <pre>
 * Map&lt;String, String&gt; map = new HashMap&lt;&gt;();
 * map.put("host", "server");
 * map.put("_value", "val");
 *
 * Flux flux = Flux
 *     .from("telegraf")
 *     .rename(map);
 *
 * Flux flux = Flux
 *     .from("telegraf")
 *     .rename("\"{col}_new\"");
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (02/08/2018 11:48)
 */
public final class RenameFlux extends AbstractParametrizedFlux {

    public RenameFlux(@Nonnull final Flux source) {
        super(source);
    }

    @Nonnull
    @Override
    protected String operatorName() {
        return "rename";
    }

    /**
     * @param columns The map of columns to rename and their corresponding new names.
     * @return this
     */
    @Nonnull
    public RenameFlux withColumns(@Nonnull final Map<String, String> columns) {

        Arguments.checkNotNull(columns, "Columns are required");

        this.withPropertyValue("columns", columns);

        return this;
    }

    /**
     * @param function The function which takes a single string parameter (the old column name) and
     *                 returns a string representing the new column name.
     * @return this
     */
    @Nonnull
    public RenameFlux withFunction(@Nonnull final String function) {

        Arguments.checkNonEmpty(function, "Function");

        this.withFunction("fn: (column)", function);

        return this;
    }
}