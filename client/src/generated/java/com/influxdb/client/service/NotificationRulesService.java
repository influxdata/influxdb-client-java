package com.influxdb.client.service;

import retrofit2.Call;
import retrofit2.http.*;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.MultipartBody;

import com.influxdb.client.domain.Error;
import com.influxdb.client.domain.NotificationRule;
import com.influxdb.client.domain.NotificationRuleUpdate;
import com.influxdb.client.domain.NotificationRules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface NotificationRulesService {
  /**
   * Add new notification rule
   * 
   * @param notificationRule notificationRule to create (required)
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
   * @param ruleID ID of notification rule (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("api/v2/notificationRules/{ruleID}")
  Call<Void> deleteNotificationRulesID(
    @retrofit2.http.Path("ruleID") String ruleID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Get all notification rules
   * 
   * @param orgID only show notification rules belonging to specified organization (required)
   * @param offset  (optional)
   * @param limit  (optional, default to 20)
   * @param checkID only show notifications that belong to the specified check (optional)
   * @return Call&lt;NotificationRules&gt;
   */
  @GET("api/v2/notificationRules")
  Call<NotificationRules> getNotificationRules(
    @retrofit2.http.Query("orgID") String orgID, @retrofit2.http.Query("offset") Integer offset, @retrofit2.http.Query("limit") Integer limit, @retrofit2.http.Query("checkID") String checkID
  );

  /**
   * Get a notification rule
   * 
   * @param ruleID ID of notification rule (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;NotificationRule&gt;
   */
  @GET("api/v2/notificationRules/{ruleID}")
  Call<NotificationRule> getNotificationRulesID(
    @retrofit2.http.Path("ruleID") String ruleID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Update a notification rule
   * 
   * @param ruleID ID of notification rule (required)
   * @param notificationRuleUpdate notification rule update to apply (required)
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
   * Update a notification rule
   * 
   * @param ruleID ID of notification rule (required)
   * @param notificationRule notification rule update to apply (required)
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
