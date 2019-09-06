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
package com.influxdb.internal;

import java.io.EOFException;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import javax.annotation.Nonnull;

import com.influxdb.LogLevel;
import com.influxdb.exceptions.BadGatewayException;
import com.influxdb.exceptions.BadRequestException;
import com.influxdb.exceptions.ForbiddenException;
import com.influxdb.exceptions.InfluxException;
import com.influxdb.exceptions.InternalServerErrorException;
import com.influxdb.exceptions.MethodNotAllowedException;
import com.influxdb.exceptions.NotAcceptableException;
import com.influxdb.exceptions.NotFoundException;
import com.influxdb.exceptions.NotImplementedException;
import com.influxdb.exceptions.PaymentRequiredException;
import com.influxdb.exceptions.ProxyAuthenticationRequiredException;
import com.influxdb.exceptions.RequestEntityTooLargeException;
import com.influxdb.exceptions.RequestTimeoutException;
import com.influxdb.exceptions.ServiceUnavailableException;
import com.influxdb.exceptions.UnauthorizedException;
import com.influxdb.exceptions.UnprocessableEntityException;
import com.influxdb.test.AbstractMockServerTest;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.mockwebserver.MockResponse;
import okio.Buffer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;

/**
 * @author Jakub Bednar (bednar@github) (04/10/2018 07:57)
 */
@RunWith(JUnitPlatform.class)
class RestClientTest extends AbstractMockServerTest {

    private AbstractRestClient restClient;

    private String serverURL;

    @BeforeEach
    void setUp() {
        restClient = new AbstractRestClient() {
        };

        serverURL = startMockServer();
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
        Assertions.assertThatThrownBy(() -> errorResponse(413)).isInstanceOf(RequestEntityTooLargeException.class);
        Assertions.assertThatThrownBy(() -> errorResponse(422)).isInstanceOf(UnprocessableEntityException.class);
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
    void catchOrPropagateCatchSocketClosed() throws InterruptedException {

        CountDownLatch countDownLatch = new CountDownLatch(1);

        restClient.catchOrPropagateException(new IOException("Socket closed"), throwable -> countDownLatch.countDown());

        countDownLatch.await();
    }

    @Test
    void mappingLogLevel() {

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();

        restClient.setLogLevel(interceptor, LogLevel.BODY);
        Assertions.assertThat(interceptor.getLevel()).isEqualTo(HttpLoggingInterceptor.Level.BODY);
        Assertions.assertThat(restClient.getLogLevel(interceptor)).isEqualTo(LogLevel.BODY);

        restClient.setLogLevel(interceptor, LogLevel.HEADERS);
        Assertions.assertThat(interceptor.getLevel()).isEqualTo(HttpLoggingInterceptor.Level.HEADERS);
        Assertions.assertThat(restClient.getLogLevel(interceptor)).isEqualTo(LogLevel.HEADERS);

        restClient.setLogLevel(interceptor, LogLevel.BASIC);
        Assertions.assertThat(interceptor.getLevel()).isEqualTo(HttpLoggingInterceptor.Level.BASIC);
        Assertions.assertThat(restClient.getLogLevel(interceptor)).isEqualTo(LogLevel.BASIC);

        restClient.setLogLevel(interceptor, LogLevel.NONE);
        Assertions.assertThat(interceptor.getLevel()).isEqualTo(HttpLoggingInterceptor.Level.NONE);
        Assertions.assertThat(restClient.getLogLevel(interceptor)).isEqualTo(LogLevel.NONE);
    }

    @Test
    void restCall() throws IOException {

        mockServer.enqueue(new MockResponse()
                .setBody("Begonia is a genus of perennial flowering plants in the family Begoniaceae."));

        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(serverURL)
                .client(okHttpClient)
                .build();

        ServerAPI serverAPI = retrofit.create(ServerAPI.class);
        Call<ResponseBody> call = serverAPI.findFlowerByID("Begonia");

        ResponseBody response = restClient.execute(call);
        Assertions.assertThat(response.source().readUtf8()).isEqualTo("Begonia is a genus of perennial flowering plants in the family Begoniaceae.");
    }

    @Test
    void restCallError() {

        mockServer.enqueue(createErrorResponse("flower not found"));

        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(serverURL)
                .client(okHttpClient)
                .build();

        ServerAPI serverAPI = retrofit.create(ServerAPI.class);
        Call<ResponseBody> call = serverAPI.findFlowerByID("Camellias");

        Assertions.assertThatThrownBy(() -> restClient.execute(call))
                .isInstanceOf(InfluxException.class)
                .hasMessage("flower not found");
    }
    
    @Test
    void restCallIOError() throws IOException {

        mockServer.shutdown();

        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(serverURL)
                .client(okHttpClient)
                .build();

        ServerAPI serverAPI = retrofit.create(ServerAPI.class);
        Call<ResponseBody> call = serverAPI.findFlowerByID("Camellias");

        Assertions.assertThatThrownBy(() -> restClient.execute(call))
                .isInstanceOf(InfluxException.class)
                .hasMessageStartingWith("Failed to connect to");
    }

    private void errorResponse(final int code) {
        okhttp3.Response.Builder builder = new okhttp3.Response.Builder() //
                .code(code)
                .message("Response.error()")
                .protocol(Protocol.HTTP_1_1)
                .request(new Request.Builder().url("http://localhost/").build());

        ResponseBody body = ResponseBody.create(MediaType.parse("application/json"), "");

        throw restClient.responseToError(Response.error(body, builder.build()));
    }

    private interface ServerAPI {

        @GET("/flowers/{id}")
        @Nonnull
        @Headers("Content-Type: application/text")
        Call<ResponseBody> findFlowerByID(@Nonnull @Path("id") final String flowerName);
    }
}