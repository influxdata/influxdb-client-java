package org.influxdata.client.service;

import retrofit2.Call;
import retrofit2.http.*;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.MultipartBody;

import org.influxdata.client.domain.AddResourceMemberRequestBody;
import org.influxdata.client.domain.Bucket;
import org.influxdata.client.domain.Buckets;
import org.influxdata.client.domain.Error;
import org.influxdata.client.domain.LabelMapping;
import org.influxdata.client.domain.LabelResponse;
import org.influxdata.client.domain.LabelsResponse;
import org.influxdata.client.domain.OperationLogs;
import org.influxdata.client.domain.ResourceMember;
import org.influxdata.client.domain.ResourceMembers;
import org.influxdata.client.domain.ResourceOwner;
import org.influxdata.client.domain.ResourceOwners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface BucketsService {
  /**
   * Delete a bucket
   * 
   * @param bucketID ID of bucket to delete (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("buckets/{bucketID}")
  Call<Void> bucketsBucketIDDelete(
    @retrofit2.http.Path("bucketID") String bucketID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Retrieve a bucket
   * 
   * @param bucketID ID of bucket to get (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Bucket&gt;
   */
  @GET("buckets/{bucketID}")
  Call<Bucket> bucketsBucketIDGet(
    @retrofit2.http.Path("bucketID") String bucketID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * list all labels for a bucket
   * 
   * @param bucketID ID of the bucket (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;LabelsResponse&gt;
   */
  @GET("buckets/{bucketID}/labels")
  Call<LabelsResponse> bucketsBucketIDLabelsGet(
    @retrofit2.http.Path("bucketID") String bucketID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * delete a label from a bucket
   * 
   * @param bucketID ID of the bucket (required)
   * @param labelID the label id to delete (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("buckets/{bucketID}/labels/{labelID}")
  Call<Void> bucketsBucketIDLabelsLabelIDDelete(
    @retrofit2.http.Path("bucketID") String bucketID, @retrofit2.http.Path("labelID") String labelID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * add a label to a bucket
   * 
   * @param bucketID ID of the bucket (required)
   * @param labelMapping label to add (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;LabelsResponse&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("buckets/{bucketID}/labels")
  Call<LabelResponse> bucketsBucketIDLabelsPost(
    @retrofit2.http.Path("bucketID") String bucketID, @retrofit2.http.Body LabelMapping labelMapping, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Retrieve operation logs for a bucket
   * 
   * @param bucketID ID of the bucket (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param offset  (optional)
   * @param limit  (optional, default to 20)
   * @return Call&lt;OperationLogs&gt;
   */
  @GET("buckets/{bucketID}/logs")
  Call<OperationLogs> bucketsBucketIDLogsGet(
    @retrofit2.http.Path("bucketID") String bucketID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Query("offset") Integer offset, @retrofit2.http.Query("limit") Integer limit
  );

  /**
   * List all users with member privileges for a bucket
   * 
   * @param bucketID ID of the bucket (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;ResourceMembers&gt;
   */
  @GET("buckets/{bucketID}/members")
  Call<ResourceMembers> bucketsBucketIDMembersGet(
    @retrofit2.http.Path("bucketID") String bucketID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Add bucket member
   * 
   * @param bucketID ID of the bucket (required)
   * @param addResourceMemberRequestBody user to add as member (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;ResourceMember&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("buckets/{bucketID}/members")
  Call<ResourceMember> bucketsBucketIDMembersPost(
    @retrofit2.http.Path("bucketID") String bucketID, @retrofit2.http.Body AddResourceMemberRequestBody addResourceMemberRequestBody, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * removes a member from an bucket
   * 
   * @param userID ID of member to remove (required)
   * @param bucketID ID of the bucket (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("buckets/{bucketID}/members/{userID}")
  Call<Void> bucketsBucketIDMembersUserIDDelete(
    @retrofit2.http.Path("userID") String userID, @retrofit2.http.Path("bucketID") String bucketID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * List all owners of a bucket
   * 
   * @param bucketID ID of the bucket (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;ResourceOwners&gt;
   */
  @GET("buckets/{bucketID}/owners")
  Call<ResourceOwners> bucketsBucketIDOwnersGet(
    @retrofit2.http.Path("bucketID") String bucketID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Add bucket owner
   * 
   * @param bucketID ID of the bucket (required)
   * @param addResourceMemberRequestBody user to add as owner (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;ResourceOwner&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("buckets/{bucketID}/owners")
  Call<ResourceOwner> bucketsBucketIDOwnersPost(
    @retrofit2.http.Path("bucketID") String bucketID, @retrofit2.http.Body AddResourceMemberRequestBody addResourceMemberRequestBody, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * removes an owner from a bucket
   * 
   * @param userID ID of owner to remove (required)
   * @param bucketID ID of the bucket (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("buckets/{bucketID}/owners/{userID}")
  Call<Void> bucketsBucketIDOwnersUserIDDelete(
    @retrofit2.http.Path("userID") String userID, @retrofit2.http.Path("bucketID") String bucketID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Update a bucket
   * 
   * @param bucketID ID of bucket to update (required)
   * @param bucket bucket update to apply (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Bucket&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @PATCH("buckets/{bucketID}")
  Call<Bucket> bucketsBucketIDPatch(
    @retrofit2.http.Path("bucketID") String bucketID, @retrofit2.http.Body Bucket bucket, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * List all buckets
   * 
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param offset  (optional)
   * @param limit  (optional, default to 20)
   * @param org specifies the organization name of the resource (optional)
   * @param orgID specifies the organization id of the resource (optional)
   * @param name only returns buckets with the specified name (optional)
   * @return Call&lt;Buckets&gt;
   */
  @GET("buckets")
  Call<Buckets> bucketsGet(
    @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Query("offset") Integer offset, @retrofit2.http.Query("limit") Integer limit, @retrofit2.http.Query("org") String org, @retrofit2.http.Query("orgID") String orgID, @retrofit2.http.Query("name") String name
  );

  /**
   * Create a bucket
   * 
   * @param bucket bucket to create (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Bucket&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("buckets")
  Call<Bucket> bucketsPost(
    @retrofit2.http.Body Bucket bucket, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Get a sources buckets (will return dbrps in the form of buckets if it is a v1 source)
   * 
   * @param sourceID ID of the source (required)
   * @param org specifies the organization of the resource (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Buckets&gt;
   */
  @GET("sources/{sourceID}/buckets")
  Call<Buckets> sourcesSourceIDBucketsGet(
    @retrofit2.http.Path("sourceID") String sourceID, @retrofit2.http.Query("org") String org, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

}
