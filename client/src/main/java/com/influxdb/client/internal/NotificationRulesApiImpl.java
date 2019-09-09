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
import com.influxdb.client.FindOptions;
import com.influxdb.client.NotificationRulesApi;
import com.influxdb.client.domain.HTTPNotificationEndpoint;
import com.influxdb.client.domain.HTTPNotificationRule;
import com.influxdb.client.domain.Label;
import com.influxdb.client.domain.LabelMapping;
import com.influxdb.client.domain.LabelResponse;
import com.influxdb.client.domain.LabelsResponse;
import com.influxdb.client.domain.NotificationEndpoint;
import com.influxdb.client.domain.NotificationRule;
import com.influxdb.client.domain.NotificationRuleUpdate;
import com.influxdb.client.domain.NotificationRules;
import com.influxdb.client.domain.PagerDutyNotificationEndpoint;
import com.influxdb.client.domain.PagerDutyNotificationRule;
import com.influxdb.client.domain.RuleStatusLevel;
import com.influxdb.client.domain.SlackNotificationEndpoint;
import com.influxdb.client.domain.SlackNotificationRule;
import com.influxdb.client.domain.StatusRule;
import com.influxdb.client.domain.TagRule;
import com.influxdb.client.domain.TaskStatusType;
import com.influxdb.client.service.NotificationRulesService;
import com.influxdb.internal.AbstractRestClient;

import retrofit2.Call;

/**
 * @author Jakub Bednar (23/09/2019 10:55)
 */
final class NotificationRulesApiImpl extends AbstractRestClient implements NotificationRulesApi {

    private final NotificationRulesService service;

    NotificationRulesApiImpl(@Nonnull final NotificationRulesService service) {

        Arguments.checkNotNull(service, "service");

        this.service = service;
    }

    @Nonnull
    @Override
    public SlackNotificationRule createSlackRule(@Nonnull final String name,
                                                 @Nonnull final String every,
                                                 @Nonnull final String messageTemplate,
                                                 @Nonnull final RuleStatusLevel status,
                                                 @Nonnull final SlackNotificationEndpoint endpoint,
                                                 @Nonnull final String orgID) {

        return createSlackRule(name, every, messageTemplate, status, Collections.emptyList(), endpoint, orgID);
    }

    @Nonnull
    @Override
    public SlackNotificationRule createSlackRule(@Nonnull final String name,
                                                 @Nonnull final String every,
                                                 @Nonnull final String messageTemplate,
                                                 @Nonnull final RuleStatusLevel status,
                                                 @Nonnull final List<TagRule> tagRules,
                                                 @Nonnull final SlackNotificationEndpoint endpoint,
                                                 @Nonnull final String orgID) {

        Arguments.checkNonEmpty(name, "name");
        Arguments.checkDuration(every, "every");
        Arguments.checkNonEmpty(messageTemplate, "messageTemplate");
        Arguments.checkNotNull(status, "status");
        Arguments.checkNotNull(tagRules, "tagRules");
        Arguments.checkNotNull(endpoint, "endpoint");
        Arguments.checkNonEmpty(orgID, "orgID");

        SlackNotificationRule rule = new SlackNotificationRule();
        rule.setMessageTemplate(messageTemplate);

        return (SlackNotificationRule)
                createRule(name, every, status, tagRules, endpoint, orgID, rule);
    }

    @Nonnull
    @Override
    public PagerDutyNotificationRule createPagerDutyRule(@Nonnull final String name,
                                                         @Nonnull final String every,
                                                         @Nonnull final String messageTemplate,
                                                         @Nonnull final RuleStatusLevel status,
                                                         @Nonnull final List<TagRule> tagRules,
                                                         @Nonnull final PagerDutyNotificationEndpoint endpoint,
                                                         @Nonnull final String orgID) {
        Arguments.checkNonEmpty(name, "name");
        Arguments.checkDuration(every, "every");
        Arguments.checkNonEmpty(messageTemplate, "messageTemplate");
        Arguments.checkNotNull(status, "status");
        Arguments.checkNotNull(tagRules, "tagRules");
        Arguments.checkNotNull(endpoint, "endpoint");
        Arguments.checkNonEmpty(orgID, "orgID");

        PagerDutyNotificationRule rule = new PagerDutyNotificationRule();
        rule.setMessageTemplate(messageTemplate);

        return (PagerDutyNotificationRule)
                createRule(name, every, status, tagRules, endpoint, orgID, rule);
    }

    @Nonnull
    @Override
    public HTTPNotificationRule createHTTPRule(@Nonnull final String name,
                                               @Nonnull final String every,
                                               @Nonnull final RuleStatusLevel status,
                                               @Nonnull final List<TagRule> tagRules,
                                               @Nonnull final HTTPNotificationEndpoint endpoint,
                                               @Nonnull final String orgID) {


        HTTPNotificationRule rule = new HTTPNotificationRule();

        return (HTTPNotificationRule)
                createRule(name, every, status, tagRules, endpoint, orgID, rule);
    }

    @Override
    @Nonnull
    public NotificationRule createRule(@Nonnull final NotificationRule rule) {

        Arguments.checkNotNull(rule, "rule");

        Call<NotificationRule> call = service.createNotificationRule(rule);
        return execute(call);
    }

