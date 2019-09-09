package com.influxdb.client.service;

import retrofit2.Call;
import retrofit2.http.*;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.MultipartBody;

import com.influxdb.client.domain.Document;
import com.influxdb.client.domain.DocumentCreate;
import com.influxdb.client.domain.DocumentUpdate;
import com.influxdb.client.domain.Documents;
import com.influxdb.client.domain.Error;
import com.influxdb.client.domain.LabelMapping;
import com.influxdb.client.domain.LabelResponse;
import com.influxdb.client.domain.LabelsResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface TemplatesService {
  /**
   * Delete a template
   * 
   * @param templateID The template ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("api/v2/documents/templates/{templateID}")
  Call<Void> deleteDocumentsTemplatesID(
    @retrofit2.http.Path("templateID") String templateID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Delete a label from a template
   * 
   * @param templateID The template ID. (required)
   * @param labelID The label ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("api/v2/documents/templates/{templateID}/labels/{labelID}")
  Call<Void> deleteDocumentsTemplatesIDLabelsID(
    @retrofit2.http.Path("templateID") String templateID, @retrofit2.http.Path("labelID") String labelID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * 
   * 
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param org Specifies the name of the organization of the template. (optional)
   * @param orgID Specifies the organization ID of the template. (optional)
   * @return Call&lt;Documents&gt;
   */
  @GET("api/v2/documents/templates")
  Call<Documents> getDocumentsTemplates(
    @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Query("org") String org, @retrofit2.http.Query("orgID") String orgID
  );

  /**
   * 
   * 
   * @param templateID The template ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Document&gt;
   */
  @GET("api/v2/documents/templates/{templateID}")
  Call<Document> getDocumentsTemplatesID(
    @retrofit2.http.Path("templateID") String templateID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * List all labels for a template
   * 
   * @param templateID The template ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;LabelsResponse&gt;
   */
  @GET("api/v2/documents/templates/{templateID}/labels")
  Call<LabelsResponse> getDocumentsTemplatesIDLabels(
    @retrofit2.http.Path("templateID") String templateID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Create a template
   * 
   * @param documentCreate Template that will be created (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Document&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/documents/templates")
  Call<Document> postDocumentsTemplates(
    @retrofit2.http.Body DocumentCreate documentCreate, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Add a label to a template
   * 
   * @param templateID The template ID. (required)
   * @param labelMapping Label to add (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;LabelResponse&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/documents/templates/{templateID}/labels")
  Call<LabelResponse> postDocumentsTemplatesIDLabels(
    @retrofit2.http.Path("templateID") String templateID, @retrofit2.http.Body LabelMapping labelMapping, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * 
   * 
   * @param templateID The template ID. (required)
   * @param documentUpdate Template that will be updated (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Document&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @PUT("api/v2/documents/templates/{templateID}")
  Call<Document> putDocumentsTemplatesID(
    @retrofit2.http.Path("templateID") String templateID, @retrofit2.http.Body DocumentUpdate documentUpdate, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

}
