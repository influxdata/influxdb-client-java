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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import com.influxdb.client.domain.Check;
import com.influxdb.client.domain.CheckPatch;
import com.influxdb.client.domain.CheckStatusLevel;
import com.influxdb.client.domain.Checks;
import com.influxdb.client.domain.DeadmanCheck;
import com.influxdb.client.domain.GreaterThreshold;
import com.influxdb.client.domain.Label;
import com.influxdb.client.domain.LesserThreshold;
import com.influxdb.client.domain.RangeThreshold;
import com.influxdb.client.domain.TaskStatusType;
import com.influxdb.client.domain.Threshold;
import com.influxdb.client.domain.ThresholdCheck;
import com.influxdb.exceptions.NotFoundException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (18/09/2019 08:30)
 */
@RunWith(JUnitPlatform.class)
class ITChecksApi extends AbstractITClientTest {

    private ChecksApi checksApi;
    private String orgID;

    @BeforeEach
    void setUp() {
        checksApi = influxDBClient.getChecksApi();
        orgID = findMyOrg().getId();

        checksApi.findChecks(orgID, new FindOptions())
                .getChecks()
                .stream()
                .filter(check -> check.getName().endsWith("-IT"))
                .forEach(check -> checksApi.deleteCheck(check));
    }

    @Test
    public void createThresholdCheck() {

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        GreaterThreshold greater = new GreaterThreshold();
        greater.value(80F).level(CheckStatusLevel.CRIT).allValues(true);

        LesserThreshold lesser = new LesserThreshold();
        lesser.value(20F).level(CheckStatusLevel.OK);

        RangeThreshold range = new RangeThreshold();
        range.min(50F).max(70F).level(CheckStatusLevel.WARN);

        List<Threshold> thresholds = Arrays.asList(greater, lesser, range);
        ThresholdCheck threshold = checksApi.createThresholdCheck(generateName("th-check"),
                "from(bucket: \"foo\") |> range(start: -1d, stop: now()) |> aggregateWindow(every: 1m, fn: mean) |> yield()",
                "usage_user",
                "1h",
                "Check: ${ r._check_name } is: ${ r._level }",
                thresholds,
                orgID);

        Assertions.assertThat(threshold).isNotNull();
        Assertions.assertThat(threshold.getType()).isEqualTo(ThresholdCheck.TypeEnum.THRESHOLD);
        Assertions.assertThat(threshold.getThresholds()).hasSize(3);

        Assertions.assertThat(threshold.getThresholds().get(0)).isInstanceOf(GreaterThreshold.class);
        GreaterThreshold greaterThreshold = (GreaterThreshold) threshold.getThresholds().get(0);
        Assertions.assertThat(greaterThreshold.getType()).isEqualTo(GreaterThreshold.TypeEnum.GREATER);
        Assertions.assertThat(greaterThreshold.getValue()).isEqualTo(80F);
        Assertions.assertThat(greaterThreshold.getLevel()).isEqualTo(CheckStatusLevel.CRIT);
        Assertions.assertThat(greaterThreshold.getAllValues()).isTrue();

        Assertions.assertThat(threshold.getThresholds().get(1)).isInstanceOf(LesserThreshold.class);
        LesserThreshold lesserThreshold = (LesserThreshold) threshold.getThresholds().get(1);
        Assertions.assertThat(lesserThreshold.getType()).isEqualTo(LesserThreshold.TypeEnum.LESSER);
        Assertions.assertThat(lesserThreshold.getValue()).isEqualTo(20F);
        Assertions.assertThat(lesserThreshold.getLevel()).isEqualTo(CheckStatusLevel.OK);
        Assertions.assertThat(lesserThreshold.getAllValues()).isFalse();

        Assertions.assertThat(threshold.getThresholds().get(2)).isInstanceOf(RangeThreshold.class);
        RangeThreshold rangeThreshold = (RangeThreshold) threshold.getThresholds().get(2);
        Assertions.assertThat(rangeThreshold.getType()).isEqualTo(RangeThreshold.TypeEnum.RANGE);
        Assertions.assertThat(rangeThreshold.getMin()).isEqualTo(50F);
        Assertions.assertThat(rangeThreshold.getMax()).isEqualTo(70F);
        Assertions.assertThat(rangeThreshold.getLevel()).isEqualTo(CheckStatusLevel.WARN);
        Assertions.assertThat(rangeThreshold.getAllValues()).isFalse();
        Assertions.assertThat(rangeThreshold.getWithin()).isFalse();

        Assertions.assertThat(threshold.getId()).isNotBlank();
        Assertions.assertThat(threshold.getName()).startsWith("th-check");
        Assertions.assertThat(threshold.getOrgID()).isEqualTo(orgID);
        Assertions.assertThat(threshold.getCreatedAt()).isAfter(now);
        Assertions.assertThat(threshold.getUpdatedAt()).isAfter(now);
        Assertions.assertThat(threshold.getQuery()).isNotNull();
        Assertions.assertThat(threshold.getQuery().getText()).isEqualTo("from(bucket: \"foo\") |> range(start: -1d, stop: now()) |> aggregateWindow(every: 1m, fn: mean) |> yield()");
        Assertions.assertThat(threshold.getQuery().getBuilderConfig().getTags().get(0).getKey()).isEqualTo("_field");
        Assertions.assertThat(threshold.getQuery().getBuilderConfig().getTags().get(0).getValues()).hasSize(1);
        Assertions.assertThat(threshold.getQuery().getBuilderConfig().getTags().get(0).getValues().get(0)).isEqualTo("usage_user");
        Assertions.assertThat(threshold.getStatus()).isEqualTo(TaskStatusType.ACTIVE);
        Assertions.assertThat(threshold.getEvery()).isEqualTo("1h");
        Assertions.assertThat(threshold.getOffset()).isBlank();
        Assertions.assertThat(threshold.getTags()).isEmpty();
        Assertions.assertThat(threshold.getDescription()).isBlank();
        Assertions.assertThat(threshold.getStatusMessageTemplate()).isEqualTo("Check: ${ r._check_name } is: ${ r._level }");
        Assertions.assertThat(threshold.getLabels()).isEmpty();
        Assertions.assertThat(threshold.getLinks()).isNotNull();
        Assertions.assertThat(threshold.getLinks().getSelf())
                .isEqualTo(String.format("/api/v2/checks/%s", threshold.getId()));
        Assertions.assertThat(threshold.getLinks().getLabels())
                .isEqualTo(String.format("/api/v2/checks/%s/labels", threshold.getId()));
        Assertions.assertThat(threshold.getLinks().getMembers())
                .isEqualTo(String.format("/api/v2/checks/%s/members", threshold.getId()));
        Assertions.assertThat(threshold.getLinks().getOwners())
                .isEqualTo(String.format("/api/v2/checks/%s/owners", threshold.getId()));
    }

