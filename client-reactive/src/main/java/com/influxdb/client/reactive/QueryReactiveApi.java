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
package com.influxdb.client.reactive;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.influxdb.client.domain.Dialect;
import com.influxdb.query.FluxRecord;

import io.reactivex.Flowable;
import org.reactivestreams.Publisher;

/**
 * The client that allow perform Flux query against theInfluxDB 2.0by a reactive way.
 *
 * @author Jakub Bednar (bednar@github) (21/11/2018 07:19)
 */
public interface QueryReactiveApi {

    /**
     * Returns {@link Flowable} emitting {@link FluxRecord}s which are matched the query.
     * If none found than return {@link Flowable#empty()}.
     *
     * @param query the Flux query to execute
     * @param orgID specifies the source organization
     * @return {@link Flowable} of {@link FluxRecord}s
     */
    @Nonnull
    Flowable<FluxRecord> query(@Nonnull final String query, @Nonnull final String orgID);

    /**
     * Execute a Flux against the Flux service.
     *
     * @param query           the flux query to execute
     * @param orgID           specifies the source organization
     * @param measurementType the class type used to which will be result mapped
     * @param <M>             the type of the measurement (POJO)
     * @return {@link Flowable} emitting a POJO mapped to {@code measurementType} which are matched
     * the query or {@link Flowable#empty()} if none found.
     */
    <M> Flowable<M> query(@Nonnull final String query,
                          @Nonnull final String orgID,
                          @Nonnull final Class<M> measurementType);

    /**
     * Returns {@link Flowable} emitting {@link FluxRecord}s which are matched the query.
     * If none found than return {@link Flowable#empty()}.
     *
     * @param queryStream the Flux query publisher
     * @param orgID       specifies the source organization
     * @return {@link Flowable} of {@link FluxRecord}s
     */
    @Nonnull
    Flowable<FluxRecord> query(@Nonnull final Publisher<String> queryStream, @Nonnull final String orgID);

    /**
     * Returns the {@link Flowable} emitting POJO stream.
     * <p>
     * If none found than return {@link Flowable#empty()}.
     *
     * @param measurementType the measurement class (POJO)
     * @param orgID           specifies the source organization
     * @param <M>             the type of the measurement (POJO)
     * @param queryStream     the Flux query publisher
     * @return {@link Flowable} of {@link FluxRecord}s
     */
    @Nonnull
    <M> Flowable<M> query(@Nonnull final Publisher<String> queryStream,
                          @Nonnull final String orgID,
                          @Nonnull final Class<M> measurementType);

    /**
     * Returns {@link Flowable} emitting raw response fromInfluxDB 2.0server line by line.
     *
     * @param query the Flux query to execute
     * @param orgID specifies the source organization
     * @return {@link Flowable} of response lines
     */
    @Nonnull
    Flowable<String> queryRaw(@Nonnull final String query, @Nonnull final String orgID);

    /**
     * Returns {@link Flowable} emitting queryRaw response from InfluxDB server line by line.
     *
     * @param queryStream the Flux query publisher
     * @param orgID       specifies the source organization
     * @return {@link Flowable} of response lines
     */
    @Nonnull
    Flowable<String> queryRaw(@Nonnull final Publisher<String> queryStream, @Nonnull final String orgID);

    /**
     * Returns {@link Flowable} emitting queryRaw response fromInfluxDB 2.0server line by line.
     *
     * @param dialect Dialect is an object defining the options to use when encoding the response.
     *                <a href="http://bit.ly/flux-dialect">See dialect SPEC.</a>.
     * @param query   the Flux query to execute
     * @param orgID   specifies the source organization
     * @return {@link Flowable} of response lines
     */
    @Nonnull
    Flowable<String> queryRaw(@Nonnull final String query,
                              @Nullable final Dialect dialect,
                              @Nonnull final String orgID);

    /**
     * Returns {@link Flowable} emitting queryRaw response fromInfluxDB 2.0server line by line.
     *
     * @param dialect     Dialect is an object defining the options to use when encoding the response.
     *                    <a href="http://bit.ly/flux-dialect">See dialect SPEC.</a>.
     * @param queryStream the Flux query publisher
     * @param orgID       specifies the source organization
     * @return {@link Flowable} of response lines
     */
    @Nonnull
    Flowable<String> queryRaw(@Nonnull final Publisher<String> queryStream,
                              @Nullable final Dialect dialect,
                              @Nonnull final String orgID);
}