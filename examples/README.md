# InfluxDB Client Examples

This directory contains Java, Kotlin and Scala examples.

## Java
- [WriteDataEverySecond.java](src/main/java/example/WriteDataEverySecond.java) - Write data every second 
- [ParameterizedQuery.java](src/main/java/example/ParameterizedQuery.java) - How to use Parameterized Queries
- [InfluxQLExample.java](src/main/java/example/InfluxQLExample.java) - How to use queries with the old influxQL dialect

    The `InfluxQL` can be used with `/query compatibility` endpoint which uses the **database** and **retention policy**
    specified in the query request to map the request to an InfluxDB bucket.
    For more information, see:
    - [/query 1.x compatibility API](https://docs.influxdata.com/influxdb/latest/reference/api/influxdb-1x/query/)
    - [Database and retention policy mapping](https://docs.influxdata.com/influxdb/latest/reference/api/influxdb-1x/dbrp/)

### Others
- [InvokableScripts.java](src/main/java/example/InvokableScripts.java) - How to use Invokable scripts Cloud API to create custom endpoints that query data
- [InfluxDBEnterpriseExample.java](src/main/java/example/InfluxDBEnterpriseExample.java) - How to use `consistency` parameter for InfluxDB Enterprise
- [RecordRowExample.java](src/main/java/example/RecordRowExample.java) - How to use `FluxRecord.getRow()` (List) instead of `FluxRecord.getValues()` (Map),
  in case of duplicity column names
- [WriteHttpExceptionHandled](src/main/java/example/WriteHttpExceptionHandled.java) - How to work with HTTP Exceptions for debugging and recovery.

## Kotlin

### Query
- [KotlinQuery.kt](src/main/java/example/KotlinQuery.kt) - How to query data into a stream of `FluxRecord` and filter them by [Flow](https://kotlinlang.org/docs/flow.html) operators
- [KotlinQueryRaw.kt](src/main/java/example/KotlinQueryRaw.kt) - How to query data into a stream of `String`
- [KotlinQueryDSL.kt](src/main/java/example/KotlinQueryDSL.kt) - How to use the [FluxDSL](../flux-dsl) to query data

### Writes
- [KotlinWriteApi.kt](src/main/java/example/KotlinWriteApi.kt) - How to ingest data by `DataPoint`, `LineProtocol` or `Data class`
- [KotlinWriteBatchingByFlow.kt](src/main/java/example/KotlinWriteBatchingByFlow.kt) - How to use [Flow](https://kotlinlang.org/docs/flow.html) operators to prepare batches for synchronous write into InfluxDB

## Scala

### Query
- [ScalaQuery.scala](src/main/java/example/ScalaQuery.scala) - How to query data into a stream of `FluxRecord` and filter them by `Flow` operators
- [ScalaQueryRaw.scala](src/main/java/example/ScalaQueryRaw.scala) - How to query data into a stream of `String`
- [ScalaQueryDSL.scala](src/main/java/example/ScalaQueryDSL.scala) - How to use the [FluxDSL](../flux-dsl) to query data

### Writes
- [ScalaWriteApi.scala](src/main/java/example/ScalaWriteApi.scala) - How to ingest data by `DataPoint`, `LineProtocol` or `POJO`
