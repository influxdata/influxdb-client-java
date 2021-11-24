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
import javax.annotation.concurrent.ThreadSafe;

import com.influxdb.client.InfluxDBClientOptions;
import com.influxdb.client.domain.Dialect;
import com.influxdb.query.FluxRecord;

import org.reactivestreams.Publisher;

/**
 * The client that allow perform Flux query against theInfluxDB 2.0by a reactive way.
 *
 * @author Jakub Bednar (bednar@github) (21/11/2018 07:19)
 */
@ThreadSafe
public interface QueryReactiveApi {

    /**
     * Returns {@link Publisher} emitting {@link FluxRecord}s which are matched the query.
     * If none found than return empty sequence.
     *
     * <p>The {@link InfluxDBClientOptions#getOrg()} will be used as source organization.</p>
     *
     * @param query the Flux query to execute
     * @return {@link Publisher} of {@link FluxRecord}s
     */
    @Nonnull
    Publisher<FluxRecord> query(@Nonnull final String query);

    /**
     * Returns {@link Publisher} emitting {@link FluxRecord}s which are matched the query.
     * If none found than return empty sequence.
     *
     * @param query the Flux query to execute
     * @param org   specifies the source organization
     * @return {@link Publisher} of {@link FluxRecord}s
     */
    @Nonnull
    Publisher<FluxRecord> query(@Nonnull final String query, @Nonnull final String org);

    /**
     * Execute a Flux against the Flux service.
     *
     * <p>The {@link InfluxDBClientOptions#getOrg()} will be used as source organization.</p>
     *
     * @param query           the flux query to execute
     * @param measurementType the class type used to which will be result mapped
     * @param <M>             the type of the measurement (POJO)
     * @return {@link Publisher} emitting a POJO mapped to {@code measurementType} which are matched
     * the query or empty sequence if none found.
     */
    <M> Publisher<M> query(@Nonnull final String query,
                           @Nonnull final Class<M> measurementType);

    /**
     * Execute a Flux against the Flux service.
     *
     * @param query           the flux query to execute
     * @param org             specifies the source organization
     * @param measurementType the class type used to which will be result mapped
     * @param <M>             the type of the measurement (POJO)
     * @return {@link Publisher} emitting a POJO mapped to {@code measurementType} which are matched
     * the query or empty sequence if none found.
     */
    <M> Publisher<M> query(@Nonnull final String query,
                           @Nonnull final String org,
                           @Nonnull final Class<M> measurementType);

    /**
     * Returns {@link Publisher} emitting {@link FluxRecord}s which are matched the query.
     * If none found than return empty sequence.
     *
     * <p>The {@link InfluxDBClientOptions#getOrg()} will be used as source organization.</p>
     *
     * @param queryStream the Flux query publisher
     * @return {@link Publisher} of {@link FluxRecord}s
     */
    @Nonnull
    Publisher<FluxRecord> query(@Nonnull final Publisher<String> queryStream);

    /**
     * Returns {@link Publisher} emitting {@link FluxRecord}s which are matched the query.
     * If none found than return empty sequence.
     *
     * @param queryStream the Flux query publisher
     * @param org         specifies the source organization
     * @return {@link Publisher} of {@link FluxRecord}s
     */
    @Nonnull
    Publisher<FluxRecord> query(@Nonnull final Publisher<String> queryStream, @Nonnull final String org);

    /**
     * Returns the {@link Publisher} emitting POJO stream.
     * <p>
     * If none found than return empty sequence.
     *
     * <p>The {@link InfluxDBClientOptions#getOrg()} will be used as source organization.</p>
     *
     * @param measurementType the measurement class (POJO)
     * @param <M>             the type of the measurement (POJO)
     * @param queryStream     the Flux query publisher
     * @return {@link Publisher} of {@link FluxRecord}s
     */
    @Nonnull
    <M> Publisher<M> query(@Nonnull final Publisher<String> queryStream,
                           @Nonnull final Class<M> measurementType);

