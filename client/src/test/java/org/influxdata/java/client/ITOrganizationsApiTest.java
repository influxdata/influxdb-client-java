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
package org.influxdata.java.client;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import org.influxdata.java.client.domain.FindOptions;
import org.influxdata.java.client.domain.Label;
import org.influxdata.java.client.domain.OperationLogEntries;
import org.influxdata.java.client.domain.OperationLogEntry;
import org.influxdata.java.client.domain.Organization;
import org.influxdata.java.client.domain.ResourceMember;
import org.influxdata.java.client.domain.User;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (12/09/2018 09:51)
 */
@RunWith(JUnitPlatform.class)
class ITOrganizationsApiTest extends AbstractITClientTest {

    private static final Logger LOG = Logger.getLogger(ITOrganizationsApiTest.class.getName());

    private OrganizationsApi organizationsApi;
    private UsersApi usersApi;

    @BeforeEach
    void setUp() {

        organizationsApi = influxDBClient.getOrganizationsApi();
        usersApi = influxDBClient.getUsersApi();
    }

    @Test
    void createOrganization() {

        String organizationName = generateName("Constant Pro");

        Organization organization = organizationsApi.createOrganization(organizationName);

        LOG.log(Level.INFO, "Created organization: {0}", organization);

        Assertions.assertThat(organization).isNotNull();
        Assertions.assertThat(organization.getId()).isNotBlank();
        Assertions.assertThat(organization.getName()).isEqualTo(organizationName);
        Assertions.assertThat(organization.getLinks())
                .hasSize(8)
                .containsKeys("buckets", "dashboards", "log", "members", "self", "tasks", "labels", "secrets");
    }

    @Test
    void findOrganizationByID() {

        String organizationName = generateName("Constant Pro");

        Organization organization = organizationsApi.createOrganization(organizationName);

        Organization organizationByID = organizationsApi.findOrganizationByID(organization.getId());

        Assertions.assertThat(organizationByID).isNotNull();
        Assertions.assertThat(organizationByID.getId()).isEqualTo(organization.getId());
        Assertions.assertThat(organizationByID.getName()).isEqualTo(organization.getName());
        Assertions.assertThat(organizationByID.getLinks())
                .hasSize(8)
                .containsKeys("buckets", "dashboards", "log", "members", "self", "tasks", "labels", "secrets");
    }

    @Test
    void findOrganizationByIDNull() {

        Organization organization = organizationsApi.findOrganizationByID("020f755c3c082000");

        Assertions.assertThat(organization).isNull();
    }

    @Test
    void findOrganizations() {

        int size = organizationsApi.findOrganizations().size();

        organizationsApi.createOrganization(generateName("Constant Pro"));

        List<Organization> organizations = organizationsApi.findOrganizations();
        Assertions.assertThat(organizations).hasSize(size + 1);
    }

    @Test
    void deleteOrganization() {

        Organization createdOrganization = organizationsApi.createOrganization(generateName("Constant Pro"));
        Assertions.assertThat(createdOrganization).isNotNull();

        Organization foundOrganization = organizationsApi.findOrganizationByID(createdOrganization.getId());
        Assertions.assertThat(foundOrganization).isNotNull();

        // delete organization
        organizationsApi.deleteOrganization(createdOrganization);

        foundOrganization = organizationsApi.findOrganizationByID(createdOrganization.getId());
        Assertions.assertThat(foundOrganization).isNull();
    }

