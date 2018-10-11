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
import javax.annotation.Nullable;

import org.influxdata.flux.domain.FluxRecord;

import io.reactivex.Flowable;
import io.reactivex.Single;
import okhttp3.logging.HttpLoggingInterceptor;
import org.reactivestreams.Publisher;

/**
 * The client that allow perform Flux query against the InfluxDB by a reactive way.
 *
 * @author Jakub Bednar (bednar@github) (01/10/2018 12:38)
 */
public interface FluxClientReactive {

    /**
     * Returns {@link Flowable} emitting {@link FluxRecord}s which are matched the query.
     * If none found than return {@link Flowable#empty()}.
     *
     * @param query the Flux query to execute
     * @return {@link Flowable} of {@link FluxRecord}s
     */
    @Nonnull
    Flowable<FluxRecord> query(@Nonnull final String query);

    /**
     * Returns {@link Flowable} emitting {@link FluxRecord}s which are matched the query.
     * If none found than return {@link Flowable#empty()}.
     *
     * @param queryStream the Flux query publisher
     * @return {@link Flowable} of {@link FluxRecord}s
     */
    @Nonnull
    Flowable<FluxRecord> query(@Nonnull final Publisher<String> queryStream);

    /**
     * Returns {@link Flowable} emitting raw response from InfluxDB server line by line.
     *
     * @param query the Flux query to execute
     * @return  {@link Flowable} of response lines
     */
    @Nonnull
    Flowable<String> queryRaw(@Nonnull final String query);


    /**
     * Returns {@link Flowable} emitting queryRaw response from InfluxDB server line by line.
     *
     * @param queryStream the Flux query publisher
     * @return {@link Flowable} of response lines
     */
    @Nonnull
    Flowable<String> queryRaw(@Nonnull final Publisher<String> queryStream);

    /**
     * Returns {@link Flowable} emitting queryRaw response from InfluxDB server line by line.
     *
     * @param dialect Dialect is an object defining the options to use when encoding the response.
     *                <a href="http://bit.ly/flux-dialect">See dialect SPEC.</a>.
     * @param query   the Flux query to execute
     * @return {@link Flowable} of response lines
     */
    @Nonnull
    Flowable<String> queryRaw(@Nonnull final String query, @Nullable final String dialect);

    /**
     * Returns {@link Flowable} emitting queryRaw response from InfluxDB server line by line.
     *
     * @param dialect     Dialect is an object defining the options to use when encoding the response.
     *                    <a href="http://bit.ly/flux-dialect">See dialect SPEC.</a>.
     * @param queryStream the Flux query publisher
     * @return {@link Flowable} of response lines
     */
    @Nonnull
    Flowable<String> queryRaw(@Nonnull final Publisher<String> queryStream, @Nullable final String dialect);

    /**
     * Check the status of InfluxDB Server.
     *
     * @return {@link Boolean#TRUE} if server is healthy otherwise return {@link Boolean#FALSE}
     */
    @Nonnull
    Single<Boolean> ping();

    /**
     * Return the version of the connected InfluxDB Server.
     *
     * @return the version String, otherwise unknown.
     */
    @Nonnull
    Single<String> version();

    /**
     * The {@link HttpLoggingInterceptor.Level} that is used for logging requests and responses.
     *
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