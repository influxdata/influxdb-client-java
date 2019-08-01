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
package com.influxdb.client.internal;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.influxdb.Arguments;
import com.influxdb.client.TasksApi;
import com.influxdb.client.domain.AddResourceMemberRequestBody;
import com.influxdb.client.domain.Label;
import com.influxdb.client.domain.LabelMapping;
import com.influxdb.client.domain.LabelResponse;
import com.influxdb.client.domain.LabelsResponse;
import com.influxdb.client.domain.LogEvent;
import com.influxdb.client.domain.Logs;
import com.influxdb.client.domain.Organization;
import com.influxdb.client.domain.ResourceMember;
import com.influxdb.client.domain.ResourceMembers;
import com.influxdb.client.domain.ResourceOwner;
import com.influxdb.client.domain.ResourceOwners;
import com.influxdb.client.domain.Run;
import com.influxdb.client.domain.RunManually;
import com.influxdb.client.domain.Runs;
import com.influxdb.client.domain.Task;
import com.influxdb.client.domain.TaskCreateRequest;
import com.influxdb.client.domain.TaskStatusType;
import com.influxdb.client.domain.TaskUpdateRequest;
import com.influxdb.client.domain.Tasks;
import com.influxdb.client.domain.User;
import com.influxdb.client.service.TasksService;
import com.influxdb.exceptions.InternalServerErrorException;
import com.influxdb.internal.AbstractRestClient;

import retrofit2.Call;

/**
 * @author Jakub Bednar (bednar@github) (11/09/2018 07:59)
 */
final class TasksApiImpl extends AbstractRestClient implements TasksApi {

    private static final Logger LOG = Logger.getLogger(TasksApiImpl.class.getName());

    private final TasksService service;

    TasksApiImpl(@Nonnull final TasksService service) {

        Arguments.checkNotNull(service, "service");

        this.service = service;
    }

    @Nonnull
    @Override
    public Task findTaskByID(@Nonnull final String taskID) {

        Arguments.checkNonEmpty(taskID, "taskID");

        Call<Task> call = service.getTasksID(taskID, null);

        return execute(call);
    }

    @Nonnull
    @Override
    public List<Task> findTasks() {
        return findTasks(null, null, null);
    }

    @Nonnull
    @Override
    public List<Task> findTasksByUser(@Nonnull final User user) {

        Arguments.checkNotNull(user, "user");

        return findTasksByUserID(user.getId());
    }

    @Nonnull
    @Override
    public List<Task> findTasksByUserID(@Nullable final String userID) {

        return findTasks(null, userID, null);
    }

    @Nonnull
    @Override
    public List<Task> findTasksByOrganization(@Nonnull final Organization organization) {

        Arguments.checkNotNull(organization, "organization");

        return findTasksByOrganizationID(organization.getId());
    }

    @Nonnull
    @Override
    public List<Task> findTasksByOrganizationID(@Nullable final String orgID) {
        return findTasks(null, null, orgID);
    }

    @Nonnull
    @Override
    public List<Task> findTasks(@Nullable final String afterID,
                                @Nullable final String userID,
                                @Nullable final String orgID) {

        Call<Tasks> call = service.getTasks(null, afterID, userID, null, orgID, null);

        //TODO https://github.com/influxdata/influxdb/issues/13576
        Tasks tasks = execute(call, InternalServerErrorException.class, new Tasks());
        LOG.log(Level.FINEST, "findTasks found: {0}", tasks);

        return tasks.getTasks();
    }

    @Nonnull
    @Override
    public Task createTask(@Nonnull final Task task, @Nonnull final String token) {

        Arguments.checkNotNull(task, "task");
        Arguments.checkNonEmpty(token, "token");

        TaskCreateRequest request = new TaskCreateRequest();
        request.setFlux(task.getFlux());
        request.setOrgID(task.getOrgID());
        request.setOrg(task.getOrg());
        request.setDescription(task.getDescription());
        request.setToken(token);

        if (task.getStatus() != null) {
            request.setStatus(TaskStatusType.fromValue(task.getStatus().getValue()));
        }

        return createTask(request);
    }

