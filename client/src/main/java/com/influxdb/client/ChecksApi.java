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

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import com.influxdb.client.domain.Check;
import com.influxdb.client.domain.CheckPatch;
import com.influxdb.client.domain.CheckStatusLevel;
import com.influxdb.client.domain.Checks;
import com.influxdb.client.domain.DeadmanCheck;
import com.influxdb.client.domain.Label;
import com.influxdb.client.domain.LabelResponse;
import com.influxdb.client.domain.Threshold;
import com.influxdb.client.domain.ThresholdCheck;

/**
 * The client of the InfluxDB 2.0 that implement Check Api.
 *
 * @author Jakub Bednar (18/09/2019 08:07)
 */
@ThreadSafe
public interface ChecksApi {

    /**
     * Add new Threshold check.
     *
     * @param name            the check name
     * @param query           The text of the flux query
     * @param every           Check repetition interval
     * @param messageTemplate template that is used to generate and write a status message
     * @param threshold      condition for that specific status
     * @param orgID           the organization that owns this check
     * @return ThresholdCheck created
     */
    @Nonnull
    ThresholdCheck createThresholdCheck(@Nonnull final String name,
                                        @Nonnull final String query,
                                        @Nonnull final String every,
                                        @Nonnull final String messageTemplate,
                                        @Nonnull final Threshold threshold,
                                        @Nonnull final String orgID);

    /**
     * Add new Threshold check.
     *
     * @param name            the check name
     * @param query           The text of the flux query
     * @param every           Check repetition interval
     * @param messageTemplate template that is used to generate and write a status message
     * @param thresholds      conditions for that specific status
     * @param orgID           the organization that owns this check
     * @return ThresholdCheck created
     */
    @Nonnull
    ThresholdCheck createThresholdCheck(@Nonnull final String name,
                                        @Nonnull final String query,
                                        @Nonnull final String every,
                                        @Nonnull final String messageTemplate,
                                        @Nonnull final List<Threshold> thresholds,
                                        @Nonnull final String orgID);


    /**
     * Add new Deadman check.
     *
     * @param name            the check name
     * @param query           The text of the flux query
     * @param every           Check repetition interval
     * @param timeSince       string duration before deadman triggers
     * @param staleTime       string duration for time that a series is considered stale and should not trigger deadman
     * @param messageTemplate template that is used to generate and write a status message
     * @param level           the state to record if check matches a criteria
     * @param orgID           the organization that owns this check
     * @return DeadmanCheck created
     */
    @Nonnull
    DeadmanCheck createDeadmanCheck(@Nonnull final String name,
                                    @Nonnull final String query,
                                    @Nonnull final String every,
                                    @Nonnull final String timeSince,
                                    @Nonnull final String staleTime,
                                    @Nonnull final String messageTemplate,
                                    @Nonnull final CheckStatusLevel level,
                                    @Nonnull final String orgID);

    /**
     * Add new check.
     *
     * @param check check to create
     * @return Check created
     */
    @Nonnull
    Check createCheck(@Nonnull final Check check);

    /**
     * Update a check.
     *
     * @param check check update to apply
     * @return An updated check
     */
    @Nonnull
    Check updateCheck(@Nonnull final Check check);

    /**
     * Update a check.
     *
     * @param checkID ID of check
     * @param patch   update to apply
     * @return An updated check
     */
    @Nonnull
    Check updateCheck(@Nonnull final String checkID, @Nonnull final CheckPatch patch);

    /**
     * Delete a check.
     *
     * @param check the check to delete
     */
    void deleteCheck(@Nonnull final Check check);

    /**
     * Delete a check.
     *
     * @param checkID the ID of check to delete
     */
    void deleteCheck(@Nonnull final String checkID);

    /**
     * Get a check.
     *
     * @param checkID ID of check
     * @return the check requested
     */
    @Nonnull
    Check findCheckByID(@Nonnull final String checkID);

    /**
     * Get checks.
     *
     * @param orgID only show checks belonging to specified organization
     * @return A list of checks
     */
    @Nonnull
    List<Check> findChecks(@Nonnull final String orgID);

    /**
     * Get all checks.
     *
     * @param orgID       only show checks belonging to specified organization
     * @param findOptions find options
     * @return A list of checks
     */
    @Nonnull
    Checks findChecks(@Nonnull final String orgID, @Nonnull final FindOptions findOptions);

    /**
     * List all labels for a check.
     *
     * @param check the check
     * @return a list of all labels for a check
     */
    @Nonnull
    List<Label> getLabels(@Nonnull final Check check);

    /**
     * List all labels for a check.
     *
     * @param checkID ID of the check
     * @return a list of all labels for a check
     */
    @Nonnull
    List<Label> getLabels(@Nonnull final String checkID);

    /**
     * Add a label to a check.
     *
     * @param label label to add
     * @param check the check
     * @return the label was added to the check
     */
    @Nonnull
    LabelResponse addLabel(@Nonnull final Label label, @Nonnull final Check check);

    /**
     * Add a label to a check.
     *
     * @param labelID ID of label to add
     * @param checkID ID of the check
     * @return the label was added to the check
     */
    @Nonnull
    LabelResponse addLabel(@Nonnull final String labelID, @Nonnull final String checkID);

    /**
     * Delete label from a check.
     *
     * @param label the label to delete
     * @param check he check
     */
    void deleteLabel(@Nonnull final Label label, @Nonnull final Check check);

    /**
     * Delete label from a check.
     *
     * @param labelID the label id to delete
     * @param checkID ID of the check
     */
    void deleteLabel(@Nonnull final String labelID, @Nonnull final String checkID);
}
