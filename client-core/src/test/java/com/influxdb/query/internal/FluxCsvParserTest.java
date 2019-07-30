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

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

import com.influxdb.Cancellable;
import com.influxdb.query.FluxColumn;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.influxdb.query.exceptions.FluxCsvParserException;
import com.influxdb.query.exceptions.FluxQueryException;

import okio.Buffer;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Jakub Bednar (bednar@github) (16/07/2018 12:26)
 */
@RunWith(JUnitPlatform.class)
class FluxCsvParserTest {

    private static final Logger LOG = Logger.getLogger(FluxCsvParserTest.class.getName());

    private FluxCsvParser parser;

    @BeforeEach
    void setUp() {
        parser = new FluxCsvParser();
    }

    @Test
    void responseWithMultipleValues() throws IOException {

        // curl -i -XPOST --data-urlencode 'q=from(bucket: "ubuntu_test") |> last()
        // |> map(fn: (r) => ({value1: r._value, _value2:r._value * r._value, value_str: "test"}))'
        // --data-urlencode "orgName=0" http://localhost:8093/api/v2/query

        String data = "#datatype,string,long,dateTime:RFC3339,dateTime:RFC3339,string,string,string,string,long,long,string\n"
                + "#group,false,false,true,true,true,true,true,true,false,false,false\n"
                + "#default,_result,,,,,,,,,,\n"
                + ",result,table,_start,_stop,_field,_measurement,host,region,_value2,value1,value_str\n"
                + ",,0,1677-09-21T00:12:43.145224192Z,2018-07-16T11:21:02.547596934Z,free,mem,A,west,121,11,test\n"
                + ",,1,1677-09-21T00:12:43.145224192Z,2018-07-16T11:21:02.547596934Z,free,mem,B,west,484,22,test\n"
                + ",,2,1677-09-21T00:12:43.145224192Z,2018-07-16T11:21:02.547596934Z,usage_system,cpu,A,west,1444,38,test\n"
                + ",,3,1677-09-21T00:12:43.145224192Z,2018-07-16T11:21:02.547596934Z,user_usage,cpu,A,west,2401,49,test";

        List<FluxTable> tables = parseFluxResponse(data);

        List<FluxColumn> columnHeaders = tables.get(0).getColumns();
        Assertions.assertThat(columnHeaders).hasSize(11);
        FluxColumn fluxColumn1 = columnHeaders.get(0);
        LOG.info("FluxColumn1: " + fluxColumn1);

        Assertions.assertThat(fluxColumn1.isGroup()).isFalse();
        Assertions.assertThat(columnHeaders.get(1).isGroup()).isFalse();
        Assertions.assertThat(columnHeaders.get(2).isGroup()).isTrue();
        Assertions.assertThat(columnHeaders.get(3).isGroup()).isTrue();
        Assertions.assertThat(columnHeaders.get(4).isGroup()).isTrue();
        Assertions.assertThat(columnHeaders.get(5).isGroup()).isTrue();
        Assertions.assertThat(columnHeaders.get(6).isGroup()).isTrue();
        Assertions.assertThat(columnHeaders.get(7).isGroup()).isTrue();
        Assertions.assertThat(columnHeaders.get(8).isGroup()).isFalse();
        Assertions.assertThat(columnHeaders.get(9).isGroup()).isFalse();
        Assertions.assertThat(columnHeaders.get(10).isGroup()).isFalse();

        Assertions.assertThat(tables).hasSize(4);

        // Record 1
        FluxTable fluxTable1 = tables.get(0);
        LOG.info("FluxTable1: " + fluxTable1);

        Assertions.assertThat(fluxTable1.getRecords()).hasSize(1);

        FluxRecord fluxRecord1 = fluxTable1.getRecords().get(0);
        LOG.info("FluxRecord1: " + fluxRecord1);

        Assertions.assertThat(0).isEqualTo(fluxRecord1.getTable());
        Assertions.assertThat(fluxRecord1.getValues())
                .hasEntrySatisfying("host", value -> Assertions.assertThat(value).isEqualTo("A"))
                .hasEntrySatisfying("region", value -> Assertions.assertThat(value).isEqualTo("west"));
        Assertions.assertThat(fluxRecord1.getValues()).hasSize(11);
        Assertions.assertThat(fluxRecord1.getValue()).isNull();
        Assertions.assertThat(fluxRecord1.getValues())
                .hasEntrySatisfying("value1", value -> Assertions.assertThat(value).isEqualTo(11L))
                .hasEntrySatisfying("_value2", value -> Assertions.assertThat(value).isEqualTo(121L))
                .hasEntrySatisfying("value_str", value -> Assertions.assertThat(value).isEqualTo("test"));
        Assertions.assertThat(fluxRecord1.getValueByIndex(8)).isEqualTo(121L);
        Assertions.assertThat(fluxRecord1.getValueByIndex(9)).isEqualTo(11L);
        Assertions.assertThat(fluxRecord1.getValueByIndex(10)).isEqualTo("test");

        // Record 2
        FluxTable fluxTable2 = tables.get(1);
        LOG.info("FluxTable2: " + fluxTable2);

        Assertions.assertThat(fluxTable2.getRecords()).hasSize(1);

        FluxRecord fluxRecord2 = fluxTable2.getRecords().get(0);
        Assertions.assertThat(1).isEqualTo(fluxRecord2.getTable());
        Assertions.assertThat(fluxRecord2.getValues())
                .hasEntrySatisfying("host", value -> Assertions.assertThat(value).isEqualTo("B"))
                .hasEntrySatisfying("region", value -> Assertions.assertThat(value).isEqualTo("west"));
        Assertions.assertThat(fluxRecord2.getValues()).hasSize(11);
        Assertions.assertThat(fluxRecord2.getValue()).isNull();
        Assertions.assertThat(fluxRecord2.getValues())
                .hasEntrySatisfying("value1", value -> Assertions.assertThat(value).isEqualTo(22L))
                .hasEntrySatisfying("_value2", value -> Assertions.assertThat(value).isEqualTo(484L))
                .hasEntrySatisfying("value_str", value -> Assertions.assertThat(value).isEqualTo("test"));

        // Record 3
        FluxTable fluxTable3 = tables.get(2);
        LOG.info("FluxTable3: " + fluxTable3);

        Assertions.assertThat(fluxTable3.getRecords()).hasSize(1);

        FluxRecord fluxRecord3 = fluxTable3.getRecords().get(0);
        Assertions.assertThat(2).isEqualTo(fluxRecord3.getTable());
        Assertions.assertThat(fluxRecord3.getValues())
                .hasEntrySatisfying("host", value -> Assertions.assertThat(value).isEqualTo("A"))
                .hasEntrySatisfying("region", value -> Assertions.assertThat(value).isEqualTo("west"));
        Assertions.assertThat(fluxRecord3.getValues()).hasSize(11);
        Assertions.assertThat(fluxRecord3.getValue()).isNull();
        Assertions.assertThat(fluxRecord3.getValues())
                .hasEntrySatisfying("value1", value -> Assertions.assertThat(value).isEqualTo(38L))
                .hasEntrySatisfying("_value2", value -> Assertions.assertThat(value).isEqualTo(1444L))
                .hasEntrySatisfying("value_str", value -> Assertions.assertThat(value).isEqualTo("test"));

        // Record 4
        FluxTable fluxTable4 = tables.get(3);
        LOG.info("FluxTable4: " + fluxTable4);

        Assertions.assertThat(fluxTable4.getRecords()).hasSize(1);

        FluxRecord fluxRecord4 = fluxTable4.getRecords().get(0);
        Assertions.assertThat(3).isEqualTo(fluxRecord4.getTable());
        Assertions.assertThat(fluxRecord4.getValues())
                .hasEntrySatisfying("host", value -> Assertions.assertThat(value).isEqualTo("A"))
                .hasEntrySatisfying("region", value -> Assertions.assertThat(value).isEqualTo("west"));
        Assertions.assertThat(fluxRecord4.getValues()).hasSize(11);
        Assertions.assertThat(fluxRecord4.getValue()).isNull();
        Assertions.assertThat(fluxRecord4.getValues())
                .hasEntrySatisfying("value1", value -> Assertions.assertThat(value).isEqualTo(49L))
                .hasEntrySatisfying("_value2", value -> Assertions.assertThat(value).isEqualTo(2401L))
                .hasEntrySatisfying("value_str", value -> Assertions.assertThat(value).isEqualTo("test"));
    }

