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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

import org.influxdata.flux.Flux;
import org.influxdata.platform.Arguments;

/**
 * Join two time series together on time and the list of <i>on</i> keys.
 * <a href="http://bit.ly/flux-spec#join">See SPEC</a>.
 *
 * <h3>Options</h3>
 * <ul>
 * <li><b>tables</b> - Map of tables to join. Currently only two tables are allowed. [map of tables]</li>
 * <li><b>on</b> - List of tag keys that when equal produces a result set. [array of strings]</li>
 * <li>
 * <b>method</b> - An optional parameter that specifies the type of join to be performed. </li>
 * </ul>
 *
 * <h3>Example</h3>
 * <pre>
 * Flux cpu = Flux.from("telegraf")
 *     .filter(Restrictions.and(Restrictions.measurement().equal("cpu"), Restrictions.field().equal("usage_user")))
 *     .range(-30L, ChronoUnit.MINUTES);
 *
 * Flux mem = Flux.from("telegraf")
 *     .filter(Restrictions.and(Restrictions.measurement().equal("mem"), Restrictions.field().equal("used_percent")))
 *     .range(-30L, ChronoUnit.MINUTES);
 *
 * Flux flux = Flux.join()
 *     .withTable("cpu", cpu)
 *     .withTable("mem", mem)
 *     .withOn("host")
 *     .withMethod("outer");
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (17/07/2018 14:47)
 */
public final class JoinFlux extends AbstractParametrizedFlux {

    private Map<String, Flux> tables = new LinkedHashMap<>();

    public JoinFlux() {
        super();

        // add tables: property
        withPropertyValue("tables", (Supplier<String>) () -> {

            StringJoiner tablesValue = new StringJoiner(", ", "{", "}");

            tables.keySet().forEach(key -> tablesValue.add(String.format("%s:%s", key, key)));
            return tablesValue.toString();
        });
    }

    public enum MethodType {

        /**
         * inner join.
         */
        INNER,

        /**
         * cross product.
         */
        CROSS,

        /**
         * left outer join.
         */
        LEFT,

        /**
         * right outer join.
         */
        RIGHT,

        /**
         * full outer join.
         */
        OUTER
    }

    @Nonnull
    @Override
    protected String operatorName() {
        return "join";
    }

    @Override
    protected void beforeAppendOperatorName(@Nonnull final StringBuilder operator,
                                            @Nonnull final Map<String, Object> parameters) {

        // add tables Flux scripts
        tables.keySet().forEach(key -> {

            operator.append(String.format("%s = %s\n", key, tables.get(key).toString(parameters)));
        });
    }

    /**
     * Map of table to join. Currently only two tables are allowed.
     *
     * @param name  table name
     * @param table Flux script to map table
     * @return this
     */
    @Nonnull
    public JoinFlux withTable(@Nonnull final String name, @Nonnull final Flux table) {

        Arguments.checkNonEmpty(name, "FluxTable name");
        Arguments.checkNotNull(table, "Flux script to map table");

        tables.put(name, table);

        return this;
    }

    /**
     * @param tag Tag key that when equal produces a result set.
     * @return this
     */
    @Nonnull
    public JoinFlux withOn(@Nonnull final String tag) {

        Arguments.checkNonEmpty(tag, "Tag name");

        return withOn(new String[]{tag});
    }

    /**
     * @param tags List of tag keys that when equal produces a result set.
     * @return this
     */
    @Nonnull
    public JoinFlux withOn(@Nonnull final String[] tags) {

        Arguments.checkNotNull(tags, "Tags are required");

        withPropertyValue("on", tags);

        return this;
    }

    /**
     * @param tags List of tag keys that when equal produces a result set.
     * @return this
     */
    @Nonnull
    public JoinFlux withOn(@Nonnull final Collection<String> tags) {

        Arguments.checkNotNull(tags, "Tags are required");

        withPropertyValue("on", tags);

        return this;
    }

    /**
     * @param method the type of join to be performed
     * @return this
     */
    @Nonnull
    public JoinFlux withMethod(@Nonnull final String method) {

        Arguments.checkNonEmpty(method, "Method");

        this.withPropertyValueEscaped("method", method);

        return this;
    }

    /**
     * @param method the type of join to be performed
     * @return this
     */
    @Nonnull
    public JoinFlux withMethod(@Nonnull final MethodType method) {

        Arguments.checkNotNull(method, "Method");

        this.withPropertyValueEscaped("method", method.toString().toLowerCase());

        return this;
    }
}
