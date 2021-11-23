package com.influxdb.client.service;

import retrofit2.Call;
import retrofit2.http.*;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.MultipartBody;

import com.influxdb.client.domain.Error;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface ResourcesService {
  /**
   * List all known resources
   * 
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;List&lt;String&gt;&gt;
   */
  @GET("api/v2/resources")
  Call<List<String>> getResources(
    @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

}
