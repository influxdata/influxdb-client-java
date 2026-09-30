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
import java.util.Collections;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import com.influxdb.LogLevel;
import com.influxdb.client.domain.WriteConsistency;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.PointSettings;
import com.influxdb.client.write.WriteParameters;
import com.influxdb.exceptions.InfluxException;
import com.influxdb.utils.Arguments;
import com.influxdb.utils.TlsUtils;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;

/**
 * InfluxDBClientOptions are used to configure theInfluxDB 2.x connections.
 *
 * @author Jakub Bednar (bednar@github) (05/09/2018 10:22)
 */
public final class InfluxDBClientOptions {

    private static final Pattern TAGS_PROPERTY = Pattern.compile("(influx2\\.tags\\.)(.+)");
    private static final Pattern DURATION_PATTERN = Pattern.compile("^(\\d+)([a-zA-Z]{0,2})$");

    private final String url;
    private final String clientType;
    private final OkHttpClient.Builder okHttpClient;
    private final LogLevel logLevel;

    private final AuthScheme authScheme;
    private final char[] token;
    private final String username;
    private final char[] password;

    private final String certificatePath;
    private final String certificateKeyPath;
    private final String certificateP12FilePath;
    private final char[] keyPassword;

    private final String trustFilePath;
    private final char[] trustFilePassword;


    private final String org;
    private final String bucket;
    private final WritePrecision precision;
    private final WriteConsistency consistency;
    private final PointSettings pointSettings;

