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
package com.influxdb.client;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;

import com.influxdb.utils.Arguments;

import io.reactivex.rxjava3.core.BackpressureOverflowStrategy;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.schedulers.Schedulers;


/**
 * WriteOptions are used to configure writes the data point into InfluxDB 2.0.
 *
 * <p>
 * The default setting use the batching configured to (consistent with Telegraf):
 * <ul>
 * <li>batchSize = 1000</li>
 * <li>flushInterval = 1000 ms</li>
 * <li>retryInterval = 5000 ms</li>
 * <li>jitterInterval = 0</li>
 * <li>bufferLimit = 10_000</li>
 * </ul>
 * <p>
 * The default backpressure strategy is {@link BackpressureOverflowStrategy#DROP_OLDEST}.
 * <p>
 *
 * @author Jakub Bednar (bednar@github) (21/09/2018 10:11)
 */
@ThreadSafe
public final class WriteOptions implements WriteApi.RetryOptions {

    public static final int DEFAULT_BATCH_SIZE = 1000;
    public static final int DEFAULT_FLUSH_INTERVAL = 1000;
    public static final int DEFAULT_JITTER_INTERVAL = 0;
    public static final int DEFAULT_RETRY_INTERVAL = 5000;
    public static final int DEFAULT_MAX_RETRIES = 5;
    public static final int DEFAULT_MAX_RETRY_DELAY = 125_000;
    public static final int DEFAULT_MAX_RETRY_TIME = 180_000;
    public static final int DEFAULT_EXPONENTIAL_BASE = 2;
    public static final int DEFAULT_BUFFER_LIMIT = 10000;

    /**
     * Default configuration with values that are consistent with Telegraf.
     */
    public static final WriteOptions DEFAULTS = WriteOptions.builder().build();

    private final int batchSize;
    private final int flushInterval;
    private final int jitterInterval;
    private final int retryInterval;
    private final int maxRetries;
    private final int maxRetryDelay;
    private final int maxRetryTime;
    private final int exponentialBase;
    private final int bufferLimit;
    private final Scheduler writeScheduler;
    private final BackpressureOverflowStrategy backpressureStrategy;

    /**
     * @return the number of data point to collect in batch
     * @see WriteOptions.Builder#batchSize(int)
     */
    public int getBatchSize() {
        return batchSize;
    }

    /**
     * @return the time to wait at most (milliseconds)
     * @see WriteOptions.Builder#flushInterval(int) (int)
     */
    public int getFlushInterval() {
        return flushInterval;
    }

    /**
     * @return batch flush jitter interval value (milliseconds)
     * @see WriteOptions.Builder#jitterInterval(int)
     */
    @Override
    public int getJitterInterval() {
        return jitterInterval;
    }

    /**
     * The retry interval is used when the InfluxDB server does not specify "Retry-After" header.
     * <br>
     * Retry-After: A non-negative decimal integer indicating the seconds to delay after the response is received.
     *
     * @return the time to wait before retry unsuccessful write (milliseconds)
     * @see WriteOptions.Builder#retryInterval(int)
     */
    @Override
    public int getRetryInterval() {
        return retryInterval;
    }

    /**
     * The number of max retries when write fails.
     *
     * @return number of max retries
     * @see WriteOptions.Builder#maxRetries(int)
     */
    @Override
    public int getMaxRetries() {
        return maxRetries;
    }

    /**
     * The maximum delay between each retry attempt in milliseconds.
     *
     * @return maximum delay
     * @see WriteOptions.Builder#maxRetryDelay(int)
     */
    @Override
    public int getMaxRetryDelay() {
        return maxRetryDelay;
    }

    /**
     * The maximum total retry timeout in milliseconds.
     *
     * @return maximum delay
     * @see WriteOptions.Builder#maxRetryTime(int)
     */
    public int getMaxRetryTime() {
        return maxRetryTime;
    }

