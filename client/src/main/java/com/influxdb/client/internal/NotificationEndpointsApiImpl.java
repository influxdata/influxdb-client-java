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

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.influxdb.Arguments;
import com.influxdb.client.FindOptions;
import com.influxdb.client.NotificationEndpointsApi;
import com.influxdb.client.domain.HTTPNotificationEndpoint;
import com.influxdb.client.domain.HTTPNotificationEndpoint.MethodEnum;
import com.influxdb.client.domain.Label;
import com.influxdb.client.domain.LabelMapping;
import com.influxdb.client.domain.LabelResponse;
import com.influxdb.client.domain.LabelsResponse;
import com.influxdb.client.domain.NotificationEndpoint;
import com.influxdb.client.domain.NotificationEndpointBase;
import com.influxdb.client.domain.NotificationEndpointType;
import com.influxdb.client.domain.NotificationEndpointUpdate;
import com.influxdb.client.domain.NotificationEndpoints;
import com.influxdb.client.domain.PagerDutyNotificationEndpoint;
import com.influxdb.client.domain.SlackNotificationEndpoint;
import com.influxdb.client.service.NotificationEndpointsService;
import com.influxdb.internal.AbstractRestClient;

import retrofit2.Call;

/**
 * @author Jakub Bednar (11/09/2019 09:23)
 */
final class NotificationEndpointsApiImpl extends AbstractRestClient implements NotificationEndpointsApi {

    private final NotificationEndpointsService service;

    NotificationEndpointsApiImpl(@Nonnull final NotificationEndpointsService service) {

        Arguments.checkNotNull(service, "notificationEndpointsService");

        this.service = service;
    }

    @Nonnull
    @Override
    public SlackNotificationEndpoint createSlackEndpoint(@Nonnull final String name,
                                                         @Nonnull final String url,
                                                         @Nonnull final String orgID) {

        Arguments.checkNotNull(url, "url");

        return createSlackEndpoint(name, url, null, orgID);
    }

    @Nonnull
    @Override
    public SlackNotificationEndpoint createSlackEndpoint(@Nonnull final String name,
                                                         @Nullable final String url,
                                                         @Nullable final String token,
                                                         @Nonnull final String orgID) {

        Arguments.checkNonEmpty(name, "name");
        Arguments.checkNonEmpty(orgID, "orgID");

        SlackNotificationEndpoint endpoint = new SlackNotificationEndpoint();
        endpoint.setType(NotificationEndpointType.SLACK);
        endpoint.setUrl(url);
        endpoint.setToken(token);
        endpoint.orgID(orgID);
        endpoint.setName(name);
        endpoint.setStatus(NotificationEndpointBase.StatusEnum.ACTIVE);

        return (SlackNotificationEndpoint) createEndpoint(endpoint);
    }

    @Nonnull
    @Override
    public PagerDutyNotificationEndpoint createPagerDutyEndpoint(@Nonnull final String name,
                                                                 @Nonnull final String clientURL,
                                                                 @Nonnull final String routingKey,
                                                                 @Nonnull final String orgID) {

        Arguments.checkNonEmpty(name, "name");
        Arguments.checkNonEmpty(clientURL, "clientURL");
        Arguments.checkNonEmpty(routingKey, "routingKey");
        Arguments.checkNonEmpty(orgID, "orgID");

        PagerDutyNotificationEndpoint endpoint = new PagerDutyNotificationEndpoint();
        endpoint.setType(NotificationEndpointType.PAGERDUTY);
        endpoint.setClientURL(clientURL);
        endpoint.setRoutingKey(routingKey);
        endpoint.orgID(orgID);
        endpoint.setName(name);
        endpoint.setStatus(NotificationEndpointBase.StatusEnum.ACTIVE);

        return (PagerDutyNotificationEndpoint) createEndpoint(endpoint);
    }

