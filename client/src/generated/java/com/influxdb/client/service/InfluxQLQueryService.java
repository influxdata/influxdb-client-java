package com.influxdb.client.service;

import javax.annotation.Nonnull;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface InfluxQLQueryService {

    /**
     * @param db              Bucket to query (required)
     * @param query           (required)
     * @param retentionPolicy Retention policy name (optional)
     * @param zapTraceSpan    OpenTracing span context (optional)
     * @return response in csv format
     */
    @Headers({"Accept:application/csv", "Content-Type:application/x-www-form-urlencoded"})
    @FormUrlEncoded
    @POST("query")
    Call<ResponseBody> query(
            @Field("q") String query,
            @Nonnull @Query("db") String db,
            @Query("rp") String retentionPolicy,
            @Query("epoch") String epoch,
            @Header("Zap-Trace-Span") String zapTraceSpan
    );
}
