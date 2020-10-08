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
package com.influxdb.client;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.influxdb.Arguments;
import com.influxdb.client.domain.OnboardingRequest;
import com.influxdb.client.domain.OnboardingResponse;
import com.influxdb.client.internal.InfluxDBClientImpl;

/**
 * The Factory that create an instance of a InfluxDB 2.0 client.
 *
 * @author Jakub Bednar (bednar@github) (05/09/2018 10:04)
 */
public final class InfluxDBClientFactory {

    private InfluxDBClientFactory() {
    }

    /**
     * Create an instance of the InfluxDB 2.0 client that is configured via {@code influx2.properties}.
     * The {@code influx2.properties} has to be located on classpath.
     *
     * @return client
     */
    @Nonnull
    public static InfluxDBClient create() {

        InfluxDBClientOptions options = InfluxDBClientOptions.builder()
                .loadProperties()
                .build();

        return create(options);
    }

    /**
     * Create an instance of the InfluxDB 2.0 client. The url could be a connection string with various configurations.
     * <p>
     * e.g.: "http://localhost:8086?readTimeout=5000&amp;connectTimeout=5000&amp;logLevel=BASIC
     *
     * @param connectionString connection string with various configurations.
     * @return client
     */
    @Nonnull
    public static InfluxDBClient create(@Nonnull final String connectionString) {

        InfluxDBClientOptions options = InfluxDBClientOptions.builder()
                .connectionString(connectionString)
                .build();

        return create(options);
    }

    /**
     * Create an instance of the InfluxDB 2.0 client.
     *
     * <p>
     * The <i>username/password</i> auth is based on
     * <a href="http://bit.ly/http-basic-auth">HTTP "Basic" authentication</a>. The authorization expires when the
     * <a href="http://bit.ly/session-lengthh">time-to-live (TTL)</a> (default 60 minutes) is reached
     * and client produces {@link com.influxdb.exceptions.UnauthorizedException}.
     * </p>
     *
     * @param url      the url to connect to the InfluxDB
     * @param username the username to use in the basic auth
     * @param password the password to use in the basic auth
     * @return client
     * @see InfluxDBClientOptions.Builder#url(String)
     */
    @Nonnull
    public static InfluxDBClient create(@Nonnull final String url,
                                        @Nonnull final String username,
                                        @Nonnull final char[] password) {

        InfluxDBClientOptions options = InfluxDBClientOptions.builder()
                .url(url)
                .authenticate(username, password)
                .build();

        return create(options);
    }

    /**
     * Create an instance of the InfluxDB 2.0 client.
     *
     * @param url   the url to connect to the InfluxDB
     * @param token the token to use for the authorization
     * @return client
     * @see InfluxDBClientOptions.Builder#url(String)
     */
    @Nonnull
    public static InfluxDBClient create(@Nonnull final String url, @Nonnull final char[] token) {

        return create(url, token, null);
    }

    /**
     * Create an instance of the InfluxDB 2.0 client.
     *
     * @param url   the url to connect to the InfluxDB
     * @param token the token to use for the authorization
     * @param org   the name of an organization
     * @return client
     * @see InfluxDBClientOptions.Builder#url(String)
     */
    @Nonnull
    public static InfluxDBClient create(@Nonnull final String url,
                                        @Nonnull final char[] token,
                                        @Nullable final String org) {

        return create(url, token, org, null);
    }

    /**
     * Create an instance of the InfluxDB 2.0 client.
     *
     * @param url    the url to connect to the InfluxDB
     * @param token  the token to use for the authorization
     * @param org    the name of an organization
     * @param bucket the name of a bucket
     * @return client
     * @see InfluxDBClientOptions.Builder#url(String)
     */
    @Nonnull
    public static InfluxDBClient create(@Nonnull final String url,
                                        @Nonnull final char[] token,
                                        @Nullable final String org,
                                        @Nullable final String bucket) {

        InfluxDBClientOptions options = InfluxDBClientOptions.builder()
                .url(url)
                .authenticateToken(token)
                .org(org)
                .bucket(bucket)
                .build();

        return create(options);
    }

    /**
     * Create a instance of the InfluxDB 2.0 client to connect into InfluxDB 1.8.
     *
     * @param url             the url to connect to the InfluxDB 1.8
     * @param username        authorization username
     * @param password        authorization password
     * @param database        database name
     * @param retentionPolicy retention policy
     * @return client
     */
    @Nonnull
    public static InfluxDBClient createV1(@Nonnull final String url,
                                          @Nullable final String username,
                                          final char[] password,
                                          @Nonnull final String database,
                                          @Nullable final String retentionPolicy) {

        Arguments.checkNonEmpty(database, "database");

        InfluxDBClientOptions options = InfluxDBClientOptions.builder()
                .url(url)
                .org("-")
                .authenticateToken(String.format("%s:%s",
                        username == null ? "" : username,
                        password == null ? "" : String.valueOf(password)).toCharArray())
                .bucket(String.format("%s/%s", database, retentionPolicy == null ? "" : retentionPolicy))
                .build();

        return create(options);
    }

    /**
     * Create an instance of the InfluxDB 2.0 client.
     *
     * @param options the connection configuration
     * @return client
     */
    @Nonnull
    public static InfluxDBClient create(@Nonnull final InfluxDBClientOptions options) {

        Arguments.checkNotNull(options, "InfluxDBClientOptions");

        return new InfluxDBClientImpl(options);
    }

    /**
     * Post onboarding request, to setup initial user, org and bucket.
     *
     * @param url      the url to connect to the InfluxDB
     * @param username the name of an user
     * @param password the password to connect as an user
     * @param org      the name of an organization
     * @param bucket   the name of a bucket
     * @return Created default user, bucket, org.
     */
    @Nonnull
    public static OnboardingResponse onBoarding(@Nonnull final String url,
                                                @Nonnull final String username,
                                                @Nonnull final String password,
                                                @Nonnull final String org,
                                                @Nonnull final String bucket) {

        Arguments.checkNonEmpty(url, "url");
        Arguments.checkNonEmpty(username, "username");
        Arguments.checkNonEmpty(password, "password");
        Arguments.checkNonEmpty(org, "org");
        Arguments.checkNonEmpty(bucket, "bucket");

        OnboardingRequest onboarding = new OnboardingRequest();
        onboarding.setUsername(username);
        onboarding.setPassword(password);
        onboarding.setOrg(org);
        onboarding.setBucket(bucket);

        return onBoarding(url, onboarding);
    }


    /**
     * Post onboarding request, to setup initial user, org and bucket.
     *
     * @param url        the url to connect to the InfluxDB
     * @param onboarding the defaults
     * @return Created default user, bucket, org.
     */
    @Nonnull
    public static OnboardingResponse onBoarding(@Nonnull final String url,
                                                @Nonnull final OnboardingRequest onboarding) {
        Arguments.checkNonEmpty(url, "url");
        Arguments.checkNotNull(onboarding, "onboarding");

        try (InfluxDBClientImpl client = new InfluxDBClientImpl(InfluxDBClientOptions.builder().url(url).build())) {

            return client.onBoarding(onboarding);
        }
    }
}