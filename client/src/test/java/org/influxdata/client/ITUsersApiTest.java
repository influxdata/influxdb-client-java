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

import java.time.OffsetDateTime;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import org.influxdata.client.domain.OperationLog;
import org.influxdata.client.domain.OperationLogs;
import org.influxdata.client.domain.User;
import org.influxdata.exceptions.NotFoundException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (11/09/2018 11:26)
 */
@RunWith(JUnitPlatform.class)
class ITUsersApiTest extends AbstractITClientTest {

    private static final Logger LOG = Logger.getLogger(ITUsersApiTest.class.getName());

    private UsersApi usersApi;

    @BeforeEach
    void setUp() {

        usersApi = influxDBClient.getUsersApi();
    }

    @Test
    void createUser() {

        String userName = generateName("John Ryzen");

        User user = usersApi.createUser(userName);

        LOG.info("Created User: " + user);

        Assertions.assertThat(user).isNotNull();
        Assertions.assertThat(user.getId()).isNotBlank();
        Assertions.assertThat(user.getName()).isEqualTo(userName);
        Assertions.assertThat(user.getLinks().getLogs()).isEqualTo("/api/v2/users/" + user.getId() + "/logs");
        Assertions.assertThat(user.getLinks().getSelf()).isEqualTo("/api/v2/users/" + user.getId());
    }

    @Test
    void findUserByID() {

        String userName = generateName("John Ryzen");

        User user = usersApi.createUser(userName);

        User userByID = usersApi.findUserByID(user.getId());

        Assertions.assertThat(userByID).isNotNull();
        Assertions.assertThat(userByID.getId()).isEqualTo(user.getId());
        Assertions.assertThat(userByID.getName()).isEqualTo(user.getName());
    }

    @Test
    void findUserByIDNull() {

        User user = usersApi.findUserByID("020f755c3c082000");

        Assertions.assertThat(user).isNull();
    }

    @Test
    void findUsers() {

        int size = usersApi.findUsers().size();

        usersApi.createUser(generateName("John Ryzen"));

        List<User> users = usersApi.findUsers();
        Assertions.assertThat(users).hasSize(size + 1);
    }

    @Test
    void deleteUser() {

        User createdUser = usersApi.createUser(generateName("John Ryzen"));
        Assertions.assertThat(createdUser).isNotNull();

        User foundUser = usersApi.findUserByID(createdUser.getId());
        Assertions.assertThat(foundUser).isNotNull();

        // delete user
        usersApi.deleteUser(createdUser);

        foundUser = usersApi.findUserByID(createdUser.getId());
        Assertions.assertThat(foundUser).isNull();
    }

    @Test
    void updateUser() {

        User createdUser = usersApi.createUser(generateName("John Ryzen"));
        createdUser.setName("Tom Push");

        User updatedUser = usersApi.updateUser(createdUser);

        Assertions.assertThat(updatedUser).isNotNull();
        Assertions.assertThat(updatedUser.getId()).isEqualTo(createdUser.getId());
        Assertions.assertThat(updatedUser.getName()).isEqualTo("Tom Push");
    }

    @Test
    void meAuthenticated() {

        User me = usersApi.me();

        Assertions.assertThat(me).isNotNull();
        Assertions.assertThat(me.getName()).isEqualTo("my-user");
    }

    @Test
    void meNotAuthenticated() throws Exception {

        influxDBClient.close();
        
        User me = usersApi.me();

        Assertions.assertThat(me).isNull();
    }

    @Test
    @Tag("basic_auth")
    void updateMePassword() {

        User user = usersApi.meUpdatePassword("my-password", "my-password");

        Assertions.assertThat(user).isNotNull();
        Assertions.assertThat(user.getName()).isEqualTo("my-user");
    }

    @Test
    void updateMePasswordNotAuthenticate() throws Exception {

        influxDBClient.close();

        User user = usersApi.meUpdatePassword("my-password", "my-password");

        Assertions.assertThat(user).isNull();
    }

