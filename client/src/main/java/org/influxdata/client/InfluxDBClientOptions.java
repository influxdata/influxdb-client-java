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
import javax.annotation.concurrent.NotThreadSafe;

import org.influxdata.Arguments;

import okhttp3.OkHttpClient;

/**
 * InfluxDBClientOptions are used to configure theInfluxDB 2.0connections.
 *
 * @author Jakub Bednar (bednar@github) (05/09/2018 10:22)
 */
public final class InfluxDBClientOptions {

    private final String url;
    private final OkHttpClient.Builder okHttpClient;

    private AuthScheme authScheme;
    private char[] token;
    private String username;
    private char[] password;

    private String org;
    private String bucket;

    private InfluxDBClientOptions(@Nonnull final InfluxDBClientOptions.Builder builder) {

        Arguments.checkNotNull(builder, "InfluxDBClientOptions.Builder");

        this.url = builder.url;
        this.okHttpClient = builder.okHttpClient;
        this.authScheme = builder.authScheme;
        this.token = builder.token;
        this.username = builder.username;
        this.password = builder.password;

        this.org = builder.org;
        this.bucket = builder.bucket;
    }

    /**
     * The scheme uses to Authentication.
     */
    public enum AuthScheme {

        /**
         * Basic auth.
         */
        SESSION,

        /**
         * Authentication token.
         */
        TOKEN
    }

    /**
     * @return the url to connect to InfluxDB
     * @see InfluxDBClientOptions.Builder#url(String)
     */
    @Nonnull
    public String getUrl() {
        return url;
    }

    /**
     * @return the authorization scheme
     * @see InfluxDBClientOptions.Builder#authenticateToken(char[])
     * @see InfluxDBClientOptions.Builder#authenticate(String, char[]) (char[])
     */
    @Nullable
    public AuthScheme getAuthScheme() {
        return authScheme;
    }

    /**
     * @return the token to use for the authorization
     * @see InfluxDBClientOptions.Builder#authenticateToken(char[])
     */
    @Nullable
    public char[] getToken() {
        return token;
    }

    /**
     * @return the username to use in the basic auth
     * @see InfluxDBClientOptions.Builder#authenticate(String, char[])
     */
    @Nullable
    public String getUsername() {
        return username;
    }

    /**
     * @return the password to use in the basic auth
     * @see InfluxDBClientOptions.Builder#authenticate(String, char[])
     */
    @Nullable
    public char[] getPassword() {
        return password;
    }

    /**
     * @return the default destination organization for writes and queries
     * @see InfluxDBClientOptions.Builder#org(String)
     */
    @Nullable
    public String getOrg() {
        return org;
    }

    /**
     * @return default destination bucket for writes
     * @see InfluxDBClientOptions.Builder#bucket(String)
     */
    @Nullable
    public String getBucket() {
        return bucket;
    }

    /**
     * @return HTTP client to use for communication with InfluxDB
     * @see InfluxDBClientOptions.Builder#okHttpClient(OkHttpClient.Builder)
     */
    @Nonnull
    public OkHttpClient.Builder getOkHttpClient() {
        return okHttpClient;
    }

    /**
     * Creates a builder instance.
     *
     * @return a builder
     */
    @Nonnull
    public static InfluxDBClientOptions.Builder builder() {
        return new InfluxDBClientOptions.Builder();
    }

    /**
     * A builder for {@code InfluxDBClientOptions}.
     */
    @NotThreadSafe
    public static class Builder {

        private String url;
        private OkHttpClient.Builder okHttpClient;

        private AuthScheme authScheme;
        private char[] token;
        private String username;
        private char[] password;

        private String org;
        private String bucket;

        /**
         * Set the url to connect to InfluxDB.
         *
         * @param url the url to connect to InfluxDB. It must be defined.
         * @return {@code this}
         */
        @Nonnull
        public InfluxDBClientOptions.Builder url(@Nonnull final String url) {
            Arguments.checkNonEmpty(url, "url");
            this.url = url;
//            if (!this.url.endsWith("/"))  {
//                this.url += "/";
//            }
            return this;
        }

        /**
         * Set the HTTP client to use for communication with InfluxDB.
         *
         * @param okHttpClient the HTTP client to use.
         * @return {@code this}
         */
        @Nonnull
        public InfluxDBClientOptions.Builder okHttpClient(@Nonnull final OkHttpClient.Builder okHttpClient) {

            Arguments.checkNotNull(okHttpClient, "OkHttpClient.Builder");

            this.okHttpClient = okHttpClient;

            return this;
        }

        /**
         * Setup authorization by {@link AuthScheme#SESSION}.
         *
         * @param username the username to use in the basic auth
         * @param password the password to use in the basic auth
         * @return {@link InfluxDBClientOptions}
         */
        @Nonnull
        public InfluxDBClientOptions.Builder authenticate(@Nonnull final String username,
                                                          @Nonnull final char[] password) {

            Arguments.checkNonEmpty(username, "username");
            Arguments.checkNotNull(password, "password");

            this.authScheme = AuthScheme.SESSION;
            this.username = username;
            this.password = password;

            return this;
        }

        /**
         * Setup authorization by {@link AuthScheme#TOKEN}.
         *
         * @param token the token to use for the authorization
         * @return {@link InfluxDBClientOptions}
         */
        @Nonnull
        public InfluxDBClientOptions.Builder authenticateToken(final char[] token) {

            Arguments.checkNotNull(token, "token");

            this.authScheme = AuthScheme.TOKEN;
            this.token = token;

            return this;
        }

        /**
         * Specify the default destination organization for writes and queries.
         *
         * @param org the default destination organization for writes and queries
         * @return {@link InfluxDBClientOptions}
         */
        @Nonnull
        public InfluxDBClientOptions.Builder org(@Nullable final String org) {

            this.org = org;

            return this;
        }

        /**
         * Specify the default destination bucket for writes.
         *
         * @param bucket default destination bucket for writes
         * @return {@link InfluxDBClientOptions}
         */
        @Nonnull
        public InfluxDBClientOptions.Builder bucket(@Nullable final String bucket) {

            this.bucket = bucket;

            return this;
        }

        /**
         * Build an instance of InfluxDBClientOptions.
         *
         * @return {@link InfluxDBClientOptions}
         */
        @Nonnull
        public InfluxDBClientOptions build() {

            if (url == null) {
                throw new IllegalStateException("The url to connect to InfluxDB has to be defined.");
            }

            if (okHttpClient == null) {
                okHttpClient = new OkHttpClient.Builder();
            }

            return new InfluxDBClientOptions(this);
        }
    }
}