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
package com.influxdb.query.dsl.functions;

import java.util.HashMap;
import java.util.Map;

import com.influxdb.query.dsl.Flux;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class ArrayFromFluxTest {

    @Test
    void arrayFluxInstance() {

        Map<String, Object> record1 = new HashMap<>();
        record1.put("foo",  "bar");
        record1.put("int",  42);
        record1.put("double1",  6.23);
        record1.put("double2",  6.);
        record1.put("doubleScientific",  6.23087E8);
        Flux flux = new ArrayFromFlux()
                .withRow(record1);

        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace("import \"array\"\n" +
                "array.from(rows:[{double2: 6.0, foo: \"bar\", double1: 6.23, doubleScientific: 623087000.0, int: 42}])");
    }

    @Test
    void arrayFlux() {

        Map<String, Object> record1 = new HashMap<>();
        record1.put("foo",  "bar");
        record1.put("baz",  21.2);
        Flux flux = Flux.arrayFrom(record1, record1);

        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace("import \"array\"\n" +
                "array.from(rows:[{foo: \"bar\", baz: 21.2}, {foo: \"bar\", baz: 21.2}])");
    }
}
