package com.influxdb.client.service;

import retrofit2.Call;
import retrofit2.http.*;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.MultipartBody;

import com.influxdb.client.domain.AddResourceMemberRequestBody;
import com.influxdb.client.domain.Error;
import com.influxdb.client.domain.LabelMapping;
import com.influxdb.client.domain.LabelResponse;
import com.influxdb.client.domain.LabelsResponse;
import com.influxdb.client.domain.OperationLogs;
import com.influxdb.client.domain.Organization;
import com.influxdb.client.domain.Organizations;
import com.influxdb.client.domain.ResourceMember;
import com.influxdb.client.domain.ResourceMembers;
import com.influxdb.client.domain.ResourceOwner;
import com.influxdb.client.domain.ResourceOwners;
import com.influxdb.client.domain.SecretKeys;
import com.influxdb.client.domain.SecretKeysResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface OrganizationsService {
  /**
   * Delete an organization
   * 
   * @param orgID The ID of the organization to delete. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("api/v2/orgs/{orgID}")
  Call<Void> deleteOrgsID(
    @retrofit2.http.Path("orgID") String orgID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Delete a label from an organization
   * 
   * @param orgID The organization ID. (required)
   * @param labelID The label ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("api/v2/orgs/{orgID}/labels/{labelID}")
  Call<Void> deleteOrgsIDLabelsID(
    @retrofit2.http.Path("orgID") String orgID, @retrofit2.http.Path("labelID") String labelID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Remove a member from an organization
   * 
   * @param userID The ID of the member to remove. (required)
   * @param orgID The organization ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("api/v2/orgs/{orgID}/members/{userID}")
  Call<Void> deleteOrgsIDMembersID(
    @retrofit2.http.Path("userID") String userID, @retrofit2.http.Path("orgID") String orgID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Remove an owner from an organization
   * 
   * @param userID The ID of the owner to remove. (required)
   * @param orgID The organization ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("api/v2/orgs/{orgID}/owners/{userID}")
  Call<Void> deleteOrgsIDOwnersID(
    @retrofit2.http.Path("userID") String userID, @retrofit2.http.Path("orgID") String orgID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * List all organizations
   * 
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param org Filter organizations to a specific organization name. (optional)
   * @param orgID Filter organizations to a specific organization ID. (optional)
   * @return Call&lt;Organizations&gt;
   */
  @GET("api/v2/orgs")
  Call<Organizations> getOrgs(
    @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Query("org") String org, @retrofit2.http.Query("orgID") String orgID
  );

  /**
   * Retrieve an organization
   * 
   * @param orgID The ID of the organization to get. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Organization&gt;
   */
  @GET("api/v2/orgs/{orgID}")
  Call<Organization> getOrgsID(
    @retrofit2.http.Path("orgID") String orgID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * List all labels for a organization
   * 
   * @param orgID The organization ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;LabelsResponse&gt;
   */
  @GET("api/v2/orgs/{orgID}/labels")
  Call<LabelsResponse> getOrgsIDLabels(
    @retrofit2.http.Path("orgID") String orgID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Retrieve operation logs for an organization
   * 
   * @param orgID The organization ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param offset  (optional)
   * @param limit  (optional, default to 20)
   * @return Call&lt;OperationLogs&gt;
   */
  @GET("api/v2/orgs/{orgID}/logs")
  Call<OperationLogs> getOrgsIDLogs(
    @retrofit2.http.Path("orgID") String orgID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Query("offset") Integer offset, @retrofit2.http.Query("limit") Integer limit
  );

  /**
   * List all members of an organization
   * 
   * @param orgID The organization ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;ResourceMembers&gt;
   */
  @GET("api/v2/orgs/{orgID}/members")
  Call<ResourceMembers> getOrgsIDMembers(
    @retrofit2.http.Path("orgID") String orgID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * List all owners of an organization
   * 
   * @param orgID The organization ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;ResourceOwners&gt;
   */
  @GET("api/v2/orgs/{orgID}/owners")
  Call<ResourceOwners> getOrgsIDOwners(
    @retrofit2.http.Path("orgID") String orgID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * List all secret keys for an organization
   * 
   * @param orgID The organization ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;SecretKeysResponse&gt;
   */
  @GET("api/v2/orgs/{orgID}/secrets")
  Call<SecretKeysResponse> getOrgsIDSecrets(
    @retrofit2.http.Path("orgID") String orgID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Update an organization
   * 
   * @param orgID The ID of the organization to get. (required)
   * @param organization Organization update to apply (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Organization&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @PATCH("api/v2/orgs/{orgID}")
  Call<Organization> patchOrgsID(
    @retrofit2.http.Path("orgID") String orgID, @retrofit2.http.Body Organization organization, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Update secrets in an organization
   * 
   * @param orgID The organization ID. (required)
   * @param requestBody Secret key value pairs to update/add (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @PATCH("api/v2/orgs/{orgID}/secrets")
  Call<Void> patchOrgsIDSecrets(
    @retrofit2.http.Path("orgID") String orgID, @retrofit2.http.Body Map<String, String> requestBody, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Create an organization
   * 
   * @param organization Organization to create (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Organization&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/orgs")
  Call<Organization> postOrgs(
    @retrofit2.http.Body Organization organization, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Add a label to an organization
   * 
   * @param orgID The organization ID. (required)
   * @param labelMapping Label to add (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;LabelResponse&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/orgs/{orgID}/labels")
  Call<LabelResponse> postOrgsIDLabels(
    @retrofit2.http.Path("orgID") String orgID, @retrofit2.http.Body LabelMapping labelMapping, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Add a member to an organization
   * 
   * @param orgID The organization ID. (required)
   * @param addResourceMemberRequestBody User to add as member (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;ResourceMember&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/orgs/{orgID}/members")
  Call<ResourceMember> postOrgsIDMembers(
    @retrofit2.http.Path("orgID") String orgID, @retrofit2.http.Body AddResourceMemberRequestBody addResourceMemberRequestBody, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Add an owner to an organization
   * 
   * @param orgID The organization ID. (required)
   * @param addResourceMemberRequestBody User to add as owner (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;ResourceOwner&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/orgs/{orgID}/owners")
  Call<ResourceOwner> postOrgsIDOwners(
    @retrofit2.http.Path("orgID") String orgID, @retrofit2.http.Body AddResourceMemberRequestBody addResourceMemberRequestBody, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Delete secrets from an organization
   * 
   * @param orgID The organization ID. (required)
   * @param secretKeys Secret key to delete (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/orgs/{orgID}/secrets/delete")
  Call<Void> postOrgsIDSecrets(
    @retrofit2.http.Path("orgID") String orgID, @retrofit2.http.Body SecretKeys secretKeys, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

}
