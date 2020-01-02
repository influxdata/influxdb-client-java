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


package com.influxdb.client.domain;

import java.util.Objects;
import java.util.Arrays;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.influxdb.client.domain.Label;
import com.influxdb.client.domain.NotificationEndpointBase;
import com.influxdb.client.domain.NotificationEndpointBaseLinks;
import com.influxdb.client.domain.NotificationEndpointType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * SlackNotificationEndpoint
 */

public class SlackNotificationEndpoint extends NotificationEndpoint {
  public static final String SERIALIZED_NAME_URL = "url";
  @SerializedName(SERIALIZED_NAME_URL)
  private String url;

  public static final String SERIALIZED_NAME_TOKEN = "token";
  @SerializedName(SERIALIZED_NAME_TOKEN)
  private String token;

  public SlackNotificationEndpoint url(String url) {
    this.url = url;
    return this;
  }

   /**
   * Specifies the URL of the Slack endpoint. Specify either &#x60;URL&#x60; or &#x60;Token&#x60;.
   * @return url
  **/
  @ApiModelProperty(required = true, value = "Specifies the URL of the Slack endpoint. Specify either `URL` or `Token`.")
  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public SlackNotificationEndpoint token(String token) {
    this.token = token;
    return this;
  }

   /**
   * Specifies the API token string. Specify either &#x60;URL&#x60; or &#x60;Token&#x60;.
   * @return token
  **/
  @ApiModelProperty(value = "Specifies the API token string. Specify either `URL` or `Token`.")
  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SlackNotificationEndpoint slackNotificationEndpoint = (SlackNotificationEndpoint) o;
    return Objects.equals(this.url, slackNotificationEndpoint.url) &&
        Objects.equals(this.token, slackNotificationEndpoint.token) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(url, token, super.hashCode());
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SlackNotificationEndpoint {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    url: ").append(toIndentedString(url)).append("\n");
    sb.append("    token: ").append(toIndentedString(token)).append("\n");
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

