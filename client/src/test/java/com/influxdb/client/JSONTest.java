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
package com.influxdb.client;

import com.influxdb.client.domain.CheckStatusLevel;
import com.influxdb.client.domain.Threshold;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class JSONTest {
    @Test
    public void serializeUnknownDiscriminator() {
        DummyThreshold dummyThreshold = new DummyThreshold();
        dummyThreshold.setLevel(CheckStatusLevel.OK);

        Gson gson = JSON.createGson().create();
        String json = gson.toJson(dummyThreshold);
        Assertions.assertThat(json).isEqualTo("{\"type\":\"dummy\",\"value\":15,\"level\":\"OK\"}");
    }

    @Test
    public void deSerializeUnknownDiscriminator() {
        Gson gson = JSON.createGson().create();
        Assertions.assertThatThrownBy(() -> {
                    gson.fromJson("{\"type\":\"not_registered\",\"value\":16,\"level\":\"OK\"}", Threshold.class);
                })
                .isInstanceOf(JsonParseException.class)
                .hasMessage("Cannot find model: 'not_registered' for discriminator: "
                        + "'class com.influxdb.client.domain.Threshold'. The discriminator wasn't registered.");
    }

    @SuppressWarnings("unused")
    public static class DummyThreshold extends Threshold {
        @SerializedName("type")
        private String type = "dummy";

        @SerializedName("value")
        private Integer value = 15;
    }
}
