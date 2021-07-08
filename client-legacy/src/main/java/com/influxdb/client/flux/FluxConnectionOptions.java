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
package com.influxdb.client.flux;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;

import com.influxdb.Arguments;
import com.influxdb.exceptions.InfluxException;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;

/**
 * FluxConnectionOptions are used to configure queries to the Flux.
 *
 * @author Jakub Bednar (bednar@github) (26/06/2018 08:59)
 */
@ThreadSafe
public final class FluxConnectionOptions {

    private final String url;
    private OkHttpClient.Builder okHttpClient;
    private Map<String, String> parameters;

    private FluxConnectionOptions(@Nonnull final Builder builder) {

        Arguments.checkNotNull(builder, "FluxConnectionOptions.Builder");

        url = builder.url;
        okHttpClient = builder.okHttpClient;
        parameters = builder.parameters;
    }

    /**
     * Creates a builder instance.
     *
     * @return a builder
     */
    @Nonnull
    public static FluxConnectionOptions.Builder builder() {
        return new FluxConnectionOptions.Builder();
    }

    public static Builder builder(final String connectionString) {
        Builder builder = new Builder();

        HttpUrl parse = HttpUrl.parse(connectionString);
        if (parse == null) {
            throw new InfluxException("Unable to parse connection string " + connectionString);
        }

        HttpUrl url = parse.newBuilder().build();

        String urlWithoutParams = url.scheme() + "://" + url.host() + ":" + url.port() + url.encodedPath();
        if (!urlWithoutParams.endsWith("/")) {
            urlWithoutParams += "/";
        }
        builder.url(urlWithoutParams);

        Set<String> parameters = url.queryParameterNames();
        parameters.forEach(paramName -> builder.withParam(paramName, url.queryParameter(paramName)));

        return builder;
    }

    /**
     * @return the url to connect to Flux
     * @see FluxConnectionOptions.Builder#url(String)
     */
    @Nonnull
    public String getUrl() {
        return url;
    }

    /**
     * @return HTTP client to use for communication with Flux
     * @see FluxConnectionOptions.Builder#okHttpClient(OkHttpClient.Builder)
     */
    @Nonnull
    public OkHttpClient.Builder getOkHttpClient() {
        return okHttpClient;
    }

    /**
     * @return returns the map with connection string parameters
     */
    public Map<String, String> getParameters() {
        return parameters;
    }

    /**
     * A builder for {@code FluxConnectionOptions}.
     */
    @NotThreadSafe
    public static class Builder {

        private String url;
        private OkHttpClient.Builder okHttpClient = new OkHttpClient.Builder()
                .protocols(Collections.singletonList(Protocol.HTTP_1_1));
        private Map<String, String> parameters = new HashMap<>();

        /**
         * Set the url to connect to Flux.
         *
         * @param url the url to connect to Flux. It must be defined.
         * @return {@code this}
         */
        @Nonnull
        public Builder url(@Nonnull final String url) {
            Arguments.checkNonEmpty(url, "url");
            this.url = url;
            return this;
        }

        public Builder withParam(@Nonnull final String paramName, @Nullable final String value) {
            Arguments.checkNotNull(paramName, "paramName");
            Arguments.checkNotNull(value, "value");

            parameters.put(paramName, value);

            return this;
        }

        /**
         * Set the HTTP client to use for communication with Flux.
         *
         * @param okHttpClient the HTTP client to use.
         * @return {@code this}
         */
        @Nonnull
        public Builder okHttpClient(@Nonnull final OkHttpClient.Builder okHttpClient) {
            Arguments.checkNotNull(okHttpClient, "OkHttpClient.Builder");
            this.okHttpClient = okHttpClient;
            return this;
        }

        /**
         * Build an instance of FluxConnectionOptions.
         *
         * @return {@link FluxConnectionOptions}
         */
        @Nonnull
        public FluxConnectionOptions build() {

            if (url == null) {
                throw new IllegalStateException("The url to connect to Flux has to be defined.");
            }

            //apply parameters from connection string
            parameters.forEach((key, value) -> {
                switch (key) {
                    case "readTimeout":
                        okHttpClient.readTimeout(Long.parseLong(value), TimeUnit.MILLISECONDS);
                        break;

                    case "writeTimeout":
                        okHttpClient.writeTimeout(Long.parseLong(value), TimeUnit.MILLISECONDS);
                        break;

                    case "connectTimeout":
                        okHttpClient.connectTimeout(Long.parseLong(value), TimeUnit.MILLISECONDS);
                        break;
                    case "logLevel":
                        //this parameter is handled after instance in client instance constructor
                        break;
                    default:
                        throw new InfluxException("Invalid connection string parameter: " + key);
                }
            });

            return new FluxConnectionOptions(this);
        }
    }
}
