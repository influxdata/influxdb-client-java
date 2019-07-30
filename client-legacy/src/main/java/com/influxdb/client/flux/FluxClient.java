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
package com.influxdb.client.flux;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.influxdb.Cancellable;
import com.influxdb.LogLevel;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;

/**
 * The client that allows perform Flux queries against the InfluxDB /api/v2/query endpoint.
 *
 * @author Jakub Bednar (bednar@github) (01/10/2018 12:17)
 */
public interface FluxClient {

    /**
     * Executes the Flux query against the InfluxDB and synchronously map whole response to {@code List<FluxTable>}.
     * <p>
     * NOTE: This method is not intended for large query results.
     * Use {@link FluxClient#query(String, BiConsumer, Consumer, Runnable)} for large data streaming.
     *
     * @param query the flux query to execute
     * @return {@code List<FluxTable>} which are matched the query
     */
    @Nonnull
    List<FluxTable> query(@Nonnull final String query);

    /**
     * Executes the Flux query against the InfluxDB and synchronously map whole response to list of object with
     * given type.
     * <p>
     * NOTE: This method is not intended for large query results.
     * Use {@link FluxClient#query(String, Class, BiConsumer, Consumer, Runnable)} for large data streaming.
     *
     * @param query the flux query to execute
     * @param measurementType  the type of measurement
     * @return {@code List<FluxTable>} which are matched the query
     */
    @Nonnull
    <M> List<M> query(@Nonnull final String query, @Nonnull final Class<M> measurementType);

    /**
     * Executes the Flux query against the InfluxDB and asynchronously stream {@link FluxRecord}s
     * to {@code onNext} consumer.
     *
     * @param query  the flux query to execute
     * @param onNext the callback to consume the FluxRecord result with capability to discontinue a streaming query
     */
    void query(@Nonnull final String query,
               @Nonnull final BiConsumer<Cancellable, FluxRecord> onNext);


    /**
     * Executes the Flux query against the InfluxDB and asynchronously stream POJO classes
     * to {@code onNext} consumer.
     *
     * @param query  the flux query to execute
     * @param measurementType the measurement type (POJO)
     * @param onNext the callback to consume the FluxRecord result with capability to discontinue a streaming query
     * @param <M> the type of the measurement (POJO)
     */
    <M> void query(@Nonnull final String query,
                   @Nonnull final Class<M> measurementType,
                   @Nonnull final BiConsumer<Cancellable, M> onNext);

    /**
     * Executes the Flux query against the InfluxDB and asynchronously stream {@link FluxRecord}s
     * to {@code onNext} consumer.
     *
     * @param query   the flux query to execute
     * @param onNext  the callback to consume FluxRecord result with capability to discontinue a streaming query
     * @param onError the callback to consume any error notification
     */
    void query(@Nonnull final String query,
               @Nonnull final BiConsumer<Cancellable, FluxRecord> onNext,
               @Nonnull final Consumer<? super Throwable> onError);

    /**
     * Executes the Flux query against the InfluxDB and asynchronously stream POJO classes
     * to {@code onNext} consumer.
     *
     * @param query   the flux query to execute
     * @param measurementType the measurement type (POJO)
     * @param onNext  the callback to consume POJO record with capability to discontinue a streaming query
     * @param onError the callback to consume any error notification
     * @param <M> the type of the measurement (POJO)
     */
    <M> void query(@Nonnull final String query,
                   @Nonnull final Class<M> measurementType,
                   @Nonnull final BiConsumer<Cancellable, M> onNext,
                   @Nonnull final Consumer<? super Throwable> onError);

    /**
     * Executes the Flux query against the InfluxDB and asynchronously stream {@link FluxRecord}s
     * to {@code onNext} consumer.
     *
     * @param query      the flux query to execute
     * @param onNext     the callback to consume FluxRecord result with capability to discontinue a streaming query
     * @param onError    the callback to consume any error notification
     * @param onComplete the callback to consume a notification about successfully end of stream
     */
    void query(@Nonnull final String query,
               @Nonnull final BiConsumer<Cancellable, FluxRecord> onNext,
               @Nonnull final Consumer<? super Throwable> onError,
               @Nonnull final Runnable onComplete);

    /**
     * Executes the Flux query and asynchronously stream result as POJO.
     *
     * @param query the flux query to execute
     * @param measurementType the measurement type (POJO)
     * @param onNext the callback to consume POJO record with capability to discontinue a streaming query
     * @param onError the callback to consume any error notification
     * @param onComplete the callback to consume a notification about successfully end of stream
     * @param <M> the type of the measurement (POJO)
     */
    <M> void query(@Nonnull final String query,
                   @Nonnull final Class<M> measurementType,
                   @Nonnull final BiConsumer<Cancellable, M> onNext,
                   @Nonnull final Consumer<? super Throwable> onError,
                   @Nonnull final Runnable onComplete);


    /**
     * Executes the Flux query against the InfluxDB and synchronously map whole response to {@link String} result.
     * <p>
     * NOTE: This method is not intended for large responses, that do not fit into memory.
     * Use {@link FluxClient#queryRaw(String, BiConsumer, Consumer, Runnable)} for large data streaming.
     *
     * @param query the flux query to execute
     * @return the raw response that matched the query
     */
    @Nonnull
    String queryRaw(@Nonnull final String query);