    @Nonnull
    @Override
    public Task createTask(@Nonnull final TaskCreateRequest taskCreateRequest) {

        Arguments.checkNotNull(taskCreateRequest, "taskCreateRequest");

        Call<Task> call = service.postTasks(taskCreateRequest, null);

        return execute(call);
    }

    @Nonnull
    @Override
    public Task createTaskCron(@Nonnull final String name,
                               @Nonnull final String flux,
                               @Nonnull final String cron,
                               @Nonnull final Organization organization,
                               @Nonnull final String token) {

        Arguments.checkNonEmpty(name, "name of the task");
        Arguments.checkNonEmpty(flux, "Flux script to run");
        Arguments.checkNonEmpty(cron, "cron expression");
        Arguments.checkNotNull(organization, "organization");
        Arguments.checkNonEmpty(token, "token");

        Task task = createTask(name, flux, null, cron, organization.getId());

        return createTask(task, token);
    }

    @Nonnull
    @Override
    public Task createTaskCron(@Nonnull final String name,
                               @Nonnull final String flux,
                               @Nonnull final String cron,
                               @Nonnull final String orgID,
                               @Nonnull final String token) {

        Arguments.checkNonEmpty(name, "name of the task");
        Arguments.checkNonEmpty(flux, "Flux script to run");
        Arguments.checkNonEmpty(cron, "cron expression");
        Arguments.checkNonEmpty(orgID, "Organization ID");
        Arguments.checkNonEmpty(token, "token");

        Task task = createTask(name, flux, null, cron, orgID);

        return createTask(task, token);
    }

    @Nonnull
    @Override
    public Task createTaskEvery(@Nonnull final String name,
                                @Nonnull final String flux,
                                @Nonnull final String every,
                                @Nonnull final Organization organization,
                                @Nonnull final String token) {

        Arguments.checkNonEmpty(name, "name of the task");
        Arguments.checkNonEmpty(flux, "Flux script to run");
        Arguments.checkNonEmpty(every, "every");
        Arguments.checkNotNull(organization, "organization");
        Arguments.checkNonEmpty(token, "token");

        Task task = createTask(name, flux, every, null, organization.getId());

        return createTask(task, token);
    }

    @Nonnull
    @Override
    public Task createTaskEvery(@Nonnull final String name,
                                @Nonnull final String flux,
                                @Nonnull final String every,
                                @Nonnull final String orgID,
                                @Nonnull final String token) {

        Arguments.checkNonEmpty(name, "name of the task");
        Arguments.checkNonEmpty(flux, "Flux script to run");
        Arguments.checkNonEmpty(every, "every expression");
        Arguments.checkNonEmpty(orgID, "Organization ID");
        Arguments.checkNonEmpty(token, "token");

        Task task = createTask(name, flux, every, null, orgID);

        return createTask(task, token);
    }


    @Nonnull
    @Override
    public Task updateTask(@Nonnull final Task task) {

        Arguments.checkNotNull(task, "Task is required");
        Arguments.checkDurationNotRequired(task.getEvery(), "Task.every");

        TaskUpdateRequest taskUpdateRequest = new TaskUpdateRequest();
        taskUpdateRequest.setStatus(TaskStatusType.fromValue(task.getStatus().getValue()));
        taskUpdateRequest.setFlux(task.getFlux());
        taskUpdateRequest.setName(task.getName());
        taskUpdateRequest.setEvery(task.getEvery());
        taskUpdateRequest.setCron(task.getCron());
        taskUpdateRequest.setOffset(task.getOffset());
        taskUpdateRequest.setDescription(task.getDescription());

        return updateTask(task.getId(), taskUpdateRequest);
    }

    @Nonnull
    @Override
    public Task updateTask(@Nonnull final String taskID, @Nonnull final TaskUpdateRequest request) {

        Arguments.checkNotNull(request, "request");
        Arguments.checkNonEmpty(taskID, "taskID");

        Call<Task> call = service.patchTasksID(taskID, request, null);

        return execute(call);
    }

    @Override
    public void deleteTask(@Nonnull final Task task) {

        Arguments.checkNotNull(task, "Task is required");

        deleteTask(task.getId());
    }

    @Override
    public void deleteTask(@Nonnull final String taskID) {

        Arguments.checkNonEmpty(taskID, "taskID");

        Call<Void> call = service.deleteTasksID(taskID, null);
        execute(call);
    }

