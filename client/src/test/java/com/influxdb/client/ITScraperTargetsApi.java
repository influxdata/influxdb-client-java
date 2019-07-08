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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.influxdb.client.domain.Bucket;
import com.influxdb.client.domain.Label;
import com.influxdb.client.domain.Organization;
import com.influxdb.client.domain.ResourceMember;
import com.influxdb.client.domain.ResourceOwner;
import com.influxdb.client.domain.ScraperTargetResponse;
import com.influxdb.client.domain.User;
import com.influxdb.exceptions.NotFoundException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (22/01/2019 08:23)
 */
@RunWith(JUnitPlatform.class)
class ITScraperTargetsApi extends AbstractITClientTest {

    private ScraperTargetsApi scraperTargetsApi;
    private UsersApi usersApi;
    private Bucket bucket;
    private Organization organization;

    @BeforeEach
    void setUp() {

        scraperTargetsApi = influxDBClient.getScraperTargetsApi();
        usersApi = influxDBClient.getUsersApi();
        bucket = influxDBClient.getBucketsApi().findBucketByName("my-bucket");
        organization = findMyOrg();

        scraperTargetsApi.findScraperTargets()
                .forEach(scraperTargetResponse -> scraperTargetsApi.deleteScraperTarget(scraperTargetResponse));
    }

    @Test
    void createScraperTarget() {

        ScraperTargetResponse scraper = scraperTargetsApi.createScraperTarget(generateName("InfluxDB scraper"),
                "http://localhost:9999", bucket.getId(), organization.getId());

        Assertions.assertThat(scraper).isNotNull();
        Assertions.assertThat(scraper.getBucket()).isEqualTo("my-bucket");
        Assertions.assertThat(scraper.getOrg()).isEqualTo("my-org");
        Assertions.assertThat(scraper.getLinks().getSelf()).isEqualTo("/api/v2/scrapers/" + scraper.getId());
        Assertions.assertThat(scraper.getLinks().getMembers()).isEqualTo("/api/v2/scrapers/" + scraper.getId() + "/members");
        Assertions.assertThat(scraper.getLinks().getOwners()).isEqualTo("/api/v2/scrapers/" + scraper.getId() + "/owners");
        Assertions.assertThat(scraper.getLinks().getBucket()).isEqualTo("/api/v2/buckets/" + bucket.getId());
        Assertions.assertThat(scraper.getLinks().getOrganization()).isEqualTo("/api/v2/orgs/" + organization.getId());
    }

    @Test
    void updateScraper() {

        ScraperTargetResponse scraper = scraperTargetsApi.createScraperTarget(generateName("InfluxDB scraper"),
                "http://localhost:9999", bucket.getId(), organization.getId());

        scraper.setName("Name updated");

        ScraperTargetResponse updated = scraperTargetsApi.updateScraperTarget(scraper);

        Assertions.assertThat(updated.getName()).isEqualTo("Name updated");
    }

    @Test
    void findScrapers() {

        int size = scraperTargetsApi.findScraperTargets().size();

        scraperTargetsApi.createScraperTarget(generateName("InfluxDB scraper"),
                "http://localhost:9999", bucket.getId(), organization.getId());

        List<ScraperTargetResponse> scraperTargets = scraperTargetsApi.findScraperTargets();
        Assertions.assertThat(scraperTargets).hasSize(size + 1);
    }

    @Test
    void findScrapersByOrganization() {


        Organization org = influxDBClient.getOrganizationsApi().createOrganization(generateName("org"));

        List<ScraperTargetResponse> scraperTargets = scraperTargetsApi.findScraperTargetsByOrg(org);
        Assertions.assertThat(scraperTargets).hasSize(0);

        scraperTargetsApi.createScraperTarget(generateName("InfluxDB scraper"),
                "http://localhost:9999", bucket.getId(), org.getId());

        scraperTargets = scraperTargetsApi.findScraperTargetsByOrg(org);
        Assertions.assertThat(scraperTargets).hasSize(1);

        influxDBClient.getOrganizationsApi().deleteOrganization(org);
        scraperTargetsApi.deleteScraperTarget(scraperTargets.get(0));
    }

