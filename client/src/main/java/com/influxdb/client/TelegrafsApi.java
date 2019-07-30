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
import javax.annotation.Nullable;

import com.influxdb.client.domain.Label;
import com.influxdb.client.domain.LabelResponse;
import com.influxdb.client.domain.Organization;
import com.influxdb.client.domain.ResourceMember;
import com.influxdb.client.domain.ResourceOwner;
import com.influxdb.client.domain.Telegraf;
import com.influxdb.client.domain.TelegrafRequest;
import com.influxdb.client.domain.TelegrafRequestPlugin;
import com.influxdb.client.domain.User;

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
 * Telegraf telegrafConfig = telegrafsApi
 *      .createTelegraf("Telegraf config", "test-config", organization, 1_000, output, cpu);
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (28/02/2019 08:38)
 */
public interface TelegrafsApi {

    /**
     * Create a telegraf config.
     *
     * @param telegrafRequest Telegraf Configuration to create
     * @return Telegraf config created
     */
    @Nonnull
    Telegraf createTelegraf(@Nonnull final TelegrafRequest telegrafRequest);

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
    Telegraf createTelegraf(@Nonnull final String name,
                            @Nullable final String description,
                            @Nonnull final String orgID,
                            @Nonnull final Integer collectionInterval,
                            @Nonnull final TelegrafRequestPlugin... plugins);

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
    Telegraf createTelegraf(@Nonnull final String name,
                            @Nullable final String description,
                            @Nonnull final Organization org,
                            @Nonnull final Integer collectionInterval,
                            @Nonnull final TelegrafRequestPlugin... plugins);

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
    Telegraf createTelegraf(@Nonnull final String name,
                            @Nullable final String description,
                            @Nonnull final String orgID,
                            @Nonnull final Integer collectionInterval,
                            @Nonnull final List<TelegrafRequestPlugin> plugins);

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
    Telegraf createTelegraf(@Nonnull final String name,
                            @Nullable final String description,
                            @Nonnull final Organization org,
                            @Nonnull final Integer collectionInterval,
                            @Nonnull final List<TelegrafRequestPlugin> plugins);

    /**
     * Update a telegraf config.
     *
     * @param telegraf telegraf config update to apply
     * @return An updated telegraf
     */
    @Nonnull
    Telegraf updateTelegraf(@Nonnull final Telegraf telegraf);

    /**
     * Update a telegraf config.
     *
     * @param telegrafID      ID of telegraf config
     * @param telegrafRequest telegraf config update to apply
     * @return An updated telegraf
     */
    @Nonnull
    Telegraf updateTelegraf(@Nonnull final String telegrafID,
                            @Nonnull final TelegrafRequest telegrafRequest);

    /**
     * Delete a telegraf config.
     *
     * @param telegraf telegraf config to delete
     */
    void deleteTelegraf(@Nonnull final Telegraf telegraf);

    /**
     * Delete a telegraf config.
     *
     * @param telegrafID ID of telegraf config to delete
     */
    void deleteTelegraf(@Nonnull final String telegrafID);

    /**
     * Clone a telegraf config.
     *
     * @param clonedName       name of cloned telegraf config
     * @param telegrafConfigID ID of telegraf config to clone
     * @return cloned telegraf config
     */
    @Nonnull
    Telegraf cloneTelegraf(@Nonnull final String clonedName, @Nonnull final String telegrafConfigID);

    /**
     * Clone a telegraf config.
     *
     * @param clonedName name of cloned telegraf config
     * @param telegraf   telegraf config to clone
     * @return cloned telegraf config
     */
    @Nonnull
    Telegraf cloneTelegraf(@Nonnull final String clonedName, @Nonnull final Telegraf telegraf);

    /**
     * Retrieve a telegraf config.
     *
     * @param telegrafID ID of telegraf config to get
     * @return telegraf config details
     */
    @Nonnull
    Telegraf findTelegrafByID(@Nonnull final String telegrafID);

    /**
     * Returns a list of telegraf configs.
     *
     * @return A list of telegraf configs
     */
    @Nonnull
    List<Telegraf> findTelegrafs();

    /**
     * Returns a list of telegraf configs for specified {@code organization}.
     *
     * @param organization specifies the organization of the telegraf configs
     * @return A list of telegraf configs
     */
    @Nonnull
    List<Telegraf> findTelegrafsByOrg(@Nonnull final Organization organization);

    /**
     * Returns a list of telegraf configs for specified {@code orgID}.
     *
     * @param orgID specifies the organization of the telegraf configs
     * @return A list of telegraf configs
     */
    @Nonnull
    List<Telegraf> findTelegrafsByOrgId(@Nullable final String orgID);

    /**
     * Retrieve a telegraf config in TOML.
     *
     * @param telegraf telegraf config to get
     * @return telegraf config details in TOML format
     */
    @Nonnull
    String getTOML(@Nonnull final Telegraf telegraf);

