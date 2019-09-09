package com.influxdb.client.service;

import retrofit2.Call;
import retrofit2.http.*;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.MultipartBody;

import com.influxdb.client.domain.Error;
import com.influxdb.client.domain.SecretKeys;
import com.influxdb.client.domain.SecretKeysResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface SecretsService {
  /**
   * List all secret keys for an organization
   * 
   * @param orgID The organization ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;SecretKeysResponse&gt;
   */
  @GET("api/v2/orgs/{orgID}/secrets")
  Call<SecretKeysResponse> getOrgsIDSecrets(
    @retrofit2.http.Path("orgID") String orgID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Update secrets in an organization
   * 
   * @param orgID The organization ID. (required)
   * @param requestBody Secret key value pairs to update/add (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @PATCH("api/v2/orgs/{orgID}/secrets")
  Call<Void> patchOrgsIDSecrets(
    @retrofit2.http.Path("orgID") String orgID, @retrofit2.http.Body Map<String, String> requestBody, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Delete secrets from an organization
   * 
   * @param orgID The organization ID. (required)
   * @param secretKeys Secret key to delete (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/orgs/{orgID}/secrets/delete")
  Call<Void> postOrgsIDSecrets(
    @retrofit2.http.Path("orgID") String orgID, @retrofit2.http.Body SecretKeys secretKeys, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

}
