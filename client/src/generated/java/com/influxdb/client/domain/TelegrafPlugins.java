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
import com.influxdb.client.domain.TelegrafPlugin;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * TelegrafPlugins
 */

public class TelegrafPlugins {
  public static final String SERIALIZED_NAME_VERSION = "version";
  @SerializedName(SERIALIZED_NAME_VERSION)
  private String version;

  public static final String SERIALIZED_NAME_OS = "os";
  @SerializedName(SERIALIZED_NAME_OS)
  private String os;

  public static final String SERIALIZED_NAME_PLUGINS = "plugins";
  @SerializedName(SERIALIZED_NAME_PLUGINS)
  private List<TelegrafPlugin> plugins = new ArrayList<>();

  public TelegrafPlugins version(String version) {
    this.version = version;
    return this;
  }

   /**
   * Get version
   * @return version
  **/
  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public TelegrafPlugins os(String os) {
    this.os = os;
    return this;
  }

   /**
   * Get os
   * @return os
  **/
  public String getOs() {
    return os;
  }

  public void setOs(String os) {
    this.os = os;
  }

  public TelegrafPlugins plugins(List<TelegrafPlugin> plugins) {
    this.plugins = plugins;
    return this;
  }

  public TelegrafPlugins addPluginsItem(TelegrafPlugin pluginsItem) {
    if (this.plugins == null) {
      this.plugins = new ArrayList<>();
    }
    this.plugins.add(pluginsItem);
    return this;
  }

   /**
   * Get plugins
   * @return plugins
  **/
  public List<TelegrafPlugin> getPlugins() {
    return plugins;
  }

  public void setPlugins(List<TelegrafPlugin> plugins) {
    this.plugins = plugins;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TelegrafPlugins telegrafPlugins = (TelegrafPlugins) o;
    return Objects.equals(this.version, telegrafPlugins.version) &&
        Objects.equals(this.os, telegrafPlugins.os) &&
        Objects.equals(this.plugins, telegrafPlugins.plugins);
  }

  @Override
  public int hashCode() {
    return Objects.hash(version, os, plugins);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TelegrafPlugins {\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
    sb.append("    os: ").append(toIndentedString(os)).append("\n");
    sb.append("    plugins: ").append(toIndentedString(plugins)).append("\n");
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
