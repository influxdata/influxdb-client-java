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
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

import com.influxdb.client.domain.Authorization;
import com.influxdb.client.domain.Bucket;
import com.influxdb.client.domain.Label;
import com.influxdb.client.domain.LogEvent;
import com.influxdb.client.domain.Organization;
import com.influxdb.client.domain.Permission;
import com.influxdb.client.domain.PermissionResource;
import com.influxdb.client.domain.ResourceMember;
import com.influxdb.client.domain.ResourceOwner;
import com.influxdb.client.domain.Run;
import com.influxdb.client.domain.RunLog;
import com.influxdb.client.domain.RunManually;
import com.influxdb.client.domain.Task;
import com.influxdb.client.domain.TaskStatusType;
import com.influxdb.client.domain.User;
import com.influxdb.exceptions.NotFoundException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (05/09/2018 15:54)
 */
@RunWith(JUnitPlatform.class)
class ITTasksApi extends AbstractITClientTest {

    private static final Logger LOG = Logger.getLogger(ITTasksApi.class.getName());
    private static final String TASK_FLUX = "from(bucket:\"my-bucket\") |> range(start: -1m) |> last()";

    private Organization organization;

    private TasksApi tasksApi;

    @BeforeEach
    void setUp() throws Exception {

        organization = findMyOrg();

        //
        // Add Task permission
        //
        Authorization authorization = addTasksAuthorization(organization);

        influxDBClient.close();
        influxDBClient = InfluxDBClientFactory.create(influxDB_URL, authorization.getToken().toCharArray());

        tasksApi = influxDBClient.getTasksApi();

        tasksApi.findTasks().forEach(task -> tasksApi.deleteTask(task));
    }

    @Test
    void createTask() {

        String taskName = generateName("it task");

        String flux = "option task = {\n"
                + "    name: \"" + taskName + "\",\n"
                + "    every: 1h\n"
                + "}\n\n" + TASK_FLUX;

        Task task = new Task();
        task.setName(taskName);
        task.setOrgID(organization.getId());
        task.setFlux(flux);
        task.setStatus(TaskStatusType.ACTIVE);
        task.setDescription("Task description");

        task = tasksApi.createTask(task);

        Assertions.assertThat(task).isNotNull();
        Assertions.assertThat(task.getId()).isNotBlank();
        Assertions.assertThat(task.getName()).isEqualTo(taskName);
        Assertions.assertThat(task.getOrgID()).isEqualTo(organization.getId());
        Assertions.assertThat(task.getStatus()).isEqualTo(TaskStatusType.ACTIVE);
        Assertions.assertThat(task.getEvery()).isEqualTo("1h");
        Assertions.assertThat(task.getCron()).isNull();
        Assertions.assertThat(task.getFlux()).isEqualToIgnoringWhitespace(flux);
        Assertions.assertThat(task.getDescription()).isEqualTo("Task description");
    }

    @Test
    void createTaskWithOffset() {

        String taskName = generateName("it task");

        String flux = "option task = {\n"
                + "    name: \"" + taskName + "\",\n"
                + "    every: 1h,\n"
                + "    offset: 30m\n"
                + "}\n\n" + TASK_FLUX;

        Task task = new Task();
        task.setName(taskName);
        task.setOrgID(organization.getId());
        task.setFlux(flux);
        task.setStatus(TaskStatusType.ACTIVE);

        task = tasksApi.createTask(task);

        Assertions.assertThat(task).isNotNull();
        Assertions.assertThat(task.getOffset()).isEqualTo("30m");
    }

    @Test
    void createTaskEvery() {

        String taskName = generateName("it task");

        Task task = tasksApi.createTaskEvery(taskName, TASK_FLUX, "1h", organization);

        Assertions.assertThat(task).isNotNull();
        Assertions.assertThat(task.getId()).isNotBlank();
        Assertions.assertThat(task.getName()).isEqualTo(taskName);
        Assertions.assertThat(task.getOrgID()).isEqualTo(organization.getId());
        Assertions.assertThat(task.getStatus()).isEqualTo(TaskStatusType.ACTIVE);
        Assertions.assertThat(task.getEvery()).isEqualTo("1h");
        Assertions.assertThat(task.getCron()).isNull();
        Assertions.assertThat(task.getFlux()).endsWith(TASK_FLUX);
    }

