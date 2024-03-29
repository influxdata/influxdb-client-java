/*
 * InfluxDB OSS API Service
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
import com.influxdb.client.domain.TemplateKind;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * TemplateExportByIDResourceFilters
 */

public class TemplateExportByIDResourceFilters {
  public static final String SERIALIZED_NAME_BY_LABEL = "byLabel";
  @SerializedName(SERIALIZED_NAME_BY_LABEL)
  private List<String> byLabel = new ArrayList<>();

  public static final String SERIALIZED_NAME_BY_RESOURCE_KIND = "byResourceKind";
  @SerializedName(SERIALIZED_NAME_BY_RESOURCE_KIND)
  private List<TemplateKind> byResourceKind = new ArrayList<>();

  public TemplateExportByIDResourceFilters byLabel(List<String> byLabel) {
    this.byLabel = byLabel;
    return this;
  }

  public TemplateExportByIDResourceFilters addByLabelItem(String byLabelItem) {
    if (this.byLabel == null) {
      this.byLabel = new ArrayList<>();
    }
    this.byLabel.add(byLabelItem);
    return this;
  }

   /**
   * Get byLabel
   * @return byLabel
  **/
  public List<String> getByLabel() {
    return byLabel;
  }

  public void setByLabel(List<String> byLabel) {
    this.byLabel = byLabel;
  }

  public TemplateExportByIDResourceFilters byResourceKind(List<TemplateKind> byResourceKind) {
    this.byResourceKind = byResourceKind;
    return this;
  }

  public TemplateExportByIDResourceFilters addByResourceKindItem(TemplateKind byResourceKindItem) {
    if (this.byResourceKind == null) {
      this.byResourceKind = new ArrayList<>();
    }
    this.byResourceKind.add(byResourceKindItem);
    return this;
  }

   /**
   * Get byResourceKind
   * @return byResourceKind
  **/
  public List<TemplateKind> getByResourceKind() {
    return byResourceKind;
  }

  public void setByResourceKind(List<TemplateKind> byResourceKind) {
    this.byResourceKind = byResourceKind;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TemplateExportByIDResourceFilters templateExportByIDResourceFilters = (TemplateExportByIDResourceFilters) o;
    return Objects.equals(this.byLabel, templateExportByIDResourceFilters.byLabel) &&
        Objects.equals(this.byResourceKind, templateExportByIDResourceFilters.byResourceKind);
  }

  @Override
  public int hashCode() {
    return Objects.hash(byLabel, byResourceKind);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TemplateExportByIDResourceFilters {\n");
    sb.append("    byLabel: ").append(toIndentedString(byLabel)).append("\n");
    sb.append("    byResourceKind: ").append(toIndentedString(byResourceKind)).append("\n");
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

