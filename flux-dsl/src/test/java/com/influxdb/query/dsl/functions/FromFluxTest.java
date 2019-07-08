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
package com.influxdb.query.dsl.functions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.influxdb.query.dsl.Flux;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (22/06/2018 12:04)
 */
@RunWith(JUnitPlatform.class)
class FromFluxTest {

    @Test
    void database() {

        Flux flux = Flux.from("telegraf");

        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace("from(bucket:\"telegraf\")");
    }

    @Test
    void databaseRequired() {

        Assertions.assertThatThrownBy(() -> FromFlux.from(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Expecting a non-empty string for Bucket name");
    }

    @Test
    void hostCollection() {

        List<String> hosts = new ArrayList<>();
        hosts.add("fluxdHost");
        hosts.add("192.168.1.100");

        Flux flux = Flux.from("telegraf", hosts);

        Assertions.assertThat(flux.toString())
                .isEqualToIgnoringWhitespace("from(bucket:\"telegraf\", hosts:[\"fluxdHost\", \"192.168.1.100\"])");
    }

    @Test
    void hostCollectionEmpty() {

        Flux flux = Flux.from("telegraf", new ArrayList<>());

        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace("from(bucket:\"telegraf\")");
    }

    @Test
    void hostCollectionRequired() {

        Assertions.assertThatThrownBy(() -> FromFlux.from("telegraf", (Collection<String>) null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Expecting a not null reference for Hosts are required");
    }

    @Test
    void hostArray() {

        Flux flux = Flux.from("telegraf", new String[]{"fluxdHost", "192.168.1.100"});

        Assertions.assertThat(flux.toString())
                .isEqualToIgnoringWhitespace("from(bucket:\"telegraf\", hosts:[\"fluxdHost\", \"192.168.1.100\"])");
    }

    @Test
    void hostArrayEmpty() {

        Flux flux = Flux.from("telegraf", new String[]{});

        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace("from(bucket:\"telegraf\")");
    }

    @Test
    void hostArrayRequired() {

        Assertions.assertThatThrownBy(() -> FromFlux.from("telegraf", (String[]) null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Expecting a not null reference for Hosts are required");
    }
}