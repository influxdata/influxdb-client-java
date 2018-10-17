# flux-java

The Java library for InfluxDB 1.7 /v2/query REST API using [Flux language](https://github.com/influxdata/flux/blob/master/docs/SPEC.md). 



> This library is under development and no stable version has been released yet.  
> The API can change at any moment.

[![Build Status](https://travis-ci.org/bonitoo-io/influxdata-platform-java.svg?branch=master)](https://travis-ci.org/bonitoo-io/influxdata-platform-java)
[![codecov](https://codecov.io/gh/bonitoo-io/influxdata-platform-java/branch/master/graph/badge.svg)](https://codecov.io/gh/bonitoo-io/influxdata-platform-java)
[![License](https://img.shields.io/github/license/bonitoo-io/influxdata-platform-java.svg)](https://github.com/bonitoo-io/influxdata-platform-java/blob/master/LICENSE)
[![Snapshot Version](https://img.shields.io/nexus/s/https/apitea.com/nexus/io.bonitoo.flux/flux-java.svg)](https://apitea.com/nexus/content/repositories/bonitoo-snapshot/)
[![GitHub issues](https://img.shields.io/github/issues-raw/bonitoo-io/influxdata-platform-java.svg)](https://github.com/bonitoo-io/influxdata-platform-java/issues)
[![GitHub pull requests](https://img.shields.io/github/issues-pr-raw/bonitoo-io/influxdata-platform-java.svg)](https://github.com/bonitoo-io/influxdata-platform-java/pulls)

### Create client

The `FluxClientFactory` creates the instance of a `FluxClient` client that can be customized by `FluxConnectionOptions`.

`FluxConnectionOptions` parameters:
 
- `url` -  the url to connect to Platform
- `okHttpClient` - custom HTTP client to use for communication with Platform (optional)

```java
    // client creation
    FluxConnectionOptions options = FluxConnectionOptions.builder()
        .url("http://localhost:8086/")
        .okHttpClient
        .build();

    FluxClient fluxClient = FluxClientFactory.create(options);

    fluxClient.query(...)
     ...
```

#### Client connection string

Client can be constructed using connection string, that can contain the FluxConnectionOptions parameters encoded into URL.  
 
```java
    FluxClient fluxClient = FluxClientFactory.create("http://localhost:8086?readTimeout=5000&connectionTimeout=5000&logLevel=BASIC")
```
Following options are supported:

| Property name | default | description |
| --------------|-------------|-------------| 
| readTimeout       | 10000 ms| read timeout |
| writeTimeout      | 10000 ms| write timeout |
| connectTimeout    | 10000 ms| socket timeout |
| logLevel          | NONE | rest client verbosity level |

## Query using Flux language

Library supports both synchronous and asynchronous queries. 

Simple synchronous example:

```java
String query = "from(bucket:\"telegraf\") |> filter(fn: (r) => r[\"_measurement\"] == \"cpu\" AND r[\"_field\"] == \"usage_user\") |> sum()";

//simple synchronous query
List<FluxTable> tables = fluxClient.flux(query);

```
For larger data sets is more effective to stream data and use [asynchronous](#asynchronous-query) or [reactive](../flux-client-rxjava) 
client based on RxJava2.   

### Construct queries using [flux-dsl](../flux-dsl) query builder

[flux-dsl](../flux-dsl) contains java classes representing elements in Flux language to help build Flux queries and expressions. 

All supported operators are documented in [Operators](../flux-dsl) and in javadoc. Custom own functions can be added
easily, see [Custom operator](../flux-dsl/README.md#custom-operator).

Example of using `Flux` query builder:

```java
Flux.from("telegraf")
        .filter(
            Restrictions.and(
                Restrictions.measurement().equal("cpu"),
                Restrictions.field().equal("usage_system"))
        )
        .range(-1L, ChronoUnit.DAYS)
        .sample(5, 1); 
```

#### Asynchronous query

Asynchronous query API allows streaming of `FluxRecord`s with possibility to implement custom
error handling and `onComplete` callback notification. 

`Cancellable` object is used for aborting query while processing. 

For developers that are familiar with reactive programming and for more advanced usecases is possible 
to use [flux-client-rxjava](../flux-client-rxjava) extension.

Asynchronous query example:   

```java
    String fluxQuery = "from(bucket: \"telegraf\")\n" +
        " |> filter(fn: (r) => (r[\"_measurement\"] == \"cpu\" AND r[\"_field\"] == \"usage_system\"))" +
        " |> range(start: -1d)" +
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
           System.out.println("Error occured: "+ error.getMessage());

        }, () -> {
          // on complete notification
           System.out.println("Query completed");
        });
```

#### Raw query response

It is possible to parse line by line result using `queryRaw` method.  

```java
    void queryRaw(@Nonnull final String query,
                  @Nonnull final BiConsumer<Cancellable, String> onResponse,
                  @Nonnull final Consumer<? super Throwable> onError,
                  @Nonnull final Runnable onComplete);

```

### Advanced Usage

#### Gzip's support

> Currently unsupported by server.

#### Log HTTP Request and Response
The Requests and Responses can be logged by changing LogLevel. LogLevel values are NONE, BASIC, HEADER, BODY. Note that 
applying `BODY` LogLevel will disable chunking while streaming a loads whole response into memory.  

```java
fluxClient.setLogLevel(Level.HEADERS);
```

#### Check the server status and version

Server availability can be checked using `fluxClient.ping()` endpoint, server version can be obtained using `fluxClient.version()`.
 
## Version

The latest version for Maven dependency:
```xml
<dependency>
  <groupId>org.influxdata</groupId>
  <artifactId>flux-client</artifactId>
  <version>1.0.0-SNAPSHOT</version>
</dependency>
```
  
Or when using with Gradle:
```groovy
dependencies {
    compile "org.influxdata:flux-client:1.0.0-SNAPSHOT"
}
```

### Snapshot repository
The snapshot repository is temporally located [here](https://apitea.com/nexus/content/repositories/bonitoo-snapshot/).

#### Maven
```xml
<repository>
    <id>bonitoo-snapshot</id>
    <name>Bonitoo.io snapshot repository</name>
    <url>https://apitea.com/nexus/content/repositories/bonitoo-snapshot/</url>
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
    maven { url "https://apitea.com/nexus/content/repositories/bonitoo-snapshot" }
}
```
