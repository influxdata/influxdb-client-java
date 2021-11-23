package com.influxdb.client.service;

import retrofit2.Call;
import retrofit2.http.*;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.MultipartBody;

import com.influxdb.client.domain.Error;
import com.influxdb.client.domain.File;
import com.influxdb.client.domain.MetadataBackup;
import java.time.OffsetDateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface BackupService {
  /**
   * Download snapshot of metadata stored in the server&#39;s embedded KV store. Should not be used in versions greater than 2.1.x, as it doesn&#39;t include metadata stored in embedded SQL.
   * 
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;ResponseBody&gt;
   * @deprecated
   */
  @Deprecated
  @GET("api/v2/backup/kv")
  Call<ResponseBody> getBackupKV(
    @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Download snapshot of all metadata in the server
   * 
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param acceptEncoding Indicates the content encoding (usually a compression algorithm) that the client can understand. (optional, default to identity)
   * @return Call&lt;MetadataBackup&gt;
   */
  @GET("api/v2/backup/metadata")
  Call<MetadataBackup> getBackupMetadata(
    @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Header("Accept-Encoding") String acceptEncoding
  );

  /**
   * Download snapshot of all TSM data in a shard
   * 
   * @param shardID The shard ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param acceptEncoding Indicates the content encoding (usually a compression algorithm) that the client can understand. (optional, default to identity)
   * @param since Earliest time to include in the snapshot. RFC3339 format. (optional)
   * @return Call&lt;ResponseBody&gt;
   */
  @GET("api/v2/backup/shards/{shardID}")
  Call<ResponseBody> getBackupShardId(
    @retrofit2.http.Path("shardID") Long shardID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Header("Accept-Encoding") String acceptEncoding, @retrofit2.http.Query("since") OffsetDateTime since
  );

}
