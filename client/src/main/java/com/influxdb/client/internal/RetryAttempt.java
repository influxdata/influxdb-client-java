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
package com.influxdb.client.internal;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.influxdb.client.WriteOptions;

import retrofit2.HttpException;

/**
 * RetryConfiguration.
 *
 * @author Jakub Bednar (29/09/2020 14:19)
 */
final class RetryAttempt {
    private static final Integer ABLE_TO_RETRY_ERROR = 429;
    private static final Logger LOG = Logger.getLogger(AbstractWriteClient.class.getName());

    private final Throwable throwable;
    private final int count;
    private final WriteOptions writeOptions;

    RetryAttempt(final Throwable throwable, final int count, final WriteOptions writeOptions) {
        this.throwable = throwable;
        this.count = count;
        this.writeOptions = writeOptions;
    }

    /**
     * Is this request retryable?
     *
     * @return true if its retryable otherwise false
     */
    boolean isRetry() {
        if (!(throwable instanceof HttpException)) {
            return false;
        }

        HttpException he = (HttpException) throwable;

        //
        // Retry HTTP error codes >= 429
        //
        if (he.code() < ABLE_TO_RETRY_ERROR) {
            return false;
        }

        //
        // Max retries exceeded.
        //
        if (count > writeOptions.getMaxRetries()) {
            String msg = String.format("Max write retries exceeded. Response: [%d]: %s", he.code(), he.message());
            LOG.log(Level.WARNING, msg);
            return false;
        }

        return true;
    }

    /**
     * Get current retry interval.
     *
     * @return retry interval to sleep
     */
    long getRetryInterval() {

        long retryInterval;

        String retryAfter = ((HttpException) throwable).response().headers().get("Retry-After");
        // from header
        if (retryAfter != null) {

            retryInterval = TimeUnit.MILLISECONDS.convert(Integer.parseInt(retryAfter),
                    TimeUnit.SECONDS);
            // from default conf
        } else {

            retryInterval = writeOptions.getRetryInterval()
                    * (long) (Math.pow(writeOptions.getExponentialBase(), count - 1));

            retryInterval = Math.min(retryInterval, writeOptions.getMaxRetryDelay());

            String msg = "The InfluxDB does not specify \"Retry-After\". "
                    + "Use the default retryInterval: {0}";
            LOG.log(Level.FINEST, msg, retryInterval);
        }

        retryInterval = retryInterval + jitterDelay(writeOptions.getJitterInterval());

        return retryInterval;
    }

    /**
     * @return current throwable
     */
    Throwable getThrowable() {
        return throwable;
    }

    /**
     * @param jitterInterval batch flush jitter interval
     * @return randomized delay
     */
    static int jitterDelay(final int jitterInterval) {

        return (int) (Math.random() * jitterInterval);
    }
}
