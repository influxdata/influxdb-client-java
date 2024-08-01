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
package com.influxdb.exceptions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.ResponseBody;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import retrofit2.HttpException;
import retrofit2.Response;

/**
 * @author Jakub Bednar (bednar@github) (02/08/2018 08:58)
 */
class InfluxExceptionTest {

    @Test
    void unExpectedError() {

        Assertions
                .assertThatThrownBy(() -> {
                    throw new InfluxException(new IllegalStateException("unExpectedError"));
                })
                .isInstanceOf(InfluxException.class)
                .hasCauseInstanceOf(IllegalStateException.class)
                .hasMessage("unExpectedError");
    }

    @Test
    void nullException() {

        Assertions
                .assertThatThrownBy(() -> {
                    throw new InfluxException((Throwable) null);
                })
                .isInstanceOf(InfluxException.class)
                .hasNoCause()
                .hasMessage(null);
    }

    @Test
    void retrofitHttpException() {

        Assertions
                .assertThatThrownBy(() -> {
                    throw new InfluxException(new HttpException(errorResponse("Wrong query")));
                })
                .isInstanceOf(InfluxException.class)
                .hasCauseInstanceOf(HttpException.class)
                .hasMessage("HTTP status code: 500; Message: Wrong query");
    }

    @Test
    void retrofitHttpExceptionEmptyError() {

        Assertions
                .assertThatThrownBy(() -> {
                    throw new InfluxException(new HttpException(errorResponse("")));
                })
                .isInstanceOf(InfluxException.class)
                .hasCauseInstanceOf(HttpException.class)
                .hasMessage("HTTP 500 Response.error()");
    }

    @Test
    void retrofitHttpExceptionNullError() {

        Assertions
                .assertThatThrownBy(() -> {
                    throw new InfluxException(new HttpException(errorResponse(null)));
                })
                .isInstanceOf(InfluxException.class)
                .hasCauseInstanceOf(HttpException.class)
                .hasMessage("HTTP 500 Response.error()");
    }

    @Test
    void statusResponse() {

        Assertions
                .assertThatThrownBy(() -> {
                    throw new InfluxException(new HttpException(errorResponse("Wrong query", 501)));
                })
                .matches((Predicate<Throwable>) throwable -> ((InfluxException) throwable).status() == 501);
    }

    @Test
    void statusResponseNull() {

        Assertions
                .assertThatThrownBy(() -> {
                    throw new InfluxException(new IllegalStateException("unExpectedError"));
                })
                .matches((Predicate<Throwable>) throwable -> ((InfluxException) throwable).status() == 0);
    }

    @Test
    void reference() {

        Assertions
                .assertThatThrownBy(() -> {
                    throw new InfluxException(new HttpException(errorResponse("Wrong query", 501, 15)));
                })
                .matches((Predicate<Throwable>) throwable -> ((InfluxException) throwable).reference() == 15);
    }

    @Test
    void referenceResponseNull() {

        Assertions
                .assertThatThrownBy(() -> {
                    throw new InfluxException(new IllegalStateException("unExpectedError"));
                })
                .matches((Predicate<Throwable>) throwable -> ((InfluxException) throwable).reference() == 0);
    }

    @Test
    void referenceResponseWithoutHeader() {

        Assertions
                .assertThatThrownBy(() -> {
                    throw new InfluxException(new HttpException(errorResponse("Wrong query")));
                })
                .matches((Predicate<Throwable>) throwable -> ((InfluxException) throwable).reference() == 0);
    }

    @Test
    void errorBody() {
        Assertions
                .assertThatThrownBy(() -> {
                    Response<Object> response = errorResponse("Wrong query", 501, 15, "{\"error\": \"error-body\"}");
                    throw new InfluxException(new HttpException(response));
                })
                .matches((Predicate<Throwable>) throwable -> ((InfluxException) throwable).errorBody().get("error").equals("error-body"));
    }

    @Test
    void errorBodyInfluxDB() {
        Assertions
                .assertThatThrownBy(() -> {
                    Response<Object> response = errorResponse("not found", 404, 15, "{\"code\":\"not found\",\"message\":\"user not found\"}", "X-Platform-Error-Code");
                    throw new InfluxException(new HttpException(response));
                })
                .matches((Predicate<Throwable>) throwable -> throwable.getMessage().equals("HTTP status code: 404; Message: user not found"));
    }

    @Test
    void errorBodyInfluxDBWithoutMsg() {
        Assertions
                .assertThatThrownBy(() -> {
                    Response<Object> response = errorResponse("not found", 404, 15, "{\"code\":\"not found\"}", "X-Platform-Error-Code");
                    throw new InfluxException(new HttpException(response));
                })
                .matches((Predicate<Throwable>) throwable -> throwable.getMessage().equals("HTTP status code: 404; Message: not found"));
    }

