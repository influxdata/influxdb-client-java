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
 * RemoteConnectionUpdateRequest
 */

public class RemoteConnectionUpdateRequest {
  public static final String SERIALIZED_NAME_NAME = "name";
  @SerializedName(SERIALIZED_NAME_NAME)
  private String name;

  public static final String SERIALIZED_NAME_DESCRIPTION = "description";
  @SerializedName(SERIALIZED_NAME_DESCRIPTION)
  private String description;

  public static final String SERIALIZED_NAME_REMOTE_U_R_L = "remoteURL";
  @SerializedName(SERIALIZED_NAME_REMOTE_U_R_L)
  private String remoteURL;

  public static final String SERIALIZED_NAME_REMOTE_A_P_I_TOKEN = "remoteAPIToken";
  @SerializedName(SERIALIZED_NAME_REMOTE_A_P_I_TOKEN)
  private String remoteAPIToken;

  public static final String SERIALIZED_NAME_REMOTE_ORG_I_D = "remoteOrgID";
  @SerializedName(SERIALIZED_NAME_REMOTE_ORG_I_D)
  private String remoteOrgID;

  public static final String SERIALIZED_NAME_ALLOW_INSECURE_T_L_S = "allowInsecureTLS";
  @SerializedName(SERIALIZED_NAME_ALLOW_INSECURE_T_L_S)
  private Boolean allowInsecureTLS = false;

  public RemoteConnectionUpdateRequest name(String name) {
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

  public RemoteConnectionUpdateRequest description(String description) {
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

  public RemoteConnectionUpdateRequest remoteURL(String remoteURL) {
    this.remoteURL = remoteURL;
    return this;
  }

   /**
   * Get remoteURL
   * @return remoteURL
  **/
  public String getRemoteURL() {
    return remoteURL;
  }

  public void setRemoteURL(String remoteURL) {
    this.remoteURL = remoteURL;
  }

  public RemoteConnectionUpdateRequest remoteAPIToken(String remoteAPIToken) {
    this.remoteAPIToken = remoteAPIToken;
    return this;
  }

   /**
   * Get remoteAPIToken
   * @return remoteAPIToken
  **/
  public String getRemoteAPIToken() {
    return remoteAPIToken;
  }

  public void setRemoteAPIToken(String remoteAPIToken) {
    this.remoteAPIToken = remoteAPIToken;
  }

  public RemoteConnectionUpdateRequest remoteOrgID(String remoteOrgID) {
    this.remoteOrgID = remoteOrgID;
    return this;
  }

   /**
   * Get remoteOrgID
   * @return remoteOrgID
  **/
  public String getRemoteOrgID() {
    return remoteOrgID;
  }

  public void setRemoteOrgID(String remoteOrgID) {
    this.remoteOrgID = remoteOrgID;
  }

  public RemoteConnectionUpdateRequest allowInsecureTLS(Boolean allowInsecureTLS) {
    this.allowInsecureTLS = allowInsecureTLS;
    return this;
  }

   /**
   * Get allowInsecureTLS
   * @return allowInsecureTLS
  **/
  public Boolean getAllowInsecureTLS() {
    return allowInsecureTLS;
  }

  public void setAllowInsecureTLS(Boolean allowInsecureTLS) {
    this.allowInsecureTLS = allowInsecureTLS;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RemoteConnectionUpdateRequest remoteConnectionUpdateRequest = (RemoteConnectionUpdateRequest) o;
    return Objects.equals(this.name, remoteConnectionUpdateRequest.name) &&
        Objects.equals(this.description, remoteConnectionUpdateRequest.description) &&
        Objects.equals(this.remoteURL, remoteConnectionUpdateRequest.remoteURL) &&
        Objects.equals(this.remoteAPIToken, remoteConnectionUpdateRequest.remoteAPIToken) &&
        Objects.equals(this.remoteOrgID, remoteConnectionUpdateRequest.remoteOrgID) &&
        Objects.equals(this.allowInsecureTLS, remoteConnectionUpdateRequest.allowInsecureTLS);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, description, remoteURL, remoteAPIToken, remoteOrgID, allowInsecureTLS);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RemoteConnectionUpdateRequest {\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    remoteURL: ").append(toIndentedString(remoteURL)).append("\n");
    sb.append("    remoteAPIToken: ").append(toIndentedString(remoteAPIToken)).append("\n");
    sb.append("    remoteOrgID: ").append(toIndentedString(remoteOrgID)).append("\n");
    sb.append("    allowInsecureTLS: ").append(toIndentedString(allowInsecureTLS)).append("\n");
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
