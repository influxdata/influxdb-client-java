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
import com.influxdb.client.domain.Dialect;
import com.influxdb.client.domain.File;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.IOException;

/**
 * Query influx with specific return formatting.
 */
@ApiModel(description = "Query influx with specific return formatting.")

public class Query {
  public static final String SERIALIZED_NAME_EXTERN = "extern";
  @SerializedName(SERIALIZED_NAME_EXTERN)
  private File extern = null;

  public static final String SERIALIZED_NAME_QUERY = "query";
  @SerializedName(SERIALIZED_NAME_QUERY)
  private String query;

  /**
   * The type of query.
   */
  @JsonAdapter(TypeEnum.Adapter.class)
  public enum TypeEnum {
    FLUX("flux"),
    
    INFLUXQL("influxql");

    private String value;

    TypeEnum(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    public static TypeEnum fromValue(String text) {
      for (TypeEnum b : TypeEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }

    public static class Adapter extends TypeAdapter<TypeEnum> {
      @Override
      public void write(final JsonWriter jsonWriter, final TypeEnum enumeration) throws IOException {
        jsonWriter.value(enumeration.getValue());
      }

      @Override
      public TypeEnum read(final JsonReader jsonReader) throws IOException {
        String value = jsonReader.nextString();
        return TypeEnum.fromValue(String.valueOf(value));
      }
    }
  }

  public static final String SERIALIZED_NAME_TYPE = "type";
  @SerializedName(SERIALIZED_NAME_TYPE)
  private TypeEnum type = TypeEnum.FLUX;

  public static final String SERIALIZED_NAME_DB = "db";
  @SerializedName(SERIALIZED_NAME_DB)
  private String db;

  public static final String SERIALIZED_NAME_RP = "rp";
  @SerializedName(SERIALIZED_NAME_RP)
  private String rp;

  public static final String SERIALIZED_NAME_CLUSTER = "cluster";
  @SerializedName(SERIALIZED_NAME_CLUSTER)
  private String cluster;

  public static final String SERIALIZED_NAME_DIALECT = "dialect";
  @SerializedName(SERIALIZED_NAME_DIALECT)
  private Dialect dialect = null;

  public Query extern(File extern) {
    this.extern = extern;
    return this;
  }

   /**
   * Get extern
   * @return extern
  **/
  @ApiModelProperty(value = "")
  public File getExtern() {
    return extern;
  }

  public void setExtern(File extern) {
    this.extern = extern;
  }

  public Query query(String query) {
    this.query = query;
    return this;
  }

   /**
   * Query script to execute.
   * @return query
  **/
  @ApiModelProperty(required = true, value = "Query script to execute.")
  public String getQuery() {
    return query;
  }

  public void setQuery(String query) {
    this.query = query;
  }

  public Query type(TypeEnum type) {
    this.type = type;
    return this;
  }

   /**
   * The type of query.
   * @return type
  **/
  @ApiModelProperty(value = "The type of query.")
  public TypeEnum getType() {
    return type;
  }

  public void setType(TypeEnum type) {
    this.type = type;
  }

  public Query db(String db) {
    this.db = db;
    return this;
  }

   /**
   * Required for &#x60;influxql&#x60; type queries.
   * @return db
  **/
  @ApiModelProperty(value = "Required for `influxql` type queries.")
  public String getDb() {
    return db;
  }

  public void setDb(String db) {
    this.db = db;
  }

  public Query rp(String rp) {
    this.rp = rp;
    return this;
  }

   /**
   * Required for &#x60;influxql&#x60; type queries.
   * @return rp
  **/
  @ApiModelProperty(value = "Required for `influxql` type queries.")
  public String getRp() {
    return rp;
  }

  public void setRp(String rp) {
    this.rp = rp;
  }

  public Query cluster(String cluster) {
    this.cluster = cluster;
    return this;
  }

   /**
   * Required for &#x60;influxql&#x60; type queries.
   * @return cluster
  **/
  @ApiModelProperty(value = "Required for `influxql` type queries.")
  public String getCluster() {
    return cluster;
  }

  public void setCluster(String cluster) {
    this.cluster = cluster;
  }

  public Query dialect(Dialect dialect) {
    this.dialect = dialect;
    return this;
  }

   /**
   * Get dialect
   * @return dialect
  **/
  @ApiModelProperty(value = "")
  public Dialect getDialect() {
    return dialect;
  }

  public void setDialect(Dialect dialect) {
    this.dialect = dialect;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Query query = (Query) o;
    return Objects.equals(this.extern, query.extern) &&
        Objects.equals(this.query, query.query) &&
        Objects.equals(this.type, query.type) &&
        Objects.equals(this.db, query.db) &&
        Objects.equals(this.rp, query.rp) &&
        Objects.equals(this.cluster, query.cluster) &&
        Objects.equals(this.dialect, query.dialect);
  }

  @Override
  public int hashCode() {
    return Objects.hash(extern, query, type, db, rp, cluster, dialect);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Query {\n");
    sb.append("    extern: ").append(toIndentedString(extern)).append("\n");
    sb.append("    query: ").append(toIndentedString(query)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    db: ").append(toIndentedString(db)).append("\n");
    sb.append("    rp: ").append(toIndentedString(rp)).append("\n");
    sb.append("    cluster: ").append(toIndentedString(cluster)).append("\n");
    sb.append("    dialect: ").append(toIndentedString(dialect)).append("\n");
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

