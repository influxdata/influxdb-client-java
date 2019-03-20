package org.influxdata.client.service;

import retrofit2.Call;
import retrofit2.http.*;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.MultipartBody;

import org.influxdata.client.domain.IsOnboarding;
import org.influxdata.client.domain.OnboardingRequest;
import org.influxdata.client.domain.OnboardingResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface SetupService {
  /**
   * check if database has default user, org, bucket created, returns true if not.
   * 
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;IsOnboarding&gt;
   */
  @GET("setup")
  Call<IsOnboarding> setupGet(
    @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * post onboarding request, to setup initial user, org and bucket
   * 
   * @param onboardingRequest source to create (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;OnboardingResponse&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("setup")
  Call<OnboardingResponse> setupPost(
    @retrofit2.http.Body OnboardingRequest onboardingRequest, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

}
