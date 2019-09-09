package com.influxdb.client.service;

import retrofit2.Call;
import retrofit2.http.*;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.MultipartBody;

import com.influxdb.client.domain.Authorization;
import com.influxdb.client.domain.AuthorizationUpdateRequest;
import com.influxdb.client.domain.Authorizations;
import com.influxdb.client.domain.Error;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface AuthorizationsService {
  /**
   * Delete a authorization
   * 
   * @param authID The ID of the authorization to delete. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("api/v2/authorizations/{authID}")
  Call<Void> deleteAuthorizationsID(
    @retrofit2.http.Path("authID") String authID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * List all authorizations
   * 
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param userID Only show authorizations that belong to a user ID. (optional)
   * @param user Only show authorizations that belong to a user name. (optional)
   * @param orgID Only show authorizations that belong to an organization ID. (optional)
   * @param org Only show authorizations that belong to a organization name. (optional)
   * @return Call&lt;Authorizations&gt;
   */
  @GET("api/v2/authorizations")
  Call<Authorizations> getAuthorizations(
    @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Query("userID") String userID, @retrofit2.http.Query("user") String user, @retrofit2.http.Query("orgID") String orgID, @retrofit2.http.Query("org") String org
  );

  /**
   * Retrieve an authorization
   * 
   * @param authID The ID of the authorization to get. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Authorization&gt;
   */
  @GET("api/v2/authorizations/{authID}")
  Call<Authorization> getAuthorizationsID(
    @retrofit2.http.Path("authID") String authID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Update an authorization to be active or inactive
   * 
   * @param authID The ID of the authorization to update. (required)
   * @param authorizationUpdateRequest Authorization to update (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Authorization&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @PATCH("api/v2/authorizations/{authID}")
  Call<Authorization> patchAuthorizationsID(
    @retrofit2.http.Path("authID") String authID, @retrofit2.http.Body AuthorizationUpdateRequest authorizationUpdateRequest, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Create an authorization
   * 
   * @param authorization Authorization to create (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Authorization&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/authorizations")
  Call<Authorization> postAuthorizations(
    @retrofit2.http.Body Authorization authorization, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

}
