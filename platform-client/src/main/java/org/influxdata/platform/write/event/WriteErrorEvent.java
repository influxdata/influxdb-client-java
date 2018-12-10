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
package org.influxdata.platform.write.event;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

import org.influxdata.platform.Arguments;

/**
 * The event is published when occurs a write exception.
 *
 * @author Jakub Bednar (bednar@github) (30/07/2018 14:57)
 */
public final class WriteErrorEvent extends AbstractWriteEvent {

    private static final Logger LOG = Logger.getLogger(WriteErrorEvent.class.getName());

    private final Throwable throwable;

    public WriteErrorEvent(@Nonnull final Throwable throwable) {

        Arguments.checkNotNull(throwable, "Throwable");

        this.throwable = throwable;
    }

    /**
     * @return the exception that was throw
     */
    @Nonnull
    public Throwable getThrowable() {
        return throwable;
    }

    @Override
    public void logEvent() {
        LOG.log(Level.SEVERE, "The error occurred during writing of data", throwable);
    }
}
