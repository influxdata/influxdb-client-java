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

import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.internal.AbstractInfluxDBClientTest;
import com.influxdb.client.write.Point;

import okhttp3.mockwebserver.RecordedRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.suite.api.Suite;

/**
 * @author Jakub Bednar (12/11/2020 10:25)
 */
@Suite
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
        Assertions.assertThat("ns").isEqualTo(request.getRequestUrl().queryParameter("precision"));
        Assertions.assertThat("b1").isEqualTo(request.getRequestUrl().queryParameter("bucket"));
        Assertions.assertThat("org1").isEqualTo(request.getRequestUrl().queryParameter("org"));

        request = takeRequest();

        Assertions.assertThat("h2o,location=europe level=2i 2")
                .isEqualTo(request.getBody().readUtf8());
        Assertions.assertThat("s").isEqualTo(request.getRequestUrl().queryParameter("precision"));
        Assertions.assertThat("b1").isEqualTo(request.getRequestUrl().queryParameter("bucket"));
        Assertions.assertThat("org1").isEqualTo(request.getRequestUrl().queryParameter("org"));
    }
}
