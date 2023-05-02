# influxdb-client-java

[![javadoc](https://img.shields.io/badge/javadoc-link-brightgreen.svg)](https://influxdata.github.io/influxdb-client-java/influxdb-client-java/apidocs/index.html)

The reference Java client that allows query, write and management (bucket, organization, users) for the InfluxDB 2.x.

## Features
 
- [Querying data using Flux language](#queries)
  - [Parameterized Queries](#parameterized-queries)
- [Querying data using InfluxQL](#influxql-queries)
- [Writing data using](#writes)
    - [Line Protocol](#by-lineprotocol) 
    - [Data Point](#by-data-point) 
    - [POJO](#by-pojo)
    - [Default Tags](#default-tags)
- [InfluxDB 2.x Management API](#management-api)
    - sources, buckets
    - tasks
    - authorizations
    - health check
- [Advanced Usage](#advanced-usage)
    - [Delete data](#delete-data)
    - [Gzip support](#gzip-support)
    - [Proxy configuration](#proxy-configuration)
    - [Client configuration file](#client-configuration-file)
    - [Client connection string](#client-connection-string)
    - [Writing data using synchronous blocking API](#writing-data-using-synchronous-blocking-api)
    - [Monitoring & Alerting](#monitoring--alerting)
         
## Queries

For querying data we use [QueryApi](https://influxdata.github.io/influxdb-client-java/influxdb-client-java/apidocs/com/influxdb/client/QueryApi.html) that allow perform synchronous, asynchronous and also use raw query response.

For POJO mapping, snake_case column names are mapped to camelCase field names if exact matches not found.

### Synchronous query

The synchronous query is not intended for large query results because the Flux response can be potentially unbound.

```java
package example;

import java.util.List;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;

public class SynchronousQuery {

    private static char[] token = "my-token".toCharArray();
    private static String org = "my-org";

    public static void main(final String[] args) {

        InfluxDBClient influxDBClient = InfluxDBClientFactory.create("http://localhost:8086", token, org);

        String flux = "from(bucket:\"my-bucket\") |> range(start: 0)";

        QueryApi queryApi = influxDBClient.getQueryApi();

        //
        // Query data
        //
        List<FluxTable> tables = queryApi.query(flux);
        for (FluxTable fluxTable : tables) {
            List<FluxRecord> records = fluxTable.getRecords();
            for (FluxRecord fluxRecord : records) {
                System.out.println(fluxRecord.getTime() + ": " + fluxRecord.getValueByKey("_value"));
            }
        }

        influxDBClient.close();
    }
}
```

The synchronous query offers a possibility map [FluxRecords](http://bit.ly/flux-spec#record) to POJO:

```java
package example;

import java.time.Instant;
import java.util.List;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;

public class SynchronousQueryPojo {

    private static char[] token = "my-token".toCharArray();
    private static String org = "my-org";

    public static void main(final String[] args) {

        InfluxDBClient influxDBClient = InfluxDBClientFactory.create("http://localhost:8086", token, org);

        //
        // Query data
        //
        String flux = "from(bucket:\"my-bucket\") |> range(start: 0) |> filter(fn: (r) => r._measurement == \"temperature\")";

        QueryApi queryApi = influxDBClient.getQueryApi();

        //
        // Map to POJO
        //
        List<Temperature> temperatures = queryApi.query(flux, Temperature.class);
        for (Temperature temperature : temperatures) {
            System.out.println(temperature.location + ": " + temperature.value + " at " + temperature.time);
        }

        influxDBClient.close();
    }

    @Measurement(name = "temperature")
    public static class Temperature {

        @Column(tag = true)
        String location;

        @Column
        Double value;

        @Column(timestamp = true)
        Instant time;
    }
}
```

### Asynchronous query

The Asynchronous query offers possibility to process unbound query and allow user to handle exceptions, 
stop receiving more results and notify that all data arrived.  

```java
package example;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;

public class AsynchronousQuery {

    private static char[] token = "my-token".toCharArray();
    private static String org = "my-org";

    public static void main(final String[] args) throws InterruptedException {

        InfluxDBClient influxDBClient = InfluxDBClientFactory.create("http://localhost:8086", token, org);
        //
        // Query data
        //
        String flux = "from(bucket:\"my-bucket\") |> range(start: 0)";

        QueryApi queryApi = influxDBClient.getQueryApi();

        queryApi.query(flux, (cancellable, fluxRecord) -> {

            //
            // The callback to consume a FluxRecord.
            //
            // cancelable - object has the cancel method to stop asynchronous query
            //
            System.out.println(fluxRecord.getTime() + ": " + fluxRecord.getValueByKey("_value"));

        }, throwable -> {

            //
            // The callback to consume any error notification.
            //
            System.out.println("Error occurred: " + throwable.getMessage());

        }, () -> {

            //
            // The callback to consume a notification about successfully end of stream.
            //
            System.out.println("Query completed");

        });

        Thread.sleep(5_000);

        influxDBClient.close();
    }
}
```

And there is also a possibility map [FluxRecords](http://bit.ly/flux-spec#record) to POJO:

```java
package example;

import java.time.Instant;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;

public class AsynchronousQueryPojo {

    private static char[] token = "my-token".toCharArray();
    private static String org = "my-org";

    public static void main(final String[] args) throws InterruptedException {

        InfluxDBClient influxDBClient = InfluxDBClientFactory.create("http://localhost:8086", token, org);

        //
        // Query data
        //
        String flux = "from(bucket:\"my-bucket\") |> range(start: 0) |> filter(fn: (r) => r._measurement == \"temperature\")";

        QueryApi queryApi = influxDBClient.getQueryApi();

        //
        // Map to POJO
        //
        queryApi.query(flux, Temperature.class, (cancellable, temperature) -> {

            //
            // The callback to consume a FluxRecord mapped to POJO.
            //
            // cancelable - object has the cancel method to stop asynchronous query
            //
            System.out.println(temperature.location + ": " + temperature.value + " at " + temperature.time);
        });

        Thread.sleep(5_000);

        influxDBClient.close();
    }

    @Measurement(name = "temperature")
    public static class Temperature {

        @Column(tag = true)
        String location;

        @Column
        Double value;

        @Column(timestamp = true)
        Instant time;
    }
}
```

### Raw query

The Raw query allows direct processing original [CSV response](http://bit.ly/flux-spec#csv): 

```java
package example;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;

public class RawQuery {

    private static char[] token = "my-token".toCharArray();
    private static String org = "my-org";

    public static void main(final String[] args) {

        InfluxDBClient influxDBClient = InfluxDBClientFactory.create("http://localhost:8086", token, org);

        //
        // Query data
        //
        String flux = "from(bucket:\"my-bucket\") |> range(start: 0)";

        QueryApi queryApi = influxDBClient.getQueryApi();

        String csv = queryApi.queryRaw(flux);

        System.out.println("CSV response: " + csv);

        influxDBClient.close();
    }
}
```

The Asynchronous version allows processing line by line:

```java
package example;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;

public class RawQueryAsynchronous {

    private static char[] token = "my-token".toCharArray();
    private static String org = "my-org";

    public static void main(final String[] args) throws Exception {

        InfluxDBClient influxDBClient = InfluxDBClientFactory.create("http://localhost:8086", token, org);

        //
        // Query data
        //
        String flux = "from(bucket:\"my-bucket\") |> range(start: 0)";

        QueryApi queryApi = influxDBClient.getQueryApi();

        queryApi.queryRaw(flux, (cancellable, line) -> {

            //
            // The callback to consume a line of CSV response
            //
            // cancelable - object has the cancel method to stop asynchronous query
            //
            System.out.println("Response: " + line);
        });

        Thread.sleep(5_000);

        influxDBClient.close();
    }
}
```

### Parameterized Queries

InfluxDB Cloud supports [Parameterized Queries](https://docs.influxdata.com/influxdb/cloud/query-data/parameterized-queries/)
that let you dynamically change values in a query using the InfluxDB API. Parameterized queries make Flux queries more
reusable and can also be used to help prevent injection attacks.

InfluxDB Cloud inserts the params object into the Flux query as a Flux record named `params`. Use dot or bracket
notation to access parameters in the `params` record in your Flux query. Parameterized Flux queries support only `int`
, `float`, and `string` data types. To convert the supported data types into
other [Flux basic data types, use Flux type conversion functions](https://docs.influxdata.com/influxdb/cloud/query-data/parameterized-queries/#supported-parameter-data-types).

Parameterized query example:
> :warning: Parameterized Queries are supported only in InfluxDB Cloud, currently there is no support in InfluxDB OSS.
```java
package example;

import java.time.Instant;
import java.time.Period;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxTable;

public class ParameterizedQuery {

    public static void main(String[] args) {

        String url = "https://us-west-2-1.aws.cloud2.influxdata.com";
        String token = "my-token";
        String org = "my-org";
        String bucket = "my-bucket";
        try (InfluxDBClient client = InfluxDBClientFactory.create(url, token.toCharArray(), org, bucket)) {

            QueryApi queryApi = client.getQueryApi();

            Instant yesterday = Instant.now().minus(Period.ofDays(1));

            Point p = Point.measurement("temperature")
                .addTag("location", "north")
                .addField("value", 60.0)
                .time(yesterday, WritePrecision.NS);

            WriteApiBlocking writeApi = client.getWriteApiBlocking();
            writeApi.writePoint(p);

            //
            // Query range start parameter using Instant
            //
            Map<String, Object> params = new HashMap<>();
            params.put("bucketParam", bucket);
            params.put("startParam", yesterday.toString());

            String parametrizedQuery = "from(bucket: params.bucketParam) |> range(start: time(v: params.startParam))";

            List<FluxTable> query = queryApi.query(parametrizedQuery, org, params);
            query.forEach(fluxTable -> fluxTable.getRecords()
                .forEach(r -> System.out.println(r.getTime() + ": " + r.getValueByKey("_value"))));

            //
            // Query range start parameter using duration
            //
            params.put("startParam", "-1d10s");
            parametrizedQuery = "from(bucket: params.bucketParam) |> range(start: duration(v: params.startParam))";
            query = queryApi.query(parametrizedQuery, org, params);
            query.forEach(fluxTable -> fluxTable.getRecords()
                .forEach(r -> System.out.println(r.getTime() + ": " + r.getValueByKey("_value"))));

        }
    }
}
```

### InfluxQL Queries

The `InfluxQL` can be used with `/query compatibility` endpoint which uses the **database** and **retention policy** specified in the query request to map the request to an InfluxDB bucket.
For more information, see: .

- [/query 1.x compatibility API](https://docs.influxdata.com/influxdb/latest/reference/api/influxdb-1x/query/)
- [Database and retention policy mapping](https://docs.influxdata.com/influxdb/latest/reference/api/influxdb-1x/dbrp/)

This is an example of how to use this library to run a query with influxQL:

```java
package example;

import java.math.BigDecimal;
import java.time.Instant;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.InfluxQLQueryApi;
import com.influxdb.client.domain.InfluxQLQuery;
import com.influxdb.query.InfluxQLQueryResult;

public class InfluxQLExample {

  private static char[] token = "my-token".toCharArray();
  private static String org = "my-org";

  private static String database = "my-org";

  public static void main(final String[] args) {

    try (InfluxDBClient influxDBClient = InfluxDBClientFactory.create("http://localhost:8086", token, org)) {

      //
      // Query data
      //
      String influxQL = "SELECT FIRST(\"free\") FROM \"influxql\"";

      InfluxQLQueryApi queryApi = influxDBClient.getInfluxQLQueryApi();

      // send request
      InfluxQLQueryResult result = queryApi.query(new InfluxQLQuery(influxQL, database).setPrecision(InfluxQLQuery.InfluxQLPrecision.SECONDS),
              (columnName, rawValue, resultIndex, seriesName) -> {
                // convert columns
                switch (columnName) {
                  case "time":
                    return Instant.ofEpochSecond(Long.parseLong(rawValue));
                  case "first":
                    return new BigDecimal(rawValue);
                  default:
                    throw new IllegalArgumentException("unexpected column " + columnName);
                }
              });

      for (InfluxQLQueryResult.Result resultResult : result.getResults()) {
        for (InfluxQLQueryResult.Series series : resultResult.getSeries()) {
          for (InfluxQLQueryResult.Series.Record record : series.getValues()) {
            System.out.println(record.getValueByKey("time") + ": " + record.getValueByKey("first"));
          }
        }
      }

    }
  }
}
```

## Writes

The client offers two types of API to ingesting data:
1. [Synchronous blocking API](#synchronous-blocking-api)
1. [Asynchronous non-blocking API](#asynchronous-non-blocking-api) which supports batching, retrying and jittering

### Synchronous blocking API

The [WriteApiBlocking](https://influxdata.github.io/influxdb-client-java/influxdb-client-java/apidocs/com/influxdb/client/WriteApiBlocking.html) provides a synchronous blocking API to writing data using [InfluxDB Line Protocol](https://docs.influxdata.com/influxdb/v1.6/write_protocols/line_protocol_tutorial/), Data Point and POJO.

_It's up to user to handle a server or a http exception._

```java
package example;

import java.time.Instant;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.exceptions.InfluxException;

public class WriteDataBlocking {

    private static char[] token = "my-token".toCharArray();
    private static String org = "my-org";
    private static String bucket = "my-bucket";

    public static void main(final String[] args) {

        InfluxDBClient influxDBClient = InfluxDBClientFactory.create("http://localhost:8086", token, org, bucket);

        WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();

        try {
            //
            // Write by LineProtocol
            //
            String record = "temperature,location=north value=60.0";

            writeApi.writeRecord(WritePrecision.NS, record);

            //
            // Write by Data Point
            //
            Point point = Point.measurement("temperature")
                    .addTag("location", "west")
                    .addField("value", 55D)
                    .time(Instant.now().toEpochMilli(), WritePrecision.MS);

            writeApi.writePoint(point);

            //
            // Write by POJO
            //
            Temperature temperature = new Temperature();
            temperature.location = "south";
            temperature.value = 62D;
            temperature.time = Instant.now();

            writeApi.writeMeasurement(WritePrecision.NS, temperature);

        } catch (InfluxException ie) {
            System.out.println("InfluxException: " + ie);
        }

        influxDBClient.close();
    }

    @Measurement(name = "temperature")
    private static class Temperature {

        @Column(tag = true)
        String location;

        @Column
        Double value;

        @Column(timestamp = true)
        Instant time;
    }
}
```
         
### Asynchronous non-blocking API

> :warning: **The `WriteApi` is supposed to be use as a singleton**. Don't create a new instance for every write!

For writing data we use [WriteApi](https://influxdata.github.io/influxdb-client-java/influxdb-client-java/apidocs/com/influxdb/client/WriteApi.html) that is an asynchronous non-blocking API and supports:

1. writing data using [InfluxDB Line Protocol](https://docs.influxdata.com/influxdb/v1.6/write_protocols/line_protocol_tutorial/), Data Point, POJO 
2. use batching for writes
3. use client backpressure strategy
4. produces events that allow user to be notified and react to this events
    - `WriteSuccessEvent` - published when arrived the success response from Platform server
    - `BackpressureEvent` - published when is **client** backpressure applied
    - `WriteErrorEvent` - published when occurs a unhandled exception
    - `WriteRetriableErrorEvent` - published when occurs a retriable error
5. use GZIP compression for data

The writes are processed in batches which are configurable by `WriteOptions`:

| Property | Description | Default Value |
| --- | --- | --- |
| **batchSize** | the number of data point to collect in batch | 1000 |
| **flushInterval** | the number of milliseconds before the batch is written | 1000 |
| **jitterInterval** | the number of milliseconds to increase the batch flush interval by a random amount | 0 |
| **retryInterval** | the number of milliseconds to retry unsuccessful write. The retry interval is used when the InfluxDB server does not specify "Retry-After" header.| 5000 |
| **maxRetries** | the number of max retries when write fails | 5 |
| **maxRetryDelay** | the maximum delay between each retry attempt in milliseconds | 125_000 |
| **maxRetryTime** | maximum total retry timeout in milliseconds | 180_000 |
| **exponentialBase** | the base for the exponential retry delay, the next delay is computed using random exponential backoff as a random value within the interval  ``retryInterval * exponentialBase^(attempts-1)`` and ``retryInterval * exponentialBase^(attempts)``. Example for ``retryInterval=5_000, exponentialBase=2, maxRetryDelay=125_000, total=5`` Retry delays are random distributed values within the ranges of ``[5_000-10_000, 10_000-20_000, 20_000-40_000, 40_000-80_000, 80_000-125_000]``
| **bufferLimit** | the maximum number of unwritten stored points | 10000 |
| **backpressureStrategy** | the strategy to deal with buffer overflow | DROP_OLDEST |

#### Backpressure
The backpressure presents the problem of what to do with a growing backlog of unconsumed data points. 
The key feature of backpressure is to provide the capability to avoid consuming the unexpected amount of system resources.  
This situation is not common and can be caused by several problems: generating too much measurements in short interval,
long term unavailability of the InfluxDB server, network issues. 

The size of backlog is configured by 
`WriteOptions.bufferLimit` and backpressure strategy by `WriteOptions.backpressureStrategy`.

##### Strategy how react to backlog overflows
- `DROP_OLDEST` - Drop the oldest data points from the backlog 
- `DROP_LATEST` - Drop the latest data points from the backlog  
- `ERROR` - Signal a exception
- `BLOCK` - (not implemented yet) Wait specified time for space in buffer to become available
  - `timeout` - how long to wait before giving up
  - `unit` - TimeUnit of the timeout

If is used the strategy `DROP_OLDEST` or `DROP_LATEST` there is a possibility to react on backpressure event and slowdown the producing new measurements:

```java
WriteApi writeApi = influxDBClient.getWriteApi(writeOptions);
writeApi.listenEvents(BackpressureEvent.class, value -> {
    //
    // slowdown producers
    //...
});
```

There is also a synchronous blocking version of `WriteApi` - [WriteApiBlocking](#writing-data-using-synchronous-blocking-api).

#### Writing data

##### By POJO

Write Measurement into specified bucket:

```java
package example;

import java.time.Instant;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;

public class WritePojo {

    private static char[] token = "my-token".toCharArray();
    private static String org = "my-org";
    private static String bucket = "my-bucket";

    public static void main(final String[] args) {

        InfluxDBClient influxDBClient = InfluxDBClientFactory.create("http://localhost:8086", token, org, bucket);

        //
        // Write data
        //
        try (WriteApi writeApi = influxDBClient.makeWriteApi()) {

            //
            // Write by POJO
            //
            Temperature temperature = new Temperature();
            temperature.location = "south";
            temperature.value = 62D;
            temperature.time = Instant.now();

            writeApi.writeMeasurement(WritePrecision.NS, temperature);
        }

        influxDBClient.close();
    }

    @Measurement(name = "temperature")
    private static class Temperature {

        @Column(tag = true)
        String location;

        @Column
        Double value;

        @Column(timestamp = true)
        Instant time;
    }
}
```

##### By Data Point

Write Data point into specified bucket:

```java
package example;

import java.time.Instant;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;

public class WriteDataPoint {

    private static char[] token = "my-token".toCharArray();
    private static String org = "my-org";
    private static String bucket = "my-bucket";

    public static void main(final String[] args) {

        InfluxDBClient influxDBClient = InfluxDBClientFactory.create("http://localhost:8086", token, org, bucket);

        //
        // Write data
        //
        try (WriteApi writeApi = influxDBClient.makeWriteApi()) {

            //
            // Write by Data Point
            //
            Point point = Point.measurement("temperature")
                    .addTag("location", "west")
                    .addField("value", 55D)
                    .time(Instant.now().toEpochMilli(), WritePrecision.MS);

            writeApi.writePoint(point);
        }

        influxDBClient.close();
    }
}
```

##### By LineProtocol

Write Line Protocol record into specified bucket:

```java
package example;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;

public class WriteLineProtocol {

    private static char[] token = "my-token".toCharArray();
    private static String org = "my-org";
    private static String bucket = "my-bucket";

    public static void main(final String[] args) {

        InfluxDBClient influxDBClient = InfluxDBClientFactory.create("http://localhost:8086", token, org, bucket);

        //
        // Write data
        //
        try (WriteApi writeApi = influxDBClient.makeWriteApi()) {

            //
            // Write by LineProtocol
            //
            String record = "temperature,location=north value=60.0";

            writeApi.writeRecord(WritePrecision.NS, record);
        }

        influxDBClient.close();
    }
}
```

##### Default Tags

Sometimes is useful to store same information in every measurement e.g. `hostname`, `location`, `customer`. 
The client is able to use static value, system property or env property as a tag value.

The expressions:
- `California Miner` - static value
- `${version}` -  system property
- `${env.hostname}` - environment property

###### Via Configuration file

In a [configuration file](#client-configuration-file) you are able to specify default tags by `influx2.measurement` prefix.

```properties
influx2.tags.id = 132-987-655
influx2.tags.customer = California Miner
influx2.tags.hostname = ${env.hostname}
influx2.tags.sensor-version = ${version}
```

###### Via API

```java
InfluxDBClientOptions options = InfluxDBClientOptions.builder()
    .url(url)
    .authenticateToken(token)
    .addDefaultTag("id", "132-987-655")
    .addDefaultTag("customer", "California Miner")
    .addDefaultTag("hostnamer", "${env.hostname}")
    .addDefaultTag("sensor-version", "${version}")
    .build();
```

Both of configurations will produce the Line protocol:

```
mine-sensor,id=132-987-655,customer="California Miner",hostname=example.com,sensor-version=v1.00 altitude=10
```

#### Handle the Events

##### Handle the Success write

```java
WriteApi writeApi = influxDBClient.makeWriteApi();
writeApi.listenEvents(WriteSuccessEvent.class, event -> {

    String data = event.getLineProtocol();

    //
    // handle success
    //
});
```

##### Handle the Error Write

```java
WriteApi writeApi = influxDBClient.makeWriteApi();
writeApi.listenEvents(WriteErrorEvent.class, event -> {

    Throwable exception = event.getThrowable();

    //
    // handle error
    //
});
```

## Management API

The client has following management API:

| API endpoint | Description | Javadoc |
| --- | --- | --- |
| **/api/v2/authorizations** | Managing authorization data | [AuthorizationsApi](https://influxdata.github.io/influxdb-client-java/influxdb-client-java/apidocs/com/influxdb/client/AuthorizationsApi.html) |
| **/api/v2/buckets** | Managing bucket data | [BucketsApi](https://influxdata.github.io/influxdb-client-java/influxdb-client-java/apidocs/com/influxdb/client/BucketsApi.html) |
| **/api/v2/orgs** | Managing organization data | [OrganizationsApi](https://influxdata.github.io/influxdb-client-java/influxdb-client-java/apidocs/com/influxdb/client/OrganizationsApi.html) |
| **/api/v2/users** | Managing user data | [UsersApi](https://influxdata.github.io/influxdb-client-java/influxdb-client-java/apidocs/com/influxdb/client/UsersApi.html) |
| **/api/v2/sources** | Managing sources | [SourcesApi](https://influxdata.github.io/influxdb-client-java/influxdb-client-java/apidocs/com/influxdb/client/SourcesApi.html) |
| **/api/v2/tasks** | Managing one-off and recurring tasks | [TasksApi](https://influxdata.github.io/influxdb-client-java/influxdb-client-java/apidocs/com/influxdb/client/TasksApi.html) |
| **/api/v2/scrapers** | Managing ScraperTarget data | [ScraperTargetsApi](https://influxdata.github.io/influxdb-client-java/influxdb-client-java/apidocs/com/influxdb/client/ScraperTargetsApi.html) |
| **/api/v2/labels** | Managing resource labels | [LabelsApi](https://influxdata.github.io/influxdb-client-java/influxdb-client-java/apidocs/com/influxdb/client/LabelsApi.html) |
| **/api/v2/telegrafs** | Managing telegraf config data | [TelegrafsApi](https://influxdata.github.io/influxdb-client-java/influxdb-client-java/apidocs/com/influxdb/client/TelegrafsApi.html) |
| **/api/v2/setup** | Managing onboarding setup | [InfluxDBClient#onBoarding()](https://influxdata.github.io/influxdb-client-java/influxdb-client-java/apidocs/com/influxdb/client/InfluxDBClient.html#onBoarding-com.influxdb.client.domain.Onboarding-) |
| **/ready** | Get the readiness of an instance at startup| [InfluxDBClient#ready()](https://influxdata.github.io/influxdb-client-java/influxdb-client-java/apidocs/com/influxdb/client/InfluxDBClient.html#ready--) |
| **/health** | Get the health of an instance anytime during execution | [InfluxDBClient#health()](https://influxdata.github.io/influxdb-client-java/influxdb-client-java/apidocs/com/influxdb/client/InfluxDBClient.html#health--) |


The following example demonstrates how to use a InfluxDB 2.x Management API. For further information see endpoints Javadoc.

```java
package example;

import java.util.Arrays;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.domain.Authorization;
import com.influxdb.client.domain.Bucket;
import com.influxdb.client.domain.Permission;
import com.influxdb.client.domain.PermissionResource;
import com.influxdb.client.domain.BucketRetentionRules;

public class InfluxDB2ManagementExample {

    private static char[] token = "my-token".toCharArray();

    public static void main(final String[] args) {

        InfluxDBClient influxDBClient = InfluxDBClientFactory.create("http://localhost:8086", token);

        //
        // Create bucket "iot_bucket" with data retention set to 3,600 seconds
        //
        BucketRetentionRules retention = new BucketRetentionRules();
        retention.setEverySeconds(3600);

        Bucket bucket = influxDBClient.getBucketsApi().createBucket("iot-bucket", retention, "12bdc4164c2e8141");

        //
        // Create access token to "iot_bucket"
        //
        PermissionResource resource = new PermissionResource();
        resource.setId(bucket.getId());
        resource.setOrgID("12bdc4164c2e8141");
        resource.setType(PermissionResource.TYPE_BUCKETS);

        // Read permission
        Permission read = new Permission();
        read.setResource(resource);
        read.setAction(Permission.ActionEnum.READ);

        // Write permission
        Permission write = new Permission();
        write.setResource(resource);
        write.setAction(Permission.ActionEnum.WRITE);

        Authorization authorization = influxDBClient.getAuthorizationsApi()
                .createAuthorization("12bdc4164c2e8141", Arrays.asList(read, write));

        //
        // Created token that can be use for writes to "iot_bucket"
        //
        String token = authorization.getToken();
        System.out.println("Token: " + token);

        influxDBClient.close();
    }
}
```

## Advanced Usage

### Writing data using synchronous blocking API

The [WriteApiBlocking](https://influxdata.github.io/influxdb-client-java/influxdb-client-java/apidocs/com/influxdb/client/WriteApiBlocking.html) provides a synchronous blocking API to writing data using [InfluxDB Line Protocol](https://docs.influxdata.com/influxdb/v1.6/write_protocols/line_protocol_tutorial/), Data Point and POJO. 

_It's up to user to handle a server or a http exception._

```java
package example;

import java.time.Instant;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.exceptions.InfluxException;

public class WriteDataBlocking {

    private static char[] token = "my-token".toCharArray();
    private static String org = "my-org";
    private static String bucket = "my-bucket";

    public static void main(final String[] args) {

        InfluxDBClient influxDBClient = InfluxDBClientFactory.create("http://localhost:8086", token, org, bucket);

        WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();

        try {
            //
            // Write by LineProtocol
            //
            String record = "temperature,location=north value=60.0";

            writeApi.writeRecord(WritePrecision.NS, record);

            //
            // Write by Data Point
            //
            Point point = Point.measurement("temperature")
                    .addTag("location", "west")
                    .addField("value", 55D)
                    .time(Instant.now().toEpochMilli(), WritePrecision.MS);

            writeApi.writePoint(point);

            //
            // Write by POJO
            //
            Temperature temperature = new Temperature();
            temperature.location = "south";
            temperature.value = 62D;
            temperature.time = Instant.now();

            writeApi.writeMeasurement(WritePrecision.NS, temperature);

        } catch (InfluxException ie) {
            System.out.println("InfluxException: " + ie);
        }

        influxDBClient.close();
    }

    @Measurement(name = "temperature")
    private static class Temperature {

        @Column(tag = true)
        String location;

        @Column
        Double value;

        @Column(timestamp = true)
        Instant time;
    }
}
```

### Monitoring & Alerting

The example below show how to create a check for monitoring a stock price. A Slack notification is created if the price is lesser than `35`.

##### Create Threshold Check

The Check set status to `Critical` if the `current` value for a `stock` measurement is lesser than `35`.

```java   
Organization org = ...;

String query = "from(bucket: \"my-bucket\") "
        + "|> range(start: v.timeRangeStart, stop: v.timeRangeStop)  "
        + "|> filter(fn: (r) => r._measurement == \"stock\")  "
        + "|> filter(fn: (r) => r.company == \"zyz\")  "
        + "|> aggregateWindow(every: 5s, fn: mean)  "
        + "|> filter(fn: (r) => r._field == \"current\")  "
        + "|> yield(name: \"mean\")";

LesserThreshold threshold = new LesserThreshold();
threshold.setLevel(CheckStatusLevel.CRIT);
threshold.setValue(35F);

String message = "The Stock price for XYZ is on: ${ r._level } level!";

influxDBClient
    .getChecksApi()
    .createThresholdCheck("XYZ Stock value", query, "5s", message, threshold, org.getId());
```   

##### Create Slack Notification endpoint

```java
String url = "https://hooks.slack.com/services/x/y/z";   

SlackNotificationEndpoint endpoint = influxDBClient
    .getNotificationEndpointsApi()
    .createSlackEndpoint("Slack Endpoint", url, org.getId());
```

##### Create Notification Rule
```java   
influxDBClient
    .getNotificationRulesApi()
    .createSlackRule("Critical status to Slack", "10s", "${ r._message }", RuleStatusLevel.CRIT, endpoint, org.getId());
```

### Delete data

The following example demonstrates how to delete data from InfluxDB 2.x.

```java
package example;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

import com.influxdb.client.DeleteApi;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.exceptions.InfluxException;

public class DeleteData {

    private static char[] token = "my-token".toCharArray();

    public static void main(final String[] args) {

        InfluxDBClient influxDBClient = InfluxDBClientFactory.create("http://localhost:8086", token);

        DeleteApi deleteApi = influxDBClient.getDeleteApi();

        try {

            OffsetDateTime start = OffsetDateTime.now().minus(1, ChronoUnit.HOURS);
            OffsetDateTime stop = OffsetDateTime.now();

            deleteApi.delete(start, stop, "", "my-bucket", "my-org");

        } catch (InfluxException ie) {
            System.out.println("InfluxException: " + ie);
        }

        influxDBClient.close();
    }
}
```

### Client configuration file

A client can be configured via configuration file. The configuration file has to be named as `influx2.properties` and has to be in root of classpath.

The following options are supported:

| Property name           | default    | description                                                |
|-------------------------|------------|------------------------------------------------------------| 
| influx2.url             | -          | the url to connect to InfluxDB                             |
| influx2.org             | -          | default destination organization for writes and queries    |
| influx2.bucket          | -          | default destination bucket for writes                      |
| influx2.token           | -          | the token to use for the authorization                     |
| influx2.logLevel        | NONE       | rest client verbosity level                                |
| influx2.readTimeout     | 10000 ms   | read timeout                                               |
| influx2.writeTimeout    | 10000 ms   | write timeout                                              |
| influx2.connectTimeout  | 10000 ms   | socket timeout                                             |
| influx2.precision       | NS         | default precision for unix timestamps in the line protocol |
| influx2.clientType      | -          | to customize the User-Agent HTTP header                    |

The `influx2.readTimeout`, `influx2.writeTimeout` and `influx2.connectTimeout` supports `ms`, `s` and `m` as unit. Default is milliseconds.


##### Configuration example

```properties
influx2.url=http://localhost:8086
influx2.org=my-org
influx2.bucket=my-bucket
influx2.token=my-token
influx2.logLevel=BODY
influx2.readTimeout=5s
influx2.writeTimeout=10s
influx2.connectTimeout=5s
```

and then:

```java
InfluxDBClient influxDBClient = InfluxDBClientFactory.create();
```

### Client connection string

A client can be constructed using a connection string that can contain the InfluxDBClientOptions parameters encoded into the URL.  
 
```java
InfluxDBClient influxDBClient = InfluxDBClientFactory
            .create("http://localhost:8086?readTimeout=5000&connectTimeout=5000&logLevel=BASIC", token)
```
The following options are supported:

| Property name    | default    | description                                                |
|------------------|------------|------------------------------------------------------------| 
| org              | -          | default destination organization for writes and queries    |
| bucket           | -          | default destination bucket for writes                      |
| token            | -          | the token to use for the authorization                     |
| logLevel         | NONE       | rest client verbosity level                                |
| readTimeout      | 10000 ms   | read timeout                                               |
| writeTimeout     | 10000 ms   | write timeout                                              |
| connectTimeout   | 10000 ms   | socket timeout                                             |
| precision        | NS         | default precision for unix timestamps in the line protocol |
| clientType       | -          | to customize the User-Agent HTTP header                    |

The `readTimeout`, `writeTimeout` and `connectTimeout` supports `ms`, `s` and `m` as unit. Default is milliseconds.

### Gzip support
`InfluxDBClient` does not enable gzip compress for http requests by default. If you want to enable gzip to reduce transfer data's size, you can call:

```java
influxDBClient.enableGzip();
```

### Proxy configuration

You can configure the client to tunnel requests through an HTTP proxy. To configure the proxy use a `okHttpClient` configuration:
        
```java
OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder()
        .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy", 8088)));

InfluxDBClientOptions options = InfluxDBClientOptions.builder()
        .url("http://localhost:9999")
        .authenticateToken("my-token".toCharArray())
        .okHttpClient(okHttpBuilder)
        .build();

InfluxDBClient client = InfluxDBClientFactory.create(options);
```

If you need to use proxy authentication then use something like:

```java
OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder()
        .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy", 8088)))
        .proxyAuthenticator(new Authenticator() {
            @Override
            public Request authenticate(@Nullable final Route route, @Nonnull final Response response) {
                return response.request().newBuilder()
                        .header("Proxy-Authorization", "Token proxy-token")
                        .build();
            }
        });

InfluxDBClientOptions options = InfluxDBClientOptions.builder()
        .url("http://localhost:9999")
        .authenticateToken("my-token".toCharArray())
        .okHttpClient(okHttpBuilder)
        .build();

InfluxDBClient client = InfluxDBClientFactory.create(options);
```

> :warning: If your proxy notify the client with permanent redirect (``HTTP 301``) to **different host**.
The client removes ``Authorization`` header, because otherwise the contents of ``Authorization`` is sent to third parties
which is a security vulnerability.

You can bypass this behaviour by:

```java
String token = "my-token";
OkHttpClient.Builder okHttpClient = new OkHttpClient.Builder()
        .addNetworkInterceptor(new Interceptor() {
            @Nonnull
            @Override
            public Response intercept(@Nonnull final Chain chain) throws IOException {
                Request authorization = chain.request().newBuilder()
                        .header("Authorization", "Token " + token)
                        .build();
                return chain.proceed(authorization);
            }
        });

InfluxDBClientOptions options = InfluxDBClientOptions.builder()
        .url("http://localhost:9999")
        .authenticateToken(token.toCharArray())
        .okHttpClient(okHttpClient)
        .build();

InfluxDBClient client = InfluxDBClientFactory.create(options);
```

### Log HTTP Request and Response
The Requests and Responses can be logged by changing the LogLevel. LogLevel values are NONE, BASIC, HEADER, BODY. Note that 
applying the `BODY` LogLevel will disable chunking while streaming and will load the whole response into memory.  

```java
influxDBClient.setLogLevel(LogLevel.HEADERS)
```

### Check the server status 

Server availability can be checked using the `influxDBClient.health()` endpoint.

### Construct queries using the [flux-dsl](../flux-dsl) query builder
```java
package example;

import java.time.temporal.ChronoUnit;
import java.util.List;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.influxdb.query.dsl.Flux;
import com.influxdb.query.dsl.functions.restriction.Restrictions;

public class SynchronousQueryDSL {

    private static char[] token = "my-token".toCharArray();
    private static String org = "my-org";

    public static void main(final String[] args) {

        InfluxDBClient influxDBClient = InfluxDBClientFactory.create("http://localhost:8086", token, org);

        Flux flux = Flux.from("my-bucket")
                .range(-30L, ChronoUnit.MINUTES)
                .filter(Restrictions.and(Restrictions.measurement().equal("cpu")))
                .limit(10);

        QueryApi queryApi = influxDBClient.getQueryApi();

        //
        // Query data
        //
        List<FluxTable> tables = queryApi.query(flux.toString());
        for (FluxTable fluxTable : tables) {
            List<FluxRecord> records = fluxTable.getRecords();
            for (FluxRecord fluxRecord : records) {
                System.out.println(fluxRecord.getTime() + ": " + fluxRecord.getValueByKey("_value"));
            }
        }

        influxDBClient.close();
    }
}
```

## Version

The latest version for Maven dependency:
```xml
<dependency>
  <groupId>com.influxdb</groupId>
  <artifactId>influxdb-client-java</artifactId>
  <version>6.8.0</version>
</dependency>
```
  
Or when using with Gradle:
```groovy
dependencies {
    implementation "com.influxdb:influxdb-client-java:6.8.0"
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

