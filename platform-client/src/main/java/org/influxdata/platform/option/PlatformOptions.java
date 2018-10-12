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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import org.influxdata.platform.Arguments;

import okhttp3.Cookie;
import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * PlatformOptions are used to configure the InfluxData Platform connections.
 *
 * @author Jakub Bednar (bednar@github) (05/09/2018 10:22)
 */
public final class PlatformOptions {

    private static final List<String> NO_AUTH_ROUTE = Arrays.asList("/api/v2/signin", "/api/v2/signout");

    private final String url;
    private final AuthScheme authScheme;
    private final OkHttpClient.Builder okHttpClient;

    private PlatformOptions(@Nonnull final PlatformOptions.Builder builder) {

        Arguments.checkNotNull(builder, "PlatformOptions.Builder");

        this.url = builder.url;
        this.authScheme = builder.authScheme;
        this.okHttpClient = builder.okHttpClient;
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
    @Nonnull
    public AuthScheme getAuthScheme() {
        return authScheme;
    }

    /**
     * @return HTTP client to use for communication with Platform
     * @see PlatformOptions.Builder#okHttpClient(OkHttpClient.Builder, Boolean)
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

        @Nullable
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
         * @param authenticate if {@link Boolean#TRUE} than the {@link OkHttpClient.Builder} uses
         *                     the {@link AuthenticateInterceptor} for authentication of requests
         * @return {@code this}
         */
        @Nonnull
        public PlatformOptions.Builder okHttpClient(@Nonnull final OkHttpClient.Builder okHttpClient,
                                                    @Nonnull final Boolean authenticate) {

            Arguments.checkNotNull(okHttpClient, "OkHttpClient.Builder");
            Arguments.checkNotNull(authenticate, "authenticate");

            this.okHttpClient = okHttpClient;
            if (authenticate) {
                this.okHttpClient.addInterceptor(new AuthenticateInterceptor());
            }
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
                okHttpClient = new OkHttpClient.Builder().addInterceptor(new AuthenticateInterceptor());
            }

            return new PlatformOptions(this);
        }

        private class AuthenticateInterceptor implements Interceptor {

            private char[] sessionToken;

            @Override
            public Response intercept(@Nonnull final Chain chain) throws IOException {

                Request request = chain.request();
                final String requestPath = request.url().encodedPath();

                // Is no authentication path?
                if (NO_AUTH_ROUTE.stream().anyMatch(requestPath::endsWith)) {
                    return chain.proceed(request);
                }

                if (AuthScheme.TOKEN.equals(authScheme)) {

                    request = request.newBuilder()
                            .header("Authorization", "Token " + string(token))
                            .build();

                } else if (AuthScheme.SESSION.equals(authScheme)) {

                    //TODO expires
                    if (sessionToken == null) {

                        Request authRequest = new Request.Builder()
                                .url(url + "api/v2/signin")
                                .addHeader("Authorization", Credentials.basic(username, string(password)))
                                .post(RequestBody.create(null, ""))
                                .build();

                        Response authResponse = okHttpClient.build().newCall(authRequest).execute();

                        Cookie sessionCookie = Cookie.parseAll(authRequest.url(), authResponse.headers()).stream()
                                .filter(cookie -> "session".equals(cookie.name()))
                                .findFirst()
                                .orElse(null);

                        if (sessionCookie != null) {
                            sessionToken = sessionCookie.value().toCharArray();
                        }

                        if (sessionToken != null) {
                            request = request.newBuilder()
                                    .header("Cookie", "session=" + string(sessionToken))
                                    .build();
                        }
                    }
                }

                return chain.proceed(request);
            }

            @Nonnull
            private String string(final char[] password) {
                return String.valueOf(password);
            }
        }
    }
}