    @Test
    public void createDeadmanCheck() {

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        DeadmanCheck deadman = checksApi.createDeadmanCheck(generateName("deadman-check"),
                "from(bucket: \"foo\") |> range(start: -1d, stop: now()) |> aggregateWindow(every: 1m, fn: mean) |> yield()",
                "usage_user",
                "15m",
                "90s",
                "10m",
                "Check: ${ r._check_name } is: ${ r._level }",
                CheckStatusLevel.CRIT,
                orgID);

        Assertions.assertThat(deadman).isNotNull();
        Assertions.assertThat(deadman.getType()).isEqualTo(DeadmanCheck.TypeEnum.DEADMAN);
        Assertions.assertThat(deadman.getTimeSince()).isEqualTo("90s");
        Assertions.assertThat(deadman.getStaleTime()).isEqualTo("10m");
        Assertions.assertThat(deadman.getLevel()).isEqualTo(CheckStatusLevel.CRIT);

        Assertions.assertThat(deadman.getId()).isNotBlank();
        Assertions.assertThat(deadman.getName()).startsWith("deadman-check");
        Assertions.assertThat(deadman.getOrgID()).isEqualTo(orgID);
        Assertions.assertThat(deadman.getCreatedAt()).isAfter(now);
        Assertions.assertThat(deadman.getUpdatedAt()).isAfter(now);
        Assertions.assertThat(deadman.getQuery()).isNotNull();
        Assertions.assertThat(deadman.getQuery().getText()).isEqualTo("from(bucket: \"foo\") |> range(start: -1d, stop: now()) |> aggregateWindow(every: 1m, fn: mean) |> yield()");
        Assertions.assertThat(deadman.getQuery().getBuilderConfig().getTags().get(0).getKey()).isEqualTo("_field");
        Assertions.assertThat(deadman.getQuery().getBuilderConfig().getTags().get(0).getValues()).hasSize(1);
        Assertions.assertThat(deadman.getQuery().getBuilderConfig().getTags().get(0).getValues().get(0)).isEqualTo("usage_user");
        Assertions.assertThat(deadman.getStatus()).isEqualTo(TaskStatusType.ACTIVE);
        Assertions.assertThat(deadman.getEvery()).isEqualTo("15m");
        Assertions.assertThat(deadman.getOffset()).isBlank();
        Assertions.assertThat(deadman.getTags()).isEmpty();
        Assertions.assertThat(deadman.getDescription()).isBlank();
        Assertions.assertThat(deadman.getStatusMessageTemplate()).isEqualTo("Check: ${ r._check_name } is: ${ r._level }");
        Assertions.assertThat(deadman.getLabels()).isEmpty();
        Assertions.assertThat(deadman.getLinks()).isNotNull();
        Assertions.assertThat(deadman.getLinks().getSelf())
                .isEqualTo(String.format("/api/v2/checks/%s", deadman.getId()));
        Assertions.assertThat(deadman.getLinks().getLabels())
                .isEqualTo(String.format("/api/v2/checks/%s/labels", deadman.getId()));
        Assertions.assertThat(deadman.getLinks().getMembers())
                .isEqualTo(String.format("/api/v2/checks/%s/members", deadman.getId()));
        Assertions.assertThat(deadman.getLinks().getOwners())
                .isEqualTo(String.format("/api/v2/checks/%s/owners", deadman.getId()));
    }