    @Test
    void shortcut() throws IOException {

        String data = "#datatype,string,long,dateTime:RFC3339,dateTime:RFC3339,dateTime:RFC3339,long,string,string,string,boolean\n"
                + "#group,false,false,false,false,false,false,false,false,false,true\n"
                + "#default,_result,,,,,,,,,true\n"
                + ",result,table,_start,_stop,_time,_value,_field,_measurement,host,value\n"
                + ",,0,1970-01-01T00:00:10Z,1970-01-01T00:00:20Z,1970-01-01T00:00:10Z,10,free,mem,A,true\n";

        List<FluxTable> tables = parseFluxResponse(data);

        Assertions.assertThat(tables).hasSize(1);
        Assertions.assertThat(tables.get(0).getRecords()).hasSize(1);

        FluxRecord fluxRecord = tables.get(0).getRecords().get(0);

        Assertions.assertThat(fluxRecord.getStart()).isEqualTo(Instant.parse("1970-01-01T00:00:10Z"));
        Assertions.assertThat(fluxRecord.getStop()).isEqualTo(Instant.parse("1970-01-01T00:00:20Z"));
        Assertions.assertThat(fluxRecord.getTime()).isEqualTo(Instant.parse("1970-01-01T00:00:10Z"));
        Assertions.assertThat(fluxRecord.getValue()).isEqualTo(10L);
        Assertions.assertThat(fluxRecord.getField()).isEqualTo("free");
        Assertions.assertThat(fluxRecord.getMeasurement()).isEqualTo("mem");
    }

