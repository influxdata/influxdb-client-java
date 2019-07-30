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
package com.influxdb.client.internal;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.influxdb.Arguments;
import com.influxdb.client.BucketsApi;
import com.influxdb.client.FindOptions;
import com.influxdb.client.domain.AddResourceMemberRequestBody;
import com.influxdb.client.domain.Bucket;
import com.influxdb.client.domain.BucketRetentionRules;
import com.influxdb.client.domain.Buckets;
import com.influxdb.client.domain.Label;
import com.influxdb.client.domain.LabelMapping;
import com.influxdb.client.domain.LabelResponse;
import com.influxdb.client.domain.LabelsResponse;
import com.influxdb.client.domain.OperationLog;
import com.influxdb.client.domain.OperationLogs;
import com.influxdb.client.domain.Organization;
import com.influxdb.client.domain.ResourceMember;
import com.influxdb.client.domain.ResourceMembers;
import com.influxdb.client.domain.ResourceOwner;
import com.influxdb.client.domain.ResourceOwners;
import com.influxdb.client.domain.User;
import com.influxdb.client.service.BucketsService;
import com.influxdb.internal.AbstractRestClient;

import retrofit2.Call;

/**
 * @author Jakub Bednar (bednar@github) (13/09/2018 10:47)
 */
final class BucketsApiImpl extends AbstractRestClient implements BucketsApi {

    private static final Logger LOG = Logger.getLogger(BucketsApiImpl.class.getName());

    private final BucketsService service;

    BucketsApiImpl(@Nonnull final BucketsService service) {

        Arguments.checkNotNull(service, "service");

        this.service = service;
    }

    @Nonnull
    @Override
    public Bucket findBucketByID(@Nonnull final String bucketID) {

        Arguments.checkNonEmpty(bucketID, "Bucket ID");

        Call<Bucket> bucket = service.getBucketsID(bucketID, null);

        return execute(bucket);
    }

    @Nullable
    @Override
    public Bucket findBucketByName(@Nonnull final String bucketName) {

        Arguments.checkNonEmpty(bucketName, "Bucket Name");

        Call<Buckets> bucket = service
                .getBuckets(null, null, null, null, null, bucketName);

        return execute(bucket).getBuckets().stream().findFirst().orElse(null);
    }

    @Nonnull
    @Override
    public List<Bucket> findBuckets() {
        return findBucketsByOrgName(null);
    }

    @Override
    @Nonnull
    public Buckets findBuckets(@Nonnull final FindOptions findOptions) {

        Arguments.checkNotNull(findOptions, "findOptions");

        return findBuckets(null, findOptions);
    }

    @Nonnull
    public List<Bucket> findBucketsByOrg(@Nonnull final Organization organization) {

        Arguments.checkNotNull(organization, "Organization is required");

        return findBucketsByOrgName(organization.getName());
    }

    @Nonnull
    @Override
    public List<Bucket> findBucketsByOrgName(@Nullable final String orgName) {

        Buckets buckets = findBuckets(orgName, new FindOptions());
        LOG.log(Level.FINEST, "findBucketsByOrgName found: {0}", buckets);

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
                               @Nullable final BucketRetentionRules bucketRetentionRules,
                               @Nonnull final Organization organization) {

        Arguments.checkNotNull(organization, "Organization is required");

        return createBucket(name, bucketRetentionRules, organization.getId());
    }

    @Nonnull
    @Override
    public Bucket createBucket(@Nonnull final String name, @Nonnull final String orgID) {
        return createBucket(name, null, orgID);
    }

    @Nonnull
    @Override
    public Bucket createBucket(@Nonnull final String name,
                               @Nullable final BucketRetentionRules bucketRetentionRules,
                               @Nonnull final String orgID) {

        Arguments.checkNonEmpty(name, "Bucket name");
        Arguments.checkNonEmpty(orgID, "Organization Id");

        Bucket bucket = new Bucket();
        bucket.setName(name);
        bucket.setOrgID(orgID);
        if (bucketRetentionRules != null) {
            bucket.getRetentionRules().add(bucketRetentionRules);
        }

        return createBucket(bucket);
    }

    @Nonnull
    @Override
    public Bucket createBucket(@Nonnull final Bucket bucket) {

        Arguments.checkNotNull(bucket, "Bucket is required");
        Arguments.checkNonEmpty(bucket.getName(), "Bucket name");

        Call<Bucket> call = service.postBuckets(bucket, null);

        return execute(call);
    }

    @Nonnull
    @Override
    public Bucket updateBucket(@Nonnull final Bucket bucket) {

        Arguments.checkNotNull(bucket, "Bucket is required");

        Call<Bucket> bucketCall = service.patchBucketsID(bucket.getId(), bucket, null);

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

        Call<Void> call = service.deleteBucketsID(bucketID, null);
        execute(call);
    }

    @Nonnull
    @Override
    public Bucket cloneBucket(@Nonnull final String clonedName, @Nonnull final String bucketID) {

        Arguments.checkNonEmpty(clonedName, "clonedName");
        Arguments.checkNonEmpty(bucketID, "bucketID");

        Bucket bucket = findBucketByID(bucketID);

        return cloneBucket(clonedName, bucket);
    }

