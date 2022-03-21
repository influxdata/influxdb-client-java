package com.influxdb.client.service;

import retrofit2.Call;
import retrofit2.http.*;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.MultipartBody;

import com.influxdb.client.domain.Script;
import com.influxdb.client.domain.ScriptCreateRequest;
import com.influxdb.client.domain.ScriptInvocationParams;
import com.influxdb.client.domain.ScriptUpdateRequest;
import com.influxdb.client.domain.Scripts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface InvocableScriptsService {
  /**
   * Delete a script
   * Deletes a script and all associated records.
   * @param scriptID The ID of the script to delete. (required)
   * @return Call&lt;Void&gt;
   */
  @DELETE("api/v2/scripts/{scriptID}")
  Call<Void> deleteScriptsID(
    @retrofit2.http.Path("scriptID") String scriptID
  );

  /**
   * List scripts
   * 
   * @param limit The number of scripts to return. (optional)
   * @param offset The offset for pagination. (optional)
   * @return Call&lt;Scripts&gt;
   */
  @GET("api/v2/scripts")
  Call<Scripts> getScripts(
    @retrofit2.http.Query("limit") Integer limit, @retrofit2.http.Query("offset") Integer offset
  );

  /**
   * Retrieve a script
   * Uses script ID to retrieve details of an invocable script.
   * @param scriptID The script ID. (required)
   * @return Call&lt;Script&gt;
   */
  @GET("api/v2/scripts/{scriptID}")
  Call<Script> getScriptsID(
    @retrofit2.http.Path("scriptID") String scriptID
  );

  /**
   * Update a script
   * Updates properties (&#x60;name&#x60;, &#x60;description&#x60;, and &#x60;script&#x60;) of an invocable script.
   * @param scriptID The script ID. (required)
   * @param scriptUpdateRequest Script update to apply (required)
   * @return Call&lt;Script&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @PATCH("api/v2/scripts/{scriptID}")
  Call<Script> patchScriptsID(
    @retrofit2.http.Path("scriptID") String scriptID, @retrofit2.http.Body ScriptUpdateRequest scriptUpdateRequest
  );

  /**
   * Create a script
   * 
   * @param scriptCreateRequest The script to create. (required)
   * @return Call&lt;Script&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/scripts")
  Call<Script> postScripts(
    @retrofit2.http.Body ScriptCreateRequest scriptCreateRequest
  );

  /**
   * Invoke a script
   * Invokes a script and substitutes &#x60;params&#x60; keys referenced in the script with &#x60;params&#x60; key-values sent in the request body.
   * @param scriptID  (required)
   * @param scriptInvocationParams  (optional)
   * @return Call&lt;String&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/scripts/{scriptID}/invoke")
  Call<String> postScriptsIDInvoke(
    @retrofit2.http.Path("scriptID") String scriptID, @retrofit2.http.Body ScriptInvocationParams scriptInvocationParams
  );

}
