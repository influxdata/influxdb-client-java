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

import java.util.List;
import javax.annotation.Nonnull;

import org.influxdata.client.domain.Document;
import org.influxdata.client.domain.DocumentCreate;
import org.influxdata.client.domain.DocumentListEntry;
import org.influxdata.client.domain.DocumentUpdate;
import org.influxdata.client.domain.Organization;

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