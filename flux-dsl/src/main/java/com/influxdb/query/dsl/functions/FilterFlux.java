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
import com.influxdb.query.dsl.functions.restriction.Restrictions;
import com.influxdb.utils.Arguments;

/**
 * Filters the results using an expression.
 * <a href="http://bit.ly/flux-spec#filter">See SPEC</a>.
 *
 * <p>
 * <b>Options</b>
 * <ul>
 * <li>
 * <b>fn</b> - Function to when filtering the records. The function must accept a single parameter
 * which will be the records and return a boolean value. Records which evaluate to true,
 * will be included in the results. [function(record) bool]
 * </li>
 * </ul>
 *
 * <p>
 * <b>Example</b>
 * <pre>
 *  Restrictions restriction = Restrictions.and(
 *          Restrictions.measurement().equal("mem"),
 *          Restrictions.field().equal("usage_system"),
 *          Restrictions.tag("service").equal("app-server")
 * );
 *
 * Flux flux = Flux
 *          .from("telegraf")
 *          .filter(restriction)
 *          .range(-4L, ChronoUnit.HOURS)
 *          .count();
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (28/06/2018 14:12)
 */
public final class FilterFlux extends AbstractParametrizedFlux {

    public FilterFlux(@Nonnull final Flux source) {
        super(source);
    }

    @Nonnull
    @Override
    protected String operatorName() {
        return "filter";
    }

    /**
     * @param restrictions filter restrictions
     * @return this
     */
    @Nonnull
    public FilterFlux withRestrictions(@Nonnull final Restrictions restrictions) {

        Arguments.checkNotNull(restrictions, "Restrictions are required");

        this.withFunction("fn: (r)", restrictions);

        return this;
    }
}
