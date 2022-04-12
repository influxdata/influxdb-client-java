# InfluxDB Client Examples

This directory contains Java, Kotlin and Scala examples.

## Java
- [WriteDataEverySecond.java](src/main/java/example/WriteDataEverySecond.java) - Write data every second 
- [ParameterizedQuery.java](src/main/java/example/ParameterizedQuery.java) - How to use Parameterized Queries

### Others
- [InvocableScripts.java](src/main/java/example/InvocableScripts.java) - How to use Invocable scripts Cloud API to create custom endpoints that query data
- [InfluxDBEnterpriseExample.java](src/main/java/example/InfluxDBEnterpriseExample.java) - How to use `consistency` parameter for InfluxDB Enterprise

## Kotlin

### Query
- [KotlinQuery.kt](src/main/java/example/KotlinQuery.kt) - How to query data into a stream of `FluxRecord` and filter them by [Flow](https://kotlinlang.org/docs/flow.html) operators
- [KotlinQueryRaw.kt](src/main/java/example/KotlinQueryRaw.kt) - How to query data into a stream of `String`
- [KotlinQueryDSL.kt](src/main/java/example/KotlinQueryDSL.kt) - How to use the [FluxDSL](../flux-dsl) to query data

### Writes
- [KotlinWriteApi.kt](src/main/java/example/KotlinWriteApi.kt) - How to ingest data by `DataPoint`, `LineProtocol` or `Data class`
- [KotlinWriteBatchingByFlow.kt](src/main/java/example/KotlinWriteBatchingByFlow.kt) - How to use [Flow](https://kotlinlang.org/docs/flow.html) operators to prepare batches for synchronous write into InfluxDB
  