    @Test
    void updateOrganization() {

        Organization createdOrganization = organizationsApi.createOrganization(generateName("Constant Pro"));
        createdOrganization.setName("Master Pb");

        Organization updatedOrganization = organizationsApi.updateOrganization(createdOrganization);

        Assertions.assertThat(updatedOrganization).isNotNull();
        Assertions.assertThat(updatedOrganization.getId()).isEqualTo(createdOrganization.getId());
        Assertions.assertThat(updatedOrganization.getName()).isEqualTo("Master Pb");

        Assertions.assertThat(updatedOrganization.getLinks()).hasSize(8);
        Assertions.assertThat(updatedOrganization.getLinks()).hasEntrySatisfying("buckets", value -> Assertions.assertThat(value).isEqualTo("/api/v2/buckets?org=Master Pb"));
        Assertions.assertThat(updatedOrganization.getLinks()).hasEntrySatisfying("dashboards", value -> Assertions.assertThat(value).isEqualTo("/api/v2/dashboards?org=Master Pb"));
        Assertions.assertThat(updatedOrganization.getLinks()).hasEntrySatisfying("self", value -> Assertions.assertThat(value).isEqualTo("/api/v2/orgs/" + updatedOrganization.getId()));
        Assertions.assertThat(updatedOrganization.getLinks()).hasEntrySatisfying("tasks", value -> Assertions.assertThat(value).isEqualTo("/api/v2/tasks?org=Master Pb"));
        Assertions.assertThat(updatedOrganization.getLinks()).hasEntrySatisfying("members", value -> Assertions.assertThat(value).isEqualTo("/api/v2/orgs/" + updatedOrganization.getId() + "/members"));
        Assertions.assertThat(updatedOrganization.getLinks()).hasEntrySatisfying("log", value -> Assertions.assertThat(value).isEqualTo("/api/v2/orgs/" + updatedOrganization.getId() + "/log"));
        Assertions.assertThat(updatedOrganization.getLinks()).hasEntrySatisfying("labels", value -> Assertions.assertThat(value).isEqualTo("/api/v2/orgs/" + updatedOrganization.getId() + "/labels"));
        Assertions.assertThat(updatedOrganization.getLinks()).hasEntrySatisfying("secrets", value -> Assertions.assertThat(value).isEqualTo("/api/v2/orgs/" + updatedOrganization.getId() + "/secrets"));
    }

    @Test
    void member() {

        Organization organization = organizationsApi.createOrganization(generateName("Constant Pro"));

        List<ResourceMember> members = organizationsApi.getMembers(organization);
        Assertions.assertThat(members).hasSize(0);

        User user = usersApi.createUser(generateName("Luke Health"));

        ResourceMember resourceMember = organizationsApi.addMember(user, organization);
        Assertions.assertThat(resourceMember).isNotNull();
        Assertions.assertThat(resourceMember.getUserID()).isEqualTo(user.getId());
        Assertions.assertThat(resourceMember.getUserName()).isEqualTo(user.getName());
        Assertions.assertThat(resourceMember.getRole()).isEqualTo(ResourceMember.UserType.MEMBER);

        members = organizationsApi.getMembers(organization);
        Assertions.assertThat(members).hasSize(1);
        Assertions.assertThat(members.get(0).getRole()).isEqualTo(ResourceMember.UserType.MEMBER);
        Assertions.assertThat(members.get(0).getUserID()).isEqualTo(user.getId());
        Assertions.assertThat(members.get(0).getUserName()).isEqualTo(user.getName());

        organizationsApi.deleteMember(user, organization);

        members = organizationsApi.getMembers(organization);
        Assertions.assertThat(members).hasSize(0);
    }

    @Test
    void owner() {

        Organization organization = organizationsApi.createOrganization(generateName("Constant Pro"));

        List<ResourceMember> owners = organizationsApi.getOwners(organization);
        Assertions.assertThat(owners).hasSize(0);

        User user = usersApi.createUser(generateName("Luke Health"));

        ResourceMember resourceMember = organizationsApi.addOwner(user, organization);
        Assertions.assertThat(resourceMember).isNotNull();
        Assertions.assertThat(resourceMember.getUserID()).isEqualTo(user.getId());
        Assertions.assertThat(resourceMember.getUserName()).isEqualTo(user.getName());
        Assertions.assertThat(resourceMember.getRole()).isEqualTo(ResourceMember.UserType.OWNER);

        owners = organizationsApi.getOwners(organization);
        Assertions.assertThat(owners).hasSize(1);
        Assertions.assertThat(owners.get(0).getRole()).isEqualTo(ResourceMember.UserType.OWNER);
        Assertions.assertThat(owners.get(0).getUserID()).isEqualTo(user.getId());
        Assertions.assertThat(owners.get(0).getUserName()).isEqualTo(user.getName());

        organizationsApi.deleteOwner(user, organization);

        owners = organizationsApi.getOwners(organization);
        Assertions.assertThat(owners).hasSize(0);
    }

