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
package com.influxdb.client.internal;

import java.time.Instant;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (15/10/2018 13:36)
 */
@RunWith(JUnitPlatform.class)
class MeasurementMapperTest {

    private MeasurementMapper mapper;

    @BeforeEach
    void setUp()
    {
        mapper = new MeasurementMapper();
    }

    @Test
    void precision() {

        Instant.parse("1970-01-01T00:00:10.999999999Z");

        Pojo pojo = new Pojo();
        pojo.tag = "value";
        pojo.value = 15;
        pojo.timestamp = Instant.parse("1970-01-01T00:00:10.999999999Z");

        Assertions.assertThat(mapper.toPoint(pojo, WritePrecision.S).toLineProtocol()).isEqualTo("pojo,tag=value value=\"15\" 10");
        Assertions.assertThat(mapper.toPoint(pojo, WritePrecision.MS).toLineProtocol()).isEqualTo("pojo,tag=value value=\"15\" 10999");
        Assertions.assertThat(mapper.toPoint(pojo, WritePrecision.US).toLineProtocol()).isEqualTo("pojo,tag=value value=\"15\" 10999999");
        Assertions.assertThat(mapper.toPoint(pojo, WritePrecision.NS).toLineProtocol()).isEqualTo("pojo,tag=value value=\"15\" 10999999999");
    }

    @Test
    void columnWithoutName() {

        Pojo pojo = new Pojo();
        pojo.tag = "tag val";
        pojo.value = 15;
        pojo.valueWithoutDefaultName = 20;
        pojo.valueWithEmptyName = 25;

        Assertions.assertThat(mapper.toPoint(pojo, WritePrecision.S).toLineProtocol()).isEqualTo("pojo,tag=tag\\ val value=\"15\",valueWithEmptyName=25i,valueWithoutDefaultName=20i");
    }

    @Test
    void defaultToString() {

        Pojo pojo = new Pojo();
        pojo.tag = "value";
        pojo.value = new Object() {
            @Override
            public String toString() {
                return "to-string";
            }
        };

        Point point = mapper.toPoint(pojo, WritePrecision.NS);
        Assertions.assertThat(point.toLineProtocol()).isEqualTo("pojo,tag=value value=\"to-string\"");
    }

    @Measurement(name = "pojo")
    private static class Pojo {

        @Column(name = "tag", tag = true)
        private String tag;

        @Column(name = "value")
        private Object value;

        @Column
        private Integer valueWithoutDefaultName;

        @Column(name = "")
        private Number valueWithEmptyName;

        @Column(timestamp = true)
        private Instant timestamp;
    }
}