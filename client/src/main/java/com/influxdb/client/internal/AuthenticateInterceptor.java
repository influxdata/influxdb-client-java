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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

import com.influxdb.Arguments;
import com.influxdb.client.InfluxDBClientOptions;

import okhttp3.Call;
import okhttp3.Cookie;
import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @author Jakub Bednar (bednar@github) (12/10/2018 12:39)
 */
class AuthenticateInterceptor implements Interceptor {

    private static final Logger LOG = Logger.getLogger(InfluxDBClientImpl.class.getName());

    private static final List<String> NO_AUTH_ROUTE = Arrays.asList("/api/v2/signin", "/api/v2/signout",
            "/api/v2/setup");

    private final InfluxDBClientOptions influxDBClientOptions;

    private OkHttpClient okHttpClient;

    private char[] sessionToken;
    private AtomicBoolean signout = new AtomicBoolean(false);

    AuthenticateInterceptor(@Nonnull final InfluxDBClientOptions influxDBClientOptions) {

        Arguments.checkNotNull(influxDBClientOptions, "InfluxDBClientOptions");

        this.influxDBClientOptions = influxDBClientOptions;
    }

    @Override
    public Response intercept(@Nonnull final Chain chain) throws IOException {

        Request request = chain.request();
        final String requestPath = request.url().encodedPath();

        // Is no authentication path?
        if (NO_AUTH_ROUTE.stream().anyMatch(requestPath::endsWith) || signout.get()) {
            return chain.proceed(request);
        }

        if (InfluxDBClientOptions.AuthScheme.TOKEN.equals(influxDBClientOptions.getAuthScheme())) {

            request = request.newBuilder()
                    .header("Authorization", "Token " + string(influxDBClientOptions.getToken()))
                    .build();

        } else if (InfluxDBClientOptions.AuthScheme.SESSION.equals(influxDBClientOptions.getAuthScheme())) {

            initToken(this.okHttpClient);

            if (sessionToken != null) {
                request = request.newBuilder()
                        .header("Cookie", "session=" + string(sessionToken))
                        .build();
            }
        }

        return chain.proceed(request);
    }

    /**
     * Init the Session token if is {@link InfluxDBClientOptions.AuthScheme#SESSION} used.
     * @param okHttpClient the client for signin and signout requests
     */
    void initToken(@Nonnull final OkHttpClient okHttpClient) {

        Arguments.checkNotNull(okHttpClient, "okHttpClient");

        this.okHttpClient = okHttpClient;

        if (!InfluxDBClientOptions.AuthScheme.SESSION.equals(influxDBClientOptions.getAuthScheme()) || signout.get()) {
            return;
        }

        //TODO or expired
        if (sessionToken == null) {

            String credentials = Credentials
                    .basic(influxDBClientOptions.getUsername(), string(influxDBClientOptions.getPassword()));

            Request authRequest = new Request.Builder()
                    .url(influxDBClientOptions.getUrl() + "/api/v2/signin")
                    .addHeader("Authorization", credentials)
                    .post(RequestBody.create(null, ""))
                    .build();

            Response authResponse;
            try {
                authResponse = this.okHttpClient.newCall(authRequest).execute();
            } catch (IOException e) {
                LOG.log(Level.WARNING, "Cannot retrieve the Session token!", e);
                return;
            }

            Cookie sessionCookie = Cookie.parseAll(authRequest.url(), authResponse.headers()).stream()
                    .filter(cookie -> "session".equals(cookie.name()))
                    .findFirst()
                    .orElse(null);

            if (sessionCookie != null) {
                sessionToken = sessionCookie.value().toCharArray();
            }
        }
    }

    /**
     * Expire the current session.
     *
     * @throws IOException if the request could not be executed due to cancellation, a connectivity problem or timeout
     * @see Call#execute()
     */
    void signout() throws IOException {

        if (!InfluxDBClientOptions.AuthScheme.SESSION.equals(influxDBClientOptions.getAuthScheme()) || signout.get()) {
            signout.set(true);
            return;
        }

        this.signout.set(true);
        this.sessionToken = null;

        Request authRequest = new Request.Builder()
                .url(influxDBClientOptions.getUrl() + "/api/v2/signout")
                .post(RequestBody.create(null, ""))
                .build();

        this.okHttpClient.newCall(authRequest).execute();
    }

    @Nonnull
    private String string(final char[] password) {
        return String.valueOf(password);
    }
}