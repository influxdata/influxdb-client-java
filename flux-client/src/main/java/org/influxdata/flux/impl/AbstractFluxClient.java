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
package org.influxdata.flux.impl;

import java.io.IOException;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.influxdata.flux.mapper.FluxResultMapper;
import org.influxdata.flux.option.FluxConnectionOptions;
import org.influxdata.platform.Arguments;
import org.influxdata.platform.error.InfluxException;
import org.influxdata.platform.rest.AbstractRestClient;
import org.influxdata.platform.rest.Cancellable;

import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import okio.BufferedSource;
import org.json.JSONArray;
import org.json.JSONObject;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * @author Jakub Bednar (bednar@github) (05/10/2018 09:57)
 */
class AbstractFluxClient<T> extends AbstractRestClient {

    static final JSONObject DEFAULT_DIALECT = new JSONObject()
            .put("header", true)
            .put("delimiter", ",")
            .put("quoteChar", "\"")
            .put("commentPrefix", "#")
            .put("annotations", new JSONArray().put("datatype").put("group").put("default"));

    static final Consumer<Throwable> ERROR_CONSUMER = throwable -> {
        if (throwable instanceof InfluxException) {
            throw (InfluxException) throwable;
        } else {
            throw new InfluxException(throwable);
        }
    };

    protected final FluxResultMapper resultMapper = new FluxResultMapper();

    final T fluxService;
    final FluxCsvParser fluxCsvParser;

    final HttpLoggingInterceptor loggingInterceptor;
    private OkHttpClient okHttpClient;

    AbstractFluxClient(@Nonnull final FluxConnectionOptions options,
                       @Nonnull final Class<T> serviceType) {

        Arguments.checkNotNull(options, "options");
        Arguments.checkNotNull(serviceType, "Flux service type");

        this.fluxCsvParser = new FluxCsvParser();

        this.loggingInterceptor = new HttpLoggingInterceptor();
        this.loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE);

        okHttpClient = options.getOkHttpClient()
                .addInterceptor(loggingInterceptor)
                .build();

        Retrofit.Builder serviceBuilder = new Retrofit.Builder()
                .baseUrl(options.getUrl())
                .client(okHttpClient);

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
    String getVersion(@Nonnull final Response<ResponseBody> response) {

        Arguments.checkNotNull(response, "Response");

        String version = response.headers().get("X-Influxdb-Version");
        if (version != null) {
            return version;
        }

        return "unknown";
    }

    @Nonnull
    RequestBody createBody(@Nullable final String dialect, @Nonnull final String query) {

        Arguments.checkNonEmpty(query, "Flux query");
        JSONObject json = new JSONObject()
                .put("query", query);

        if (dialect != null) {
            json.put("dialect", new JSONObject(dialect));
        }

        return createBody(json.toString());
    }

    void parseFluxResponseToLines(@Nonnull final Consumer<String> onResponse,
                                  @Nonnull final Cancellable cancellable,
                                  @Nonnull final BufferedSource bufferedSource) throws IOException {

        String line = bufferedSource.readUtf8Line();

        while (line != null && !cancellable.isCancelled()) {
            onResponse.accept(line);
            line = bufferedSource.readUtf8Line();
        }
    }

    /**
     * Closes the client, initiates shutdown, no new running calls are accepted during shutdown.
     */
    public void close() {
        okHttpClient.connectionPool().evictAll();
        okHttpClient.dispatcher().executorService().shutdown();
    }
}