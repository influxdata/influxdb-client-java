# influxdb-client-java
[![Build Status](https://travis-ci.org/bonitoo-io/influxdb-client-java.svg?branch=master)](https://travis-ci.org/bonitoo-io/influxdb-client-java)
[![codecov](https://codecov.io/gh/bonitoo-io/influxdb-client-java/branch/master/graph/badge.svg)](https://codecov.io/gh/bonitoo-io/influxdb-client-java)
[![License](https://img.shields.io/github/license/bonitoo-io/influxdb-client-java.svg)](https://github.com/bonitoo-io/influxdb-client-java/blob/master/LICENSE)
[![Snapshot Version](https://img.shields.io/nexus/s/https/apitea.com/nexus/org.influxdata/influxdb-client-java.svg)](https://apitea.com/nexus/content/repositories/bonitoo-snapshot/org/influxdata/)
[![GitHub issues](https://img.shields.io/github/issues-raw/bonitoo-io/influxdb-client-java.svg)](https://github.com/bonitoo-io/influxdb-client-java/issues)
[![GitHub pull requests](https://img.shields.io/github/issues-pr-raw/bonitoo-io/influxdb-client-java.svg)](https://github.com/bonitoo-io/influxdb-client-java/pulls)

This module contains the Java client for the InfluxDB 2.0.

> This library is under development and no stable version has been released yet.  
> The API can change at any moment.

### Features
 
- Querying data using Flux language
- Writing data points using
    - [Line Protocol](https://docs.influxdata.com/influxdb/v1.6/write_protocols/line_protocol_tutorial/) 
    - [Point object](https://github.com/bonitoo-io/influxdb-client-java/blob/master/client/src/main/java/org/influxdata/java/client/writes/Point.java#L76) 
    - POJO
- InfluxDB 2.0 Management API client for managing
    - sources, buckets
    - tasks
    - authorizations
    - health check
         
### Documentation

#### Data query using Flux language

#### Write data

#### Management API
