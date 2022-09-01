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
 * Replaces all null values in input tables with a non-null value.
 * <a href="http://bit.ly/flux-spec#fill">See SPEC</a>.
 *
 * <p>
 * <b>Example</b>
 * <pre>
 * Flux flux = Flux
 *     .from("telegraf")
 *     .fill();
 * </pre>
 */
public final class FillFlux extends AbstractParametrizedFlux {

    public FillFlux(@Nonnull final Flux source) {
        super(source);
    }

    @Nonnull
    @Override
    protected String operatorName() {
        return "fill";
    }

    /**
     * @param column The column to fill. Defaults to "_value".
     * @return this
     */
    @Nonnull
    public FillFlux withColumn(@Nonnull final String column) {

        Arguments.checkNonEmpty(column, "column");

        this.withPropertyValueEscaped("column", column);

        return this;
    }

    /**
     * @param value The constant value to use in place of nulls. The type must match the type of the valueColumn.
     * @return this
     */
    @Nonnull
    public FillFlux withValue(@Nonnull final Object value) {
        Arguments.checkNotNull(value, "value");

        if (value instanceof String) {
            this.withPropertyValueEscaped("value", (String) value);
        } else {
            this.withPropertyValue("value", value);
        }

        return this;
    }

    /**
     * @param usePrevious If set, then assign the value set in the previous non-null row. Cannot be used with value.
     * @return this
     */
    @Nonnull
    public FillFlux withUsePrevious(@Nonnull final Boolean usePrevious) {
        Arguments.checkNotNull(usePrevious, "usePrevious");

        this.withPropertyValue("usePrevious", usePrevious);

        return this;
    }
}
