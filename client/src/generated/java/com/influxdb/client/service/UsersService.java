package com.influxdb.client.service;

import retrofit2.Call;
import retrofit2.http.*;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.MultipartBody;

import com.influxdb.client.domain.Error;
import com.influxdb.client.domain.PasswordResetBody;
import com.influxdb.client.domain.PostUser;
import com.influxdb.client.domain.User;
import com.influxdb.client.domain.Users;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface UsersService {
  /**
   * Delete a user
   * 
   * @param userID The ID of the user to delete. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("api/v2/users/{userID}")
  Call<Void> deleteUsersID(
    @retrofit2.http.Path("userID") String userID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Return the feature flags for the currently authenticated user
   * 
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Map&lt;String, Object&gt;&gt;
   */
  @GET("api/v2/flags")
  Call<Map<String, Object>> getFlags(
    @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Retrieve the currently authenticated user
   * 
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;User&gt;
   */
  @GET("api/v2/me")
  Call<User> getMe(
    @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * List all users
   * 
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param offset  (optional)
   * @param limit  (optional, default to 20)
   * @param after The last resource ID from which to seek from (but not including). This is to be used instead of &#x60;offset&#x60;. (optional)
   * @param name  (optional)
   * @param id  (optional)
   * @return Call&lt;Users&gt;
   */
  @GET("api/v2/users")
  Call<Users> getUsers(
    @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Query("offset") Integer offset, @retrofit2.http.Query("limit") Integer limit, @retrofit2.http.Query("after") String after, @retrofit2.http.Query("name") String name, @retrofit2.http.Query("id") String id
  );

  /**
   * Retrieve a user
   * 
   * @param userID The user ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;User&gt;
   */
  @GET("api/v2/users/{userID}")
  Call<User> getUsersID(
    @retrofit2.http.Path("userID") String userID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Update a user
   * 
   * @param userID The ID of the user to update. (required)
   * @param postUser User update to apply (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;User&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @PATCH("api/v2/users/{userID}")
  Call<User> patchUsersID(
    @retrofit2.http.Path("userID") String userID, @retrofit2.http.Body PostUser postUser, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Create a user
   * 
   * @param postUser User to create (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;User&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/users")
  Call<User> postUsers(
    @retrofit2.http.Body PostUser postUser, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Update a password
   * 
   * @param userID The user ID. (required)
   * @param passwordResetBody New password (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param authorization An auth credential for the Basic scheme (optional)
   * @return Call&lt;Void&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/users/{userID}/password")
  Call<Void> postUsersIDPassword(
    @retrofit2.http.Path("userID") String userID, @retrofit2.http.Body PasswordResetBody passwordResetBody, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Header("Authorization") String authorization
  );

  /**
   * Update a password
   * 
   * @param passwordResetBody New password (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param authorization An auth credential for the Basic scheme (optional)
   * @return Call&lt;Void&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @PUT("api/v2/me/password")
  Call<Void> putMePassword(
    @retrofit2.http.Body PasswordResetBody passwordResetBody, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Header("Authorization") String authorization
  );

}
