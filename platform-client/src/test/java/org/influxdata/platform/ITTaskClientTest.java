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
package org.influxdata.platform;

import java.time.Instant;
import java.util.List;
import java.util.logging.Logger;

import org.influxdata.platform.domain.Organization;
import org.influxdata.platform.domain.ResourceType;
import org.influxdata.platform.domain.Run;
import org.influxdata.platform.domain.RunStatus;
import org.influxdata.platform.domain.Status;
import org.influxdata.platform.domain.Task;
import org.influxdata.platform.domain.User;
import org.influxdata.platform.domain.UserResourceMapping;
import org.influxdata.platform.error.InfluxException;

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
class ITTaskClientTest extends AbstractITClientTest {

    private static final Logger LOG = Logger.getLogger(ITTaskClientTest.class.getName());
    private static final String TASK_FLUX = "from(bucket:\"my-bucket\") |> range(start: 0) |> last()";

    private User user;
    private Organization organization;

    private TaskClient taskClient;

    @BeforeEach
    void setUp() {

        super.setUp(true);

        taskClient = platformClient.createTaskClient();
        organization = platformClient.createOrganizationClient()
                .findOrganizations().stream()
                .filter(organization -> organization.getName().equals("my-org"))
                .findFirst()
                .orElseThrow(IllegalStateException::new);

        user = platformClient.createUserClient().me();

        taskClient.findTasks().forEach(task -> taskClient.deleteTask(task));
    }

    @Test
    void createTask() {

        //TODO API cron, every in Flux

        String taskName = generateName("it task");

        String flux = "option task = {\n"
                + "    name: \"" + taskName + "\",\n"
                + "    every: 1h\n"
                + "}\n\n" + TASK_FLUX;

        Task task = new Task();
        task.setName(taskName);
        task.setOrganizationId(organization.getId());
        task.setOwner(user);
        task.setFlux(flux);
        task.setStatus(Status.ACTIVE);

        task = taskClient.createTask(task);

        Assertions.assertThat(task).isNotNull();
        Assertions.assertThat(task.getId()).isNotBlank();
        Assertions.assertThat(task.getName()).isEqualTo(taskName);
        Assertions.assertThat(task.getOwner()).isNotNull();
        Assertions.assertThat(task.getOwner().getId()).isEqualTo(user.getId());
        Assertions.assertThat(task.getOwner().getName()).isEqualTo(user.getName());
        Assertions.assertThat(task.getOrganizationId()).isEqualTo(organization.getId());
        Assertions.assertThat(task.getStatus()).isEqualTo(Status.ACTIVE);
        Assertions.assertThat(task.getEvery()).isEqualTo("1h0m0s");
        Assertions.assertThat(task.getCron()).isNull();
        Assertions.assertThat(task.getFlux()).isEqualToIgnoringWhitespace(flux);
    }

    @Test
    void createTaskEvery() {

        String taskName = generateName("it task");

        Task task = taskClient.createTaskEvery(taskName, TASK_FLUX, "1h", user, organization);

        Assertions.assertThat(task).isNotNull();
        Assertions.assertThat(task.getId()).isNotBlank();
        Assertions.assertThat(task.getName()).isEqualTo(taskName);
        Assertions.assertThat(task.getOwner()).isNotNull();
        Assertions.assertThat(task.getOwner().getId()).isEqualTo(user.getId());
        Assertions.assertThat(task.getOwner().getName()).isEqualTo(user.getName());
        Assertions.assertThat(task.getOrganizationId()).isEqualTo(organization.getId());
        Assertions.assertThat(task.getStatus()).isEqualTo(Status.ACTIVE);
        Assertions.assertThat(task.getEvery()).isEqualTo("1h0m0s");
        Assertions.assertThat(task.getCron()).isNull();
        Assertions.assertThat(task.getFlux()).endsWith(TASK_FLUX);
    }

