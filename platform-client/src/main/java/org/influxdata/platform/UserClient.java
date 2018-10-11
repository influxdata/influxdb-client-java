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

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.influxdata.platform.domain.User;

/**
 * The client of the InfluxData Platform that implement User HTTP API endpoint.
 *
 * @author Jakub Bednar (bednar@github) (11/09/2018 10:05)
 */
public interface UserClient {

    /**
     * Creates a new user and sets {@link User#id} with the new identifier.
     *
     * @param user the user to create
     * @return User created
     */
    @Nonnull
    User createUser(@Nonnull final User user);

    /**
     * Creates a new user and sets {@link User#id} with the new identifier.
     *
     * @param name name of the user
     * @return User created
     */
    @Nonnull
    User createUser(@Nonnull final String name);

    /**
     * Update a user.
     *
     * @param user user update to apply
     * @return user updated
     */
    @Nonnull
    User updateUser(@Nonnull final User user);

    /**
     * Delete a user.
     *
     * @param user user to delete
     */
    void deleteUser(@Nonnull final User user);

    /**
     * Delete a user.
     *
     * @param userID ID of user to delete
     */
    void deleteUser(@Nonnull final String userID);

    /**
     * Retrieve a user.
     *
     * @param userID ID of user to get
     * @return user details
     */
    @Nullable
    User findUserByID(@Nonnull final String userID);

    /**
     * List all users.
     *
     * @return List all users
     */
    @Nonnull
    List<User> findUsers();
}