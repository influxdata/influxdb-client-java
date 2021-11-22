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

public interface SignoutService {
  /**
   * Expire the current UI session
   * Expires the current UI session for the user.
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @POST("api/v2/signout")
  Call<Void> postSignout(
    @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

}
