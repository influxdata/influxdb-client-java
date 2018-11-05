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

import org.influxdata.platform.domain.Bucket;
import org.influxdata.platform.domain.Organization;
import org.influxdata.platform.domain.ResourceType;
import org.influxdata.platform.domain.User;
import org.influxdata.platform.domain.UserResourceMapping;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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

        super.setUp();

        bucketClient = platformService.createBucketClient();
        organizationClient = platformService.createOrganizationClient();
        userClient = platformService.createUserClient();

        organization = organizationClient.createOrganization(generateName("Org"));
    }

    @Test
    void createBu3cket() {

        String bucketName = generateName("robot sensor");

        Bucket bucket = bucketClient.createBucket(bucketName, retentionRule(), organization);

        Assertions.assertThat(bucket).isNotNull();
        Assertions.assertThat(bucket.getId()).isNotBlank();
        Assertions.assertThat(bucket.getName()).isEqualTo(bucketName);
        Assertions.assertThat(bucket.getOrganizationID()).isEqualTo(organization.getId());
        Assertions.assertThat(bucket.getOrganizationName()).isEqualTo(organization.getName());
        Assertions.assertThat(bucket.getRetentionRules()).hasSize(1);
        Assertions.assertThat(bucket.getRetentionRules().get(0).getEverySeconds()).isEqualTo(3600L);
        Assertions.assertThat(bucket.getRetentionRules().get(0).getType()).isEqualTo("expire");
        Assertions.assertThat(bucket.getLinks()).hasSize(3);
        Assertions.assertThat(bucket.getLinks()).hasEntrySatisfying("org", value -> Assertions.assertThat(value).isEqualTo("/api/v2/orgs/" + organization.getId()));
        Assertions.assertThat(bucket.getLinks()).hasEntrySatisfying("self", value -> Assertions.assertThat(value).isEqualTo("/api/v2/buckets/" + bucket.getId()));
        Assertions.assertThat(bucket.getLinks()).hasEntrySatisfying("log", value -> Assertions.assertThat(value).isEqualTo("/api/v2/buckets/" + bucket.getId() + "/log"));
    }

    @Test
    void findBucketByID() {

        String bucketName = generateName("robot sensor");

        Bucket bucket = bucketClient.createBucket(bucketName, retentionRule(), organization);

        Bucket bucketByID = bucketClient.findBucketByID(bucket.getId());

        Assertions.assertThat(bucketByID).isNotNull();
        Assertions.assertThat(bucketByID.getId()).isEqualTo(bucket.getId());
        Assertions.assertThat(bucketByID.getName()).isEqualTo(bucket.getName());
        Assertions.assertThat(bucketByID.getOrganizationID()).isEqualTo(bucket.getOrganizationID());
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
    void findBuckets() {

        int size = bucketClient.findBuckets().size();

        bucketClient.createBucket(generateName("robot sensor"), retentionRule(), organization);

        Organization organization2 = organizationClient.createOrganization(generateName("Second"));
        bucketClient.createBucket(generateName("robot sensor"), organization2.getName());

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

        List<UserResourceMapping> members = bucketClient.getMembers(bucket);
        Assertions.assertThat(members).hasSize(0);

        User user = userClient.createUser(generateName("Luke Health"));

        UserResourceMapping userResourceMapping = bucketClient.addMember(user, bucket);
        Assertions.assertThat(userResourceMapping).isNotNull();
        Assertions.assertThat(userResourceMapping.getResourceID()).isEqualTo(bucket.getId());
        Assertions.assertThat(userResourceMapping.getResourceType()).isEqualTo(ResourceType.BUCKET_RESOURCE_TYPE);
        Assertions.assertThat(userResourceMapping.getUserID()).isEqualTo(user.getId());
        Assertions.assertThat(userResourceMapping.getUserType()).isEqualTo(UserResourceMapping.UserType.MEMBER);

        members = bucketClient.getMembers(bucket);
        Assertions.assertThat(members).hasSize(1);
        Assertions.assertThat(members.get(0).getResourceID()).isEqualTo(bucket.getId());
        Assertions.assertThat(members.get(0).getResourceType()).isEqualTo(ResourceType.BUCKET_RESOURCE_TYPE);
        Assertions.assertThat(members.get(0).getUserID()).isEqualTo(user.getId());
        Assertions.assertThat(members.get(0).getUserType()).isEqualTo(UserResourceMapping.UserType.MEMBER);

        bucketClient.deleteMember(user, bucket);

        members = bucketClient.getMembers(bucket);
        Assertions.assertThat(members).hasSize(0);
    }

    //TODO userType will be used
    @Test
    @Disabled
    void owner() {

        Organization organization = organizationClient.createOrganization(generateName("Constant Pro"));

        Bucket bucket = bucketClient.createBucket(generateName("robot sensor"), retentionRule(), organization);

        List<UserResourceMapping> owners = bucketClient.getOwners(bucket);
        Assertions.assertThat(owners).hasSize(0);

        User user = userClient.createUser(generateName("Luke Health"));

        UserResourceMapping userResourceMapping = bucketClient.addOwner(user, bucket);
        Assertions.assertThat(userResourceMapping).isNotNull();
        Assertions.assertThat(userResourceMapping.getResourceID()).isEqualTo(bucket.getId());
        Assertions.assertThat(userResourceMapping.getResourceType()).isEqualTo(ResourceType.BUCKET_RESOURCE_TYPE);
        Assertions.assertThat(userResourceMapping.getUserID()).isEqualTo(user.getId());
        Assertions.assertThat(userResourceMapping.getUserType()).isEqualTo(UserResourceMapping.UserType.OWNER);

        owners = bucketClient.getOwners(bucket);
        Assertions.assertThat(owners).hasSize(1);
        Assertions.assertThat(owners.get(0).getResourceID()).isEqualTo(bucket.getId());
        Assertions.assertThat(owners.get(0).getResourceType()).isEqualTo(ResourceType.BUCKET_RESOURCE_TYPE);
        Assertions.assertThat(owners.get(0).getUserID()).isEqualTo(user.getId());
        Assertions.assertThat(owners.get(0).getUserType()).isEqualTo(UserResourceMapping.UserType.OWNER);

        bucketClient.deleteOwner(user, bucket);

        owners = bucketClient.getOwners(bucket);
        Assertions.assertThat(owners).hasSize(0);
    }
}