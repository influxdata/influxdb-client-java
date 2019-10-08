package com.influxdb.client.service;

import retrofit2.Call;
import retrofit2.http.*;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.MultipartBody;

import com.influxdb.client.domain.ASTResponse;
import com.influxdb.client.domain.AnalyzeQueryResponse;
import com.influxdb.client.domain.Error;
import com.influxdb.client.domain.FluxSuggestion;
import com.influxdb.client.domain.FluxSuggestions;
import com.influxdb.client.domain.LanguageRequest;
import com.influxdb.client.domain.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface QueryService {
  /**
   * 
   * 
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;FluxSuggestions&gt;
   */
  @GET("api/v2/query/suggestions")
  Call<FluxSuggestions> getQuerySuggestions(
    @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * 
   * 
   * @param name The name of the branching suggestion. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;FluxSuggestion&gt;
   */
  @GET("api/v2/query/suggestions/{name}")
  Call<FluxSuggestion> getQuerySuggestionsName(
    @retrofit2.http.Path("name") String name, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Query InfluxDB
   * 
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param acceptEncoding The Accept-Encoding request HTTP header advertises which content encoding, usually a compression algorithm, the client is able to understand. (optional, default to identity)
   * @param contentType  (optional)
   * @param org Specifies the name of the organization executing the query. Takes either the ID or Name interchangeably. If both &#x60;orgID&#x60; and &#x60;org&#x60; are specified, &#x60;org&#x60; takes precedence. (optional)
   * @param orgID Specifies the ID of the organization executing the query. If both &#x60;orgID&#x60; and &#x60;org&#x60; are specified, &#x60;org&#x60; takes precedence. (optional)
   * @param query Flux query or specification to execute (optional)
   * @return Call&lt;String&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/query")
  Call<String> postQuery(
    @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Header("Accept-Encoding") String acceptEncoding, @retrofit2.http.Header("Content-Type") String contentType, @retrofit2.http.Query("org") String org, @retrofit2.http.Query("orgID") String orgID, @retrofit2.http.Body Query query
  );

  /**
   * Query InfluxDB
   * 
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param acceptEncoding The Accept-Encoding request HTTP header advertises which content encoding, usually a compression algorithm, the client is able to understand. (optional, default to identity)
   * @param contentType  (optional)
   * @param org Specifies the name of the organization executing the query. Takes either the ID or Name interchangeably. If both &#x60;orgID&#x60; and &#x60;org&#x60; are specified, &#x60;org&#x60; takes precedence. (optional)
   * @param orgID Specifies the ID of the organization executing the query. If both &#x60;orgID&#x60; and &#x60;org&#x60; are specified, &#x60;org&#x60; takes precedence. (optional)
   * @param query Flux query or specification to execute (optional)
   * @return Call&lt;ResponseBody&gt;
   */
  @POST("api/v2/query")
  Call<ResponseBody> postQueryResponseBody(
    @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Header("Accept-Encoding") String acceptEncoding, @retrofit2.http.Header("Content-Type") String contentType, @retrofit2.http.Query("org") String org, @retrofit2.http.Query("orgID") String orgID, @retrofit2.http.Body Query query
  );

  /**
   * Query InfluxDB
   * 
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param acceptEncoding The Accept-Encoding request HTTP header advertises which content encoding, usually a compression algorithm, the client is able to understand. (optional, default to identity)
   * @param contentType  (optional)
   * @param org Specifies the name of the organization executing the query. Takes either the ID or Name interchangeably. If both &#x60;orgID&#x60; and &#x60;org&#x60; are specified, &#x60;org&#x60; takes precedence. (optional)
   * @param orgID Specifies the ID of the organization executing the query. If both &#x60;orgID&#x60; and &#x60;org&#x60; are specified, &#x60;org&#x60; takes precedence. (optional)
   * @param query Flux query or specification to execute (optional)
   * @return Call&lt;String&gt;
   */
  @POST("api/v2/query")
  Call<String> postQueryString(
    @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Header("Accept-Encoding") String acceptEncoding, @retrofit2.http.Header("Content-Type") String contentType, @retrofit2.http.Query("org") String org, @retrofit2.http.Query("orgID") String orgID, @retrofit2.http.Body Query query
  );

  /**
   * Analyze an InfluxQL or Flux query
   * 
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param contentType  (optional)
   * @param query Flux or InfluxQL query to analyze (optional)
   * @return Call&lt;AnalyzeQueryResponse&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/query/analyze")
  Call<AnalyzeQueryResponse> postQueryAnalyze(
    @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Header("Content-Type") String contentType, @retrofit2.http.Body Query query
  );

  /**
   * 
   * Analyzes flux query and generates a query specification.
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param contentType  (optional)
   * @param languageRequest Analyzed Flux query to generate abstract syntax tree. (optional)
   * @return Call&lt;ASTResponse&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/query/ast")
  Call<ASTResponse> postQueryAst(
    @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Header("Content-Type") String contentType, @retrofit2.http.Body LanguageRequest languageRequest
  );

}
