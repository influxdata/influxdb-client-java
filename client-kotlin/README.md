# flux-client-kotlin

The reference Kotlin client that allows you to perform [Flux queries](https://github.com/influxdata/flux/blob/master/docs/SPEC.md) against InfluxDB 1.7+. 

> This library is under development and no stable version has been released yet.  
> The API can change at any moment.

[![Build Status](https://travis-ci.org/bonitoo-io/influxdb-client-java.svg?branch=master)](https://travis-ci.org/bonitoo-io/influxdb-client-java)
[![codecov](https://codecov.io/gh/bonitoo-io/influxdb-client-java/branch/master/graph/badge.svg)](https://codecov.io/gh/bonitoo-io/influxdb-client-java)
[![License](https://img.shields.io/github/license/bonitoo-io/influxdb-client-java.svg)](https://github.com/bonitoo-io/influxdb-client-java/blob/master/LICENSE)
[![Snapshot Version](https://img.shields.io/nexus/s/https/apitea.com/nexus/io.bonitoo.flux/flux-java.svg)](https://apitea.com/nexus/content/repositories/bonitoo-snapshot/)
[![GitHub issues](https://img.shields.io/github/issues-raw/bonitoo-io/influxdb-client-java.svg)](https://github.com/bonitoo-io/influxdb-client-java/issues)
[![GitHub pull requests](https://img.shields.io/github/issues-pr-raw/bonitoo-io/influxdb-client-java.svg)](https://github.com/bonitoo-io/influxdb-client-java/pulls)

### Create client

The `FluxClientKotlinFactory` creates an instance of a `FluxClientKotlin` client that can be customized with `FluxConnectionOptions`.

`FluxConnectionOptions` parameters:
 
- `url` -  the url to connect to InfluxDB
- `okHttpClient` - custom HTTP client to use for communications with InfluxDB (optional)

```kotlin
// client creation
val options = FluxConnectionOptions.builder()
            .url("http://localhost:8086/")
            .build()

val fluxClient = FluxClientKotlinFactory.create(options)

val results = fluxClient.query(fluxQuery)
...
```

#### Client connection string

A client can be constructed using a connection string that can contain the FluxConnectionOptions parameters encoded into the URL.  
 
```java
val fluxClient = FluxClientKotlinFactory
            .create("http://localhost:8086?readTimeout=5000&connectTimeout=5000&logLevel=BASIC")
```
The following options are supported:

| Property name | default | description |
| --------------|-------------|-------------| 
| readTimeout       | 10000 ms| read timeout |
| writeTimeout      | 10000 ms| write timeout |
| connectTimeout    | 10000 ms| socket timeout |
| logLevel          | NONE | rest client verbosity level |

## Query using the Flux language

The library supports asynchronous queries by [Kotlin Channel coroutines](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.channels/-channel/index.html). 

```kotlin
val fluxQuery = ("from(bucket: \"telegraf\")\n"
            + " |> filter(fn: (r) => (r[\"_measurement\"] == \"cpu\" AND r[\"_field\"] == \"usage_system\"))"
            + " |> range(start: -1d)")

// Result is returned as a stream
val results = fluxClient.query(fluxQuery)

// Example of additional result stream processing on client side
results
    // filter on client side using `filter` built-in operator
    .filter { "cpu0" == it.getValueByKey("cpu") }
    // take first 20 records
    .take(20)
    // print results
    .consumeEach { println("Measurement: ${it.measurement}, value: ${it.value}") }
```
### Construct queries using the [flux-dsl](../flux-dsl) query builder

[Flux-dsl](../flux-dsl) contains java classes representing elements of the Flux language to help build Flux queries and expressions. 

All supported operators are documented in [Operators](../flux-dsl) and in javadoc. Custom functions can be added
easily&mdash;see [Custom operator](../flux-dsl/README.md#custom-operator).

An example of using the `Flux` query builder:

```kotlin
val flux = Flux
    .from("telegraf")
    .filter(Restrictions.and(Restrictions.measurement().equal("mem"), Restrictions.field().equal("used_percent")))
    .range(-30L, ChronoUnit.MINUTES)

// Result is returned as a stream
val results = fluxClient.query(flux.toString())

// Example of additional result stream processing on client side
results
    // filter on client side using `filter` built-in operator
    .filter { (it.value as Double) > 55 }
    // take first 20 records
    .take(20)
    // print results
    .consumeEach { println("Measurement: ${it.measurement}, value: ${it.value}") }
```

#### Raw query response

It is possible to parse a result line-by-line using the `queryRaw` method.  

```kotlin
val fluxQuery = ("from(bucket: \"telegraf\")\n"
            + " |> filter(fn: (r) => (r[\"_measurement\"] == \"cpu\" AND r[\"_field\"] == \"usage_system\"))"
            + " |> range(start: -5m)"
            + " |> sample(n: 5, pos: 1)")

// Result is returned as a stream
val results = fluxClient.queryRaw(fluxQuery, "{header: false}")

// Print results
results.consumeEach { println("Line: $it") }
```

### Advanced Usage

#### Gzip support

> Currently unsupported by the server.

#### Log HTTP Request and Response
The Requests and Responses can be logged by changing the LogLevel. LogLevel values are NONE, BASIC, HEADER, BODY. Note that 
applying the `BODY` LogLevel will disable chunking while streaming and will load the whole response into memory.  

```kotlin
fluxClient.setLogLevel(LogLevel.HEADERS)
```

#### Check the server status and version

Server availability can be checked using the `fluxClient.ping()` endpoint.  Server version can be obtained using `fluxClient.version()`.
 
## Version

The latest version for Maven dependency:
```xml
<dependency>
  <groupId>org.influxdata</groupId>
  <artifactId>flux-client-kotlin</artifactId>
  <version>1.0.0-SNAPSHOT</version>
</dependency>
```
  
Or when using with Gradle:
```groovy
dependencies {
    compile "org.influxdata:flux-client-kotlin:1.0.0-SNAPSHOT"
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
