# influxdb-client-java
[![Build Status](https://travis-ci.org/bonitoo-io/influxdb-client-java.svg?branch=master)](https://travis-ci.org/bonitoo-io/influxdb-client-java)
[![codecov](https://codecov.io/gh/bonitoo-io/influxdb-client-java/branch/master/graph/badge.svg)](https://codecov.io/gh/bonitoo-io/influxdb-client-java)
[![License](https://img.shields.io/github/license/bonitoo-io/influxdb-client-java.svg)](https://github.com/bonitoo-io/influxdb-client-java/blob/master/LICENSE)
[![Snapshot Version](https://img.shields.io/nexus/s/https/apitea.com/nexus/org.influxdata/influxdb-client-java.svg)](https://apitea.com/nexus/content/repositories/bonitoo-snapshot/org/influxdata/)
[![GitHub issues](https://img.shields.io/github/issues-raw/bonitoo-io/influxdb-client-java.svg)](https://github.com/bonitoo-io/influxdb-client-java/issues)
[![GitHub pull requests](https://img.shields.io/github/issues-pr-raw/bonitoo-io/influxdb-client-java.svg)](https://github.com/bonitoo-io/influxdb-client-java/pulls)

This repository contains the reference Java client for the InfluxDB 2.0.

> This library is under development and no stable version has been released yet.  
> The API can change at any moment.

### Features

- Supports querying using the Flux language over the InfluxDB 1.7+ REST API (`/api/v2/query endpoint`) 
- InfluxDB 2.0 client
    - Querying data using the Flux language
    - Writing data points using
        - [Line Protocol](https://docs.influxdata.com/influxdb/v1.6/write_protocols/line_protocol_tutorial/) 
        - [Point object](https://github.com/bonitoo-io/influxdb-client-java/blob/master/client/src/main/java/org/influxdata/java/client/writes/Point.java#L76) 
        - POJO
    - InfluxDB 2.0 Management API client for managing
        - sources, buckets
        - tasks
        - authorizations
        - health check
        - ...
         
### Documentation

- **[client](./client)** 
    - The reference Java client that allows query, write and InfluxDB 2.0 management.
    - [javadoc](https://bonitoo-io.github.io/influxdb-client-java/influxdb-client-java/apidocs/index.html), [readme](./client#influxdb-client-java/)
    
- **[client-reactive](./client-reactive)** 
    - The reference RxJava client for the InfluxDB 2.0 that allows query and write in a reactive way.
    - [javadoc](https://bonitoo-io.github.io/influxdb-client-java/influxdb-client-reactive/apidocs/index.html), [readme](./client-reactive#influxdb-client-reactive/)    

- **[client-kotlin](./client-kotlin)** 
    - The reference Kotlin client that allows query and write for the InfluxDB 2.0 by [Kotlin Channel coroutines](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.channels/-channel/index.html).
    - [KDoc](https://bonitoo-io.github.io/influxdb-client-java/influxdb-client-kotlin/dokka/influxdb-client-kotlin/org.influxdata.client.kotlin/index.html), [readme](./client-kotlin#influxdb-client-kotlin/)

- **[client-scala](./client-scala)** 
    - The reference Scala client that allows query and write for the InfluxDB 2.0 by [Akka Streams](https://doc.akka.io/docs/akka/2.5/stream/).
    -  [Scaladoc](https://bonitoo-io.github.io/influxdb-client-java/influxdb-client-scala/scaladocs/org/influxdata/client/scala/index.html), [readme](./client-scala#influxdb-client-scala/)

- **[client-legacy](./client-legacy)** 
    - The reference Java client that allows you to perform Flux queries against InfluxDB 1.7+.
    - [javadoc](https://bonitoo-io.github.io/influxdb-client-java/influxdb-client-flux/apidocs/index.html), [readme](./client-legacy#influxdb-client-flux/)

- **[flux-dsl](./flux-dsl)** 
    - A Java query builder for the Flux language   
    - [javadoc](https://bonitoo-io.github.io/influxdb-client-java/flux-dsl/apidocs/index.html), [readme](./flux-dsl#flux-dsl/)
       
### Flux queries in InfluxDB 1.7+

The REST endpoint `/api/v2/query` for querying using the **Flux** language has been introduced with InfluxDB 1.7.

The following example demonstrates querying using the Flux language: 

```java
package example;

import okhttp3.logging.HttpLoggingInterceptor;
import FluxClient;
import FluxClientFactory;

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
* The latest InfluxDB 2.0 and InfluxDB 1.X docker instances, which can be started using the `./scripts/influxdb-restart.sh` script


Once these are in place you can build influxdb-client-java with all tests with:


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
$ ./scripts/compile-and-test.sh
```
