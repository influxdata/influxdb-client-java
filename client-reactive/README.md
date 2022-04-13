# influxdb-client-reactive

[![javadoc](https://img.shields.io/badge/javadoc-link-brightgreen.svg)](https://influxdata.github.io/influxdb-client-java/influxdb-client-reactive/apidocs/index.html)

The reference reactive Java client for InfluxDB 2.x. The client provide supports for asynchronous stream processing with backpressure as is defined by the [Reactive Streams specification](http://www.reactive-streams.org/).

## Important

> :warning: The `Publishers` returned from [Query](src/main/java/com/influxdb/client/reactive/QueryReactiveApi.java) and [Write](src/main/java/com/influxdb/client/reactive/WriteReactiveApi.java) API are cold.
That means no request to InfluxDB is trigger until register a subscription to `Publisher`.  

## Documentation

This section contains links to the client library documentation.

* [Product documentation](https://docs.influxdata.com/influxdb/latest/api-guide/client-libraries/), [Getting Started](#queries)
* [Examples](../examples)
* [API Reference](https://influxdata.github.io/influxdb-client-java/influxdb-client-reactive/apidocs/index.html)
* [Changelog](../CHANGELOG.md)

## Features
 
- [Querying data using Flux language](#queries)
- [Writing data using](#writes)
   - Line Protocol
   - Data Point
   - POJO
- [Advanced Usage](#advanced-usage)
         
## Queries

For querying data we use [QueryReactiveApi](https://influxdata.github.io/influxdb-client-java/influxdb-client-reactive/apidocs/com/influxdb/client/reactive/QueryReactiveApi.html) that use [Reactive-Streams Pattern](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Flowable.html) for streaming query results and also support query raw response.

The following example demonstrates querying using the Flux language:

```java
package example;

import com.influxdb.client.reactive.InfluxDBClientReactive;
import com.influxdb.client.reactive.InfluxDBClientReactiveFactory;
import com.influxdb.client.reactive.QueryReactiveApi;

import io.reactivex.Flowable;

public class InfluxDB2ReactiveExample {

    private static char[] token = "my-token".toCharArray();
    private static String org = "my-org";

    public static void main(final String[] args) {

        InfluxDBClientReactive influxDBClient = InfluxDBClientReactiveFactory.create("http://localhost:8086", token, org);

        //
        // Query data
        //
        String flux = "from(bucket:\"my-bucket\") |> range(start: 0)";

        QueryReactiveApi queryApi = influxDBClient.getQueryReactiveApi();

        Flowable.fromPublisher(queryApi.query(flux))
                //
                // Filter records by measurement name
                //
                .filter(it -> "temperature".equals(it.getMeasurement()))
                //
                // Take first 10 records
                //
                .take(10)
                .subscribe(fluxRecord -> {
                    //
                    // The callback to consume a FluxRecord.
                    //
                    System.out.println(fluxRecord.getTime() + ": " + fluxRecord.getValueByKey("_value"));
                });

        influxDBClient.close();
    }
}
```

The Raw query allows direct processing original [CSV response](http://bit.ly/flux-spec#csv): 

```java
package example;

import com.influxdb.client.reactive.InfluxDBClientReactive;
import com.influxdb.client.reactive.InfluxDBClientReactiveFactory;
import com.influxdb.client.reactive.QueryReactiveApi;

import io.reactivex.Flowable;

public class InfluxDB2ReactiveExampleRaw {

    private static char[] token = "my-token".toCharArray();
    private static String org = "my-org";

    public static void main(final String[] args) {

        InfluxDBClientReactive influxDBClient = InfluxDBClientReactiveFactory.create("http://localhost:8086", token, org);

        //
        // Query data
        //
        String flux = "from(bucket:\"my-bucket\") |> range(start: 0)";

        QueryReactiveApi queryApi = influxDBClient.getQueryReactiveApi();

        Flowable.fromPublisher(queryApi.queryRaw(flux))
                //
                // Take first 10 records
                //
                .take(10)
                .subscribe(line -> {
                    //
                    // The callback to consume a line of CSV response
                    //
                    System.out.println("Response: " + line);
                });

        influxDBClient.close();
    }
}
```

The mapping result to POJO is also support:

```java
package example;

import java.time.Instant;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import com.influxdb.client.reactive.InfluxDBClientReactive;
import com.influxdb.client.reactive.InfluxDBClientReactiveFactory;
import com.influxdb.client.reactive.QueryReactiveApi;

import io.reactivex.Flowable;
import org.reactivestreams.Publisher;

public class InfluxDB2ReactiveExamplePojo {

    private static char[] token = "my-token".toCharArray();
    private static String org = "my-org";

    public static void main(final String[] args) {

        InfluxDBClientReactive influxDBClient = InfluxDBClientReactiveFactory.create("http://localhost:8086", token, org);
        //
        // Query data
        //
        String flux = "from(bucket:\"my-bucket\") |> range(start: 0) |> filter(fn: (r) => r._measurement == \"temperature\")";

        QueryReactiveApi queryApi = influxDBClient.getQueryReactiveApi();

        Publisher<Temperature> query = queryApi.query(flux, Temperature.class);
        Flowable.fromPublisher(query)
                //
                // Take first 10 records
                //
                .take(10)
                .subscribe(temperature -> {
                    //
                    // The callback to consume a FluxRecord mapped to POJO.
                    //
                    System.out.println(temperature.location + ": " + temperature.value + " at " + temperature.time);
                });

        influxDBClient.close();
    }

    @Measurement(name = "temperature")
    public static class Temperature {

        @Column(tag = true)
        String location;

        @Column
        Double value;

        @Column(timestamp = true)
        Instant time;
    }
}
```

## Writes

For writing data we use [`WriteReactiveApi`](src/main/java/com/influxdb/client/reactive/WriteReactiveApi.java) 
that supports writing data using Line Protocol, Data Point or POJO. The [GZIP compression](#gzip-support) is also supported.

The writes are configurable by [`WriteOptionsReactive`](src/main/java/com/influxdb/client/reactive/WriteOptionsReactive.java):

| Property | Description | Default Value |
| --- | --- | --- |
| **batchSize** | the number of data point to collect in batch. The `0` disable batching - whole upstream is written in one batch. | 1000 |
| **flushInterval** | the number of milliseconds before the batch is written | 1000 |
| **jitterInterval** | the number of milliseconds to increase the batch flush interval by a random amount | 0 |
| **retryInterval** | the number of milliseconds to retry unsuccessful write. The retry interval is used when the InfluxDB server does not specify "Retry-After" header.| 5000 |
| **maxRetries** | the number of max retries when write fails. The `0` disable retry strategy - the error is immediately propagate to upstream. | 5 |
| **maxRetryDelay** | the maximum delay between each retry attempt in milliseconds | 125_000 |
| **maxRetryTime** | maximum total retry timeout in milliseconds | 180_000 |
| **exponentialBase** | the base for the exponential retry delay, the next delay is computed using random exponential backoff as a random value within the interval  ``retryInterval * exponentialBase^(attempts-1)`` and ``retryInterval * exponentialBase^(attempts)``. Example for ``retryInterval=5_000, exponentialBase=2, maxRetryDelay=125_000, total=5`` Retry delays are random distributed values within the ranges of ``[5_000-10_000, 10_000-20_000, 20_000-40_000, 40_000-80_000, 80_000-125_000]``

> Backpressure: is defined by the backpressure behavior of the upstream publisher.

### Writing data

The following example demonstrates how to write measurements every 10 seconds:

```java
package example;

import java.time.Instant;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.reactive.InfluxDBClientReactive;
import com.influxdb.client.reactive.InfluxDBClientReactiveFactory;
import com.influxdb.client.reactive.WriteReactiveApi;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import org.reactivestreams.Publisher;

public class InfluxDB2ReactiveExampleWriteEveryTenSeconds {

    private static char[] token = "my-token".toCharArray();
    private static String org = "my-org";
    private static String bucket = "my-bucket";

    public static void main(final String[] args) throws InterruptedException {

        InfluxDBClientReactive influxDBClient = InfluxDBClientReactiveFactory.create("http://localhost:9999", token, org, bucket);

        //
        // Write data
        //
        WriteReactiveApi writeApi = influxDBClient.getWriteReactiveApi();

        Flowable<Temperature> measurements = Flowable.interval(10, TimeUnit.SECONDS)
                .map(time -> {

                    Temperature temperature = new Temperature();
                    temperature.location = getLocation();
                    temperature.value = getValue();
                    temperature.time = Instant.now();
                    return temperature;
                });

        //
        // ReactiveStreams publisher
        //
        Publisher<WriteReactiveApi.Success> publisher = writeApi.writeMeasurements(WritePrecision.NS, measurements);

        //
        // Subscribe to Publisher
        //
        Disposable subscriber = Flowable.fromPublisher(publisher)
                .subscribe(success -> System.out.println("Successfully written temperature"));

        Thread.sleep(35_000);

        subscriber.dispose();

        influxDBClient.close();
    }

    private static Double getValue() {
        Random r = new Random();

        return -20 + 70 * r.nextDouble();
    }

    private static String getLocation() {
        return "Prague";
    }

    @Measurement(name = "temperature")
    private static class Temperature {

        @Column(tag = true)
        String location;

        @Column
        Double value;

        @Column(timestamp = true)
        Instant time;
    }
}
```
## Advanced Usage

### Client configuration file

A client can be configured via configuration file. The configuration file has to be named as `influx2.properties` and has to be in root of classpath.

The following options are supported:

| Property name            | default    | description                                                |
|--------------------------|------------|------------------------------------------------------------| 
| influx2.url              | -          | the url to connect to InfluxDB                             |
| influx2.org              | -          | default destination organization for writes and queries    |
| influx2.bucket           | -          | default destination bucket for writes                      |
| influx2.token            | -          | the token to use for the authorization                     |
| influx2.logLevel         | NONE       | rest client verbosity level                                |
| influx2.readTimeout      | 10000 ms   | read timeout                                               |
| influx2.writeTimeout     | 10000 ms   | write timeout                                              |
| influx2.connectTimeout   | 10000 ms   | socket timeout                                             |
| influx2.precision        | NS         | default precision for unix timestamps in the line protocol |

The `influx2.readTimeout`, `influx2.writeTimeout` and `influx2.connectTimeout` supports `ms`, `s` and `m` as unit. Default is milliseconds.


##### Configuration example

```properties
influx2.url=http://localhost:8086
influx2.org=my-org
influx2.bucket=my-bucket
influx2.token=my-token
influx2.logLevel=BODY
influx2.readTimeout=5s
influx2.writeTimeout=10s
influx2.connectTimeout=5s
```

and then:

```java
InfluxDBClientReactive influxDBClient = InfluxDBClientReactiveFactory.create();
```

### Client connection string

A client can be constructed using a connection string that can contain the InfluxDBClientOptions parameters encoded into the URL.  
 
```java
InfluxDBClientReactive influxDBClient = InfluxDBClientReactiveFactory
            .create("http://localhost:8086?readTimeout=5000&connectTimeout=5000&logLevel=BASIC", token)
```
The following options are supported:

| Property name    | default    | description                                                |
|------------------|------------|------------------------------------------------------------| 
| org              | -          | default destination organization for writes and queries    |
| bucket           | -          | default destination bucket for writes                      |
| token            | -          | the token to use for the authorization                     |
| logLevel         | NONE       | rest client verbosity level                                |
| readTimeout      | 10000 ms   | read timeout                                               |
| writeTimeout     | 10000 ms   | write timeout                                              |
| connectTimeout   | 10000 ms   | socket timeout                                             |
| precision        | NS         | default precision for unix timestamps in the line protocol |

The `readTimeout`, `writeTimeout` and `connectTimeout` supports `ms`, `s` and `m` as unit. Default is milliseconds.

### Gzip support
`InfluxDBClientReactive` does not enable gzip compress for http requests by default. If you want to enable gzip to reduce transfer data's size, you can call:

```java
influxDBClient.enableGzip();
```

### Log HTTP Request and Response
The Requests and Responses can be logged by changing the LogLevel. LogLevel values are NONE, BASIC, HEADER, BODY. Note that 
applying the `BODY` LogLevel will disable chunking while streaming and will load the whole response into memory.  

```kotlin
influxDBClient.setLogLevel(LogLevel.HEADERS)
```

### Check the server status 

Server availability can be checked using the `influxDBClient.health()` endpoint.

### Construct queries using the [flux-dsl](../flux-dsl) query builder

```java
package example;

import java.time.temporal.ChronoUnit;

import com.influxdb.client.reactive.InfluxDBClientReactive;
import com.influxdb.client.reactive.InfluxDBClientReactiveFactory;
import com.influxdb.client.reactive.QueryReactiveApi;
import com.influxdb.query.dsl.Flux;
import com.influxdb.query.dsl.functions.restriction.Restrictions;

public class InfluxDB2ReactiveExampleDSL {

    private static char[] token = "my-token".toCharArray();
    private static String org = "my-org";

    public static void main(final String[] args) {

        InfluxDBClientReactive influxDBClient = InfluxDBClientReactiveFactory.create("http://localhost:8086", token, org);
        
        //
        // Query data
        //
        Flux flux = Flux.from("my-bucket")
                .range(-30L, ChronoUnit.MINUTES)
                .filter(Restrictions.and(Restrictions.measurement().equal("temperature")));

        QueryReactiveApi queryApi = influxDBClient.getQueryReactiveApi();

        queryApi
                .query(flux.toString())
                .subscribe(fluxRecord -> {
                    //
                    // The callback to consume a FluxRecord.
                    //
                    System.out.println(fluxRecord.getTime() + ": " + fluxRecord.getValueByKey("_value"));
                });

        influxDBClient.close();
    }
}
```

## Version

The latest version for Maven dependency:
```xml
<dependency>
  <groupId>com.influxdb</groupId>
  <artifactId>influxdb-client-reactive</artifactId>
  <version>5.0.0</version>
</dependency>
```
  
Or when using with Gradle:
```groovy
dependencies {
    implementation "com.influxdb:influxdb-client-reactive:5.0.0"
}
```

### Snapshot Repository
The snapshots are deployed into [OSS Snapshot repository](https://oss.sonatype.org/content/repositories/snapshots/).

#### Maven
```xml
<repository>
    <id>ossrh</id>
    <name>OSS Snapshot repository</name>
    <url>https://oss.sonatype.org/content/repositories/snapshots/</url>
    <releases>
        <enabled>false</enabled>
    </releases>
    <snapshots>
        <enabled>true</enabled>
    </snapshots>
</repository>
```
#### Gradle
```
repositories {
    maven { url "https://oss.sonatype.org/content/repositories/snapshots" }
}
```