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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import com.influxdb.client.domain.CheckStatusLevel;
import com.influxdb.client.domain.GreaterThreshold;
import com.influxdb.client.domain.HTTPNotificationEndpoint;
import com.influxdb.client.domain.HTTPNotificationRule;
import com.influxdb.client.domain.HTTPNotificationRuleBase;
import com.influxdb.client.domain.Label;
import com.influxdb.client.domain.NotificationRule;
import com.influxdb.client.domain.NotificationRuleUpdate;
import com.influxdb.client.domain.NotificationRules;
import com.influxdb.client.domain.PagerDutyNotificationEndpoint;
import com.influxdb.client.domain.PagerDutyNotificationRule;
import com.influxdb.client.domain.PagerDutyNotificationRuleBase;
import com.influxdb.client.domain.RuleStatusLevel;
import com.influxdb.client.domain.SlackNotificationEndpoint;
import com.influxdb.client.domain.SlackNotificationRule;
import com.influxdb.client.domain.SlackNotificationRuleBase;
import com.influxdb.client.domain.TagRule;
import com.influxdb.client.domain.TaskStatusType;
import com.influxdb.exceptions.NotFoundException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (24/09/2019 09:34)
 */
@RunWith(JUnitPlatform.class)
class ITNotificationRulesApi extends AbstractITClientTest {

    private NotificationRulesApi notificationRulesApi;
    private NotificationEndpointsApi notificationEndpointsApi;
    private String orgID;

    @BeforeEach
    void setUp() {
        notificationRulesApi = influxDBClient.getNotificationRulesApi();
        notificationEndpointsApi = influxDBClient.getNotificationEndpointsApi();
        orgID = findMyOrg().getId();

        notificationRulesApi.findNotificationRules(orgID, new FindOptions())
                .getNotificationRules()
                .stream()
                .filter(rule -> rule.getName().endsWith("-IT"))
                .forEach(rule -> notificationRulesApi.deleteNotificationRule(rule));

        notificationEndpointsApi.findNotificationEndpoints(orgID, new FindOptions())
                .getNotificationEndpoints()
                .stream()
                .filter(notificationEndpoint -> notificationEndpoint.getName().endsWith("-IT"))
                .forEach(notificationEndpoint -> notificationEndpointsApi.deleteNotificationEndpoint(notificationEndpoint));
    }

