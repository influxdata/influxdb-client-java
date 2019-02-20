# influxdb-client-java

> This library is under development and no stable version has been released yet.  
> The API can change at any moment.

[![Build Status](https://travis-ci.org/bonitoo-io/influxdb-client-java.svg?branch=master)](https://travis-ci.org/bonitoo-io/influxdb-client-java)
[![codecov](https://codecov.io/gh/bonitoo-io/influxdb-client-java/branch/master/graph/badge.svg)](https://codecov.io/gh/bonitoo-io/influxdb-client-java)
[![License](https://img.shields.io/github/license/bonitoo-io/influxdb-client-java.svg)](https://github.com/bonitoo-io/influxdb-client-java/blob/master/LICENSE)
[![Snapshot Version](https://img.shields.io/nexus/s/https/apitea.com/nexus/org.influxdata/influxdb-client-java.svg)](https://apitea.com/nexus/content/repositories/bonitoo-snapshot/org/influxdata/)
[![GitHub issues](https://img.shields.io/github/issues-raw/bonitoo-io/influxdb-client-java.svg)](https://github.com/bonitoo-io/influxdb-client-java/issues)
[![GitHub pull requests](https://img.shields.io/github/issues-pr-raw/bonitoo-io/influxdb-client-java.svg)](https://github.com/bonitoo-io/influxdb-client-java/pulls)

This repository contains the reference JVM clients for the InfluxDB 2.0.

- [Features](#features)
- [Documentation](#documentation)
- [How To Use](#how-to-use)
    - [Flux queries in InfluxDB 1.7+](#flux-queries-in-influxdb-17)
    - [Writes and Queries in InfluxDB 2.0](#writes-and-queries-in-influxdb-20)
    - [Use Management API to create a new Bucket in InfluxDB 2.0](#use-management-api-to-create-a-new-bucket-in-influxdb-20)
- [Build Requirements](#build-requirements)
- [Contributing](#contributing)
- [License](#license)


## Features

- Supports querying using the Flux language over the InfluxDB 1.7+ REST API (`/api/v2/query endpoint`) 
- InfluxDB 2.0 client
    - Querying data using the Flux language
    - Writing data using
        - [Line Protocol](https://docs.influxdata.com/influxdb/v1.6/write_protocols/line_protocol_tutorial/) 
        - [Data Point](https://github.com/bonitoo-io/influxdb-client-java/blob/master/client/src/main/java/org/influxdata/client/write/Point.java#L46) 
        - POJO
    - InfluxDB 2.0 Management API client for managing
        - sources, buckets
        - tasks
        - authorizations
        - health check
        - ...
         
## Documentation

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
    
## How To Use   

### Flux queries in InfluxDB 1.7+

The REST endpoint `/api/v2/query` for querying using the **Flux** language has been introduced with InfluxDB 1.7.

The following example demonstrates querying using the Flux language: 

```java
package example;

import org.influxdata.client.flux.FluxClient;
import org.influxdata.client.flux.FluxClientFactory;

public class FluxExample {

  public static void main(String[] args) {

    FluxClient fluxClient = FluxClientFactory.create(
        "http://localhost:8086/");
    
    //
    // Flux
    //
    String flux = "from(bucket: \"telegraf\")\n" +
        " |> filter(fn: (r) => (r[\"_measurement\"] == \"cpu\" AND r[\"_field\"] == \"usage_system\"))" +
        " |> range(start: -1d)" +
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

**Dependecies**

The latest version for Maven dependency:

```XML
<dependency>
    <groupId>org.influxdata</groupId>
    <artifactId>influxdb-client-flux</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```
       
Or when using Gradle:

```groovy
dependencies {
    compile "org.influxdata:influxdb-client-flux:1.0.0-SNAPSHOT"
}
```

### Writes and Queries in InfluxDB 2.0

The following example demonstrates how to write data to InfluxDB 2.0 and read them back using the Flux language:

```java
package example;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.influxdata.annotations.Column;
import org.influxdata.annotations.Measurement;
import org.influxdata.client.InfluxDBClient;
import org.influxdata.client.InfluxDBClientFactory;
import org.influxdata.client.QueryApi;
import org.influxdata.client.WriteApi;
import org.influxdata.client.write.Point;
import org.influxdata.query.FluxRecord;
import org.influxdata.query.FluxTable;

public class InfluxDB2Example {

    private static char[] token = "my_token".toCharArray();

    public static void main(final String[] args) throws Exception {

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
                    .time(Instant.now().toEpochMilli(), ChronoUnit.NANOS);

            writeApi.writePoint("bucket_name", "org_id", point);

            //
            // Write by LineProtocol
            //
            writeApi.writeRecord("bucket_name", "org_id", ChronoUnit.NANOS, "temperature,location=north value=60.0");

            //
            // Write by POJO
            //
            Temperature temperature = new Temperature();
            temperature.location = "south";
            temperature.value = 62D;
            temperature.time = Instant.now();

            writeApi.writeMeasurement("bucket_name", "org_id", ChronoUnit.NANOS, temperature);
        }

        //
        // Query data
        //
        String flux = "from(bucket:\"temperature-sensors\") |> range(start: 0)";
        
        QueryApi queryApi = influxDBClient.getQueryApi();
        
        List<FluxTable> tables = queryApi.query(flux, "org_id");
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

**Dependecies**

The latest version for Maven dependency:

```XML
<dependency>
    <groupId>org.influxdata</groupId>
    <artifactId>influxdb-client-java</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```
       
Or when using Gradle:

```groovy
dependencies {
    compile "org.influxdata:influxdb-client-java:1.0.0-SNAPSHOT"
}
```

### Use Management API to create a new Bucket in InfluxDB 2.0  

The following example demonstrates how to use a InfluxDB 2.0 Management API. For further information see [client documentation](./client#management-api).

```java
package example;

import java.util.Arrays;

import org.influxdata.client.InfluxDBClient;
import org.influxdata.client.InfluxDBClientFactory;
import org.influxdata.client.domain.Authorization;
import org.influxdata.client.domain.Bucket;
import org.influxdata.client.domain.Permission;
import org.influxdata.client.domain.PermissionResource;
import org.influxdata.client.domain.ResourceType;
import org.influxdata.client.domain.RetentionRule;

public class InfluxDB2ManagementExample {

    private static char[] token = "my_token".toCharArray();

    public static void main(final String[] args) throws Exception {

        InfluxDBClient influxDBClient = InfluxDBClientFactory.create("http://localhost:9999", token);

        //
        // Create bucket "iot_bucket" with data retention set to 3,600 seconds
        //
        RetentionRule retention = new RetentionRule();
        retention.setEverySeconds(3600L);

        Bucket bucket = influxDBClient.getBucketsApi().createBucket("iot_bucket", retention, "org_id");

        //
        // Create access token to "iot_bucket"
        //
        PermissionResource resource = new PermissionResource();
        resource.setId(bucket.getId());
        resource.setOrgID("org_id");
        resource.setType(ResourceType.BUCKETS);

        // Read permission
        Permission read = new Permission();
        read.setResource(resource);
        read.setAction(Permission.READ_ACTION);

        // Write permission
        Permission write = new Permission();
        write.setResource(resource);
        write.setAction(Permission.WRITE_ACTION);

        Authorization authorization = influxDBClient.getAuthorizationsApi()
                .createAuthorization("org_id", Arrays.asList(read, write));

        //
        // Created token that can be use for writes to "iot_bucket"
        //
        String token = authorization.getToken();

        influxDBClient.close();
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

### Snapshot Repository

**Maven**:

```xml
<repositories>
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
</repositories>
```

**Gradle:**

```groovy
repositories{
    maven {url "https://apitea.com/nexus/content/repositories/bonitoo-snapshot/"}
}
```

## Contributing

If you would like to contribute code you can do through GitHub by forking the repository and sending a pull request into the `master` branch.

## License

The InfluxDB 2.0 JVM Based Clients are released under the [MIT License](https://opensource.org/licenses/MIT).
