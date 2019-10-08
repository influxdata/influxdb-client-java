package com.influxdb.client.service;

import retrofit2.Call;
import retrofit2.http.*;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.MultipartBody;

import com.influxdb.client.domain.Error;
import com.influxdb.client.domain.LabelMapping;
import com.influxdb.client.domain.LabelResponse;
import com.influxdb.client.domain.LabelsResponse;
import com.influxdb.client.domain.Variable;
import com.influxdb.client.domain.Variables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface VariablesService {
  /**
   * Delete a variable
   * 
   * @param variableID The variable ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("api/v2/variables/{variableID}")
  Call<Void> deleteVariablesID(
    @retrofit2.http.Path("variableID") String variableID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Delete a label from a variable
   * 
   * @param variableID The variable ID. (required)
   * @param labelID The label ID to delete. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("api/v2/variables/{variableID}/labels/{labelID}")
  Call<Void> deleteVariablesIDLabelsID(
    @retrofit2.http.Path("variableID") String variableID, @retrofit2.http.Path("labelID") String labelID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Get all variables
   * 
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param org The organization name. (optional)
   * @param orgID The organization ID. (optional)
   * @return Call&lt;Variables&gt;
   */
  @GET("api/v2/variables")
  Call<Variables> getVariables(
    @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Query("org") String org, @retrofit2.http.Query("orgID") String orgID
  );

  /**
   * Get a variable
   * 
   * @param variableID The variable ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Variable&gt;
   */
  @GET("api/v2/variables/{variableID}")
  Call<Variable> getVariablesID(
    @retrofit2.http.Path("variableID") String variableID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * List all labels for a variable
   * 
   * @param variableID The variable ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;LabelsResponse&gt;
   */
  @GET("api/v2/variables/{variableID}/labels")
  Call<LabelsResponse> getVariablesIDLabels(
    @retrofit2.http.Path("variableID") String variableID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Update a variable
   * 
   * @param variableID The variable ID. (required)
   * @param variable Variable update to apply (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Variable&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @PATCH("api/v2/variables/{variableID}")
  Call<Variable> patchVariablesID(
    @retrofit2.http.Path("variableID") String variableID, @retrofit2.http.Body Variable variable, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Create a variable
   * 
   * @param variable Variable to create (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Variable&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/variables")
  Call<Variable> postVariables(
    @retrofit2.http.Body Variable variable, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Add a label to a variable
   * 
   * @param variableID The variable ID. (required)
   * @param labelMapping Label to add (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;LabelResponse&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/variables/{variableID}/labels")
  Call<LabelResponse> postVariablesIDLabels(
    @retrofit2.http.Path("variableID") String variableID, @retrofit2.http.Body LabelMapping labelMapping, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Replace a variable
   * 
   * @param variableID The variable ID. (required)
   * @param variable Variable to replace (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Variable&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @PUT("api/v2/variables/{variableID}")
  Call<Variable> putVariablesID(
    @retrofit2.http.Path("variableID") String variableID, @retrofit2.http.Body Variable variable, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

}
