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
package org.influxdata.platform;

import javax.annotation.Nonnull;

import org.influxdata.platform.domain.Health;
import org.influxdata.platform.rest.LogLevel;

import io.reactivex.Single;

/**
 *  The reference RxJava client for the <a href="https://github.com/influxdata/platform">Influx 2.0 OSS Platform</a>
 *  that allows query and write in a reactive way.
 *
 * @author Jakub Bednar (bednar@github) (20/11/2018 07:08)
 */
public interface PlatformClientReactive extends AutoCloseable {

    /**
     * Get the Query client.
     *
     * @return the new client instance for the Query API
     */
    @Nonnull
    QueryClientReactive createQueryClient();

    /**
     * Get the health of an instance.
     *
     * @return health of an instance
     */
    @Nonnull
    Single<Health> health();

    /**
     * @return the {@link LogLevel} that is used for logging requests and responses
     */
    @Nonnull
    LogLevel getLogLevel();

    /**
     * Set the log level for the request and response information.
     *
     * @param logLevel the log level to set.
     * @return the PlatformClient instance to be able to use it in a fluent manner.
     */
    @Nonnull
    PlatformClientReactive setLogLevel(@Nonnull final LogLevel logLevel);

    /**
     * Enable Gzip compress for http request body.
     *
     * Currently only the "Write" endpoint supports the Gzip compression.
     *
     * @return the {@link PlatformClient} instance to be able to use it in a fluent manner.
     */
    @Nonnull
    PlatformClientReactive enableGzip();

    /**
     * Disable Gzip compress for http request body.
     *
     * @return the {@link PlatformClient} instance to be able to use it in a fluent manner.
     */
    @Nonnull
    PlatformClientReactive disableGzip();

    /**
     * Returns whether Gzip compress for http request body is enabled.
     *
     * @return true if gzip is enabled.
     */
    boolean isGzipEnabled();
}