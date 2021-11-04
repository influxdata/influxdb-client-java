package com.influxdb.client;

import javax.annotation.Nullable;

public class TaskQuery {

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
     * The number of tasks to return (optional, default to 100)
     */
    @Nullable
    private Integer limit;

    @Nullable
    public String getName() {
        return name;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }

    @Nullable
    public String getAfter() {
        return after;
    }

    public void setAfter(@Nullable String after) {
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

    public void setOrg(@Nullable String org) {
        this.org = org;
    }

    @Nullable
    public String getOrgID() {
        return orgID;
    }

    public void setOrgID(@Nullable String orgID) {
        this.orgID = orgID;
    }

    @Nullable
    public String getStatus() {
        return status;
    }

    public void setStatus(@Nullable String status) {
        this.status = status;
    }

    @Nullable
    public Integer getLimit() {
        return limit;
    }

    public void setLimit(@Nullable Integer limit) {
        this.limit = limit;
    }

}
