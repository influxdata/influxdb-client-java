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

import com.influxdb.client.domain.HTTPNotificationEndpoint;
import com.influxdb.client.domain.HTTPNotificationRule;
import com.influxdb.client.domain.Label;
import com.influxdb.client.domain.LabelResponse;
import com.influxdb.client.domain.NotificationRule;
import com.influxdb.client.domain.NotificationRuleUpdate;
import com.influxdb.client.domain.NotificationRules;
import com.influxdb.client.domain.PagerDutyNotificationEndpoint;
import com.influxdb.client.domain.PagerDutyNotificationRule;
import com.influxdb.client.domain.RuleStatusLevel;
import com.influxdb.client.domain.SlackNotificationEndpoint;
import com.influxdb.client.domain.SlackNotificationRule;
import com.influxdb.client.domain.TagRule;

/**
 * The client of the InfluxDB 2.x that implement NotificationRules HTTP API endpoint.
 *
 * @author Jakub Bednar (23/09/2019 10:43)
 */
@ThreadSafe
public interface NotificationRulesApi {

    /**
     * Add a Slack notification rule.
     *
     * @param name            Human-readable name describing the notification rule.
     * @param every           The notification repetition interval.
     * @param messageTemplate The template used to generate notification.
     * @param status          Status rule the notification rule attempts to match.
     * @param endpoint        The endpoint to use for notification.
     * @param orgID           The ID of the organization that owns this notification rule.
     * @return Notification rule created
     */
    @Nonnull
    SlackNotificationRule createSlackRule(@Nonnull final String name,
                                          @Nonnull final String every,
                                          @Nonnull final String messageTemplate,
                                          @Nonnull final RuleStatusLevel status,
                                          @Nonnull final SlackNotificationEndpoint endpoint,
                                          @Nonnull final String orgID);

    /**
     * Add a Slack notification rule.
     *
     * @param name            Human-readable name describing the notification rule.
     * @param every           The notification repetition interval.
     * @param messageTemplate The template used to generate notification.
     * @param status          Status rule the notification rule attempts to match.
     * @param tagRules        List of tag rules the notification rule attempts to match.
     * @param endpoint        The endpoint to use for notification.
     * @param orgID           The ID of the organization that owns this notification rule.
     * @return Notification rule created
     */
    @Nonnull
    SlackNotificationRule createSlackRule(@Nonnull final String name,
                                          @Nonnull final String every,
                                          @Nonnull final String messageTemplate,
                                          @Nonnull final RuleStatusLevel status,
                                          @Nonnull final List<TagRule> tagRules,
                                          @Nonnull final SlackNotificationEndpoint endpoint,
                                          @Nonnull final String orgID);

    /**
     * Add a PagerDuty notification rule.
     *
     * @param name            Human-readable name describing the notification rule.
     * @param every           The notification repetition interval.
     * @param messageTemplate The template used to generate notification.
     * @param status          Status rule the notification rule attempts to match.
     * @param tagRules        List of tag rules the notification rule attempts to match.
     * @param endpoint        The endpoint to use for notification.
     * @param orgID           The ID of the organization that owns this notification rule.
     * @return Notification rule created
     */
    @Nonnull
    PagerDutyNotificationRule createPagerDutyRule(@Nonnull final String name,
                                                  @Nonnull final String every,
                                                  @Nonnull final String messageTemplate,
                                                  @Nonnull final RuleStatusLevel status,
                                                  @Nonnull final List<TagRule> tagRules,
                                                  @Nonnull final PagerDutyNotificationEndpoint endpoint,
                                                  @Nonnull final String orgID);

