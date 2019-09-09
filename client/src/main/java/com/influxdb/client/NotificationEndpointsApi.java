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
import javax.annotation.Nullable;

import com.influxdb.client.domain.HTTPNotificationEndpoint;
import com.influxdb.client.domain.HTTPNotificationEndpoint.MethodEnum;
import com.influxdb.client.domain.Label;
import com.influxdb.client.domain.LabelResponse;
import com.influxdb.client.domain.NotificationEndpoint;
import com.influxdb.client.domain.NotificationEndpointUpdate;
import com.influxdb.client.domain.NotificationEndpoints;
import com.influxdb.client.domain.PagerDutyNotificationEndpoint;
import com.influxdb.client.domain.SlackNotificationEndpoint;

/**
 * The client of the InfluxDB 2.0 that implement NotificationEndpoint HTTP API.
 *
 * @author Jakub Bednar (11/09/2019 09:20)
 */
public interface NotificationEndpointsApi {

    /**
     * Add new Slack notification endpoint. The {@code url} should be defined.
     *
     * @param name  Endpoint name
     * @param url   Slack WebHook URL
     * @param orgID Owner of an endpoint
     * @return created Slack notification endpoint
     */
    @Nonnull
    SlackNotificationEndpoint createSlackEndpoint(@Nonnull final String name,
                                                  @Nonnull final String url,
                                                  @Nonnull final String orgID);

    /**
     * Add new Slack notification endpoint. The {@code url} or {@code token} should be defined.
     *
     * @param name  Endpoint name
     * @param url   Slack WebHook URL
     * @param token Slack WebHook Token
     * @param orgID Owner of an endpoint
     * @return created Slack notification endpoint
     */
    @Nonnull
    SlackNotificationEndpoint createSlackEndpoint(@Nonnull final String name,
                                                  @Nullable final String url,
                                                  @Nullable final String token,
                                                  @Nonnull final String orgID);

    /**
     * Add new PagerDuty notification endpoint.
     *
     * @param name       Endpoint name
     * @param clientURL  Client URL
     * @param routingKey Routing Key
     * @param orgID      Owner of an endpoint
     * @return created PagerDuty notification endpoint
     */
    @Nonnull
    PagerDutyNotificationEndpoint createPagerDutyEndpoint(@Nonnull final String name,
                                                          @Nonnull final String clientURL,
                                                          @Nonnull final String routingKey,
                                                          @Nonnull final String orgID);

    /**
     * Add new HTTP notification endpoint without authentication.
     *
     * @param name   Endpoint name
     * @param url    URL
     * @param method HTTP Method
     * @param orgID  Owner of an endpoint
     * @return created HTTP notification endpoint
     */
    @Nonnull
    HTTPNotificationEndpoint createHTTPEndpoint(@Nonnull final String name,
                                                @Nonnull final String url,
                                                @Nonnull final MethodEnum method,
                                                @Nonnull final String orgID);

    /**
     * Add new HTTP notification endpoint with Http Basic authentication.
     *
     * @param name     Endpoint name
     * @param url      URL
     * @param method   HTTP Method
     * @param username HTTP Basic Username
     * @param password HTTP Basic Password
     * @param orgID    Owner of an endpoint
     * @return created HTTP notification endpoint
     */
    @Nonnull
    HTTPNotificationEndpoint createHTTPEndpointBasicAuth(@Nonnull final String name,
                                                         @Nonnull final String url,
                                                         @Nonnull final MethodEnum method,
                                                         @Nonnull final String username,
                                                         @Nonnull final String password,
                                                         @Nonnull final String orgID);

    /**
     * Add new HTTP notification endpoint with Bearer authentication.
     *
     * @param name   Endpoint name
     * @param url    URL
     * @param method HTTP Method
     * @param token  Bearer token
     * @param orgID  Owner of an endpoint
     * @return created HTTP notification endpoint
     */
    @Nonnull
    HTTPNotificationEndpoint createHTTPEndpointBearer(@Nonnull final String name,
                                                      @Nonnull final String url,
                                                      @Nonnull final MethodEnum method,
                                                      @Nonnull final String token,
                                                      @Nonnull final String orgID);

