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
import com.influxdb.client.domain.TemplateSummaryDiffDashboardsNewOld;
import java.io.IOException;

/**
 * TemplateSummaryDiffDashboards
 */

public class TemplateSummaryDiffDashboards {
  public static final String SERIALIZED_NAME_STATE_STATUS = "stateStatus";
  @SerializedName(SERIALIZED_NAME_STATE_STATUS)
  private String stateStatus;

  public static final String SERIALIZED_NAME_ID = "id";
  @SerializedName(SERIALIZED_NAME_ID)
  private String id;

  public static final String SERIALIZED_NAME_KIND = "kind";
  @SerializedName(SERIALIZED_NAME_KIND)
  private TemplateKind kind = null;

  public static final String SERIALIZED_NAME_TEMPLATE_META_NAME = "templateMetaName";
  @SerializedName(SERIALIZED_NAME_TEMPLATE_META_NAME)
  private String templateMetaName;

  public static final String SERIALIZED_NAME_NEW = "new";
  @SerializedName(SERIALIZED_NAME_NEW)
  private TemplateSummaryDiffDashboardsNewOld _new = null;

  public static final String SERIALIZED_NAME_OLD = "old";
  @SerializedName(SERIALIZED_NAME_OLD)
  private TemplateSummaryDiffDashboardsNewOld old = null;

  public TemplateSummaryDiffDashboards stateStatus(String stateStatus) {
    this.stateStatus = stateStatus;
    return this;
  }

   /**
   * Get stateStatus
   * @return stateStatus
  **/
  public String getStateStatus() {
    return stateStatus;
  }

  public void setStateStatus(String stateStatus) {
    this.stateStatus = stateStatus;
  }

  public TemplateSummaryDiffDashboards id(String id) {
    this.id = id;
    return this;
  }

   /**
   * Get id
   * @return id
  **/
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public TemplateSummaryDiffDashboards kind(TemplateKind kind) {
    this.kind = kind;
    return this;
  }

   /**
   * Get kind
   * @return kind
  **/
  public TemplateKind getKind() {
    return kind;
  }

  public void setKind(TemplateKind kind) {
    this.kind = kind;
  }

  public TemplateSummaryDiffDashboards templateMetaName(String templateMetaName) {
    this.templateMetaName = templateMetaName;
    return this;
  }

   /**
   * Get templateMetaName
   * @return templateMetaName
  **/
  public String getTemplateMetaName() {
    return templateMetaName;
  }

  public void setTemplateMetaName(String templateMetaName) {
    this.templateMetaName = templateMetaName;
  }

  public TemplateSummaryDiffDashboards _new(TemplateSummaryDiffDashboardsNewOld _new) {
    this._new = _new;
    return this;
  }

   /**
   * Get _new
   * @return _new
  **/
  public TemplateSummaryDiffDashboardsNewOld getNew() {
    return _new;
  }

  public void setNew(TemplateSummaryDiffDashboardsNewOld _new) {
    this._new = _new;
  }

  public TemplateSummaryDiffDashboards old(TemplateSummaryDiffDashboardsNewOld old) {
    this.old = old;
    return this;
  }

   /**
   * Get old
   * @return old
  **/
  public TemplateSummaryDiffDashboardsNewOld getOld() {
    return old;
  }

  public void setOld(TemplateSummaryDiffDashboardsNewOld old) {
    this.old = old;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TemplateSummaryDiffDashboards templateSummaryDiffDashboards = (TemplateSummaryDiffDashboards) o;
    return Objects.equals(this.stateStatus, templateSummaryDiffDashboards.stateStatus) &&
        Objects.equals(this.id, templateSummaryDiffDashboards.id) &&
        Objects.equals(this.kind, templateSummaryDiffDashboards.kind) &&
        Objects.equals(this.templateMetaName, templateSummaryDiffDashboards.templateMetaName) &&
        Objects.equals(this._new, templateSummaryDiffDashboards._new) &&
        Objects.equals(this.old, templateSummaryDiffDashboards.old);
  }

  @Override
  public int hashCode() {
    return Objects.hash(stateStatus, id, kind, templateMetaName, _new, old);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TemplateSummaryDiffDashboards {\n");
    sb.append("    stateStatus: ").append(toIndentedString(stateStatus)).append("\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    kind: ").append(toIndentedString(kind)).append("\n");
    sb.append("    templateMetaName: ").append(toIndentedString(templateMetaName)).append("\n");
    sb.append("    _new: ").append(toIndentedString(_new)).append("\n");
    sb.append("    old: ").append(toIndentedString(old)).append("\n");
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

