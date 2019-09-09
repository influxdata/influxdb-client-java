package com.influxdb.client.service;

import retrofit2.Call;
import retrofit2.http.*;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.MultipartBody;

import com.influxdb.client.domain.Error;
import com.influxdb.client.domain.LabelMapping;
import com.influxdb.client.domain.LabelResponse;
import com.influxdb.client.domain.LabelsResponse;
import com.influxdb.client.domain.NotificationEndpoint;
import com.influxdb.client.domain.NotificationEndpointUpdate;
import com.influxdb.client.domain.NotificationEndpoints;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface NotificationEndpointsService {
  /**
   * Add a notification endpoint
   * 
   * @param notificationEndpoint Notification endpoint to create (required)
   * @return Call&lt;NotificationEndpoint&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/notificationEndpoints")
  Call<NotificationEndpoint> createNotificationEndpoint(
    @retrofit2.http.Body NotificationEndpoint notificationEndpoint
  );

  /**
   * Delete a notification endpoint
   * 
   * @param endpointID The notification endpoint ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("api/v2/notificationEndpoints/{endpointID}")
  Call<Void> deleteNotificationEndpointsID(
    @retrofit2.http.Path("endpointID") String endpointID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Delete a label from a notification endpoint
   * 
   * @param endpointID The notification endpoint ID. (required)
   * @param labelID The ID of the label to delete. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("api/v2/notificationEndpoints/{endpointID}/labels/{labelID}")
  Call<Void> deleteNotificationEndpointsIDLabelsID(
    @retrofit2.http.Path("endpointID") String endpointID, @retrofit2.http.Path("labelID") String labelID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Get all notification endpoints
   * 
   * @param orgID Only show notification endpoints that belong to specific organization ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param offset  (optional)
   * @param limit  (optional, default to 20)
   * @return Call&lt;NotificationEndpoints&gt;
   */
  @GET("api/v2/notificationEndpoints")
  Call<NotificationEndpoints> getNotificationEndpoints(
    @retrofit2.http.Query("orgID") String orgID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Query("offset") Integer offset, @retrofit2.http.Query("limit") Integer limit
  );

  /**
   * Get a notification endpoint
   * 
   * @param endpointID The notification endpoint ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;NotificationEndpoint&gt;
   */
  @GET("api/v2/notificationEndpoints/{endpointID}")
  Call<NotificationEndpoint> getNotificationEndpointsID(
    @retrofit2.http.Path("endpointID") String endpointID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * List all labels for a notification endpoint
   * 
   * @param endpointID The notification endpoint ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;LabelsResponse&gt;
   */
  @GET("api/v2/notificationEndpoints/{endpointID}/labels")
  Call<LabelsResponse> getNotificationEndpointsIDLabels(
    @retrofit2.http.Path("endpointID") String endpointID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Update a notification endpoint
   * 
   * @param endpointID The notification endpoint ID. (required)
   * @param notificationEndpointUpdate Check update to apply (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;NotificationEndpoint&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @PATCH("api/v2/notificationEndpoints/{endpointID}")
  Call<NotificationEndpoint> patchNotificationEndpointsID(
    @retrofit2.http.Path("endpointID") String endpointID, @retrofit2.http.Body NotificationEndpointUpdate notificationEndpointUpdate, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Add a label to a notification endpoint
   * 
   * @param endpointID The notification endpoint ID. (required)
   * @param labelMapping Label to add (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;LabelResponse&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/notificationEndpoints/{endpointID}/labels")
  Call<LabelResponse> postNotificationEndpointIDLabels(
    @retrofit2.http.Path("endpointID") String endpointID, @retrofit2.http.Body LabelMapping labelMapping, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Update a notification endpoint
   * 
   * @param endpointID The notification endpoint ID. (required)
   * @param notificationEndpoint A new notification endpoint to replace the existing endpoint with (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;NotificationEndpoint&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @PUT("api/v2/notificationEndpoints/{endpointID}")
  Call<NotificationEndpoint> putNotificationEndpointsID(
    @retrofit2.http.Path("endpointID") String endpointID, @retrofit2.http.Body NotificationEndpoint notificationEndpoint, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

}
