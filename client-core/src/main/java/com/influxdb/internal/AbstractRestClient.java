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
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import com.influxdb.utils.Arguments;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Response;

/**
 * @author Jakub Bednar (bednar@github) (04/10/2018 07:50)
 */
public abstract class AbstractRestClient {

    private static final Logger LOG = Logger.getLogger(AbstractRestClient.class.getName());
    private static final MediaType CONTENT_TYPE_JSON = MediaType.parse("application/json");

    @Nonnull
    RequestBody createBody(@Nonnull final String content) {

        Arguments.checkNonEmpty(content, "content");

        return RequestBody.create(CONTENT_TYPE_JSON, content);
    }

    protected <T> T execute(@Nonnull final Call<T> call) throws InfluxException {
        Arguments.checkNotNull(call, "call");

        try {
            Response<T> response = call.execute();
            if (response.isSuccessful()) {
                return response.body();
            } else {

                throw responseToError(response);
            }
        } catch (IOException e) {
            throw new InfluxException(e);
        }
    }

    @Nonnull
    @SuppressWarnings("MagicNumber")
    protected InfluxException responseToError(@Nonnull final Response<?> response) {

        Arguments.checkNotNull(response, "response");

        switch (response.code()) {
            case 400:
                return new BadRequestException(response);
            case 401:
                return new UnauthorizedException(response);
            case 402:
                return new PaymentRequiredException(response);
            case 403:
                return new ForbiddenException(response);
            case 404:
                return new NotFoundException(response);
            case 405:
                return new MethodNotAllowedException(response);
            case 406:
                return new NotAcceptableException(response);
            case 407:
                return new ProxyAuthenticationRequiredException(response);
            case 408:
                return new RequestTimeoutException(response);
            case 413:
                return new RequestEntityTooLargeException(response);
            case 422:
                return new UnprocessableEntityException(response);
            case 500:
                return new InternalServerErrorException(response);
            case 501:
                return new NotImplementedException(response);
            case 502:
                return new BadGatewayException(response);
            case 503:
                return new ServiceUnavailableException(response);
            default:
                return new InfluxException(response);
        }
    }

    void catchOrPropagateException(@Nonnull final Exception exception,
                                   @Nonnull final Consumer<? super Throwable> onError) {

        Arguments.checkNotNull(exception, "exception");
        Arguments.checkNotNull(onError, "onError");

        //
        // Socket closed by remote server or end of data
        //
        if (isCloseException(exception)) {
            LOG.log(Level.FINEST, "Socket closed by remote server or end of data", exception);
        } else {
            onError.accept(exception);
        }
    }

    protected void setLogLevel(@Nonnull final HttpLoggingInterceptor interceptor, @Nonnull final LogLevel logLevel) {

        Arguments.checkNotNull(logLevel, "LogLevel");
        Arguments.checkNotNull(interceptor, "HttpLogging interceptor");

        interceptor.setLevel(HttpLoggingInterceptor.Level.valueOf(logLevel.name()));
    }

    @Nonnull
    protected LogLevel getLogLevel(@Nonnull final HttpLoggingInterceptor interceptor) {

        Arguments.checkNotNull(interceptor, "HttpLogging interceptor");

        return LogLevel.valueOf(interceptor.getLevel().name());
    }

    private boolean isCloseException(@Nonnull final Exception exception) {

        Arguments.checkNotNull(exception, "exception");

        return exception instanceof EOFException;
    }

    @Nonnull
    protected Boolean ping(@Nonnull final Call<ResponseBody> responseBody) {

        Arguments.checkNotNull(responseBody, "responseBody");

        try {
            return responseBody.execute().isSuccessful();
        } catch (IOException e) {

            LOG.log(Level.WARNING, "Ping request wasn't successful", e);
            return false;
        }
    }

    @Nonnull
    protected String version(@Nonnull final Call<ResponseBody> ping) {
        try {
            String version = ping.execute().headers().get("X-Influxdb-Version");
            if (version != null) {
                return version;
            }

            return "unknown";
        } catch (IOException e) {
            throw new InfluxException(e);
        }
    }
}