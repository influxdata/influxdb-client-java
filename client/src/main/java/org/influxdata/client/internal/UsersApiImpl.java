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
package org.influxdata.client.internal;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.influxdata.Arguments;
import org.influxdata.client.UsersApi;
import org.influxdata.client.domain.FindOptions;
import org.influxdata.client.domain.OperationLog;
import org.influxdata.client.domain.OperationLogs;
import org.influxdata.client.domain.User;
import org.influxdata.client.domain.Users;
import org.influxdata.exceptions.NotFoundException;
import org.influxdata.exceptions.UnauthorizedException;

import com.google.gson.Gson;
import okhttp3.Credentials;
import org.json.JSONObject;
import retrofit2.Call;

/**
 * @author Jakub Bednar (bednar@github) (11/09/2018 10:16)
 */
final class UsersApiImpl extends AbstractInfluxDBRestClient implements UsersApi {

    private static final Logger LOG = Logger.getLogger(UsersApiImpl.class.getName());

    UsersApiImpl(@Nonnull final InfluxDBService influxDBService, @Nonnull final Gson gson) {

        super(influxDBService, gson);
    }

    @Nullable
    @Override
    public User findUserByID(@Nonnull final String userID) {

        Arguments.checkNonEmpty(userID, "User ID");

        Call<User> user = influxDBService.findUserByID(userID);

        return execute(user, NotFoundException.class);
    }

    @Nonnull
    @Override
    public List<User> findUsers() {

        Call<Users> usersCall = influxDBService.findUsers();

        Users users = execute(usersCall);
        LOG.log(Level.FINEST, "findUsers found: {0}", users);

        return users.getUsers();
    }

    @Nonnull
    @Override
    public User createUser(@Nonnull final String name) {

        Arguments.checkNonEmpty(name, "User name");

        User user = new User();
        user.setName(name);

        return createUser(user);
    }

    @Nonnull
    @Override
    public User createUser(@Nonnull final User user) {

        Arguments.checkNotNull(user, "User");

        String json = gson.toJson(user);

        Call<User> call = influxDBService.createUser(createBody(json));

        return execute(call);
    }

    @Nonnull
    @Override
    public User updateUser(@Nonnull final User user) {

        Arguments.checkNotNull(user, "User");

        String json = gson.toJson(user);

        Call<User> userCall = influxDBService.updateUser(user.getId(), createBody(json));

        return execute(userCall);
    }

    @Nonnull
    @Override
    public User updateUserPassword(@Nonnull final User user,
                                   @Nonnull final String oldPassword,
                                   @Nonnull final String newPassword) {

        Arguments.checkNotNull(user, "User");
        Arguments.checkNotNull(oldPassword, "old password");
        Arguments.checkNotNull(newPassword, "new password");

        return updateUserPassword(user.getId(), user.getName(), oldPassword, newPassword);
    }

    @Nonnull
    @Override
    public User updateUserPassword(@Nonnull final String userID,
                                   @Nonnull final String oldPassword,
                                   @Nonnull final String newPassword) {

        Arguments.checkNotNull(userID, "User ID");
        Arguments.checkNotNull(oldPassword, "old password");
        Arguments.checkNotNull(newPassword, "new password");

        Call<User> userByID = influxDBService.findUserByID(userID);
        User user = execute(userByID);

        return updateUserPassword(userID, user.getName(), oldPassword, newPassword);
    }

    @Override
    public void deleteUser(@Nonnull final User user) {

        Arguments.checkNotNull(user, "User");

        deleteUser(user.getId());
    }

    @Override
    public void deleteUser(@Nonnull final String userID) {

        Arguments.checkNonEmpty(userID, "User ID");

        Call<Void> call = influxDBService.deleteUser(userID);
        execute(call);
    }

    @Nonnull
    @Override
    public User cloneUser(@Nonnull final String clonedName, @Nonnull final String userID) {

        Arguments.checkNonEmpty(clonedName, "clonedName");
        Arguments.checkNonEmpty(userID, "userID");

        User user = findUserByID(userID);
        if (user == null) {
            throw new IllegalStateException("NotFound User with ID: " + userID);
        }

        return cloneUser(clonedName, user);
    }

    @Nonnull
    @Override
    public User cloneUser(@Nonnull final String clonedName, @Nonnull final User user) {

        Arguments.checkNonEmpty(clonedName, "clonedName");
        Arguments.checkNotNull(user, "User");

        User cloned = new User();
        cloned.setName(clonedName);

        return createUser(cloned);
    }

    @Nullable
    @Override
    public User me() {

        Call<User> call = influxDBService.me();

        return execute(call, UnauthorizedException.class);
    }

    @Nullable
    @Override
    public User meUpdatePassword(@Nonnull final String oldPassword, @Nonnull final String newPassword) {

        Arguments.checkNotNull(oldPassword, "old password");
        Arguments.checkNotNull(newPassword, "new password");

        User user = me();
        if (user == null) {
            LOG.warning("User is not authenticated.");

            return null;
        }

        String json = new JSONObject().put("password", newPassword).toString();

        String credentials = Credentials
                .basic(user.getName(), oldPassword);

        Call<User> call = influxDBService.mePassword(credentials, createBody(json));

        return execute(call, UnauthorizedException.class);
    }

    @Nonnull
    @Override
    public List<OperationLog> findUserLogs(@Nonnull final User user) {

        Arguments.checkNotNull(user, "User");

        return findUserLogs(user.getId());
    }

    @Nonnull
    @Override
    public List<OperationLog> findUserLogs(@Nonnull final String userID) {

        Arguments.checkNonEmpty(userID, "User ID");

        return findUserLogs(userID, new FindOptions()).getLogs();
    }

    @Nonnull
    @Override
    public OperationLogs findUserLogs(@Nonnull final User user, @Nonnull final FindOptions findOptions) {

        Arguments.checkNotNull(user, "User");
        Arguments.checkNotNull(findOptions, "findOptions");

        return findUserLogs(user.getId(), findOptions);
    }

    @Nonnull
    @Override
    public OperationLogs findUserLogs(@Nonnull final String userID, @Nonnull final FindOptions findOptions) {

        Arguments.checkNonEmpty(userID, "User ID");
        Arguments.checkNotNull(findOptions, "findOptions");

        Call<OperationLogs> call = influxDBService.findUserLogs(userID, createQueryMap(findOptions));

        return getOperationLogEntries(call);
    }

    @Nonnull
    private User updateUserPassword(@Nonnull final String userID,
                                    @Nonnull final String userName,
                                    @Nonnull final String oldPassword,
                                    @Nonnull final String newPassword) {

        Arguments.checkNotNull(userID, "User ID");
        Arguments.checkNotNull(userName, "Username");
        Arguments.checkNotNull(oldPassword, "old password");
        Arguments.checkNotNull(newPassword, "new password");

        String credentials = Credentials
                .basic(userName, oldPassword);

        String json = new JSONObject().put("password", newPassword).toString();

        Call<User> call = influxDBService.updateUserPassword(userID, credentials, createBody(json));

        return execute(call);
    }
}