    @Test
    public void createSlackRule() {

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        SlackNotificationEndpoint endpoint = notificationEndpointsApi
                .createSlackEndpoint(generateName("slack-endpoint"), "https://hooks.slack.com/services/x/y/z", null, orgID);

        List<TagRule> tagRules = new ArrayList<>();
        tagRules.add(new TagRule().value("tag_value").key("tag_key").operator(TagRule.OperatorEnum.EQUAL));

        SlackNotificationRule slackRule = notificationRulesApi.createSlackRule(generateName("slack-rule"),
                "10s",
                "my-template", RuleStatusLevel.CRIT, tagRules, endpoint, orgID);

        Assertions.assertThat(slackRule).isNotNull();
        Assertions.assertThat(slackRule.getId()).isNotBlank();
        Assertions.assertThat(slackRule.getType()).isEqualTo(SlackNotificationRuleBase.TypeEnum.SLACK);
        Assertions.assertThat(slackRule.getChannel()).isBlank();
        Assertions.assertThat(slackRule.getMessageTemplate()).isEqualTo("my-template");
        Assertions.assertThat(slackRule.getEndpointID()).isEqualTo(endpoint.getId());
        Assertions.assertThat(slackRule.getOrgID()).isEqualTo(orgID);
        Assertions.assertThat(slackRule.getCreatedAt()).isAfter(now);
        Assertions.assertThat(slackRule.getUpdatedAt()).isAfter(now);
        Assertions.assertThat(slackRule.getStatus()).isEqualTo(TaskStatusType.ACTIVE);
        Assertions.assertThat(slackRule.getName()).startsWith("slack-rule");
        Assertions.assertThat(slackRule.getSleepUntil()).isBlank();
        Assertions.assertThat(slackRule.getEvery()).isEqualTo("10s");
        Assertions.assertThat(slackRule.getOffset()).isBlank();
        Assertions.assertThat(slackRule.getRunbookLink()).isBlank();
        Assertions.assertThat(slackRule.getLimitEvery()).isNull();
        Assertions.assertThat(slackRule.getLimit()).isNull();
        Assertions.assertThat(slackRule.getTagRules()).hasSize(1);
        Assertions.assertThat(slackRule.getTagRules().get(0).getKey()).isEqualTo("tag_key");
        Assertions.assertThat(slackRule.getTagRules().get(0).getValue()).isEqualTo("tag_value");
        Assertions.assertThat(slackRule.getTagRules().get(0).getOperator()).isEqualTo(TagRule.OperatorEnum.EQUAL);
        Assertions.assertThat(slackRule.getDescription()).isBlank();
        Assertions.assertThat(slackRule.getStatusRules()).hasSize(1);
        Assertions.assertThat(slackRule.getStatusRules().get(0).getCount()).isNull();
        Assertions.assertThat(slackRule.getStatusRules().get(0).getPeriod()).isNull();
        Assertions.assertThat(slackRule.getStatusRules().get(0).getPreviousLevel()).isNull();
        Assertions.assertThat(slackRule.getStatusRules().get(0).getCurrentLevel()).isEqualTo(RuleStatusLevel.CRIT);
        Assertions.assertThat(slackRule.getLabels()).isEmpty();
        Assertions.assertThat(slackRule.getLinks().getSelf())
                .isEqualTo(String.format("/api/v2/notificationRules/%s", slackRule.getId()));
        Assertions.assertThat(slackRule.getLinks().getLabels())
                .isEqualTo(String.format("/api/v2/notificationRules/%s/labels", slackRule.getId()));
        Assertions.assertThat(slackRule.getLinks().getMembers())
                .isEqualTo(String.format("/api/v2/notificationRules/%s/members", slackRule.getId()));
        Assertions.assertThat(slackRule.getLinks().getOwners())
                .isEqualTo(String.format("/api/v2/notificationRules/%s/owners", slackRule.getId()));
    }

