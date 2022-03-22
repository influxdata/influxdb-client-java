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
package com.influxdb.client.benchmark;

public class BenchmarkOptions {
    public final String influxUrl;
    public final String org;
    public final String bucket;
    public final String measurementName;
    public final int threadsCount;
    public final int secondsCount;
    public final int lineProtocolsCount;
    public final int batchSize;
    public final int flushInterval;
    public final int bufferLimit;
    public final String token;

    public BenchmarkOptions(final String influxUrl,
                            final String token,
                            final String org,
                            final String bucket,
                            final String measurementName,
                            final int threadsCount,
                            final int secondsCount,
                            final int lineProtocolsCount,
                            final int batchSize,
                            final int bufferLimit,
                            final int flushInterval) {
        this.influxUrl = influxUrl;
        this.token = token;
        this.org = org;
        this.bucket = bucket;
        this.measurementName = measurementName;
        this.threadsCount = threadsCount;
        this.secondsCount = secondsCount;
        this.lineProtocolsCount = lineProtocolsCount;
        this.batchSize = batchSize;
        this.bufferLimit = bufferLimit;
        this.flushInterval = flushInterval;
    }
}
