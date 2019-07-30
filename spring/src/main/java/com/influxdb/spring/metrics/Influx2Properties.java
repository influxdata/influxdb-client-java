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
package com.influxdb.spring.metrics;

import org.springframework.boot.actuate.autoconfigure.metrics.export.properties.StepRegistryProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * {@link ConfigurationProperties} for configuring Influx 2 metrics export.
 *
 * @author Jakub Bednar (bednar@github) (06/05/2019 13:27)
 */
@ConfigurationProperties(prefix = "management.metrics.export.influx2")
public class Influx2Properties extends StepRegistryProperties {

    /**
     * Specifies the destination bucket for writes.
     */
    private String bucket;

    /**
     * Specifies the destination organization for writes.
     */
    private String org;

    /**
     * Authenticate requests with this token.
     */
    private String token;

    /**
     * The URI for the Influx backend. The default is {@code http://localhost:8086/api/v2}.
     */
    private String uri = "http://localhost:8086/api/v2";

    /**
     * Whether to enable GZIP compression of metrics batches published to Influx.
     */
    private boolean compressed = true;

    /**
     * Whether to create the Influx bucket if it does not exist before attempting to
     * publish metrics to it.
     */
    private boolean autoCreateBucket = true;

    /**
     * The duration in seconds for how long data will be kept in the created bucket.
     */
    private Integer everySeconds;

    public String getBucket() {
        return bucket;
    }

    public void setBucket(final String bucket) {
        this.bucket = bucket;
    }

    public String getOrg() {
        return org;
    }

    public void setOrg(final String org) {
        this.org = org;
    }

    public String getToken() {
        return token;
    }

    public void setToken(final String token) {
        this.token = token;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(final String uri) {
        this.uri = uri;
    }

    public boolean isCompressed() {
        return compressed;
    }

    public void setCompressed(final boolean compressed) {
        this.compressed = compressed;
    }

    public boolean isAutoCreateBucket() {
        return autoCreateBucket;
    }

    public void setAutoCreateBucket(final boolean autoCreateBucket) {
        this.autoCreateBucket = autoCreateBucket;
    }

    public Integer getEverySeconds() {
        return everySeconds;
    }

    public void setEverySeconds(final Integer everySeconds) {
        this.everySeconds = everySeconds;
    }
}