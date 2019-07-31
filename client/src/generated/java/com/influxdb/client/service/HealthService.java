package com.influxdb.client.service;

import com.influxdb.client.domain.HealthCheck;

import retrofit2.Call;
import retrofit2.http.GET;

public interface HealthService {
  /**
   * Get the health of an instance anytime during execution. Allow us to check if the instance is still healthy.
   * 
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;HealthCheck&gt;
   */
  @GET("health")
  Call<HealthCheck> getHealth(
    @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

}
