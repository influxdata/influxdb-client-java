package com.influxdb.client.service;

import retrofit2.Call;
import retrofit2.http.*;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.MultipartBody;

import com.influxdb.client.domain.File;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface DebugService {
  /**
   * Get all runtime profiles
   * Get reports for the following [Go runtime profiles](https://pkg.go.dev/runtime/pprof):  - **allocs**: All past memory allocations - **block**: Stack traces that led to blocking on synchronization primitives - **cpu**: (Optional) Program counters sampled from the executing stack.   Include by passing the &#x60;cpu&#x60; query parameter with a [duration](https://docs.influxdata.com/influxdb/v2.1/reference/glossary/#duration) value.   Equivalent to the report from [&#x60;GET /debug/pprof/profile?seconds&#x3D;NUMBER_OF_SECONDS&#x60;](#operation/GetDebugPprofProfile). - **goroutine**: All current goroutines - **heap**: Memory allocations for live objects - **mutex**: Holders of contended mutexes - **threadcreate**: Stack traces that led to the creation of new OS threads
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param cpu Collects and returns CPU profiling data for the specified [duration](https://docs.influxdata.com/influxdb/v2.1/reference/glossary/#duration). (optional)
   * @return Call&lt;ResponseBody&gt;
   */
  @GET("debug/pprof/all")
  Call<ResponseBody> getDebugPprofAllProfiles(
    @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Query("cpu") String cpu
  );

  /**
   * Get the memory allocations runtime profile
   * Get a [Go runtime profile](https://pkg.go.dev/runtime/pprof) report of all past memory allocations. **allocs** is the same as the **heap** profile, but changes the default [pprof](https://pkg.go.dev/runtime/pprof) display to __-alloc_space__, the total number of bytes allocated since the program began (including garbage-collected bytes).
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param debug - &#x60;0&#x60;: (Default) Return the report as a gzip-compressed protocol buffer. - &#x60;1&#x60;: Return a response body with the report formatted as human-readable text.   The report contains comments that translate addresses to function names and line numbers for debugging.  &#x60;debug&#x3D;1&#x60; is mutually exclusive with the &#x60;seconds&#x60; query parameter. (optional)
   * @param seconds Number of seconds to collect statistics.  &#x60;seconds&#x60; is mutually exclusive with &#x60;debug&#x3D;1&#x60;. (optional)
   * @return Call&lt;ResponseBody&gt;
   */
  @GET("debug/pprof/allocs")
  Call<ResponseBody> getDebugPprofAllocs(
    @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Query("debug") Long debug, @retrofit2.http.Query("seconds") String seconds
  );

  /**
   * Get the block runtime profile
   * Get a [Go runtime profile](https://pkg.go.dev/runtime/pprof) report of stack traces that led to blocking on synchronization primitives.
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param debug - &#x60;0&#x60;: (Default) Return the report as a gzip-compressed protocol buffer. - &#x60;1&#x60;: Return a response body with the report formatted as human-readable text.   The report contains comments that translate addresses to function names and line numbers for debugging.  &#x60;debug&#x3D;1&#x60; is mutually exclusive with the &#x60;seconds&#x60; query parameter. (optional)
   * @param seconds Number of seconds to collect statistics.  &#x60;seconds&#x60; is mutually exclusive with &#x60;debug&#x3D;1&#x60;. (optional)
   * @return Call&lt;ResponseBody&gt;
   */
  @GET("debug/pprof/block")
  Call<ResponseBody> getDebugPprofBlock(
    @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Query("debug") Long debug, @retrofit2.http.Query("seconds") String seconds
  );

  /**
   * Get the command line invocation
   * Get the command line that invoked InfluxDB.
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;String&gt;
   */
  @GET("debug/pprof/cmdline")
  Call<String> getDebugPprofCmdline(
    @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Get the goroutines runtime profile
   * Get a [Go runtime profile](https://pkg.go.dev/runtime/pprof) report of all current goroutines.
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param debug - &#x60;0&#x60;: (Default) Return the report as a gzip-compressed protocol buffer. - &#x60;1&#x60;: Return a response body with the report formatted as human-readable text.   The report contains comments that translate addresses to function names and line numbers for debugging.  &#x60;debug&#x3D;1&#x60; is mutually exclusive with the &#x60;seconds&#x60; query parameter. (optional)
   * @param seconds Number of seconds to collect statistics.  &#x60;seconds&#x60; is mutually exclusive with &#x60;debug&#x3D;1&#x60;. (optional)
   * @return Call&lt;ResponseBody&gt;
   */
  @GET("debug/pprof/goroutine")
  Call<ResponseBody> getDebugPprofGoroutine(
    @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Query("debug") Long debug, @retrofit2.http.Query("seconds") String seconds
  );

  /**
   * Get the heap runtime profile
   * Get a [Go runtime profile](https://pkg.go.dev/runtime/pprof) report of memory allocations for live objects.  To run **garbage collection** before sampling, pass the &#x60;gc&#x60; query parameter with a value of &#x60;1&#x60;.
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param debug - &#x60;0&#x60;: (Default) Return the report as a gzip-compressed protocol buffer. - &#x60;1&#x60;: Return a response body with the report formatted as human-readable text.   The report contains comments that translate addresses to function names and line numbers for debugging.  &#x60;debug&#x3D;1&#x60; is mutually exclusive with the &#x60;seconds&#x60; query parameter. (optional)
   * @param seconds Number of seconds to collect statistics.  &#x60;seconds&#x60; is mutually exclusive with &#x60;debug&#x3D;1&#x60;. (optional)
   * @param gc - &#x60;0&#x60;: (Default) don&#39;t force garbage collection before sampling. - &#x60;1&#x60;: Force garbage collection before sampling. (optional)
   * @return Call&lt;ResponseBody&gt;
   */
  @GET("debug/pprof/heap")
  Call<ResponseBody> getDebugPprofHeap(
    @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Query("debug") Long debug, @retrofit2.http.Query("seconds") String seconds, @retrofit2.http.Query("gc") Long gc
  );

  /**
   * Get the mutual exclusion (mutex) runtime profile
   * Get a [Go runtime profile](https://pkg.go.dev/runtime/pprof) report of lock contentions. The profile contains stack traces of holders of contended mutual exclusions (mutexes).
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param debug - &#x60;0&#x60;: (Default) Return the report as a gzip-compressed protocol buffer. - &#x60;1&#x60;: Return a response body with the report formatted as human-readable text.   The report contains comments that translate addresses to function names and line numbers for debugging.  &#x60;debug&#x3D;1&#x60; is mutually exclusive with the &#x60;seconds&#x60; query parameter. (optional)
   * @param seconds Number of seconds to collect statistics.  &#x60;seconds&#x60; is mutually exclusive with &#x60;debug&#x3D;1&#x60;. (optional)
   * @return Call&lt;ResponseBody&gt;
   */
  @GET("debug/pprof/mutex")
  Call<ResponseBody> getDebugPprofMutex(
    @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Query("debug") Long debug, @retrofit2.http.Query("seconds") String seconds
  );

  /**
   * Get the CPU runtime profile
   * Get a [Go runtime profile](https://pkg.go.dev/runtime/pprof) report of program counters on the executing stack.
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param seconds Number of seconds to collect profile data. Default is &#x60;30&#x60; seconds. (optional)
   * @return Call&lt;ResponseBody&gt;
   */
  @GET("debug/pprof/profile")
  Call<ResponseBody> getDebugPprofProfile(
    @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Query("seconds") String seconds
  );

  /**
   * Get the threadcreate runtime profile
   * Get a [Go runtime profile](https://pkg.go.dev/runtime/pprof) report of stack traces that led to the creation of new OS threads.
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param debug - &#x60;0&#x60;: (Default) Return the report as a gzip-compressed protocol buffer. - &#x60;1&#x60;: Return a response body with the report formatted as human-readable text.   The report contains comments that translate addresses to function names and line numbers for debugging.  &#x60;debug&#x3D;1&#x60; is mutually exclusive with the &#x60;seconds&#x60; query parameter. (optional)
   * @param seconds Number of seconds to collect statistics.  &#x60;seconds&#x60; is mutually exclusive with &#x60;debug&#x3D;1&#x60;. (optional)
   * @return Call&lt;ResponseBody&gt;
   */
  @GET("debug/pprof/threadcreate")
  Call<ResponseBody> getDebugPprofThreadCreate(
    @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Query("debug") Long debug, @retrofit2.http.Query("seconds") String seconds
  );

  /**
   * Get the runtime execution trace
   * Trace execution events for the current program.
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param seconds Number of seconds to collect profile data. (optional)
   * @return Call&lt;ResponseBody&gt;
   */
  @GET("debug/pprof/trace")
  Call<ResponseBody> getDebugPprofTrace(
    @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Query("seconds") String seconds
  );

}
