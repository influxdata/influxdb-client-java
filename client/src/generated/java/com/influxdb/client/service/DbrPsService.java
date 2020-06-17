package com.influxdb.client.service;

import retrofit2.Call;
import retrofit2.http.*;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.MultipartBody;

import com.influxdb.client.domain.DBRP;
import com.influxdb.client.domain.DBRPUpdate;
import com.influxdb.client.domain.DBRPs;
import com.influxdb.client.domain.Error;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface DbrPsService {
  /**
   * Delete a database retention policy
   * 
   * @param dbrpID The database retention policy mapping (required)
   * @param orgID Specifies the organization ID of the mapping (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("api/v2/dbrps/{dbrpID}")
  Call<Void> deleteDBRPID(
    @retrofit2.http.Path("dbrpID") String dbrpID, @retrofit2.http.Query("orgID") String orgID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * List all database retention policy mappings
   * 
   * @param orgID Specifies the organization ID to filter on (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param id Specifies the mapping ID to filter on (optional)
   * @param bucketID Specifies the bucket ID to filter on (optional)
   * @param _default Specifies filtering on default (optional)
   * @param db Specifies the database to filter on (optional)
   * @param rp Specifies the retention policy to filter on (optional)
   * @return Call&lt;DBRPs&gt;
   */
  @GET("api/v2/dbrps")
  Call<DBRPs> getDBRPs(
    @retrofit2.http.Query("orgID") String orgID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Query("id") String id, @retrofit2.http.Query("bucketID") String bucketID, @retrofit2.http.Query("default") Boolean _default, @retrofit2.http.Query("db") String db, @retrofit2.http.Query("rp") String rp
  );

  /**
   * Retrieve a database retention policy mapping
   * 
   * @param dbrpID The database retention policy mapping ID (required)
   * @param orgID Specifies the organization ID of the mapping (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;DBRP&gt;
   */
  @GET("api/v2/dbrps/{dbrpID}")
  Call<DBRP> getDBRPsID(
    @retrofit2.http.Path("dbrpID") String dbrpID, @retrofit2.http.Query("orgID") String orgID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Update a database retention policy mapping
   * 
   * @param dbrpID The database retention policy mapping. (required)
   * @param orgID Specifies the organization ID of the mapping (required)
   * @param dbRPUpdate Database retention policy update to apply (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;DBRP&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @PATCH("api/v2/dbrps/{dbrpID}")
  Call<DBRP> patchDBRPID(
    @retrofit2.http.Path("dbrpID") String dbrpID, @retrofit2.http.Query("orgID") String orgID, @retrofit2.http.Body DBRPUpdate dbRPUpdate, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Add a database retention policy mapping
   * 
   * @param DBRP The database retention policy mapping to add (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;DBRP&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/dbrps")
  Call<DBRP> postDBRP(
    @retrofit2.http.Body DBRP DBRP, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

}
