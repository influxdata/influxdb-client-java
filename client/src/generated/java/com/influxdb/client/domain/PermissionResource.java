/*
 * Influx OSS API Service
 * The InfluxDB v2 API provides a programmatic interface for all interactions with InfluxDB. Access the InfluxDB API using the `/api/v2/` endpoint. 
 *
 * OpenAPI spec version: 2.0.0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


package com.influxdb.client.domain;

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
 * PermissionResource
 */

public class PermissionResource {
  // Possible values for type property:
  public static String TYPE_AUTHORIZATIONS = "authorizations";
  public static String TYPE_BUCKETS = "buckets";
  public static String TYPE_DASHBOARDS = "dashboards";
  public static String TYPE_ORGS = "orgs";
  public static String TYPE_SOURCES = "sources";
  public static String TYPE_TASKS = "tasks";
  public static String TYPE_TELEGRAFS = "telegrafs";
  public static String TYPE_USERS = "users";
  public static String TYPE_VARIABLES = "variables";
  public static String TYPE_SCRAPERS = "scrapers";
  public static String TYPE_SECRETS = "secrets";
  public static String TYPE_LABELS = "labels";
  public static String TYPE_VIEWS = "views";
  public static String TYPE_DOCUMENTS = "documents";
  public static String TYPE_NOTIFICATIONRULES = "notificationRules";
  public static String TYPE_NOTIFICATIONENDPOINTS = "notificationEndpoints";
  public static String TYPE_CHECKS = "checks";
  public static String TYPE_DBRP = "dbrp";
  public static String TYPE_NOTEBOOKS = "notebooks";
  public static String TYPE_ANNOTATIONS = "annotations";
  public static String TYPE_REMOTES = "remotes";
  public static String TYPE_REPLICATIONS = "replications";
  public static String TYPE_FLOWS = "flows";
  public static String TYPE_FUNCTIONS = "functions";
  public static final String SERIALIZED_NAME_TYPE = "type";
  @SerializedName(SERIALIZED_NAME_TYPE)
  private String type;

  public static final String SERIALIZED_NAME_ID = "id";
  @SerializedName(SERIALIZED_NAME_ID)
  private String id;

  public static final String SERIALIZED_NAME_NAME = "name";
  @SerializedName(SERIALIZED_NAME_NAME)
  private String name;

  public static final String SERIALIZED_NAME_ORG_I_D = "orgID";
  @SerializedName(SERIALIZED_NAME_ORG_I_D)
  private String orgID;

  public static final String SERIALIZED_NAME_ORG = "org";
  @SerializedName(SERIALIZED_NAME_ORG)
  private String org;

  public PermissionResource type(String type) {
    this.type = type;
    return this;
  }

   /**
   * Get type
   * @return type
  **/
  @ApiModelProperty(required = true, value = "")
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public PermissionResource id(String id) {
    this.id = id;
    return this;
  }

   /**
   * If ID is set that is a permission for a specific resource. if it is not set it is a permission for all resources of that resource type.
   * @return id
  **/
  @ApiModelProperty(value = "If ID is set that is a permission for a specific resource. if it is not set it is a permission for all resources of that resource type.")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public PermissionResource name(String name) {
    this.name = name;
    return this;
  }

   /**
   * Optional name of the resource if the resource has a name field.
   * @return name
  **/
  @ApiModelProperty(value = "Optional name of the resource if the resource has a name field.")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public PermissionResource orgID(String orgID) {
    this.orgID = orgID;
    return this;
  }

   /**
   * If orgID is set that is a permission for all resources owned my that org. if it is not set it is a permission for all resources of that resource type.
   * @return orgID
  **/
  @ApiModelProperty(value = "If orgID is set that is a permission for all resources owned my that org. if it is not set it is a permission for all resources of that resource type.")
  public String getOrgID() {
    return orgID;
  }

  public void setOrgID(String orgID) {
    this.orgID = orgID;
  }

  public PermissionResource org(String org) {
    this.org = org;
    return this;
  }

   /**
   * Optional name of the organization of the organization with orgID.
   * @return org
  **/
  @ApiModelProperty(value = "Optional name of the organization of the organization with orgID.")
  public String getOrg() {
    return org;
  }

  public void setOrg(String org) {
    this.org = org;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PermissionResource resource = (PermissionResource) o;
    return Objects.equals(this.type, resource.type) &&
        Objects.equals(this.id, resource.id) &&
        Objects.equals(this.name, resource.name) &&
        Objects.equals(this.orgID, resource.orgID) &&
        Objects.equals(this.org, resource.org);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, id, name, orgID, org);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PermissionResource {\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    orgID: ").append(toIndentedString(orgID)).append("\n");
    sb.append("    org: ").append(toIndentedString(org)).append("\n");
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

