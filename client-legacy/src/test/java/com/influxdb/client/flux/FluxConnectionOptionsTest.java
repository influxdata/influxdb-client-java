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

import okhttp3.OkHttpClient;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (26/06/2018 09:09)
 */
@RunWith(JUnitPlatform.class)
class FluxConnectionOptionsTest {

    @Test
    void value() {
        FluxConnectionOptions fluxConnectionOptions = FluxConnectionOptions.builder()
                .url("http://localhost:8093")
                .build();

        Assertions.assertThat(fluxConnectionOptions.getUrl()).isEqualTo("http://localhost:8093");
        Assertions.assertThat(fluxConnectionOptions.getOkHttpClient()).isNotNull();
    }

    @Test
    void urlRequired() {

        FluxConnectionOptions.Builder fluxConnectionOptions = FluxConnectionOptions.builder();

        Assertions.assertThatThrownBy(fluxConnectionOptions::build)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("The url to connect to Flux has to be defined.");
    }

    @Test
    void okHttpClientValue() {

        OkHttpClient.Builder okHttpClient = new OkHttpClient.Builder();

        FluxConnectionOptions fluxConnectionOptions = FluxConnectionOptions.builder()
                .url("http://localhost:8093")
                .okHttpClient(okHttpClient)
                .build();

        Assertions.assertThat(fluxConnectionOptions.getOkHttpClient()).isEqualTo(okHttpClient);
    }
}