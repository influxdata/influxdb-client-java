package com.influxdb.client.service;

import retrofit2.Call;
import retrofit2.http.*;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.MultipartBody;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface PingService {
  /**
   * Checks the status of InfluxDB instance and version of InfluxDB.
   * 
   * @return Call&lt;ResponseBody&gt;
   */
  @GET("ping")
  Call<ResponseBody> getPing();
    

  /**
   * Checks the status of InfluxDB instance and version of InfluxDB.
   * 
   * @return Call&lt;ResponseBody&gt;
   */
  @HEAD("ping")
  Call<ResponseBody> headPing();
    

}
