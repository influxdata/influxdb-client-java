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
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import com.google.gson.JsonIOException;
import com.google.gson.stream.JsonWriter;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Jakub Bednar (25/06/2020 13:47)
 */
public class OffsetDateTimeTypeAdapterTest {

    private JSON.OffsetDateTimeTypeAdapter adapter;
    private StringWriter writer;

    @BeforeEach
    void beforeEach() {
        adapter = new JSON.OffsetDateTimeTypeAdapter();
        writer = new StringWriter();
    }

    @Test
    public void max() throws IOException {
        OffsetDateTime time = LocalDateTime.of(9999, 12, 31, 23, 59)
                .atOffset(ZoneOffset.UTC);
        adapter.write(new JsonWriter(writer), time);

        Assertions.assertThat(writer.toString()).isEqualTo("\"9999-12-31T23:59:00Z\"");
    }

    @Test
    public void max_over() {

        OffsetDateTime time = LocalDateTime.of(10_000, 12, 31, 23, 59)
                .atOffset(ZoneOffset.UTC);

        Assertions.assertThatThrownBy(() -> adapter.write(new JsonWriter(writer), time))
                .isInstanceOf(JsonIOException.class)
                .hasMessage("OffsetDateTime is out of range. All dates and times are assumed to be in the "
                        + "\"current era\", somewhere between 0000AD and 9999AD.");
    }

    @Test
    public void max_offset() {

        Assertions.assertThatThrownBy(() -> adapter.write(new JsonWriter(writer), OffsetDateTime.MAX))
                .isInstanceOf(JsonIOException.class)
                .hasMessage("OffsetDateTime is out of range. All dates and times are assumed to be in the "
                        + "\"current era\", somewhere between 0000AD and 9999AD.");
    }

    @Test
    public void min() throws IOException {
        OffsetDateTime time = LocalDateTime.of(0, 1, 1, 0, 0)
                .atOffset(ZoneOffset.UTC);
        adapter.write(new JsonWriter(writer), time);

        Assertions.assertThat(writer.toString()).isEqualTo("\"0000-01-01T00:00:00Z\"");
    }

    @Test
    public void min_over() {
        OffsetDateTime time = LocalDateTime.of(-1, 1, 1, 0, 0)
                .atOffset(ZoneOffset.UTC);
        Assertions.assertThatThrownBy(() -> adapter.write(new JsonWriter(writer), time))
                .isInstanceOf(JsonIOException.class)
                .hasMessage("OffsetDateTime is out of range. All dates and times are assumed to be in the "
                        + "\"current era\", somewhere between 0000AD and 9999AD.");

    }

    @Test
    public void min_offset() {
        Assertions.assertThatThrownBy(() -> adapter.write(new JsonWriter(writer), OffsetDateTime.MIN))
                .isInstanceOf(JsonIOException.class)
                .hasMessage("OffsetDateTime is out of range. All dates and times are assumed to be in the "
                        + "\"current era\", somewhere between 0000AD and 9999AD.");

    }
}
