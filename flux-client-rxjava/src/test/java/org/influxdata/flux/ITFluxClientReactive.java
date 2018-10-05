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
package org.influxdata.flux;

import java.time.Instant;

import org.influxdata.flux.domain.FluxRecord;

import io.reactivex.Flowable;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (28/06/2018 07:59)
 */
@RunWith(JUnitPlatform.class)
class ITFluxClientReactive extends AbstractITFluxClientReactive {

    private static final String FROM_FLUX_DATABASE = String
            .format("from(bucket:\"%s\")", AbstractITFluxClientReactive.DATABASE_NAME);

    @BeforeEach
    void prepareDate() {
        influxDBWrite("mem,host=A,region=west free=10i 10000000000", DATABASE_NAME);
        influxDBWrite("mem,host=A,region=west free=11i 20000000000", DATABASE_NAME);
        influxDBWrite("mem,host=B,region=west free=20i 10000000000", DATABASE_NAME);
        influxDBWrite("mem,host=B,region=west free=22i 20000000000", DATABASE_NAME);
        influxDBWrite("cpu,host=A,region=west usage_system=35i,user_usage=45i 10000000000", DATABASE_NAME);
        influxDBWrite("cpu,host=A,region=west usage_system=38i,user_usage=49i 20000000000", DATABASE_NAME);
        influxDBWrite("cpu,host=A,hyper-threading=true,region=west usage_system=38i,user_usage=49i 20000000000", DATABASE_NAME);
    }

    @Test
    void oneToOneTable() {

        String flux = FROM_FLUX_DATABASE + "\n"
                + "\t|> range(start: 1970-01-01T00:00:00.000000000Z)\n"
                + "\t|> filter(fn: (r) => (r[\"_measurement\"] == \"mem\" AND r[\"_field\"] == \"free\"))\n"
                + "\t|> sum()";

        Flowable<FluxRecord> results = fluxClient.query(flux);

        results
                .test()
                .assertValueCount(2)
                .assertValueAt(0, fluxRecord -> {

                    Assertions.assertThat(fluxRecord.getMeasurement()).isEqualTo("mem");
                    Assertions.assertThat(fluxRecord.getField()).isEqualTo("free");

                    Assertions.assertThat(fluxRecord.getStart()).isEqualTo(Instant.EPOCH);
                    Assertions.assertThat(fluxRecord.getStop()).isNotNull();
                    Assertions.assertThat(fluxRecord.getTime()).isNull();

                    Assertions.assertThat(fluxRecord.getValue()).isEqualTo(21L);

                    Assertions.assertThat(fluxRecord.getValues())
                            .hasEntrySatisfying("host", value -> Assertions.assertThat(value).isEqualTo("A"))
                            .hasEntrySatisfying("region", value -> Assertions.assertThat(value).isEqualTo("west"));

                    return true;
                })
                .assertValueAt(1, fluxRecord -> {

                    // Record 2
                    Assertions.assertThat(fluxRecord.getMeasurement()).isEqualTo("mem");
                    Assertions.assertThat(fluxRecord.getField()).isEqualTo("free");

                    Assertions.assertThat(fluxRecord.getStart()).isEqualTo(Instant.EPOCH);
                    Assertions.assertThat(fluxRecord.getStop()).isNotNull();
                    Assertions.assertThat(fluxRecord.getTime()).isNull();

                    Assertions.assertThat(fluxRecord.getValue()).isEqualTo(42L);

                    Assertions.assertThat(fluxRecord.getValues())
                            .hasEntrySatisfying("host", value -> Assertions.assertThat(value).isEqualTo("B"))
                            .hasEntrySatisfying("region", value -> Assertions.assertThat(value).isEqualTo("west"));

                    return true;
                });
    }

