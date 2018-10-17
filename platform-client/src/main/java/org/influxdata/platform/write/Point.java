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
package org.influxdata.platform.write;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import org.influxdata.platform.Arguments;

/**
 * Point defines the values that will be written to the database.
 * <a href="http://bit.ly/influxdata-point">See Go Implementation</a>.
 *
 * @author Jakub Bednar (bednar@github) (11/10/2018 11:40)
 */
@NotThreadSafe
public final class Point {

    private static final ChronoUnit DEFAULT_WRITE_PRECISION = ChronoUnit.NANOS;

    private static final int MAX_FRACTION_DIGITS = 340;
    private static final ThreadLocal<NumberFormat> NUMBER_FORMATTER =
            ThreadLocal.withInitial(() -> {
                NumberFormat numberFormat = NumberFormat.getInstance(Locale.ENGLISH);
                numberFormat.setMaximumFractionDigits(MAX_FRACTION_DIGITS);
                numberFormat.setGroupingUsed(false);
                numberFormat.setMinimumFractionDigits(1);
                return numberFormat;
            });


    private String name;
    private final Map<String, String> tags = new TreeMap<>();
    private final Map<String, Object> fields = new TreeMap<>();
    private Long time;
    private ChronoUnit precision = DEFAULT_WRITE_PRECISION;

    /**
     * uUpdates the measurement name for the point.
     *
     * @param name the measurement name
     * @return new instance of {@link Point}
     */
    @Nonnull
    public static Point name(@Nonnull final String name) {

        Arguments.checkNotNull(name, "name");

        Point point = new Point();
        point.name = name;

        return point;
    }

    /**
     * Adds or replaces a tag value for a point.
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
     * Add {@link Boolean} field.
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
     * Updates the timestamp for the point.
     *
     * @param time      the timestamp
     * @param precision the timestamp precision
     * @return this
     */
    @Nonnull
    public Point time(@Nullable final Instant time, @Nonnull final ChronoUnit precision) {

        Arguments.checkPrecision(precision);

        if (time == null) {
            return time((Long) null, precision);
        }

        Long longTime;

        Duration plus = Duration.ofNanos(time.getNano()).plus(time.getEpochSecond(), ChronoUnit.SECONDS);
        switch (precision) {

            case NANOS:
                longTime = TimeUnit.NANOSECONDS.convert(plus.toNanos(), TimeUnit.NANOSECONDS);
                break;
            case MICROS:
                longTime = TimeUnit.MICROSECONDS.convert(plus.toNanos(), TimeUnit.NANOSECONDS);
                break;
            case MILLIS:
                longTime = TimeUnit.MILLISECONDS.convert(plus.toNanos(), TimeUnit.NANOSECONDS);
                break;
            case SECONDS:
                longTime = TimeUnit.SECONDS.convert(plus.toNanos(), TimeUnit.NANOSECONDS);
                break;
            default:
                throw new IllegalStateException("Unsupported precision: " + precision);
        }

        return time(longTime, precision);
    }

    /**
     * Updates the timestamp for the point.
     *
     * @param time      the timestamp
     * @param precision the timestamp precision
     * @return this
     */
    @Nonnull
    public Point time(@Nullable final Long time, @Nonnull final ChronoUnit precision) {

        Arguments.checkPrecision(precision);

        this.time = time;
        this.precision = precision;

        return this;
    }

    /**
     * @return the data point precision
     */
    @Nonnull
    public ChronoUnit getPrecision() {
        return precision;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        escapeKey(sb, name);
        appendTags(sb);
        concatenatedFields(sb);
        formattedTime(sb);

        return sb.toString();

    }

    @Nonnull
    private Point putField(@Nonnull final String field, @Nullable final Object value) {

        Arguments.checkNonEmpty(field, "fieldName");

        fields.put(field, value);
        return this;
    }

    private void appendTags(@Nonnull final StringBuilder sb) {

        for (Map.Entry<String, String> tag : this.tags.entrySet()) {

            String key = tag.getKey();
            String value = tag.getValue();

            if (key.isEmpty() || value.isEmpty()) {
                continue;
            }

            sb.append(',');
            escapeKey(sb, key);
            sb.append('=');
            escapeKey(sb, value);
        }
        sb.append(' ');
    }

    private void concatenatedFields(@Nonnull final StringBuilder sb) {

        for (Map.Entry<String, Object> field : this.fields.entrySet()) {
            Object value = field.getValue();
            if (value == null) {
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
                escapeField(sb, stringValue);
                sb.append('"');
            } else {
                sb.append(value);
            }

            sb.append(',');
        }

        // efficiently chop off the trailing comma
        int lengthMinusOne = sb.length() - 1;
        if (sb.charAt(lengthMinusOne) == ',') {
            sb.setLength(lengthMinusOne);
        }
    }

    private void formattedTime(@Nonnull final StringBuilder sb) {

        if (this.time == null) {
            return;
        }

        sb.append(" ").append(this.time);
    }

    private void escapeKey(@Nonnull final StringBuilder sb, @Nonnull final String key) {
        for (int i = 0; i < key.length(); i++) {
            switch (key.charAt(i)) {
                case ' ':
                case ',':
                case '=':
                    sb.append('\\');
                default:
                    sb.append(key.charAt(i));
            }
        }
    }

    private void escapeField(@Nonnull final StringBuilder sb, @Nonnull final String field) {
        for (int i = 0; i < field.length(); i++) {
            switch (field.charAt(i)) {
                case '\\':
                case '\"':
                    sb.append('\\');
                default:
                    sb.append(field.charAt(i));
            }
        }
    }
}