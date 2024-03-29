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
 * Replication
 */

public class Replication {
  public static final String SERIALIZED_NAME_ID = "id";
  @SerializedName(SERIALIZED_NAME_ID)
  private String id;

  public static final String SERIALIZED_NAME_NAME = "name";
  @SerializedName(SERIALIZED_NAME_NAME)
  private String name;

  public static final String SERIALIZED_NAME_DESCRIPTION = "description";
  @SerializedName(SERIALIZED_NAME_DESCRIPTION)
  private String description;

  public static final String SERIALIZED_NAME_ORG_I_D = "orgID";
  @SerializedName(SERIALIZED_NAME_ORG_I_D)
  private String orgID;

  public static final String SERIALIZED_NAME_REMOTE_I_D = "remoteID";
  @SerializedName(SERIALIZED_NAME_REMOTE_I_D)
  private String remoteID;

  public static final String SERIALIZED_NAME_LOCAL_BUCKET_I_D = "localBucketID";
  @SerializedName(SERIALIZED_NAME_LOCAL_BUCKET_I_D)
  private String localBucketID;

  public static final String SERIALIZED_NAME_REMOTE_BUCKET_I_D = "remoteBucketID";
  @SerializedName(SERIALIZED_NAME_REMOTE_BUCKET_I_D)
  private String remoteBucketID;

  public static final String SERIALIZED_NAME_MAX_QUEUE_SIZE_BYTES = "maxQueueSizeBytes";
  @SerializedName(SERIALIZED_NAME_MAX_QUEUE_SIZE_BYTES)
  private Long maxQueueSizeBytes;

  public static final String SERIALIZED_NAME_CURRENT_QUEUE_SIZE_BYTES = "currentQueueSizeBytes";
  @SerializedName(SERIALIZED_NAME_CURRENT_QUEUE_SIZE_BYTES)
  private Long currentQueueSizeBytes;

  public static final String SERIALIZED_NAME_LATEST_RESPONSE_CODE = "latestResponseCode";
  @SerializedName(SERIALIZED_NAME_LATEST_RESPONSE_CODE)
  private Integer latestResponseCode;

  public static final String SERIALIZED_NAME_LATEST_ERROR_MESSAGE = "latestErrorMessage";
  @SerializedName(SERIALIZED_NAME_LATEST_ERROR_MESSAGE)
  private String latestErrorMessage;

  public static final String SERIALIZED_NAME_DROP_NON_RETRYABLE_DATA = "dropNonRetryableData";
  @SerializedName(SERIALIZED_NAME_DROP_NON_RETRYABLE_DATA)
  private Boolean dropNonRetryableData;

