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
import com.influxdb.client.domain.AuthorizationUpdateRequest;
import com.influxdb.client.domain.Permission;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * LegacyAuthorizationPostRequest
 */

public class LegacyAuthorizationPostRequest extends AuthorizationUpdateRequest {
  public static final String SERIALIZED_NAME_ORG_I_D = "orgID";
  @SerializedName(SERIALIZED_NAME_ORG_I_D)
  private String orgID;

  public static final String SERIALIZED_NAME_USER_I_D = "userID";
  @SerializedName(SERIALIZED_NAME_USER_I_D)
  private String userID;

  public static final String SERIALIZED_NAME_TOKEN = "token";
  @SerializedName(SERIALIZED_NAME_TOKEN)
  private String token;

  public static final String SERIALIZED_NAME_PERMISSIONS = "permissions";
  @SerializedName(SERIALIZED_NAME_PERMISSIONS)
  private List<Permission> permissions = new ArrayList<>();

  public LegacyAuthorizationPostRequest orgID(String orgID) {
    this.orgID = orgID;
    return this;
  }

   /**
   * ID of org that authorization is scoped to.
   * @return orgID
  **/
  public String getOrgID() {
    return orgID;
  }

  public void setOrgID(String orgID) {
    this.orgID = orgID;
  }

  public LegacyAuthorizationPostRequest userID(String userID) {
    this.userID = userID;
    return this;
  }

   /**
   * ID of user that authorization is scoped to.
   * @return userID
  **/
  public String getUserID() {
    return userID;
  }

  public void setUserID(String userID) {
    this.userID = userID;
  }

  public LegacyAuthorizationPostRequest token(String token) {
    this.token = token;
    return this;
  }

   /**
   * Token (name) of the authorization
   * @return token
  **/
  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public LegacyAuthorizationPostRequest permissions(List<Permission> permissions) {
    this.permissions = permissions;
    return this;
  }

  public LegacyAuthorizationPostRequest addPermissionsItem(Permission permissionsItem) {
    if (this.permissions == null) {
      this.permissions = new ArrayList<>();
    }
    this.permissions.add(permissionsItem);
    return this;
  }

   /**
   * List of permissions for an auth.  An auth must have at least one Permission.
   * @return permissions
  **/
  public List<Permission> getPermissions() {
    return permissions;
  }

  public void setPermissions(List<Permission> permissions) {
    this.permissions = permissions;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LegacyAuthorizationPostRequest legacyAuthorizationPostRequest = (LegacyAuthorizationPostRequest) o;
    return Objects.equals(this.orgID, legacyAuthorizationPostRequest.orgID) &&
        Objects.equals(this.userID, legacyAuthorizationPostRequest.userID) &&
        Objects.equals(this.token, legacyAuthorizationPostRequest.token) &&
        Objects.equals(this.permissions, legacyAuthorizationPostRequest.permissions) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(orgID, userID, token, permissions, super.hashCode());
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LegacyAuthorizationPostRequest {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    orgID: ").append(toIndentedString(orgID)).append("\n");
    sb.append("    userID: ").append(toIndentedString(userID)).append("\n");
    sb.append("    token: ").append(toIndentedString(token)).append("\n");
    sb.append("    permissions: ").append(toIndentedString(permissions)).append("\n");
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

