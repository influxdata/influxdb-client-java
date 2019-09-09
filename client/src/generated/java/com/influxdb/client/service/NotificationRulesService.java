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
import com.influxdb.client.domain.NotificationRule;
import com.influxdb.client.domain.NotificationRuleUpdate;
import com.influxdb.client.domain.NotificationRules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface NotificationRulesService {
  /**
   * Add a notification rule
   * 
   * @param notificationRule Notification rule to create (required)
   * @return Call&lt;NotificationRule&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/notificationRules")
  Call<NotificationRule> createNotificationRule(
    @retrofit2.http.Body NotificationRule notificationRule
  );

  /**
   * Delete a notification rule
   * 
   * @param ruleID The notification rule ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("api/v2/notificationRules/{ruleID}")
  Call<Void> deleteNotificationRulesID(
    @retrofit2.http.Path("ruleID") String ruleID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Delete label from a notification rule
   * 
   * @param ruleID The notification rule ID. (required)
   * @param labelID The ID of the label to delete. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("api/v2/notificationRules/{ruleID}/labels/{labelID}")
  Call<Void> deleteNotificationRulesIDLabelsID(
    @retrofit2.http.Path("ruleID") String ruleID, @retrofit2.http.Path("labelID") String labelID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Get all notification rules
   * 
   * @param orgID Only show notification rules that belong to a specific organization ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param offset  (optional)
   * @param limit  (optional, default to 20)
   * @param checkID Only show notifications that belong to the specific check ID. (optional)
   * @param tag Only show notification rules that match a tag pair. Uses &#x60;AND&#x60; to specify multiple tags. (optional)
   * @return Call&lt;NotificationRules&gt;
   */
  @GET("api/v2/notificationRules")
  Call<NotificationRules> getNotificationRules(
    @retrofit2.http.Query("orgID") String orgID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Query("offset") Integer offset, @retrofit2.http.Query("limit") Integer limit, @retrofit2.http.Query("checkID") String checkID, @retrofit2.http.Query("tag") String tag
  );

  /**
   * Get a notification rule
   * 
   * @param ruleID The notification rule ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;NotificationRule&gt;
   */
  @GET("api/v2/notificationRules/{ruleID}")
  Call<NotificationRule> getNotificationRulesID(
    @retrofit2.http.Path("ruleID") String ruleID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * List all labels for a notification rule
   * 
   * @param ruleID The notification rule ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;LabelsResponse&gt;
   */
  @GET("api/v2/notificationRules/{ruleID}/labels")
  Call<LabelsResponse> getNotificationRulesIDLabels(
    @retrofit2.http.Path("ruleID") String ruleID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Update a notification rule
   * 
   * @param ruleID The notification rule ID. (required)
   * @param notificationRuleUpdate Notification rule update to apply (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;NotificationRule&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @PATCH("api/v2/notificationRules/{ruleID}")
  Call<NotificationRule> patchNotificationRulesID(
    @retrofit2.http.Path("ruleID") String ruleID, @retrofit2.http.Body NotificationRuleUpdate notificationRuleUpdate, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Add a label to a notification rule
   * 
   * @param ruleID The notification rule ID. (required)
   * @param labelMapping Label to add (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;LabelResponse&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/notificationRules/{ruleID}/labels")
  Call<LabelResponse> postNotificationRuleIDLabels(
    @retrofit2.http.Path("ruleID") String ruleID, @retrofit2.http.Body LabelMapping labelMapping, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Update a notification rule
   * 
   * @param ruleID The notification rule ID. (required)
   * @param notificationRule Notification rule update to apply (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;NotificationRule&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @PUT("api/v2/notificationRules/{ruleID}")
  Call<NotificationRule> putNotificationRulesID(
    @retrofit2.http.Path("ruleID") String ruleID, @retrofit2.http.Body NotificationRule notificationRule, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

}
