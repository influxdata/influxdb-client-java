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
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.influxdata.client.domain.Label;

/**
 * @author Jakub Bednar (bednar@github) (28/01/2019 10:37)
 */
public interface LabelsApi {

    /**
     * Creates a new label and sets {@link Label#getId()} with the new identifier.
     *
     * @param label label to create
     * @return Label created
     */
    @Nonnull
    Label createLabel(@Nonnull final Label label);

    /**
     * Creates a new label and sets {@link Label#getId()} with the new identifier.
     *
     * @param name       name of a label
     * @param properties properties of a label
     * @return Label created
     */
    @Nonnull
    Label createLabel(@Nonnull final String name, @Nonnull final Map<String, String> properties);

    /**
     * Updates a label's properties.
     *
     * @param label a label with properties to update
     * @return updated label
     */
    @Nonnull
    Label updateLabel(@Nonnull final Label label);

    /**
     * Delete a label.
     *
     * @param label label to delete
     */
    void deleteLabel(@Nonnull final Label label);

    /**
     * Delete a label.
     *
     * @param labelID ID of a label to delete
     */
    void deleteLabel(@Nonnull final String labelID);

    /**
     * Retrieve a label.
     *
     * @param labelID ID of a label to get
     * @return label details
     */
    @Nullable
    Label findLabelByID(@Nonnull final String labelID);

    /**
     * List all labels.
     *
     * @return list all labels.
     */
    @Nonnull
    List<Label> findLabels();
}