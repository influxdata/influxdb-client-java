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
package com.influxdb.client.write;

import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (28/06/2019 09:35)
 */
@RunWith(JUnitPlatform.class)
class PointSettingsTest {

    private PointSettings defaults;

    @BeforeEach
    void before() {
        defaults = new PointSettings();
    }

    @Test
    void defaultTagsEmpty() {

        Map<String, String> defaultTags = defaults.getDefaultTags();

        Assertions.assertThat(defaultTags).isNotNull();
    }

    @Test
    void defaultTagsWithoutValues() {

        defaults.addDefaultTag("id", "132-987-655")
                .addDefaultTag("customer", "California Miner")
                .addDefaultTag("hostname", "${env.hostname}")
                .addDefaultTag("sensor-version", "${version}");

        Map<String, String> defaultTags = defaults.getDefaultTags();

        Assertions.assertThat(defaultTags)
                .hasSize(2)
                .hasEntrySatisfying("id", value -> Assertions.assertThat(value).isEqualTo("132-987-655"))
                .hasEntrySatisfying("customer", value -> Assertions.assertThat(value).isEqualTo("California Miner"));
    }

    @Test
    void defaultTagsValues() {

        System.setProperty("mine-sensor.version", "1.23a");
        String envKey = (String) System.getenv().keySet().toArray()[5];

        defaults.addDefaultTag("id", "132-987-655")
                .addDefaultTag("customer", "California Miner")
                .addDefaultTag("env-variable", "${env." + envKey + "}")
                .addDefaultTag("sensor-version", "${mine-sensor.version}");

        Map<String, String> defaultTags = defaults.getDefaultTags();

        Assertions.assertThat(defaultTags).hasSize(4)
                .hasEntrySatisfying("id", value -> Assertions.assertThat(value).isEqualTo("132-987-655"))
                .hasEntrySatisfying("customer", value -> Assertions.assertThat(value).isEqualTo("California Miner"))
                .hasEntrySatisfying("sensor-version", value -> Assertions.assertThat(value).isEqualTo("1.23a"))
                .hasEntrySatisfying("env-variable", value -> Assertions.assertThat(value).isEqualTo(System.getenv().get(envKey)));
    }

    @Test
    void defaultTagsExpressionNull() {

        defaults.addDefaultTag("id", "132-987-655")
                .addDefaultTag("customer", "California Miner")
                .addDefaultTag("env-variable", null) ;

        Map<String, String> defaultTags = defaults.getDefaultTags();

        Assertions.assertThat(defaultTags).hasSize(2)
                .hasEntrySatisfying("id", value -> Assertions.assertThat(value).isEqualTo("132-987-655"))
                .hasEntrySatisfying("customer", value -> Assertions.assertThat(value).isEqualTo("California Miner"));
    }
}