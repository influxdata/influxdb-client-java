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
import java.util.ArrayList;
import java.util.List;
import org.influxdata.client.domain.DocumentMeta;

/**
 * DocumentCreate
 */

public class DocumentCreate {
  public static final String SERIALIZED_NAME_META = "meta";
  @SerializedName(SERIALIZED_NAME_META)
  private DocumentMeta meta = null;

  public static final String SERIALIZED_NAME_CONTENT = "content";
  @SerializedName(SERIALIZED_NAME_CONTENT)
  private Object content = null;

  public static final String SERIALIZED_NAME_ORG = "org";
  @SerializedName(SERIALIZED_NAME_ORG)
  private String org = null;

  public static final String SERIALIZED_NAME_ORG_I_D = "orgID";
  @SerializedName(SERIALIZED_NAME_ORG_I_D)
  private String orgID = null;

  public static final String SERIALIZED_NAME_LABELS = "labels";
  @SerializedName(SERIALIZED_NAME_LABELS)
  private List<String> labels = new ArrayList<>();

  public DocumentCreate meta(DocumentMeta meta) {
    this.meta = meta;
    return this;
  }

   /**
   * Get meta
   * @return meta
  **/
  @ApiModelProperty(required = true, value = "")
  public DocumentMeta getMeta() {
    return meta;
  }

  public void setMeta(DocumentMeta meta) {
    this.meta = meta;
  }

  public DocumentCreate content(Object content) {
    this.content = content;
    return this;
  }

   /**
   * Get content
   * @return content
  **/
  @ApiModelProperty(required = true, value = "")
  public Object getContent() {
    return content;
  }

  public void setContent(Object content) {
    this.content = content;
  }

  public DocumentCreate org(String org) {
    this.org = org;
    return this;
  }

   /**
   * must specify one of orgID and org
   * @return org
  **/
  @ApiModelProperty(value = "must specify one of orgID and org")
  public String getOrg() {
    return org;
  }

  public void setOrg(String org) {
    this.org = org;
  }

  public DocumentCreate orgID(String orgID) {
    this.orgID = orgID;
    return this;
  }

   /**
   * must specify one of orgID and org
   * @return orgID
  **/
  @ApiModelProperty(value = "must specify one of orgID and org")
  public String getOrgID() {
    return orgID;
  }

  public void setOrgID(String orgID) {
    this.orgID = orgID;
  }

  public DocumentCreate labels(List<String> labels) {
    this.labels = labels;
    return this;
  }

  public DocumentCreate addLabelsItem(String labelsItem) {
    if (this.labels == null) {
      this.labels = new ArrayList<>();
    }
    this.labels.add(labelsItem);
    return this;
  }

   /**
   * this is an array of label IDs that will be added as labels to the document
   * @return labels
  **/
  @ApiModelProperty(value = "this is an array of label IDs that will be added as labels to the document")
  public List<String> getLabels() {
    return labels;
  }

  public void setLabels(List<String> labels) {
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
    DocumentCreate documentCreate = (DocumentCreate) o;
    return Objects.equals(this.meta, documentCreate.meta) &&
        Objects.equals(this.content, documentCreate.content) &&
        Objects.equals(this.org, documentCreate.org) &&
        Objects.equals(this.orgID, documentCreate.orgID) &&
        Objects.equals(this.labels, documentCreate.labels);
  }

  @Override
  public int hashCode() {
    return Objects.hash(meta, content, org, orgID, labels);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DocumentCreate {\n");
    sb.append("    meta: ").append(toIndentedString(meta)).append("\n");
    sb.append("    content: ").append(toIndentedString(content)).append("\n");
    sb.append("    org: ").append(toIndentedString(org)).append("\n");
    sb.append("    orgID: ").append(toIndentedString(orgID)).append("\n");
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