    @Test
    @Tag("basic_auth")
    void updatePassword() {

        User user = usersApi.me();
        Assertions.assertThat(user).isNotNull();

        User updatedUser = usersApi.updateUserPassword(user, "my-password", "my-password");

        Assertions.assertThat(updatedUser).isNotNull();
        Assertions.assertThat(updatedUser.getName()).isEqualTo(user.getName());
        Assertions.assertThat(updatedUser.getId()).isEqualTo(user.getId());
    }

    //TODO set user password -> https://github.com/influxdata/influxdb/issues/11590
    @Test
    @Disabled
    void createNewUserAndSetPassword() throws Exception {

        User myNewUser = usersApi.createUser(generateName("My new user"));

        influxDBClient.close();

        //TODO set user password -> https://github.com/influxdata/influxdb/issues/11590
        influxDBClient = InfluxDBClientFactory.create(influxDB_URL, "my-user", "my-password".toCharArray());
        usersApi = influxDBClient.getUsersApi();

        User user = usersApi.updateUserPassword(myNewUser, "", "strong-password");

        Assertions.assertThat(myNewUser.getId()).isEqualTo(user.getId());
    }

    @Test
    @Tag("basic_auth")
    void updatePasswordNotFound() {

        Assertions.assertThatThrownBy(() -> usersApi.updateUserPassword("020f755c3c082000", "", "new-password"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("user not found");
    }

    @Test
    @Tag("basic_auth")
    void updatePasswordById() {

        User user = usersApi.me();
        Assertions.assertThat(user).isNotNull();

        User updatedUser = usersApi.updateUserPassword(user.getId(), "my-password", "my-password");

        Assertions.assertThat(updatedUser).isNotNull();
        Assertions.assertThat(updatedUser.getName()).isEqualTo(user.getName());
        Assertions.assertThat(updatedUser.getId()).isEqualTo(user.getId());
    }

    @Test
    void findUserLogs() {

        OffsetDateTime now = OffsetDateTime.now();

        User user = usersApi.me();
        Assertions.assertThat(user).isNotNull();

        usersApi.updateUser(user);

        List<OperationLog> userLogs = usersApi.findUserLogs(user);
        Assertions.assertThat(userLogs).isNotEmpty();
        Assertions.assertThat(userLogs.get(userLogs.size() - 1).getDescription()).isEqualTo("User Updated");
        Assertions.assertThat(userLogs.get(userLogs.size() - 1).getTime()).isAfter(now);
        //TODO https://github.com/influxdata/influxdb/issues/12544
        // Assertions.assertThat(userLogs.get(userLogs.size() - 1).get()).isEqualTo(user.getId());
    }

    @Test
    void findUserLogsNotFound() {
        List<OperationLog> userLogs = usersApi.findUserLogs("020f755c3c082000");
        Assertions.assertThat(userLogs).isEmpty();
    }

    @Test
    void findUserLogsPaging() {

        User user = usersApi.createUser(generateName("Thomas Boot"));

        IntStream
                .range(0, 19)
                .forEach(value -> {

                    user.setName(value + "_" + user.getName());
                    usersApi.updateUser(user);
                });

        List<OperationLog> logs = usersApi.findUserLogs(user);

        Assertions.assertThat(logs).hasSize(20);
        Assertions.assertThat(logs.get(0).getDescription()).isEqualTo("User Created");
        Assertions.assertThat(logs.get(19).getDescription()).isEqualTo("User Updated");

        FindOptions findOptions = new FindOptions();
        findOptions.setLimit(5);
        findOptions.setOffset(0);

        OperationLogs entries = usersApi.findUserLogs(user, findOptions);

        Assertions.assertThat(entries.getLogs()).hasSize(5);
        Assertions.assertThat(entries.getLogs().get(0).getDescription()).isEqualTo("User Created");
        Assertions.assertThat(entries.getLogs().get(1).getDescription()).isEqualTo("User Updated");
        Assertions.assertThat(entries.getLogs().get(2).getDescription()).isEqualTo("User Updated");
        Assertions.assertThat(entries.getLogs().get(3).getDescription()).isEqualTo("User Updated");
        Assertions.assertThat(entries.getLogs().get(4).getDescription()).isEqualTo("User Updated");

        //TODO isNotNull FindOptions also in Log API?
        findOptions.setOffset(findOptions.getOffset() + 5);
        Assertions.assertThat(entries.getLinks().getNext()).isNull();

        entries = usersApi.findUserLogs(user, findOptions);
        Assertions.assertThat(entries.getLogs()).hasSize(5);
        Assertions.assertThat(entries.getLogs().get(0).getDescription()).isEqualTo("User Updated");
        Assertions.assertThat(entries.getLogs().get(1).getDescription()).isEqualTo("User Updated");
        Assertions.assertThat(entries.getLogs().get(2).getDescription()).isEqualTo("User Updated");
        Assertions.assertThat(entries.getLogs().get(3).getDescription()).isEqualTo("User Updated");
        Assertions.assertThat(entries.getLogs().get(4).getDescription()).isEqualTo("User Updated");

        findOptions.setOffset(findOptions.getOffset() + 5);
        Assertions.assertThat(entries.getLinks().getNext()).isNull();

        entries = usersApi.findUserLogs(user, findOptions);
        Assertions.assertThat(entries.getLogs().get(0).getDescription()).isEqualTo("User Updated");
        Assertions.assertThat(entries.getLogs().get(1).getDescription()).isEqualTo("User Updated");
        Assertions.assertThat(entries.getLogs().get(2).getDescription()).isEqualTo("User Updated");
        Assertions.assertThat(entries.getLogs().get(3).getDescription()).isEqualTo("User Updated");
        Assertions.assertThat(entries.getLogs().get(4).getDescription()).isEqualTo("User Updated");

        findOptions.setOffset(findOptions.getOffset() + 5);
        Assertions.assertThat(entries.getLinks().getNext()).isNull();

        entries = usersApi.findUserLogs(user, findOptions);
        Assertions.assertThat(entries.getLogs()).hasSize(5);
        Assertions.assertThat(entries.getLogs().get(0).getDescription()).isEqualTo("User Updated");
        Assertions.assertThat(entries.getLogs().get(1).getDescription()).isEqualTo("User Updated");
        Assertions.assertThat(entries.getLogs().get(2).getDescription()).isEqualTo("User Updated");
        Assertions.assertThat(entries.getLogs().get(3).getDescription()).isEqualTo("User Updated");
        Assertions.assertThat(entries.getLogs().get(4).getDescription()).isEqualTo("User Updated");

        findOptions.setOffset(findOptions.getOffset() + 5);
        Assertions.assertThat(entries.getLinks().getNext()).isNull();

        entries = usersApi.findUserLogs(user, findOptions);
        Assertions.assertThat(entries.getLogs()).hasSize(0);
        Assertions.assertThat(entries.getLinks().getNext()).isNull();

        // order
        findOptions = new FindOptions();
        findOptions.setDescending(false);

        entries = usersApi.findUserLogs(user, findOptions);

        Assertions.assertThat(entries.getLogs()).hasSize(20);
        Assertions.assertThat(entries.getLogs().get(19).getDescription()).isEqualTo("User Updated");
        Assertions.assertThat(entries.getLogs().get(0).getDescription()).isEqualTo("User Created");
    }

    @Test
    void cloneUser() {

        User source = usersApi.createUser(generateName("John Ryzen"));

        String name = generateName("cloned");

        User cloned = usersApi.cloneUser(name, source.getId());

        Assertions.assertThat(cloned.getName()).isEqualTo(name);
    }

    @Test
    void cloneUserNotFound() {
        Assertions.assertThatThrownBy(() -> usersApi.cloneUser(generateName("cloned"), "020f755c3c082000"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("NotFound User with ID: 020f755c3c082000");
    }
}