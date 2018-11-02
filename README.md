# influxdata-platform-java
[![Build Status](https://travis-ci.org/bonitoo-io/influxdata-platform-java.svg?branch=master)](https://travis-ci.org/bonitoo-io/influxdata-platform-java)
[![codecov](https://codecov.io/gh/bonitoo-io/influxdata-platform-java/branch/master/graph/badge.svg)](https://codecov.io/gh/bonitoo-io/influxdata-platform-java)
[![License](https://img.shields.io/github/license/bonitoo-io/influxdata-platform-java.svg)](https://github.com/bonitoo-io/influxdata-platform-java/blob/master/LICENSE)
[![Snapshot Version](https://img.shields.io/nexus/s/https/apitea.com/nexus/org.influxdata/influxdata-platform-java.svg)](https://apitea.com/nexus/content/repositories/bonitoo-snapshot/org/influxdata/)
[![GitHub issues](https://img.shields.io/github/issues-raw/bonitoo-io/influxdata-platform-java.svg)](https://github.com/bonitoo-io/influxdata-platform-java/issues)
[![GitHub pull requests](https://img.shields.io/github/issues-pr-raw/bonitoo-io/influxdata-platform-java.svg)](https://github.com/bonitoo-io/influxdata-platform-java/pulls)

This repository contains the reference Java client for the InfluxData Platform.

> This library is under development and no stable version has been released yet.  
> The API can change at any moment.

### Features

- Supports querying using the Flux language over the InfluxDB 1.7+ REST API (`/api/v2/query endpoint`) 
- InfluxData Platform OSS 2.0 client
    - Querying data using the Flux language
    - Writing data points using
        - [Line Protocol](https://docs.influxdata.com/influxdb/v1.6/write_protocols/line_protocol_tutorial/) 
        - [Point object](https://github.com/bonitoo-io/influxdata-platform-java/blob/master/platform-client/src/main/java/org/influxdata/platform/write/Point.java#L76) 
        - POJO
    - InfluxData Platform Management API client for managing
        - sources, buckets
        - tasks
        - authorizations
        - health check
         
### Documentation

- **[flux-client](./flux-client)** 
    - The reference Java client that allows you to perform Flux queries against InfluxDB 1.7+.
    - [javadoc](https://bonitoo-io.github.io/influxdata-platform-java/flux-client/apidocs/index.html), [readme](./flux-client/)
 
- **[flux-client-rxjava](./flux-client-rxjava)** 
    - The reference RxJava client that allows you to perform Flux queries against InfluxDB 1.7+ in a [_reactive_](http://www.reactive-streams.org/) way.
    -  [javadoc](https://bonitoo-io.github.io/influxdata-platform-java/flux-client-rxjava/apidocs/index.html), [readme](./flux-client-rxjava/)

- **[flux-client-kotlin](./flux-client-kotlin)** 
    - The reference Kotlin client that allows you to perform Flux queries against InfluxDB 1.7+.
    -  [KDoc](https://bonitoo-io.github.io/influxdata-platform-java/flux-client-kotlin/dokka/flux-client-kotlin/org.influxdata.flux/index.html), [readme](./flux-client-kotlin/)

- **[flux-client-scala](./flux-client-scala)** 
    - The reference Scala client that allows you to perform Flux queries against InfluxDB 1.7+.
    -  [Scaladoc](https://bonitoo-io.github.io/influxdata-platform-java/flux-client-scala/scaladocs/org/influxdata/flux/index.html), [readme](./flux-client-scala/)

- **[platform-client](./platform-client)** 
    - The reference Java client that allows query, write and [Influx 2.0 OSS Platform](https://github.com/influxdata/platform) management.
    - [javadoc](https://bonitoo-io.github.io/influxdata-platform-java/platform-client/apidocs/index.html), [readme](./platform-client/)

- **[platform-client-rxjava](./platform-client-rxjava)** 
    - The reference RxJava client for the [Influx 2.0 OSS Platform](https://github.com/influxdata/platform]) that allows query and write in a reactive way.
    - [javadoc](https://bonitoo-io.github.io/influxdata-platform-java/platform-client-rxjava/apidocs/index.html), [readme](./platform-client-rxjava/)

- **[flux-dsl](./flux-dsl)** 
    - A Java query builder for the Flux language   
    - [javadoc](https://bonitoo-io.github.io/influxdata-platform-java/flux-dsl/apidocs/index.html), [readme](./flux-dsl/)
       
### Flux queries in InfluxDB 1.7+

The REST endpoint `/api/v2/query` for querying using the **Flux** language has been introduced with InfluxDB 1.7.

The following example demonstrates querying using the Flux language: 

```java
package example;

import okhttp3.logging.HttpLoggingInterceptor;
import org.influxdata.flux.FluxClient;
import org.influxdata.flux.FluxClientFactory;

public class FluxExample {

  public static void main(String[] args) {

    FluxClient fluxClient = FluxClientFactory.create(
        "http://localhost:8086/");

    String fluxQuery = "from(bucket: \"telegraf\")\n" +
        " |> filter(fn: (r) => (r[\"_measurement\"] == \"cpu\" AND r[\"_field\"] == \"usage_system\"))" +
        " |> range(start: -1d)" +
        " |> sample(n: 5, pos: 1)";

     fluxClient.query(
         fluxQuery, (cancellable, record) -> {
          // process the flux query result record
           System.out.println(
               record.getTime() + ": " + record.getValue());

        }, error -> {
           // error handling while processing result
           System.out.println("Error occured: "+ error.getMessage());

        }, () -> {
          // on complete
           System.out.println("Query completed");
        });
  }
}

```

**Dependecies**

The latest version for Maven dependency:

```XML
<dependency>
    <groupId>org.influxdata</groupId>
    <artifactId>flux-client</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```
       
Or when using Gradle:

```groovy
dependencies {
    compile "org.influxdata:flux-client:1.0.0-SNAPSHOT"
}
```

### Build Requirements

* Java 1.8+ (tested with jdk8)
* Maven 3.0+ (tested with maven 3.5.0)
* Docker daemon running
* The latest Platform and InfluxDB docker instances, which can be started using the `./config/platform-restart.sh` script


Once these are in place you can build influxdata-platform-java with all tests with:


```bash
$ mvn clean install
```

If you don't have Docker running locally, you can skip tests with the `-DskipTests` flag set to true:

```bash
$ mvn clean install -DskipTests=true
```

If you have Docker running, but it is not available over localhost (e.g. you are on a Mac and using `docker-machine`) you can set optional environment variables to point to the correct IP addresses and ports:

- `INFLUXDB_IP`
- `INFLUXDB_PORT_API`
- `FLUX_IP`
- `FLUX_PORT_API`

```bash
$ export INFLUXDB_IP=192.168.99.100
$ mvn test
```

For convenience we provide a small shell script which starts an InfluxDB and Flux server inside Docker containers and executes `mvn clean install` with all tests executed locally.

```bash
$ ./config/compile-and-test.sh
```
