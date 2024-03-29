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
import java.io.IOException;

/**
 * TemplateSummarySummaryLabelMappings
 */

public class TemplateSummarySummaryLabelMappings {
  public static final String SERIALIZED_NAME_STATUS = "status";
  @SerializedName(SERIALIZED_NAME_STATUS)
  private String status;

  public static final String SERIALIZED_NAME_RESOURCE_TEMPLATE_META_NAME = "resourceTemplateMetaName";
  @SerializedName(SERIALIZED_NAME_RESOURCE_TEMPLATE_META_NAME)
  private String resourceTemplateMetaName;

  public static final String SERIALIZED_NAME_RESOURCE_NAME = "resourceName";
  @SerializedName(SERIALIZED_NAME_RESOURCE_NAME)
  private String resourceName;

  public static final String SERIALIZED_NAME_RESOURCE_I_D = "resourceID";
  @SerializedName(SERIALIZED_NAME_RESOURCE_I_D)
  private String resourceID;

  public static final String SERIALIZED_NAME_RESOURCE_TYPE = "resourceType";
  @SerializedName(SERIALIZED_NAME_RESOURCE_TYPE)
  private String resourceType;

  public static final String SERIALIZED_NAME_LABEL_TEMPLATE_META_NAME = "labelTemplateMetaName";
  @SerializedName(SERIALIZED_NAME_LABEL_TEMPLATE_META_NAME)
  private String labelTemplateMetaName;

  public static final String SERIALIZED_NAME_LABEL_NAME = "labelName";
  @SerializedName(SERIALIZED_NAME_LABEL_NAME)
  private String labelName;

  public static final String SERIALIZED_NAME_LABEL_I_D = "labelID";
  @SerializedName(SERIALIZED_NAME_LABEL_I_D)
  private String labelID;

  public TemplateSummarySummaryLabelMappings status(String status) {
    this.status = status;
    return this;
  }

   /**
   * Get status
   * @return status
  **/
  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public TemplateSummarySummaryLabelMappings resourceTemplateMetaName(String resourceTemplateMetaName) {
    this.resourceTemplateMetaName = resourceTemplateMetaName;
    return this;
  }

   /**
   * Get resourceTemplateMetaName
   * @return resourceTemplateMetaName
  **/
  public String getResourceTemplateMetaName() {
    return resourceTemplateMetaName;
  }

  public void setResourceTemplateMetaName(String resourceTemplateMetaName) {
    this.resourceTemplateMetaName = resourceTemplateMetaName;
  }

  public TemplateSummarySummaryLabelMappings resourceName(String resourceName) {
    this.resourceName = resourceName;
    return this;
  }

   /**
   * Get resourceName
   * @return resourceName
  **/
  public String getResourceName() {
    return resourceName;
  }

  public void setResourceName(String resourceName) {
    this.resourceName = resourceName;
  }

  public TemplateSummarySummaryLabelMappings resourceID(String resourceID) {
    this.resourceID = resourceID;
    return this;
  }

   /**
   * Get resourceID
   * @return resourceID
  **/
  public String getResourceID() {
    return resourceID;
  }

  public void setResourceID(String resourceID) {
    this.resourceID = resourceID;
  }

  public TemplateSummarySummaryLabelMappings resourceType(String resourceType) {
    this.resourceType = resourceType;
    return this;
  }

   /**
   * Get resourceType
   * @return resourceType
  **/
  public String getResourceType() {
    return resourceType;
  }

  public void setResourceType(String resourceType) {
    this.resourceType = resourceType;
  }

  public TemplateSummarySummaryLabelMappings labelTemplateMetaName(String labelTemplateMetaName) {
    this.labelTemplateMetaName = labelTemplateMetaName;
    return this;
  }

   /**
   * Get labelTemplateMetaName
   * @return labelTemplateMetaName
  **/
  public String getLabelTemplateMetaName() {
    return labelTemplateMetaName;
  }

  public void setLabelTemplateMetaName(String labelTemplateMetaName) {
    this.labelTemplateMetaName = labelTemplateMetaName;
  }

  public TemplateSummarySummaryLabelMappings labelName(String labelName) {
    this.labelName = labelName;
    return this;
  }

   /**
   * Get labelName
   * @return labelName
  **/
  public String getLabelName() {
    return labelName;
  }

  public void setLabelName(String labelName) {
    this.labelName = labelName;
  }

  public TemplateSummarySummaryLabelMappings labelID(String labelID) {
    this.labelID = labelID;
    return this;
  }

   /**
   * Get labelID
   * @return labelID
  **/
  public String getLabelID() {
    return labelID;
  }

  public void setLabelID(String labelID) {
    this.labelID = labelID;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TemplateSummarySummaryLabelMappings templateSummarySummaryLabelMappings = (TemplateSummarySummaryLabelMappings) o;
    return Objects.equals(this.status, templateSummarySummaryLabelMappings.status) &&
        Objects.equals(this.resourceTemplateMetaName, templateSummarySummaryLabelMappings.resourceTemplateMetaName) &&
        Objects.equals(this.resourceName, templateSummarySummaryLabelMappings.resourceName) &&
        Objects.equals(this.resourceID, templateSummarySummaryLabelMappings.resourceID) &&
        Objects.equals(this.resourceType, templateSummarySummaryLabelMappings.resourceType) &&
        Objects.equals(this.labelTemplateMetaName, templateSummarySummaryLabelMappings.labelTemplateMetaName) &&
        Objects.equals(this.labelName, templateSummarySummaryLabelMappings.labelName) &&
        Objects.equals(this.labelID, templateSummarySummaryLabelMappings.labelID);
  }

  @Override
  public int hashCode() {
    return Objects.hash(status, resourceTemplateMetaName, resourceName, resourceID, resourceType, labelTemplateMetaName, labelName, labelID);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TemplateSummarySummaryLabelMappings {\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    resourceTemplateMetaName: ").append(toIndentedString(resourceTemplateMetaName)).append("\n");
    sb.append("    resourceName: ").append(toIndentedString(resourceName)).append("\n");
    sb.append("    resourceID: ").append(toIndentedString(resourceID)).append("\n");
    sb.append("    resourceType: ").append(toIndentedString(resourceType)).append("\n");
    sb.append("    labelTemplateMetaName: ").append(toIndentedString(labelTemplateMetaName)).append("\n");
    sb.append("    labelName: ").append(toIndentedString(labelName)).append("\n");
    sb.append("    labelID: ").append(toIndentedString(labelID)).append("\n");
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

