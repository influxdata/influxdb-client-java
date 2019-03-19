package org.influxdata.client.service;

import retrofit2.Call;
import retrofit2.http.*;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.MultipartBody;

import org.influxdata.client.domain.Buckets;
import org.influxdata.client.domain.Check;
import org.influxdata.client.domain.Error;
import org.influxdata.client.domain.Source;
import org.influxdata.client.domain.Sources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface SourcesService {
  /**
   * Get all sources
   * 
   * @param org specifies the organization of the resource (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Sources&gt;
   */
  @GET("sources")
  Call<Sources> sourcesGet(
    @retrofit2.http.Query("org") String org, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Creates a Source
   * 
   * @param source source to create (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Source&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("sources")
  Call<Source> sourcesPost(
    @retrofit2.http.Body Source source, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
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

  /**
   * Delete a source
   * 
   * @param sourceID ID of the source (required)
   * @return Call&lt;Void&gt;
   */
  @DELETE("sources/{sourceID}")
  Call<Void> sourcesSourceIDDelete(
    @retrofit2.http.Path("sourceID") String sourceID
  );

  /**
   * Get a source
   * 
   * @param sourceID ID of the source (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Source&gt;
   */
  @GET("sources/{sourceID}")
  Call<Source> sourcesSourceIDGet(
    @retrofit2.http.Path("sourceID") String sourceID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Get a sources health
   * 
   * @param sourceID ID of the source (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Check&gt;
   */
  @GET("sources/{sourceID}/health")
  Call<Check> sourcesSourceIDHealthGet(
    @retrofit2.http.Path("sourceID") String sourceID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Updates a Source
   * 
   * @param sourceID ID of the source (required)
   * @param source source update (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Source&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @PATCH("sources/{sourceID}")
  Call<Source> sourcesSourceIDPatch(
    @retrofit2.http.Path("sourceID") String sourceID, @retrofit2.http.Body Source source, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

}
