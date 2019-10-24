## 1.2.0 [unreleased]

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

### Bugs
1. [#57](https://github.com/influxdata/influxdb-client-java/pull/57): LabelsApi: orgID parameter has to be pass as second argument

## 1.0.0 [2019-08-30]

### Features
1. [#50](https://github.com/influxdata/influxdb-client-java/issues/50): Added support for gzip compression of query response

### Bugs
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

### Bugs
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