    @Nonnull
    @Override
    public HTTPNotificationEndpoint createHTTPEndpoint(@Nonnull final String name,
                                                       @Nonnull final String url,
                                                       @Nonnull final MethodEnum method,
                                                       @Nonnull final String orgID) {

        Arguments.checkNonEmpty(name, "name");
        Arguments.checkNonEmpty(url, "url");
        Arguments.checkNotNull(method, "method");
        Arguments.checkNonEmpty(orgID, "orgID");

        HTTPNotificationEndpoint endpoint = new HTTPNotificationEndpoint();
        endpoint.setType(NotificationEndpointType.HTTP);
        endpoint.setMethod(method);
        endpoint.setUrl(url);
        endpoint.orgID(orgID);
        endpoint.setName(name);
        endpoint.setAuthMethod(HTTPNotificationEndpoint.AuthMethodEnum.NONE);
        endpoint.setStatus(NotificationEndpointBase.StatusEnum.ACTIVE);

        return (HTTPNotificationEndpoint) createEndpoint(endpoint);
    }

    @Nonnull
    @Override
    public HTTPNotificationEndpoint createHTTPEndpointBasicAuth(@Nonnull final String name,
                                                                @Nonnull final String url,
                                                                @Nonnull final MethodEnum method,
                                                                @Nonnull final String username,
                                                                @Nonnull final String password,
                                                                @Nonnull final String orgID) {

        Arguments.checkNonEmpty(name, "name");
        Arguments.checkNonEmpty(url, "url");
        Arguments.checkNotNull(method, "method");
        Arguments.checkNonEmpty(username, "username");
        Arguments.checkNonEmpty(password, "password");
        Arguments.checkNonEmpty(orgID, "orgID");

        HTTPNotificationEndpoint endpoint = new HTTPNotificationEndpoint();
        endpoint.setType(NotificationEndpointType.HTTP);
        endpoint.setMethod(method);
        endpoint.setUrl(url);
        endpoint.orgID(orgID);
        endpoint.setName(name);
        endpoint.setUsername(username);
        endpoint.setPassword(password);
        endpoint.setAuthMethod(HTTPNotificationEndpoint.AuthMethodEnum.BASIC);
        endpoint.setStatus(NotificationEndpointBase.StatusEnum.ACTIVE);

        return (HTTPNotificationEndpoint) createEndpoint(endpoint);
    }

    @Nonnull
    @Override
    public HTTPNotificationEndpoint createHTTPEndpointBearer(@Nonnull final String name,
                                                             @Nonnull final String url,
                                                             @Nonnull final MethodEnum method,
                                                             @Nonnull final String token,
                                                             @Nonnull final String orgID) {

        Arguments.checkNonEmpty(name, "name");
        Arguments.checkNonEmpty(url, "url");
        Arguments.checkNotNull(method, "method");
        Arguments.checkNonEmpty(token, "token");
        Arguments.checkNonEmpty(orgID, "orgID");

        HTTPNotificationEndpoint endpoint = new HTTPNotificationEndpoint();
        endpoint.setType(NotificationEndpointType.HTTP);
        endpoint.setMethod(method);
        endpoint.setUrl(url);
        endpoint.orgID(orgID);
        endpoint.setName(name);
        endpoint.setToken(token);
        endpoint.setAuthMethod(HTTPNotificationEndpoint.AuthMethodEnum.BEARER);
        endpoint.setStatus(NotificationEndpointBase.StatusEnum.ACTIVE);

        return (HTTPNotificationEndpoint) createEndpoint(endpoint);
    }

    @Override
    @Nonnull
    public NotificationEndpoint createEndpoint(@Nonnull final NotificationEndpoint notificationEndpoint) {

        Arguments.checkNotNull(notificationEndpoint, "notificationEndpoint");

        Call<NotificationEndpoint> endpointCall = service
                .createNotificationEndpoint(notificationEndpoint);

        return execute(endpointCall);
    }

