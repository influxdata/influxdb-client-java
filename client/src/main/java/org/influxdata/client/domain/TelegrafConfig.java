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

import java.util.ArrayList;
import java.util.List;

import com.squareup.moshi.Json;

/**
 * TelegrafConfig stores telegraf config for one telegraf instance.
 *
 * @author Jakub Bednar (bednar@github) (28/02/2019 08:45)
 */
public final class TelegrafConfig extends AbstractHasLabels {

    private String id;
    private String name;

    @Json(name = "organizationID")
    private String orgID;
    private String description;

    private TelegrafAgent agent;

    private List<TelegrafPlugin> plugins = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getOrgID() {
        return orgID;
    }

    public void setOrgID(final String orgID) {
        this.orgID = orgID;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public TelegrafAgent getAgent() {
        return agent;
    }

    public void setAgent(final TelegrafAgent agent) {
        this.agent = agent;
    }

    public List<TelegrafPlugin> getPlugins() {
        return plugins;
    }

    public void setPlugins(final List<TelegrafPlugin> plugins) {
        this.plugins = plugins;
    }
}