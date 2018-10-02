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
package org.influxdata.flux;

import javax.annotation.Nonnull;

import org.influxdata.flux.domain.FluxRecord;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Response;

/**
 * The client for the new data scripting language centered on querying
 * and manipulating time series data by a reactive way.
 *
 * @author Jakub Bednar (bednar@github) (01/10/2018 12:38)
 */
public interface FluxClientReactive {

    /**
     * Returns {@link Flowable} emitting {@link FluxRecord}s which are matched the query.
     * If none found than return {@link Flowable#empty()}.
     *
     * @param query the flux query to execute
     * @return {@link Flowable} of {@link FluxRecord}s
     */
    @Nonnull
    Flowable<FluxRecord> query(@Nonnull final String query);

    /**
     * Returns {@link Maybe} emitting a raw {@code Response<ResponseBody>} which are matched the query.
     *
     * @param query the flux query to execute
     * @return {@link Maybe} of a raw {@code Response<ResponseBody>}
     */
    @Nonnull
    Maybe<Response<ResponseBody>> raw(@Nonnull final String query);

    /**
     * Enable Gzip compress for http request body.
     *
     * @return the FluxClient instance to be able to use it in a fluent manner.
     */
    @Nonnull
    FluxClientReactive enableGzip();

    /**
     * Disable Gzip compress for http request body.
     *
     * @return the FluxClient instance to be able to use it in a fluent manner.
     */
    @Nonnull
    FluxClientReactive disableGzip();

    /**
     * Returns whether Gzip compress for http request body is enabled.
     *
     * @return true if gzip is enabled.
     */
    boolean isGzipEnabled();

    /**
     * Check the status of Flux Server.
     *
     * @return {@link Boolean#TRUE} if server is healthy otherwise return {@link Boolean#FALSE}
     */
    @Nonnull
    Maybe<Boolean> ping();

    /**
     * @return the {@link HttpLoggingInterceptor.Level} that is used for logging requests and responses
     */
    @Nonnull
    HttpLoggingInterceptor.Level getLogLevel();

    /**
     * Set the log level for the request and response information.
     *
     * @param logLevel the log level to set.
     * @return the FluxClient instance to be able to use it in a fluent manner.
     */
    @Nonnull
    FluxClientReactive setLogLevel(@Nonnull final HttpLoggingInterceptor.Level logLevel);
}