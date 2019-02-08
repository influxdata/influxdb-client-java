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
package org.influxdata.client.domain;

/**
 * Resource is an authorizable resource.
 *
 * @author Jakub Bednar (bednar@github) (16/01/2019 06:59)
 */
public final class PermissionResource {

    /**
     * Resource ID.
     */
    private String id;

    /**
     * Resource type.
     */
    private ResourceType type;

    private String orgID;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public ResourceType getType() {
        return type;
    }

    public void setType(final ResourceType type) {
        this.type = type;
    }

    public String getOrgID() {
        return orgID;
    }

    public void setOrgID(final String orgID) {
        this.orgID = orgID;
    }
}