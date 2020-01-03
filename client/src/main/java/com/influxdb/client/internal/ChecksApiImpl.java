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

import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

import com.influxdb.Arguments;
import com.influxdb.client.ChecksApi;
import com.influxdb.client.FindOptions;
import com.influxdb.client.domain.BuilderConfig;
import com.influxdb.client.domain.BuilderTagsType;
import com.influxdb.client.domain.Check;
import com.influxdb.client.domain.CheckPatch;
import com.influxdb.client.domain.CheckStatusLevel;
import com.influxdb.client.domain.Checks;
import com.influxdb.client.domain.DashboardQuery;
import com.influxdb.client.domain.DeadmanCheck;
import com.influxdb.client.domain.Label;
import com.influxdb.client.domain.LabelMapping;
import com.influxdb.client.domain.LabelResponse;
import com.influxdb.client.domain.LabelsResponse;
import com.influxdb.client.domain.QueryEditMode;
import com.influxdb.client.domain.TaskStatusType;
import com.influxdb.client.domain.Threshold;
import com.influxdb.client.domain.ThresholdCheck;
import com.influxdb.client.service.ChecksService;
import com.influxdb.internal.AbstractRestClient;
import retrofit2.Call;

/**
 * @author Jakub Bednar (18/09/2019 08:22)
 */
final class ChecksApiImpl extends AbstractRestClient implements ChecksApi {

    private final ChecksService service;

    ChecksApiImpl(final ChecksService service) {
        Arguments.checkNotNull(service, "checksService");

        this.service = service;
    }

    @Nonnull
    @Override
    public ThresholdCheck createThresholdCheck(@Nonnull final String name,
                                               @Nonnull final String query,
                                               @Nonnull final String field,
                                               @Nonnull final String every,
                                               @Nonnull final String messageTemplate,
                                               @Nonnull final Threshold threshold,
                                               @Nonnull final String orgID) {

        Arguments.checkNotNull(threshold, "threshold");

        List<Threshold> thresholds = Collections.singletonList(threshold);

        return createThresholdCheck(name, query, field, every, messageTemplate, thresholds, orgID);
    }

    @Nonnull
    @Override
    public ThresholdCheck createThresholdCheck(@Nonnull final String name,
                                               @Nonnull final String query,
                                               @Nonnull final String field,
                                               @Nonnull final String every,
                                               @Nonnull final String messageTemplate,
                                               @Nonnull final List<Threshold> thresholds,
                                               @Nonnull final String orgID) {

        Arguments.checkNonEmpty(name, "name");
        Arguments.checkNonEmpty(query, "query");
        Arguments.checkNonEmpty(field, "field");
        Arguments.checkDuration(every, "every");
        Arguments.checkNonEmpty(messageTemplate, "messageTemplate");
        Arguments.checkNotNull(thresholds, "thresholds");
        Arguments.checkNonEmpty(orgID, "orgID");

        ThresholdCheck threshold = new ThresholdCheck();
        threshold
                .thresholds(thresholds)
                .name(name)
                .orgID(orgID)
                .every(every)
                .query(createDashboardQuery(query, field))
                .statusMessageTemplate(messageTemplate)
                .status(TaskStatusType.ACTIVE);

        return (ThresholdCheck) createCheck(threshold);
    }

    @Nonnull
    @Override
    public DeadmanCheck createDeadmanCheck(@Nonnull final String name,
                                           @Nonnull final String query,
                                           @Nonnull final String field,
                                           @Nonnull final String every,
                                           @Nonnull final String timeSince,
                                           @Nonnull final String staleTime,
                                           @Nonnull final String messageTemplate,
                                           @Nonnull final CheckStatusLevel level,
                                           @Nonnull final String orgID) {

        Arguments.checkNonEmpty(name, "name");
        Arguments.checkNonEmpty(query, "query");
        Arguments.checkNonEmpty(field, "field");
        Arguments.checkDuration(every, "every");
        Arguments.checkDuration(timeSince, "timeSince");
        Arguments.checkDuration(staleTime, "staleTime");
        Arguments.checkNonEmpty(messageTemplate, "messageTemplate");
        Arguments.checkNotNull(level, "level");
        Arguments.checkNonEmpty(orgID, "orgID");

        DeadmanCheck deadman = new DeadmanCheck();
        deadman
                .level(level)
                .staleTime(staleTime)
                .timeSince(timeSince)
                .name(name)
                .every(every)
                .orgID(orgID)
                .query(createDashboardQuery(query, field))
                .statusMessageTemplate(messageTemplate)
                .status(TaskStatusType.ACTIVE);

        return (DeadmanCheck) createCheck(deadman);
    }

