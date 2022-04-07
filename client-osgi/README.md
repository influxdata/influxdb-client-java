# OSGi Client Bundle for InfluxDB 2.x

[![javadoc](https://img.shields.io/badge/javadoc-link-brightgreen.svg)](https://influxdata.github.io/influxdb-client-java/influxdb-client-osgi/apidocs/index.html)

OSGi (R6) client bundle contains InfluxDB 2.x client and dependencies excluding Kotlin and JSR-305 (FindBugs).

## Services

The following declarative services are implemented by OSGi Client Bundle: InfluxDBConnector, LineProtocolWriter and PointWriter.

See [JavaDoc](https://influxdata.github.io/influxdb-client-java/influxdb-client-osgi/apidocs/index.html) for details.

## Configuration

Metadata are generated for declarative services, so administration UIs (i.e. Apache Karaf [web console](https://karaf.apache.org/manual/latest/webconsole) or [Hawtio](https://hawt.io/)) support specifying configurations in a type safe way:

- com.influxdb.client.osgi.InfluxDBConnector
- com.influxdb.client.osgi.LineProtocolWriter
- com.influxdb.client.osgi.PointWriter

See [JavaDoc](https://influxdata.github.io/influxdb-client-java/influxdb-client-osgi/apidocs/index.html) for details.
