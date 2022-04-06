package com.influxdb.client.service;

import retrofit2.Call;
import retrofit2.http.*;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.MultipartBody;

import com.influxdb.client.domain.RemoteConnection;
import com.influxdb.client.domain.RemoteConnectionCreationRequest;
import com.influxdb.client.domain.RemoteConnectionUpdateRequest;
import com.influxdb.client.domain.RemoteConnections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface RemoteConnectionsService {
  /**
   * Delete a remote connection
   * 
   * @param remoteID  (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("api/v2/remotes/{remoteID}")
  Call<Void> deleteRemoteConnectionByID(
    @retrofit2.http.Path("remoteID") String remoteID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Retrieve a remote connection
   * 
   * @param remoteID  (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;RemoteConnection&gt;
   */
  @GET("api/v2/remotes/{remoteID}")
  Call<RemoteConnection> getRemoteConnectionByID(
    @retrofit2.http.Path("remoteID") String remoteID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * List all remote connections
   * 
   * @param orgID The organization ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param name  (optional)
   * @param remoteURL  (optional)
   * @return Call&lt;RemoteConnections&gt;
   */
  @GET("api/v2/remotes")
  Call<RemoteConnections> getRemoteConnections(
    @retrofit2.http.Query("orgID") String orgID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Query("name") String name, @retrofit2.http.Query("remoteURL") String remoteURL
  );

  /**
   * Update a remote connection
   * 
   * @param remoteID  (required)
   * @param remoteConnectionUpdateRequest  (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;RemoteConnection&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @PATCH("api/v2/remotes/{remoteID}")
  Call<RemoteConnection> patchRemoteConnectionByID(
    @retrofit2.http.Path("remoteID") String remoteID, @retrofit2.http.Body RemoteConnectionUpdateRequest remoteConnectionUpdateRequest, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Register a new remote connection
   * 
   * @param remoteConnectionCreationRequest  (required)
   * @return Call&lt;RemoteConnection&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/remotes")
  Call<RemoteConnection> postRemoteConnection(
    @retrofit2.http.Body RemoteConnectionCreationRequest remoteConnectionCreationRequest
  );

}
