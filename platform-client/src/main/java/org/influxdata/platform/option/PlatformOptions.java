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
package org.influxdata.platform.option;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import org.influxdata.platform.Arguments;

import okhttp3.OkHttpClient;

/**
 * PlatformOptions are used to configure the InfluxData Platform connections.
 *
 * @author Jakub Bednar (bednar@github) (05/09/2018 10:22)
 */
public final class PlatformOptions {

    private final String url;
    private final OkHttpClient.Builder okHttpClient;

    private AuthScheme authScheme;
    private char[] token;
    private String username;
    private char[] password;

    private PlatformOptions(@Nonnull final PlatformOptions.Builder builder) {

        Arguments.checkNotNull(builder, "PlatformOptions.Builder");

        this.url = builder.url;
        this.okHttpClient = builder.okHttpClient;
        this.authScheme = builder.authScheme;
        this.token = builder.token;
        this.username = builder.username;
        this.password = builder.password;
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
     * @return the url to connect to Platform
     * @see PlatformOptions.Builder#url(String)
     */
    @Nonnull
    public String getUrl() {
        return url;
    }

    /**
     * @return the authorization scheme
     * @see PlatformOptions.Builder#authenticateToken(char[])
     * @see PlatformOptions.Builder#authenticate(String, char[]) (char[])
     */
    @Nullable
    public AuthScheme getAuthScheme() {
        return authScheme;
    }

    /**
     * @return the token to use for the authorization
     * @see PlatformOptions.Builder#authenticateToken(char[])
     */
    @Nullable
    public char[] getToken() {
        return token;
    }

    /**
     * @return the username to use in the basic auth
     * @see PlatformOptions.Builder#authenticate(String, char[])
     */
    @Nullable
    public String getUsername() {
        return username;
    }

    /**
     * @return the password to use in the basic auth
     * @see PlatformOptions.Builder#authenticate(String, char[])
     */
    @Nullable
    public char[] getPassword() {
        return password;
    }

    /**
     * @return HTTP client to use for communication with Platform
     * @see PlatformOptions.Builder#okHttpClient(OkHttpClient.Builder)
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
    public static PlatformOptions.Builder builder() {
        return new PlatformOptions.Builder();
    }

    /**
     * A builder for {@code PlatformOptions}.
     */
    @NotThreadSafe
    public static class Builder {

        private String url;
        private OkHttpClient.Builder okHttpClient;

        private AuthScheme authScheme;
        private char[] token;
        private String username;
        private char[] password;

        /**
         * Set the url to connect to Platform.
         *
         * @param url the url to connect to Platform. It must be defined.
         * @return {@code this}
         */
        @Nonnull
        public PlatformOptions.Builder url(@Nonnull final String url) {
            Arguments.checkNonEmpty(url, "url");
            this.url = url;
            return this;
        }

        /**
         * Set the HTTP client to use for communication with Platform.
         *
         * @param okHttpClient the HTTP client to use.
         * @return {@code this}
         */
        @Nonnull
        public PlatformOptions.Builder okHttpClient(@Nonnull final OkHttpClient.Builder okHttpClient) {

            Arguments.checkNotNull(okHttpClient, "OkHttpClient.Builder");

            this.okHttpClient = okHttpClient;

            return this;
        }

        /**
         * Setup authorization by {@link AuthScheme#SESSION}.
         *
         * @param username the username to use in the basic auth
         * @param password the password to use in the basic auth
         * @return {@link PlatformOptions}
         */
        @Nonnull
        public PlatformOptions.Builder authenticate(@Nonnull final String username,
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
         * @return {@link PlatformOptions}
         */
        @Nonnull
        public PlatformOptions.Builder authenticateToken(final char[] token) {

            Arguments.checkNotNull(token, "token");

            this.authScheme = AuthScheme.TOKEN;
            this.token = token;

            return this;
        }

        /**
         * Build an instance of PlatformOptions.
         *
         * @return {@link PlatformOptions}
         */
        @Nonnull
        public PlatformOptions build() {

            if (url == null) {
                throw new IllegalStateException("The url to connect to Platform has to be defined.");
            }

            if (okHttpClient == null) {
                okHttpClient = new OkHttpClient.Builder();
            }

            return new PlatformOptions(this);
        }
    }
}