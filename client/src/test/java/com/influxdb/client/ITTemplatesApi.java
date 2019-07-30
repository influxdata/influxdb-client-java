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

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

import com.influxdb.client.domain.Document;
import com.influxdb.client.domain.DocumentCreate;
import com.influxdb.client.domain.DocumentListEntry;
import com.influxdb.client.domain.DocumentMeta;
import com.influxdb.client.domain.Label;
import com.influxdb.client.domain.LabelCreateRequest;
import com.influxdb.client.domain.Organization;
import com.influxdb.exceptions.BadRequestException;
import com.influxdb.exceptions.NotFoundException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (25/03/2019 09:52)
 */
@RunWith(JUnitPlatform.class)
class ITTemplatesApi extends AbstractITClientTest {

    private TemplatesApi templatesApi;
    private Organization organization;

    @BeforeEach
    void setUp() {

        templatesApi = influxDBClient.getTemplatesApi();
        organization = findMyOrg();

        templatesApi.findTemplates(organization)
                .forEach(documentListEntry -> templatesApi.deleteTemplate(documentListEntry.getId()));
    }

    @Test
    void create() {

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        LabelCreateRequest labelCreateRequest = new LabelCreateRequest();
        labelCreateRequest.setOrgID(organization.getId());
        labelCreateRequest.setName(generateName("label"));
        labelCreateRequest.putPropertiesItem("color", "red");
        labelCreateRequest.putPropertiesItem("priority", "top");

        Label label = influxDBClient.getLabelsApi().createLabel(labelCreateRequest);

        DocumentMeta meta = new DocumentMeta();
        meta.setName(generateName("document-name"));
        meta.setType("testing");
        meta.setVersion("1");
        meta.setDescription(" meta description ");

        DocumentCreate documentCreate = new DocumentCreate();
        documentCreate.setMeta(meta);
        documentCreate.setOrgID(organization.getId());
        documentCreate.setContent("templates content");

        ArrayList<String> labels = new ArrayList<>();
        labels.add(label.getId());
        documentCreate.setLabels(labels);

        Document template = templatesApi.createTemplate(documentCreate);

        Assertions.assertThat(template).isNotNull();
        Assertions.assertThat(template.getId()).isNotBlank();
        Assertions.assertThat(template.getContent()).isEqualTo("templates content");
        Assertions.assertThat(template.getMeta()).isNotNull();
        Assertions.assertThat(template.getMeta().getName()).isEqualTo(meta.getName());
        Assertions.assertThat(template.getMeta().getVersion()).isEqualTo("1");
        Assertions.assertThat(template.getMeta().getType()).isEqualTo("testing");
        Assertions.assertThat(template.getMeta().getDescription()).isEqualTo(" meta description ");
        Assertions.assertThat(template.getMeta().getCreatedAt()).isAfter(now);
        Assertions.assertThat(template.getLinks()).isNotNull();
        Assertions.assertThat(template.getLinks().getSelf()).isEqualTo("/api/v2/documents/templates/" + template.getId());

        Assertions.assertThat(template.getLabels()).hasSize(1);
        Assertions.assertThat(template.getLabels().get(0).getName()).isEqualTo(labelCreateRequest.getName());
    }