    @Test
    void secrets() {

        Organization organization = organizationsApi.createOrganization(generateName("Constant Pro"));

        List<String> secrets = organizationsApi.getSecrets(organization);
        Assertions.assertThat(secrets).isEmpty();

        Map<String, String> secretsKV = new HashMap<>();
        secretsKV.put("gh", "123456789");
        secretsKV.put("az", "987654321");

        organizationsApi.putSecrets(secretsKV, organization);

        secrets = organizationsApi.getSecrets(organization);
        Assertions.assertThat(secrets).hasSize(2).contains("gh", "az");

        organizationsApi.deleteSecrets(Collections.singletonList("gh"), organization);

        secrets = organizationsApi.getSecrets(organization);
        Assertions.assertThat(secrets).hasSize(1).contains("az");
    }

    @Test
    @Disabled
        //TODO labels
    void labels() {

        LabelsApi labelsApi = influxDBClient.getLabelsApi();

        Organization organization = organizationsApi.createOrganization(generateName("Constant Pro"));

        Map<String, String> properties = new HashMap<>();
        properties.put("color", "green");
        properties.put("location", "west");

        Label label = labelsApi.createLabel(generateName("Cool Resource"), properties);

        List<Label> labels = organizationsApi.getLabels(organization);
        Assertions.assertThat(labels).hasSize(0);

        Label addedLabel = organizationsApi.addLabel(label, organization);
        Assertions.assertThat(addedLabel).isNotNull();
        Assertions.assertThat(addedLabel.getId()).isEqualTo(label.getId());
        Assertions.assertThat(addedLabel.getName()).isEqualTo(label.getName());
        Assertions.assertThat(addedLabel.getProperties()).isEqualTo(label.getProperties());

        labels = organizationsApi.getLabels(organization);
        Assertions.assertThat(labels).hasSize(1);
        Assertions.assertThat(labels.get(0).getId()).isEqualTo(label.getId());
        Assertions.assertThat(labels.get(0).getName()).isEqualTo(label.getName());

        organizationsApi.deleteLabel(label, organization);

        labels = organizationsApi.getLabels(organization);
        Assertions.assertThat(labels).hasSize(0);
    }

    @Test
    void findOrganizationLogs() {

        Organization organization = organizationsApi.createOrganization(generateName("Constant Pro"));

        List<OperationLogEntry> userLogs = organizationsApi.findOrganizationLogs(organization);
        Assertions.assertThat(userLogs).isNotEmpty();
        Assertions.assertThat(userLogs.get(0).getDescription()).isEqualTo("Organization Created");
    }

    @Test
    void findOrganizationLogsNotFound() {
        List<OperationLogEntry> userLogs = organizationsApi.findOrganizationLogs("020f755c3c082000");
        Assertions.assertThat(userLogs).isEmpty();
    }

