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
import org.influxdata.platform.domain.ScraperTarget;
import org.influxdata.platform.domain.ScraperTargetResponse;

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
    private Bucket bucket;

    @BeforeEach
    void setUp() {

        scraperClient = platformClient.createScraperClient();
        bucket = platformClient.createBucketClient().findBucketByName("my-bucket");
    }

    @Test
    void createScrapperTarget() {

        Organization organization = findMyOrg();
        ScraperTargetResponse scrapper = scraperClient.createScraperTarget("InfluxDB scrapper",
                "http://localhost:9999", bucket.getId(), organization.getId());

        Assertions.assertThat(scrapper).isNotNull();
        Assertions.assertThat(scrapper.getBucketName()).isEqualTo("my-bucket");
        Assertions.assertThat(scrapper.getOrganizationName()).isEqualTo("my-org");
        Assertions.assertThat(scrapper.getLinks())
                .hasSize(3)
                .hasEntrySatisfying("bucket", value -> Assertions.assertThat(value).isEqualTo("/api/v2/buckets/" + bucket.getId()))
                .hasEntrySatisfying("organization", value -> Assertions.assertThat(value).isEqualTo("/api/v2/orgs/" + organization.getId()))
                .hasEntrySatisfying("self", value -> Assertions.assertThat(value).isEqualTo("/api/v2/scrapers/" + scrapper.getId()));
    }

    @Test
    void updateScraper() {

        ScraperTarget scrapper = scraperClient.createScraperTarget("InfluxDB scrapper",
                "http://localhost:9999", bucket.getId(), findMyOrg().getId());

        scrapper.setName("Name updated");

        ScraperTargetResponse updated = scraperClient.updateScraperTarget(scrapper);

        Assertions.assertThat(updated.getName()).isEqualTo("Name updated");
    }

    @Test
    void findScrapers() {

        int size = scraperClient.findScraperTargets().size();

        scraperClient.createScraperTarget("InfluxDB scrapper",
                "http://localhost:9999", bucket.getId(), findMyOrg().getId());

        List<ScraperTargetResponse> scraperTargets = scraperClient.findScraperTargets();
        Assertions.assertThat(scraperTargets).hasSize(size + 1);
    }

    @Test
    void findScraperByID() {

        ScraperTargetResponse scraper = scraperClient.createScraperTarget("InfluxDB scrapper",
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

        ScraperTargetResponse createdScraper = scraperClient.createScraperTarget("InfluxDB scrapper",
                "http://localhost:9999", bucket.getId(), findMyOrg().getId());
        Assertions.assertThat(createdScraper).isNotNull();

        ScraperTargetResponse foundScraper = scraperClient.findScraperTargetByID(createdScraper.getId());
        Assertions.assertThat(foundScraper).isNotNull();

        // delete scraper
        scraperClient.deleteScraperTarget(createdScraper);

        foundScraper = scraperClient.findScraperTargetByID(createdScraper.getId());
        Assertions.assertThat(foundScraper).isNull();
    }
}