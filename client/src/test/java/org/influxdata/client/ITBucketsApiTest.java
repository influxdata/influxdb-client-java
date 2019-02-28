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
package org.influxdata.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.influxdata.client.domain.Bucket;
import org.influxdata.client.domain.Buckets;
import org.influxdata.client.domain.FindOptions;
import org.influxdata.client.domain.Label;
import org.influxdata.client.domain.OperationLogEntries;
import org.influxdata.client.domain.OperationLogEntry;
import org.influxdata.client.domain.Organization;
import org.influxdata.client.domain.ResourceMember;
import org.influxdata.client.domain.User;
import org.influxdata.exceptions.NotFoundException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (13/09/2018 10:49)
 */
@RunWith(JUnitPlatform.class)
class ITBucketsApiTest extends AbstractITClientTest {

    private Organization organization;

    private BucketsApi bucketsApi;
    private OrganizationsApi organizationsApi;
    private UsersApi usersApi;

    @BeforeEach
    void setUp() {

        bucketsApi = influxDBClient.getBucketsApi();
        organizationsApi = influxDBClient.getOrganizationsApi();
        usersApi = influxDBClient.getUsersApi();

        organization = organizationsApi.createOrganization(generateName("Org"));

        bucketsApi.findBuckets()
                .stream()
                .filter(bucket -> bucket.getName().endsWith("-IT"))
                .forEach(bucket -> bucketsApi.deleteBucket(bucket));
    }

    @Test
    void createBucket() {

        String bucketName = generateName("robot sensor");

        Bucket bucket = bucketsApi.createBucket(bucketName, retentionRule(), organization);

        Assertions.assertThat(bucket).isNotNull();
        Assertions.assertThat(bucket.getId()).isNotBlank();
        Assertions.assertThat(bucket.getName()).isEqualTo(bucketName);
        Assertions.assertThat(bucket.getOrgID()).isEqualTo(organization.getId());
        Assertions.assertThat(bucket.getOrgName()).isEqualTo(organization.getName());
        Assertions.assertThat(bucket.getRetentionRules()).hasSize(1);
        Assertions.assertThat(bucket.getRetentionRules().get(0).getEverySeconds()).isEqualTo(3600L);
        Assertions.assertThat(bucket.getRetentionRules().get(0).getType()).isEqualTo("expire");
        Assertions.assertThat(bucket.getLinks()).hasSize(4);
        Assertions.assertThat(bucket.getLinks()).hasEntrySatisfying("org", value -> Assertions.assertThat(value).isEqualTo("/api/v2/orgs/" + organization.getId()));
        Assertions.assertThat(bucket.getLinks()).hasEntrySatisfying("self", value -> Assertions.assertThat(value).isEqualTo("/api/v2/buckets/" + bucket.getId()));
        Assertions.assertThat(bucket.getLinks()).hasEntrySatisfying("log", value -> Assertions.assertThat(value).isEqualTo("/api/v2/buckets/" + bucket.getId() + "/log"));
        Assertions.assertThat(bucket.getLinks()).hasEntrySatisfying("labels", value -> Assertions.assertThat(value).isEqualTo("/api/v2/buckets/" + bucket.getId() + "/labels"));
    }

    @Test
    void findBucketByID() {

        String bucketName = generateName("robot sensor");

        Bucket bucket = bucketsApi.createBucket(bucketName, retentionRule(), organization);

        Bucket bucketByID = bucketsApi.findBucketByID(bucket.getId());

        Assertions.assertThat(bucketByID).isNotNull();
        Assertions.assertThat(bucketByID.getId()).isEqualTo(bucket.getId());
        Assertions.assertThat(bucketByID.getName()).isEqualTo(bucket.getName());
        Assertions.assertThat(bucketByID.getOrgID()).isEqualTo(bucket.getOrgID());
        Assertions.assertThat(bucketByID.getOrgName()).isEqualTo(bucket.getOrgName());
        Assertions.assertThat(bucketByID.getRetentionRules().size()).isEqualTo(bucket.getRetentionRules().size());
        Assertions.assertThat(bucketByID.getRetentionRules()).hasSize(1);
        Assertions.assertThat(bucketByID.getLinks()).hasSize(bucket.getLinks().size());
    }