    private InfluxDBClientOptions(@Nonnull final InfluxDBClientOptions.Builder builder) {

        Arguments.checkNotNull(builder, "InfluxDBClientOptions.Builder");

        this.url = builder.url;
        this.clientType = builder.clientType;
        this.okHttpClient = builder.okHttpClient;
        this.logLevel = builder.logLevel;
        this.authScheme = builder.authScheme;
        this.token = builder.token;
        this.username = builder.username;
        this.password = builder.password;

        this.org = builder.org;
        this.bucket = builder.bucket;
        this.precision = builder.precision != null ? builder.precision : WriteParameters.DEFAULT_WRITE_PRECISION;
        this.consistency = builder.consistency;
        this.pointSettings = builder.pointSettings;

        this.certificatePath = builder.certificatePath;
        this.certificateKeyPath = builder.certificateKeyPath;
        this.certificateP12FilePath = builder.certificateP12FilePath;
        this.keyPassword = builder.keyPassword;
        this.trustFilePath = builder.trustFilePath;
        this.trustFilePassword = builder.trustFilePassword;
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
     * If the {@code clientType} is not specified the `java`, `kotlin` or `scala` is used depends on the type of client.
     *
     * @return the client type to report as a part of User-Agent to remote server
     * @see InfluxDBClientOptions.Builder#clientType(String)
     */
    @Nullable
    public String getClientType() {
        return clientType;
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
     * @return default precision for unix timestamps in the line protocol
     * @see InfluxDBClientOptions.Builder#precision(WritePrecision)
     */
    @Nonnull
    public WritePrecision getPrecision() {
        return precision;
    }


    /**
     * The write consistency for the point.
     * <p>
     * InfluxDB assumes that the write consistency is {@link  WriteConsistency#ONE} if you do not specify consistency.
     * See the <a href="https://bit.ly/enterprise-consistency">InfluxDB Enterprise documentation</a> for
     * detailed descriptions of each consistency option.
     *
     * <b>Available with InfluxDB Enterprise clusters only!</b>
     *
     * @return the write consistency for the point.
     * @see InfluxDBClientOptions.Builder#consistency(WriteConsistency)
     */
    @Nullable
    public WriteConsistency getConsistency() {
        return consistency;
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
     * Retrieves the file path of the certificate used for secure communication.
     *
     * @return the file path of the certificate, or null if no certificate path is specified
     */
    @Nullable
    public String getCertificatePath() {
        return certificatePath;
    }

    /**
     * Retrieves the file path of the certificate key used for secure communication.
     *
     * @return the file path of the certificate key, or null if no certificate key path is specified
     */
    @Nullable
    public String getCertificateKeyPath() {
        return certificateKeyPath;
    }

    /**
     * Retrieves the file path of the PKCS#12 (P12) certificate used for secure communication.
     *
     * @return the file path of the PKCS#12 certificate, or null if no certificate path is specified
     */
    @Nullable
    public String getCertificateP12FilePath() {
        return certificateP12FilePath;
    }

    /**
     * Retrieves the password for the key used in secure communication.
     *
     * @return the key password as a character array, or null if no key password is specified
     */
    @Nullable
    public char[] getKeyPassword() {
        return keyPassword;
    }

    /**
     * Retrieves the file path of the trust store used for secure communication.
     *
     * @return the file path of the trust store, or null if no trust store path is specified
     */
    @Nullable
    public String getTrustFilePath() {
        return trustFilePath;
    }

    /**
     * Retrieves the password for the trust store used in secure communication.
     *
     * @return the trust store password as a character array, or null if no trust store password is specified
     */
    @Nullable
    public char[] getTrustFilePassword() {
        return trustFilePassword;
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
        private String clientType;
        private OkHttpClient.Builder okHttpClient;
        private LogLevel logLevel;

        private AuthScheme authScheme;
        private char[] token;
        private String username;
        private char[] password;

        private String certificatePath;
        private String certificateKeyPath;
        private String certificateP12FilePath;
        private char[] keyPassword;

        private String trustFilePath;
        private char[] trustFilePassword;

        private String org;
        private String bucket;
        private WritePrecision precision;
        private WriteConsistency consistency;

        private final PointSettings pointSettings = new PointSettings();

        /**
         * Set the url to connect to InfluxDB.
         * <p>
         * The url could be a connection string with various configurations. For more info
         * see: {@link #connectionString(String)}.
         * </p>
         *
         * @param url the url to connect to InfluxDB (required). Example: http://localhost:8086?readTimeout=5000
         * @return {@code this}
         */
        @Nonnull
        public InfluxDBClientOptions.Builder url(@Nonnull final String url) {
            Arguments.checkNonEmpty(url, "url");

            connectionString(url);

            return this;
        }

        /**
         * Set the {@code clientType} to customize the User-Agent HTTP header.
         * <p>
         * If the {@code clientType} is set to "awesome-service" the User-Agent header will be:
         * <code>influxdb-client-awesome-service/6.2.0</code>.
         *
         * @param clientType the client type to report as a part of User-Agent to remote server
         * @return {@code this}
         */
        @Nonnull
        public InfluxDBClientOptions.Builder clientType(@Nonnull final String clientType) {
            Arguments.checkNonEmpty(clientType, "clientType");

            this.clientType = clientType;

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
         * <p>
         * The <i>username/password</i> auth is based on
         * <a href="http://bit.ly/http-basic-auth">HTTP "Basic" authentication</a>. The authorization expires when the
         * <a href="http://bit.ly/session-length">time-to-live (TTL)</a> (default 60 minutes) is reached
         * and client produces {@link com.influxdb.exceptions.UnauthorizedException}.
         * </p>
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
         * Specify the default precision for unix timestamps in the line protocol.
         *
         * @param precision default precision for unix timestamps in the line protocol
         * @return {@code this}
         */
        @Nonnull
        public InfluxDBClientOptions.Builder precision(@Nullable final WritePrecision precision) {

            this.precision = precision;

            return this;
        }

        /**
         * Specify the write consistency for the point.
         * <p>
         * InfluxDB assumes that the write consistency is {@link  WriteConsistency#ONE}
         * if you do not specify consistency.
         * See the <a href="https://bit.ly/enterprise-consistency">InfluxDB Enterprise documentation</a> for
         * detailed descriptions of each consistency option.
         *
         * <b>Available with InfluxDB Enterprise clusters only!</b>
         *
         * @param consistency The write consistency for the point.
         * @return {@code this}
         */
        @Nonnull
        public InfluxDBClientOptions.Builder consistency(@Nullable final WriteConsistency consistency) {

            this.consistency = consistency;

            return this;
        }

        /**
         * Sets the file paths for the certificate and its corresponding private key for secure connections.
         *
         * @param certificatePath the file path to the certificate file in PEM format, must not be null.
         * @param certificateKeyPath the file path to the certificate's private key in PEM format, must not be null.
         * @return the updated {@link InfluxDBClientOptions.Builder} instance.
         */
        @Nonnull
        public InfluxDBClientOptions.Builder certificateFilePath(@Nonnull final String certificatePath,
                                                                 @Nonnull final String certificateKeyPath) {
            this.certificatePath = certificatePath;
            this.certificateKeyPath = certificateKeyPath;

            return this;
        }

        /**
         * Sets the file path to the certificate in P12 format and the optional password for the certificate key.
         *
         * @param p12FilePath the file path to the P12 certificate. Must not be null.
         * @param password the optional password for the certificate key. Can be null if no password is required.
         * @return the Builder instance for method chaining.
         */
        @Nonnull
        public InfluxDBClientOptions.Builder certificateP12FilePath(@Nonnull final String p12FilePath,
                                                                    @Nullable final char[] password) {
            this.certificateP12FilePath = p12FilePath;
            this.keyPassword = password;

            return this;
        }

        /**
         * Sets the file path to the trusted certificate for SSL/TLS communication.
         *
         * @param trustFilePath the file path to the trusted certificate; must not be null.
         * @param password the password for the trusted certificate file; can be null if not required.
         * @return the updated {@link InfluxDBClientOptions.Builder} instance.
         */
        @Nonnull
        public InfluxDBClientOptions.Builder trustFilePath(@Nonnull final String trustFilePath,
                                                           @Nullable final char[] password) {
            this.trustFilePath = trustFilePath;
            this.trustFilePassword = password;

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
         * Configure Builder via connection string. The allowed configuration:
         *
         * <ul>
         *     <li><code>org</code> - default destination organization for writes and queries</li>
         *     <li><code>bucket</code> - default destination bucket for writes</li>
         *     <li><code>token</code> - the token to use for the authorization</li>
         *     <li><code>logLevel</code> - rest client verbosity level</li>
         *     <li><code>readTimeout</code> - read timeout</li>
         *     <li><code>writeTimeout</code> - write timeout</li>
         *     <li><code>connectTimeout</code> - socket timeout</li>
         *     <li><code>precision</code> - default precision for unix timestamps in the line protocol</li>
         *     <li><code>consistency</code> - specify the write consistency for the point</li>
         *     <li><code>clientType</code> - to customize the User-Agent HTTP header</li>
         * </ul>
         * <p>
         * Connection string example:
         * <pre>
         * http://localhost:8086?readTimeout=30000&amp;token=my-token&amp;bucket=my-bucket&amp;org=my-org
         * </pre>
         *
         * @return {@code this}
         */
        @Nonnull
        public InfluxDBClientOptions.Builder connectionString(@Nonnull final String connectionString) {

            Arguments.checkNonEmpty(connectionString, "url");

            ParsedUrl parsedUrl = new ParsedUrl(connectionString);
            HttpUrl parse = parsedUrl.httpUrl;

            String org = parse.queryParameter("org");
            String bucket = parse.queryParameter("bucket");
            String token = parse.queryParameter("token");
            String logLevel = parse.queryParameter("logLevel");
            String readTimeout = parse.queryParameter("readTimeout");
            String writeTimeout = parse.queryParameter("writeTimeout");
            String connectTimeout = parse.queryParameter("connectTimeout");
            String precision = parse.queryParameter("precision");
            String consistency = parse.queryParameter("consistency");
            String clientType = parse.queryParameter("clientType");

            String url = parsedUrl.urlWithoutParams;
            return configure(url, org, bucket, token, logLevel, readTimeout, writeTimeout, connectTimeout,
                    precision, consistency, clientType);
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
                String precision = properties.getProperty("influx2.precision");
                String consistency = properties.getProperty("influx2.consistency");
                String clientType = properties.getProperty("influx2.clientType");

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

                return configure(url, org, bucket, token, logLevel, readTimeout, writeTimeout, connectTimeout,
                        precision, consistency, clientType);

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
                okHttpClient = new OkHttpClient.Builder()
                        .protocols(Collections.singletonList(Protocol.HTTP_1_1));
            }

            HttpUrl parsedUrl = HttpUrl.parse(url);
            if (parsedUrl != null && parsedUrl.isHttps()) {
                configureTls(okHttpClient);
            }

            if (logLevel == null) {
                logLevel = LogLevel.NONE;
            }

            return new InfluxDBClientOptions(this);
        }

        /**
         * Configures TLS for an OkHttpClient by setting up SSL context and trust managers based on
         * provided certificate paths or trust file paths.
         *
         * @param okHttpClient an OkHttpClient.Builder instance on which TLS configuration will be applied.
         *                     This is required to establish secure connections with the server.
         * @throws IllegalArgumentException if both {@code certificatePath} and {@code certificateP12FilePath}
         *                                  are set, as only one can be specified at a time.
         * @throws InfluxException if there is an error during the TLS configuration process, such as issues
         *                         with loading the certificate, trust file, or setting up the SSL context.
         */
        private void configureTls(@Nonnull final OkHttpClient.Builder okHttpClient) {

            if (certificatePath != null && certificateP12FilePath != null) {
                throw new IllegalArgumentException("Cannot set both p12FilePath and certificatePath");
            }

            if (certificatePath == null && certificateP12FilePath == null && trustFilePath == null) {
                return;
            }

            try {
                TrustManagerFactory tmf = null;
                if (trustFilePath != null) {
                    tmf = TlsUtils.createTmf(trustFilePath, trustFilePassword);
                }

                KeyManagerFactory kmf = null;
                if (certificatePath != null) {
                    kmf = TlsUtils.createKmf(certificatePath, certificateKeyPath);
                } else if (certificateP12FilePath != null) {
                    kmf = TlsUtils.createKmfP12(certificateP12FilePath, keyPassword);
                }

                SSLContext sslContext = TlsUtils.buildSslContext(kmf, tmf);
                if (sslContext != null) {
                    X509TrustManager trustManager = TlsUtils.getX509TrustManager(tmf);
                    okHttpClient.sslSocketFactory(sslContext.getSocketFactory(), trustManager);
                }
            } catch (Exception e) {
                throw new InfluxException(e);
            }
        }

        @Nonnull
        private InfluxDBClientOptions.Builder configure(@Nonnull final String url,
                                                        @Nullable final String org,
                                                        @Nullable final String bucket,
                                                        @Nullable final String token,
                                                        @Nullable final String logLevel,
                                                        @Nullable final String readTimeout,
                                                        @Nullable final String writeTimeout,
                                                        @Nullable final String connectTimeout,
                                                        @Nullable final String precision,
                                                        @Nullable final String consistency,
                                                        @Nullable final String clientType) {

            this.url = new ParsedUrl(url).urlWithoutParams;
            if (org != null) {
                org(org);
            }
            if (bucket != null) {
                bucket(bucket);
            }

            if (token != null) {
                authenticateToken(token.toCharArray());
            }

            if (logLevel != null) {
                logLevel(Enum.valueOf(LogLevel.class, logLevel));
            }

            if (precision != null) {
                precision(Enum.valueOf(WritePrecision.class, precision));
            }

            if (consistency != null) {
                consistency(Enum.valueOf(WriteConsistency.class, consistency));
            }

            if (okHttpClient == null) {
                okHttpClient = new OkHttpClient.Builder()
                        .protocols(Collections.singletonList(Protocol.HTTP_1_1));
            }
            if (readTimeout != null) {
                okHttpClient.readTimeout(toDuration(readTimeout));
            }

            if (writeTimeout != null) {
                okHttpClient.writeTimeout(toDuration(writeTimeout));
            }
            if (connectTimeout != null) {
                okHttpClient.connectTimeout(toDuration(connectTimeout));
            }
            if (clientType != null) {
                clientType(clientType);
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

            return Duration.of(Long.parseLong(amount), chronoUnit);
        }

        private static final class ParsedUrl {
            @Nonnull
            private final String urlWithoutParams;
            @Nonnull
            private final HttpUrl httpUrl;

            private ParsedUrl(@Nonnull final String connectionString) {

                HttpUrl parse = HttpUrl.parse(connectionString);
                if (parse == null) {
                    throw new InfluxException("Unable to parse connection string " + connectionString);
                }
                this.httpUrl = parse;

                HttpUrl url = this.httpUrl.newBuilder().build();

                //detect IPV6
                String host = url.host().contains(":") ? "[" + url.host() + "]" : url.host();
                String urlWithoutParams = url.scheme() + "://" + host + ":" + url.port() + url.encodedPath();

                this.urlWithoutParams = urlWithoutParams.endsWith("/")
                        ? urlWithoutParams
                        : urlWithoutParams + "/";
            }
        }
    }
}