    /**
     * Retrieve a telegraf config in TOML.
     *
     * @param telegrafID ID of telegraf config to get
     * @return telegraf config details in TOML format
     */
    @Nonnull
    String getTOML(@Nonnull final String telegrafID);

    /**
     * List all users with member privileges for a telegraf config.
     *
     * @param telegraf the telegraf config
     * @return a list of telegraf config members
     */
    @Nonnull
    List<ResourceMember> getMembers(@Nonnull final Telegraf telegraf);

    /**
     * List all users with member privileges for a telegraf config.
     *
     * @param telegrafID ID of the telegraf config
     * @return a list of telegraf config members
     */
    @Nonnull
    List<ResourceMember> getMembers(@Nonnull final String telegrafID);

    /**
     * Add telegraf config member.
     *
     * @param member   user to add as member
     * @param telegraf the telegraf config
     * @return member added to telegraf
     */
    @Nonnull
    ResourceMember addMember(@Nonnull final User member, @Nonnull final Telegraf telegraf);

    /**
     * Add telegraf config member.
     *
     * @param memberID   user ID to add as member
     * @param telegrafID ID of the telegraf config
     * @return member added to telegraf
     */
    @Nonnull
    ResourceMember addMember(@Nonnull final String memberID, @Nonnull final String telegrafID);

    /**
     * Removes a member from a telegraf config.
     *
     * @param member   member to remove
     * @param telegraf the telegraf
     */
    void deleteMember(@Nonnull final User member, @Nonnull final Telegraf telegraf);

    /**
     * Removes a member from a telegraf config.
     *
     * @param telegrafID ID of the telegraf
     * @param memberID   ID of member to remove
     */
    void deleteMember(@Nonnull final String memberID, @Nonnull final String telegrafID);

    /**
     * List all owners of a telegraf config.
     *
     * @param telegraf the telegraf config
     * @return a list of telegraf config owners
     */
    @Nonnull
    List<ResourceOwner> getOwners(@Nonnull final Telegraf telegraf);

    /**
     * List all owners of a telegraf config.
     *
     * @param telegrafID ID of the telegraf config
     * @return a list of telegraf config owners
     */
    @Nonnull
    List<ResourceOwner> getOwners(@Nonnull final String telegrafID);

    /**
     * Add telegraf config owner.
     *
     * @param owner    user to add as owner
     * @param telegraf the telegraf config
     * @return telegraf config owner added
     */
    @Nonnull
    ResourceOwner addOwner(@Nonnull final User owner, @Nonnull final Telegraf telegraf);

    /**
     * Add telegraf config owner.
     *
     * @param telegrafID ID of the telegraf config
     * @param ownerID    ID of user to add as owner
     * @return telegraf config owner added
     */
    @Nonnull
    ResourceOwner addOwner(@Nonnull final String ownerID, @Nonnull final String telegrafID);

    /**
     * Removes an owner from a telegraf config.
     *
     * @param owner    owner to remove
     * @param telegraf the telegraf config
     */
    void deleteOwner(@Nonnull final User owner, @Nonnull final Telegraf telegraf);

    /**
     * Removes an owner from a telegraf config.
     *
     * @param telegrafID ID of the telegraf config
     * @param ownerID    ID of owner to remove
     */
    void deleteOwner(@Nonnull final String ownerID, @Nonnull final String telegrafID);

    /**
     * List all labels for a telegraf config.
     *
     * @param telegraf the telegraf config
     * @return a list of all labels for a telegraf config
     */
    @Nonnull
    List<Label> getLabels(@Nonnull final Telegraf telegraf);

    /**
     * List all labels for a telegraf config.
     *
     * @param telegrafID ID of the telegraf config
     * @return a list of all labels for a telegraf config
     */
    @Nonnull
    List<Label> getLabels(@Nonnull final String telegrafID);

    /**
     * Add a label to a telegraf config.
     *
     * @param label    label to add
     * @param telegraf the telegraf config
     * @return added label
     */
    @Nonnull
    LabelResponse addLabel(@Nonnull final Label label, @Nonnull final Telegraf telegraf);

    /**
     * Add a label to a telegraf config.
     *
     * @param telegrafID ID of the telegraf config
     * @param labelID    ID of label to add
     * @return added label
     */
    @Nonnull
    LabelResponse addLabel(@Nonnull final String labelID, @Nonnull final String telegrafID);

    /**
     * Delete a label from a telegraf config.
     *
     * @param label    label to delete
     * @param telegraf the telegraf config
     */
    void deleteLabel(@Nonnull final Label label, @Nonnull final Telegraf telegraf);

    /**
     * Delete a label from a telegraf config.
     *
     * @param telegrafID ID of the telegraf config
     * @param labelID    ID of label to delete
     */
    void deleteLabel(@Nonnull final String labelID, @Nonnull final String telegrafID);
}