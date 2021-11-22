/*
 * Influx OSS API Service
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
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.influxdb.client.domain.Statement;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * A set of statements
 */
@ApiModel(description = "A set of statements")

public class Block extends Node {
  public static final String SERIALIZED_NAME_TYPE = "type";
  @SerializedName(SERIALIZED_NAME_TYPE)
  private String type;

  public static final String SERIALIZED_NAME_BODY = "body";
  @SerializedName(SERIALIZED_NAME_BODY)
  @JsonAdapter(BlockBodyAdapter.class)
  private List<Statement> body = new ArrayList<>();

  public Block type(String type) {
    this.type = type;
    return this;
  }

   /**
   * Type of AST node
   * @return type
  **/
  @ApiModelProperty(value = "Type of AST node")
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Block body(List<Statement> body) {
    this.body = body;
    return this;
  }

  public Block addBodyItem(Statement bodyItem) {
    if (this.body == null) {
      this.body = new ArrayList<>();
    }
    this.body.add(bodyItem);
    return this;
  }

   /**
   * Block body
   * @return body
  **/
  @ApiModelProperty(value = "Block body")
  public List<Statement> getBody() {
    return body;
  }

  public void setBody(List<Statement> body) {
    this.body = body;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Block block = (Block) o;
    return Objects.equals(this.type, block.type) &&
        Objects.equals(this.body, block.body) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, body, super.hashCode());
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Block {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    body: ").append(toIndentedString(body)).append("\n");
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

  public static class BlockBodyAdapter implements JsonDeserializer<Object>, JsonSerializer<Object> {

    public BlockBodyAdapter() {
    }

    @Override
    public Object deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {

      List<String> discriminator = Arrays.asList("type");

      List<Object> results = new ArrayList<>();

      for (JsonElement arrayItem: json.getAsJsonArray()){
        JsonObject jsonObject = arrayItem.getAsJsonObject();

        String[] types = discriminator.stream().map(jsonObject::get).filter(Objects::nonNull).map(JsonElement::getAsString).toArray(String[]::new);

        results.add(deserialize(types, jsonObject, context));
      }

      return results;
    }

    @Override
    public JsonElement serialize(Object object, Type typeOfSrc, JsonSerializationContext context) {

      return context.serialize(object);
    }

    private Object deserialize(final String[] types, final JsonElement json, final JsonDeserializationContext context) {

      if (Arrays.equals(new String[]{ "BadStatement" }, types)) {
        return context.deserialize(json, BadStatement.class);
      }
      if (Arrays.equals(new String[]{ "VariableAssignment" }, types)) {
        return context.deserialize(json, VariableAssignment.class);
      }
      if (Arrays.equals(new String[]{ "MemberAssignment" }, types)) {
        return context.deserialize(json, MemberAssignment.class);
      }
      if (Arrays.equals(new String[]{ "ExpressionStatement" }, types)) {
        return context.deserialize(json, ExpressionStatement.class);
      }
      if (Arrays.equals(new String[]{ "ReturnStatement" }, types)) {
        return context.deserialize(json, ReturnStatement.class);
      }
      if (Arrays.equals(new String[]{ "OptionStatement" }, types)) {
        return context.deserialize(json, OptionStatement.class);
      }
      if (Arrays.equals(new String[]{ "BuiltinStatement" }, types)) {
        return context.deserialize(json, BuiltinStatement.class);
      }
      if (Arrays.equals(new String[]{ "TestStatement" }, types)) {
        return context.deserialize(json, TestStatement.class);
      }

      return context.deserialize(json, Object.class);
    }
  }
}