    @Test
    void createTaskCron() {

        String taskName = generateName("it task");

        Task task = tasksApi.createTaskCron(taskName, TASK_FLUX, "0 2 * * *", organization);

        Assertions.assertThat(task).isNotNull();
        Assertions.assertThat(task.getId()).isNotBlank();
        Assertions.assertThat(task.getName()).isEqualTo(taskName);
        Assertions.assertThat(task.getOrgID()).isEqualTo(organization.getId());
        Assertions.assertThat(task.getStatus()).isEqualTo(TaskStatusType.ACTIVE);
        Assertions.assertThat(task.getCron()).isEqualTo("0 2 * * *");
        Assertions.assertThat(task.getEvery()).isNull();
        Assertions.assertThat(task.getFlux()).endsWith(TASK_FLUX);
        Assertions.assertThat(task.getLinks()).isNotNull();
        Assertions.assertThat(task.getLinks().getLogs()).isEqualTo("/api/v2/tasks/" + task.getId() + "/logs");
        Assertions.assertThat(task.getLinks().getMembers()).isEqualTo("/api/v2/tasks/" + task.getId() + "/members");
        Assertions.assertThat(task.getLinks().getOwners()).isEqualTo("/api/v2/tasks/" + task.getId() + "/owners");
        Assertions.assertThat(task.getLinks().getRuns()).isEqualTo("/api/v2/tasks/" + task.getId() + "/runs");
        Assertions.assertThat(task.getLinks().getSelf()).isEqualTo("/api/v2/tasks/" + task.getId());
        Assertions.assertThat(task.getLinks().getLabels()).isEqualTo("/api/v2/tasks/" + task.getId() + "/labels");
    }

    @Test
    void findTaskByID() {

        String taskName = generateName("it task");

        Task task = tasksApi.createTaskCron(taskName, TASK_FLUX, "0 2 * * *", organization.getId());

        Task taskByID = tasksApi.findTaskByID(task.getId());
        LOG.info("TaskByID: " + taskByID);

        Assertions.assertThat(taskByID).isNotNull();
        Assertions.assertThat(taskByID.getId()).isEqualTo(task.getId());
        Assertions.assertThat(taskByID.getName()).isEqualTo(task.getName());
        Assertions.assertThat(taskByID.getOrgID()).isEqualTo(task.getOrgID());
        Assertions.assertThat(taskByID.getEvery()).isNull();
        Assertions.assertThat(taskByID.getCron()).isEqualTo(task.getCron());
        Assertions.assertThat(taskByID.getFlux()).isEqualTo(task.getFlux());
        Assertions.assertThat(taskByID.getStatus()).isEqualTo(TaskStatusType.ACTIVE);
        Assertions.assertThat(taskByID.getCreatedAt()).isNotNull();
    }

