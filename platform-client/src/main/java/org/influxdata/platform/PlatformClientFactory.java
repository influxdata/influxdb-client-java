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
package org.influxdata.platform;

import java.util.Objects;
import javax.annotation.Nonnull;

import org.influxdata.platform.impl.PlatformClientImpl;
import org.influxdata.platform.option.PlatformOptions;

/**
 * The Factory that create a instance of a Platform client.
 *
 * @author Jakub Bednar (bednar@github) (05/09/2018 10:04)
 */
public final class PlatformClientFactory {

    private PlatformClientFactory() {
    }

    /**
     * Create a instance of the Platform client.
     *
     * @param url      the url to connect to the Platform
     * @param username the username to use in the basic auth
     * @param password the password to use in the basic auth
     * @return client
     * @see PlatformOptions.Builder#url(String)
     */
    @Nonnull
    public static PlatformClient create(@Nonnull final String url,
                                        @Nonnull final String username,
                                        @Nonnull final char[] password) {

        PlatformOptions options = PlatformOptions.builder()
                .url(url)
                .authenticate(username, password)
                .build();

        return create(options);
    }

    /**
     * Create a instance of the Platform client.
     *
     * @param url      the url to connect to the Platform
     * @param token    the token to use for the authorization
     * @return client
     * @see PlatformOptions.Builder#url(String)
     */
    @Nonnull
    public static PlatformClient create(@Nonnull final String url,
                                        @Nonnull final char[] token) {

        PlatformOptions options = PlatformOptions.builder()
                .url(url)
                .authenticateToken(token)
                .build();

        return create(options);
    }

    /**
     * Create a instance of the Platform client.
     *
     * @param options the connection configuration
     * @return client
     */
    @Nonnull
    public static PlatformClient create(@Nonnull final PlatformOptions options) {

        Objects.requireNonNull(options, "PlatformOptions are required");

        return new PlatformClientImpl(options);
    }
}