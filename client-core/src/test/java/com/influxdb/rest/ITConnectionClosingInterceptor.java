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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

import okhttp3.Call;
import okhttp3.Connection;
import okhttp3.EventListener;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import org.assertj.core.api.Assertions;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.influxdb.test.AbstractMockServerTest;

class ITConnectionClosingInterceptor extends AbstractMockServerTest {

    private static final Logger LOG = Logger.getLogger(ITConnectionClosingInterceptor.class.getName());

    private String url;
    private OkHttpClient client;
    private ConnectionsListener connectionsListener;

    @BeforeEach
    void setUp() {
        connectionsListener = new ConnectionsListener();
        url = startMockServer();
    }

    @AfterEach
    void tearDown() {
        client.connectionPool().evictAll();
        client.dispatcher().executorService().shutdown();
    }

    @Test
    public void withoutTTLonConnection() throws Exception {

        client = new OkHttpClient.Builder()
                .eventListener(connectionsListener)
                .build();

        callApi(5, 3);

        Assertions.assertThat(connectionsListener.connections).hasSize(1);
        Assertions.assertThat(client.connectionPool().connectionCount()).isEqualTo(1);
    }

    @Test
    public void withTTLonConnection() throws Exception {

        // Use connection TTL of 2 second
        ConnectionClosingInterceptor interceptor = new ConnectionClosingInterceptor(Duration.ofSeconds(2)) {

            @Override
            public void connectionAcquired(@NotNull Call call, @NotNull Connection connection) {
                super.connectionAcquired(call, connection);

                // count the number of connections, the okhttp client can have only one listener => we have to use this
                connectionsListener.connections.add(connection);
            }
        };

        client = new OkHttpClient.Builder()
                .addNetworkInterceptor(interceptor)
                .eventListener(interceptor)
                .protocols(Collections.singletonList(Protocol.HTTP_1_1))
                .build();

        callApi(5, 3);

        Assertions.assertThat(connectionsListener.connections).hasSize(3);
        Assertions.assertThat(client.connectionPool().connectionCount()).isEqualTo(1);
    }

    /**
     * Call API by specified times.
     *
     * @param times        the number of times to call API
     * @param sleepSeconds the number of seconds to sleep between calls
     * @throws IOException if an error occurs
     */
    private void callApi(final int times, final int sleepSeconds) throws Exception {
        for (int i = 0; i < times; i++) {
            mockServer.enqueue(createResponse(""));

            Request request = new Request.Builder()
                    .url(url)
                    .build();

            LOG.info(String.format("Calling API %d", i));
            try (Response response = client.newCall(request).execute()) {
                Assertions.assertThat(response.isSuccessful()).isTrue();
            }

            LOG.info(String.format("Sleeping %d seconds; connection counts: %d", sleepSeconds, connectionsListener.connections.size()));
            Thread.sleep(sleepSeconds * 1000L);
        }
    }

    /**
     * Event listener that store acquired connections.
     */
    private static class ConnectionsListener extends EventListener {
        private final Set<Connection> connections = new HashSet<>();

        @Override
        public void connectionAcquired(@Nonnull final Call call, @Nonnull final Connection connection) {
            connections.add(connection);
        }
    }
}
