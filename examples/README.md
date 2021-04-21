# InfluxDB Client Examples

This directory contains Java, Kotlin and Scala examples.

## Java
- [WriteDataEverySecond.java](src/main/java/example/WriteDataEverySecond.java) - Write data every second 

## Kotlin

### Query
- [InfluxDB2KotlinExample.kt](src/main/java/example/InfluxDB2KotlinExample.kt) - How to query data into a stream of `FluxRecord`s and filter them by [Flow](https://kotlinlang.org/docs/flow.html) operators
- [InfluxDB2KotlinExampleRaw.kt](src/main/java/example/InfluxDB2KotlinExampleRaw.kt) - How to query data into a stream of `String`s
- [InfluxDB2KotlinExampleDSL.kt](src/main/java/example/InfluxDB2KotlinExampleDSL.kt) - How to use the [FluxDSL](../flux-dsl) to query data
  