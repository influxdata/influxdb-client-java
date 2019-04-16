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
package org.influxdata.client.internal;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.influxdata.Arguments;
import org.influxdata.client.ScraperTargetsApi;
import org.influxdata.client.domain.AddResourceMemberRequestBody;
import org.influxdata.client.domain.Label;
import org.influxdata.client.domain.LabelMapping;
import org.influxdata.client.domain.LabelResponse;
import org.influxdata.client.domain.LabelsResponse;
import org.influxdata.client.domain.Organization;
import org.influxdata.client.domain.ResourceMember;
import org.influxdata.client.domain.ResourceMembers;
import org.influxdata.client.domain.ResourceOwner;
import org.influxdata.client.domain.ResourceOwners;
import org.influxdata.client.domain.ScraperTargetRequest;
import org.influxdata.client.domain.ScraperTargetResponse;
import org.influxdata.client.domain.ScraperTargetResponses;
import org.influxdata.client.domain.User;
import org.influxdata.client.service.ScraperTargetsService;
import org.influxdata.exceptions.NotFoundException;
import org.influxdata.internal.AbstractRestClient;

import retrofit2.Call;

/**
 * @author Jakub Bednar (bednar@github) (22/01/2019 08:17)
 */
final class ScraperTargetsApiImpl extends AbstractRestClient implements ScraperTargetsApi {

    private static final Logger LOG = Logger.getLogger(ScraperTargetsApiImpl.class.getName());

    private final ScraperTargetsService service;

    ScraperTargetsApiImpl(@Nonnull final ScraperTargetsService service) {

        Arguments.checkNotNull(service, "service");

        this.service = service;
    }

    @Nonnull
    @Override
    public ScraperTargetResponse createScraperTarget(@Nonnull final ScraperTargetRequest scraperTargetRequest) {

        Arguments.checkNotNull(scraperTargetRequest, "scraperTargetRequest");

        Call<ScraperTargetResponse> call = service.scrapersPost(scraperTargetRequest);

        return execute(call);
    }

    @Nonnull
    @Override
    public ScraperTargetResponse createScraperTarget(@Nonnull final String name,
                                                     @Nonnull final String url,
                                                     @Nonnull final String bucketID,
                                                     @Nonnull final String orgID) {

        Arguments.checkNonEmpty(name, "name");
        Arguments.checkNonEmpty(url, "url");
        Arguments.checkNonEmpty(bucketID, "bucketID");
        Arguments.checkNonEmpty(orgID, " orgID");

        ScraperTargetRequest scraperTarget = new ScraperTargetRequest();
        scraperTarget.setName(name);
        scraperTarget.setUrl(url);
        scraperTarget.setBucketID(bucketID);
        scraperTarget.setOrgID(orgID);

        return createScraperTarget(scraperTarget);
    }

    @Nonnull
    @Override
    public ScraperTargetResponse updateScraperTarget(@Nonnull final ScraperTargetResponse scraperTargetResponse) {

        Arguments.checkNotNull(scraperTargetResponse, "scraperTarget");

        return updateScraperTarget(scraperTargetResponse.getId(), scraperTargetResponse);
    }

    @Nonnull
    @Override
    public ScraperTargetResponse updateScraperTarget(@Nonnull final String scraperTargetID,
                                                     @Nonnull final ScraperTargetRequest scraperTargetRequest) {

        Arguments.checkNonEmpty(scraperTargetID, "scraperTargetID");
        Arguments.checkNotNull(scraperTargetRequest, "scraperTargetRequest");

        Call<ScraperTargetResponse> call = service
                .scrapersScraperTargetIDPatch(scraperTargetID, scraperTargetRequest, null);

        return execute(call);
    }

    @Override
    public void deleteScraperTarget(@Nonnull final ScraperTargetResponse scraperTargetResponse) {

        Arguments.checkNotNull(scraperTargetResponse, "scraperTarget");

        deleteScraperTarget(scraperTargetResponse.getId());
    }

    @Override
    public void deleteScraperTarget(@Nonnull final String scraperTargetID) {

        Arguments.checkNonEmpty(scraperTargetID, "scraperTargetID");

        Call<Void> call = service.scrapersScraperTargetIDDelete(scraperTargetID, null);
        execute(call);
    }

    @Nonnull
    @Override
    public ScraperTargetResponse cloneScraperTarget(@Nonnull final String clonedName,
                                                    @Nonnull final String scraperTargetID) {

        Arguments.checkNonEmpty(clonedName, "clonedName");
        Arguments.checkNonEmpty(scraperTargetID, "scraperTargetID");

        ScraperTargetResponse scraperTarget = findScraperTargetByID(scraperTargetID);
        if (scraperTarget == null) {
            throw new IllegalStateException("NotFound ScraperTarget with ID: " + scraperTargetID);
        }

        return cloneScraperTarget(clonedName, scraperTarget);
    }

