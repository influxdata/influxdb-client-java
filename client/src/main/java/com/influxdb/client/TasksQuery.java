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

import javax.annotation.Nullable;

public class TasksQuery {

    /**
     * Returns task with a specific name. (optional)
     */
    @Nullable
    private String name;

    /**
     * Return tasks after a specified ID. (optional)
     */
    @Nullable
    private String after;

    /**
     * Filter tasks to a specific user ID. (optional)
     */
    @Nullable
    private String user;

    /**
     * Filter tasks to a specific organization name. (optional)
     */
    @Nullable
    private String org;

    /**
     * Filter tasks to a specific organization ID. (optional)
     */
    @Nullable
    private String orgID;

    /**
     * Filter tasks by a status--\&quot;inactive\&quot; or \&quot;active\&quot;. (optional)
     */
    @Nullable
    private String status;

    /**
     * The number of tasks to return. (optional, default to 100)
     */
    @Nullable
    private Integer limit;

    @Nullable
    public String getName() {
        return name;
    }

    public void setName(@Nullable final String name) {
        this.name = name;
    }

    @Nullable
    public String getAfter() {
        return after;
    }

    public void setAfter(@Nullable final String after) {
        this.after = after;
    }

    @Nullable
    public String getUser() {
        return user;
    }

    public void setUser(@Nullable final String user) {
        this.user = user;
    }

    @Nullable
    public String getOrg() {
        return org;
    }

    public void setOrg(@Nullable final String org) {
        this.org = org;
    }

    @Nullable
    public String getOrgID() {
        return orgID;
    }

    public void setOrgID(@Nullable final String orgID) {
        this.orgID = orgID;
    }

    @Nullable
    public String getStatus() {
        return status;
    }

    public void setStatus(@Nullable final String status) {
        this.status = status;
    }

    @Nullable
    public Integer getLimit() {
        return limit;
    }

    public void setLimit(@Nullable final Integer limit) {
        this.limit = limit;
    }

}