    @Test
    public void createPagerDutyRule() {

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        PagerDutyNotificationEndpoint endpoint = notificationEndpointsApi
                .createPagerDutyEndpoint(generateName("pager-duty"), "https://events.pagerduty.com/v2/enqueue", "secret-key", orgID);

        List<TagRule> tagRules = new ArrayList<>();
        tagRules.add(new TagRule().value("tag_value").key("tag_key").operator(TagRule.OperatorEnum.EQUAL));

        PagerDutyNotificationRule rule = notificationRulesApi.createPagerDutyRule(generateName("pagerduty-rule"),
                "10s",
                "my-template", RuleStatusLevel.CRIT, tagRules, endpoint, orgID);

        Assertions.assertThat(rule).isNotNull();
        Assertions.assertThat(rule.getId()).isNotBlank();
        Assertions.assertThat(rule.getType()).isEqualTo(PagerDutyNotificationRuleBase.TypeEnum.PAGERDUTY);
        Assertions.assertThat(rule.getMessageTemplate()).isEqualTo("my-template");
        Assertions.assertThat(rule.getEndpointID()).isEqualTo(endpoint.getId());
        Assertions.assertThat(rule.getOrgID()).isEqualTo(orgID);
        Assertions.assertThat(rule.getCreatedAt()).isAfter(now);
        Assertions.assertThat(rule.getUpdatedAt()).isAfter(now);
        Assertions.assertThat(rule.getStatus()).isEqualTo(TaskStatusType.ACTIVE);
        Assertions.assertThat(rule.getName()).startsWith("pagerduty-rule");
        Assertions.assertThat(rule.getSleepUntil()).isBlank();
        Assertions.assertThat(rule.getEvery()).isEqualTo("10s");
        Assertions.assertThat(rule.getOffset()).isBlank();
        Assertions.assertThat(rule.getRunbookLink()).isBlank();
        Assertions.assertThat(rule.getLimitEvery()).isNull();
        Assertions.assertThat(rule.getLimit()).isNull();
        Assertions.assertThat(rule.getTagRules()).hasSize(1);
        Assertions.assertThat(rule.getTagRules().get(0).getKey()).isEqualTo("tag_key");
        Assertions.assertThat(rule.getTagRules().get(0).getValue()).isEqualTo("tag_value");
        Assertions.assertThat(rule.getTagRules().get(0).getOperator()).isEqualTo(TagRule.OperatorEnum.EQUAL);
        Assertions.assertThat(rule.getDescription()).isBlank();
        Assertions.assertThat(rule.getStatusRules()).hasSize(1);
        Assertions.assertThat(rule.getStatusRules().get(0).getCount()).isNull();
        Assertions.assertThat(rule.getStatusRules().get(0).getPeriod()).isNull();
        Assertions.assertThat(rule.getStatusRules().get(0).getPreviousLevel()).isNull();
        Assertions.assertThat(rule.getStatusRules().get(0).getCurrentLevel()).isEqualTo(RuleStatusLevel.CRIT);
        Assertions.assertThat(rule.getLabels()).isEmpty();
        Assertions.assertThat(rule.getLinks().getSelf())
                .isEqualTo(String.format("/api/v2/notificationRules/%s", rule.getId()));
        Assertions.assertThat(rule.getLinks().getLabels())
                .isEqualTo(String.format("/api/v2/notificationRules/%s/labels", rule.getId()));
        Assertions.assertThat(rule.getLinks().getMembers())
                .isEqualTo(String.format("/api/v2/notificationRules/%s/members", rule.getId()));
        Assertions.assertThat(rule.getLinks().getOwners())
                .isEqualTo(String.format("/api/v2/notificationRules/%s/owners", rule.getId()));
    }

    @Test
    public void createHttpRule() {

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        HTTPNotificationEndpoint endpoint = notificationEndpointsApi
                .createHTTPEndpoint(generateName("http"), "http://localhost:1234/mock",
                        HTTPNotificationEndpoint.MethodEnum.POST, orgID);

        List<TagRule> tagRules = new ArrayList<>();
        tagRules.add(new TagRule().value("tag_value").key("tag_key").operator(TagRule.OperatorEnum.EQUAL));

        HTTPNotificationRule rule = notificationRulesApi.createHTTPRule(generateName("http-rule"),
                "10s",
                RuleStatusLevel.CRIT, tagRules, endpoint, orgID);

        Assertions.assertThat(rule).isNotNull();
        Assertions.assertThat(rule.getId()).isNotBlank();
        Assertions.assertThat(rule.getType()).isEqualTo(HTTPNotificationRuleBase.TypeEnum.HTTP);
        Assertions.assertThat(rule.getEndpointID()).isEqualTo(endpoint.getId());
        Assertions.assertThat(rule.getOrgID()).isEqualTo(orgID);
        Assertions.assertThat(rule.getCreatedAt()).isAfter(now);
        Assertions.assertThat(rule.getUpdatedAt()).isAfter(now);
        Assertions.assertThat(rule.getStatus()).isEqualTo(TaskStatusType.ACTIVE);
        Assertions.assertThat(rule.getName()).startsWith("http-rule");
        Assertions.assertThat(rule.getSleepUntil()).isBlank();
        Assertions.assertThat(rule.getEvery()).isEqualTo("10s");
        Assertions.assertThat(rule.getOffset()).isBlank();
        Assertions.assertThat(rule.getRunbookLink()).isBlank();
        Assertions.assertThat(rule.getLimitEvery()).isNull();
        Assertions.assertThat(rule.getLimit()).isNull();
        Assertions.assertThat(rule.getTagRules()).hasSize(1);
        Assertions.assertThat(rule.getTagRules().get(0).getKey()).isEqualTo("tag_key");
        Assertions.assertThat(rule.getTagRules().get(0).getValue()).isEqualTo("tag_value");
        Assertions.assertThat(rule.getTagRules().get(0).getOperator()).isEqualTo(TagRule.OperatorEnum.EQUAL);
        Assertions.assertThat(rule.getDescription()).isBlank();
        Assertions.assertThat(rule.getStatusRules()).hasSize(1);
        Assertions.assertThat(rule.getStatusRules().get(0).getCount()).isNull();
        Assertions.assertThat(rule.getStatusRules().get(0).getPeriod()).isNull();
        Assertions.assertThat(rule.getStatusRules().get(0).getPreviousLevel()).isNull();
        Assertions.assertThat(rule.getStatusRules().get(0).getCurrentLevel()).isEqualTo(RuleStatusLevel.CRIT);
        Assertions.assertThat(rule.getLabels()).isEmpty();
        Assertions.assertThat(rule.getLinks().getSelf())
                .isEqualTo(String.format("/api/v2/notificationRules/%s", rule.getId()));
        Assertions.assertThat(rule.getLinks().getLabels())
                .isEqualTo(String.format("/api/v2/notificationRules/%s/labels", rule.getId()));
        Assertions.assertThat(rule.getLinks().getMembers())
                .isEqualTo(String.format("/api/v2/notificationRules/%s/members", rule.getId()));
        Assertions.assertThat(rule.getLinks().getOwners())
                .isEqualTo(String.format("/api/v2/notificationRules/%s/owners", rule.getId()));
    }