    @Test
    void errorBodyInfluxDBNotJson() {
        Assertions
                .assertThatThrownBy(() -> {
                    Response<Object> response = errorResponse("not found", 404, 15, "not-json", "X-Platform-Error-Code");
                    throw new InfluxException(new HttpException(response));
                })
                .matches((Predicate<Throwable>) throwable -> throwable.getMessage().equals("HTTP status code: 404; Message: not found"));
    }

    @Test
    void errorBodyReadAgain() {
        Assertions
                .assertThatThrownBy(() -> {
                    Response<Object> response = errorResponse("Wrong query", 501, 15, "{\"error\": \"error-body\"}");
                    throw new InfluxException(new HttpException(response));
                })
                .matches((Predicate<Throwable>) throwable -> {
                    Map errorBody1 = ((InfluxException) throwable).errorBody();
                    Map errorBody2 = ((InfluxException) throwable).errorBody();
                    return errorBody1.get("error").equals("error-body")
                            && errorBody1.equals(errorBody2);
                });
    }

    @Test
    void errorBodyResponseNull() {

        Assertions
                .assertThatThrownBy(() -> {
                    throw new InfluxException(new IllegalStateException("unExpectedError"));
                })
                .matches((Predicate<Throwable>) throwable -> ((InfluxException) throwable).errorBody().isEmpty());
    }

    @Test
    void errorBodyResponseWithoutBody() {

        Assertions
                .assertThatThrownBy(() -> {
                    throw new InfluxException(new HttpException(errorResponse("Wrong query")));
                })
                .matches((Predicate<Throwable>) throwable -> ((InfluxException) throwable).errorBody().isEmpty());
    }

    @Test
    void message() {

        Assertions.assertThatThrownBy(() -> {
                    throw new InfluxException("Wrong query");
                })
                .isInstanceOf(InfluxException.class)
                .hasMessage("Wrong query")
                .matches((Predicate<Throwable>) throwable -> ((InfluxException) throwable).status() == 0)
                .matches((Predicate<Throwable>) throwable -> ((InfluxException) throwable).reference() == 0)
                .matches((Predicate<Throwable>) throwable -> ((InfluxException) throwable).errorBody().isEmpty());

    }

    @Test
    void messageNull() {

        Assertions.assertThatThrownBy(() -> {
                    throw new InfluxException((String) null);
                })
                .isInstanceOf(InfluxException.class)
                .hasMessage(null)
                .matches((Predicate<Throwable>) throwable -> ((InfluxException) throwable).status() == 0)
                .matches((Predicate<Throwable>) throwable -> ((InfluxException) throwable).reference() == 0)
                .matches((Predicate<Throwable>) throwable -> ((InfluxException) throwable).errorBody().isEmpty());

    }

    @Test
    void headerValues() {
        Assertions
                .assertThatThrownBy(() -> {
                    Response<Object> response = errorResponse(
                            "not found",
                            404,
                            15,
                            "not-json",
                            "X-Platform-Error-Code",
                            Collections.singletonMap("Retry-After", "145"));
                    throw new InfluxException(new HttpException(response));
                })
                .matches((Predicate<Throwable>) throwable -> ((InfluxException) throwable).headers().size() == 3)
                .matches((Predicate<Throwable>) throwable -> ((InfluxException) throwable).headers().get("Retry-After").equals("145"))
                .matches((Predicate<Throwable>) throwable -> ((InfluxException) throwable).headers().get("X-Platform-Error-Code").equals("not found"))
                .matches((Predicate<Throwable>) throwable -> ((InfluxException) throwable).headers().get("X-Influx-Reference").equals("15"));
    }

    @Test
    void headerValuesEmpty() {
        Assertions
                .assertThatThrownBy(() -> {
                    throw new InfluxException("Wrong query");
                })
                .matches((Predicate<Throwable>) throwable -> ((InfluxException) throwable).headers().isEmpty());
    }

    @Test
    void headerValuesIgnoreCase() {
        Assertions
                .assertThatThrownBy(() -> {
                    Response<Object> response = errorResponse(
                            "not found",
                            404,
                            15,
                            "not-json",
                            "X-Platform-Error-Code",
                            Collections.singletonMap("Retry-After", "145"));
                    throw new InfluxException(new HttpException(response));
                })
                .matches((Predicate<Throwable>) throwable -> ((InfluxException) throwable).headers().get("retry-after").equals("145"));
    }

