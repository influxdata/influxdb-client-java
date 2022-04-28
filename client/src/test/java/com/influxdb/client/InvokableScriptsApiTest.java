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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import com.influxdb.annotations.Column;
import com.influxdb.client.internal.AbstractInfluxDBClientTest;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

@RunWith(JUnitPlatform.class)
class InvokableScriptsApiTest extends AbstractInfluxDBClientTest {

    static final String SUCCESS_DATA = ",result,table,_start,_stop,_time,_value,_field,_measurement,host,value\n"
            + ",,0,1970-01-01T00:00:10Z,1970-01-01T00:00:20Z,1970-01-01T00:00:10Z,10,free,mem,A,12.25\n"
            + ",,1,1970-01-01T00:00:10Z,1970-01-01T00:00:20Z,1970-01-01T00:00:10Z,10,free,mem,A,15.55\n";

    private InvokableScriptsApi invokableScriptsApi;

    @BeforeEach
    protected void setUp() {
        super.setUp();

        invokableScriptsApi = influxDBClient.getInvokableScriptsApi();
    }

    @Test
    public void queryFluxTable() {
        mockServer.enqueue(createResponse(SUCCESS_DATA));

        List<FluxTable> tables = invokableScriptsApi.invokeScript("script_id", new HashMap<>());
        Assertions.assertThat(tables).hasSize(2);
        Assertions.assertThat(tables.get(0).getRecords()).hasSize(1);
        Assertions.assertThat(tables.get(0).getRecords().get(0).getValues().get("value")).isEqualTo("12.25");
        Assertions.assertThat(tables.get(1).getRecords()).hasSize(1);
        Assertions.assertThat(tables.get(1).getRecords().get(0).getValues().get("value")).isEqualTo("15.55");
    }

    @Test
    public void queryStreamFluxRecordFluxTable() {
        mockServer.enqueue(createResponse(SUCCESS_DATA));

        List<FluxRecord> records = new ArrayList<>();
        CountDownLatch recordsCountDown = new CountDownLatch(2);
        invokableScriptsApi.invokeScript("script_id", new HashMap<>(), (cancellable, fluxRecord) -> {
            records.add(fluxRecord);
            recordsCountDown.countDown();
        });

        waitToCallback(recordsCountDown, 10);

        Assertions.assertThat(records).hasSize(2);
        Assertions.assertThat(records.get(0).getValues().get("value")).isEqualTo("12.25");
        Assertions.assertThat(records.get(1).getValues().get("value")).isEqualTo("15.55");
    }

    @Test
    public void queryMeasurements() {
        mockServer.enqueue(createResponse(SUCCESS_DATA));

        List<InvokableScriptsPojo> pojos = invokableScriptsApi.invokeScript("script_id", new HashMap<>(), InvokableScriptsPojo.class);
        Assertions.assertThat(pojos).hasSize(2);
        Assertions.assertThat(pojos.get(0).value).isEqualTo("12.25");
        Assertions.assertThat(pojos.get(0).host).isEqualTo("A");
        Assertions.assertThat(pojos.get(1).value).isEqualTo("15.55");
        Assertions.assertThat(pojos.get(1).host).isEqualTo("A");
    }

    @Test
    public void queryStreamMeasurements() {
        mockServer.enqueue(createResponse(SUCCESS_DATA));

        List<InvokableScriptsPojo> pojos = new ArrayList<>();
        CountDownLatch measurementsCountDown = new CountDownLatch(2);
        invokableScriptsApi.invokeScript("script_id", new HashMap<>(), InvokableScriptsPojo.class, (cancellable, invokableScriptsPojo) -> {
            pojos.add(invokableScriptsPojo);
            measurementsCountDown.countDown();
        });

        waitToCallback(measurementsCountDown, 10);
        
        Assertions.assertThat(pojos).hasSize(2);
        Assertions.assertThat(pojos.get(0).value).isEqualTo("12.25");
        Assertions.assertThat(pojos.get(0).host).isEqualTo("A");
        Assertions.assertThat(pojos.get(1).value).isEqualTo("15.55");
        Assertions.assertThat(pojos.get(1).host).isEqualTo("A");
    }

    @Test
    public void queryRaw() {
        mockServer.enqueue(createResponse(SUCCESS_DATA));

        String response = invokableScriptsApi.invokeScriptRaw("script_id", new HashMap<>());
        Assertions.assertThat(response).isEqualToIgnoringNewLines(SUCCESS_DATA);
    }

    @Test
    public void queryStreamRaw() {
        mockServer.enqueue(createResponse(SUCCESS_DATA));

        List<String> lines = new ArrayList<>();
        CountDownLatch linesCountDown = new CountDownLatch(3);
        invokableScriptsApi.invokeScriptRaw("script_id", new HashMap<>(), (cancellable, line) -> {
            lines.add(line);
            linesCountDown.countDown();
        });

        waitToCallback(linesCountDown, 10);

        Assertions.assertThat(lines).hasSize(3);
        Assertions.assertThat(lines.get(0)).isEqualTo(",result,table,_start,_stop,_time,_value,_field,_measurement,host,value");
        Assertions.assertThat(lines.get(1)).isEqualTo(",,0,1970-01-01T00:00:10Z,1970-01-01T00:00:20Z,1970-01-01T00:00:10Z,10,free,mem,A,12.25");
        Assertions.assertThat(lines.get(2)).isEqualTo(",,1,1970-01-01T00:00:10Z,1970-01-01T00:00:20Z,1970-01-01T00:00:10Z,10,free,mem,A,15.55");
    }

    @SuppressWarnings("NewClassNamingConvention")
    public static class InvokableScriptsPojo {

        @Column(name = "host", tag = true)
        String host;

        @Column(name = "value")
        String value;
    }
}


