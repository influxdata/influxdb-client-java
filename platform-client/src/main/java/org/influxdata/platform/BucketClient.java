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

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.influxdata.platform.domain.Bucket;
import org.influxdata.platform.domain.Organization;

/**
 * The client of the InfluxData Platform that implement Bucket HTTP API endpoint.
 *
 * @author Jakub Bednar (bednar@github) (13/09/2018 10:31)
 */
public interface BucketClient {

    /**
     * Creates a new bucket and sets {@link Bucket#id} with the new identifier.
     *
     * @param bucket bucket to create
     * @return Bucket created
     */
    @Nonnull
    Bucket createBucket(@Nonnull Bucket bucket);

    /**
     * Creates a new bucket and sets {@link Bucket#id} with the new identifier.
     *
     * @param name            name of the bucket
     * @param retentionPeriod bucket retention period
     * @param organization    owner of bucket
     * @return Bucket created
     */
    @Nonnull
    Bucket createBucket(@Nonnull final String name,
                        @Nonnull final String retentionPeriod,
                        @Nonnull final Organization organization);

    /**
     * Creates a new bucket and sets {@link Bucket#id} with the new identifier.
     *
     * @param name             name of the bucket
     * @param retentionPeriod  bucket retention period
     * @param organizationName owner of bucket
     * @return Bucket created
     */
    @Nonnull
    Bucket createBucket(@Nonnull final String name,
                        @Nonnull final String retentionPeriod,
                        @Nonnull final String organizationName);

    /**
     * Update a bucket name and retention.
     *
     * @param bucket bucket update to apply
     * @return bucket updated
     */
    @Nonnull
    Bucket updateBucket(@Nonnull final Bucket bucket);

    /**
     * Delete a bucket.
     *
     * @param bucket bucket to delete
     */
    void deleteBucket(@Nonnull final Bucket bucket);

    /**
     * Delete a bucket.
     *
     * @param bucketID ID of bucket to delete
     */
    void deleteBucket(@Nonnull final String bucketID);

    /**
     * Retrieve a bucket.
     *
     * @param bucketID ID of bucket to get
     * @return bucket details
     */
    @Nullable
    Bucket findBucketByID(@Nonnull final String bucketID);

    /**
     * List all buckets.
     *
     * @return List all buckets
     */
    @Nonnull
    List<Bucket> findBuckets();

    /**
     * List all buckets for specified {@code organizationID}.
     *
     * @param organization filter buckets to a specific organization
     * @return A list of buckets
     */
    @Nonnull
    List<Bucket> findBucketsByOrganization(@Nonnull final Organization organization);

    /**
     * List all buckets for specified {@code organizationID}.
     *
     * @param organizationName filter buckets to a specific organization name
     * @return A list of buckets
     */
    @Nonnull
    List<Bucket> findBucketsByOrganizationName(@Nullable final String organizationName);
}