  public Replication id(String id) {
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

  public Replication name(String name) {
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

  public Replication description(String description) {
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

  public Replication orgID(String orgID) {
    this.orgID = orgID;
    return this;
  }

   /**
   * Get orgID
   * @return orgID
  **/
  public String getOrgID() {
    return orgID;
  }

  public void setOrgID(String orgID) {
    this.orgID = orgID;
  }

  public Replication remoteID(String remoteID) {
    this.remoteID = remoteID;
    return this;
  }

   /**
   * Get remoteID
   * @return remoteID
  **/
  public String getRemoteID() {
    return remoteID;
  }

  public void setRemoteID(String remoteID) {
    this.remoteID = remoteID;
  }

  public Replication localBucketID(String localBucketID) {
    this.localBucketID = localBucketID;
    return this;
  }

   /**
   * Get localBucketID
   * @return localBucketID
  **/
  public String getLocalBucketID() {
    return localBucketID;
  }

  public void setLocalBucketID(String localBucketID) {
    this.localBucketID = localBucketID;
  }

  public Replication remoteBucketID(String remoteBucketID) {
    this.remoteBucketID = remoteBucketID;
    return this;
  }

   /**
   * Get remoteBucketID
   * @return remoteBucketID
  **/
  public String getRemoteBucketID() {
    return remoteBucketID;
  }

  public void setRemoteBucketID(String remoteBucketID) {
    this.remoteBucketID = remoteBucketID;
  }

  public Replication maxQueueSizeBytes(Long maxQueueSizeBytes) {
    this.maxQueueSizeBytes = maxQueueSizeBytes;
    return this;
  }

   /**
   * Get maxQueueSizeBytes
   * @return maxQueueSizeBytes
  **/
  public Long getMaxQueueSizeBytes() {
    return maxQueueSizeBytes;
  }

  public void setMaxQueueSizeBytes(Long maxQueueSizeBytes) {
    this.maxQueueSizeBytes = maxQueueSizeBytes;
  }

  public Replication currentQueueSizeBytes(Long currentQueueSizeBytes) {
    this.currentQueueSizeBytes = currentQueueSizeBytes;
    return this;
  }

   /**
   * Get currentQueueSizeBytes
   * @return currentQueueSizeBytes
  **/
  public Long getCurrentQueueSizeBytes() {
    return currentQueueSizeBytes;
  }

  public void setCurrentQueueSizeBytes(Long currentQueueSizeBytes) {
    this.currentQueueSizeBytes = currentQueueSizeBytes;
  }

  public Replication latestResponseCode(Integer latestResponseCode) {
    this.latestResponseCode = latestResponseCode;
    return this;
  }

   /**
   * Get latestResponseCode
   * @return latestResponseCode
  **/
  public Integer getLatestResponseCode() {
    return latestResponseCode;
  }

  public void setLatestResponseCode(Integer latestResponseCode) {
    this.latestResponseCode = latestResponseCode;
  }

  public Replication latestErrorMessage(String latestErrorMessage) {
    this.latestErrorMessage = latestErrorMessage;
    return this;
  }

   /**
   * Get latestErrorMessage
   * @return latestErrorMessage
  **/
  public String getLatestErrorMessage() {
    return latestErrorMessage;
  }

  public void setLatestErrorMessage(String latestErrorMessage) {
    this.latestErrorMessage = latestErrorMessage;
  }

  public Replication dropNonRetryableData(Boolean dropNonRetryableData) {
    this.dropNonRetryableData = dropNonRetryableData;
    return this;
  }

   /**
   * Get dropNonRetryableData
   * @return dropNonRetryableData
  **/
  public Boolean getDropNonRetryableData() {
    return dropNonRetryableData;
  }

  public void setDropNonRetryableData(Boolean dropNonRetryableData) {
    this.dropNonRetryableData = dropNonRetryableData;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Replication replication = (Replication) o;
    return Objects.equals(this.id, replication.id) &&
        Objects.equals(this.name, replication.name) &&
        Objects.equals(this.description, replication.description) &&
        Objects.equals(this.orgID, replication.orgID) &&
        Objects.equals(this.remoteID, replication.remoteID) &&
        Objects.equals(this.localBucketID, replication.localBucketID) &&
        Objects.equals(this.remoteBucketID, replication.remoteBucketID) &&
        Objects.equals(this.maxQueueSizeBytes, replication.maxQueueSizeBytes) &&
        Objects.equals(this.currentQueueSizeBytes, replication.currentQueueSizeBytes) &&
        Objects.equals(this.latestResponseCode, replication.latestResponseCode) &&
        Objects.equals(this.latestErrorMessage, replication.latestErrorMessage) &&
        Objects.equals(this.dropNonRetryableData, replication.dropNonRetryableData);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, description, orgID, remoteID, localBucketID, remoteBucketID, maxQueueSizeBytes, currentQueueSizeBytes, latestResponseCode, latestErrorMessage, dropNonRetryableData);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Replication {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    orgID: ").append(toIndentedString(orgID)).append("\n");
    sb.append("    remoteID: ").append(toIndentedString(remoteID)).append("\n");
    sb.append("    localBucketID: ").append(toIndentedString(localBucketID)).append("\n");
    sb.append("    remoteBucketID: ").append(toIndentedString(remoteBucketID)).append("\n");
    sb.append("    maxQueueSizeBytes: ").append(toIndentedString(maxQueueSizeBytes)).append("\n");
    sb.append("    currentQueueSizeBytes: ").append(toIndentedString(currentQueueSizeBytes)).append("\n");
    sb.append("    latestResponseCode: ").append(toIndentedString(latestResponseCode)).append("\n");
    sb.append("    latestErrorMessage: ").append(toIndentedString(latestErrorMessage)).append("\n");
    sb.append("    dropNonRetryableData: ").append(toIndentedString(dropNonRetryableData)).append("\n");
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

