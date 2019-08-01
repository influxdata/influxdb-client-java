# influxdb-client-kotlin

> This library is under development and no stable version has been released yet.  
> The API can change at any moment.

[![KDoc](https://img.shields.io/badge/KDoc-link-brightgreen.svg)](https://influxdata.github.io/influxdb-client-java/influxdb-client-kotlin/dokka/influxdb-client-kotlin/com.influxdb.client.kotlin/index.html)

The reference Kotlin client that allows query and write for the InfluxDB 2.0 by Kotlin Channel coroutines. 

## Features

- [Querying data using Flux language](#queries)
- [Advanced Usage](#advanced-usage)
   
## Queries

The [QueryKotlinApi](https://influxdata.github.io/influxdb-client-java/influxdb-client-kotlin/dokka/influxdb-client-kotlin/com.influxdb.client.kotlin/-query-kotlin-api/index.html) supports asynchronous queries by [Kotlin Channel coroutines](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.channels/-channel/index.html).
   
The following example demonstrates querying using the Flux language:

```kotlin
package example

import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.filter
import kotlinx.coroutines.channels.take
import kotlinx.coroutines.runBlocking
import com.influxdb.client.kotlin.InfluxDBClientKotlinFactory

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
import com.influxdb.client.kotlin.InfluxDBClientKotlinFactory

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

```kotlin
val influxDBClient = InfluxDBClientKotlinFactory.create();
```

### Client connection string

A client can be constructed using a connection string that can contain the InfluxDBClientOptions parameters encoded into the URL.  
 
```kotlin
val influxDBClient = InfluxDBClientKotlinFactory
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
import com.influxdb.client.kotlin.InfluxDBClientKotlinFactory
import com.influxdb.query.dsl.Flux
import com.influxdb.query.dsl.functions.restriction.Restrictions
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
  <groupId>com.influxdb</groupId>
  <artifactId>influxdb-client-kotlin</artifactId>
  <version>1.0.0.M3-SNAPSHOT</version>
</dependency>
```
  
Or when using with Gradle:
```groovy
dependencies {
    compile "com.influxdb:influxdb-client-kotlin:1.0.0.M3-SNAPSHOT"
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
