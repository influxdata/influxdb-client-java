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
package org.influxdata.platform.impl;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.influxdata.platform.Arguments;
import org.influxdata.platform.BucketClient;
import org.influxdata.platform.domain.Bucket;
import org.influxdata.platform.domain.Buckets;
import org.influxdata.platform.domain.Organization;
import org.influxdata.platform.domain.ResourceMember;
import org.influxdata.platform.domain.ResourceMembers;
import org.influxdata.platform.domain.RetentionRule;
import org.influxdata.platform.domain.User;
import org.influxdata.platform.error.rest.NotFoundException;
import org.influxdata.platform.rest.AbstractRestClient;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import retrofit2.Call;

/**
 * @author Jakub Bednar (bednar@github) (13/09/2018 10:47)
 */
final class BucketClientImpl extends AbstractRestClient implements BucketClient {

    private static final Logger LOG = Logger.getLogger(BucketClientImpl.class.getName());

    private final PlatformService platformService;
    private final JsonAdapter<Bucket> adapter;
    private final JsonAdapter<User> userAdapter;

    BucketClientImpl(@Nonnull final PlatformService platformService, @Nonnull final Moshi moshi) {

        Arguments.checkNotNull(platformService, "PlatformService");
        Arguments.checkNotNull(moshi, "Moshi to create adapter");

        this.platformService = platformService;
        this.adapter = moshi.adapter(Bucket.class);
        this.userAdapter = moshi.adapter(User.class);
    }

    @Nullable
    @Override
    public Bucket findBucketByID(@Nonnull final String bucketID) {

        Arguments.checkNonEmpty(bucketID, "Bucket ID");

        Call<Bucket> bucket = platformService.findBucketByID(bucketID);

        return execute(bucket, NotFoundException.class);
    }

    @Nonnull
    @Override
    public List<Bucket> findBuckets() {
        return findBucketsByOrganizationName(null);
    }

    @Nonnull
    public List<Bucket> findBucketsByOrganization(@Nonnull final Organization organization) {

        Arguments.checkNotNull(organization, "Organization is required");

        return findBucketsByOrganizationName(organization.getName());
    }

    @Nonnull
    @Override
    public List<Bucket> findBucketsByOrganizationName(@Nullable final String organizationName) {

        Call<Buckets> bucketsCall = platformService.findBuckets(organizationName);

        Buckets buckets = execute(bucketsCall);
        LOG.log(Level.FINEST, "findBucketsByOrganizationName found: {0}", buckets);

        return buckets.getBuckets();
    }

    @Nonnull
    @Override
    public Bucket createBucket(@Nonnull final String name, @Nonnull final Organization organization) {
        return createBucket(name, null, organization);
    }

    @Nonnull
    @Override
    public Bucket createBucket(@Nonnull final String name,
                               @Nullable  final RetentionRule retentionRule,
                               @Nonnull final Organization organization) {

        Arguments.checkNotNull(organization, "Organization is required");

        return createBucket(name, retentionRule, organization.getName());
    }

    @Nonnull
    @Override
    public Bucket createBucket(@Nonnull final String name, @Nonnull final String organizationName) {
        return createBucket(name, null, organizationName);
    }

    @Nonnull
    @Override
    public Bucket createBucket(@Nonnull final String name,
                               @Nullable final RetentionRule retentionRule,
                               @Nonnull final String organizationName) {

        Arguments.checkNonEmpty(name, "Bucket name");
        Arguments.checkNonEmpty(organizationName, "Organization name");

        Bucket bucket = new Bucket();
        bucket.setName(name);
        bucket.setOrganizationName(organizationName);
        if (retentionRule != null) {
            bucket.getRetentionRules().add(retentionRule);
        }

        return createBucket(bucket);
    }

    @Nonnull
    @Override
    public Bucket createBucket(@Nonnull final Bucket bucket) {

        Arguments.checkNotNull(bucket, "Bucket is required");
        Arguments.checkNonEmpty(bucket.getName(), "Bucket name");
        Arguments.checkNonEmpty(bucket.getOrganizationName(), "Organization name");

        Call<Bucket> call = platformService.createBucket(createBody(adapter.toJson(bucket)));

        return execute(call);
    }

    @Nonnull
    @Override
    public Bucket updateBucket(@Nonnull final Bucket bucket) {

        Arguments.checkNotNull(bucket, "Bucket is required");

        String json = adapter.toJson(bucket);

        Call<Bucket> bucketCall = platformService.updateBucket(bucket.getId(), createBody(json));

        return execute(bucketCall);
    }

