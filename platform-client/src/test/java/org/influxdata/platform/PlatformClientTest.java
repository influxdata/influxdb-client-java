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

import javax.annotation.Nonnull;

import org.influxdata.platform.impl.TodoException;
import org.influxdata.platform.option.WriteOptions;

import okhttp3.logging.HttpLoggingInterceptor;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (05/09/2018 14:00)
 */
@RunWith(JUnitPlatform.class)
class PlatformClientTest extends AbstractPlatformClientTest {

    @Test
    void writeClient() {

        todo(() -> platformClient.createWriteClient());
        todo(() -> platformClient.createWriteClient(WriteOptions.DEFAULTS));
    }

    @Test
    void createAuthorizationClient() {
        todo(() -> platformClient.createAuthorizationClient());
    }

    @Test
    void createBucketClient() {
        todo(() -> platformClient.createBucketClient());
    }

    @Test
    void createOrganizationClient() {
        todo(() -> platformClient.createOrganizationClient());
    }

    @Test
    void createSourceClient() {
        todo(() -> platformClient.createSourceClient());
    }

    @Test
    void createTaskClient() {
        todo(() -> platformClient.createTaskClient());
    }

    @Test
    void createUserClient() {
        todo(() -> platformClient.createUserClient());
    }

    @Test
    void logLevel() {

        todo(platformClient::getLogLevel);
        todo(() -> platformClient.setLogLevel(HttpLoggingInterceptor.Level.BODY));
    }

    @Test
    void close() {
        todo(() -> platformClient.close());
    }

    private void todo(@Nonnull final ThrowableAssert.ThrowingCallable callable) {
        Assertions.assertThatThrownBy(callable)
                .isInstanceOf(TodoException.class)
                .hasMessage("TODO");
    }
}