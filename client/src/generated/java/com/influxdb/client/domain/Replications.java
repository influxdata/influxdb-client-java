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
import com.influxdb.client.domain.Replication;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Replications
 */

public class Replications {
  public static final String SERIALIZED_NAME_REPLICATIONS = "replications";
  @SerializedName(SERIALIZED_NAME_REPLICATIONS)
  private List<Replication> replications = new ArrayList<>();

  public Replications replications(List<Replication> replications) {
    this.replications = replications;
    return this;
  }

  public Replications addReplicationsItem(Replication replicationsItem) {
    if (this.replications == null) {
      this.replications = new ArrayList<>();
    }
    this.replications.add(replicationsItem);
    return this;
  }

   /**
   * Get replications
   * @return replications
  **/
  public List<Replication> getReplications() {
    return replications;
  }

  public void setReplications(List<Replication> replications) {
    this.replications = replications;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Replications replications = (Replications) o;
    return Objects.equals(this.replications, replications.replications);
  }

  @Override
  public int hashCode() {
    return Objects.hash(replications);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Replications {\n");
    sb.append("    replications: ").append(toIndentedString(replications)).append("\n");
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

