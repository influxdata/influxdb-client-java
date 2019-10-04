# influxdb-client-reactive

[![javadoc](https://img.shields.io/badge/javadoc-link-brightgreen.svg)](https://influxdata.github.io/influxdb-client-java/influxdb-client-reactive/apidocs/index.html)

The reference Java client that allows query and write for the InfluxDB 2.0 by a reactive way.

## Features
 
- [Querying data using Flux language](#queries)
- [Writing data using](#writes)
   - Line Protocol
   - Data Point
   - POJO
- [Advanced Usage](#advanced-usage)
         
## Queries

For querying data we use [QueryReactiveApi](https://influxdata.github.io/influxdb-client-java/influxdb-client-reactive/apidocs/org/influxdata/client/reactive/QueryReactiveApi.html) that use [Reactive-Streams Pattern](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Flowable.html) for streaming query results and also support query raw response.

The following example demonstrates querying using the Flux language:

```java
package example;

import com.influxdb.client.reactive.InfluxDBClientReactive;
import com.influxdb.client.reactive.InfluxDBClientReactiveFactory;
import com.influxdb.client.reactive.QueryReactiveApi;

public class InfluxDB2ReactiveExample {

    private static char[] token = "my-token".toCharArray();

    public static void main(final String[] args) {

        InfluxDBClientReactive influxDBClient = InfluxDBClientReactiveFactory.create("http://localhost:9999", token);

        //
        // Query data
        //
        String flux = "from(bucket:\"my-bucket\") |> range(start: 0)";

        QueryReactiveApi queryApi = influxDBClient.getQueryReactiveApi();

        queryApi
                .query(flux, "my-org")
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

public class InfluxDB2ReactiveExampleRaw {

    private static char[] token = "my-token".toCharArray();

    public static void main(final String[] args) {

        InfluxDBClientReactive influxDBClient = InfluxDBClientReactiveFactory.create("http://localhost:9999", token);

        //
        // Query data
        //
        String flux = "from(bucket:\"my-bucket\") |> range(start: 0)";

        QueryReactiveApi queryApi = influxDBClient.getQueryReactiveApi();

        queryApi
                .queryRaw(flux, "my-org")
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

The mapping result to POJO is also supported:

```java
package example;

import java.time.Instant;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import com.influxdb.client.reactive.InfluxDBClientReactive;
import com.influxdb.client.reactive.InfluxDBClientReactiveFactory;
import com.influxdb.client.reactive.QueryReactiveApi;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

public class InfluxDB2ReactiveExamplePojo {

    private static char[] token = "my-token".toCharArray();

    public static void main(final String[] args) {

        InfluxDBClientReactive influxDBClient = InfluxDBClientReactiveFactory.create("http://localhost:9999", token);
        //
        // Query data
        //
        String flux = "from(bucket:\"my-bucket\") |> range(start: 0) |> filter(fn: (r) => r._measurement == \"temperature\")";

        QueryReactiveApi queryApi = influxDBClient.getQueryReactiveApi();

        queryApi
                .query(flux, "my-org", Temperature.class)
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

For writing data we use [WriteReactiveApi](https://influxdata.github.io/influxdb-client-java/influxdb-client-reactive/apidocs/org/influxdata/client/reactive/WriteReactiveApi.html) that supports same configuration as [non reactive client](../client#writes):

1. writing data using Line Protocol, Data Point, POJO
2. use batching for writes
3. use client backpressure strategy
4. produces events that allow user to be notified and react to this events
    - `WriteSuccessEvent` - published when arrived the success response from Platform server
    - `BackpressureEvent` - published when is **client** backpressure applied
    - `WriteErrorEvent` - published when occurs a unhandled exception
    - `WriteRetriableErrorEvent` - published when occurs a retriable error
5. use GZIP compression for data

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
import com.influxdb.client.reactive.QueryReactiveApi;
import com.influxdb.client.reactive.WriteReactiveApi;

import io.reactivex.Flowable;

public class InfluxDB2ReactiveExampleWriteEveryTenSeconds {

    private static char[] token = "my-token".toCharArray();

    public static void main(final String[] args) throws InterruptedException {

        InfluxDBClientReactive influxDBClient = InfluxDBClientReactiveFactory.create("http://localhost:9999", token);

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

        writeApi.writeMeasurements("my-bucket", "my-org", WritePrecision.NS, measurements);

        Thread.sleep(30_000);

        writeApi.close();
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

| Property name             | default   | description |
| --------------------------|-----------|-------------| 
| influx2.url               | -         | the url to connect to InfluxDB |
| influx2.org               | -         | default destination organization for writes and queries |
| influx2.bucket            | -         | default destination bucket for writes |
| influx2.token             | -         | the token to use for the authorization |
| influx2.logLevel          | NONE      | rest client verbosity level |
| influx2.readTimeout       | 10000 ms  | read timeout |
| influx2.writeTimeout      | 10000 ms  | write timeout |
| influx2.connectTimeout    | 10000 ms  | socket timeout |

The `influx2.readTimeout`, `influx2.writeTimeout` and `influx2.connectTimeout` supports `ms`, `s` and `m` as unit. Default is milliseconds.


##### Configuration example

```properties
influx2.url=http://localhost:9999
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
            .create("http://localhost:9999?readTimeout=5000&connectTimeout=5000&logLevel=BASIC", token)
```
The following options are supported:

| Property name     | default   | description |
| ------------------|-----------|-------------| 
| org               | -         | default destination organization for writes and queries |
| bucket            | -         | default destination bucket for writes |
| token             | -         | the token to use for the authorization |
| logLevel          | NONE      | rest client verbosity level |
| readTimeout       | 10000 ms  | read timeout |
| writeTimeout      | 10000 ms  | write timeout |
| connectTimeout    | 10000 ms  | socket timeout |

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

    public static void main(final String[] args) {

        InfluxDBClientReactive influxDBClient = InfluxDBClientReactiveFactory.create("http://localhost:9999", token);
        
        //
        // Query data
        //
        Flux flux = Flux.from("my-bucket")
                .range(-30L, ChronoUnit.MINUTES)
                .filter(Restrictions.and(Restrictions.measurement().equal("temperature")));

        QueryReactiveApi queryApi = influxDBClient.getQueryReactiveApi();

        queryApi
                .query(flux.toString(), "my-org")
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
  <version>1.0.0</version>
</dependency>
```
  
Or when using with Gradle:
```groovy
dependencies {
    compile "com.influxdb:influxdb-client-reactive:1.0.0"
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