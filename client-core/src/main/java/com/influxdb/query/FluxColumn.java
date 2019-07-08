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
package com.influxdb.query;

import java.util.StringJoiner;
import javax.annotation.Nonnull;

/**
 * This class represents a column header specification of {@link FluxTable}.
 * <p>
 * The mapping data types for a column are:
 * <ul>
 * <li>"boolean" to {@link Boolean}</li>
 * <li>"unsignedLong" to {@link Long}</li>
 * <li>"long" to {@link Long}</li>
 * <li>"double" to {@link Double}</li>
 * <li>"string" to {@link String}</li>
 * <li>"base64Binary" to {@link byte}s array</li>
 * <li>"dateTime:RFC3339" to {@link java.time.Instant}</li>
 * <li>"dateTime:RFC3339Nano" to {@link java.time.Instant}</li>
 * <li>"duration" to {@link java.time.Duration}</li>
 * </ul>
 */
public final class FluxColumn {

    /**
     * Column index in record.
     */
    private int index;

    /**
     * The label of column (e.g., "_start", "_stop", "_time").
     */
    private String label;

    /**
     * The data type of column (e.g., "string", "long", "dateTime:RFC3339").
     */
    private String dataType;

    /**
     * Boolean flag indicating if the column is part of the table's group key.
     */
    private boolean group;

    /**
     * Default value to be used for rows whose string value is the empty string.
     */
    private String defaultValue;

    @Nonnull
    public String getDataType() {
        return dataType;
    }

    public void setDataType(final String dataType) {
        this.dataType = dataType;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(final int index) {
        this.index = index;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(final String label) {
        this.label = label;
    }

    public boolean isGroup() {
        return group;
    }

    public void setGroup(final boolean group) {
        this.group = group;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(final String defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", FluxColumn.class.getSimpleName() + "[", "]")
                .add("index=" + index)
                .add("label='" + label + "'")
                .add("dataType='" + dataType + "'")
                .add("group=" + group)
                .add("defaultValue='" + defaultValue + "'")
                .toString();
    }
}