    @Nonnull
    @Override
    public Bucket cloneBucket(@Nonnull final String clonedName, @Nonnull final Bucket bucket) {

        Arguments.checkNonEmpty(clonedName, "clonedName");
        Arguments.checkNotNull(bucket, "Bucket");

        Bucket cloned = new Bucket();
        cloned.setName(clonedName);
        cloned.setOrgID(bucket.getOrgID());
        cloned.setRp(bucket.getRp());
        cloned.setDescription(bucket.getDescription());
        cloned.getRetentionRules().addAll(bucket.getRetentionRules());

        Bucket created = createBucket(cloned);

        getLabels(bucket).forEach(label -> addLabel(label, created));

        return created;
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

        Call<ResourceMembers> call = service.getBucketsIDMembers(bucketID, null);
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

        AddResourceMemberRequestBody user = new AddResourceMemberRequestBody();
        user.setId(memberID);

        Call<ResourceMember> call = service.postBucketsIDMembers(bucketID, user, null);

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

        Call<Void> call = service.deleteBucketsIDMembersID(memberID, bucketID, null);
        execute(call);
    }

    @Nonnull
    @Override
    public List<ResourceOwner> getOwners(@Nonnull final Bucket bucket) {

        Arguments.checkNotNull(bucket, "bucket");

        return getOwners(bucket.getId());
    }

    @Nonnull
    @Override
    public List<ResourceOwner> getOwners(@Nonnull final String bucketID) {

        Arguments.checkNonEmpty(bucketID, "Bucket.ID");

        Call<ResourceOwners> call = service.getBucketsIDOwners(bucketID, null);
        ResourceOwners resourceMembers = execute(call);
        LOG.log(Level.FINEST, "findBucketOwners found: {0}", resourceMembers);

        return resourceMembers.getUsers();
    }

    @Nonnull
    @Override
    public ResourceOwner addOwner(@Nonnull final User owner, @Nonnull final Bucket bucket) {

        Arguments.checkNotNull(bucket, "bucket");
        Arguments.checkNotNull(owner, "owner");

        return addOwner(owner.getId(), bucket.getId());
    }

    @Nonnull
    @Override
    public ResourceOwner addOwner(@Nonnull final String ownerID, @Nonnull final String bucketID) {

        Arguments.checkNonEmpty(ownerID, "Owner ID");
        Arguments.checkNonEmpty(bucketID, "Bucket.ID");

        AddResourceMemberRequestBody user = new AddResourceMemberRequestBody();
        user.setId(ownerID);

        Call<ResourceOwner> call = service.postBucketsIDOwners(bucketID, user, null);

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

        Call<Void> call = service.deleteBucketsIDOwnersID(ownerID, bucketID, null);
        execute(call);
    }

    @Nonnull
    @Override
    public List<OperationLog> findBucketLogs(@Nonnull final Bucket bucket) {

        Arguments.checkNotNull(bucket, "bucket");

        return findBucketLogs(bucket.getId());
    }

    @Nonnull
    @Override
    public OperationLogs findBucketLogs(@Nonnull final Bucket bucket,
                                        @Nonnull final FindOptions findOptions) {

        Arguments.checkNotNull(bucket, "bucket");
        Arguments.checkNotNull(findOptions, "findOptions");

        return findBucketLogs(bucket.getId(), findOptions);
    }

    @Nonnull
    @Override
    public List<OperationLog> findBucketLogs(@Nonnull final String bucketID) {

        Arguments.checkNonEmpty(bucketID, "Bucket.ID");

        return findBucketLogs(bucketID, new FindOptions()).getLogs();
    }

    @Nonnull
    @Override
    public OperationLogs findBucketLogs(@Nonnull final String bucketID,
                                        @Nonnull final FindOptions findOptions) {

        Arguments.checkNonEmpty(bucketID, "Bucket.ID");
        Arguments.checkNotNull(findOptions, "findOptions");

        Call<OperationLogs> call = service.getBucketsIDLogs(bucketID, null,
                findOptions.getOffset(), findOptions.getLimit());

        return execute(call);
    }

    @Nonnull
    @Override
    public List<Label> getLabels(@Nonnull final Bucket bucket) {

        Arguments.checkNotNull(bucket, "bucket");

        return getLabels(bucket.getId());
    }

    @Nonnull
    @Override
    public List<Label> getLabels(@Nonnull final String bucketID) {

        Arguments.checkNonEmpty(bucketID, "bucketID");

        Call<LabelsResponse> call = service.getBucketsIDLabels(bucketID, null);

        return execute(call).getLabels();
    }

    @Nonnull
    @Override
    public LabelResponse addLabel(@Nonnull final Label label, @Nonnull final Bucket bucket) {

        Arguments.checkNotNull(label, "label");
        Arguments.checkNotNull(bucket, "bucket");

        return addLabel(label.getId(), bucket.getId());
    }

    @Nonnull
    @Override
    public LabelResponse addLabel(@Nonnull final String labelID, @Nonnull final String bucketID) {

        Arguments.checkNonEmpty(labelID, "labelID");
        Arguments.checkNonEmpty(bucketID, "bucketID");

        LabelMapping labelMapping = new LabelMapping();
        labelMapping.setLabelID(labelID);

        Call<LabelResponse> call = service.postBucketsIDLabels(bucketID, labelMapping, null);

        return execute(call);
    }

    @Override
    public void deleteLabel(@Nonnull final Label label, @Nonnull final Bucket bucket) {

        Arguments.checkNotNull(label, "label");
        Arguments.checkNotNull(bucket, "bucket");

        deleteLabel(label.getId(), bucket.getId());
    }

    @Override
    public void deleteLabel(@Nonnull final String labelID, @Nonnull final String bucketID) {

        Arguments.checkNonEmpty(labelID, "labelID");
        Arguments.checkNonEmpty(bucketID, "bucketID");

        Call<Void> call = service.deleteBucketsIDLabelsID(bucketID, labelID, null);
        execute(call);
    }

    @Nonnull
    private Buckets findBuckets(@Nullable final String orgName,
                                @Nonnull final FindOptions findOptions) {

        Call<Buckets> bucketsCall = service.getBuckets(null, findOptions.getOffset(),
                findOptions.getLimit(), orgName, null, null);

        return execute(bucketsCall);
    }
}