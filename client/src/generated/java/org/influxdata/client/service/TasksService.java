package org.influxdata.client.service;

import retrofit2.Call;
import retrofit2.http.*;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.MultipartBody;

import org.influxdata.client.domain.AddResourceMemberRequestBody;
import org.influxdata.client.domain.Error;
import org.influxdata.client.domain.LabelMapping;
import org.influxdata.client.domain.LabelResponse;
import org.influxdata.client.domain.LabelsResponse;
import org.influxdata.client.domain.Logs;
import java.time.OffsetDateTime;
import org.influxdata.client.domain.ResourceMember;
import org.influxdata.client.domain.ResourceMembers;
import org.influxdata.client.domain.ResourceOwner;
import org.influxdata.client.domain.ResourceOwners;
import org.influxdata.client.domain.Run;
import org.influxdata.client.domain.RunManually;
import org.influxdata.client.domain.Runs;
import org.influxdata.client.domain.Task;
import org.influxdata.client.domain.TaskCreateRequest;
import org.influxdata.client.domain.TaskUpdateRequest;
import org.influxdata.client.domain.Tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface TasksService {
  /**
   * List tasks.
   * 
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param after returns tasks after specified ID (optional)
   * @param user filter tasks to a specific user ID (optional)
   * @param org filter tasks to a specific organization name (optional)
   * @param orgID filter tasks to a specific organization ID (optional)
   * @param limit the number of tasks to return (optional, default to 100)
   * @return Call&lt;Tasks&gt;
   */
  @GET("tasks")
  Call<Tasks> tasksGet(
    @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Query("after") String after, @retrofit2.http.Query("user") String user, @retrofit2.http.Query("org") String org, @retrofit2.http.Query("orgID") String orgID, @retrofit2.http.Query("limit") Integer limit
  );

  /**
   * Create a new task
   * 
   * @param taskCreateRequest task to create (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Task&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("tasks")
  Call<Task> tasksPost(
    @retrofit2.http.Body TaskCreateRequest taskCreateRequest, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Delete a task
   * Deletes a task and all associated records
   * @param taskID ID of task to delete (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("tasks/{taskID}")
  Call<Void> tasksTaskIDDelete(
    @retrofit2.http.Path("taskID") String taskID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Retrieve an task
   * 
   * @param taskID ID of task to get (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Task&gt;
   */
  @GET("tasks/{taskID}")
  Call<Task> tasksTaskIDGet(
    @retrofit2.http.Path("taskID") String taskID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * list all labels for a task
   * 
   * @param taskID ID of the task (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;LabelsResponse&gt;
   */
  @GET("tasks/{taskID}/labels")
  Call<LabelsResponse> tasksTaskIDLabelsGet(
    @retrofit2.http.Path("taskID") String taskID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * delete a label from a task
   * 
   * @param taskID ID of the task (required)
   * @param labelID the label id (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("tasks/{taskID}/labels/{labelID}")
  Call<Void> tasksTaskIDLabelsLabelIDDelete(
    @retrofit2.http.Path("taskID") String taskID, @retrofit2.http.Path("labelID") String labelID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * add a label to a task
   * 
   * @param taskID ID of the task (required)
   * @param labelMapping label to add (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;LabelResponse&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("tasks/{taskID}/labels")
  Call<LabelResponse> tasksTaskIDLabelsPost(
    @retrofit2.http.Path("taskID") String taskID, @retrofit2.http.Body LabelMapping labelMapping, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Retrieve all logs for a task
   * 
   * @param taskID ID of task to get logs for (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Logs&gt;
   */
  @GET("tasks/{taskID}/logs")
  Call<Logs> tasksTaskIDLogsGet(
    @retrofit2.http.Path("taskID") String taskID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * List all task members
   * 
   * @param taskID ID of the task (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;ResourceMembers&gt;
   */
  @GET("tasks/{taskID}/members")
  Call<ResourceMembers> tasksTaskIDMembersGet(
    @retrofit2.http.Path("taskID") String taskID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Add task member
   * 
   * @param taskID ID of the task (required)
   * @param addResourceMemberRequestBody user to add as member (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;ResourceMember&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("tasks/{taskID}/members")
  Call<ResourceMember> tasksTaskIDMembersPost(
    @retrofit2.http.Path("taskID") String taskID, @retrofit2.http.Body AddResourceMemberRequestBody addResourceMemberRequestBody, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * removes a member from an task
   * 
   * @param userID ID of member to remove (required)
   * @param taskID ID of the task (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("tasks/{taskID}/members/{userID}")
  Call<Void> tasksTaskIDMembersUserIDDelete(
    @retrofit2.http.Path("userID") String userID, @retrofit2.http.Path("taskID") String taskID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * List all task owners
   * 
   * @param taskID ID of the task (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;ResourceOwners&gt;
   */
  @GET("tasks/{taskID}/owners")
  Call<ResourceOwners> tasksTaskIDOwnersGet(
    @retrofit2.http.Path("taskID") String taskID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Add task owner
   * 
   * @param taskID ID of the task (required)
   * @param addResourceMemberRequestBody user to add as owner (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;ResourceOwner&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("tasks/{taskID}/owners")
  Call<ResourceOwner> tasksTaskIDOwnersPost(
    @retrofit2.http.Path("taskID") String taskID, @retrofit2.http.Body AddResourceMemberRequestBody addResourceMemberRequestBody, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * removes an owner from an task
   * 
   * @param userID ID of owner to remove (required)
   * @param taskID ID of the task (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("tasks/taskID}/owners/{userID}")
  Call<Void> tasksTaskIDOwnersUserIDDelete(
    @retrofit2.http.Path("userID") String userID, @retrofit2.http.Path("taskID") String taskID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Update a task
   * Update a task. This will cancel all queued runs.
   * @param taskID ID of task to get (required)
   * @param taskUpdateRequest task update to apply (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Task&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @PATCH("tasks/{taskID}")
  Call<Task> tasksTaskIDPatch(
    @retrofit2.http.Path("taskID") String taskID, @retrofit2.http.Body TaskUpdateRequest taskUpdateRequest, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Retrieve list of run records for a task
   * 
   * @param taskID ID of task to get runs for (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param after returns runs after specified ID (optional)
   * @param limit the number of runs to return (optional, default to 20)
   * @param afterTime filter runs to those scheduled after this time, RFC3339 (optional)
   * @param beforeTime filter runs to those scheduled before this time, RFC3339 (optional)
   * @return Call&lt;Runs&gt;
   */
  @GET("tasks/{taskID}/runs")
  Call<Runs> tasksTaskIDRunsGet(
    @retrofit2.http.Path("taskID") String taskID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Query("after") String after, @retrofit2.http.Query("limit") Integer limit, @retrofit2.http.Query("afterTime") OffsetDateTime afterTime, @retrofit2.http.Query("beforeTime") OffsetDateTime beforeTime
  );

  /**
   * manually start a run of the task now overriding the current schedule.
   * 
   * @param taskID  (required)
   * @param runManually  (optional)
   * @return Call&lt;Run&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("tasks/{taskID}/runs")
  Call<Run> tasksTaskIDRunsPost(
    @retrofit2.http.Path("taskID") String taskID, @retrofit2.http.Body RunManually runManually
  );

  /**
   * Cancel a run
   * cancels a currently running run.
   * @param taskID task ID (required)
   * @param runID run ID (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("tasks/{taskID}/runs/{runID}")
  Call<Void> tasksTaskIDRunsRunIDDelete(
    @retrofit2.http.Path("taskID") String taskID, @retrofit2.http.Path("runID") String runID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Retrieve a single run record for a task
   * 
   * @param taskID task ID (required)
   * @param runID run ID (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Run&gt;
   */
  @GET("tasks/{taskID}/runs/{runID}")
  Call<Run> tasksTaskIDRunsRunIDGet(
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
  @GET("tasks/{taskID}/runs/{runID}/logs")
  Call<Logs> tasksTaskIDRunsRunIDLogsGet(
    @retrofit2.http.Path("taskID") String taskID, @retrofit2.http.Path("runID") String runID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Retry a task run
   * 
   * @param taskID task ID (required)
   * @param runID run ID (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Run&gt;
   */
  @POST("tasks/{taskID}/runs/{runID}/retry")
  Call<Run> tasksTaskIDRunsRunIDRetryPost(
    @retrofit2.http.Path("taskID") String taskID, @retrofit2.http.Path("runID") String runID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

}
