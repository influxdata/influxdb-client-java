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
package org.influxdata.client;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.influxdata.LogLevel;
import org.influxdata.client.domain.Authorization;
import org.influxdata.client.domain.Bucket;
import org.influxdata.client.domain.Check;
import org.influxdata.client.domain.Document;
import org.influxdata.client.domain.Label;
import org.influxdata.client.domain.OnboardingRequest;
import org.influxdata.client.domain.OnboardingResponse;
import org.influxdata.client.domain.Organization;
import org.influxdata.client.domain.Proto;
import org.influxdata.client.domain.Ready;
import org.influxdata.client.domain.ScraperTargetResponse;
import org.influxdata.client.domain.Source;
import org.influxdata.client.domain.Task;
import org.influxdata.client.domain.Telegraf;
import org.influxdata.client.domain.User;
import org.influxdata.client.domain.Variable;
import org.influxdata.exceptions.UnprocessableEntityException;

/**
 * The client of theInfluxDB 2.0for Time Series that implements HTTP API defined by
 * <a href="https://github.com/influxdata/influxdb/blob/master/http/swagger.yml">Influx API Service swagger.yml</a>.
 *
 * @author Jakub Bednar (bednar@github) (11/10/2018 08:56)
 */
public interface InfluxDBClient extends AutoCloseable {

    /**
     * Get the Query client.
     *
     * @return the new client instance for the Query API
     */
    @Nonnull
    QueryApi getQueryApi();

    /**
     * Get the Write client.
     *
     * @return the new client instance for the Write API
     */
    @Nonnull
    WriteApi getWriteApi();

    /**
     * Get the Write client.
     *
     * @param writeOptions the writes configuration
     * @return the new client instance for the Write API
     */
    @Nonnull
    WriteApi getWriteApi(@Nonnull final WriteOptions writeOptions);


    /**
     * Get the {@link Authorization} client.
     *
     * @return the new client instance for Authorization API
     */
    @Nonnull
    AuthorizationsApi getAuthorizationsApi();

    /**
     * Get the {@link Bucket} client.
     *
     * @return the new client instance for Bucket API
     */
    @Nonnull
    BucketsApi getBucketsApi();

    /**
     * Get the {@link Organization} client.
     *
     * @return the new client instance for Organization API
     */
    @Nonnull
    OrganizationsApi getOrganizationsApi();

    /**
     * Get the {@link Source} client.
     *
     * @return the new client instance for Source API
     */
    @Nonnull
    SourcesApi getSourcesApi();

    /**
     * Get the {@link Task} client.
     *
     * @return the new client instance for Task API
     */
    @Nonnull
    TasksApi getTasksApi();

    /**
     * Get the {@link User} client.
     *
     * @return the new client instance for User API
     */
    @Nonnull
    UsersApi getUsersApi();

    /**
     * Get the {@link ScraperTargetResponse} client.
     *
     * @return the new client instance for Scraper API
     */
    @Nonnull
    ScraperTargetsApi getScraperTargetsApi();

    /**
     * Get the {@link Telegraf} client.
     *
     * @return the new client instance for Telegrafs API
     */
    @Nonnull
    TelegrafsApi getTelegrafsApi();

    /**
     * Get the {@link Label} client.
     *
     * @return the new client instance for Label API
     */
    @Nonnull
    LabelsApi getLabelsApi();

    /**
     * Get the {@link Document} client.
     *
     * @return the new client instance for Template API
     */
    @Nonnull
    TemplatesApi getTemplatesApi();

    /**
     * Get the {@link Variable} client.
     *
     * @return the new client instance for Variable API
     */
    @Nonnull
    VariablesApi getVariablesApi();

    /**
     * Get the {@link Proto} client.
     *
     * @return the new client instance for Proto API
     */
    @Nonnull
    ProtosApi getProtosApi();

    /**
     * Get the health of an instance.
     *
     * @return health of an instance
     */
    @Nonnull
    Check health();

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
     * Enable Gzip compress for http request body.
     * <p>
     * Currently only the "Write" endpoint supports the Gzip compression.
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
}