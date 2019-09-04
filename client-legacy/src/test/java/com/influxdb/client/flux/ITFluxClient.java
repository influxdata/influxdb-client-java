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
package com.influxdb.client.flux;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

import com.influxdb.annotations.Column;
import com.influxdb.exceptions.InfluxException;
import com.influxdb.query.FluxColumn;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (31/07/2018 09:30)
 */
@RunWith(JUnitPlatform.class)
class ITFluxClient extends AbstractITFluxClient {

    private static final Logger LOG = Logger.getLogger(ITFluxClient.class.getName());

    private static final String FROM_FLUX_DATABASE = String
            .format("from(bucket:\"%s\") |> range(start: 0)", DATABASE_NAME);

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
    void chunkedOneTable() {

        prepareChunkRecords(DATABASE_NAME);

        String flux = FROM_FLUX_DATABASE + "\n"
                + "\t|> filter(fn: (r) => r[\"_measurement\"] == \"chunked\")\n"
                + "\t|> range(start: 1970-01-01T00:00:00.000000000Z)";

        fluxClient.query(flux, (cancellable, fluxRecord) -> {

            // +1 record
            countDownLatch.countDown();

            if (countDownLatch.getCount() % 100_000 == 0) {
                LOG.info(String.format("Remaining parsed: %s records", countDownLatch.getCount()));
            }
        });

        waitToCallback(30);
    }

    @Test
    void chunkedMoreTables() {

        prepareChunkRecords(DATABASE_NAME);

        String flux = FROM_FLUX_DATABASE + "\n"
                + "\t|> filter(fn: (r) => r[\"_measurement\"] == \"chunked\")\n"
                + "\t|> range(start: 1970-01-01T00:00:00.000000000Z)\n"
                + "\t|> window(every: 10m)";

        fluxClient.query(flux, (cancellable, fluxRecord) -> {

            // +1 record
            countDownLatch.countDown();

            if (countDownLatch.getCount() % 100_000 == 0) {
                LOG.info(String.format("Remaining parsed: %s records", countDownLatch.getCount()));
            }
        });

        waitToCallback(30);
    }

    @Test
    void chunkedCancel() {

        prepareChunkRecords(DATABASE_NAME);

        String flux = FROM_FLUX_DATABASE + "\n"
                + "\t|> filter(fn: (r) => r[\"_measurement\"] == \"chunked\")\n"
                + "\t|> range(start: 1970-01-01T00:00:00.000000000Z)\n"
                + "\t|> window(every: 10m)";

        countDownLatch = new CountDownLatch(10_000);
        CountDownLatch cancelCountDown = new CountDownLatch(1);

        fluxClient.query(flux, (cancellable, fluxRecord) -> {

            // +1 record
            countDownLatch.countDown();

            if (countDownLatch.getCount() % 1_000 == 0 && this.countDownLatch.getCount() != 0) {
                LOG.info(String.format("Remaining parsed: %s records", this.countDownLatch.getCount()));
            }

            if (countDownLatch.getCount() == 9_000) {
                cancellable.cancel();
                cancelCountDown.countDown();
            }
        });

        // wait to cancel
        waitToCallback(cancelCountDown, 30);

        //
        Assertions.assertThat(countDownLatch.getCount()).isEqualTo(9_000);
    }

    @Test
    void query() {

        String flux = FROM_FLUX_DATABASE + "\n"
                + "\t|> filter(fn: (r) => (r[\"_measurement\"] == \"mem\" and r[\"_field\"] == \"free\"))\n"
                + "\t|> sum()";

        List<FluxTable> fluxTables = fluxClient.query(flux);

        assertFluxResult(fluxTables);
    }

    @Test
    void queryWithTime() {

        String flux = FROM_FLUX_DATABASE + "\n"
                + "\t|> filter(fn: (r) => (r[\"_measurement\"] == \"mem\" and r[\"_field\"] == \"free\"))";

        List<FluxTable> fluxTables = fluxClient.query(flux);

        assertFluxResultWithTime(fluxTables);
    }

