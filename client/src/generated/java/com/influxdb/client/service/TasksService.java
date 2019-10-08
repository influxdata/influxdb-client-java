package com.influxdb.client.service;

import retrofit2.Call;
import retrofit2.http.*;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.MultipartBody;

import com.influxdb.client.domain.AddResourceMemberRequestBody;
import com.influxdb.client.domain.Error;
import com.influxdb.client.domain.LabelMapping;
import com.influxdb.client.domain.LabelResponse;
import com.influxdb.client.domain.LabelsResponse;
import com.influxdb.client.domain.Logs;
import java.time.OffsetDateTime;
import com.influxdb.client.domain.ResourceMember;
import com.influxdb.client.domain.ResourceMembers;
import com.influxdb.client.domain.ResourceOwner;
import com.influxdb.client.domain.ResourceOwners;
import com.influxdb.client.domain.Run;
import com.influxdb.client.domain.RunManually;
import com.influxdb.client.domain.Runs;
import com.influxdb.client.domain.Task;
import com.influxdb.client.domain.TaskCreateRequest;
import com.influxdb.client.domain.TaskUpdateRequest;
import com.influxdb.client.domain.Tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface TasksService {
  /**
   * Delete a task
   * Deletes a task and all associated records
   * @param taskID The ID of the task to delete. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("api/v2/tasks/{taskID}")
  Call<Void> deleteTasksID(
    @retrofit2.http.Path("taskID") String taskID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Delete a label from a task
   * 
   * @param taskID The task ID. (required)
   * @param labelID The label ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("api/v2/tasks/{taskID}/labels/{labelID}")
  Call<Void> deleteTasksIDLabelsID(
    @retrofit2.http.Path("taskID") String taskID, @retrofit2.http.Path("labelID") String labelID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Remove a member from a task
   * 
   * @param userID The ID of the member to remove. (required)
   * @param taskID The task ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("api/v2/tasks/{taskID}/members/{userID}")
  Call<Void> deleteTasksIDMembersID(
    @retrofit2.http.Path("userID") String userID, @retrofit2.http.Path("taskID") String taskID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Remove an owner from a task
   * 
   * @param userID The ID of the owner to remove. (required)
   * @param taskID The task ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("api/v2/tasks/{taskID}/owners/{userID}")
  Call<Void> deleteTasksIDOwnersID(
    @retrofit2.http.Path("userID") String userID, @retrofit2.http.Path("taskID") String taskID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Cancel a running task
   * 
   * @param taskID The task ID. (required)
   * @param runID The run ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("api/v2/tasks/{taskID}/runs/{runID}")
  Call<Void> deleteTasksIDRunsID(
    @retrofit2.http.Path("taskID") String taskID, @retrofit2.http.Path("runID") String runID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * List all tasks
   * 
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param name Returns task with a specific name. (optional)
   * @param after Return tasks after a specified ID. (optional)
   * @param user Filter tasks to a specific user ID. (optional)
   * @param org Filter tasks to a specific organization name. (optional)
   * @param orgID Filter tasks to a specific organization ID. (optional)
   * @param limit The number of tasks to return (optional, default to 100)
   * @return Call&lt;Tasks&gt;
   */
  @GET("api/v2/tasks")
  Call<Tasks> getTasks(
    @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Query("name") String name, @retrofit2.http.Query("after") String after, @retrofit2.http.Query("user") String user, @retrofit2.http.Query("org") String org, @retrofit2.http.Query("orgID") String orgID, @retrofit2.http.Query("limit") Integer limit
  );

  /**
   * Retrieve a task
   * 
   * @param taskID The task ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Task&gt;
   */
  @GET("api/v2/tasks/{taskID}")
  Call<Task> getTasksID(
    @retrofit2.http.Path("taskID") String taskID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * List all labels for a task
   * 
   * @param taskID The task ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;LabelsResponse&gt;
   */
  @GET("api/v2/tasks/{taskID}/labels")
  Call<LabelsResponse> getTasksIDLabels(
    @retrofit2.http.Path("taskID") String taskID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Retrieve all logs for a task
   * 
   * @param taskID The task ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Logs&gt;
   */
  @GET("api/v2/tasks/{taskID}/logs")
  Call<Logs> getTasksIDLogs(
    @retrofit2.http.Path("taskID") String taskID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * List all task members
   * 
   * @param taskID The task ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;ResourceMembers&gt;
   */
  @GET("api/v2/tasks/{taskID}/members")
  Call<ResourceMembers> getTasksIDMembers(
    @retrofit2.http.Path("taskID") String taskID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * List all owners of a task
   * 
   * @param taskID The task ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;ResourceOwners&gt;
   */
  @GET("api/v2/tasks/{taskID}/owners")
  Call<ResourceOwners> getTasksIDOwners(
    @retrofit2.http.Path("taskID") String taskID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * List runs for a task
   * 
   * @param taskID The ID of the task to get runs for. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param after Returns runs after a specific ID. (optional)
   * @param limit The number of runs to return (optional, default to 100)
   * @param afterTime Filter runs to those scheduled after this time, RFC3339 (optional)
   * @param beforeTime Filter runs to those scheduled before this time, RFC3339 (optional)
   * @return Call&lt;Runs&gt;
   */
  @GET("api/v2/tasks/{taskID}/runs")
  Call<Runs> getTasksIDRuns(
    @retrofit2.http.Path("taskID") String taskID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Query("after") String after, @retrofit2.http.Query("limit") Integer limit, @retrofit2.http.Query("afterTime") OffsetDateTime afterTime, @retrofit2.http.Query("beforeTime") OffsetDateTime beforeTime
  );

  /**
   * Retrieve a single run for a task
   * 
   * @param taskID The task ID. (required)
   * @param runID The run ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Run&gt;
   */
  @GET("api/v2/tasks/{taskID}/runs/{runID}")
  Call<Run> getTasksIDRunsID(
    @retrofit2.http.Path("taskID") String taskID, @retrofit2.http.Path("runID") String runID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Retrieve all logs for a run
   * 
   * @param taskID ID of task to get logs for. (required)
   * @param runID ID of run to get logs for. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Logs&gt;
   */
  @GET("api/v2/tasks/{taskID}/runs/{runID}/logs")
  Call<Logs> getTasksIDRunsIDLogs(
    @retrofit2.http.Path("taskID") String taskID, @retrofit2.http.Path("runID") String runID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Update a task
   * Update a task. This will cancel all queued runs.
   * @param taskID The task ID. (required)
   * @param taskUpdateRequest Task update to apply (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Task&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @PATCH("api/v2/tasks/{taskID}")
  Call<Task> patchTasksID(
    @retrofit2.http.Path("taskID") String taskID, @retrofit2.http.Body TaskUpdateRequest taskUpdateRequest, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Create a new task
   * 
   * @param taskCreateRequest Task to create (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Task&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/tasks")
  Call<Task> postTasks(
    @retrofit2.http.Body TaskCreateRequest taskCreateRequest, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Add a label to a task
   * 
   * @param taskID The task ID. (required)
   * @param labelMapping Label to add (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;LabelResponse&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/tasks/{taskID}/labels")
  Call<LabelResponse> postTasksIDLabels(
    @retrofit2.http.Path("taskID") String taskID, @retrofit2.http.Body LabelMapping labelMapping, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Add a member to a task
   * 
   * @param taskID The task ID. (required)
   * @param addResourceMemberRequestBody User to add as member (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;ResourceMember&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/tasks/{taskID}/members")
  Call<ResourceMember> postTasksIDMembers(
    @retrofit2.http.Path("taskID") String taskID, @retrofit2.http.Body AddResourceMemberRequestBody addResourceMemberRequestBody, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Add an owner to a task
   * 
   * @param taskID The task ID. (required)
   * @param addResourceMemberRequestBody User to add as owner (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;ResourceOwner&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/tasks/{taskID}/owners")
  Call<ResourceOwner> postTasksIDOwners(
    @retrofit2.http.Path("taskID") String taskID, @retrofit2.http.Body AddResourceMemberRequestBody addResourceMemberRequestBody, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Manually start a task run, overriding the current schedule
   * 
   * @param taskID  (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param runManually  (optional)
   * @return Call&lt;Run&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/tasks/{taskID}/runs")
  Call<Run> postTasksIDRuns(
    @retrofit2.http.Path("taskID") String taskID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Body RunManually runManually
  );

  /**
   * Retry a task run
   * 
   * @param taskID The task ID. (required)
   * @param runID The run ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Run&gt;
   */
  @POST("api/v2/tasks/{taskID}/runs/{runID}/retry")
  Call<Run> postTasksIDRunsIDRetry(
    @retrofit2.http.Path("taskID") String taskID, @retrofit2.http.Path("runID") String runID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

}
