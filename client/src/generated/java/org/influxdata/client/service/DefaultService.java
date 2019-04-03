package org.influxdata.client.service;

import retrofit2.Call;
import retrofit2.http.*;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.MultipartBody;

import org.influxdata.client.domain.Error;
import org.influxdata.client.domain.Routes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface DefaultService {
  /**
   * Map of all top level routes available
   * 
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Routes&gt;
   */
  @GET("api/v2/")
  Call<Routes> rootGet(
    @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Exchange basic auth credentials for session
   * 
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param authorization An auth credential for the Basic scheme (optional)
   * @return Call&lt;Void&gt;
   */
  @POST("api/v2/signin")
  Call<Void> signinPost(
    @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Header("Authorization") String authorization
  );

  /**
   * Expire the current session
   * 
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @POST("api/v2/signout")
  Call<Void> signoutPost(
    @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

}
