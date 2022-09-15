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
package com.influxdb.client.reactive;

import java.time.Instant;
import java.util.Arrays;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.domain.Authorization;
import com.influxdb.client.domain.Bucket;
import com.influxdb.client.domain.Dialect;
import com.influxdb.client.domain.Permission;
import com.influxdb.client.domain.PermissionResource;
import com.influxdb.client.domain.Query;
import com.influxdb.client.domain.User;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.internal.AbstractInfluxDBClient;
import com.influxdb.client.write.Point;
import com.influxdb.exceptions.BadRequestException;
import com.influxdb.query.FluxRecord;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;

/**
 * @author Jakub Bednar (bednar@github) (22/11/2018 06:59)
 */
class ITWriteQueryReactiveApi extends AbstractITInfluxDBClientTest {

    private WriteReactiveApi writeClient;
    private QueryReactiveApi queryClient;

    private Bucket bucket;
    private String token;

    @BeforeEach
    void setUp() throws Exception {

        super.setUp();

        InfluxDBClient client = InfluxDBClientFactory.create(influxDB_URL, "my-token".toCharArray());

        bucket = client.getBucketsApi()
                .createBucket(generateName("h2o"), null, organization);

        //
        // Add Permissions to read and write to the Bucket
        //

        PermissionResource resource = new PermissionResource();
        resource.setOrgID(organization.getId());
        resource.setType(PermissionResource.TYPE_BUCKETS);
        resource.setId(bucket.getId());

        Permission readBucket = new Permission();
        readBucket.setResource(resource);
        readBucket.setAction(Permission.ActionEnum.READ);

        Permission writeBucket = new Permission();
        writeBucket.setResource(resource);
        writeBucket.setAction(Permission.ActionEnum.WRITE);

        User loggedUser = client.getUsersApi().me();
        Assertions.assertThat(loggedUser).isNotNull();

        Authorization authorization = client.getAuthorizationsApi()
                .createAuthorization(organization, Arrays.asList(readBucket, writeBucket));

        token = authorization.getToken();

        client.close();

        influxDBClient.close();
        influxDBClient = InfluxDBClientReactiveFactory.create(influxDB_URL, token.toCharArray(), organization.getId(), bucket.getId());
        queryClient = influxDBClient.getQueryReactiveApi();
        writeClient = influxDBClient.getWriteReactiveApi();
    }

    @AfterEach
    void tearDown() {
        influxDBClient.close();
    }

    @Test
    void writeRecord() {

        String bucketName = bucket.getName();

        String lineProtocol = "h2o_feet,location=coyote_creek level\\ water_level=1.0 1";

        Publisher<WriteReactiveApi.Success> success = writeClient.writeRecord(WritePrecision.NS, lineProtocol);
        Flowable.fromPublisher(success)
                .test()
                .assertValueCount(1);

        Publisher<FluxRecord> result = queryClient.query("from(bucket:\"" + bucketName + "\") |> range(start: 1970-01-01T00:00:00.000000001Z)", organization.getId());

        Flowable.fromPublisher(result)
                .test()
                .assertValueCount(1)
                .assertValue(fluxRecord -> {

                    Assertions.assertThat(fluxRecord.getMeasurement()).isEqualTo("h2o_feet");
                    Assertions.assertThat(fluxRecord.getValue()).isEqualTo(1.0D);
                    Assertions.assertThat(fluxRecord.getField()).isEqualTo("level water_level");
                    Assertions.assertThat(fluxRecord.getTime()).isEqualTo(Instant.ofEpochSecond(0, 1));

                    return true;
                });
    }

    @Test
    void writeRecordEmpty() {

        Publisher<WriteReactiveApi.Success> success = writeClient.writeRecord(WritePrecision.S, null);
        Flowable.fromPublisher(success)
                .test()
                .assertValueCount(1);
    }

