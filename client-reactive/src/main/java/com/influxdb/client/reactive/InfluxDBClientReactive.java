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
package com.influxdb.client.reactive;

import javax.annotation.Nonnull;

import com.influxdb.LogLevel;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteOptions;
import com.influxdb.client.domain.HealthCheck;

import io.reactivex.Single;

/**
 *  The reference RxJava client for the <a href="https://github.com/influxdata/influxdb">InfluxDB 2.0</a>
 *  that allows query and write in a reactive way.
 *
 * @author Jakub Bednar (bednar@github) (20/11/2018 07:08)
 */
public interface InfluxDBClientReactive extends AutoCloseable {

    /**
     * Get the Query client.
     *
     * @return the new client instance for the Query API
     */
    @Nonnull
    QueryReactiveApi getQueryReactiveApi();

    /**
     * Get the Write client.
     *
     * @return the new client instance for the Write API
     */
    @Nonnull
    WriteReactiveApi getWriteReactiveApi();

    /**
     * Get the Write client.
     *
     * @return the new client instance for the Write API
     */
    @Nonnull
    WriteReactiveApi getWriteReactiveApi(@Nonnull final WriteOptions writeOptions);

    /**
     * Get the health of an instance.
     *
     * @return health of an instance
     */
    @Nonnull
    Single<HealthCheck> health();

    /**
     * @return the {@link LogLevel} that is used for logging requests and responses
     */
    @Nonnull
    LogLevel getLogLevel();

    /**
     * Set the log level for the request and response information.
     *
     * @param logLevel the log level to set.
     * @return the InfluxDBClient instance to be able to use it in a fluent manner.
     */
    @Nonnull
    InfluxDBClientReactive setLogLevel(@Nonnull final LogLevel logLevel);

    /**
     * Enable Gzip compress for http requests.
     *
     * Currently only the "Write" and "Query" endpoints supports the Gzip compression.
     *
     * @return the {@link InfluxDBClient} instance to be able to use it in a fluent manner.
     */
    @Nonnull
    InfluxDBClientReactive enableGzip();

    /**
     * Disable Gzip compress for http request body.
     *
     * @return the {@link InfluxDBClient} instance to be able to use it in a fluent manner.
     */
    @Nonnull
    InfluxDBClientReactive disableGzip();

    /**
     * Returns whether Gzip compress for http request body is enabled.
     *
     * @return true if gzip is enabled.
     */
    boolean isGzipEnabled();

    /**
     * Shutdown and close the client.
     */
    @Override
    void close();
}