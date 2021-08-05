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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import com.influxdb.LogLevel;
import com.influxdb.client.domain.Authorization;
import com.influxdb.client.domain.Bucket;
import com.influxdb.client.domain.Check;
import com.influxdb.client.domain.Dashboard;
import com.influxdb.client.domain.Document;
import com.influxdb.client.domain.HealthCheck;
import com.influxdb.client.domain.Label;
import com.influxdb.client.domain.NotificationEndpoint;
import com.influxdb.client.domain.NotificationRules;
import com.influxdb.client.domain.OnboardingRequest;
import com.influxdb.client.domain.OnboardingResponse;
import com.influxdb.client.domain.Organization;
import com.influxdb.client.domain.Ready;
import com.influxdb.client.domain.ScraperTargetResponse;
import com.influxdb.client.domain.Source;
import com.influxdb.client.domain.Task;
import com.influxdb.client.domain.Telegraf;
import com.influxdb.client.domain.User;
import com.influxdb.client.domain.Variable;
import com.influxdb.exceptions.UnprocessableEntityException;

/**
 * The client of theInfluxDB 2.0for Time Series that implements HTTP API defined by
 * <a href="https://github.com/influxdata/influxdb/blob/master/http/swagger.yml">Influx API Service swagger.yml</a>.
 *
 * @author Jakub Bednar (bednar@github) (11/10/2018 08:56)
 */
@ThreadSafe
public interface InfluxDBClient extends AutoCloseable {

    /**
     * Create a new Query client.
     *
     * @return the new client instance for the Query API
     */
    @Nonnull
    QueryApi getQueryApi();

    /**
     * Create a new asynchronous non-blocking Write client.
     *
     * @deprecated use {@link #makeWriteApi()}, the API is subject to removal in a next major release
     * @return the new client instance for the Write API
     */
    @Nonnull
    @Deprecated
    WriteApi getWriteApi();

    /**
     * Create a new asynchronous non-blocking Write client.
     *
     * <p>
     * The {@link WriteApi} uses background thread to ingesting data into InfluxDB and is suppose to run as a singleton.
     * <b>Don't create new instance for every write.</b>
     * </p>
     *
     * @return the new client instance for the Write API
     */
    @Nonnull
    WriteApi makeWriteApi();

    /**
     * Create a new asynchronous non-blocking Write client.
     *
     * @param writeOptions the writes configuration
     * @deprecated use {@link #makeWriteApi(WriteOptions)}, the API is subject to removal in a next major release
     * @return the new client instance for the Write API
     */
    @Nonnull
    @Deprecated
    WriteApi getWriteApi(@Nonnull final WriteOptions writeOptions);

    /**
     * Create a new asynchronous non-blocking Write client.
     *
     * <p>
     * The {@link WriteApi} uses background thread to ingesting data into InfluxDB and is suppose to run as a singleton.
     * <b>Don't create new instance for every write.</b>
     * </p>
     *
     * @param writeOptions the writes configuration
     * @return the new client instance for the Write API
     */
    @Nonnull
    WriteApi makeWriteApi(@Nonnull final WriteOptions writeOptions);

    /**
     * Create a new synchronous blocking Write client.
     *
     * @return the new client instance for the Write API
     */
    @Nonnull
    WriteApiBlocking getWriteApiBlocking();

    /**
     * Create a new {@link Authorization} client.
     *
     * @return the new client instance for Authorization API
     */
    @Nonnull
    AuthorizationsApi getAuthorizationsApi();

    /**
     * Create a new {@link Bucket} client.
     *
     * @return the new client instance for Bucket API
     */
    @Nonnull
    BucketsApi getBucketsApi();

    /**
     * Create a new {@link Organization} client.
     *
     * @return the new client instance for Organization API
     */
    @Nonnull
    OrganizationsApi getOrganizationsApi();

    /**
     * Create a new {@link Source} client.
     *
     * @return the new client instance for Source API
     */
    @Nonnull
    SourcesApi getSourcesApi();

    /**
     * Create a new {@link Task} client.
     *
     * @return the new client instance for Task API
     */
    @Nonnull
    TasksApi getTasksApi();

    /**
     * Create a new {@link User} client.
     *
     * @return the new client instance for User API
     */
    @Nonnull
    UsersApi getUsersApi();

