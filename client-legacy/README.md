# influxdb-client-flux

[![javadoc](https://img.shields.io/badge/javadoc-link-brightgreen.svg)](https://influxdata.github.io/influxdb-client-java/influxdb-client-flux/apidocs/index.html)

The client that allow perform Flux Query against the InfluxDB 1.7+.

## Documentation

This section contains links to the client library documentation.

* [Product documentation](https://docs.influxdata.com/influxdb/latest/api-guide/client-libraries/), [Getting Started](#create-client)
* [Examples](../examples)
* [API Reference](https://influxdata.github.io/influxdb-client-java/influxdb-client-flux/apidocs/index.html)
* [Changelog](../CHANGELOG.md)

### Create client

The `FluxClientFactory` creates an instance of a `FluxClient` client that can be customized with `FluxConnectionOptions`.

`FluxConnectionOptions` parameters:
 
- `url` -  the url to connect to InfluxDB
- `okHttpClient` - custom HTTP client to use for communications with InfluxDB (optional)

```java
// client creation
FluxConnectionOptions options = FluxConnectionOptions.builder()
    .url("http://localhost:8086/")
    .build();

FluxClient fluxClient = FluxClientFactory.create(options);

fluxClient.query(...)
 ...
```

#### Client connection string

A client can be constructed using a connection string that can contain the FluxConnectionOptions parameters encoded into the URL.  
 
```java
FluxClient fluxClient = FluxClientFactory.create("http://localhost:8086?readTimeout=5000&connectTimeout=5000&logLevel=BASIC")
```
The following options are supported:

| Property name  | default  | description                 |
|----------------|----------|-----------------------------| 
| readTimeout    | 10000 ms | read timeout                |
| writeTimeout   | 10000 ms | write timeout               |
| connectTimeout | 10000 ms | socket timeout              |
| logLevel       | NONE     | rest client verbosity level |

## Query using the Flux language

The library supports both synchronous and asynchronous queries. 

A simple synchronous example:

```java
String query = "from(bucket:\"telegraf\") |> range(start: -1d) |> filter(fn: (r) => r[\"_measurement\"] == \"cpu\" and r[\"_field\"] == \"usage_user\") |> sum()";

//simple synchronous query
List<FluxTable> tables = fluxClient.flux(query);
```

For larger data sets it is more effective to stream data and to use [asynchronous](#asynchronous-query) requests or the [reactive](../flux-client-rxjava) 
client based on RxJava2.   

### Construct queries using the [flux-dsl](../flux-dsl) query builder

[Flux-dsl](../flux-dsl) contains java classes representing elements of the Flux language to help build Flux queries and expressions. 

All supported operators are documented in [Operators](../flux-dsl) and in javadoc. Custom functions can be added
easily&mdash;see [Custom operator](../flux-dsl/README.md#custom-operator).

An example of using the `Flux` query builder:

```java
Flux.from("telegraf")  
        .range(-1L, ChronoUnit.DAYS)
        .filter(
            Restrictions.and(
                Restrictions.measurement().equal("cpu"),
                Restrictions.field().equal("usage_system"))
        )
        .sample(5, 1); 
```

#### Asynchronous query

The asynchronous query API allows streaming of `FluxRecord`s with the possibility of implementing custom
error handling and `onComplete` callback notification. 

A `Cancellable` object is used for aborting a query while processing. 

For developers that are familiar with reactive programming and for more advanced usecases it is possible 
to use the [flux-client-rxjava](../flux-client-rxjava) extension.

An asynchronous query example:   

```java
    String fluxQuery = "from(bucket: \"telegraf\")\n" +   
        " |> range(start: -1d)" +
        " |> filter(fn: (r) => (r[\"_measurement\"] == \"cpu\" and r[\"_field\"] == \"usage_system\"))" +
        " |> sample(n: 5, pos: 1)";

     fluxClient.query(
         fluxQuery, (cancellable, record) -> {
          // process the flux query result record
           System.out.println(
               record.getTime() + ": " + record.getValue());
           // found what I'm looking for ?
           if (some condition) {
                 // abort processing
                 cancellable.cancel();
           }

           
        }, error -> {
           // error handling while processing result
           System.out.println("Error occurred: "+ error.getMessage());

        }, () -> {
          // on complete notification
           System.out.println("Query completed");
        });
```

#### Raw query response

It is possible to parse a result line-by-line using the `queryRaw` method.  

```java
void queryRaw(@Nonnull final String query,
              @Nonnull final BiConsumer<Cancellable, String> onResponse,
              @Nonnull final Consumer<? super Throwable> onError,
              @Nonnull final Runnable onComplete);
```

### Advanced Usage

#### Gzip support

> Currently unsupported by the server.

#### Log HTTP Request and Response
The Requests and Responses can be logged by changing the LogLevel. LogLevel values are NONE, BASIC, HEADER, BODY. Note that 
applying the `BODY` LogLevel will disable chunking while streaming and will load the whole response into memory.  

```java
fluxClient.setLogLevel(Level.HEADERS);
```

#### Check the server status and version

Server availability can be checked using the `fluxClient.ping()` endpoint.  Server version can be obtained using `fluxClient.version()`.
 
## Version

The latest version for Maven dependency:
```xml
<dependency>
  <groupId>com.influxdb</groupId>
  <artifactId>influxdb-client-flux</artifactId>
  <version>6.8.0</version>
</dependency>
```
  
Or when using with Gradle:
```groovy
dependencies {
    implementation "com.influxdb:influxdb-client-flux:6.8.0"
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
