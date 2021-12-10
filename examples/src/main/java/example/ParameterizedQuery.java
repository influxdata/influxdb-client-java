package example;

import java.time.Instant;
import java.time.Period;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxTable;

public class ParameterizedQuery {

    public static void main(String[] args) {

        String url = System.getenv("INFLUX_URL");
        String token = System.getenv("INFLUX_TOKEN");
        String org = System.getenv("INFLUX_ORG");
        String bucket = System.getenv("INFLUX_BUCKET");

        InfluxDBClient client = InfluxDBClientFactory.create(url,
            token.toCharArray(),
            org, bucket);

        WriteApi writeApi = client.makeWriteApi();

        Instant yesterday = Instant.now().minus(Period.ofDays(1));

        Point p = Point.measurement("temperature")
            .addTag("location", "north")
            .addField("value", 60.0)
            .time(yesterday, WritePrecision.NS);

        writeApi.writePoint(p);

        writeApi.close();

        //
        // Query range start parameter using Instant
        //
        QueryApi queryApi = client.getQueryApi();
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
        params = new HashMap<>();
        params.put("bucketParam", bucket);
        params.put("startParam", "-1d10s");
        parametrizedQuery = "from(bucket: params.bucketParam) |> range(start: duration(v: params.startParam))";
        query = queryApi.query(parametrizedQuery, org, params);
        query.forEach(fluxTable -> fluxTable.getRecords()
            .forEach(r -> System.out.println(r.getTime() + ": " + r.getValueByKey("_value"))));

        client.close();

    }
}
