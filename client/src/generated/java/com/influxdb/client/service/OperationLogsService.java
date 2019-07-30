package com.influxdb.client.service;

import retrofit2.Call;
import retrofit2.http.*;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.MultipartBody;

import com.influxdb.client.domain.Error;
import com.influxdb.client.domain.OperationLogs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface OperationLogsService {
  /**
   * Retrieve operation logs for a bucket
   * 
   * @param bucketID ID of the bucket (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param offset  (optional)
   * @param limit  (optional, default to 20)
   * @return Call&lt;OperationLogs&gt;
   */
  @GET("api/v2/buckets/{bucketID}/logs")
  Call<OperationLogs> getBucketsIDLogs(
    @retrofit2.http.Path("bucketID") String bucketID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Query("offset") Integer offset, @retrofit2.http.Query("limit") Integer limit
  );

  /**
   * Retrieve operation logs for a dashboard
   * 
   * @param dashboardID ID of the dashboard (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param offset  (optional)
   * @param limit  (optional, default to 20)
   * @return Call&lt;OperationLogs&gt;
   */
  @GET("api/v2/dashboards/{dashboardID}/logs")
  Call<OperationLogs> getDashboardsIDLogs(
    @retrofit2.http.Path("dashboardID") String dashboardID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Query("offset") Integer offset, @retrofit2.http.Query("limit") Integer limit
  );

  /**
   * Retrieve operation logs for an organization
   * 
   * @param orgID ID of the organization (required)
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
   * Retrieve operation logs for a user
   * 
   * @param userID ID of the user (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param offset  (optional)
   * @param limit  (optional, default to 20)
   * @return Call&lt;OperationLogs&gt;
   */
  @GET("api/v2/users/{userID}/logs")
  Call<OperationLogs> getUsersIDLogs(
    @retrofit2.http.Path("userID") String userID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Query("offset") Integer offset, @retrofit2.http.Query("limit") Integer limit
  );

}
