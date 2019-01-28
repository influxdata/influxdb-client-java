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
package org.influxdata.platform.impl;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.influxdata.platform.Arguments;
import org.influxdata.platform.AuthorizationClient;
import org.influxdata.platform.BucketClient;
import org.influxdata.platform.LabelService;
import org.influxdata.platform.OrganizationClient;
import org.influxdata.platform.PlatformClient;
import org.influxdata.platform.QueryClient;
import org.influxdata.platform.ScraperClient;
import org.influxdata.platform.SourceClient;
import org.influxdata.platform.TaskClient;
import org.influxdata.platform.UserClient;
import org.influxdata.platform.WriteClient;
import org.influxdata.platform.domain.Health;
import org.influxdata.platform.domain.Ready;
import org.influxdata.platform.error.InfluxException;
import org.influxdata.platform.option.PlatformOptions;
import org.influxdata.platform.option.WriteOptions;
import org.influxdata.platform.rest.LogLevel;

import retrofit2.Call;

/**
 * @author Jakub Bednar (bednar@github) (11/10/2018 09:36)
 */
public final class PlatformClientImpl extends AbstractPlatformClient<PlatformService> implements PlatformClient {

    private static final Logger LOG = Logger.getLogger(PlatformClientImpl.class.getName());

    public PlatformClientImpl(@Nonnull final PlatformOptions options) {

        super(options, PlatformService.class);
    }

    @Nonnull
    @Override
    public QueryClient createQueryClient() {
        return new QueryClientImpl(platformService);
    }

    @Nonnull
    @Override
    public WriteClient createWriteClient() {
        return createWriteClient(WriteOptions.DEFAULTS);
    }

    @Nonnull
    @Override
    public WriteClient createWriteClient(@Nonnull final WriteOptions writeOptions) {

        Arguments.checkNotNull(writeOptions, "WriteOptions");

        return new WriteClientImpl(writeOptions, platformService);
    }

    @Nonnull
    @Override
    public AuthorizationClient createAuthorizationClient() {
        return new AuthorizationClientImpl(platformService, moshi);
    }

    @Nonnull
    @Override
    public BucketClient createBucketClient() {
        return new BucketClientImpl(platformService, moshi);
    }

    @Nonnull
    @Override
    public OrganizationClient createOrganizationClient() {
        return new OrganizationClientImpl(platformService, moshi);
    }

    @Nonnull
    @Override
    public SourceClient createSourceClient() {
        return new SourceClientImpl(platformService, moshi, this);
    }

    @Nonnull
    @Override
    public TaskClient createTaskClient() {
        return new TaskClientImpl(platformService, moshi);
    }

    @Nonnull
    @Override
    public UserClient createUserClient() {
        return new UserClientImpl(platformService, moshi);
    }

    @Nonnull
    @Override
    public ScraperClient createScraperClient() {
        return new ScraperClientImpl(platformService, moshi);
    }

    @Nonnull
    @Override
    public LabelService createLabelService() {
        return new LabelServiceImpl(platformService, moshi);
    }

    @Nonnull
    @Override
    public Health health() {

        return health(platformService.health());
    }

    @Nullable
    @Override
    public Ready ready() {
        Call<Ready> call = platformService.ready();
        try {
            return execute(call);
        } catch (InfluxException e) {
            LOG.log(Level.WARNING, "The exception occurs during check instance readiness", e);
            return null;
        }
    }

    @Nonnull
    @Override
    public LogLevel getLogLevel() {
        return getLogLevel(this.loggingInterceptor);
    }

    @Nonnull
    @Override
    public PlatformClient setLogLevel(@Nonnull final LogLevel logLevel) {

        setLogLevel(this.loggingInterceptor, logLevel);

        return this;
    }

    @Nonnull
    @Override
    public PlatformClient enableGzip() {

        this.gzipInterceptor.enableGzip();

        return this;
    }

    @Nonnull
    @Override
    public PlatformClient disableGzip() {

        this.gzipInterceptor.disableGzip();

        return this;
    }

    @Override
    public boolean isGzipEnabled() {

        return this.gzipInterceptor.isEnabledGzip();
    }
}