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
package com.influxdb.client.internal;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

import com.influxdb.Arguments;
import com.influxdb.client.FindOptions;
import com.influxdb.client.UsersApi;
import com.influxdb.client.domain.OperationLog;
import com.influxdb.client.domain.OperationLogs;
import com.influxdb.client.domain.PasswordResetBody;
import com.influxdb.client.domain.User;
import com.influxdb.client.domain.Users;
import com.influxdb.client.service.UsersService;
import com.influxdb.internal.AbstractRestClient;

import okhttp3.Credentials;
import retrofit2.Call;

/**
 * @author Jakub Bednar (bednar@github) (11/09/2018 10:16)
 */
final class UsersApiImpl extends AbstractRestClient implements UsersApi {

    private static final Logger LOG = Logger.getLogger(UsersApiImpl.class.getName());

    private final UsersService service;

    UsersApiImpl(@Nonnull final UsersService service) {

        Arguments.checkNotNull(service, "service");

        this.service = service;
    }

    @Nonnull
    @Override
    public User findUserByID(@Nonnull final String userID) {

        Arguments.checkNonEmpty(userID, "User ID");

        Call<User> user = service.getUsersID(userID, null);

        return execute(user);
    }

    @Nonnull
    @Override
    public List<User> findUsers() {

        Call<Users> usersCall = service.getUsers(null);

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

        Call<User> call = service.postUsers(user, null);

        return execute(call);
    }

    @Nonnull
    @Override
    public User updateUser(@Nonnull final User user) {

        Arguments.checkNotNull(user, "User");

        Call<User> userCall = service.patchUsersID(user.getId(), user, null);

        return execute(userCall);
    }

    @Override
    public void updateUserPassword(@Nonnull final User user,
                                   @Nonnull final String oldPassword,
                                   @Nonnull final String newPassword) {

        Arguments.checkNotNull(user, "User");
        Arguments.checkNotNull(oldPassword, "old password");
        Arguments.checkNotNull(newPassword, "new password");

        updateUserPassword(user.getId(), user.getName(), oldPassword, newPassword);
    }

    @Override
    public void updateUserPassword(@Nonnull final String userID,
                                   @Nonnull final String oldPassword,
                                   @Nonnull final String newPassword) {

        Arguments.checkNotNull(userID, "User ID");
        Arguments.checkNotNull(oldPassword, "old password");
        Arguments.checkNotNull(newPassword, "new password");

        Call<User> userByID = service.getUsersID(userID, null);
        User user = execute(userByID);

        updateUserPassword(userID, user.getName(), oldPassword, newPassword);
    }

    @Override
    public void deleteUser(@Nonnull final User user) {

        Arguments.checkNotNull(user, "User");

        deleteUser(user.getId());
    }

    @Override
    public void deleteUser(@Nonnull final String userID) {

        Arguments.checkNonEmpty(userID, "User ID");

        Call<Void> call = service.deleteUsersID(userID, null);
        execute(call);
    }

    @Nonnull
    @Override
    public User cloneUser(@Nonnull final String clonedName, @Nonnull final String userID) {

        Arguments.checkNonEmpty(clonedName, "clonedName");
        Arguments.checkNonEmpty(userID, "userID");

        User user = findUserByID(userID);

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

    @Nonnull
    @Override
    public User me() {

        Call<User> call = service.getMe(null);

        return execute(call);
    }

    @Override
    public void meUpdatePassword(@Nonnull final String oldPassword, @Nonnull final String newPassword) {

        Arguments.checkNotNull(oldPassword, "old password");
        Arguments.checkNotNull(newPassword, "new password");

        User user = me();

        String credentials = Credentials
                .basic(user.getName(), oldPassword);

        PasswordResetBody passwordResetBody = new PasswordResetBody().password(newPassword);

        Call<Void> call = service.putMePassword(passwordResetBody, null, credentials);

        execute(call);
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

        Call<OperationLogs> call = service.getUsersIDLogs(userID, null,
                findOptions.getOffset(),
                findOptions.getLimit());

        return execute(call);
    }

    private void updateUserPassword(@Nonnull final String userID,
                                    @Nonnull final String userName,
                                    @Nonnull final String oldPassword,
                                    @Nonnull final String newPassword) {

        Arguments.checkNotNull(userID, "User ID");
        Arguments.checkNotNull(userName, "Username");
        Arguments.checkNotNull(oldPassword, "old password");
        Arguments.checkNotNull(newPassword, "new password");

        String credentials = Credentials.basic(userName, oldPassword);

        PasswordResetBody resetBody = new PasswordResetBody().password(newPassword);
        Call<Void> call = service.putUsersIDPassword(userID, resetBody, null, credentials);

        execute(call);
    }
}