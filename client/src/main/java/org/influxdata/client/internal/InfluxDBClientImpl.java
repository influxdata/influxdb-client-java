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
import org.influxdata.client.DashboardsApi;
import org.influxdata.client.InfluxDBClient;
import org.influxdata.client.InfluxDBClientOptions;
import org.influxdata.client.LabelsApi;
import org.influxdata.client.OrganizationsApi;
import org.influxdata.client.ProtosApi;
import org.influxdata.client.QueryApi;
import org.influxdata.client.ScraperTargetsApi;
import org.influxdata.client.SourcesApi;
import org.influxdata.client.TasksApi;
import org.influxdata.client.TelegrafsApi;
import org.influxdata.client.TemplatesApi;
import org.influxdata.client.UsersApi;
import org.influxdata.client.VariablesApi;
import org.influxdata.client.WriteApi;
import org.influxdata.client.WriteOptions;
import org.influxdata.client.domain.Check;
import org.influxdata.client.domain.IsOnboarding;
import org.influxdata.client.domain.OnboardingRequest;
import org.influxdata.client.domain.OnboardingResponse;
import org.influxdata.client.domain.Ready;
import org.influxdata.client.service.AuthorizationsService;
import org.influxdata.client.service.BucketsService;
import org.influxdata.client.service.DashboardsService;
import org.influxdata.client.service.LabelsService;
import org.influxdata.client.service.OrganizationsService;
import org.influxdata.client.service.ProtosService;
import org.influxdata.client.service.QueryService;
import org.influxdata.client.service.ReadyService;
import org.influxdata.client.service.ScraperTargetsService;
import org.influxdata.client.service.SetupService;
import org.influxdata.client.service.SourcesService;
import org.influxdata.client.service.TasksService;
import org.influxdata.client.service.TelegrafsService;
import org.influxdata.client.service.TemplatesService;
import org.influxdata.client.service.UsersService;
import org.influxdata.client.service.VariablesService;
import org.influxdata.client.service.WriteService;
import org.influxdata.exceptions.InfluxException;
import org.influxdata.exceptions.UnprocessableEntityException;

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
        return new QueryApiImpl(retrofit.create(QueryService.class));
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

        return new WriteApiImpl(writeOptions, retrofit.create(WriteService.class));
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
    public ProtosApi getProtosApi() {
        return new ProtosApiImpl(retrofit.create(ProtosService.class));
    }

    @Nonnull
    @Override
    public DashboardsApi getDashboardsApi() {
        return new DashboardsApiImpl(retrofit.create(DashboardsService.class));
    }

    @Nonnull
    @Override
    public Check health() {

        return health(healthService.healthGet(null));
    }

    @Nullable
    @Override
    public Ready ready() {
        Call<Ready> call = readyService.readyGet();
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

        Call<OnboardingResponse> call = setupService.setupPost(onboarding, null);

        return execute(call);
    }

    @Nonnull
    @Override
    public Boolean isOnboardingAllowed() {

        IsOnboarding isOnboarding = execute(setupService.setupGet(null));

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