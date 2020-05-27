package com.influxdb.client.service;

import retrofit2.Call;
import retrofit2.http.*;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.MultipartBody;

import com.influxdb.client.domain.Error;
import com.influxdb.client.domain.Invite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface InvitesService {
  /**
   * Remove an invite to an organization
   * 
   * @param inviteID The ID of the invite to remove. (required)
   * @param orgID The organization ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("api/v2/orgs/{orgID}/invites/{inviteID}")
  Call<Void> deleteOrgsIDInviteID(
    @retrofit2.http.Path("inviteID") String inviteID, @retrofit2.http.Path("orgID") String orgID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Resends an invite
   * 
   * @param inviteID The ID of the invite to resend. (required)
   * @param orgID The organization ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Invite&gt;
   */
  @POST("api/v2/orgs/{orgID}/invites/{inviteID}/resend")
  Call<Invite> deleteOrgsIDInviteID_0(
    @retrofit2.http.Path("inviteID") String inviteID, @retrofit2.http.Path("orgID") String orgID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Creates an invite to an organization
   * 
   * @param orgID The organization ID. (required)
   * @param invite Invite to be sent (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Invite&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/orgs/{orgID}/invites")
  Call<Invite> postOrgsIDInvites(
    @retrofit2.http.Path("orgID") String orgID, @retrofit2.http.Body Invite invite, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

}
