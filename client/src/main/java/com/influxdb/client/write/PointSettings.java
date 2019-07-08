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
package com.influxdb.client.write;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.influxdb.Arguments;

/**
 * The setting for store data point: default values, threshold, ...
 *
 * @author Jakub Bednar (bednar@github) (28/06/2019 09:06)
 */
public final class PointSettings {

    private static final Pattern ENV_PROPERTY = Pattern.compile("(\\$\\{env\\.)(.+)(})");
    private static final Pattern SYSTEM_PROPERTY = Pattern.compile("(\\$\\{)(.+)(})");

    private final Map<String, String> defaultTags = new TreeMap<>();

    /**
     * Add default tag.
     *
     * @param key        the tag name
     * @param expression the tag value expression
     * @return this
     */
    @Nonnull
    public PointSettings addDefaultTag(@Nonnull final String key,
                                       @Nullable final String expression) {

        Arguments.checkNotNull(key, "tagName");

        defaultTags.put(key, expression);

        return this;
    }

    /**
     * Get default tags with evaluated expressions.
     *
     * @return evaluated default tags
     */
    @Nonnull
    Map<String, String> getDefaultTags() {

        Function<String, String> evaluation = expression -> {

            if (expression == null) {
                return null;
            }

            // env property
            Matcher matcher = ENV_PROPERTY.matcher(expression);
            if (matcher.matches()) {
                return System.getenv(matcher.group(2));
            }

            // system property
            matcher = SYSTEM_PROPERTY.matcher(expression);
            if (matcher.matches()) {
                return System.getProperty(matcher.group(2));
            }

            return expression;
        };

        return defaultTags
                .entrySet()
                .stream()
                .map(entry -> new String[]{entry.getKey(), evaluation.apply(entry.getValue())})
                .filter(keyValue -> keyValue[1] != null)
                .collect(Collectors.toMap(keyValue -> keyValue[0], keyValue -> keyValue[1], (o, n) -> n, TreeMap::new));
    }
}