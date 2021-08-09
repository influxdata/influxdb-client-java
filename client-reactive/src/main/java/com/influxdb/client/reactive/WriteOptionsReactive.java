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
package com.influxdb.client.reactive;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;

import com.influxdb.Arguments;
import com.influxdb.client.WriteApi;

import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;

import static com.influxdb.client.WriteOptions.DEFAULT_BATCH_SIZE;
import static com.influxdb.client.WriteOptions.DEFAULT_EXPONENTIAL_BASE;
import static com.influxdb.client.WriteOptions.DEFAULT_FLUSH_INTERVAL;
import static com.influxdb.client.WriteOptions.DEFAULT_JITTER_INTERVAL;
import static com.influxdb.client.WriteOptions.DEFAULT_MAX_RETRIES;
import static com.influxdb.client.WriteOptions.DEFAULT_MAX_RETRY_DELAY;
import static com.influxdb.client.WriteOptions.DEFAULT_MAX_RETRY_TIME;
import static com.influxdb.client.WriteOptions.DEFAULT_RETRY_INTERVAL;

/**
 * The configuration for {@link WriteReactiveApi}.
 *
 * <p>
 * <b>Example:</b>
 * <pre>
 *     WriteOptionsReactive writeOptions = WriteOptionsReactive.builder()
 *                 .batchSize(10_000)
 *                 .flushInterval(500)
 *                 .jitterInterval(1_000)
 *                 .retryInterval(2_000)
 *                 .maxRetries(5)
 *                 .maxRetryDelay(250_123)
 *                 .maxRetryTime(500_000)
 *                 .exponentialBase(2)
 *                 .computationScheduler(Schedulers.newThread())
 *                 .build();
 * </pre>
 * </p>
 *
 * @author Jakub Bednar (05/08/2021 9:13)
 */
@ThreadSafe
public final class WriteOptionsReactive implements WriteApi.RetryOptions {

    /**
     * Default configuration with values that are consistent with Telegraf.
     */
    public static final WriteOptionsReactive DEFAULTS = WriteOptionsReactive.builder().build();

    private final int batchSize;
    private final int flushInterval;
    private final int jitterInterval;
    private final int retryInterval;
    private final int maxRetries;
    private final int maxRetryDelay;
    private final int maxRetryTime;
    private final int exponentialBase;
    private final Scheduler computationScheduler;

    /**
     * @return the number of data point to collect in batch
     * @see WriteOptionsReactive.Builder#batchSize(int)
     */
    public int getBatchSize() {
        return batchSize;
    }

    /**
     * @return the time to wait at most (milliseconds)
     * @see WriteOptionsReactive.Builder#flushInterval(int) (int)
     */
    public int getFlushInterval() {
        return flushInterval;
    }

    /**
     * @return batch flush jitter interval value (milliseconds)
     * @see WriteOptionsReactive.Builder#jitterInterval(int)
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
     * @see WriteOptionsReactive.Builder#retryInterval(int)
     */
    @Override
    public int getRetryInterval() {
        return retryInterval;
    }

    /**
     * The number of max retries when write fails.
     *
     * @return number of max retries
     * @see WriteOptionsReactive.Builder#maxRetries(int)
     */
    @Override
    public int getMaxRetries() {
        return maxRetries;
    }

    /**
     * The maximum delay between each retry attempt in milliseconds.
     *
     * @return maximum delay
     * @see WriteOptionsReactive.Builder#maxRetryDelay(int)
     */
    @Override
    public int getMaxRetryDelay() {
        return maxRetryDelay;
    }

    /**
     * The maximum total retry timeout in milliseconds.
     *
     * @return maximum delay
     * @see WriteOptionsReactive.Builder#maxRetryTime(int)
     */
    public int getMaxRetryTime() {
        return maxRetryTime;
    }

    /**
     * The base for the exponential retry delay.
     * <p>
     * The next delay is computed as: retryInterval * exponentialBase^(attempts-1) + random(jitterInterval)
     * </p>
     *
     * @return exponential base
     * @see WriteOptionsReactive.Builder#exponentialBase(int)
     */
    @Override
    public int getExponentialBase() {
        return exponentialBase;
    }

