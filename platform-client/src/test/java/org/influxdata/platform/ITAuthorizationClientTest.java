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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

import org.influxdata.platform.domain.Authorization;
import org.influxdata.platform.domain.Bucket;
import org.influxdata.platform.domain.Organization;
import org.influxdata.platform.domain.Permission;
import org.influxdata.platform.domain.PermissionResource;
import org.influxdata.platform.domain.PermissionResourceType;
import org.influxdata.platform.domain.Status;
import org.influxdata.platform.domain.User;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (17/09/2018 12:02)
 */
@RunWith(JUnitPlatform.class)
class ITAuthorizationClientTest extends AbstractITClientTest {

    private static final Logger LOG = Logger.getLogger(ITAuthorizationClientTest.class.getName());

    private AuthorizationClient authorizationClient;

    private User user;
    private Organization organization;

    @BeforeEach
    void setUp() throws Exception {

        super.setUp();

        authorizationClient = platformClient.createAuthorizationClient();

        user = platformClient.createUserClient().me();
        organization = findMyOrg();
    }

    @Test
    void createAuthorization() {

        PermissionResource userResource = new PermissionResource();
        userResource.setOrgID(organization.getId());
        userResource.setType(PermissionResourceType.USER);

        Permission readUsers = new Permission();
        readUsers.setAction(Permission.READ_ACTION);
        readUsers.setResource(userResource);

        PermissionResource orgResource = new PermissionResource();
        orgResource.setOrgID(organization.getId());
        orgResource.setType(PermissionResourceType.ORG);

        Permission writeOrganizations = new Permission();
        writeOrganizations.setAction(Permission.WRITE_ACTION);
        writeOrganizations.setResource(orgResource);

        List<Permission> permissions = new ArrayList<>();
        permissions.add(readUsers);
        permissions.add(writeOrganizations);

        Authorization authorization = authorizationClient.createAuthorization(organization.getId(), permissions);

        LOG.log(Level.INFO, "Created authorization: {0}", authorization);

        Assertions.assertThat(authorization).isNotNull();
        Assertions.assertThat(authorization.getId()).isNotBlank();
        Assertions.assertThat(authorization.getToken()).isNotBlank();
        Assertions.assertThat(authorization.getUserID()).isEqualTo(user.getId());
        Assertions.assertThat(authorization.getUserName()).isEqualTo(user.getName());
        Assertions.assertThat(authorization.getOrgID()).isEqualTo(organization.getId());
        Assertions.assertThat(authorization.getOrgName()).isEqualTo(organization.getName());
        Assertions.assertThat(authorization.getStatus()).isEqualTo(Status.ACTIVE);

        Assertions.assertThat(authorization.getPermissions()).hasSize(2);
        Assertions.assertThat(authorization.getPermissions().get(0).getResource().getType()).isEqualTo(PermissionResourceType.USER);
        Assertions.assertThat(authorization.getPermissions().get(0).getResource().getOrgID()).isEqualTo(organization.getId());
        Assertions.assertThat(authorization.getPermissions().get(0).getAction()).isEqualTo("read");

        Assertions.assertThat(authorization.getPermissions().get(1).getResource().getType()).isEqualTo(PermissionResourceType.ORG);
        Assertions.assertThat(authorization.getPermissions().get(1).getResource().getOrgID()).isEqualTo(organization.getId());
        Assertions.assertThat(authorization.getPermissions().get(1).getAction()).isEqualTo("write");

        Assertions.assertThat(authorization.getLinks()).hasSize(2);
        Assertions.assertThat(authorization.getLinks()).hasEntrySatisfying("self", value -> Assertions.assertThat(value).isEqualTo("/api/v2/authorizations/" + authorization.getId()));
        Assertions.assertThat(authorization.getLinks()).hasEntrySatisfying("user", value -> Assertions.assertThat(value).isEqualTo("/api/v2/users/" + user.getId()));
    }