    @Test
    public void updateCheck() {

        GreaterThreshold greater = new GreaterThreshold();
        greater.value(80F).level(CheckStatusLevel.CRIT).allValues(true);

        ThresholdCheck check = checksApi.createThresholdCheck(generateName("th-check"),
                "from(bucket: \"foo\") |> range(start: -1d, stop: now()) |> aggregateWindow(every: 1m, fn: mean) |> yield()",
                "usage_user",
                "1h",
                "Check: ${ r._check_name } is: ${ r._level }",
                Collections.singletonList(greater),
                orgID);

        check.setName(generateName("updated name"));
        check.setDescription("updated description");
        check.setStatus(TaskStatusType.INACTIVE);

        check = (ThresholdCheck) checksApi.updateCheck(check);

        Assertions.assertThat(check.getName()).startsWith("updated name");
        Assertions.assertThat(check.getDescription()).isEqualTo("updated description");
        Assertions.assertThat(check.getStatus()).isEqualTo(TaskStatusType.INACTIVE);
    }

    @Test
    public void updateCheckNotExists() {

        CheckPatch update = new CheckPatch()
                .name("not exists name")
                .description("not exists update")
                .status(CheckPatch.StatusEnum.ACTIVE);

        Assertions.assertThatThrownBy(() -> checksApi.updateCheck("020f755c3c082000", update))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("check not found");
    }