    /**
     * Returns the {@link Publisher} emitting POJO stream.
     * <p>
     * If none found than return empty sequence.
     *
     * @param measurementType the measurement class (POJO)
     * @param org             specifies the source organization
     * @param <M>             the type of the measurement (POJO)
     * @param queryStream     the Flux query publisher
     * @return {@link Publisher} of {@link FluxRecord}s
     */
    @Nonnull
    <M> Publisher<M> query(@Nonnull final Publisher<String> queryStream,
                           @Nonnull final String org,
                           @Nonnull final Class<M> measurementType);

    /**
     * Returns {@link Publisher} emitting raw response fromInfluxDB 2.0server line by line.
     *
     * <p>The {@link InfluxDBClientOptions#getOrg()} will be used as source organization.</p>
     *
     * @param query the Flux query to execute
     * @return {@link Publisher} of response lines
     */
    @Nonnull
    Publisher<String> queryRaw(@Nonnull final String query);

    /**
     * Returns {@link Publisher} emitting raw response fromInfluxDB 2.0server line by line.
     *
     * @param query the Flux query to execute
     * @param org   specifies the source organization
     * @return {@link Publisher} of response lines
     */
    @Nonnull
    Publisher<String> queryRaw(@Nonnull final String query, @Nonnull final String org);

    /**
     * Returns {@link Publisher} emitting queryRaw response from InfluxDB server line by line.
     *
     * <p>The {@link InfluxDBClientOptions#getOrg()} will be used as source organization.</p>
     *
     * @param queryStream the Flux query publisher
     * @return {@link Publisher} of response lines
     */
    @Nonnull
    Publisher<String> queryRaw(@Nonnull final Publisher<String> queryStream);

    /**
     * Returns {@link Publisher} emitting queryRaw response from InfluxDB server line by line.
     *
     * @param queryStream the Flux query publisher
     * @param org         specifies the source organization
     * @return {@link Publisher} of response lines
     */
    @Nonnull
    Publisher<String> queryRaw(@Nonnull final Publisher<String> queryStream, @Nonnull final String org);

    /**
     * Returns {@link Publisher} emitting queryRaw response fromInfluxDB 2.0server line by line.
     *
     * <p>The {@link InfluxDBClientOptions#getOrg()} will be used as source organization.</p>
     *
     * @param dialect Dialect is an object defining the options to use when encoding the response.
     *                <a href="http://bit.ly/flux-dialect">See dialect SPEC.</a>.
     * @param query   the Flux query to execute
     * @return {@link Publisher} of response lines
     */
    @Nonnull
    Publisher<String> queryRaw(@Nonnull final String query,
                               @Nullable final Dialect dialect);

    /**
     * Returns {@link Publisher} emitting queryRaw response fromInfluxDB 2.0server line by line.
     *
     * @param dialect Dialect is an object defining the options to use when encoding the response.
     *                <a href="http://bit.ly/flux-dialect">See dialect SPEC.</a>.
     * @param query   the Flux query to execute
     * @param org     specifies the source organization
     * @return {@link Publisher} of response lines
     */
    @Nonnull
    Publisher<String> queryRaw(@Nonnull final String query,
                               @Nullable final Dialect dialect,
                               @Nonnull final String org);

    /**
     * Returns {@link Publisher} emitting queryRaw response fromInfluxDB 2.0server line by line.
     *
     * <p>The {@link InfluxDBClientOptions#getOrg()} will be used as source organization.</p>
     *
     * @param dialect     Dialect is an object defining the options to use when encoding the response.
     *                    <a href="http://bit.ly/flux-dialect">See dialect SPEC.</a>.
     * @param queryStream the Flux query publisher
     * @return {@link Publisher} of response lines
     */
    @Nonnull
    Publisher<String> queryRaw(@Nonnull final Publisher<String> queryStream,
                               @Nullable final Dialect dialect);

    /**
     * Returns {@link Publisher} emitting queryRaw response fromInfluxDB 2.0server line by line.
     *
     * @param dialect     Dialect is an object defining the options to use when encoding the response.
     *                    <a href="http://bit.ly/flux-dialect">See dialect SPEC.</a>.
     * @param queryStream the Flux query publisher
     * @param org         specifies the source organization
     * @return {@link Publisher} of response lines
     */
    @Nonnull
    Publisher<String> queryRaw(@Nonnull final Publisher<String> queryStream,
                               @Nullable final Dialect dialect,
                               @Nonnull final String org);
}