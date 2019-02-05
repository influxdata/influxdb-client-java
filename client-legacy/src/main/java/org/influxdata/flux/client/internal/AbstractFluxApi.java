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
package org.influxdata.flux.client.internal;

import java.util.Map;
import javax.annotation.Nonnull;

import org.influxdata.client.Arguments;
import org.influxdata.client.internal.AbstractQueryApi;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * TODO delete.
 * @author Jakub Bednar (bednar@github) (05/10/2018 09:57)
 */
public class AbstractFluxApi<T> extends AbstractQueryApi {

    public final T fluxService;

    public final HttpLoggingInterceptor loggingInterceptor;
    private final OkHttpClient okHttpClient;

    public AbstractFluxApi(@Nonnull final OkHttpClient.Builder okHttpClientBuilder,
                           @Nonnull final String url,
                           @Nonnull final Map<String, String> parameters,
                           @Nonnull final Class<T> serviceType) {

        Arguments.checkNotNull(okHttpClientBuilder, "OkHttpClient.Builder");
        Arguments.checkNonEmpty(url, "Service url");
        Arguments.checkNotNull(parameters, "parameters");
        Arguments.checkNotNull(serviceType, "Flux service type");

        this.loggingInterceptor = new HttpLoggingInterceptor();

        String logLevelParam = parameters.get("logLevel");

        if (logLevelParam == null) {
            this.loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE);
        } else {
            this.loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.valueOf(logLevelParam));
        }

        this.okHttpClient = okHttpClientBuilder
                .addInterceptor(this.loggingInterceptor)
                .build();

        Retrofit.Builder serviceBuilder = new Retrofit.Builder()
                .baseUrl(url)
                .client(this.okHttpClient);

        configure(serviceBuilder);

        this.fluxService = serviceBuilder
                .build()
                .create(serviceType);
    }

    /**
     * Configure Retrofit Service Builder.
     *
     * @param serviceBuilder builder
     */
    protected void configure(@Nonnull final Retrofit.Builder serviceBuilder) {
    }

    @Nonnull
    public String getVersion(@Nonnull final Response<ResponseBody> response) {

        Arguments.checkNotNull(response, "Response");

        String version = response.headers().get("X-Influxdb-Version");
        if (version != null) {
            return version;
        }

        return "unknown";
    }

    /**
     * Closes the client, initiates shutdown, no new running calls are accepted during shutdown.
     */
    public void close() {
        okHttpClient.connectionPool().evictAll();
        okHttpClient.dispatcher().executorService().shutdown();
    }
}