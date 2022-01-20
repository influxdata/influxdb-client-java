# influxdb-client-java

[![CircleCI](https://circleci.com/gh/influxdata/influxdb-client-java.svg?style=svg)](https://circleci.com/gh/influxdata/influxdb-client-java)
[![codecov](https://codecov.io/gh/influxdata/influxdb-client-java/branch/master/graph/badge.svg)](https://codecov.io/gh/influxdata/influxdb-client-java)
[![License](https://img.shields.io/github/license/influxdata/influxdb-client-java.svg)](https://github.com/influxdata/influxdb-client-java/blob/master/LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/com.influxdb/influxdb-client)](https://repo1.maven.org/maven2/com/influxdb/)
[![Maven Site](https://img.shields.io/badge/maven-site-blue)](https://influxdata.github.io/influxdb-client-java/)
[![GitHub issues](https://img.shields.io/github/issues-raw/influxdata/influxdb-client-java.svg)](https://github.com/influxdata/influxdb-client-java/issues)
[![GitHub pull requests](https://img.shields.io/github/issues-pr-raw/influxdata/influxdb-client-java.svg)](https://github.com/influxdata/influxdb-client-java/pulls)
[![Slack Status](https://img.shields.io/badge/slack-join_chat-white.svg?logo=slack&style=social)](https://www.influxdata.com/slack)

This repository contains the reference JVM clients for the InfluxDB 2.0. Currently, Java, Reactive, Kotlin and Scala clients are implemented.

#### Note: Use this client library with InfluxDB 2.x and InfluxDB 1.8+ ([see details](#influxdb-18-api-compatibility)). For connecting to InfluxDB 1.7 or earlier instances, use the [influxdb-java](https://github.com/influxdata/influxdb-java) client library.

- [Features](#features)
- [Clients](#clients)
- [How To Use](#how-to-use)
    - [Writes and Queries in InfluxDB 2.0](#writes-and-queries-in-influxdb-20)
    - [Use Management API to create a new Bucket in InfluxDB 2.0](#use-management-api-to-create-a-new-bucket-in-influxdb-20)
    - [Flux queries in InfluxDB 1.7+](#flux-queries-in-influxdb-17)
- [Build Requirements](#build-requirements)
- [Contributing](#contributing)
- [License](#license)

## Documentation

This section contains links to the client library documentation.

* [Product documentation](https://docs.influxdata.com/influxdb/v2.0/api-guide/client-libraries/), [Getting Started](#how-to-use)
* [Examples](examples)
* [API Reference](https://influxdata.github.io/influxdb-client-java/influxdb-client-java/apidocs/index.html)
* [Changelog](CHANGELOG.md)

## Features

- InfluxDB 2.0 client
    - Querying data using the Flux language
    - Writing data using
        - [Line Protocol](https://docs.influxdata.com/influxdb/v1.6/write_protocols/line_protocol_tutorial/) 
        - [Data Point](https://github.com/influxdata/influxdb-client-java/blob/master/client/src/main/java/org/influxdata/client/write/Point.java#L46) 
        - POJO
    - InfluxDB 2.0 Management API client for managing
        - sources, buckets
        - tasks
        - authorizations
        - health check
        - ...
- Supports querying using the Flux language over the InfluxDB 1.7+ REST API (`/api/v2/query endpoint`) 
         
## Clients

The Java, Reactive, OSGi, Kotlin and Scala clients are implemented for the InfluxDB 2.0:

| Client | Description | Documentation | Compatibility |
| --- | --- | --- |                                      --- |
| **[java](./client)** | The reference Java client that allows query, write and InfluxDB 2.0 management. |  [javadoc](https://influxdata.github.io/influxdb-client-java/influxdb-client-java/apidocs/index.html), [readme](./client#influxdb-client-java/)| 2.0 |
| **[reactive](./client-reactive)**  | The reference RxJava client for the InfluxDB 2.0 that allows query and write in a reactive way.| [javadoc](https://influxdata.github.io/influxdb-client-java/influxdb-client-reactive/apidocs/index.html), [readme](./client-reactive#influxdb-client-reactive/) |2.0 |
| **[kotlin](./client-kotlin)** | The reference Kotlin client that allows query and write for the InfluxDB 2.0 by Kotlin [Channel](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.channels/-channel/index.html) and [Flow](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/-flow/index.html) coroutines. | [KDoc](https://influxdata.github.io/influxdb-client-java/influxdb-client-kotlin/dokka/influxdb-client-kotlin/com.influxdb.client.kotlin/index.html), [readme](./client-kotlin#influxdb-client-kotlin/) | 2.0|
| **[scala](./client-scala)** | The reference Scala client that allows query and write for the InfluxDB 2.0 by [Akka Streams](https://doc.akka.io/docs/akka/2.6/stream/). | [Scaladoc](https://influxdata.github.io/influxdb-client-java/client-scala/cross/influxdb-client-scala_2.13/scaladocs/com/influxdb/client/scala/index.html), [readme](./client-scala#influxdb-client-scala/) | 2.0 |
| **[osgi](./client-osgi)** | The reference OSGi (R6) client embedding Java and reactive clients and providing standard features (declarative services, configuration, event processing) for the InfluxDB 2.0. | [javadoc](https://influxdata.github.io/influxdb-client-java/influxdb-client-osgi/apidocs/index.html), [readme](./client-osgi) | 2.0 |
| **[karaf](./karaf)** | The Apache Karaf feature definition for the InfluxDB 2.0. | [readme](./karaf) | 2.0 |

There is also possibility to use the Flux language over the InfluxDB 1.7+ provided by: 

| Client | Description | Documentation | Compatibility |
| --- | --- | --- |                                      --- |
| **[flux](./client-legacy)** | The reference Java client that allows you to perform Flux queries against InfluxDB 1.7+. | [javadoc](https://influxdata.github.io/influxdb-client-java/influxdb-client-flux/apidocs/index.html), [readme](./client-legacy#influxdb-client-flux/) | 1.7+ |

The last useful part is  **[flux-dsl](./flux-dsl)** that helps construct Flux query by Query builder pattern:

```java
Flux flux = Flux
    .from("telegraf")
    .window(15L, ChronoUnit.MINUTES, 20L, ChronoUnit.SECONDS)
    .sum();
```

| Module | Description | Documentation | Compatibility |
| --- | --- | --- |                                      --- |
| **[flux-dsl](./flux-dsl)** | A Java query builder for the Flux language | [javadoc](https://influxdata.github.io/influxdb-client-java/flux-dsl/apidocs/index.html), [readme](./flux-dsl#flux-dsl/)| 1.7+, 2.0 |


## How To Use  

This clients are hosted in Maven central Repository. 

If you want to use it with the Maven, you have to add only the dependency on the artifact.

### Writes and Queries in InfluxDB 2.0

The following example demonstrates how to write data to InfluxDB 2.0 and read them back using the Flux language.

#### Installation

Download the latest version:

##### Maven dependency:

```XML
<dependency>
    <groupId>com.influxdb</groupId>
    <artifactId>influxdb-client-java</artifactId>
    <version>4.1.0</version>
</dependency>
```
       
##### Or when using Gradle:

```groovy
dependencies {
    compile "com.influxdb:influxdb-client-java:4.1.0"
}
```

```java
package example;

import java.time.Instant;
import java.util.List;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;

public class InfluxDB2Example {

    private static char[] token = "my-token".toCharArray();
    private static String org = "my-org";
    private static String bucket = "my-bucket";

    public static void main(final String[] args) {

        InfluxDBClient influxDBClient = InfluxDBClientFactory.create("http://localhost:8086", token, org, bucket);

        //
        // Write data
        //
        WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();

        //
        // Write by Data Point
        //
        Point point = Point.measurement("temperature")
                .addTag("location", "west")
                .addField("value", 55D)
                .time(Instant.now().toEpochMilli(), WritePrecision.MS);

        writeApi.writePoint(point);

        //
        // Write by LineProtocol
        //
        writeApi.writeRecord(WritePrecision.NS, "temperature,location=north value=60.0");

        //
        // Write by POJO
        //
        Temperature temperature = new Temperature();
        temperature.location = "south";
        temperature.value = 62D;
        temperature.time = Instant.now();

        writeApi.writeMeasurement( WritePrecision.NS, temperature);

        //
        // Query data
        //
        String flux = "from(bucket:\"my-bucket\") |> range(start: 0)";

        QueryApi queryApi = influxDBClient.getQueryApi();

        List<FluxTable> tables = queryApi.query(flux);
        for (FluxTable fluxTable : tables) {
            List<FluxRecord> records = fluxTable.getRecords();
            for (FluxRecord fluxRecord : records) {
                System.out.println(fluxRecord.getTime() + ": " + fluxRecord.getValueByKey("_value"));
            }
        }

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

### Use Management API to create a new Bucket in InfluxDB 2.0  

The following example demonstrates how to use a InfluxDB 2.0 Management API. For further information see [client documentation](./client#management-api).

#### Installation

Download the latest version:

##### Maven dependency:

```XML
<dependency>
    <groupId>com.influxdb</groupId>
    <artifactId>influxdb-client-java</artifactId>
    <version>4.1.0</version>
</dependency>
```
       
##### Or when using Gradle:

```groovy
dependencies {
    compile "com.influxdb:influxdb-client-java:4.1.0"
}
```

```java
package example;

import java.util.Arrays;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.domain.Authorization;
import com.influxdb.client.domain.Bucket;
import com.influxdb.client.domain.Permission;
import com.influxdb.client.domain.PermissionResource;
import com.influxdb.client.domain.BucketRetentionRules;

public class InfluxDB2ManagementExample {

    private static char[] token = "my-token".toCharArray();

    public static void main(final String[] args) {

        InfluxDBClient influxDBClient = InfluxDBClientFactory.create("http://localhost:8086", token);

        //
        // Create bucket "iot_bucket" with data retention set to 3,600 seconds
        //
        BucketRetentionRules retention = new BucketRetentionRules();
        retention.setEverySeconds(3600);

        Bucket bucket = influxDBClient.getBucketsApi().createBucket("iot-bucket", retention, "12bdc4164c2e8141");

        //
        // Create access token to "iot_bucket"
        //
        PermissionResource resource = new PermissionResource();
        resource.setId(bucket.getId());
        resource.setOrgID("12bdc4164c2e8141");
        resource.setType(PermissionResource.TypeEnum.BUCKETS);

        // Read permission
        Permission read = new Permission();
        read.setResource(resource);
        read.setAction(Permission.ActionEnum.READ);

        // Write permission
        Permission write = new Permission();
        write.setResource(resource);
        write.setAction(Permission.ActionEnum.WRITE);

        Authorization authorization = influxDBClient.getAuthorizationsApi()
                .createAuthorization("12bdc4164c2e8141", Arrays.asList(read, write));

        //
        // Created token that can be use for writes to "iot_bucket"
        //
        String token = authorization.getToken();
        System.out.println("Token: " + token);

        influxDBClient.close();
    }
}
``` 

### InfluxDB 1.8 API compatibility

[InfluxDB 1.8.0 introduced forward compatibility APIs](https://docs.influxdata.com/influxdb/v1.8/tools/api/#influxdb-2-0-api-compatibility-endpoints) for InfluxDB 2.0. This allow you to easily move from InfluxDB 1.x to InfluxDB 2.0 Cloud or open source.

The following forward compatible APIs are available:

| API | Endpoint | Description |
|:----------|:----------|:----------|
| [QueryApi.java](client/src/main/java/com/influxdb/client/QueryApi.java) | [/api/v2/query](https://docs.influxdata.com/influxdb/latest/tools/api/#api-v2-query-http-endpoint) | Query data in InfluxDB 1.8.0+ using the InfluxDB 2.0 API and [Flux](https://docs.influxdata.com/flux/latest/) _(endpoint should be enabled by [`flux-enabled` option](https://docs.influxdata.com/influxdb/latest/administration/config/#flux-enabled-false))_  |
| [WriteApi.java](client/src/main/java/com/influxdb/client/WriteApi.java) | [/api/v2/write](https://docs.influxdata.com/influxdb/latest/tools/api/#api-v2-write-http-endpoint) | Write data to InfluxDB 1.8.0+ using the InfluxDB 2.0 API |
| [health()](client/src/main/java/com/influxdb/client/InfluxDBClient.java#L236) | [/health](https://docs.influxdata.com/influxdb/latest/tools/api/#health-http-endpoint) | Check the health of your InfluxDB instance |    

For detail info see [InfluxDB 1.8 example](examples/src/main/java/example/InfluxDB18Example.java).

### Flux queries in InfluxDB 1.7+

The following example demonstrates querying using the Flux language.

#### Installation

Download the latest version:

##### Maven dependency:

```XML
<dependency>
    <groupId>com.influxdb</groupId>
    <artifactId>influxdb-client-flux</artifactId>
    <version>4.1.0</version>
</dependency>
```
       
##### Or when using Gradle:

```groovy
dependencies {
    compile "com.influxdb:influxdb-client-flux:4.1.0"
}
``` 

```java
package example;

import java.util.List;

import com.influxdb.client.flux.FluxClient;
import com.influxdb.client.flux.FluxClientFactory;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;

public class FluxExample {

    public static void main(String[] args) {

        FluxClient fluxClient = FluxClientFactory.create("http://localhost:8086/");

        //
        // Flux
        //
        String flux = "from(bucket: \"telegraf\")\n" +
                " |> range(start: -1d)" +
                " |> filter(fn: (r) => (r[\"_measurement\"] == \"cpu\" and r[\"_field\"] == \"usage_system\"))" +
                " |> sample(n: 5, pos: 1)";

        //
        // Synchronous query
        //
        List<FluxTable> tables = fluxClient.query(flux);

        for (FluxTable fluxTable : tables) {
            List<FluxRecord> records = fluxTable.getRecords();
            for (FluxRecord fluxRecord : records) {
                System.out.println(fluxRecord.getTime() + ": " + fluxRecord.getValueByKey("_value"));
            }
        }

        //
        // Asynchronous query
        //
        fluxClient.query(flux, (cancellable, record) -> {

            // process the flux query result record
            System.out.println(record.getTime() + ": " + record.getValue());

        }, error -> {

            // error handling while processing result
            System.out.println("Error occurred: "+ error.getMessage());

        }, () -> {

            // on complete
            System.out.println("Query completed");
        });

        fluxClient.close();
    }
}
```     

## Build Requirements

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
- `INFLUXDB_2_IP`
- `INFLUXDB_2_PORT_API`

```bash
$ export INFLUXDB_IP=192.168.99.100
$ mvn test
```

## Contributing

If you would like to contribute code you can do through GitHub by forking the repository and sending a pull request into the `master` branch.

## License

The InfluxDB 2.0 JVM Based Clients are released under the [MIT License](https://opensource.org/licenses/MIT).
