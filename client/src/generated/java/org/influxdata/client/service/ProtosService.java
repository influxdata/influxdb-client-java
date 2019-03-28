package org.influxdata.client.service;

import retrofit2.Call;
import retrofit2.http.*;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.MultipartBody;

import org.influxdata.client.domain.CreateProtoResourcesRequest;
import org.influxdata.client.domain.Dashboards;
import org.influxdata.client.domain.Error;
import org.influxdata.client.domain.Protos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface ProtosService {
  /**
   * List of available protos (templates of tasks/dashboards/etc)
   * 
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Protos&gt;
   */
  @GET("api/v2/protos")
  Call<Protos> protosGet(
    @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Create instance of a proto dashboard
   * 
   * @param protoID ID of proto (required)
   * @param createProtoResourcesRequest organization that the dashboard will be created as (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Dashboards&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/protos/{protoID}/dashboards")
  Call<Dashboards> protosProtoIDDashboardsPost(
    @retrofit2.http.Path("protoID") String protoID, @retrofit2.http.Body CreateProtoResourcesRequest createProtoResourcesRequest, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

}
