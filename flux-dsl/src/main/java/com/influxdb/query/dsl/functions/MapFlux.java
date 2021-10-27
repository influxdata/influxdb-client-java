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
 * Applies a function to each row of the table.
 * <a href="http://bit.ly/flux-spec#map">See SPEC</a>.
 *
 * <h3>Options</h3>
 * <ul>
 * <li>
 * <b>fn</b> - The function to apply to each row.
 * The return value of the function may be a single value or an object. [function(record) value]
 * </li>
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
 * // Square the value
 * Flux flux = Flux
 *     .from("telegraf")
 *     .filter(restriction)
 *     .range(-12L, ChronoUnit.HOURS)
 *     .map("r._value * r._value");
 *
 * // Square the value and keep the original value
 * Flux flux = Flux
 *     .from("telegraf")
 *     .filter(restriction)
 *     .range(-12L, ChronoUnit.HOURS)
 *     .map("{value: r._value, value2:r._value * r._value}");
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (17/07/2018 07:48)
 */
public final class MapFlux extends AbstractParametrizedFlux {

    public MapFlux(@Nonnull final Flux source) {
        super(source);
    }

    @Nonnull
    @Override
    protected String operatorName() {
        return "map";
    }

    /**
     * @param function The function for map row of table. Example: "r._value * r._value".
     * @return this
     */
    @Nonnull
    public MapFlux withFunction(@Nonnull final String function) {

        Arguments.checkNonEmpty(function, "Function");

        this.withFunction("fn: (r)", function);

        return this;
    }
}
