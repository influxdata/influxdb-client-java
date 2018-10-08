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
package org.influxdata.platform.rest;

import java.io.EOFException;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.influxdata.platform.error.InfluxException;
import org.influxdata.platform.error.rest.BadGatewayException;
import org.influxdata.platform.error.rest.BadRequestException;
import org.influxdata.platform.error.rest.ForbiddenException;
import org.influxdata.platform.error.rest.InternalServerErrorException;
import org.influxdata.platform.error.rest.MethodNotAllowedException;
import org.influxdata.platform.error.rest.NotAcceptableException;
import org.influxdata.platform.error.rest.NotFoundException;
import org.influxdata.platform.error.rest.NotImplementedException;
import org.influxdata.platform.error.rest.PaymentRequiredException;
import org.influxdata.platform.error.rest.ProxyAuthenticationRequiredException;
import org.influxdata.platform.error.rest.RequestTimeoutException;
import org.influxdata.platform.error.rest.ServiceUnavailableException;
import org.influxdata.platform.error.rest.UnauthorizedException;

import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.Buffer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import retrofit2.Response;

/**
 * @author Jakub Bednar (bednar@github) (04/10/2018 07:57)
 */
@RunWith(JUnitPlatform.class)
class RestClientTest {

    private AbstractRestClient restClient;

    @BeforeEach
    void setUp()
    {
        restClient = new AbstractRestClient() {
        };
    }

    @Test
    void createBodyString() throws IOException {

        RequestBody body = restClient.createBody("mem,host=A,region=west free=10i 10000000000");
        Assertions.assertThat(body).isNotNull();
        Assertions.assertThat(body.contentLength()).isEqualTo(43L);

        Buffer buffer = new Buffer();
        body.writeTo(buffer);

        Assertions.assertThat(buffer.readUtf8()).isEqualTo("mem,host=A,region=west free=10i 10000000000");
    }

    @Test
    void exceptionType() {
        Assertions.assertThatThrownBy(() -> errorResponse(400)).isInstanceOf(BadRequestException.class);
        Assertions.assertThatThrownBy(() -> errorResponse(401)).isInstanceOf(UnauthorizedException.class);
        Assertions.assertThatThrownBy(() -> errorResponse(402)).isInstanceOf(PaymentRequiredException.class);
        Assertions.assertThatThrownBy(() -> errorResponse(403)).isInstanceOf(ForbiddenException.class);
        Assertions.assertThatThrownBy(() -> errorResponse(404)).isInstanceOf(NotFoundException.class);
        Assertions.assertThatThrownBy(() -> errorResponse(405)).isInstanceOf(MethodNotAllowedException.class);
        Assertions.assertThatThrownBy(() -> errorResponse(406)).isInstanceOf(NotAcceptableException.class);
        Assertions.assertThatThrownBy(() -> errorResponse(407)).isInstanceOf(ProxyAuthenticationRequiredException.class);
        Assertions.assertThatThrownBy(() -> errorResponse(408)).isInstanceOf(RequestTimeoutException.class);
        Assertions.assertThatThrownBy(() -> errorResponse(500)).isInstanceOf(InternalServerErrorException.class);
        Assertions.assertThatThrownBy(() -> errorResponse(501)).isInstanceOf(NotImplementedException.class);
        Assertions.assertThatThrownBy(() -> errorResponse(502)).isInstanceOf(BadGatewayException.class);
        Assertions.assertThatThrownBy(() -> errorResponse(503)).isInstanceOf(ServiceUnavailableException.class);
        Assertions.assertThatThrownBy(() -> errorResponse(550)).isInstanceOf(InfluxException.class);
    }

    @Test
    void catchOrPropagate() throws InterruptedException {

        CountDownLatch countDownLatch = new CountDownLatch(1);

        restClient.catchOrPropagateException(new IOException(), throwable -> countDownLatch.countDown());

        countDownLatch.await();
    }

    @Test
    void catchOrPropagateCatchEOFException() {

        restClient.catchOrPropagateException(new EOFException(), throwable -> Assertions.fail("Unreachable", throwable));
    }

    @Test
    void catchOrPropagateCatchSocketClosed() {

        restClient.catchOrPropagateException(new IOException("Socket closed"), throwable -> Assertions.fail("Unreachable", throwable));
    }

    private void errorResponse(final int code)
    {
        okhttp3.Response.Builder builder = new okhttp3.Response.Builder() //
                .code(code)
                .message("Response.error()")
                .protocol(Protocol.HTTP_1_1)
                .request(new Request.Builder().url("http://localhost/").build());

        ResponseBody body = ResponseBody.create(MediaType.parse("application/json"), "");

        throw restClient.responseToError(Response.error(body, builder.build()));
    }
}