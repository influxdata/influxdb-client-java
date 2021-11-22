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
import com.influxdb.client.domain.CreateDashboardRequest;
import com.influxdb.client.domain.DashboardLinks;
import com.influxdb.client.domain.DashboardMeta;
import com.influxdb.client.domain.Label;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * DashboardWithViewProperties
 */

public class DashboardWithViewProperties extends CreateDashboardRequest {
  public static final String SERIALIZED_NAME_LINKS = "links";
  @SerializedName(SERIALIZED_NAME_LINKS)
  private DashboardLinks links = null;

  public static final String SERIALIZED_NAME_ID = "id";
  @SerializedName(SERIALIZED_NAME_ID)
  private String id;

  public static final String SERIALIZED_NAME_META = "meta";
  @SerializedName(SERIALIZED_NAME_META)
  private DashboardMeta meta = null;

  public static final String SERIALIZED_NAME_CELLS = "cells";
  @SerializedName(SERIALIZED_NAME_CELLS)
  private List<CellWithViewProperties> cells = new ArrayList<>();

  public static final String SERIALIZED_NAME_LABELS = "labels";
  @SerializedName(SERIALIZED_NAME_LABELS)
  private List<Label> labels = new ArrayList<>();

  public DashboardWithViewProperties links(DashboardLinks links) {
    this.links = links;
    return this;
  }

   /**
   * Get links
   * @return links
  **/
  @ApiModelProperty(value = "")
  public DashboardLinks getLinks() {
    return links;
  }

  public void setLinks(DashboardLinks links) {
    this.links = links;
  }

   /**
   * Get id
   * @return id
  **/
  @ApiModelProperty(value = "")
  public String getId() {
    return id;
  }

  public DashboardWithViewProperties meta(DashboardMeta meta) {
    this.meta = meta;
    return this;
  }

   /**
   * Get meta
   * @return meta
  **/
  @ApiModelProperty(value = "")
  public DashboardMeta getMeta() {
    return meta;
  }

  public void setMeta(DashboardMeta meta) {
    this.meta = meta;
  }

  public DashboardWithViewProperties cells(List<CellWithViewProperties> cells) {
    this.cells = cells;
    return this;
  }

  public DashboardWithViewProperties addCellsItem(CellWithViewProperties cellsItem) {
    if (this.cells == null) {
      this.cells = new ArrayList<>();
    }
    this.cells.add(cellsItem);
    return this;
  }

   /**
   * Get cells
   * @return cells
  **/
  @ApiModelProperty(value = "")
  public List<CellWithViewProperties> getCells() {
    return cells;
  }

  public void setCells(List<CellWithViewProperties> cells) {
    this.cells = cells;
  }

  public DashboardWithViewProperties labels(List<Label> labels) {
    this.labels = labels;
    return this;
  }

  public DashboardWithViewProperties addLabelsItem(Label labelsItem) {
    if (this.labels == null) {
      this.labels = new ArrayList<>();
    }
    this.labels.add(labelsItem);
    return this;
  }

   /**
   * Get labels
   * @return labels
  **/
  @ApiModelProperty(value = "")
  public List<Label> getLabels() {
    return labels;
  }

  public void setLabels(List<Label> labels) {
    this.labels = labels;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DashboardWithViewProperties dashboardWithViewProperties = (DashboardWithViewProperties) o;
    return Objects.equals(this.links, dashboardWithViewProperties.links) &&
        Objects.equals(this.id, dashboardWithViewProperties.id) &&
        Objects.equals(this.meta, dashboardWithViewProperties.meta) &&
        Objects.equals(this.cells, dashboardWithViewProperties.cells) &&
        Objects.equals(this.labels, dashboardWithViewProperties.labels) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(links, id, meta, cells, labels, super.hashCode());
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DashboardWithViewProperties {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    links: ").append(toIndentedString(links)).append("\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    meta: ").append(toIndentedString(meta)).append("\n");
    sb.append("    cells: ").append(toIndentedString(cells)).append("\n");
    sb.append("    labels: ").append(toIndentedString(labels)).append("\n");
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

