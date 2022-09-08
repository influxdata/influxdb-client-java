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

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import com.influxdb.client.domain.Bucket;
import com.influxdb.client.domain.BucketRetentionRules;
import com.influxdb.client.domain.Buckets;
import com.influxdb.client.domain.Label;
import com.influxdb.client.domain.Organization;
import com.influxdb.client.domain.ResourceMember;
import com.influxdb.client.domain.ResourceOwner;
import com.influxdb.client.domain.User;
import com.influxdb.exceptions.NotFoundException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * @author Jakub Bednar (bednar@github) (13/09/2018 10:49)
 */
class ITBucketsApi extends AbstractITClientTest {

    private Organization organization;

    private BucketsApi bucketsApi;
    private OrganizationsApi organizationsApi;
    private UsersApi usersApi;

    @BeforeEach
    void setUp() {

        bucketsApi = influxDBClient.getBucketsApi();
        organizationsApi = influxDBClient.getOrganizationsApi();
        usersApi = influxDBClient.getUsersApi();

        organizationsApi.findOrganizations()
                .stream()
                .filter(org -> org.getName().endsWith("-IT"))
                .forEach(organizationsApi::deleteOrganization);

        organization = organizationsApi.createOrganization(generateName("Org"));

        bucketsApi.findBuckets()
                .stream()
                .filter(bucket -> bucket.getName().endsWith("-IT"))
                .forEach(bucket -> bucketsApi.deleteBucket(bucket));
    }

    @Test
    void createBucket() {

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        String bucketName = generateName("robot sensor");

        Bucket bucket = bucketsApi.createBucket(bucketName, retentionRule(), organization);

        Assertions.assertThat(bucket).isNotNull();
        Assertions.assertThat(bucket.getId()).isNotBlank();
        Assertions.assertThat(bucket.getName()).isEqualTo(bucketName);
        Assertions.assertThat(bucket.getOrgID()).isEqualTo(organization.getId());
        Assertions.assertThat(bucket.getCreatedAt()).isAfter(now);
        Assertions.assertThat(bucket.getUpdatedAt()).isAfter(now);
        Assertions.assertThat(bucket.getType()).isEqualTo(Bucket.TypeEnum.USER);
        Assertions.assertThat(bucket.getRetentionRules()).hasSize(1);
        Assertions.assertThat(bucket.getRetentionRules().get(0).getEverySeconds()).isEqualTo(3600L);
        Assertions.assertThat(bucket.getRetentionRules().get(0).getType()).isEqualTo(BucketRetentionRules.TypeEnum.EXPIRE);
        Assertions.assertThat(bucket.getLinks().getOrg()).isEqualTo("/api/v2/orgs/" + organization.getId());
        Assertions.assertThat(bucket.getLinks().getSelf()).isEqualTo("/api/v2/buckets/" + bucket.getId());
        Assertions.assertThat(bucket.getLinks().getMembers()).isEqualTo("/api/v2/buckets/" + bucket.getId() + "/members");
        Assertions.assertThat(bucket.getLinks().getOwners()).isEqualTo("/api/v2/buckets/" + bucket.getId() + "/owners");
        Assertions.assertThat(bucket.getLinks().getLabels()).isEqualTo("/api/v2/buckets/" + bucket.getId() + "/labels");
        Assertions.assertThat(bucket.getLinks().getWrite()).isEqualTo("/api/v2/write?org=" + organization.getId() + "&bucket=" + bucket.getId());
    }

    @Test
    void bucketDescription() {

        Bucket bucket = new Bucket();
        bucket.setName(generateName("robot sensor"));
        bucket.setOrgID(organization.getId());
        bucket.getRetentionRules().add(new BucketRetentionRules().everySeconds(3600));
        bucket.setDescription("description _ test");

        bucket = bucketsApi.createBucket(bucket);

        Assertions.assertThat(bucket).isNotNull();
        Assertions.assertThat(bucket.getDescription()).isEqualTo("description _ test");
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
        Assertions.assertThat(bucketByID.getRetentionRules().size()).isEqualTo(bucket.getRetentionRules().size());
        Assertions.assertThat(bucketByID.getRetentionRules()).hasSize(1);
        Assertions.assertThat(bucketByID.getLinks()).isEqualTo(bucket.getLinks());
    }

