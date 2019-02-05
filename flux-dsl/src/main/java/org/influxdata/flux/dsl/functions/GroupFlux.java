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
 * Groups results by a user-specified set of tags.
 * <a href="http://bit.ly/flux-spec#group">See SPEC</a>.
 *
 * <h3>Options</h3>
 * <ul>
 * <li><b>columns</b> - List of columns used to calculate the new group key. Default <i>[]</i> [array of strings]</li>
 * <li><b>mode</b> -  The grouping mode, can be one of <i>by</i> or <i>except</i>. The default is <i>by</i> [string].
 * </ul>
 *
 * <h3>Example</h3>
 * <pre>
 * Flux.from("telegraf")
 *     .range(-30L, ChronoUnit.MINUTES)
 *     .groupBy(new String[]{"tag_a", "tag_b"});
 *
 * Flux.from("telegraf")
 *     .range(-30L, ChronoUnit.MINUTES)
 *     .groupBy("tag_a"});
 *
 * Flux.from("telegraf")
 *     .range(-30L, ChronoUnit.MINUTES)
 *     .groupExcept(new String[]{"tag_c"});
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (25/06/2018 14:56)
 */
public final class GroupFlux extends AbstractParametrizedFlux {

    public GroupFlux(@Nonnull final Flux source) {

        super(source);
    }

    @Nonnull
    @Override
    protected String operatorName() {
        return "group";
    }

    /**
     * @param groupBy Group by these specific tag name.
     * @return this
     */
    @Nonnull
    public GroupFlux withBy(@Nonnull final String groupBy) {

        Arguments.checkNotNull(groupBy, "GroupBy Column are required");

        this.withPropertyValue("columns", new String[]{groupBy});

        return this;
    }

    /**
     * @param groupBy Group by these specific tag names.
     * @return this
     */
    @Nonnull
    public GroupFlux withBy(@Nonnull final String[] groupBy) {

        Arguments.checkNotNull(groupBy, "GroupBy Columns are required");

        this.withPropertyValue("columns", groupBy);

        return this;
    }

    /**
     * @param groupBy Group by these specific tag names.
     * @return this
     */
    @Nonnull
    public GroupFlux withBy(@Nonnull final Collection<String> groupBy) {

        Arguments.checkNotNull(groupBy, "GroupBy Columns are required");

        this.withPropertyValue("columns", groupBy);

        return this;
    }

    /**
     * @param except Group by all but these tag key Cannot be used.
     * @return this
     */
    @Nonnull
    public GroupFlux withExcept(@Nonnull final String except) {

        Arguments.checkNotNull(except, "GroupBy Except Columns are required");

        this.withPropertyValue("columns", new String[]{except});
        this.withPropertyValueEscaped("mode", "except");

        return this;
    }

    /**
     * @param except Group by all but these tag keys Cannot be used.
     * @return this
     */
    @Nonnull
    public GroupFlux withExcept(@Nonnull final String[] except) {

        Arguments.checkNotNull(except, "GroupBy Except Columns are required");

        this.withPropertyValue("columns", except);
        this.withPropertyValueEscaped("mode", "except");

        return this;
    }

    /**
     * @param except Group by all but these tag keys Cannot be used.
     * @return this
     */
    @Nonnull
    public GroupFlux withExcept(@Nonnull final Collection<String> except) {

        Arguments.checkNotNull(except, "GroupBy Except Columns are required");

        this.withPropertyValue("columns", except);
        this.withPropertyValueEscaped("mode", "except");

        return this;
    }
}
