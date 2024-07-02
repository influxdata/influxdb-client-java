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
package com.influxdb.client.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class InfluxQLQueryTest {

  @Test
  public void setRetentionPolicy(){
    String rp = "oneOffRP";
    InfluxQLQuery query = new InfluxQLQuery("SELECT * FROM cpu", "test_db");
    Assertions.assertThat(query.setRetentionPolicy(rp).getRetentionPolicy()).isEqualTo(rp);
  }

  @Test
  public void headerSelectDefault(){
    InfluxQLQuery query = new InfluxQLQuery("SELECT * FROM cpu", "test_db");
    Assertions.assertThat(query.getAcceptHeaderVal()).isEqualTo("application/json");
  }

  @Test
  public void headerSelect(){
    InfluxQLQuery query = new InfluxQLQuery("SELECT * FROM cpu",
      "test_db",
      InfluxQLQuery.AcceptHeader.CSV);
    Assertions.assertThat(query.getAcceptHeaderVal()).isEqualTo("application/csv");
  }

  @Test
  public void headerSet(){
    InfluxQLQuery query = new InfluxQLQuery("SELECT * FROM cpu", "test_db");
    Assertions.assertThat(query.getAcceptHeaderVal()).isEqualTo("application/json");
    Assertions.assertThat(query.setAcceptHeader(InfluxQLQuery.AcceptHeader.CSV).getAcceptHeaderVal())
      .isEqualTo("application/csv");
  }

  @Test
  public void timeUnitPrecisionConversion(){
    Map<TimeUnit, String> expected = Map.of(
      TimeUnit.NANOSECONDS, "n",
      TimeUnit.MICROSECONDS, "u",
      TimeUnit.MILLISECONDS, "ms",
      TimeUnit.SECONDS, "s",
      TimeUnit.MINUTES, "m",
      TimeUnit.HOURS, "h");
    for(TimeUnit tu: TimeUnit.values()){
      if(!tu.equals(TimeUnit.DAYS)){
        Assertions.assertThat(expected.get(tu)).isEqualTo(InfluxQLQuery.InfluxQLPrecision.toTimePrecision(tu).getSymbol());
      } else {
        Assertions.assertThatThrownBy(() -> InfluxQLQuery.InfluxQLPrecision.toTimePrecision(tu))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("time precision must be one of:[HOURS, MINUTES, SECONDS, MILLISECONDS, MICROSECONDS, NANOSECONDS]");
      }
    }
  }
}
