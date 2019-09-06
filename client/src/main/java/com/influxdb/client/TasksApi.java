/*
 * The MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.influxdb.client;

import java.time.OffsetDateTime;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.influxdb.client.domain.Label;
import com.influxdb.client.domain.LabelResponse;
import com.influxdb.client.domain.LogEvent;
import com.influxdb.client.domain.Organization;
import com.influxdb.client.domain.ResourceMember;
import com.influxdb.client.domain.ResourceOwner;
import com.influxdb.client.domain.Run;
import com.influxdb.client.domain.RunManually;
import com.influxdb.client.domain.Task;
import com.influxdb.client.domain.TaskCreateRequest;
import com.influxdb.client.domain.TaskUpdateRequest;
import com.influxdb.client.domain.User;

/**
 * The client of the InfluxDB 2.0 that implement Task HTTP API endpoint.
 *
 * @author Jakub Bednar (bednar@github) (11/09/2018 07:54)
 */
public interface TasksApi {

    /**
     * Creates a new task. The {@link Task#getFlux()} has to have defined a cron or a every repetition
     * by the <a href="http://bit.ly/option-statement">option statement</a>.
     * <p>
     * Flux example:
     * <pre>
     * option task = {
     *     name: "mean",
     *     every: 1h,
     * }
     *
     * from(bucket:"metrics/autogen")
     *     |&gt; range(start:-task.every)
     *     |&gt; group(columns:["level"])
     *     |&gt; mean()
     *     |&gt; yield(name:"mean")
     * </pre>
     *
     * @param task the task to create
     * @return Task created
     */
    @Nonnull
    Task createTask(@Nonnull final Task task);

    /**
     * Create a new task.
     *
     * @param taskCreateRequest task to create (required)
     * @return Task created
     */
    @Nonnull
    Task createTask(@Nonnull final TaskCreateRequest taskCreateRequest);

    /**
     * Creates a new task with task repetition by cron.
     * The {@link Task#getFlux()} is without a cron or a every repetition.
     * The repetition is automatically append to the <a href="http://bit.ly/option-statement">option statement</a>.
     *
     * @param name         description of the task
     * @param flux         the Flux script to run for this task
     * @param cron         a task repetition schedule in the form '* * * * * *'
     * @param organization the organization that owns this Task
     * @return Task created
     */
    @Nonnull
    Task createTaskCron(@Nonnull final String name,
                        @Nonnull final String flux,
                        @Nonnull final String cron,
                        @Nonnull final Organization organization);

    /**
     * Creates a new task with task repetition by cron.
     * The {@link Task#getFlux()} is without a cron or a every repetition.
     * The repetition is automatically append to the <a href="http://bit.ly/option-statement">option statement</a>.
     *
     * @param name  description of the task
     * @param flux  the Flux script to run for this task
     * @param cron  a task repetition schedule in the form '* * * * * *'
     * @param orgID an id of the organization that owns this Task
     * @return Task created
     */
    @Nonnull
    Task createTaskCron(@Nonnull final String name,
                        @Nonnull final String flux,
                        @Nonnull final String cron,
                        @Nonnull final String orgID);

    /**
     * Creates a new task with task repetition by duration expression ("1h", "30s").
     * The {@link Task#getFlux()} is without a cron or a every repetition. The repetition is automatically append
     * to the <a href="http://bit.ly/option-statement">option statement</a>.
     *
     * @param name         description of the task
     * @param flux         the Flux script to run for this task
     * @param every        a task repetition by duration expression
     * @param organization the organization that owns this Task
     * @return Task created
     */
    @Nonnull
    Task createTaskEvery(@Nonnull final String name,
                         @Nonnull final String flux,
                         @Nonnull final String every,
                         @Nonnull final Organization organization);

    /**
     * Creates a new task with task repetition by duration expression ("1h", "30s").
     * The {@link Task#getFlux()} is without a cron or a every repetition. The repetition is automatically append
     * to the <a href="http://bit.ly/option-statement">option statement</a>.
     *
     * @param name  description of the task
     * @param flux  the Flux script to run for this task
     * @param every a task repetition by duration expression
     * @param orgID an id of the organization that owns this Task
     * @return Task created
     */
    @Nonnull
    Task createTaskEvery(@Nonnull final String name,
                         @Nonnull final String flux,
                         @Nonnull final String every,
                         @Nonnull final String orgID);

    /**
     * Update a task. This will cancel all queued runs.
     *
     * @param task task update to apply
     * @return task updated
     */
    @Nonnull
    Task updateTask(@Nonnull final Task task);

    /**
     * Update a task. This will cancel all queued runs.
     *
     * @param taskID  ID of task to get
     * @param request task update to apply
     * @return task updated
     */
    @Nonnull
    Task updateTask(@Nonnull String taskID, @Nonnull TaskUpdateRequest request);

    /**
     * Delete a task. Deletes a task and all associated records.
     *
     * @param task task to delete
     */
    void deleteTask(@Nonnull final Task task);

    /**
     * Delete a task. Deletes a task and all associated records.
     *
     * @param taskID ID of task to delete
     */
    void deleteTask(@Nonnull final String taskID);