    @Nonnull
    @Override
    public NotificationEndpoint updateEndpoint(@Nonnull final NotificationEndpoint notificationEndpoint) {

        Arguments.checkNotNull(notificationEndpoint, "notificationEndpoint");

        NotificationEndpointUpdate update = new NotificationEndpointUpdate()
                .name(notificationEndpoint.getName())
                .description(notificationEndpoint.getDescription())
                .status(NotificationEndpointUpdate.StatusEnum.fromValue(notificationEndpoint.getStatus().getValue()));

        return updateEndpoint(notificationEndpoint.getId(), update);
    }

    @Nonnull
    @Override
    public NotificationEndpoint updateEndpoint(@Nonnull final String endpointID,
                                               @Nonnull final NotificationEndpointUpdate notificationEndpointUpdate) {

        Arguments.checkNonEmpty(endpointID, "endpointID");
        Arguments.checkNotNull(notificationEndpointUpdate, "notificationEndpointUpdate");

        Call<NotificationEndpoint> endpointCall = service
                .patchNotificationEndpointsID(endpointID, notificationEndpointUpdate, null);

        return execute(endpointCall);
    }

    @Override
    public void deleteNotificationEndpoint(@Nonnull final NotificationEndpoint notificationEndpoint) {

        Arguments.checkNotNull(notificationEndpoint, "notificationEndpoint");

        deleteNotificationEndpoint(notificationEndpoint.getId());
    }

    @Override
    public void deleteNotificationEndpoint(@Nonnull final String endpointID) {

        Arguments.checkNonEmpty(endpointID, "endpointID");

        Call<Void> endpointCall = service
                .deleteNotificationEndpointsID(endpointID, null);

        execute(endpointCall);
    }

    @Nonnull
    @Override
    public List<NotificationEndpoint> findNotificationEndpoints(@Nonnull final String orgID) {

        Arguments.checkNonEmpty(orgID, "orgID");

        return findNotificationEndpoints(orgID, new FindOptions()).getNotificationEndpoints();
    }

    @Nonnull
    @Override
    public NotificationEndpoints findNotificationEndpoints(@Nonnull final String orgID,
                                                           @Nonnull final FindOptions findOptions) {

        Arguments.checkNonEmpty(orgID, "orgID");
        Arguments.checkNotNull(findOptions, "findOptions");

        Call<NotificationEndpoints> endpointCall = service
                .getNotificationEndpoints(orgID, null, findOptions.getOffset(), findOptions.getLimit());

        return execute(endpointCall);
    }

    @Nonnull
    @Override
    public SlackNotificationEndpoint cloneSlackEndpoint(@Nonnull final String name,
                                                        @Nullable final String token,
                                                        @Nonnull final String endpointID) {

        Arguments.checkNonEmpty(name, "name");
        Arguments.checkNotNull(endpointID, "endpointID");

        return cloneSlackEndpoint(name, token, (SlackNotificationEndpoint) findNotificationEndpointByID(endpointID));
    }

    @Nonnull
    @Override
    public SlackNotificationEndpoint cloneSlackEndpoint(@Nonnull final String name,
                                                        @Nullable final String token,
                                                        @Nonnull final SlackNotificationEndpoint endpoint) {

        Arguments.checkNonEmpty(name, "name");
        Arguments.checkNotNull(endpoint, "endpoint");

        SlackNotificationEndpoint cloned = new SlackNotificationEndpoint()
                .url(endpoint.getUrl())
                .token(token);

        return (SlackNotificationEndpoint) cloneEndpoint(name, endpoint, cloned);
    }

    @Nonnull
    @Override
    public PagerDutyNotificationEndpoint clonePagerDutyEndpoint(@Nonnull final String name,
                                                                @Nonnull final String routingKey,
                                                                @Nonnull final String endpointID) {

        Arguments.checkNonEmpty(name, "name");
        Arguments.checkNonEmpty(routingKey, "routingKey");
        Arguments.checkNonEmpty(endpointID, "endpointID");

        return clonePagerDutyEndpoint(name, routingKey,
                (PagerDutyNotificationEndpoint) findNotificationEndpointByID(endpointID));
    }

