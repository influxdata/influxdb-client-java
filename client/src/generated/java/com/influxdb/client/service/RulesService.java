package com.influxdb.client.service;

import retrofit2.Call;
import retrofit2.http.*;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.MultipartBody;

import com.influxdb.client.domain.Error;
import com.influxdb.client.domain.FluxResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface RulesService {
  /**
   * Get a notification rule query
   * 
   * @param ruleID The notification rule ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;FluxResponse&gt;
   */
  @GET("api/v2/notificationRules/{ruleID}/query")
  Call<FluxResponse> getNotificationRulesIDQuery(
    @retrofit2.http.Path("ruleID") String ruleID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

}
