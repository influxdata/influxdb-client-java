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

import javax.annotation.Nonnull;

import org.influxdata.flux.Flux;
import org.influxdata.platform.Arguments;

/**
 * <a href="http://bit.ly/flux-spec#set">set</a> - Yield a query results
 * to yielded results.
 *
 * <h3>Options</h3>
 * <ul>
 * <li><b>name</b> - The unique name to give to yielded results [string].</li>
 * </ul>
 *
 * <h3>Example</h3>
 * <pre>
 * Flux flux = Flux
 *     .from("telegraf")
 *     .yield("0");
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (29/06/2018 09:55)
 */
public final class YieldFlux extends AbstractParametrizedFlux {

    public YieldFlux(@Nonnull final Flux source) {
        super(source);
    }

    @Nonnull
    @Override
    protected String operatorName() {
        return "yield";
    }

    /**
     * @param name The unique name to give to yielded results. Has to be defined.
     * @return this
     */
    @Nonnull
    public final YieldFlux withName(@Nonnull final String name) {
        Arguments.checkNonEmpty(name, "Result name");

        withPropertyValueEscaped("name", name);

        return this;
    }
}
