package com.influxdb.client.service;

import retrofit2.Call;
import retrofit2.http.*;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.MultipartBody;

import com.influxdb.client.domain.CloudUsers;
import com.influxdb.client.domain.Error;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface CloudUsersService {
  /**
   * Deletes a cloud user
   * 
   * @param userID The ID of the user to remove. (required)
   * @param orgID The organization ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("api/v2/orgs/{orgID}/users/{userID}")
  Call<Void> deleteOrgsIDCloudUserID(
    @retrofit2.http.Path("userID") String userID, @retrofit2.http.Path("orgID") String orgID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * 
   * 
   * @param orgID Specifies the organization ID of the CloudUser. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;CloudUsers&gt;
   */
  @GET("api/v2/orgs/{orgID}/users")
  Call<CloudUsers> getCloudUsers(
    @retrofit2.http.Path("orgID") String orgID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

}
