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

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import javax.annotation.Nonnull;

import com.influxdb.Arguments;

/**
 * Flux duration literal -
 * <a href="http://bit.ly/flux-spec#duration-literals">spec</a>.
 * <p>
 * A duration literal is a representation of a length of time. It has an integer part and a duration unit part.
 *
 * @author Jakub Bednar (bednar@github) (28/06/2018 06:40)
 */
public class TimeInterval {

    private Long interval;
    private ChronoUnit chronoUnit;

    public TimeInterval(@Nonnull final Long interval, @Nonnull final ChronoUnit chronoUnit) {

        Arguments.checkNotNull(interval, "Interval is required");
        Arguments.checkNotNull(chronoUnit, "ChronoUnit is required");

        this.interval = interval;
        this.chronoUnit = chronoUnit;
    }

    @Override
    @SuppressWarnings("MagicNumber")
    public String toString() {

        String unit;
        Long calculatedInterval = interval;
        switch (chronoUnit) {
            case NANOS:
                unit = "ns";
                break;
            case MICROS:
                unit = "us";
                break;
            case MILLIS:
                unit = "ms";
                break;
            case SECONDS:
                unit = "s";
                break;
            case MINUTES:
                unit = "m";
                break;
            case HOURS:
                unit = "h";
                break;
            case HALF_DAYS:
                unit = "h";
                calculatedInterval = Duration.of(interval, ChronoUnit.HALF_DAYS).toHours();
                break;
            case DAYS:
                unit = "d";
                break;
            case WEEKS:
                unit = "w";
                break;
            case MONTHS:
                unit = "mo";
                break;
            case YEARS:
                unit = "y";
                break;
            case DECADES:
                unit = "y";
                calculatedInterval = Math.multiplyExact(interval, 10);
                break;
            case CENTURIES:
                unit = "y";
                calculatedInterval = Math.multiplyExact(interval, 100);
                break;
            case MILLENNIA:
                unit = "y";
                calculatedInterval = Math.multiplyExact(interval, 1000);
                break;
            case ERAS:
                unit = "y";
                calculatedInterval = Math.multiplyExact(interval, 1000_000_000);
                break;
            case FOREVER:
            default:
                String message = String.format("The ChronoUnit.%s is not supported.", chronoUnit);

                throw new IllegalArgumentException(message);
        }

        return String.valueOf(calculatedInterval) + unit;
    }
}
