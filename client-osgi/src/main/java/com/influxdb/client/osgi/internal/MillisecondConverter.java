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
package com.influxdb.client.osgi.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.influxdb.client.domain.WritePrecision;

import static java.util.function.Function.identity;

/**
 * Millisecond converter.
 *
 * Utility class converting epoch milliseconds to epoch with a given precision.
 */
public final class MillisecondConverter {

    private static final long THOUSAND = 1000L;
    private static final long MILLION = 1000000L;

    private MillisecondConverter() {
    }

    /**
     * Timestamp calculation functions to add timestamp to records.
     */
    private static final Map<WritePrecision, Function<Long, Long>> TIMESTAMP_CALCULATIONS = new HashMap<>();

    static {
        TIMESTAMP_CALCULATIONS.put(WritePrecision.S, (timestamp) -> timestamp / THOUSAND);
        TIMESTAMP_CALCULATIONS.put(WritePrecision.MS, identity());
        TIMESTAMP_CALCULATIONS.put(WritePrecision.US, (timestamp) -> timestamp * THOUSAND);
        TIMESTAMP_CALCULATIONS.put(WritePrecision.NS, (timestamp) -> timestamp * MILLION);
    }

    /**
     * Convert epoch timestamp (in millis) to a given precision.
     *
     * @param millis    epoch timestamp in millis
     * @param precision precision
     * @return epoch timestamp in precision
     */
    public static long convert(final long millis, final WritePrecision precision) {
        return TIMESTAMP_CALCULATIONS.get(precision).apply(millis);
    }
}