    @Override
    public void deleteBucket(@Nonnull final Bucket bucket) {

        Arguments.checkNotNull(bucket, "Bucket");

        deleteBucket(bucket.getId());
    }

    @Override
    public void deleteBucket(@Nonnull final String bucketID) {

        Arguments.checkNonEmpty(bucketID, "bucketID");

        Call<Void> call = platformService.deleteBucket(bucketID);
        execute(call);
    }

    @Nonnull
    @Override
    public List<ResourceMember> getMembers(@Nonnull final Bucket bucket) {

        Arguments.checkNotNull(bucket, "Bucket");

        return getMembers(bucket.getId());
    }

    @Nonnull
    @Override
    public List<ResourceMember> getMembers(@Nonnull final String bucketID) {

        Arguments.checkNonEmpty(bucketID, "Bucket.ID");

        Call<ResourceMembers> call = platformService.findBucketMembers(bucketID);
        ResourceMembers resourceMembers = execute(call);
        LOG.log(Level.FINEST, "findBucketMembers found: {0}", resourceMembers);

        return resourceMembers.getUsers();
    }

    @Nonnull
    @Override
    public ResourceMember addMember(@Nonnull final User member, @Nonnull final Bucket bucket) {

        Arguments.checkNotNull(bucket, "bucket");
        Arguments.checkNotNull(member, "member");

        return addMember(member.getId(), bucket.getId());
    }

    @Nonnull
    @Override
    public ResourceMember addMember(@Nonnull final String memberID, @Nonnull final String bucketID) {

        Arguments.checkNonEmpty(memberID, "Member ID");
        Arguments.checkNonEmpty(bucketID, "Bucket.ID");

        User user = new User();
        user.setId(memberID);

        String json = userAdapter.toJson(user);
        Call<ResourceMember> call = platformService.addBucketMember(bucketID, createBody(json));

        return execute(call);
    }

    @Override
    public void deleteMember(@Nonnull final User member, @Nonnull final Bucket bucket) {

        Arguments.checkNotNull(bucket, "bucket");
        Arguments.checkNotNull(member, "member");

        deleteMember(member.getId(), bucket.getId());
    }

    @Override
    public void deleteMember(@Nonnull final String memberID, @Nonnull final String bucketID) {

        Arguments.checkNonEmpty(memberID, "Member ID");
        Arguments.checkNonEmpty(bucketID, "Bucket.ID");

        Call<Void> call = platformService.deleteBucketMember(bucketID, memberID);
        execute(call);
    }

    @Nonnull
    @Override
    public List<ResourceMember> getOwners(@Nonnull final Bucket bucket) {

        Arguments.checkNotNull(bucket, "bucket");

        return getOwners(bucket.getId());
    }

    @Nonnull
    @Override
    public List<ResourceMember> getOwners(@Nonnull final String bucketID) {

        Arguments.checkNonEmpty(bucketID, "Bucket.ID");

        Call<ResourceMembers> call = platformService.findBucketOwners(bucketID);
        ResourceMembers resourceMembers = execute(call);
        LOG.log(Level.FINEST, "findBucketOwners found: {0}", resourceMembers);

        return resourceMembers.getUsers();
    }

    @Nonnull
    @Override
    public ResourceMember addOwner(@Nonnull final User owner, @Nonnull final Bucket bucket) {

        Arguments.checkNotNull(bucket, "bucket");
        Arguments.checkNotNull(owner, "owner");

        return addOwner(owner.getId(), bucket.getId());
    }

    @Nonnull
    @Override
    public ResourceMember addOwner(@Nonnull final String ownerID, @Nonnull final String bucketID) {

        Arguments.checkNonEmpty(ownerID, "Owner ID");
        Arguments.checkNonEmpty(bucketID, "Bucket.ID");

        User user = new User();
        user.setId(ownerID);

        String json = userAdapter.toJson(user);
        Call<ResourceMember> call = platformService.addBucketOwner(bucketID, createBody(json));

        return execute(call);
    }

    @Override
    public void deleteOwner(@Nonnull final User owner, @Nonnull final Bucket bucket) {

        Arguments.checkNotNull(bucket, "bucket");
        Arguments.checkNotNull(owner, "owner");

        deleteOwner(owner.getId(), bucket.getId());
    }

    @Override
    public void deleteOwner(@Nonnull final String ownerID, @Nonnull final String bucketID) {

        Arguments.checkNonEmpty(ownerID, "Owner ID");
        Arguments.checkNonEmpty(bucketID, "Bucket.ID");

        Call<Void> call = platformService.deleteBucketOwner(bucketID, ownerID);
        execute(call);
    }
}