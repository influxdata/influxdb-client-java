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

import java.time.temporal.ChronoUnit;
import javax.annotation.Nonnull;

import com.influxdb.query.dsl.Flux;
import com.influxdb.utils.Arguments;

/**
 * Applies an aggregate or selector function (any function with a column parameter) to fixed windows of time.
 * <a href="http://bit.ly/flux-spec#aggregateWindow">See SPEC</a>.
 *
 * <h3>Options</h3>
 * <ul>
 *     <li><b>every</b> - The duration of windows. [duration]</li>
 *     <li><b>fn</b> - The aggregate function used in the operation. [function]</li>
 *     <li><b>column</b> - The column on which to operate. Defaults to <i>"_value"</i>. [string]</li>
 *     <li><b>timeSrc</b> -
 *     The time column from which time is copied for the aggregate record. Defaults to <i>"_stop"</i>. [string]</li>
 *     <li><b>timeDst</b> -
 *     The “time destination” column to which time is copied for the aggregate record.
 *     Defaults to <i>"_time"</i>. [string]</li>
 *     <li><b>createEmpty</b> -
 *     For windows without data, this will create an empty window and fill it with a <i>null</i> aggregate value.
 *     Defaults to <i>true</i>. [boolean]</li>
 * </ul>
 *
 * <h3>Example</h3>
 * <pre>
 * Flux flux = Flux
 *     .from("telegraf")
 *     .aggregateWindow(10L, ChronoUnit.SECONDS, "mean");
 *
 * Flux flux = Flux
 *     .from("telegraf")
 *     .aggregateWindow()
 *         .withEvery("10s")
 *         .withAggregateFunction("sum")
 *         .withColumn("_value")
 *         .withTimeSrc("_stop")
 *         .withTimeDst("_time")
 *         .withCreateEmpty(true);
 *
 * Flux flux = Flux
 *     .from("telegraf")
 *     .aggregateWindow()
 *         .withEvery(5L, ChronoUnit.MINUTES)
 *         .withFunction("tables |&gt; quantile(q: 0.99, column:column)");
 * </pre>
 *
 * @author Jakub Bednar (13/05/2020 08:41)
 */
public final class AggregateWindow extends AbstractParametrizedFlux {

    public AggregateWindow(@Nonnull final Flux source) {
        super(source);
    }

    @Nonnull
    @Override
    protected String operatorName() {
        return "aggregateWindow";
    }

    /**
     * @param every     The duration of windows.
     * @param everyUnit a {@code ChronoUnit} determining how to interpret the {@code every}.
     * @return this
     */
    @Nonnull
    public AggregateWindow withEvery(@Nonnull final Long every, @Nonnull final ChronoUnit everyUnit) {

        Arguments.checkNotNull(every, "Every is required");
        Arguments.checkNotNull(everyUnit, "Every ChronoUnit is required");

        this.withPropertyValue("every", every, everyUnit);

        return this;
    }

    /**
     * @param every The duration of windows.
     * @return this
     */
    @Nonnull
    public AggregateWindow withEvery(@Nonnull final String every) {

        Arguments.checkDuration(every, "Every");

        this.withPropertyValue("every", every);

        return this;
    }

    /**
     * @param function specifies the aggregate operation to perform.
     * @return this
     */
    @Nonnull
    public AggregateWindow withFunction(@Nonnull final String function) {

        Arguments.checkNonEmpty(function, "Function");

        this.withFunction("fn: (column, tables=<-)", function);

        return this;
    }

    /**
     * @param namedFunction specifies the named aggregate operation to perform.
     * @return this
     */
    @Nonnull
    public AggregateWindow withAggregateFunction(@Nonnull final String namedFunction) {

        Arguments.checkNonEmpty(namedFunction, "Function");

        this.withPropertyValue("fn", namedFunction);

        return this;
    }

    /**
     * @param column The column on which to operate.
     * @return this
     */
    @Nonnull
    public AggregateWindow withColumn(@Nonnull final String column) {

        Arguments.checkNonEmpty(column, "Column");

        this.withPropertyValueEscaped("column", column);

        return this;
    }

    /**
     * @param timeSrc The time column from which time is copied for the aggregate record.
     * @return this
     */
    @Nonnull
    public AggregateWindow withTimeSrc(@Nonnull final String timeSrc) {

        Arguments.checkNonEmpty(timeSrc, "timeSrc");

        this.withPropertyValueEscaped("timeSrc", timeSrc);

        return this;
    }

    /**
     * @param timeDst The “time destination” column to which time is copied for the aggregate record.
     * @return this
     */
    @Nonnull
    public AggregateWindow withTimeDst(@Nonnull final String timeDst) {

        Arguments.checkNonEmpty(timeDst, "timeDst");

        this.withPropertyValueEscaped("timeDst", timeDst);

        return this;
    }

    /**
     * @param createEmpty For windows without data,
     *                    this will create an empty window and fill it with a <i>null</i> aggregate value.
     * @return this
     */
    @Nonnull
    public AggregateWindow withCreateEmpty(final boolean createEmpty) {

        this.withPropertyValue("createEmpty", createEmpty);

        return this;
    }
}
