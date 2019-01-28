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
package org.influxdata.platform.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.influxdata.platform.Arguments;
import org.influxdata.platform.UserClient;
import org.influxdata.platform.domain.OperationLogEntry;
import org.influxdata.platform.domain.OperationLogResponse;
import org.influxdata.platform.domain.User;
import org.influxdata.platform.domain.Users;
import org.influxdata.platform.error.rest.NotFoundException;
import org.influxdata.platform.error.rest.UnauthorizedException;
import org.influxdata.platform.rest.AbstractRestClient;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import okhttp3.Credentials;
import org.json.JSONObject;
import retrofit2.Call;

/**
 * @author Jakub Bednar (bednar@github) (11/09/2018 10:16)
 */
final class UserClientImpl extends AbstractRestClient implements UserClient {

    private static final Logger LOG = Logger.getLogger(UserClientImpl.class.getName());

    private final PlatformService platformService;
    private final JsonAdapter<User> adapter;

    UserClientImpl(@Nonnull final PlatformService platformService, @Nonnull final Moshi moshi) {

        Arguments.checkNotNull(platformService, "PlatformService");
        Arguments.checkNotNull(moshi, "Moshi to create adapter");

        this.platformService = platformService;
        this.adapter = moshi.adapter(User.class);
    }

    @Nullable
    @Override
    public User findUserByID(@Nonnull final String userID) {

        Arguments.checkNonEmpty(userID, "User ID");

        Call<User> user = platformService.findUserByID(userID);

        return execute(user, NotFoundException.class);
    }

    @Nonnull
    @Override
    public List<User> findUsers() {

        Call<Users> usersCall = platformService.findUsers();

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

        String json = adapter.toJson(user);

        Call<User> call = platformService.createUser(createBody(json));

        return execute(call);
    }

    @Nonnull
    @Override
    public User updateUser(@Nonnull final User user) {

        Arguments.checkNotNull(user, "User");

        String json = adapter.toJson(user);

        Call<User> userCall = platformService.updateUser(user.getId(), createBody(json));

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

    @Nullable
    @Override
    public User updateUserPassword(@Nonnull final String userID,
                                   @Nonnull final String oldPassword,
                                   @Nonnull final String newPassword) {

        Arguments.checkNotNull(userID, "User ID");
        Arguments.checkNotNull(oldPassword, "old password");
        Arguments.checkNotNull(newPassword, "new password");

        User user = findUserByID(userID);
        if (user == null) {

            LOG.log(Level.WARNING, "User with id: {} not found.", userID);
            return null;
        }

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

        Call<Void> call = platformService.deleteUser(userID);
        execute(call);
    }

    @Nullable
    @Override
    public User me() {

        Call<User> call = platformService.me();

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

        Call<User> call = platformService.mePassword(credentials, createBody(json));

        return execute(call, UnauthorizedException.class);
    }

    @Nonnull
    @Override
    public List<OperationLogEntry> findUserLogs(@Nonnull final User user) {

        Arguments.checkNotNull(user, "User");

        return findUserLogs(user.getId());
    }

    @Nonnull
    @Override
    public List<OperationLogEntry> findUserLogs(@Nonnull final String userID) {

        Call<OperationLogResponse> logsCall = platformService.findUserLogs(userID);

        //TODO https://github.com/influxdata/influxdb/issues/11632
        OperationLogResponse logResponse = execute(logsCall, "oplog not found");
        if (logResponse == null) {
            return new ArrayList<>();
        }

        LOG.log(Level.FINEST, "findUserLogs found: {0}", logResponse);

        return logResponse.getLog();
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

        Call<User> call = platformService.updateUserPassword(userID, credentials, createBody(json));

        return execute(call);
    }
}