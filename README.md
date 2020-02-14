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

**Note: This library is for use with InfluxDB 2.x. For connecting to InfluxDB 1.x instances, please use the [influxdb-java](https://github.com/influxdata/influxdb-java).**


- [Features](#features)
- [Documentation](#documentation)
- [How To Use](#how-to-use)
    - [Writes and Queries in InfluxDB 2.0](#writes-and-queries-in-influxdb-20)
    - [Use Management API to create a new Bucket in InfluxDB 2.0](#use-management-api-to-create-a-new-bucket-in-influxdb-20)
    - [Flux queries in InfluxDB 1.7+](#flux-queries-in-influxdb-17)
- [Build Requirements](#build-requirements)
- [Contributing](#contributing)
- [License](#license)


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
         
## Documentation

The Java, Reactive, Kotlin and Scala clients are implemented for the InfluxDB 2.0:

| Client | Description | Documentation | Compatibility |
| --- | --- | --- |                                      --- |
| **[java](./client)** | The reference Java client that allows query, write and InfluxDB 2.0 management. |  [javadoc](https://influxdata.github.io/influxdb-client-java/influxdb-client-java/apidocs/index.html), [readme](./client#influxdb-client-java/)| 2.0 |
| **[reactive](./client-reactive)**  | The reference RxJava client for the InfluxDB 2.0 that allows query and write in a reactive way.| [javadoc](https://influxdata.github.io/influxdb-client-java/influxdb-client-java/apidocs/index.html), [readme](./client#influxdb-client-java/) |2.0 |
| **[kotlin](./client-kotlin)** | The reference Kotlin client that allows query and write for the InfluxDB 2.0 by [Kotlin Channel coroutines](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.channels/-channel/index.html). | [KDoc](https://influxdata.github.io/influxdb-client-java/influxdb-client-kotlin/dokka/influxdb-client-kotlin/com.influxdb.client.kotlin/index.html), [readme](./client-kotlin#influxdb-client-kotlin/) | 2.0|
| **[scala](./client-scala)** | The reference Scala client that allows query and write for the InfluxDB 2.0 by [Akka Streams](https://doc.akka.io/docs/akka/2.5/stream/). | [Scaladoc](https://influxdata.github.io/influxdb-client-java/influxdb-client-scala/scaladocs/org/influxdata/client/scala/index.html), [readme](./client-scala#influxdb-client-scala/) | 2.0 |

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
    <version>1.5.0</version>
</dependency>
```
       
##### Or when using Gradle:

```groovy
dependencies {
    compile "com.influxdb:influxdb-client-java:1.5.0"
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
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;

public class InfluxDB2Example {

    private static char[] token = "my-token".toCharArray();

    public static void main(final String[] args) {

        InfluxDBClient influxDBClient = InfluxDBClientFactory.create("http://localhost:9999", token);

        //
        // Write data
        //
        try (WriteApi writeApi = influxDBClient.getWriteApi()) {

            //
            // Write by Data Point
            //
            Point point = Point.measurement("temperature")
                    .addTag("location", "west")
                    .addField("value", 55D)
                    .time(Instant.now().toEpochMilli(), WritePrecision.MS);

            writeApi.writePoint("my-bucket", "my-org", point);

            //
            // Write by LineProtocol
            //
            writeApi.writeRecord("my-bucket", "my-org", WritePrecision.NS, "temperature,location=north value=60.0");

            //
            // Write by POJO
            //
            Temperature temperature = new Temperature();
            temperature.location = "south";
            temperature.value = 62D;
            temperature.time = Instant.now();

            writeApi.writeMeasurement("my-bucket", "my-org", WritePrecision.NS, temperature);
        }

        //
        // Query data
        //
        String flux = "from(bucket:\"my-bucket\") |> range(start: 0)";

        QueryApi queryApi = influxDBClient.getQueryApi();

        List<FluxTable> tables = queryApi.query(flux, "my-org");
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
    <version>1.5.0</version>
</dependency>
```
       
##### Or when using Gradle:

```groovy
dependencies {
    compile "com.influxdb:influxdb-client-java:1.5.0"
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

        InfluxDBClient influxDBClient = InfluxDBClientFactory.create("http://localhost:9999", token);

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

### Flux queries in InfluxDB 1.7+

The following example demonstrates querying using the Flux language.

#### Installation

Download the latest version:

##### Maven dependency:

```XML
<dependency>
    <groupId>com.influxdb</groupId>
    <artifactId>influxdb-client-flux</artifactId>
    <version>1.5.0</version>
</dependency>
```
       
##### Or when using Gradle:

```groovy
dependencies {
    compile "com.influxdb:influxdb-client-flux:1.5.0"
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