    @Test
    void findScraperByID() {

        ScraperTargetResponse scraper = scraperTargetsApi.createScraperTarget(generateName("InfluxDB scraper"),
                "http://localhost:9999", bucket.getId(), organization.getId());

        ScraperTargetResponse scraperByID =  scraperTargetsApi.findScraperTargetByID(scraper.getId());

        Assertions.assertThat(scraperByID).isNotNull();
        Assertions.assertThat(scraperByID.getId()).isEqualTo(scraper.getId());
        Assertions.assertThat(scraperByID.getName()).isEqualTo(scraper.getName());
    }

    @Test
    void findScraperByIDNull() {

        Assertions.assertThatThrownBy(() -> scraperTargetsApi.findScraperTargetByID("020f755c3c082000"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("scraper target is not found");
    }

    @Test
    void deleteScraper() {

        ScraperTargetResponse createdScraper = scraperTargetsApi.createScraperTarget(generateName("InfluxDB scraper"),
                "http://localhost:9999", bucket.getId(), organization.getId());
        Assertions.assertThat(createdScraper).isNotNull();

        ScraperTargetResponse foundScraper = scraperTargetsApi.findScraperTargetByID(createdScraper.getId());
        Assertions.assertThat(foundScraper).isNotNull();

        // delete scraper
        scraperTargetsApi.deleteScraperTarget(createdScraper);

        Assertions.assertThatThrownBy(() -> scraperTargetsApi.findScraperTargetByID(createdScraper.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("scraper target is not found");
    }

    @Test
    void member() {

        ScraperTargetResponse scraper =  scraperTargetsApi.createScraperTarget(generateName("InfluxDB scraper"),
                "http://localhost:9999", bucket.getId(), organization.getId());

        List<ResourceMember> members = scraperTargetsApi.getMembers(scraper);
        Assertions.assertThat(members).hasSize(0);

        User user = usersApi.createUser(generateName("Luke Health"));

        ResourceMember resourceMember = scraperTargetsApi.addMember(user, scraper);
        Assertions.assertThat(resourceMember).isNotNull();
        Assertions.assertThat(resourceMember.getId()).isEqualTo(user.getId());
        Assertions.assertThat(resourceMember.getName()).isEqualTo(user.getName());
        Assertions.assertThat(resourceMember.getRole()).isEqualTo(ResourceMember.RoleEnum.MEMBER);

        members = scraperTargetsApi.getMembers(scraper);
        Assertions.assertThat(members).hasSize(1);
        Assertions.assertThat(members.get(0).getRole()).isEqualTo(ResourceMember.RoleEnum.MEMBER);
        Assertions.assertThat(members.get(0).getId()).isEqualTo(user.getId());
        Assertions.assertThat(members.get(0).getName()).isEqualTo(user.getName());

        scraperTargetsApi.deleteMember(user, scraper);

        members = scraperTargetsApi.getMembers(scraper);
        Assertions.assertThat(members).hasSize(0);
    }

    @Test
    void owner() {

        ScraperTargetResponse scraper =  scraperTargetsApi.createScraperTarget(generateName("InfluxDB scraper"),
                "http://localhost:9999", bucket.getId(), organization.getId());

        List<ResourceOwner> owners = scraperTargetsApi.getOwners(scraper);
        Assertions.assertThat(owners).hasSize(1);

        User user = usersApi.createUser(generateName("Luke Health"));

        ResourceOwner resourceMember = scraperTargetsApi.addOwner(user, scraper);
        Assertions.assertThat(resourceMember).isNotNull();
        Assertions.assertThat(resourceMember.getId()).isEqualTo(user.getId());
        Assertions.assertThat(resourceMember.getName()).isEqualTo(user.getName());
        Assertions.assertThat(resourceMember.getRole()).isEqualTo(ResourceOwner.RoleEnum.OWNER);

        owners = scraperTargetsApi.getOwners(scraper);
        Assertions.assertThat(owners).hasSize(2);
        Assertions.assertThat(owners.get(1).getRole()).isEqualTo(ResourceOwner.RoleEnum.OWNER);
        Assertions.assertThat(owners.get(1).getId()).isEqualTo(user.getId());
        Assertions.assertThat(owners.get(1).getName()).isEqualTo(user.getName());

        scraperTargetsApi.deleteOwner(user, scraper);

        owners = scraperTargetsApi.getOwners(scraper);
        Assertions.assertThat(owners).hasSize(1);
    }

    @Test
    void labels() {

        LabelsApi labelsApi = influxDBClient.getLabelsApi();

        ScraperTargetResponse scraper =  scraperTargetsApi.createScraperTarget(generateName("InfluxDB scraper"),
                "http://localhost:9999", bucket.getId(), organization.getId());

        Map<String, String> properties = new HashMap<>();
        properties.put("color", "green");
        properties.put("location", "west");

        Label label = labelsApi.createLabel(generateName("Cool Resource"), properties, organization.getId());

        List<Label> labels = scraperTargetsApi.getLabels(scraper);
        Assertions.assertThat(labels).hasSize(0);

        Label addedLabel = scraperTargetsApi.addLabel(label, scraper).getLabel();
        Assertions.assertThat(addedLabel).isNotNull();
        Assertions.assertThat(addedLabel.getId()).isEqualTo(label.getId());
        Assertions.assertThat(addedLabel.getName()).isEqualTo(label.getName());
        Assertions.assertThat(addedLabel.getProperties()).isEqualTo(label.getProperties());

        labels = scraperTargetsApi.getLabels(scraper);
        Assertions.assertThat(labels).hasSize(1);
        Assertions.assertThat(labels.get(0).getId()).isEqualTo(label.getId());
        Assertions.assertThat(labels.get(0).getName()).isEqualTo(label.getName());

        scraperTargetsApi.deleteLabel(label, scraper);

        labels = scraperTargetsApi.getLabels(scraper);
        Assertions.assertThat(labels).hasSize(0);
    }

    @Test
    void cloneScraperTarget() {

        ScraperTargetResponse source = scraperTargetsApi.createScraperTarget(generateName("InfluxDB scraper"),
                "http://localhost:9999", bucket.getId(), organization.getId());

        String name = generateName("cloned");

        Map<String, String> properties = new HashMap<>();
        properties.put("color", "green");
        properties.put("location", "west");

        Label label = influxDBClient.getLabelsApi().createLabel(generateName("Cool Resource"), properties, organization.getId());

        scraperTargetsApi.addLabel(label, source);

        ScraperTargetResponse cloned = scraperTargetsApi.cloneScraperTarget(name, source.getId());

        Assertions.assertThat(cloned.getName()).isEqualTo(name);
        Assertions.assertThat(cloned.getType()).isEqualTo(source.getType());
        Assertions.assertThat(cloned.getUrl()).isEqualTo(source.getUrl());
        Assertions.assertThat(cloned.getOrgID()).isEqualTo(source.getOrgID());
        Assertions.assertThat(cloned.getBucketID()).isEqualTo(source.getBucketID());

        List<Label> labels = scraperTargetsApi.getLabels(cloned);
        Assertions.assertThat(labels).hasSize(1);
        Assertions.assertThat(labels.get(0).getId()).isEqualTo(label.getId());
    }

    @Test
    void cloneScraperTargetNotFound() {
        Assertions.assertThatThrownBy(() -> scraperTargetsApi.cloneScraperTarget(generateName("cloned"), "020f755c3c082000"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("scraper target is not found");
    }
}