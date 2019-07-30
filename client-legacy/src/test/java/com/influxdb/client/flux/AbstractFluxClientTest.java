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
package com.influxdb.client.flux;

import javax.annotation.Nonnull;

import com.influxdb.client.flux.internal.FluxApiImpl;
import com.influxdb.test.AbstractMockServerTest;

import okhttp3.mockwebserver.MockResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * @author Jakub Bednar (bednar@github) (31/07/2018 07:06)
 */
public abstract class AbstractFluxClientTest extends AbstractMockServerTest {

    static final String SUCCESS_DATA =
            "#datatype,string,long,dateTime:RFC3339,dateTime:RFC3339,dateTime:RFC3339,long,string,string,string,string\n"
            + "#group,false,false,false,false,false,false,false,false,false,true\n"
            + "#default,_result,,,,,,,,,\n"
            + ",result,table,_start,_stop,_time,_value,_field,_measurement,host,region\n"
            + ",,0,1970-01-01T00:00:10Z,1970-01-01T00:00:20Z,1970-01-01T00:00:10Z,10,free,mem,A,west\n"
            + ",,0,1970-01-01T00:00:10Z,1970-01-01T00:00:20Z,1970-01-01T00:00:10Z,20,free,mem,B,west\n"
            + ",,0,1970-01-01T00:00:20Z,1970-01-01T00:00:30Z,1970-01-01T00:00:20Z,11,free,mem,A,west\n"
            + ",,0,1970-01-01T00:00:20Z,1970-01-01T00:00:30Z,1970-01-01T00:00:20Z,22,free,mem,B,west";

    FluxClient fluxClient;

    @BeforeEach
    protected void setUp() {

        FluxConnectionOptions fluxConnectionOptions = FluxConnectionOptions.builder()
                .url(startMockServer())
                .build();

        fluxClient = new FluxApiImpl(fluxConnectionOptions);
    }

    @AfterEach
    void close() throws Exception {
        fluxClient.close();
    }

        @Nonnull
    MockResponse createResponse() {

        return createResponse(SUCCESS_DATA);
    }
}