package com.influxdb.client.service;

import retrofit2.Call;
import retrofit2.http.*;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.MultipartBody;

import com.influxdb.client.domain.Replication;
import com.influxdb.client.domain.ReplicationCreationRequest;
import com.influxdb.client.domain.ReplicationUpdateRequest;
import com.influxdb.client.domain.Replications;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface ReplicationsService {
  /**
   * Delete a replication
   * 
   * @param replicationID  (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("api/v2/replications/{replicationID}")
  Call<Void> deleteReplicationByID(
    @retrofit2.http.Path("replicationID") String replicationID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Retrieve a replication
   * 
   * @param replicationID  (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Replication&gt;
   */
  @GET("api/v2/replications/{replicationID}")
  Call<Replication> getReplicationByID(
    @retrofit2.http.Path("replicationID") String replicationID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * List all replications
   * 
   * @param orgID The organization ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param name  (optional)
   * @param remoteID  (optional)
   * @param localBucketID  (optional)
   * @return Call&lt;Replications&gt;
   */
  @GET("api/v2/replications")
  Call<Replications> getReplications(
    @retrofit2.http.Query("orgID") String orgID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Query("name") String name, @retrofit2.http.Query("remoteID") String remoteID, @retrofit2.http.Query("localBucketID") String localBucketID
  );

  /**
   * Update a replication
   * 
   * @param replicationID  (required)
   * @param replicationUpdateRequest  (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param validate If true, validate the updated information, but don&#39;t save it. (optional, default to false)
   * @return Call&lt;Replication&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @PATCH("api/v2/replications/{replicationID}")
  Call<Replication> patchReplicationByID(
    @retrofit2.http.Path("replicationID") String replicationID, @retrofit2.http.Body ReplicationUpdateRequest replicationUpdateRequest, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Query("validate") Boolean validate
  );

  /**
   * Register a new replication
   * 
   * @param replicationCreationRequest  (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param validate If true, validate the replication, but don&#39;t save it. (optional, default to false)
   * @return Call&lt;Replication&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/replications")
  Call<Replication> postReplication(
    @retrofit2.http.Body ReplicationCreationRequest replicationCreationRequest, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Query("validate") Boolean validate
  );

  /**
   * Validate a replication
   * 
   * @param replicationID  (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @POST("api/v2/replications/{replicationID}/validate")
  Call<Void> postValidateReplicationByID(
    @retrofit2.http.Path("replicationID") String replicationID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

}
