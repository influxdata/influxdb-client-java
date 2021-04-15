# Apache Karaf Integration for InfluxDB 2.0

## Features

- [Submodules](#submodules)
- [Installation](#installation)
- [Configuration](#configuration)
- [Examples](#examples)

## Submodules

The Apache Karaf feature definition modules supporting InfluxDB 2.0:

| Module | Description |
| --- | --- |
| **karaf-features** | Apache Karaf feature definition (XML) artifact for InfluxDB 2.0. |
| **karaf-kar**      | KAraf aRchive (KAR) artifact containing client for InfluxDB 2.0. |
| **karaf-assembly** | Apache Karaf (sample) distribution having InfluxDB 2.0 client. |

## Installation

Apache Karaf Integration can be enabled by installing Karaf feature or KAR file.

### Preparing

Download [Apache Karaf](http://karaf.apache.org/download.html) 4.2+, extract to directory referenced by `KARAF_HOME` below and start container:

```
$KARAF_HOME/bin/karaf
```

### Karaf feature installation

Add feature repository (`mvn:com.influxdb/influxdb-karaf-features/VERSION/xml/features`) and install feature named `influxdb-client` (replace `VERSION` with current version).

```
karaf@root()> feature:repo-add mvn:com.influxdb/influxdb-karaf-features/VERSION/xml/features
karaf@root()> feature:install influxdb-client
```

Feature repository can be referenced by custom Karaf distributions too.

### KAR deployment

Install KAR Maven artifact `mvn:com.influxdb/influxdb-karaf-kar/VERSION/kar` (replace `VERSION` with current version):

```
karaf@root()> kar:install mvn:com.influxdb/influxdb-karaf-kar/VERSION/kar
```

### Sample Apache Karaf distribution

A sample Apache Karaf distribution is created automatically by compiling karaf-assembly Maven module. ZIP and TGZ packages can be found in `karaf-assembly/target/` directory. OSGi component developers using InfluxDB do not need to download Apache Karaf and complete installation process (above) but unpack archive only and start the server.

An alternate way to start the server is running `karaf-assembly/target/assembly/bin/karaf`. Installed features and bundles can be checked by the following commands:

```
karaf@root()> feature:list | grep influx
influxdb-client                      │ 2.2.0.SNAPSHOT   │          │ Started     │ influxdb-features-2.2.0-SNAPSHOT     │ InfluxDB client
kotlin                               │ 1.3.72           │          │ Started     │ influxdb-features-2.2.0-SNAPSHOT     │ Kotlin
influxdb-karaf-features              │ 2.2.0.SNAPSHOT   │          │ Uninstalled │ influxdb-features-2.2.0-SNAPSHOT     │ Apache Karaf Features for InfluxDB 2.0
karaf@root()> bundle:list | grep -i influx
16 │ Active │  80 │ 2.2.0.SNAPSHOT │ The OSGi InfluxDB 2.0 Client
```

Declarative services (i.e. that are writing data to InfluxDB by OSGi events) become available if you copy configuration files (see below) to `karaf-assembly/target/assembly/deploy` directory.

```
karaf@root()> scr:list 
com.influxdb.client.osgi.LineProtocolWriter in bundle 16 (com.influxdb.client-osgi:2.2.0.SNAPSHOT) enabled, 1 instance.
    Id: 3, State:ACTIVE, PID(s): [com.influxdb.client.osgi.LineProtocolWriter.7617b1fb-5b49-402b-b794-1bab69bf6136]
com.influxdb.client.osgi.InfluxDBConnector in bundle 16 (com.influxdb.client-osgi:2.2.0.SNAPSHOT) enabled, 1 instance.
    Id: 1, State:ACTIVE, PID(s): [com.influxdb.client.osgi.InfluxDBConnector.1e27ec4c-4dcb-4545-a2ec-40405877a409]
com.influxdb.client.osgi.PointWriter in bundle 16 (com.influxdb.client-osgi:2.2.0.SNAPSHOT) enabled, 1 instance.
```

## Configuration

Configuration options are described by [client-osgi](../client-osgi/README.md). Create configurations by hand (`*.cfg` files, copy them to `KARAF_HOME/deploy` directory) or via web console (need to install `webconsole` Karaf feature).

## Examples

### Preparing

Install Apache Karaf Integration as described above. Create the following configuration files (replace connection properties):

- `KARAF_HOME/deploy/com.influxdb.client.osgi.InfluxDBConnector-test.cfg`
```
alias=test
url=http://localhost:8086
token=TOKEN
organization=example
bucket=test
```

- `KARAF_HOME/deploy/com.influxdb.client.osgi.LineProtocolWriter-tester.cfg`
```
client.target=(alias=test)
timestamp.append=true
```

- `KARAF_HOME/deploy/com.influxdb.client.osgi.PointWriter-tester.cfg`
```
event.topics=influxdb/point/*
client.target=(alias=test)
host.name.add=true
host.address.add=true
timestamp.add=true
timestamp.precision=ns
```

### Write RAW (line protocol) record

Publish OSGi event containing line protocol record (i.e. via Karaf console).

```
karaf@root()> event:send influxdb/lineprotocol "record=weather,location=HU temperature=5.89,humidity=48"
```

### Write structured data

Publish OSGi event containing structured data (i.e. by custom component). Both InfluxDB Point and Java Map types are supported (see [client-osgi](../client-osgi/README.md) and [javadoc](https://influxdata.github.io/influxdb-client-java/influxdb-client-osgi/apidocs/index.html) for details).

```java
@Component
public class Tester {

    @Reference
    EventAdmin eventAdmin;

    public void tester() {
        final Map<String, String> tags = new TreeMap<>();
        tags.put("location", "HU");

        final Map<String, Object> fields = new TreeMap<>();
        fields.put("temperature", 5.89);
        fields.put("humidity", 48);
        fields.put("raining", false);
        fields.put("sky", "CLOUDY");

        final Map<String, Object> point = new TreeMap<>();
        point.put("tags", tags);
        point.put("fields", fields);

        eventAdmin.postEvent(new Event("influxdb/point/weather", Collections.singletonMap("point", point)));
    }
}
```