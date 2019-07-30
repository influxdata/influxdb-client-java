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
package com.influxdb.query.dsl.functions.properties;

import java.time.temporal.ChronoUnit;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (09/10/2018 12:24)
 */
@RunWith(JUnitPlatform.class)
class TimeIntervalTest {

    @Test
    void nanos() {

        TimeInterval interval = new TimeInterval(1L, ChronoUnit.NANOS);

        Assertions.assertThat(interval.toString()).isEqualTo("1ns");
    }

    @Test
    void micros() {

        TimeInterval interval = new TimeInterval(2L, ChronoUnit.MICROS);

        Assertions.assertThat(interval.toString()).isEqualTo("2us");
    }

    @Test
    void millis() {

        TimeInterval interval = new TimeInterval(3L, ChronoUnit.MILLIS);

        Assertions.assertThat(interval.toString()).isEqualTo("3ms");
    }

    @Test
    void seconds() {

        TimeInterval interval = new TimeInterval(4L, ChronoUnit.SECONDS);

        Assertions.assertThat(interval.toString()).isEqualTo("4s");
    }

    @Test
    void minutes() {

        TimeInterval interval = new TimeInterval(5L, ChronoUnit.MINUTES);

        Assertions.assertThat(interval.toString()).isEqualTo("5m");
    }

    @Test
    void hours() {

        TimeInterval interval = new TimeInterval(6L, ChronoUnit.HOURS);

        Assertions.assertThat(interval.toString()).isEqualTo("6h");
    }

    @Test
    void halfDays() {

        TimeInterval interval = new TimeInterval(7L, ChronoUnit.HALF_DAYS);

        Assertions.assertThat(interval.toString()).isEqualTo("84h");
    }

    @Test
    void days() {

        TimeInterval interval = new TimeInterval(8L, ChronoUnit.DAYS);

        Assertions.assertThat(interval.toString()).isEqualTo("8d");
    }

    @Test
    void weeks() {

        TimeInterval interval = new TimeInterval(9L, ChronoUnit.WEEKS);

        Assertions.assertThat(interval.toString()).isEqualTo("9w");
    }

    @Test
    void months() {

        TimeInterval interval = new TimeInterval(10L, ChronoUnit.MONTHS);

        Assertions.assertThat(interval.toString()).isEqualTo("10mo");
    }

    @Test
    void years() {

        TimeInterval interval = new TimeInterval(11L, ChronoUnit.YEARS);

        Assertions.assertThat(interval.toString()).isEqualTo("11y");
    }

    @Test
    void decades() {

        TimeInterval interval = new TimeInterval(12L, ChronoUnit.DECADES);

        Assertions.assertThat(interval.toString()).isEqualTo("120y");
    }

    @Test
    void centuries() {

        TimeInterval interval = new TimeInterval(13L, ChronoUnit.CENTURIES);

        Assertions.assertThat(interval.toString()).isEqualTo("1300y");
    }

    @Test
    void millennia() {

        TimeInterval interval = new TimeInterval(14L, ChronoUnit.MILLENNIA);

        Assertions.assertThat(interval.toString()).isEqualTo("14000y");
    }

    @Test
    void eras() {

        TimeInterval interval = new TimeInterval(15L, ChronoUnit.ERAS);

        Assertions.assertThat(interval.toString()).isEqualTo("15000000000y");
    }

    @Test
    void forever() {

        //noinspection ResultOfMethodCallIgnored
        Assertions.assertThatThrownBy(() -> new TimeInterval(1L, ChronoUnit.FOREVER).toString())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The ChronoUnit.Forever is not supported.");
    }
}