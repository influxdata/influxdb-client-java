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
package com.influxdb.query;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class InfluxQLQueryResultTest {

  @Test
  public void readLegacyTimestamps(){
    Map<String,String> times = Map.of(
      "2024-06-20T11:41:17.690437345Z", "1718883677690437345", // nano
      "2024-06-20T11:41:17.690437Z", "1718883677690437000", // micro
      "2024-06-20T11:41:17.690Z", "1718883677690000000", // milli
      "2024-06-20T11:41:17Z", "1718883677000000000"  // second
    );
    for(String stamp : times.keySet()){
      Object result = InfluxQLQueryResult.Series.legacyExtractValue("time", stamp, 0, "test");
      Assertions.assertThat(times.get(stamp)).isEqualTo(result);
    }
  }

}
