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
package org.influxdata.platform.domain;

import java.io.Serializable;
import java.util.StringJoiner;

/**
 * Task is a task.
 *
 * TODO missing links?
 *
 * @author Jakub Bednar (bednar@github) (05/09/2018 08:09)
 */
public final class Task implements Serializable {

    private String id;

    /**
     * A read-only description of the task.
     */
    private String name;

    /**
     * The {@link User} that owns this Task.
     */
    private User owner;

    /**
     * The ID of the organization that owns this Task.
     */
    private String organizationId;

    /**
     * The current status of the task. When updated to 'disabled', cancels all queued jobs of this task.
     */
    private Status status;

    /**
     * The Flux script to run for this task.
     */
    private String flux;

    /**
     * A simple task repetition schedule (duration type); parsed from Flux.
     */
    private String every;

    /**
     * A task repetition schedule in the form '* * * * * *'; parsed from Flux.
     */
    private String cron;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(final User owner) {
        this.owner = owner;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(final String organizationId) {
        this.organizationId = organizationId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(final Status status) {
        this.status = status;
    }

    public String getFlux() {
        return flux;
    }

    public void setFlux(final String flux) {
        this.flux = flux;
    }

    public String getEvery() {
        return every;
    }

    public void setEvery(final String every) {
        this.every = every;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(final String cron) {
        this.cron = cron;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Task.class.getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .add("name='" + name + "'")
                .add("owner=" + owner)
                .add("organizationId='" + organizationId + "'")
                .add("status=" + status)
                .add("flux='" + flux + "'")
                .add("every='" + every + "'")
                .add("cron='" + cron + "'")
                .toString();
    }
}