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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.HashMap;

import com.influxdb.client.domain.WritePrecision;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Jakub Bednar (bednar@github) (11/10/2018 12:57)
 */
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

        Assertions.assertThat(point.toLineProtocol()).isEqualTo("h2=o,location=europe level=2i");

        point = Point.measurement("h2,o")
                .addTag("location", "europe")
                .addTag("", "warn")
                .addField("level", 2);

        Assertions.assertThat(point.toLineProtocol()).isEqualTo("h2\\,o,location=europe level=2i");
    }

    @Test
    public void createByConstructor() {
        Point point = new Point("h2o")
                .addTag("location", "europe")
                .addField("level", 2);

        Assertions.assertThat(point.toLineProtocol()).isEqualTo("h2o,location=europe level=2i");
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void createByConstructorMeasurementRequired() {
        Assertions.assertThatThrownBy(() -> new Point(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Expecting a not null reference for measurement");
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
    void tagNullValue() {

        Point point = Point.measurement("h2o")
                .addTag("location", "europe")
                .addTag("log", null)
                .addField("level", 2);

        Assertions.assertThat(point.toLineProtocol()).isEqualTo("h2o,location=europe level=2i");
    }

    @Test
    public void tagEscapingKeyAndValue() {

        Point point = Point.measurement("h\n2\ro\t_data")
                .addTag("new\nline", "new\nline")
                .addTag("carriage\rreturn", "carriage\rreturn")
                .addTag("t\tab", "t\tab")
                .addField("level", 2);

        Assertions.assertThat(point.toLineProtocol())
                .isEqualTo("h\\n2\\ro\\t_data,carriage\\rreturn=carriage\\rreturn,new\\nline=new\\nline,t\\tab=t\\tab level=2i");
    }

    @Test
    public void equalSignEscaping() {

        Point point = Point.measurement("h=2o")
                .addTag("l=ocation", "e=urope")
                .addField("l=evel", 2);

        Assertions.assertThat(point.toLineProtocol())
                .isEqualTo("h=2o,l\\=ocation=e\\=urope l\\=evel=2i");
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
                .addField("booleanObject", Boolean.TRUE)
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
    void tagEscape() {
        Point point = Point.measurement("h2o")
                .addTag("location", "\\")
                .addTag("zone", "europe")
                .addField("level", "dummy value");

        Assertions.assertThat(point.toLineProtocol()).isEqualTo("h2o,location=\\\\,zone=europe level=\"dummy value\"");
    }

    @Test
    void time() {

        Point point = Point.measurement("h2o")
                .addTag("location", "europe")
                .addField("level", 2)
                .time(123L, WritePrecision.S);

        Assertions.assertThat(point.toLineProtocol()).isEqualTo("h2o,location=europe level=2i 123");
    }

    @Test
    void timeBigInteger() {

        Point point = Point.measurement("h2o")
                .addTag("location", "europe")
                .addField("level", 2)
                .time(new BigInteger("123"), WritePrecision.S);

        Assertions.assertThat(point.toLineProtocol()).isEqualTo("h2o,location=europe level=2i 123");

        // Friday, June 22, 3353
        point = Point.measurement("h2o")
                .addTag("location", "europe")
                .addField("level", 2)
                .time(new BigInteger("43658216763800123456"), WritePrecision.NS);

        Assertions.assertThat(point.toLineProtocol()).isEqualTo("h2o,location=europe level=2i 43658216763800123456");
    }

    @Test
    void timeBigDecimal() {

        Point point = Point.measurement("h2o")
                .addTag("location", "europe")
                .addField("level", 2)
                .time(new BigDecimal("123"), WritePrecision.S);

        Assertions.assertThat(point.toLineProtocol()).isEqualTo("h2o,location=europe level=2i 123");

        point = Point.measurement("h2o")
                .addTag("location", "europe")
                .addField("level", 2)
                .time(new BigDecimal("1.23E+02"), WritePrecision.NS);

        Assertions.assertThat(point.toLineProtocol()).isEqualTo("h2o,location=europe level=2i 123");

        // Friday, June 22, 3353
        point = Point.measurement("h2o")
                .addTag("location", "europe")
                .addField("level", 2)
                .time(new BigDecimal("43658216763800123456"), WritePrecision.NS);

        Assertions.assertThat(point.toLineProtocol()).isEqualTo("h2o,location=europe level=2i 43658216763800123456");
    }

    @Test
    void timeFloat() {

        Point point = Point.measurement("h2o")
                .addTag("location", "europe")
                .addField("level", 2)
                .time(new Float("123"), WritePrecision.S);

        Assertions.assertThat(point.toLineProtocol()).isEqualTo("h2o,location=europe level=2i 123");

        point = Point.measurement("h2o")
                .addTag("location", "europe")
                .addField("level", 2)
                .time(new Float("1.23"), WritePrecision.NS);

        Assertions.assertThat(point.toLineProtocol()).isEqualTo("h2o,location=europe level=2i 1");
    }

    @Test
    void timePrecisionDefault() {

        Point point = Point.measurement("h2o")
                .addTag("location", "europe")
                .addField("level", 2);

        Assertions.assertThat(point.getPrecision()).isEqualTo(WritePrecision.NS);
    }

    @Test
    void timeInstantOver2262() {

        Instant time = Instant.parse("3353-06-22T10:26:03.800123456Z");

        Point point = Point.measurement("h2o")
                .addTag("location", "europe")
                .addField("level", 2)
                .time(time, WritePrecision.NS);

        Assertions.assertThat(point.toLineProtocol()).isEqualTo("h2o,location=europe level=2i 43658216763800123456");
    }

    @Test
    void timeInstantNull() {

        Point point = Point.measurement("h2o")
                .addTag("location", "europe")
                .addField("level", 2)
                .time((Instant) null, WritePrecision.S);

        Assertions.assertThat(point.toLineProtocol()).isEqualTo("h2o,location=europe level=2i");
    }

    @Test
    void timeGetTime() {

        Instant time = Instant.parse("2022-06-12T10:26:03.800123456Z");

        Point point = Point.measurement("h2o")
                .addTag("location", "europe")
                .addField("level", 2)
                .time(time, WritePrecision.NS);

        Assertions.assertThat(point.getTime()).isEqualTo(BigInteger.valueOf(1655029563800123456L));
    }

    @Test
    void defaultTags() {

        Point point = Point.measurement("h2o")
                .addTag("location", "europe")
                .addField("level", 2);

        PointSettings defaults = new PointSettings().addDefaultTag("expensive", "true");

        Assertions.assertThat(point.toLineProtocol(defaults)).isEqualTo("h2o,expensive=true,location=europe level=2i");
        Assertions.assertThat(point.toLineProtocol()).isEqualTo("h2o,location=europe level=2i");
    }

    @Test
    void defaultTagsOverride() {

        Point point = Point.measurement("h2o")
                .addTag("location", "europe")
                .addTag("expensive", "")
                .addField("level", 2);

        PointSettings defaults = new PointSettings().addDefaultTag("expensive", "true");

        Assertions.assertThat(point.toLineProtocol(defaults)).isEqualTo("h2o,expensive=true,location=europe level=2i");
    }

    @Test
    void defaultTagsOverrideNull() {

        Point point = Point.measurement("h2o")
                .addTag("location", "europe")
                .addTag("expensive", null)
                .addField("level", 2);

        PointSettings defaults = new PointSettings().addDefaultTag("expensive", "true");

        Assertions.assertThat(point.toLineProtocol(defaults)).isEqualTo("h2o,expensive=true,location=europe level=2i");
    }

    @Test
    void defaultTagsNotOverride() {

        Point point = Point.measurement("h2o")
                .addTag("location", "europe")
                .addTag("expensive", "false")
                .addField("level", 2);

        PointSettings defaults = new PointSettings().addDefaultTag("expensive", "true");

        Assertions.assertThat(point.toLineProtocol(defaults)).isEqualTo("h2o,expensive=false,location=europe level=2i");
    }

    @Test
    void defaultTagsSorted() {

        Point point = Point.measurement("h2o")
                .addTag("location", "europe")
                .addField("level", 2);

        PointSettings defaults = new PointSettings()
                .addDefaultTag("a-expensive", "true")
                .addDefaultTag("z-expensive", "false");

        Assertions.assertThat(point.toLineProtocol(defaults)).isEqualTo("h2o,a-expensive=true,location=europe,z-expensive=false level=2i");
    }

    @Test
    public void infinityValues() {
        Point point = Point.measurement("h2o")
                .addTag("location", "europe")
                .addField("double-infinity-positive", Double.POSITIVE_INFINITY)
                .addField("double-infinity-negative", Double.NEGATIVE_INFINITY)
                .addField("double-nan", Double.NaN)
                .addField("flout-infinity-positive", Float.POSITIVE_INFINITY)
                .addField("flout-infinity-negative", Float.NEGATIVE_INFINITY)
                .addField("flout-nan", Float.NaN)
                .addField("level", 2);

        Assertions.assertThat(point.toLineProtocol()).isEqualTo("h2o,location=europe level=2i");
    }

    @Test
    public void onlyInfinityValues() {
        Point point = Point.measurement("h2o")
                .addTag("location", "europe")
                .addField("double-infinity-positive", Double.POSITIVE_INFINITY)
                .addField("double-infinity-negative", Double.NEGATIVE_INFINITY)
                .addField("double-nan", Double.NaN)
                .addField("flout-infinity-positive", Float.POSITIVE_INFINITY)
                .addField("flout-infinity-negative", Float.NEGATIVE_INFINITY)
                .addField("flout-nan", Float.NaN);

        Assertions.assertThat(point.toLineProtocol()).isEqualTo("");
    }

    @Test
    void hasFields() {

        Assertions.assertThat(Point.measurement("h2o").hasFields()).isFalse();
        Assertions.assertThat(Point.measurement("h2o").addTag("location", "europe").hasFields()).isFalse();
        Assertions.assertThat(Point.measurement("h2o").addField("level", 2).hasFields()).isTrue();
        Assertions.assertThat(Point.measurement("h2o").addTag("location", "europe").addField("level", 3).hasFields()).isTrue();
    }

    @Test
    void addTags() {

        HashMap<String, String> tags = new HashMap<>();
        tags.put("type", "production");
        tags.put("location", "europe");
        tags.put("expensive", "");

        Point point = Point.measurement("h2o")
                .addField("level", 2)
                .addTags(tags);

        Assertions.assertThat(point.toLineProtocol()).isEqualTo("h2o,location=europe,type=production level=2i");
    }

    @Test
    void addFields() {

        HashMap<String, Object> fields = new HashMap<>();
        fields.put("level", 2);
        fields.put("accepted", true);
        fields.put("power", 2.56);
        fields.put("clean", null);

        Point point = Point
                .measurement("h2o")
                .addTag("location", "europe")
                .addFields(fields);

        Assertions.assertThat(point.toLineProtocol()).isEqualTo("h2o,location=europe accepted=true,level=2i,power=2.56");
    }
}