package example;

import java.util.List;

import org.influxdata.flux.FluxClient;
import org.influxdata.flux.FluxClientFactory;
import org.influxdata.flux.domain.FluxRecord;
import org.influxdata.flux.domain.FluxTable;
import org.influxdata.flux.option.FluxConnectionOptions;

@SuppressWarnings("CheckStyle")
public class FluxClientSimpleExample {

    public static void main(final String[] args) {

        FluxConnectionOptions options = FluxConnectionOptions.builder()
            .url("http://localhost:8086/")
            .build();

        FluxClient fluxClient = FluxClientFactory.create(options);

        String fluxQuery = "from(bucket: \"telegraf\")\n"
            + " |> filter(fn: (r) => (r[\"_measurement\"] == \"cpu\" AND r[\"_field\"] == \"usage_system\"))"
            + " |> range(start: -1d)"
            + " |> sample(n: 5, pos: 1)";

        List<FluxTable> tables = fluxClient.query(fluxQuery);

        for (FluxTable fluxTable : tables) {
            List<FluxRecord> records = fluxTable.getRecords();
            for (FluxRecord fluxRecord : records) {
                System.out.println(fluxRecord.getTime() + ": " + fluxRecord.getValueByKey("_value"));
            }
        }

        fluxClient.close();
    }

}