    @Test
    void authorizationDescription() {

        PermissionResource resource = new PermissionResource();
        resource.setOrgID(organization.getId());
        resource.setType(PermissionResourceType.SOURCE);

        Permission createSource = new Permission();
        createSource.setResource(resource);
        createSource.setAction(Permission.WRITE_ACTION);

        List<Permission> permissions = new ArrayList<>();
        permissions.add(createSource);

        Authorization authorization = new Authorization();
        authorization.setOrgID(organization.getId());
        authorization.setPermissions(permissions);
        authorization.setStatus(Status.ACTIVE);
        authorization.setDescription("My description!");

        Authorization created = authorizationClient.createAuthorization(authorization);

        Assertions.assertThat(created).isNotNull();
        Assertions.assertThat(created.getDescription()).isEqualTo("My description!");
    }

    @Test
    void createAuthorizationTask() {

        PermissionResource resource = new PermissionResource();
        resource.setOrgID(organization.getId());
        resource.setType(PermissionResourceType.TASK);

        Permission createTask = new Permission();
        createTask.setResource(resource);
        createTask.setAction(Permission.READ_ACTION);

        Permission deleteTask = new Permission();
        deleteTask.setResource(resource);
        deleteTask.setAction(Permission.WRITE_ACTION);

        List<Permission> permissions = new ArrayList<>();
        permissions.add(createTask);
        permissions.add(deleteTask);

        Authorization authorization = authorizationClient.createAuthorization(organization, permissions);

        Assertions.assertThat(authorization.getPermissions()).hasSize(2);
        Assertions.assertThat(authorization.getPermissions().get(0).getResource().getType()).isEqualTo(PermissionResourceType.TASK);
        Assertions.assertThat(authorization.getPermissions().get(0).getResource().getOrgID()).isEqualTo(organization.getId());
        Assertions.assertThat(authorization.getPermissions().get(0).getAction()).isEqualTo("read");
        Assertions.assertThat(authorization.getPermissions().get(1).getResource().getType()).isEqualTo(PermissionResourceType.TASK);
        Assertions.assertThat(authorization.getPermissions().get(1).getResource().getOrgID()).isEqualTo(organization.getId());
        Assertions.assertThat(authorization.getPermissions().get(1).getAction()).isEqualTo("write");
    }

    @Test
    void createAuthorizationBucket() {

        Organization organization = platformClient.createOrganizationClient().createOrganization(generateName("Auth Organization"));
        Bucket bucket = platformClient.createBucketClient().createBucket(generateName("Auth Bucket"), retentionRule(), organization);

        PermissionResource resource = new PermissionResource();
        resource.setOrgID(organization.getId());
        resource.setType(PermissionResourceType.BUCKET);
        resource.setId(bucket.getId());

        Permission readBucket = new Permission();
        readBucket.setResource(resource);
        readBucket.setAction(Permission.READ_ACTION);

        Permission writeBucket = new Permission();
        writeBucket.setResource(resource);
        writeBucket.setAction(Permission.WRITE_ACTION);

        List<Permission> permissions = new ArrayList<>();
        permissions.add(readBucket);
        permissions.add(writeBucket);

        Authorization authorization = authorizationClient.createAuthorization(organization, permissions);

        Assertions.assertThat(authorization.getPermissions()).hasSize(2);
        Assertions.assertThat(authorization.getPermissions().get(0).getResource().getId()).isEqualTo(bucket.getId());
        Assertions.assertThat(authorization.getPermissions().get(0).getResource().getType()).isEqualTo(PermissionResourceType.BUCKET);
        Assertions.assertThat(authorization.getPermissions().get(0).getAction()).isEqualTo("read");
        Assertions.assertThat(authorization.getPermissions().get(1).getResource().getId()).isEqualTo(bucket.getId());
        Assertions.assertThat(authorization.getPermissions().get(1).getResource().getType()).isEqualTo(PermissionResourceType.BUCKET);
        Assertions.assertThat(authorization.getPermissions().get(1).getAction()).isEqualTo("write");
    }

