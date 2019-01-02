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
package org.influxdata.platform.domain;

import com.squareup.moshi.Json;

/**
 * The Resource Member.
 *
 * @author Jakub Bednar (bednar@github) (16/10/2018 07:52)
 */
public final class ResourceMember extends AbstractHasLinks {

    /**
     * User ID.
     */
    @Json(name = "id")
    private String userID;

    /**
     * User name.
     */
    @Json(name = "name")
    private String userName;

    @Json(name = "role")
    private UserType role;

    /**
     * The user type.
     */
    public enum UserType {

        @Json(name = "owner")
        OWNER,

        @Json(name = "member")
        MEMBER
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(final String userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(final String userName) {
        this.userName = userName;
    }

    public UserType getRole() {
        return role;
    }

    public void setRole(final UserType role) {
        this.role = role;
    }
}