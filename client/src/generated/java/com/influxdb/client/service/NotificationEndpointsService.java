package com.influxdb.client.service;

import retrofit2.Call;
import retrofit2.http.*;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.MultipartBody;

import com.influxdb.client.domain.Error;
import com.influxdb.client.domain.NotificationEndpoint;
import com.influxdb.client.domain.NotificationEndpoints;
import com.influxdb.client.domain.NotificationRule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface NotificationEndpointsService {
  /**
   * Add new notification endpoint
   * 
   * @param notificationEndpoint notificationEndpoint to create (required)
   * @return Call&lt;NotificationRule&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/notificationEndpoints")
  Call<NotificationRule> createNotificationEndpoint(
    @retrofit2.http.Body NotificationEndpoint notificationEndpoint
  );

  /**
   * Delete a notification endpoint
   * 
   * @param endpointID ID of notification endpoint (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("api/v2/notificationEndpoints/{endpointID}")
  Call<Void> deleteNotificationEndpointsID(
    @retrofit2.http.Path("endpointID") String endpointID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Get all notification endpoints
   * 
   * @param orgID only show notification endpoints belonging to specified organization (required)
   * @param offset  (optional)
   * @param limit  (optional, default to 20)
   * @return Call&lt;NotificationEndpoints&gt;
   */
  @GET("api/v2/notificationEndpoints")
  Call<NotificationEndpoints> getNotificationEndpoints(
    @retrofit2.http.Query("orgID") String orgID, @retrofit2.http.Query("offset") Integer offset, @retrofit2.http.Query("limit") Integer limit
  );

  /**
   * Get a notification endpoint
   * 
   * @param endpointID ID of notification endpoint (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;NotificationEndpoint&gt;
   */
  @GET("api/v2/notificationEndpoints/{endpointID}")
  Call<NotificationEndpoint> getNotificationEndpointsID(
    @retrofit2.http.Path("endpointID") String endpointID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Update a notification endpoint
   * 
   * @param endpointID ID of notification endpoint (required)
   * @param notificationEndpoint check update to apply (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;NotificationEndpoint&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @PATCH("api/v2/notificationEndpoints/{endpointID}")
  Call<NotificationEndpoint> patchNotificationEndpointsID(
    @retrofit2.http.Path("endpointID") String endpointID, @retrofit2.http.Body NotificationEndpoint notificationEndpoint, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

}
