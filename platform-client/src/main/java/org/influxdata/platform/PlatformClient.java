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
package org.influxdata.platform;

import javax.annotation.Nonnull;

import org.influxdata.platform.option.WriteOptions;

import okhttp3.logging.HttpLoggingInterceptor;

/**
 * The client of the InfluxData Platform for Time Series that implements HTTP API defined by
 * <a href="https://github.com/influxdata/platform/blob/master/http/swagger.yml">Influx API Service swagger.yml</a>.
 *
 * @author Jakub Bednar (bednar@github) (11/10/2018 08:56)
 */
public interface PlatformClient extends AutoCloseable {

    /**
     * Get the Write client.
     *
     * @return the new client instance for the Write API
     */
    @Nonnull
    WriteClient createWriteClient();

    /**
     * Get the Write client.
     *
     * @return the new client instance for the Write API
     */
    @Nonnull
    WriteClient createWriteClient(@Nonnull final WriteOptions writeOptions);

    /**
     * Get the {@link org.influxdata.platform.domain.Authorization} client.
     *
     * @return the new client instance for Authorization API
     */
    @Nonnull
    AuthorizationClient createAuthorizationClient();

    /**
     * Get the {@link org.influxdata.platform.domain.Bucket} client.
     *
     * @return the new client instance for Bucket API
     */
    @Nonnull
    BucketClient createBucketClient();

    /**
     * Get the {@link org.influxdata.platform.domain.Organization} client.
     *
     * @return the new client instance for Organization API
     */
    @Nonnull
    OrganizationClient createOrganizationClient();

    /**
     * Get the {@link org.influxdata.platform.domain.Source} client.
     *
     * @return the new client instance for Source API
     */
    @Nonnull
    SourceClient createSourceClient();

    /**
     * Get the {@link org.influxdata.platform.domain.Task} client.
     *
     * @return the new client instance for Task API
     */
    @Nonnull
    TaskClient createTaskClient();

    /**
     * Get the {@link org.influxdata.platform.domain.User} client.
     *
     * @return the new client instance for User API
     */
    @Nonnull
    UserClient createUserClient();

    /**
     * @return the {@link HttpLoggingInterceptor.Level} that is used for logging requests and responses
     */
    @Nonnull
    HttpLoggingInterceptor.Level getLogLevel();

    /**
     * Set the log level for the request and response information.
     *
     * @param logLevel the log level to set.
     * @return the PlatformClient instance to be able to use it in a fluent manner.
     */
    @Nonnull
    PlatformClient setLogLevel(@Nonnull final HttpLoggingInterceptor.Level logLevel);
}