    /**
     * Add new notification endpoint.
     *
     * @param notificationEndpoint notificationEndpoint to create
     * @return Notification endpoint created
     */
    @Nonnull
    NotificationEndpoint createEndpoint(@Nonnull final NotificationEndpoint notificationEndpoint);

    /**
     * Update a notification endpoint. The updates is used for fields from {@link NotificationEndpointUpdate}.
     *
     * @param notificationEndpoint update to apply
     * @return An updated notification endpoint
     */
    @Nonnull
    NotificationEndpoint updateEndpoint(@Nonnull final NotificationEndpoint notificationEndpoint);

    /**
     * Update a notification endpoint.
     *
     * @param endpointID                 ID of notification endpoint
     * @param notificationEndpointUpdate update to apply
     * @return An updated notification endpoint
     */
    @Nonnull
    NotificationEndpoint updateEndpoint(@Nonnull final String endpointID,
                                        @Nonnull final NotificationEndpointUpdate notificationEndpointUpdate);

    /**
     * Delete a notification endpoint.
     *
     * @param notificationEndpoint notification endpoint
     */
    void deleteNotificationEndpoint(@Nonnull final NotificationEndpoint notificationEndpoint);

    /**
     * Delete a notification endpoint.
     *
     * @param endpointID ID of notification endpoint
     */
    void deleteNotificationEndpoint(@Nonnull final String endpointID);

    /**
     * Get notification endpoints.
     *
     * @param orgID only show notification endpoints belonging to specified organization
     * @return A list of notification endpoint
     */
    @Nonnull
    List<NotificationEndpoint> findNotificationEndpoints(@Nonnull final String orgID);

    /**
     * Get all notification endpoints.
     *
     * @param orgID       only show notification endpoints belonging to specified organization
     * @param findOptions the find options
     * @return A list of notification endpoint
     */
    @Nonnull
    NotificationEndpoints findNotificationEndpoints(@Nonnull final String orgID,
                                                    @Nonnull final FindOptions findOptions);

    /**
     * Clone a Slack Notification endpoint.
     *
     * @param name       name of cloned endpoint
     * @param token      Slack WebHook Token
     * @param endpointID ID of endpoint to clone
     * @return Notification endpoint cloned
     */
    @Nonnull
    SlackNotificationEndpoint cloneSlackEndpoint(@Nonnull final String name,
                                                 @Nullable final String token,
                                                 @Nonnull final String endpointID);

    /**
     * Clone a Slack Notification endpoint.
     *
     * @param name     name of cloned endpoint
     * @param token    Slack WebHook Token
     * @param endpoint endpoint to clone
     * @return Notification endpoint cloned
     */
    @Nonnull
    SlackNotificationEndpoint cloneSlackEndpoint(@Nonnull final String name,
                                                 @Nullable final String token,
                                                 @Nonnull final SlackNotificationEndpoint endpoint);

    /**
     * Clone a PagerDuty Notification endpoint.
     *
     * @param name       name of cloned endpoint
     * @param routingKey Routing Key
     * @param endpointID ID of endpoint to clone
     * @return Notification endpoint cloned
     */
    @Nonnull
    PagerDutyNotificationEndpoint clonePagerDutyEndpoint(@Nonnull final String name,
                                                         @Nonnull final String routingKey,
                                                         @Nonnull final String endpointID);

    /**
     * Clone a PagerDuty Notification endpoint.
     *
     * @param name       name of cloned endpoint
     * @param routingKey Routing Key
     * @param endpoint   endpoint to clone
     * @return Notification endpoint cloned
     */
    @Nonnull
    PagerDutyNotificationEndpoint clonePagerDutyEndpoint(@Nonnull final String name,
                                                         @Nonnull final String routingKey,
                                                         @Nonnull final PagerDutyNotificationEndpoint endpoint);

    /**
     * Clone a Http Notification endpoint without authentication.
     *
     * @param name       name of cloned endpoint
     * @param endpointID ID of endpoint to clone
     * @return Notification endpoint cloned
     */
    @Nonnull
    HTTPNotificationEndpoint cloneHTTPEndpoint(@Nonnull final String name,
                                               @Nonnull final String endpointID);