    @Nonnull
    @Override
    public Task cloneTask(@Nonnull final String taskID, @Nonnull final String token) {

        Arguments.checkNonEmpty(taskID, "taskID");
        Arguments.checkNonEmpty(token, "token");

        Task task = findTaskByID(taskID);

        return cloneTask(task, token);
    }

    @Nonnull
    @Override
    public Task cloneTask(@Nonnull final Task task, @Nonnull final String token) {

        Arguments.checkNotNull(task, "task");
        Arguments.checkNonEmpty(token, "token");

        Task cloned = new Task();
        cloned.setName(task.getName());
        cloned.setOrgID(task.getOrgID());
        cloned.setFlux(task.getFlux());
        cloned.setStatus(TaskStatusType.ACTIVE);

        Task created = createTask(cloned, token);

        getLabels(task).forEach(label -> addLabel(label, created));

        return created;
    }

    @Nonnull
    @Override
    public List<ResourceMember> getMembers(@Nonnull final String taskID) {

        Arguments.checkNonEmpty(taskID, "Task.ID");

        Call<ResourceMembers> call = service.getTasksIDMembers(taskID, null);

        ResourceMembers resourceMembers = execute(call);
        LOG.log(Level.FINEST, "findTaskMembers found: {0}", resourceMembers);

        return resourceMembers.getUsers();
    }

    @Nonnull
    @Override
    public List<ResourceMember> getMembers(@Nonnull final Task task) {

        Arguments.checkNotNull(task, "Task");

        return getMembers(task.getId());
    }

    @Nonnull
    @Override
    public ResourceMember addMember(@Nonnull final User member, @Nonnull final Task task) {

        Arguments.checkNotNull(task, "task");
        Arguments.checkNotNull(member, "member");

        return addMember(member.getId(), task.getId());
    }

    @Nonnull
    @Override
    public ResourceMember addMember(@Nonnull final String memberID, @Nonnull final String taskID) {

        Arguments.checkNonEmpty(memberID, "Member ID");
        Arguments.checkNonEmpty(taskID, "Task.ID");

        AddResourceMemberRequestBody user = new AddResourceMemberRequestBody();
        user.setId(memberID);

        Call<ResourceMember> call = service.postTasksIDMembers(taskID, user, null);

        return execute(call);
    }

    @Override
    public void deleteMember(@Nonnull final User member, @Nonnull final Task task) {

        Arguments.checkNotNull(task, "task");
        Arguments.checkNotNull(member, "member");

        deleteMember(member.getId(), task.getId());
    }

    @Override
    public void deleteMember(@Nonnull final String memberID, @Nonnull final String taskID) {

        Arguments.checkNonEmpty(memberID, "Member ID");
        Arguments.checkNonEmpty(taskID, "Task.ID");

        Call<Void> call = service.deleteTasksIDMembersID(memberID, taskID, null);
        execute(call);
    }

    @Nonnull
    @Override
    public List<ResourceOwner> getOwners(@Nonnull final String taskID) {

        Arguments.checkNonEmpty(taskID, "Task.ID");

        Call<ResourceOwners> call = service.getTasksIDOwners(taskID, null);

        ResourceOwners resourceMembers = execute(call);
        LOG.log(Level.FINEST, "findTaskMembers found: {0}", resourceMembers);

        return resourceMembers.getUsers();
    }

    @Nonnull
    @Override
    public List<ResourceOwner> getOwners(@Nonnull final Task task) {

        Arguments.checkNotNull(task, "task");

        return getOwners(task.getId());
    }

    @Nonnull
    @Override
    public ResourceOwner addOwner(@Nonnull final User owner, @Nonnull final Task task) {

        Arguments.checkNotNull(task, "task");
        Arguments.checkNotNull(owner, "owner");

        return addOwner(owner.getId(), task.getId());
    }

    @Nonnull
    @Override
    public ResourceOwner addOwner(@Nonnull final String ownerID, @Nonnull final String taskID) {

        Arguments.checkNonEmpty(ownerID, "Owner ID");
        Arguments.checkNonEmpty(taskID, "Task.ID");

        AddResourceMemberRequestBody user = new AddResourceMemberRequestBody();
        user.setId(ownerID);

        Call<ResourceOwner> call = service.postTasksIDOwners(taskID, user, null);

        return execute(call);
    }

