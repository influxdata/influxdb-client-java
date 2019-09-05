package com.influxdb.client.service;

import retrofit2.Call;
import retrofit2.http.*;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.MultipartBody;

import com.influxdb.client.domain.Check;
import com.influxdb.client.domain.Checks;
import com.influxdb.client.domain.Error;
import com.influxdb.client.domain.FluxResponse;
import com.influxdb.client.domain.LabelMapping;
import com.influxdb.client.domain.LabelResponse;
import com.influxdb.client.domain.LabelsResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface ChecksService {
  /**
   * Add new check
   * 
   * @param check check to create (required)
   * @return Call&lt;Check&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/checks")
  Call<Check> createCheck(
    @retrofit2.http.Body Check check
  );

  /**
   * Delete a check
   * 
   * @param checkID ID of check (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("api/v2/checks/{checkID}")
  Call<Void> deleteChecksID(
    @retrofit2.http.Path("checkID") String checkID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * delete label from a check
   * 
   * @param checkID ID of the check (required)
   * @param labelID the label id to delete (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("api/v2/checks/{checkID}/labels/{labelID}")
  Call<Void> deleteChecksIDLabelsID(
    @retrofit2.http.Path("checkID") String checkID, @retrofit2.http.Path("labelID") String labelID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Get all checks
   * 
   * @param orgID only show checks belonging to specified organization (required)
   * @param offset  (optional)
   * @param limit  (optional, default to 20)
   * @return Call&lt;Checks&gt;
   */
  @GET("api/v2/checks")
  Call<Checks> getChecks(
    @retrofit2.http.Query("orgID") String orgID, @retrofit2.http.Query("offset") Integer offset, @retrofit2.http.Query("limit") Integer limit
  );

  /**
   * Get a check
   * 
   * @param checkID ID of check (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Check&gt;
   */
  @GET("api/v2/checks/{checkID}")
  Call<Check> getChecksID(
    @retrofit2.http.Path("checkID") String checkID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * list all labels for a check
   * 
   * @param checkID ID of the check (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;LabelsResponse&gt;
   */
  @GET("api/v2/checks/{checkID}/labels")
  Call<LabelsResponse> getChecksIDLabels(
    @retrofit2.http.Path("checkID") String checkID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Get an check query
   * 
   * @param checkID ID of check (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;FluxResponse&gt;
   */
  @GET("api/v2/checks/{checkID}/query")
  Call<FluxResponse> getChecksIDQuery(
    @retrofit2.http.Path("checkID") String checkID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Update a check
   * 
   * @param checkID ID of check (required)
   * @param check check update to apply (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Check&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @PATCH("api/v2/checks/{checkID}")
  Call<Check> patchChecksID(
    @retrofit2.http.Path("checkID") String checkID, @retrofit2.http.Body Check check, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * add a label to a check
   * 
   * @param checkID ID of the check (required)
   * @param labelMapping label to add (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;LabelResponse&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/checks/{checkID}/labels")
  Call<LabelResponse> postChecksIDLabels(
    @retrofit2.http.Path("checkID") String checkID, @retrofit2.http.Body LabelMapping labelMapping, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

}
