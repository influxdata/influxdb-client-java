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
package org.influxdata.client.internal;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.influxdata.Arguments;
import org.influxdata.client.BucketsApi;
import org.influxdata.client.domain.AddResourceMemberRequestBody;
import org.influxdata.client.domain.Bucket;
import org.influxdata.client.domain.BucketRetentionRules;
import org.influxdata.client.domain.Buckets;
import org.influxdata.client.domain.FindOptions;
import org.influxdata.client.domain.Label;
import org.influxdata.client.domain.OperationLog;
import org.influxdata.client.domain.OperationLogs;
import org.influxdata.client.domain.Organization;
import org.influxdata.client.domain.ResourceMember;
import org.influxdata.client.domain.ResourceMembers;
import org.influxdata.client.domain.ResourceOwner;
import org.influxdata.client.domain.ResourceOwners;
import org.influxdata.client.domain.User;
import org.influxdata.exceptions.NotFoundException;

import com.google.gson.Gson;
import retrofit2.Call;

/**
 * @author Jakub Bednar (bednar@github) (13/09/2018 10:47)
 */
final class BucketsApiImpl extends AbstractInfluxDBRestClient implements BucketsApi {

    private static final Logger LOG = Logger.getLogger(BucketsApiImpl.class.getName());

    BucketsApiImpl(@Nonnull final InfluxDBService influxDBService, @Nonnull final Gson gson) {

        super(influxDBService, gson);
    }

    @Nullable
    @Override
    public Bucket findBucketByID(@Nonnull final String bucketID) {

        Arguments.checkNonEmpty(bucketID, "Bucket ID");

        Call<Bucket> bucket = influxDBService.findBucketByID(bucketID);

        return execute(bucket, NotFoundException.class);
    }

    @Nullable
    @Override
    public Bucket findBucketByName(@Nonnull final String bucketName) {

        Arguments.checkNonEmpty(bucketName, "Bucket Name");

        Call<Buckets> bucket = influxDBService.findBucketByName(bucketName);

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
        bucket.setOrganizationID(orgID);
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

        Call<Bucket> call = influxDBService.createBucket(createBody(gson.toJson(bucket)));

        return execute(call);
    }

    @Nonnull
    @Override
    public Bucket updateBucket(@Nonnull final Bucket bucket) {

        Arguments.checkNotNull(bucket, "Bucket is required");

        String json = gson.toJson(bucket);

        Call<Bucket> bucketCall = influxDBService.updateBucket(bucket.getId(), createBody(json));

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

        Call<Void> call = influxDBService.deleteBucket(bucketID);
        execute(call);
    }

    @Nonnull
    @Override
    public Bucket cloneBucket(@Nonnull final String clonedName, @Nonnull final String bucketID) {

        Arguments.checkNonEmpty(clonedName, "clonedName");
        Arguments.checkNonEmpty(bucketID, "bucketID");

        Bucket bucket = findBucketByID(bucketID);
        if (bucket == null) {
            throw new IllegalStateException("NotFound Bucket with ID: " + bucketID);
        }

        return cloneBucket(clonedName, bucket);
    }

    @Nonnull
    @Override
    public Bucket cloneBucket(@Nonnull final String clonedName, @Nonnull final Bucket bucket) {

        Arguments.checkNonEmpty(clonedName, "clonedName");
        Arguments.checkNotNull(bucket, "Bucket");

        Bucket cloned = new Bucket();
        cloned.setName(clonedName);
        cloned.setOrganizationID(bucket.getOrganizationID());
        cloned.setOrganization(bucket.getOrganization());
        cloned.setRp(bucket.getRp());
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

        Call<ResourceMembers> call = influxDBService.findBucketMembers(bucketID);
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

        String json = gson.toJson(user);
        Call<ResourceMember> call = influxDBService.addBucketMember(bucketID, createBody(json));

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

        Call<Void> call = influxDBService.deleteBucketMember(bucketID, memberID);
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

        Call<ResourceOwners> call = influxDBService.findBucketOwners(bucketID);
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

        String json = gson.toJson(user);
        Call<ResourceOwner> call = influxDBService.addBucketOwner(bucketID, createBody(json));

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

        Call<Void> call = influxDBService.deleteBucketOwner(bucketID, ownerID);
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

        return findBucketLogs(bucketID, new FindOptions()).getLog();
    }

    @Nonnull
    @Override
    public OperationLogs findBucketLogs(@Nonnull final String bucketID,
                                        @Nonnull final FindOptions findOptions) {

        Arguments.checkNonEmpty(bucketID, "Bucket.ID");
        Arguments.checkNotNull(findOptions, "findOptions");

        Call<OperationLogs> call = influxDBService.findBucketLogs(bucketID, createQueryMap(findOptions));

        return getOperationLogEntries(call, new OperationLogs());
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

        return getLabels(bucketID, "buckets");
    }

    @Nonnull
    @Override
    public Label addLabel(@Nonnull final Label label, @Nonnull final Bucket bucket) {

        Arguments.checkNotNull(label, "label");
        Arguments.checkNotNull(bucket, "bucket");

        return addLabel(label.getId(), bucket.getId());
    }

    @Nonnull
    @Override
    public Label addLabel(@Nonnull final String labelID, @Nonnull final String bucketID) {

        Arguments.checkNonEmpty(labelID, "labelID");
        Arguments.checkNonEmpty(bucketID, "bucketID");

        return addLabel(labelID, bucketID, "buckets");
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

        deleteLabel(labelID, bucketID, "buckets");
    }

    @Nonnull
    private Buckets findBuckets(@Nullable final String orgNae,
                                @Nonnull final FindOptions findOptions) {

        Call<Buckets> bucketsCall = influxDBService.findBuckets(orgNae, createQueryMap(findOptions));

        return execute(bucketsCall);
    }
}