    @Override
    public void deleteOwner(@Nonnull final User owner, @Nonnull final Task task) {

        Arguments.checkNotNull(task, "task");
        Arguments.checkNotNull(owner, "owner");

        deleteOwner(owner.getId(), task.getId());
    }

    @Override
    public void deleteOwner(@Nonnull final String ownerID, @Nonnull final String taskID) {

        Arguments.checkNonEmpty(ownerID, "Owner ID");
        Arguments.checkNonEmpty(taskID, "Task.ID");

        Call<Void> call = service.deleteTasksIDOwnersID(ownerID, taskID, null);
        execute(call);
    }

    @Nonnull
    @Override
    public List<Run> getRuns(@Nonnull final Task task) {

        Arguments.checkNotNull(task, "task");

        return getRuns(task, null, null, null);
    }

    @Nonnull
    @Override
    public List<Run> getRuns(@Nonnull final Task task,
                             @Nullable final OffsetDateTime afterTime,
                             @Nullable final OffsetDateTime beforeTime,
                             @Nullable final Integer limit) {

        Arguments.checkNotNull(task, "task");

        return getRuns(task.getId(), task.getOrgID(), afterTime, beforeTime, limit);
    }

    @Nonnull
    @Override
    public List<Run> getRuns(@Nonnull final String taskID, @Nonnull final String orgID) {

        Arguments.checkNonEmpty(taskID, "Task.ID");
        Arguments.checkNonEmpty(orgID, "Org.ID");

        return getRuns(taskID, orgID, null, null, null);
    }

    @Nonnull
    @Override
    public List<Run> getRuns(@Nonnull final String taskID,
                             @Nonnull final String orgID,
                             @Nullable final OffsetDateTime afterTime,
                             @Nullable final OffsetDateTime beforeTime,
                             @Nullable final Integer limit) {

        Arguments.checkNonEmpty(taskID, "Task.ID");
        Arguments.checkNonEmpty(orgID, "Org.ID");

        Call<Runs> runs = service.getTasksIDRuns(taskID, null, null, limit, afterTime, beforeTime);
        Runs execute = execute(runs);

        return execute.getRuns();
    }

    @Nonnull
    @Override
    public Run getRun(@Nonnull final Run run) {

        Arguments.checkNotNull(run, "run");

        return getRun(run.getTaskID(), run.getId());
    }

    @Nonnull
    @Override
    public Run getRun(@Nonnull final String taskID, @Nonnull final String runID) {

        Arguments.checkNonEmpty(taskID, "Task.ID");
        Arguments.checkNonEmpty(runID, "Run.ID");

        Call<Run> run = service.getTasksIDRunsID(taskID, runID, null);

        return execute(run);
    }

    @Nonnull
    @Override
    public List<LogEvent> getRunLogs(@Nonnull final Run run) {

        Arguments.checkNotNull(run, "run");

        return getRunLogs(run.getTaskID(), run.getId());
    }

    @Nonnull
    @Override
    public List<LogEvent> getRunLogs(@Nonnull final String taskID,
                                     @Nonnull final String runID) {

        Arguments.checkNonEmpty(taskID, "Task.ID");
        Arguments.checkNonEmpty(runID, "Run.ID");

        Call<Logs> call = service.getTasksIDRunsIDLogs(taskID, runID, null);

        Logs logs = execute(call);

        return logs.getEvents();
    }

    @Nonnull
    @Override
    public Run runManually(@Nonnull final Task task) {

        Arguments.checkNotNull(task, "task");

        return runManually(task.getId(), new RunManually());
    }

    @Nonnull
    @Override
    public Run runManually(@Nonnull final String taskId, @Nonnull final RunManually runManually) {

        Arguments.checkNonEmpty(taskId, "taskId");
        Arguments.checkNotNull(runManually, "runManually");

        Call<Run> call = service.postTasksIDRuns(taskId, null, runManually);

        return execute(call);
    }

    @Nonnull
    @Override
    public Run retryRun(@Nonnull final Run run) {

        Arguments.checkNotNull(run, "run");

        return retryRun(run.getTaskID(), run.getId());
    }

