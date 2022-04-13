package com.influxdb.client.service;

import retrofit2.Call;
import retrofit2.http.*;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.MultipartBody;

import com.influxdb.client.domain.Error;
import com.influxdb.client.domain.LineProtocolError;
import com.influxdb.client.domain.LineProtocolLengthError;
import com.influxdb.client.domain.WriteConsistency;
import com.influxdb.client.domain.WritePrecision;
import io.reactivex.rxjava3.core.Single;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface WriteService {
  /**
   * Write data
   * Writes data to a bucket.  To write data into InfluxDB, you need the following:  - **organization name or ID** – _See [View organizations](https://docs.influxdata.com/influxdb/v2.1/organizations/view-orgs/#view-your-organization-id) for instructions on viewing your organization ID._ - **bucket** – _See [View buckets](https://docs.influxdata.com/influxdb/v2.1/organizations/buckets/view-buckets/) for  instructions on viewing your bucket ID._ - **API token** – _See [View tokens](https://docs.influxdata.com/influxdb/v2.1/security/tokens/view-tokens/)  for instructions on viewing your API token._ - **InfluxDB URL** – _See [InfluxDB URLs](https://docs.influxdata.com/influxdb/v2.1/reference/urls/)_. - data in [line protocol](https://docs.influxdata.com/influxdb/v2.1/reference/syntax/line-protocol) format.  InfluxDB Cloud enforces rate and size limits different from InfluxDB OSS. For details, see Responses.  For more information and examples, see the following: - [Write data with the InfluxDB API](https://docs.influxdata.com/influxdb/v2.1/write-data/developer-tools/api). - [Optimize writes to InfluxDB](https://docs.influxdata.com/influxdb/v2.1/write-data/best-practices/optimize-writes/). - [Troubleshoot issues writing data](https://docs.influxdata.com/influxdb/v2.1/write-data/troubleshoot/)
   * @param org Destination organization for writes. The database writes all points in the batch to this organization. If you provide both &#x60;orgID&#x60; and &#x60;org&#x60; parameters, &#x60;org&#x60; takes precedence. (required)
   * @param bucket Destination bucket for writes. (required)
   * @param body Data in line protocol format. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param contentEncoding The value tells InfluxDB what compression is applied to the line protocol in the request payload. To make an API request with a GZIP payload, send &#x60;Content-Encoding: gzip&#x60; as a request header. (optional, default to identity)
   * @param contentType Format of the data in the request body. To make an API request with a line protocol payload, send &#x60;Content-Type: text/plain; charset&#x3D;utf-8&#x60; as a request header. (optional, default to text/plain; charset&#x3D;utf-8)
   * @param contentLength Size of the entity-body, in bytes, sent to the database. If the length is greater than the database&#39;s &#x60;max body&#x60; configuration option, the server responds with status code &#x60;413&#x60;. (optional)
   * @param accept Content type that the client can understand. Writes only return a response body if they fail, e.g. due to a formatting problem or quota limit. #### InfluxDB Cloud   - returns only &#x60;application/json&#x60; for format and limit errors.   - returns only &#x60;text/html&#x60; for some quota limit errors.   #### InfluxDB OSS   - returns only &#x60;application/json&#x60; for format and limit errors.   For more information about write errors, see how to [troubleshoot issues writing data](https://docs.influxdata.com/influxdb/v2.1/write-data/troubleshoot/). (optional, default to application/json)
   * @param orgID ID of the destination organization for writes. If both &#x60;orgID&#x60; and &#x60;org&#x60; are specified, &#x60;org&#x60; takes precedence. (optional)
   * @param precision Precision for unix timestamps in the line protocol of the request payload. (optional, default to null)
   * @param consistency Sets the write consistency for the point. InfluxDB assumes that the write consistency is &#39;one&#39; if you do not specify. Available with InfluxDB Enterprise clusters only. (optional, default to null)
   * @return Call&lt;Void&gt;
   */
  @Headers({
    "Content-Type:text/plain"
  })
  @POST("api/v2/write")
  Call<Void> postWrite(
    @retrofit2.http.Query("org") String org, @retrofit2.http.Query("bucket") String bucket, @retrofit2.http.Body String body, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Header("Content-Encoding") String contentEncoding, @retrofit2.http.Header("Content-Type") String contentType, @retrofit2.http.Header("Content-Length") Integer contentLength, @retrofit2.http.Header("Accept") String accept, @retrofit2.http.Query("orgID") String orgID, @retrofit2.http.Query("precision") WritePrecision precision, @retrofit2.http.Query("consistency") WriteConsistency consistency
  );

  /**
   * Write data
   * Writes data to a bucket.  To write data into InfluxDB, you need the following:  - **organization name or ID** – _See [View organizations](https://docs.influxdata.com/influxdb/v2.1/organizations/view-orgs/#view-your-organization-id) for instructions on viewing your organization ID._ - **bucket** – _See [View buckets](https://docs.influxdata.com/influxdb/v2.1/organizations/buckets/view-buckets/) for  instructions on viewing your bucket ID._ - **API token** – _See [View tokens](https://docs.influxdata.com/influxdb/v2.1/security/tokens/view-tokens/)  for instructions on viewing your API token._ - **InfluxDB URL** – _See [InfluxDB URLs](https://docs.influxdata.com/influxdb/v2.1/reference/urls/)_. - data in [line protocol](https://docs.influxdata.com/influxdb/v2.1/reference/syntax/line-protocol) format.  InfluxDB Cloud enforces rate and size limits different from InfluxDB OSS. For details, see Responses.  For more information and examples, see the following: - [Write data with the InfluxDB API](https://docs.influxdata.com/influxdb/v2.1/write-data/developer-tools/api). - [Optimize writes to InfluxDB](https://docs.influxdata.com/influxdb/v2.1/write-data/best-practices/optimize-writes/). - [Troubleshoot issues writing data](https://docs.influxdata.com/influxdb/v2.1/write-data/troubleshoot/)
   * @param org Destination organization for writes. The database writes all points in the batch to this organization. If you provide both &#x60;orgID&#x60; and &#x60;org&#x60; parameters, &#x60;org&#x60; takes precedence. (required)
   * @param bucket Destination bucket for writes. (required)
   * @param body Data in line protocol format. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param contentEncoding The value tells InfluxDB what compression is applied to the line protocol in the request payload. To make an API request with a GZIP payload, send &#x60;Content-Encoding: gzip&#x60; as a request header. (optional, default to identity)
   * @param contentType Format of the data in the request body. To make an API request with a line protocol payload, send &#x60;Content-Type: text/plain; charset&#x3D;utf-8&#x60; as a request header. (optional, default to text/plain; charset&#x3D;utf-8)
   * @param contentLength Size of the entity-body, in bytes, sent to the database. If the length is greater than the database&#39;s &#x60;max body&#x60; configuration option, the server responds with status code &#x60;413&#x60;. (optional)
   * @param accept Content type that the client can understand. Writes only return a response body if they fail, e.g. due to a formatting problem or quota limit. #### InfluxDB Cloud   - returns only &#x60;application/json&#x60; for format and limit errors.   - returns only &#x60;text/html&#x60; for some quota limit errors.   #### InfluxDB OSS   - returns only &#x60;application/json&#x60; for format and limit errors.   For more information about write errors, see how to [troubleshoot issues writing data](https://docs.influxdata.com/influxdb/v2.1/write-data/troubleshoot/). (optional, default to application/json)
   * @param orgID ID of the destination organization for writes. If both &#x60;orgID&#x60; and &#x60;org&#x60; are specified, &#x60;org&#x60; takes precedence. (optional)
   * @param precision Precision for unix timestamps in the line protocol of the request payload. (optional, default to null)
   * @param consistency Sets the write consistency for the point. InfluxDB assumes that the write consistency is &#39;one&#39; if you do not specify. Available with InfluxDB Enterprise clusters only. (optional, default to null)
   * @return Single&lt;Response&lt;Void&gt;&gt;
   */
  @POST("api/v2/write")
  Single<Response<Void>> postWriteRx(
    @retrofit2.http.Query("org") String org, @retrofit2.http.Query("bucket") String bucket, @retrofit2.http.Body String body, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Header("Content-Encoding") String contentEncoding, @retrofit2.http.Header("Content-Type") String contentType, @retrofit2.http.Header("Content-Length") Integer contentLength, @retrofit2.http.Header("Accept") String accept, @retrofit2.http.Query("orgID") String orgID, @retrofit2.http.Query("precision") WritePrecision precision, @retrofit2.http.Query("consistency") WriteConsistency consistency
  );

}
