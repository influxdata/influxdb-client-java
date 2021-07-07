package com.influxdb.client.service;

import retrofit2.Call;
import retrofit2.http.*;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.MultipartBody;

import com.influxdb.client.domain.Error;
import com.influxdb.client.domain.TelegrafPlugins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface TelegrafPluginsService {
  /**
   * List all Telegraf plugins
   * 
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param type The type of plugin desired. (optional)
   * @return Call&lt;TelegrafPlugins&gt;
   */
  @GET("api/v2/telegraf/plugins")
  Call<TelegrafPlugins> getTelegrafPlugins(
    @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Query("type") String type
  );

}
