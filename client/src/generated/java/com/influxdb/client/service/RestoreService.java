package com.influxdb.client.service;

import retrofit2.Call;
import retrofit2.http.*;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.MultipartBody;

import com.influxdb.client.domain.BucketMetadataManifest;
import com.influxdb.client.domain.File;
import com.influxdb.client.domain.PostRestoreKVResponse;
import com.influxdb.client.domain.RestoredBucketMappings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface RestoreService {
  /**
   * Overwrite storage metadata for a bucket with shard info from a backup.
   * 
   * @param bucketID The bucket ID. (required)
   * @param body Database info serialized as protobuf. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param contentType  (optional, default to application/octet-stream)
   * @return Call&lt;byte[]&gt;
   * @deprecated
   */
  @Deprecated
  @Headers({
    "Content-Type:text/plain"
  })
  @POST("api/v2/restore/bucket/{bucketID}")
  Call<byte[]> postRestoreBucketID(
    @retrofit2.http.Path("bucketID") String bucketID, @retrofit2.http.Body byte[] body, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Header("Content-Type") String contentType
  );

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
  @POST("api/v2/restore/bucketMetadata")
  Call<RestoredBucketMappings> postRestoreBucketMetadata(
    @retrofit2.http.Body BucketMetadataManifest bucketMetadataManifest, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Overwrite the embedded KV store on the server with a backed-up snapshot.
   * 
   * @param body Full KV snapshot. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param contentEncoding The value tells InfluxDB what compression is applied to the line protocol in the request payload. To make an API request with a GZIP payload, send &#x60;Content-Encoding: gzip&#x60; as a request header. (optional, default to identity)
   * @param contentType  (optional, default to application/octet-stream)
   * @return Call&lt;PostRestoreKVResponse&gt;
   */
  @Headers({
    "Content-Type:text/plain"
  })
  @POST("api/v2/restore/kv")
  Call<PostRestoreKVResponse> postRestoreKV(
    @retrofit2.http.Body File body, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Header("Content-Encoding") String contentEncoding, @retrofit2.http.Header("Content-Type") String contentType
  );

  /**
   * Overwrite the embedded SQL store on the server with a backed-up snapshot.
   * 
   * @param body Full SQL snapshot. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param contentEncoding The value tells InfluxDB what compression is applied to the line protocol in the request payload. To make an API request with a GZIP payload, send &#x60;Content-Encoding: gzip&#x60; as a request header. (optional, default to identity)
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
   * @param contentEncoding The value tells InfluxDB what compression is applied to the line protocol in the request payload. To make an API request with a GZIP payload, send &#x60;Content-Encoding: gzip&#x60; as a request header. (optional, default to identity)
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