    @Test
    void findBucketByIDNull() {

        Bucket bucket = bucketsApi.findBucketByID("020f755c3c082000");

        Assertions.assertThat(bucket).isNull();
    }

    @Test
    void findBucketByName() {

        Bucket bucket = bucketsApi.findBucketByName("my-bucket");

        Assertions.assertThat(bucket).isNotNull();
        Assertions.assertThat(bucket.getId()).isNotEmpty();
        Assertions.assertThat(bucket.getName()).isEqualTo("my-bucket");
        Assertions.assertThat(bucket.getOrgID()).isEqualTo(findMyOrg().getId());
    }

    @Test
    void findBucketByNameNotFound() {

        Bucket bucket = bucketsApi.findBucketByName("my-bucket-not-found");

        Assertions.assertThat(bucket).isNull();
    }

    @Test
    void findBuckets() {

        int size = bucketsApi.findBuckets().size();

        bucketsApi.createBucket(generateName("robot sensor"), retentionRule(), organization);

        Organization organization2 = organizationsApi.createOrganization(generateName("Second"));
        bucketsApi.createBucket(generateName("robot sensor"), organization2.getId());

        List<Bucket> buckets = bucketsApi.findBuckets();
        Assertions.assertThat(buckets).hasSize(size + 2);
    }

    @Test
    void findBucketsPaging() {

        IntStream
                .range(0, 20 - bucketsApi.findBuckets().size())
                .forEach(value -> bucketsApi.createBucket(generateName(String.valueOf(value)), retentionRule(), organization));

        FindOptions findOptions = new FindOptions();
        findOptions.setLimit(5);

        Buckets buckets = bucketsApi.findBuckets(findOptions);
        Assertions.assertThat(buckets.getBuckets()).hasSize(5);
        Assertions.assertThat(buckets.getNextPage()).isNotNull();

        buckets = bucketsApi.findBuckets(buckets.getNextPage());
        Assertions.assertThat(buckets.getBuckets()).hasSize(5);
        Assertions.assertThat(buckets.getNextPage()).isNotNull();

        buckets = bucketsApi.findBuckets(buckets.getNextPage());
        Assertions.assertThat(buckets.getBuckets()).hasSize(5);
        Assertions.assertThat(buckets.getNextPage()).isNotNull();

        buckets = bucketsApi.findBuckets(buckets.getNextPage());
        Assertions.assertThat(buckets.getBuckets()).hasSize(5);
        Assertions.assertThat(buckets.getNextPage()).isNotNull();

        buckets = bucketsApi.findBuckets(buckets.getNextPage());
        Assertions.assertThat(buckets.getBuckets()).hasSize(0);
        Assertions.assertThat(buckets.getNextPage()).isNull();
    }

    @Test
    void findBucketsByOrganization() {

        Assertions.assertThat(bucketsApi.findBucketsByOrg(organization)).hasSize(0);

        bucketsApi.createBucket(generateName("robot sensor"), organization);

        Organization organization2 = organizationsApi.createOrganization(generateName("Second"));
        bucketsApi.createBucket(generateName("robot sensor"), retentionRule(), organization2);

        Assertions.assertThat(bucketsApi.findBucketsByOrg(organization)).hasSize(1);
    }

    @Test
    void deleteBucket() {

        Bucket createBucket = bucketsApi.createBucket(generateName("robot sensor"), retentionRule(), organization);
        Assertions.assertThat(createBucket).isNotNull();

        Bucket foundBucket = bucketsApi.findBucketByID(createBucket.getId());
        Assertions.assertThat(foundBucket).isNotNull();

        // delete task
        bucketsApi.deleteBucket(createBucket);

        foundBucket = bucketsApi.findBucketByID(createBucket.getId());
        Assertions.assertThat(foundBucket).isNull();
    }

    @Test
    void updateOrganization() {

        Bucket createBucket = bucketsApi.createBucket(generateName("robot sensor"), retentionRule(), organization);
        createBucket.setName("Therm sensor 2000");
        createBucket.getRetentionRules().get(0).setEverySeconds(1000L);

        Bucket updatedBucket = bucketsApi.updateBucket(createBucket);

        Assertions.assertThat(updatedBucket).isNotNull();
        Assertions.assertThat(updatedBucket.getId()).isEqualTo(createBucket.getId());
        Assertions.assertThat(updatedBucket.getName()).isEqualTo("Therm sensor 2000");
        Assertions.assertThat(updatedBucket.getRetentionRules().get(0).getEverySeconds()).isEqualTo(1000L);
    }

