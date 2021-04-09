# Apache Karaf Integration for InfluxDB 2.0

## Features

- [Installation](#installation)
- [Configuration](#configuration)
- [Examples](#examples)

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

Install KAR Maven artifact `mvn:com.influxdb/influxdb-karaf-kar/VERSION/kar` (replace `VERSION` with current version:

```
karaf@root()> kar:install mvn:com.influxdb/influxdb-karaf-kar/VERSION/kar
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

### Write RAW (line protocol) record via Karaf console

```
karaf@root()> event:send influxdb/lineprotocol "record=weather,location=HU temperature=5.89,humidity=48"
```

### Write structured data by custom sources

```java
@Component
public class Tester {

    @Reference
    EventAdmin eventAdmin;

    public void tester() {
        final Map<String, Object> data = new TreeMap<>();
        data.put("location", "HU");
        data.put("temperature", 5.89);
        data.put("humidity", 48);

        eventAdmin.postEvent(new Event("influxdb/point/weather", Collections.singletonMap("point", data)));
    }
}
```