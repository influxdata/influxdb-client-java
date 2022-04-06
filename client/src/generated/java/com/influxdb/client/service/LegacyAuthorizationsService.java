package com.influxdb.client.service;

import retrofit2.Call;
import retrofit2.http.*;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.MultipartBody;

import com.influxdb.client.domain.Authorization;
import com.influxdb.client.domain.AuthorizationUpdateRequest;
import com.influxdb.client.domain.Authorizations;
import com.influxdb.client.domain.LegacyAuthorizationPostRequest;
import com.influxdb.client.domain.PasswordResetBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface LegacyAuthorizationsService {
  /**
   * Delete a legacy authorization
   * 
   * @param authID The ID of the legacy authorization to delete. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("private/legacy/authorizations/{authID}")
  Call<Void> deleteLegacyAuthorizationsID(
    @retrofit2.http.Path("authID") String authID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * List all legacy authorizations
   * 
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param userID Only show legacy authorizations that belong to a user ID. (optional)
   * @param user Only show legacy authorizations that belong to a user name. (optional)
   * @param orgID Only show legacy authorizations that belong to an organization ID. (optional)
   * @param org Only show legacy authorizations that belong to a organization name. (optional)
   * @param token Only show legacy authorizations with a specified token (auth name). (optional)
   * @param authID Only show legacy authorizations with a specified auth ID. (optional)
   * @return Call&lt;Authorizations&gt;
   */
  @GET("private/legacy/authorizations")
  Call<Authorizations> getLegacyAuthorizations(
    @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Query("userID") String userID, @retrofit2.http.Query("user") String user, @retrofit2.http.Query("orgID") String orgID, @retrofit2.http.Query("org") String org, @retrofit2.http.Query("token") String token, @retrofit2.http.Query("authID") String authID
  );

  /**
   * Retrieve a legacy authorization
   * 
   * @param authID The ID of the legacy authorization to get. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Authorization&gt;
   */
  @GET("private/legacy/authorizations/{authID}")
  Call<Authorization> getLegacyAuthorizationsID(
    @retrofit2.http.Path("authID") String authID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Update a legacy authorization to be active or inactive
   * 
   * @param authID The ID of the legacy authorization to update. (required)
   * @param authorizationUpdateRequest Legacy authorization to update (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Authorization&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @PATCH("private/legacy/authorizations/{authID}")
  Call<Authorization> patchLegacyAuthorizationsID(
    @retrofit2.http.Path("authID") String authID, @retrofit2.http.Body AuthorizationUpdateRequest authorizationUpdateRequest, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Create a legacy authorization
   * 
   * @param legacyAuthorizationPostRequest Legacy authorization to create (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Authorization&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("private/legacy/authorizations")
  Call<Authorization> postLegacyAuthorizations(
    @retrofit2.http.Body LegacyAuthorizationPostRequest legacyAuthorizationPostRequest, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Set a legacy authorization password
   * 
   * @param authID The ID of the legacy authorization to update. (required)
   * @param passwordResetBody New password (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("private/legacy/authorizations/{authID}/password")
  Call<Void> postLegacyAuthorizationsIDPassword(
    @retrofit2.http.Path("authID") String authID, @retrofit2.http.Body PasswordResetBody passwordResetBody, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

}
