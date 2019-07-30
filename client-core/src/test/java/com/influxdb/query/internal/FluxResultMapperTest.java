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

import java.math.BigDecimal;

import com.influxdb.annotations.Column;
import com.influxdb.exceptions.InfluxException;
import com.influxdb.query.FluxRecord;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (01/02/2019 09:01)
 */
@RunWith(JUnitPlatform.class)
class FluxResultMapperTest {

    private FluxResultMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new FluxResultMapper();
    }

    @Test
    void mapToBigDecimal() {

        FluxRecord record = new FluxRecord(0);
        record.getValues().put("value1", "12345678901234567000");
        record.getValues().put("value2", 1234567890123456700L);
        record.getValues().put("value3", 1234567890.1234567);
        record.getValues().put("value4", 5.5D);

        BigDecimalBean bean = mapper.toPOJO(record, BigDecimalBean.class);
        Assertions.assertThat(bean.value1).isEqualByComparingTo(new BigDecimal("12345678901234567000"));
        Assertions.assertThat(bean.value2).isEqualByComparingTo(new BigDecimal("1234567890123456700"));
        Assertions.assertThat(bean.value3).isEqualByComparingTo(new BigDecimal("1234567890.1234567"));
        Assertions.assertThat(bean.value4).isEqualByComparingTo(new BigDecimal("5.5"));
    }

    @Test
    void mapToBigDecimalNotSupportedType() {

        FluxRecord record = new FluxRecord(0);
        record.getValues().put("value1", Boolean.TRUE);

        Assertions.assertThatThrownBy(() -> mapper.toPOJO(record, BigDecimalBean.class))
                .isInstanceOf(InfluxException.class)
                .hasMessageEndingWith("The correct type is 'java.lang.Boolean' (current field value: 'true').");
    }

    public static class BigDecimalBean
    {
        @Column(name = "value1")
        BigDecimal value1;

        @Column(name = "value2")
        BigDecimal value2;

        @Column(name = "value3")
        BigDecimal value3;

        @Column(name = "value4")
        BigDecimal value4;
    }
}