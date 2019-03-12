/*
 * Influx API Service
 * No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
 *
 * OpenAPI spec version: 0.1.0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


package org.influxdata.client.domain;

import java.util.Objects;
import java.util.Arrays;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.IOException;

/**
 * OrganizationLinks
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2019-03-12T13:11:42.908+01:00[Europe/Prague]")
public class OrganizationLinks {
  public static final String SERIALIZED_NAME_SELF = "self";
  @SerializedName(SERIALIZED_NAME_SELF)
  private String self = null;

  public static final String SERIALIZED_NAME_MEMBERS = "members";
  @SerializedName(SERIALIZED_NAME_MEMBERS)
  private String members = null;

  public static final String SERIALIZED_NAME_OWNERS = "owners";
  @SerializedName(SERIALIZED_NAME_OWNERS)
  private String owners = null;

  public static final String SERIALIZED_NAME_LABELS = "labels";
  @SerializedName(SERIALIZED_NAME_LABELS)
  private String labels = null;

  public static final String SERIALIZED_NAME_SECRETS = "secrets";
  @SerializedName(SERIALIZED_NAME_SECRETS)
  private String secrets = null;

  public static final String SERIALIZED_NAME_BUCKETS = "buckets";
  @SerializedName(SERIALIZED_NAME_BUCKETS)
  private String buckets = null;

  public static final String SERIALIZED_NAME_TASKS = "tasks";
  @SerializedName(SERIALIZED_NAME_TASKS)
  private String tasks = null;

  public static final String SERIALIZED_NAME_DASHBOARDS = "dashboards";
  @SerializedName(SERIALIZED_NAME_DASHBOARDS)
  private String dashboards = null;

  public static final String SERIALIZED_NAME_LOGS = "logs";
  @SerializedName(SERIALIZED_NAME_LOGS)
  private String logs = null;

  public OrganizationLinks self(String self) {
    this.self = self;
    return this;
  }

   /**
   * Get self
   * @return self
  **/
  @ApiModelProperty(value = "")
  public String getSelf() {
    return self;
  }

  public void setSelf(String self) {
    this.self = self;
  }

  public OrganizationLinks members(String members) {
    this.members = members;
    return this;
  }

   /**
   * Get members
   * @return members
  **/
  @ApiModelProperty(value = "")
  public String getMembers() {
    return members;
  }

  public void setMembers(String members) {
    this.members = members;
  }

  public OrganizationLinks owners(String owners) {
    this.owners = owners;
    return this;
  }

   /**
   * Get owners
   * @return owners
  **/
  @ApiModelProperty(value = "")
  public String getOwners() {
    return owners;
  }

  public void setOwners(String owners) {
    this.owners = owners;
  }

  public OrganizationLinks labels(String labels) {
    this.labels = labels;
    return this;
  }

   /**
   * Get labels
   * @return labels
  **/
  @ApiModelProperty(value = "")
  public String getLabels() {
    return labels;
  }

  public void setLabels(String labels) {
    this.labels = labels;
  }

  public OrganizationLinks secrets(String secrets) {
    this.secrets = secrets;
    return this;
  }

   /**
   * Get secrets
   * @return secrets
  **/
  @ApiModelProperty(value = "")
  public String getSecrets() {
    return secrets;
  }

  public void setSecrets(String secrets) {
    this.secrets = secrets;
  }

  public OrganizationLinks buckets(String buckets) {
    this.buckets = buckets;
    return this;
  }

   /**
   * Get buckets
   * @return buckets
  **/
  @ApiModelProperty(value = "")
  public String getBuckets() {
    return buckets;
  }

  public void setBuckets(String buckets) {
    this.buckets = buckets;
  }

  public OrganizationLinks tasks(String tasks) {
    this.tasks = tasks;
    return this;
  }

   /**
   * Get tasks
   * @return tasks
  **/
  @ApiModelProperty(value = "")
  public String getTasks() {
    return tasks;
  }

  public void setTasks(String tasks) {
    this.tasks = tasks;
  }

  public OrganizationLinks dashboards(String dashboards) {
    this.dashboards = dashboards;
    return this;
  }

   /**
   * Get dashboards
   * @return dashboards
  **/
  @ApiModelProperty(value = "")
  public String getDashboards() {
    return dashboards;
  }

  public void setDashboards(String dashboards) {
    this.dashboards = dashboards;
  }

  public OrganizationLinks logs(String logs) {
    this.logs = logs;
    return this;
  }

   /**
   * Get logs
   * @return logs
  **/
  @ApiModelProperty(value = "")
  public String getLogs() {
    return logs;
  }

  public void setLogs(String logs) {
    this.logs = logs;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OrganizationLinks organizationLinks = (OrganizationLinks) o;
    return Objects.equals(this.self, organizationLinks.self) &&
        Objects.equals(this.members, organizationLinks.members) &&
        Objects.equals(this.owners, organizationLinks.owners) &&
        Objects.equals(this.labels, organizationLinks.labels) &&
        Objects.equals(this.secrets, organizationLinks.secrets) &&
        Objects.equals(this.buckets, organizationLinks.buckets) &&
        Objects.equals(this.tasks, organizationLinks.tasks) &&
        Objects.equals(this.dashboards, organizationLinks.dashboards) &&
        Objects.equals(this.logs, organizationLinks.logs);
  }

  @Override
  public int hashCode() {
    return Objects.hash(self, members, owners, labels, secrets, buckets, tasks, dashboards, logs);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class OrganizationLinks {\n");
    
    sb.append("    self: ").append(toIndentedString(self)).append("\n");
    sb.append("    members: ").append(toIndentedString(members)).append("\n");
    sb.append("    owners: ").append(toIndentedString(owners)).append("\n");
    sb.append("    labels: ").append(toIndentedString(labels)).append("\n");
    sb.append("    secrets: ").append(toIndentedString(secrets)).append("\n");
    sb.append("    buckets: ").append(toIndentedString(buckets)).append("\n");
    sb.append("    tasks: ").append(toIndentedString(tasks)).append("\n");
    sb.append("    dashboards: ").append(toIndentedString(dashboards)).append("\n");
    sb.append("    logs: ").append(toIndentedString(logs)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

}

