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

package com.influxdb.query.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.Instant;
import java.util.Map;

import com.influxdb.query.FluxColumn;
import com.influxdb.query.FluxRecord;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FluxStructureSerializationTest {

    @Test
    public void testFluxColumnSerialization() throws IOException, ClassNotFoundException {
        FluxColumn source = new FluxColumn();
        source.setDataType("dateTime:RFC3339");
        source.setDefaultValue("val1");
        source.setGroup(true);
        source.setIndex(1);
        source.setLabel("my-column");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new ObjectOutputStream(out).writeObject(source);

        Object copy = new ObjectInputStream(new ByteArrayInputStream(out.toByteArray())).readObject();

        Assertions.assertEquals(source.hashCode(), copy.hashCode());
        Assertions.assertEquals(source, copy);
    }

    @Test
    public void testFluxRecordSerialization()  throws IOException, ClassNotFoundException {
        FluxRecord source = new FluxRecord(1);
        Map<String, Object> values = source.getValues();
        values.put("val1", 1);
        values.put("val2", Instant.now());
        values.put("val3", "my-string");
        values.put("val4", 3.14f);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new ObjectOutputStream(out).writeObject(source);

        FluxRecord copy = (FluxRecord) new ObjectInputStream(new ByteArrayInputStream(out.toByteArray())).readObject();

        Assertions.assertEquals(source.getValues(), copy.getValues());
        Assertions.assertEquals(source.hashCode(), copy.hashCode());
        Assertions.assertEquals(source, copy);
    }

}