    @Test
    void findOrganizationLogsPaging() {

        Organization organization = organizationsApi.createOrganization(generateName("Constant Pro"));

        IntStream
                .range(0, 19)
                .forEach(value -> {

                    organization.setName(value + "_" + organization.getName());
                    organizationsApi.updateOrganization(organization);
                });

        List<OperationLogEntry> logs = organizationsApi.findOrganizationLogs(organization);

        Assertions.assertThat(logs).hasSize(20);
        Assertions.assertThat(logs.get(0).getDescription()).isEqualTo("Organization Created");
        Assertions.assertThat(logs.get(19).getDescription()).isEqualTo("Organization Updated");

        FindOptions findOptions = new FindOptions();
        findOptions.setLimit(5);
        findOptions.setOffset(0);

        OperationLogEntries entries = organizationsApi.findOrganizationLogs(organization, findOptions);

        Assertions.assertThat(entries.getLogs()).hasSize(5);
        Assertions.assertThat(entries.getLogs().get(0).getDescription()).isEqualTo("Organization Created");
        Assertions.assertThat(entries.getLogs().get(1).getDescription()).isEqualTo("Organization Updated");
        Assertions.assertThat(entries.getLogs().get(2).getDescription()).isEqualTo("Organization Updated");
        Assertions.assertThat(entries.getLogs().get(3).getDescription()).isEqualTo("Organization Updated");
        Assertions.assertThat(entries.getLogs().get(4).getDescription()).isEqualTo("Organization Updated");

        //TODO isNotNull FindOptions also in Log API?
        findOptions.setOffset(findOptions.getOffset() + 5);
        Assertions.assertThat(entries.getNextPage()).isNull();

        entries = organizationsApi.findOrganizationLogs(organization, findOptions);
        Assertions.assertThat(entries.getLogs()).hasSize(5);
        Assertions.assertThat(entries.getLogs().get(0).getDescription()).isEqualTo("Organization Updated");
        Assertions.assertThat(entries.getLogs().get(1).getDescription()).isEqualTo("Organization Updated");
        Assertions.assertThat(entries.getLogs().get(2).getDescription()).isEqualTo("Organization Updated");
        Assertions.assertThat(entries.getLogs().get(3).getDescription()).isEqualTo("Organization Updated");
        Assertions.assertThat(entries.getLogs().get(4).getDescription()).isEqualTo("Organization Updated");

        findOptions.setOffset(findOptions.getOffset() + 5);
        Assertions.assertThat(entries.getNextPage()).isNull();

        entries = organizationsApi.findOrganizationLogs(organization, findOptions);
        Assertions.assertThat(entries.getLogs().get(0).getDescription()).isEqualTo("Organization Updated");
        Assertions.assertThat(entries.getLogs().get(1).getDescription()).isEqualTo("Organization Updated");
        Assertions.assertThat(entries.getLogs().get(2).getDescription()).isEqualTo("Organization Updated");
        Assertions.assertThat(entries.getLogs().get(3).getDescription()).isEqualTo("Organization Updated");
        Assertions.assertThat(entries.getLogs().get(4).getDescription()).isEqualTo("Organization Updated");

        findOptions.setOffset(findOptions.getOffset() + 5);
        Assertions.assertThat(entries.getNextPage()).isNull();

        entries = organizationsApi.findOrganizationLogs(organization, findOptions);
        Assertions.assertThat(entries.getLogs()).hasSize(5);
        Assertions.assertThat(entries.getLogs().get(0).getDescription()).isEqualTo("Organization Updated");
        Assertions.assertThat(entries.getLogs().get(1).getDescription()).isEqualTo("Organization Updated");
        Assertions.assertThat(entries.getLogs().get(2).getDescription()).isEqualTo("Organization Updated");
        Assertions.assertThat(entries.getLogs().get(3).getDescription()).isEqualTo("Organization Updated");
        Assertions.assertThat(entries.getLogs().get(4).getDescription()).isEqualTo("Organization Updated");

        findOptions.setOffset(findOptions.getOffset() + 5);
        Assertions.assertThat(entries.getNextPage()).isNull();

        entries = organizationsApi.findOrganizationLogs(organization, findOptions);
        Assertions.assertThat(entries.getLogs()).hasSize(0);
        Assertions.assertThat(entries.getNextPage()).isNull();

        // order
        findOptions = new FindOptions();
        findOptions.setDescending(false);

        entries = organizationsApi.findOrganizationLogs(organization, findOptions);

        Assertions.assertThat(entries.getLogs()).hasSize(20);
        Assertions.assertThat(entries.getLogs().get(19).getDescription()).isEqualTo("Organization Updated");
        Assertions.assertThat(entries.getLogs().get(0).getDescription()).isEqualTo("Organization Created");
    }
}