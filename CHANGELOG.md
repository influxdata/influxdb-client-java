## 1.12.0 [unreleased]

### Features
1. [#150](https://github.com/influxdata/influxdb-client-java/pull/150): flux-dsl: added support for an offset parameter to limit operator, aggregates accept only a 'column' parameter

### API
1. [#139](https://github.com/influxdata/influxdb-client-java/pull/148): Changed default port from 9999 to 8086
1. [#153](https://github.com/influxdata/influxdb-client-java/pull/153): Removed labels in Organization API, removed Pkg* domains, added "after" to FindOption  

### Bug Fixes
1. [#151](https://github.com/influxdata/influxdb-client-java/pull/151): Fixed closing OkHttp3 response body

## 1.11.0 [2020-08-14]

### Features
1. [#139](https://github.com/influxdata/influxdb-client-java/pull/139): Marked Apis as @ThreadSafe
1. [#140](https://github.com/influxdata/influxdb-client-java/pull/140): Validate OffsetDateTime to satisfy RFC 3339
1. [#141](https://github.com/influxdata/influxdb-client-java/issues/141): Move swagger api generator to separate module influxdb-clients-apigen 

### Bug Fixes
1. [#136](https://github.com/influxdata/influxdb-client-java/pull/136): Data Point: measurement name is requiring in constructor
1. [#132](https://github.com/influxdata/influxdb-client-java/pull/132): Fixed thread safe issue in MeasurementMapper 

## 1.10.0 [2020-07-17]

### Bug Fixes
1. [#129](https://github.com/influxdata/influxdb-client-java/pull/129): Fixed serialization of `\n`, `\r` and `\t` to Line Protocol, `=` is valid sign for measurement name 

### Dependencies

1. [#124](https://github.com/influxdata/influxdb-client-java/pull/124): Update dependencies: akka: 2.6.6, commons-io: 2.7, spring: 5.2.7.RELEASE, retrofit: 2.9.0, okhttp3: 4.7.2
1. [#124](https://github.com/influxdata/influxdb-client-java/pull/124): Update plugins: maven-project-info-reports-plugin: 3.1.0, dokka-maven-plugin: 0.10.1, scoverage-maven-plugin: 1.4.1

## 1.9.0 [2020-06-19]

### Features
1. [#119](https://github.com/influxdata/influxdb-client-java/pull/119): Scala and Kotlin clients has their own user agent string

### API
1. [#117](https://github.com/influxdata/influxdb-client-java/pull/117): Update swagger to latest version
1. [#122](https://github.com/influxdata/influxdb-client-java/pull/122): Removed log system from Bucket, Dashboard, Organization, Task and Users API - [influxdb#18459](https://github.com/influxdata/influxdb/pull/18459)

### CI
1. [#123](https://github.com/influxdata/influxdb-client-java/pull/123): Upgraded InfluxDB 1.7 to 1.8 

### Bug Fixes
1. [#116](https://github.com/influxdata/influxdb-client-java/pull/116): The closing message of the `WriteApi` has `Fine` log level

### Dependencies

1. [#112](https://github.com/influxdata/influxdb-client-java/pull/112): Update dependencies: akka: 2.6.5, assertj-core: 3.16.1, 
assertk-jvm: 0.22, commons-csv:1.8, commons-lang3: 3.10, gson: 2.8.6, json: 20190722, junit-jupiter: 5.6.2, 
junit-platform-runner:1.6.2, okhttp3: 4.6.0, okio: 2.60, retrofit: 2.8.1, rxjava: 2.2.19, scala: 2.13.2, 
scalatest: 3.1.2, spring: 5.2.6.RELEASE, spring-boot: 2.2.7.RELEASE
1. [#112](https://github.com/influxdata/influxdb-client-java/pull/112): Update plugins: build-helper-maven-plugin: 3.1.0,
jacoco-maven-plugin: 0.8.5, maven-checkstyle: 3.1.1, maven-javadoc: 3.2.0, maven-site: 3.9.0, maven-surefire: 2.22.2

## 1.8.0 [2020-05-15]

### Features

1. [#110](https://github.com/influxdata/influxdb-client-java/pull/110): Added support "inf" in Duration
1. [#111](https://github.com/influxdata/influxdb-client-java/pull/111): Add aggregateWindow operator to FluxDSL

### Bug Fixes

1. [#108](https://github.com/influxdata/influxdb-client-java/pull/108): Fixed naming for Window function arguments - FluxDSL

## 1.7.0 [2020-04-17]

### Features
1. [#93](https://github.com/influxdata/influxdb-client-java/issues/93): Add addTags and addFields helper functions to Point
1. [#97](https://github.com/influxdata/influxdb-client-java/pull/97): Add the ability to specify the org and the bucket when creating the client

### Documentation
1. [#103](https://github.com/influxdata/influxdb-client-java/pull/103): Clarify how to use a client with InfluxDB 1.8

### Bug Fixes
1. [#98](https://github.com/influxdata/influxdb-client-java/issues/98): @Column supports super class inheritance for write measurements

## 1.6.0 [2020-03-13]

### Features
1. [#85](https://github.com/influxdata/influxdb-client-java/issues/85): Time field in Point supports BigInteger and BigDecimal
1. [#83](https://github.com/influxdata/influxdb-client-java/issues/83): Add reduce operator to FluxDSL
1. [#91](https://github.com/influxdata/influxdb-client-java/pull/91): Set User-Agent to influxdb-client-java/VERSION for all requests

### Bug Fixes
1. [#90](https://github.com/influxdata/influxdb-client-java/pull/90): Correctly parse CSV where multiple results include multiple tables
1. [#89](https://github.com/influxdata/influxdb-client-java/issues/89): @Column supports super class inheritance


## 1.5.0 [2020-02-14]

### Features
1. [#33](https://github.com/influxdata/influxdb-client-java/issues/33): InfluxDBClient.close also dispose a created writeApi
1. [#80](https://github.com/influxdata/influxdb-client-java/issues/80): FluxRecord, FluxColumn, FluxTable are serializable

### Bug Fixes
1. [#82](https://github.com/influxdata/influxdb-client-java/pull/82): Apply backpressure strategy when a buffer overflow

## 1.4.0 [2020-01-17]

### Features
1. [#76](https://github.com/influxdata/influxdb-client-java/pull/76): Added exists operator to Flux restrictions

### API
1. [#77](https://github.com/influxdata/influxdb-client-java/pull/77): Updated swagger to latest version

## 1.3.0 [2019-12-06]

### API
1. [#68](https://github.com/influxdata/influxdb-client-java/pull/68): Updated swagger to latest version

### Bug Fixes
1. [#69](https://github.com/influxdata/influxdb-client-java/issues/69): Fixed android compatibility

## 1.2.0 [2019-11-08]

### Features
1. [#66](https://github.com/influxdata/influxdb-client-java/pull/66): Added DeleteApi

### API
1. [#65](https://github.com/influxdata/influxdb-client-java/pull/65): Updated swagger to latest version

## 1.1.0 [2019-10-11]

### Features
1. [#59](https://github.com/influxdata/influxdb-client-java/issues/59): Added support for Monitoring & Alerting

### Improvements
1. [#60](https://github.com/influxdata/influxdb-client-java/pull/60): Writes performance optimized
1. [#61](https://github.com/influxdata/influxdb-client-java/pull/61): Use Try-With-Resources without catching clause

### API
1. [#58](https://github.com/influxdata/influxdb-client-java/pull/58): Updated swagger to latest version

### Bug Fixes
1. [#57](https://github.com/influxdata/influxdb-client-java/pull/57): LabelsApi: orgID parameter has to be pass as second argument

## 1.0.0 [2019-08-30]

### Features
1. [#50](https://github.com/influxdata/influxdb-client-java/issues/50): Added support for gzip compression of query response

### Bug Fixes
1. [#48](https://github.com/influxdata/influxdb-client-java/issues/48): The org parameter takes either the ID or Name interchangeably
1. [#53](https://github.com/influxdata/influxdb-client-java/issues/53): Drop NaN and infinity values from fields when writing to InfluxDB

### API
1. [#46](https://github.com/influxdata/influxdb-client-java/issues/46): Updated swagger to latest version

## 1.0.0.M2 [2019-08-01]

### Breaking Changes
1. [#40](https://github.com/influxdata/influxdb-client-java/issues/40): The client is hosted in Maven Central repository
    - Repackaged from `org.influxdata` to `com.influxdb`
    - Changed _groupId_ from `org.influxdata` to `com.influxdb`
    - Snapshots are located in the _OSS Snapshot repository_: `https://oss.sonatype.org/content/repositories/snapshots/`

### Features
1. [#34](https://github.com/influxdata/influxdb-client-java/issues/34): Auto-configure client from configuration file
1. [#35](https://github.com/influxdata/influxdb-client-java/issues/35): Possibility to specify default tags
1. [#41](https://github.com/influxdata/influxdb-client-java/issues/41): Synchronous blocking API to Write time-series data into InfluxDB 2.0

### Bug Fixes
1. [#43](https://github.com/influxdata/influxdb-client-java/issues/43): The data point without field should be ignored

### CI
1. [#37](https://github.com/influxdata/influxdb-client-java/issues/37): Switch CI from oraclejdk to openjdk 

## 1.0.0.M1

### Features
1. [client-java](https://github.com/influxdata/influxdb-client-java/tree/master/client#influxdb-client-java): The reference Java client that allows query, write and InfluxDB 2.0 management
1. [client-reactive](https://github.com/influxdata/influxdb-client-java/tree/master/client-reactive#influxdb-client-reactive): The reference RxJava client for the InfluxDB 2.0 that allows query and write in a reactive way
1. [client-kotlin](https://github.com/influxdata/influxdb-client-java/tree/master/client-kotlin#influxdb-client-kotlin): The reference Kotlin client that allows query and write for the InfluxDB 2.0 by Kotlin Channel coroutines
1. [client-scala](https://github.com/influxdata/influxdb-client-java/tree/master/client-scala#influxdb-client-scala): The reference Scala client that allows query and write for the InfluxDB 2.0 by Akka Streams
1. [client-legacy](https://github.com/influxdata/influxdb-client-java/tree/master/client-legacy#influxdb-client-flux):  The reference Java client that allows you to perform Flux queries against InfluxDB 1.7+
1. [flux-dsl](https://github.com/influxdata/influxdb-client-java/tree/master/flux-dsl#flux-dsl): A Java query builder for the Flux language