    /**
     * Clone a task.
     *
     * @param taskID ID of task to clone
     * @return cloned task
     */
    @Nonnull
    Task cloneTask(@Nonnull final String taskID);

    /**
     * Clone a task.
     *
     * @param task task to clone
     * @return cloned task
     */
    @Nonnull
    Task cloneTask(@Nonnull final Task task);

    /**
     * Retrieve an task.
     *
     * @param taskID ID of task to get
     * @return task details
     */
    @Nonnull
    Task findTaskByID(@Nonnull final String taskID);

    /**
     * Lists tasks, limit 100.
     *
     * @return A list of tasks
     */
    @Nonnull
    List<Task> findTasks();

    /**
     * Lists tasks, limit 100.
     *
     * @param user filter tasks to a specific user
     * @return A list of tasks
     */
    @Nonnull
    List<Task> findTasksByUser(@Nonnull final User user);

    /**
     * Lists tasks, limit 100.
     *
     * @param userID filter tasks to a specific user id
     * @return A list of tasks
     */
    @Nonnull
    List<Task> findTasksByUserID(@Nullable final String userID);

    /**
     * Lists tasks, limit 100.
     *
     * @param organization filter tasks to a specific organization
     * @return A list of tasks
     */
    @Nonnull
    List<Task> findTasksByOrganization(@Nonnull final Organization organization);

    /**
     * Lists tasks, limit 100.
     *
     * @param orgID filter tasks to a specific organization id
     * @return A list of tasks
     */
    @Nonnull
    List<Task> findTasksByOrganizationID(@Nullable final String orgID);

    /**
     * Lists tasks, limit 100.
     *
     * @param afterID returns tasks after specified ID
     * @param userID  filter tasks to a specific user id
     * @param orgID   filter tasks to a specific organization id
     * @return A list of tasks
     */
    @Nonnull
    List<Task> findTasks(@Nullable final String afterID,
                         @Nullable final String userID,
                         @Nullable final String orgID);

    /**
     * List all task members.
     *
     * @param taskID ID of the task
     * @return return the list all task members
     */
    @Nonnull
    List<ResourceMember> getMembers(@Nonnull final String taskID);

    /**
     * List all task members.
     *
     * @param task the task with members
     * @return return the list all task members
     */
    @Nonnull
    List<ResourceMember> getMembers(@Nonnull final Task task);

    /**
     * Add task member.
     *
     * @param member the member of an task
     * @param task   the task for the member
     * @return created mapping
     */
    @Nonnull
    ResourceMember addMember(@Nonnull final User member, @Nonnull final Task task);

    /**
     * Add task member.
     *
     * @param memberID the ID of a member
     * @param taskID   the ID of a task
     * @return created mapping
     */
    @Nonnull
    ResourceMember addMember(@Nonnull final String memberID, @Nonnull final String taskID);

    /**
     * Removes a member from an task.
     *
     * @param member the member of a task
     * @param task   the task
     */
    void deleteMember(@Nonnull final User member, @Nonnull final Task task);

    /**
     * Removes a member from an task.
     *
     * @param taskID   the ID of a task
     * @param memberID the ID of a member
     */
    void deleteMember(@Nonnull final String memberID, @Nonnull final String taskID);

    /**
     * List all task owners.
     *
     * @param task the task with owners
     * @return return List all task owners
     */
    @Nonnull
    List<ResourceOwner> getOwners(@Nonnull final Task task);

    /**
     * List all task owners.
     *
     * @param taskID ID of task to get owners
     * @return return List all task owners
     */
    @Nonnull
    List<ResourceOwner> getOwners(@Nonnull final String taskID);

    /**
     * Add task owner.
     *
     * @param owner the owner of a task
     * @param task  the task
     * @return created mapping
     */
    @Nonnull
    ResourceOwner addOwner(@Nonnull final User owner, @Nonnull final Task task);

    /**
     * Add task owner.
     *
     * @param taskID  the ID of a task
     * @param ownerID the ID of a owner
     * @return created mapping
     */
    @Nonnull
    ResourceOwner addOwner(@Nonnull final String ownerID, @Nonnull final String taskID);

    /**
     * Removes an owner from an task.
     *
     * @param owner the owner of a task
     * @param task  the task
     */
    void deleteOwner(@Nonnull final User owner, @Nonnull final Task task);

    /**
     * Removes an owner from an task.
     *
     * @param taskID  the ID of a task
     * @param ownerID the ID of a owner
     */
    void deleteOwner(@Nonnull final String ownerID, @Nonnull final String taskID);

    /**
     * Retrieve list of run records for a task.
     *
     * @param task task to get runs for
     * @return the list of run records for a task
     */
    @Nonnull
    List<Run> getRuns(@Nonnull final Task task);

    /**
     * Retrieve list of run records for a task.
     *
     * @param task       task to get runs for
     * @param afterTime  filter runs to those scheduled after this time
     * @param beforeTime filter runs to those scheduled before this time
     * @param limit      the number of runs to return. Default value: 20.
     * @return the list of run records for a task
     */
    @Nonnull
    List<Run> getRuns(@Nonnull final Task task,
                      @Nullable final OffsetDateTime afterTime,
                      @Nullable final OffsetDateTime beforeTime,
                      @Nullable final Integer limit);