    @Nonnull
    @Override
    public ScraperTargetResponse cloneScraperTarget(@Nonnull final String clonedName,
                                                    @Nonnull final ScraperTargetResponse scraperTargetResponse) {

        Arguments.checkNonEmpty(clonedName, "clonedName");
        Arguments.checkNotNull(scraperTargetResponse, "scraperTarget");

        ScraperTargetRequest cloned = new ScraperTargetRequest();
        cloned.setName(clonedName);
        cloned.setUrl(scraperTargetResponse.getUrl());
        cloned.setOrgID(scraperTargetResponse.getOrgID());
        cloned.setBucketID(scraperTargetResponse.getBucketID());

        ScraperTargetResponse created = createScraperTarget(cloned);

        getLabels(scraperTargetResponse).forEach(label -> addLabel(label, created));

        return created;
    }

    @Nullable
    @Override
    public ScraperTargetResponse findScraperTargetByID(@Nonnull final String scraperTargetID) {

        Arguments.checkNonEmpty(scraperTargetID, "scraperTargetID");

        Call<ScraperTargetResponse> call = service.scrapersScraperTargetIDGet(scraperTargetID, null);

        return execute(call, NotFoundException.class);
    }

    @Nonnull
    @Override
    public List<ScraperTargetResponse> findScraperTargets() {

        return findScraperTargetsByOrgId(null);
    }

    @Nonnull
    @Override
    public List<ScraperTargetResponse> findScraperTargetsByOrg(@Nonnull final Organization organization) {

        Arguments.checkNotNull(organization, "organization");

        return findScraperTargetsByOrgId(organization.getId());
    }

    @Nonnull
    @Override
    public List<ScraperTargetResponse> findScraperTargetsByOrgId(@Nullable final String orgID) {

        Call<ScraperTargetResponses> call = service.scrapersGet(orgID, null);

        ScraperTargetResponses responses = execute(call);
        LOG.log(Level.FINEST, "findScraperTargets found: {0}", responses);

        return responses.getConfigurations();
    }

    @Nonnull
    @Override
    public List<ResourceMember> getMembers(@Nonnull final ScraperTargetResponse scraperTargetResponse) {

        Arguments.checkNotNull(scraperTargetResponse, "scraperTarget");

        return getMembers(scraperTargetResponse.getId());
    }

    @Nonnull
    @Override
    public List<ResourceMember> getMembers(@Nonnull final String scraperTargetID) {

        Arguments.checkNonEmpty(scraperTargetID, "scraperTargetID");

        Call<ResourceMembers> call = service.scrapersScraperTargetIDMembersGet(scraperTargetID, null);
        ResourceMembers resourceMembers = execute(call);
        LOG.log(Level.FINEST, "findScraperTargetMembers found: {0}", resourceMembers);

        return resourceMembers.getUsers();
    }

    @Nonnull
    @Override
    public ResourceMember addMember(@Nonnull final User member,
                                    @Nonnull final ScraperTargetResponse scraperTargetResponse) {

        Arguments.checkNotNull(scraperTargetResponse, "scraperTarget");
        Arguments.checkNotNull(member, "member");

        return addMember(member.getId(), scraperTargetResponse.getId());
    }

    @Nonnull
    @Override
    public ResourceMember addMember(@Nonnull final String memberID, @Nonnull final String scraperTargetID) {

        Arguments.checkNonEmpty(memberID, "Member ID");
        Arguments.checkNonEmpty(scraperTargetID, "scraperTargetID");

        AddResourceMemberRequestBody user = new AddResourceMemberRequestBody();
        user.setId(memberID);

        Call<ResourceMember> call = service
                .scrapersScraperTargetIDMembersPost(scraperTargetID, user, null);

        return execute(call);
    }

    @Override
    public void deleteMember(@Nonnull final User member, @Nonnull final ScraperTargetResponse scraperTargetResponse) {

        Arguments.checkNotNull(scraperTargetResponse, "scraperTarget");
        Arguments.checkNotNull(member, "member");

        deleteMember(member.getId(), scraperTargetResponse.getId());
    }

    @Override
    public void deleteMember(@Nonnull final String memberID, @Nonnull final String scraperTargetID) {

        Arguments.checkNonEmpty(memberID, "Member ID");
        Arguments.checkNonEmpty(scraperTargetID, "scraperTargetID");

        Call<Void> call = service.scrapersScraperTargetIDMembersUserIDDelete(memberID, scraperTargetID, null);
        execute(call);
    }

