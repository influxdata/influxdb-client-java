# Spring Integration for InfluxDB 2.0

[![javadoc](https://img.shields.io/badge/javadoc-link-brightgreen.svg)](https://influxdata.github.io/influxdb-client-java/influxdb-client-java/apidocs/index.html)

## Features

- [InfluxDB2 auto-configuration](#influxdb2-auto-configuration)
- [Actuator for InfluxDB2 micrometer registry](#actuator-for-influxdb2-micrometer-registry)
- [Actuator for InfluxDB2 health](#actuator-for-influxdb2-health)

## InfluxDB2 auto-configuration

To enable `InfluxDBClient` support you need to set a `spring.influx2.url` property, and include `influxdb-client-java` on your classpath. 

`InfluxDBClient` relies on OkHttp. If you need to tune the http client, you can register an `InfluxDB2OkHttpClientBuilderProvider` bean.

The default configuration can be override via properties:

```yaml
spring.influx2:
    url: http://localhost:8086/api/v2 # URL to connect to InfluxDB.
    username: my-user # Username to use in the basic auth.
    password: my-password # Password to use in the basic auth.
    token: my-token # Token to use for the authorization.
    org: my-org # Default destination organization for writes and queries.
    bucket: my-bucket # Default destination bucket for writes.
    logLevel: BODY # The log level for logging the HTTP request and HTTP response. (Default: NONE)
    readTimeout: 5s # Read timeout for OkHttpClient. (Default: 10s)
    writeTimeout: 5s # Write timeout for OkHttpClient. (Default: 10s)
    connectTimeout: 5s # Connection timeout for OkHttpClient. (Default: 10s)
```

## Actuator for InfluxDB2 micrometer registry

The enable export metrics to **InfluxDB 2.0** you need to include `micrometer-registry-influx2` on your classpath.

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
Maven dependency:

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
 
## Actuator for InfluxDB2 health

The `/health` endpoint can monitor an **InfluxDB 2.0** server.

InfluxDB 2.0 health check relies on `InfluxDBClient` and can be configured via:

```yaml
management.health.influxdb2.enabled=true # Whether to enable InfluxDB 2.0 health check.
```

## Version

The latest version for Maven dependency:
```xml
<dependency>
  <groupId>com.influxdb</groupId>
  <artifactId>influxdb-spring</artifactId>
  <version>1.1.0</version>
</dependency>
```
  
Or when using with Gradle:
```groovy
dependencies {
    compile "com.influxdb:influxdb-spring:1.1.0"
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
