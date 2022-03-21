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
import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import com.influxdb.client.domain.Script;
import com.influxdb.client.domain.ScriptCreateRequest;
import com.influxdb.client.domain.ScriptUpdateRequest;

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
     * @return scripts
     */
    @Nonnull
    List<Script> findScripts();

    /**
     * List scripts.
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
}
