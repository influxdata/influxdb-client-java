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
import java.time.OffsetDateTime;
import org.influxdata.client.domain.Labels;
import org.influxdata.client.domain.TaskLinks;

/**
 * Task
 */
@javax.annotation.Generated(value = "org.influxdata.codegen.InfluxJavaGenerator", date = "2019-03-18T13:14:51.923+01:00[Europe/Prague]")
public class Task {
  public static final String SERIALIZED_NAME_ID = "id";
  @SerializedName(SERIALIZED_NAME_ID)
  private String id = null;

  public static final String SERIALIZED_NAME_ORG_I_D = "orgID";
  @SerializedName(SERIALIZED_NAME_ORG_I_D)
  private String orgID = null;

  public static final String SERIALIZED_NAME_ORG = "org";
  @SerializedName(SERIALIZED_NAME_ORG)
  private String org = null;

  public static final String SERIALIZED_NAME_NAME = "name";
  @SerializedName(SERIALIZED_NAME_NAME)
  private String name = null;

  /**
   * The current status of the task. When updated to &#39;inactive&#39;, cancels all queued jobs of this task.
   */
  @JsonAdapter(StatusEnum.Adapter.class)
  public enum StatusEnum {
    ACTIVE("active"),
    
    INACTIVE("inactive");

    private String value;

    StatusEnum(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    public static StatusEnum fromValue(String text) {
      for (StatusEnum b : StatusEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }

    public static class Adapter extends TypeAdapter<StatusEnum> {
      @Override
      public void write(final JsonWriter jsonWriter, final StatusEnum enumeration) throws IOException {
        jsonWriter.value(enumeration.getValue());
      }

      @Override
      public StatusEnum read(final JsonReader jsonReader) throws IOException {
        String value = jsonReader.nextString();
        return StatusEnum.fromValue(String.valueOf(value));
      }
    }
  }

  public static final String SERIALIZED_NAME_STATUS = "status";
  @SerializedName(SERIALIZED_NAME_STATUS)
  private StatusEnum status = StatusEnum.ACTIVE;

  public static final String SERIALIZED_NAME_LABELS = "labels";
  @SerializedName(SERIALIZED_NAME_LABELS)
  private Labels labels = null;

  public static final String SERIALIZED_NAME_AUTHORIZATION_I_D = "authorizationID";
  @SerializedName(SERIALIZED_NAME_AUTHORIZATION_I_D)
  private String authorizationID = null;

  public static final String SERIALIZED_NAME_FLUX = "flux";
  @SerializedName(SERIALIZED_NAME_FLUX)
  private String flux = null;

  public static final String SERIALIZED_NAME_EVERY = "every";
  @SerializedName(SERIALIZED_NAME_EVERY)
  private String every = null;

  public static final String SERIALIZED_NAME_CRON = "cron";
  @SerializedName(SERIALIZED_NAME_CRON)
  private String cron = null;

  public static final String SERIALIZED_NAME_OFFSET = "offset";
  @SerializedName(SERIALIZED_NAME_OFFSET)
  private String offset = null;

  public static final String SERIALIZED_NAME_LATEST_COMPLETED = "latestCompleted";
  @SerializedName(SERIALIZED_NAME_LATEST_COMPLETED)
  private OffsetDateTime latestCompleted = null;

  public static final String SERIALIZED_NAME_CREATED_AT = "createdAt";
  @SerializedName(SERIALIZED_NAME_CREATED_AT)
  private OffsetDateTime createdAt = null;

  public static final String SERIALIZED_NAME_UPDATED_AT = "updatedAt";
  @SerializedName(SERIALIZED_NAME_UPDATED_AT)
  private OffsetDateTime updatedAt = null;

  public static final String SERIALIZED_NAME_LINKS = "links";
  @SerializedName(SERIALIZED_NAME_LINKS)
  private TaskLinks links = null;

   /**
   * Get id
   * @return id
  **/
  @ApiModelProperty(required = true, value = "")
  public String getId() {
    return id;
  }

  public Task orgID(String orgID) {
    this.orgID = orgID;
    return this;
  }

   /**
   * The ID of the organization that owns this Task.
   * @return orgID
  **/
  @ApiModelProperty(required = true, value = "The ID of the organization that owns this Task.")
  public String getOrgID() {
    return orgID;
  }

  public void setOrgID(String orgID) {
    this.orgID = orgID;
  }

  public Task org(String org) {
    this.org = org;
    return this;
  }

   /**
   * The name of the organization that owns this Task.
   * @return org
  **/
  @ApiModelProperty(value = "The name of the organization that owns this Task.")
  public String getOrg() {
    return org;
  }

  public void setOrg(String org) {
    this.org = org;
  }

  public Task name(String name) {
    this.name = name;
    return this;
  }

   /**
   * A description of the task.
   * @return name
  **/
  @ApiModelProperty(required = true, value = "A description of the task.")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Task status(StatusEnum status) {
    this.status = status;
    return this;
  }

   /**
   * The current status of the task. When updated to &#39;inactive&#39;, cancels all queued jobs of this task.
   * @return status
  **/
  @ApiModelProperty(value = "The current status of the task. When updated to 'inactive', cancels all queued jobs of this task.")
  public StatusEnum getStatus() {
    return status;
  }

  public void setStatus(StatusEnum status) {
    this.status = status;
  }

  public Task labels(Labels labels) {
    this.labels = labels;
    return this;
  }

   /**
   * Get labels
   * @return labels
  **/
  @ApiModelProperty(value = "")
  public Labels getLabels() {
    return labels;
  }

  public void setLabels(Labels labels) {
    this.labels = labels;
  }

  public Task authorizationID(String authorizationID) {
    this.authorizationID = authorizationID;
    return this;
  }

   /**
   * The ID of the authorization used when this task communicates with the query engine.
   * @return authorizationID
  **/
  @ApiModelProperty(value = "The ID of the authorization used when this task communicates with the query engine.")
  public String getAuthorizationID() {
    return authorizationID;
  }

  public void setAuthorizationID(String authorizationID) {
    this.authorizationID = authorizationID;
  }

  public Task flux(String flux) {
    this.flux = flux;
    return this;
  }

   /**
   * The Flux script to run for this task.
   * @return flux
  **/
  @ApiModelProperty(required = true, value = "The Flux script to run for this task.")
  public String getFlux() {
    return flux;
  }

  public void setFlux(String flux) {
    this.flux = flux;
  }

  public Task every(String every) {
    this.every = every;
    return this;
  }

   /**
   * A simple task repetition schedule; parsed from Flux.
   * @return every
  **/
  @ApiModelProperty(value = "A simple task repetition schedule; parsed from Flux.")
  public String getEvery() {
    return every;
  }

  public void setEvery(String every) {
    this.every = every;
  }

  public Task cron(String cron) {
    this.cron = cron;
    return this;
  }

   /**
   * A task repetition schedule in the form &#39;* * * * * *&#39;; parsed from Flux.
   * @return cron
  **/
  @ApiModelProperty(value = "A task repetition schedule in the form '* * * * * *'; parsed from Flux.")
  public String getCron() {
    return cron;
  }

  public void setCron(String cron) {
    this.cron = cron;
  }

  public Task offset(String offset) {
    this.offset = offset;
    return this;
  }

   /**
   * Duration to delay after the schedule, before executing the task; parsed from flux.
   * @return offset
  **/
  @ApiModelProperty(value = "Duration to delay after the schedule, before executing the task; parsed from flux.")
  public String getOffset() {
    return offset;
  }

  public void setOffset(String offset) {
    this.offset = offset;
  }

   /**
   * Timestamp of latest scheduled, completed run, RFC3339.
   * @return latestCompleted
  **/
  @ApiModelProperty(value = "Timestamp of latest scheduled, completed run, RFC3339.")
  public OffsetDateTime getLatestCompleted() {
    return latestCompleted;
  }

   /**
   * Get createdAt
   * @return createdAt
  **/
  @ApiModelProperty(value = "")
  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

   /**
   * Get updatedAt
   * @return updatedAt
  **/
  @ApiModelProperty(value = "")
  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }

