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
package org.influxdata.client;

import java.util.Collection;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.influxdata.client.domain.Organization;
import org.influxdata.client.domain.TelegrafConfig;
import org.influxdata.client.domain.TelegrafPlugin;

/**
 * The client of the InfluxDB 2.0 that implement Telegrafs HTTP API endpoint.
 * <br/>
 * <br/>
 * <p>
 * The following example shows how to create a Telegraf configuration with an output plugin and an input cpu plugin.
 * <pre>
 * TelegrafPlugin output = new TelegrafPlugin();
 * output.setName("influxdb_v2");
 * output.setType(TelegrafPluginType.OUTPUT);
 * output.setComment("Output to Influx 2.0");
 * output.getConfig().put("organization", "my-org");
 * output.getConfig().put("bucket", "my-bucket");
 * output.getConfig().put("urls", new String[]{"http://127.0.0.1:9999"});
 * output.getConfig().put("token", "$INFLUX_TOKEN");
 *
 * TelegrafPlugin cpu = new TelegrafPlugin();
 * cpu.setName("cpu");
 * cpu.setType(TelegrafPluginType.INPUT);
 *
 * TelegrafConfig telegrafConfig = telegrafsApi
 *      .createTelegrafConfig("Telegraf config", "test-config", organization, 1_000, output, cpu);
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (28/02/2019 08:38)
 */
public interface TelegrafsApi {

    /**
     * Create a telegraf config
     *
     * @param telegrafConfig Telegraf Configuration to create
     * @return Telegraf config created
     */
    @Nonnull
    TelegrafConfig createTelegrafConfig(@Nonnull final TelegrafConfig telegrafConfig);

    /**
     * Create a telegraf config
     *
     * @param name               Telegraf Configuration Name
     * @param description        Telegraf Configuration Description
     * @param orgID              The ID of the organization that owns this config
     * @param collectionInterval Default data collection interval for all inputs in milliseconds
     * @param plugins            The telegraf plugins config
     * @return Telegraf config created
     */
    @Nonnull
    TelegrafConfig createTelegrafConfig(@Nonnull final String name,
                                        @Nullable final String description,
                                        @Nonnull final String orgID,
                                        @Nonnull final Integer collectionInterval,
                                        @Nonnull final TelegrafPlugin... plugins);

    /**
     * Create a telegraf config
     *
     * @param name               Telegraf Configuration Name
     * @param description        Telegraf Configuration Description
     * @param org                The organization that owns this config
     * @param collectionInterval Default data collection interval for all inputs in milliseconds
     * @param plugins            The telegraf plugins config
     * @return Telegraf config created
     */
    @Nonnull
    TelegrafConfig createTelegrafConfig(@Nonnull final String name,
                                        @Nullable final String description,
                                        @Nonnull final Organization org,
                                        @Nonnull final Integer collectionInterval,
                                        @Nonnull final TelegrafPlugin... plugins);

    /**
     * Create a telegraf config
     *
     * @param name               Telegraf Configuration Name
     * @param description        Telegraf Configuration Description
     * @param orgID              The ID of the organization that owns this config
     * @param collectionInterval Default data collection interval for all inputs in milliseconds
     * @param plugins            The telegraf plugins config
     * @return Telegraf config created
     */
    @Nonnull
    TelegrafConfig createTelegrafConfig(@Nonnull final String name,
                                        @Nullable final String description,
                                        @Nonnull final String orgID,
                                        @Nonnull final Integer collectionInterval,
                                        @Nonnull final Collection<TelegrafPlugin> plugins);

    /**
     * Create a telegraf config
     *
     * @param name               Telegraf Configuration Name
     * @param description        Telegraf Configuration Description
     * @param org                The organization that owns this config
     * @param collectionInterval Default data collection interval for all inputs in milliseconds
     * @param plugins            The telegraf plugins config
     * @return Telegraf config created
     */
    @Nonnull
    TelegrafConfig createTelegrafConfig(@Nonnull final String name,
                                        @Nullable final String description,
                                        @Nonnull final Organization org,
                                        @Nonnull final Integer collectionInterval,
                                        @Nonnull final Collection<TelegrafPlugin> plugins);
}