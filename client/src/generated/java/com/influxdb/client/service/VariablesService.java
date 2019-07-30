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
   * delete a variable
   * 
   * @param variableID id of the variable (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("api/v2/variables/{variableID}")
  Call<Void> deleteVariablesID(
    @retrofit2.http.Path("variableID") String variableID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * delete a label from a variable
   * 
   * @param variableID ID of the variable (required)
   * @param labelID the label id to delete (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("api/v2/variables/{variableID}/labels/{labelID}")
  Call<Void> deleteVariablesIDLabelsID(
    @retrofit2.http.Path("variableID") String variableID, @retrofit2.http.Path("labelID") String labelID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * get all variables
   * 
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param org specifies the organization name of the resource (optional)
   * @param orgID specifies the organization id of the resource (optional)
   * @return Call&lt;Variables&gt;
   */
  @GET("api/v2/variables")
  Call<Variables> getVariables(
    @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Query("org") String org, @retrofit2.http.Query("orgID") String orgID
  );

  /**
   * get a variable
   * 
   * @param variableID ID of the variable (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Variable&gt;
   */
  @GET("api/v2/variables/{variableID}")
  Call<Variable> getVariablesID(
    @retrofit2.http.Path("variableID") String variableID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * list all labels for a variable
   * 
   * @param variableID ID of the variable (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;LabelsResponse&gt;
   */
  @GET("api/v2/variables/{variableID}/labels")
  Call<LabelsResponse> getVariablesIDLabels(
    @retrofit2.http.Path("variableID") String variableID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * update a variable
   * 
   * @param variableID id of the variable (required)
   * @param variable variable update to apply (required)
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
   * create a variable
   * 
   * @param variable variable to create (required)
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
   * add a label to a variable
   * 
   * @param variableID ID of the variable (required)
   * @param labelMapping label to add (required)
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
   * replace a variable
   * 
   * @param variableID id of the variable (required)
   * @param variable variable to replace (required)
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
