# influxdb-client-scala

> This library is under development and no stable version has been released yet.  
> The API can change at any moment.

[![KDoc](https://img.shields.io/badge/Scaladoc-link-brightgreen.svg)](https://bonitoo-io.github.io/influxdb-client-java/influxdb-client-scala/scaladocs/org/influxdata/client/scala/index.html)

The reference Scala client that allows query and write for the InfluxDB 2.0 by [Akka Streams](https://doc.akka.io/docs/akka/2.5/stream/).

## Features

- [Querying data using Flux language](#queries)
- [Advanced Usage](#advanced-usage)

## Queries

The [QueryScalaApi](https://bonitoo-io.github.io/influxdb-client-java/influxdb-client-scala/scaladocs/org/influxdata/client/scala/QueryScalaApi.html) is based on the [Akka Streams](https://doc.akka.io/docs/akka/2.5/stream/). 
The streaming can be configured by:

- `bufferSize` - Size of a buffer for incoming responses. Default 10000. 
- `overflowStrategy` - Strategy that is used when incoming response cannot fit inside the buffer. Default `akka.stream.OverflowStrategies.Backpressure`.

```scala
val fluxClient = InfluxDBClientScalaFactory.create(options, 5000, OverflowStrategy.dropTail)
```

The following example demonstrates querying using the Flux language:

```scala
package example

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import org.influxdata.client.scala.InfluxDBClientScalaFactory
import org.influxdata.query.FluxRecord

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object FluxQuery {

  implicit val system: ActorSystem = ActorSystem("it-tests")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  
  private val token = "my_token".toCharArray

  def main(args: Array[String]): Unit = {

    val influxDBClient = InfluxDBClientScalaFactory.create("http://localhost:9999", token)

    val fluxQuery = ("from(bucket: \"telegraf\")\n"
      + " |> filter(fn: (r) => (r[\"_measurement\"] == \"cpu\" AND r[\"_field\"] == \"usage_system\"))"
      + " |> range(start: -1d)")

    //Result is returned as a stream
    val results = influxDBClient.getQueryScalaApi().query(fluxQuery, "org_id")

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
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import org.influxdata.client.scala.InfluxDBClientScalaFactory

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object FluxQueryRaw {

  implicit val system: ActorSystem = ActorSystem("it-tests")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  
  private val token = "my_token".toCharArray

  def main(args: Array[String]): Unit = {
    
    val influxDBClient = InfluxDBClientScalaFactory.create("http://localhost:9999", token)

    val fluxQuery = ("from(bucket: \"telegraf\")\n"
      + " |> filter(fn: (r) => (r[\"_measurement\"] == \"cpu\" AND r[\"_field\"] == \"usage_system\"))"
      + " |> range(start: -5m)"
      + " |> sample(n: 5, pos: 1)")

    //Result is returned as a stream
    val sink = influxDBClient.getQueryScalaApi().queryRaw(fluxQuery, "{header: false}", "org_id")
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

### Client connection string

A client can be constructed using a connection string that can contain the InfluxDBClientOptions parameters encoded into the URL.  
 
```java
val influxDBClient = InfluxDBClientScalaFactory
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
`InfluxDBClientScala` does not enable gzip compress for http request body by default. If you want to enable gzip to reduce transfer data's size, you can call:

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

## Version

The latest version for Maven dependency:
```xml
<dependency>
  <groupId>org.influxdata</groupId>
  <artifactId>influxdb-client-scala</artifactId>
  <version>1.0.0-SNAPSHOT</version>
</dependency>
```
  
Or when using with Gradle:
```groovy
dependencies {
    compile "org.influxdata:influxdb-client-scala:1.0.0-SNAPSHOT"
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
