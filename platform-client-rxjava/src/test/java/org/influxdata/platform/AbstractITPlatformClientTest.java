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

import org.influxdata.platform.domain.Organization;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * @author Jakub Bednar (bednar@github) (11/09/2018 10:29)
 */
abstract class AbstractITPlatformClientTest extends AbstractTest {

    private static final Logger LOG = Logger.getLogger(AbstractITPlatformClientTest.class.getName());

    PlatformClientReactive platformClient;
    String platformURL;
    Organization organization;

    @BeforeEach
    void setUp() throws Exception {

        String platformIP = System.getenv().getOrDefault("PLATFORM_IP", "127.0.0.1");
        String platformPort = System.getenv().getOrDefault("PLATFORM_PORT", "9999");

        platformURL = "http://" + platformIP + ":" + platformPort;
        LOG.log(Level.FINEST, "Platform URL: {0}", platformURL);

        PlatformClient platformClient = PlatformClientFactory.create(platformURL, "my-user", "my-password".toCharArray());

        organization = platformClient.createOrganizationClient()
                .findOrganizations().stream()
                .filter(organization -> organization.getName().equals("my-org"))
                .findFirst()
                .orElseThrow(IllegalStateException::new);

        String token = platformClient.createAuthorizationClient()
                .findAuthorizations()
                .stream()
                .filter(authorization -> authorization.getPermissions().size() == 4)
                .findFirst()
                .orElseThrow(IllegalStateException::new).getToken();

        platformClient.close();
        
        try {
            this.platformClient = PlatformClientReactiveFactory.create(platformURL, token.toCharArray());
        } catch (Exception e) {
            Assertions.fail("Can't authorize via token", e);
        }

    }

    @AfterEach
    void logout() throws Exception {
        platformClient.close();
    }

}