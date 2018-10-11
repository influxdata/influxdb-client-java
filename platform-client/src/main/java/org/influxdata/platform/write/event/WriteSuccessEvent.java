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

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

/**
 * The event is published when arrived the success response from Platform server.
 *
 * @author Jakub Bednar (bednar@github) (25/09/2018 12:26)
 */
public final class WriteSuccessEvent extends AbstractWriteEvent {

    private static final Logger LOG = Logger.getLogger(WriteSuccessEvent.class.getName());

    private String organization;
    private String bucket;
    private TimeUnit precision;
    private String token;
    private String lineProtocol;

    public WriteSuccessEvent(@Nonnull final String organization,
                             @Nonnull final String bucket,
                             @Nonnull final TimeUnit precision,
                             @Nonnull final String token,
                             @Nonnull final String lineProtocol) {


        this.organization = organization;
        this.bucket = bucket;
        this.precision = precision;
        this.token = token;
        this.lineProtocol = lineProtocol;
    }

    /**
     * @return the destination organization name for writes
     */
    public String getOrganization() {
        return organization;
    }

    /**
     * @return the destination bucket name for writes
     */
    public String getBucket() {
        return bucket;
    }

    /**
     * @return the token used to authorize write to bucket
     */
    public String getToken() {
        return token;
    }

    /**
     * @return the precision for the write
     */
    public TimeUnit getPrecision() {
        return precision;
    }

    /**
     * @return the data for the write
     */
    public String getLineProtocol() {
        return lineProtocol;
    }

    @Override
    public void logEvent() {

        LOG.log(Level.FINEST, "Success response from InfluxDB");
    }
}