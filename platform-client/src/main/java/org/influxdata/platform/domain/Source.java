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
package org.influxdata.platform.domain;

import com.squareup.moshi.Json;

/**
 * Source is an external Influx with time series data.
 *
 * @author Jakub Bednar (bednar@github) (18/09/2018 08:47)
 */
public final class Source {

    /**
     * The unique ID of the source.
     */
    private String id;

    /**
     * The organization ID that resource belongs to.
     */
    private String organizationID;

    /**
     * Specifies the default source for the application.
     */
    @Json(name = "default")
    private boolean defaultSource;

    /**
     * The user-defined name for the source.
     */
    private String name;

    /**
     * Type specifies which kinds of source (enterprise vs oss vs 2.0).
     */
    private SourceType type;

    /**
     * SourceType is a string for types of sources.
     */
    public enum SourceType {

        @Json(name = "v2")
        V2SourceType,

        @Json(name = "v1")
        V1SourceType,

        @Json(name = "self")
        SelfSourceType
    }

    /**
     * URL are the connections to the source.
     */
    private String url;

    /**
     * InsecureSkipVerify as true means any certificate presented by the source is accepted.
     */
    private boolean insecureSkipVerify;

    /**
     * Telegraf is the db telegraf is written to. By default it is "telegraf".
     */
    private String telegraf;

    /**
     * Token is the 2.0 authorization token associated with a source.
     */
    private String token;

    //
    // V1SourceFields are the fields for connecting to a 1.0 source (oss or enterprise)
    //

    /**
     * The username to connect to the source (V1SourceFields).
     */
    private String username;

    /**
     * Password is in CLEARTEXT (V1SourceFields).
     */
    private String password;

    /**
     * The optional signing secret for Influx JWT authorization (V1SourceFields).
     */
    private String sharedSecret;

    /**
     * The url for the meta node (V1SourceFields).
     */
    private String metaUrl;

    /**
     * The default retention policy used in database queries to this source (V1SourceFields).
     */
    private String defaultRP;

    /**
     * The url for a flux connected to a 1x source (V1SourceFields).
     */
    private String fluxURL;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getOrganizationID() {
        return organizationID;
    }

    public void setOrganizationID(final String organizationID) {
        this.organizationID = organizationID;
    }

    public boolean isDefaultSource() {
        return defaultSource;
    }

    public void setDefaultSource(final boolean defaultSource) {
        this.defaultSource = defaultSource;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public SourceType getType() {
        return type;
    }

    public void setType(final SourceType type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public boolean isInsecureSkipVerify() {
        return insecureSkipVerify;
    }

    public void setInsecureSkipVerify(final boolean insecureSkipVerify) {
        this.insecureSkipVerify = insecureSkipVerify;
    }

    public String getTelegraf() {
        return telegraf;
    }

    public void setTelegraf(final String telegraf) {
        this.telegraf = telegraf;
    }

    public String getToken() {
        return token;
    }

    public void setToken(final String token) {
        this.token = token;
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

    public String getSharedSecret() {
        return sharedSecret;
    }

    public void setSharedSecret(final String sharedSecret) {
        this.sharedSecret = sharedSecret;
    }

    public String getMetaUrl() {
        return metaUrl;
    }

    public void setMetaUrl(final String metaUrl) {
        this.metaUrl = metaUrl;
    }

    public String getDefaultRP() {
        return defaultRP;
    }

    public void setDefaultRP(final String defaultRP) {
        this.defaultRP = defaultRP;
    }

    public String getFluxURL() {
        return fluxURL;
    }

    public void setFluxURL(final String fluxURL) {
        this.fluxURL = fluxURL;
    }
}
