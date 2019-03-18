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
import org.influxdata.client.domain.ResourceMember;
import org.influxdata.client.domain.ResourceMembers;
import org.influxdata.client.domain.ResourceOwner;
import org.influxdata.client.domain.ResourceOwners;
import org.influxdata.client.domain.ScraperTargetRequest;
import org.influxdata.client.domain.ScraperTargetResponse;
import org.influxdata.client.domain.ScraperTargetResponses;
import org.influxdata.client.domain.User;
import org.influxdata.exceptions.NotFoundException;

import com.google.gson.Gson;
import retrofit2.Call;

/**
 * @author Jakub Bednar (bednar@github) (22/01/2019 08:17)
 */
final class ScraperTargetsApiImpl extends AbstractInfluxDBRestClient implements ScraperTargetsApi {

    private static final Logger LOG = Logger.getLogger(ScraperTargetsApiImpl.class.getName());

    ScraperTargetsApiImpl(@Nonnull final InfluxDBService influxDBService, @Nonnull final Gson gson) {

        super(influxDBService, gson);
    }

    @Nonnull
    @Override
    public ScraperTargetResponse createScraperTarget(@Nonnull final ScraperTargetRequest scraperTarget) {

        Arguments.checkNotNull(scraperTarget, "scraperTarget");

        String json = gson.toJson(scraperTarget);
        Call<ScraperTargetResponse> call = influxDBService.createScraperTarget(createBody(json));

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
        scraperTarget.setType(ScraperTargetRequest.TypeEnum.PROMETHEUS);

        return createScraperTarget(scraperTarget);
    }

    @Nonnull
    @Override
    public ScraperTargetResponse updateScraperTarget(@Nonnull final ScraperTargetResponse scraperTarget) {

        Arguments.checkNotNull(scraperTarget, "scraperTarget");

        String json = gson.toJson(scraperTarget);

        Call<ScraperTargetResponse> call = influxDBService.updateScraperTarget(scraperTarget.getId(), createBody(json));

        return execute(call);
    }

    @Override
    public void deleteScraperTarget(@Nonnull final ScraperTargetResponse scraperTarget) {

        Arguments.checkNotNull(scraperTarget, "scraperTarget");

        deleteScraperTarget(scraperTarget.getId());
    }

    @Override
    public void deleteScraperTarget(@Nonnull final String scraperTargetID) {

        Arguments.checkNonEmpty(scraperTargetID, "scraperTargetID");

        Call<Void> call = influxDBService.deleteScraperTarget(scraperTargetID);
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
                                                    @Nonnull final ScraperTargetResponse scraperTarget) {

        Arguments.checkNonEmpty(clonedName, "clonedName");
        Arguments.checkNotNull(scraperTarget, "scraperTarget");

        ScraperTargetRequest cloned = new ScraperTargetRequest();
        cloned.setName(clonedName);
        cloned.setType(scraperTarget.getType());
        cloned.setUrl(scraperTarget.getUrl());
        cloned.setOrgID(scraperTarget.getOrgID());
        cloned.setBucketID(scraperTarget.getBucketID());

        ScraperTargetResponse created = createScraperTarget(cloned);

        getLabels(scraperTarget).forEach(label -> addLabel(label, created));

        return created;
    }

    @Nullable
    @Override
    public ScraperTargetResponse findScraperTargetByID(@Nonnull final String scraperTargetID) {

        Arguments.checkNonEmpty(scraperTargetID, "scraperTargetID");

        Call<ScraperTargetResponse> call = influxDBService.findScraperTargetByID(scraperTargetID);

        return execute(call, NotFoundException.class);
    }

    @Nonnull
    @Override
    public List<ScraperTargetResponse> findScraperTargets() {

        Call<ScraperTargetResponses> call = influxDBService.findScraperTargets();

        ScraperTargetResponses responses = execute(call);
        LOG.log(Level.FINEST, "findScraperTargets found: {0}", responses);

        return responses.getConfigurations();
    }

    @Nonnull
    @Override
    public List<ResourceMember> getMembers(@Nonnull final ScraperTargetResponse scraperTarget) {

        Arguments.checkNotNull(scraperTarget, "scraperTarget");

        return getMembers(scraperTarget.getId());
    }

    @Nonnull
    @Override
    public List<ResourceMember> getMembers(@Nonnull final String scraperTargetID) {

        Arguments.checkNonEmpty(scraperTargetID, "scraperTargetID");

        Call<ResourceMembers> call = influxDBService.findScraperTargetMembers(scraperTargetID);
        ResourceMembers resourceMembers = execute(call);
        LOG.log(Level.FINEST, "findScraperTargetMembers found: {0}", resourceMembers);

        return resourceMembers.getUsers();
    }

    @Nonnull
    @Override
    public ResourceMember addMember(@Nonnull final User member, @Nonnull final ScraperTargetResponse scraperTarget) {

        Arguments.checkNotNull(scraperTarget, "scraperTarget");
        Arguments.checkNotNull(member, "member");

        return addMember(member.getId(), scraperTarget.getId());
    }

