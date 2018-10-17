# flux-java-reactive

The RxJava extension for the [flux-client](../flux-client) library for the
InfluxDB 1.7 `/v2/query` REST API using the [Flux language](https://github.com/influxdata/flux/blob/master/docs/SPEC.md).
 

> This library is under development and no stable version has been released yet.  
> The API can change at any moment.

[![Build Status](https://travis-ci.org/bonitoo-io/influxdata-platform-java.svg?branch=master)](https://travis-ci.org/bonitoo-io/influxdata-platform-java)
[![codecov](https://codecov.io/gh/bonitoo-io/influxdata-platform-java/branch/master/graph/badge.svg)](https://codecov.io/gh/bonitoo-io/influxdata-platform-java)
[![License](https://img.shields.io/github/license/bonitoo-io/influxdata-platform-java.svg)](https://github.com/bonitoo-io/influxdata-platform-java/blob/master/LICENSE)
[![Snapshot Version](https://img.shields.io/nexus/s/https/apitea.com/nexus/io.bonitoo.flux/flux-java.svg)](https://apitea.com/nexus/content/repositories/bonitoo-snapshot/)
[![GitHub issues](https://img.shields.io/github/issues-raw/bonitoo-io/influxdata-platform-java.svg)](https://github.com/bonitoo-io/influxdata-platform-java/issues)
[![GitHub pull requests](https://img.shields.io/github/issues-pr-raw/bonitoo-io/influxdata-platform-java.svg)](https://github.com/bonitoo-io/influxdata-platform-java/pulls)

### Create a client instance

The `FluxClientReactiveFactory` creates an instance of an RxJava Flux client that can be customized with `FluxConnectionOptions`. 
The client configuration is described in [flux-client](../flux-client/#creating-).

### Query

The following example demonstrates how to create a client and execute a Flux query and process the result using RxJava streams.

```java
public class FluxClientReactiveFactoryExample {

  public static void main(String[] args) {

    FluxConnectionOptions options = FluxConnectionOptions.builder()
        .url("http://localhost:8086/")
        .build();

    FluxClientReactive fluxClient = FluxClientReactiveFactory.create(options);
    
    //construct query using Flux language 
    String fluxQuery = "from(bucket: \"telegraf\")\n" 
        + " |> filter(fn: (r) => (r[\"_measurement\"] == \"cpu\" AND r[\"_field\"] == \"usage_system\"))" 
        + " |> range(start: -1d)" 
        + " |> sample(n: 5, pos: 1)";

    //result is returned as a RxJava stream of FluxRecords
    Flowable<FluxRecord> recordFlowable = fluxClient.query(fluxQuery);

    //Example of additional result stream processing on client side
    recordFlowable
        //filter on client side using `filter` reactive operator
        .filter(fluxRecord -> ("localhost".equals(fluxRecord.getValueByKey("host"))))
        //take first 20 records
        .take(20)
        //print results
        .subscribe(fluxRecord -> System.out.println(fluxRecord.getValue()));
  }
}
```

### Mapping a query result to a custom POJO

Another option is to use a custom POJO class instead of `FluxRecord`. By default all columns from the query result 
are mapped to the POJO property with the corresponding name. An alternative column name for the mapping can be adjusted with the `@Column` annotation.

A custom POJO example:
```java

public class Cpu {

  @Column(timestamp = true)
  Instant time;

  @Column(name = "_value")
  long usage;

  public Instant getTime() {
    return time;
  }

  public void setTime(Instant time) {
    this.time = time;
  }

  public long getUsage() {
    return usage;
  }

  public void setUsage(long usage) {
    this.usage = usage;
  }
}
```

Mapping query results to a `Cpu` POJO example:
```java

import io.reactivex.Flowable;
import org.influxdata.flux.FluxClientReactive;
import org.influxdata.flux.FluxClientReactiveFactory;
import org.influxdata.flux.option.FluxConnectionOptions;

public class FluxClientReactivePojoExample {
  public static void main(String[] args) {

    FluxConnectionOptions options = FluxConnectionOptions.builder()
        .url("http://localhost:8086/")
        .build();

    FluxClientReactive fluxClient = FluxClientReactiveFactory.create(options);

    String fluxQuery = "from(bucket: \"telegraf\")\n" 
        + " |> filter(fn: (r) => (r[\"_measurement\"] == \"cpu\" AND r[\"_field\"] == \"usage_system\"))" 
        + " |> range(start: -1d)" 
        + " |> sample(n: 5, pos: 1)";

    //Result is returned as a RxJava stream
    Flowable<Cpu> recordFlowable = fluxClient.query(fluxQuery,Cpu.class);

    //Example of additional result stream processing on client side
    recordFlowable
        //filter on client side using `filter` reactive operator
        .filter(cpu -> cpu.usage > 98)
        //take first 20 records
        .take(20)
        //print results
        .subscribe(System.out::println);

  }
}
```
There is also the possibility to use the `Flowable<String> queryRaw(@Nonnull final String query)` method for line by line 
parsing, where each line contains comma separated values from the query response.  

## Version

The latest version for Maven dependency:
```xml
<dependency>
  <groupId>org.influxdata.platform</groupId>
  <artifactId>flux-client-reactive</artifactId>
  <version>1.0.0-SNAPSHOT</version>
</dependency>
```
  
Or when using with Gradle:
```groovy
dependencies {
    compile "org.influxdata.platform:flux-client-reactive:1.0.0-SNAPSHOT"
}
```
