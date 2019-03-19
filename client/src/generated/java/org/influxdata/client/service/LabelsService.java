package org.influxdata.client.service;

import retrofit2.Call;
import retrofit2.http.*;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.MultipartBody;

import org.influxdata.client.domain.Error;
import org.influxdata.client.domain.Label;
import org.influxdata.client.domain.LabelResponse;
import org.influxdata.client.domain.LabelUpdate;
import org.influxdata.client.domain.LabelsResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface LabelsService {
  /**
   * Get all labels
   * 
   * @return Call&lt;LabelsResponse&gt;
   */
  @GET("labels")
  Call<LabelsResponse> labelsGet();
    

  /**
   * Delete a label
   * 
   * @param labelID ID of label to delete (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("labels/{labelID}")
  Call<Void> labelsLabelIDDelete(
    @retrofit2.http.Path("labelID") String labelID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Get a label
   * 
   * @param labelID ID of label to update (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;LabelResponse&gt;
   */
  @GET("labels/{labelID}")
  Call<LabelResponse> labelsLabelIDGet(
    @retrofit2.http.Path("labelID") String labelID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Update a single label
   * 
   * @param labelID ID of label to update (required)
   * @param labelUpdate label update (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;LabelResponse&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @PATCH("labels/{labelID}")
  Call<LabelResponse> labelsLabelIDPatch(
    @retrofit2.http.Path("labelID") String labelID, @retrofit2.http.Body LabelUpdate labelUpdate, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Create a label
   * 
   * @param label label to create (required)
   * @return Call&lt;LabelResponse&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("labels")
  Call<LabelResponse> labelsPost(
    @retrofit2.http.Body Label label
  );

}
