package com.influxdb.client.service;

import retrofit2.Call;
import retrofit2.http.*;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.MultipartBody;

import com.influxdb.client.domain.Error;
import com.influxdb.client.domain.Pkg;
import com.influxdb.client.domain.PkgApply;
import com.influxdb.client.domain.PkgCreate;
import com.influxdb.client.domain.PkgSummary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface InfluxPackagesService {
  /**
   * Apply or dry run an influx package
   * 
   * @param pkgApply  (required)
   * @return Call&lt;PkgSummary&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/packages/apply")
  Call<PkgSummary> applyPkg(
    @retrofit2.http.Body PkgApply pkgApply
  );

  /**
   * Create a new Influx package
   * 
   * @param pkgCreate Influx package to create (optional)
   * @return Call&lt;Pkg&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/packages")
  Call<Pkg> createPkg(
    @retrofit2.http.Body PkgCreate pkgCreate
  );

}
