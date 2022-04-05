package com.influxdb.client.service;

import retrofit2.Call;
import retrofit2.http.*;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.MultipartBody;

import com.influxdb.client.domain.IsOnboarding;
import com.influxdb.client.domain.OnboardingRequest;
import com.influxdb.client.domain.OnboardingResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface SetupService {
  /**
   * Check if database has default user, org, bucket
   * Returns &#x60;true&#x60; if no default user, organization, or bucket has been created.
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;IsOnboarding&gt;
   */
  @GET("api/v2/setup")
  Call<IsOnboarding> getSetup(
    @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Set up initial user, org and bucket
   * Post an onboarding request to set up initial user, org and bucket.
   * @param onboardingRequest Source to create (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;OnboardingResponse&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/setup")
  Call<OnboardingResponse> postSetup(
    @retrofit2.http.Body OnboardingRequest onboardingRequest, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

}
