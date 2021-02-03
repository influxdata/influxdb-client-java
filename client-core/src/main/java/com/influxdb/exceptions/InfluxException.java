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

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.ResponseBody;
import retrofit2.HttpException;
import retrofit2.Response;


/**
 * The base type for Influx errors.
 *
 * @author Jakub Bednar (bednar@github) (31/07/2018 11:53)
 */
public class InfluxException extends RuntimeException {

    private static final Logger LOG = Logger.getLogger(InfluxException.class.getName());

    private final Response<?> response;
    private final String message;

    private JsonObject errorBody = new JsonObject();// = new JsonNode();

    public InfluxException(@Nullable final String message) {

        this.response = null;
        this.message = message;
    }

    public InfluxException(@Nullable final Throwable cause) {

        super(cause);

        if (cause instanceof HttpException) {
            this.response = ((HttpException) cause).response();
        } else {
            this.response = null;
        }

        this.message = messageFromResponse();
    }

    public InfluxException(@Nullable final Response<?> cause) {

        super();
        this.response = cause;
        this.message = messageFromResponse();
    }

    @Override
    public String getMessage() {
        return message;
    }

    /**
     * Gets the reference code unique to the error type. If the reference code is not present than return "0".
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
     * If the response is not present than return "0".
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
     * The JSON unsuccessful response body.
     *
     * @return a response body
     */
    @Nonnull
    public JsonObject errorBody() {

        return errorBody;
    }

    @Nullable
    private String messageFromResponse() {
        if (response != null) {
            try {
                ResponseBody body = response.errorBody();
                if (body != null) {
                    String json = body.source().readUtf8();
                    if (!json.isEmpty()) {
                        errorBody = new JsonParser().parse(json).getAsJsonObject();
                        if (errorBody.has("message")) {
                            return errorBody.get("message").getAsString();
                        }
                    }
                }
            } catch (Exception e) {
                LOG.log(Level.FINEST, "Can't parse msg from response {}", response);
            }

            String value = Stream.of("X-Platform-Error-Code", "X-Influx-Error", "X-InfluxDb-Error")
                    .map(name -> response.headers().get(name))
                    .filter(message -> message != null && !message.isEmpty()).findFirst()
                    .orElse(null);

            if (value != null) {
                return value;
            }
        }

        Throwable cause = getCause();
        if (cause != null) {
            return cause.getMessage();
        }

        return null;
    }
}