    @Test
    void mappingBoolean() throws IOException {

        String data = "#datatype,string,long,dateTime:RFC3339,dateTime:RFC3339,dateTime:RFC3339,long,string,string,string,boolean\n"
                + "#group,false,false,false,false,false,false,false,false,false,true\n"
                + "#default,_result,,,,,,,,,true\n"
                + ",result,table,_start,_stop,_time,_value,_field,_measurement,host,value\n"
                + ",,0,1970-01-01T00:00:10Z,1970-01-01T00:00:20Z,1970-01-01T00:00:10Z,10,free,mem,A,true\n"
                + ",,0,1970-01-01T00:00:10Z,1970-01-01T00:00:20Z,1970-01-01T00:00:10Z,10,free,mem,A,false\n"
                + ",,0,1970-01-01T00:00:10Z,1970-01-01T00:00:20Z,1970-01-01T00:00:10Z,10,free,mem,A,x\n"
                + ",,0,1970-01-01T00:00:10Z,1970-01-01T00:00:20Z,1970-01-01T00:00:10Z,10,free,mem,A,\n";

        List<FluxTable> tables = parseFluxResponse(data);
        Assertions.assertThat(tables.get(0).getRecords().get(0).getValueByKey("value")).isEqualTo(true);
        Assertions.assertThat(tables.get(0).getRecords().get(1).getValueByKey("value")).isEqualTo(false);
        Assertions.assertThat(tables.get(0).getRecords().get(2).getValueByKey("value")).isEqualTo(false);
        Assertions.assertThat(tables.get(0).getRecords().get(3).getValueByKey("value")).isEqualTo(true);
    }

    @Test
    void mappingUnsignedLong() throws IOException {

        String data = "#datatype,string,long,dateTime:RFC3339,dateTime:RFC3339,dateTime:RFC3339,long,string,string,string,unsignedLong\n"
                + "#group,false,false,false,false,false,false,false,false,false,true\n"
                + "#default,_result,,,,,,,,,\n"
                + ",result,table,_start,_stop,_time,_value,_field,_measurement,host,value\n"
                + ",,0,1970-01-01T00:00:10Z,1970-01-01T00:00:20Z,1970-01-01T00:00:10Z,10,free,mem,A,17916881237904312345\n"
                + ",,0,1970-01-01T00:00:10Z,1970-01-01T00:00:20Z,1970-01-01T00:00:10Z,10,free,mem,A,\n";

        long expected = Long.parseUnsignedLong("17916881237904312345");

        List<FluxTable> tables = parseFluxResponse(data);
        Assertions.assertThat(tables.get(0).getRecords().get(0).getValueByKey("value")).isEqualTo(expected);
        Assertions.assertThat(tables.get(0).getRecords().get(1).getValueByKey("value")).isNull();
    }

