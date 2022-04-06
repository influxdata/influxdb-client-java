package com.influxdb.client.service;

import retrofit2.Call;
import retrofit2.http.*;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.MultipartBody;

import com.influxdb.client.domain.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface ConfigService {
  /**
   * Get the run-time configuration of the instance
   * 
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Config&gt;
   */
  @GET("api/v2/config")
  Call<Config> getConfig(
    @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

}
