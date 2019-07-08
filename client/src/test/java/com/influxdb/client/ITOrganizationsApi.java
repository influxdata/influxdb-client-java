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

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import com.influxdb.client.domain.Label;
import com.influxdb.client.domain.OperationLog;
import com.influxdb.client.domain.OperationLogs;
import com.influxdb.client.domain.Organization;
import com.influxdb.client.domain.ResourceMember;
import com.influxdb.client.domain.ResourceOwner;
import com.influxdb.client.domain.SecretKeysResponse;
import com.influxdb.client.domain.User;
import com.influxdb.exceptions.NotFoundException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (12/09/2018 09:51)
 */
@RunWith(JUnitPlatform.class)
class ITOrganizationsApi extends AbstractITClientTest {

    private static final Logger LOG = Logger.getLogger(ITOrganizationsApi.class.getName());

    private OrganizationsApi organizationsApi;
    private UsersApi usersApi;

    @BeforeEach
    void setUp() {

        organizationsApi = influxDBClient.getOrganizationsApi();
        usersApi = influxDBClient.getUsersApi();
    }

    @Test
    void createOrganization() {

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        String orgName = generateName("Constant Pro");

        Organization organization = organizationsApi.createOrganization(orgName);

        LOG.log(Level.INFO, "Created organization: {0}", organization);

        Assertions.assertThat(organization).isNotNull();
        Assertions.assertThat(organization.getId()).isNotBlank();
        Assertions.assertThat(organization.getName()).isEqualTo(orgName);
        Assertions.assertThat(organization.getLinks()).isNotNull();
        Assertions.assertThat(organization.getCreatedAt()).isAfter(now);
        Assertions.assertThat(organization.getUpdatedAt()).isAfter(now);
        Assertions.assertThat(organization.getLinks().getBuckets()).isEqualTo("/api/v2/buckets?org=" + orgName);
        Assertions.assertThat(organization.getLinks().getDashboards()).isEqualTo("/api/v2/dashboards?org=" + orgName);
        Assertions.assertThat(organization.getLinks().getLogs()).isEqualTo("/api/v2/orgs/" + organization.getId() + "/logs");
        Assertions.assertThat(organization.getLinks().getMembers()).isEqualTo("/api/v2/orgs/" + organization.getId() + "/members");
        Assertions.assertThat(organization.getLinks().getOwners()).isEqualTo("/api/v2/orgs/" + organization.getId() + "/owners");
        Assertions.assertThat(organization.getLinks().getSelf()).isEqualTo("/api/v2/orgs/" + organization.getId());
        Assertions.assertThat(organization.getLinks().getTasks()).isEqualTo("/api/v2/tasks?org=" + orgName);
        Assertions.assertThat(organization.getLinks().getLabels()).isEqualTo("/api/v2/orgs/" + organization.getId() + "/labels");
        Assertions.assertThat(organization.getLinks().getSecrets()).isEqualTo("/api/v2/orgs/" + organization.getId() + "/secrets");
    }

    @Test
    void createOrganizationDescription() {

        Organization organization = new Organization();
        organization.setName(generateName("org"));
        organization.setDescription("description of org");

        organization = organizationsApi.createOrganization(organization);

        Assertions.assertThat(organization.getDescription()).isEqualTo("description of org");
    }

    @Test
    void findOrganizationByID() {

        String orgName = generateName("Constant Pro");

        Organization organization = organizationsApi.createOrganization(orgName);

        Organization organizationByID = organizationsApi.findOrganizationByID(organization.getId());

        Assertions.assertThat(organizationByID).isNotNull();
        Assertions.assertThat(organizationByID.getId()).isEqualTo(organization.getId());
        Assertions.assertThat(organizationByID.getName()).isEqualTo(organization.getName());
        Assertions.assertThat(organizationByID.getLinks()).isNotNull();
    }

