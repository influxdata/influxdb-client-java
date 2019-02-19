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
package org.influxdata.query.dsl.functions;

import javax.annotation.Nonnull;

import org.influxdata.Arguments;
import org.influxdata.query.dsl.Flux;

/**
 * Assigns a static value to each record.
 * <a href="http://bit.ly/flux-spec#set">See SPEC</a>.
 *
 * <h3>Options</h3>
 * <ul>
 * <li><b>key</b> - Label for the column to set [string].</li>
 * <li><b>value</b> - Value for the column to set [string]</li>
 * </ul>
 *
 * <h3>Example</h3>
 * <pre>
 * Flux flux = Flux
 *     .from("telegraf")
 *     .set("location", "Carolina");
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (29/06/2018 09:19)
 */
public final class SetFlux extends AbstractParametrizedFlux {

    public SetFlux(@Nonnull final Flux source) {
        super(source);
    }

    @Nonnull
    @Override
    protected String operatorName() {
        return "set";
    }

    /**
     * @param key   label for the column. Has to be defined.
     * @param value value for the column. Has to be defined.
     * @return this
     */
    @Nonnull
    public SetFlux withKeyValue(@Nonnull final String key, @Nonnull final String value) {

        Arguments.checkNonEmpty(key, "Key");
        Arguments.checkNonEmpty(value, "Value");

        withPropertyValueEscaped("key", key);
        withPropertyValueEscaped("value", value);

        return this;
    }
}