    /**
     * Create a new {@link ScraperTargetResponse} client.
     *
     * @return the new client instance for Scraper API
     */
    @Nonnull
    ScraperTargetsApi getScraperTargetsApi();

    /**
     * Create a new {@link Telegraf} client.
     *
     * @return the new client instance for Telegrafs API
     */
    @Nonnull
    TelegrafsApi getTelegrafsApi();

    /**
     * Create a new {@link Label} client.
     *
     * @return the new client instance for Label API
     */
    @Nonnull
    LabelsApi getLabelsApi();

    /**
     * Create a new {@link Document} client.
     *
     * @return the new client instance for Template API
     */
    @Nonnull
    TemplatesApi getTemplatesApi();

    /**
     * Create a new {@link Variable} client.
     *
     * @return the new client instance for Variable API
     */
    @Nonnull
    VariablesApi getVariablesApi();

    /**
     * Create a new {@link Dashboard} client.
     *
     * @return the new client instance for Dashboard API
     */
    @Nonnull
    DashboardsApi getDashboardsApi();

    /**
     * Create a new {@link Check} client.
     *
     * @return the new client instance for Checks API
     */
    @Nonnull
    ChecksApi getChecksApi();

    /**
     * Create a new {@link NotificationEndpoint} client.
     *
     * @return the new client instance for NotificationEndpoint API
     */
    @Nonnull
    NotificationEndpointsApi getNotificationEndpointsApi();

    /**
     * Create a new {@link NotificationRules} client.
     *
     * @return the new client instance for NotificationRules API
     */
    @Nonnull
    NotificationRulesApi getNotificationRulesApi();

    /**
     * Create a new Delete client.
     *
     * @return the new client instance for the Delete API
     */
    @Nonnull
    DeleteApi getDeleteApi();

    /**
     * Create an implementation of the API endpoints defined by the {@code service} interface.
     * <p>
     * The endpoints are defined in {@link com.influxdb.client.service}.
     *
     * @param service service to instantiate
     * @param <S>     type of service
     * @return instance of service
     */
    @Nonnull
    <S> S getService(@Nonnull final Class<S> service);

    /**
     * Get the health of an instance.
     *
     * @return health of an instance
     */
    @Nonnull
    HealthCheck health();

    /**
     * The readiness of the InfluxDB 2.0.
     *
     * @return return null if the InfluxDB is not ready
     */
    @Nullable
    Ready ready();

    /**
     * Post onboarding request, to setup initial user, org and bucket.
     *
     * @param onboarding to setup defaults
     * @return defaults for first run
     * @throws UnprocessableEntityException when an onboarding has already been completed
     */
    @Nonnull
    OnboardingResponse onBoarding(@Nonnull final OnboardingRequest onboarding) throws UnprocessableEntityException;

    /**
     * Check if database has default user, org, bucket created, returns true if not.
     *
     * @return {@link Boolean#FALSE} if onboarding has already been completed otherwise {@link Boolean#FALSE}.
     */
    @Nonnull
    Boolean isOnboardingAllowed();

    /**
     * @return the {@link LogLevel} that is used for logging requests and responses
     */
    @Nonnull
    LogLevel getLogLevel();

    /**
     * Set the log level for the request and response information.
     *
     * @param logLevel the log level to set.
     * @return the InfluxDBClient instance to be able to use it in a fluent manner.
     */
    @Nonnull
    InfluxDBClient setLogLevel(@Nonnull final LogLevel logLevel);

    /**
     * Enable Gzip compress for http requests.
     * <p>
     * Currently only the "Write" and "Query" endpoints supports the Gzip compression.
     *
     * @return the {@link InfluxDBClient} instance to be able to use it in a fluent manner.
     */
    @Nonnull
    InfluxDBClient enableGzip();

    /**
     * Disable Gzip compress for http request body.
     *
     * @return the {@link InfluxDBClient} instance to be able to use it in a fluent manner.
     */
    @Nonnull
    InfluxDBClient disableGzip();

    /**
     * Returns whether Gzip compress for http request body is enabled.
     *
     * @return true if gzip is enabled.
     */
    boolean isGzipEnabled();

    /**
     * Shutdown and close the client.
     */
    @Override
    void close();
}