    @Test
    void findBucketByIDNull() {

        Assertions.assertThatThrownBy(() -> bucketsApi.findBucketByID("020f755c3c082000"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("HTTP status code: 404; Message: bucket not found");
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
        // 2 x created + task logs + monitoring logs for new org
        Assertions.assertThat(buckets).hasSize(size + 4);
    }

    @Test
    @Disabled
    //TODO https://github.com/influxdata/influxdb/issues/14900
    void findBucketsPaging() {

        IntStream
                .range(0, 20 - bucketsApi.findBuckets().size())
                .forEach(value -> bucketsApi.createBucket(generateName(String.valueOf(value)), retentionRule(), organization));

        FindOptions findOptions = new FindOptions();
        findOptions.setLimit(5);

        Buckets buckets = bucketsApi.findBuckets(findOptions);
        Assertions.assertThat(buckets.getBuckets()).hasSize(5);
        Assertions.assertThat(buckets.getLinks().getNext()).isEqualTo("/api/v2/buckets?descending=false&limit=5&offset=5");

        buckets = bucketsApi.findBuckets(FindOptions.create(buckets.getLinks().getNext()));
        Assertions.assertThat(buckets.getBuckets()).hasSize(5);
        Assertions.assertThat(buckets.getLinks().getNext()).isEqualTo("/api/v2/buckets?descending=false&limit=5&offset=10");

        buckets = bucketsApi.findBuckets(FindOptions.create(buckets.getLinks().getNext()));
        Assertions.assertThat(buckets.getBuckets()).hasSize(5);
        Assertions.assertThat(buckets.getLinks().getNext()).isEqualTo("/api/v2/buckets?descending=false&limit=5&offset=15");

        buckets = bucketsApi.findBuckets(FindOptions.create(buckets.getLinks().getNext()));
        Assertions.assertThat(buckets.getBuckets()).hasSize(5);
        Assertions.assertThat(buckets.getLinks().getNext()).isEqualTo("/api/v2/buckets?descending=false&limit=5&offset=20");

        buckets = bucketsApi.findBuckets(FindOptions.create(buckets.getLinks().getNext()));
        Assertions.assertThat(buckets.getBuckets()).hasSize(0);
        Assertions.assertThat(buckets.getLinks().getNext()).isNull();
    }

    @Test
    void findBucketsByOrganization() {

        Organization organization2 = organizationsApi.createOrganization(generateName("Second"));
        Assertions.assertThat(bucketsApi.findBucketsByOrg(organization2)).hasSize(2);

        bucketsApi.createBucket(generateName("robot sensor"), retentionRule(), organization2);

        Assertions.assertThat(bucketsApi.findBucketsByOrg(organization2)).hasSize(3);
    }

    @Test
    void deleteBucket() {

        Bucket createBucket = bucketsApi.createBucket(generateName("robot sensor"), retentionRule(), organization);
        Assertions.assertThat(createBucket).isNotNull();

        Bucket foundBucket = bucketsApi.findBucketByID(createBucket.getId());
        Assertions.assertThat(foundBucket).isNotNull();

        // delete task
        bucketsApi.deleteBucket(createBucket);

        Assertions.assertThatThrownBy(() -> bucketsApi.findBucketByID(createBucket.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("HTTP status code: 404; Message: bucket not found");
    }

    @Test
    @Disabled()
    //TODO https://github.com/influxdata/influxdb/issues/19518
    void updateBucket() {

        Bucket createBucket = bucketsApi.createBucket(generateName("robot sensor"), null, organization);

        createBucket.setName("Therm sensor 2000");
        createBucket.getRetentionRules().add(new BucketRetentionRules().everySeconds(3600*2));
        OffsetDateTime updatedAt = createBucket.getUpdatedAt();

        Bucket updatedBucket = bucketsApi.updateBucket(createBucket);

        Assertions.assertThat(updatedBucket).isNotNull();
        Assertions.assertThat(updatedBucket.getId()).isEqualTo(createBucket.getId());
        Assertions.assertThat(updatedBucket.getName()).isEqualTo("Therm sensor 2000");
        Assertions.assertThat(updatedBucket.getUpdatedAt()).isAfter(updatedAt);
        Assertions.assertThat(updatedBucket.getRetentionRules().get(0).getEverySeconds()).isEqualTo(3600*2);
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
        Assertions.assertThat(resourceMember.getId()).isEqualTo(user.getId());
        Assertions.assertThat(resourceMember.getName()).isEqualTo(user.getName());
        Assertions.assertThat(resourceMember.getRole()).isEqualTo(ResourceMember.RoleEnum.MEMBER);

        members = bucketsApi.getMembers(bucket);
        Assertions.assertThat(members).hasSize(1);
        Assertions.assertThat(members.get(0).getRole()).isEqualTo(ResourceMember.RoleEnum.MEMBER);
        Assertions.assertThat(members.get(0).getId()).isEqualTo(user.getId());
        Assertions.assertThat(members.get(0).getName()).isEqualTo(user.getName());

        bucketsApi.deleteMember(user, bucket);

        members = bucketsApi.getMembers(bucket);
        Assertions.assertThat(members).hasSize(0);
    }

    @Test
    @Disabled("https://github.com/influxdata/influxdb/issues/17244")
    void owner() {

        Organization organization = organizationsApi.createOrganization(generateName("Constant Pro"));

        Bucket bucket = bucketsApi.createBucket(generateName("robot sensor"), retentionRule(), organization);

        List<ResourceOwner> owners = bucketsApi.getOwners(bucket);
        Assertions.assertThat(owners).hasSize(1);
        Assertions.assertThat(owners.get(0).getName()).isEqualTo("my-user");

        User user = usersApi.createUser(generateName("Luke Health"));

        ResourceOwner resourceMember = bucketsApi.addOwner(user, bucket);
        Assertions.assertThat(resourceMember).isNotNull();
        Assertions.assertThat(resourceMember.getId()).isEqualTo(user.getId());
        Assertions.assertThat(resourceMember.getName()).isEqualTo(user.getName());
        Assertions.assertThat(resourceMember.getRole()).isEqualTo(ResourceOwner.RoleEnum.OWNER);

        owners = bucketsApi.getOwners(bucket);
        Assertions.assertThat(owners).hasSize(2);
        Assertions.assertThat(owners.get(1).getRole()).isEqualTo(ResourceOwner.RoleEnum.OWNER);
        Assertions.assertThat(owners.get(1).getId()).isEqualTo(user.getId());
        Assertions.assertThat(owners.get(1).getName()).isEqualTo(user.getName());

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

        Label label = labelsApi.createLabel(generateName("Cool Resource"), properties, organization.getId());

        List<Label> labels = bucketsApi.getLabels(bucket);
        Assertions.assertThat(labels).hasSize(0);

        Label addedLabel = bucketsApi.addLabel(label, bucket).getLabel();
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
    @Disabled("TODO https://github.com/influxdata/influxdb/issues/18409")
    void labelDeleteNotExists() {

        Bucket bucket = bucketsApi.createBucket(generateName("robot sensor"), retentionRule(), organization);

        Assertions.assertThatThrownBy(() -> bucketsApi.deleteLabel("020f755c3c082000", bucket.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("label not found");
    }

    @Test
    void cloneBucket() {

        Bucket source = bucketsApi.createBucket(generateName("robot sensor"), new BucketRetentionRules().everySeconds(3600), organization);

        String name = generateName("cloned");

        Map<String, String> properties = new HashMap<>();
        properties.put("color", "green");
        properties.put("location", "west");

        Label label = influxDBClient.getLabelsApi().createLabel(generateName("Cool Resource"), properties, organization.getId());

        bucketsApi.addLabel(label, source);

        Bucket cloned = bucketsApi.cloneBucket(name, source.getId());

        Assertions.assertThat(cloned.getName()).isEqualTo(name);
        Assertions.assertThat(cloned.getOrgID()).isEqualTo(organization.getId());
        Assertions.assertThat(cloned.getRp()).isNull();
        Assertions.assertThat(cloned.getRetentionRules()).hasSize(1);
        Assertions.assertThat(cloned.getRetentionRules().get(0).getEverySeconds()).isEqualTo(3600);
        Assertions.assertThat(cloned.getRetentionRules().get(0).getType()).isEqualTo(BucketRetentionRules.TypeEnum.EXPIRE);

        List<Label> labels = bucketsApi.getLabels(cloned);
        Assertions.assertThat(labels).hasSize(1);
        Assertions.assertThat(labels.get(0).getId()).isEqualTo(label.getId());
    }

    @Test
    void cloneBucketNotFound() {
         Assertions.assertThatThrownBy(() -> bucketsApi.cloneBucket(generateName("cloned"), "020f755c3c082000"))
                 .isInstanceOf(NotFoundException.class)
                 .hasMessage("HTTP status code: 404; Message: bucket not found");
    }
}