# influxdb-client-kotlin

> This library is under development and no stable version has been released yet.  
> The API can change at any moment.

[![KDoc](https://img.shields.io/badge/KDoc-link-brightgreen.svg)](https://bonitoo-io.github.io/influxdb-client-java/influxdb-client-kotlin/dokka/influxdb-client-kotlin/org.influxdata.client.kotlin/index.html)

The reference Kotlin client that allows query and write for the InfluxDB 2.0 by Kotlin Channel coroutines. 

## Features

- [Querying data using Flux language](#queries)
- [Advanced Usage](#advanced-usage)
   
## Queries

The [QueryKotlinApi](https://bonitoo-io.github.io/influxdb-client-java/influxdb-client-kotlin/dokka/influxdb-client-kotlin/org.influxdata.client.kotlin/-query-kotlin-api/index.html) supports asynchronous queries by [Kotlin Channel coroutines](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.channels/-channel/index.html).
   
The following example demonstrates querying using the Flux language:

```kotlin
package example

import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.filter
import kotlinx.coroutines.channels.take
import kotlinx.coroutines.runBlocking
import org.influxdata.client.kotlin.InfluxDBClientKotlinFactory

private val token = "my_token".toCharArray()

fun main(args: Array<String>) = runBlocking {

    val influxDBClient = InfluxDBClientKotlinFactory.create("http://localhost:9999", token)

    val fluxQuery = ("from(bucket: \"telegraf\")\n"
            + " |> filter(fn: (r) => (r[\"_measurement\"] == \"cpu\" AND r[\"_field\"] == \"usage_system\"))"
            + " |> range(start: -1d)")

    //Result is returned as a stream
    val results = influxDBClient.getQueryKotlinApi().query(fluxQuery, "org_id")

    //Example of additional result stream processing on client side
    results
            //filter on client side using `filter` built-in operator
            .filter { "cpu0" == it.getValueByKey("cpu") }
            //take first 20 records
            .take(20)
            //print results
            .consumeEach { println("Measurement: ${it.measurement}, value: ${it.value}") }

    influxDBClient.close()
}
```

It is possible to parse a result line-by-line using the `queryRaw` method:

```kotlin
package example

import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.runBlocking
import org.influxdata.client.kotlin.InfluxDBClientKotlinFactory

private val token = "my_token".toCharArray()

fun main(args: Array<String>) = runBlocking {

    val influxDBClient = InfluxDBClientKotlinFactory.create("http://localhost:9999", token)

    val fluxQuery = ("from(bucket: \"telegraf\")\n"
            + " |> filter(fn: (r) => (r[\"_measurement\"] == \"cpu\" AND r[\"_field\"] == \"usage_system\"))"
            + " |> range(start: -5m)"
            + " |> sample(n: 5, pos: 1)")

    // Result is returned as a stream
    val results = influxDBClient.getQueryKotlinApi().queryRaw(fluxQuery, "{header: false}", "org_id")

    // Print results
    results.consumeEach { println("Line: $it") }

    influxDBClient.close()
}
```

## Advanced Usage

### Client connection string

A client can be constructed using a connection string that can contain the InfluxDBClientOptions parameters encoded into the URL.  
 
```java
val influxDBClient = InfluxDBClientKotlinFactory
            .create("http://localhost:8086?readTimeout=5000&connectTimeout=5000&logLevel=BASIC", token)
```
The following options are supported:

| Property name | default | description |
| --------------|-------------|-------------| 
| readTimeout       | 10000 ms| read timeout |
| writeTimeout      | 10000 ms| write timeout |
| connectTimeout    | 10000 ms| socket timeout |
| logLevel          | NONE | rest client verbosity level |


### Gzip support
`InfluxDBClientKotlin` does not enable gzip compress for http request body by default. If you want to enable gzip to reduce transfer data's size, you can call:

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

```kotlin
package example

import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.filter
import kotlinx.coroutines.channels.take
import kotlinx.coroutines.runBlocking
import org.influxdata.client.kotlin.InfluxDBClientKotlinFactory
import org.influxdata.query.dsl.Flux
import org.influxdata.query.dsl.functions.restriction.Restrictions
import java.time.temporal.ChronoUnit

private val token = "my_token".toCharArray()

fun main(args: Array<String>) = runBlocking {

    val influxDBClient = InfluxDBClientKotlinFactory.create("http://localhost:9999", token)

    val mem = Flux.from("telegraf")
            .filter(Restrictions.and(Restrictions.measurement().equal("mem"), Restrictions.field().equal("used_percent")))
            .range(-30L, ChronoUnit.MINUTES)

    //Result is returned as a stream
    val results = influxDBClient.getQueryKotlinApi().query(mem.toString(), "my-org")

    //Example of additional result stream processing on client side
    results
            //filter on client side using `filter` built-in operator
            .filter { (it.value as Double) > 55 }
            // take first 20 records
            .take(20)
            //print results
            .consumeEach { println("Measurement: ${it.measurement}, value: ${it.value}") }

    influxDBClient.close()
}
```
 
## Version

The latest version for Maven dependency:
```xml
<dependency>
  <groupId>org.influxdata</groupId>
  <artifactId>influxdb-client-kotlin</artifactId>
  <version>1.0-SNAPSHOT</version>
</dependency>
```
  
Or when using with Gradle:
```groovy
dependencies {
    compile "org.influxdata:influxdb-client-kotlin:1.0-SNAPSHOT"
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