    /**
     * Executes the Flux query against the InfluxDB and synchronously map whole response to {@link String} result.
     * <p>
     * NOTE: This method is not intended for large responses, that do not fit into memory.
     * Use {@link FluxClient#queryRaw(String, String, BiConsumer, Consumer, Runnable)} for large data streaming.
     *
     * @param query   the flux query to execute
     * @param dialect Dialect is an object defining the options to use when encoding the response.
     *                <a href="http://bit.ly/flux-dialect">See dialect SPEC.</a>.
     * @return the raw response that matched the query
     */
    @Nonnull
    String queryRaw(@Nonnull final String query, @Nullable final String dialect);

    /**
     * Executes the Flux query against the InfluxDB and asynchronously stream response
     * (line by line) to {@code onResponse}.
     *
     * @param query      the flux query to execute
     * @param onResponse callback to consume the response line by line with capability to discontinue a streaming query
     */
    void queryRaw(@Nonnull final String query,
                  @Nonnull final BiConsumer<Cancellable, String> onResponse);

    /**
     * Executes the Flux query against the InfluxDB and asynchronously stream response
     * (line by line) to {@code onResponse}.
     *
     * @param query      the flux query to execute
     * @param dialect    Dialect is an object defining the options to use when encoding the response.
     *                   <a href="http://bit.ly/flux-dialect">See dialect SPEC.</a>.
     * @param onResponse the callback to consume the response line by line
     *                   with capability to discontinue a streaming query
     */
    void queryRaw(@Nonnull final String query,
                  @Nullable final String dialect,
                  @Nonnull final BiConsumer<Cancellable, String> onResponse);

    /**
     * Executes the Flux query against the InfluxDB and asynchronously stream response
     * (line by line) to {@code onResponse}.
     *
     * @param query      the flux query to execute
     * @param onResponse the callback to consume the response line by line
     *                   with capability to discontinue a streaming query
     * @param onError    callback to consume any error notification
     */
    void queryRaw(@Nonnull final String query,
                  @Nonnull final BiConsumer<Cancellable, String> onResponse,
                  @Nonnull final Consumer<? super Throwable> onError);

    /**
     * Executes the Flux query against the InfluxDB and asynchronously stream response
     * (line by line) to {@code onResponse}.
     *
     * @param query      the flux query to execute
     * @param dialect    Dialect is an object defining the options to use when encoding the response.
     *                   <a href="http://bit.ly/flux-dialect">See dialect SPEC.</a>.
     * @param onResponse the callback to consume the response line by line
     *                   with capability to discontinue a streaming query
     * @param onError    callback to consume any error notification
     */
    void queryRaw(@Nonnull final String query,
                  @Nullable final String dialect,
                  @Nonnull final BiConsumer<Cancellable, String> onResponse,
                  @Nonnull final Consumer<? super Throwable> onError);

    /**
     * Executes the Flux query against the InfluxDB and asynchronously stream response
     * (line by line) to {@code onResponse}.
     *
     * @param query      the flux query to execute
     * @param onResponse the callback to consume the response line by line
     *                   with capability to discontinue a streaming query
     * @param onError    callback to consume any error notification
     * @param onComplete callback to consume a notification about successfully end of stream
     */
    void queryRaw(@Nonnull final String query,
                  @Nonnull final BiConsumer<Cancellable, String> onResponse,
                  @Nonnull final Consumer<? super Throwable> onError,
                  @Nonnull final Runnable onComplete);


    /**
     * Executes the Flux query against the InfluxDB and asynchronously stream response
     * (line by line) to {@code onResponse}.
     *
     * @param query      the flux query to execute
     * @param dialect    Dialect is an object defining the options to use when encoding the response.
     *                   <a href="http://bit.ly/flux-dialect">See dialect SPEC.</a>.
     * @param onResponse the callback to consume the response line by line
     *                   with capability to discontinue a streaming query
     *                   The callback call contains the one line of the response.
     * @param onError    callback to consume any error notification
     * @param onComplete callback to consume a notification about successfully end of stream
     */
    void queryRaw(@Nonnull final String query,
                  @Nullable final String dialect,
                  @Nonnull final BiConsumer<Cancellable, String> onResponse,
                  @Nonnull final Consumer<? super Throwable> onError,
                  @Nonnull final Runnable onComplete);

    /**
     * Check the status of InfluxDB Server.
     *
     * @return {@link Boolean#TRUE} if server is healthy otherwise return {@link Boolean#FALSE}
     */
    @Nonnull
    Boolean ping();

    /**
     * Returns the version of the connected InfluxDB Server.
     *
     * @return the version String, otherwise unknown.
     */
    @Nonnull
    String version();

    /**
     * Gets the {@link LogLevel} that is used for logging requests and responses.
     *
     * @return the {@link LogLevel} that is used for logging requests and responses
     */
    @Nonnull
    LogLevel getLogLevel();

    /**
     * Sets the log level for the request and response information.
     *
     * @param logLevel the log level to set.
     * @return the FluxClient instance to be able to use it in a fluent manner.
     */
    @Nonnull
    FluxClient setLogLevel(@Nonnull final LogLevel logLevel);

    /**
     * Shutdown and close the client.
     */
    void close();
}