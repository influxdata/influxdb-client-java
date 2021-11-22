package com.influxdb.client.service;

import retrofit2.Call;
import retrofit2.http.*;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.MultipartBody;

import com.influxdb.client.domain.DeletePredicateRequest;
import com.influxdb.client.domain.Error;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface DeleteService {
  /**
   * Delete data
   * 
   * @param deletePredicateRequest Deletes data from an InfluxDB bucket. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param org Specifies the organization to delete data from. (optional)
   * @param bucket Specifies the bucket to delete data from. (optional)
   * @param orgID Specifies the organization ID of the resource. (optional)
   * @param bucketID Specifies the bucket ID to delete data from. (optional)
   * @return Call&lt;Void&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/delete")
  Call<Void> postDelete(
    @retrofit2.http.Body DeletePredicateRequest deletePredicateRequest, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Query("org") String org, @retrofit2.http.Query("bucket") String bucket, @retrofit2.http.Query("orgID") String orgID, @retrofit2.http.Query("bucketID") String bucketID
  );

}
