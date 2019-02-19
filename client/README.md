# influxdb-client-java

> This library is under development and no stable version has been released yet.  
> The API can change at any moment.

[![javadoc](https://img.shields.io/badge/javadoc-link-brightgreen.svg)](https://bonitoo-io.github.io/influxdb-client-java/influxdb-client-java/apidocs/index.html)

The reference Java client that allows query, write and management (bucket, organization, users) for the InfluxDB 2.0.

- [Features](#features)
- [Documentation](#documentation)
    - [Queries](#queries)
    - [Writes](#writes)
    - [Management API](#management-api)

## Features
 
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
         
## Documentation

### Queries

For querying data we use [QueryApi](https://bonitoo-io.github.io/influxdb-client-java/influxdb-client-java/apidocs/org/influxdata/client/QueryApi.html) that allow perform synchronous, asynchronous and also use raw query response.

#### Synchronous query

The synchronous query is not intended for large query results because the Flux response can be potentially unbound.

```java
package example;

import java.util.List;

import org.influxdata.client.InfluxDBClient;
import org.influxdata.client.InfluxDBClientFactory;
import org.influxdata.client.QueryApi;
import org.influxdata.query.FluxRecord;
import org.influxdata.query.FluxTable;

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

import org.influxdata.annotations.Column;
import org.influxdata.annotations.Measurement;
import org.influxdata.client.InfluxDBClient;
import org.influxdata.client.InfluxDBClientFactory;
import org.influxdata.client.QueryApi;

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

#### Asynchronous query

The Asynchronous query offers possibility to process unbound query and allow user to handle exceptions, 
stop receiving more results and notify that all data arrived.  

```java
package example;

import org.influxdata.client.InfluxDBClient;
import org.influxdata.client.InfluxDBClientFactory;
import org.influxdata.client.QueryApi;

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
            // cancelable - possibility to cancel query
            // fluxRecord - mapped FluxRecord
            //
            System.out.println(fluxRecord.getTime() + ": " + fluxRecord.getValueByKey("_value"));
            
        }, throwable -> {
            
            //
            // error handling while processing result
            //
            System.out.println("Error occurred: " + throwable.getMessage());
            
        }, () -> {
            
            //
            // on complete
            //
            System.out.println("Query completed");
            
        });

        influxDBClient.close();
    }
}
```

The asynchronous query offers a possibility map [FluxRecords](http://bit.ly/flux-spec#record) to POJO:

```java
package example;

import java.time.Instant;

import org.influxdata.annotations.Column;
import org.influxdata.annotations.Measurement;
import org.influxdata.client.InfluxDBClient;
import org.influxdata.client.InfluxDBClientFactory;
import org.influxdata.client.QueryApi;

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
            // cancelable - possibility to cancel query
            // temperature - mapped POJO measurement
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

#### Raw query

The Raw query allows direct processing original [CSV response](http://bit.ly/flux-spec#csv): 

```java
package example;

import org.influxdata.client.InfluxDBClient;
import org.influxdata.client.InfluxDBClientFactory;
import org.influxdata.client.QueryApi;

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

Asynchronous version allows processing line by line:

```java
package example;

import org.influxdata.client.InfluxDBClient;
import org.influxdata.client.InfluxDBClientFactory;
import org.influxdata.client.QueryApi;

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
            // cancelable - possibility to cancel query
            // line - line of the CSV response
            //
            System.out.println("Response: " + line);
        });

        influxDBClient.close();
    }
}
```

### Writes

#### Configuration

#### By Measurement

#### By Data Point

#### By LineProtocol

### Management API


