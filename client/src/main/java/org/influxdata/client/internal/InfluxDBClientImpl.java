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
package org.influxdata.client.internal;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.influxdata.Arguments;
import org.influxdata.LogLevel;
import org.influxdata.client.AuthorizationsApi;
import org.influxdata.client.BucketsApi;
import org.influxdata.client.InfluxDBClient;
import org.influxdata.client.InfluxDBClientOptions;
import org.influxdata.client.LabelsApi;
import org.influxdata.client.OrganizationsApi;
import org.influxdata.client.QueryApi;
import org.influxdata.client.ScraperTargetsApi;
import org.influxdata.client.SourcesApi;
import org.influxdata.client.TasksApi;
import org.influxdata.client.TelegrafsApi;
import org.influxdata.client.UsersApi;
import org.influxdata.client.WriteApi;
import org.influxdata.client.WriteOptions;
import org.influxdata.client.domain.Check;
import org.influxdata.client.domain.IsOnboarding;
import org.influxdata.client.domain.OnboardingRequest;
import org.influxdata.client.domain.OnboardingResponse;
import org.influxdata.client.service.AuthorizationsService;
import org.influxdata.client.service.BucketsService;
import org.influxdata.client.service.LabelsService;
import org.influxdata.client.service.OrganizationsService;
import org.influxdata.client.service.ScraperTargetsService;
import org.influxdata.client.service.SourcesService;
import org.influxdata.client.service.TasksService;
import org.influxdata.client.service.TelegrafsService;
import org.influxdata.exceptions.InfluxException;
import org.influxdata.exceptions.UnprocessableEntityException;

import retrofit2.Call;

/**
 * @author Jakub Bednar (bednar@github) (11/10/2018 09:36)
 */
public final class InfluxDBClientImpl extends AbstractInfluxDBClient<InfluxDBService> implements InfluxDBClient {

    private static final Logger LOG = Logger.getLogger(InfluxDBClientImpl.class.getName());

    public InfluxDBClientImpl(@Nonnull final InfluxDBClientOptions options) {

        super(options, InfluxDBService.class);
    }

    @Nonnull
    @Override
    public QueryApi getQueryApi() {
        return new QueryApiImpl(influxDBService);
    }

    @Nonnull
    @Override
    public WriteApi getWriteApi() {
        return getWriteApi(WriteOptions.DEFAULTS);
    }

    @Nonnull
    @Override
    public WriteApi getWriteApi(@Nonnull final WriteOptions writeOptions) {

        Arguments.checkNotNull(writeOptions, "WriteOptions");

        return new WriteApiImpl(writeOptions, influxDBService);
    }

    @Nonnull
    @Override
    public AuthorizationsApi getAuthorizationsApi() {
        return new AuthorizationsApiImpl(influxDBService, retrofit.create(AuthorizationsService.class), gson);
    }

    @Nonnull
    @Override
    public BucketsApi getBucketsApi() {
        return new BucketsApiImpl(influxDBService, retrofit.create(BucketsService.class), gson);
    }

    @Nonnull
    @Override
    public OrganizationsApi getOrganizationsApi() {
        return new OrganizationsApiImpl(influxDBService, retrofit.create(OrganizationsService.class), gson);
    }

    @Nonnull
    @Override
    public SourcesApi getSourcesApi() {
        return new SourcesApiImpl(influxDBService, retrofit.create(SourcesService.class), this, gson);
    }

    @Nonnull
    @Override
    public TasksApi getTasksApi() {
        return new TasksApiImpl(influxDBService, retrofit.create(TasksService.class), gson);
    }

    @Nonnull
    @Override
    public UsersApi getUsersApi() {
        return new UsersApiImpl(influxDBService, gson);
    }

    @Nonnull
    @Override
    public ScraperTargetsApi getScraperTargetsApi() {
        return new ScraperTargetsApiImpl(influxDBService, retrofit.create(ScraperTargetsService.class), gson);
    }

    @Nonnull
    @Override
    public TelegrafsApi getTelegrafsApi() {
        return new TelegrafsApiImpl(influxDBService, retrofit.create(TelegrafsService.class), gson);
    }

    @Nonnull
    @Override
    public LabelsApi getLabelsApi() {
        return new LabelsApiImpl(influxDBService, retrofit.create(LabelsService.class), gson);
    }

    @Nonnull
    @Override
    public Check health() {

        return health(influxDBService.health());
    }

    @Nullable
    @Override
    public Check ready() {
        Call<Check> call = influxDBService.ready();
        try {
            return execute(call);
        } catch (InfluxException e) {
            LOG.log(Level.WARNING, "The exception occurs during check instance readiness", e);
            return null;
        }
    }

    @Nonnull
    @Override
    public OnboardingResponse onBoarding(@Nonnull final OnboardingRequest onboarding)
            throws UnprocessableEntityException {

        Arguments.checkNotNull(onboarding, "onboarding");

        String json = gson.toJson(onboarding);

        Call<OnboardingResponse> call = influxDBService.setup(createBody(json));

        return execute(call);
    }

    @Nonnull
    @Override
    public Boolean isOnboardingAllowed() {

        IsOnboarding isOnboarding = execute(influxDBService.setup());

        return isOnboarding.isAllowed();
    }

    @Nonnull
    @Override
    public LogLevel getLogLevel() {
        return getLogLevel(this.loggingInterceptor);
    }

    @Nonnull
    @Override
    public InfluxDBClient setLogLevel(@Nonnull final LogLevel logLevel) {

        setLogLevel(this.loggingInterceptor, logLevel);

        return this;
    }

    @Nonnull
    @Override
    public InfluxDBClient enableGzip() {

        this.gzipInterceptor.enableGzip();

        return this;
    }

    @Nonnull
    @Override
    public InfluxDBClient disableGzip() {

        this.gzipInterceptor.disableGzip();

        return this;
    }

    @Override
    public boolean isGzipEnabled() {

        return this.gzipInterceptor.isEnabledGzip();
    }
}