    /**
     * The base for the exponential retry delay.
     *
     * The next delay is computed as: retryInterval * exponentialBase^(attempts-1) + random(jitterInterval)
     *
     * @return exponential base
     * @see WriteOptions.Builder#exponentialBase(int)
     */
    @Override
    public int getExponentialBase() {
        return exponentialBase;
    }

    /**
     * @return Maximum number of points stored in the retry buffer.
     * @see WriteOptions.Builder#bufferLimit(int)
     */
    public int getBufferLimit() {
        return bufferLimit;
    }

    /**
     * @return The scheduler which is used for write data points.
     * @see WriteOptions.Builder#writeScheduler(Scheduler)
     */
    @Nonnull
    public Scheduler getWriteScheduler() {
        return writeScheduler;
    }

    /**
     * @return the strategy to deal with buffer overflow when using onBackpressureBuffer
     * @see WriteOptions.Builder#backpressureStrategy(BackpressureOverflowStrategy)
     */
    @Nonnull
    public BackpressureOverflowStrategy getBackpressureStrategy() {
        return backpressureStrategy;
    }

    private WriteOptions(@Nonnull final Builder builder) {

        Arguments.checkNotNull(builder, "WriteOptions.Builder");

        batchSize = builder.batchSize;
        flushInterval = builder.flushInterval;
        jitterInterval = builder.jitterInterval;
        retryInterval = builder.retryInterval;
        maxRetries = builder.maxRetries;
        maxRetryDelay = builder.maxRetryDelay;
        maxRetryTime = builder.maxRetryTime;
        exponentialBase = builder.exponentialBase;
        bufferLimit = builder.bufferLimit;
        writeScheduler = builder.writeScheduler;
        backpressureStrategy = builder.backpressureStrategy;
    }

    /**
     * Creates a builder instance.
     *
     * @return a builder
     */
    @Nonnull
    public static WriteOptions.Builder builder() {
        return new WriteOptions.Builder();
    }

    /**
     * A builder for {@code WriteOptions}.
     */
    @NotThreadSafe
    public static class Builder {

        private int batchSize = DEFAULT_BATCH_SIZE;
        private int flushInterval = DEFAULT_FLUSH_INTERVAL;
        private int jitterInterval = DEFAULT_JITTER_INTERVAL;
        private int retryInterval = DEFAULT_RETRY_INTERVAL;
        private int maxRetries = DEFAULT_MAX_RETRIES;
        private int maxRetryDelay = DEFAULT_MAX_RETRY_DELAY;
        private int maxRetryTime = DEFAULT_MAX_RETRY_TIME;
        private int exponentialBase = DEFAULT_EXPONENTIAL_BASE;
        private int bufferLimit = DEFAULT_BUFFER_LIMIT;
        private Scheduler writeScheduler = Schedulers.newThread();
        private BackpressureOverflowStrategy backpressureStrategy = BackpressureOverflowStrategy.DROP_OLDEST;

        /**
         * Set the number of data point to collect in batch.
         *
         * @param batchSize the number of data point to collect in batch
         * @return {@code this}
         */
        @Nonnull
        public Builder batchSize(final int batchSize) {
            Arguments.checkPositiveNumber(batchSize, "batchSize");
            this.batchSize = batchSize;
            return this;
        }

        /**
         * Set the time to wait at most (milliseconds).
         *
         * @param flushInterval the time to wait at most (milliseconds).
         * @return {@code this}
         */
        @Nonnull
        public Builder flushInterval(final int flushInterval) {
            Arguments.checkPositiveNumber(flushInterval, "flushInterval");
            this.flushInterval = flushInterval;
            return this;
        }

        /**
         * Jitters the batch flush interval by a random amount. This is primarily to avoid
         * large write spikes for users running a large number of client instances.
         * ie, a jitter of 5s and flush duration 10s means flushes will happen every 10-15s.
         *
         * @param jitterInterval (milliseconds)
         * @return {@code this}
         */
        @Nonnull
        public Builder jitterInterval(final int jitterInterval) {
            Arguments.checkNotNegativeNumber(jitterInterval, "jitterInterval");
            this.jitterInterval = jitterInterval;
            return this;
        }

