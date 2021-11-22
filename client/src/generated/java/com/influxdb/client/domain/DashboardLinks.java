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
 * DashboardLinks
 */

public class DashboardLinks {
  public static final String SERIALIZED_NAME_SELF = "self";
  @SerializedName(SERIALIZED_NAME_SELF)
  private String self;

  public static final String SERIALIZED_NAME_CELLS = "cells";
  @SerializedName(SERIALIZED_NAME_CELLS)
  private String cells;

  public static final String SERIALIZED_NAME_MEMBERS = "members";
  @SerializedName(SERIALIZED_NAME_MEMBERS)
  private String members;

  public static final String SERIALIZED_NAME_OWNERS = "owners";
  @SerializedName(SERIALIZED_NAME_OWNERS)
  private String owners;

  public static final String SERIALIZED_NAME_LABELS = "labels";
  @SerializedName(SERIALIZED_NAME_LABELS)
  private String labels;

  public static final String SERIALIZED_NAME_ORG = "org";
  @SerializedName(SERIALIZED_NAME_ORG)
  private String org;

   /**
   * URI of resource.
   * @return self
  **/
  @ApiModelProperty(value = "URI of resource.")
  public String getSelf() {
    return self;
  }

   /**
   * URI of resource.
   * @return cells
  **/
  @ApiModelProperty(value = "URI of resource.")
  public String getCells() {
    return cells;
  }

   /**
   * URI of resource.
   * @return members
  **/
  @ApiModelProperty(value = "URI of resource.")
  public String getMembers() {
    return members;
  }

   /**
   * URI of resource.
   * @return owners
  **/
  @ApiModelProperty(value = "URI of resource.")
  public String getOwners() {
    return owners;
  }

   /**
   * URI of resource.
   * @return labels
  **/
  @ApiModelProperty(value = "URI of resource.")
  public String getLabels() {
    return labels;
  }

   /**
   * URI of resource.
   * @return org
  **/
  @ApiModelProperty(value = "URI of resource.")
  public String getOrg() {
    return org;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DashboardLinks dashboardWithViewPropertiesLinks = (DashboardLinks) o;
    return Objects.equals(this.self, dashboardWithViewPropertiesLinks.self) &&
        Objects.equals(this.cells, dashboardWithViewPropertiesLinks.cells) &&
        Objects.equals(this.members, dashboardWithViewPropertiesLinks.members) &&
        Objects.equals(this.owners, dashboardWithViewPropertiesLinks.owners) &&
        Objects.equals(this.labels, dashboardWithViewPropertiesLinks.labels) &&
        Objects.equals(this.org, dashboardWithViewPropertiesLinks.org);
  }

  @Override
  public int hashCode() {
    return Objects.hash(self, cells, members, owners, labels, org);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DashboardLinks {\n");
    sb.append("    self: ").append(toIndentedString(self)).append("\n");
    sb.append("    cells: ").append(toIndentedString(cells)).append("\n");
    sb.append("    members: ").append(toIndentedString(members)).append("\n");
    sb.append("    owners: ").append(toIndentedString(owners)).append("\n");
    sb.append("    labels: ").append(toIndentedString(labels)).append("\n");
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

