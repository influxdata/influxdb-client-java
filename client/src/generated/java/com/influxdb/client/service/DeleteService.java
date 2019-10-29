package com.influxdb.client.service;

import com.influxdb.client.domain.DeletePredicateRequest;

import retrofit2.Call;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * @author Pavlina Rolincova (rolincova@github) (29/10/2019).
 */
public interface DeleteService {
    /**
     * delete Time series data from InfluxDB
     *
     * @param deletePredicateRequest predicate delete request (required)
     * @param zapTraceSpan OpenTracing span context (optional)
     * @param org specifies the destination organization for writes (optional)
     * @param bucket specifies the destination bucket for writes (optional)
     * @param orgID specifies the organization ID of the resource (optional)
     * @param bucketID specifies the destination bucket ID for writes (optional)
     * @return Call&lt;Void&gt;
     */
    @Headers({
            "Content-Type:application/json"
    })
    @POST("api/v2/delete")
    Call<Void> deletePost(
            @retrofit2.http.Body DeletePredicateRequest deletePredicateRequest,
            @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan,
            @retrofit2.http.Query("org") String org,
            @retrofit2.http.Query("bucket") String bucket,
            @retrofit2.http.Query("orgID") String orgID,
            @retrofit2.http.Query("bucketID") String bucketID
    );
}
