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

import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import okhttp3.HttpUrl;

/**
 * FindOptions represents options passed to all find methods with multiple results.
 *
 * @author Jakub Bednar (bednar@github) (30/01/2019 07:41)
 */
public final class FindOptions {

    public static final String LIMIT_KEY = "limit";
    public static final String OFFSET_KEY = "offset";
    public static final String SORT_BY_KEY = "sortBy";
    public static final String DESCENDING_KEY = "descending";

    private Integer limit;
    private Integer offset;
    private String sortBy;
    private Boolean descending;

    @Nonnull
    public static FindOptions create(@Nullable final String link) {

        FindOptions options = new FindOptions();
        if (link == null) {
            return options;
        }

        HttpUrl httpUrl = HttpUrl.parse("https://influxdb" + link);
        if (httpUrl == null) {
            return options;
        }

        Set<String> qp = httpUrl.queryParameterNames();
        if (!qp.contains(LIMIT_KEY) && !qp.contains(OFFSET_KEY) && !qp.contains(SORT_BY_KEY)
                && !qp.contains(DESCENDING_KEY)) {

            return options;
        }

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

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(final Integer limit) {
        this.limit = limit;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(final Integer offset) {
        this.offset = offset;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(final String sortBy) {
        this.sortBy = sortBy;
    }

    public Boolean getDescending() {
        return descending;
    }

    public void setDescending(final Boolean descending) {
        this.descending = descending;
    }
}