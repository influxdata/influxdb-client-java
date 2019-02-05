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

import java.util.Collection;
import javax.annotation.Nonnull;

import org.influxdata.client.Arguments;
import org.influxdata.flux.dsl.Flux;

/**
 * Sorts the results by the specified columns Default sort is ascending.
 * <a href="http://bit.ly/flux-spec#sort">See SPEC</a>.
 *
 * <h3>Options</h3>
 * <ul>
 * <li>
 * <b>columns</b> - List of columns used to sort; precedence from left to right.
 * Default is ["value"] [array of strings]
 * </li>
 * <li><b>desc</b> - Sort results descending. Default false [bool]</li>
 * </ul>
 *
 * <h3>Example</h3>
 * <pre>
 * Flux flux = Flux
 *     .from("telegraf")
 *     .sort(new String[]{"region", "value"});
 *
 *     from(bucket:"telegraf")
 *          |&gt; filter(fn: (r) =&gt; r["_measurement"] == "system" AND r["_field"] == "uptime")
 *          |&gt; range(start:-12h)
 *          |&gt; sort(desc: true)
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (25/06/2018 13:20)
 */
public final class SortFlux extends AbstractParametrizedFlux {

    public SortFlux(@Nonnull final Flux flux) {
        super(flux);
    }

    @Nonnull
    @Override
    protected String operatorName() {
        return "sort";
    }

    /**
     * @param desc use the descending sorting
     * @return this
     */
    @Nonnull
    public SortFlux withDesc(final boolean desc) {

        this.withPropertyValue("desc", desc);

        return this;
    }

    /**
     * @param columns columns used to sort
     * @return this
     */
    @Nonnull
    public SortFlux withColumns(@Nonnull final String[] columns) {

        Arguments.checkNotNull(columns, "Columns are required");

        this.withPropertyValue("columns", columns);

        return this;
    }

    /**
     * @param columns columns used to sort
     * @return this
     */
    @Nonnull
    public SortFlux withColumns(@Nonnull final Collection<String> columns) {

        Arguments.checkNotNull(columns, "Columns are required");

        this.withPropertyValue("columns", columns);

        return this;
    }
}
