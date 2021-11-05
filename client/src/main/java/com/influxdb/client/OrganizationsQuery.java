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
     * descending  (optional, default to false)
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
