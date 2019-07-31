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
package com.influxdb.client;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.influxdb.client.domain.Bucket;
import com.influxdb.client.domain.HealthCheck;
import com.influxdb.client.domain.Source;

/**
 * The client of the InfluxDB 2.0 that implement Source HTTP API endpoint.
 *
 * @author Jakub Bednar (bednar@github) (18/09/2018 09:01)
 */
public interface SourcesApi {

    /**
     * Creates a Source, sets the sources ID and stores it.
     *
     * @param source to create
     * @return created source
     */
    @Nonnull
    Source createSource(@Nonnull final Source source);

    /**
     * Update the source.
     *
     * @param source to update
     * @return updated source
     */
    @Nonnull
    Source updateSource(@Nonnull final Source source);

    /**
     * Delete a source.
     *
     * @param source source to delete
     */
    void deleteSource(@Nonnull final Source source);

    /**
     * Delete a source.
     *
     * @param sourceID ID of source to delete
     */
    void deleteSource(@Nonnull final String sourceID);

    /**
     * Clone a source.
     *
     * @param clonedName name of cloned source
     * @param sourceID   ID of source to clone
     * @return cloned source
     */
    @Nonnull
    Source cloneSource(@Nonnull final String clonedName, @Nonnull final String sourceID);

    /**
     * Clone a source.
     *
     * @param clonedName name of cloned source
     * @param source     source to clone
     * @return cloned source
     */
    @Nonnull
    Source cloneSource(@Nonnull final String clonedName, @Nonnull final Source source);

    /**
     * Retrieve a source.
     *
     * @param sourceID ID of source to get
     * @return source details
     */
    @Nonnull
    Source findSourceByID(@Nonnull final String sourceID);

    /**
     * Get all sources.
     *
     * @return A list of sources
     */
    @Nonnull
    List<Source> findSources();

    /**
     * Get a sources buckets (will return dbrps in the form of buckets if it is a v1 source).
     *
     * @param source filter buckets to a specific source
     * @return buckets for source. If source does not exist than return null.
     */
    @Nullable
    List<Bucket> findBucketsBySource(@Nonnull final Source source);

    /**
     * Get a sources buckets (will return dbrps in the form of buckets if it is a v1 source).
     *
     * @param sourceID filter buckets to a specific source ID
     * @return buckets for source. If source does not exist than return null.
     */
    @Nonnull
    List<Bucket> findBucketsBySourceID(@Nonnull final String sourceID);

    /**
     * Get a sources health.
     *
     * @param source to check health
     *
     * @return health of source
     */
    @Nonnull
    HealthCheck health(@Nonnull final Source source);

    /**
     * Get a sources health.
     *
     * @param sourceID to check health
     *
     * @return health of source
     */
    @Nonnull
    HealthCheck health(@Nonnull final String sourceID);
}