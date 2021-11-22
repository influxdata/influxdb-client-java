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

import com.influxdb.client.InfluxDBClientOptions;
import com.influxdb.utils.Arguments;

import okhttp3.Call;
import okhttp3.Credentials;
import okhttp3.HttpUrl;
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

    private char[] sessionCookies;
    private final AtomicBoolean signout = new AtomicBoolean(false);

    AuthenticateInterceptor(@Nonnull final InfluxDBClientOptions influxDBClientOptions) {

        Arguments.checkNotNull(influxDBClientOptions, "InfluxDBClientOptions");

        this.influxDBClientOptions = influxDBClientOptions;
    }

    @Override
    @Nonnull
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

            if (sessionCookies != null) {
                request = request.newBuilder()
                        .header("Cookie", string(sessionCookies))
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

        if (sessionCookies == null) {

            String credentials = Credentials
                    .basic(influxDBClientOptions.getUsername(), string(influxDBClientOptions.getPassword()));

            Request authRequest = new Request.Builder()
                    .url(buildPath("api/v2/signin"))
                    .addHeader("Authorization", credentials)
                    .post(RequestBody.create("application/json", null))
                    .build();

            try (Response authResponse = this.okHttpClient.newCall(authRequest).execute()) {
                String cookieHeader = authResponse.headers().get("Set-Cookie");

                if (cookieHeader != null) {
                    sessionCookies = cookieHeader.toCharArray();
                }
            } catch (IOException e) {
                LOG.log(Level.WARNING, "Cannot retrieve the Session token!", e);
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

        Request.Builder authRequest = new Request.Builder()
                .url(buildPath("api/v2/signout"))
                .post(RequestBody.create("application/json", null));

        if (sessionCookies != null) {
            authRequest.addHeader("Cookie", string(sessionCookies));
        }

        signout.set(true);
        sessionCookies = null;

        Response response = okHttpClient.newCall(authRequest.build()).execute();
        response.close();
    }

    @Nonnull
    String buildPath(final String buildPath) {

        Arguments.checkNotNull(buildPath, "buildPath");

        return HttpUrl
                .parse(influxDBClientOptions.getUrl())
                .newBuilder()
                .addEncodedPathSegments(buildPath)
                .build()
                .toString();
    }

    @Nonnull
    private String string(final char[] password) {
        return String.valueOf(password);
    }
}