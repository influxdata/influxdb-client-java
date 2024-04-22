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

import javax.annotation.Nonnull;

import com.influxdb.query.dsl.Flux;
import com.influxdb.query.dsl.functions.properties.TimeInterval;
import com.influxdb.utils.Arguments;

/**
 * Add an extra "elapsed" column to the result showing the time elapsed since the previous record in the series.
 *
 * <p>
 *   <b>Example</b>
 *   <pre>
 *    Flux flux = Flux.from("my-bucket")
 *        .range(Instant.now().minus(15, ChronoUnit.MINUTES), Instant.now())
 *        .filter(Restrictions.measurement().equal("wumpus"))
 *        .elapsed(new TimeInterval(100L, ChronoUnit.NANOS));
 *   </pre>
 *
 */
public class ElapsedFlux extends AbstractParametrizedFlux {

  public ElapsedFlux(@Nonnull final Flux source) {
    super(source);
  }

  @Nonnull
  @Override
  protected String operatorName() {
    return "elapsed";
  }

  /**
   *
   * @param duration - TimeInterval to be used for units when reporting elapsed period.
   * @return this
   */
  public ElapsedFlux withDuration(final TimeInterval duration) {
    Arguments.checkNotNull(duration, "Duration is required");

    this.withPropertyValue("unit", duration);
    return this;
  }
}
