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

import com.influxdb.Arguments;
import com.influxdb.client.TemplatesApi;
import com.influxdb.client.domain.Document;
import com.influxdb.client.domain.DocumentCreate;
import com.influxdb.client.domain.DocumentListEntry;
import com.influxdb.client.domain.DocumentUpdate;
import com.influxdb.client.domain.Documents;
import com.influxdb.client.domain.Label;
import com.influxdb.client.domain.LabelMapping;
import com.influxdb.client.domain.LabelResponse;
import com.influxdb.client.domain.LabelsResponse;
import com.influxdb.client.domain.Organization;
import com.influxdb.client.service.TemplatesService;
import com.influxdb.internal.AbstractRestClient;

import retrofit2.Call;

/**
 * @author Jakub Bednar (bednar@github) (25/03/2019 09:45)
 */
final class TemplatesApiImpl extends AbstractRestClient implements TemplatesApi {

    private final TemplatesService service;

    TemplatesApiImpl(@Nonnull final TemplatesService service) {

        Arguments.checkNotNull(service, "service");

        this.service = service;
    }

    @Nonnull
    @Override
    public Document createTemplate(@Nonnull final DocumentCreate templateCreate) {

        Arguments.checkNotNull(templateCreate, "documentCreate");

        Call<Document> call = service.postDocumentsTemplates(templateCreate, null);

        return execute(call);
    }

    @Nonnull
    @Override
    public Document updateTemplate(@Nonnull final Document template) {

        Arguments.checkNotNull(template, "template");

        DocumentUpdate update = new DocumentUpdate()
                .meta(template.getMeta())
                .content(template.getContent());

        return updateTemplate(template.getId(), update);
    }

    @Nonnull
    @Override
    public Document updateTemplate(@Nonnull final String templateID,
                                   @Nonnull final DocumentUpdate template) {

        Arguments.checkNonEmpty(templateID, "templateID");
        Arguments.checkNotNull(template, "template");

        Call<Document> call = service.putDocumentsTemplatesID(templateID, template, null);

        return execute(call);
    }

    @Override
    public void deleteTemplate(@Nonnull final Document template) {

        Arguments.checkNotNull(template, "template");

        deleteTemplate(template.getId());
    }

    @Override
    public void deleteTemplate(@Nonnull final String templateID) {

        Arguments.checkNonEmpty(templateID, "templateID");

        Call<Void> call = service.deleteDocumentsTemplatesID(templateID, null);

        execute(call);
    }

    @Nonnull
    @Override
    public List<Label> getLabels(@Nonnull final Document template) {

        Arguments.checkNotNull(template, "template");

        return getLabels(template.getId());
    }

    @Nonnull
    @Override
    public List<Label> getLabels(@Nonnull final String templateID) {

        Arguments.checkNonEmpty(templateID, "templateID");

        Call<LabelsResponse> call = service.getDocumentsTemplatesIDLabels(templateID, null);

        return execute(call).getLabels();
    }

    @Nonnull
    @Override
    public LabelResponse addLabel(@Nonnull final Label label, @Nonnull final Document template) {

        Arguments.checkNotNull(label, "label");
        Arguments.checkNotNull(template, "template");


        return addLabel(label.getId(), template.getId());
    }

    @Nonnull
    @Override
    public LabelResponse addLabel(@Nonnull final String labelID, @Nonnull final String templateID) {

        Arguments.checkNonEmpty(labelID, "labelID");
        Arguments.checkNonEmpty(templateID, "templateID");

        LabelMapping mapping = new LabelMapping()
                .labelID(labelID);

        Call<LabelResponse> call = service.postDocumentsTemplatesIDLabels(templateID, mapping, null);

        return execute(call);
    }

    @Override
    public void deleteLabel(@Nonnull final Label label, @Nonnull final Document template) {

        Arguments.checkNotNull(label, "label");
        Arguments.checkNotNull(template, "template");

        deleteLabel(label.getId(), template.getId());

    }

    @Override
    public void deleteLabel(@Nonnull final String labelID, @Nonnull final String templateID) {

        Arguments.checkNonEmpty(labelID, "labelID");
        Arguments.checkNonEmpty(templateID, "templateID");

        Call<Void> call = service.deleteDocumentsTemplatesIDLabelsID(templateID, labelID, null);

        execute(call);
    }

    @Nonnull
    @Override
    public Document cloneTemplate(@Nonnull final String clonedName, @Nonnull final String templateID) {

        Arguments.checkNonEmpty(templateID, "templateID");

        return cloneTemplate(clonedName, findTemplateByID(templateID));
    }

    @Nonnull
    @Override
    public Document cloneTemplate(@Nonnull final String clonedName, @Nonnull final Document template) {

        Arguments.checkNonEmpty(clonedName, "clonedName");
        Arguments.checkNotNull(template, "template");

        DocumentCreate documentCreate = new DocumentCreate();
        documentCreate
                .meta(template.getMeta().name(clonedName).description(template.getMeta().getDescription()))
                .content(template.getContent());

        if (template.getLabels() != null) {

            template.getLabels().forEach(label -> documentCreate.addLabelsItem(label.getName()));
        }

        return createTemplate(documentCreate);
    }

    @Nonnull
    @Override
    public Document findTemplateByID(@Nonnull final String templateID) {

        Arguments.checkNonEmpty(templateID, "templateID");

        Call<Document> call = service.getDocumentsTemplatesID(templateID, null);

        return execute(call);
    }

    @Nonnull
    @Override
    public List<DocumentListEntry> findTemplates(@Nonnull final Organization organization) {

        Arguments.checkNotNull(organization, "organization");

        return findTemplates(organization.getName());
    }

    @Nonnull
    @Override
    public List<DocumentListEntry> findTemplates(@Nonnull final String orgName) {

        Arguments.checkNonEmpty(orgName, "orgName");

        Call<Documents> call = service.getDocumentsTemplates(null, orgName, null);

        return execute(call).getDocuments();
    }
}