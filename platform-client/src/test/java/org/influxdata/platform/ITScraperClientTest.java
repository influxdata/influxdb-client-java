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
import org.influxdata.platform.domain.ResourceMember;
import org.influxdata.platform.domain.ScraperTarget;
import org.influxdata.platform.domain.ScraperTargetResponse;
import org.influxdata.platform.domain.User;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (22/01/2019 08:23)
 */
@RunWith(JUnitPlatform.class)
class ITScraperClientTest extends AbstractITClientTest {

    private ScraperClient scraperClient;
    private UserClient userClient;
    private Bucket bucket;

    @BeforeEach
    void setUp() {

        scraperClient = platformClient.createScraperClient();
        userClient = platformClient.createUserClient();
        bucket = platformClient.createBucketClient().findBucketByName("my-bucket");
    }

    @Test
    void createScraperTarget() {

        Organization organization = findMyOrg();
        ScraperTargetResponse scraper = scraperClient.createScraperTarget(generateName("InfluxDB scraper"),
                "http://localhost:9999", bucket.getId(), organization.getId());

        Assertions.assertThat(scraper).isNotNull();
        Assertions.assertThat(scraper.getBucketName()).isEqualTo("my-bucket");
        Assertions.assertThat(scraper.getOrganizationName()).isEqualTo("my-org");
        Assertions.assertThat(scraper.getLinks())
                .hasSize(3)
                .hasEntrySatisfying("bucket", value -> Assertions.assertThat(value).isEqualTo("/api/v2/buckets/" + bucket.getId()))
                .hasEntrySatisfying("organization", value -> Assertions.assertThat(value).isEqualTo("/api/v2/orgs/" + organization.getId()))
                .hasEntrySatisfying("self", value -> Assertions.assertThat(value).isEqualTo("/api/v2/scrapers/" + scraper.getId()));
    }

    @Test
    void updateScraper() {

        ScraperTarget scraper = scraperClient.createScraperTarget(generateName("InfluxDB scraper"),
                "http://localhost:9999", bucket.getId(), findMyOrg().getId());

        scraper.setName("Name updated");

        ScraperTargetResponse updated = scraperClient.updateScraperTarget(scraper);

        Assertions.assertThat(updated.getName()).isEqualTo("Name updated");
    }

    @Test
    void findScrapers() {

        int size = scraperClient.findScraperTargets().size();

        scraperClient.createScraperTarget(generateName("InfluxDB scraper"),
                "http://localhost:9999", bucket.getId(), findMyOrg().getId());

        List<ScraperTargetResponse> scraperTargets = scraperClient.findScraperTargets();
        Assertions.assertThat(scraperTargets).hasSize(size + 1);
    }

    @Test
    void findScraperByID() {

        ScraperTargetResponse scraper = scraperClient.createScraperTarget(generateName("InfluxDB scraper"),
                "http://localhost:9999", bucket.getId(), findMyOrg().getId());

        ScraperTargetResponse scraperByID =  scraperClient.findScraperTargetByID(scraper.getId());

        Assertions.assertThat(scraperByID).isNotNull();
        Assertions.assertThat(scraperByID.getId()).isEqualTo(scraper.getId());
        Assertions.assertThat(scraperByID.getName()).isEqualTo(scraper.getName());
    }

    @Test
    void findScraperByIDNull() {

        ScraperTargetResponse scraper = scraperClient.findScraperTargetByID("020f755c3c082000");

        Assertions.assertThat(scraper).isNull();
    }

    @Test
    void deleteScraper() {

        ScraperTargetResponse createdScraper = scraperClient.createScraperTarget(generateName("InfluxDB scraper"),
                "http://localhost:9999", bucket.getId(), findMyOrg().getId());
        Assertions.assertThat(createdScraper).isNotNull();

        ScraperTargetResponse foundScraper = scraperClient.findScraperTargetByID(createdScraper.getId());
        Assertions.assertThat(foundScraper).isNotNull();

        // delete scraper
        scraperClient.deleteScraperTarget(createdScraper);

        foundScraper = scraperClient.findScraperTargetByID(createdScraper.getId());
        Assertions.assertThat(foundScraper).isNull();
    }

    @Test
    void member() {

        ScraperTarget scraper =  scraperClient.createScraperTarget(generateName("InfluxDB scraper"),
                "http://localhost:9999", bucket.getId(), findMyOrg().getId());

        List<ResourceMember> members = scraperClient.getMembers(scraper);
        Assertions.assertThat(members).hasSize(0);

        User user = userClient.createUser(generateName("Luke Health"));

        ResourceMember resourceMember = scraperClient.addMember(user, scraper);
        Assertions.assertThat(resourceMember).isNotNull();
        Assertions.assertThat(resourceMember.getUserID()).isEqualTo(user.getId());
        Assertions.assertThat(resourceMember.getUserName()).isEqualTo(user.getName());
        Assertions.assertThat(resourceMember.getRole()).isEqualTo(ResourceMember.UserType.MEMBER);

        members = scraperClient.getMembers(scraper);
        Assertions.assertThat(members).hasSize(1);
        Assertions.assertThat(members.get(0).getRole()).isEqualTo(ResourceMember.UserType.MEMBER);
        Assertions.assertThat(members.get(0).getUserID()).isEqualTo(user.getId());
        Assertions.assertThat(members.get(0).getUserName()).isEqualTo(user.getName());

        scraperClient.deleteMember(user, scraper);

        members = scraperClient.getMembers(scraper);
        Assertions.assertThat(members).hasSize(0);
    }

    @Test
    void owner() {

        ScraperTarget scraper =  scraperClient.createScraperTarget(generateName("InfluxDB scraper"),
                "http://localhost:9999", bucket.getId(), findMyOrg().getId());

        List<ResourceMember> owners = scraperClient.getOwners(scraper);
        Assertions.assertThat(owners).hasSize(1);

        User user = userClient.createUser(generateName("Luke Health"));

        ResourceMember resourceMember = scraperClient.addOwner(user, scraper);
        Assertions.assertThat(resourceMember).isNotNull();
        Assertions.assertThat(resourceMember.getUserID()).isEqualTo(user.getId());
        Assertions.assertThat(resourceMember.getUserName()).isEqualTo(user.getName());
        Assertions.assertThat(resourceMember.getRole()).isEqualTo(ResourceMember.UserType.OWNER);

        owners = scraperClient.getOwners(scraper);
        Assertions.assertThat(owners).hasSize(2);
        Assertions.assertThat(owners.get(1).getRole()).isEqualTo(ResourceMember.UserType.OWNER);
        Assertions.assertThat(owners.get(1).getUserID()).isEqualTo(user.getId());
        Assertions.assertThat(owners.get(1).getUserName()).isEqualTo(user.getName());

        scraperClient.deleteOwner(user, scraper);

        owners = scraperClient.getOwners(scraper);
        Assertions.assertThat(owners).hasSize(1);
    }
}