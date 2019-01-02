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

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

import org.influxdata.platform.domain.Organization;
import org.influxdata.platform.domain.RetentionRule;

import org.junit.jupiter.api.AfterEach;

/**
 * @author Jakub Bednar (bednar@github) (11/09/2018 10:29)
 */
abstract class AbstractITClientTest extends AbstractTest {

    private static final Logger LOG = Logger.getLogger(AbstractITClientTest.class.getName());

    PlatformClient platformClient;
    String platformURL;

    void setUp() throws Exception {

        String platformIP = System.getenv().getOrDefault("PLATFORM_IP", "127.0.0.1");
        String platformPort = System.getenv().getOrDefault("PLATFORM_PORT", "9999");

        platformURL = "http://" + platformIP + ":" + platformPort;
        LOG.log(Level.FINEST, "Platform URL: {0}", platformURL);

        platformClient = PlatformClientFactory.create(platformURL, "my-user", "my-password".toCharArray());
    }

    @AfterEach
    void logout() throws Exception {
        platformClient.close();
    }

    @Nonnull
    RetentionRule retentionRule() {
        RetentionRule retentionRule = new RetentionRule();
        retentionRule.setType("expire");
        retentionRule.setEverySeconds(3600L);
        return retentionRule;
    }

    @Nonnull
    Organization findMyOrg() {
        return platformClient.createOrganizationClient()
                .findOrganizations().stream()
                .filter(organization -> organization.getName().equals("my-org"))
                .findFirst()
                .orElseThrow(IllegalStateException::new);
    }
}