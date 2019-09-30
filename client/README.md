# influxdb-client-java

[![javadoc](https://img.shields.io/badge/javadoc-link-brightgreen.svg)](https://influxdata.github.io/influxdb-client-java/influxdb-client-java/apidocs/index.html)

The reference Java client that allows query, write and management (bucket, organization, users) for the InfluxDB 2.0.

## Features
 
- [Querying data using Flux language](#queries)
- [Writing data using](#writes)
    - [Line Protocol](#by-lineprotocol) 
    - [Data Point](#by-data-point) 
    - [POJO](#by-pojo)
    - [Default Tags](#default-tags)
- [InfluxDB 2.0 Management API](#management-api)
    - sources, buckets
    - tasks
    - authorizations
    - health check
- [Advanced Usage](#advanced-usage)
    - [Writing data using synchronous blocking API](#writing-data-using-synchronous-blocking-api)
    - [Client configuration file](#client-configuration-file)
    - [Client connection string](#client-connection-string)
    - [Gzip support](#gzip-support)
         
## Queries

For querying data we use [QueryApi](https://influxdata.github.io/influxdb-client-java/influxdb-client-java/apidocs/org/influxdata/client/QueryApi.html) that allow perform synchronous, asynchronous and also use raw query response.

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

    private static char[] token = "my_token".toCharArray();

    public static void main(final String[] args) throws Exception {

        InfluxDBClient influxDBClient = InfluxDBClientFactory.create("http://localhost:9999", token);

        String flux = "from(bucket:\"temperature-sensors\") |> range(start: 0)";

        QueryApi queryApi = influxDBClient.getQueryApi();

        //
        // Query data
        //
        List<FluxTable> tables = queryApi.query(flux, "org_id");
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

public class SynchronousQuery {

    private static char[] token = "my_token".toCharArray();

    public static void main(final String[] args) throws Exception {

        InfluxDBClient influxDBClient = InfluxDBClientFactory.create("http://localhost:9999", token);

        //
        // Query data
        //
        String flux = "from(bucket:\"temperature-sensors\") |> range(start: 0)";

        QueryApi queryApi = influxDBClient.getQueryApi();

        //
        // Map to POJO
        //
        List<Temperature> temperatures = queryApi.query(flux, "org_id", Temperature.class);
        for (Temperature temperature : temperatures) {
            System.out.println(temperature.location + ": " + temperature.value + " at " + temperature.time);
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

### Asynchronous query

The Asynchronous query offers possibility to process unbound query and allow user to handle exceptions, 
stop receiving more results and notify that all data arrived.  

```java
package example;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;

public class AsynchronousQuery {

    private static char[] token = "my_token".toCharArray();

    public static void main(final String[] args) throws Exception {

        InfluxDBClient influxDBClient = InfluxDBClientFactory.create("http://localhost:9999", token);

        //
        // Query data
        //
        String flux = "from(bucket:\"temperature-sensors\") |> range(start: 0)";

        QueryApi queryApi = influxDBClient.getQueryApi();

        queryApi.query(flux, "org_id", (cancellable, fluxRecord) -> {

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

public class AsynchronousQuery {

    private static char[] token = "my_token".toCharArray();

    public static void main(final String[] args) throws Exception {

        InfluxDBClient influxDBClient = InfluxDBClientFactory.create("http://localhost:9999", token);

        //
        // Query data
        //
        String flux = "from(bucket:\"temperature-sensors\") |> range(start: 0)";

        QueryApi queryApi = influxDBClient.getQueryApi();

        //
        // Map to POJO
        //
        queryApi.query(flux, "org_id", Temperature.class, (cancellable, temperature) -> {
            
            //
            // The callback to consume a FluxRecord mapped to POJO.
            //
            // cancelable - object has the cancel method to stop asynchronous query
            //
            System.out.println(temperature.location + ": " + temperature.value + " at " + temperature.time);
        });

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

### Raw query

The Raw query allows direct processing original [CSV response](http://bit.ly/flux-spec#csv): 

```java
package example;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;

public class RawQuery {

    private static char[] token = "my_token".toCharArray();

    public static void main(final String[] args) throws Exception {

        InfluxDBClient influxDBClient = InfluxDBClientFactory.create("http://localhost:9999", token);

        //
        // Query data
        //
        String flux = "from(bucket:\"temperature-sensors\") |> range(start: 0)";

        QueryApi queryApi = influxDBClient.getQueryApi();

        String csv = queryApi.queryRaw(flux, "org_id");

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

    private static char[] token = "my_token".toCharArray();

    public static void main(final String[] args) throws Exception {

        InfluxDBClient influxDBClient = InfluxDBClientFactory.create("http://localhost:9999", token);

        //
        // Query data
        //
        String flux = "from(bucket:\"temperature-sensors\") |> range(start: 0)";

        QueryApi queryApi = influxDBClient.getQueryApi();

        queryApi.queryRaw(flux, "org_id", (cancellable, line) -> {

            //
            // The callback to consume a line of CSV response
            //
            // cancelable - object has the cancel method to stop asynchronous query
            //
            System.out.println("Response: " + line);
        });

        influxDBClient.close();
    }
}
```

## Writes

For writing data we use [WriteApi](https://influxdata.github.io/influxdb-client-java/influxdb-client-java/apidocs/org/influxdata/client/WriteApi.html) that is an asynchronous non-blocking API and supports:

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
| **retryInterval** | the number of milliseconds to retry unsuccessful write. The retry interval is used when the InfluxDB server does not specify "Retry-After" header.| 1000 |
| **bufferLimit** | the maximum number of unwritten stored points | 10000 |
| **backpressureStrategy** | the strategy to deal with buffer overflow | DROP_OLDEST |

### Backpressure
The backpressure presents the problem of what to do with a growing backlog of unconsumed data points. 
The key feature of backpressure is to provide the capability to avoid consuming the unexpected amount of system resources.  
This situation is not common and can be caused by several problems: generating too much measurements in short interval,
long term unavailability of the InfluxDB server, network issues. 

The size of backlog is configured by 
`WriteOptions.bufferLimit` and backpressure strategy by `WriteOptions.backpressureStrategy`.

#### Strategy how react to backlog overflows
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

### Writing data

#### By POJO

Write Measurement into specified bucket:

```java
package example;

import java.time.Instant;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;

public class WritePOJO {

    private static char[] token = "my_token".toCharArray();

    public static void main(final String[] args) throws Exception {

        InfluxDBClient influxDBClient = InfluxDBClientFactory.create("http://localhost:9999", token);

        //
        // Write data
        //
        try (WriteApi writeApi = influxDBClient.getWriteApi()) {

            //
            // Write by POJO
            //
            Temperature temperature = new Temperature();
            temperature.location = "south";
            temperature.value = 62D;
            temperature.time = Instant.now();

            writeApi.writeMeasurement("bucket_name", "org_id", WritePrecision.NS, temperature);
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

#### By Data Point

Write Data point into specified bucket:

```java
package example;

import java.time.Instant;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import com.influxdb.client.write.Point;

public class WriteDataPoint {

    private static char[] token = "my_token".toCharArray();

    public static void main(final String[] args) throws Exception {

        InfluxDBClient influxDBClient = InfluxDBClientFactory.create("http://localhost:9999", token);

        //
        // Write data
        //
        try (WriteApi writeApi = influxDBClient.getWriteApi()) {

            //
            // Write by Data Point
            //
            Point point = Point.measurement("temperature")
                    .addTag("location", "west")
                    .addField("value", 55D)
                    .time(Instant.now().toEpochMilli(), WritePrecision.NS);

            writeApi.writePoint("bucket_name", "org_id", point);
        }

        influxDBClient.close();
    }
}
```

#### By LineProtocol

Write Line Protocol record into specified bucket:

```java
package example;

import java.time.temporal.ChronoUnit;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;

public class WriteLineProtocol {

    private static char[] token = "my_token".toCharArray();

    public static void main(final String[] args) throws Exception {

        InfluxDBClient influxDBClient = InfluxDBClientFactory.create("http://localhost:9999", token);

        //
        // Write data
        //
        try (WriteApi writeApi = influxDBClient.getWriteApi()) {

            //
            // Write by LineProtocol
            //
            String record = "temperature,location=north value=60.0";
            
            writeApi.writeRecord("bucket_name", "org_id", WritePrecision.NS, record);
        }

        influxDBClient.close();
    }
}
```

#### Default Tags

Sometimes is useful to store same information in every measurement e.g. `hostname`, `location`, `customer`. 
The client is able to use static value, system property or env property as a tag value.

The expressions:
- `California Miner` - static value
- `${version}` -  system property
- `${env.hostname}` - environment property

##### Via Configuration file

In a [configuration file](#client-configuration-file) you are able to specify default tags by `influx2.measurement` prefix.

```properties
influx2.tags.id = 132-987-655
influx2.tags.customer = California Miner
influx2.tags.hostname = ${env.hostname}
influx2.tags.sensor-version = ${version}
```

##### Via API

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

### Handle the Events

#### Handle the Success write

```java
WriteApi writeApi = influxDBClient.getWriteApi();
writeApi.listenEvents(WriteSuccessEvent.class, event -> {

    String data = event.getLineProtocol();

    //
    // handle success
    //
});
```

#### Handle the Error Write

```java
WriteApi writeApi = influxDBClient.getWriteApi();
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
| **/api/v2/authorizations** | Managing authorization data | [AuthorizationsApi](https://influxdata.github.io/influxdb-client-java/influxdb-client-java/apidocs/org/influxdata/client/AuthorizationsApi.html) |
| **/api/v2/buckets** | Managing bucket data | [BucketsApi](https://influxdata.github.io/influxdb-client-java/influxdb-client-java/apidocs/org/influxdata/client/BucketsApi.html) |
| **/api/v2/orgs** | Managing organization data | [OrganizationsApi](https://influxdata.github.io/influxdb-client-java/influxdb-client-java/apidocs/org/influxdata/client/OrganizationsApi.html) |
| **/api/v2/users** | Managing user data | [UsersApi](https://influxdata.github.io/influxdb-client-java/influxdb-client-java/apidocs/org/influxdata/client/UsersApi.html) |
| **/api/v2/sources** | Managing sources | [SourcesApi](https://influxdata.github.io/influxdb-client-java/influxdb-client-java/apidocs/org/influxdata/client/SourcesApi.html) |
| **/api/v2/tasks** | Managing one-off and recurring tasks | [TasksApi](https://influxdata.github.io/influxdb-client-java/influxdb-client-java/apidocs/org/influxdata/client/TasksApi.html) |
| **/api/v2/scrapers** | Managing ScraperTarget data | [ScraperTargetsApi](https://influxdata.github.io/influxdb-client-java/influxdb-client-java/apidocs/org/influxdata/client/ScraperTargetsApi.html) |
| **/api/v2/labels** | Managing resource labels | [LabelsApi](https://influxdata.github.io/influxdb-client-java/influxdb-client-java/apidocs/org/influxdata/client/LabelsApi.html) |
| **/api/v2/telegrafs** | Managing telegraf config data | [TelegrafsApi](https://influxdata.github.io/influxdb-client-java/influxdb-client-java/apidocs/org/influxdata/client/TelegrafsApi.html) |
| **/api/v2/setup** | Managing onboarding setup | [InfluxDBClient#onBoarding()](https://influxdata.github.io/influxdb-client-java/influxdb-client-java/apidocs/org/influxdata/client/InfluxDBClient.html#onBoarding-com.influxdb.client.domain.Onboarding-) |
| **/ready** | Get the readiness of an instance at startup| [InfluxDBClient#ready()](https://influxdata.github.io/influxdb-client-java/influxdb-client-java/apidocs/org/influxdata/client/InfluxDBClient.html#ready--) |
| **/health** | Get the health of an instance anytime during execution | [InfluxDBClient#health()](https://influxdata.github.io/influxdb-client-java/influxdb-client-java/apidocs/org/influxdata/client/InfluxDBClient.html#health--) |


The following example demonstrates how to use a InfluxDB 2.0 Management API. For further information see endpoints Javadoc.

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

    private static char[] token = "my_token".toCharArray();

    public static void main(final String[] args) throws Exception {

        InfluxDBClient influxDBClient = InfluxDBClientFactory.create("http://localhost:9999", token);

        //
        // Create bucket "iot_bucket" with data retention set to 3,600 seconds
        //
        RetentionRule retention = new RetentionRule();
        retention.setEverySeconds(3600L);

        Bucket bucket = influxDBClient.getBucketsApi().createBucket("iot_bucket", retention, "org_id");

        //
        // Create access token to "iot_bucket"
        //
        PermissionResource resource = new PermissionResource();
        resource.setId(bucket.getId());
        resource.setOrgID("org_id");
        resource.setType(PermissionResource.TypeEnum.BUCKETS);

        // Read permission
        Permission read = new Permission();
        read.setResource(resource);
        read.setAction(Permission.ActionEnum.READ);

        // Write permission
        Permission write = new Permission();
        write.setResource(resource);
        write.setAction(Permission.ActionEnum.WRITE);

        Authorization authorization = influxDBClient.getAuthorizationsApi()
                .createAuthorization("org_id", Arrays.asList(read, write));

        //
        // Created token that can be use for writes to "iot_bucket"
        //
        String token = authorization.getToken();

        influxDBClient.close();
    }
}
```

## Advanced Usage

### Writing data using synchronous blocking API

The [WriteApiBlocking](https://influxdata.github.io/influxdb-client-java/influxdb-client-java/apidocs/org/influxdata/client/WriteApiBlocking.html) provides a synchronous blocking API to writing data using [InfluxDB Line Protocol](https://docs.influxdata.com/influxdb/v1.6/write_protocols/line_protocol_tutorial/), Data Point and POJO. 

_It's up to user to handle a server or a http exception._

```java
package example;

import java.time.Instant;

import org.influxdata.annotations.Column;
import org.influxdata.annotations.Measurement;
import org.influxdata.client.InfluxDBClient;
import org.influxdata.client.InfluxDBClientFactory;
import org.influxdata.client.WriteApiBlocking;
import org.influxdata.client.domain.WritePrecision;
import org.influxdata.client.write.Point;
import org.influxdata.exceptions.InfluxException;

public class WriteDataPoint {

    private static char[] token = "my_token".toCharArray();

    public static void main(final String[] args) throws Exception {

        InfluxDBClient influxDBClient = InfluxDBClientFactory.create("http://localhost:9999", token);

        WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();
        
        try {
            //
            // Write by LineProtocol
            //
            String record = "temperature,location=north value=60.0";

            writeApi.writeRecord("bucket_name", "org_id", WritePrecision.NS, record);

            //
            // Write by Data Point
            //
            Point point = Point.measurement("temperature")
                    .addTag("location", "west")
                    .addField("value", 55D)
                    .time(Instant.now().toEpochMilli(), WritePrecision.NS);

            writeApi.writePoint("bucket_name", "org_id", point);

            //
            // Write by POJO
            //
            Temperature temperature = new Temperature();
            temperature.location = "south";
            temperature.value = 62D;
            temperature.time = Instant.now();

            writeApi.writeMeasurement("bucket_name", "org_id", WritePrecision.NS, temperature);

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

### Client configuration file

A client can be configured via configuration file. The configuration file has to be named as `influx2.properties` and has to be in root of classpath.

The following options are supported:

| Property name             | default   | description |
| --------------------------|-----------|-------------| 
| influx2.url               | -         | the url to connect to InfluxDB |
| influx2.org               | -         | default destination organization for writes and queries |
| influx2.bucket            | -         | default destination bucket for writes |
| influx2.token             | -         | the token to use for the authorization |
| influx2.logLevel          | NONE      | rest client verbosity level |
| influx2.readTimeout       | 10000 ms  | read timeout |
| influx2.writeTimeout      | 10000 ms  | write timeout |
| influx2.connectTimeout    | 10000 ms  | socket timeout |

The `influx2.readTimeout`, `influx2.writeTimeout` and `influx2.connectTimeout` supports `ms`, `s` and `m` as unit. Default is milliseconds.


##### Configuration example

```properties
influx2.url=http://localhost:9999
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

| Property name     | default   | description |
| ------------------|-----------|-------------| 
| org               | -         | default destination organization for writes and queries |
| bucket            | -         | default destination bucket for writes |
| token             | -         | the token to use for the authorization |
| logLevel          | NONE      | rest client verbosity level |
| readTimeout       | 10000 ms  | read timeout |
| writeTimeout      | 10000 ms  | write timeout |
| connectTimeout    | 10000 ms  | socket timeout |

The `readTimeout`, `writeTimeout` and `connectTimeout` supports `ms`, `s` and `m` as unit. Default is milliseconds.

### Gzip support
`InfluxDBClient` does not enable gzip compress for http requests by default. If you want to enable gzip to reduce transfer data's size, you can call:

```java
influxDBClient.enableGzip();
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

import java.util.List;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;
import com.influxdb.query.dsl.Flux;
import com.influxdb.query.dsl.functions.restriction.Restrictions;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;

public class SynchronousQuery {

    private static char[] token = "my_token".toCharArray();

    public static void main(final String[] args) throws Exception {

        InfluxDBClient influxDBClient = InfluxDBClientFactory.create("http://localhost:9999", token);

        Flux flux = Flux.from("temperature-sensors")
                .filter(Restrictions.and(Restrictions.field().equal("pressure")))
                .limit(10);

        QueryApi queryApi = influxDBClient.getQueryApi();

        //
        // Query data
        //
        List<FluxTable> tables = queryApi.query(flux.toString(), "org_id");
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
  <version>1.0.0</version>
</dependency>
```
  
Or when using with Gradle:
```groovy
dependencies {
    compile "com.influxdb:influxdb-client-java:1.0.0"
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

