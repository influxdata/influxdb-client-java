package com.influxdb.client.service;

import retrofit2.Call;
import retrofit2.http.*;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.MultipartBody;

import com.influxdb.client.domain.BucketMetadataManifest;
import com.influxdb.client.domain.Error;
import com.influxdb.client.domain.File;
import com.influxdb.client.domain.RestoredBucketMappings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface RestoreService {
  /**
   * Create a new bucket pre-seeded with shard info from a backup.
   * 
   * @param bucketMetadataManifest Metadata manifest for a bucket. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;RestoredBucketMappings&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/restore/bucket-metadata")
  Call<RestoredBucketMappings> postRestoreBucketMetadata(
    @retrofit2.http.Body BucketMetadataManifest bucketMetadataManifest, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Overwrite the embedded KV store on the server with a backed-up snapshot.
   * 
   * @param body Full KV snapshot. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param contentEncoding When present, its value indicates to the database that compression is applied to the line-protocol body. (optional, default to identity)
   * @param contentType  (optional, default to application/octet-stream)
   * @return Call&lt;Void&gt;
   */
  @Headers({
    "Content-Type:text/plain"
  })
  @POST("api/v2/restore/kv")
  Call<Void> postRestoreKV(
    @retrofit2.http.Body File body, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Header("Content-Encoding") String contentEncoding, @retrofit2.http.Header("Content-Type") String contentType
  );

  /**
   * Overwrite the embedded SQL store on the server with a backed-up snapshot.
   * 
   * @param body Full SQL snapshot. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param contentEncoding When present, its value indicates to the database that compression is applied to the line-protocol body. (optional, default to identity)
   * @param contentType  (optional, default to application/octet-stream)
   * @return Call&lt;Void&gt;
   */
  @Headers({
    "Content-Type:text/plain"
  })
  @POST("api/v2/restore/sql")
  Call<Void> postRestoreSQL(
    @retrofit2.http.Body File body, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Header("Content-Encoding") String contentEncoding, @retrofit2.http.Header("Content-Type") String contentType
  );

  /**
   * Restore a TSM snapshot into a shard.
   * 
   * @param shardID The shard ID. (required)
   * @param body TSM snapshot. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param contentEncoding When present, its value indicates to the database that compression is applied to the line-protocol body. (optional, default to identity)
   * @param contentType  (optional, default to application/octet-stream)
   * @return Call&lt;Void&gt;
   */
  @Headers({
    "Content-Type:text/plain"
  })
  @POST("api/v2/restore/shards/{shardID}")
  Call<Void> postRestoreShardId(
    @retrofit2.http.Path("shardID") String shardID, @retrofit2.http.Body File body, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Header("Content-Encoding") String contentEncoding, @retrofit2.http.Header("Content-Type") String contentType
  );

}
