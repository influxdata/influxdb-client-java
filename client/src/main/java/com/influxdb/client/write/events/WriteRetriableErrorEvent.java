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
package com.influxdb.client.write.events;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

import com.influxdb.Arguments;

/**
 * The event is published when occurs a retriable write exception.
 *
 * @author Jakub Bednar (bednar@github) (05/03/2019 09:46)
 */
public final class WriteRetriableErrorEvent extends AbstractWriteEvent {

    private static final Logger LOG = Logger.getLogger(WriteRetriableErrorEvent.class.getName());

    private final Throwable throwable;
    private final Long retryInterval;

    public WriteRetriableErrorEvent(@Nonnull final Throwable throwable,
                                    @Nonnull final Long retryInterval) {

        Arguments.checkNotNull(throwable, "Throwable");
        Arguments.checkNotNull(retryInterval, "retryInterval");

        this.throwable = throwable;
        this.retryInterval = retryInterval;
    }

    /**
     * @return the exception that was throw
     */
    @Nonnull
    public Throwable getThrowable() {
        return throwable;
    }

    /**
     * @return the time to wait before retry unsuccessful write (milliseconds)
     */
    @Nonnull
    public Long getRetryInterval() {
        return retryInterval;
    }

    @Override
    public void logEvent() {
        String msg = "The retriable error occurred during writing of data. Retry in: {0} [ms]";
        LOG.log(Level.WARNING, msg, retryInterval);
    }
}