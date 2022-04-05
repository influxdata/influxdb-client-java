package com.influxdb.client.service;

import retrofit2.Call;
import retrofit2.http.*;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.MultipartBody;

import com.influxdb.client.domain.AddResourceMemberRequestBody;
import com.influxdb.client.domain.Bucket;
import com.influxdb.client.domain.Buckets;
import com.influxdb.client.domain.Error;
import com.influxdb.client.domain.LabelMapping;
import com.influxdb.client.domain.LabelResponse;
import com.influxdb.client.domain.LabelsResponse;
import com.influxdb.client.domain.PatchBucketRequest;
import com.influxdb.client.domain.PostBucketRequest;
import com.influxdb.client.domain.ResourceMember;
import com.influxdb.client.domain.ResourceMembers;
import com.influxdb.client.domain.ResourceOwner;
import com.influxdb.client.domain.ResourceOwners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface BucketsService {
  /**
   * Delete a bucket
   * 
   * @param bucketID The ID of the bucket to delete. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("api/v2/buckets/{bucketID}")
  Call<Void> deleteBucketsID(
    @retrofit2.http.Path("bucketID") String bucketID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Delete a label from a bucket
   * 
   * @param bucketID The bucket ID. (required)
   * @param labelID The ID of the label to delete. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("api/v2/buckets/{bucketID}/labels/{labelID}")
  Call<Void> deleteBucketsIDLabelsID(
    @retrofit2.http.Path("bucketID") String bucketID, @retrofit2.http.Path("labelID") String labelID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Remove a member from a bucket
   * 
   * @param userID The ID of the member to remove. (required)
   * @param bucketID The bucket ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("api/v2/buckets/{bucketID}/members/{userID}")
  Call<Void> deleteBucketsIDMembersID(
    @retrofit2.http.Path("userID") String userID, @retrofit2.http.Path("bucketID") String bucketID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Remove an owner from a bucket
   * 
   * @param userID The ID of the owner to remove. (required)
   * @param bucketID The bucket ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("api/v2/buckets/{bucketID}/owners/{userID}")
  Call<Void> deleteBucketsIDOwnersID(
    @retrofit2.http.Path("userID") String userID, @retrofit2.http.Path("bucketID") String bucketID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * List all buckets
   * 
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param offset  (optional)
   * @param limit  (optional, default to 20)
   * @param after Resource ID to seek from. Results are not inclusive of this ID. Use &#x60;after&#x60; instead of &#x60;offset&#x60;. (optional)
   * @param org The name of the organization. (optional)
   * @param orgID The organization ID. (optional)
   * @param name Only returns buckets with a specific name. (optional)
   * @param id Only returns buckets with a specific ID. (optional)
   * @return Call&lt;Buckets&gt;
   */
  @GET("api/v2/buckets")
  Call<Buckets> getBuckets(
    @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Query("offset") Integer offset, @retrofit2.http.Query("limit") Integer limit, @retrofit2.http.Query("after") String after, @retrofit2.http.Query("org") String org, @retrofit2.http.Query("orgID") String orgID, @retrofit2.http.Query("name") String name, @retrofit2.http.Query("id") String id
  );

  /**
   * Retrieve a bucket
   * 
   * @param bucketID The bucket ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Bucket&gt;
   */
  @GET("api/v2/buckets/{bucketID}")
  Call<Bucket> getBucketsID(
    @retrofit2.http.Path("bucketID") String bucketID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * List all labels for a bucket
   * 
   * @param bucketID The bucket ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;LabelsResponse&gt;
   */
  @GET("api/v2/buckets/{bucketID}/labels")
  Call<LabelsResponse> getBucketsIDLabels(
    @retrofit2.http.Path("bucketID") String bucketID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * List all users with member privileges for a bucket
   * 
   * @param bucketID The bucket ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;ResourceMembers&gt;
   */
  @GET("api/v2/buckets/{bucketID}/members")
  Call<ResourceMembers> getBucketsIDMembers(
    @retrofit2.http.Path("bucketID") String bucketID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * List all owners of a bucket
   * 
   * @param bucketID The bucket ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;ResourceOwners&gt;
   */
  @GET("api/v2/buckets/{bucketID}/owners")
  Call<ResourceOwners> getBucketsIDOwners(
    @retrofit2.http.Path("bucketID") String bucketID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Get buckets in a source
   * 
   * @param sourceID The source ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param org The name of the organization. (optional)
   * @return Call&lt;Buckets&gt;
   */
  @GET("api/v2/sources/{sourceID}/buckets")
  Call<Buckets> getSourcesIDBuckets(
    @retrofit2.http.Path("sourceID") String sourceID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Query("org") String org
  );

  /**
   * Update a bucket
   * 
   * @param bucketID The bucket ID. (required)
   * @param patchBucketRequest Bucket update to apply (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Bucket&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @PATCH("api/v2/buckets/{bucketID}")
  Call<Bucket> patchBucketsID(
    @retrofit2.http.Path("bucketID") String bucketID, @retrofit2.http.Body PatchBucketRequest patchBucketRequest, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Create a bucket
   * 
   * @param postBucketRequest Bucket to create (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Bucket&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/buckets")
  Call<Bucket> postBuckets(
    @retrofit2.http.Body PostBucketRequest postBucketRequest, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Add a label to a bucket
   * 
   * @param bucketID The bucket ID. (required)
   * @param labelMapping Label to add (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;LabelResponse&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/buckets/{bucketID}/labels")
  Call<LabelResponse> postBucketsIDLabels(
    @retrofit2.http.Path("bucketID") String bucketID, @retrofit2.http.Body LabelMapping labelMapping, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Add a member to a bucket
   * 
   * @param bucketID The bucket ID. (required)
   * @param addResourceMemberRequestBody User to add as member (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;ResourceMember&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/buckets/{bucketID}/members")
  Call<ResourceMember> postBucketsIDMembers(
    @retrofit2.http.Path("bucketID") String bucketID, @retrofit2.http.Body AddResourceMemberRequestBody addResourceMemberRequestBody, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Add an owner to a bucket
   * 
   * @param bucketID The bucket ID. (required)
   * @param addResourceMemberRequestBody User to add as owner (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;ResourceOwner&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/buckets/{bucketID}/owners")
  Call<ResourceOwner> postBucketsIDOwners(
    @retrofit2.http.Path("bucketID") String bucketID, @retrofit2.http.Body AddResourceMemberRequestBody addResourceMemberRequestBody, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

}