    @Nonnull
    @Override
    public PagerDutyNotificationEndpoint clonePagerDutyEndpoint(@Nonnull final String name,
                                                                @Nonnull final String routingKey,
                                                                @Nonnull final PagerDutyNotificationEndpoint endpoint) {

        Arguments.checkNonEmpty(name, "name");
        Arguments.checkNonEmpty(routingKey, "routingKey");
        Arguments.checkNotNull(endpoint, "endpoint");

        PagerDutyNotificationEndpoint cloned = new PagerDutyNotificationEndpoint()
                .clientURL(endpoint.getClientURL())
                .routingKey(routingKey);

        return (PagerDutyNotificationEndpoint) cloneEndpoint(name, endpoint, cloned);
    }

    @Nonnull
    @Override
    public HTTPNotificationEndpoint cloneHTTPEndpoint(@Nonnull final String name, @Nonnull final String endpointID) {

        Arguments.checkNonEmpty(name, "name");
        Arguments.checkNonEmpty(endpointID, "endpointID");

        return cloneHTTPEndpoint(name, (HTTPNotificationEndpoint) findNotificationEndpointByID(endpointID));
    }

    @Nonnull
    @Override
    public HTTPNotificationEndpoint cloneHTTPEndpoint(@Nonnull final String name,
                                                      @Nonnull final HTTPNotificationEndpoint endpoint) {

        Arguments.checkNonEmpty(name, "name");
        Arguments.checkNotNull(endpoint, "endpoint");

        HTTPNotificationEndpoint cloned = new HTTPNotificationEndpoint()
                .url(endpoint.getUrl())
                .method(endpoint.getMethod())
                .authMethod(HTTPNotificationEndpoint.AuthMethodEnum.NONE)
                .contentTemplate(endpoint.getContentTemplate())
                .headers(endpoint.getHeaders());

        return (HTTPNotificationEndpoint) cloneEndpoint(name, endpoint, cloned);
    }


    @Nonnull
    @Override
    public HTTPNotificationEndpoint cloneHTTPEndpointBasicAuth(@Nonnull final String name,
                                                               @Nonnull final String username,
                                                               @Nonnull final String password,
                                                               @Nonnull final String endpointID) {

        Arguments.checkNonEmpty(name, "name");
        Arguments.checkNonEmpty(username, "username");
        Arguments.checkNonEmpty(password, "password");
        Arguments.checkNonEmpty(endpointID, "endpointID");

        return cloneHTTPEndpointBasicAuth(name, username, password,
                (HTTPNotificationEndpoint) findNotificationEndpointByID(endpointID));
    }

    @Nonnull
    @Override
    public HTTPNotificationEndpoint cloneHTTPEndpointBasicAuth(@Nonnull final String name,
                                                               @Nonnull final String username,
                                                               @Nonnull final String password,
                                                               @Nonnull final HTTPNotificationEndpoint endpoint) {

        Arguments.checkNonEmpty(name, "name");
        Arguments.checkNonEmpty(username, "username");
        Arguments.checkNonEmpty(password, "password");
        Arguments.checkNotNull(endpoint, "endpoint");

        HTTPNotificationEndpoint cloned = new HTTPNotificationEndpoint()
                .url(endpoint.getUrl())
                .username(username)
                .password(password)
                .method(endpoint.getMethod())
                .authMethod(HTTPNotificationEndpoint.AuthMethodEnum.BASIC)
                .contentTemplate(endpoint.getContentTemplate())
                .headers(endpoint.getHeaders());

        return (HTTPNotificationEndpoint) cloneEndpoint(name, endpoint, cloned);
    }

    @Nonnull
    @Override
    public HTTPNotificationEndpoint cloneHTTPEndpointBearer(@Nonnull final String name,
                                                            @Nonnull final String token,
                                                            @Nonnull final String endpointID) {

        Arguments.checkNonEmpty(name, "name");
        Arguments.checkNonEmpty(token, "token");
        Arguments.checkNonEmpty(endpointID, "endpointID");

        return cloneHTTPEndpointBearer(name, token,
                (HTTPNotificationEndpoint) findNotificationEndpointByID(endpointID));
    }

