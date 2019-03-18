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
import java.util.ArrayList;
import java.util.List;
import org.influxdata.client.domain.Links;
import org.influxdata.client.domain.Organization;

/**
 * Organizations
 */
@javax.annotation.Generated(value = "org.influxdata.codegen.InfluxJavaGenerator", date = "2019-03-18T13:14:51.923+01:00[Europe/Prague]")
public class Organizations {
  public static final String SERIALIZED_NAME_LINKS = "links";
  @SerializedName(SERIALIZED_NAME_LINKS)
  private Links links = null;

  public static final String SERIALIZED_NAME_ORGS = "orgs";
  @SerializedName(SERIALIZED_NAME_ORGS)
  private List<Organization> orgs = new ArrayList<>();

  public Organizations links(Links links) {
    this.links = links;
    return this;
  }

   /**
   * Get links
   * @return links
  **/
  @ApiModelProperty(value = "")
  public Links getLinks() {
    return links;
  }

  public void setLinks(Links links) {
    this.links = links;
  }

  public Organizations orgs(List<Organization> orgs) {
    this.orgs = orgs;
    return this;
  }

  public Organizations addOrgsItem(Organization orgsItem) {
    if (this.orgs == null) {
      this.orgs = new ArrayList<>();
    }
    this.orgs.add(orgsItem);
    return this;
  }

   /**
   * Get orgs
   * @return orgs
  **/
  @ApiModelProperty(value = "")
  public List<Organization> getOrgs() {
    return orgs;
  }

  public void setOrgs(List<Organization> orgs) {
    this.orgs = orgs;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Organizations organizations = (Organizations) o;
    return Objects.equals(this.links, organizations.links) &&
        Objects.equals(this.orgs, organizations.orgs);
  }

  @Override
  public int hashCode() {
    return Objects.hash(links, orgs);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Organizations {\n");
    sb.append("    links: ").append(toIndentedString(links)).append("\n");
    sb.append("    orgs: ").append(toIndentedString(orgs)).append("\n");
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

