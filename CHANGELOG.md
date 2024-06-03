## 7.2.0 [unreleased]

### Dependencies

Update dependencies:

#### Maven:
 - [#729](https://github.com/influxdata/influxdb-client-java/pull/729): `maven-enforcer-plugin` to `3.5.0`

## 7.1.0 [2024-05-17]

### Bug Fixes

1. [#684](https://github.com/influxdata/influxdb-client-java/issues/684): Fix checking for CSV end of table marker when parsing CSV stream to InfluxQLQueryResult, needed for example when parsing the results of a query like "SHOW SERIES".
2. [#662](https://github.com/influxdata/influxdb-client-java/issues/662): Adds to FluxDsl support for the `|> elapsed(unit)` function.
3. [#623](https://github.com/influxdata/influxdb-client-java/issues/623): Enables the use of IPv6 addresses. 
4. [#604](https://github.com/influxdata/influxdb-client-java/issues/604): Custom FluxDSL restrictions for regular expressions

### Dependencies

Update dependencies:

#### Build:
 - [#716](https://github.com/influxdata/influxdb-client-java/pull/716): `karaf` to `4.4.6`
 - [#710](https://github.com/influxdata/influxdb-client-java/pull/710): `spring-boot` to `3.2.5`
 - [#686](https://github.com/influxdata/influxdb-client-java/pull/686): `scala-library` to `2.12.19`
 - [#690](https://github.com/influxdata/influxdb-client-java/pull/690): `kotlinx-coroutines` to `1.8.0`
 - [#707](https://github.com/influxdata/influxdb-client-java/pull/707): `micrometer-registry-influx` to `1.12.5`
 - [#696](https://github.com/influxdata/influxdb-client-java/pull/696): `okio` to `3.9.0`
 - [#694](https://github.com/influxdata/influxdb-client-java/pull/694): `retrofit` to `2.11.0`
 - [#699](https://github.com/influxdata/influxdb-client-java/pull/699): `kotlin` to `1.9.23`
 - [#701](https://github.com/influxdata/influxdb-client-java/pull/701): `lombok` to `1.18.32`
 - [#702](https://github.com/influxdata/influxdb-client-java/pull/702): `commons-io` to `2.16.0`

#### Maven:
 - [#676](https://github.com/influxdata/influxdb-client-java/pull/676): `maven-compiler-plugin` to `3.12.1`
 - [#677](https://github.com/influxdata/influxdb-client-java/pull/677): `maven-surefire-plugin`, `maven-failsafe-plugin` to `3.2.5`
 - [#679](https://github.com/influxdata/influxdb-client-java/pull/679): `build-helper-maven-plugin` to `3.5.0`
 - [#682](https://github.com/influxdata/influxdb-client-java/pull/682): `maven-checkstyle-plugin` to `3.3.1`
 - [#712](https://github.com/influxdata/influxdb-client-java/pull/712): `maven-gpg-plugin` to `3.2.4`
 - [#703](https://github.com/influxdata/influxdb-client-java/pull/703): `dokka-maven-plugin` to `1.9.20`
 - [#713](https://github.com/influxdata/influxdb-client-java/pull/713): `maven-jar-plugin` to `3.4.1`
 - [#709](https://github.com/influxdata/influxdb-client-java/pull/709): `scala-maven-plugin` to `4.9.0`
 - [#708](https://github.com/influxdata/influxdb-client-java/pull/708): `maven-deploy-plugin` to `3.1.2`

#### Test:
 - [#683](https://github.com/influxdata/influxdb-client-java/pull/683): `assertj` to `3.25.3`
 - [#687](https://github.com/influxdata/influxdb-client-java/pull/687): `junit-jupiter` to `5.10.2`
 - [#714](https://github.com/influxdata/influxdb-client-java/pull/714): `logback-classic` to `1.5.6`

#### Provided:
 - [#711](https://github.com/influxdata/influxdb-client-java/pull/711): `slf4j-api` to `2.0.13`

#### Examples:
 - [#715](https://github.com/influxdata/influxdb-client-java/pull/715): `commons-cli ` to `1.7.0`

## 7.0.0 [2024-01-30]

:warning: This client version discontinues support for JDK 8 and 11. The minimum supported JDK version is now JDK 17.

:warning: This client version discontinues support for Akka Streams and introduces support for Pekko Streams instead. Apache Pekko is a fork of [Akka](https://github.com/akka/akka) 2.6.x, created after the Akka project adopted the Business Source License, which is not compatible with open-source usage.

### Features
1. [#661](https://github.com/influxdata/influxdb-client-java/pull/661): Replaced Akka Streams with Pekko Streams in the Scala client.
1. [#673](https://github.com/influxdata/influxdb-client-java/pull/673): Upgrade SpringBoot to v3 and Spring to v6
1. [#673](https://github.com/influxdata/influxdb-client-java/pull/673): Disable support for old JDKs (< 17)

### Dependencies

Update dependencies:

#### Build:
 - [#664](https://github.com/influxdata/influxdb-client-java/pull/664): `kotlin` to `1.9.22`
 - [#666](https://github.com/influxdata/influxdb-client-java/pull/666): `okio` to `3.7.0`
 - [#667](https://github.com/influxdata/influxdb-client-java/pull/667): `rxjava` to `3.1.8`
 - [#669](https://github.com/influxdata/influxdb-client-java/pull/669): `commons-lang3` to `3.14.0`
 - [#670](https://github.com/influxdata/influxdb-client-java/pull/670): `micrometer-registry-influx` to `1.12.1`
 - [#673](https://github.com/influxdata/influxdb-client-java/pull/673): `spring-boot` to `3.2.2`
 - [#673](https://github.com/influxdata/influxdb-client-java/pull/673): `spring` to `6.1.3`
 - [#673](https://github.com/influxdata/influxdb-client-java/pull/673): `scala-library` to `2.13.11`
 - [#673](https://github.com/influxdata/influxdb-client-java/pull/673): `okhttp` to `4.12.0`

#### Maven:
 - [#671](https://github.com/influxdata/influxdb-client-java/pull/671): `maven-javadoc-plugin` to `3.6.3`

#### Test:
 - [#668](https://github.com/influxdata/influxdb-client-java/pull/668): `junit-jupiter` to `5.10.1`

## 6.12.0 [2023-12-15]

### Features
1. [#643](https://github.com/influxdata/influxdb-client-java/pull/643): `ConnectionClosingInterceptor` interceptor closes connections that exceed 
a specified maximum lifetime age (TTL). It's beneficial for scenarios where your application requires establishing new connections to the same host after 
a predetermined interval. 

The connection to the InfluxDB Enterprise with the `ConnectionClosingInterceptor` can be configured as follows:
```java
package example;

import java.time.Duration;
import java.util.Collections;

import okhttp3.OkHttpClient;
import okhttp3.Protocol;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.InfluxDBClientOptions;
import com.influxdb.client.domain.WriteConsistency;
import com.influxdb.rest.ConnectionClosingInterceptor;

public class InfluxQLExample {

    public static void main(final String[] args) throws InterruptedException {

        //
        // Credentials to connect to InfluxDB Enterprise
        //
        String url = "https://localhost:8086";
        String username = "admin";
        String password = "password";
        String database = "database";
        WriteConsistency consistency = WriteConsistency.ALL;

        //
        // Configure underlying HTTP client
        //
        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder()
                .protocols(Collections.singletonList(Protocol.HTTP_1_1));

        //
        // Use new Connection TTL feature
        //
        Duration connectionMaxAge = Duration.ofMinutes(1);
        ConnectionClosingInterceptor interceptor = new ConnectionClosingInterceptor(connectionMaxAge);
        okHttpClientBuilder
                .addNetworkInterceptor(interceptor)
                .eventListenerFactory(call -> interceptor);

        //
        // Configure InfluxDB client
        //
        InfluxDBClientOptions.Builder optionsBuilder = InfluxDBClientOptions.builder()
                .url(url)
                .org("-")
                .authenticateToken(String.format("%s:%s", username, password).toCharArray())
                .bucket(String.format("%s/%s", database, ""))
                .consistency(consistency)
                .okHttpClient(okHttpClientBuilder);

        //
        // Create client and write data
        //
        try (InfluxDBClient client = InfluxDBClientFactory.create(optionsBuilder.build())) {

            // ...
        }
    }
}
```

## 6.11.0 [2023-12-05]

### Features
1. [#647](https://github.com/influxdata/influxdb-client-java/pull/647): `findTasksStream` function with pagination

### Bug Fixes
1. [#648](https://github.com/influxdata/influxdb-client-java/pull/648): With csv parsing, return empty string when `stringValue` and `defaultValue` are both an empty string

### Dependencies

Update dependencies:

#### Build:
 - [#614](https://github.com/influxdata/influxdb-client-java/pull/614): `commons-lang3` to `3.13.0`
 - [#653](https://github.com/influxdata/influxdb-client-java/pull/653): `commons-io` to `2.15.1`
 - [#622](https://github.com/influxdata/influxdb-client-java/pull/622): `micrometer-registry-influx` to `1.11.3`
 - [#635](https://github.com/influxdata/influxdb-client-java/pull/635): `spring-boot` to `2.7.17`
 - [#625](https://github.com/influxdata/influxdb-client-java/pull/625): `lombok` to `1.18.30`
 - [#629](https://github.com/influxdata/influxdb-client-java/pull/629): `karaf` to `4.4.4`
 - [#634](https://github.com/influxdata/influxdb-client-java/pull/634): `kotlin` to `1.9.20`
 - [#542](https://github.com/influxdata/influxdb-client-java/pull/542): `okhttp` to `4.11.0`
 - [#630](https://github.com/influxdata/influxdb-client-java/pull/630): `okio` to `3.6.0`

#### Maven:
 - [#656](https://github.com/influxdata/influxdb-client-java/pull/656): `maven-enforcer-plugin` to `3.4.1`
 - [#636](https://github.com/influxdata/influxdb-client-java/pull/636): `dokka-maven-plugin` to `1.9.10`
 - [#658](https://github.com/influxdata/influxdb-client-java/pull/658): `versions-maven-plugin` to `2.16.2`
 - [#627](https://github.com/influxdata/influxdb-client-java/pull/627): `assertk-jvm` to `0.27.0`
 - [#637](https://github.com/influxdata/influxdb-client-java/pull/637): `maven-javadoc-plugin` to `3.6.0`
 - [#639](https://github.com/influxdata/influxdb-client-java/pull/639): `license-maven-plugin` to `4.3`
 - [#651](https://github.com/influxdata/influxdb-client-java/pull/651): `maven-surefire-plugin`, `maven-failsafe-plugin` to `3.2.2`
 - [#654](https://github.com/influxdata/influxdb-client-java/pull/654): `jacoco-maven-plugin` to `0.8.11`
 - [#633](https://github.com/influxdata/influxdb-client-java/pull/633): `maven-surefire-plugin`, `maven-failsafe-plugin` to `3.2.1`
 - [#655](https://github.com/influxdata/influxdb-client-java/pull/655): `maven-project-info-reports-plugin` to `3.5.0`

#### Examples:
 - [#638](https://github.com/influxdata/influxdb-client-java/pull/638): `commons-cli ` to `1.6.0`

#### Test:
 - [#650](https://github.com/influxdata/influxdb-client-java/pull/650): `logback-classic` to `1.3.14`

#### Provided:
 - [#657](https://github.com/influxdata/influxdb-client-java/pull/657): `slf4j-api` to `2.0.9`

## 6.10.0 [2023-07-28]

### Bug Fixes
1. [#584](https://github.com/influxdata/influxdb-client-java/pull/584): InfluxQL tags support

### CI
1. [#593](https://github.com/influxdata/influxdb-client-java/pull/593): Add JDK 20 to CI pipeline

### Dependencies

Update dependencies:

#### Build:
 - [#567](https://github.com/influxdata/influxdb-client-java/pull/567): `lombok` to `1.18.28`
 - [#582](https://github.com/influxdata/influxdb-client-java/pull/582): `scala-collection-compat_2.12` to `2.11.0`
 - [#601](https://github.com/influxdata/influxdb-client-java/pull/601): `micrometer-registry-influx` to `1.11.2`
 - [#608](https://github.com/influxdata/influxdb-client-java/pull/608): `spring-boot` to `2.7.14`
 - [#588](https://github.com/influxdata/influxdb-client-java/pull/588): `scala-library` to `2.12.18`
 - [#589](https://github.com/influxdata/influxdb-client-java/pull/589): `kotlin` to `1.8.22`
 - [#592](https://github.com/influxdata/influxdb-client-java/pull/592): `akka` to `2.6.21`
 - [#602](https://github.com/influxdata/influxdb-client-java/pull/602): `okio` to `3.4.0`
 - [#613](https://github.com/influxdata/influxdb-client-java/pull/613): `kotlinx-coroutines` to `1.7.3`

#### Maven:
 - [#569](https://github.com/influxdata/influxdb-client-java/pull/569): `maven-enforcer-plugin` to `3.3.0`
 - [#570](https://github.com/influxdata/influxdb-client-java/pull/570): `build-helper-maven-plugin` to `3.4.0`
 - [#573](https://github.com/influxdata/influxdb-client-java/pull/573): `dokka-maven-plugin` to `1.8.20`
 - [#583](https://github.com/influxdata/influxdb-client-java/pull/583): `maven-project-info-reports-plugin` to `3.4.5`
 - [#586](https://github.com/influxdata/influxdb-client-java/pull/586): `maven-surefire-plugin`, `maven-failsafe-plugin` to `3.1.2`
 - [#590](https://github.com/influxdata/influxdb-client-java/pull/590): `maven-bundle-plugin` to `5.1.9`
 - [#591](https://github.com/influxdata/influxdb-client-java/pull/591): `maven-source-plugin` to `3.3.0`

#### Examples:
 - [#571](https://github.com/influxdata/influxdb-client-java/pull/571): `commons-io` to `2.12.0`

#### Test:
 - [#596](https://github.com/influxdata/influxdb-client-java/pull/596): `logback-classic` to `1.3.8`

## 6.9.0 [2023-05-31]

### Dependencies

Update dependencies:

#### Build:
 - [#507](https://github.com/influxdata/influxdb-client-java/pull/507): `rxjava` to `3.1.5`
 - [#511](https://github.com/influxdata/influxdb-client-java/pull/511): `lombok` to `1.18.26`
 - [#512](https://github.com/influxdata/influxdb-client-java/pull/512): `commons-csv` to `1.10.0`
 - [#536](https://github.com/influxdata/influxdb-client-java/pull/536): `spring-boot` to `2.7.11`
 - [#540](https://github.com/influxdata/influxdb-client-java/pull/540): `kotlin` to `1.8.21`
 - [#545](https://github.com/influxdata/influxdb-client-java/pull/545): `scala-collection-compat_2.12` to `2.10.0`
 - [#548](https://github.com/influxdata/influxdb-client-java/pull/548): `maven-gpg-plugin` to `3.1.0`
 - [#552](https://github.com/influxdata/influxdb-client-java/pull/552): `micrometer-registry-influx` to `1.11.0`

#### Maven:
 - [#527](https://github.com/influxdata/influxdb-client-java/pull/527): `scala-maven-plugin` to `4.8.1`
 - [#528](https://github.com/influxdata/influxdb-client-java/pull/528): `license-maven-plugin` to `4.2`
 - [#529](https://github.com/influxdata/influxdb-client-java/pull/529): `maven-deploy-plugin` to `3.1.1`
 - [#543](https://github.com/influxdata/influxdb-client-java/pull/543): `jacoco-maven-plugin` to `0.8.10`
 - [#544](https://github.com/influxdata/influxdb-client-java/pull/544): `maven-surefire-plugin`, `maven-failsafe-plugin` to `3.1.0`
 - [#549](https://github.com/influxdata/influxdb-client-java/pull/549): `maven-checkstyle-plugin` to `3.2.2`
 - [#550](https://github.com/influxdata/influxdb-client-java/pull/550): `maven-compiler-plugin` to `3.11.0`
 - [#559](https://github.com/influxdata/influxdb-client-java/pull/559): `maven-project-info-reports-plugin` to `3.4.3`

#### Provided:
 - [#561](https://github.com/influxdata/influxdb-client-java/pull/561): `slf4j-api` to `2.0.7`

#### Test:
 - [#556](https://github.com/influxdata/influxdb-client-java/pull/556): `logback-classic` to `1.3.7`
 - [#564](https://github.com/influxdata/influxdb-client-java/pull/564): `assertk-jvm` to `0.26.1`

## 6.8.0 [2023-03-29]

### Bug Fixes
1. [#470](https://github.com/influxdata/influxdb-client-java/pull/470): Move auto-configuration registration to `AutoConfiguration.imports` [spring]
1. [#483](https://github.com/influxdata/influxdb-client-java/pull/483): Fix of potential NPE for `WriteParameters#hashCode`
1. [#521](https://github.com/influxdata/influxdb-client-java/issues/521): Ensure write data is actually gzip'ed when enabled

### CI
1. [#484](https://github.com/influxdata/influxdb-client-java/pull/4884): Add JDK 19 to CI pipeline

### Dependencies

Update dependencies:

#### Build:
 - [#473](https://github.com/influxdata/influxdb-client-java/pull/473): `micrometer-registry-influx` to `1.10.2`
 - [#477](https://github.com/influxdata/influxdb-client-java/pull/477): `kotlin` to `1.7.22`
 - [#476](https://github.com/influxdata/influxdb-client-java/pull/476): `scala-collection-compat_2.12` to `2.9.0`
 - [#492](https://github.com/influxdata/influxdb-client-java/pull/492): `versions-maven-plugin` to `2.14.2`

#### Maven Plugin:
 - [#479](https://github.com/influxdata/influxdb-client-java/pull/479): `scala-maven-plugin` to `4.8.0`

#### Provided:
 - [#487](https://github.com/influxdata/influxdb-client-java/pull/487): `slf4j-api` to `2.0.6`
 - [#490](https://github.com/influxdata/influxdb-client-java/pull/490): `org.osgi.service.component.annotations` to `1.5.1`

#### Test:
 - [#485](https://github.com/influxdata/influxdb-client-java/pull/485): `mockito` to `4.10.0`
 - [#471](https://github.com/influxdata/influxdb-client-java/pull/471): `logback-classic` to `1.3.5`

## 6.7.0 [2022-11-03]

### Features
1. [#439](https://github.com/influxdata/influxdb-client-java/pull/439): Add `FluxRecord.getRow()` which stores response data in a list
1. [#457](https://github.com/influxdata/influxdb-client-java/pull/457): Add possibility to use `AuthorizationPostRequest` and `AuthorizationUpdateRequest` in `AuthorizationApi`

### Bug Fixes
1. [#459](https://github.com/influxdata/influxdb-client-java/pull/459): Fix support for InfluxDB 1.8.x in InfluxQLQueryAPI

### CI
1. [#460](https://github.com/influxdata/influxdb-client-java/pull/460): Check dependency licenses

### Dependencies
1. [#446](https://github.com/influxdata/influxdb-client-java/pull/446): Remove `gson-fire`

Update dependencies:

#### Build:
  - [#434](https://github.com/influxdata/influxdb-client-java/pull/434): `kotlin` to `1.7.20`
  - [#436](https://github.com/influxdata/influxdb-client-java/pull/436): `scala-library` to `2.13.9`
  - [#443](https://github.com/influxdata/influxdb-client-java/pull/443): `micrometer-registry-influx` to `1.9.5`
  - [#451](https://github.com/influxdata/influxdb-client-java/pull/451): `karaf` to `4.4.2`
  - [#449](https://github.com/influxdata/influxdb-client-java/pull/449): `spring-boot` to `2.7.5`
  - [#462](https://github.com/influxdata/influxdb-client-java/pull/462): `gson` to `2.10`

#### Maven Plugin:
  - [#445](https://github.com/influxdata/influxdb-client-java/pull/445): `dokka-maven-plugin` to `1.7.20`
  - [#450](https://github.com/influxdata/influxdb-client-java/pull/450): `versions-maven-plugin` to `2.13.0`

#### Test:
  - [#440](https://github.com/influxdata/influxdb-client-java/pull/440): `logback-classic` to `1.3.4`
  - [#444](https://github.com/influxdata/influxdb-client-java/pull/444): `scalatest-maven-plugin` to `2.2.0`
  - [#452](https://github.com/influxdata/influxdb-client-java/pull/452): `mockito` to `4.8.1`

## 6.6.0 [2022-09-29]

### Features
1. [#419](https://github.com/influxdata/influxdb-client-java/pull/419): Add possibility to get time from `Point` data structure

### Bug Fixes
1. [#414](https://github.com/influxdata/influxdb-client-java/pull/414): Mapping `Number` types to POJO query output

### Documentation
1. [#406](https://github.com/influxdata/influxdb-client-java/pull/406): Fix compatibility of the `doclint` between JDK 8 and JDK 18

### Dependencies
Update dependencies:

#### Build:
  - [#412](https://github.com/influxdata/influxdb-client-java/pull/412), [#416](https://github.com/influxdata/influxdb-client-java/pull/416): `akka` to `2.6.20`
  - [#420](https://github.com/influxdata/influxdb-client-java/pull/420): `micrometer-registry-influx` to `1.9.4`
  - [#423](https://github.com/influxdata/influxdb-client-java/pull/423): `scala-library` to `2.12.17`
  - [#430](https://github.com/influxdata/influxdb-client-java/pull/430): `spring-boot` to `2.7.4`

#### Maven Plugin:
  - [#413](https://github.com/influxdata/influxdb-client-java/pull/413): `versions-maven-plugin` to `2.12.0`
  - [#426](https://github.com/influxdata/influxdb-client-java/pull/426): `maven-jar-plugin` to `3.3.0`
  - [#432](https://github.com/influxdata/influxdb-client-java/pull/432): `scala-maven-plugin` to `4.7.2`

#### Provided:
  - [#431](https://github.com/influxdata/influxdb-client-java/pull/431): `slf4j-api` to `2.0.3`

#### Test:
  - [#422](https://github.com/influxdata/influxdb-client-java/pull/422): `logback-classic` to `1.3.1`
  - [#417](https://github.com/influxdata/influxdb-client-java/pull/417): `mockito` to `4.8.0`
  - [#425](https://github.com/influxdata/influxdb-client-java/pull/425): `spring-test` to `5.3.23`
  - [#427](https://github.com/influxdata/influxdb-client-java/pull/427): `junit-jupiter-engine` to `5.9.1`

Remove dependencies:
#### Test:
  - [#418](https://github.com/influxdata/influxdb-client-java/pull/418): `junit-platform-runner`

## 6.5.0 [2022-08-29]

### Breaking Changes

#### FluxDSL
The `percentile()` function renamed to `quantile()`.

### Features
1. [#366](https://github.com/influxdata/influxdb-client-java/pull/366): Added an endpoint to query with InfluxQL (v1) for more info see [README.md](./client).

### Bug Fixes
1. [#390](https://github.com/influxdata/influxdb-client-java/pull/390): Rename `percentile()` function renamed to `quantile()` [FluxDSL]
1. [#398](https://github.com/influxdata/influxdb-client-java/pull/398): Append `task option` at the end of script

### Dependencies
Update dependencies:

#### Build:
  - [#389](https://github.com/influxdata/influxdb-client-java/pull/389): `scala-collection-compat_2.12` to `2.8.1`
  - [#392](https://github.com/influxdata/influxdb-client-java/pull/392): `gson` to `2.9.1`
  - [#396](https://github.com/influxdata/influxdb-client-java/pull/396): `micrometer-registry-influx` to `1.9.3`
  - [#402](https://github.com/influxdata/influxdb-client-java/pull/402): `spring-boot` to `2.7.3`

#### Maven Plugin:
  - [#391](https://github.com/influxdata/influxdb-client-java/pull/391): `maven-bundle-plugin` to `5.1.8`
  - [#395](https://github.com/influxdata/influxdb-client-java/pull/395): `maven-site-plugin` to `3.12.1`
  - [#399](https://github.com/influxdata/influxdb-client-java/pull/399): `maven-project-info-reports-plugin` to `3.4.1`
  - [#401](https://github.com/influxdata/influxdb-client-java/pull/401): `maven-javadoc-plugin` to `3.4.1`
  - [#404](https://github.com/influxdata/influxdb-client-java/pull/404): `maven-checkstyle-plugin` to `3.2.0`

#### Provided:
  - [#403](https://github.com/influxdata/influxdb-client-java/pull/403): `slf4j-api` to `2.0.0`

#### Test:
  - [#400](https://github.com/influxdata/influxdb-client-java/pull/400): `mockito` to `4.7.0`

## 6.4.0 [2022-07-29]

### Release Notice

#### Spring

:warning: The client upgrades the `OkHttp` library to version `4.10.0`.

The `spring-boot` supports the `OkHttp:4.10.0` from the version `3.0.0-M4` - [spring-boot/OkHttp 4.10,0](https://github.com/spring-projects/spring-boot/commit/6cb1a958a5d43a2fffb7e7635e3be9c0ee15f3b1).
For the older version of `spring-boot` you have to configure Spring Boot's `okhttp3.version` property:

```xml
<properties>
    <okhttp3.version>4.10.0</okhttp3.version>
</properties>
```

### Features
1. [#373](https://github.com/influxdata/influxdb-client-java/pull/373): Improve `FluxDSL`:
   * Add ability to define imports for each flux function [FluxDSL]
   * Add ability use multiple flux expressions [FluxDSL]
   * Add ability to define custom functions [FluxDSL]
   * Improve join flux, so it can be nested [FluxDSL]
   * Add missing parameter variants for RangeFlux amd AggregateWindow [FluxDSL]
   * Add TruncateTimeColumnFlux [FluxDSL]
   * Add ArrayFromFlux [FluxDSL]
   * Add UnionFlux [FluxDSL]
1. [#376](https://github.com/influxdata/influxdb-client-java/pull/376) Add FillFlux [FluxDSL]

### Bug Fixes
1. [#358](https://github.com/influxdata/influxdb-client-java/pull/358): Missing backpressure for asynchronous non-blocking API
1. [#372](https://github.com/influxdata/influxdb-client-java/pull/372): Redact the `Authorization` HTTP header from log

### Dependencies
1. [#377](https://github.com/influxdata/influxdb-client-java/pull/377): Update dependencies:

#### Build:
    - kotlin-stdlib to 1.7.10
    - kotlinx-coroutines-core to 1.6.4
    - lombok to 1.18.24
    - micrometer-registry-influx to 1.9.2
    - okhttp3 to 4.10.0
    - okio to 3.2.0
    - rxjava to 3.1.5
    - scala-library_2 to 2.12.16
    - scala-collection-compat_2.12 to 2.8.0
    - spring to 5.3.22
    - spring-boot to 2.7.2

#### Maven Plugin:
    - maven-bundle-plugin to 5.1.7
    - maven-checkstyle-plugin to 3.1.2
    - maven-compiler-plugin to 3.10.1
    - maven-enforcer-plugin to 3.1.0
    - maven-failsafe-plugin to 3.0.0-M7
    - maven-jar-plugin to 3.2.2
    - maven-javadoc-plugin to 3.4.0
    - maven-project-info-reports-plugin to 3.4.0
    - maven-site-plugin to 3.12.0
    - maven-surefire-plugin to 3.0.0-M7
    - build-helper-maven-plugin to 3.3.0
    - dokka-maven-plugin to 1.7.10
    - jacoco-maven-plugin to 0.8.8
    - karaf-maven-plugin to 4.4.1
    - kotlin-maven-plugin to 1.7.10
    - license-maven-plugin to 4.1
    - nexus-staging-maven-plugin to 1.6.13
    - scala-maven-plugin to 4.7.1
    - scalatest-maven-plugin to 2.1.0
    - scala-maven-plugin to 3.4.4
    - scoverage-maven-plugin to 1.4.11
    - versions-maven-plugin to 2.11.0

#### Test:
    - assertj-core to 3.23.1
    - junit-jupiter-engine to 5.9.0
    - junit-platform-runner to 1.9.0
    - mockito to 4.6.1
    - scalatest_2.12 to 3.2.12
    - scalatest_2.13 to 3.2.12

## 6.3.0 [2022-06-30]

### Features
1. [#367](https://github.com/influxdata/influxdb-client-java/pull/367): Add HTTP status code to detail message of `InfluxException`
1. [#367](https://github.com/influxdata/influxdb-client-java/pull/367): Add `GatewayTimeoutException` for HTTP status code 504
1. [#371](https://github.com/influxdata/influxdb-client-java/pull/371): Add possibility to customize the `User-Agent` HTTP header

### CI
1. [#369](https://github.com/influxdata/influxdb-client-java/pull/369): Add JDK 18 to CI pipeline

## 6.2.0 [2022-06-24]

### Features
1. [#354](https://github.com/influxdata/influxdb-client-java/pull/354): Supports `contains` filter [FluxDSL]

### Bug Fixes
1. [#359](https://github.com/influxdata/influxdb-client-java/pull/359): Enable `OkHttp` retries for connection failure
1. [#360](https://github.com/influxdata/influxdb-client-java/pull/360): Fix double quote escape in flux-dsl

## 6.1.0 [2022-05-20]

### Breaking Changes
1. [#344](https://github.com/influxdata/influxdb-client-java/pull/344): Rename `InvocableScripts` to `InvokableScripts`

### Features
1. [#337](https://github.com/influxdata/influxdb-client-java/pull/337): Supports `columns` function [FluxDSL]
1. [#347](https://github.com/influxdata/influxdb-client-java/pull/347): Add `Scala` WriteApi

### Bug Fixes
1. [#339](https://github.com/influxdata/influxdb-client-java/pull/339): Evaluation of connection string
1. [#352](https://github.com/influxdata/influxdb-client-java/pull/352): Creating `Tasks` with `import` statements

## 6.0.0 [2022-04-19]

### Migration Notice
:warning: The InfluxDB Client Library uses internally `RxJava` to support write with batching, retry and backpressure.
 The underlying outdated `RxJava2` library was upgraded to the latest `RxJava3`.

- see [What is different in RxJava3](https://github.com/ReactiveX/RxJava/wiki/What's-different-in-3.0)

#### Spring

:warning: The client upgrades the `OkHttp` library to version `4.9.3`. The version `3.12.x` is no longer supported - [okhttp#requirements](https://github.com/square/okhttp#requirements).

The `spring-boot` supports the `OkHttp:4.9.3` from the version `2.7.0.M2` - [spring-boot/OkHttp 4.9.3](https://github.com/spring-projects/spring-boot/commit/fc8f55fbf44bd54e8e09de5858f8dbedb21fa9a5).
For the older version of `spring-boot` you have to configure Spring Boot's `okhttp3.version` property:

```xml
<properties>
    <okhttp3.version>4.9.3</okhttp3.version>
</properties>
```

### Changes in public API

  - `WriteService` imports:
    - `io.reactivex.Single` is refactored to `io.reactivex.rxjava3.core.Single`
  - `WriteOptions` imports:
    - `io.reactivex.BackpressureOverflowStrategy` -> `io.reactivex.rxjava3.core.BackpressureOverflowStrategy`
    - `io.reactivex.Scheduler` -> `io.reactivex.rxjava3.core.Scheduler`
    - `io.reactivex.schedulers.Schedulers` -> `io.reactivex.rxjava3.schedulers.Schedulers`
  - `InfluxDBClientReactive`:
    - `Single<HealthCheck> health()` -> `Publisher<HealthCheck> health()`
  - `WriteOptionsReactive`
    - `io.reactivex.Scheduler` -> `io.reactivex.rxjava3.core.Scheduler`
    - `io.reactivex.schedulers.Schedulers` -> `io.reactivex.rxjava3.schedulers.Schedulers`
  - `TelegrafsService` and `TelegrafsApi`
    - `TelegrafRequest` renamed to  `TelegrafPluginRequest` to create/update `Telegraf` configuration
    - `TelegrafPlugin.TypeEnum.INPUTS` renamed to  `TelegrafPlugin.TypeEnum.INPUT`
    - `TelegrafPlugin.TypeEnum.OUTPUTS` renamed to  `TelegrafPlugin.TypeEnum.OUTPUT`


### Services

This release also uses new version of InfluxDB OSS API definitions - [oss.yml](https://github.com/influxdata/openapi/blob/master/contracts/oss.yml). The following breaking changes are in underlying API services and doesn't affect common apis such as - `WriteApi`, `QueryApi`, `BucketsApi`, `OrganizationsApi`...

- Add `ConfigService` to retrieve InfluxDB's runtime configuration
- Add `DebugService` to retrieve debug and performance data from runtime
- Add `RemoteConnectionsService` to deal with registered remote InfluxDB connections
- Add `MetricsService` to deal with exposed prometheus metrics
- Add `ReplicationService` to manage InfluxDB replications
- Update `TemplatesService` to deal with `Stack` and `Template` API
- Update `RestoreService` to deal with new restore functions of InfluxDB

### List of updated dependencies:
 - Core:
    - com.squareup.okhttp3:okhttp:jar:4.9.3
    - com.squareup.okio:okio:jar:2.10.0
    - com.google.code.gson:gson:jar:2.9.0
    - io.reactivex.rxjava3:rxjava:jar:3.1.4
    - org.apache.commons:commons-csv:jar 1.9.0
    - io.gsonfire:gson-fire:1.8.5
 - Kotlin
   - org.jetbrains.kotlin:kotlin-stdlib:1.6.20
   - org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.4.3
   - org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0
 - Karaf
   - karaf 4.3.6
   - gson-fire 1.8.5
 - Micrometer
   - micrometer 1.8.4
 - OSGi
   - org.osgi:osgi.core:8.0.0
 - Spring integration
   - org.springframework.boot:spring-boot:jar:2.6.6
   - org.springframework:spring-core:jar:5.3.17

### Features
1. [#324](https://github.com/influxdata/influxdb-client-java/pull/298) Removed dependency on `io.swagger:swagger-annotations` and updated swagger to the latest version
1. [#289](https://github.com/influxdata/influxdb-client-java/pull/298): Upgrade `RxJava2` -> `RxJava3`, update outdated dependencies
1. [#316](https://github.com/influxdata/influxdb-client-java/pull/316): Add `InvokableScriptsApi` to create, update, list, delete and invoke scripts by seamless way
1. [#315](https://github.com/influxdata/influxdb-client-java/pull/315): Add support for timezones [FluxDSL]
1. [#317](https://github.com/influxdata/influxdb-client-java/pull/317): Gets HTTP headers from the unsuccessful HTTP request
1. [#334](https://github.com/influxdata/influxdb-client-java/pull/334): Supports not operator [FluxDSL]
1. [#335](https://github.com/influxdata/influxdb-client-java/pull/335): URL to connect to the InfluxDB is always evaluate as a connection string
1. [#329](https://github.com/influxdata/influxdb-client-java/pull/329): Add support for write `consistency` parameter [InfluxDB Enterprise]

    Configure `consistency` via `Write API`:
    ```diff
    - writeApi.writeRecord(WritePrecision.NS, "cpu_load_short,host=server02 value=0.67");
    + WriteParameters parameters = new WriteParameters(WritePrecision.NS, WriteConsistency.ALL);
    +
    + writeApi.writeRecord("cpu_load_short,host=server02 value=0.67", parameters);
    ```

    Configure `consistency` via client options:
    ```diff
    - InfluxDBClient client = InfluxDBClientFactory.createV1("http://influxdb_enterpriser:8086",
    -    "my-username",
    -    "my-password".toCharArray(),
    -    "my-db",
    -    "autogen");
    + InfluxDBClient client = InfluxDBClientFactory.createV1("http://influxdb_enterpriser:8086",
    +    "my-username",
    +    "my-password".toCharArray(),
    +    "my-db",
    +    "autogen",
    +    WriteConsistency.ALL);
    ```

### Bug Fixes
1. [#313](https://github.com/influxdata/influxdb-client-java/pull/313): Do not deliver `exception` when the consumer is already disposed [influxdb-client-reactive]

## 5.0.0 [2022-03-18]

### Breaking Changes

- Change type of `PermissionResource.type` to `String`. You are able to easily migrate by:
    ```diff
    - resource.setType(PermissionResource.TypeEnum.BUCKETS);
    + resource.setType(PermissionResource.TYPE_BUCKETS);
    ```

### Bug Fixes
1. [#303](https://github.com/influxdata/influxdb-client-java/pull/303): Change `PermissionResource.type` to `String`

### CI
1. [#304](https://github.com/influxdata/influxdb-client-java/pull/304): Use new Codecov uploader for reporting code coverage

## 4.3.0 [2022-02-18]

### Bug Fixes
1. [#300](https://github.com/influxdata/influxdb-client-java/pull/300): Uses native support for Rx requests to better performance

## 4.2.0 [2022-02-04]

### Bug Fixes
1. [#300](https://github.com/influxdata/influxdb-client-java/pull/300): Add missing PermissionResources from Cloud API definition

## 4.1.0 [2022-01-20]

### Features
1. [#286](https://github.com/influxdata/influxdb-client-java/pull/286): Add support for Parameterized Queries

### Bug Fixes
1. [#283](https://github.com/influxdata/influxdb-client-java/pull/283): Serialization `null` tag's value into LineProtocol
1. [#285](https://github.com/influxdata/influxdb-client-java/pull/285): Default dialect for Query APIs
1. [#294](https://github.com/influxdata/influxdb-client-java/pull/294): Mapping measurement with primitive `float`
1. [#297](https://github.com/influxdata/influxdb-client-java/pull/297): Transient dependency of `okhttp`, `retrofit` and `rxjava`
1. [#292](https://github.com/influxdata/influxdb-client-java/pull/292): Publishing runtime error as a WriteErrorEvent

## 4.0.0 [2021-11-26]

### Breaking Changes

The `Arguments` helper moved from package `com.influxdb` to package `com.influxdb.utils`.

#### Management API
This release uses the latest InfluxDB OSS API definitions - [oss.yml](https://raw.githubusercontent.com/influxdata/openapi/7d9edc32995f38b3474a24c36b89a8e125837f3c/contracts/oss.yml). The following breaking changes are in underlying API services and doesn't affect common apis such as - `WriteApi`, `QueryApi`, `BucketsApi`, `OrganizationsApi`...

- Add `LegacyAuthorizationsService` to deal with legacy authorizations
- Add `ResourceService` to retrieve all knows resources
- Move `postSignin` operation from `DefaultService` to `SigninService`
- Move `postSignout` operation from `DefaultService` to `SignoutService`
- Remove `TemplateApi` in favour of [InfluxDB Community Templates](https://github.com/influxdata/community-templates). For more info see - [influxdb#19300](https://github.com/influxdata/influxdb/pull/19300), [openapi#192](https://github.com/influxdata/openapi/pull/192)

### Deprecates
- `InfluxDBClient.health()`: instead use `InfluxDBClient.ping()`
- `InfluxDBClientKotlin.health()`: instead use `InfluxDBClientKotlin.ping()`
- `InfluxDBClientScala.health()`: instead use `InfluxDBClientScala.ping()`
- `SecretsService.postOrgsIDSecrets()`: instead use `SecretsService.deleteOrgsIDSecretsID()`

### Features
1. [#272](https://github.com/influxdata/influxdb-client-java/pull/272): Add `PingService` to check status of OSS and Cloud instance
1. [#278](https://github.com/influxdata/influxdb-client-java/pull/278): Add query method with all params for BucketsApi, OrganizationApi and TasksApi
1. [#280](https://github.com/influxdata/influxdb-client-java/pull/280): Use async HTTP calls in the Batching writer
1. [#251](https://github.com/influxdata/influxdb-client-java/pull/251): Client uses `Reactive Streams` in public API, `WriteReactiveApi` is cold `Publisher` [influxdb-client-reactive]

### Bug Fixes
1. [#279](https://github.com/influxdata/influxdb-client-java/pull/279): Session authentication for InfluxDB `2.1`
1. [#276](https://github.com/influxdata/influxdb-client-java/pull/276): `influxdb-client-utils` uses different package then `influxdb-client-core`[java module system]

### API
1. [#281](https://github.com/influxdata/influxdb-client-java/pull/281): Update to the latest InfluxDB OSS API

### CI
1. [#275](https://github.com/influxdata/influxdb-client-java/pull/275): Deploy `influxdb-client-test` package into Maven repository

## 3.4.0 [2021-10-22]

### Features
1. [#269](https://github.com/influxdata/influxdb-client-java/pull/269): Add possibility to use dynamic `measurement` in mapping from/to `POJO`

### CI
1. [#267](https://github.com/influxdata/influxdb-client-java/pull/267): Add JDK 17 (LTS) to CI pipeline instead of JDK 16

## 3.3.0 [2021-09-17]

### Bug Fixes
1. [#258](https://github.com/influxdata/influxdb-client-java/pull/258): Avoid requirements to `jdk.unsupported` module
1. [#263](https://github.com/influxdata/influxdb-client-java/pull/263): Fix dependency structure for `flux-dsl` module

### Dependencies
1. [#258](https://github.com/influxdata/influxdb-client-java/pull/258): Update dependencies:
    - Gson to 2.8.8

### CI
1. [#266](https://github.com/influxdata/influxdb-client-java/pull/266): Switch to next-gen CircleCI's convenience images

## 3.2.0 [2021-08-20]

### Bug Fixes
1. [#252](https://github.com/influxdata/influxdb-client-java/pull/252): Spring auto-configuration works even without `influxdb-client-reactive` [spring]
1. [#254](https://github.com/influxdata/influxdb-client-java/pull/254): Avoid reading entire query response into bytes array

### Deprecates
1. [#255](https://github.com/influxdata/influxdb-client-java/pull/255): `InfluxDBClient#getWriteApi()` instead use `InfluxDBClient#makeWriteApi()`

### Documentation
1. [#257](https://github.com/influxdata/influxdb-client-java/pull/257): How to configure proxy

## 3.1.0 [2021-07-27]

### Breaking Changes

#### `influxdb-spring`:

Change configuration prefix from `spring.influx2` to `influx` according to [Spring Docs](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#features.developing-auto-configuration.custom-starter.configuration-keys) - for more info see [README.md](./spring).

### Features
1. [#244](https://github.com/influxdata/influxdb-client-java/pull/244): Add support for auto-configure the reactive client - `InfluxDBClientReactive` [spring]

### Bug Fixes
1. [#246](https://github.com/influxdata/influxdb-client-java/pull/246): Parsing infinite numbers
1. [#241](https://github.com/influxdata/influxdb-client-java/pull/241): Set default HTTP protocol to HTTP 1.1

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
