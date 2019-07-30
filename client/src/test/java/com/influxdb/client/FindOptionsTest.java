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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (18/03/2019 11:53)
 */
@RunWith(JUnitPlatform.class)
class FindOptionsTest {

    @Test
    void mappingLinks() {

        FindOptions findOptions = FindOptions.create("/api/v2/buckets?descending=false\u0026limit=20\u0026offset=0");

        Assertions.assertThat(findOptions.getLimit()).isEqualTo(20);
        Assertions.assertThat(findOptions.getDescending()).isFalse();
        Assertions.assertThat(findOptions.getOffset()).isEqualTo(0);
        Assertions.assertThat(findOptions.getSortBy()).isNull();
    }

    @Test
    void sortBy() {

        FindOptions findOptions = FindOptions.create("/api/v2/buckets?descending=true\u0026limit=20\u0026offset=10\u0026sortBy=ID");
        
        Assertions.assertThat(findOptions.getLimit()).isEqualTo(20);
        Assertions.assertThat(findOptions.getDescending()).isTrue();
        Assertions.assertThat(findOptions.getOffset()).isEqualTo(10);
        Assertions.assertThat(findOptions.getSortBy()).isEqualTo("ID");
    }
}