    @Nonnull
    @Override
    public List<ResourceOwner> getOwners(@Nonnull final ScraperTargetResponse scraperTargetResponse) {

        Arguments.checkNotNull(scraperTargetResponse, "scraperTarget");

        return getOwners(scraperTargetResponse.getId());
    }

    @Nonnull
    @Override
    public List<ResourceOwner> getOwners(@Nonnull final String scraperTargetID) {

        Arguments.checkNonEmpty(scraperTargetID, "scraperTargetID");

        Call<ResourceOwners> call = service.scrapersScraperTargetIDOwnersGet(scraperTargetID, null);
        ResourceOwners resourceMembers = execute(call);
        LOG.log(Level.FINEST, "findScraperTargetOwners found: {0}", resourceMembers);

        return resourceMembers.getUsers();
    }

    @Nonnull
    @Override
    public ResourceOwner addOwner(@Nonnull final User owner,
                                  @Nonnull final ScraperTargetResponse scraperTargetResponse) {

        Arguments.checkNotNull(scraperTargetResponse, "scraperTarget");
        Arguments.checkNotNull(owner, "owner");

        return addOwner(owner.getId(), scraperTargetResponse.getId());
    }

    @Nonnull
    @Override
    public ResourceOwner addOwner(@Nonnull final String ownerID, @Nonnull final String scraperTargetID) {

        Arguments.checkNonEmpty(ownerID, "Owner ID");
        Arguments.checkNonEmpty(scraperTargetID, "scraperTargetID");

        AddResourceMemberRequestBody user = new AddResourceMemberRequestBody();
        user.setId(ownerID);

        Call<ResourceOwner> call = service.scrapersScraperTargetIDOwnersPost(scraperTargetID, user, null);

        return execute(call);
    }

    @Override
    public void deleteOwner(@Nonnull final User owner, @Nonnull final ScraperTargetResponse scraperTargetResponse) {

        Arguments.checkNotNull(scraperTargetResponse, "scraperTarget");
        Arguments.checkNotNull(owner, "owner");

        deleteOwner(owner.getId(), scraperTargetResponse.getId());
    }

    @Override
    public void deleteOwner(@Nonnull final String ownerID, @Nonnull final String scraperTargetID) {

        Arguments.checkNonEmpty(ownerID, "Owner ID");
        Arguments.checkNonEmpty(scraperTargetID, "scraperTargetID");

        Call<Void> call = service.scrapersScraperTargetIDOwnersUserIDDelete(ownerID, scraperTargetID, null);
        execute(call);
    }

    @Nonnull
    @Override
    public List<Label> getLabels(@Nonnull final ScraperTargetResponse scraperTargetResponse) {

        Arguments.checkNotNull(scraperTargetResponse, "scraperTarget");

        return getLabels(scraperTargetResponse.getId());
    }

    @Nonnull
    @Override
    public List<Label> getLabels(@Nonnull final String scraperTargetID) {

        Arguments.checkNonEmpty(scraperTargetID, "scraperTargetID");

        Call<LabelsResponse> call = service.scrapersScraperTargetIDLabelsGet(scraperTargetID, null);

        return execute(call).getLabels();
    }

    @Nonnull
    @Override
    public LabelResponse addLabel(@Nonnull final Label label,
                                  @Nonnull final ScraperTargetResponse scraperTargetResponse) {

        Arguments.checkNotNull(label, "label");
        Arguments.checkNotNull(scraperTargetResponse, "scraperTarget");

        return addLabel(label.getId(), scraperTargetResponse.getId());
    }

    @Nonnull
    @Override
    public LabelResponse addLabel(@Nonnull final String labelID, @Nonnull final String scraperTargetID) {

        Arguments.checkNonEmpty(labelID, "labelID");
        Arguments.checkNonEmpty(scraperTargetID, "scraperTargetID");

        LabelMapping labelMapping = new LabelMapping();
        labelMapping.setLabelID(labelID);

        Call<LabelResponse> call = service.scrapersScraperTargetIDLabelsPost(scraperTargetID, labelMapping, null);

        return execute(call);
    }

    @Override
    public void deleteLabel(@Nonnull final Label label, @Nonnull final ScraperTargetResponse scraperTargetResponse) {

        Arguments.checkNotNull(label, "label");
        Arguments.checkNotNull(scraperTargetResponse, "scraperTarget");

        deleteLabel(label.getId(), scraperTargetResponse.getId());
    }

    @Override
    public void deleteLabel(@Nonnull final String labelID, @Nonnull final String scraperTargetID) {

        Arguments.checkNonEmpty(labelID, "labelID");
        Arguments.checkNonEmpty(scraperTargetID, "scraperTargetID");

        Call<Void> call = service.scrapersScraperTargetIDLabelsLabelIDDelete(scraperTargetID, labelID, null);
        execute(call);
    }
}