    @Test
    @Disabled
    void oneToManyTable() {

        String flux = FROM_FLUX_DATABASE + "\n"
                + "\t|> filter(fn: (r) => r[\"_measurement\"] == \"chunked\")\n"
                + "\t|> range(start: 1970-01-01T00:00:00.000000000Z)\n"
                + "\t|> window(every: 10m)";

        Flowable<FluxRecord> results = fluxClient.query(flux);

        results
                .test()
                .assertValueCount(4)
                .assertValueAt(0, fluxRecord -> {

                    Assertions.assertThat(fluxRecord.getMeasurement()).isEqualTo("mem");
                    Assertions.assertThat(fluxRecord.getField()).isEqualTo("free");

                    Assertions.assertThat(fluxRecord.getValues())
                            .hasEntrySatisfying("host", value -> Assertions.assertThat(value).isEqualTo("A"))
                            .hasEntrySatisfying("region", value -> Assertions.assertThat(value).isEqualTo("west"));

                    Assertions.assertThat(fluxRecord.getValue()).isEqualTo(10L);

                    return true;
                })
                .assertValueAt(1, fluxRecord -> {

                    Assertions.assertThat(fluxRecord.getMeasurement()).isEqualTo("mem");
                    Assertions.assertThat(fluxRecord.getField()).isEqualTo("free");

                    Assertions.assertThat(fluxRecord.getValues())
                            .hasEntrySatisfying("host", value -> Assertions.assertThat(value).isEqualTo("B"))
                            .hasEntrySatisfying("region", value -> Assertions.assertThat(value).isEqualTo("west"));

                    Assertions.assertThat(fluxRecord.getValue()).isEqualTo(20L);

                    return true;
                })
                .assertValueAt(2, fluxRecord -> {


                    Assertions.assertThat(fluxRecord.getMeasurement()).isEqualTo("mem");
                    Assertions.assertThat(fluxRecord.getField()).isEqualTo("free");

                    Assertions.assertThat(fluxRecord.getValues())
                            .hasEntrySatisfying("host", value -> Assertions.assertThat(value).isEqualTo("A"))
                            .hasEntrySatisfying("region", value -> Assertions.assertThat(value).isEqualTo("west"));

                    Assertions.assertThat(fluxRecord.getValue()).isEqualTo(11L);

                    return true;
                })
                .assertValueAt(3, fluxRecord -> {

                    Assertions.assertThat(fluxRecord.getMeasurement()).isEqualTo("mem");
                    Assertions.assertThat(fluxRecord.getField()).isEqualTo("free");

                    Assertions.assertThat(fluxRecord.getValues())
                            .hasEntrySatisfying("host", value -> Assertions.assertThat(value).isEqualTo("B"))
                            .hasEntrySatisfying("region", value -> Assertions.assertThat(value).isEqualTo("west"));

                    Assertions.assertThat(fluxRecord.getValue()).isEqualTo(22L);

                    return true;
                });
    }

    @Test
    @Disabled
    void manyToOne() {

        String flux = "from(bucket:\"flux_database\")\n"
                + "\t|> range(start: 1970-01-01T00:00:00.000000000Z)\n"
                + "\t|> filter(fn: (r) => (r[\"_measurement\"] == \"mem\" AND r[\"_field\"] == \"free\"))\n"
                + "\t|> window(every: 10s)\n"
                + "\t|> group(by: [\"region\"])";

        Flowable<FluxRecord> results = fluxClient.query(flux);

        results
                .test()
                .assertValueCount(4)
                .assertValueAt(0, fluxRecord -> {

                    // Record1
                    Assertions.assertThat(fluxRecord.getMeasurement()).isEqualTo("mem");
                    Assertions.assertThat(fluxRecord.getField()).isEqualTo("free");

                    Assertions.assertThat(fluxRecord.getValues())
                            .hasEntrySatisfying("host", value -> Assertions.assertThat(value).isEqualTo("A"))
                            .hasEntrySatisfying("region", value -> Assertions.assertThat(value).isEqualTo("west"));

                    Assertions.assertThat(fluxRecord.getValue()).isEqualTo(10L);

                    return true;
                })
                .assertValueAt(1, fluxRecord -> {

                    Assertions.assertThat(fluxRecord.getMeasurement()).isEqualTo("mem");
                    Assertions.assertThat(fluxRecord.getField()).isEqualTo("free");

                    Assertions.assertThat(fluxRecord.getValues())
                            .hasEntrySatisfying("host", value -> Assertions.assertThat(value).isEqualTo("B"))
                            .hasEntrySatisfying("region", value -> Assertions.assertThat(value).isEqualTo("west"));

                    Assertions.assertThat(fluxRecord.getValue()).isEqualTo(20L);

                    return true;
                })
                .assertValueAt(2, fluxRecord -> {

                    // Record3
                    Assertions.assertThat(fluxRecord.getMeasurement()).isEqualTo("mem");
                    Assertions.assertThat(fluxRecord.getField()).isEqualTo("free");

                    Assertions.assertThat(fluxRecord.getValues())
                            .hasEntrySatisfying("host", value -> Assertions.assertThat(value).isEqualTo("A"))
                            .hasEntrySatisfying("region", value -> Assertions.assertThat(value).isEqualTo("west"));

                    Assertions.assertThat(fluxRecord.getValue()).isEqualTo(11L);

                    return true;
                })
                .assertValueAt(3, fluxRecord -> {

                    // Record4
                    Assertions.assertThat(fluxRecord.getMeasurement()).isEqualTo("mem");
                    Assertions.assertThat(fluxRecord.getField()).isEqualTo("free");

                    Assertions.assertThat(fluxRecord.getValues())
                            .hasEntrySatisfying("host", value -> Assertions.assertThat(value).isEqualTo("B"))
                            .hasEntrySatisfying("region", value -> Assertions.assertThat(value).isEqualTo("west"));

                    Assertions.assertThat(fluxRecord.getValue()).isEqualTo(22L);

                    return true;
                })
        ;
    }

    @Test
    void ping() {

        fluxClient
                .ping()
                .test()
                .assertValue(true);
    }

    @Test
    void version() {
        fluxClient.version().test().assertValue(version -> !version.isEmpty());
    }
}
