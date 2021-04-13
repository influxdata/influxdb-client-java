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
package com.influxdb.client.write.internal;

import java.math.BigInteger;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.influxdb.client.domain.WritePrecision;

import static java.util.function.Function.identity;

/**
 * Nanosecond converter.
 *
 * Utility class converting epoch nanoseconds to epoch with a given precision.
 */
public final class NanosecondConverter {

    private static final BigInteger NANOS_PER_SECOND = BigInteger.valueOf(1000_000_000L);
    private static final BigInteger MICRO_PER_NANOS = BigInteger.valueOf(1000L);
    private static final BigInteger MILLIS_PER_NANOS = BigInteger.valueOf(1000000L);
    private static final BigInteger SECONDS_PER_NANOS = BigInteger.valueOf(1000000000L);

    private NanosecondConverter() {
    }

    /**
     * Timestamp calculation functions to add timestamp to records.
     */
    private static final Map<WritePrecision, Function<BigInteger, BigInteger>> TIMESTAMP_CALCULATIONS = new HashMap<>();

    static {
        TIMESTAMP_CALCULATIONS.put(WritePrecision.S, (timestamp) -> timestamp.divide(SECONDS_PER_NANOS));
        TIMESTAMP_CALCULATIONS.put(WritePrecision.MS, (timestamp) -> timestamp.divide(MILLIS_PER_NANOS));
        TIMESTAMP_CALCULATIONS.put(WritePrecision.US, (timestamp) -> timestamp.divide(MICRO_PER_NANOS));
        TIMESTAMP_CALCULATIONS.put(WritePrecision.NS, identity());
    }

    /**
     * Convert epoch timestamp (in millis) to a given precision.
     *
     * @param millis    epoch timestamp in millis
     * @param precision precision
     * @return epoch timestamp in precision
     */
    public static BigInteger convert(final long millis, final WritePrecision precision) {
        return TIMESTAMP_CALCULATIONS.get(precision).apply(BigInteger.valueOf(millis).multiply(MILLIS_PER_NANOS));
    }

    /**
     * Convert {@link Instant} timestamp to a given precision.
     *
     * @param instant   Instant timestamp
     * @param precision precision
     * @return epoch timestamp in precision
     */
    public static BigInteger convert(final Instant instant, final WritePrecision precision) {
        BigInteger nanos = BigInteger.valueOf(instant.getEpochSecond())
                .multiply(NANOS_PER_SECOND)
                .add(BigInteger.valueOf(instant.getNano()));

        return TIMESTAMP_CALCULATIONS.get(precision).apply(nanos);
    }

    /**
     * Get current timestamp in a given precision.
     *
     * @param precision precision
     * @return epoch timestamp in precision
     */
    public static BigInteger currentTimestamp(final WritePrecision precision) {
        Instant now = Instant.now();
        return convert(now, precision);
    }
}