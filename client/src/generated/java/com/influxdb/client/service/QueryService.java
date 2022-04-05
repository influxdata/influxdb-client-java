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
   * Retrieve query suggestions
   * 
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;FluxSuggestions&gt;
   */
  @GET("api/v2/query/suggestions")
  Call<FluxSuggestions> getQuerySuggestions(
    @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Retrieve query suggestions for a branching suggestion
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
   * Query data
   * Retrieves data from InfluxDB buckets.  To query data, you need the following: - **organization** – _See [View organizations](https://docs.influxdata.com/influxdb/v2.1/organizations/view-orgs/#view-your-organization-id) for instructions on viewing your organization ID._ - **API token** – _See [View tokens](https://docs.influxdata.com/influxdb/v2.1/security/tokens/view-tokens/)  for instructions on viewing your API token._ - **InfluxDB URL** – _See [InfluxDB URLs](https://docs.influxdata.com/influxdb/v2.1/reference/urls/)_. - **Flux query** – _See [Flux](https://docs.influxdata.com/flux/v0.x/)._  For more information and examples, see [Query with the InfluxDB API](https://docs.influxdata.com/influxdb/v2.1/query-data/execute-queries/influx-api/).
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param acceptEncoding Indicates the content encoding (usually a compression algorithm) that the client can understand. (optional, default to identity)
   * @param contentType  (optional)
   * @param org Name of the organization executing the query. Accepts either the ID or Name. If you provide both &#x60;orgID&#x60; and &#x60;org&#x60;, &#x60;org&#x60; takes precedence. (optional)
   * @param orgID ID of the organization executing the query. If you provide both &#x60;orgID&#x60; and &#x60;org&#x60;, &#x60;org&#x60; takes precedence. (optional)
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
   * Query data
   * Retrieves data from InfluxDB buckets.  To query data, you need the following: - **organization** – _See [View organizations](https://docs.influxdata.com/influxdb/v2.1/organizations/view-orgs/#view-your-organization-id) for instructions on viewing your organization ID._ - **API token** – _See [View tokens](https://docs.influxdata.com/influxdb/v2.1/security/tokens/view-tokens/)  for instructions on viewing your API token._ - **InfluxDB URL** – _See [InfluxDB URLs](https://docs.influxdata.com/influxdb/v2.1/reference/urls/)_. - **Flux query** – _See [Flux](https://docs.influxdata.com/flux/v0.x/)._  For more information and examples, see [Query with the InfluxDB API](https://docs.influxdata.com/influxdb/v2.1/query-data/execute-queries/influx-api/).
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param acceptEncoding Indicates the content encoding (usually a compression algorithm) that the client can understand. (optional, default to identity)
   * @param contentType  (optional)
   * @param org Name of the organization executing the query. Accepts either the ID or Name. If you provide both &#x60;orgID&#x60; and &#x60;org&#x60;, &#x60;org&#x60; takes precedence. (optional)
   * @param orgID ID of the organization executing the query. If you provide both &#x60;orgID&#x60; and &#x60;org&#x60;, &#x60;org&#x60; takes precedence. (optional)
   * @param query Flux query or specification to execute (optional)
   * @return Call&lt;ResponseBody&gt;
   */
  @POST("api/v2/query")
  @Streaming
  Call<ResponseBody> postQueryResponseBody(
    @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Header("Accept-Encoding") String acceptEncoding, @retrofit2.http.Header("Content-Type") String contentType, @retrofit2.http.Query("org") String org, @retrofit2.http.Query("orgID") String orgID, @retrofit2.http.Body Query query
  );

  /**
   * Analyze a Flux query
   * 
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param contentType  (optional)
   * @param query Flux query to analyze (optional)
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
   * Generate an Abstract Syntax Tree (AST) from a query
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