    @Override
    @Nonnull
    public Check createCheck(@Nonnull final Check check) {

        Arguments.checkNotNull(check, "check");

        Call<Check> call = service.createCheck(check);

        return execute(call);
    }

    @Nonnull
    @Override
    public Check updateCheck(@Nonnull final Check check) {

        Arguments.checkNotNull(check, "check");

        CheckPatch patch = new CheckPatch()
                .name(check.getName())
                .description(check.getDescription())
                .status(CheckPatch.StatusEnum.fromValue(check.getStatus().getValue()));

        return updateCheck(check.getId(), patch);
    }

    @Nonnull
    @Override
    public Check updateCheck(@Nonnull final String checkID, @Nonnull final CheckPatch patch) {

        Arguments.checkNotNull(checkID, "checkID");
        Arguments.checkNotNull(patch, "patch");

        Call<Check> call = service.patchChecksID(checkID, patch, null);

        return execute(call);
    }

    @Override
    public void deleteCheck(@Nonnull final Check check) {

        Arguments.checkNotNull(check, "check");

        deleteCheck(check.getId());
    }

    @Override
    public void deleteCheck(@Nonnull final String checkID) {

        Arguments.checkNotNull(checkID, "checkID");

        Call<Void> call = service.deleteChecksID(checkID, null);

        execute(call);
    }

    @Nonnull
    @Override
    public Check findCheckByID(@Nonnull final String checkID) {

        Arguments.checkNotNull(checkID, "checkID");

        Call<Check> call = service.getChecksID(checkID, null);

        return execute(call);
    }

    @Nonnull
    @Override
    public List<Check> findChecks(@Nonnull final String orgID) {

        Arguments.checkNonEmpty(orgID, "orgID");

        return findChecks(orgID, new FindOptions()).getChecks();
    }

    @Nonnull
    @Override
    public Checks findChecks(@Nonnull final String orgID, @Nonnull final FindOptions findOptions) {

        Arguments.checkNonEmpty(orgID, "orgID");
        Arguments.checkNotNull(findOptions, "findOptions");

        Call<Checks> call = service.getChecks(orgID, null, findOptions.getOffset(), findOptions.getLimit());
        return execute(call);
    }

    @Nonnull
    @Override
    public List<Label> getLabels(@Nonnull final Check check) {
        Arguments.checkNotNull(check, "check");

        return getLabels(check.getId());
    }

    @Nonnull
    @Override
    public List<Label> getLabels(@Nonnull final String checkID) {
        Arguments.checkNonEmpty(checkID, "checkID");

        Call<LabelsResponse> call = service.getChecksIDLabels(checkID, null);

        return execute(call).getLabels();
    }

    @Nonnull
    @Override
    public LabelResponse addLabel(@Nonnull final Label label, @Nonnull final Check check) {

        Arguments.checkNotNull(label, "label");
        Arguments.checkNotNull(check, "check");

        return addLabel(label.getId(), check.getId());
    }

    @Nonnull
    @Override
    public LabelResponse addLabel(@Nonnull final String labelID, @Nonnull final String checkID) {

        Arguments.checkNonEmpty(labelID, "labelID");
        Arguments.checkNonEmpty(checkID, "checkID");

        LabelMapping labelMapping = new LabelMapping();
        labelMapping.setLabelID(labelID);

        Call<LabelResponse> call = service.postChecksIDLabels(checkID, labelMapping, null);

        return execute(call);
    }

    @Override
    public void deleteLabel(@Nonnull final Label label, @Nonnull final Check check) {

        Arguments.checkNotNull(label, "label");
        Arguments.checkNotNull(check, "check");

        deleteLabel(label.getId(), check.getId());
    }

    @Override
    public void deleteLabel(@Nonnull final String labelID, @Nonnull final String checkID) {

        Arguments.checkNonEmpty(labelID, "labelID");
        Arguments.checkNonEmpty(checkID, "checkID");

        Call<Void> call = service.deleteChecksIDLabelsID(checkID, labelID, null);
        execute(call);
    }

    @Nonnull
    private DashboardQuery createDashboardQuery(@Nonnull final String query, @Nonnull final String field) {
        return new DashboardQuery()
                .editMode(QueryEditMode.ADVANCED)
                .text(query)
                .builderConfig(new BuilderConfig()
                        .addTagsItem(new BuilderTagsType().key("_field").addValuesItem(field))
                )
                ;
    }
}
