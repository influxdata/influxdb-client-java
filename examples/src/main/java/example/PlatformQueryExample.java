package example;

import java.util.List;

import org.influxdata.flux.Flux;
import org.influxdata.flux.domain.FluxRecord;
import org.influxdata.flux.domain.FluxTable;
import org.influxdata.platform.PlatformClientFactory;
import org.influxdata.platform.QueryClient;

public final class PlatformQueryExample {

    private PlatformQueryExample() {
    }

    public static void main(final String[] args) {
        QueryClient queryClient = PlatformClientFactory.create("http://localhost:9999").createQueryClient();

        List<FluxTable> query = queryClient.query(Flux.from("my-bucket").last().toString());

        for (FluxTable fluxTable : query) {
            List<FluxRecord> records = fluxTable.getRecords();
            for (FluxRecord fluxRecord : records) {
                System.out.println(fluxRecord);
            }
        }


    }
}