    @Test
    void mappingDouble() throws IOException {

        String data = "#datatype,string,long,dateTime:RFC3339,dateTime:RFC3339,dateTime:RFC3339,long,string,string,string,double\n"
                + "#group,false,false,false,false,false,false,false,false,false,true\n"
                + "#default,_result,,,,,,,,,\n"
                + ",result,table,_start,_stop,_time,_value,_field,_measurement,host,value\n"
                + ",,0,1970-01-01T00:00:10Z,1970-01-01T00:00:20Z,1970-01-01T00:00:10Z,10,free,mem,A,12.25\n"
                + ",,0,1970-01-01T00:00:10Z,1970-01-01T00:00:20Z,1970-01-01T00:00:10Z,10,free,mem,A,\n";

        List<FluxTable> tables = parseFluxResponse(data);
        Assertions.assertThat(tables.get(0).getRecords().get(0).getValueByKey("value")).isEqualTo(12.25D);
        Assertions.assertThat(tables.get(0).getRecords().get(1).getValueByKey("value")).isNull();
    }

    @Test
    void mappingBase64Binary() throws IOException {

        String binaryData = "test value";
        String encodedString = Base64.getEncoder().encodeToString(binaryData.getBytes(UTF_8));

        String data = "#datatype,string,long,dateTime:RFC3339,dateTime:RFC3339,dateTime:RFC3339,long,string,string,string,base64Binary\n"
                + "#group,false,false,false,false,false,false,false,false,false,true\n"
                + "#default,_result,,,,,,,,,\n"
                + ",result,table,_start,_stop,_time,_value,_field,_measurement,host,value\n"
                + ",,0,1970-01-01T00:00:10Z,1970-01-01T00:00:20Z,1970-01-01T00:00:10Z,10,free,mem,A," + encodedString + "\n"
                + ",,0,1970-01-01T00:00:10Z,1970-01-01T00:00:20Z,1970-01-01T00:00:10Z,10,free,mem,A,\n";

        List<FluxTable> tables = parseFluxResponse(data);

        byte[] value = (byte[]) tables.get(0).getRecords().get(0).getValueByKey("value");
        Assertions.assertThat(value).isNotEmpty();
        Assertions.assertThat(new String(value, UTF_8)).isEqualTo(binaryData);

        Assertions.assertThat(tables.get(0).getRecords().get(1).getValueByKey("value")).isNull();
    }

    @Test
    void mappingRFC3339() throws IOException {

        String data = "#datatype,string,long,dateTime:RFC3339,dateTime:RFC3339,dateTime:RFC3339,long,string,string,string,dateTime:RFC3339\n"
                + "#group,false,false,false,false,false,false,false,false,false,true\n"
                + "#default,_result,,,,,,,,,\n"
                + ",result,table,_start,_stop,_time,_value,_field,_measurement,host,value\n"
                + ",,0,1970-01-01T00:00:10Z,1970-01-01T00:00:20Z,1970-01-01T00:00:10Z,10,free,mem,A,1970-01-01T00:00:10Z\n"
                + ",,0,1970-01-01T00:00:10Z,1970-01-01T00:00:20Z,1970-01-01T00:00:10Z,10,free,mem,A,\n";

        List<FluxTable> tables = parseFluxResponse(data);
        Assertions.assertThat(tables.get(0).getRecords().get(0).getValueByKey("value")).isEqualTo(Instant.ofEpochSecond(10));
        Assertions.assertThat(tables.get(0).getRecords().get(1).getValueByKey("value")).isNull();
    }

    @Test
    void mappingRFC3339Nano() throws IOException {

        String data = "#datatype,string,long,dateTime:RFC3339,dateTime:RFC3339,dateTime:RFC3339,long,string,string,string,dateTime:RFC3339Nano\n"
                + "#group,false,false,false,false,false,false,false,false,false,true\n"
                + "#default,_result,,,,,,,,,\n"
                + ",result,table,_start,_stop,_time,_value,_field,_measurement,host,value\n"
                + ",,0,1970-01-01T00:00:10Z,1970-01-01T00:00:20Z,1970-01-01T00:00:10Z,10,free,mem,A,1970-01-01T00:00:10.999999999Z\n"
                + ",,0,1970-01-01T00:00:10Z,1970-01-01T00:00:20Z,1970-01-01T00:00:10Z,10,free,mem,A,\n";

        List<FluxTable> tables = parseFluxResponse(data);

        Assertions.assertThat(tables.get(0).getRecords().get(0).getValueByKey("value"))
                .isEqualTo(Instant.ofEpochSecond(10).plusNanos(999999999));
        Assertions.assertThat(tables.get(0).getRecords().get(1).getValueByKey("value"))
                .isNull();
    }