    @Test
    void member() {

        Organization organization = organizationsApi.createOrganization(generateName("Constant Pro"));

        Bucket bucket = bucketsApi.createBucket(generateName("robot sensor"), retentionRule(), organization);

        List<ResourceMember> members = bucketsApi.getMembers(bucket);
        Assertions.assertThat(members).hasSize(0);

        User user = usersApi.createUser(generateName("Luke Health"));

        ResourceMember resourceMember = bucketsApi.addMember(user, bucket);
        Assertions.assertThat(resourceMember).isNotNull();
        Assertions.assertThat(resourceMember.getUserID()).isEqualTo(user.getId());
        Assertions.assertThat(resourceMember.getUserName()).isEqualTo(user.getName());
        Assertions.assertThat(resourceMember.getRole()).isEqualTo(ResourceMember.UserType.MEMBER);

        members = bucketsApi.getMembers(bucket);
        Assertions.assertThat(members).hasSize(1);
        Assertions.assertThat(members.get(0).getRole()).isEqualTo(ResourceMember.UserType.MEMBER);
        Assertions.assertThat(members.get(0).getUserID()).isEqualTo(user.getId());
        Assertions.assertThat(members.get(0).getUserName()).isEqualTo(user.getName());

        bucketsApi.deleteMember(user, bucket);

        members = bucketsApi.getMembers(bucket);
        Assertions.assertThat(members).hasSize(0);
    }

    @Test
    void owner() {

        Organization organization = organizationsApi.createOrganization(generateName("Constant Pro"));

        Bucket bucket = bucketsApi.createBucket(generateName("robot sensor"), retentionRule(), organization);

        List<ResourceMember> owners = bucketsApi.getOwners(bucket);
        Assertions.assertThat(owners).hasSize(1);
        Assertions.assertThat(owners.get(0).getUserName()).isEqualTo("my-user");

        User user = usersApi.createUser(generateName("Luke Health"));

        ResourceMember resourceMember = bucketsApi.addOwner(user, bucket);
        Assertions.assertThat(resourceMember).isNotNull();
        Assertions.assertThat(resourceMember.getUserID()).isEqualTo(user.getId());
        Assertions.assertThat(resourceMember.getUserName()).isEqualTo(user.getName());
        Assertions.assertThat(resourceMember.getRole()).isEqualTo(ResourceMember.UserType.OWNER);

        owners = bucketsApi.getOwners(bucket);
        Assertions.assertThat(owners).hasSize(2);
        Assertions.assertThat(owners.get(1).getRole()).isEqualTo(ResourceMember.UserType.OWNER);
        Assertions.assertThat(owners.get(1).getUserID()).isEqualTo(user.getId());
        Assertions.assertThat(owners.get(1).getUserName()).isEqualTo(user.getName());

        bucketsApi.deleteOwner(user, bucket);

        owners = bucketsApi.getOwners(bucket);
        Assertions.assertThat(owners).hasSize(1);
    }

    @Test
    void labels() {

        LabelsApi labelsApi = influxDBClient.getLabelsApi();

        Bucket bucket = bucketsApi.createBucket(generateName("robot sensor"), retentionRule(), organization);

        Map<String, String> properties = new HashMap<>();
        properties.put("color", "green");
        properties.put("location", "west");

        Label label = labelsApi.createLabel(generateName("Cool Resource"), properties);

        List<Label> labels = bucketsApi.getLabels(bucket);
        Assertions.assertThat(labels).hasSize(0);

        Label addedLabel = bucketsApi.addLabel(label, bucket);
        Assertions.assertThat(addedLabel).isNotNull();
        Assertions.assertThat(addedLabel.getId()).isEqualTo(label.getId());
        Assertions.assertThat(addedLabel.getName()).isEqualTo(label.getName());
        Assertions.assertThat(addedLabel.getProperties()).isEqualTo(label.getProperties());

        labels = bucketsApi.getLabels(bucket);
        Assertions.assertThat(labels).hasSize(1);
        Assertions.assertThat(labels.get(0).getId()).isEqualTo(label.getId());
        Assertions.assertThat(labels.get(0).getName()).isEqualTo(label.getName());

        bucketsApi.deleteLabel(label, bucket);

        labels = bucketsApi.getLabels(bucket);
        Assertions.assertThat(labels).hasSize(0);
    }

