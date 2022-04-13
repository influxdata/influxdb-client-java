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
package com.influxdb.client.write;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.time.Instant;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.internal.NanosecondConverter;
import com.influxdb.utils.Arguments;

import static com.influxdb.client.write.WriteParameters.DEFAULT_WRITE_PRECISION;

/**
 * Point defines the values that will be written to the database.
 * <a href="http://bit.ly/influxdata-point">See Go Implementation</a>.
 *
 * @author Jakub Bednar (bednar@github) (11/10/2018 11:40)
 */
@NotThreadSafe
public final class Point {

    private static final int MAX_FRACTION_DIGITS = 340;
    private static final ThreadLocal<NumberFormat> NUMBER_FORMATTER =
            ThreadLocal.withInitial(() -> {
                NumberFormat numberFormat = NumberFormat.getInstance(Locale.ENGLISH);
                numberFormat.setMaximumFractionDigits(MAX_FRACTION_DIGITS);
                numberFormat.setGroupingUsed(false);
                numberFormat.setMinimumFractionDigits(1);
                return numberFormat;
            });


    private final String name;
    private final Map<String, String> tags = new TreeMap<>();
    private final Map<String, Object> fields = new TreeMap<>();
    private Number time;
    private WritePrecision precision = DEFAULT_WRITE_PRECISION;

    /**
     * Create a new Point with specified a measurement name.
     *
     * @param measurementName the measurement name
     */
    public Point(@Nonnull final String measurementName) {

        Arguments.checkNotNull(measurementName, "measurement");

        this.name = measurementName;
    }

    /**
     * Create a new Point withe specified a measurement name.
     *
     * @param measurementName the measurement name
     * @return new instance of {@link Point}
     */
    @Nonnull
    public static Point measurement(@Nonnull final String measurementName) {

        Arguments.checkNotNull(measurementName, "measurement");

        return new Point(measurementName);
    }

    /**
     * Adds or replaces a tag value for this point.
     *
     * @param key   the tag name
     * @param value the tag value
     * @return this
     */
    @Nonnull
    public Point addTag(@Nonnull final String key, @Nullable final String value) {

        Arguments.checkNotNull(key, "tagName");

        tags.put(key, value);

        return this;
    }

    /**
     * Adds or replaces tags for this point.
     *
     * @param tagsToAdd the Map of tags to add
     * @return this
     */
    @Nonnull
    public Point addTags(@Nonnull final Map<String, String> tagsToAdd) {

        Arguments.checkNotNull(tagsToAdd, "tagsToAdd");

        tagsToAdd.forEach(this::addTag);

        return this;
    }

    /**
     * Add {@link Boolean} field.
     *
     * @param field the field name
     * @param value the field value
     * @return this
     */
    @Nonnull
    public Point addField(@Nonnull final String field, final boolean value) {
        return putField(field, value);
    }

    /**
     * Add {@link Long} field.
     *
     * @param field the field name
     * @param value the field value
     * @return this
     */
    public Point addField(@Nonnull final String field, final long value) {
        return putField(field, value);
    }

    /**
     * Add {@link Double} field.
     *
     * @param field the field name
     * @param value the field value
     * @return this
     */
    @Nonnull
    public Point addField(@Nonnull final String field, final double value) {
        return putField(field, value);
    }

    /**
     * Add {@link Number} field.
     *
     * @param field the field name
     * @param value the field value
     * @return this
     */
    @Nonnull
    public Point addField(@Nonnull final String field, @Nullable final Number value) {
        return putField(field, value);
    }

    /**
     * Add {@link String} field.
     *
     * @param field the field name
     * @param value the field value
     * @return this
     */
    @Nonnull
    public Point addField(@Nonnull final String field, @Nullable final String value) {
        return putField(field, value);
    }

    /**
     * Adds or replaces fields for this point.
     *
     * @param fieldsToAdd the Map of fields to add
     * @return this
     */
    @Nonnull
    public Point addFields(@Nonnull final Map<String, Object> fieldsToAdd) {

        Arguments.checkNotNull(fieldsToAdd, "fieldsToAdd");

        fieldsToAdd.forEach(this::putField);

        return this;
    }

    /**
     * Updates the timestamp for the point.
     *
     * @param time      the timestamp
     * @param precision the timestamp precision
     * @return this
     */
    @Nonnull
    public Point time(@Nullable final Instant time, @Nonnull final WritePrecision precision) {

        if (time == null) {
            return time((Long) null, precision);
        }

        BigInteger convertedTime = NanosecondConverter.convert(time, precision);

        return time(convertedTime, precision);
    }

    /**
     * Updates the timestamp for the point.
     *
     * @param time      the timestamp
     * @param precision the timestamp precision
     * @return this
     */
    @Nonnull
    public Point time(@Nullable final Number time, @Nonnull final WritePrecision precision) {

        Arguments.checkNotNull(precision, "precision");

        this.time = time;
        this.precision = precision;

        return this;
    }

    /**
     * Updates the timestamp for the point.
     *
     * @param time      the timestamp
     * @param precision the timestamp precision
     * @return this
     */
    @Nonnull
    public Point time(@Nullable final Long time, @Nonnull final WritePrecision precision) {

        return time((Number) time, precision);
    }

    /**
     * @return the data point precision
     */
    @Nonnull
    public WritePrecision getPrecision() {
        return precision;
    }

    /**
     * Has point any fields?
     *
     * @return true, if the point contains any fields, false otherwise.
     */
    public boolean hasFields() {
        return !fields.isEmpty();
    }

    /**
     * @return Line Protocol
     */
    @Nonnull
    public String toLineProtocol() {
        return toLineProtocol(null);
    }