    /**
     * Clone a Http Notification endpoint without authentication.
     *
     * @param name     name of cloned endpoint
     * @param endpoint endpoint to clone
     * @return Notification endpoint cloned
     */
    @Nonnull
    HTTPNotificationEndpoint cloneHTTPEndpoint(@Nonnull final String name,
                                               @Nonnull final HTTPNotificationEndpoint endpoint);

    /**
     * Clone a Http Notification endpoint with Http Basic authentication.
     *
     * @param name       name of cloned endpoint
     * @param username   HTTP Basic Username
     * @param password   HTTP Basic Password
     * @param endpointID ID of endpoint to clone
     * @return Notification endpoint cloned
     */
    @Nonnull
    HTTPNotificationEndpoint cloneHTTPEndpointBasicAuth(@Nonnull final String name,
                                                        @Nonnull final String username,
                                                        @Nonnull final String password,
                                                        @Nonnull final String endpointID);

    /**
     * Clone a Http Notification endpoint with Http Basic authentication.
     *
     * @param name     name of cloned endpoint
     * @param username HTTP Basic Username
     * @param password HTTP Basic Password
     * @param endpoint endpoint to clone
     * @return Notification endpoint cloned
     */
    @Nonnull
    HTTPNotificationEndpoint cloneHTTPEndpointBasicAuth(@Nonnull final String name,
                                                        @Nonnull final String username,
                                                        @Nonnull final String password,
                                                        @Nonnull final HTTPNotificationEndpoint endpoint);

    /**
     * Clone a Http Notification endpoint with Bearer authentication.
     *
     * @param name       name of cloned endpoint
     * @param token      Bearer token
     * @param endpointID ID of  endpoint to clone
     * @return Notification endpoint cloned
     */
    @Nonnull
    HTTPNotificationEndpoint cloneHTTPEndpointBearer(@Nonnull final String name,
                                                     @Nonnull final String token,
                                                     @Nonnull final String endpointID);

    /**
     * Clone a Http Notification endpoint with Bearer authentication.
     *
     * @param name     name of cloned endpoint
     * @param token    Bearer token
     * @param endpoint endpoint to clone
     * @return Notification endpoint cloned
     */
    @Nonnull
    HTTPNotificationEndpoint cloneHTTPEndpointBearer(@Nonnull final String name,
                                                     @Nonnull final String token,
                                                     @Nonnull final HTTPNotificationEndpoint endpoint);

    /**
     * Get a notification endpoint.
     *
     * @param endpointID ID of notification endpoint
     * @return the notification endpoint requested
     */
    @Nonnull
    NotificationEndpoint findNotificationEndpointByID(@Nonnull final String endpointID);

    /**
     * List all labels for a notification endpoint.
     *
     * @param endpoint the notification endpoint
     * @return a list of all labels for a notification endpoint
     */
    @Nonnull
    List<Label> getLabels(@Nonnull final NotificationEndpoint endpoint);

    /**
     * List all labels for a notification endpoint.
     *
     * @param endpointID ID of the notification endpoint
     * @return a list of all labels for a notification endpoint
     */
    @Nonnull
    List<Label> getLabels(@Nonnull final String endpointID);

    /**
     * Add a label to a notification endpoint.
     *
     * @param label    label to add
     * @param endpoint the notification endpoint
     * @return the label was added to the notification endpoint
     */
    @Nonnull
    LabelResponse addLabel(@Nonnull final Label label, @Nonnull final NotificationEndpoint endpoint);

    /**
     * Add a label to a notification endpoint.
     *
     * @param endpointID the ID of the notification endpoint
     * @param labelID    the ID of label to add
     * @return the label was added to the notification endpoint
     */
    @Nonnull
    LabelResponse addLabel(@Nonnull final String labelID, @Nonnull final String endpointID);

    /**
     * Delete label from a notification endpoint.
     *
     * @param label    the label to delete
     * @param endpoint the notification endpoint
     */
    void deleteLabel(@Nonnull final Label label, @Nonnull final NotificationEndpoint endpoint);

    /**
     * Delete label from a notification endpoint.
     *
     * @param endpointID ID of the notification endpoint
     * @param labelID    the label id to delete
     */
    void deleteLabel(@Nonnull final String labelID, @Nonnull final String endpointID);
}