    @Test
    void labelAddNotExists() {

        Bucket bucket = bucketsApi.createBucket(generateName("robot sensor"), retentionRule(), organization);

        Assertions.assertThatThrownBy(() -> bucketsApi.addLabel("020f755c3c082000", bucket.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void labelDeleteNotExists() {

        Bucket bucket = bucketsApi.createBucket(generateName("robot sensor"), retentionRule(), organization);

        bucketsApi.deleteLabel("020f755c3c082000", bucket.getId());
    }

    @Test
    void findBucketLogs() {

        Bucket bucket = bucketsApi.createBucket(generateName("robot sensor"), retentionRule(), organization);

        List<OperationLogEntry> logs = bucketsApi.findBucketLogs(bucket);

        Assertions.assertThat(logs).hasSize(1);
        Assertions.assertThat(logs.get(0).getDescription()).isEqualTo("Bucket Created");
    }

    @Test
    void findBucketLogsPaging() {

        Bucket bucket = bucketsApi.createBucket(generateName("robot sensor"), retentionRule(), organization);

        IntStream
                .range(0, 19)
                .forEach(value -> {

                    bucket.setName(value + "_" + bucket.getName());
                    bucketsApi.updateBucket(bucket);
                });

        List<OperationLogEntry> logs = bucketsApi.findBucketLogs(bucket);

        Assertions.assertThat(logs).hasSize(20);
        Assertions.assertThat(logs.get(0).getDescription()).isEqualTo("Bucket Created");
        Assertions.assertThat(logs.get(19).getDescription()).isEqualTo("Bucket Updated");

        FindOptions findOptions = new FindOptions();
        findOptions.setLimit(5);
        findOptions.setOffset(0);

        OperationLogEntries entries = bucketsApi.findBucketLogs(bucket, findOptions);
        
        Assertions.assertThat(entries.getLogs()).hasSize(5);
        Assertions.assertThat(entries.getLogs().get(0).getDescription()).isEqualTo("Bucket Created");
        Assertions.assertThat(entries.getLogs().get(1).getDescription()).isEqualTo("Bucket Updated");
        Assertions.assertThat(entries.getLogs().get(2).getDescription()).isEqualTo("Bucket Updated");
        Assertions.assertThat(entries.getLogs().get(3).getDescription()).isEqualTo("Bucket Updated");
        Assertions.assertThat(entries.getLogs().get(4).getDescription()).isEqualTo("Bucket Updated");

        //TODO isNotNull FindOptions also in Log API?
        findOptions.setOffset(findOptions.getOffset() + 5);
        Assertions.assertThat(entries.getNextPage()).isNull();

        entries = bucketsApi.findBucketLogs(bucket, findOptions);
        Assertions.assertThat(entries.getLogs()).hasSize(5);
        Assertions.assertThat(entries.getLogs().get(0).getDescription()).isEqualTo("Bucket Updated");
        Assertions.assertThat(entries.getLogs().get(1).getDescription()).isEqualTo("Bucket Updated");
        Assertions.assertThat(entries.getLogs().get(2).getDescription()).isEqualTo("Bucket Updated");
        Assertions.assertThat(entries.getLogs().get(3).getDescription()).isEqualTo("Bucket Updated");
        Assertions.assertThat(entries.getLogs().get(4).getDescription()).isEqualTo("Bucket Updated");

        findOptions.setOffset(findOptions.getOffset() + 5);
        Assertions.assertThat(entries.getNextPage()).isNull();

        entries = bucketsApi.findBucketLogs(bucket, findOptions);
        Assertions.assertThat(entries.getLogs().get(0).getDescription()).isEqualTo("Bucket Updated");
        Assertions.assertThat(entries.getLogs().get(1).getDescription()).isEqualTo("Bucket Updated");
        Assertions.assertThat(entries.getLogs().get(2).getDescription()).isEqualTo("Bucket Updated");
        Assertions.assertThat(entries.getLogs().get(3).getDescription()).isEqualTo("Bucket Updated");
        Assertions.assertThat(entries.getLogs().get(4).getDescription()).isEqualTo("Bucket Updated");

        findOptions.setOffset(findOptions.getOffset() + 5);
        Assertions.assertThat(entries.getNextPage()).isNull();

        entries = bucketsApi.findBucketLogs(bucket, findOptions);
        Assertions.assertThat(entries.getLogs()).hasSize(5);
        Assertions.assertThat(entries.getLogs().get(0).getDescription()).isEqualTo("Bucket Updated");
        Assertions.assertThat(entries.getLogs().get(1).getDescription()).isEqualTo("Bucket Updated");
        Assertions.assertThat(entries.getLogs().get(2).getDescription()).isEqualTo("Bucket Updated");
        Assertions.assertThat(entries.getLogs().get(3).getDescription()).isEqualTo("Bucket Updated");
        Assertions.assertThat(entries.getLogs().get(4).getDescription()).isEqualTo("Bucket Updated");

        findOptions.setOffset(findOptions.getOffset() + 5);
        Assertions.assertThat(entries.getNextPage()).isNull();

        entries = bucketsApi.findBucketLogs(bucket, findOptions);
        Assertions.assertThat(entries.getLogs()).hasSize(0);
        Assertions.assertThat(entries.getNextPage()).isNull();

        // order
        findOptions = new FindOptions();
        findOptions.setDescending(false);

        entries = bucketsApi.findBucketLogs(bucket, findOptions);

        Assertions.assertThat(entries.getLogs()).hasSize(20);
        Assertions.assertThat(entries.getLogs().get(19).getDescription()).isEqualTo("Bucket Updated");
        Assertions.assertThat(entries.getLogs().get(0).getDescription()).isEqualTo("Bucket Created");
    }

    @Test
    void findBucketLogsNotFound() {

        List<OperationLogEntry> bucketLogs = bucketsApi.findBucketLogs("020f755c3c082000");

        Assertions.assertThat(bucketLogs).isEmpty();
    }

    @Test
    void findBucketLogsFindOptionsNotFound() {

        OperationLogEntries entries = bucketsApi.findBucketLogs("020f755c3c082000", new FindOptions());

        Assertions.assertThat(entries).isNotNull();
        Assertions.assertThat(entries.getLogs()).isEmpty();
    }

    @Test
    void cloneBucket() {

        Bucket source = bucketsApi.createBucket(generateName("robot sensor"), retentionRule(), organization);

        String name = generateName("cloned");

        Map<String, String> properties = new HashMap<>();
        properties.put("color", "green");
        properties.put("location", "west");

        Label label = influxDBClient.getLabelsApi().createLabel(generateName("Cool Resource"), properties);

        bucketsApi.addLabel(label, source);

        Bucket cloned = bucketsApi.cloneBucket(name, source.getId());

        Assertions.assertThat(cloned.getName()).isEqualTo(name);
        Assertions.assertThat(cloned.getOrgID()).isEqualTo(organization.getId());
        Assertions.assertThat(cloned.getOrgName()).isEqualTo(organization.getName());
        Assertions.assertThat(cloned.getRetentionPolicyName()).isNull();
        Assertions.assertThat(cloned.getRetentionRules()).hasSize(1);
        Assertions.assertThat(cloned.getRetentionRules().get(0).getEverySeconds()).isEqualTo(3600);
        Assertions.assertThat(cloned.getRetentionRules().get(0).getType()).isEqualTo("expire");

        List<Label> labels = bucketsApi.getLabels(cloned);
        Assertions.assertThat(labels).hasSize(1);
        Assertions.assertThat(labels.get(0).getId()).isEqualTo(label.getId());
    }

    @Test
    void cloneBucketNotFound() {
         Assertions.assertThatThrownBy(() -> bucketsApi.cloneBucket(generateName("cloned"), "020f755c3c082000"))
                 .isInstanceOf(IllegalStateException.class)
                 .hasMessage("NotFound Bucket with ID: 020f755c3c082000");
    }
}