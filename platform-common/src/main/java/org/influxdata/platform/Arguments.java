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
package org.influxdata.platform;

import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.Objects;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

/**
 * Functions for parameter validation.
 * <p>
 * <a href="https://github.com/influxdata/influxdb-java/">Thanks</a>
 */
public final class Arguments {

    private static final Pattern DURATION_PATTERN = Pattern.compile("([-+]?)([0-9]+(\\.[0-9]*)?[a-z]+)+",
            Pattern.CASE_INSENSITIVE);

    private static final String DURATION_MESSAGE = "Expecting a duration string for %s. But got: %s";

    /**
     * The precisions that are allowed to use in the write.
     */
    private static final EnumSet<ChronoUnit> ALLOWED_PRECISION = EnumSet.of(ChronoUnit.NANOS,
            ChronoUnit.MICROS, ChronoUnit.MILLIS, ChronoUnit.SECONDS);

    private Arguments() {
    }

    /**
     * Enforces that the string is {@linkplain String#isEmpty() not empty}.
     *
     * @param string the string to test
     * @param name   variable name for reporting
     * @return {@code string}
     * @throws IllegalArgumentException if the string is empty
     */
    public static String checkNonEmpty(final String string, final String name) throws IllegalArgumentException {
        if (string == null || string.isEmpty()) {
            throw new IllegalArgumentException("Expecting a non-empty string for " + name);
        }
        return string;
    }

    /**
     * Enforces that the string has exactly one char.
     *
     * @param string the string to test
     * @param name   variable name for reporting
     * @return {@code string}
     * @throws IllegalArgumentException if the string has not one char
     */
    public static String checkOneCharString(final String string, final String name) throws IllegalArgumentException {
        if (string == null || string.length() != 1) {
            throw new IllegalArgumentException("Expecting a one char string for " + name);
        }
        return string;
    }

    /**
     * Enforces that the string is duration literal.
     *
     * @param string the string to test
     * @param name   variable name for reporting
     * @return {@code string}
     * @throws IllegalArgumentException if the string is not duration literal
     */
    public static String checkDuration(final String string, final String name) throws IllegalArgumentException {
        if (string == null || string.isEmpty() || !DURATION_PATTERN.matcher(string).matches()) {
            throw new IllegalArgumentException(String.format(DURATION_MESSAGE, name, string));
        }

        return string;
    }

    /**
     * Enforces that the string is duration literal. Empty or null strings are valid.
     *
     * @param string the string to test
     * @param name   variable name for reporting
     * @return {@code string}
     * @throws IllegalArgumentException if the string is not duration literal
     */
    public static String checkDurationNotRequired(final String string, final String name)
            throws IllegalArgumentException {
        if (string != null && !string.isEmpty() && !DURATION_PATTERN.matcher(string).matches()) {
            throw new IllegalArgumentException(String.format(DURATION_MESSAGE, name, string));
        }

        return string;
    }

    /**
     * Enforces that the number is larger than 0.
     *
     * @param number the number to test
     * @param name   variable name for reporting
     * @throws IllegalArgumentException if the number is less or equal to 0
     */
    public static void checkPositiveNumber(final Number number, final String name) throws IllegalArgumentException {
        if (number == null || number.doubleValue() <= 0) {
            throw new IllegalArgumentException("Expecting a positive number for " + name);
        }
    }

    /**
     * Enforces that the number is not negative.
     *
     * @param number the number to test
     * @param name   variable name for reporting
     * @throws IllegalArgumentException if the number is less or equal to 0
     */
    public static void checkNotNegativeNumber(final Number number, final String name) throws IllegalArgumentException {
        if (number == null || number.doubleValue() < 0) {
            throw new IllegalArgumentException("Expecting a positive or zero number for " + name);
        }
    }

    /**
     * Checks that the specified object reference is not {@code null}.
     *
     * @param obj  the object reference to check for nullity
     * @param name variable name for reporting
     * @throws NullPointerException if the object is {@code null}
     * @see Objects#requireNonNull(Object, String)
     */
    public static void checkNotNull(final Object obj, final String name) throws NullPointerException {

        Objects.requireNonNull(obj, () -> "Expecting a not null reference for " + name);
    }

    /**
     * Checks that the precision reference to one of {@link Arguments#ALLOWED_PRECISION}.
     *
     * @throws IllegalArgumentException if the object is not one of {@link Arguments#ALLOWED_PRECISION}
     */
    public static void checkPrecision(@Nullable final ChronoUnit precision) throws IllegalArgumentException {

        if (!ALLOWED_PRECISION.contains(precision)) {
            throw new IllegalArgumentException("Precision must be one of: " + ALLOWED_PRECISION);
        }
    }
}
