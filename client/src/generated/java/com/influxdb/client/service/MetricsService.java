package com.influxdb.client.service;

import retrofit2.Call;
import retrofit2.http.*;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.MultipartBody;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface MetricsService {
  /**
   * Get metrics of an instance
   * 
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;String&gt;
   */
  @GET("metrics")
  Call<String> getMetrics(
    @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

}
