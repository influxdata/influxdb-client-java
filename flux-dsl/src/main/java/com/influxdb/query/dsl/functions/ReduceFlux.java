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
 * Reduce aggregates records in each table according to the reducer.
 * <a href="http://bit.ly/flux-spec#reduce">See SPEC</a>.
 *
 * <h3>Options</h3>
 * <ul>
 * <li>
 * <b>fn</b> -
 * Function to apply to each record with a reducer object of type 'a. [(r: record, accumulator: 'a) -&gt; 'a]
 * </li>
 * <li><b>identity</b> - an initial value to use when creating a reducer ['a]</li>
 * </ul>
 *
 * <h3>Example</h3>
 * <pre>
 * Restrictions restriction = Restrictions.and(
 *     Restrictions.measurement().equal("cpu"),
 *     Restrictions.field().equal("usage_system"),
 *     Restrictions.tag("service").equal("app-server")
 * );
 *
 * Flux flux = Flux
 *     .from("telegraf")
 *     .filter(restriction)
 *     .range(-12L, ChronoUnit.HOURS)
 *     .reduce("{ sum: r._value + accumulator.sum }", "{sum: 0.0}");
 *
 * Flux flux = Flux
 *     .from("telegraf")
 *     .filter(restriction)
 *     .range(-12L, ChronoUnit.HOURS)
 *     .reduce()
 *         .withFunction("{sum: r._value + accumulator.sum,\ncount: accumulator.count + 1.0}")
 *         .withIdentity("{sum: 0.0, count: 0.0}");
 * </pre>
 *
 * @author Jakub Bednar (24/02/2020 13:02)
 */
public final class ReduceFlux extends AbstractParametrizedFlux {
    public ReduceFlux(@Nonnull final Flux source) {
        super(source);
    }

    @Nonnull
    @Override
    protected String operatorName() {
        return "reduce";
    }

    /**
     * @param function Function to apply to each record. Example: "{sum: r._value + accumulator.sum}".
     * @return this
     */
    @Nonnull
    public ReduceFlux withFunction(@Nonnull final String function) {

        Arguments.checkNonEmpty(function, "Function");

        this.withFunction("fn: (r, accumulator)", String.format("(%s)", function));

        return this;
    }

    /**
     * @param identity An initial value to use when creating a reducer. Example: "{sum: 0.0}".
     * @return this
     */
    @Nonnull
    public ReduceFlux withIdentity(@Nonnull final String identity) {
        Arguments.checkNonEmpty(identity, "identity");

        this.withPropertyValue("identity", identity);

        return this;
    }
}
