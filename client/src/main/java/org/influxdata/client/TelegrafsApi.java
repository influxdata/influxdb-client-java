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
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.influxdata.client.domain.Label;
import org.influxdata.client.domain.Organization;
import org.influxdata.client.domain.ResourceMember;
import org.influxdata.client.domain.TelegrafConfig;
import org.influxdata.client.domain.TelegrafPlugin;
import org.influxdata.client.domain.User;

/**
 * The client of the InfluxDB 2.0 that implement Telegrafs HTTP API endpoint.
 * <br>
 * <br>
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
     * Create a telegraf config.
     *
     * @param telegrafConfig Telegraf Configuration to create
     * @return Telegraf config created
     */
    @Nonnull
    TelegrafConfig createTelegrafConfig(@Nonnull final TelegrafConfig telegrafConfig);

    /**
     * Create a telegraf config.
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
     * Create a telegraf config.
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
     * Create a telegraf config.
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
     * Create a telegraf config.
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

    /**
     * Update a telegraf config.
     *
     * @param telegrafConfig telegraf config update to apply
     * @return An updated telegraf
     */
    @Nonnull
    TelegrafConfig updateTelegrafConfig(@Nonnull final TelegrafConfig telegrafConfig);

    /**
     * Delete a telegraf config.
     *
     * @param telegrafConfig telegraf config to delete
     */
    void deleteTelegrafConfig(@Nonnull final TelegrafConfig telegrafConfig);

    /**
     * Delete a telegraf config.
     *
     * @param telegrafConfigID ID of telegraf config to delete
     */
    void deleteTelegrafConfig(@Nonnull final String telegrafConfigID);

    /**
     * Clone a telegraf config.
     *
     * @param clonedName       name of cloned telegraf config
     * @param telegrafConfigID ID of telegraf config to clone
     * @return cloned telegraf config
     */
    @Nonnull
    TelegrafConfig cloneTelegrafConfig(@Nonnull final String clonedName, @Nonnull final String telegrafConfigID);

    /**
     * Clone a telegraf config.
     *
     * @param clonedName     name of cloned telegraf config
     * @param telegrafConfig telegraf config to clone
     * @return cloned telegraf config
     */
    @Nonnull
    TelegrafConfig cloneTelegrafConfig(@Nonnull final String clonedName, @Nonnull final TelegrafConfig telegrafConfig);

    /**
     * Retrieve a telegraf config.
     *
     * @param telegrafConfigID ID of telegraf config to get
     * @return telegraf config details
     */
    @Nullable
    TelegrafConfig findTelegrafConfigByID(@Nonnull final String telegrafConfigID);

    /**
     * Returns a list of telegraf configs.
     *
     * @return A list of telegraf configs
     */
    @Nonnull
    List<TelegrafConfig> findTelegrafConfigs();

    /**
     * Returns a list of telegraf configs for specified {@code organization}.
     *
     * @param organization specifies the organization of the telegraf configs
     * @return A list of telegraf configs
     */
    @Nonnull
    List<TelegrafConfig> findTelegrafConfigsByOrg(@Nonnull final Organization organization);

    /**
     * Returns a list of telegraf configs for specified {@code orgId}.
     *
     * @param orgId specifies the organization of the telegraf configs
     * @return A list of telegraf configs
     */
    @Nonnull
    List<TelegrafConfig> findTelegrafConfigsByOrgId(@Nullable final String orgId);

    /**
     * Retrieve a telegraf config in TOML.
     *
     * @param telegrafConfig telegraf config to get
     * @return telegraf config details in TOML format
     */
    @Nonnull
    String getTOML(@Nonnull final TelegrafConfig telegrafConfig);

    /**
     * Retrieve a telegraf config in TOML.
     *
     * @param telegrafConfigID ID of telegraf config to get
     * @return telegraf config details in TOML format
     */
    @Nonnull
    String getTOML(@Nonnull final String telegrafConfigID);

    /**
     * List all users with member privileges for a telegraf config.
     *
     * @param telegrafConfig the telegraf config
     * @return a list of telegraf config members
     */
    @Nonnull
    List<ResourceMember> getMembers(@Nonnull final TelegrafConfig telegrafConfig);

    /**
     * List all users with member privileges for a telegraf config.
     *
     * @param telegrafConfigID ID of the telegraf config
     * @return a list of telegraf config members
     */
    @Nonnull
    List<ResourceMember> getMembers(@Nonnull final String telegrafConfigID);

    /**
     * Add telegraf config member.
     *
     * @param member         user to add as member
     * @param telegrafConfig the telegraf config
     * @return member added to telegraf
     */
    @Nonnull
    ResourceMember addMember(@Nonnull final User member, @Nonnull final TelegrafConfig telegrafConfig);

    /**
     * Add telegraf config member.
     *
     * @param memberID         user ID to add as member
     * @param telegrafConfigID ID of the telegraf config
     * @return member added to telegraf
     */
    @Nonnull
    ResourceMember addMember(@Nonnull final String memberID, @Nonnull final String telegrafConfigID);

    /**
     * Removes a member from a telegraf config.
     *
     * @param member         member to remove
     * @param telegrafConfig the telegraf
     */
    void deleteMember(@Nonnull final User member, @Nonnull final TelegrafConfig telegrafConfig);

    /**
     * Removes a member from a telegraf config.
     *
     * @param telegrafConfigID ID of the telegraf
     * @param memberID         ID of member to remove
     */
    void deleteMember(@Nonnull final String memberID, @Nonnull final String telegrafConfigID);

    /**
     * List all owners of a telegraf config.
     *
     * @param telegrafConfig the telegraf config
     * @return a list of telegraf config owners
     */
    @Nonnull
    List<ResourceMember> getOwners(@Nonnull final TelegrafConfig telegrafConfig);

    /**
     * List all owners of a telegraf config.
     *
     * @param telegrafConfigID ID of the telegraf config
     * @return a list of telegraf config owners
     */
    @Nonnull
    List<ResourceMember> getOwners(@Nonnull final String telegrafConfigID);

    /**
     * Add telegraf config owner.
     *
     * @param owner          user to add as owner
     * @param telegrafConfig the telegraf config
     * @return telegraf config owner added
     */
    @Nonnull
    ResourceMember addOwner(@Nonnull final User owner, @Nonnull final TelegrafConfig telegrafConfig);

    /**
     * Add telegraf config owner.
     *
     * @param telegrafConfigID ID of the telegraf config
     * @param ownerID          ID of user to add as owner
     * @return telegraf config owner added
     */
    @Nonnull
    ResourceMember addOwner(@Nonnull final String ownerID, @Nonnull final String telegrafConfigID);

    /**
     * Removes an owner from a telegraf config.
     *
     * @param owner          owner to remove
     * @param telegrafConfig the telegraf config
     */
    void deleteOwner(@Nonnull final User owner, @Nonnull final TelegrafConfig telegrafConfig);

    /**
     * Removes an owner from a telegraf config.
     *
     * @param telegrafConfigID ID of the telegraf config
     * @param ownerID          ID of owner to remove
     */
    void deleteOwner(@Nonnull final String ownerID, @Nonnull final String telegrafConfigID);

    /**
     * List all labels for a telegraf config.
     *
     * @param telegrafConfig the telegraf config
     * @return a list of all labels for a telegraf config
     */
    @Nonnull
    List<Label> getLabels(@Nonnull final TelegrafConfig telegrafConfig);

    /**
     * List all labels for a telegraf config.
     *
     * @param telegrafConfigID ID of the telegraf config
     * @return a list of all labels for a telegraf config
     */
    @Nonnull
    List<Label> getLabels(@Nonnull final String telegrafConfigID);

    /**
     * Add a label to a telegraf config.
     *
     * @param label          label to add
     * @param telegrafConfig the telegraf config
     * @return added label
     */
    @Nonnull
    Label addLabel(@Nonnull final Label label, @Nonnull final TelegrafConfig telegrafConfig);

    /**
     * Add a label to a telegraf config.
     *
     * @param telegrafConfigID ID of the telegraf config
     * @param labelID          ID of label to add
     * @return added label
     */
    @Nonnull
    Label addLabel(@Nonnull final String labelID, @Nonnull final String telegrafConfigID);

    /**
     * Delete a label from a telegraf config.
     *
     * @param label          label to delete
     * @param telegrafConfig the telegraf config
     */
    void deleteLabel(@Nonnull final Label label, @Nonnull final TelegrafConfig telegrafConfig);

    /**
     * Delete a label from a telegraf config.
     *
     * @param telegrafConfigID ID of the telegraf config
     * @param labelID  ID of label to delete
     */
    void deleteLabel(@Nonnull final String labelID, @Nonnull final String telegrafConfigID);
}