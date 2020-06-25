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

import java.io.IOException;
import java.io.StringWriter;
import java.time.OffsetDateTime;

import com.google.gson.stream.JsonWriter;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Jakub Bednar (25/06/2020 13:47)
 */
public class OffsetDateTimeTypeAdapterTest {

    @Test
    public void max() throws IOException {
        JSON.OffsetDateTimeTypeAdapter offsetDateTimeTypeAdapter = new JSON.OffsetDateTimeTypeAdapter();

        StringWriter writer = new StringWriter();
        offsetDateTimeTypeAdapter.write(new JsonWriter(writer), OffsetDateTime.MAX);

        Assertions.assertThat(writer.toString()).isEqualTo("\"+999999999-12-31T23:59:59.999999999-18:00\"");
    }

    @Test
    public void min() throws IOException {
        JSON.OffsetDateTimeTypeAdapter offsetDateTimeTypeAdapter = new JSON.OffsetDateTimeTypeAdapter();

        StringWriter writer = new StringWriter();
        offsetDateTimeTypeAdapter.write(new JsonWriter(writer), OffsetDateTime.MIN);

        Assertions.assertThat(writer.toString()).isEqualTo("\"-999999999-01-01T00:00:00+18:00\"");
    }
}
