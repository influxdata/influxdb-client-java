package org.influxdata.exceptions;

import javax.annotation.Nullable;

import retrofit2.Response;

/**
 * The exception is thrown if a HTTP 413 response code arrived - Request Entity Too Large.
 *
 * @author Jakub Bednar (bednar@github) (04/03/2019 11:37)
 */
public class RequestEntityTooLargeException extends InfluxException {
    public RequestEntityTooLargeException(@Nullable final Response<?> cause) {
        super(cause);
    }
}