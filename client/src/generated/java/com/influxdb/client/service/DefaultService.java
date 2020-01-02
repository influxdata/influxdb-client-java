package com.influxdb.client.service;

import retrofit2.Call;
import retrofit2.http.*;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.MultipartBody;

import com.influxdb.client.domain.DeletePredicateRequest;
import com.influxdb.client.domain.Error;
import com.influxdb.client.domain.Routes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface DefaultService {
  /**
   * Delete time series data from InfluxDB
   * 
   * @param deletePredicateRequest Predicate delete request (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param org Specifies the organization to delete data from. (optional)
   * @param bucket Specifies the bucket to delete data from. (optional)
   * @param orgID Specifies the organization ID of the resource. (optional)
   * @param bucketID Specifies the bucket ID to delete data from. (optional)
   * @return Call&lt;Void&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/delete")
  Call<Void> deletePost(
    @retrofit2.http.Body DeletePredicateRequest deletePredicateRequest, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Query("org") String org, @retrofit2.http.Query("bucket") String bucket, @retrofit2.http.Query("orgID") String orgID, @retrofit2.http.Query("bucketID") String bucketID
  );

  /**
   * Map of all top level routes available
   * 
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Routes&gt;
   */
  @GET("api/v2/")
  Call<Routes> getRoutes(
    @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Exchange basic auth credentials for session
   * 
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param authorization An auth credential for the Basic scheme (optional)
   * @return Call&lt;Void&gt;
   */
  @POST("api/v2/signin")
  Call<Void> postSignin(
    @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Header("Authorization") String authorization
  );

  /**
   * Expire the current session
   * 
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @POST("api/v2/signout")
  Call<Void> postSignout(
    @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

}