    /**
     * @return The scheduler which is used for computational work.
     * @see WriteOptionsReactive.Builder#computationScheduler(Scheduler)
     */
    @Nonnull
    public Scheduler getComputationScheduler() {
        return computationScheduler;
    }

    private WriteOptionsReactive(@Nonnull final WriteOptionsReactive.Builder builder) {

        Arguments.checkNotNull(builder, "WriteOptionsReactive.Builder");

        batchSize = builder.batchSize;
        flushInterval = builder.flushInterval;
        jitterInterval = builder.jitterInterval;
        retryInterval = builder.retryInterval;
        maxRetries = builder.maxRetries;
        maxRetryDelay = builder.maxRetryDelay;
        maxRetryTime = builder.maxRetryTime;
        exponentialBase = builder.exponentialBase;
        computationScheduler = builder.computationScheduler;
    }

    /**
     * Creates a builder instance.
     *
     * @return a builder
     */
    @Nonnull
    public static WriteOptionsReactive.Builder builder() {
        return new WriteOptionsReactive.Builder();
    }

    /**
     * A builder for {@link WriteOptionsReactive}.
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
        private Scheduler computationScheduler = Schedulers.computation();

        /**
         * Set the number of data point to collect in batch.
         *
         * <p>
         * If you set the {@code batchSize} to '0'
         * the batching is disabled - whole upstream is written in one batch.
         * </p>
         *
         * @param batchSize the number of data point to collect in batch
         * @return {@code this}
         */
        @Nonnull
        public WriteOptionsReactive.Builder batchSize(final int batchSize) {
            Arguments.checkNotNegativeNumber(batchSize, "batchSize");
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
        public WriteOptionsReactive.Builder flushInterval(final int flushInterval) {
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
        public WriteOptionsReactive.Builder jitterInterval(final int jitterInterval) {
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
        public WriteOptionsReactive.Builder retryInterval(final int retryInterval) {
            Arguments.checkPositiveNumber(retryInterval, "retryInterval");
            this.retryInterval = retryInterval;
            return this;
        }

        /**
         * The number of max retries when write fails.
         *
         * <p>
         * If you set the {@code maxRetries} to '0'
         * the retry strategy is disabled - the error is immediately propagate to upstream.
         * </p>
         *
         * @param maxRetries number of max retries
         * @return {@code this}
         */
        @Nonnull
        public WriteOptionsReactive.Builder maxRetries(final int maxRetries) {
            Arguments.checkNotNegativeNumber(maxRetries, "maxRetries");
            this.maxRetries = maxRetries;
            return this;
        }

        /**
         * The maximum delay between each retry attempt in milliseconds.
         *
         * @param maxRetryDelay maximum delay
         * @return {@code this}
         */
        @Nonnull
        public WriteOptionsReactive.Builder maxRetryDelay(final int maxRetryDelay) {
            Arguments.checkPositiveNumber(maxRetryDelay, "maxRetryDelay");
            this.maxRetryDelay = maxRetryDelay;
            return this;
        }

        /**
         * The maximum total retry timeout in milliseconds.
         *
         * @param maxRetryTime maximum timout
         * @return {@code this}
         */
        @Nonnull
        public WriteOptionsReactive.Builder maxRetryTime(final int maxRetryTime) {
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
        public WriteOptionsReactive.Builder exponentialBase(final int exponentialBase) {
            Arguments.checkPositiveNumber(exponentialBase, "exponentialBase");
            this.exponentialBase = exponentialBase;
            return this;
        }


        /**
         * Set the scheduler which is used for computational work. Default value is {@link Schedulers#computation()}.
         *
         * @param computationScheduler the scheduler which is used for computational work.
         * @return {@code this}
         */
        @Nonnull
        public WriteOptionsReactive.Builder computationScheduler(@Nonnull final Scheduler computationScheduler) {

            Arguments.checkNotNull(computationScheduler, "Computation scheduler");

            this.computationScheduler = computationScheduler;
            return this;
        }

        /**
         * Build an instance of WriteOptions.
         *
         * @return {@code WriteOptions}
         */
        @Nonnull
        public WriteOptionsReactive build() {

            return new WriteOptionsReactive(this);
        }
    }
}
