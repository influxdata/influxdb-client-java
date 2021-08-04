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

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;

import com.influxdb.client.WriteOptions;

import org.jetbrains.annotations.Nullable;
import retrofit2.HttpException;

/**
 * RetryConfiguration.
 *
 * @author Jakub Bednar (29/09/2020 14:19)
 */
public final class RetryAttempt {
    private static final Integer ABLE_TO_RETRY_ERROR = 429;
    private static final Logger LOG = Logger.getLogger(AbstractWriteClient.class.getName());
    private static Supplier<Double> jitterRandomSupplier;
    private static Supplier<Double> retryRandomSupplier;

    private final Throwable throwable;
    private final int count;
    private final WriteOptions writeOptions;

    RetryAttempt(final Throwable throwable, final int count, final WriteOptions writeOptions) {
        this.throwable = throwable;
        this.count = count;
        this.writeOptions = writeOptions;
    }

    /**
     * Sets the specific jitter random supplier.
     * @param supplier the hook supplier to set, null allowed
     */
    public static void setJitterRandomSupplier(@Nullable final Supplier<Double> supplier) {
        jitterRandomSupplier = supplier;
    }

    /**
     * Sets the specific retry random supplier.
     * @param supplier the hook supplier to set, null allowed
     */
    public static void setRetryRandomSupplier(@Nullable final Supplier<Double> supplier) {
        retryRandomSupplier = supplier;
    }

    /**
     * Is this request retryable?
     *
     * @return true if its retryable otherwise false
     */
    boolean isRetry() {

        //
        // Max retries exceeded.
        //
        if (count > writeOptions.getMaxRetries()) {

            LOG.log(Level.WARNING, "Max write retries exceeded.", throwable);

            return false;
        }

        if (throwable instanceof HttpException) {

            HttpException he = (HttpException) throwable;

            //
            // Retry HTTP error codes >= 429
            //
            return he.code() >= ABLE_TO_RETRY_ERROR;
        }

        if (throwable instanceof IOException) {
            // Much of the code here is inspired
            // by that in okhttp3.internal.http.RetryAndFollowUpInterceptor.

            if (throwable instanceof ProtocolException) {
                return false;
            }

            if (throwable instanceof InterruptedIOException) {
                return throwable instanceof SocketTimeoutException;
            }

            if (throwable instanceof SSLHandshakeException) {
                if (throwable.getCause() instanceof CertificateException) {
                    return false;
                }
            }
            if (throwable instanceof SSLPeerUnverifiedException) {
                // e.g. a certificate pinning error.
                return false;
            }

            return true;
        }

        return false;
    }

    /**
     * Get current retry interval.
     *
     * @return retry interval to sleep
     */
    long getRetryInterval() {

        long retryInterval;

        String retryAfter = getRetryAfter();
        // from header
        if (retryAfter != null) {

            retryInterval = TimeUnit.MILLISECONDS.convert(Integer.parseInt(retryAfter), TimeUnit.SECONDS);
            return retryInterval + jitterDelay(writeOptions.getJitterInterval());
            // from default conf
        } else {

            long rangeStart = writeOptions.getRetryInterval();
            long rangeStop = (long) writeOptions.getRetryInterval() * this.writeOptions.getExponentialBase();

            int i = 1;
            while (i < count) {
                i++;
                rangeStart = rangeStop;
                rangeStop = rangeStop * writeOptions.getExponentialBase();
                if (rangeStop > writeOptions.getMaxRetryDelay()) {
                    break;
                }
            }

            if (rangeStop > writeOptions.getMaxRetryDelay()) {
                rangeStop = writeOptions.getMaxRetryDelay();
            }

            retryInterval = (long) (rangeStart + (rangeStop - rangeStart)
                    * (retryRandomSupplier != null ? retryRandomSupplier.get() : Math.random()));

            String msg = "The InfluxDB does not specify \"Retry-After\". Use the default retryInterval: {0}";
            LOG.log(Level.FINEST, msg, retryInterval);
            LOG.log(Level.FINEST, "retry interval in range: [" + rangeStart + "," + rangeStop + "]");
            return retryInterval;
        }
    }

    /**
     * @return current throwable
     */
    Throwable getThrowable() {
        return throwable;
    }

    @Nullable
    private String getRetryAfter() {
        if (throwable instanceof HttpException) {
            return ((HttpException) throwable).response().headers().get("Retry-After");
        }

        return null;
    }

    /**
     * @param jitterInterval batch flush jitter interval
     * @return randomized delay
     */
    static int jitterDelay(final int jitterInterval) {

        return (int) ((jitterRandomSupplier != null ? jitterRandomSupplier.get() : Math.random()) * jitterInterval);
    }
}
