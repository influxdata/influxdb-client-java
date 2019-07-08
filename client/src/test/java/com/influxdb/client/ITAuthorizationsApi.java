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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

import com.influxdb.client.domain.Authorization;
import com.influxdb.client.domain.Bucket;
import com.influxdb.client.domain.Organization;
import com.influxdb.client.domain.Permission;
import com.influxdb.client.domain.PermissionResource;
import com.influxdb.client.domain.User;
import com.influxdb.exceptions.NotFoundException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (17/09/2018 12:02)
 */
@RunWith(JUnitPlatform.class)
class ITAuthorizationsApi extends AbstractITClientTest {

    private static final Logger LOG = Logger.getLogger(ITAuthorizationsApi.class.getName());

    private AuthorizationsApi authorizationsApi;

    private User user;
    private Organization organization;

    @BeforeEach
    void setUp() {
        authorizationsApi = influxDBClient.getAuthorizationsApi();

        user = influxDBClient.getUsersApi().me();
        organization = findMyOrg();
    }

    @Test
    void createAuthorization() {

        PermissionResource userResource = new PermissionResource();
        userResource.setOrgID(organization.getId());
        userResource.setType(PermissionResource.TypeEnum.USERS);

        Permission readUsers = new Permission();
        readUsers.setAction(Permission.ActionEnum.READ);
        readUsers.setResource(userResource);

        PermissionResource orgResource = new PermissionResource();
        orgResource.setOrgID(organization.getId());
        orgResource.setType(PermissionResource.TypeEnum.ORGS);

        Permission writeOrganizations = new Permission();
        writeOrganizations.setAction(Permission.ActionEnum.WRITE);
        writeOrganizations.setResource(orgResource);

        List<Permission> permissions = new ArrayList<>();
        permissions.add(readUsers);
        permissions.add(writeOrganizations);

        Authorization authorization = authorizationsApi.createAuthorization(organization.getId(), permissions);

        LOG.log(Level.INFO, "Created authorization: {0}", authorization);

        Assertions.assertThat(authorization).isNotNull();
        Assertions.assertThat(authorization.getId()).isNotBlank();
        Assertions.assertThat(authorization.getToken()).isNotBlank();
        Assertions.assertThat(authorization.getUserID()).isEqualTo(user.getId());
        Assertions.assertThat(authorization.getUser()).isEqualTo(user.getName());
        Assertions.assertThat(authorization.getOrgID()).isEqualTo(organization.getId());
        Assertions.assertThat(authorization.getOrg()).isEqualTo(organization.getName());
        Assertions.assertThat(authorization.getStatus()).isEqualTo(Authorization.StatusEnum.ACTIVE);

        Assertions.assertThat(authorization.getPermissions()).hasSize(2);
        Assertions.assertThat(authorization.getPermissions().get(0).getResource().getType()).isEqualTo(PermissionResource.TypeEnum.USERS);
        Assertions.assertThat(authorization.getPermissions().get(0).getResource().getOrgID()).isEqualTo(organization.getId());
        Assertions.assertThat(authorization.getPermissions().get(0).getAction()).isEqualTo(Permission.ActionEnum.READ);

        Assertions.assertThat(authorization.getPermissions().get(1).getResource().getType()).isEqualTo(PermissionResource.TypeEnum.ORGS);
        Assertions.assertThat(authorization.getPermissions().get(1).getResource().getOrgID()).isEqualTo(organization.getId());
        Assertions.assertThat(authorization.getPermissions().get(1).getAction()).isEqualTo(Permission.ActionEnum.WRITE);

        Assertions.assertThat(authorization.getLinks()).isNotNull();
        Assertions.assertThat(authorization.getLinks().getSelf()).isEqualTo("/api/v2/authorizations/" + authorization.getId());
        Assertions.assertThat(authorization.getLinks().getUser()).isEqualTo("/api/v2/users/" + user.getId());
    }

    @Test
    void authorizationDescription() {

        PermissionResource resource = new PermissionResource();
        resource.setOrgID(organization.getId());
        resource.setType(PermissionResource.TypeEnum.SOURCES);

        Permission createSource = new Permission();
        createSource.setResource(resource);
        createSource.setAction(Permission.ActionEnum.WRITE);

        List<Permission> permissions = new ArrayList<>();
        permissions.add(createSource);

        Authorization authorization = new Authorization();
        authorization.setOrgID(organization.getId());
        authorization.setPermissions(permissions);
        authorization.setStatus(Authorization.StatusEnum.ACTIVE);
        authorization.setDescription("My description!");

        Authorization created = authorizationsApi.createAuthorization(authorization);

        Assertions.assertThat(created).isNotNull();
        Assertions.assertThat(created.getDescription()).isEqualTo("My description!");
    }

