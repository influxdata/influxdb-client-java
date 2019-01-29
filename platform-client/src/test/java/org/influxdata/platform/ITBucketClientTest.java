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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.influxdata.platform.domain.Bucket;
import org.influxdata.platform.domain.Label;
import org.influxdata.platform.domain.Organization;
import org.influxdata.platform.domain.ResourceMember;
import org.influxdata.platform.domain.User;
import org.influxdata.platform.error.rest.NotFoundException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (13/09/2018 10:49)
 */
@RunWith(JUnitPlatform.class)
class ITBucketClientTest extends AbstractITClientTest {

    private Organization organization;

    private BucketClient bucketClient;
    private OrganizationClient organizationClient;
    private UserClient userClient;

    @BeforeEach
    void setUp() {

        bucketClient = platformClient.createBucketClient();
        organizationClient = platformClient.createOrganizationClient();
        userClient = platformClient.createUserClient();

        organization = organizationClient.createOrganization(generateName("Org"));

        bucketClient.findBuckets()
                .stream()
                .filter(bucket -> bucket.getName().endsWith("-IT"))
                .forEach(bucket -> bucketClient.deleteBucket(bucket));
    }

    @Test
    void createBucket() {

        String bucketName = generateName("robot sensor");

        Bucket bucket = bucketClient.createBucket(bucketName, retentionRule(), organization);

        Assertions.assertThat(bucket).isNotNull();
        Assertions.assertThat(bucket.getId()).isNotBlank();
        Assertions.assertThat(bucket.getName()).isEqualTo(bucketName);
        Assertions.assertThat(bucket.getOrgID()).isEqualTo(organization.getId());
        Assertions.assertThat(bucket.getOrganizationName()).isEqualTo(organization.getName());
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

        Bucket bucket = bucketClient.createBucket(bucketName, retentionRule(), organization);

        Bucket bucketByID = bucketClient.findBucketByID(bucket.getId());

        Assertions.assertThat(bucketByID).isNotNull();
        Assertions.assertThat(bucketByID.getId()).isEqualTo(bucket.getId());
        Assertions.assertThat(bucketByID.getName()).isEqualTo(bucket.getName());
        Assertions.assertThat(bucketByID.getOrgID()).isEqualTo(bucket.getOrgID());
        Assertions.assertThat(bucketByID.getOrganizationName()).isEqualTo(bucket.getOrganizationName());
        Assertions.assertThat(bucketByID.getRetentionRules().size()).isEqualTo(bucket.getRetentionRules().size());
        Assertions.assertThat(bucketByID.getRetentionRules()).hasSize(1);
        Assertions.assertThat(bucketByID.getLinks()).hasSize(bucket.getLinks().size());
    }

    @Test
    void findBucketByIDNull() {

        Bucket bucket = bucketClient.findBucketByID("020f755c3c082000");

        Assertions.assertThat(bucket).isNull();
    }

    @Test
    void findBucketByName() {

        Bucket bucket = bucketClient.findBucketByName("my-bucket");

        Assertions.assertThat(bucket).isNotNull();
        Assertions.assertThat(bucket.getId()).isNotEmpty();
        Assertions.assertThat(bucket.getName()).isEqualTo("my-bucket");
        Assertions.assertThat(bucket.getOrgID()).isEqualTo(findMyOrg().getId());
    }

    @Test
    void findBucketByNameNotFound() {

        Bucket bucket = bucketClient.findBucketByName("my-bucket-not-found");

        Assertions.assertThat(bucket).isNull();
    }

    @Test
    void findBuckets() {

        int size = bucketClient.findBuckets().size();

        bucketClient.createBucket(generateName("robot sensor"), retentionRule(), organization);

        Organization organization2 = organizationClient.createOrganization(generateName("Second"));
        bucketClient.createBucket(generateName("robot sensor"), organization2.getId());

        List<Bucket> buckets = bucketClient.findBuckets();
        Assertions.assertThat(buckets).hasSize(size + 2);
    }

    @Test
    void findBucketsByOrganization() {

        Assertions.assertThat(bucketClient.findBucketsByOrganization(organization)).hasSize(0);

        bucketClient.createBucket(generateName("robot sensor"), organization);

        Organization organization2 = organizationClient.createOrganization(generateName("Second"));
        bucketClient.createBucket(generateName("robot sensor"), retentionRule(), organization2);

        Assertions.assertThat(bucketClient.findBucketsByOrganization(organization)).hasSize(1);
    }

    @Test
    void deleteBucket() {

        Bucket createBucket = bucketClient.createBucket(generateName("robot sensor"), retentionRule(), organization);
        Assertions.assertThat(createBucket).isNotNull();

        Bucket foundBucket = bucketClient.findBucketByID(createBucket.getId());
        Assertions.assertThat(foundBucket).isNotNull();

        // delete task
        bucketClient.deleteBucket(createBucket);

        foundBucket = bucketClient.findBucketByID(createBucket.getId());
        Assertions.assertThat(foundBucket).isNull();
    }

