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

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * The wrapper for "/api/v2/authorizations" response.
 *
 * @author Jakub Bednar (bednar@github) (17/09/2018 11:43)
 */
public final class Authorizations extends AbstractHasLinks {

    private List<Authorization> auths = new ArrayList<>();

    public List<Authorization> getAuths() {
        return auths;
    }

    public void setAuths(final List<Authorization> auths) {
        this.auths = auths;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Authorizations.class.getSimpleName() + "[", "]")
                .add("auths=" + auths)
                .toString();
    }
}