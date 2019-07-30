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
package com.influxdb.spring.influx;

import java.time.Duration;

import com.influxdb.LogLevel;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for InfluxDB 2.
 *
 * @author Jakub Bednar (bednar@github) (06/05/2019 12:54)
 */
@ConfigurationProperties(prefix = "spring.influx2")
public class InfluxDB2Properties {

    private static final int DEFAULT_TIMEOUT = 10_000;

    /**
     * URL to connect to InfluxDB.
     */
    private String url;

    /**
     * Username to use in the basic auth.
     */
    private String username;

    /**
     * Password to use in the basic auth.
     */
    private String password;

    /**
     * Token to use for the authorization.
     */
    private String token;

    /**
     * Default destination organization for writes and queries.
     */
    private String org;

    /**
     * Default destination bucket for writes.
     */
    private String bucket;

    /**
     * The log level for logging the HTTP request and HTTP response.
     */
    private LogLevel logLevel = LogLevel.NONE;

    /**
     * Read timeout for {@code OkHttpClient}.
     */
    private Duration readTimeout = Duration.ofMillis(DEFAULT_TIMEOUT);

    /**
     * Write timeout for {@code OkHttpClient}.
     */
    private Duration writeTimeout = Duration.ofMillis(DEFAULT_TIMEOUT);

    /**
     * Connection timeout for {@code OkHttpClient}.
     */
    private Duration connectTimeout = Duration.ofMillis(DEFAULT_TIMEOUT);

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(final String token) {
        this.token = token;
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(final LogLevel logLevel) {
        this.logLevel = logLevel;
    }

    public String getOrg() {
        return org;
    }

    public void setOrg(final String org) {
        this.org = org;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(final String bucket) {
        this.bucket = bucket;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(final Duration readTimeout) {
        this.readTimeout = readTimeout;
    }

    public Duration getWriteTimeout() {
        return writeTimeout;
    }

    public void setWriteTimeout(final Duration writeTimeout) {
        this.writeTimeout = writeTimeout;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(final Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }
}