    @Test
    void updateOrganization() {

        Bucket createBucket = bucketClient.createBucket(generateName("robot sensor"), retentionRule(), organization);
        createBucket.setName("Therm sensor 2000");
        createBucket.getRetentionRules().get(0).setEverySeconds(1000L);

        Bucket updatedBucket = bucketClient.updateBucket(createBucket);

        Assertions.assertThat(updatedBucket).isNotNull();
        Assertions.assertThat(updatedBucket.getId()).isEqualTo(createBucket.getId());
        Assertions.assertThat(updatedBucket.getName()).isEqualTo("Therm sensor 2000");
        Assertions.assertThat(updatedBucket.getRetentionRules().get(0).getEverySeconds()).isEqualTo(1000L);
    }

    @Test
    void member() {

        Organization organization = organizationClient.createOrganization(generateName("Constant Pro"));

        Bucket bucket = bucketClient.createBucket(generateName("robot sensor"), retentionRule(), organization);

        List<ResourceMember> members = bucketClient.getMembers(bucket);
        Assertions.assertThat(members).hasSize(0);

        User user = userClient.createUser(generateName("Luke Health"));

        ResourceMember resourceMember = bucketClient.addMember(user, bucket);
        Assertions.assertThat(resourceMember).isNotNull();
        Assertions.assertThat(resourceMember.getUserID()).isEqualTo(user.getId());
        Assertions.assertThat(resourceMember.getUserName()).isEqualTo(user.getName());
        Assertions.assertThat(resourceMember.getRole()).isEqualTo(ResourceMember.UserType.MEMBER);

        members = bucketClient.getMembers(bucket);
        Assertions.assertThat(members).hasSize(1);
        Assertions.assertThat(members.get(0).getRole()).isEqualTo(ResourceMember.UserType.MEMBER);
        Assertions.assertThat(members.get(0).getUserID()).isEqualTo(user.getId());
        Assertions.assertThat(members.get(0).getUserName()).isEqualTo(user.getName());

        bucketClient.deleteMember(user, bucket);

        members = bucketClient.getMembers(bucket);
        Assertions.assertThat(members).hasSize(0);
    }

    @Test
    void owner() {

        Organization organization = organizationClient.createOrganization(generateName("Constant Pro"));

        Bucket bucket = bucketClient.createBucket(generateName("robot sensor"), retentionRule(), organization);

        List<ResourceMember> owners = bucketClient.getOwners(bucket);
        Assertions.assertThat(owners).hasSize(0);

        User user = userClient.createUser(generateName("Luke Health"));

        ResourceMember resourceMember = bucketClient.addOwner(user, bucket);
        Assertions.assertThat(resourceMember).isNotNull();
        Assertions.assertThat(resourceMember.getUserID()).isEqualTo(user.getId());
        Assertions.assertThat(resourceMember.getUserName()).isEqualTo(user.getName());
        Assertions.assertThat(resourceMember.getRole()).isEqualTo(ResourceMember.UserType.OWNER);

        owners = bucketClient.getOwners(bucket);
        Assertions.assertThat(owners).hasSize(1);
        Assertions.assertThat(owners.get(0).getRole()).isEqualTo(ResourceMember.UserType.OWNER);
        Assertions.assertThat(owners.get(0).getUserID()).isEqualTo(user.getId());
        Assertions.assertThat(owners.get(0).getUserName()).isEqualTo(user.getName());

        bucketClient.deleteOwner(user, bucket);

        owners = bucketClient.getOwners(bucket);
        Assertions.assertThat(owners).hasSize(0);
    }

    @Test
    void labels() {

        LabelClient labelClient = platformClient.createLabelClient();

        Bucket bucket = bucketClient.createBucket(generateName("robot sensor"), retentionRule(), organization);

        Map<String, String> properties = new HashMap<>();
        properties.put("color", "green");
        properties.put("location", "west");

        Label label = labelClient.createLabel(generateName("Cool Resource"), properties);

        List<Label> labels = bucketClient.getLabels(bucket);
        Assertions.assertThat(labels).hasSize(0);

        Label addedLabel = bucketClient.addLabel(label, bucket);
        Assertions.assertThat(addedLabel).isNotNull();
        Assertions.assertThat(addedLabel.getId()).isEqualTo(label.getId());
        Assertions.assertThat(addedLabel.getName()).isEqualTo(label.getName());
        Assertions.assertThat(addedLabel.getProperties()).isEqualTo(label.getProperties());

        labels = bucketClient.getLabels(bucket);
        Assertions.assertThat(labels).hasSize(1);
        Assertions.assertThat(labels.get(0).getId()).isEqualTo(label.getId());
        Assertions.assertThat(labels.get(0).getName()).isEqualTo(label.getName());

        bucketClient.deleteLabel(label, bucket);

        labels = bucketClient.getLabels(bucket);
        Assertions.assertThat(labels).hasSize(0);
    }

    @Test
    void labelAddNotExists() {

        Bucket bucket = bucketClient.createBucket(generateName("robot sensor"), retentionRule(), organization);

        Assertions.assertThatThrownBy(() -> bucketClient.addLabel("020f755c3c082000", bucket.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void labelDeleteNotExists() {

        Bucket bucket = bucketClient.createBucket(generateName("robot sensor"), retentionRule(), organization);

        bucketClient.deleteLabel("020f755c3c082000", bucket.getId());
    }
}