    @Nonnull
    @Override
    public HTTPNotificationEndpoint cloneHTTPEndpointBearer(@Nonnull final String name,
                                                            @Nonnull final String token,
                                                            @Nonnull final HTTPNotificationEndpoint endpoint) {

        Arguments.checkNonEmpty(name, "name");
        Arguments.checkNonEmpty(token, "token");
        Arguments.checkNotNull(endpoint, "endpoint");

        HTTPNotificationEndpoint cloned = new HTTPNotificationEndpoint().url(endpoint.getUrl())
                .token(token)
                .method(endpoint.getMethod())
                .authMethod(HTTPNotificationEndpoint.AuthMethodEnum.BEARER)
                .contentTemplate(endpoint.getContentTemplate())
                .headers(endpoint.getHeaders());

        return (HTTPNotificationEndpoint) cloneEndpoint(name, endpoint, cloned);
    }

    @Nonnull
    private NotificationEndpoint cloneEndpoint(@Nonnull final String name,
                                               @Nonnull final NotificationEndpoint toCloneEndpoint,
                                               @Nonnull final NotificationEndpoint clonedEndpoint) {

        Arguments.checkNonEmpty(name, "name");
        Arguments.checkNotNull(toCloneEndpoint, "notificationEndpoint");

        clonedEndpoint
                .name(name)
                .orgID(toCloneEndpoint.getOrgID())
                .description(toCloneEndpoint.getDescription())
                .status(toCloneEndpoint.getStatus())
                .type(toCloneEndpoint.getType());

        NotificationEndpoint created = createEndpoint(clonedEndpoint);

        getLabels(created).forEach(label -> addLabel(label, created));

        return created;
    }

    @Nonnull
    @Override
    public NotificationEndpoint findNotificationEndpointByID(@Nonnull final String endpointID) {

        Arguments.checkNonEmpty(endpointID, "endpointID");

        Call<NotificationEndpoint> endpointCall = service
                .getNotificationEndpointsID(endpointID, null);

        return execute(endpointCall);
    }

    @Nonnull
    @Override
    public List<Label> getLabels(@Nonnull final NotificationEndpoint endpoint) {

        Arguments.checkNotNull(endpoint, "endpoint");

        return getLabels(endpoint.getId());
    }

    @Nonnull
    @Override
    public List<Label> getLabels(@Nonnull final String endpointID) {

        Arguments.checkNonEmpty(endpointID, "endpointID");

        Call<LabelsResponse> call = service.getNotificationEndpointsIDLabels(endpointID, null);

        return execute(call).getLabels();
    }

    @Nonnull
    @Override
    public LabelResponse addLabel(@Nonnull final Label label, @Nonnull final NotificationEndpoint endpoint) {

        Arguments.checkNotNull(label, "label");
        Arguments.checkNotNull(endpoint, "endpoint");

        return addLabel(label.getId(), endpoint.getId());
    }

    @Nonnull
    @Override
    public LabelResponse addLabel(@Nonnull final String labelID, @Nonnull final String endpointID) {

        Arguments.checkNonEmpty(labelID, "labelID");
        Arguments.checkNonEmpty(endpointID, "endpointID");

        LabelMapping labelMapping = new LabelMapping();
        labelMapping.setLabelID(labelID);

        Call<LabelResponse> call = service.postNotificationEndpointIDLabels(endpointID, labelMapping, null);

        return execute(call);
    }

    @Override
    public void deleteLabel(@Nonnull final Label label, @Nonnull final NotificationEndpoint endpoint) {

        Arguments.checkNotNull(label, "label");
        Arguments.checkNotNull(endpoint, "bucket");

        deleteLabel(label.getId(), endpoint.getId());
    }

    @Override
    public void deleteLabel(@Nonnull final String labelID, @Nonnull final String endpointID) {

        Arguments.checkNonEmpty(labelID, "labelID");
        Arguments.checkNonEmpty(endpointID, "endpointID");

        Call<Void> call = service.deleteNotificationEndpointsIDLabelsID(endpointID, labelID, null);
        execute(call);
    }
}
