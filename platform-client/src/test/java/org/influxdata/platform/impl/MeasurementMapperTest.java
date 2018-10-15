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
package org.influxdata.platform.impl;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import org.influxdata.platform.annotations.Column;
import org.influxdata.platform.annotations.Measurement;
import org.influxdata.platform.write.Point;

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

        Assertions.assertThat(mapper.toPoint(pojo, TimeUnit.SECONDS).toString()).isEqualTo("pojo,tag=value value=\"15\" 10");
        Assertions.assertThat(mapper.toPoint(pojo, TimeUnit.MILLISECONDS).toString()).isEqualTo("pojo,tag=value value=\"15\" 10999");
        Assertions.assertThat(mapper.toPoint(pojo, TimeUnit.MICROSECONDS).toString()).isEqualTo("pojo,tag=value value=\"15\" 10999999");
        Assertions.assertThat(mapper.toPoint(pojo, TimeUnit.NANOSECONDS).toString()).isEqualTo("pojo,tag=value value=\"15\" 10999999999");
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

        Point point = mapper.toPoint(pojo, TimeUnit.NANOSECONDS);
        Assertions.assertThat(point.toString()).isEqualTo("pojo,tag=value value=\"to-string\"");
    }

    @Measurement(name = "pojo")
    private static class Pojo {

        @Column(name = "tag", tag = true)
        private String tag;

        @Column(name = "value")
        private Object value;

        @Column(timestamp = true)
        private Instant timestamp;
    }

}