    @Test
    void nullResponse() {
        Assertions.assertThatThrownBy(() -> {
                    throw new InfluxException((Response<?>) null);
                })
                .isInstanceOf(InfluxException.class)
                .hasMessage(null)
                .matches((Predicate<Throwable>) throwable -> ((InfluxException) throwable).status() == 0)
                .matches((Predicate<Throwable>) throwable -> ((InfluxException) throwable).reference() == 0)
                .matches((Predicate<Throwable>) throwable -> ((InfluxException) throwable).errorBody().isEmpty());
    }

    @Test
    void messageContainsHttpErrorCode() {
        Assertions
                .assertThatThrownBy(() -> {
                    Response<Object> response = errorResponse("Wrong query", 501, 15, "{\"error\": \"error-body\"}");
                    throw new InfluxException(new HttpException(response));
                })
                .matches((Predicate<Throwable>) throwable -> throwable.getMessage().equals("HTTP status code: 501; Message: Wrong query"))
                .matches((Predicate<Throwable>) throwable -> throwable.toString().equals("com.influxdb.exceptions.InfluxException: HTTP status code: 501; Message: Wrong query"));
    }

    @Test
    void exceptionContainsHttpResponseHeaders() {
      Assertions.assertThatThrownBy(() -> {
        Response<Object> response = errorResponse(
          "not found",
          404,
          15,
          "not-json",
          "X-Platform-Error-Code",
          Map.of("Retry-After", "145",
            "Trace-ID", "1234567989ABCDEF0",
            "X-Influxdb-Build", "OSS"));
        throw new InfluxException(new HttpException(response));
        }
      ).matches((Predicate<Throwable>) throwable -> ((InfluxException) throwable).status() == 404)
       .matches((Predicate<Throwable>) throwable -> throwable.getMessage().equals(
         "HTTP status code: 404; Message: not found"
       ))
       .matches((Predicate<Throwable>) throwable -> ((InfluxException) throwable).headers().size() == 5)
       .matches((Predicate<Throwable>) throwable -> ((InfluxException) throwable).headers().get("Retry-After").equals("145"))
       .matches((Predicate<Throwable>) throwable -> ((InfluxException) throwable).headers().get("X-Influxdb-Build").equals("OSS"))
       .matches((Predicate<Throwable>) throwable -> ((InfluxException) throwable).headers().get("X-Influx-Reference").equals("15"))
       .matches((Predicate<Throwable>) throwable -> ((InfluxException) throwable).headers().get("X-Platform-Error-Code").equals("not found"))
       .matches((Predicate<Throwable>) throwable -> ((InfluxException) throwable).headers().get("Trace-ID").equals("1234567989ABCDEF0"));
    }

    @Nonnull
    private Response<Object> errorResponse(@Nullable final String influxError) {
        return errorResponse(influxError, 500);
    }

    @Nonnull
    private Response<Object> errorResponse(@Nullable final String influxError, final int responseCode) {
        return errorResponse(influxError, responseCode, null);
    }

    @Nonnull
    private Response<Object> errorResponse(@Nullable final String influxError, final int responseCode,
                                           @Nullable final Integer referenceCode) {
        return errorResponse(influxError, responseCode, referenceCode, "");
    }

    @Nonnull
    private Response<Object> errorResponse(@Nullable final String influxError, final int responseCode,
                                           @Nullable final Integer referenceCode,
                                           @Nonnull final String errorBody) {
        return errorResponse(influxError, responseCode, referenceCode, errorBody,
                "X-Influx-Error");
    }

    @Nonnull
    private Response<Object> errorResponse(@Nullable final String influxError,
                                           final int responseCode,
                                           @Nullable final Integer referenceCode,
                                           @Nonnull final String errorBody,
                                           @Nonnull final String errorHeaderName) {
        return errorResponse(influxError, responseCode, referenceCode, errorBody, errorHeaderName, new HashMap<>());
    }

    @Nonnull
    private Response<Object> errorResponse(@Nullable final String influxError,
                                           final int responseCode,
                                           @Nullable final Integer referenceCode,
                                           @Nonnull final String errorBody,
                                           @Nonnull final String errorHeaderName,
                                           @Nonnull final Map<String, String> headers) {

        okhttp3.Response.Builder builder = new okhttp3.Response.Builder() //
                .code(responseCode)
                .message("Response.error()")
                .protocol(Protocol.HTTP_1_1)
                .request(new Request.Builder().url("http://localhost/").build());

        if (influxError != null) {
            builder.addHeader(errorHeaderName, influxError);
        }

        if (referenceCode != null) {
            builder.addHeader("X-Influx-Reference", referenceCode.toString());
        }

        headers.forEach(builder::addHeader);

        ResponseBody body = ResponseBody.create(errorBody, MediaType.parse("application/json"));

        return Response.error(body, builder.build());
    }
}