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
package com.influxdb.client.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.influxdb.Cancellable;
import com.influxdb.client.InvocableScriptsApi;
import com.influxdb.client.InvocableScriptsQuery;
import com.influxdb.client.domain.Script;
import com.influxdb.client.domain.ScriptCreateRequest;
import com.influxdb.client.domain.ScriptInvocationParams;
import com.influxdb.client.domain.ScriptUpdateRequest;
import com.influxdb.client.domain.Scripts;
import com.influxdb.client.service.InvocableScriptsService;
import com.influxdb.internal.AbstractQueryApi;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.influxdb.query.internal.FluxCsvParser;
import com.influxdb.utils.Arguments;

import okhttp3.ResponseBody;
import retrofit2.Call;

/**
 * @author Jakub Bednar (bednar@github) (03/21/2022 07:54)
 */
final class InvocableScriptsApiImpl extends AbstractQueryApi implements InvocableScriptsApi {

    private final InvocableScriptsService service;

    InvocableScriptsApiImpl(@Nonnull final InvocableScriptsService service) {
        super(new FluxCsvParser(FluxCsvParser.ResponseMetadataMode.ONLY_NAMES));

        Arguments.checkNotNull(service, "service");

        this.service = service;
    }

    @Nonnull
    @Override
    public Script createScript(@Nonnull final ScriptCreateRequest createRequest) {

        Arguments.checkNotNull(createRequest, "createRequest");

        Call<Script> call = service.postScripts(createRequest);
        return execute(call);
    }

    @Nonnull
    @Override
    public Script updateScript(@Nonnull final String scriptId, @Nonnull final ScriptUpdateRequest updateRequest) {
        Arguments.checkNonEmpty(scriptId, "scriptId");
        Arguments.checkNotNull(updateRequest, "updateRequest");

        Call<Script> call = service.patchScriptsID(scriptId, updateRequest);
        return execute(call);
    }

    @Nonnull
    @Override
    public List<Script> findScripts() {
        return findScripts(new InvocableScriptsQuery());
    }

    @Nonnull
    @Override
    public List<Script> findScripts(@Nonnull final InvocableScriptsQuery query) {

        Arguments.checkNotNull(query, "query");

        Call<Scripts> call = service.getScripts(query.getLimit(), query.getOffset());

        return execute(call).getScripts();
    }

    @Override
    public void deleteScript(@Nonnull final String scriptId) {
        Arguments.checkNonEmpty(scriptId, "scriptId");

        Call<Void> call = service.deleteScriptsID(scriptId);

        execute(call);
    }

    @Nonnull
    @Override
    public List<FluxTable> invokeScript(@Nonnull final String scriptId,
                                        @Nullable final Map<String, Object> params) {

        Arguments.checkNonEmpty(scriptId, "scriptId");

        FluxCsvParser.FluxResponseConsumerTable consumer = fluxCsvParser.new FluxResponseConsumerTable();

        query(scriptId, params, consumer, ERROR_CONSUMER, EMPTY_ACTION, false);

        return consumer.getTables();
    }

    @Override
    public void invokeScript(@Nonnull final String scriptId,
                             @Nullable final Map<String, Object> params,
                             @Nonnull final BiConsumer<Cancellable, FluxRecord> onNext) {

        invokeScript(scriptId, params, onNext, ERROR_CONSUMER, EMPTY_ACTION);
    }

    @Override
    public void invokeScript(@Nonnull final String scriptId,
                             @Nullable final Map<String, Object> params,
                             @Nonnull final BiConsumer<Cancellable, FluxRecord> onNext,
                             @Nonnull final Consumer<? super Throwable> onError,
                             @Nonnull final Runnable onComplete) {

        Arguments.checkNotNull(onNext, "onNext");

        FluxCsvParser.FluxResponseConsumer consumer = new FluxCsvParser.FluxResponseConsumer() {

            @Override
            public void accept(final int index,
                               @Nonnull final Cancellable cancellable,
                               @Nonnull final FluxTable table) {
            }

            @Override
            public void accept(final int index,
                               @Nonnull final Cancellable cancellable,
                               @Nonnull final FluxRecord record) {
                onNext.accept(cancellable, record);
            }
        };

        query(scriptId, params, consumer, onError, onComplete, true);
    }

    @Nonnull
    @Override
    public <M> List<M> invokeScript(@Nonnull final String scriptId,
                                    @Nullable final Map<String, Object> params,
                                    @Nonnull final Class<M> measurementType) {

        List<M> measurements = new ArrayList<>();

        FluxCsvParser.FluxResponseConsumer consumer = new FluxCsvParser.FluxResponseConsumer() {

            @Override
            public void accept(final int index,
                               @Nonnull final Cancellable cancellable,
                               @Nonnull final FluxTable table) {

            }

            @Override
            public void accept(final int index,
                               @Nonnull final Cancellable cancellable,
                               @Nonnull final FluxRecord record) {

                measurements.add(resultMapper.toPOJO(record, measurementType));
            }
        };

        query(scriptId, params, consumer, ERROR_CONSUMER, EMPTY_ACTION, false);

        return measurements;
    }

