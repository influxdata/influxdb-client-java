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

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A InfluxQL query.
 */
public class InfluxQLQuery {

    private final String command;
    private final String database;
    private String retentionPolicy;
    private InfluxQLPrecision precision;
    private AcceptHeader acceptHeader;

    /**
     * @param command the InfluxQL command to execute
     * @param database the database to run this query against
     */
    public InfluxQLQuery(@Nonnull final String command, @Nonnull final String database) {
        this.command = command;
        this.database = database;
        this.acceptHeader = AcceptHeader.JSON;
    }

    /**
     * @param command the InfluxQL command to execute
     * @param database the database to run this query against
     * @param acceptHeader the <code>Accept</code> header to use in the request
     */
    public InfluxQLQuery(@Nonnull final String command,
                         @Nonnull final String database,
                         @Nonnull final AcceptHeader acceptHeader) {
        this.command = command;
        this.database = database;
        this.acceptHeader = acceptHeader;
    }

    /**
     * @return the InfluxQL command to execute
     */
    @Nonnull
    public String getCommand() {
        return command;
    }

    /**
     * @return the database to run this query against
     */
    @Nonnull
    public String getDatabase() {
        return database;
    }

    /**
     * @return the retention policy to use
     */
    @Nullable
    public String getRetentionPolicy() {
        return retentionPolicy;
    }

    /**
     * @param retentionPolicy the retention policy to use
     * @return this
     */
    @Nonnull
    public InfluxQLQuery setRetentionPolicy(@Nullable final String retentionPolicy) {
        this.retentionPolicy = retentionPolicy;
        return this;
    }

    /**
     * @return The precision used for the timestamps returned by the query
     */
    @Nullable
    public InfluxQLPrecision getPrecision() {
        return precision;
    }

    /**
     *
     * @param precision The precision used for the timestamps returned by the query
     * @return this
     */
    @Nonnull
    public InfluxQLQuery setPrecision(@Nullable final InfluxQLPrecision precision) {
        this.precision = precision;
        return this;
    }

    /**
     * @return the current AcceptHeader used when making queries.
     */
    public AcceptHeader getAcceptHeader() {
        return acceptHeader;
    }

    /***
     * @param acceptHeader the AcceptHeader to be used when making queries.
     */
    public void setAcceptHeader(final AcceptHeader acceptHeader) {
        this.acceptHeader = acceptHeader;
    }

    /**
     * @return the string value of the AcceptHeader used when making queries.
     */
    public String getAcceptHeaderVal() {
        return acceptHeader != null ? acceptHeader.getVal() : AcceptHeader.JSON.getVal();
    }

    /**
     * The precision used for the timestamps returned by InfluxQL queries.
     */
    public enum InfluxQLPrecision {
        HOURS("h"),
        MINUTES("m"),
        SECONDS("s"),
        MILLISECONDS("ms"),
        MICROSECONDS("u"),
        NANOSECONDS("n");

        private final String symbol;

        InfluxQLPrecision(final String symbol) {
            this.symbol = symbol;
        }

        /**
         * @return the InfluxQL specific symbol
         */
        @Nonnull
        public String getSymbol() {
            return symbol;
        }

        @Nonnull
        public static InfluxQLPrecision toTimePrecision(final TimeUnit t) {
            switch (t) {
                case HOURS:
                    return HOURS;
                case MINUTES:
                    return MINUTES;
                case SECONDS:
                    return SECONDS;
                case MILLISECONDS:
                    return MILLISECONDS;
                case MICROSECONDS:
                    return MICROSECONDS;
                case NANOSECONDS:
                    return NANOSECONDS;
                default:
                    throw new IllegalArgumentException("time precision must be one of:"
                            + Arrays.toString(InfluxQLPrecision.values()));
            }
        }
    }

    /**
     * The possible values to be used in the header <code>Accept</code>, when making queries.
     */
    public enum AcceptHeader {
        JSON("application/json"),
        CSV("application/csv");

        private final String val;

        AcceptHeader(final String val) {
            this.val = val;
        }

        public String getVal() {
            return val;
        }
    }
}
