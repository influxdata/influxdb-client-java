package com.influxdb.client;

import javax.annotation.Nullable;

public class BucketsQuery {

    /**
     * offset  (optional)
     */
    @Nullable
    private Integer offset;

    /**
     *  (optional, default to 20)
     */
    @Nullable
    private Integer limit;

    /**
     * The last resource ID from which to seek from (but not including). This is to be used instead of &#x60;offset&#x60;. (optional)
     */
    @Nullable
    private String after;

    /**
     * The name of the organization. (optional)
     */
    @Nullable
    private String org;

    /**
     * The organization ID. (optional)
     */
    @Nullable
    private String orgID;

    /**
     * Only returns buckets with a specific name. (optional)
     */
    @Nullable
    private String name;

    /**
     * Only returns buckets with a specific ID. (optional)
     */
    @Nullable
    private String id;

    @Nullable
    public Integer getOffset() {
        return offset;
    }

    public void setOffset(@Nullable Integer offset) {
        this.offset = offset;
    }

    @Nullable
    public Integer getLimit() {
        return limit;
    }

    public void setLimit(@Nullable Integer limit) {
        this.limit = limit;
    }

    @Nullable
    public String getAfter() {
        return after;
    }

    public void setAfter(@Nullable String after) {
        this.after = after;
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
    public String getName() {
        return name;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }

    @Nullable
    public String getId() {
        return id;
    }

    public void setId(@Nullable String id) {
        this.id = id;
    }

}