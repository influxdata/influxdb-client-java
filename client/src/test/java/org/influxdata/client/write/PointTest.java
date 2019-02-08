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
package org.influxdata.client.write;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (11/10/2018 12:57)
 */
@RunWith(JUnitPlatform.class)
class PointTest {

    @Test
    void measurementEscape() {

        Point point = Point.measurement("h2 o")
                .addTag("location", "europe")
                .addTag("", "warn")
                .addField("level", 2);

        Assertions.assertThat(point.toLineProtocol()).isEqualTo("h2\\ o,location=europe level=2i");

        point = Point.measurement("h2=o")
                .addTag("location", "europe")
                .addTag("", "warn")
                .addField("level", 2);

        Assertions.assertThat(point.toLineProtocol()).isEqualTo("h2\\=o,location=europe level=2i");

        point = Point.measurement("h2,o")
                .addTag("location", "europe")
                .addTag("", "warn")
                .addField("level", 2);

        Assertions.assertThat(point.toLineProtocol()).isEqualTo("h2\\,o,location=europe level=2i");
    }

    @Test
    void tagEmptyKey() {

        Point point = Point.measurement("h2o")
                .addTag("location", "europe")
                .addTag("", "warn")
                .addField("level", 2);

        Assertions.assertThat(point.toLineProtocol()).isEqualTo("h2o,location=europe level=2i");
    }

    @Test
    void tagEmptyValue() {

        Point point = Point.measurement("h2o")
                .addTag("location", "europe")
                .addTag("log", "")
                .addField("level", 2);

        Assertions.assertThat(point.toLineProtocol()).isEqualTo("h2o,location=europe level=2i");
    }

    @Test
    void fieldTypes() {

        Point point = Point.measurement("h2o").addTag("location", "europe")
                .addField("long", 1L)
                .addField("double", 2D)
                .addField("float", 3F)
                .addField("longObject", Long.valueOf("4"))
                .addField("doubleObject", Double.valueOf("5"))
                .addField("floatObject", Float.valueOf("6"))
                .addField("bigDecimal", new BigDecimal("33.45"))
                .addField("integer", 7)
                .addField("integerObject", Integer.valueOf("8"))
                .addField("boolean", false)
                .addField("booleanObject", Boolean.valueOf("true"))
                .addField("string", "string value");

        String expected = "h2o,location=europe bigDecimal=33.45,boolean=false,booleanObject=true,double=2.0,doubleObject=5.0,"
                + "float=3.0,floatObject=6.0,integer=7i,integerObject=8i,long=1i,longObject=4i,string=\"string value\"";
        Assertions.assertThat(point.toLineProtocol()).isEqualTo(expected);
    }

    @Test
    void fieldNullValue() {

        Point point = Point.measurement("h2o").addTag("location", "europe").addField("level", 2)
                .addField("warning", (String) null);

        Assertions.assertThat(point.toLineProtocol()).isEqualTo("h2o,location=europe level=2i");
    }

    @Test
    void fieldEscape() {

        Point point = Point.measurement("h2o")
                .addTag("location", "europe")
                .addField("level", "string esc\\ape value");

        Assertions.assertThat(point.toLineProtocol()).isEqualTo("h2o,location=europe level=\"string esc\\\\ape value\"");

        point = Point.measurement("h2o")
                .addTag("location", "europe")
                .addField("level", "string esc\"ape value");

        Assertions.assertThat(point.toLineProtocol()).isEqualTo("h2o,location=europe level=\"string esc\\\"ape value\"");
    }

    @Test
    void time() {

        Point point = Point.measurement("h2o")
                .addTag("location", "europe")
                .addField("level", 2)
                .time(123L, ChronoUnit.SECONDS);

        Assertions.assertThat(point.toLineProtocol()).isEqualTo("h2o,location=europe level=2i 123");
    }

    @Test
    void timePrecisionDefault() {

        Point point = Point.measurement("h2o")
                .addTag("location", "europe")
                .addField("level", 2);

        Assertions.assertThat(point.getPrecision()).isEqualTo(ChronoUnit.NANOS);
    }

    @Test
    void timePrecisionNotSupported() {

        Point point = Point.measurement("h2o")
                .addTag("location", "europe")
                .addField("level", 2);


        Assertions.assertThatThrownBy(() -> point.time(123L, ChronoUnit.DAYS))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Precision must be one of: [Nanos, Micros, Millis, Seconds]");
    }

    @Test
    void timeInstantNull() {

        Point point = Point.measurement("h2o")
                .addTag("location", "europe")
                .addField("level", 2)
                .time((Instant) null, ChronoUnit.SECONDS);

        Assertions.assertThat(point.toLineProtocol()).isEqualTo("h2o,location=europe level=2i");
    }
}