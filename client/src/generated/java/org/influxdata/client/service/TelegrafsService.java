package org.influxdata.client.service;

import retrofit2.Call;
import retrofit2.http.*;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.MultipartBody;

import org.influxdata.client.domain.AddResourceMemberRequestBody;
import org.influxdata.client.domain.Error;
import org.influxdata.client.domain.LabelMapping;
import org.influxdata.client.domain.LabelResponse;
import org.influxdata.client.domain.LabelsResponse;
import org.influxdata.client.domain.ResourceMember;
import org.influxdata.client.domain.ResourceMembers;
import org.influxdata.client.domain.ResourceOwner;
import org.influxdata.client.domain.ResourceOwners;
import org.influxdata.client.domain.Telegraf;
import org.influxdata.client.domain.TelegrafRequest;
import org.influxdata.client.domain.Telegrafs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface TelegrafsService {
  /**
   * 
   * 
   * @param orgID specifies the organization of the resource (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Telegrafs&gt;
   */
  @GET("telegrafs")
  Call<Telegrafs> telegrafsGet(
    @retrofit2.http.Query("orgID") String orgID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
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
  @POST("telegrafs")
  Call<Telegraf> telegrafsPost(
    @retrofit2.http.Body TelegrafRequest telegrafRequest, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * delete a telegraf config
   * 
   * @param telegrafID ID of telegraf config (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("telegrafs/{telegrafID}")
  Call<Void> telegrafsTelegrafIDDelete(
    @retrofit2.http.Path("telegrafID") String telegrafID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Retrieve a telegraf config
   * 
   * @param telegrafID ID of telegraf config (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param accept specifies the return content format. (optional, default to application/json)
   * @return Call&lt;Telegraf&gt;
   */
  @GET("telegrafs/{telegrafID}")
  Call<Telegraf> telegrafsTelegrafIDGet(
    @retrofit2.http.Path("telegrafID") String telegrafID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Header("Accept") String accept
  );

  /**
   * Retrieve a telegraf config
   * 
   * @param telegrafID ID of telegraf config (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param accept specifies the return content format. (optional, default to application/json)
   * @return Call&lt;ResponseBody&gt;
   */
  @GET("telegrafs/{telegrafID}")
  Call<ResponseBody> telegrafsTelegrafIDGetResponseBody(
    @retrofit2.http.Path("telegrafID") String telegrafID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Header("Accept") String accept
  );

  /**
   * list all labels for a telegraf config
   * 
   * @param telegrafID ID of the telegraf config (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;LabelsResponse&gt;
   */
  @GET("telegrafs/{telegrafID}/labels")
  Call<LabelsResponse> telegrafsTelegrafIDLabelsGet(
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
  @DELETE("telegrafs/{telegrafID}/labels/{labelID}")
  Call<Void> telegrafsTelegrafIDLabelsLabelIDDelete(
    @retrofit2.http.Path("telegrafID") String telegrafID, @retrofit2.http.Path("labelID") String labelID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
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
  @POST("telegrafs/{telegrafID}/labels")
  Call<LabelResponse> telegrafsTelegrafIDLabelsPost(
    @retrofit2.http.Path("telegrafID") String telegrafID, @retrofit2.http.Body LabelMapping labelMapping, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * List all users with member privileges for a telegraf config
   * 
   * @param telegrafID ID of the telegraf config (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;ResourceMembers&gt;
   */
  @GET("telegrafs/{telegrafID}/members")
  Call<ResourceMembers> telegrafsTelegrafIDMembersGet(
    @retrofit2.http.Path("telegrafID") String telegrafID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
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
  @POST("telegrafs/{telegrafID}/members")
  Call<ResourceMember> telegrafsTelegrafIDMembersPost(
    @retrofit2.http.Path("telegrafID") String telegrafID, @retrofit2.http.Body AddResourceMemberRequestBody addResourceMemberRequestBody, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * removes a member from a telegraf config
   * 
   * @param userID ID of member to remove (required)
   * @param telegrafID ID of the telegraf (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("telegrafs/{telegrafID}/members/{userID}")
  Call<Void> telegrafsTelegrafIDMembersUserIDDelete(
    @retrofit2.http.Path("userID") String userID, @retrofit2.http.Path("telegrafID") String telegrafID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * List all owners of a telegraf config
   * 
   * @param telegrafID ID of the telegraf config (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;ResourceOwners&gt;
   */
  @GET("telegrafs/{telegrafID}/owners")
  Call<ResourceOwners> telegrafsTelegrafIDOwnersGet(
    @retrofit2.http.Path("telegrafID") String telegrafID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
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
  @POST("telegrafs/{telegrafID}/owners")
  Call<ResourceOwner> telegrafsTelegrafIDOwnersPost(
    @retrofit2.http.Path("telegrafID") String telegrafID, @retrofit2.http.Body AddResourceMemberRequestBody addResourceMemberRequestBody, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * removes an owner from a telegraf config
   * 
   * @param userID ID of owner to remove (required)
   * @param telegrafID ID of the telegraf config (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("telegrafs/{telegrafID}/owners/{userID}")
  Call<Void> telegrafsTelegrafIDOwnersUserIDDelete(
    @retrofit2.http.Path("userID") String userID, @retrofit2.http.Path("telegrafID") String telegrafID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
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
  @PUT("telegrafs/{telegrafID}")
  Call<Telegraf> telegrafsTelegrafIDPut(
    @retrofit2.http.Path("telegrafID") String telegrafID, @retrofit2.http.Body TelegrafRequest telegrafRequest, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

}
