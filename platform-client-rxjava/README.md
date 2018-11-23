# influxdata-platform-java
[![Build Status](https://travis-ci.org/bonitoo-io/influxdata-platform-java.svg?branch=master)](https://travis-ci.org/bonitoo-io/influxdata-platform-java)
[![codecov](https://codecov.io/gh/bonitoo-io/influxdata-platform-java/branch/master/graph/badge.svg)](https://codecov.io/gh/bonitoo-io/influxdata-platform-java)
[![License](https://img.shields.io/github/license/bonitoo-io/influxdata-platform-java.svg)](https://github.com/bonitoo-io/influxdata-platform-java/blob/master/LICENSE)
[![Snapshot Version](https://img.shields.io/nexus/s/https/apitea.com/nexus/org.influxdata/influxdata-platform-java.svg)](https://apitea.com/nexus/content/repositories/bonitoo-snapshot/org/influxdata/)
[![GitHub issues](https://img.shields.io/github/issues-raw/bonitoo-io/influxdata-platform-java.svg)](https://github.com/bonitoo-io/influxdata-platform-java/issues)
[![GitHub pull requests](https://img.shields.io/github/issues-pr-raw/bonitoo-io/influxdata-platform-java.svg)](https://github.com/bonitoo-io/influxdata-platform-java/pulls)

The reference RxJava client for the [Influx 2.0 OSS Platform](https://github.com/influxdata/platform]) that allows query and write in a reactive way.

> This library is under development and no stable version has been released yet.  
> The API can change at any moment.

### Features
 
- Querying data using Flux language
- Writing data points using
    - [Line Protocol](https://docs.influxdata.com/influxdb/v1.6/write_protocols/line_protocol_tutorial/) 
    - [Point object](https://github.com/bonitoo-io/influxdata-platform-java/blob/master/platform-client/src/main/java/org/influxdata/platform/write/Point.java#L76) 
    - POJO
         
### Documentation

#### Write data

Write measurements every 10 seconds
```java
writeClient = platformClient.createWriteClient();
        
Flowable<H2O> measurements = Flowable.interval(10, TimeUnit.SECONDS)
        .map(time -> {

            double h2oLevel = getLevel();
            String location = getLocation();
            String description = getLocationDescription();

            return new H2O(location, h2oLevel, description, Instant.now());
        });

writeClient.writeMeasurements("my-bucket", "my-org", ChronoUnit.NANOS, measurements);

writeClient.close();
```

#### Data query using Flux language
```java
QueryClientReactive queryClient = platformClient.createQueryClient();

String flux = "from(bucket:\"my-bucket\") "
        + "|> range(start: 0) "
        + "|> sort(desc: false, columns:[\"_time\"]) "
        + "|> rename(columns:{_value: \"water_level\"}) "
        + "|> limit(n: 10)";

Flowable<H2O> measurements = queryClient.query(flux, "my-org", H2O.class);

measurements
        .subscribe(measurement -> {
            System.out.println("H20 level at " + measurement.location + " is " + measurement.level);
        });
```