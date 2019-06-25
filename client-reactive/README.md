# influxdb-client-reactive

> This library is under development and no stable version has been released yet.  
> The API can change at any moment.

[![javadoc](https://img.shields.io/badge/javadoc-link-brightgreen.svg)](https://bonitoo-io.github.io/influxdb-client-java/influxdb-client-reactive/apidocs/index.html)

The reference Java client that allows query and write for the InfluxDB 2.0 by a reactive way.

## Features
 
- [Querying data using Flux language](#queries)
- [Writing data using](#writes)
   - Line Protocol
   - Data Point
   - POJO
- [Advanced Usage](#advanced-usage)
         
## Queries

For querying data we use [QueryReactiveApi](https://bonitoo-io.github.io/influxdb-client-java/influxdb-client-reactive/apidocs/org/influxdata/client/reactive/QueryReactiveApi.html) that use [Reactive-Streams Pattern](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Flowable.html) for streaming query results and also support query raw response.

The following example demonstrates querying using the Flux language:

```java
package example;

import org.influxdata.client.reactive.InfluxDBClientReactive;
import org.influxdata.client.reactive.InfluxDBClientReactiveFactory;
import org.influxdata.client.reactive.QueryReactiveApi;

public class ReactiveQuery {

    private static char[] token = "my_token".toCharArray();

    public static void main(final String[] args) throws Exception {

        InfluxDBClientReactive influxDBClient = InfluxDBClientReactiveFactory.create("http://localhost:9999", token);

        //
        // Query data
        //
        String flux = "from(bucket:\"temperature-sensors\") |> range(start: 0)";

        QueryReactiveApi queryApi = influxDBClient.getQueryReactiveApi();

        queryApi
                .query(flux, "org_id")
                //
                // Filter records by field name
                //
                .filter(it -> "pressure".equals(it.getField()))
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

import org.influxdata.client.reactive.InfluxDBClientReactive;
import org.influxdata.client.reactive.InfluxDBClientReactiveFactory;
import org.influxdata.client.reactive.QueryReactiveApi;

public class ReactiveQueryRaw {

    private static char[] token = "my_token".toCharArray();

    public static void main(final String[] args) throws Exception {

        InfluxDBClientReactive influxDBClient = InfluxDBClientReactiveFactory.create("http://localhost:9999", token);

        //
        // Query data
        //
        String flux = "from(bucket:\"temperature-sensors\") |> range(start: 0)";

        QueryReactiveApi queryApi = influxDBClient.getQueryReactiveApi();

        queryApi
                .queryRaw(flux, "org_id")
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

import org.influxdata.annotations.Column;
import org.influxdata.annotations.Measurement;
import org.influxdata.client.reactive.InfluxDBClientReactive;
import org.influxdata.client.reactive.InfluxDBClientReactiveFactory;
import org.influxdata.client.reactive.QueryReactiveApi;

/**
 * @author Jakub Bednar (bednar@github) (19/02/2019 09:20)
 */
public class ReactiveQueryPojo {

    private static char[] token = "my_token".toCharArray();

    public static void main(final String[] args) throws Exception {

        InfluxDBClientReactive influxDBClient = InfluxDBClientReactiveFactory.create("http://localhost:9999", token);

        //
        // Query data
        //
        String flux = "from(bucket:\"temperature-sensors\") |> range(start: 0)";

        QueryReactiveApi queryApi = influxDBClient.getQueryReactiveApi();

        queryApi
                .query(flux, "org_id", Temperature.class)
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

## Writes

For writing data we use [WriteReactiveApi](https://bonitoo-io.github.io/influxdb-client-java/influxdb-client-reactive/apidocs/org/influxdata/client/reactive/WriteReactiveApi.html) that supports same configuration as [non reactive client](../client#writes):

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
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import org.influxdata.annotations.Column;
import org.influxdata.annotations.Measurement;
import org.influxdata.client.reactive.InfluxDBClientReactive;
import org.influxdata.client.reactive.InfluxDBClientReactiveFactory;
import org.influxdata.client.reactive.WriteReactiveApi;

import io.reactivex.Flowable;

public class WriteEvery10Seconds {

    private static char[] token = "my_token".toCharArray();

    public static void main(final String[] args) throws Exception {

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

        writeApi.writeMeasurements("bucket_name", "org_id", ChronoUnit.NANOS, measurements);

        writeApi.close();
        influxDBClient.close();
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
            .create("http://localhost:8086?readTimeout=5000&connectTimeout=5000&logLevel=BASIC", token)
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
`InfluxDBClientReactive` does not enable gzip compress for http request body by default. If you want to enable gzip to reduce transfer data's size, you can call:

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

import org.influxdata.client.reactive.InfluxDBClientReactive;
import org.influxdata.client.reactive.InfluxDBClientReactiveFactory;
import org.influxdata.client.reactive.QueryReactiveApi;
import org.influxdata.query.dsl.Flux;
import org.influxdata.query.dsl.functions.restriction.Restrictions;

public class ReactiveQuery {

    private static char[] token = "my_token".toCharArray();

    public static void main(final String[] args) throws Exception {

        InfluxDBClientReactive influxDBClient = InfluxDBClientReactiveFactory.create("http://localhost:9999", token);

        //
        // Query data
        //
        Flux flux = Flux.from("temperature-sensors")
                .filter(Restrictions.and(Restrictions.field().equal("pressure")))
                .limit(10);

        QueryReactiveApi queryApi = influxDBClient.getQueryReactiveApi();

        queryApi
                .query(flux.toString(), "org_id")
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
  <groupId>org.influxdata</groupId>
  <artifactId>influxdb-client-reactive</artifactId>
  <version>1.0-SNAPSHOT</version>
</dependency>
```
  
Or when using with Gradle:
```groovy
dependencies {
    compile "org.influxdata:influxdb-client-reactive:1.0-SNAPSHOT"
}
```

### Repository
The repository is temporally located [here](https://apitea.com/nexus/content/repositories/bonitoo-snapshot/).

#### Maven
```xml
<repository>
    <id>bonitoo-repository</id>
    <name>Bonitoo.io repository</name>
    <url>https://apitea.com/nexus/content/repositories/bonitoo-snapshot/</url>
    <releases>
        <enabled>true</enabled>
    </releases>
    <snapshots>
        <enabled>true</enabled>
    </snapshots>
</repository>
```
#### Gradle
```
repositories {
    maven { url "https://apitea.com/nexus/content/repositories/bonitoo-snapshot" }
}
```