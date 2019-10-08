package com.influxdb.client.service;

import retrofit2.Call;
import retrofit2.http.*;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.MultipartBody;

import com.influxdb.client.domain.Error;
import com.influxdb.client.domain.LabelCreateRequest;
import com.influxdb.client.domain.LabelResponse;
import com.influxdb.client.domain.LabelUpdate;
import com.influxdb.client.domain.LabelsResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface LabelsService {
  /**
   * Delete a label
   * 
   * @param labelID The ID of the label to delete. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("api/v2/labels/{labelID}")
  Call<Void> deleteLabelsID(
    @retrofit2.http.Path("labelID") String labelID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Get all labels
   * 
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param orgID The organization ID. (optional)
   * @return Call&lt;LabelsResponse&gt;
   */
  @GET("api/v2/labels")
  Call<LabelsResponse> getLabels(
    @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Query("orgID") String orgID
  );

  /**
   * Get a label
   * 
   * @param labelID The ID of the label to update. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;LabelResponse&gt;
   */
  @GET("api/v2/labels/{labelID}")
  Call<LabelResponse> getLabelsID(
    @retrofit2.http.Path("labelID") String labelID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Update a label
   * 
   * @param labelID The ID of the label to update. (required)
   * @param labelUpdate Label update (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;LabelResponse&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @PATCH("api/v2/labels/{labelID}")
  Call<LabelResponse> patchLabelsID(
    @retrofit2.http.Path("labelID") String labelID, @retrofit2.http.Body LabelUpdate labelUpdate, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Create a label
   * 
   * @param labelCreateRequest Label to create (required)
   * @return Call&lt;LabelResponse&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/labels")
  Call<LabelResponse> postLabels(
    @retrofit2.http.Body LabelCreateRequest labelCreateRequest
  );

}