    @Test
    void createEmpty() {

        Assertions.assertThatThrownBy(() -> templatesApi.createTemplate(new DocumentCreate()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("missing document body");
    }

    @Test
    void notExistLabel() {

        DocumentCreate documentCreate = createDoc();

        ArrayList<String> labels = new ArrayList<>();
        labels.add("020f755c3c082000");
        documentCreate.setLabels(labels);

        Assertions.assertThatThrownBy(() -> templatesApi.createTemplate(documentCreate))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("label not found");
    }

    @Test
    void deleteTemplate() {

        DocumentCreate documentCreate = createDoc();

        Document createdTemplate = templatesApi.createTemplate(documentCreate);
        Assertions.assertThat(createdTemplate).isNotNull();

        Document foundTemplate = templatesApi.findTemplateByID(createdTemplate.getId());
        Assertions.assertThat(foundTemplate).isNotNull();

        // delete template
        templatesApi.deleteTemplate(createdTemplate);

        Assertions.assertThatThrownBy(() -> templatesApi.findTemplateByID(createdTemplate.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void findTemplateByID() {

        Document template = templatesApi.createTemplate(createDoc());

        Document templateByID = templatesApi.findTemplateByID(template.getId());

        Assertions.assertThat(templateByID).isNotNull();
        Assertions.assertThat(templateByID.getId()).isEqualTo(template.getId());
        Assertions.assertThat(templateByID.getMeta().getName()).isEqualTo(template.getMeta().getName());
    }

    @Test
    void findTemplateByIDNull() {

        Assertions.assertThatThrownBy(() -> templatesApi.findTemplateByID("020f755c3c082000"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("document not found");
    }

    @Test
    void findTemplates() {

        Organization org = influxDBClient.getOrganizationsApi().createOrganization(generateName("org"));

        LabelCreateRequest labelCreateRequest = new LabelCreateRequest();
        labelCreateRequest.setOrgID(organization.getId());
        labelCreateRequest.setName(generateName("label"));
        labelCreateRequest.putPropertiesItem("color", "red");
        labelCreateRequest.putPropertiesItem("priority", "top");

        Label label = influxDBClient.getLabelsApi().createLabel(labelCreateRequest);

        List<DocumentListEntry> templates = templatesApi.findTemplates(org);
        Assertions.assertThat(templates).isEmpty();

        DocumentMeta meta = new DocumentMeta();
        meta.setName(generateName("document-name"));
        meta.setVersion("1");

        DocumentCreate documentCreate = new DocumentCreate();
        documentCreate.setMeta(meta);
        documentCreate.setOrgID(org.getId());
        documentCreate.setContent("templates content");
        documentCreate.setLabels(Arrays.asList(label.getId()));

        templatesApi.createTemplate(documentCreate);
        templates = templatesApi.findTemplates(org);
        Assertions.assertThat(templates).hasSize(1);

        DocumentListEntry entry = templates.get(0);
        Assertions.assertThat(entry.getId()).isNotBlank();
        Assertions.assertThat(entry.getMeta()).isNotNull();
        Assertions.assertThat(entry.getMeta().getVersion()).isEqualTo("1");
        Assertions.assertThat(entry.getMeta().getName()).isEqualTo(meta.getName());
        Assertions.assertThat(entry.getLinks()).isNotNull();
        Assertions.assertThat(entry.getLinks().getSelf()).isEqualTo("/api/v2/documents/templates/" + entry.getId());

        Assertions.assertThat(entry.getLabels()).hasSize(1);
        Assertions.assertThat(entry.getLabels().get(0).getName()).isEqualTo(labelCreateRequest.getName());

        //delete
        templatesApi.deleteTemplate(entry.getId());
    }

    @Test
    void findTemplatesNotFound() {

        Assertions.assertThatThrownBy(() -> templatesApi.findTemplates("020f755c3c082000"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("organization name \"020f755c3c082000\" not found");
    }

    @Test
    void updateTemplate() {

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        DocumentCreate documentCreate = createDoc();

        Document template = templatesApi.createTemplate(documentCreate);

        template
                .content("changed_content")
                .getMeta()
                .version("2")
                .name("changed_name.txt");

        Document updated = templatesApi.updateTemplate(template);

        Assertions.assertThat(updated).isNotNull();
        Assertions.assertThat(updated.getContent()).isEqualTo("changed_content");
        Assertions.assertThat(updated.getMeta()).isNotNull();
        Assertions.assertThat(updated.getMeta().getVersion()).isEqualTo("2");
        Assertions.assertThat(updated.getMeta().getName()).isEqualTo("changed_name.txt");
        Assertions.assertThat(updated.getMeta().getUpdatedAt()).isAfter(now);
        Assertions.assertThat(updated.getMeta().getUpdatedAt()).isAfter(updated.getMeta().getCreatedAt());
    }

    @Test
    void labels() {

        LabelsApi labelsApi = influxDBClient.getLabelsApi();

        Document template = templatesApi.createTemplate(createDoc());

        Map<String, String> properties = new HashMap<>();
        properties.put("color", "green");
        properties.put("location", "west");

        Label label = labelsApi.createLabel(generateName("Cool Resource"), properties, organization.getId());

        List<Label> labels = templatesApi.getLabels(template);
        Assertions.assertThat(labels).hasSize(0);

        Label addedLabel = templatesApi.addLabel(label, template).getLabel();
        Assertions.assertThat(addedLabel).isNotNull();
        Assertions.assertThat(addedLabel.getId()).isEqualTo(label.getId());
        Assertions.assertThat(addedLabel.getName()).isEqualTo(label.getName());
        Assertions.assertThat(addedLabel.getProperties()).isEqualTo(label.getProperties());

        labels = templatesApi.getLabels(template);
        Assertions.assertThat(labels).hasSize(1);
        Assertions.assertThat(labels.get(0).getId()).isEqualTo(label.getId());
        Assertions.assertThat(labels.get(0).getName()).isEqualTo(label.getName());

        templatesApi.deleteLabel(label, template);

        labels = templatesApi.getLabels(template);
        Assertions.assertThat(labels).hasSize(0);
    }

    @Test
    void labelAddNotExists() {

        Document template = templatesApi.createTemplate(createDoc());

        Assertions.assertThatThrownBy(() -> templatesApi.addLabel("020f755c3c082000", template.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void labelDeleteNotExists() {

        Document template = templatesApi.createTemplate(createDoc());

        Assertions.assertThatThrownBy(() -> templatesApi.deleteLabel("020f755c3c082000", template.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @Disabled
    //TODO https://github.com/influxdata/influxdb/issues/12968
    void cloneTemplate() {

        Document template = templatesApi.createTemplate(createDoc());

        Map<String, String> properties = new HashMap<>();
        properties.put("color", "green");
        properties.put("location", "west");

        Label label = influxDBClient.getLabelsApi().createLabel(generateName("Cool Resource"), properties, organization.getId());

        templatesApi.addLabel(label, template);

        Document cloned = templatesApi.cloneTemplate(generateName("cloned-template"), template.getId());

        Assertions.assertThat(cloned).isNotNull();
        Assertions.assertThat(cloned.getContent()).isEqualTo("templates content");
        Assertions.assertThat(cloned.getMeta()).isNotNull();
        Assertions.assertThat(cloned.getMeta().getVersion()).isEqualTo("1");
        Assertions.assertThat(cloned.getMeta().getName()).startsWith("cloned-template");

        List<Label> labels = templatesApi.getLabels(cloned);
        Assertions.assertThat(labels).hasSize(1);
        Assertions.assertThat(labels.get(0).getId()).isEqualTo(label.getId());
    }

    @Nonnull
    private DocumentCreate createDoc() {

        DocumentMeta meta = new DocumentMeta();
        meta.setName(generateName("document-name"));
        meta.setVersion("1");

        DocumentCreate documentCreate = new DocumentCreate();
        documentCreate.setMeta(meta);
        documentCreate.setOrgID(organization.getId());
        documentCreate.setContent("templates content");

        return documentCreate;
    }
}