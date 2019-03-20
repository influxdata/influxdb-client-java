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

import org.influxdata.client.domain.Check;
import org.influxdata.client.domain.OnboardingRequest;
import org.influxdata.client.domain.OnboardingResponse;
import org.influxdata.client.domain.User;
import org.influxdata.exceptions.UnprocessableEntityException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (20/11/2018 07:37)
 */
@RunWith(JUnitPlatform.class)
class ITInfluxDBClient extends AbstractITClientTest {

    @Test
    void health() {

        Check check = influxDBClient.health();

        Assertions.assertThat(check).isNotNull();
        Assertions.assertThat(check.getStatus()).isEqualTo(Check.StatusEnum.PASS);
        Assertions.assertThat(check.getMessage()).isEqualTo("ready for queries and writes");
    }

    @Test
    void healthNotRunningInstance() throws Exception {

        InfluxDBClient clientNotRunning = InfluxDBClientFactory.create("http://localhost:8099");
        Check check = clientNotRunning.health();

        Assertions.assertThat(check).isNotNull();
        Assertions.assertThat(check.getStatus()).isEqualTo(Check.StatusEnum.FAIL);
        Assertions.assertThat(check.getMessage()).startsWith("Failed to connect to");

        clientNotRunning.close();
    }

    @Test
    void ready() {

        Check ready = influxDBClient.ready();

        Assertions.assertThat(ready).isNotNull();

        //TODO https://github.com/influxdata/influxdb/issues/12546
        // Assertions.assertThat(ready.getStatus()).isEqualTo("ready");
        // Assertions.assertThat(ready.getStarted()).isNotNull();
        // Assertions.assertThat(ready.getStarted()).isBefore(Instant.now());
        // Assertions.assertThat(ready.getUp()).isNotBlank();
    }

    @Test
    void readyNotRunningInstance() throws Exception {

        InfluxDBClient clientNotRunning = InfluxDBClientFactory.create("http://localhost:8099");

        Check ready = clientNotRunning.ready();
        Assertions.assertThat(ready).isNull();

        clientNotRunning.close();
    }

    @Test
    void isOnboardingNotAllowed() {

        Boolean onboardingAllowed = influxDBClient.isOnboardingAllowed();

        Assertions.assertThat(onboardingAllowed).isFalse();
    }

    @Test
    void onboarding() throws Exception {

        String url = "http://" + influxDB_IP + ":9990/api/v2/";

        InfluxDBClient influxDBClient = InfluxDBClientFactory.create(url);

        Boolean onboardingAllowed = influxDBClient.isOnboardingAllowed();
        Assertions.assertThat(onboardingAllowed).isTrue();

        influxDBClient.close();

        OnboardingResponse onboarding = InfluxDBClientFactory.onBoarding(url, "admin", "11111111", "Testing", "test");

        Assertions.assertThat(onboarding).isNotNull();
        Assertions.assertThat(onboarding.getUser()).isNotNull();
        Assertions.assertThat(onboarding.getUser().getId()).isNotEmpty();
        Assertions.assertThat(onboarding.getUser().getName()).isEqualTo("admin");

        Assertions.assertThat(onboarding.getBucket()).isNotNull();
        Assertions.assertThat(onboarding.getBucket().getId()).isNotEmpty();
        Assertions.assertThat(onboarding.getBucket().getName()).isEqualTo("test");

        Assertions.assertThat(onboarding.getOrg()).isNotNull();
        Assertions.assertThat(onboarding.getOrg().getId()).isNotEmpty();
        Assertions.assertThat(onboarding.getOrg().getName()).isEqualTo("Testing");

        Assertions.assertThat(onboarding.getAuth()).isNotNull();
        Assertions.assertThat(onboarding.getAuth().getId()).isNotEmpty();
        Assertions.assertThat(onboarding.getAuth().getToken()).isNotEmpty();

        influxDBClient.close();

        influxDBClient = InfluxDBClientFactory.create(url, onboarding.getAuth().getToken().toCharArray());

        User me = influxDBClient.getUsersApi().me();
        Assertions.assertThat(me).isNotNull();
        Assertions.assertThat(me.getName()).isEqualTo("admin");

        influxDBClient.close();
    }

    @Test
    void onboardingAlreadyDone() {

        OnboardingRequest onboarding = new OnboardingRequest();
        onboarding.setUsername("admin");
        onboarding.setPassword("11111111");
        onboarding.setOrg("Testing");
        onboarding.setBucket("test");

        Assertions.assertThatThrownBy(() -> influxDBClient.onBoarding(onboarding))
                .isInstanceOf(UnprocessableEntityException.class)
                .hasMessage("onboarding has already been completed");
    }
}