    @Override
    public <M> void invokeScript(@Nonnull final String scriptId,
                                 @Nullable final Map<String, Object> params,
                                 @Nonnull final Class<M> measurementType,
                                 @Nonnull final BiConsumer<Cancellable, M> onNext) {

        invokeScript(scriptId, params, measurementType, onNext, ERROR_CONSUMER, EMPTY_ACTION);
    }

    @Override
    public <M> void invokeScript(@Nonnull final String scriptId,
                                 @Nullable final Map<String, Object> params,
                                 @Nonnull final Class<M> measurementType,
                                 @Nonnull final BiConsumer<Cancellable, M> onNext,
                                 @Nonnull final Consumer<? super Throwable> onError,
                                 @Nonnull final Runnable onComplete) {

        Arguments.checkNotNull(onNext, "onNext");

        FluxCsvParser.FluxResponseConsumer consumer = new FluxCsvParser.FluxResponseConsumer() {

            @Override
            public void accept(final int index,
                               @Nonnull final Cancellable cancellable,
                               @Nonnull final FluxTable table) {

            }

            @Override
            public void accept(final int index,
                               @Nonnull final Cancellable cancellable,
                               @Nonnull final FluxRecord record) {

                onNext.accept(cancellable, resultMapper.toPOJO(record, measurementType));

            }
        };

        query(scriptId, params, consumer, onError, onComplete, true);
    }

    @Nonnull
    @Override
    public String invokeScriptRaw(@Nonnull final String scriptId, @Nullable final Map<String, Object> params) {

        List<String> rows = new ArrayList<>();

        BiConsumer<Cancellable, String> consumer = (cancellable, row) -> rows.add(row);

        queryRaw(scriptId, params, consumer, ERROR_CONSUMER, EMPTY_ACTION, false);

        return String.join("\n", rows);
    }

    @Override
    public void invokeScriptRaw(@Nonnull final String scriptId,
                                @Nullable final Map<String, Object> params,
                                @Nonnull final BiConsumer<Cancellable, String> onResponse) {
        invokeScriptRaw(scriptId, params, onResponse, ERROR_CONSUMER, EMPTY_ACTION);
    }

    @Override
    public void invokeScriptRaw(@Nonnull final String scriptId,
                                @Nullable final Map<String, Object> params,
                                @Nonnull final BiConsumer<Cancellable, String> onResponse,
                                @Nonnull final Consumer<? super Throwable> onError,
                                @Nonnull final Runnable onComplete) {
        queryRaw(scriptId, params, onResponse, onError, onComplete, true);
    }

    private void query(@Nonnull final String scriptId,
                       @Nullable final Map<String, Object> params,
                       @Nonnull final FluxCsvParser.FluxResponseConsumer responseConsumer,
                       @Nonnull final Consumer<? super Throwable> onError,
                       @Nonnull final Runnable onComplete,
                       @Nonnull final Boolean asynchronously) {

        Arguments.checkNonEmpty(scriptId, "scriptId");
        Arguments.checkNotNull(responseConsumer, "responseConsumer");
        Arguments.checkNotNull(onError, "onError");
        Arguments.checkNotNull(onComplete, "onComplete");
        Arguments.checkNotNull(asynchronously, "asynchronously");

        Call<ResponseBody> queryCall = service.postScriptsIDInvokeResponseBody(scriptId,
                new ScriptInvocationParams().params(params));


        query(queryCall, responseConsumer, onError, onComplete, asynchronously);
    }

    private void queryRaw(@Nonnull final String scriptId,
                          @Nullable final Map<String, Object> params,
                          @Nonnull final BiConsumer<Cancellable, String> onResponse,
                          @Nonnull final Consumer<? super Throwable> onError,
                          @Nonnull final Runnable onComplete,
                          @Nonnull final Boolean asynchronously) {

        Arguments.checkNonEmpty(scriptId, "scriptId");
        Arguments.checkNotNull(onResponse, "onResponse");
        Arguments.checkNotNull(onError, "onError");
        Arguments.checkNotNull(onComplete, "onComplete");
        Arguments.checkNotNull(asynchronously, "asynchronously");

        Call<ResponseBody> queryCall = service.postScriptsIDInvokeResponseBody(scriptId,
                new ScriptInvocationParams().params(params));

        queryRaw(queryCall, onResponse, onError, onComplete, asynchronously);
    }
}
