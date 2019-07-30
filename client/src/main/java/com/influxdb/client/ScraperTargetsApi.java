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
import com.influxdb.client.domain.ScraperTargetRequest;
import com.influxdb.client.domain.ScraperTargetResponse;
import com.influxdb.client.domain.User;

/**
 * Represents an HTTP API handler for scraper targets.
 *
 * @author Jakub Bednar (bednar@github) (22/01/2019 08:08)
 */
public interface ScraperTargetsApi {

    /**
     * Creates a new ScraperTarget and sets {@link ScraperTargetResponse#getId()} with the new identifier.
     *
     * @param scraperTargetRequest the scraper to create
     * @return ScraperTarget created
     */
    @Nonnull
    ScraperTargetResponse createScraperTarget(@Nonnull final ScraperTargetRequest scraperTargetRequest);

    /**
     * Create new ScraperTarget with {@link ScraperTargetRequest#getType()} set to
     * {@link ScraperTargetRequest.TypeEnum#PROMETHEUS}.
     *
     * @param name     the name of the new ScraperTarget
     * @param url      the url of the new ScraperTarget
     * @param bucketID the id of the scraperTarget that its use to writes
     * @param orgID    the id of the organization that owns new ScraperTarget
     * @return ScraperTarget created
     */
    @Nonnull
    ScraperTargetResponse createScraperTarget(@Nonnull final String name,
                                              @Nonnull final String url,
                                              @Nonnull final String bucketID,
                                              @Nonnull final String orgID);

    /**
     * Update a ScraperTarget.
     *
     * @param scraperTargetResponse ScraperTarget update to apply
     * @return ScraperTarget updated
     */
    @Nonnull
    ScraperTargetResponse updateScraperTarget(@Nonnull final ScraperTargetResponse scraperTargetResponse);

    /**
     * Update a ScraperTarget.
     *
     * @param scraperTargetID      id of the scraper target (required)
     * @param scraperTargetRequest ScraperTarget update to apply
     * @return ScraperTarget updated
     */
    @Nonnull
    ScraperTargetResponse updateScraperTarget(@Nonnull final String scraperTargetID,
                                              @Nonnull final ScraperTargetRequest scraperTargetRequest);

    /**
     * Delete a ScraperTarget.
     *
     * @param scraperTargetResponse ScraperTarget to delete
     */
    void deleteScraperTarget(@Nonnull final ScraperTargetResponse scraperTargetResponse);

    /**
     * Delete a ScraperTarget.
     *
     * @param scraperTargetID ID of ScraperTarget to delete
     */
    void deleteScraperTarget(@Nonnull final String scraperTargetID);

    /**
     * Clone a ScraperTarget.
     *
     * @param clonedName      name of cloned ScraperTarget
     * @param scraperTargetID ID of ScraperTarget to clone
     * @return cloned ScraperTarget
     */
    @Nonnull
    ScraperTargetResponse cloneScraperTarget(@Nonnull final String clonedName, @Nonnull final String scraperTargetID);

    /**
     * Clone a ScraperTarget.
     *
     * @param clonedName    name of cloned ScraperTarget
     * @param scraperTargetResponse ScraperTarget to clone
     * @return cloned ScraperTarget
     */
    @Nonnull
    ScraperTargetResponse cloneScraperTarget(@Nonnull final String clonedName,
                                             @Nonnull final ScraperTargetResponse scraperTargetResponse);

    /**
     * Retrieve a ScraperTarget.
     *
     * @param scraperTargetID ID of ScraperTarget to get
     * @return ScraperTarget details
     */
    @Nonnull
    ScraperTargetResponse findScraperTargetByID(@Nonnull final String scraperTargetID);

    /**
     * Get all scraper targets.
     *
     * @return List all ScraperTargets
     */
    @Nonnull
    List<ScraperTargetResponse> findScraperTargets();

    /**
     * Get all scraper targets.
     *
     * @param organization specifies the organization of the resource
     * @return all scraper targets
     */
    @Nonnull
    List<ScraperTargetResponse> findScraperTargetsByOrg(@Nonnull final Organization organization);

    /**
     * Get all scraper targets.
     *
     * @param orgID specifies the organization ID of the resource
     * @return all scraper targets
     */
    @Nonnull
    List<ScraperTargetResponse> findScraperTargetsByOrgId(@Nullable final String orgID);

    /**
     * List all users with member privileges for a ScraperTarget.
     *
     * @param scraperTargetResponse the ScraperTarget with members
     * @return return the list all users with member privileges for a ScraperTarget
     */
    @Nonnull
    List<ResourceMember> getMembers(@Nonnull final ScraperTargetResponse scraperTargetResponse);

    /**
     * List all users with member privileges for a ScraperTarget.
     *
     * @param scraperTargetID ID of ScraperTarget to get members
     * @return return the list all users with member privileges for a ScraperTarget
     */
    @Nonnull
    List<ResourceMember> getMembers(@Nonnull final String scraperTargetID);