    @Test
    void createTaskCron() {

        String taskName = generateName("it task");

        Task task = taskClient.createTaskCron(taskName, TASK_FLUX, "0 2 * * *", user, organization);

        Assertions.assertThat(task).isNotNull();
        Assertions.assertThat(task.getId()).isNotBlank();
        Assertions.assertThat(task.getName()).isEqualTo(taskName);
        Assertions.assertThat(task.getOwner()).isNotNull();
        Assertions.assertThat(task.getOwner().getId()).isEqualTo(user.getId());
        Assertions.assertThat(task.getOwner().getName()).isEqualTo(user.getName());
        Assertions.assertThat(task.getOrganizationId()).isEqualTo(organization.getId());
        Assertions.assertThat(task.getStatus()).isEqualTo(Status.ACTIVE);
        Assertions.assertThat(task.getCron()).isEqualTo("0 2 * * *");
        Assertions.assertThat(task.getEvery()).isEqualTo("0s");
        Assertions.assertThat(task.getFlux()).endsWith(TASK_FLUX);
    }

    @Test
    void findTaskByID() {

        String taskName = generateName("it task");

        Task task = taskClient.createTaskCron(taskName, TASK_FLUX, "0 2 * * *", user.getId(), organization.getId());

        Task taskByID = taskClient.findTaskByID(task.getId());
        LOG.info("TaskByID: " + taskByID);

        Assertions.assertThat(taskByID).isNotNull();
        Assertions.assertThat(taskByID.getId()).isEqualTo(task.getId());
        Assertions.assertThat(taskByID.getName()).isEqualTo(task.getName());
        Assertions.assertThat(taskByID.getOwner()).isNotNull();
        Assertions.assertThat(taskByID.getOwner().getId()).isEqualTo(task.getOwner().getId());
        Assertions.assertThat(taskByID.getOrganizationId()).isEqualTo(task.getOrganizationId());
        Assertions.assertThat(taskByID.getEvery()).isNull();
        Assertions.assertThat(taskByID.getCron()).isEqualTo(task.getCron());
        Assertions.assertThat(taskByID.getFlux()).isEqualTo(task.getFlux());
        Assertions.assertThat(taskByID.getStatus()).isEqualTo(Status.ACTIVE);
    }

    @Test
    void findTasks() {

        int size = taskClient.findTasks().size();

        Task everyTask = taskClient.createTaskEvery(generateName("it task"), TASK_FLUX, "2h", user.getId(), organization.getId());
        Assertions.assertThat(everyTask).isNotNull();

        List<Task> tasks = taskClient.findTasks();
        Assertions.assertThat(tasks).hasSize(size + 1);
        tasks.forEach(task -> Assertions.assertThat(task.getStatus()).isNotNull());
    }

    @Test
    void findTasksByUserID() {

        User taskUser = platformClient.createUserClient().createUser(generateName("TaskUser"));

        taskClient.createTaskCron(generateName("it task"), TASK_FLUX, "0 2 * * *", taskUser, organization);

        List<Task> tasks = taskClient.findTasksByUser(taskUser);
        Assertions.assertThat(tasks).hasSize(1);
    }

    @Test
    void findTasksByOrganizationID() {
        Organization taskOrganization = platformClient.createOrganizationClient().createOrganization(generateName("TaskOrg"));
        taskClient.createTaskCron(generateName("it task"), TASK_FLUX, "0 2 * * *", user, taskOrganization);

        List<Task> tasks = taskClient.findTasksByOrganization(taskOrganization);
        Assertions.assertThat(tasks).hasSize(1);
    }

    @Test
    void findTasksAfterSpecifiedID() {

        Task task1 = taskClient.createTaskCron(generateName("it task"), TASK_FLUX, "0 2 * * *", user, organization);
        Task task2 = taskClient.createTaskCron(generateName("it task"), TASK_FLUX, "0 2 * * *", user, organization);

        List<Task> tasks = taskClient.findTasks(task1.getId(), null, null);

        Assertions.assertThat(tasks).hasSize(1);
        Assertions.assertThat(tasks.get(0).getId()).isEqualTo(task2.getId());
    }

    @Test
    void deleteTask() {

        Task createdTask = taskClient.createTaskCron(generateName("it task"), TASK_FLUX, "0 2 * * *", user, organization);
        Assertions.assertThat(createdTask).isNotNull();

        Task foundTask = taskClient.findTaskByID(createdTask.getId());
        Assertions.assertThat(foundTask).isNotNull();

        // delete task
        taskClient.deleteTask(createdTask);

        foundTask = taskClient.findTaskByID(createdTask.getId());
        Assertions.assertThat(foundTask).isNull();
    }

