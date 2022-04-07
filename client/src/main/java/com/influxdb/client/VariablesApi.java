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
import javax.annotation.concurrent.ThreadSafe;

import com.influxdb.client.domain.Label;
import com.influxdb.client.domain.Organization;
import com.influxdb.client.domain.Variable;

/**
 * The client of the InfluxDB 2.x that implement Variables HTTP API endpoint.
 *
 * @author Jakub Bednar (27/03/2019 09:35)
 */
@ThreadSafe
public interface VariablesApi {

    /**
     * Create a variable.
     *
     * @param variable variable to create
     * @return variable created
     */
    @Nonnull
    Variable createVariable(@Nonnull final Variable variable);

    /**
     * Update a variable.
     *
     * @param variable variable update to apply
     * @return variable updated
     */
    @Nonnull
    Variable updateVariable(@Nonnull final Variable variable);

    /**
     * Delete a variable.
     *
     * @param variable variable to delete
     */
    void deleteVariable(@Nonnull final Variable variable);

    /**
     * Delete a variable.
     *
     * @param variableID id of the variable
     */
    void deleteVariable(@Nonnull final String variableID);

    /**
     * Clone a variable.
     *
     * @param clonedName name of cloned variable
     * @param variableID ID of variable to clone
     * @return cloned variable
     */
    @Nonnull
    Variable cloneVariable(@Nonnull final String clonedName, @Nonnull final String variableID);

    /**
     * Clone a variable.
     *
     * @param clonedName name of cloned variable
     * @param variable   variable to clone
     * @return cloned variable
     */
    @Nonnull
    Variable cloneVariable(@Nonnull final String clonedName, @Nonnull final Variable variable);

    /**
     * Get a variable.
     *
     * @param variableID ID of the variable (required)
     * @return variable found
     */
    @Nonnull
    Variable findVariableByID(@Nonnull final String variableID);

    /**
     * Get all variables.
     *
     * @param organization specifies the organization of the resource
     * @return all variables for an organization
     */
    @Nonnull
    List<Variable> findVariables(@Nonnull final Organization organization);

    /**
     * Get all variables.
     *
     * @param orgID specifies the organization id of the resource
     * @return all variables for an organization
     */
    @Nonnull
    List<Variable> findVariables(@Nonnull final String orgID);

    /**
     * List all labels for a variable.
     *
     * @param variable the variable
     * @return a list of all labels for a variable
     */
    @Nonnull
    List<Label> getLabels(@Nonnull final Variable variable);

    /**
     * List all labels for a variable.
     *
     * @param variableID ID of the variable
     * @return a list of all labels for a variable
     */
    @Nonnull
    List<Label> getLabels(@Nonnull final String variableID);

    /**
     * Add a label to a variable.
     *
     * @param label    label to add
     * @param variable the variable
     * @return a list of all labels for a variable
     */
    @Nonnull
    Label addLabel(@Nonnull final Label label, @Nonnull final Variable variable);

    /**
     * Add a label to a variable.
     *
     * @param variableID ID of the variable
     * @param labelID    the ID of label to add
     * @return a list of all labels for a variable
     */
    @Nonnull
    Label addLabel(@Nonnull final String labelID, @Nonnull final String variableID);

    /**
     * Delete a label from a variable.
     *
     * @param label    the label to delete
     * @param variable the variable
     */
    void deleteLabel(@Nonnull final Label label, @Nonnull final Variable variable);

    /**
     * Delete a label from a variable.
     *
     * @param variableID ID of the variable
     * @param labelID    the ID of a label
     */
    void deleteLabel(@Nonnull final String labelID, @Nonnull final String variableID);
}