    @Test
    void mappingDuration() throws IOException {

        String data = "#datatype,string,long,dateTime:RFC3339,dateTime:RFC3339,dateTime:RFC3339,long,string,string,string,duration\n"
                + "#group,false,false,false,false,false,false,false,false,false,true\n"
                + "#default,_result,,,,,,,,,\n"
                + ",result,table,_start,_stop,_time,_value,_field,_measurement,host,value\n"
                + ",,0,1970-01-01T00:00:10Z,1970-01-01T00:00:20Z,1970-01-01T00:00:10Z,10,free,mem,A,125\n"
                + ",,0,1970-01-01T00:00:10Z,1970-01-01T00:00:20Z,1970-01-01T00:00:10Z,10,free,mem,A,\n";

        List<FluxTable> tables = parseFluxResponse(data);

        Assertions.assertThat(tables.get(0).getRecords().get(0).getValueByKey("value"))
                .isEqualTo(Duration.ofNanos(125));
        Assertions.assertThat(tables.get(0).getRecords().get(1).getValueByKey("value"))
                .isNull();
    }

    @Test
    void groupKey() throws IOException {

        String data = "#datatype,string,long,dateTime:RFC3339,dateTime:RFC3339,dateTime:RFC3339,long,string,string,string,duration\n"
                + "#group,false,false,false,false,true,false,false,false,false,true\n"
                + "#default,_result,,,,,,,,,\n"
                + ",result,table,_start,_stop,_time,_value,_field,_measurement,host,value\n"
                + ",,0,1970-01-01T00:00:10Z,1970-01-01T00:00:20Z,1970-01-01T00:00:10Z,10,free,mem,A,125\n"
                + ",,0,1970-01-01T00:00:10Z,1970-01-01T00:00:20Z,1970-01-01T00:00:10Z,10,free,mem,A,\n";

        List<FluxTable> tables = parseFluxResponse(data);

        Assertions.assertThat(tables.get(0).getColumns()).hasSize(10);
        Assertions.assertThat(tables.get(0).getGroupKey()).hasSize(2);
    }

    @Test
    void unknownTypeAsString() throws IOException {

        String data = "#datatype,string,long,dateTime:RFC3339,dateTime:RFC3339,dateTime:RFC3339,long,string,string,string,unknown\n"
                + "#group,false,false,false,false,false,false,false,false,false,true\n"
                + "#default,_result,,,,,,,,,\n"
                + ",result,table,_start,_stop,_time,_value,_field,_measurement,host,value\n"
                + ",,0,1970-01-01T00:00:10Z,1970-01-01T00:00:20Z,1970-01-01T00:00:10Z,10,free,mem,A,12.25\n"
                + ",,0,1970-01-01T00:00:10Z,1970-01-01T00:00:20Z,1970-01-01T00:00:10Z,10,free,mem,A,\n";

        List<FluxTable> tables = parseFluxResponse(data);
        Assertions.assertThat(tables.get(0).getRecords().get(0).getValueByKey("value")).isEqualTo("12.25");
        Assertions.assertThat(tables.get(0).getRecords().get(1).getValueByKey("value")).isNull();
    }

    @Test
    void error() {

        String data =
                "#datatype,string,string\n"
                        + "#group,true,true\n"
                        + "#default,,\n"
                        + ",error,reference\n"
                        + ",failed to create physical plan: invalid time bounds from procedure from: bounds contain zero time,897";

        Assertions.assertThatThrownBy(() -> parseFluxResponse(data))
                .isInstanceOf(FluxQueryException.class)
                .hasMessage("failed to create physical plan: invalid time bounds from procedure from: bounds contain zero time")
                .matches((Predicate<Throwable>) throwable -> ((FluxQueryException) throwable).reference() == 897);
    }

    @Test
    void errorWithoutReference() {

        String data =
                "#datatype,string,string\n"
                        + "#group,true,true\n"
                        + "#default,,\n"
                        + ",error,reference\n"
                        + ",failed to create physical plan: invalid time bounds from procedure from: bounds contain zero time,";

        Assertions.assertThatThrownBy(() -> parseFluxResponse(data))
                .isInstanceOf(FluxQueryException.class)
                .hasMessage("failed to create physical plan: invalid time bounds from procedure from: bounds contain zero time")
                .matches((Predicate<Throwable>) throwable -> ((FluxQueryException) throwable).reference() == 0);
    }

