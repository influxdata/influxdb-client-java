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

import java.util.List;
import javax.annotation.Nonnull;

import com.influxdb.client.InvocableScriptsApi;
import com.influxdb.client.InvocableScriptsQuery;
import com.influxdb.client.domain.Script;
import com.influxdb.client.domain.ScriptCreateRequest;
import com.influxdb.client.domain.ScriptUpdateRequest;
import com.influxdb.client.domain.Scripts;
import com.influxdb.client.service.InvocableScriptsService;
import com.influxdb.internal.AbstractRestClient;
import com.influxdb.utils.Arguments;

import retrofit2.Call;

/**
 * @author Jakub Bednar (bednar@github) (03/21/2022 07:54)
 */
final class InvocableScriptsApiImpl extends AbstractRestClient implements InvocableScriptsApi {

    private final InvocableScriptsService service;

    InvocableScriptsApiImpl(@Nonnull final InvocableScriptsService service) {

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
}
