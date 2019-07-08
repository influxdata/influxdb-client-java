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

import com.influxdb.client.domain.Document;
import com.influxdb.client.domain.DocumentCreate;
import com.influxdb.client.domain.DocumentListEntry;
import com.influxdb.client.domain.DocumentUpdate;
import com.influxdb.client.domain.Label;
import com.influxdb.client.domain.LabelResponse;
import com.influxdb.client.domain.Organization;

/**
 * @author Jakub Bednar (bednar@github) (25/03/2019 09:11)
 */
public interface TemplatesApi {

    /**
     * Create a template.
     *
     * @param templateCreate template that will be created
     * @return Template created
     */
    @Nonnull
    Document createTemplate(@Nonnull final DocumentCreate templateCreate);

    /**
     * Update a template.
     *
     * @param template template that will be updated
     * @return the newly updated template
     */
    @Nonnull
    Document updateTemplate(@Nonnull final Document template);

    /**
     * Update a template.
     *
     * @param templateID ID of template
     * @param template   template that will be updated
     * @return the newly updated template
     */
    @Nonnull
    Document updateTemplate(@Nonnull final String templateID, @Nonnull final DocumentUpdate template);

    /**
     * Delete a template.
     *
     * @param template template to delete
     */
    void deleteTemplate(@Nonnull final Document template);

    /**
     * Delete a template.
     *
     * @param templateID ID of template to delete
     */
    void deleteTemplate(@Nonnull final String templateID);

    /**
     * List all labels for a template.
     *
     * @param template the template
     * @return return a list of all labels for a template
     */
    @Nonnull
    List<Label> getLabels(@Nonnull final Document template);

    /**
     * List all labels for a template.
     *
     * @param templateID ID of template
     * @return return a list of all labels for a template
     */
    @Nonnull
    List<Label> getLabels(@Nonnull final String templateID);

    /**
     * Add a label to a template.
     *
     * @param label    label to add
     * @param template the template
     * @return added label
     */
    @Nonnull
    LabelResponse addLabel(@Nonnull final Label label, @Nonnull final Document template);

    /**
     * Add a label to a template.
     *
     * @param templateID ID of template
     * @param labelID    the ID of label to add
     * @return added label
     */
    @Nonnull
    LabelResponse addLabel(@Nonnull final String labelID, @Nonnull final String templateID);

    /**
     * Delete a label from a template.
     *
     * @param label    the label
     * @param template the template
     */
    void deleteLabel(@Nonnull final Label label, @Nonnull final Document template);

    /**
     * Delete a label from a template.
     *
     * @param templateID   ID of template
     * @param labelID the label ID
     */
    void deleteLabel(@Nonnull final String labelID, @Nonnull final String templateID);

    /**
     * Clone a template.
     *
     * @param clonedName name of cloned template
     * @param templateID ID of template to clone
     * @return cloned template
     */
    @Nonnull
    Document cloneTemplate(@Nonnull final String clonedName, @Nonnull final String templateID);

    /**
     * Clone a template.
     *
     * @param clonedName name of cloned template
     * @param template   template to clone
     * @return cloned template
     */
    @Nonnull
    Document cloneTemplate(@Nonnull final String clonedName, @Nonnull final Document template);

    /**
     * Retrieve a template.
     *
     * @param templateID ID of template
     * @return the template requested
     */
    @Nonnull
    Document findTemplateByID(@Nonnull final String templateID);

    /**
     * List of template documents.
     *
     * @param organization specifies the organization of the template (required)
     * @return a list of template documents
     */
    @Nonnull
    List<DocumentListEntry> findTemplates(@Nonnull final Organization organization);

    /**
     * List of template documents.
     *
     * @param orgName specifies the name of the organization of the template (required)
     * @return a list of template documents
     */
    @Nonnull
    List<DocumentListEntry> findTemplates(@Nonnull final String orgName);
}