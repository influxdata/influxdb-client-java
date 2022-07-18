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

import com.influxdb.utils.Arguments;

/**
 * The event is published when is backpressure applied.
 *
 * @author Jakub Bednar (bednar@github) (25/09/2018 12:27)
 */
public final class BackpressureEvent extends AbstractWriteEvent {

    private final BackpressureReason reason;

    public enum BackpressureReason {
        /**
         * DataPoint are emitted to fast.
         */
        FAST_EMITTING,

        /**
         * Server is not able to process batches fast enough.
         */
        TOO_MUCH_BATCHES,
    }

    private static final Logger LOG = Logger.getLogger(BackpressureEvent.class.getName());

    public BackpressureEvent(@Nonnull final BackpressureReason reason) {

        Arguments.checkNotNull(reason, "reason");

        this.reason = reason;
    }

    @Override
    public void logEvent() {

        LOG.log(Level.WARNING,
                String.format("Backpressure[%s] applied, try increase WriteOptions.bufferLimit", reason));
    }

    /**
     * @return reason of the backpressure
     */
    @Nonnull
    public BackpressureReason getReason() {
        return reason;
    }
}