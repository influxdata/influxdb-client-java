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
import com.influxdb.client.domain.ResourceMember;
import com.influxdb.client.domain.ResourceMembers;
import com.influxdb.client.domain.ResourceOwner;
import com.influxdb.client.domain.ResourceOwners;
import com.influxdb.client.domain.Telegraf;
import com.influxdb.client.domain.TelegrafRequest;
import com.influxdb.client.domain.Telegrafs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface TelegrafsService {
  /**
   * delete a telegraf config
   * 
   * @param telegrafID ID of telegraf config (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("api/v2/telegrafs/{telegrafID}")
  Call<Void> deleteTelegrafsID(
    @retrofit2.http.Path("telegrafID") String telegrafID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * delete a label from a telegraf config
   * 
   * @param telegrafID ID of the telegraf config (required)
   * @param labelID the label ID (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("api/v2/telegrafs/{telegrafID}/labels/{labelID}")
  Call<Void> deleteTelegrafsIDLabelsID(
    @retrofit2.http.Path("telegrafID") String telegrafID, @retrofit2.http.Path("labelID") String labelID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * removes a member from a telegraf config
   * 
   * @param userID ID of member to remove (required)
   * @param telegrafID ID of the telegraf (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("api/v2/telegrafs/{telegrafID}/members/{userID}")
  Call<Void> deleteTelegrafsIDMembersID(
    @retrofit2.http.Path("userID") String userID, @retrofit2.http.Path("telegrafID") String telegrafID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * removes an owner from a telegraf config
   * 
   * @param userID ID of owner to remove (required)
   * @param telegrafID ID of the telegraf config (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("api/v2/telegrafs/{telegrafID}/owners/{userID}")
  Call<Void> deleteTelegrafsIDOwnersID(
    @retrofit2.http.Path("userID") String userID, @retrofit2.http.Path("telegrafID") String telegrafID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * 
   * 
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param orgID specifies the organization of the resource (optional)
   * @return Call&lt;Telegrafs&gt;
   */
  @GET("api/v2/telegrafs")
  Call<Telegrafs> getTelegrafs(
    @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Query("orgID") String orgID
  );

  /**
   * Retrieve a telegraf config
   * 
   * @param telegrafID ID of telegraf config (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param accept  (optional, default to application/toml)
   * @return Call&lt;String&gt;
   */
  @GET("api/v2/telegrafs/{telegrafID}")
  Call<String> getTelegrafsID(
    @retrofit2.http.Path("telegrafID") String telegrafID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Header("Accept") String accept
  );

  /**
   * Retrieve a telegraf config
   * 
   * @param telegrafID ID of telegraf config (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param accept  (optional, default to application/toml)
   * @return Call&lt;ResponseBody&gt;
   */
  @GET("api/v2/telegrafs/{telegrafID}")
  Call<ResponseBody> getTelegrafsIDResponseBody(
    @retrofit2.http.Path("telegrafID") String telegrafID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Header("Accept") String accept
  );

  /**
   * Retrieve a telegraf config
   * 
   * @param telegrafID ID of telegraf config (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param accept  (optional, default to application/toml)
   * @return Call&lt;String&gt;
   */
  @GET("api/v2/telegrafs/{telegrafID}")
  Call<String> getTelegrafsIDString(
    @retrofit2.http.Path("telegrafID") String telegrafID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Header("Accept") String accept
  );

  /**
   * Retrieve a telegraf config
   * 
   * @param telegrafID ID of telegraf config (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param accept  (optional, default to application/toml)
   * @return Call&lt;Telegraf&gt;
   */
  @GET("api/v2/telegrafs/{telegrafID}")
  Call<Telegraf> getTelegrafsIDTelegraf(
    @retrofit2.http.Path("telegrafID") String telegrafID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Header("Accept") String accept
  );

  /**
   * list all labels for a telegraf config
   * 
   * @param telegrafID ID of the telegraf config (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;LabelsResponse&gt;
   */
  @GET("api/v2/telegrafs/{telegrafID}/labels")
  Call<LabelsResponse> getTelegrafsIDLabels(
    @retrofit2.http.Path("telegrafID") String telegrafID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * List all users with member privileges for a telegraf config
   * 
   * @param telegrafID ID of the telegraf config (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;ResourceMembers&gt;
   */
  @GET("api/v2/telegrafs/{telegrafID}/members")
  Call<ResourceMembers> getTelegrafsIDMembers(
    @retrofit2.http.Path("telegrafID") String telegrafID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * List all owners of a telegraf config
   * 
   * @param telegrafID ID of the telegraf config (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;ResourceOwners&gt;
   */
  @GET("api/v2/telegrafs/{telegrafID}/owners")
  Call<ResourceOwners> getTelegrafsIDOwners(
    @retrofit2.http.Path("telegrafID") String telegrafID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Create a telegraf config
   * 
   * @param telegrafRequest telegraf config to create (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Telegraf&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/telegrafs")
  Call<Telegraf> postTelegrafs(
    @retrofit2.http.Body TelegrafRequest telegrafRequest, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * add a label to a telegraf config
   * 
   * @param telegrafID ID of the telegraf config (required)
   * @param labelMapping label to add (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;LabelResponse&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/telegrafs/{telegrafID}/labels")
  Call<LabelResponse> postTelegrafsIDLabels(
    @retrofit2.http.Path("telegrafID") String telegrafID, @retrofit2.http.Body LabelMapping labelMapping, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Add telegraf config member
   * 
   * @param telegrafID ID of the telegraf config (required)
   * @param addResourceMemberRequestBody user to add as member (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;ResourceMember&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/telegrafs/{telegrafID}/members")
  Call<ResourceMember> postTelegrafsIDMembers(
    @retrofit2.http.Path("telegrafID") String telegrafID, @retrofit2.http.Body AddResourceMemberRequestBody addResourceMemberRequestBody, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Add telegraf config owner
   * 
   * @param telegrafID ID of the telegraf config (required)
   * @param addResourceMemberRequestBody user to add as owner (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;ResourceOwner&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/telegrafs/{telegrafID}/owners")
  Call<ResourceOwner> postTelegrafsIDOwners(
    @retrofit2.http.Path("telegrafID") String telegrafID, @retrofit2.http.Body AddResourceMemberRequestBody addResourceMemberRequestBody, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Update a telegraf config
   * 
   * @param telegrafID ID of telegraf config (required)
   * @param telegrafRequest telegraf config update to apply (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Telegraf&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @PUT("api/v2/telegrafs/{telegrafID}")
  Call<Telegraf> putTelegrafsID(
    @retrofit2.http.Path("telegrafID") String telegrafID, @retrofit2.http.Body TelegrafRequest telegrafRequest, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

}
