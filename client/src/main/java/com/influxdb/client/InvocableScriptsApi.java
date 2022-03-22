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

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import com.influxdb.Cancellable;
import com.influxdb.client.domain.Script;
import com.influxdb.client.domain.ScriptCreateRequest;
import com.influxdb.client.domain.ScriptUpdateRequest;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;

/**
 * Use API invokable scripts to create custom InfluxDB API endpoints that query, process, and shape data.
 * <p>
 * API invokable scripts let you assign scripts to API endpoints and then execute them as standard REST operations
 * in InfluxDB Cloud.
 *
 * @author Jakub Bednar (bednar@github) (03/21/2022 07:54)
 */
@ThreadSafe
public interface InvocableScriptsApi {

    /**
     * Create a script.
     *
     * @param createRequest The script to create. (required)
     * @return The created script.
     */
    @Nonnull
    Script createScript(@Nonnull final ScriptCreateRequest createRequest);

    /**
     * Update a script.
     *
     * @param scriptId      The ID of the script to update. (required)
     * @param updateRequest Script updates to apply (required)
     * @return The updated script.
     */
    @Nonnull
    Script updateScript(@Nonnull final String scriptId, @Nonnull final ScriptUpdateRequest updateRequest);

    /**
     * List scripts.
     *
     * @return scripts
     */
    @Nonnull
    List<Script> findScripts();

    /**
     * List scripts.
     *
     * @param query to filtering results
     * @return scripts
     */
    @Nonnull
    List<Script> findScripts(@Nonnull final InvocableScriptsQuery query);

    /**
     * Delete a script.
     *
     * @param scriptId The ID of the script to delete. (required)
     */
    void deleteScript(@Nonnull final String scriptId);

    /**
     * Executes the script and synchronously map whole response to {@code List<FluxTable>}.
     * <p>
     * NOTE: This method is not intended for large query results.
     * Use {@link InvocableScriptsApi#invokeScript(String, Map, BiConsumer, Consumer, Runnable)}
     * for large data streaming.
     *
     * @param scriptId The ID of the script to invoke. (required)
     * @param params   bind parameters
     * @return {@code List<FluxTable>}
     */
    @Nonnull
    List<FluxTable> invokeScript(@Nonnull final String scriptId, @Nullable final Map<String, Object> params);

    /**
     * Executes the script and asynchronously stream {@link FluxRecord}s to {@code onNext} consumer.
     *
     * @param scriptId The ID of the script to invoke. (required)
     * @param params   bind parameters
     * @param onNext   the callback to consume the FluxRecord result with capability
     *                 to discontinue a streaming invocation
     */
    void invokeScript(@Nonnull final String scriptId,
                      @Nullable final Map<String, Object> params,
                      @Nonnull final BiConsumer<Cancellable, FluxRecord> onNext);

    /**
     * Executes the script and asynchronously stream {@link FluxRecord}s to {@code onNext} consumer.
     *
     * @param scriptId   The ID of the script to invoke. (required)
     * @param params     bind parameters
     * @param onNext     the callback to consume the FluxRecord result with capability
     *                   to discontinue a streaming invocation
     * @param onError    the callback to consume any error notification
     * @param onComplete the callback to consume a notification about successfully end of stream
     */
    void invokeScript(@Nonnull final String scriptId,
                      @Nullable final Map<String, Object> params,
                      @Nonnull final BiConsumer<Cancellable, FluxRecord> onNext,
                      @Nonnull final Consumer<? super Throwable> onError,
                      @Nonnull final Runnable onComplete);

    /**
     * Executes the script and synchronously map whole response to list of object with given type. <p>
     * <p>
     * NOTE: This method is not intended for large query results.
     * Use {@link InvocableScriptsApi#invokeScript(String, Map, Class, BiConsumer, Consumer, Runnable)}
     * for large data streaming.
     *
     * @param scriptId        The ID of the script to invoke. (required)
     * @param params          bind parameters
     * @param measurementType the type of measurement
     * @param <M>             the type of the measurement (POJO)
     * @return {@code List<T>}
     */
    @Nonnull
    <M> List<M> invokeScript(@Nonnull final String scriptId,
                             @Nullable final Map<String, Object> params,
                             @Nonnull final Class<M> measurementType);

    /**
     * Executes the script and asynchronously stream POJO classes to {@code onNext} consumer.
     *
     * @param scriptId        The ID of the script to invoke. (required)
     * @param params          bind parameters
     * @param measurementType the type of measurement
     * @param onNext          the callback to consume the mapped Measurements with capability to discontinue
     *                        a streaming invocation
     * @param <M>             the type of the measurement (POJO)
     */
    <M> void invokeScript(@Nonnull final String scriptId,
                          @Nullable final Map<String, Object> params,
                          @Nonnull final Class<M> measurementType,
                          @Nonnull final BiConsumer<Cancellable, M> onNext);

    /**
     * Executes the script and asynchronously stream POJO classes to {@code onNext} consumer.
     *
     * @param scriptId        The ID of the script to invoke. (required)
     * @param params          bind parameters
     * @param measurementType the type of measurement
     * @param onNext          the callback to consume the mapped Measurements with capability to discontinue
     *                        a streaming invocation
     * @param onError         the callback to consume any error notification
     * @param onComplete      the callback to consume a notification about successfully end of stream
     * @param <M>             the type of the measurement (POJO)
     */
    <M> void invokeScript(@Nonnull final String scriptId,
                          @Nullable final Map<String, Object> params,
                          @Nonnull final Class<M> measurementType,
                          @Nonnull final BiConsumer<Cancellable, M> onNext,
                          @Nonnull final Consumer<? super Throwable> onError,
                          @Nonnull final Runnable onComplete);

    /**
     * Executes the script and synchronously map whole response to {@link String} result.
     * <p>
     * NOTE: This method is not intended for large query results.
     * Use {@link InvocableScriptsApi#invokeScriptRaw(String, Map, BiConsumer, Consumer, Runnable)}
     * for large data streaming.
     *
     * @param scriptId The ID of the script to invoke. (required)
     * @param params   bind parameters
     * @return the raw response that matched the invocation
     */
    @Nonnull
    String invokeScriptRaw(@Nonnull final String scriptId,
                           @Nullable final Map<String, Object> params);

    /**
     * Executes the script and asynchronously stream response (line by line) to {@code onResponse}.
     *
     * @param scriptId   The ID of the script to invoke. (required)
     * @param params     bind parameters
     * @param onResponse callback to consume the response line by line with capability to discontinue a streaming query
     */
    void invokeScriptRaw(@Nonnull final String scriptId,
                         @Nullable final Map<String, Object> params,
                         @Nonnull final BiConsumer<Cancellable, String> onResponse);

    /**
     * Executes the script and asynchronously stream response (line by line) to {@code onResponse}.
     *
     * @param scriptId   The ID of the script to invoke. (required)
     * @param params     bind parameters
     * @param onResponse callback to consume the response line by line with capability to discontinue a streaming query
     * @param onError    the callback to consume any error notification
     * @param onComplete the callback to consume a notification about successfully end of stream
     */
    void invokeScriptRaw(@Nonnull final String scriptId,
                         @Nullable final Map<String, Object> params,
                         @Nonnull final BiConsumer<Cancellable, String> onResponse,
                         @Nonnull final Consumer<? super Throwable> onError,
                         @Nonnull final Runnable onComplete);
}