    // TODO Enable after implement mapping background Task to Task /platform/task/platform_adapter.go:89
    @Test
    @Disabled
    void updateTask() {

        String taskName = generateName("it task");
        Task cronTask = taskClient.createTaskCron(taskName, TASK_FLUX, "0 2 * * *", user, organization);

        String flux = "option task = {\n"
                + "    name: \"" + taskName + "\",\n"
                + "    every: 2m\n"
                + "}\n\n" + TASK_FLUX;

        cronTask.setFlux(flux);
        cronTask.setStatus(Status.ACTIVE);

        Task updatedTask = taskClient.updateTask(cronTask);

        Assertions.assertThat(updatedTask).isNotNull();
        Assertions.assertThat(updatedTask.getId()).isEqualTo(cronTask.getId());
        Assertions.assertThat(updatedTask.getOwner()).isNotNull();
        Assertions.assertThat(updatedTask.getOwner().getName()).isEqualTo(cronTask.getOwner().getName());
        Assertions.assertThat(updatedTask.getEvery()).isEqualTo("2m0s");
        Assertions.assertThat(updatedTask.getCron()).isNull();
        Assertions.assertThat(updatedTask.getFlux()).isEqualTo(TASK_FLUX);
        Assertions.assertThat(updatedTask.getStatus()).isEqualTo(Status.INACTIVE);
        Assertions.assertThat(updatedTask.getOwner().getId()).isEqualTo(cronTask.getOwner().getId());
        Assertions.assertThat(updatedTask.getOrganizationId()).isEqualTo(cronTask.getOrganizationId());
        Assertions.assertThat(updatedTask.getName()).isEqualTo(cronTask.getName());
    }

    //TODO wait to fix task path ":tid" => ":id"
    @Test
    @Disabled
    void member() {

        UserClient userClient = platformClient.createUserClient();

        Task task = taskClient.createTaskCron(generateName("task"), TASK_FLUX, "0 2 * * *", user, organization);

        List<UserResourceMapping> members = taskClient.getMembers(task);
        Assertions.assertThat(members).hasSize(0);

        User user = userClient.createUser(generateName("Luke Health"));

        UserResourceMapping userResourceMapping = taskClient.addMember(user, task);
        Assertions.assertThat(userResourceMapping).isNotNull();
        Assertions.assertThat(userResourceMapping.getResourceID()).isEqualTo(task.getId());
        Assertions.assertThat(userResourceMapping.getResourceType()).isEqualTo(ResourceType.TASK_RESOURCE_TYPE);
        Assertions.assertThat(userResourceMapping.getUserID()).isEqualTo(user.getId());
        Assertions.assertThat(userResourceMapping.getUserType()).isEqualTo(UserResourceMapping.UserType.MEMBER);

        members = taskClient.getMembers(task);
        Assertions.assertThat(members).hasSize(1);
        Assertions.assertThat(members.get(0).getResourceID()).isEqualTo(task.getId());
        Assertions.assertThat(members.get(0).getResourceType()).isEqualTo(ResourceType.TASK_RESOURCE_TYPE);
        Assertions.assertThat(members.get(0).getUserID()).isEqualTo(user.getId());
        Assertions.assertThat(members.get(0).getUserType()).isEqualTo(UserResourceMapping.UserType.MEMBER);

        taskClient.deleteMember(user, task);

        members = taskClient.getMembers(task);
        Assertions.assertThat(members).hasSize(0);
    }

    //TODO wait to fix task path ":tid" => ":id"
    @Test
    @Disabled
    void owner() {

        UserClient userClient = platformClient.createUserClient();

        Task task = taskClient.createTaskCron(generateName("task"), TASK_FLUX, "0 2 * * *", user, organization);

        List<UserResourceMapping> owners = taskClient.getOwners(task);
        Assertions.assertThat(owners).hasSize(0);

        User user = userClient.createUser(generateName("Luke Health"));

        UserResourceMapping userResourceMapping = taskClient.addOwner(user, task);
        Assertions.assertThat(userResourceMapping).isNotNull();
        Assertions.assertThat(userResourceMapping.getResourceID()).isEqualTo(task.getId());
        Assertions.assertThat(userResourceMapping.getResourceType()).isEqualTo(ResourceType.TASK_RESOURCE_TYPE);
        Assertions.assertThat(userResourceMapping.getUserID()).isEqualTo(user.getId());
        Assertions.assertThat(userResourceMapping.getUserType()).isEqualTo(UserResourceMapping.UserType.OWNER);

        owners = taskClient.getOwners(task);
        Assertions.assertThat(owners).hasSize(1);
        Assertions.assertThat(owners.get(0).getResourceID()).isEqualTo(task.getId());
        Assertions.assertThat(owners.get(0).getResourceType()).isEqualTo(ResourceType.TASK_RESOURCE_TYPE);
        Assertions.assertThat(owners.get(0).getUserID()).isEqualTo(user.getId());
        Assertions.assertThat(owners.get(0).getUserType()).isEqualTo(UserResourceMapping.UserType.OWNER);

        taskClient.deleteOwner(user, task);

        owners = taskClient.getOwners(task);
        Assertions.assertThat(owners).hasSize(0);
    }