    @Test
    void writeRecords() {

        writeClient = influxDBClient.getWriteReactiveApi(WriteOptionsReactive.builder().batchSize(1).build());

        Publisher<String> records = Flowable.just(
                "h2o_feet,location=coyote_creek level\\ water_level=1.0 1",
                "h2o_feet,location=coyote_creek level\\ water_level=2.0 2");

        Publisher<WriteReactiveApi.Success> success = writeClient.writeRecords(WritePrecision.S, records);
        Flowable.fromPublisher(success)
                .test()
                .assertValueCount(2);

        Publisher<FluxRecord> results = queryClient.query("from(bucket:\"" + bucket.getName() + "\") |> range(start: 1970-01-01T00:00:00.000000001Z)");

        Flowable.fromPublisher(results)
                .test()
                .assertValueCount(2)
                .assertValueAt(0, fluxRecord -> {

                    Assertions.assertThat(fluxRecord.getMeasurement()).isEqualTo("h2o_feet");
                    Assertions.assertThat(fluxRecord.getValue()).isEqualTo(1.0D);
                    Assertions.assertThat(fluxRecord.getField()).isEqualTo("level water_level");
                    Assertions.assertThat(fluxRecord.getTime()).isEqualTo(Instant.ofEpochSecond(1));

                    return true;
                })
                .assertValueAt(1, fluxRecord -> {

                    Assertions.assertThat(fluxRecord.getMeasurement()).isEqualTo("h2o_feet");
                    Assertions.assertThat(fluxRecord.getValue()).isEqualTo(2.0D);
                    Assertions.assertThat(fluxRecord.getField()).isEqualTo("level water_level");
                    Assertions.assertThat(fluxRecord.getTime()).isEqualTo(Instant.ofEpochSecond(2));

                    return true;
                });
    }

    @Test
    void writePoint() {

        Instant time = Instant.ofEpochSecond(1000);
        Point point = Point.measurement("h2o_feet").addTag("location", "south").addField("water_level", 1).time(time, WritePrecision.MS);

        Publisher<WriteReactiveApi.Success> success = writeClient.writePoint(WritePrecision.MS, point);
        Flowable.fromPublisher(success)
                .test()
                .assertValueCount(1);

        Publisher<FluxRecord> result = queryClient.query("from(bucket:\"" + bucket.getName() + "\") |> range(start: 1970-01-01T00:00:00.000000001Z) |> last()");

        Flowable.fromPublisher(result)
                .test()
                .assertValueCount(1)
                .assertValue(fluxRecord -> {

                    Assertions.assertThat(fluxRecord.getMeasurement()).isEqualTo("h2o_feet");
                    Assertions.assertThat(fluxRecord.getValue()).isEqualTo(1L);
                    Assertions.assertThat(fluxRecord.getField()).isEqualTo("water_level");
                    Assertions.assertThat(fluxRecord.getValueByKey("location")).isEqualTo("south");
                    Assertions.assertThat(fluxRecord.getTime()).isEqualTo(time);

                    return true;
                });
    }

    @Test
    void writePoints() {

        Instant time = Instant.ofEpochSecond(2000);

        Point point1 = Point.measurement("h2o_feet").addTag("location", "west").addField("water_level", 1).time(time, WritePrecision.MS);
        Point point2 = Point.measurement("h2o_feet").addTag("location", "west").addField("water_level", 2).time(time.plusSeconds(10), WritePrecision.S);

        Publisher<WriteReactiveApi.Success> success = writeClient.writePoints(WritePrecision.S, Flowable.just(point1, point2));
        Flowable.fromPublisher(success)
                .test()
                .assertValueCount(1);

        Publisher<FluxRecord> result = queryClient.query("from(bucket:\"" + bucket.getName() + "\") |> range(start: 1970-01-01T00:00:00.000000001Z)");

        Flowable.fromPublisher(result)
                .test()
                .assertValueCount(2);
    }

    @Test
    void writeMeasurement() {

        H2O measurement = new H2O();
        measurement.location = "coyote_creek";
        measurement.level = 2.927;
        measurement.time = Instant.ofEpochSecond(10);

        Publisher<WriteReactiveApi.Success> success = writeClient.writeMeasurement(WritePrecision.NS, measurement);
        Flowable.fromPublisher(success)
                .test()
                .assertValueCount(1);

        Publisher<H2O> measurements = queryClient.query("from(bucket:\"" + bucket.getName() + "\") |> range(start: 1970-01-01T00:00:00.000000001Z) |> last() |> rename(columns:{_value: \"water_level\"})", H2O.class);

        Flowable.fromPublisher(measurements)
                .test()
                .assertValueCount(1)
                .assertValueAt(0, h2O -> {

                    Assertions.assertThat(h2O.location).isEqualTo("coyote_creek");
                    Assertions.assertThat(h2O.description).isNull();
                    Assertions.assertThat(h2O.level).isEqualTo(2.927);
                    Assertions.assertThat(h2O.time).isEqualTo(measurement.time);

                    return true;
                });
    }