    @Nonnull
    @Override
    public Run retryRun(@Nonnull final String taskID, @Nonnull final String runID) {

        Arguments.checkNonEmpty(taskID, "Task.ID");
        Arguments.checkNonEmpty(runID, "Run.ID");

        Call<Run> run = service.postTasksIDRunsIDRetry(taskID, runID, null);

        return execute(run);
    }

    @Override
    public void cancelRun(@Nonnull final Run run) {

        Arguments.checkNotNull(run, "run");

        cancelRun(run.getTaskID(), run.getId());
    }

    @Override
    public void cancelRun(@Nonnull final String taskID, @Nonnull final String runID) {

        Arguments.checkNonEmpty(taskID, "Task.ID");
        Arguments.checkNonEmpty(runID, "Run.ID");

        Call<Void> run = service.deleteTasksIDRunsID(taskID, runID, null);
        execute(run);
    }

    @Nonnull
    @Override
    public List<LogEvent> getLogs(@Nonnull final Task task) {

        Arguments.checkNotNull(task, "task");

        return getLogs(task.getId());
    }

    @Nonnull
    @Override
    public List<LogEvent> getLogs(@Nonnull final String taskID) {

        Arguments.checkNonEmpty(taskID, "Task.ID");

        Call<Logs> execute = service.getTasksIDLogs(taskID, null);

        Logs logs = execute(execute);

        return logs.getEvents();
    }

    @Nonnull
    @Override
    public List<Label> getLabels(@Nonnull final Task task) {

        Arguments.checkNotNull(task, "task");

        return getLabels(task.getId());
    }

    @Nonnull
    @Override
    public List<Label> getLabels(@Nonnull final String taskID) {

        Arguments.checkNonEmpty(taskID, "taskID");

        Call<LabelsResponse> call = service.getTasksIDLabels(taskID, null);

        return execute(call).getLabels();
    }

    @Nonnull
    @Override
    public LabelResponse addLabel(@Nonnull final Label label, @Nonnull final Task task) {

        Arguments.checkNotNull(label, "label");
        Arguments.checkNotNull(task, "task");

        return addLabel(label.getId(), task.getId());
    }

    @Nonnull
    @Override
    public LabelResponse addLabel(@Nonnull final String labelID, @Nonnull final String taskID) {

        Arguments.checkNonEmpty(labelID, "labelID");
        Arguments.checkNonEmpty(taskID, "taskID");

        LabelMapping labelMapping = new LabelMapping();
        labelMapping.setLabelID(labelID);

        Call<LabelResponse> call = service.postTasksIDLabels(taskID, labelMapping, null);

        return execute(call);
    }

    @Override
    public void deleteLabel(@Nonnull final Label label, @Nonnull final Task task) {

        Arguments.checkNotNull(label, "label");
        Arguments.checkNotNull(task, "task");

        deleteLabel(label.getId(), task.getId());
    }

    @Override
    public void deleteLabel(@Nonnull final String labelID, @Nonnull final String taskID) {

        Arguments.checkNonEmpty(labelID, "labelID");
        Arguments.checkNonEmpty(taskID, "taskID");

        Call<Void> call = service.deleteTasksIDLabelsID(taskID, labelID, null);
        execute(call);
    }

    @Nonnull
    private Task createTask(@Nonnull final String name,
                            @Nonnull final String flux,
                            @Nullable final String every,
                            @Nullable final String cron,
                            @Nonnull final String orgID) {

        Arguments.checkNonEmpty(name, "name of the task");
        Arguments.checkNotNull(flux, "Flux script to run");
        Arguments.checkNonEmpty(orgID, "Organization ID");
        Arguments.checkDurationNotRequired(every, "Task.every");

        Task task = new Task();
        task.setName(name);
        task.setOrgID(orgID);
        task.setStatus(TaskStatusType.ACTIVE);
        task.setFlux(flux);

        String repetition = "";
        if (every != null) {
            repetition += "every: ";
            repetition += every;
        }
        if (cron != null) {
            repetition += "cron: ";
            repetition += "\"" + cron + "\"";
        }
        String fluxWithOptions = String.format("option task = {name: \"%s\", %s} \n %s", name, repetition, flux);

        task.setFlux(fluxWithOptions);

        return task;
    }
}