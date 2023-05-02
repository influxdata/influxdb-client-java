# Spring Integration for InfluxDB 2.x

[![javadoc](https://img.shields.io/badge/javadoc-link-brightgreen.svg)](https://influxdata.github.io/influxdb-client-java/influxdb-client-java/apidocs/index.html)

## Features

- [InfluxDB2 auto-configuration](#influxdb2-auto-configuration)
- [Actuator for InfluxDB2 micrometer registry](#actuator-for-influxdb2-micrometer-registry)
- [Actuator for InfluxDB2 health](#actuator-for-influxdb2-health)

## Spring Boot Compatibility

:warning: The client version `6.4.0` upgrades the `OkHttp` library to version `4.10.0`. The version `3.12.x` is no longer supported - [okhttp#requirements](https://github.com/square/okhttp#requirements).

The `spring-boot` supports the `OkHttp:4.10.0` from the version `3.0.0-M4` - [spring-boot/OkHttp 4.10,0](https://github.com/spring-projects/spring-boot/commit/6cb1a958a5d43a2fffb7e7635e3be9c0ee15f3b1).
For the older version of `spring-boot` you have to configure Spring Boot's `okhttp3.version` property:

```xml
<properties>
    <okhttp3.version>4.10.0</okhttp3.version>
</properties>
```

## InfluxDB2 auto-configuration

To enable `InfluxDBClient` support you need to set a `influx.url` property, and include `influxdb-client-java` on your classpath. 

`InfluxDBClient` relies on OkHttp. If you need to tune the http client, you can register an `InfluxDB2OkHttpClientBuilderProvider` bean.

The default configuration can be override via properties:

```yaml
influx:
    url: http://localhost:8086/ # URL to connect to InfluxDB.
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

If you want to configure the `InfluxDBClientReactive` client, you need to include `influxdb-client-reactive` on your classpath instead of `influxdb-client-java`.

## Actuator for InfluxDB2 micrometer registry

To enable export metrics to **InfluxDB 2.x** you need to include `micrometer-registry-influx` on your classpath.

The default configuration can be override via properties:

```yaml
management.metrics.export.influx:
    bucket: my-bucket # Specifies the destination bucket for writes
    org: my-org # Specifies the destination organization for writes.
    token: my-token # Authenticate requests with this token.
    uri: http://localhost:8086/api/v2 # The URI for the Influx backend. (Default: http://localhost:8086/api/v2)
    compressed: true # Whether to enable GZIP compression of metrics batches published to Influx. (Default: true)
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
    <artifactId>micrometer-registry-influx</artifactId>
    <version>1.7.0</version>
</dependency>
```

or when using with Gradle:
```groovy
dependencies {
    implementation "io.micrometer:micrometer-registry-influx:1.7.0"
}
```
 
## Actuator for InfluxDB2 health

The `/health` endpoint can monitor an **InfluxDB 2.x** server.

InfluxDB 2.x health check relies on `InfluxDBClient` and can be configured via:

```yaml
management.health.influx.enabled=true # Whether to enable InfluxDB 2.x health check.
```

## Version

The latest version for Maven dependency:
```xml
<dependency>
  <groupId>com.influxdb</groupId>
  <artifactId>influxdb-spring</artifactId>
  <version>6.8.0</version>
</dependency>
```
  
Or when using with Gradle:
```groovy
dependencies {
    implementation "com.influxdb:influxdb-spring:6.8.0"
}
```
