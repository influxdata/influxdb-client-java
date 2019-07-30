package com.influxdb.client.service;

import retrofit2.Call;
import retrofit2.http.*;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.MultipartBody;

import com.influxdb.client.domain.Error;
import com.influxdb.client.domain.Ready;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface ReadyService {
  /**
   * Get the readiness of a instance at startup. Allow us to confirm the instance is prepared to accept requests.
   * 
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Ready&gt;
   */
  @GET("ready")
  Call<Ready> getReady(
    @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

}
