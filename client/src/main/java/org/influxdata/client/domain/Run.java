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
package org.influxdata.client.domain;

import java.time.Instant;
import java.util.List;

/**
 * The run of a {@link Task}.
 *
 * @author Jakub Bednar (bednar@github) (22/10/2018 09:05)
 */
public final class Run {

    private String id;

    private String taskID;

    private RunStatus status;

    private Instant scheduledFor;
    private Instant startedAt;
    private Instant finishedAt;
    private Instant requestedAt;

    private List<LogEvent> log;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getTaskID() {
        return taskID;
    }

    public void setTaskID(final String taskID) {
        this.taskID = taskID;
    }

    public RunStatus getStatus() {
        return status;
    }

    public void setStatus(final RunStatus status) {
        this.status = status;
    }

    public Instant getScheduledFor() {
        return scheduledFor;
    }

    public void setScheduledFor(final Instant scheduledFor) {
        this.scheduledFor = scheduledFor;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(final Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(final Instant finishedAt) {
        this.finishedAt = finishedAt;
    }

    public Instant getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(final Instant requestedAt) {
        this.requestedAt = requestedAt;
    }

    public List<LogEvent> getLog() {
        return log;
    }

    public void setLog(final List<LogEvent> log) {
        this.log = log;
    }
}