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

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.influxdata.platform.domain.Task;

/**
 * The client of the InfluxData Platform that implement Task HTTP API endpoint.
 *
 * @author Jakub Bednar (bednar@github) (11/09/2018 07:54)
 */
public interface TaskClient {

    /**
     * Creates a new task.
     *
     * @param task the task to create
     * @return Task created
     */
    @Nonnull
    Task createTask(@Nonnull final Task task);

    /**
     * Creates a new task with task repetition by cron.
     *
     * @param name           description of the task
     * @param flux           the Flux script to run for this task
     * @param cron           a task repetition schedule in the form '* * * * * *'
     * @param userID         an id of the user that owns this Task
     * @param organizationID an id of the organization that owns this Task
     * @return Task created
     */
    @Nonnull
    Task createTaskCron(@Nonnull final String name,
                        @Nonnull final String flux,
                        @Nonnull final String cron,
                        @Nonnull final String userID,
                        @Nonnull final String organizationID);

    /**
     * Creates a new task with task repetition by duration expression ("1h", "30s").
     *
     * @param name           description of the task
     * @param flux           the Flux script to run for this task
     * @param every          a task repetition by duration expression
     * @param userID         an id of the user that owns this Task
     * @param organizationID an id of the organization that owns this Task
     * @return Task created
     */
    @Nonnull
    Task createTaskEvery(@Nonnull final String name,
                         @Nonnull final String flux,
                         @Nonnull final String every,
                         @Nonnull final String userID,
                         @Nonnull final String organizationID);

    /**
     * Update a task. This will cancel all queued runs.
     *
     * @param task task update to apply
     * @return task updated
     */
    @Nonnull
    Task updateTask(@Nonnull final Task task);

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
     * Retrieve an task.
     *
     * @param taskID ID of task to get
     * @return task details
     */
    @Nullable
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
     * @param userID filter tasks to a specific user id
     * @return A list of tasks
     */
    @Nonnull
    List<Task> findTasksByUserID(@Nullable final String userID);

    /**
     * Lists tasks, limit 100.
     *
     * @param organizationID filter tasks to a specific organization id
     * @return A list of tasks
     */
    @Nonnull
    List<Task> findTasksByOrganizationID(@Nullable final String organizationID);

    /**
     * Lists tasks, limit 100.
     *
     * @param afterID        returns tasks after specified ID
     * @param userID         filter tasks to a specific user id
     * @param organizationID filter tasks to a specific organization id
     * @return A list of tasks
     */
    @Nonnull
    List<Task> findTasks(@Nullable final String afterID,
                         @Nullable final String userID,
                         @Nullable final String organizationID);
}