    @Test
    public void updateRule() {

        HTTPNotificationEndpoint endpoint = notificationEndpointsApi
                .createHTTPEndpoint(generateName("http"), "http://localhost:1234/mock",
                        HTTPNotificationEndpoint.MethodEnum.POST, orgID);

        List<TagRule> tagRules = new ArrayList<>();
        tagRules.add(new TagRule().value("tag_value").key("tag_key").operator(TagRule.OperatorEnum.EQUAL));

        HTTPNotificationRule rule = notificationRulesApi.createHTTPRule(generateName("http-rule"),
                "10s",
                RuleStatusLevel.CRIT, tagRules, endpoint, orgID);

        rule.setName(generateName("updated name"));
        rule.setDescription("updated description");
        rule.setStatus(TaskStatusType.INACTIVE);

        rule = (HTTPNotificationRule) notificationRulesApi.updateNotificationRule(rule);

        Assertions.assertThat(rule.getName()).startsWith("updated name");
        Assertions.assertThat(rule.getDescription()).isEqualTo("updated description");
        Assertions.assertThat(rule.getStatus()).isEqualTo(TaskStatusType.INACTIVE);
    }

    @Test
    public void updateRuleNotExists() {

        NotificationRuleUpdate update = new NotificationRuleUpdate()
                .name("not exists name")
                .description("not exists update")
                .status(NotificationRuleUpdate.StatusEnum.ACTIVE);

        Assertions.assertThatThrownBy(() -> notificationRulesApi.updateNotificationRule("020f755c3c082000", update))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("notification rule not found");
    }