    @Nonnull
    @Override
    public ResourceMember addMember(@Nonnull final String memberID, @Nonnull final String scraperTargetID) {

        Arguments.checkNonEmpty(memberID, "Member ID");
        Arguments.checkNonEmpty(scraperTargetID, "scraperTargetID");

        AddResourceMemberRequestBody user = new AddResourceMemberRequestBody();
        user.setId(memberID);

        String json = gson.toJson(user);
        Call<ResourceMember> call = influxDBService.addScraperTargetMember(scraperTargetID, createBody(json));

        return execute(call);
    }

    @Override
    public void deleteMember(@Nonnull final User member, @Nonnull final ScraperTargetResponse scraperTarget) {

        Arguments.checkNotNull(scraperTarget, "scraperTarget");
        Arguments.checkNotNull(member, "member");

        deleteMember(member.getId(), scraperTarget.getId());
    }

    @Override
    public void deleteMember(@Nonnull final String memberID, @Nonnull final String scraperTargetID) {

        Arguments.checkNonEmpty(memberID, "Member ID");
        Arguments.checkNonEmpty(scraperTargetID, "scraperTargetID");

        Call<Void> call = influxDBService.deleteScraperTargetMember(scraperTargetID, memberID);
        execute(call);
    }

    @Nonnull
    @Override
    public List<ResourceOwner> getOwners(@Nonnull final ScraperTargetResponse scraperTarget) {

        Arguments.checkNotNull(scraperTarget, "scraperTarget");

        return getOwners(scraperTarget.getId());
    }

    @Nonnull
    @Override
    public List<ResourceOwner> getOwners(@Nonnull final String scraperTargetID) {

        Arguments.checkNonEmpty(scraperTargetID, "scraperTargetID");

        Call<ResourceOwners> call = influxDBService.findScraperTargetOwners(scraperTargetID);
        ResourceOwners resourceMembers = execute(call);
        LOG.log(Level.FINEST, "findScraperTargetOwners found: {0}", resourceMembers);

        return resourceMembers.getUsers();
    }

    @Nonnull
    @Override
    public ResourceOwner addOwner(@Nonnull final User owner, @Nonnull final ScraperTargetResponse scraperTarget) {

        Arguments.checkNotNull(scraperTarget, "scraperTarget");
        Arguments.checkNotNull(owner, "owner");

        return addOwner(owner.getId(), scraperTarget.getId());
    }

    @Nonnull
    @Override
    public ResourceOwner addOwner(@Nonnull final String ownerID, @Nonnull final String scraperTargetID) {

        Arguments.checkNonEmpty(ownerID, "Owner ID");
        Arguments.checkNonEmpty(scraperTargetID, "scraperTargetID");

        AddResourceMemberRequestBody user = new AddResourceMemberRequestBody();
        user.setId(ownerID);

        String json = gson.toJson(user);
        Call<ResourceOwner> call = influxDBService.addScraperTargetOwner(scraperTargetID, createBody(json));

        return execute(call);
    }

    @Override
    public void deleteOwner(@Nonnull final User owner, @Nonnull final ScraperTargetResponse scraperTarget) {

        Arguments.checkNotNull(scraperTarget, "scraperTarget");
        Arguments.checkNotNull(owner, "owner");

        deleteOwner(owner.getId(), scraperTarget.getId());
    }

    @Override
    public void deleteOwner(@Nonnull final String ownerID, @Nonnull final String scraperTargetID) {

        Arguments.checkNonEmpty(ownerID, "Owner ID");
        Arguments.checkNonEmpty(scraperTargetID, "scraperTargetID");

        Call<Void> call = influxDBService.deleteScraperTargetOwner(scraperTargetID, ownerID);
        execute(call);
    }

    @Nonnull
    @Override
    public List<Label> getLabels(@Nonnull final ScraperTargetResponse scraperTarget) {

        Arguments.checkNotNull(scraperTarget, "scraperTarget");

        return getLabels(scraperTarget.getId());
    }

    @Nonnull
    @Override
    public List<Label> getLabels(@Nonnull final String scraperTargetID) {

        Arguments.checkNonEmpty(scraperTargetID, "scraperTargetID");

        return getLabels(scraperTargetID, "scrapers");
    }

    @Nonnull
    @Override
    public Label addLabel(@Nonnull final Label label, @Nonnull final ScraperTargetResponse scraperTarget) {

        Arguments.checkNotNull(label, "label");
        Arguments.checkNotNull(scraperTarget, "scraperTarget");

        return addLabel(label.getId(), scraperTarget.getId());
    }

    @Nonnull
    @Override
    public Label addLabel(@Nonnull final String labelID, @Nonnull final String scraperTargetID) {

        Arguments.checkNonEmpty(labelID, "labelID");
        Arguments.checkNonEmpty(scraperTargetID, "scraperTargetID");

        return addLabel(labelID, scraperTargetID, "scrapers");
    }

    @Override
    public void deleteLabel(@Nonnull final Label label, @Nonnull final ScraperTargetResponse scraperTarget) {

        Arguments.checkNotNull(label, "label");
        Arguments.checkNotNull(scraperTarget, "scraperTarget");

        deleteLabel(label.getId(), scraperTarget.getId());
    }

    @Override
    public void deleteLabel(@Nonnull final String labelID, @Nonnull final String scraperTargetID) {

        Arguments.checkNonEmpty(labelID, "labelID");
        Arguments.checkNonEmpty(scraperTargetID, "scraperTargetID");

        deleteLabel(labelID, scraperTargetID, "scrapers");
    }
}