    @Test
    void createAuthorizationTask() {

        PermissionResource resource = new PermissionResource();
        resource.setOrgID(organization.getId());
        resource.setType(PermissionResource.TypeEnum.TASKS);

        Permission createTask = new Permission();
        createTask.setResource(resource);
        createTask.setAction(Permission.ActionEnum.READ);

        Permission deleteTask = new Permission();
        deleteTask.setResource(resource);
        deleteTask.setAction(Permission.ActionEnum.WRITE);

        List<Permission> permissions = new ArrayList<>();
        permissions.add(createTask);
        permissions.add(deleteTask);

        Authorization authorization = authorizationsApi.createAuthorization(organization, permissions);

        Assertions.assertThat(authorization.getPermissions()).hasSize(2);
        Assertions.assertThat(authorization.getPermissions().get(0).getResource().getType()).isEqualTo(PermissionResource.TypeEnum.TASKS);
        Assertions.assertThat(authorization.getPermissions().get(0).getResource().getOrgID()).isEqualTo(organization.getId());
        Assertions.assertThat(authorization.getPermissions().get(0).getAction()).isEqualTo(Permission.ActionEnum.READ);
        Assertions.assertThat(authorization.getPermissions().get(1).getResource().getType()).isEqualTo(PermissionResource.TypeEnum.TASKS);
        Assertions.assertThat(authorization.getPermissions().get(1).getResource().getOrgID()).isEqualTo(organization.getId());
        Assertions.assertThat(authorization.getPermissions().get(1).getAction()).isEqualTo(Permission.ActionEnum.WRITE);
    }

    @Test
    void createAuthorizationBucket() {

        Organization organization = influxDBClient.getOrganizationsApi().createOrganization(generateName("Auth Organization"));
        Bucket bucket = influxDBClient.getBucketsApi().createBucket(generateName("Auth Bucket"), retentionRule(), organization);

        PermissionResource resource = new PermissionResource();
        resource.setOrgID(organization.getId());
        resource.setType(PermissionResource.TypeEnum.BUCKETS);
        resource.setId(bucket.getId());

        Permission readBucket = new Permission();
        readBucket.setResource(resource);
        readBucket.setAction(Permission.ActionEnum.READ);

        Permission writeBucket = new Permission();
        writeBucket.setResource(resource);
        writeBucket.setAction(Permission.ActionEnum.WRITE);

        List<Permission> permissions = new ArrayList<>();
        permissions.add(readBucket);
        permissions.add(writeBucket);

        Authorization authorization = authorizationsApi.createAuthorization(organization, permissions);

        Assertions.assertThat(authorization.getPermissions()).hasSize(2);
        Assertions.assertThat(authorization.getPermissions().get(0).getResource().getId()).isEqualTo(bucket.getId());
        Assertions.assertThat(authorization.getPermissions().get(0).getResource().getType()).isEqualTo(PermissionResource.TypeEnum.BUCKETS);
        Assertions.assertThat(authorization.getPermissions().get(0).getAction()).isEqualTo(Permission.ActionEnum.READ);
        Assertions.assertThat(authorization.getPermissions().get(1).getResource().getId()).isEqualTo(bucket.getId());
        Assertions.assertThat(authorization.getPermissions().get(1).getResource().getType()).isEqualTo(PermissionResource.TypeEnum.BUCKETS);
        Assertions.assertThat(authorization.getPermissions().get(1).getAction()).isEqualTo(Permission.ActionEnum.WRITE);
    }

    @Test
    void findAuthorizationsByID() {

        Authorization authorization = authorizationsApi.createAuthorization(organization, newPermissions());

        Authorization foundAuthorization = authorizationsApi.findAuthorizationByID(authorization.getId());

        Assertions.assertThat(foundAuthorization).isNotNull();
        Assertions.assertThat(authorization.getId()).isEqualTo(foundAuthorization.getId());
        Assertions.assertThat(authorization.getToken()).isEqualTo(foundAuthorization.getToken());
        Assertions.assertThat(authorization.getUserID()).isEqualTo(foundAuthorization.getUserID());
        Assertions.assertThat(authorization.getUser()).isEqualTo(foundAuthorization.getUser());
        Assertions.assertThat(authorization.getStatus()).isEqualTo(foundAuthorization.getStatus());
    }

