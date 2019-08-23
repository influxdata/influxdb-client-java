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

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import com.influxdb.client.domain.HealthCheck;
import com.influxdb.client.domain.OnboardingRequest;
import com.influxdb.client.domain.OnboardingResponse;
import com.influxdb.client.domain.Ready;
import com.influxdb.client.domain.Routes;
import com.influxdb.client.domain.User;
import com.influxdb.client.service.DefaultService;
import com.influxdb.exceptions.UnprocessableEntityException;

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

        HealthCheck check = influxDBClient.health();

        Assertions.assertThat(check).isNotNull();
        Assertions.assertThat(check.getName()).isEqualTo("influxdb");
        Assertions.assertThat(check.getStatus()).isEqualTo(HealthCheck.StatusEnum.PASS);
        Assertions.assertThat(check.getMessage()).isEqualTo("ready for queries and writes");
    }

    @Test
    void healthNotRunningInstance() throws Exception {

        InfluxDBClient clientNotRunning = InfluxDBClientFactory.create("http://localhost:8099");
		HealthCheck check = clientNotRunning.health();

        Assertions.assertThat(check).isNotNull();
        Assertions.assertThat(check.getName()).isEqualTo("influxdb");
        Assertions.assertThat(check.getStatus()).isEqualTo(HealthCheck.StatusEnum.FAIL);
        Assertions.assertThat(check.getMessage()).startsWith("Failed to connect to");

        clientNotRunning.close();
    }

    @Test
    void ready() {

        Ready ready = influxDBClient.ready();

        Assertions.assertThat(ready).isNotNull();


        Assertions.assertThat(ready.getStatus()).isEqualTo(Ready.StatusEnum.READY);
        Assertions.assertThat(ready.getStarted()).isNotNull();
        Assertions.assertThat(ready.getStarted()).isBefore(OffsetDateTime.now(ZoneOffset.UTC));
        Assertions.assertThat(ready.getUp()).isNotBlank();
    }

    @Test
    void readyNotRunningInstance() throws Exception {

        InfluxDBClient clientNotRunning = InfluxDBClientFactory.create("http://localhost:8099");

        Ready ready = clientNotRunning.ready();
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

        String url = String.format("http://%s:%s",
                System.getenv().getOrDefault("INFLUXDB_2_ONBOARDING_IP", "127.0.0.1"),
                System.getenv().getOrDefault("INFLUXDB_2_ONBOARDING_PORT", "9990"));

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

    @Test
    void defaultService() throws IOException {

        DefaultService service = influxDBClient.getService(DefaultService.class);

        Assertions.assertThat(service).isNotNull();

        Routes routes = service.getRoutes(null).execute().body();

        Assertions.assertThat(routes).isNotNull();
        Assertions.assertThat(routes.getAuthorizations()).isEqualTo("/api/v2/authorizations");
        Assertions.assertThat(routes.getBuckets()).isEqualTo("/api/v2/buckets");
        Assertions.assertThat(routes.getDashboards()).isEqualTo("/api/v2/dashboards");
        Assertions.assertThat(routes.getVariables()).isEqualTo("/api/v2/variables");
        Assertions.assertThat(routes.getMe()).isEqualTo("/api/v2/me");
        Assertions.assertThat(routes.getOrgs()).isEqualTo("/api/v2/orgs");
        Assertions.assertThat(routes.getQuery()).isNotNull();
        Assertions.assertThat(routes.getSetup()).isEqualTo("/api/v2/setup");
        Assertions.assertThat(routes.getSignin()).isEqualTo("/api/v2/signin");
        Assertions.assertThat(routes.getSignout()).isEqualTo("/api/v2/signout");
        Assertions.assertThat(routes.getSources()).isEqualTo("/api/v2/sources");
        Assertions.assertThat(routes.getSystem()).isNotNull();
        Assertions.assertThat(routes.getTasks()).isEqualTo("/api/v2/tasks");
        Assertions.assertThat(routes.getTelegrafs()).isEqualTo("/api/v2/telegrafs");
        Assertions.assertThat(routes.getUsers()).isEqualTo("/api/v2/users");
        Assertions.assertThat(routes.getWrite()).isEqualTo("/api/v2/write");
    }
}