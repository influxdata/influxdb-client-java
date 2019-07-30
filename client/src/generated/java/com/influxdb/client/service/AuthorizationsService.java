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
   * @param authID ID of authorization to delete (required)
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
   * @param userID filter authorizations belonging to a user id (optional)
   * @param user filter authorizations belonging to a user name (optional)
   * @param orgID filter authorizations belonging to a org id (optional)
   * @param org filter authorizations belonging to a org name (optional)
   * @return Call&lt;Authorizations&gt;
   */
  @GET("api/v2/authorizations")
  Call<Authorizations> getAuthorizations(
    @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Query("userID") String userID, @retrofit2.http.Query("user") String user, @retrofit2.http.Query("orgID") String orgID, @retrofit2.http.Query("org") String org
  );

  /**
   * Retrieve an authorization
   * 
   * @param authID ID of authorization to get (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Authorization&gt;
   */
  @GET("api/v2/authorizations/{authID}")
  Call<Authorization> getAuthorizationsID(
    @retrofit2.http.Path("authID") String authID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * update authorization to be active or inactive. requests using an inactive authorization will be rejected.
   * 
   * @param authID ID of authorization to update (required)
   * @param authorizationUpdateRequest authorization to update to apply (required)
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
   * @param authorization authorization to create (required)
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
