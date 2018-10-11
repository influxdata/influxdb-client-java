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
package org.influxdata.platform.domain;

import java.util.StringJoiner;
import javax.annotation.Nonnull;

import org.influxdata.platform.Arguments;


/**
 * Permission defines an action and a resource.
 *
 * @author Jakub Bednar (bednar@github) (17/09/2018 09:48)
 */
public final class Permission {

    /**
     * Action for reading.
     */
    public static final String READ_ACTION = "read";

    /**
     * Action for writing.
     */
    public static final String WRITE_ACTION = "write";

    /**
     * Action for creating new resources.
     */
    public static final String CREATE_ACTION = "create";

    /**
     * Deleting an existing resource.
     */
    public static final String DELETE_ACTION = "delete";

    /**
     * Represents the user resource actions can apply to.
     */
    public static final String USER_RESOURCE = "user";

    /**
     * Represents the org resource actions can apply to.
     */
    public static final String ORGANIZATION_RESOURCE = "org";


    /**
     * Represents the task resource scoped to an organization.
     *
     * @param orgID organization scope
     * @return task resource
     */
    @Nonnull
    public static String taskResource(@Nonnull final String orgID) {

        Arguments.checkNonEmpty(orgID, "orgID");

        return String.format("org/%s/task", orgID);
    }

    /**
     * BucketResource constructs a bucket resource.
     *
     * @param bucketID bucket scope
     * @return bucket resource
     */
    public static String bucketResource(@Nonnull final String bucketID) {

        Arguments.checkNonEmpty(bucketID, "bucketID");

        return String.format("bucket/%s", bucketID);
    }

    private String resource;
    private String action;

    public String getResource() {
        return resource;
    }

    public void setResource(final String resource) {
        this.resource = resource;
    }

    public String getAction() {
        return action;
    }

    public void setAction(final String action) {
        this.action = action;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Permission.class.getSimpleName() + "[", "]")
                .add("resource='" + resource + "'")
                .add("action='" + action + "'")
                .toString();
    }
}