    @Nonnull
    @Override
    public NotificationRule updateNotificationRule(@Nonnull final NotificationRule notificationRule) {
        Arguments.checkNotNull(notificationRule, "update");

        NotificationRuleUpdate update = new NotificationRuleUpdate()
                .name(notificationRule.getName())
                .description(notificationRule.getDescription())
                .status(NotificationRuleUpdate.StatusEnum.fromValue(notificationRule.getStatus().getValue()));

        return updateNotificationRule(notificationRule.getId(), update);
    }

    @Nonnull
    @Override
    public NotificationRule updateNotificationRule(@Nonnull final String ruleID,
                                                   @Nonnull final NotificationRuleUpdate update) {
        Arguments.checkNotNull(ruleID, "ruleID");
        Arguments.checkNotNull(update, "update");

        Call<NotificationRule> call = service.patchNotificationRulesID(ruleID, update, null);

        return execute(call);
    }

    @Override
    public void deleteNotificationRule(@Nonnull final NotificationRule notificationRule) {
        Arguments.checkNotNull(notificationRule, "notificationRule");

        deleteNotificationRule(notificationRule.getId());
    }

    @Override
    public void deleteNotificationRule(@Nonnull final String ruleID) {
        Arguments.checkNotNull(ruleID, "ruleID");

        Call<Void> call = service.deleteNotificationRulesID(ruleID, null);

        execute(call);
    }

    @Nonnull
    @Override
    public NotificationRule findNotificationRuleByID(@Nonnull final String ruleID) {
        Arguments.checkNotNull(ruleID, "ruleID");

        Call<NotificationRule> call = service.getNotificationRulesID(ruleID, null);

        return execute(call);
    }

    @Nonnull
    @Override
    public List<NotificationRule> findNotificationRules(@Nonnull final String orgID) {
        Arguments.checkNonEmpty(orgID, "orgID");

        return findNotificationRules(orgID, new FindOptions()).getNotificationRules();
    }

    @Nonnull
    @Override
    public NotificationRules findNotificationRules(@Nonnull final String orgID,
                                                   @Nonnull final FindOptions findOptions) {
        Arguments.checkNonEmpty(orgID, "orgID");
        Arguments.checkNotNull(findOptions, "findOptions");

        Call<NotificationRules> call = service
                .getNotificationRules(orgID, null, findOptions.getOffset(), findOptions.getLimit(),
                        null, null);

        return execute(call);
    }

    @Nonnull
    @Override
    public List<Label> getLabels(@Nonnull final NotificationRule notificationRule) {
        Arguments.checkNotNull(notificationRule, "notificationRule");

        return getLabels(notificationRule.getId());
    }

    @Nonnull
    @Override
    public List<Label> getLabels(@Nonnull final String ruleID) {
        Arguments.checkNonEmpty(ruleID, "ruleID");

        Call<LabelsResponse> call = service.getNotificationRulesIDLabels(ruleID, null);

        return execute(call).getLabels();
    }

    @Nonnull
    @Override
    public LabelResponse addLabel(@Nonnull final Label label, @Nonnull final NotificationRule notificationRule) {
        Arguments.checkNotNull(label, "label");
        Arguments.checkNotNull(notificationRule, "notificationRule");

        return addLabel(label.getId(), notificationRule.getId());
    }

    @Nonnull
    @Override
    public LabelResponse addLabel(@Nonnull final String labelID, @Nonnull final String ruleID) {
        Arguments.checkNonEmpty(labelID, "labelID");
        Arguments.checkNonEmpty(ruleID, "ruleID");

        LabelMapping labelMapping = new LabelMapping();
        labelMapping.setLabelID(labelID);

        Call<LabelResponse> call = service.postNotificationRuleIDLabels(ruleID, labelMapping, null);

        return execute(call);
    }

    @Override
    public void deleteLabel(@Nonnull final Label label, @Nonnull final NotificationRule notificationRule) {
        Arguments.checkNotNull(label, "label");
        Arguments.checkNotNull(notificationRule, "check");

        deleteLabel(label.getId(), notificationRule.getId());
    }

    @Override
    public void deleteLabel(@Nonnull final String labelID, @Nonnull final String ruleID) {
        Arguments.checkNonEmpty(labelID, "labelID");
        Arguments.checkNonEmpty(ruleID, "ruleID");

        Call<Void> call = service.deleteNotificationRulesIDLabelsID(ruleID, labelID, null);
        execute(call);
    }

    @Nonnull
    private NotificationRule createRule(@Nonnull final String name,
                                        @Nonnull final String every,
                                        @Nonnull final RuleStatusLevel status,
                                        @Nonnull final List<TagRule> tagRules,
                                        @Nonnull final NotificationEndpoint notificationEndpoint,
                                        @Nonnull final String orgID,
                                        @Nonnull final NotificationRule rule) {

        Arguments.checkNotNull(rule, "rule");

        rule.setName(name);
        rule.setEvery(every);
        rule.setOrgID(orgID);
        rule.setTagRules(tagRules);
        rule.addStatusRulesItem(new StatusRule().currentLevel(status));
        rule.setEndpointID(notificationEndpoint.getId());
        rule.setStatus(TaskStatusType.ACTIVE);

        return createRule(rule);
    }
}