    /**
     * Add a HTTP notification rule.
     *
     * @param name     Human-readable name describing the notification rule.
     * @param every    The notification repetition interval.
     * @param status   Status rule the notification rule attempts to match.
     * @param tagRules List of tag rules the notification rule attempts to match.
     * @param endpoint The endpoint to use for notification.
     * @param orgID    The ID of the organization that owns this notification rule.
     * @return Notification rule created
     */
    @Nonnull
    HTTPNotificationRule createHTTPRule(@Nonnull final String name,
                                        @Nonnull final String every,
                                        @Nonnull final RuleStatusLevel status,
                                        @Nonnull final List<TagRule> tagRules,
                                        @Nonnull final HTTPNotificationEndpoint endpoint,
                                        @Nonnull final String orgID);

    /**
     * Add a notification rule.
     *
     * @param rule Notification rule to create
     * @return Notification rule created
     */
    @Nonnull
    NotificationRule createRule(@Nonnull final NotificationRule rule);

    /**
     * Update a notification rule.
     *
     * @param notificationRule Notification rule update to apply
     * @return An updated notification rule
     */
    @Nonnull
    NotificationRule updateNotificationRule(@Nonnull final NotificationRule notificationRule);

    /**
     * Update a notification rule.
     *
     * @param ruleID The notification rule ID.
     * @param update Notification rule update to apply
     * @return An updated notification rule
     */
    @Nonnull
    NotificationRule updateNotificationRule(@Nonnull final String ruleID, @Nonnull final NotificationRuleUpdate update);

    /**
     * Delete a notification rule.
     *
     * @param notificationRule The notification rule
     */
    void deleteNotificationRule(@Nonnull final NotificationRule notificationRule);

    /**
     * Delete a notification rule.
     *
     * @param ruleID The notification rule ID
     */
    void deleteNotificationRule(@Nonnull final String ruleID);

    /**
     * Get a notification rule.
     *
     * @param ruleID The notification rule ID
     * @return The notification rule requested
     */
    @Nonnull
    NotificationRule findNotificationRuleByID(@Nonnull final String ruleID);

    /**
     * Get notification rules.
     *
     * @param orgID Only show notification rules that belong to a specific organization ID.
     * @return A list of notification rules
     */
    @Nonnull
    List<NotificationRule> findNotificationRules(@Nonnull final String orgID);

    /**
     * Get all notification rules.
     *
     * @param orgID       Only show notification rules that belong to a specific organization ID.
     * @param findOptions find options
     * @return A list of notification rules
     */
    @Nonnull
    NotificationRules findNotificationRules(@Nonnull final String orgID, @Nonnull final FindOptions findOptions);

    /**
     * List all labels for a notification rule.
     *
     * @param notificationRule The notification rule.
     * @return A list of all labels for a notification rule
     */
    @Nonnull
    List<Label> getLabels(@Nonnull final NotificationRule notificationRule);

    /**
     * List all labels for a notification rule.
     *
     * @param ruleID The notification rule ID.
     * @return A list of all labels for a notification rule
     */
    @Nonnull
    List<Label> getLabels(@Nonnull final String ruleID);

    /**
     * Add a label to a notification rule.
     *
     * @param label            Label to add
     * @param notificationRule The notification rule.
     * @return The label was added to the notification rule
     */
    @Nonnull
    LabelResponse addLabel(@Nonnull final Label label, @Nonnull final NotificationRule notificationRule);

    /**
     * Add a label to a notification rule.
     *
     * @param labelID Label to add
     * @param ruleID  The notification rule ID.
     * @return The label was added to the notification rule
     */
    @Nonnull
    LabelResponse addLabel(@Nonnull final String labelID, @Nonnull final String ruleID);

    /**
     * Delete label from a notification rule.
     *
     * @param label            The label to delete.
     * @param notificationRule The notification rule.
     */
    void deleteLabel(@Nonnull final Label label, @Nonnull final NotificationRule notificationRule);

    /**
     * Delete label from a notification rule.
     *
     * @param labelID The ID of the label to delete.
     * @param ruleID  The notification rule ID.
     */
    void deleteLabel(@Nonnull final String labelID, @Nonnull final String ruleID);
}
