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

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

import com.influxdb.client.InfluxDBClientOptions;
import com.influxdb.client.JSON;
import com.influxdb.client.domain.Dialect;
import com.influxdb.client.domain.HealthCheck;
import com.influxdb.client.service.HealthService;
import com.influxdb.client.service.PingService;
import com.influxdb.exceptions.InfluxException;
import com.influxdb.internal.AbstractRestClient;
import com.influxdb.internal.UserAgentInterceptor;
import com.influxdb.utils.Arguments;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * @author Jakub Bednar (bednar@github) (20/11/2018 07:13)
 */
public abstract class AbstractInfluxDBClient extends AbstractRestClient {

    private static final Logger LOG = Logger.getLogger(AbstractInfluxDBClient.class.getName());

    public static final Dialect DEFAULT_DIALECT = new Dialect().header(true)
            .delimiter(",")
            .commentPrefix("#")
            .addAnnotationsItem(Dialect.AnnotationsEnum.DATATYPE)
            .addAnnotationsItem(Dialect.AnnotationsEnum.GROUP).addAnnotationsItem(Dialect.AnnotationsEnum.DEFAULT);

    public final HealthService healthService;
    public final PingService pingService;

    protected final Retrofit retrofit;
    protected final InfluxDBClientOptions options;

    protected final HttpLoggingInterceptor loggingInterceptor;
    protected final GzipInterceptor gzipInterceptor;
    private final AuthenticateInterceptor authenticateInterceptor;
    private final OkHttpClient okHttpClient;
    protected final Collection<AutoCloseable> autoCloseables = new CopyOnWriteArrayList<>();

    public AbstractInfluxDBClient(@Nonnull final InfluxDBClientOptions options, @Nonnull final String clientType) {
        this(options, clientType, Collections.singletonList(RxJava3CallAdapterFactory.createSynchronous()));
    }

    public AbstractInfluxDBClient(@Nonnull final InfluxDBClientOptions options,
                                  @Nonnull final String clientType,
                                  @Nonnull final Collection<CallAdapter.Factory> factories) {

        Arguments.checkNotNull(options, "InfluxDBClientOptions");
        Arguments.checkNotNull(factories, "factories");
        Arguments.checkNonEmpty(clientType, "clientType");

        this.options = options;
        this.loggingInterceptor = new HttpLoggingInterceptor();
        setLogLevel(loggingInterceptor, options.getLogLevel());
        this.authenticateInterceptor = new AuthenticateInterceptor(options);
        this.gzipInterceptor = new GzipInterceptor();

        this.okHttpClient = options.getOkHttpClient()
                // Connection errors are handled by RetryAttempt in AbstractWriteClient.
                .retryOnConnectionFailure(false)
                .addInterceptor(new UserAgentInterceptor(clientType))
                .addInterceptor(this.loggingInterceptor)
                .addInterceptor(this.authenticateInterceptor)
                .addInterceptor(this.gzipInterceptor)
                .build();

        this.authenticateInterceptor.initToken(okHttpClient);

        Retrofit.Builder retrofitBuilder = new Retrofit.Builder()
                .baseUrl(options.getUrl())
                .client(okHttpClient)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(new JSON().getGson()));

        for (CallAdapter.Factory factory : factories) {
            retrofitBuilder.addCallAdapterFactory(factory);
        }

        this.retrofit = retrofitBuilder.build();

        this.healthService = retrofit.create(HealthService.class);
        this.pingService = retrofit.create(PingService.class);
    }

    public void close() {

        autoCloseables.stream().filter(Objects::nonNull).forEach(resource -> {
            try {
                resource.close();
            } catch (Exception e) {
                LOG.log(Level.WARNING, String.format("Exception was thrown while closing: %s", resource), e);
            }
        });
        autoCloseables.clear();

        //
        // signout
        //
        try {
            this.authenticateInterceptor.signout();
        } catch (IOException e) {
            LOG.log(Level.FINEST, "The signout exception", e);
        }

        //
        // Shutdown OkHttp
        //
        okHttpClient.connectionPool().evictAll();
        okHttpClient.dispatcher().executorService().shutdown();
    }

    @Nonnull
    protected HealthCheck health(final Call<HealthCheck> healthCall) {

        Arguments.checkNotNull(healthCall, "health call");

        try {
            return execute(healthCall);
        } catch (InfluxException e) {
            HealthCheck health = new HealthCheck();
            health.setName("influxdb");
            health.setStatus(HealthCheck.StatusEnum.FAIL);
            health.setMessage(e.getMessage());

            return health;
        }
    }
}