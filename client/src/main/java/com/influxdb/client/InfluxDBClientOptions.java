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

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import com.influxdb.Arguments;
import com.influxdb.LogLevel;
import com.influxdb.client.write.PointSettings;
import com.influxdb.exceptions.InfluxException;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

/**
 * InfluxDBClientOptions are used to configure theInfluxDB 2.0 connections.
 *
 * @author Jakub Bednar (bednar@github) (05/09/2018 10:22)
 */
public final class InfluxDBClientOptions {

    private static final Pattern TAGS_PROPERTY = Pattern.compile("(influx2\\.tags\\.)(.+)");
    private static final Pattern DURATION_PATTERN = Pattern.compile("^(\\d+)([a-zA-Z]{0,2})$");

    private final String url;
    private final OkHttpClient.Builder okHttpClient;
    private final LogLevel logLevel;

    private AuthScheme authScheme;
    private char[] token;
    private String username;
    private char[] password;

    private String org;
    private String bucket;
    private final PointSettings pointSettings;

    private InfluxDBClientOptions(@Nonnull final InfluxDBClientOptions.Builder builder) {

        Arguments.checkNotNull(builder, "InfluxDBClientOptions.Builder");

        this.url = builder.url;
        this.okHttpClient = builder.okHttpClient;
        this.logLevel = builder.logLevel;
        this.authScheme = builder.authScheme;
        this.token = builder.token;
        this.username = builder.username;
        this.password = builder.password;

        this.org = builder.org;
        this.bucket = builder.bucket;
        this.pointSettings = builder.pointSettings;
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
     * @return HTTP client to use for communication with InfluxDB
     * @see InfluxDBClientOptions.Builder#okHttpClient(OkHttpClient.Builder)
     */
    @Nonnull
    public OkHttpClient.Builder getOkHttpClient() {
        return okHttpClient;
    }

    /**
     * @return The log level for the request and response information.
     * @see InfluxDBClientOptions.Builder#logLevel(LogLevel)
     */
    @Nonnull
    public LogLevel getLogLevel() {
        return logLevel;
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
     * Default tags that will be use for writes by Point and POJO.
     *
     * @return default tags
     * @see InfluxDBClientOptions.Builder#addDefaultTag(String, String)
     */
    @Nonnull
    public PointSettings getPointSettings() {
        return pointSettings;
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
        private LogLevel logLevel;

        private AuthScheme authScheme;
        private char[] token;
        private String username;
        private char[] password;

        private String org;
        private String bucket;

        private PointSettings pointSettings = new PointSettings();

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
         * Set the log level for the request and response information.
         *
         * @param logLevel The log level for the request and response information.
         * @return {@code this}
         */
        @Nonnull
        public InfluxDBClientOptions.Builder logLevel(@Nonnull final LogLevel logLevel) {

            Arguments.checkNotNull(logLevel, "logLevel");

            this.logLevel = logLevel;

            return this;
        }

        /**
         * Setup authorization by {@link AuthScheme#SESSION}.
         *
         * @param username the username to use in the basic auth
         * @param password the password to use in the basic auth
         * @return {@code this}
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
         * @return {@code this}
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
         * @return {@code this}
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
         * @return {@code this}
         */
        @Nonnull
        public InfluxDBClientOptions.Builder bucket(@Nullable final String bucket) {

            this.bucket = bucket;

            return this;
        }

        /**
         * Add default tag that will be use for writes by Point and POJO.
         * <p>
         * The expressions can be:
         * <ul>
         * <li>"California Miner" - static value</li>
         * <li>"${version}" - system property</li>
         * <li>"${env.hostname}" - environment property</li>
         * </ul>
         *
         * @param key        the tag name
         * @param expression the tag value expression
         * @return this
         */
        @Nonnull
        public InfluxDBClientOptions.Builder addDefaultTag(@Nonnull final String key,
                                                           @Nullable final String expression) {

            Arguments.checkNotNull(key, "tagName");

            pointSettings.addDefaultTag(key, expression);

            return this;
        }

        /**
         * Configure Builder via connection string.
         *
         * @return {@code this}
         */
        @Nonnull
        public InfluxDBClientOptions.Builder connectionString(@Nonnull final String connectionString) {

            Arguments.checkNonEmpty(connectionString, "url");

            HttpUrl parse = HttpUrl.parse(connectionString);
            if (parse == null) {
                throw new InfluxException("Unable to parse connection string " + connectionString);
            }

            String url = parse.scheme() + "://" + parse.host() + ":" + parse.port() + parse.encodedPath();

            String org = parse.queryParameter("org");
            String bucket = parse.queryParameter("bucket");
            String token = parse.queryParameter("token");
            String logLevel = parse.queryParameter("logLevel");
            String readTimeout = parse.queryParameter("readTimeout");
            String writeTimeout = parse.queryParameter("writeTimeout");
            String connectTimeout = parse.queryParameter("connectTimeout");

            return configure(url, org, bucket, token, logLevel, readTimeout, writeTimeout, connectTimeout);
        }

        /**
         * Configure Builder via {@code influx2.properties}.
         *
         * @return {@code this}
         */
        @Nonnull
        public InfluxDBClientOptions.Builder loadProperties() {

            try (InputStream inputStream = this.getClass().getResourceAsStream("/influx2.properties")) {

                Properties properties = new Properties();
                properties.load(inputStream);

                String url = properties.getProperty("influx2.url");
                String org = properties.getProperty("influx2.org");
                String bucket = properties.getProperty("influx2.bucket");
                String token = properties.getProperty("influx2.token");
                String logLevel = properties.getProperty("influx2.logLevel");
                String readTimeout = properties.getProperty("influx2.readTimeout");
                String writeTimeout = properties.getProperty("influx2.writeTimeout");
                String connectTimeout = properties.getProperty("influx2.connectTimeout");

                //
                // Default tags
                //
                properties.stringPropertyNames().forEach(key -> {

                    Matcher matcher = TAGS_PROPERTY.matcher(key);

                    if (matcher.matches()) {
                        String tagKey = matcher.group(2);
                        addDefaultTag(tagKey, properties.getProperty(key).trim());

                    }
                });

                return configure(url, org, bucket, token, logLevel, readTimeout, writeTimeout, connectTimeout);

            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
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

            if (logLevel == null) {
                logLevel = LogLevel.NONE;
            }

            return new InfluxDBClientOptions(this);
        }

        @Nonnull
        private InfluxDBClientOptions.Builder configure(@Nonnull final String url,
                                                        @Nullable final String org,
                                                        @Nullable final String bucket,
                                                        @Nullable final String token,
                                                        @Nullable final String logLevel,
                                                        @Nullable final String readTimeout,
                                                        @Nullable final String writeTimeout,
                                                        @Nullable final String connectTimeout) {

            url(url);
            org(org);
            bucket(bucket);

            if (token != null) {
                authenticateToken(token.toCharArray());
            }

            if (logLevel != null) {
                logLevel(Enum.valueOf(LogLevel.class, logLevel));
            }

            okHttpClient = new OkHttpClient.Builder();
            if (readTimeout != null) {
                okHttpClient.readTimeout(toDuration(readTimeout));
            }

            if (writeTimeout != null) {
                okHttpClient.writeTimeout(toDuration(writeTimeout));
            }
            if (connectTimeout != null) {
                okHttpClient.connectTimeout(toDuration(connectTimeout));
            }

            return this;
        }

        @Nonnull
        private Duration toDuration(@Nonnull final String value) {

            Matcher matcher = DURATION_PATTERN.matcher(value);
            if (!matcher.matches()) {
                throw new InfluxException("'" + value + "' is not a valid duration");
            }

            String amount = matcher.group(1);
            String unit = matcher.group(2);

            ChronoUnit chronoUnit;
            switch (unit != null && !unit.isEmpty() ? unit.toLowerCase() : "ms") {
                case "ms":
                    chronoUnit = ChronoUnit.MILLIS;
                    break;
                case "s":
                    chronoUnit = ChronoUnit.SECONDS;
                    break;
                case "m":
                    chronoUnit = ChronoUnit.MINUTES;
                    break;
                default:
                    throw new InfluxException("unknown unit for '" + value + "'");
            }

            return Duration.of(Long.valueOf(amount), chronoUnit);
        }
    }
}