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
package org.influxdata.flux.dsl.functions.restriction;

import javax.annotation.Nonnull;

import org.influxdata.Arguments;
import org.influxdata.flux.dsl.functions.properties.FunctionsParameters;

/**
 * The column restrictions.
 *
 * @author Jakub Bednar (bednar@github) (28/06/2018 13:04)
 */
public final class ColumnRestriction {

    private final String fieldName;

    ColumnRestriction(@Nonnull final String recordColumn) {

        Arguments.checkNonEmpty(recordColumn, "Record column");

        this.fieldName = recordColumn;
    }

    /**
     * Is column of record "equal" than {@code value}?
     *
     * @param value the value to compare
     * @return restriction
     */
    @Nonnull
    public Restrictions equal(@Nonnull final Object value) {
        return custom(value, "==");
    }

    /**
     * Is column of record "not equal" than {@code value}?
     *
     * @param value the value to compare
     * @return restriction
     */
    @Nonnull
    public Restrictions notEqual(@Nonnull final Object value) {
        return custom(value, "!=");
    }

    /**
     * Is column of record "less" than {@code value}?
     *
     * @param value the value to compare
     * @return restriction
     */
    @Nonnull
    public Restrictions less(@Nonnull final Object value) {
        return custom(value, "<");
    }

    /**
     * Is column of record "greater" than {@code value}?
     *
     * @param value the value to compare
     * @return restriction
     */
    @Nonnull
    public Restrictions greater(@Nonnull final Object value) {
        return custom(value, ">");
    }

    /**
     * Is column of record "less or equal" than {@code value}?
     *
     * @param value the value to compare
     * @return restriction
     */
    @Nonnull
    public Restrictions lessOrEqual(@Nonnull final Object value) {
        return custom(value, "<=");
    }

    /**
     * Is column of record "greater or equal" than {@code value}?
     *
     * @param value the value to compare
     * @return restriction
     */
    @Nonnull
    public Restrictions greaterOrEqual(@Nonnull final Object value) {
        return custom(value, ">=");
    }

    /**
     * Is column of record "{@code operator}" than {@code value}?
     *
     * @param value    the value to compare
     * @param operator the restriction operator
     * @return restriction
     */
    @Nonnull
    public Restrictions custom(@Nonnull final Object value, @Nonnull final String operator) {
        return new OperatorRestrictions(fieldName, value, operator);
    }

    private final class OperatorRestrictions extends Restrictions {
        private final String fieldName;
        private final Object fieldValue;
        private final String operator;

        private OperatorRestrictions(@Nonnull final String fieldName,
                                     @Nonnull final Object fieldValue,
                                     @Nonnull final String operator) {
            this.fieldName = fieldName;
            this.fieldValue = fieldValue;
            this.operator = operator;
        }

        @Override
        public String toString() {

            String value;
            if (fieldValue instanceof String) {
                value = "\"" + fieldValue + "\"";
            } else {
                value = FunctionsParameters.serializeValue(fieldValue);
            }

            return "r[\"" + fieldName + "\"] " + operator + " " + value;
        }
    }
}