  public Task links(TaskLinks links) {
    this.links = links;
    return this;
  }

   /**
   * Get links
   * @return links
  **/
  @ApiModelProperty(value = "")
  public TaskLinks getLinks() {
    return links;
  }

  public void setLinks(TaskLinks links) {
    this.links = links;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Task task = (Task) o;
    return Objects.equals(this.id, task.id) &&
        Objects.equals(this.orgID, task.orgID) &&
        Objects.equals(this.org, task.org) &&
        Objects.equals(this.name, task.name) &&
        Objects.equals(this.status, task.status) &&
        Objects.equals(this.labels, task.labels) &&
        Objects.equals(this.authorizationID, task.authorizationID) &&
        Objects.equals(this.flux, task.flux) &&
        Objects.equals(this.every, task.every) &&
        Objects.equals(this.cron, task.cron) &&
        Objects.equals(this.offset, task.offset) &&
        Objects.equals(this.latestCompleted, task.latestCompleted) &&
        Objects.equals(this.createdAt, task.createdAt) &&
        Objects.equals(this.updatedAt, task.updatedAt) &&
        Objects.equals(this.links, task.links);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, orgID, org, name, status, labels, authorizationID, flux, every, cron, offset, latestCompleted, createdAt, updatedAt, links);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Task {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    orgID: ").append(toIndentedString(orgID)).append("\n");
    sb.append("    org: ").append(toIndentedString(org)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    labels: ").append(toIndentedString(labels)).append("\n");
    sb.append("    authorizationID: ").append(toIndentedString(authorizationID)).append("\n");
    sb.append("    flux: ").append(toIndentedString(flux)).append("\n");
    sb.append("    every: ").append(toIndentedString(every)).append("\n");
    sb.append("    cron: ").append(toIndentedString(cron)).append("\n");
    sb.append("    offset: ").append(toIndentedString(offset)).append("\n");
    sb.append("    latestCompleted: ").append(toIndentedString(latestCompleted)).append("\n");
    sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
    sb.append("    updatedAt: ").append(toIndentedString(updatedAt)).append("\n");
    sb.append("    links: ").append(toIndentedString(links)).append("\n");
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

