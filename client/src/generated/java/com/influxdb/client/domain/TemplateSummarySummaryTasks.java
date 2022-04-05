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
 * TemplateSummarySummaryTasks
 */

public class TemplateSummarySummaryTasks {
  public static final String SERIALIZED_NAME_KIND = "kind";
  @SerializedName(SERIALIZED_NAME_KIND)
  private TemplateKind kind = null;

  public static final String SERIALIZED_NAME_TEMPLATE_META_NAME = "templateMetaName";
  @SerializedName(SERIALIZED_NAME_TEMPLATE_META_NAME)
  private String templateMetaName;

  public static final String SERIALIZED_NAME_ID = "id";
  @SerializedName(SERIALIZED_NAME_ID)
  private String id;

  public static final String SERIALIZED_NAME_NAME = "name";
  @SerializedName(SERIALIZED_NAME_NAME)
  private String name;

  public static final String SERIALIZED_NAME_CRON = "cron";
  @SerializedName(SERIALIZED_NAME_CRON)
  private String cron;

  public static final String SERIALIZED_NAME_DESCRIPTION = "description";
  @SerializedName(SERIALIZED_NAME_DESCRIPTION)
  private String description;

  public static final String SERIALIZED_NAME_EVERY = "every";
  @SerializedName(SERIALIZED_NAME_EVERY)
  private String every;

  public static final String SERIALIZED_NAME_OFFSET = "offset";
  @SerializedName(SERIALIZED_NAME_OFFSET)
  private String offset;

  public static final String SERIALIZED_NAME_QUERY = "query";
  @SerializedName(SERIALIZED_NAME_QUERY)
  private String query;

  public static final String SERIALIZED_NAME_STATUS = "status";
  @SerializedName(SERIALIZED_NAME_STATUS)
  private String status;

  public static final String SERIALIZED_NAME_ENV_REFERENCES = "envReferences";
  @SerializedName(SERIALIZED_NAME_ENV_REFERENCES)
  private List<Object> envReferences = new ArrayList<>();

  public TemplateSummarySummaryTasks kind(TemplateKind kind) {
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

  public TemplateSummarySummaryTasks templateMetaName(String templateMetaName) {
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

  public TemplateSummarySummaryTasks id(String id) {
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

  public TemplateSummarySummaryTasks name(String name) {
    this.name = name;
    return this;
  }

   /**
   * Get name
   * @return name
  **/
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public TemplateSummarySummaryTasks cron(String cron) {
    this.cron = cron;
    return this;
  }

   /**
   * Get cron
   * @return cron
  **/
  public String getCron() {
    return cron;
  }

  public void setCron(String cron) {
    this.cron = cron;
  }

  public TemplateSummarySummaryTasks description(String description) {
    this.description = description;
    return this;
  }

   /**
   * Get description
   * @return description
  **/
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public TemplateSummarySummaryTasks every(String every) {
    this.every = every;
    return this;
  }

   /**
   * Get every
   * @return every
  **/
  public String getEvery() {
    return every;
  }

  public void setEvery(String every) {
    this.every = every;
  }

  public TemplateSummarySummaryTasks offset(String offset) {
    this.offset = offset;
    return this;
  }

   /**
   * Get offset
   * @return offset
  **/
  public String getOffset() {
    return offset;
  }

  public void setOffset(String offset) {
    this.offset = offset;
  }

  public TemplateSummarySummaryTasks query(String query) {
    this.query = query;
    return this;
  }

   /**
   * Get query
   * @return query
  **/
  public String getQuery() {
    return query;
  }

  public void setQuery(String query) {
    this.query = query;
  }

  public TemplateSummarySummaryTasks status(String status) {
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

  public TemplateSummarySummaryTasks envReferences(List<Object> envReferences) {
    this.envReferences = envReferences;
    return this;
  }

  public TemplateSummarySummaryTasks addEnvReferencesItem(Object envReferencesItem) {
    if (this.envReferences == null) {
      this.envReferences = new ArrayList<>();
    }
    this.envReferences.add(envReferencesItem);
    return this;
  }

   /**
   * Get envReferences
   * @return envReferences
  **/
  public List<Object> getEnvReferences() {
    return envReferences;
  }

  public void setEnvReferences(List<Object> envReferences) {
    this.envReferences = envReferences;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TemplateSummarySummaryTasks templateSummarySummaryTasks = (TemplateSummarySummaryTasks) o;
    return Objects.equals(this.kind, templateSummarySummaryTasks.kind) &&
        Objects.equals(this.templateMetaName, templateSummarySummaryTasks.templateMetaName) &&
        Objects.equals(this.id, templateSummarySummaryTasks.id) &&
        Objects.equals(this.name, templateSummarySummaryTasks.name) &&
        Objects.equals(this.cron, templateSummarySummaryTasks.cron) &&
        Objects.equals(this.description, templateSummarySummaryTasks.description) &&
        Objects.equals(this.every, templateSummarySummaryTasks.every) &&
        Objects.equals(this.offset, templateSummarySummaryTasks.offset) &&
        Objects.equals(this.query, templateSummarySummaryTasks.query) &&
        Objects.equals(this.status, templateSummarySummaryTasks.status) &&
        Objects.equals(this.envReferences, templateSummarySummaryTasks.envReferences);
  }

  @Override
  public int hashCode() {
    return Objects.hash(kind, templateMetaName, id, name, cron, description, every, offset, query, status, envReferences);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TemplateSummarySummaryTasks {\n");
    sb.append("    kind: ").append(toIndentedString(kind)).append("\n");
    sb.append("    templateMetaName: ").append(toIndentedString(templateMetaName)).append("\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    cron: ").append(toIndentedString(cron)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    every: ").append(toIndentedString(every)).append("\n");
    sb.append("    offset: ").append(toIndentedString(offset)).append("\n");
    sb.append("    query: ").append(toIndentedString(query)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    envReferences: ").append(toIndentedString(envReferences)).append("\n");
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