    @Test
    void writeMeasurements() {

        Instant time = Instant.ofEpochSecond(50);

        H2O measurement1 = new H2O();
        measurement1.location = "coyote_creek";
        measurement1.level = 2.927;
        measurement1.time = time;

        H2O measurement2 = new H2O();
        measurement2.location = "bohemia";
        measurement2.level = 3.927;
        measurement2.time = time.plusSeconds(1);

        Publisher<WriteReactiveApi.Success> success = writeClient.writeMeasurements(WritePrecision.NS, Flowable.just(measurement1, measurement2));
        Flowable.fromPublisher(success)
                .test()
                .assertValueCount(1);

        Publisher<H2O> measurements = queryClient.query("from(bucket:\"" + bucket.getName() + "\") |> range(start: 1970-01-01T00:00:00.000000001Z) |> sort(desc: false, columns:[\"_time\"]) |>rename(columns:{_value: \"water_level\"})", H2O.class);

        Flowable.fromPublisher(measurements)
                .test()
                .assertValueCount(2)
                .assertValueAt(1, h2O -> {

                    Assertions.assertThat(h2O.location).isEqualTo("coyote_creek");
                    Assertions.assertThat(h2O.description).isNull();
                    Assertions.assertThat(h2O.level).isEqualTo(2.927);
                    Assertions.assertThat(h2O.time).isEqualTo(measurement1.time);

                    return true;
                })
                .assertValueAt(0, h2O -> {

                    Assertions.assertThat(h2O.location).isEqualTo("bohemia");
                    Assertions.assertThat(h2O.description).isNull();
                    Assertions.assertThat(h2O.level).isEqualTo(3.927);
                    Assertions.assertThat(h2O.time).isEqualTo(measurement2.time);

                    return true;
                });
    }

    @Test
    void queryRaw() {

        Publisher<WriteReactiveApi.Success> success = writeClient.writeRecord(WritePrecision.NS, "h2o_feet,location=coyote_creek level\\ water_level=1.0 1");
        Flowable.fromPublisher(success)
                .test()
                .assertValueCount(1);

        Publisher<String> result = queryClient.queryRaw("from(bucket:\"" + bucket.getName() + "\") |> range(start: 1970-01-01T00:00:00.000000001Z)");

        Flowable.fromPublisher(result)
                .test()
                .assertValueCount(6)
                .assertValueAt(0, "#datatype,string,long,dateTime:RFC3339,dateTime:RFC3339,dateTime:RFC3339,double,string,string,string")
                .assertValueAt(1, "#group,false,false,true,true,false,false,true,true,true")
                .assertValueAt(2, "#default,_result,,,,,,,,")
                .assertValueAt(3, ",result,table,_start,_stop,_time,_value,_field,_measurement,location")
                .assertValueAt(4, value -> {

                    Assertions.assertThat(value).endsWith("1970-01-01T00:00:00.000000001Z,1,level water_level,h2o_feet,coyote_creek");

                    return true;
                })
                .assertValueAt(5, "");
    }

    @Test
    void queryRawDialect() {

        Publisher<WriteReactiveApi.Success> success = writeClient.writeRecord(WritePrecision.NS, "h2o_feet,location=coyote_creek level\\ water_level=1.0 1");
        Flowable.fromPublisher(success)
                .test()
                .assertValueCount(1);

        Publisher<String> result = queryClient.queryRaw("from(bucket:\"" + bucket.getName() + "\") |> range(start: 1970-01-01T00:00:00.000000001Z)", (Dialect) null);

        Flowable.fromPublisher(result)
                .test()
                .assertValueCount(3)
                .assertValueAt(0, ",result,table,_start,_stop,_time,_value,_field,_measurement,location")
                .assertValueAt(1, value -> {

                    Assertions.assertThat(value).endsWith("1970-01-01T00:00:00.000000001Z,1,level water_level,h2o_feet,coyote_creek");

                    return true;
                })
                .assertValueAt(2, "");
    }