    @Test
    void findOrganizationByIDNull() {

        Assertions.assertThatThrownBy(() -> organizationsApi.findOrganizationByID("020f755c3c082000"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("organization not found");
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

        Assertions.assertThatThrownBy(() -> organizationsApi.findOrganizationByID(createdOrganization.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("organization not found");
    }

    @Test
    void updateOrganization() {

        Organization createdOrganization = organizationsApi.createOrganization(generateName("Constant Pro"));

        String updatedName = generateName("Master Pb");
        createdOrganization.setName(updatedName);

        OffsetDateTime updatedAt = createdOrganization.getUpdatedAt();

        Organization updatedOrganization = organizationsApi.updateOrganization(createdOrganization);

        Assertions.assertThat(updatedOrganization).isNotNull();
        Assertions.assertThat(updatedOrganization.getId()).isEqualTo(createdOrganization.getId());
        Assertions.assertThat(updatedOrganization.getName()).isEqualTo(updatedName);
        Assertions.assertThat(updatedOrganization.getUpdatedAt()).isAfter(updatedAt);

        Assertions.assertThat(updatedOrganization.getLinks()).isNotNull();
    }

    @Test
    void member() {

        Organization organization = organizationsApi.createOrganization(generateName("Constant Pro"));

        List<ResourceMember> members = organizationsApi.getMembers(organization);
        Assertions.assertThat(members).hasSize(0);

        User user = usersApi.createUser(generateName("Luke Health"));

        ResourceMember resourceMember = organizationsApi.addMember(user, organization);
        Assertions.assertThat(resourceMember).isNotNull();
        Assertions.assertThat(resourceMember.getId()).isEqualTo(user.getId());
        Assertions.assertThat(resourceMember.getName()).isEqualTo(user.getName());
        Assertions.assertThat(resourceMember.getRole()).isEqualTo(ResourceMember.RoleEnum.MEMBER);

        members = organizationsApi.getMembers(organization);
        Assertions.assertThat(members).hasSize(1);
        Assertions.assertThat(members.get(0).getRole()).isEqualTo(ResourceMember.RoleEnum.MEMBER);
        Assertions.assertThat(members.get(0).getId()).isEqualTo(user.getId());
        Assertions.assertThat(members.get(0).getName()).isEqualTo(user.getName());

        organizationsApi.deleteMember(user, organization);

        members = organizationsApi.getMembers(organization);
        Assertions.assertThat(members).hasSize(0);
    }

    @Test
    void owner() {

        Organization organization = organizationsApi.createOrganization(generateName("Constant Pro"));

        List<ResourceOwner> owners = organizationsApi.getOwners(organization);
        Assertions.assertThat(owners).hasSize(1);
        Assertions.assertThat(owners.get(0).getName()).isEqualTo("my-user");

        User user = usersApi.createUser(generateName("Luke Health"));

        ResourceOwner resourceMember = organizationsApi.addOwner(user, organization);
        Assertions.assertThat(resourceMember).isNotNull();
        Assertions.assertThat(resourceMember.getId()).isEqualTo(user.getId());
        Assertions.assertThat(resourceMember.getName()).isEqualTo(user.getName());
        Assertions.assertThat(resourceMember.getRole()).isEqualTo(ResourceOwner.RoleEnum.OWNER);

        owners = organizationsApi.getOwners(organization);
        Assertions.assertThat(owners).hasSize(2);
        Assertions.assertThat(owners.get(1).getRole()).isEqualTo(ResourceOwner.RoleEnum.OWNER);
        Assertions.assertThat(owners.get(1).getId()).isEqualTo(user.getId());
        Assertions.assertThat(owners.get(1).getName()).isEqualTo(user.getName());

        organizationsApi.deleteOwner(user, organization);

        owners = organizationsApi.getOwners(organization);
        Assertions.assertThat(owners).hasSize(1);
    }

    @Test
    void secrets() {

        Organization organization = organizationsApi.createOrganization(generateName("Constant Pro"));

        SecretKeysResponse secrets = organizationsApi.getSecrets(organization);
        Assertions.assertThat(secrets.getSecrets()).isEmpty();

        Map<String, String> secretsKV = new HashMap<>();
        secretsKV.put("gh", "123456789");
        secretsKV.put("az", "987654321");

        organizationsApi.putSecrets(secretsKV, organization);

        secrets = organizationsApi.getSecrets(organization);
        Assertions.assertThat(secrets.getSecrets()).hasSize(2).contains("gh", "az");
        Assertions.assertThat(secrets.getLinks().getOrg()).isEqualTo("/api/v2/orgs/" + organization.getId());
        Assertions.assertThat(secrets.getLinks().getSelf()).isEqualTo("/api/v2/orgs/" + organization.getId() + "/secrets");

        organizationsApi.deleteSecrets(Collections.singletonList("gh"), organization);
        secrets = organizationsApi.getSecrets(organization);
        Assertions.assertThat(secrets.getSecrets()).hasSize(1).contains("az");
    }

    @Test
    void labels() {

        LabelsApi labelsApi = influxDBClient.getLabelsApi();

        Organization organization = organizationsApi.createOrganization(generateName("Constant Pro"));

        Map<String, String> properties = new HashMap<>();
        properties.put("color", "green");
        properties.put("location", "west");

        Label label = labelsApi.createLabel(generateName("Cool Resource"), properties, organization.getId());

        List<Label> labels = organizationsApi.getLabels(organization);
        Assertions.assertThat(labels).hasSize(0);

        Label addedLabel = organizationsApi.addLabel(label, organization).getLabel();
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

        List<OperationLog> userLogs = organizationsApi.findOrganizationLogs(organization);
        Assertions.assertThat(userLogs).isNotEmpty();
        Assertions.assertThat(userLogs.get(0).getDescription()).isEqualTo("Organization Created");
    }

    @Test
    void findOrganizationLogsNotFound() {
        List<OperationLog> userLogs = organizationsApi.findOrganizationLogs("020f755c3c082000");
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

        List<OperationLog> logs = organizationsApi.findOrganizationLogs(organization);

        Assertions.assertThat(logs).hasSize(20);
        Assertions.assertThat(logs.get(0).getDescription()).isEqualTo("Organization Created");
        Assertions.assertThat(logs.get(19).getDescription()).isEqualTo("Organization Updated");

        FindOptions findOptions = new FindOptions();
        findOptions.setLimit(5);
        findOptions.setOffset(0);

        OperationLogs entries = organizationsApi.findOrganizationLogs(organization, findOptions);

        Assertions.assertThat(entries.getLogs()).hasSize(5);
        Assertions.assertThat(entries.getLogs().get(0).getDescription()).isEqualTo("Organization Created");
        Assertions.assertThat(entries.getLogs().get(1).getDescription()).isEqualTo("Organization Updated");
        Assertions.assertThat(entries.getLogs().get(2).getDescription()).isEqualTo("Organization Updated");
        Assertions.assertThat(entries.getLogs().get(3).getDescription()).isEqualTo("Organization Updated");
        Assertions.assertThat(entries.getLogs().get(4).getDescription()).isEqualTo("Organization Updated");

        findOptions.setOffset(findOptions.getOffset() + 5);
        Assertions.assertThat(entries.getLinks().getNext()).isNull();

        entries = organizationsApi.findOrganizationLogs(organization, findOptions);
        Assertions.assertThat(entries.getLogs()).hasSize(5);
        Assertions.assertThat(entries.getLogs().get(0).getDescription()).isEqualTo("Organization Updated");
        Assertions.assertThat(entries.getLogs().get(1).getDescription()).isEqualTo("Organization Updated");
        Assertions.assertThat(entries.getLogs().get(2).getDescription()).isEqualTo("Organization Updated");
        Assertions.assertThat(entries.getLogs().get(3).getDescription()).isEqualTo("Organization Updated");
        Assertions.assertThat(entries.getLogs().get(4).getDescription()).isEqualTo("Organization Updated");

        findOptions.setOffset(findOptions.getOffset() + 5);
        Assertions.assertThat(entries.getLinks().getNext()).isNull();

        entries = organizationsApi.findOrganizationLogs(organization, findOptions);
        Assertions.assertThat(entries.getLogs().get(0).getDescription()).isEqualTo("Organization Updated");
        Assertions.assertThat(entries.getLogs().get(1).getDescription()).isEqualTo("Organization Updated");
        Assertions.assertThat(entries.getLogs().get(2).getDescription()).isEqualTo("Organization Updated");
        Assertions.assertThat(entries.getLogs().get(3).getDescription()).isEqualTo("Organization Updated");
        Assertions.assertThat(entries.getLogs().get(4).getDescription()).isEqualTo("Organization Updated");

        findOptions.setOffset(findOptions.getOffset() + 5);
        Assertions.assertThat(entries.getLinks().getNext()).isNull();

        entries = organizationsApi.findOrganizationLogs(organization, findOptions);
        Assertions.assertThat(entries.getLogs()).hasSize(5);
        Assertions.assertThat(entries.getLogs().get(0).getDescription()).isEqualTo("Organization Updated");
        Assertions.assertThat(entries.getLogs().get(1).getDescription()).isEqualTo("Organization Updated");
        Assertions.assertThat(entries.getLogs().get(2).getDescription()).isEqualTo("Organization Updated");
        Assertions.assertThat(entries.getLogs().get(3).getDescription()).isEqualTo("Organization Updated");
        Assertions.assertThat(entries.getLogs().get(4).getDescription()).isEqualTo("Organization Updated");

        findOptions.setOffset(findOptions.getOffset() + 5);
        Assertions.assertThat(entries.getLinks().getNext()).isNull();

        entries = organizationsApi.findOrganizationLogs(organization, findOptions);
        Assertions.assertThat(entries.getLogs()).hasSize(0);
        Assertions.assertThat(entries.getLinks().getNext()).isNull();

        // order
        findOptions = new FindOptions();
        findOptions.setDescending(false);

        entries = organizationsApi.findOrganizationLogs(organization, findOptions);

        Assertions.assertThat(entries.getLogs()).hasSize(20);
        Assertions.assertThat(entries.getLogs().get(19).getDescription()).isEqualTo("Organization Updated");
        Assertions.assertThat(entries.getLogs().get(0).getDescription()).isEqualTo("Organization Created");
    }

    @Test
    void cloneOrganization() {

        Organization source = organizationsApi.createOrganization(generateName("Constant Pro"));

        String name = generateName("cloned");

        Map<String, String> properties = new HashMap<>();
        properties.put("color", "green");
        properties.put("location", "west");

        Label label = influxDBClient.getLabelsApi().createLabel(generateName("Cool Resource"), properties, source.getId());

        organizationsApi.addLabel(label, source);

        Organization cloned = organizationsApi.cloneOrganization(name, source.getId());

        Assertions.assertThat(cloned.getName()).isEqualTo(name);

        List<Label> labels = organizationsApi.getLabels(cloned);
        Assertions.assertThat(labels).hasSize(1);
        Assertions.assertThat(labels.get(0).getId()).isEqualTo(label.getId());
    }

    @Test
    void cloneOrganizationNotFound() {
        Assertions.assertThatThrownBy(() -> organizationsApi.cloneOrganization(generateName("cloned"), "020f755c3c082000"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("organization not found");
    }
}