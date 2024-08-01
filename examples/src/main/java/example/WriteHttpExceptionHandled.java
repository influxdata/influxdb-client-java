package example;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.exceptions.InfluxException;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.logging.Logger;

public class WriteHttpExceptionHandled {

  static Logger Log = Logger.getLogger(WriteHttpExceptionHandled.class.getName());

  public static String resolveProperty(final String property, final String fallback) {
    return System.getProperty(property, System.getenv(property)) == null
      ? fallback : System.getProperty(property, System.getenv(property));
  }

  private static final String influxUrl = resolveProperty("INFLUX_URL", "http://localhost:8086");
  private static final char[] token = resolveProperty("INFLUX_TOKEN","my-token").toCharArray();
  private static final String org = resolveProperty("INFLUX_ORG","my-org");
  private static final String bucket = resolveProperty("INFLUX_DATABASE","my-bucket");

  public static void main(String[] args) {

    InfluxDBClient influxDBClient = InfluxDBClientFactory.create(influxUrl, token, org, bucket);

    Log.info("\nWriting invalid records to InfluxDB reactively - check log SEVERE messages.\n");
    WriteApi writeApi = influxDBClient.makeWriteApi();

    // the following call will cause an HTTP 400 error, which will
    // include selected HTTP response headers in the error log
    writeApi.writeRecords(WritePrecision.MS, List.of("invalid", "clumsy", "broken", "unusable"));
    writeApi.close();

    Log.info("\nWriting invalid records to InfluxDB blocking - can handle caught InfluxException.\n");
    WriteApiBlocking writeApiBlocking = influxDBClient.getWriteApiBlocking();
    try {
      writeApiBlocking.writeRecord(WritePrecision.MS, "asdf");
    } catch (InfluxException e) {
      logInfluxException(e);
    }

    // Note when writing batches with one bad record:
    //    Cloud v3.x - The bad record is ignored.
    //    OSS   v2.x - returns exception
    Log.info("Writing Batch with 1 bad record.");
    Instant now = Instant.now();

    List<String> lpData = List.of(
      String.format("temperature,location=north value=60.0 %d", now.toEpochMilli()),
      String.format("temperature,location=south value=65.0 %d", now.minus(1, ChronoUnit.SECONDS).toEpochMilli()),
      String.format("temperature,location=north value=59.8 %d", now.minus(2, ChronoUnit.SECONDS).toEpochMilli()),
      String.format("temperature,location=south value=64.8 %d", now.minus(3, ChronoUnit.SECONDS).toEpochMilli()),
      String.format("temperature,location=north value=59.7 %d", now.minus(4, ChronoUnit.SECONDS).toEpochMilli()),
      "asdf",
      String.format("temperature,location=north value=59.9 %d", now.minus(6, ChronoUnit.SECONDS).toEpochMilli()),
      String.format("temperature,location=south value=64.9 %d", now.minus(7, ChronoUnit.SECONDS).toEpochMilli()),
      String.format("temperature,location=north value=60.1 %d", now.minus(8, ChronoUnit.SECONDS).toEpochMilli()),
      String.format("temperature,location=south value=65.1 %d", now.minus(9, ChronoUnit.SECONDS).toEpochMilli())
    );

    try {
      writeApiBlocking.writeRecords(WritePrecision.MS, lpData);
    } catch (InfluxException e) {
      logInfluxException(e);
    }

    Log.info("Done");
  }

  private static void logInfluxException(@Nonnull InfluxException e) {
    StringBuilder sBuilder = new StringBuilder().append("Handling InfluxException:\n");
    //Log.info("Message: " + e.getMessage());
    sBuilder.append("      ").append(e.getMessage());
    String headers = e.headers()
      .keySet()
      .stream()
      .reduce("\n", (set, key) -> set.concat(
        String.format("        %s: %s\n", key, e.headers().get(key)))
      );
    sBuilder.append("\n      HTTP Response Headers:");
    sBuilder.append(headers);
    Log.info(sBuilder.toString());
  }
}