        /**
         * Set the the time to wait before retry unsuccessful write (milliseconds).
         * <br><br>
         * The retry interval is used when the InfluxDB server does not specify "Retry-After" header.
         * <br>
         * Retry-After: A non-negative decimal integer indicating the seconds to delay after the response is received.
         *
         * @param retryInterval the time to wait before retry unsuccessful write
         * @return {@code this}
         */
        @Nonnull
        public Builder retryInterval(final int retryInterval) {
            Arguments.checkPositiveNumber(retryInterval, "retryInterval");
            this.retryInterval = retryInterval;
            return this;
        }

        /**
         * The number of max retries when write fails.
         *
         * @param maxRetries number of max retries
         * @return {@code this}
         */
        @Nonnull
        public Builder maxRetries(final int maxRetries) {
            Arguments.checkPositiveNumber(maxRetries, "maxRetries");
            this.maxRetries = maxRetries;
            return this;
        }

        /**
         * The maximum delay between each retry attempt in milliseconds.
         *
         * @param maxRetryDelay  maximum delay
         * @return {@code this}
         */
        @Nonnull
        public Builder maxRetryDelay(final int maxRetryDelay) {
            Arguments.checkPositiveNumber(maxRetryDelay, "maxRetryDelay");
            this.maxRetryDelay = maxRetryDelay;
            return this;
        }

        /**
         * The maximum total retry timeout in milliseconds.
         *
         * @param maxRetryTime  maximum timout
         * @return {@code this}
         */
        @Nonnull
        public Builder maxRetryTime(final int maxRetryTime) {
            Arguments.checkPositiveNumber(maxRetryTime, "maxRetryTime");
            this.maxRetryTime = maxRetryTime;
            return this;
        }

        /**
         * The base for the exponential retry delay.
         *
         * @param exponentialBase exponential base
         * @return {@code this}
         */
        @Nonnull
        public Builder exponentialBase(final int exponentialBase) {
            Arguments.checkPositiveNumber(exponentialBase, "exponentialBase");
            this.exponentialBase = exponentialBase;
            return this;
        }

        /**
         * The client maintains a buffer for failed writes so that the writes will be retried later on. This may
         * help to overcome temporary network problems or InfluxDB load spikes.
         * When the buffer is full and new points are written, oldest entries in the buffer are lost.
         *
         * @param bufferLimit maximum number of points stored in the retry buffer
         * @return {@code this}
         */
        @Nonnull
        public Builder bufferLimit(final int bufferLimit) {
            Arguments.checkNotNegativeNumber(bufferLimit, "bufferLimit");
            this.bufferLimit = bufferLimit;
            return this;
        }

        /**
         * Set the scheduler which is used for write data points. It is useful for disabling batch writes or
         * for tuning the performance. Default value is {@link Schedulers#newThread()}.
         *
         * @param writeScheduler the scheduler which is used for write data points.
         * @return {@code this}
         */
        @Nonnull
        public Builder writeScheduler(@Nonnull final Scheduler writeScheduler) {

            Arguments.checkNotNull(writeScheduler, "Write scheduler");

            this.writeScheduler = writeScheduler;
            return this;
        }

        /**
         * Set the strategy to deal with buffer overflow when using onBackpressureBuffer.
         *
         * @param backpressureStrategy the strategy to deal with buffer overflow when using onBackpressureBuffer.
         *                             Default {@link BackpressureOverflowStrategy#DROP_OLDEST};
         * @return {@code this}
         */
        @Nonnull
        public Builder backpressureStrategy(@Nonnull final BackpressureOverflowStrategy backpressureStrategy) {
            Arguments.checkNotNull(backpressureStrategy, "Backpressure Overflow Strategy");
            this.backpressureStrategy = backpressureStrategy;
            return this;
        }

        /**
         * Build an instance of WriteOptions.
         *
         * @return {@code WriteOptions}
         */
        @Nonnull
        public WriteOptions build() {

            return new WriteOptions(this);
        }
    }
}