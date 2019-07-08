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
package com.influxdb.client.flux;

import com.influxdb.LogLevel;

import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

@RunWith(JUnitPlatform.class)
class TestConnectionString extends AbstractITFluxClient {
    @Test
    void connectionStringTest() {

        FluxConnectionOptions options =
            FluxConnectionOptions.builder("http://localhost:8086/context/app?" +
                "readTimeout=1000&writeTimeout=3000&connectTimeout=2000&logLevel=HEADERS").build();

        OkHttpClient.Builder okHttpClient = options.getOkHttpClient();
        OkHttpClient client = okHttpClient.build();

        Assertions.assertEquals(1000, client.readTimeoutMillis());
        Assertions.assertEquals(3000, client.writeTimeoutMillis());
        Assertions.assertEquals(2000, client.connectTimeoutMillis());

        FluxClient fluxClient = FluxClientFactory.create(options);
        Assertions.assertEquals(LogLevel.HEADERS, fluxClient.getLogLevel());

    }
}