    @Test
    @Disabled("https://github.com/influxdata/influxdb/issues/16109")
    void queryRawParams() {

        Publisher<WriteReactiveApi.Success> success = writeClient.writeRecord(WritePrecision.NS,
            "h2o_feet,location=coyote_creek level\\ water_level=1.0 1");

        Flowable.fromPublisher(success)
            .test()
            .assertValueCount(1);

        Publisher<String> result = queryClient.queryRaw(
            new Query().query("from(bucket: params.bucketParam) |> range(start: 1970-01-01T00:00:00.000000001Z)")
                .dialect(null).putParamsItem("bucketParam", bucket.getName()));

        Flowable.fromPublisher(result)
            .test()
            .assertValueCount(3)
            .assertValueAt(0, ",result,table,_start,_stop,_time,_value,_field,_measurement,location")
            .assertValueAt(1, value -> {

                Assertions.assertThat(value).endsWith("1970-01-01T00:00:00.000000001Z,1,level water_level,h2o_feet,coyote_creek");

                return true;
            })
            .assertValueAt(2, "");
    }


    @Test
    public void subscribeOn() {

        Publisher<WriteReactiveApi.Success> success = writeClient.writeRecord(WritePrecision.NS,
                "h2o_feet,location=coyote_creek level\\ water_level=1.0 1");

        Flowable.fromPublisher(success)
                .subscribeOn(Schedulers.newThread())
                .test()
                .awaitCount(1)
                .assertValueCount(1)
                .assertComplete();
    }

    @Test
    public void defaultOrgBucket() {

        InfluxDBClientReactive client = InfluxDBClientReactiveFactory.create(influxDB_URL, token.toCharArray(), organization.getId(), bucket.getName());

        WriteReactiveApi writeApi = client.getWriteReactiveApi();

        Publisher<WriteReactiveApi.Success> success = writeApi.writeRecord(bucket.getId(), organization.getName(), WritePrecision.NS, "h2o,location=north water_level=60.0 1");
        Flowable.fromPublisher(success)
                .test()
                .assertValueCount(1);

        QueryReactiveApi queryApi = client.getQueryReactiveApi();

        String query = "from(bucket:\"" + bucket.getName() + "\") |> range(start: 1970-01-01T00:00:00.000000001Z)";
        // String
        {
            Publisher<FluxRecord> result = queryApi.query(query);

            Flowable.fromPublisher(result)
                    .test()
                    .assertValueCount(1)
                    .assertValue(fluxRecord -> {

                        Assertions.assertThat(fluxRecord.getValue()).isEqualTo(60.0);

                        return true;
                    });
        }

        // Publisher
        {
            Publisher<FluxRecord> result = queryApi.query(Flowable.just(query));

            Flowable.fromPublisher(result)
                    .test()
                    .assertValueCount(1)
                    .assertValue(fluxRecord -> {

                        Assertions.assertThat(fluxRecord.getValue()).isEqualTo(60.0);

                        return true;
                    });
        }

        // Measurement
        {
            Publisher<H2O> result = queryApi.query(query, H2O.class);

            Flowable.fromPublisher(result).test().assertValueCount(1);
        }

        // Raw
        {
            Publisher<String> result = queryApi.queryRaw(query);
            Flowable.fromPublisher(result).test().assertValueCount(6);

            result = queryApi.queryRaw(Flowable.just(query));
            Flowable.fromPublisher(result).test().assertValueCount(6);

            result = queryApi.queryRaw(query, AbstractInfluxDBClient.DEFAULT_DIALECT);
            Flowable.fromPublisher(result).test().assertValueCount(6);

            result = queryApi.queryRaw(Flowable.just(query), AbstractInfluxDBClient.DEFAULT_DIALECT);
            Flowable.fromPublisher(result).test().assertValueCount(6);
        }

        client.close();
    }

    @Test
    public void propagateError() {

        writeClient = influxDBClient.getWriteReactiveApi();

        Publisher<WriteReactiveApi.Success> success = writeClient.writeRecord(bucket.getName(), organization.getId(), WritePrecision.S, "not.valid.lp");
        Flowable.fromPublisher(success)
                .test()
                .assertError(throwable -> {
                    Assertions.assertThat(throwable).isInstanceOf(BadRequestException.class);
                    Assertions.assertThat(throwable).hasMessageContaining("unable to parse");
                    return true;
                });
    }

    @Measurement(name = "h2o")
    public static class H2O {

        @Column(name = "location", tag = true)
        String location;

        @Column(name = "water_level")
        Double level;

        @Column(name = "level description")
        String description;

        @Column(name = "time", timestamp = true)
        Instant time;
    }
}