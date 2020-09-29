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
package com.influxdb.client.internal;

import javax.annotation.Nonnull;

import com.influxdb.client.internal.AbstractWriteClient.RetryAttempt;

import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.ResponseBody;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import retrofit2.HttpException;
import retrofit2.Response;

/**
 * @author Jakub Bednar (29/09/2020 11:21)
 */
@RunWith(JUnitPlatform.class)
class RetryAttemptTest {

    @Test
    public void throwableType() {
        RetryAttempt retry = new RetryAttempt(new NullPointerException(""), 1, 3);

        Assertions.assertThat(retry.isRetry()).isFalse();

        retry = new RetryAttempt(new HttpException(errorResponse(429)), 1, 3);

        Assertions.assertThat(retry.isRetry()).isTrue();
    }

    @Test
    public void retryableHttpErrorCodes() {
        RetryAttempt retry = new RetryAttempt(new HttpException(errorResponse(428)), 1, 3);
        Assertions.assertThat(retry.isRetry()).isFalse();

        retry = new RetryAttempt(new HttpException(errorResponse(429)), 1, 3);
        Assertions.assertThat(retry.isRetry()).isTrue();

        retry = new RetryAttempt(new HttpException(errorResponse(504)), 1, 3);
        Assertions.assertThat(retry.isRetry()).isTrue();
    }

    @Test
    public void maxRetries() {

        RetryAttempt retry = new RetryAttempt(new HttpException(errorResponse(429)), 1, 3);
        Assertions.assertThat(retry.isRetry()).isTrue();

        retry = new RetryAttempt(new HttpException(errorResponse(429)), 2, 3);
        Assertions.assertThat(retry.isRetry()).isTrue();

        retry = new RetryAttempt(new HttpException(errorResponse(429)), 3, 3);
        Assertions.assertThat(retry.isRetry()).isTrue();

        retry = new RetryAttempt(new HttpException(errorResponse(429)), 4, 3);
        Assertions.assertThat(retry.isRetry()).isFalse();
    }

    @Nonnull
    private Response<Object> errorResponse(final int httpErrorCode) {

        okhttp3.Response.Builder builder = new okhttp3.Response.Builder() //
                .code(httpErrorCode)
                .message("Response.error()")
                .protocol(Protocol.HTTP_1_1)
                .request(new Request.Builder().url("http://localhost/").build());

        ResponseBody body = ResponseBody.create("error", MediaType.parse("application/json"));

        return Response.error(body, builder.build());
    }
}