    @Test
    void queryDifferentSchemas() {

        String flux = FROM_FLUX_DATABASE + "\n"
                + "\t|> range(start: 1970-01-01T00:00:00.000000000Z)";

        List<FluxTable> fluxTables = fluxClient.query(flux);

        Assertions.assertThat(fluxTables).hasSize(6);
    }

    @Test
    void error() {

        Assertions.assertThatThrownBy(() -> fluxClient.query("from(bucket:\"telegraf\")"))
                .isInstanceOf(InfluxException.class)
                .hasMessageStartingWith("error in building plan while starting program:")
                .hasMessageEndingWith("try bounding 'from' with a call to 'range'");
    }

    @Test
    void callback() {

        countDownLatch = new CountDownLatch(3);
        List<FluxRecord> records = new ArrayList<>();

        String flux = FROM_FLUX_DATABASE + "\n"
                + "\t|> filter(fn: (r) => (r[\"_measurement\"] == \"mem\" and r[\"_field\"] == \"free\"))\n"
                + "\t|> sum()";

        fluxClient.query(flux, (cancellable, record) -> {

            records.add(record);

            countDownLatch.countDown();
        }, throwable -> Assertions.fail("Unreachable"), () -> countDownLatch.countDown());

        waitToCallback();
        assertFluxRecords(records);
    }

    @Test
    void callbackWhenConnectionRefuse() {

        FluxConnectionOptions options = FluxConnectionOptions.builder()
                .url("http://localhost:8003")
                .build();

        FluxClient fluxClient = FluxClientFactory.create(options);
        fluxClient.query(FROM_FLUX_DATABASE + " |> last()",
                (cancellable, record) -> {
                },
                throwable -> countDownLatch.countDown());

        waitToCallback();
    }


    @Test
    void callbackToMeasurement() {

        String flux = FROM_FLUX_DATABASE + "\n"
                + "\t|> range(start: 1970-01-01T00:00:00.000000000Z)\n"
                + "\t|> filter(fn: (r) => (r[\"_measurement\"] == \"mem\" and r[\"_field\"] == \"free\"))";

        List<Mem> memory = new ArrayList<>();
        countDownLatch = new CountDownLatch(4);

        fluxClient.query(flux, Mem.class, (cancellable, mem) -> {
            memory.add(mem);
            countDownLatch.countDown();
        });

        waitToCallback();

        Assertions.assertThat(memory).hasSize(4);

        Assertions.assertThat(memory.get(0).host).isEqualTo("A");
        Assertions.assertThat(memory.get(0).region).isEqualTo("west");
        Assertions.assertThat(memory.get(0).free).isEqualTo(10L);
        Assertions.assertThat(memory.get(0).time).isEqualTo(Instant.ofEpochSecond(10));

        Assertions.assertThat(memory.get(1).host).isEqualTo("A");
        Assertions.assertThat(memory.get(1).region).isEqualTo("west");
        Assertions.assertThat(memory.get(1).free).isEqualTo(11L);
        Assertions.assertThat(memory.get(1).time).isEqualTo(Instant.ofEpochSecond(20));

        Assertions.assertThat(memory.get(2).host).isEqualTo("B");
        Assertions.assertThat(memory.get(2).region).isEqualTo("west");
        Assertions.assertThat(memory.get(2).free).isEqualTo(20L);
        Assertions.assertThat(memory.get(2).time).isEqualTo(Instant.ofEpochSecond(10));

        Assertions.assertThat(memory.get(3).host).isEqualTo("B");
        Assertions.assertThat(memory.get(3).region).isEqualTo("west");
        Assertions.assertThat(memory.get(3).free).isEqualTo(22L);
        Assertions.assertThat(memory.get(3).time).isEqualTo(Instant.ofEpochSecond(20));
    }

    @Test
    void ping() {

        Assertions.assertThat(fluxClient.ping()).isTrue();
    }

    @Test
    void version() {
        Assertions.assertThat(fluxClient.version()).isNotBlank();
    }

