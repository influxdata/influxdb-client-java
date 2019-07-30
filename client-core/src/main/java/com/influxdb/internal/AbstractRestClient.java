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
import javax.annotation.Nullable;

import com.influxdb.Arguments;
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

import okhttp3.MediaType;
import okhttp3.RequestBody;
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
    protected RequestBody createBody(@Nonnull final String content) {

        Arguments.checkNonEmpty(content, "content");

        return RequestBody.create(CONTENT_TYPE_JSON, content);
    }

    protected <T> T execute(@Nonnull final Call<T> call) throws InfluxException {
        return execute(call, (String) null);
    }

    //
    // TODO null catch can be only used for "search" not for getByID
    //
    protected <T> T execute(@Nonnull final Call<T> call, @Nullable final String nullError) throws InfluxException {
        return execute(call, nullError, null, null);
    }

    protected <T, E extends InfluxException> T execute(@Nonnull final Call<T> call,
                                                       @Nullable final Class<E> nullType) throws InfluxException {
        return execute(call, nullType, null);
    }

    protected <T, E extends InfluxException> T execute(@Nonnull final Call<T> call,
                                                       @Nullable final Class<E> nullType,
                                                       @Nullable final T defaultValue) throws InfluxException {

        return execute(call, null, nullType, defaultValue);
    }

    private <T, E extends InfluxException> T execute(@Nonnull final Call<T> call,
                                                     @Nullable final String nullError,
                                                     @Nullable final Class<E> nullType,
                                                     @Nullable final T defaultValue) throws InfluxException {

        Arguments.checkNotNull(call, "call");

        try {
            Response<T> response = call.execute();
            if (response.isSuccessful()) {
                return response.body();
            } else {

                InfluxException ie = responseToError(response);

                //
                // The error message signal not found on the server => return null
                //
                boolean nullByMessage = nullError != null && (nullError.equals(ie.getMessage())
                        ||
                        //TODO set user password -> https://github.com/influxdata/influxdb/issues/11590
                        (ie.errorBody().has("error") && ie.errorBody().getString("error").equals(nullError)));
                boolean nullByType = nullType != null && nullType.isAssignableFrom(ie.getClass());

                if (nullByMessage || nullByType) {

                    LOG.log(Level.FINEST, "Error is considered as null response.", ie);

                    return defaultValue;
                }

                throw ie;
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

    protected void catchOrPropagateException(@Nonnull final Exception exception,
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

    protected boolean isCloseException(@Nonnull final Exception exception) {

        Arguments.checkNotNull(exception, "exception");

        return exception instanceof EOFException;
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
}