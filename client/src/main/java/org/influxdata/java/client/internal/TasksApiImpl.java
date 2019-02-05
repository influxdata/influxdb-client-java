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
package org.influxdata.java.client.internal;

import java.time.Instant;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.influxdata.client.Arguments;
import org.influxdata.client.exceptions.NotFoundException;
import org.influxdata.java.client.TasksApi;
import org.influxdata.java.client.domain.Label;
import org.influxdata.java.client.domain.Organization;
import org.influxdata.java.client.domain.ResourceMember;
import org.influxdata.java.client.domain.ResourceMembers;
import org.influxdata.java.client.domain.ResourceType;
import org.influxdata.java.client.domain.Run;
import org.influxdata.java.client.domain.RunsResponse;
import org.influxdata.java.client.domain.Status;
import org.influxdata.java.client.domain.Task;
import org.influxdata.java.client.domain.Tasks;
import org.influxdata.java.client.domain.User;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import retrofit2.Call;

/**
 * @author Jakub Bednar (bednar@github) (11/09/2018 07:59)
 */
final class TasksApiImpl extends AbstractInfluxDBRestClient implements TasksApi {

    private static final Logger LOG = Logger.getLogger(TasksApiImpl.class.getName());

    private final JsonAdapter<Task> adapter;
    private final JsonAdapter<User> userAdapter;

    TasksApiImpl(@Nonnull final InfluxDBService influxDBService, @Nonnull final Moshi moshi) {

        super(influxDBService, moshi);

        this.adapter = moshi.adapter(Task.class);
        this.userAdapter = moshi.adapter(User.class);
    }

    @Nullable
    @Override
    public Task findTaskByID(@Nonnull final String taskID) {

        Arguments.checkNonEmpty(taskID, "taskID");

        Call<Task> call = influxDBService.findTaskByID(taskID);

        return execute(call, NotFoundException.class);
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

        Call<Tasks> call = influxDBService.findTasks(afterID, userID, orgID);

        Tasks tasks = execute(call);
        LOG.log(Level.FINEST, "findTasks found: {0}", tasks);

        return tasks.getTasks();
    }

    @Nonnull
    @Override
    public Task createTask(@Nonnull final Task task) {

        Arguments.checkNotNull(task, "task");

        Call<Task> call = influxDBService.createTask(createBody(adapter.toJson(task)));

        return execute(call);
    }

    @Nonnull
    @Override
    public Task createTaskCron(@Nonnull final String name,
                               @Nonnull final String flux,
                               @Nonnull final String cron,
                               @Nonnull final Organization organization) {

        Arguments.checkNonEmpty(name, "name of the task");
        Arguments.checkNonEmpty(flux, "Flux script to run");
        Arguments.checkNonEmpty(cron, "cron expression");
        Arguments.checkNotNull(organization, "organization");

        Task task = createTask(name, flux, null, cron, organization.getId());

        return createTask(task);
    }

    @Nonnull
    @Override
    public Task createTaskCron(@Nonnull final String name,
                               @Nonnull final String flux,
                               @Nonnull final String cron,
                               @Nonnull final String orgID) {

        Arguments.checkNonEmpty(name, "name of the task");
        Arguments.checkNonEmpty(flux, "Flux script to run");
        Arguments.checkNonEmpty(cron, "cron expression");
        Arguments.checkNonEmpty(orgID, "Organization ID");

        Organization organization = new Organization();
        organization.setId(orgID);

        return createTaskCron(name, flux, cron, organization);
    }

    @Nonnull
    @Override
    public Task createTaskEvery(@Nonnull final String name,
                                @Nonnull final String flux,
                                @Nonnull final String every,
                                @Nonnull final Organization organization) {

        Arguments.checkNonEmpty(name, "name of the task");
        Arguments.checkNonEmpty(flux, "Flux script to run");
        Arguments.checkNonEmpty(every, "every");
        Arguments.checkNotNull(organization, "organization");

        Task task = createTask(name, flux, every, null, organization.getId());

        return createTask(task);
    }

    @Nonnull
    @Override
    public Task createTaskEvery(@Nonnull final String name,
                                @Nonnull final String flux,
                                @Nonnull final String every,
                                @Nonnull final String orgID) {

        Arguments.checkNonEmpty(name, "name of the task");
        Arguments.checkNonEmpty(flux, "Flux script to run");
        Arguments.checkNonEmpty(every, "every expression");
        Arguments.checkNonEmpty(orgID, "Organization ID");

        Organization organization = new Organization();
        organization.setId(orgID);

        return createTaskEvery(name, flux, every, organization);
    }


    @Nonnull
    @Override
    public Task updateTask(@Nonnull final Task task) {

        Arguments.checkNotNull(task, "Task is required");
        Arguments.checkDurationNotRequired(task.getEvery(), "Task.every");

        Call<Task> call = influxDBService.updateTask(task.getId(), createBody(adapter.toJson(task)));

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

        Call<Void> call = influxDBService.deleteTask(taskID);
        execute(call);
    }

    @Nonnull
    @Override
    public List<ResourceMember> getMembers(@Nonnull final String taskID) {

        Arguments.checkNonEmpty(taskID, "Task.ID");

        Call<ResourceMembers> call = influxDBService.findTaskMembers(taskID);

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

        User user = new User();
        user.setId(memberID);

        String json = userAdapter.toJson(user);
        Call<ResourceMember> call = influxDBService.addTaskMember(taskID, createBody(json));

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

        Call<Void> call = influxDBService.deleteTaskMember(taskID, memberID);
        execute(call);
    }

