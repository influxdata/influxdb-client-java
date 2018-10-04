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
package org.influxdata.platform.error;

import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import okhttp3.ResponseBody;
import retrofit2.HttpException;
import retrofit2.Response;


/**
 * The base type for Influx errors.
 *
 * @author Jakub Bednar (bednar@github) (31/07/2018 11:53)
 */
public class InfluxException extends RuntimeException {

    private final Response<?> response;

    public InfluxException(@Nullable final String message) {

        super(message);
        this.response = null;
    }

    public InfluxException(@Nullable final Throwable cause) {

        super(errorMessage(cause), cause);

        if (cause instanceof HttpException) {
            response = ((HttpException) cause).response();
        } else {
            response = null;
        }
    }

    public InfluxException(@Nullable final Response<?> cause) {

        super((errorMessage(cause)));
        response = cause;
    }

    /**
     * Gets the reference code unique to the error type. It the reference code is not present than return "0".
     *
     * @return reference code unique to the error type
     */
    public int reference() {

        String reference = null;
        if (response != null) {
            reference = this.response.headers().get("X-Influx-Reference");
        }

        if (reference != null) {
            return Integer.valueOf(reference);
        }

        return 0;
    }

    /**
     * Gets the HTTP status code of the unsuccessful response.
     * It the response is not present than return "0".
     *
     * @return HTTP status code
     */
    public int status() {

        if (response != null) {
            return response.code();
        }

        return 0;
    }

    /**
     * The raw unsuccessful response body.
     *
     * @return a response body
     */
    @Nonnull
    public String errorBody() throws IOException {

        if (response != null) {
            ResponseBody body = response.errorBody();
            if (body == null) {
                return "";
            }
            return body.source().readUtf8();
        }

        return "";
    }

    @Nullable
    private static String errorMessage(@Nullable final Throwable cause) {

        if (cause == null) {
            return "";
        }

        if (cause instanceof HttpException) {
            Response<?> response = ((HttpException) cause).response();
            String message = errorMessage(response);
            if (message != null) {
                return message;
            }
        }

        return cause.getMessage();
    }

    @Nullable
    private static String errorMessage(@Nullable final Response<?> response) {

        if (response == null) {
            return null;
        }

        String message = response.headers().get("X-Influx-Error");
        if (message != null && !message.isEmpty()) {
            return message;
        }

        return null;
    }
}