    @Test
    void parsingToConsumer() throws IOException {

        String data = "#datatype,string,long,dateTime:RFC3339,dateTime:RFC3339,dateTime:RFC3339,long,string,string,string,unknown\n"
                + "#group,false,false,false,false,false,false,false,false,false,true\n"
                + "#default,_result,,,,,,,,,\n"
                + ",result,table,_start,_stop,_time,_value,_field,_measurement,host,value\n"
                + ",,0,1970-01-01T00:00:10Z,1970-01-01T00:00:20Z,1970-01-01T00:00:10Z,10,free,mem,A,12.25\n"
                + ",,0,1970-01-01T00:00:10Z,1970-01-01T00:00:20Z,1970-01-01T00:00:10Z,10,free,mem,A,\n";

        List<FluxRecord> records = Lists.newArrayList();

        FluxCsvParser.FluxResponseConsumer consumer = new FluxCsvParser.FluxResponseConsumer() {
            @Override
            public void accept(final int index, @Nonnull final Cancellable cancellable, @Nonnull final FluxTable table) {

            }

            @Override
            public void accept(final int index, @Nonnull final Cancellable cancellable, @Nonnull final FluxRecord record) {
                records.add(record);
            }
        };

        Buffer buffer = new Buffer();
        buffer.writeUtf8(data);

        parser.parseFluxResponse(buffer, new DefaultCancellable(), consumer);
        Assertions.assertThat(records).hasSize(2);
    }

    @Test
    void cancelParsing() throws IOException {

        String data = "#datatype,string,long,dateTime:RFC3339,dateTime:RFC3339,dateTime:RFC3339,long,string,string,string,unknown\n"
                + "#group,false,false,false,false,false,false,false,false,false,true\n"
                + "#default,_result,,,,,,,,,\n"
                + ",result,table,_start,_stop,_time,_value,_field,_measurement,host,value\n"
                + ",,0,1970-01-01T00:00:10Z,1970-01-01T00:00:20Z,1970-01-01T00:00:10Z,10,free,mem,A,12.25\n"
                + ",,0,1970-01-01T00:00:10Z,1970-01-01T00:00:20Z,1970-01-01T00:00:10Z,10,free,mem,A,\n";

        List<FluxRecord> records = Lists.newArrayList();

        DefaultCancellable defaultCancellable = new DefaultCancellable();

        FluxCsvParser.FluxResponseConsumer consumer = new FluxCsvParser.FluxResponseConsumer() {
            @Override
            public void accept(final int index, @Nonnull final Cancellable cancellable, @Nonnull final FluxTable table) {

            }

            @Override
            public void accept(final int index, @Nonnull final Cancellable cancellable, @Nonnull final FluxRecord record) {
                defaultCancellable.cancel();
                records.add(record);
            }
        };

        Buffer buffer = new Buffer();
        buffer.writeUtf8(data);

        parser.parseFluxResponse(buffer, defaultCancellable, consumer);
        Assertions.assertThat(records).hasSize(1);
    }

    @Test
    void parsingWithoutTableDefinition() {

        String data = ",result,table,_start,_stop,_time,_value,_field,_measurement,host,value\n"
                + ",,0,1970-01-01T00:00:10Z,1970-01-01T00:00:20Z,1970-01-01T00:00:10Z,10,free,mem,A,12.25\n"
                + ",,0,1970-01-01T00:00:10Z,1970-01-01T00:00:20Z,1970-01-01T00:00:10Z,10,free,mem,A,\n";

        Assertions.assertThatThrownBy(() -> parseFluxResponse(data))
                .isInstanceOf(FluxCsvParserException.class)
                .hasMessage("Unable to parse CSV response. FluxTable definition was not found.");
    }

    @Nonnull
    private List<FluxTable> parseFluxResponse(@Nonnull final String data) throws IOException {


        Buffer buffer = new Buffer();
        buffer.writeUtf8(data);

        FluxCsvParser.FluxResponseConsumerTable consumer = parser.new FluxResponseConsumerTable();
        parser.parseFluxResponse(buffer, new DefaultCancellable(), consumer);

        return consumer.getTables();
    }

    private static class DefaultCancellable implements Cancellable {

        private boolean cancelled = false;

        @Override
        public void cancel() {
            cancelled = true;
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }
    }
}