    @Test
    void findAuthorizationsByIDNull() {

        Assertions.assertThatThrownBy(() -> authorizationsApi.findAuthorizationByID("020f755c3c082000"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("authorization not found");
    }

    @Test
    void findAuthorizations() {

        int size = authorizationsApi.findAuthorizations().size();

        authorizationsApi.createAuthorization(organization, newPermissions());

        List<Authorization> authorizations = authorizationsApi.findAuthorizations();
        Assertions.assertThat(authorizations).hasSize(size + 1);
    }

    @Test
    void findAuthorizationsByUser() {

        int size = authorizationsApi.findAuthorizationsByUser(user).size();

        authorizationsApi.createAuthorization(organization, newPermissions());

        List<Authorization> authorizations = authorizationsApi.findAuthorizationsByUser(user);
        Assertions.assertThat(authorizations).hasSize(size + 1);
    }

    @Test
    void findAuthorizationsByUserName() {

        int size = authorizationsApi.findAuthorizationsByUserName(user.getName()).size();

        authorizationsApi.createAuthorization(organization, newPermissions());

        List<Authorization> authorizations = authorizationsApi.findAuthorizationsByUserName(user.getName());
        Assertions.assertThat(authorizations).hasSize(size + 1);
    }

    @Test
    void findAuthorizationsByOrg() {

        int size = authorizationsApi.findAuthorizationsByOrg(organization).size();

        authorizationsApi.createAuthorization(organization, newPermissions());

        List<Authorization> authorizations = authorizationsApi.findAuthorizationsByOrg(organization);
        Assertions.assertThat(authorizations).hasSize(size + 1);
    }

    @Test
    void findAuthorizationsByOrgNotFound() {

        List<Authorization> authorizationsByOrgID = authorizationsApi.findAuthorizationsByOrgID("ffffffffffffffff");

        Assertions.assertThat(authorizationsByOrgID).isEmpty();
    }

    @Test
    void updateAuthorizationStatus() {

        PermissionResource resource = new PermissionResource();
        resource.setOrgID(organization.getId());
        resource.setType(PermissionResource.TypeEnum.USERS);

        Permission readUsers = new Permission();
        readUsers.setAction(Permission.ActionEnum.READ);
        readUsers.setResource(resource);

        List<Permission> permissions = new ArrayList<>();
        permissions.add(readUsers);

        Authorization authorization = authorizationsApi.createAuthorization(organization, permissions);

        Assertions.assertThat(authorization.getStatus()).isEqualTo(Authorization.StatusEnum.ACTIVE);

        authorization.setStatus(Authorization.StatusEnum.INACTIVE);
        authorization = authorizationsApi.updateAuthorization(authorization);

        Assertions.assertThat(authorization.getStatus()).isEqualTo(Authorization.StatusEnum.INACTIVE);

        authorization.setStatus(Authorization.StatusEnum.ACTIVE);
        authorization = authorizationsApi.updateAuthorization(authorization);

        Assertions.assertThat(authorization.getStatus()).isEqualTo(Authorization.StatusEnum.ACTIVE);
    }

    @Test
    void deleteAuthorization() {

        Authorization createdAuthorization = authorizationsApi.createAuthorization(organization, newPermissions());
        Assertions.assertThat(createdAuthorization).isNotNull();

        Authorization foundAuthorization = authorizationsApi.findAuthorizationByID(createdAuthorization.getId());
        Assertions.assertThat(foundAuthorization).isNotNull();

        // delete authorization
        authorizationsApi.deleteAuthorization(createdAuthorization);

        Assertions.assertThatThrownBy(() -> authorizationsApi.findAuthorizationByID(createdAuthorization.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("authorization not found");
    }

    @Test
    void cloneAuthorization() {

        Authorization source = authorizationsApi.createAuthorization(organization, newPermissions());

        Authorization cloned = authorizationsApi.cloneAuthorization(source.getId());

        Assertions.assertThat(cloned.getToken()).isNotBlank();
        Assertions.assertThat(cloned.getToken()).isNotEqualTo(source.getToken());
        Assertions.assertThat(cloned.getUserID()).isEqualTo(source.getUserID());
        Assertions.assertThat(cloned.getUser()).isEqualTo(source.getUser());
        Assertions.assertThat(cloned.getOrgID()).isEqualTo(organization.getId());
        Assertions.assertThat(cloned.getOrg()).isEqualTo(organization.getName());
        Assertions.assertThat(cloned.getStatus()).isEqualTo(Authorization.StatusEnum.ACTIVE);
        Assertions.assertThat(cloned.getDescription()).isEqualTo(source.getDescription());
        Assertions.assertThat(cloned.getPermissions()).hasSize(1);
        Assertions.assertThat(cloned.getPermissions().get(0).getAction()).isEqualTo(Permission.ActionEnum.READ);
        Assertions.assertThat(cloned.getPermissions().get(0).getResource().getType()).isEqualTo(PermissionResource.TypeEnum.USERS);
        Assertions.assertThat(cloned.getPermissions().get(0).getResource().getOrgID()).isEqualTo(organization.getId());
    }

    @Test
    void cloneAuthorizationNotFound() {
        
        Assertions.assertThatThrownBy(() -> authorizationsApi.cloneAuthorization("020f755c3c082000"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("authorization not found");
    }

    @Nonnull
    private List<Permission> newPermissions() {

        PermissionResource resource = new PermissionResource();
        resource.setOrgID(organization.getId());
        resource.setType(PermissionResource.TypeEnum.USERS);

        Permission permission = new Permission();
        permission.setAction(Permission.ActionEnum.READ);
        permission.setResource(resource);

        return Collections.singletonList(permission);
    }
}