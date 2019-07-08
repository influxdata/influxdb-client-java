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
import javax.annotation.Nonnull;

import com.influxdb.client.domain.OperationLog;
import com.influxdb.client.domain.OperationLogs;
import com.influxdb.client.domain.User;

/**
 * The client of the InfluxDB 2.0 that implement User HTTP API endpoint.
 *
 * @author Jakub Bednar (bednar@github) (11/09/2018 10:05)
 */
public interface UsersApi {

    /**
     * Creates a new user and sets {@link User#getId()} with the new identifier.
     *
     * @param user the user to create
     * @return User created
     */
    @Nonnull
    User createUser(@Nonnull final User user);

    /**
     * Creates a new user and sets {@link User#getId()} with the new identifier.
     *
     * @param name name of the user
     * @return User created
     */
    @Nonnull
    User createUser(@Nonnull final String name);

    /**
     * Update an user.
     *
     * @param user user update to apply
     * @return user updated
     */
    @Nonnull
    User updateUser(@Nonnull final User user);

    /**
     * Update password to an user.
     *
     * @param user        user to update password
     * @param oldPassword old password
     * @param newPassword new password
     */
    void updateUserPassword(@Nonnull final User user,
                            @Nonnull final String oldPassword,
                            @Nonnull final String newPassword);

    /**
     * Update password to an user.
     *
     * @param userID      ID of user to update password
     * @param oldPassword old password
     * @param newPassword new password
     */
    void updateUserPassword(@Nonnull final String userID,
                            @Nonnull final String oldPassword,
                            @Nonnull final String newPassword);

    /**
     * Delete an user.
     *
     * @param user user to delete
     */
    void deleteUser(@Nonnull final User user);

    /**
     * Delete an user.
     *
     * @param userID ID of user to delete
     */
    void deleteUser(@Nonnull final String userID);

    /**
     * Clone an user.
     *
     * @param clonedName name of cloned user
     * @param userID     ID of user to clone
     * @return cloned user
     */
    @Nonnull
    User cloneUser(@Nonnull final String clonedName, @Nonnull final String userID);

    /**
     * Clone an user.
     *
     * @param clonedName name of cloned user
     * @param user       user to clone
     * @return cloned user
     */
    @Nonnull
    User cloneUser(@Nonnull final String clonedName, @Nonnull final User user);

    /**
     * Returns currently authenticated user.
     *
     * @return user
     */
    @Nonnull
    User me();

    /**
     * Update the password to a currently authenticated user.
     *
     * @param oldPassword old password
     * @param newPassword new password
     */
    void meUpdatePassword(@Nonnull final String oldPassword, @Nonnull final String newPassword);

    /**
     * Retrieve an user.
     *
     * @param userID ID of user to get
     * @return user details
     */
    @Nonnull
    User findUserByID(@Nonnull final String userID);

    /**
     * List all users.
     *
     * @return List all users
     */
    @Nonnull
    List<User> findUsers();

    /**
     * Retrieve an user's logs.
     *
     * @param user for retrieve logs
     * @return logs
     */
    @Nonnull
    List<OperationLog> findUserLogs(@Nonnull final User user);

    /**
     * Retrieve an user's logs.
     *
     * @param user for retrieve logs
     * @return logs
     */
    @Nonnull
    OperationLogs findUserLogs(@Nonnull final User user, @Nonnull final FindOptions findOptions);

    /**
     * Retrieve an user's logs.
     *
     * @param userID id of an user
     * @return logs
     */
    @Nonnull
    List<OperationLog> findUserLogs(@Nonnull final String userID);

    /**
     * Retrieve an user's logs.
     *
     * @param userID id of an user
     * @return logs
     */
    @Nonnull
    OperationLogs findUserLogs(@Nonnull final String userID, @Nonnull final FindOptions findOptions);
}