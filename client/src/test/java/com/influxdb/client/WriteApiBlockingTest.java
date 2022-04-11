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

import java.util.Arrays;
import java.util.Collections;

import com.influxdb.client.domain.WriteConsistency;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.internal.AbstractInfluxDBClientTest;
import com.influxdb.client.write.Point;
import com.influxdb.client.write.WriteParameters;

import okhttp3.mockwebserver.RecordedRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (12/11/2020 10:25)
 */
@RunWith(JUnitPlatform.class)
class WriteApiBlockingTest extends AbstractInfluxDBClientTest {

    @Test
    public void groupPointsByPrecision() throws InterruptedException {
        mockServer.enqueue(createResponse("{}"));

        Point point1 = Point.measurement("h2o").addTag("location", "europe").addField("level", 1).time(1L, WritePrecision.NS);
        Point point2 = Point.measurement("h2o").addTag("location", "europe").addField("level", 2).time(2L, WritePrecision.NS);

        influxDBClient
                .getWriteApiBlocking()
                .writePoints("b1", "org1", Arrays.asList(point1, point2));

        Assertions.assertThat(mockServer.getRequestCount()).isEqualTo(1);

        RecordedRequest request = takeRequest();

        Assertions.assertThat("h2o,location=europe level=1i 1\nh2o,location=europe level=2i 2")
                .isEqualTo(request.getBody().readUtf8());
        Assertions.assertThat(request.getRequestUrl()).isNotNull();
        Assertions.assertThat("ns").isEqualTo(request.getRequestUrl().queryParameter("precision"));
        Assertions.assertThat("b1").isEqualTo(request.getRequestUrl().queryParameter("bucket"));
        Assertions.assertThat("org1").isEqualTo(request.getRequestUrl().queryParameter("org"));
    }

    @Test
    public void groupPointsByPrecisionDifferent() throws InterruptedException {
        mockServer.enqueue(createResponse("{}"));
        mockServer.enqueue(createResponse("{}"));

        Point point1 = Point.measurement("h2o").addTag("location", "europe").addField("level", 1).time(1L, WritePrecision.NS);
        Point point2 = Point.measurement("h2o").addTag("location", "europe").addField("level", 2).time(2L, WritePrecision.S);

        influxDBClient
                .getWriteApiBlocking()
                .writePoints("b1", "org1", Arrays.asList(point1, point2));

        Assertions.assertThat(mockServer.getRequestCount()).isEqualTo(2);

        RecordedRequest request = takeRequest();

        Assertions.assertThat("h2o,location=europe level=1i 1")
                .isEqualTo(request.getBody().readUtf8());
        Assertions.assertThat(request.getRequestUrl()).isNotNull();
        Assertions.assertThat("ns").isEqualTo(request.getRequestUrl().queryParameter("precision"));
        Assertions.assertThat("b1").isEqualTo(request.getRequestUrl().queryParameter("bucket"));
        Assertions.assertThat("org1").isEqualTo(request.getRequestUrl().queryParameter("org"));

        request = takeRequest();

        Assertions.assertThat("h2o,location=europe level=2i 2")
                .isEqualTo(request.getBody().readUtf8());
        Assertions.assertThat(request.getRequestUrl()).isNotNull();
        Assertions.assertThat("s").isEqualTo(request.getRequestUrl().queryParameter("precision"));
        Assertions.assertThat("b1").isEqualTo(request.getRequestUrl().queryParameter("bucket"));
        Assertions.assertThat("org1").isEqualTo(request.getRequestUrl().queryParameter("org"));
    }

    @Test
    void writeParameters() {
        Runnable assertParameters = () -> {
            RecordedRequest request;
            try {
                request = takeRequest();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            Assertions.assertThat(request.getRequestUrl()).isNotNull();
            Assertions.assertThat("s").isEqualTo(request.getRequestUrl().queryParameter("precision"));
            Assertions.assertThat("a").isEqualTo(request.getRequestUrl().queryParameter("bucket"));
            Assertions.assertThat("b").isEqualTo(request.getRequestUrl().queryParameter("org"));
            Assertions.assertThat("quorum").isEqualTo(request.getRequestUrl().queryParameter("consistency"));
        };

        WriteParameters parameters = new WriteParameters("a", "b", WritePrecision.S, WriteConsistency.QUORUM);

        // record
        mockServer.enqueue(createResponse("{}"));
        influxDBClient
                .getWriteApiBlocking()
                .writeRecord("h2o,location=europe level=1i 1", parameters);
        assertParameters.run();

        // records
        mockServer.enqueue(createResponse("{}"));
        influxDBClient
                .getWriteApiBlocking()
                .writeRecords(Collections.singletonList("h2o,location=europe level=1i 1"), parameters);
        assertParameters.run();

        // point
        Point point = Point.measurement("h2o").addTag("location", "europe").addField("level", 1).time(1L, WritePrecision.S);
        mockServer.enqueue(createResponse("{}"));
        influxDBClient
                .getWriteApiBlocking()
                .writePoint(point, parameters);
        assertParameters.run();

        // points
        mockServer.enqueue(createResponse("{}"));
        influxDBClient
                .getWriteApiBlocking()
                .writePoints(Collections.singletonList(point), parameters);
        assertParameters.run();

        // measurement
        H2OFeetMeasurement measurement = new H2OFeetMeasurement("coyote_creek", 2.927, "below 3 feet", 1440046800L);
        mockServer.enqueue(createResponse("{}"));
        influxDBClient
                .getWriteApiBlocking()
                .writeMeasurement(measurement, parameters);
        assertParameters.run();

        // measurements
        mockServer.enqueue(createResponse("{}"));
        influxDBClient
                .getWriteApiBlocking()
                .writeMeasurements(Collections.singletonList(measurement), parameters);
        assertParameters.run();

    }
}
