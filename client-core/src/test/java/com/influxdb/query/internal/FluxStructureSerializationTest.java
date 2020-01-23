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
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

@RunWith(JUnitPlatform.class)
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

        System.out.println("source = " + source);
        System.out.println("copy = " + copy);

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

        System.out.println("source = " + source);
        System.out.println("copy = " + copy);

        Assertions.assertEquals(source.getValues(), copy.getValues());
        Assertions.assertEquals(source.hashCode(), copy.hashCode());
        Assertions.assertEquals(source, copy);
    }

}
