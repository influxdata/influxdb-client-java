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
