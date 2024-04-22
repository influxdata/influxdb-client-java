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

import com.influxdb.query.dsl.Flux;
import com.influxdb.query.dsl.functions.properties.TimeInterval;
import com.influxdb.query.dsl.functions.restriction.Restrictions;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;

import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.*;

import static java.util.Map.entry;

public class ElapsedFluxTest {

  @Test
  void elapsedBasic(){
    Flux flux = Flux.from("telegraf")
      .filter(Restrictions.measurement().equal("cpu"))
      .range(-15L, ChronoUnit.MINUTES)
      .elapsed(new TimeInterval(1000L, ChronoUnit.NANOS));

    String expected = "from(bucket:\"telegraf\")\n" +
      "\t|> filter(fn: (r) => r[\"_measurement\"] == \"cpu\")\n" +
      "\t|> range(start:-15m)\n" +
      "\t|> elapsed(unit:1000ns)";

    Assertions.assertThat(flux.toString()).isEqualTo(expected);
  }

  @Test
  void elapsedIntChrono(){
    Flux flux = Flux.from("telegraf")
      .filter(Restrictions.measurement().equal("mem"))
      .range(-5L, ChronoUnit.MINUTES)
      .elapsed(10, ChronoUnit.MICROS);

    String expected = "from(bucket:\"telegraf\")\n" +
      "\t|> filter(fn: (r) => r[\"_measurement\"] == \"mem\")\n" +
      "\t|> range(start:-5m)\n" +
      "\t|> elapsed(unit:10us)";

    Assertions.assertThat(flux.toString()).isEqualTo(expected);
  }

  @Test
  void elapsedChrono(){
    Flux flux = Flux.from("telegraf")
      .filter(Restrictions.measurement().equal("netio"))
      .range(-3L, ChronoUnit.HOURS)
      .elapsed(ChronoUnit.MINUTES);

    String expected = "from(bucket:\"telegraf\")\n" +
      "\t|> filter(fn: (r) => r[\"_measurement\"] == \"netio\")\n" +
      "\t|> range(start:-3h)\n" +
      "\t|> elapsed(unit:1m)";

    Assertions.assertThat(flux.toString()).isEqualTo(expected);
  }

  @Test
  void elapsedDefault(){
    Flux flux = Flux.from("telegraf")
      .filter(Restrictions.measurement().equal("disk"))
      .range(-30L, ChronoUnit.MINUTES)
      .elapsed();

    String expected = "from(bucket:\"telegraf\")\n" +
      "\t|> filter(fn: (r) => r[\"_measurement\"] == \"disk\")\n" +
      "\t|> range(start:-30m)\n" +
      "\t|> elapsed(unit:1ms)";

    Assertions.assertThat(flux.toString()).isEqualTo(expected);
  }

  private static Map<ChronoUnit, String> chronoVals = Map.ofEntries(
    entry(ChronoUnit.NANOS, "1ns"),
    entry(ChronoUnit.MICROS, "1us"),
    entry(ChronoUnit.MILLIS, "1ms"),
    entry(ChronoUnit.SECONDS, "1s"),
    entry(ChronoUnit.MINUTES, "1m"),
    entry(ChronoUnit.HOURS, "1h"),
    entry(ChronoUnit.HALF_DAYS, "12h"),
    entry(ChronoUnit.DAYS, "1d"),
    entry(ChronoUnit.WEEKS, "1w"),
    entry(ChronoUnit.MONTHS, "1mo"),
    entry(ChronoUnit.YEARS, "1y"),
    entry(ChronoUnit.DECADES, "10y"),
    entry(ChronoUnit.CENTURIES, "100y"),
    entry(ChronoUnit.MILLENNIA, "1000y"),
    entry(ChronoUnit.ERAS, "1000000000y")
  );

  @Test
  void chronoUnitsSupported(){
    for(ChronoUnit cu : ChronoUnit.values()){
      if(cu.equals(ChronoUnit.FOREVER)){
        Flux flux = Flux.from("telegraf")
          .elapsed(cu);
        Assertions.assertThatThrownBy(flux::toString)
          .isInstanceOf(IllegalArgumentException.class);
      }else {
        Flux flux = Flux.from("telegraf")
          .elapsed(cu);

        Assertions.assertThat(String.format("from(bucket:\"telegraf\")\n" +
          "\t|> elapsed(unit:%s)", chronoVals.get(cu))).isEqualTo(flux.toString());
      }
    }
  }
}