    /**
     * Retrieve list of run records for a task.
     *
     * @param taskID ID of task to get runs for
     *
     * @return the list of run records for a task
     */
    @Nonnull
    List<Run> getRuns(@Nonnull final String taskID);

    /**
     * Retrieve list of run records for a task.
     *
     * @param taskID     ID of task to get runs for
     * @param afterTime  filter runs to those scheduled after this time
     * @param beforeTime filter runs to those scheduled before this time
     * @param limit      the number of runs to return. Default value: 20.
     * @return the list of run records for a task
     */
    @Nonnull
    List<Run> getRuns(@Nonnull final String taskID,
                      @Nullable final OffsetDateTime afterTime,
                      @Nullable final OffsetDateTime beforeTime,
                      @Nullable final Integer limit);

    /**
     * Retrieve a single run record for a task.
     *
     * @param run the run with a taskID and a runID
     * @return a single run record for a task
     */
    @Nonnull
    Run getRun(@Nonnull final Run run);

    /**
     * Retrieve a single run record for a task.
     *
     * @param taskID ID of task to get runs for
     * @param runID  ID of run
     * @return a single run record for a task
     */
    @Nonnull
    Run getRun(@Nonnull final String taskID, @Nonnull final String runID);

    /**
     * Retrieve all logs for a run.
     *
     * @param run the run with a taskID and a runID
     * @return the list of all logs for a run
     */
    @Nonnull
    List<LogEvent> getRunLogs(@Nonnull final Run run);

    /**
     * Retrieve all logs for a run.
     *
     * @param taskID ID of task to get logs for it
     * @param runID  ID of run
     * @return the list of all logs for a run
     */
    @Nonnull
    List<LogEvent> getRunLogs(@Nonnull final String taskID, @Nonnull final String runID);

    /**
     * Manually start a run of the task now overriding the current schedule.
     *
     * @param task the task to run
     * @return Run scheduled to start
     */
    @Nonnull
    Run runManually(@Nonnull final Task task);

    /**
     * Manually start a run of the task now overriding the current schedule.
     *
     * @param taskId      ID of task to run
     * @param runManually to specify time
     * @return Run scheduled to start
     */
    @Nonnull
    Run runManually(@Nonnull final String taskId, @Nonnull final RunManually runManually);

    /**
     * Retry a task run.
     *
     * @param run the run with a taskID and a runID to retry
     * @return the executed run
     */
    @Nonnull
    Run retryRun(@Nonnull final Run run);

    /**
     * Retry a task run.
     *
     * @param taskID ID of task to get runs for
     * @param runID  ID of run
     * @return the executed run
     */
    @Nonnull
    Run retryRun(@Nonnull final String taskID, @Nonnull final String runID);

    /**
     * Cancels a currently running run.
     *
     * @param run the run with a taskID and a runID to cancel
     */
    void cancelRun(@Nonnull final Run run);

    /**
     * Cancels a currently running run.
     *
     * @param taskID ID of task
     * @param runID  ID of run
     */
    void cancelRun(@Nonnull final String taskID, @Nonnull final String runID);

    /**
     * Retrieve all logs for a task.
     *
     * @param task task to get logs for
     * @return the list of all logs for a task
     */
    @Nonnull
    List<LogEvent> getLogs(@Nonnull final Task task);

    /**
     * Retrieve all logs for a task.
     *
     * @param taskID ID of task to get logs for
     * @return the list of all logs for a task
     */
    @Nonnull
    List<LogEvent> getLogs(@Nonnull final String taskID);

    /**
     * List all labels of a Task.
     *
     * @param task the Task with labels
     * @return return List all labels of a Task.
     */
    @Nonnull
    List<Label> getLabels(@Nonnull final Task task);

    /**
     * List all labels of a Task.
     *
     * @param taskID ID of Task to get labels
     * @return return List all labels of a Task
     */
    @Nonnull
    List<Label> getLabels(@Nonnull final String taskID);

    /**
     * Add the Task label.
     *
     * @param label the label of a Task
     * @param task  the bucket
     * @return added label
     */
    @Nonnull
    LabelResponse addLabel(@Nonnull final Label label, @Nonnull final Task task);

    /**
     * Add the Task label.
     *
     * @param taskID  the ID of a Task
     * @param labelID the ID of a label
     * @return added label
     */
    @Nonnull
    LabelResponse addLabel(@Nonnull final String labelID, @Nonnull final String taskID);

    /**
     * Removes a label from a Task.
     *
     * @param label the Label of a Task
     * @param task  the Task
     */
    void deleteLabel(@Nonnull final Label label, @Nonnull final Task task);

    /**
     * Removes a label from a bucket.
     *
     * @param taskID  the ID of a task
     * @param labelID the ID of a label
     */
    void deleteLabel(@Nonnull final String labelID, @Nonnull final String taskID);
}