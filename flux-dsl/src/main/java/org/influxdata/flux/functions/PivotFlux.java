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
import javax.annotation.Nonnull;

import org.influxdata.flux.Flux;
import org.influxdata.platform.Arguments;

/**
 * Pivot collects values stored vertically (column-wise) in a table and aligns them horizontally (row-wise)
 * into logical sets.
 * <a href="http://bit.ly/flux-spec#pivot">See SPEC</a>.
 *
 * <h3>Options</h3>
 * <ul>
 * <li><b>rowKey</b> - List of columns used to uniquely identify a row for the output. [array of strings]</li>
 * <li><b>colKey</b> -
 * List of columns used to pivot values onto each row identified by the rowKey. [array of strings]</li>
 * <li><b>valueCol</b> - Identifies the single column that contains the value to be moved around the pivot [string]</li>
 * </ul>
 *
 * <h3>Example</h3>
 * <pre>
 * Flux flux = Flux.from("telegraf")
 *     .pivot()
 *         .withRowKey(new String[]{"_time"})
 *         .withColKey(new String[]{"_field"})
 *         .withValueCol("_value");
 * </pre>
 *
 * @author Jakub Bednar (10/10/2018 06:16)
 */
public final class PivotFlux extends AbstractParametrizedFlux {

    public PivotFlux(@Nonnull final Flux source) {
        super(source);
    }

    @Nonnull
    @Override
    protected String operatorName() {
        return "pivot";
    }

    /**
     * @param rowKey the columns used to uniquely identify a row for the output.
     * @return this
     */
    @Nonnull
    public PivotFlux withRowKey(@Nonnull final String[] rowKey) {

        Arguments.checkNotNull(rowKey, "rowKey");

        this.withPropertyValue("rowKey", rowKey);

        return this;
    }

    /**
     * @param rowKey the columns used to uniquely identify a row for the output.
     * @return this
     */
    @Nonnull
    public PivotFlux withRowKey(@Nonnull final Collection<String> rowKey) {

        Arguments.checkNotNull(rowKey, "rowKey");

        this.withPropertyValue("rowKey", rowKey);

        return this;
    }

    /**
     * @param colKey the columns used to pivot values onto each row identified by the rowKey.
     * @return this
     */
    @Nonnull
    public PivotFlux withColKey(@Nonnull final String[] colKey) {

        Arguments.checkNotNull(colKey, "colKey");

        this.withPropertyValue("colKey", colKey);

        return this;
    }

    /**
     * @param colKey the columns used to pivot values onto each row identified by the rowKey.
     * @return this
     */
    @Nonnull
    public PivotFlux withColKey(@Nonnull final Collection<String> colKey) {

        Arguments.checkNotNull(colKey, "colKey");

        this.withPropertyValue("colKey", colKey);

        return this;
    }

    /**
     * @param valueCol the single column that contains the value to be moved around the pivot
     * @return this
     */
    @Nonnull
    public PivotFlux withValueCol(@Nonnull final String valueCol) {

        Arguments.checkNonEmpty(valueCol, "valueCol");

        this.withPropertyValueEscaped("valueCol", valueCol);

        return this;
    }
}