    @Test
    void findTaskByIDNull() {

        Assertions.assertThatThrownBy(() -> tasksApi.findTaskByID("020f755c3d082000"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("failed to find task: task not found");
    }

    @Test
    void findTasks() {

        int size = tasksApi.findTasks().size();

        Task everyTask = tasksApi.createTaskEvery(generateName("it task"), TASK_FLUX, "2h", organization.getId());
        Assertions.assertThat(everyTask).isNotNull();

        List<Task> tasks = tasksApi.findTasks();
        Assertions.assertThat(tasks).hasSize(size + 1);
        tasks.forEach(task -> Assertions.assertThat(task.getStatus()).isNotNull());
    }

    @Test
    @Disabled
    //TODO set user password -> https://github.com/influxdata/influxdb/issues/11590
    void findTasksByUserID() {

        User taskUser = influxDBClient.getUsersApi().createUser(generateName("TaskUser"));

        tasksApi.createTaskCron(generateName("it task"), TASK_FLUX, "0 2 * * *", organization);

        List<Task> tasks = tasksApi.findTasksByUser(taskUser);
        Assertions.assertThat(tasks).hasSize(1);
    }

    @Test
    @Disabled
    //TODO https://github.com/influxdata/influxdb/issues/11491
    void findTasksByOrganizationID() throws Exception {

        Organization taskOrganization = influxDBClient.getOrganizationsApi().createOrganization(generateName("TaskOrg"));

        Authorization authorization = addTasksAuthorization(taskOrganization);
        influxDBClient.close();
        influxDBClient = InfluxDBClientFactory.create(influxDB_URL, authorization.getToken().toCharArray());
        tasksApi = influxDBClient.getTasksApi();

        tasksApi.createTaskCron(generateName("it task"), TASK_FLUX, "0 2 * * *", taskOrganization);

        List<Task> tasks = tasksApi.findTasksByOrganization(taskOrganization);
        Assertions.assertThat(tasks).hasSize(1);

        tasksApi.findTasks().forEach(task -> tasksApi.deleteTask(task));
    }

    @Test
    @Disabled
    //TODO https://github.com/influxdata/influxdb/issues/13577
    void findTasksAfterSpecifiedID() {

        Task task1 = tasksApi.createTaskCron(generateName("it task"), TASK_FLUX, "0 2 * * *", organization);
        Task task2 = tasksApi.createTaskCron(generateName("it task"), TASK_FLUX, "0 2 * * *", organization);

        List<Task> tasks = tasksApi.findTasks(task1.getId(), null, null);

        Assertions.assertThat(tasks).hasSize(1);
        Assertions.assertThat(tasks.get(0).getId()).isEqualTo(task2.getId());
    }

    @Test
    void deleteTask() {

        Task createdTask = tasksApi.createTaskCron(generateName("it task"), TASK_FLUX, "0 2 * * *", organization);
        Assertions.assertThat(createdTask).isNotNull();

        Task foundTask = tasksApi.findTaskByID(createdTask.getId());
        Assertions.assertThat(foundTask).isNotNull();

        // delete task
        tasksApi.deleteTask(createdTask);

        Assertions.assertThatThrownBy(() -> tasksApi.findTaskByID(createdTask.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("failed to find task: task not found");
    }

    @Test
    void updateTask() {

        String taskName = generateName("it task");
        Task cronTask = tasksApi.createTaskCron(taskName, TASK_FLUX, "0 2 * * *", organization);

        String flux = "option task = {\n"
                + "    name: \"" + taskName + "\",\n"
                + "    every: 3m\n"
                + "}\n\n" + TASK_FLUX;

        cronTask.setCron(null);
        cronTask.setEvery("3m");
        cronTask.setStatus(TaskStatusType.INACTIVE);
        cronTask.setDescription("Updated description");

        Task updatedTask = tasksApi.updateTask(cronTask);

        Assertions.assertThat(updatedTask).isNotNull();
        Assertions.assertThat(updatedTask.getId()).isEqualTo(cronTask.getId());
        Assertions.assertThat(updatedTask.getEvery()).isEqualTo("3m");
        Assertions.assertThat(updatedTask.getCron()).isNull();
        Assertions.assertThat(updatedTask.getFlux()).isEqualToIgnoringWhitespace(flux);
        Assertions.assertThat(updatedTask.getStatus()).isEqualTo(TaskStatusType.INACTIVE);
        Assertions.assertThat(updatedTask.getOrgID()).isEqualTo(cronTask.getOrgID());
        Assertions.assertThat(updatedTask.getName()).isEqualTo(cronTask.getName());
        Assertions.assertThat(updatedTask.getUpdatedAt()).isNotNull();
        Assertions.assertThat(updatedTask.getDescription()).isEqualTo("Updated description");
    }

    @Test
    void member() {

        UsersApi usersApi = influxDBClient.getUsersApi();

        Task task = tasksApi.createTaskCron(generateName("task"), TASK_FLUX, "0 2 * * *", organization);

        List<ResourceMember> members = tasksApi.getMembers(task);
        Assertions.assertThat(members).hasSize(0);

        User user = usersApi.createUser(generateName("Luke Health"));

        ResourceMember resourceMember = tasksApi.addMember(user, task);
        Assertions.assertThat(resourceMember).isNotNull();
        Assertions.assertThat(resourceMember.getId()).isEqualTo(user.getId());
        Assertions.assertThat(resourceMember.getName()).isEqualTo(user.getName());
        Assertions.assertThat(resourceMember.getRole()).isEqualTo(ResourceMember.RoleEnum.MEMBER);

        members = tasksApi.getMembers(task);
        Assertions.assertThat(members).hasSize(1);
        Assertions.assertThat(members.get(0).getRole()).isEqualTo(ResourceMember.RoleEnum.MEMBER);
        Assertions.assertThat(members.get(0).getId()).isEqualTo(user.getId());
        Assertions.assertThat(members.get(0).getName()).isEqualTo(user.getName());

        tasksApi.deleteMember(user, task);

        members = tasksApi.getMembers(task);
        Assertions.assertThat(members).hasSize(0);
    }

    @Test
    void owner() {

        UsersApi usersApi = influxDBClient.getUsersApi();

        Task task = tasksApi.createTaskCron(generateName("task"), TASK_FLUX, "0 2 * * *", organization);

        List<ResourceOwner> owners = tasksApi.getOwners(task);
        Assertions.assertThat(owners).hasSize(1);

        User user = usersApi.createUser(generateName("Luke Health"));

        ResourceOwner resourceMember = tasksApi.addOwner(user, task);
        Assertions.assertThat(resourceMember).isNotNull();
        Assertions.assertThat(resourceMember.getId()).isEqualTo(user.getId());
        Assertions.assertThat(resourceMember.getName()).isEqualTo(user.getName());
        Assertions.assertThat(resourceMember.getRole()).isEqualTo(ResourceOwner.RoleEnum.OWNER);

        owners = tasksApi.getOwners(task);
        Assertions.assertThat(owners).hasSize(2);
        Assertions.assertThat(owners.get(1).getRole()).isEqualTo(ResourceOwner.RoleEnum.OWNER);
        Assertions.assertThat(owners.get(1).getId()).isEqualTo(user.getId());
        Assertions.assertThat(owners.get(1).getName()).isEqualTo(user.getName());

        tasksApi.deleteOwner(user, task);

        owners = tasksApi.getOwners(task);
        Assertions.assertThat(owners).hasSize(1);
    }

    @Test
    void runs() throws Exception {

        String taskName = generateName("it task");

        Task task = tasksApi.createTaskEvery(taskName, TASK_FLUX, "1s", organization);

        Thread.sleep(5_000);

        List<Run> runs = tasksApi.getRuns(task);
        Assertions.assertThat(runs).isNotEmpty();

        Run run = runs.stream().filter(it -> it.getStatus().equals(Run.StatusEnum.SUCCESS))
                .findFirst()
                .orElseThrow(() -> new AssertionError(String.format("The runs: <%s> doesn't have a success run", runs)));

        String failMessage = String.format("Runs: <%s>, first run: <%s>", runs, run);

        Assertions.assertThat(run.getId()).withFailMessage(failMessage).isNotBlank();
        Assertions.assertThat(run.getTaskID()).withFailMessage(failMessage).isEqualTo(task.getId());
        Assertions.assertThat(run.getStatus()).withFailMessage(failMessage).isEqualTo(Run.StatusEnum.SUCCESS);
        Assertions.assertThat(run.getScheduledFor()).withFailMessage(failMessage).isBefore(OffsetDateTime.now());
        Assertions.assertThat(run.getStartedAt()).withFailMessage(failMessage).isBefore(OffsetDateTime.now());
        Assertions.assertThat(run.getFinishedAt()).withFailMessage(failMessage).isBefore(OffsetDateTime.now());
        Assertions.assertThat(run.getRequestedAt()).withFailMessage(failMessage).isNull();
        Assertions.assertThat(run.getLinks()).withFailMessage(failMessage).isNotNull();
        Assertions.assertThat(run.getLinks().getLogs()).withFailMessage(failMessage)
                .isEqualTo("/api/v2/tasks/" + task.getId() + "/runs/" + run.getId() + "/logs");
        Assertions.assertThat(run.getLinks().getRetry()).withFailMessage(failMessage)
                .isEqualTo("/api/v2/tasks/" + task.getId() + "/runs/" + run.getId() + "/retry");
        Assertions.assertThat(run.getLinks().getSelf()).withFailMessage(failMessage)
                .isEqualTo("/api/v2/tasks/" + task.getId() + "/runs/" + run.getId());
        Assertions.assertThat(run.getLinks().getTask()).withFailMessage(failMessage)
                .isEqualTo("/api/v2/tasks/" + task.getId());
    }

    @Test
    void runsNotExist() {

        Assertions.assertThatThrownBy(() -> tasksApi.getRuns("020f755c3c082000"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("failed to find runs: task not found");
    }

    @Test
    void runsByTime() throws InterruptedException {

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC).minus(1, ChronoUnit.DAYS);

        String taskName = generateName("it task");

        Task task = tasksApi.createTaskEvery(taskName, TASK_FLUX, "1s", organization);

        Thread.sleep(5_000);

        List<Run> runs = tasksApi.getRuns(task, null, now, null);
        // TODO https://github.com/influxdata/influxdb/issues/13577
        // Assertions.assertThat(runs).hasSize(0);

        runs = tasksApi.getRuns(task, now, null, null);
        Assertions.assertThat(runs).isNotEmpty();
    }

    @Test
    void runsLimit() throws InterruptedException {

        String taskName = generateName("it task");

        Task task = tasksApi.createTaskEvery(taskName, TASK_FLUX, "1s", organization);

        Thread.sleep(5_000);

        List<Run> runs = tasksApi.getRuns(task, null, null, 1);

        Assertions.assertThat(runs).hasSize(1);

        runs = tasksApi.getRuns(task, null, null, null);
        Assertions.assertThat(runs.size()).isGreaterThan(1);
    }

    @Test
    void run() throws InterruptedException {

        String taskName = generateName("it task");

        Task task = tasksApi.createTaskEvery(taskName, TASK_FLUX, "1s", organization);

        Thread.sleep(5_000);

        List<Run> runs = tasksApi.getRuns(task);
        Assertions.assertThat(runs).isNotEmpty();

        Run firstRun = runs.stream().filter(it -> it.getStatus().equals(Run.StatusEnum.SUCCESS))
                .findFirst()
                .orElseThrow(() -> new AssertionError(String.format("The runs: <%s> doesn't have a success run", runs)));

        List<RunLog> logs = firstRun.getLog();
        Assertions.assertThat(logs).isNotEmpty();
        Assertions.assertThat(logs.size())
                .withFailMessage("Run logs: <%s>", logs)
                .isEqualTo(3);
        Assertions.assertThat(logs.get(0).getMessage())
                .withFailMessage("Run logs: <%s>", logs)
                .startsWith("Started task from script");
        Assertions.assertThat(logs.get(0).getRunID())
                .withFailMessage("Run logs: <%s>", logs)
                .isEqualTo(firstRun.getId());
        Assertions.assertThat(logs.get(2).getMessage())
                .withFailMessage("Run logs: <%s>", logs)
                .startsWith("Completed successfully");
        Assertions.assertThat(logs.get(2).getRunID())
                .withFailMessage("Run logs: <%s>", logs)
                .isEqualTo(firstRun.getId());

        Run runById = tasksApi.getRun(firstRun);

        Assertions.assertThat(runById).isNotNull();
        Assertions.assertThat(runById.getId()).isEqualTo(firstRun.getId());

        task = tasksApi.findTaskByID(task.getId());

        Assertions.assertThat(task).isNotNull();
        Assertions.assertThat(task.getLatestCompleted()).isNotNull();
    }

    @Test
    void runNotExist() {

        String taskName = generateName("it task");

        Task task = tasksApi.createTaskEvery(taskName, TASK_FLUX, "5s", organization);

        Assertions.assertThatThrownBy(() -> tasksApi.getRun(task.getId(), "020f755c3c082000"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("failed to find run: run not found");
    }

    @Test
    void runTaskManually() {

        Task task = tasksApi.createTaskEvery(generateName("it task"), TASK_FLUX, "1s", organization);

        Run run = tasksApi.runManually(task);

        Assertions.assertThat(run).isNotNull();
        Assertions.assertThat(run.getStatus()).isEqualTo(Run.StatusEnum.SCHEDULED);
    }

    @Test
    void runTaskManuallyNotExist() {

        Assertions.assertThatThrownBy(() -> tasksApi.runManually("020f755c3c082000", new RunManually()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("failed to force run: task not found");
    }

    @Test
    void retryRun() throws InterruptedException {

        String taskName = generateName("it task");

        Task task = tasksApi.createTaskEvery(taskName, TASK_FLUX, "1s", organization);

        Thread.sleep(5_000);

        List<Run> runs = tasksApi.getRuns(task);
        Assertions.assertThat(runs).isNotEmpty();

        Run run = tasksApi.retryRun(runs.get(0));

        Assertions.assertThat(run).isNotNull();
        Assertions.assertThat(run.getTaskID()).isEqualTo(runs.get(0).getTaskID());

        Assertions.assertThat(run.getStatus()).isEqualTo(Run.StatusEnum.SCHEDULED);
        Assertions.assertThat(run.getTaskID()).isEqualTo(task.getId());
    }

    @Test
    void retryRunNotExist() {

        String taskName = generateName("it task");

        Task task = tasksApi.createTaskEvery(taskName, TASK_FLUX, "5s", organization);

        Assertions.assertThatThrownBy(() -> tasksApi.retryRun(task.getId(), "020f755c3c082000"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("failed to retry run: run not found");
    }

    @Test
    void logs() throws Exception {

        String taskName = generateName("it task");

        Task task = tasksApi.createTaskEvery(taskName, TASK_FLUX, "3s", organization);

        Thread.sleep(6_000);

        List<LogEvent> logs = tasksApi.getLogs(task);
        Assertions.assertThat(logs).isNotEmpty();

        //
        // The completed task are on the end of the list
        //
        logs = logs.subList(logs.size() - 3, logs.size());
        Assertions.assertThat(logs.get(0).getMessage())
                .withFailMessage("LogEvents: %s", logs)
                .startsWith("Started task from script:");

        Assertions.assertThat(logs.get(1).getMessage())
                .withFailMessage("LogEvents: %s", logs)
                .contains("total_duration");

        Assertions.assertThat(logs.get(2).getMessage())
                .withFailMessage("LogEvents: %s", logs)
                .contains("Completed successfully");
    }

    @Test
    void logsNotExist() {

        Assertions.assertThatThrownBy(() -> tasksApi.getLogs("020f755c3c082000"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("failed to find task logs: task not found");
    }

    @Test
    void runLogs() throws Exception {

        String taskName = generateName("it task");

        Task task = tasksApi.createTaskEvery(taskName, TASK_FLUX, "1s", organization);

        Thread.sleep(5_000);

        List<Run> runs = tasksApi.getRuns(task);
        Assertions.assertThat(runs).isNotEmpty();

        List<LogEvent> logs = tasksApi.getRunLogs(runs.get(0));

        Assertions.assertThat(logs).isNotEmpty();
        Assertions.assertThat(logs.get(logs.size() - 1).getMessage()).endsWith("Completed successfully");
    }

    @Test
    void runLogsNotExist() {

        String taskName = generateName("it task");

        Task task = tasksApi.createTaskEvery(taskName, TASK_FLUX, "5s", organization);

        Assertions.assertThatThrownBy(() -> tasksApi.getRunLogs(task.getId(), "020f755c3c082000"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("failed to find task logs: run not found");
    }

    @Test
    void cancelRunNotExist() throws InterruptedException {

        String taskName = generateName("it task");

        Task task = tasksApi.createTaskEvery(taskName, TASK_FLUX, "1s", organization);

        Thread.sleep(5_000);

        List<Run> runs = tasksApi.getRuns(task);

        Assertions.assertThatThrownBy(() -> tasksApi.cancelRun(runs.get(0)))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("failed to cancel run: run not found");
    }

    @Test
    void cancelRunTaskNotExist() {

        Assertions.assertThatThrownBy(() -> tasksApi.cancelRun("020f755c3c082000", "020f755c3c082000"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("failed to cancel run: task not found");
    }

    @Test
    void labels() {

        LabelsApi labelsApi = influxDBClient.getLabelsApi();

        Task task = tasksApi.createTaskEvery(generateName("it task"), TASK_FLUX, "1s", organization);

        Map<String, String> properties = new HashMap<>();
        properties.put("color", "green");
        properties.put("location", "west");

        Label label = labelsApi.createLabel(generateName("Cool Resource"), properties, organization.getId());

        List<Label> labels = tasksApi.getLabels(task);
        Assertions.assertThat(labels).hasSize(0);

        Label addedLabel = tasksApi.addLabel(label, task).getLabel();
        Assertions.assertThat(addedLabel).isNotNull();
        Assertions.assertThat(addedLabel.getId()).isEqualTo(label.getId());
        Assertions.assertThat(addedLabel.getName()).isEqualTo(label.getName());
        Assertions.assertThat(addedLabel.getProperties()).isEqualTo(label.getProperties());

        labels = tasksApi.getLabels(task);
        Assertions.assertThat(labels).hasSize(1);
        Assertions.assertThat(labels.get(0).getId()).isEqualTo(label.getId());
        Assertions.assertThat(labels.get(0).getName()).isEqualTo(label.getName());

        task = tasksApi.findTaskByID(task.getId());
        Assertions.assertThat(task).isNotNull();
        Assertions.assertThat(task.getLabels()).hasSize(1);
        Assertions.assertThat(task.getLabels().get(0).getId()).isEqualTo(label.getId());
        Assertions.assertThat(task.getLabels().get(0).getName()).isEqualTo(label.getName());

        tasksApi.deleteLabel(label, task);

        labels = tasksApi.getLabels(task);
        Assertions.assertThat(labels).hasSize(0);
    }

    @Test
    void cloneTask() {

        Task source = tasksApi.createTaskEvery(generateName("it task"), TASK_FLUX, "1s", organization);

        Map<String, String> properties = new HashMap<>();
        properties.put("color", "green");
        properties.put("location", "west");

        Label label = influxDBClient.getLabelsApi().createLabel(generateName("Cool Resource"), properties, organization.getId());

        tasksApi.addLabel(label, source);

        Task cloned = tasksApi.cloneTask(source.getId());

        Assertions.assertThat(cloned.getName()).isEqualTo(source.getName());
        Assertions.assertThat(cloned.getOrgID()).isEqualTo(organization.getId());
        Assertions.assertThat(cloned.getFlux()).isEqualTo(source.getFlux());
        Assertions.assertThat(cloned.getEvery()).isEqualTo("1s");
        Assertions.assertThat(cloned.getCron()).isNull();
        Assertions.assertThat(cloned.getOffset()).isNull();

        List<Label> labels = tasksApi.getLabels(cloned);
        Assertions.assertThat(labels).hasSize(1);
        Assertions.assertThat(labels.get(0).getId()).isEqualTo(label.getId());
    }

    @Test
    void cloneTaskNotFound() {
        Assertions.assertThatThrownBy(() -> tasksApi.cloneTask("020f755c3c082000"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("failed to find task: task not found");
    }

    @Nonnull
    private Authorization addTasksAuthorization(final Organization organization) {

        PermissionResource resource = new PermissionResource();
        resource.setOrgID(organization.getId());
        resource.setType(PermissionResource.TypeEnum.TASKS);

        Permission createTask = new Permission();
        createTask.setResource(resource);
        createTask.setAction(Permission.ActionEnum.READ);

        Permission deleteTask = new Permission();
        deleteTask.setResource(resource);
        deleteTask.setAction(Permission.ActionEnum.WRITE);

        PermissionResource orgResource = new PermissionResource();
        orgResource.setType(PermissionResource.TypeEnum.ORGS);

        Permission createOrg = new Permission();
        createOrg.setAction(Permission.ActionEnum.WRITE);
        createOrg.setResource(orgResource);

        Permission readOrg = new Permission();
        readOrg.setAction(Permission.ActionEnum.READ);
        readOrg.setResource(orgResource);

        PermissionResource userResource = new PermissionResource();
        userResource.setType(PermissionResource.TypeEnum.USERS);

        Permission createUsers = new Permission();
        createUsers.setAction(Permission.ActionEnum.WRITE);
        createUsers.setResource(userResource);


        PermissionResource labelResource = new PermissionResource();
        labelResource.setType(PermissionResource.TypeEnum.LABELS);

        Permission createLabels = new Permission();
        createLabels.setAction(Permission.ActionEnum.WRITE);
        createLabels.setResource(labelResource);

        PermissionResource authResource = new PermissionResource();
        authResource.setType(PermissionResource.TypeEnum.AUTHORIZATIONS);

        Permission createAuth = new Permission();
        createAuth.setAction(Permission.ActionEnum.WRITE);
        createAuth.setResource(userResource);

        Bucket bucket = influxDBClient.getBucketsApi().findBucketByName("my-bucket");
        Assertions.assertThat(bucket).isNotNull();

        PermissionResource bucketResource = new PermissionResource();
        bucketResource.setOrgID(organization.getId());
        bucketResource.setType(PermissionResource.TypeEnum.BUCKETS);
        bucketResource.setId(bucket.getId());

        Permission readBucket = new Permission();
        readBucket.setResource(bucketResource);
        readBucket.setAction(Permission.ActionEnum.READ);

        Permission writeBucket = new Permission();
        writeBucket.setResource(bucketResource);
        writeBucket.setAction(Permission.ActionEnum.WRITE);

        List<Permission> permissions = new ArrayList<>();
        permissions.add(createTask);
        permissions.add(deleteTask);
        permissions.add(createOrg);
        permissions.add(readOrg);
        permissions.add(createUsers);
        permissions.add(createAuth);
        permissions.add(readBucket);
        permissions.add(writeBucket);
        permissions.add(createLabels);

        return influxDBClient.getAuthorizationsApi().createAuthorization(organization, permissions);
    }
}