    @Test
    public void deleteRule() {

        SlackNotificationEndpoint endpoint = notificationEndpointsApi
                .createSlackEndpoint(generateName("slack-endpoint"), "https://hooks.slack.com/services/x/y/z", null, orgID);

        List<TagRule> tagRules = new ArrayList<>();
        tagRules.add(new TagRule().value("tag_value").key("tag_key").operator(TagRule.OperatorEnum.EQUAL));

        SlackNotificationRule created = notificationRulesApi.createSlackRule(generateName("slack-rule"),
                "10s",
                "my-template", RuleStatusLevel.CRIT, tagRules, endpoint, orgID);

        notificationRulesApi.deleteNotificationRule(created);

        Assertions.assertThatThrownBy(() -> notificationRulesApi.findNotificationRuleByID(created.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("notification rule not found");
    }

    @Test
    public void deleteRuleNotFound() {

        Assertions.assertThatThrownBy(() -> notificationRulesApi.deleteNotificationRule("020f755c3c082000"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("notification rule not found");
    }

    @Test
    public void findRuleByID() {

        PagerDutyNotificationEndpoint endpoint = notificationEndpointsApi
                .createPagerDutyEndpoint(generateName("pager-duty"), "https://events.pagerduty.com/v2/enqueue", "secret-key", orgID);

        List<TagRule> tagRules = new ArrayList<>();

        PagerDutyNotificationRule slackRule = notificationRulesApi.createPagerDutyRule(generateName("pagerduty-rule"),
                "10s",
                "my-template", RuleStatusLevel.CRIT, tagRules, endpoint, orgID);

        PagerDutyNotificationRule found = (PagerDutyNotificationRule) notificationRulesApi.findNotificationRuleByID(slackRule.getId());

        Assertions.assertThat(slackRule.getId()).isEqualTo(found.getId());
    }

    @Test
    public void findRuleByIDNotFound() {
        Assertions.assertThatThrownBy(() -> notificationRulesApi.findNotificationRuleByID("020f755c3c082000"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("notification rule not found");
    }

    @Test
    void findRules() {

        GreaterThreshold greater = new GreaterThreshold();
        greater.value(80F).level(CheckStatusLevel.CRIT).allValues(true);

        int size = notificationRulesApi.findNotificationRules(orgID).size();

        PagerDutyNotificationEndpoint endpoint = notificationEndpointsApi
                .createPagerDutyEndpoint(generateName("pager-duty"), "https://events.pagerduty.com/v2/enqueue", "secret-key", orgID);

        List<TagRule> tagRules = new ArrayList<>();
        tagRules.add(new TagRule().value("tag_value").key("tag_key").operator(TagRule.OperatorEnum.EQUAL));

        notificationRulesApi.createPagerDutyRule(generateName("pagerduty-rule"),
                "10s",
                "my-template", RuleStatusLevel.CRIT, tagRules, endpoint, orgID);

        List<NotificationRule> rules =  notificationRulesApi.findNotificationRules(orgID);
        Assertions.assertThat(rules).hasSize(size + 1);
    }

    @Test
    void findRulesPaging() {

        PagerDutyNotificationEndpoint endpoint = notificationEndpointsApi
                .createPagerDutyEndpoint(generateName("pager-duty"), "https://events.pagerduty.com/v2/enqueue", "secret-key", orgID);


        IntStream
                .range(0, 20 - notificationRulesApi.findNotificationRules(orgID).size())
                .forEach(value -> notificationRulesApi.createPagerDutyRule(generateName("pagerduty-rule"),
                        "10s",
                        "my-template", RuleStatusLevel.CRIT, Collections.emptyList(), endpoint, orgID));

        FindOptions findOptions = new FindOptions();
        findOptions.setLimit(5);

        NotificationRules rules = notificationRulesApi.findNotificationRules(orgID, findOptions);
        Assertions.assertThat(rules.getNotificationRules()).hasSize(5);
        Assertions.assertThat(rules.getLinks().getNext()).isEqualTo("/api/v2/notificationRules?descending=false&limit=5&offset=5&orgID=" + orgID);

        rules = notificationRulesApi.findNotificationRules(orgID, FindOptions.create(rules.getLinks().getNext()));
        Assertions.assertThat(rules.getNotificationRules()).hasSize(5);
        Assertions.assertThat(rules.getLinks().getNext()).isEqualTo("/api/v2/notificationRules?descending=false&limit=5&offset=10&orgID=" + orgID);

        rules = notificationRulesApi.findNotificationRules(orgID, FindOptions.create(rules.getLinks().getNext()));
        Assertions.assertThat(rules.getNotificationRules()).hasSize(5);
        Assertions.assertThat(rules.getLinks().getNext()).isEqualTo("/api/v2/notificationRules?descending=false&limit=5&offset=15&orgID=" + orgID);

        rules = notificationRulesApi.findNotificationRules(orgID, FindOptions.create(rules.getLinks().getNext()));
        Assertions.assertThat(rules.getNotificationRules()).hasSize(5);
        Assertions.assertThat(rules.getLinks().getNext()).isEqualTo("/api/v2/notificationRules?descending=false&limit=5&offset=20&orgID=" + orgID);

        rules = notificationRulesApi.findNotificationRules(orgID, FindOptions.create(rules.getLinks().getNext()));
        Assertions.assertThat(rules.getNotificationRules()).hasSize(0);
        Assertions.assertThat(rules.getLinks().getNext()).isNull();
    }

    @Test
    void labels() {

        LabelsApi labelsApi = influxDBClient.getLabelsApi();

        SlackNotificationEndpoint endpoint = notificationEndpointsApi
                .createSlackEndpoint(generateName("slack-endpoint"), "https://hooks.slack.com/services/x/y/z", null, orgID);

        List<TagRule> tagRules = new ArrayList<>();
        tagRules.add(new TagRule().value("tag_value").key("tag_key").operator(TagRule.OperatorEnum.EQUAL));

        SlackNotificationRule rule = notificationRulesApi.createSlackRule(generateName("slack-rule"),
                "10s",
                "my-template", RuleStatusLevel.CRIT, tagRules, endpoint, orgID);

        Map<String, String> properties = new HashMap<>();
        properties.put("color", "green");
        properties.put("location", "west");

        Label label = labelsApi.createLabel(generateName("Cool Resource"), properties, orgID);

        List<Label> labels = notificationRulesApi.getLabels(rule);
        Assertions.assertThat(labels).hasSize(0);

        Label addedLabel = notificationRulesApi.addLabel(label, rule).getLabel();
        Assertions.assertThat(addedLabel).isNotNull();
        Assertions.assertThat(addedLabel.getId()).isEqualTo(label.getId());
        Assertions.assertThat(addedLabel.getName()).isEqualTo(label.getName());
        Assertions.assertThat(addedLabel.getProperties()).isEqualTo(label.getProperties());

        labels = notificationRulesApi.getLabels(rule);
        Assertions.assertThat(labels).hasSize(1);
        Assertions.assertThat(labels.get(0).getId()).isEqualTo(label.getId());
        Assertions.assertThat(labels.get(0).getName()).isEqualTo(label.getName());

        notificationRulesApi.deleteLabel(label, rule);

        labels = notificationRulesApi.getLabels(rule);
        Assertions.assertThat(labels).hasSize(0);
    }

    @Test
    void labelAddNotExists() {

        SlackNotificationEndpoint endpoint = notificationEndpointsApi
                .createSlackEndpoint(generateName("slack-endpoint"), "https://hooks.slack.com/services/x/y/z", null, orgID);

        List<TagRule> tagRules = new ArrayList<>();
        tagRules.add(new TagRule().value("tag_value").key("tag_key").operator(TagRule.OperatorEnum.EQUAL));

        SlackNotificationRule rule = notificationRulesApi.createSlackRule(generateName("slack-rule"),
                "10s",
                "my-template", RuleStatusLevel.CRIT, tagRules, endpoint, orgID);

        Assertions.assertThatThrownBy(() -> notificationRulesApi.addLabel("020f755c3c082000", rule.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void labelDeleteNotExists() {

        SlackNotificationEndpoint endpoint = notificationEndpointsApi
                .createSlackEndpoint(generateName("slack-endpoint"), "https://hooks.slack.com/services/x/y/z", null, orgID);

        List<TagRule> tagRules = new ArrayList<>();
        tagRules.add(new TagRule().value("tag_value").key("tag_key").operator(TagRule.OperatorEnum.EQUAL));

        SlackNotificationRule rule = notificationRulesApi.createSlackRule(generateName("slack-rule"),
                "10s",
                "my-template", RuleStatusLevel.CRIT, tagRules, endpoint, orgID);

        notificationRulesApi.deleteLabel("020f755c3c082000", rule.getId());
    }
}