    //TODO wait to fix task path ":tid" => ":id"
    @Test
    @Disabled
    void runs() throws Exception {

        String taskName = generateName("it task");

        Task task = taskClient.createTaskEvery(taskName, TASK_FLUX, "5s", user, organization);

        Thread.sleep(7_000);

        List<Run> runs = taskClient.getRuns(task);
        Assertions.assertThat(runs).hasSize(1);

        Run run = runs.get(0);

        Assertions.assertThat(run.getId()).isNotBlank();
        Assertions.assertThat(run.getTaskId()).isEqualTo(task.getId());
        Assertions.assertThat(run.getStatus()).isEqualTo(RunStatus.SUCCESS);
        Assertions.assertThat(run.getScheduledFor()).isBefore(Instant.now());
        Assertions.assertThat(run.getStartedAt()).isBefore(Instant.now());
        Assertions.assertThat(run.getFinishedAt()).isBefore(Instant.now());
        Assertions.assertThat(run.getRequestedAt()).isNull();
        Assertions.assertThat(run.getLog()).isEmpty();
    }

    //TODO wait to fix task path ":tid" => ":id"
    @Test
    @Disabled
    void runsNotExist() {

        List<Run> runs = taskClient.getRuns("020f755c3c082000", organization.getId());
        Assertions.assertThat(runs).hasSize(0);
    }

    //TODO wait to fix task path ":tid" => ":id"
    @Test
    @Disabled
    void runsByTime() throws InterruptedException {

        Instant now = Instant.now();

        String taskName = generateName("it task");

        Task task = taskClient.createTaskEvery(taskName, TASK_FLUX, "5s", user, organization);

        List<Run> runs = taskClient.getRuns(task, null, now, null);
        Assertions.assertThat(runs).hasSize(0);

        Thread.sleep(7_000);

        runs = taskClient.getRuns(task, now, null, null);
        Assertions.assertThat(runs).hasSize(1);
    }

    //TODO wait to fix task path ":tid" => ":id"
    @Test
    @Disabled
    void runsLimit() throws InterruptedException {

        String taskName = generateName("it task");

        Task task = taskClient.createTaskEvery(taskName, TASK_FLUX, "1s", user, organization);

        Thread.sleep(5_000);

        List<Run> runs = taskClient.getRuns(task, null, null, 1);
        Assertions.assertThat(runs).hasSize(1);

        runs = taskClient.getRuns(task, null, null, null);
        Assertions.assertThat(runs.size()).isGreaterThan(1);
    }

    //TODO wait to fix (task/backend/query_logreader.go:149) - FindRunByID (avoid "panic: column _measurement is not of type time goroutine")
    @Test
    @Disabled
    void run() throws InterruptedException {

        String taskName = generateName("it task");

        Task task = taskClient.createTaskEvery(taskName, TASK_FLUX, "1s", user, organization);

        Thread.sleep(5_000);

        List<Run> runs = taskClient.getRuns(task, null, null, 1);
        Assertions.assertThat(runs).hasSize(1);

        Run firstRun = runs.get(0);
        Run runById = taskClient.getRun(firstRun);

        Assertions.assertThat(runById).isNotNull();
        Assertions.assertThat(runById.getId()).isEqualTo(firstRun.getId());
    }

    //TODO wait to fix task path ":tid" => ":id"
    @Test
    @Disabled
    void runNotExist() {

        String taskName = generateName("it task");

        Task task = taskClient.createTaskEvery(taskName, TASK_FLUX, "5s", user, organization);

        Run run = taskClient.getRun(task.getId(), "020f755c3c082000");
        Assertions.assertThat(run).isNull();
    }