    @Test
    void findAuthorizationsByID() {

        Authorization authorization = authorizationClient.createAuthorization(organization, newPermissions());

        Authorization foundAuthorization = authorizationClient.findAuthorizationByID(authorization.getId());

        Assertions.assertThat(foundAuthorization).isNotNull();
        Assertions.assertThat(authorization.getId()).isEqualTo(foundAuthorization.getId());
        Assertions.assertThat(authorization.getToken()).isEqualTo(foundAuthorization.getToken());
        Assertions.assertThat(authorization.getUserID()).isEqualTo(foundAuthorization.getUserID());
        Assertions.assertThat(authorization.getUserName()).isEqualTo(foundAuthorization.getUserName());
        Assertions.assertThat(authorization.getStatus()).isEqualTo(foundAuthorization.getStatus());
    }

    @Test
    void findAuthorizationsByIDNull() {

        Authorization authorization = authorizationClient.findAuthorizationByID("020f755c3c082000");

        Assertions.assertThat(authorization).isNull();
    }

    @Test
    void findAuthorizations() {

        int size = authorizationClient.findAuthorizations().size();

        authorizationClient.createAuthorization(organization, newPermissions());

        List<Authorization> authorizations = authorizationClient.findAuthorizations();
        Assertions.assertThat(authorizations).hasSize(size + 1);
    }

    @Test
    void findAuthorizationsByUser() {

        int size = authorizationClient.findAuthorizationsByUser(user).size();

        authorizationClient.createAuthorization(organization, newPermissions());

        List<Authorization> authorizations = authorizationClient.findAuthorizationsByUser(user);
        Assertions.assertThat(authorizations).hasSize(size + 1);
    }

    @Test
    void findAuthorizationsByUserName() {

        int size = authorizationClient.findAuthorizationsByUserName(user.getName()).size();

        authorizationClient.createAuthorization(organization, newPermissions());

        List<Authorization> authorizations = authorizationClient.findAuthorizationsByUserName(user.getName());
        Assertions.assertThat(authorizations).hasSize(size + 1);
    }

    @Test
    void updateAuthorizationStatus() {

        PermissionResource resource = new PermissionResource();
        resource.setOrgID(organization.getId());
        resource.setType(PermissionResourceType.USER);

        Permission readUsers = new Permission();
        readUsers.setAction(Permission.READ_ACTION);
        readUsers.setResource(resource);

        List<Permission> permissions = new ArrayList<>();
        permissions.add(readUsers);

        Authorization authorization = authorizationClient.createAuthorization(organization, permissions);

        Assertions.assertThat(authorization.getStatus()).isEqualTo(Status.ACTIVE);

        authorization.setStatus(Status.INACTIVE);
        authorization = authorizationClient.updateAuthorization(authorization);

        Assertions.assertThat(authorization.getStatus()).isEqualTo(Status.INACTIVE);

        authorization.setStatus(Status.ACTIVE);
        authorization = authorizationClient.updateAuthorization(authorization);

        Assertions.assertThat(authorization.getStatus()).isEqualTo(Status.ACTIVE);
    }

    @Test
    void deleteAuthorization() {

        Authorization createdAuthorization = authorizationClient.createAuthorization(organization, newPermissions());
        Assertions.assertThat(createdAuthorization).isNotNull();

        Authorization foundAuthorization = authorizationClient.findAuthorizationByID(createdAuthorization.getId());
        Assertions.assertThat(foundAuthorization).isNotNull();

        // delete authorization
        authorizationClient.deleteAuthorization(createdAuthorization);

        foundAuthorization = authorizationClient.findAuthorizationByID(createdAuthorization.getId());
        Assertions.assertThat(foundAuthorization).isNull();
    }

    @Nonnull
    private List<Permission> newPermissions() {

        PermissionResource resource = new PermissionResource();
        resource.setOrgID(organization.getId());
        resource.setType(PermissionResourceType.USER);

        Permission permission = new Permission();
        permission.setAction(Permission.READ_ACTION);
        permission.setResource(resource);

        return Collections.singletonList(permission);
    }
}