    private void assertFluxResult(@Nonnull final List<FluxTable> tables) {

        Assertions.assertThat(tables).isNotNull();

        Assertions.assertThat(tables).hasSize(2);

        FluxTable table1 = tables.get(0);
        // Data types
        Assertions.assertThat(table1.getColumns()).hasSize(9);
        Assertions.assertThat(table1.getColumns().stream().map(FluxColumn::getDataType))
                .containsExactlyInAnyOrder("string", "long", "dateTime:RFC3339", "dateTime:RFC3339", "long", "string", "string", "string", "string");

        // Columns
        Assertions.assertThat(table1.getColumns().stream().map(FluxColumn::getLabel))
                .containsExactlyInAnyOrder("result", "table", "_start", "_stop", "_value", "_field", "_measurement", "host", "region");

        // Records
        Assertions.assertThat(table1.getRecords()).hasSize(1);

        List<FluxRecord> records = new ArrayList<>();
        records.add(table1.getRecords().get(0));
        records.add(tables.get(1).getRecords().get(0));
        assertFluxRecords(records);
    }

    private void assertFluxResultWithTime(@Nonnull final List<FluxTable> tables) {

        Assertions.assertThat(tables).isNotNull();

        Assertions.assertThat(tables).hasSize(2);

        FluxTable table1 = tables.get(0);
        // Data types
        Assertions.assertThat(table1.getColumns()).hasSize(10);
        Assertions.assertThat(table1.getColumns().stream().map(FluxColumn::getDataType))
                .containsExactlyInAnyOrder("string", "long", "dateTime:RFC3339", "dateTime:RFC3339", "dateTime:RFC3339", "long", "string", "string", "string", "string");

        // Columns
        Assertions.assertThat(table1.getColumns().stream().map(FluxColumn::getLabel))
                .containsExactlyInAnyOrder("result", "table", "_start", "_stop", "_time", "_value", "_field", "_measurement", "host", "region");

        // Records
        Assertions.assertThat(table1.getRecords()).hasSize(2);
        Assertions.assertThat(tables.get(1).getRecords()).hasSize(2);
    }

    private void assertFluxRecords(@Nonnull final List<FluxRecord> records) {
        Assertions.assertThat(records).isNotNull();
        Assertions.assertThat(records).hasSize(2);

        // Record 1
        FluxRecord record1 = records.get(0);
        Assertions.assertThat(record1.getMeasurement()).isEqualTo("mem");
        Assertions.assertThat(record1.getField()).isEqualTo("free");

        Assertions.assertThat(record1.getStart()).isEqualTo(Instant.EPOCH);
        Assertions.assertThat(record1.getStop()).isNotNull();
        Assertions.assertThat(record1.getTime()).isNull();

        Assertions.assertThat(record1.getValue()).isEqualTo(21L);

        Assertions.assertThat(record1.getValues())
                .hasEntrySatisfying("host", value -> Assertions.assertThat(value).isEqualTo("A"))
                .hasEntrySatisfying("region", value -> Assertions.assertThat(value).isEqualTo("west"));

        // Record 2
        FluxRecord record2 = records.get(1);
        Assertions.assertThat(record2.getMeasurement()).isEqualTo("mem");
        Assertions.assertThat(record2.getField()).isEqualTo("free");

        Assertions.assertThat(record2.getStart()).isEqualTo(Instant.EPOCH);
        Assertions.assertThat(record2.getStop()).isNotNull();
        Assertions.assertThat(record2.getTime()).isNull();

        Assertions.assertThat(record2.getValue()).isEqualTo(42L);

        Assertions.assertThat(record2.getValues())
                .hasEntrySatisfying("host", value -> Assertions.assertThat(value).isEqualTo("B"))
                .hasEntrySatisfying("region", value -> Assertions.assertThat(value).isEqualTo("west"));
    }

    public static class Mem {
        
        private String host;
        private String region;

        @Column(name = "_value")
        private Long free;
        @Column(name = "_time")
        private Instant time;
    }
}