    /**
     * @param pointSettings with the default values
     * @return Line Protocol
     */
    @Nonnull
    public String toLineProtocol(@Nullable final PointSettings pointSettings) {
        return toLineProtocol(pointSettings, precision);
    }

    /**
     * @param pointSettings with the default values
     * @param precision     required precision
     * @return Line Protocol
     */
    @Nonnull
    public String toLineProtocol(@Nullable final PointSettings pointSettings,
                                 @Nonnull final WritePrecision precision) {

        StringBuilder sb = new StringBuilder();

        escapeKey(sb, name, false);
        appendTags(sb, pointSettings);
        boolean appendedFields = appendFields(sb);
        if (!appendedFields) {
            return "";
        }
        appendTime(sb, precision);

        return sb.toString();
    }

    @Nonnull
    private Point putField(@Nonnull final String field, @Nullable final Object value) {

        Arguments.checkNonEmpty(field, "fieldName");

        fields.put(field, value);
        return this;
    }

    private void appendTags(@Nonnull final StringBuilder sb, @Nullable final PointSettings pointSettings) {


        Set<Map.Entry<String, String>> entries = this.tags.entrySet();
        if (pointSettings != null) {

            Map<String, String> defaultTags = pointSettings.getDefaultTags();
            if (!defaultTags.isEmpty()) {

                entries = Stream.of(this.tags, defaultTags)
                        .map(Map::entrySet)
                        .flatMap(Collection::stream)
                        .filter(entry -> entry.getValue() != null)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> {
                            if (v1.isEmpty()) {
                                return v2;
                            }

                            return v1;
                        }, TreeMap::new))
                        .entrySet();
            }
        }

        for (Map.Entry<String, String> tag : entries) {

            String key = tag.getKey();
            String value = tag.getValue();

            if (key.isEmpty() || value == null || value.isEmpty()) {
                continue;
            }

            sb.append(',');
            escapeKey(sb, key);
            sb.append('=');
            escapeKey(sb, value);
        }
        sb.append(' ');
    }

    private boolean appendFields(@Nonnull final StringBuilder sb) {

        boolean appended = false;
        for (Map.Entry<String, Object> field : this.fields.entrySet()) {
            Object value = field.getValue();
            if (isNotDefined(value)) {
                continue;
            }
            escapeKey(sb, field.getKey());
            sb.append('=');
            if (value instanceof Number) {
                if (value instanceof Double || value instanceof Float || value instanceof BigDecimal) {
                    sb.append(NUMBER_FORMATTER.get().format(value));
                } else {
                    sb.append(value).append('i');
                }
            } else if (value instanceof String) {
                String stringValue = (String) value;
                sb.append('"');
                escapeValue(sb, stringValue);
                sb.append('"');
            } else {
                sb.append(value);
            }

            sb.append(',');

            appended = true;
        }

        // efficiently chop off the trailing comma
        int lengthMinusOne = sb.length() - 1;
        if (sb.charAt(lengthMinusOne) == ',') {
            sb.setLength(lengthMinusOne);
        }

        return appended;
    }

    private void appendTime(@Nonnull final StringBuilder sb, final WritePrecision precision) {

        if (this.time == null) {
            return;
        }

        sb.append(" ");

        if (this.precision == precision) {
            if (this.time instanceof BigDecimal) {
                sb.append(((BigDecimal) this.time).toBigInteger());
            } else if (this.time instanceof BigInteger) {
                sb.append(this.time);
            } else {
                sb.append(this.time.longValue());
            }
        } else {
            long time;
            if (this.time instanceof BigDecimal) {
                time = ((BigDecimal) this.time).longValueExact();
            } else if (this.time instanceof BigInteger) {
                time = ((BigInteger) this.time).longValueExact();
            } else {
                time = this.time.longValue();
            }
            sb.append(toTimeUnit(precision).convert(time, toTimeUnit(this.precision)));
        }
    }

    private void escapeKey(@Nonnull final StringBuilder sb, @Nonnull final String key) {
        escapeKey(sb, key, true);
    }

    private void escapeKey(@Nonnull final StringBuilder sb, @Nonnull final String key, final boolean escapeEqual) {
        for (int i = 0; i < key.length(); i++) {
            switch (key.charAt(i)) {
                case '\n':
                    sb.append("\\n");
                    continue;
                case '\r':
                    sb.append("\\r");
                    continue;
                case '\t':
                    sb.append("\\t");
                    continue;
                case ' ':
                case ',':
                    sb.append('\\');
                    break;
                case '=':
                    if (escapeEqual) {
                        sb.append('\\');
                    }
                    break;
                default:
            }

            sb.append(key.charAt(i));
        }
    }

    private void escapeValue(@Nonnull final StringBuilder sb, @Nonnull final String value) {
        for (int i = 0; i < value.length(); i++) {
            switch (value.charAt(i)) {
                case '\\':
                case '\"':
                    sb.append('\\');
                default:
                    sb.append(value.charAt(i));
            }
        }
    }

    private boolean isNotDefined(final Object value) {
        return value == null
                || (value instanceof Double && !Double.isFinite((Double) value))
                || (value instanceof Float && !Float.isFinite((Float) value));
    }

    @Nonnull
    private TimeUnit toTimeUnit(@Nonnull final WritePrecision precision) {
        switch (precision) {
            case MS:
                return TimeUnit.MILLISECONDS;
            case S:
                return TimeUnit.SECONDS;
            case US:
                return TimeUnit.MICROSECONDS;
            case NS:
                return TimeUnit.NANOSECONDS;
            default:
                throw new IllegalStateException("Unexpected value: " + precision);
        }
    }
}