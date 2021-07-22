## 3.0.1 [2021-07-16]

### Features
1. [#242](https://github.com/influxdata/influxdb-client-java/pull/242): Add Spring Boot configuration metadata that helps the IDE understand the `application.properties` [spring]

### Bug Fixes
1. [#248](https://github.com/influxdata/influxdb-client-java/pull/248): Remove not supported autoconfiguration [spring]

## 3.0.0 [2021-07-09]

### Breaking Changes

#### `influxdb-spring`:

The `micrometer` v1.7.0 brings [support](https://github.com/micrometer-metrics/micrometer/issues/1974) for InfluxDB 2. 
That is a reason why the [influxdb-spring](./spring) no longer needs provide a custom Micrometer metrics exporter.
Now you are able to use `micrometer-registry-influx`, for more info [see our docs](./spring/README.md#actuator-for-influxdb2-micrometer-registry). 
 
#### Management API
This release introduces a support for new InfluxDB OSS API definitions - [oss.yml](https://github.com/influxdata/openapi/blob/master/contracts/oss.yml). The following breaking changes are in underlying API services and doesn't affect common apis such as - `WriteApi`, `QueryApi`, `BucketsApi`, `OrganizationsApi`...

- `UsersService` uses `PostUser` to create `User`
- `AuthorizationsService` uses `AuthorizationPostRequest` to create `Authorization`
- `BucketsService` uses `PatchBucketRequest` to update `Bucket`
- `OrganizationsService` uses `PostOrganizationRequest` to create `Organization`
- `OrganizationsService` uses `PatchOrganizationRequest` to update `Organization`
- `DashboardsService` uses `PatchDashboardRequest` to update `Dashboard`
- `DeleteService` is used to delete time series data instead of `DefaultService`
- `Run` contains list of `LogEvent` in `Log` property
- `DBRPs` contains list of `DBRP` in `Content` property
- `DbrPsService` uses `DBRPCreate` to create `DBRP`
- Inheritance structure:
   - `Check` <- `CheckDiscriminator` <- `CheckBase`
   - `NotificationEndpoint` <- `NotificationEndpointDiscriminator` <- `NotificationEndpointBase`
   - `NotificationRule` <- `NotificationRuleDiscriminator` <- `NNotificationRuleBase`
- Flux AST literals extends the AST `Expression` object

#### FluxDSL
The `shift()` function renamed to `timeShift()`.

### Features
1. [#231](https://github.com/influxdata/influxdb-client-java/pull/231): Add support for Spring Boot 2.4 [spring]
2. [#229](https://github.com/influxdata/influxdb-client-java/pull/229): Support translating column name from some_col to someCol [query]

### Bug Fixes
1. [#227](https://github.com/influxdata/influxdb-client-java/pull/227): Connection URL with custom base path
1. [#236](https://github.com/influxdata/influxdb-client-java/pull/236): Rename `shift()` to `timeShift()` [FluxDSL]
1. [#241](https://github.com/influxdata/influxdb-client-java/pull/241): Set default HTTP protocol to HTTP 1.1

### Dependencies
1. [#227](https://github.com/influxdata/influxdb-client-java/pull/227): Update dependencies:
   - Kotlin to 1.5.10

### API
1. [#233](https://github.com/influxdata/influxdb-client-java/pull/233): Use InfluxDB OSS API definitions to generated APIs
 
## 2.3.0 [2021-06-04]

### Features
1. [#223](https://github.com/influxdata/influxdb-client-java/pull/223): Exponential random backoff retry strategy

## 2.2.0 [2021-04-30]

### Breaking Changes

This release introduces a support to cross-built Scala Client against Scala `2.12` and `2.13`.
You have to replace your dependency from: `influxdb-client-scala` to:
- `influxdb-client-scala_2.12` or
- `influxdb-client-scala_2.13`

### Features
1. [#211](https://github.com/influxdata/influxdb-client-java/pull/211): Add supports for Scala cross versioning [`2.12`, `2.13`]
1. [#213](https://github.com/influxdata/influxdb-client-java/pull/213): Supports empty logic operator [FluxDSL]
1. [#216](https://github.com/influxdata/influxdb-client-java/pull/216): Allow to specify a name of `column` in `last` function [FluxDSL]
1. [#218](https://github.com/influxdata/influxdb-client-java/pull/218): Supports enum types in mapping into POJO
1. [#220](https://github.com/influxdata/influxdb-client-java/pull/220): Create client supporting OSGi environments
1. [#221](https://github.com/influxdata/influxdb-client-java/pull/221): Add feature definition and documentation for Apache Karaf support
1. [#222](https://github.com/influxdata/influxdb-client-java/pull/221): Add `Kotlin` WriteApi

### Dependencies
1. [#222](https://github.com/influxdata/influxdb-client-csharp/pull/222): Update dependencies:
   - Kotlin to 1.4.32
1. [#222](https://github.com/influxdata/influxdb-client-csharp/pull/222): Update plugins:
   - dokka-maven-plugin to 1.4.30
   
## 2.1.0 [2021-04-01]

### Bug Fixes
1. [#205](https://github.com/influxdata/influxdb-client-java/pull/205): Fix GZIP issue for query executed from all clients [see issue comments](https://github.com/influxdata/influxdb-client-java/issues/50#issuecomment-796896401)

### API
1. [#206](https://github.com/influxdata/influxdb-client-java/pull/206): Updated swagger to the latest version

## 2.0.0 [2021-03-05]

### API
1. [#197](https://github.com/influxdata/influxdb-client-java/pull/197): InfluxException bodyError type changed from JSONObject to Map<String, Object>

### Bug Fixes
1. [#196](https://github.com/influxdata/influxdb-client-java/issues/196): Removed badly licenced JSON-Java library
1. [#199](https://github.com/influxdata/influxdb-client-java/pull/199): Correct implementation of Backpressure for Scala Querying

### CI
1. [#203](https://github.com/influxdata/influxdb-client-java/pull/203): Updated stable image to `influxdb:latest` and nightly to `quay.io/influxdb/influxdb:nightly`

## 1.15.0 [2021-01-29]

### Features
1. [#191](https://github.com/influxdata/influxdb-client-java/pull/191): Added tail operator to FluxDSL

### CI
1. [#192](https://github.com/influxdata/influxdb-client-java/pull/192): Updated default docker image to v2.0.3

## 1.14.0 [2020-12-04]

### Features
1. [#172](https://github.com/influxdata/influxdb-client-java/pull/172): flux-dsl: added `to` function without `org` parameter
1. [#183](https://github.com/influxdata/influxdb-client-java/pull/183): CSV parser is able to parse export from UI

### Bug Fixes
1. [#173](https://github.com/influxdata/influxdb-client-java/pull/173): Query error could be after _success_ table
1. [#176](https://github.com/influxdata/influxdb-client-java/pull/176): Blocking API batches Point by precision
1. [#180](https://github.com/influxdata/influxdb-client-java/pull/180): Fixed concatenation of url

### CI
1. [#184](https://github.com/influxdata/influxdb-client-java/pull/184): Updated default docker image to v2.0.2

## 1.13.0 [2020-10-30]

### Features
1. [#163](https://github.com/influxdata/influxdb-client-java/pull/163): Improved logging message for retries

### Bug Fixes
1. [#161](https://github.com/influxdata/influxdb-client-java/pull/161): Offset param could be 0 - FluxDSL
1. [#164](https://github.com/influxdata/influxdb-client-java/pull/164): Query response parser uses UTF-8 encoding
1. [#169](https://github.com/influxdata/influxdb-client-java/pull/169): Downgrade gson to 2.8.5 to support Java 8

## 1.12.0 [2020-10-02]

### Features
1. [#150](https://github.com/influxdata/influxdb-client-java/pull/150): flux-dsl: added support for an offset parameter to limit operator, aggregates accept only a 'column' parameter
1. [#156](https://github.com/influxdata/influxdb-client-java/pull/156): Added exponential backoff strategy for batching writes. Default value for `retryInterval` is 5_000 milliseconds.

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
