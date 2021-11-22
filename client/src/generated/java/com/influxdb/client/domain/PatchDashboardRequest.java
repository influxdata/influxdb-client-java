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
import com.influxdb.client.domain.CellWithViewProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.IOException;

/**
 * PatchDashboardRequest
 */

public class PatchDashboardRequest {
  public static final String SERIALIZED_NAME_NAME = "name";
  @SerializedName(SERIALIZED_NAME_NAME)
  private String name;

  public static final String SERIALIZED_NAME_DESCRIPTION = "description";
  @SerializedName(SERIALIZED_NAME_DESCRIPTION)
  private String description;

  public static final String SERIALIZED_NAME_CELLS = "cells";
  @SerializedName(SERIALIZED_NAME_CELLS)
  private CellWithViewProperties cells = null;

  public PatchDashboardRequest name(String name) {
    this.name = name;
    return this;
  }

   /**
   * optional, when provided will replace the name
   * @return name
  **/
  @ApiModelProperty(value = "optional, when provided will replace the name")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public PatchDashboardRequest description(String description) {
    this.description = description;
    return this;
  }

   /**
   * optional, when provided will replace the description
   * @return description
  **/
  @ApiModelProperty(value = "optional, when provided will replace the description")
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public PatchDashboardRequest cells(CellWithViewProperties cells) {
    this.cells = cells;
    return this;
  }

   /**
   * Get cells
   * @return cells
  **/
  @ApiModelProperty(value = "")
  public CellWithViewProperties getCells() {
    return cells;
  }

  public void setCells(CellWithViewProperties cells) {
    this.cells = cells;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PatchDashboardRequest patchDashboardRequest1 = (PatchDashboardRequest) o;
    return Objects.equals(this.name, patchDashboardRequest1.name) &&
        Objects.equals(this.description, patchDashboardRequest1.description) &&
        Objects.equals(this.cells, patchDashboardRequest1.cells);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, description, cells);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PatchDashboardRequest {\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    cells: ").append(toIndentedString(cells)).append("\n");
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

