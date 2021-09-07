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
package com.influxdb;

import java.time.temporal.ChronoUnit;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (01/08/2018 15:29)
 */
@SuppressWarnings("ConstantConditions")
@RunWith(JUnitPlatform.class)
class ArgumentsTest {

    @Test
    void checkNonEmptyString() {

        Arguments.checkNonEmpty("valid", "property");
    }

    @Test
    void checkNonEmptyStringEmpty() {

        Assertions.assertThatThrownBy(() -> Arguments.checkNonEmpty("", "property"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Expecting a non-empty string for property");
    }

    @Test
    void checkNonEmptyStringNull() {

        Assertions.assertThatThrownBy(() -> Arguments.checkNonEmpty(null, "property"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Expecting a non-empty string for property");
    }

    @Test
    void checkPositiveNumber() {

        Arguments.checkPositiveNumber(10, "property");
    }

    @Test
    void checkPositiveNumberNull() {

        Assertions.assertThatThrownBy(() -> Arguments.checkPositiveNumber(null, "property"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Expecting a positive number for property");
    }

    @Test
    void checkPositiveNumberZero() {

        Assertions.assertThatThrownBy(() -> Arguments.checkPositiveNumber(0, "property"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Expecting a positive number for property");
    }

    @Test
    void checkPositiveNumberZeroNegative() {

        Assertions.assertThatThrownBy(() -> Arguments.checkPositiveNumber(-12L, "property"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Expecting a positive number for property");
    }

    @Test
    void checkNotNegativeNumber() {

        Arguments.checkNotNegativeNumber(0, "valid");
    }

    @Test
    void checkNotNegativeNumberNull() {

        Assertions.assertThatThrownBy(() -> Arguments.checkNotNegativeNumber(null, "property"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Expecting a positive or zero number for property");
    }

    @Test
    void checkNotNegativeNumberNegative() {

        Assertions.assertThatThrownBy(() -> Arguments.checkNotNegativeNumber(-12L, "property"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Expecting a positive or zero number for property");
    }

    @Test
    void checkOneCharString() {

        Arguments.checkOneCharString("#", "valid");
    }

    @Test
    void checkOneCharStringEmpty() {

        Assertions.assertThatThrownBy(() -> Arguments.checkOneCharString("", "property"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Expecting a one char string for property");
    }

    @Test
    void checkOneCharStringNull() {

        Assertions.assertThatThrownBy(() -> Arguments.checkOneCharString(null, "property"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Expecting a one char string for property");
    }

    @Test
    void checkOneCharStringLarge() {

        Assertions.assertThatThrownBy(() -> Arguments.checkOneCharString("##", "property"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Expecting a one char string for property");
    }

    @Test
    void checkNotNull() {
        Assertions.assertThatThrownBy(() -> Arguments.checkNotNull(null, "property"))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Expecting a not null reference for property");
    }

    @Test
    void checkPrecision() {

        Arguments.checkPrecision(ChronoUnit.SECONDS);
    }

    @Test
    void checkPrecisionNotSupported() {

        Assertions.assertThatThrownBy(() -> Arguments.checkPrecision(ChronoUnit.DAYS))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Precision must be one of: [Nanos, Micros, Millis, Seconds]");
    }
}