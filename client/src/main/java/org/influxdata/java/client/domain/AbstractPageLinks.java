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
package org.influxdata.java.client.domain;

import java.util.Set;
import javax.annotation.Nullable;

import org.influxdata.client.Arguments;

import okhttp3.HttpUrl;

import static org.influxdata.java.client.domain.FindOptions.DESCENDING_KEY;
import static org.influxdata.java.client.domain.FindOptions.LIMIT_KEY;
import static org.influxdata.java.client.domain.FindOptions.OFFSET_KEY;
import static org.influxdata.java.client.domain.FindOptions.SORT_BY_KEY;

/**
 * PagingLinks represents paging links.
 *
 * @author Jakub Bednar (bednar@github) (30/01/2019 08:26)
 */
public abstract class AbstractPageLinks extends AbstractHasLinks {

    @Nullable
    public FindOptions getPrevPage() {
        return getFindOptions("prev");
    }

    @Nullable
    public FindOptions getSelfPage() {
        return getFindOptions("self");
    }

    @Nullable
    public FindOptions getNextPage() {
        return getFindOptions("next");
    }

    @Nullable
    private FindOptions getFindOptions(final String key) {

        Arguments.checkNonEmpty(key, "key");

        String link = getLinks().get(key);
        if (link == null) {
            return null;
        }


        HttpUrl httpUrl = HttpUrl.parse("https://influxdb" + link);
        if (httpUrl == null) {
            return null;
        }

        Set<String> qp = httpUrl.queryParameterNames();
        if (!qp.contains(LIMIT_KEY) && !qp.contains(OFFSET_KEY) && !qp.contains(SORT_BY_KEY)
                && !qp.contains(DESCENDING_KEY)) {

            return null;
        }

        FindOptions options = new FindOptions();

        String limit = httpUrl.queryParameter(LIMIT_KEY);
        if (limit != null) {
            options.setLimit(Integer.valueOf(limit));
        }

        String offset = httpUrl.queryParameter(OFFSET_KEY);
        if (offset != null) {
            options.setOffset(Integer.valueOf(offset));
        }

        String sortBy = httpUrl.queryParameter(SORT_BY_KEY);
        if (sortBy != null) {
            options.setSortBy(sortBy);
        }

        String descending = httpUrl.queryParameter(DESCENDING_KEY);
        if (descending != null) {
            options.setDescending(Boolean.valueOf(descending));
        }

        return options;
    }

}