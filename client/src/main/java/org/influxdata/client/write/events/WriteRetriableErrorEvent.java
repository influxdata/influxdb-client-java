package org.influxdata.client.write.events;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

import org.influxdata.Arguments;

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
        LOG.log(Level.SEVERE, msg, retryInterval);
    }
}