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
package org.influxdata.query;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.influxdata.Arguments;


/**
 * A record is a tuple of values. Each record in the table represents a single point in the series.
 *
 * <a href="http://bit.ly/flux-spec#record">Specification</a>.
 */
public final class FluxRecord {

    /**
     * The Index of the table that the record belongs.
     */
    private final Integer table;

    /**
     * The record's values.
     */
    private LinkedHashMap<String, Object> values = new LinkedHashMap<>();

    public FluxRecord(@Nonnull final Integer table) {

        Arguments.checkNotNull(table, "Table index");

        this.table = table;
    }

    /**
     * @return the inclusive lower time bound of all records
     */
    @Nullable
    public Instant getStart() {
        return (Instant) getValueByKey("_start");
    }

    /**
     * @return the exclusive upper time bound of all records
     */
    @Nullable
    public Instant getStop() {
        return (Instant) getValueByKey("_stop");
    }

    /**
     * @return the time of the record
     */
    @Nullable
    public Instant getTime() {
        return (Instant) getValueByKey("_time");
    }

    /**
     * @return the value of the record
     */
    @Nullable
    public Object getValue() {
        return getValueByKey("_value");
    }

    /**
     * @return get value with key <i>_field</i>
     */
    @Nullable
    public String getField() {
        return (String) getValueByKey("_field");
    }

    /**
     * @return get value with key <i>_measurement</i>
     */
    @Nullable
    public String getMeasurement() {
        return (String) getValueByKey("_measurement");
    }

    /**
     * @return the index of table which contains the record
     */
    @Nonnull
    public Integer getTable() {
        return table;
    }

    /**
     * @return tuple of values
     */
    @Nonnull
    public Map<String, Object> getValues() {
        return values;
    }

    /**
     * Get FluxRecord value by index.
     *
     * @param index of value in CSV response
     * @return value
     * @see ArrayIndexOutOfBoundsException
     */
    @Nullable
    public Object getValueByIndex(final int index) {

        //noinspection unchecked
        return values.values().toArray()[index];
    }

    /**
     * Get FluxRecord value by key.
     *
     * @param key of value in CSV response
     * @return value
     */
    @Nullable
    public Object getValueByKey(@Nonnull final String key) {

        Arguments.checkNonEmpty(key, "key");

        return values.get(key);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", FluxRecord.class.getSimpleName() + "[", "]")
                .add("table=" + table)
                .add("values=" + values.size())
                .toString();
    }
}
