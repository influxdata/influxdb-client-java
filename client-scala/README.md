# influxdb-client-scala

[![KDoc](https://img.shields.io/badge/Scaladoc-link-brightgreen.svg)](https://influxdata.github.io/influxdb-client-java/influxdb-client-scala/scaladocs/org/influxdata/client/scala/index.html)

The reference Scala client that allows query and write for the InfluxDB 2.0 by [Akka Streams](https://doc.akka.io/docs/akka/2.6/stream/).

## Features

- [Querying data using Flux language](#queries)
- [Advanced Usage](#advanced-usage)

## Queries

The [QueryScalaApi](https://influxdata.github.io/influxdb-client-java/influxdb-client-scala/scaladocs/org/influxdata/client/scala/QueryScalaApi.html) is based on the [Akka Streams](https://doc.akka.io/docs/akka/2.6/stream/). 

The following example demonstrates querying using the Flux language:

```scala
package example

import akka.actor.ActorSystem
import akka.stream.scaladsl.Sink
import com.influxdb.client.scala.InfluxDBClientScalaFactory
import com.influxdb.query.FluxRecord

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object InfluxDB2ScalaExample {

  implicit val system: ActorSystem = ActorSystem("it-tests")

  def main(args: Array[String]): Unit = {

    val influxDBClient = InfluxDBClientScalaFactory
      .create("http://localhost:8086", "my-token".toCharArray, "my-org")

    val fluxQuery = ("from(bucket: \"my-bucket\")\n"
      + " |> range(start: -1d)"
      + " |> filter(fn: (r) => (r[\"_measurement\"] == \"cpu\" and r[\"_field\"] == \"usage_system\"))")

    //Result is returned as a stream
    val results = influxDBClient.getQueryScalaApi().query(fluxQuery)

    //Example of additional result stream processing on client side
    val sink = results
      //filter on client side using `filter` built-in operator
      .filter(it => "cpu0" == it.getValueByKey("cpu"))
      //take first 20 records
      .take(20)
      //print results
      .runWith(Sink.foreach[FluxRecord](it => println(s"Measurement: ${it.getMeasurement}, value: ${it.getValue}")
      ))

    // wait to finish
    Await.result(sink, Duration.Inf)

    influxDBClient.close()
    system.terminate()
  }
}

```

It is possible to parse a result line-by-line using the `queryRaw` method:

```scala
package example

import akka.actor.ActorSystem
import akka.stream.scaladsl.Sink
import com.influxdb.client.scala.InfluxDBClientScalaFactory

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object InfluxDB2ScalaExampleRaw {

  implicit val system: ActorSystem = ActorSystem("it-tests")

  def main(args: Array[String]): Unit = {
    val influxDBClient = InfluxDBClientScalaFactory
      .create("http://localhost:8086", "my-token".toCharArray, "my-org")

    val fluxQuery = ("from(bucket: \"my-bucket\")\n"
      + " |> range(start: -5m)"
      + " |> filter(fn: (r) => (r[\"_measurement\"] == \"cpu\" and r[\"_field\"] == \"usage_system\"))"
      + " |> sample(n: 5, pos: 1)")

    //Result is returned as a stream
    val sink = influxDBClient.getQueryScalaApi().queryRaw(fluxQuery)
      //print results
      .runWith(Sink.foreach[String](it => println(s"Line: $it")))

    // wait to finish
    Await.result(sink, Duration.Inf)

    influxDBClient.close()
    system.terminate()
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

```scala
val influxDBClient = InfluxDBClientScalaFactory.create();
```

### Client connection string

A client can be constructed using a connection string that can contain the InfluxDBClientOptions parameters encoded into the URL.  
 
```scala
val influxDBClient = InfluxDBClientScalaFactory
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
`InfluxDBClientScala` does not enable gzip compress for http requests by default. If you want to enable gzip to reduce transfer data's size, you can call:

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

```scala
package example

import java.time.temporal.ChronoUnit

import akka.actor.ActorSystem
import akka.stream.scaladsl.Sink
import com.influxdb.client.scala.InfluxDBClientScalaFactory
import com.influxdb.query.FluxRecord
import com.influxdb.query.dsl.Flux
import com.influxdb.query.dsl.functions.restriction.Restrictions

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object InfluxDB2ScalaExampleDSL {

  implicit val system: ActorSystem = ActorSystem("it-tests")

  def main(args: Array[String]) {

    val influxDBClient = InfluxDBClientScalaFactory
      .create("http://localhost:8086", "my-token".toCharArray, "my-org")

    val mem = Flux.from("my-bucket")
      .range(-30L, ChronoUnit.MINUTES)
      .filter(Restrictions.and(Restrictions.measurement().equal("mem"), Restrictions.field().equal("used_percent")))

    //Result is returned as a stream
    val results = influxDBClient.getQueryScalaApi().query(mem.toString())

    //Example of additional result stream processing on client side
    val sink = results
      //filter on client side using `filter` built-in operator
      .filter(it => it.getValue.asInstanceOf[Double] > 55)
      //take first 20 records
      .take(20)
      //print results
      .runWith(Sink.foreach[FluxRecord](it => println(s"Measurement: ${it.getMeasurement}, value: ${it.getValue}")))

    // wait to finish
    Await.result(sink, Duration.Inf)

    influxDBClient.close()
    system.terminate()
  }
}
```

## Version

The latest version for Maven dependency:
```xml
<dependency>
  <groupId>com.influxdb</groupId>
  <artifactId>influxdb-client-scala</artifactId>
  <version>1.15.0</version>
</dependency>
```
  
Or when using with Gradle:
```groovy
dependencies {
    compile "com.influxdb:influxdb-client-scala:1.15.0"
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
