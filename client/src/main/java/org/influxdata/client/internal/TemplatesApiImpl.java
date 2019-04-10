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
import javax.annotation.Nonnull;

import org.influxdata.Arguments;
import org.influxdata.client.TemplatesApi;
import org.influxdata.client.domain.Document;
import org.influxdata.client.domain.DocumentCreate;
import org.influxdata.client.domain.DocumentListEntry;
import org.influxdata.client.domain.DocumentUpdate;
import org.influxdata.client.domain.Documents;
import org.influxdata.client.domain.Organization;
import org.influxdata.client.service.TemplatesService;
import org.influxdata.internal.AbstractRestClient;

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

        Call<Document> call = service.documentsTemplatesPost(templateCreate, null);

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

        Call<Document> call = service.documentsTemplatesTemplateIDPut(templateID, template, null);

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

        Call<Void> call = service.documentsTemplatesTemplateIDDelete(templateID, null);

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
                .meta(template.getMeta().name(clonedName))
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

        Call<Document> call = service.documentsTemplatesTemplateIDGet(templateID, null);

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

        Call<Documents> call = service.documentsTemplatesGet(null, orgName, null);

        return execute(call).getDocuments();
    }
}