    /**
     * Add the scraperTarget member.
     *
     * @param member the member of an ScraperTarget
     * @param scraperTargetResponse the ScraperTarget for the member
     * @return created mapping
     */
    @Nonnull
    ResourceMember addMember(@Nonnull final User member, @Nonnull final ScraperTargetResponse scraperTargetResponse);

    /**
     * Add the ScraperTarget member.
     *
     * @param memberID the ID of a member
     * @param scraperTargetID the ID of a ScraperTarget
     * @return created mapping
     */
    @Nonnull
    ResourceMember addMember(@Nonnull final String memberID, @Nonnull final String scraperTargetID);

    /**
     * Removes a member from a ScraperTarget.
     *
     * @param member the member of a ScraperTarget
     * @param scraperTargetResponse the scraperTarget
     */
    void deleteMember(@Nonnull final User member, @Nonnull final ScraperTargetResponse scraperTargetResponse);

    /**
     * Removes a member from a ScraperTarget.
     *
     * @param scraperTargetID the ID of a ScraperTarget
     * @param memberID the ID of a member
     */
    void deleteMember(@Nonnull final String memberID, @Nonnull final String scraperTargetID);

    /**
     * List all owners of a ScraperTarget.
     *
     * @param scraperTargetResponse the ScraperTarget with owners
     * @return return List all owners of a ScraperTarget.
     */
    @Nonnull
    List<ResourceOwner> getOwners(@Nonnull final ScraperTargetResponse scraperTargetResponse);

    /**
     * List all owners of a ScraperTarget.
     *
     * @param scraperTargetID ID of ScraperTarget to get owners
     * @return return List all owners of a ScraperTarget
     */
    @Nonnull
    List<ResourceOwner> getOwners(@Nonnull final String scraperTargetID);

    /**
     * Add the ScraperTarget owner.
     *
     * @param owner  the owner of a ScraperTarget
     * @param scraperTargetResponse the ScraperTarget
     * @return created mapping
     */
    @Nonnull
    ResourceOwner addOwner(@Nonnull final User owner, @Nonnull final ScraperTargetResponse scraperTargetResponse);

    /**
     * Add the ScraperTarget owner.
     *
     * @param scraperTargetID the ID of a ScraperTarget
     * @param ownerID  the ID of a owner
     * @return created mapping
     */
    @Nonnull
    ResourceOwner addOwner(@Nonnull final String ownerID, @Nonnull final String scraperTargetID);

    /**
     * Removes a owner from a ScraperTarget.
     *
     * @param owner  the owner of a ScraperTarget
     * @param scraperTargetResponse the scraperTarget
     */
    void deleteOwner(@Nonnull final User owner, @Nonnull final ScraperTargetResponse scraperTargetResponse);

    /**
     * Removes a owner from a ScraperTarget.
     *
     * @param scraperTargetID the ID of a ScraperTarget
     * @param ownerID  the ID of a owner
     */
    void deleteOwner(@Nonnull final String ownerID, @Nonnull final String scraperTargetID);


    /**
     * List all labels of a ScraperTarget.
     *
     * @param scraperTargetResponse the ScraperTarget with labels
     * @return return List all labels of a ScraperTarget.
     */
    @Nonnull
    List<Label> getLabels(@Nonnull final ScraperTargetResponse scraperTargetResponse);

    /**
     * List all labels of a ScraperTarget.
     *
     * @param scraperTargetID ID of ScraperTarget to get labels
     * @return return List all labels of a ScraperTarget
     */
    @Nonnull
    List<Label> getLabels(@Nonnull final String scraperTargetID);

    /**
     * Add the ScraperTarget label.
     *
     * @param label         the label of a ScraperTarget
     * @param scraperTargetResponse the ScraperTarget
     * @return added label
     */
    @Nonnull
    LabelResponse addLabel(@Nonnull final Label label, @Nonnull final ScraperTargetResponse scraperTargetResponse);

    /**
     * Add the ScraperTarget label.
     *
     * @param scraperTargetID the ID of a ScraperTarget
     * @param labelID         the ID of a label
     * @return added label
     */
    @Nonnull
    LabelResponse addLabel(@Nonnull final String labelID, @Nonnull final String scraperTargetID);

    /**
     * Removes a label from a ScraperTarget.
     *
     * @param label         the label of a ScraperTarget
     * @param scraperTargetResponse the ScraperTarget
     */
    void deleteLabel(@Nonnull final Label label, @Nonnull final ScraperTargetResponse scraperTargetResponse);

    /**
     * Removes a label from a ScraperTarget.
     *
     * @param scraperTargetID the ID of a ScraperTarget
     * @param labelID         the ID of a Label
     */
    void deleteLabel(@Nonnull final String labelID, @Nonnull final String scraperTargetID);
}