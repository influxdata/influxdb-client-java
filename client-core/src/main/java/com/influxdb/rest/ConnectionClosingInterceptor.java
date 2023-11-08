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
package com.influxdb.rest;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

import okhttp3.Call;
import okhttp3.Connection;
import okhttp3.EventListener;
import okhttp3.Interceptor;
import okhttp3.Response;
import okhttp3.internal.connection.RealConnection;

/**
 * This interceptor closes connections that exceed a specified maximum age.
 * It's beneficial for scenarios where your application requires establishing new connections to the same host after a predetermined interval.
 * This interceptor is most effective in applications that use a single connection, meaning requests are not made in parallel.
 * <p>
 * Caution is advised, as setting a very short interval can lead to performance issues because establishing new connections is a resource-intensive operation.
 */
public class ConnectionClosingInterceptor extends EventListener implements Interceptor {

    private static final Logger LOG = Logger.getLogger(ConnectionClosingInterceptor.class.getName());

    private final ConcurrentMap<Connection, Long> connectionTimes = new ConcurrentHashMap<>();
    private final long connectionMaxAgeMillis;

    /**
     * Create a new interceptor that will close connections older than the given max age.
     *
     * @param connectionMaxAge the max age of connections, the precision is milliseconds
     */
    public ConnectionClosingInterceptor(@Nonnull final Duration connectionMaxAge) {
        this.connectionMaxAgeMillis = connectionMaxAge.toMillis();
    }

    @Override
    @Nonnull
    public Response intercept(@Nonnull final Chain chain) throws IOException {
        Connection connection = chain.connection();

        //
        // If the connection is old, mark it to not be reused.
        //
        if (connection != null && isConnectionOld(connection)) {
            if (connection instanceof RealConnection) {
                LOG.fine("Marking connection to not be reused: " + connection);
                ((RealConnection) connection).noNewExchanges$okhttp();
            } else {
                LOG.warning("Unable to mark connection to not be reused: " + connection);
            }
        }

        return chain.proceed(chain.request());
    }

    @Override
    public void connectionAcquired(@Nonnull final Call call, @Nonnull final Connection connection) {
        connectionTimes.putIfAbsent(connection, System.currentTimeMillis());
    }

    /**
     * Check if the connection is older than the max age.
     *
     * @param connection the connection to check
     * @return true if the connection is older than the max age
     */
    private boolean isConnectionOld(@Nonnull final Connection connection) {
        Long time = connectionTimes.get(connection);
        return (time != null && (System.currentTimeMillis() - time) > connectionMaxAgeMillis);
    }
}