    @Nonnull
    @Override
    public List<ResourceMember> getOwners(@Nonnull final String taskID) {

        Arguments.checkNonEmpty(taskID, "Task.ID");

        Call<ResourceMembers> call = influxDBService.findTaskOwners(taskID);

        ResourceMembers resourceMembers = execute(call);
        LOG.log(Level.FINEST, "findTaskMembers found: {0}", resourceMembers);

        return resourceMembers.getUsers();
    }

    @Nonnull
    @Override
    public List<ResourceMember> getOwners(@Nonnull final Task task) {

        Arguments.checkNotNull(task, "task");

        return getOwners(task.getId());
    }

    @Nonnull
    @Override
    public ResourceMember addOwner(@Nonnull final User owner, @Nonnull final Task task) {

        Arguments.checkNotNull(task, "task");
        Arguments.checkNotNull(owner, "owner");

        return addOwner(owner.getId(), task.getId());
    }

    @Nonnull
    @Override
    public ResourceMember addOwner(@Nonnull final String ownerID, @Nonnull final String taskID) {

        Arguments.checkNonEmpty(ownerID, "Owner ID");
        Arguments.checkNonEmpty(taskID, "Task.ID");

        User user = new User();
        user.setId(ownerID);

        String json = userAdapter.toJson(user);
        Call<ResourceMember> call = influxDBService.addTaskOwner(taskID, createBody(json));

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

        Call<Void> call = influxDBService.deleteTaskOwner(taskID, ownerID);
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
                             @Nullable final Instant afterTime,
                             @Nullable final Instant beforeTime,
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
                             @Nullable final Instant afterTime,
                             @Nullable final Instant beforeTime,
                             @Nullable final Integer limit) {

        Arguments.checkNonEmpty(taskID, "Task.ID");
        Arguments.checkNonEmpty(orgID, "Org.ID");

        Call<RunsResponse> runs = influxDBService.findTaskRuns(taskID, afterTime, beforeTime, limit, orgID);
        RunsResponse execute = execute(runs);

        return execute.getRuns();
    }

    @Nullable
    @Override
    public Run getRun(@Nonnull final Run run) {

        Arguments.checkNotNull(run, "run");

        return getRun(run.getTaskID(), run.getId());
    }

    @Nullable
    @Override
    public Run getRun(@Nonnull final String taskID, @Nonnull final String runID) {

        Arguments.checkNonEmpty(taskID, "Task.ID");
        Arguments.checkNonEmpty(runID, "Run.ID");

        Call<Run> run = influxDBService.findTaskRun(taskID, runID);

        return execute(run, NotFoundException.class);
    }

    @Nonnull
    @Override
    public List<String> getRunLogs(@Nonnull final Run run, @Nonnull final String orgID) {

        Arguments.checkNotNull(run, "run");

        return getRunLogs(run.getTaskID(), run.getId(), orgID);
    }

    @Nonnull
    @Override
    public List<String> getRunLogs(@Nonnull final String taskID,
                                   @Nonnull final String runID,
                                   @Nonnull final String orgID) {

        Arguments.checkNonEmpty(taskID, "Task.ID");
        Arguments.checkNonEmpty(runID, "Run.ID");
        Arguments.checkNonEmpty(orgID, "Org.ID");

        Call<List<String>> logs = influxDBService.findRunLogs(taskID, runID, orgID);

        return execute(logs);
    }

    @Nullable
    @Override
    public Run retryRun(@Nonnull final Run run) {

        Arguments.checkNotNull(run, "run");

        return retryRun(run.getTaskID(), run.getId());
    }

    @Nullable
    @Override
    public Run retryRun(@Nonnull final String taskID, @Nonnull final String runID) {

        Arguments.checkNonEmpty(taskID, "Task.ID");
        Arguments.checkNonEmpty(runID, "Run.ID");

        Call<Run> run = influxDBService.retryTaskRun(taskID, runID);

        return execute(run, NotFoundException.class);
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

        Call<Void> run = influxDBService.cancelRun(taskID, runID);
        execute(run);
    }

    @Nonnull
    @Override
    public List<String> getLogs(@Nonnull final Task task) {

        Arguments.checkNotNull(task, "task");

        return getLogs(task.getId(), task.getOrgID());
    }

    @Nonnull
    @Override
    public List<String> getLogs(@Nonnull final String taskID, @Nonnull final String orgID) {

        Arguments.checkNonEmpty(taskID, "Task.ID");
        Arguments.checkNonEmpty(orgID, "Org.ID");

        Call<List<String>> execute = influxDBService.findTaskLogs(taskID, orgID);

        return execute(execute);
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

        return getLabels(taskID, "tasks");
    }

    @Nonnull
    @Override
    public Label addLabel(@Nonnull final Label label, @Nonnull final Task task) {

        Arguments.checkNotNull(label, "label");
        Arguments.checkNotNull(task, "task");

        return addLabel(label.getId(), task.getId());
    }

    @Nonnull
    @Override
    public Label addLabel(@Nonnull final String labelID, @Nonnull final String taskID) {

        Arguments.checkNonEmpty(labelID, "labelID");
        Arguments.checkNonEmpty(taskID, "taskID");

        return addLabel(labelID, taskID, "tasks", ResourceType.TASKS);
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

        deleteLabel(labelID, taskID, "tasks");
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
        task.setStatus(Status.ACTIVE);
        task.setEvery(every);
        task.setCron(cron);
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