    @Test
    public void deleteCheck() {

        GreaterThreshold greater = new GreaterThreshold();
        greater.value(80F).level(CheckStatusLevel.CRIT).allValues(true);

        Check created = checksApi.createThresholdCheck(generateName("th-check"),
                "from(bucket: \"foo\") |> range(start: -1d, stop: now()) |> aggregateWindow(every: 1m, fn: mean) |> yield()",
                "usage_user",
                "1h",
                "Check: ${ r._check_name } is: ${ r._level }",
                Collections.singletonList(greater),
                orgID);

        Check found = checksApi.findCheckByID(created.getId());

        checksApi.deleteCheck(found);

        Assertions.assertThatThrownBy(() -> checksApi.findCheckByID(found.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("check not found");
    }

    @Test
    public void deleteCheckNotFound() {

        Assertions.assertThatThrownBy(() -> checksApi.deleteCheck("020f755c3c082000"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("check not found");
    }

    @Test
    public void findCheckByID() {

        GreaterThreshold greater = new GreaterThreshold();
        greater.value(80F).level(CheckStatusLevel.CRIT).allValues(true);

        Check check = checksApi.createThresholdCheck(generateName("th-check"),
                "from(bucket: \"foo\") |> range(start: -1d, stop: now()) |> aggregateWindow(every: 1m, fn: mean) |> yield()",
                "usage_user",
                "1h",
                "Check: ${ r._check_name } is: ${ r._level }",
                Collections.singletonList(greater),
                orgID);

        ThresholdCheck found = (ThresholdCheck) checksApi.findCheckByID(check.getId());

        Assertions.assertThat(check.getId()).isEqualTo(found.getId());
    }

    @Test
    public void findCheckByIDNotFound() {
        Assertions.assertThatThrownBy(() -> checksApi.findCheckByID("020f755c3c082000"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("check not found");
    }

    @Test
    void labels() {

        LabelsApi labelsApi = influxDBClient.getLabelsApi();

        GreaterThreshold greater = new GreaterThreshold();
        greater.value(80F).level(CheckStatusLevel.CRIT).allValues(true);

        Check check = checksApi.createThresholdCheck(generateName("th-check"),
                "from(bucket: \"foo\") |> range(start: -1d, stop: now()) |> aggregateWindow(every: 1m, fn: mean) |> yield()",
                "usage_user",
                "1h",
                "Check: ${ r._check_name } is: ${ r._level }",
                Collections.singletonList(greater),
                orgID);

        Map<String, String> properties = new HashMap<>();
        properties.put("color", "green");
        properties.put("location", "west");

        Label label = labelsApi.createLabel(generateName("Cool Resource"), properties, orgID);

        List<Label> labels = checksApi.getLabels(check);
        Assertions.assertThat(labels).hasSize(0);

        Label addedLabel = checksApi.addLabel(label, check).getLabel();
        Assertions.assertThat(addedLabel).isNotNull();
        Assertions.assertThat(addedLabel.getId()).isEqualTo(label.getId());
        Assertions.assertThat(addedLabel.getName()).isEqualTo(label.getName());
        Assertions.assertThat(addedLabel.getProperties()).isEqualTo(label.getProperties());

        labels = checksApi.getLabels(check);
        Assertions.assertThat(labels).hasSize(1);
        Assertions.assertThat(labels.get(0).getId()).isEqualTo(label.getId());
        Assertions.assertThat(labels.get(0).getName()).isEqualTo(label.getName());

        checksApi.deleteLabel(label, check);

        labels = checksApi.getLabels(check);
        Assertions.assertThat(labels).hasSize(0);
    }

    @Test
    void labelAddNotExists() {

        GreaterThreshold greater = new GreaterThreshold();
        greater.value(80F).level(CheckStatusLevel.CRIT).allValues(true);

        Check check = checksApi.createThresholdCheck(generateName("th-check"),
                "from(bucket: \"foo\") |> range(start: -1d, stop: now()) |> aggregateWindow(every: 1m, fn: mean) |> yield()",
                "usage_user",
                "1h",
                "Check: ${ r._check_name } is: ${ r._level }",
                Collections.singletonList(greater),
                orgID);

        Assertions.assertThatThrownBy(() -> checksApi.addLabel("020f755c3c082000", check.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void labelDeleteNotExists() {

        GreaterThreshold greater = new GreaterThreshold();
        greater.value(80F).level(CheckStatusLevel.CRIT).allValues(true);

        Check check = checksApi.createThresholdCheck(generateName("th-check"),
                "from(bucket: \"foo\") |> range(start: -1d, stop: now()) |> aggregateWindow(every: 1m, fn: mean) |> yield()",
                "usage_user",
                "1h",
                "Check: ${ r._check_name } is: ${ r._level }",
                Collections.singletonList(greater),
                orgID);

        checksApi.deleteLabel("020f755c3c082000", check.getId());
    }

    @Test
    void findChecks() {

        GreaterThreshold greater = new GreaterThreshold();
        greater.value(80F).level(CheckStatusLevel.CRIT).allValues(true);

        int size = checksApi.findChecks(orgID).size();

        checksApi.createThresholdCheck(generateName("th-check"),
                "from(bucket: \"foo\") |> range(start: -1d, stop: now()) |> aggregateWindow(every: 1m, fn: mean) |> yield()",
                "usage_user",
                "1h",
                "Check: ${ r._check_name } is: ${ r._level }",
                Collections.singletonList(greater),
                orgID);

        List<Check> checks = checksApi.findChecks(orgID);
        Assertions.assertThat(checks).hasSize(size + 1);
    }

    @Test
    void findChecksPaging() {

        GreaterThreshold greater = new GreaterThreshold();
        greater.value(80F).level(CheckStatusLevel.CRIT).allValues(true);

        IntStream
                .range(0, 20 - checksApi.findChecks(orgID).size())
                .forEach(value -> checksApi.createThresholdCheck(generateName("th-check"),
                        "from(bucket: \"foo\") |> range(start: -1d, stop: now()) |> aggregateWindow(every: 1m, fn: mean) |> yield()",
                        "usage_user",
                        "1h",
                        "Check: ${ r._check_name } is: ${ r._level }",
                        Collections.singletonList(greater),
                        orgID));

        FindOptions findOptions = new FindOptions();
        findOptions.setLimit(5);

        Checks checks = checksApi.findChecks(orgID, findOptions);
        Assertions.assertThat(checks.getChecks()).hasSize(5);
        Assertions.assertThat(checks.getLinks().getNext()).isEqualTo("/api/v2/checks?descending=false&limit=5&offset=5&orgID=" + orgID);

        checks = checksApi.findChecks(orgID, FindOptions.create(checks.getLinks().getNext()));
        Assertions.assertThat(checks.getChecks()).hasSize(5);
        Assertions.assertThat(checks.getLinks().getNext()).isEqualTo("/api/v2/checks?descending=false&limit=5&offset=10&orgID=" + orgID);

        checks = checksApi.findChecks(orgID, FindOptions.create(checks.getLinks().getNext()));
        Assertions.assertThat(checks.getChecks()).hasSize(5);
        Assertions.assertThat(checks.getLinks().getNext()).isEqualTo("/api/v2/checks?descending=false&limit=5&offset=15&orgID=" + orgID);

        checks = checksApi.findChecks(orgID, FindOptions.create(checks.getLinks().getNext()));
        Assertions.assertThat(checks.getChecks()).hasSize(5);
        Assertions.assertThat(checks.getLinks().getNext()).isEqualTo("/api/v2/checks?descending=false&limit=5&offset=20&orgID=" + orgID);

        checks = checksApi.findChecks(orgID, FindOptions.create(checks.getLinks().getNext()));
        Assertions.assertThat(checks.getChecks()).hasSize(0);
        Assertions.assertThat(checks.getLinks().getNext()).isNull();
    }
}
