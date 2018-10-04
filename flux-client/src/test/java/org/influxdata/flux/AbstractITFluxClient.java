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
package org.influxdata.flux;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

import org.influxdata.flux.option.FluxConnectionOptions;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * @author Jakub Bednar (bednar@github) (31/07/2018 09:35)
 */
public abstract class AbstractITFluxClient extends AbstractTest {

    private static final Logger LOG = Logger.getLogger(AbstractITFluxClient.class.getName());

    static final String DATABASE_NAME = "flux_database";

    FluxClient fluxClient;
    private String influxURL;

    @BeforeEach
    protected void setUp() {

        String fluxIP = System.getenv().getOrDefault("FLUX_IP", "127.0.0.1");
        String fluxPort = System.getenv().getOrDefault("FLUX_PORT_API", "8086");
        String fluxURL = "http://" + fluxIP + ":" + fluxPort;
        LOG.log(Level.FINEST, "Flux URL: {0}", fluxURL);

        FluxConnectionOptions options = FluxConnectionOptions.builder()
                .url(fluxURL)
                .build();

        fluxClient = FluxClientFactory.connect(options);

        String influxdbIP = System.getenv().getOrDefault("INFLUXDB_IP", "127.0.0.1");
        String influxdbPort = System.getenv().getOrDefault("INFLUXDB_PORT_API", "8086");
        influxURL = "http://" + influxdbIP + ":" + influxdbPort;
        LOG.log(Level.FINEST, "Influx URL: {0}", influxURL);

        influxDBQuery("CREATE DATABASE " + DATABASE_NAME);
    }

    @AfterEach
    protected void after() {

        influxDBQuery("DROP DATABASE " + DATABASE_NAME);
    }

    void influxDBWrite(@Nonnull final String lineProtocol) {

        Request request = new Request.Builder()
                .url(influxURL + "/write?db=" + DATABASE_NAME )
                .addHeader("accept", "application/json")
                .post(RequestBody.create(MediaType.parse("text/plain"), lineProtocol))
                .build();

        influxDBRequest(request);
    }

    private void influxDBQuery(@Nonnull final String query) {

        Request request = new Request.Builder()
                .url(influxURL + "/query?db=" + DATABASE_NAME + ";q=" + query)
                .addHeader("accept", "application/json")
                .get()
                .build();

        influxDBRequest(request);
    }

    private void influxDBRequest(@Nonnull final Request request) {

        Assertions.assertThat(request).isNotNull();

        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        Response response;
        try {
            response = okHttpClient.newCall(request).execute();
            Assertions.assertThat(response.isSuccessful()).isTrue();

            Thread.sleep(100);
        } catch (Exception e) {
            Assertions.fail("Unexpected exception", e);
        }
    }
}