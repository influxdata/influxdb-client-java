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

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.influxdb.Arguments;
import com.influxdb.LogLevel;
import com.influxdb.client.AuthorizationsApi;
import com.influxdb.client.BucketsApi;
import com.influxdb.client.ChecksApi;
import com.influxdb.client.DashboardsApi;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientOptions;
import com.influxdb.client.LabelsApi;
import com.influxdb.client.NotificationEndpointsApi;
import com.influxdb.client.NotificationRulesApi;
import com.influxdb.client.OrganizationsApi;
import com.influxdb.client.QueryApi;
import com.influxdb.client.ScraperTargetsApi;
import com.influxdb.client.SourcesApi;
import com.influxdb.client.TasksApi;
import com.influxdb.client.TelegrafsApi;
import com.influxdb.client.TemplatesApi;
import com.influxdb.client.UsersApi;
import com.influxdb.client.VariablesApi;
import com.influxdb.client.WriteApi;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.WriteOptions;
import com.influxdb.client.domain.HealthCheck;
import com.influxdb.client.domain.IsOnboarding;
import com.influxdb.client.domain.OnboardingRequest;
import com.influxdb.client.domain.OnboardingResponse;
import com.influxdb.client.domain.Ready;
import com.influxdb.client.service.AuthorizationsService;
import com.influxdb.client.service.BucketsService;
import com.influxdb.client.service.ChecksService;
import com.influxdb.client.service.DashboardsService;
import com.influxdb.client.service.LabelsService;
import com.influxdb.client.service.NotificationEndpointsService;
import com.influxdb.client.service.NotificationRulesService;
import com.influxdb.client.service.OrganizationsService;
import com.influxdb.client.service.QueryService;
import com.influxdb.client.service.ReadyService;
import com.influxdb.client.service.ScraperTargetsService;
import com.influxdb.client.service.SetupService;
import com.influxdb.client.service.SourcesService;
import com.influxdb.client.service.TasksService;
import com.influxdb.client.service.TelegrafsService;
import com.influxdb.client.service.TemplatesService;
import com.influxdb.client.service.UsersService;
import com.influxdb.client.service.VariablesService;
import com.influxdb.client.service.WriteService;
import com.influxdb.exceptions.InfluxException;
import com.influxdb.exceptions.UnprocessableEntityException;

import retrofit2.Call;

/**
 * @author Jakub Bednar (bednar@github) (11/10/2018 09:36)
 */
public final class InfluxDBClientImpl extends AbstractInfluxDBClient implements InfluxDBClient {

    private static final Logger LOG = Logger.getLogger(InfluxDBClientImpl.class.getName());

    private final SetupService setupService;
    private final ReadyService readyService;


    public InfluxDBClientImpl(@Nonnull final InfluxDBClientOptions options) {

        super(options);

        setupService = retrofit.create(SetupService.class);
        readyService = retrofit.create(ReadyService.class);
    }

    @Nonnull
    @Override
    public QueryApi getQueryApi() {
        return new QueryApiImpl(retrofit.create(QueryService.class), options);
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

        return new WriteApiImpl(writeOptions, retrofit.create(WriteService.class), options);
    }

    @Nonnull
    @Override
    public WriteApiBlocking getWriteApiBlocking() {
        return new WriteApiBlockingImpl(retrofit.create(WriteService.class), options);
    }

    @Nonnull
    @Override
    public AuthorizationsApi getAuthorizationsApi() {
        return new AuthorizationsApiImpl(retrofit.create(AuthorizationsService.class));
    }

    @Nonnull
    @Override
    public BucketsApi getBucketsApi() {
        return new BucketsApiImpl(retrofit.create(BucketsService.class));
    }

    @Nonnull
    @Override
    public OrganizationsApi getOrganizationsApi() {
        return new OrganizationsApiImpl(retrofit.create(OrganizationsService.class));
    }

    @Nonnull
    @Override
    public SourcesApi getSourcesApi() {
        return new SourcesApiImpl(retrofit.create(SourcesService.class), this);
    }

    @Nonnull
    @Override
    public TasksApi getTasksApi() {
        return new TasksApiImpl(retrofit.create(TasksService.class));
    }

    @Nonnull
    @Override
    public UsersApi getUsersApi() {
        return new UsersApiImpl(retrofit.create(UsersService.class));
    }

    @Nonnull
    @Override
    public ScraperTargetsApi getScraperTargetsApi() {
        return new ScraperTargetsApiImpl(retrofit.create(ScraperTargetsService.class));
    }

    @Nonnull
    @Override
    public TelegrafsApi getTelegrafsApi() {
        return new TelegrafsApiImpl(retrofit.create(TelegrafsService.class));
    }

    @Nonnull
    @Override
    public LabelsApi getLabelsApi() {
        return new LabelsApiImpl(retrofit.create(LabelsService.class));
    }

    @Nonnull
    @Override
    public TemplatesApi getTemplatesApi() {
        return new TemplatesApiImpl(retrofit.create(TemplatesService.class));
    }

    @Nonnull
    @Override
    public VariablesApi getVariablesApi() {
        return new VariablesApiImpl(retrofit.create(VariablesService.class));
    }

    @Nonnull
    @Override
    public DashboardsApi getDashboardsApi() {
        return new DashboardsApiImpl(retrofit.create(DashboardsService.class));
    }

    @Nonnull
    @Override
    public ChecksApi getChecksApi() {
        return new ChecksApiImpl(retrofit.create(ChecksService.class));
    }

    @Nonnull
    @Override
    public NotificationEndpointsApi getNotificationEndpointsApi() {
        return new NotificationEndpointsApiImpl(retrofit.create(NotificationEndpointsService.class));
    }

    @Nonnull
    @Override
    public NotificationRulesApi getNotificationRulesApi() {
        return new NotificationRulesApiImpl(retrofit.create(NotificationRulesService.class));
    }

    @Nonnull
    @Override
    public <S> S getService(@Nonnull final Class<S> service) {

        Arguments.checkNotNull(service, "service");

        return retrofit.create(service);
    }

    @Nonnull
    @Override
    public HealthCheck health() {

        return health(healthService.getHealth(null));
    }

    @Nullable
    @Override
    public Ready ready() {
        Call<Ready> call = readyService.getReady(null);
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

        Call<OnboardingResponse> call = setupService.postSetup(onboarding, null);

        return execute(call);
    }

    @Nonnull
    @Override
    public Boolean isOnboardingAllowed() {

        IsOnboarding isOnboarding = execute(setupService.getSetup(null));

        return isOnboarding.getAllowed();
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