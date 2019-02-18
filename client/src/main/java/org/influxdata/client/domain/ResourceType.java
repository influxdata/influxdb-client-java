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
package org.influxdata.client.domain;

import com.squareup.moshi.Json;

/**
 * @author Jakub Bednar (bednar@github) (02/01/2019 10,19)
 */
public enum ResourceType {

    @Json(name = "authorizations")
    AUTHORIZATIONS,

    @Json(name = "buckets")
    BUCKETS,

    @Json(name = "dashboards")
    DASHBOARDS,

    @Json(name = "orgs")
    ORGS,

    @Json(name = "sources")
    SOURCES,

    @Json(name = "tasks")
    TASKS,

    @Json(name = "telegrafs")
    TELEGRAFS,

    @Json(name = "users")
    USERS,

    @Json(name = "variables")
    VARIABLES,

    @Json(name = "scrapers")
    SCRAPERS,

    @Json(name = "secrets")
    SECRETS,

    @Json(name = "labels")
    LABELS,

    @Json(name = "views")
    VIEWS
}