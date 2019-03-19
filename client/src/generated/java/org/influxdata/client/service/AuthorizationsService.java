package org.influxdata.client.service;

import org.influxdata.client.domain.Authorization;
import org.influxdata.client.domain.Authorizations;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;

public interface AuthorizationsService {
  /**
   * Delete a authorization
   * 
   * @param authID ID of authorization to delete (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("authorizations/{authID}")
  Call<Void> authorizationsAuthIDDelete(
    @retrofit2.http.Path("authID") String authID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Retrieve an authorization
   * 
   * @param authID ID of authorization to get (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Authorization&gt;
   */
  @GET("authorizations/{authID}")
  Call<Authorization> authorizationsAuthIDGet(
    @retrofit2.http.Path("authID") String authID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * update authorization to be active or inactive. requests using an inactive authorization will be rejected.
   * 
   * @param authID ID of authorization to update (required)
   * @param authorization authorization to update to apply (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Authorization&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @PATCH("authorizations/{authID}")
  Call<Authorization> authorizationsAuthIDPatch(
    @retrofit2.http.Path("authID") String authID, @retrofit2.http.Body Authorization authorization, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * List all authorizations
   * 
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param userID filter authorizations belonging to a user id (optional)
   * @param user filter authorizations belonging to a user name (optional)
   * @return Call&lt;Authorizations&gt;
   */
  @GET("authorizations")
  Call<Authorizations> authorizationsGet(
    @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Query("userID") String userID, @retrofit2.http.Query("user") String user
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
  @POST("authorizations")
  Call<Authorization> authorizationsPost(
    @retrofit2.http.Body Authorization authorization, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

}
