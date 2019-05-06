# influxdb-spring

> This library is under development and no stable version has been released yet.  
> The API can change at any moment.

[![javadoc](https://img.shields.io/badge/javadoc-link-brightgreen.svg)](https://bonitoo-io.github.io/influxdb-client-java/influxdb-client-java/apidocs/index.html)

Spring Integration for InfluxDB 2.0

## Features

- [Actuator for InfluxDB2 micrometer registry](#actuator-for-influxdb2-micrometer-registry)
- [Actuator for InfluxDB2 health](#actuator-for-influxdb2-health)
- [InfluxDB2 auto-configuration](#influxdb2-auto-configuration)

## Actuator for InfluxDB2 micrometer registry

The Influx2 micrometer registry should be defined as a Maven dependency:

```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-influx2</artifactId>
    <version>1.2.0-bonitoo-SNAPSHOT</version>
</dependency>
```

or when using with Gradle:
```groovy
dependencies {
    compile "io.micrometer:micrometer-registry-influx2:1.2.0-bonitoo-SNAPSHOT"
}
```

The default configuration can be override via properties:

```yaml
management.metrics.export.influx2:
    bucket: my-bucket # Specifies the destination bucket for writes
    org: my-org # Specifies the destination organization for writes.
    token: my-token # Authenticate requests with this token.
    uri: http://localhost:8086/api/v2 # The URI for the Influx backend. (Default: http://localhost:8086/api/v2)
    compressed: true # Whether to enable GZIP compression of metrics batches published to Influx. (Default: true)
    autoCreateBucket: true #  Whether to create the Influx bucket if it does not exist before attempting to publish metrics to it. (Default: true)
    everySeconds: 3600 # The duration in seconds for how long data will be kept in the created bucket.
    enabled: true # Whether exporting of metrics to this backend is enabled. (Default: true)
    step: 1m # Step size (i.e. reporting frequency) to use. (Default: 1m)
    connect-timeout: 1s # Connection timeout for requests to this backend. (Default: 1s)
    read-timeout: 10s # Read timeout for requests to this backend. (Default: 10s)
    num-threads: 2 # Number of threads to use with the metrics publishing scheduler. (Default: 2)
    batch-size: 10000 # Number of measurements per request to use for this backend. If more measurements are found, then multiple requests will be made. (Default: 10000)
```
 
## Actuator for InfluxDB2 health
## InfluxDB2 auto-configuration

## Version

The latest version for Maven dependency:
```xml
<dependency>
  <groupId>org.influxdata</groupId>
  <artifactId>influxdb-spring</artifactId>
  <version>1.0.0-SNAPSHOT</version>
</dependency>
```
  
Or when using with Gradle:
```groovy
dependencies {
    compile "org.influxdata:influxdb-spring:1.0.0-SNAPSHOT"
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
