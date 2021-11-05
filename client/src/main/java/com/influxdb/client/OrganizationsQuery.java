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

public class OrganizationsQuery {

    /**
     * Offset. (optional)
     */
    @Nullable
    private Integer offset;

    /**
     * Limit. (optional, default to 20)
     */
    @Nullable
    private Integer limit;

    /**
     * Descending. (optional, default to false)
     */
    @Nullable
    private Boolean descending;

    /**
     * Filter organizations to a specific organization name. (optional)
     */
    @Nullable
    private String org;
    /**
     * Filter organizations to a specific organization ID. (optional)
     */
    @Nullable
    private String orgID;

    /**
     * Filter organizations to a specific user ID. (optional)
     */
    @Nullable
    private String userID;

    @Nullable
    public Integer getOffset() {
        return offset;
    }

    public void setOffset(@Nullable final Integer offset) {
        this.offset = offset;
    }

    @Nullable
    public Integer getLimit() {
        return limit;
    }

    public void setLimit(@Nullable final Integer limit) {
        this.limit = limit;
    }

    @Nullable
    public Boolean getDescending() {
        return descending;
    }

    public void setDescending(@Nullable final Boolean descending) {
        this.descending = descending;
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
    public String getUserID() {
        return userID;
    }

    public void setUserID(@Nullable final String userID) {
        this.userID = userID;
    }

}
