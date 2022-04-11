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

import java.util.List;
import java.util.logging.Logger;

import com.influxdb.client.domain.User;
import com.influxdb.exceptions.ForbiddenException;
import com.influxdb.exceptions.NotFoundException;
import com.influxdb.exceptions.UnauthorizedException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.platform.suite.api.Suite;

/**
 * @author Jakub Bednar (bednar@github) (11/09/2018 11:26)
 */
@Suite
class ITUsersApi extends AbstractITClientTest {

    private static final Logger LOG = Logger.getLogger(ITUsersApi.class.getName());

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

        Assertions.assertThatThrownBy(() -> usersApi.findUserByID("020f755c3c082000"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("user not found");
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

        Assertions.assertThatThrownBy(() -> usersApi.findUserByID(createdUser.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("user not found");
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
    void meNotAuthenticated() {

        influxDBClient.close();

        Assertions.assertThatThrownBy(() -> usersApi.me())
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("unauthorized access");
    }

    @Test
    @Tag("basic_auth")
    @Disabled("TODO not implemented set password https://github.com/influxdata/influxdb/pull/15981")
    void updateMePassword() {

        usersApi.meUpdatePassword("my-password", "my-password");
    }

    @Test
    @Tag("basic_auth")
    @Disabled("TODO not implemented set password https://github.com/influxdata/influxdb/pull/15981")
    void updateMePasswordWrongPassword() {

        Assertions.assertThatThrownBy(() -> usersApi.meUpdatePassword("my-password-wrong", "my-password-new"))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("your username or password is incorrect");
    }

    @Test
    @Tag("basic_auth")
    @Disabled("TODO not implemented set password https://github.com/influxdata/influxdb/pull/15981")
    void updatePassword() {

        User user = usersApi.me();
        Assertions.assertThat(user).isNotNull();

        usersApi.updateUserPassword(user, "my-password", "my-password");
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

        usersApi.updateUserPassword(myNewUser, "", "strong-password");
    }

    @Test
    @Tag("basic_auth")
    @Disabled("TODO not implemented set password https://github.com/influxdata/influxdb/pull/15981")
    void updatePasswordNotFound() {

        Assertions.assertThatThrownBy(() -> usersApi.updateUserPassword("020f755c3c082000", "", "new-password"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("user not found");
    }

    @Test
    @Tag("basic_auth")
    @Disabled("TODO not implemented set password https://github.com/influxdata/influxdb/pull/15981")
    void updatePasswordById() {

        User user = usersApi.me();
        Assertions.assertThat(user).isNotNull();

        usersApi.updateUserPassword(user.getId(), "my-password", "my-password");
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
                .isInstanceOf(NotFoundException.class)
                .hasMessage("user not found");
    }
}