    //TODO wait to fix (task/backend/query_logreader.go:149) - FindRunByID (avoid "panic: column _measurement is not of type time goroutine")
    @Test
    @Disabled
    void retry() throws InterruptedException {

        String taskName = generateName("it task");

        Task task = taskClient.createTaskEvery(taskName, TASK_FLUX, "4s", user, organization);

        Thread.sleep(5_000);

        List<Run> runs = taskClient.getRuns(task);
        Assertions.assertThat(runs).isNotEmpty();

        Run run = taskClient.retryRun(runs.get(0));

        Assertions.assertThat(run).isNotNull();
        Assertions.assertThat(run.getId()).isEqualTo(runs.get(0).getId());

        Assertions.assertThat(runs.size()).isLessThan(taskClient.getRuns(task).size());
    }

    //TODO wait to fix task path ":tid" => ":id"
    @Test
    @Disabled
    void retryNotExist() {

        String taskName = generateName("it task");

        Task task = taskClient.createTaskEvery(taskName, TASK_FLUX, "5s", user, organization);

        Assertions.assertThatThrownBy(() ->  taskClient.retryRun(task.getId(), "020f755c3c082000"))
                .isInstanceOf(InfluxException.class)
                .hasMessage("expected one run, got 0");
    }

    //TODO wait to fix task path ":tid" => ":id"
    @Test
    @Disabled
    void logs() throws Exception {

        String taskName = generateName("it task");

        Task task = taskClient.createTaskEvery(taskName, TASK_FLUX, "5s", user, organization);

        Thread.sleep(7_000);

        List<String> logs = taskClient.getLogs(task);
        Assertions.assertThat(logs).hasSize(1);
        Assertions.assertThat(logs.get(0)).endsWith("Completed successfully");
    }

    //TODO wait to fix task path ":tid" => ":id"
    @Test
    @Disabled
    void logsNotExist() {

        List<String> logs = taskClient.getLogs("020f755c3c082000", organization.getId());

        Assertions.assertThat(logs).isEmpty();
    }

    //TODO wait to fix task path ":tid" => ":id"
    @Test
    @Disabled
    void runLogs() throws Exception {

        String taskName = generateName("it task");

        Task task = taskClient.createTaskEvery(taskName, TASK_FLUX, "1s", user, organization);

        Thread.sleep(2_000);

        List<Run> runs = taskClient.getRuns(task, null, null, 1);
        Assertions.assertThat(runs).hasSize(1);

        List<String> logs = taskClient.getRunLogs(runs.get(0), organization.getId());

        Assertions.assertThat(logs).hasSize(1);
    }

    //TODO wait to fix task path ":tid" => ":id"
    @Test
    @Disabled
    void runLogsNotExist() {

        String taskName = generateName("it task");

        Task task = taskClient.createTaskEvery(taskName, TASK_FLUX, "5s", user, organization);

        List<String> logs = taskClient.getRunLogs(task.getId(), "020f755c3c082000", organization.getId());
        Assertions.assertThat(logs).isEmpty();
    }

    //TODO wait to change pAdapter{s: s, r: r} => pAdapter{s: s, r: r, rc: rc} (platform_adapter.go)
    @Test
    @Disabled
    void cancelRun() throws InterruptedException {

        String taskName = generateName("it task");

        Task task = taskClient.createTaskEvery(taskName, TASK_FLUX, "1s", user, organization);

        Thread.sleep(2_000);

        List<Run> runs = taskClient.getRuns(task);
        taskClient.cancelRun(runs.get(0));

        Run canceledRun = taskClient.getRun(runs.get(0));
        Assertions.assertThat(canceledRun).isNotNull();
        Assertions.assertThat(canceledRun.getStatus()).isEqualTo(RunStatus.FAILED);
    }

    //TODO wait to change pAdapter{s: s, r: r} => pAdapter{s: s, r: r, rc: rc} (platform_adapter.go)
    @Test
    @Disabled
    void cancelRunNotExist() {

        Assertions.assertThatThrownBy(() -> taskClient.cancelRun("020f755c3c082000", "020f755c3c082000"))
                .isInstanceOf(InfluxException.class)
                .hasMessage("task not found");
    }
}
